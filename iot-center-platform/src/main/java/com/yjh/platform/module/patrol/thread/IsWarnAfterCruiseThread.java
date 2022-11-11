package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.AlarmService;
import com.yjh.platform.common.mqtt.FtpsService;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.configuration.UpFtpsConfig;
import com.yjh.platform.module.patrol.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.service.ProcessResultToUpSystem;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.service.TWarnInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * @author YC
 * @date 2020/12/18 15:38
 * 机器人的巡视结果再做告警判断线程
 */
@Slf4j
public class IsWarnAfterCruiseThread implements Runnable {

    private Map<String, String> threadMap;
    private RedisTemplate redisTemplate;
    private UPatrolTaskService uPatrolTaskService;
    private FtpsService ftpsservice;
    private AlarmService alarmService;

    public IsWarnAfterCruiseThread(Map<String, String> threadMap, RedisTemplate redisTemplate) {
        this.threadMap = threadMap;
        this.redisTemplate = redisTemplate;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        ftpsservice = StaticContextAccessor.getBean(FtpsService.class);
        alarmService = StaticContextAccessor.getBean(AlarmService.class);
    }

    @Override
    public void run() {
        try {
            log.info("开始判断巡视结果是否告警 >>>>>>> threadMap==={}", threadMap);
            String taskId = threadMap.get("taskCode");
            String robotCode = threadMap.get("robotCode");

            String robotTaskId = taskId;
            UPatrolTask uPatrolTask = uPatrolTaskService.selectByPrimaryId(taskId);
            log.info("taskId is {} uPatrolTask is: {}", taskId, uPatrolTask);

            String redisKey = "Robot_SPAndIN_Info:" + robotCode + ":" + robotTaskId + ":" + threadMap.get("deviceId");
            Map<String,String> robotInfoKeyMap = redisTemplate.opsForHash().entries(redisKey);
            Long instanceId = Long.valueOf(robotInfoKeyMap.get("instanceId"));
            TStdDeviceMete tStdDevicemete = uPatrolTaskService.selectDeviceMeteInfo(instanceId);

            if (Objects.isNull(tStdDevicemete)){
                return;
            }
            // 该巡视点还在,能找到对应
            Map<String, Object> params = new HashMap<>(16);
            params.put("value", threadMap.get("value"));
            params.put("stdDeviceMeteName", tStdDevicemete.getMeteName());
            params.put("meteKind", tStdDevicemete.getMeteKind());
            params.put("alarmState", tStdDevicemete.getAlarmState());
            params.put("stateZero", tStdDevicemete.getStateZero());
            params.put("stateOne", tStdDevicemete.getStateOne());
            params.put("alarmLevel", tStdDevicemete.getAlarmLevel());
            params.put("highLimit1", tStdDevicemete.getHighLimit1());
            params.put("lowLimit1", tStdDevicemete.getLowLimit1());
            params.put("highLimit2", tStdDevicemete.getHighLimit2());
            params.put("lowLimit2", tStdDevicemete.getLowLimit2());
            params.put("highLimit3", tStdDevicemete.getHighLimit3());
            params.put("lowLimit3", tStdDevicemete.getLowLimit3());
            params.put("highLimit4", tStdDevicemete.getHighLimit4());
            params.put("lowLimit4", tStdDevicemete.getLowLimit4());

            Result result = StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(Constant.WARN_JUDGE, Result.class, params);
            Map<String, Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
            log.info("object转map的东西==={}", map);
            Boolean isWarN = (Boolean) map.get("isWarn");

            if (Boolean.FALSE.equals(isWarN)) {
                return;
            }
            alarmStoreAndHandler(map, taskId, tStdDevicemete, instanceId);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 存储告警信息及其他处理
     *
     * @param map            根据告警规则判断的结果
     * @param taskId         任务id
     * @param tStdDevicemete 测点信息
     * @param instanceId     巡视点id
     */
    private void alarmStoreAndHandler(Map<String, Object> map, String taskId, TStdDeviceMete tStdDevicemete, Long instanceId) {
        log.info("An alarm is generated！！！");
        TWarnInfo warnInfo = new TWarnInfo();
        try {
            warnInfo.setWarnTime(DateTimeUtil.parse(threadMap.get("time")));
            warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
            warnInfo.setCunstomId(tStdDevicemete.getCustomId());
            warnInfo.setInstanceId(instanceId);
            warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
            warnInfo.setConfMode(276);
            warnInfo.setDefectModel(Integer.valueOf(uPatrolTaskService.selectDictCodeByNote("其他", "defect_model")));
            warnInfo.setAlarmSource(282);
            warnInfo.setImagePath(threadMap.get("relativePath"));
            warnInfo.setValue(threadMap.get("value"));
            warnInfo.setTaskId(taskId);
            warnInfo.setDeviceCode(String.valueOf(uPatrolTaskService.selectRobotInfoByCode(threadMap.get("robotCode")).getRobotId()));
            warnInfo.setWarnName(String.valueOf(map.get("warnName")));
            warnInfo.setWarnLevel(Integer.valueOf(String.valueOf(map.get("warnLevel"))));
            warnInfo.setWarnContent(String.valueOf(map.get("warnContent")));
            warnInfo.setOutRange(Objects.nonNull(map.get("outRange"))? String.valueOf(map.get("outRange")) : null);
            log.info("warnInfo==={}", JSON.toJSONString(warnInfo));
            StaticContextAccessor.getBean(TWarnInfoService.class).insert(warnInfo);

            StaticContextAccessor.getBean(PatrolResultHandler.class).pushAlarmInfo(warnInfo.getWarnName(), warnInfo.getWarnContent());

            // 将告警信息放入redis
            Map<String, String> warnMap = putWarnToRedis(taskId, warnInfo);

            // 告警推送
            Map<String, String> infoMap = new HashMap<>(5);
            infoMap.put("alarmLevel", String.valueOf(warnInfo.getWarnLevel()));
            infoMap.put("flag", "warn");
            infoMap.put("defectModel", String.valueOf(warnInfo.getDefectModel()));
            StaticContextAccessor.getBean(PatrolResultHandler.class).alarmPopUp(tStdDevicemete, infoMap);

            // 将产生的告警上送至上一级系统
            alarmToUpSystem(warnInfo, taskId, instanceId);

            // 将产生的告警上送到算法管理平台
            alarmToAmPlatform(warnMap);

        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将产生的告警上送到算法管理平台
     * 这不是我写的 我从上个人那边copy的
     *
     * @param warnMap 告警信息
     */
    private void alarmToAmPlatform (Map<String, String> warnMap) {
        try{
            log.info("开始与算法管理平台交互");
            String flag = ftpsservice.getFlag();
            if (StringUtils.equals("1", flag)) {
                Alarm alarm = new Alarm();
                alarm.setBay_name(warnMap.get(""));
                alarm.setTime(warnMap.get("warnTime"));
                //获取原始路径
                String year = Integer.toString(LocalDate.now().getYear());
                String month = Integer.toString(LocalDate.now().getMonthValue());
                String taskidbak = warnMap.get("taskId");
                String instanceIdbak = warnMap.get("instanceId");
                Map<String, Object> cruiseResult2 = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskidbak + ":" + instanceIdbak);
                String devicename= threadMap.get("deviceName");
                alarm.setDevice_name(devicename);
                String origpicpath = cruiseResult2.get("origpic").toString();
                String[] str2 = origpicpath.split("/");
                String origpcimagename = str2[str2.length - 1];
                String remoteorigfilepath = ftpsservice.getFtpsRemotePath() + "/" + "different" + "/" + year + "/" + month + "/" + origpcimagename;
                //结果文件
                String resultImagebak = threadMap.get("absolutePath");
                String[] str3 = resultImagebak.split("/");
                //获取结果图名称，然后拼接远程文件全路径
                String resultimagename = str3[str3.length - 1];
                String remoteresultfilepath = ftpsservice.getFtpsRemotePath() + "/" + "different" + "/" + year + "/" + month + "/" + resultimagename;
                //原图  算法管理平台对应的原始文件路径
                alarm.setPic_raw(remoteresultfilepath);
                //机器人分析结果图 算法管理平台对应的原始文件路径
                alarm.setPic_different(remoteorigfilepath);
                //原始图片上传
                ftpsservice.uploadFile("判别告警", origpicpath, remoteorigfilepath);
                //判别结果图片
                ftpsservice.uploadFile("判别告警", resultImagebak, remoteresultfilepath);

                if( !ftpsservice.fileExits(remoteorigfilepath)){
                    //原始图片上传
                    ftpsservice.uploadFile("判别告警", origpicpath, remoteorigfilepath);
                }
                if( !ftpsservice.fileExits(remoteorigfilepath)){
                    //判别结果图片
                    ftpsservice.uploadFile("判别告警", resultImagebak, remoteresultfilepath);
                }
                alarmService.PushMsg(alarm);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将告警信息放入redis
     *
     * @param taskId 任务id
     * @param warnInfo 告警信息
     * @return  Map<String, String>
     */
    private Map<String, String> putWarnToRedis(String taskId, TWarnInfo warnInfo) {
        String warnName = "warnInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
        Map<String, String> warnMap = new HashMap<>(16);
        try {
            warnMap.put("deviceId", String.valueOf(warnInfo.getDeviceId()));
            warnMap.put("customId", warnInfo.getCunstomId());
            warnMap.put("instanceId", String.valueOf(warnInfo.getInstanceId()));
            warnMap.put("stdMeteId", String.valueOf(warnInfo.getStdMeteId()));
            warnMap.put("taskId", warnInfo.getTaskId());
            warnMap.put("value", warnInfo.getValue());
            warnMap.put("imagePath", warnInfo.getImagePath());
            warnMap.put("confMode", "276");
            warnMap.put("alarmSource", String.valueOf(warnInfo.getAlarmSource()));
            warnMap.put("defectModel", String.valueOf(warnInfo.getDefectModel()));
            warnMap.put("warnLevel", String.valueOf(warnInfo.getWarnLevel()));
            warnMap.put("warnName", warnInfo.getWarnName());
            warnMap.put("warnTime", new SimpleDateFormat().format(warnInfo.getWarnTime()));
            warnMap.put("warnContent", warnInfo.getWarnContent());
            warnMap.put("outRange", Objects.nonNull(warnInfo.getOutRange()) ? warnInfo.getOutRange() : "");
            log.info("warnMap==={}", warnMap);
            redisTemplate.opsForHash().putAll(warnName, warnMap);
        }catch (Exception e){
            log.error("将告警信息放入redis异常：", e);
        }
        return warnMap;
    }

    /**
     * 将产生的告警上送至上级系统
     *
     * @param warnInfo 告警信息
     * @param taskId 任务id
     * @param instanceId 巡视点id
     */
    private void alarmToUpSystem(TWarnInfo warnInfo, String taskId, Long instanceId){
        try{
            String alarmLevel = "";
            switch (warnInfo.getWarnLevel()){
                case 130:
                    alarmLevel = "1";
                    break;
                case 131:
                    alarmLevel = "2";
                    break;
                case 132:
                    alarmLevel = "3";
                    break;
                case 133:
                    alarmLevel = "4";
                    break;
                default:
                    break;
            }

            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            StaticContextAccessor.getBean(ProcessResultToUpSystem.class).alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, warnInfo);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Redis数据库批量查询Key值游标
     *
     * @param key redis的key
     * @return Set<String>
     */
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });


    }
}
