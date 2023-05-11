package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.AlarmService;
import com.yjh.platform.common.mqtt.FtpsService;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.patrol.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.service.TWarnInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static com.yjh.platform.module.patrol.CruiseConstant.CRUISE_ABNORMAL_ABNORMALALARM;
import static com.yjh.platform.module.patrol.CruiseConstant.CRUISE_RESULT_ABNORMAL;
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
    private FtpsService ftpsService;
    private AlarmService alarmService;
    private PatrolResultHandler patrolResultHandler;
    private ApplicationProperties applicationProperties;
    private TStdRegionDao tStdRegionDao;
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    private TStdDeviceDao tStdDeviceDao;

    public IsWarnAfterCruiseThread(Map<String, String> threadMap, RedisTemplate redisTemplate) {
        this.threadMap = threadMap;
        this.redisTemplate = redisTemplate;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        ftpsService = StaticContextAccessor.getBean(FtpsService.class);
        alarmService = StaticContextAccessor.getBean(AlarmService.class);
        this.patrolResultHandler = StaticContextAccessor.getBean(PatrolResultHandler.class);
        this.tStdRegionDao = StaticContextAccessor.getBean(TStdRegionDao.class);
        this.tCruisePointInstanceDao = StaticContextAccessor.getBean(TCruisePointInstanceDao.class);
        this.tStdDeviceDao = StaticContextAccessor.getBean(TStdDeviceDao.class);
        this.applicationProperties = StaticContextAccessor.getBean(ApplicationProperties.class);
    }

    @Override
    public void run() {
        try {
            log.info("开始判断巡视结果是否告警 >>>>>>> threadMap==={}", threadMap);
            String taskId = threadMap.get("taskCode");

            Long instanceId = NumberUtils.toLong(threadMap.get("instanceId"));
            TStdDeviceMete tStdDevicemete = uPatrolTaskService.selectDeviceMeteInfo(instanceId);

            UPatrolTask uPatrolTask = uPatrolTaskService.selectByPrimaryId(taskId);
            log.info("taskId is {} uPatrolTask is: {}", taskId, uPatrolTask);

            // 该巡视点无了,找不到对应
            if (Objects.isNull(tStdDevicemete)) {
                return;
            }

            Map<String, String> initInfo = new HashMap<>(16);
            initInfo.put("value", threadMap.get("value"));

            String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"));
            boolean isTemDif = StringUtils.equals("2", sysLevel)
                    && 1 == tStdDevicemete.getIsTemdif()
                    && Objects.equals("222", tStdDevicemete.getMeteType());
            initInfo.put("isTemDif", String.valueOf(isTemDif));
            if (isTemDif) {
                // 配置了红外温差任务用差值去判断告警
                String temperature = String.valueOf(redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("temperature", ""));
                if (!CommonUtils.isEmptyOrNullstr(temperature)) {
                    double abs = Math.abs(Double.parseDouble(temperature) - Double.parseDouble(threadMap.get("value")));
                    String valueTemp = new DecimalFormat("#0.00").format(Double.valueOf(abs));
                    String temperatureTemp = new DecimalFormat("#0.00").format(Double.valueOf(temperature));

                    initInfo.put("valueTemp", valueTemp);
                    initInfo.put("temperature", temperatureTemp);
                    initInfo.put("warnName", tStdDevicemete.getMeteName() + "温差任务");
                    initInfo.put("warnContent", "传感器环境温度与测温产生温差:环境" + temperatureTemp + "--测温" + threadMap.get("value") + "--温差" + valueTemp);
                    initInfo.put("outRange", String.valueOf(abs));
                }
            }

            Map<String, Object> params = new HashMap<>(16);
            params.put("value", initInfo.get("valueTemp"));
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
            alarmStoreAndHandler(map, taskId, tStdDevicemete, instanceId, initInfo);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 存储告警信息及其他处理
     *
     * @param map            根据告警规则判断的结果
     * @param taskId         任务id
     * @param tStdDevicemete 测点信息
     * @param instanceId     巡视点id
     * @param initInfo       温差任务告警信息
     */
    private void alarmStoreAndHandler(Map<String, Object> map, String taskId, TStdDeviceMete tStdDevicemete, Long instanceId, Map<String, String> initInfo) {
        log.info("An alarm is generated！！！");
        log.info("initInfo=={}", initInfo);
        boolean isTemDif = Boolean.parseBoolean(initInfo.get("isTemDif"));
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
            warnInfo.setImagePath(threadMap.getOrDefault("relativePath", ""));
            warnInfo.setValue(threadMap.get("value"));
            warnInfo.setTaskId(taskId);
            warnInfo.setDeviceCode(tStdDeviceDao.selectByUnionKeys(tStdDevicemete.getDeviceId()).getDeviceCode());
            warnInfo.setWarnName(isTemDif ? initInfo.get("warnName") : String.valueOf(map.get("warnName")));
            warnInfo.setWarnLevel(Integer.valueOf(String.valueOf(map.get("warnLevel"))));
            warnInfo.setWarnContent(isTemDif ? initInfo.get("warnContent") : String.valueOf(map.get("warnContent")));
            warnInfo.setOutRange(isTemDif ? initInfo.get("outRange") : Objects.nonNull(map.get("outRange")) ? String.valueOf(map.get("outRange")) : null);
            log.info("warnInfo==={}", JSON.toJSONString(warnInfo));
            StaticContextAccessor.getBean(TWarnInfoService.class).insert(warnInfo);

            redisTemplate.opsForHash().put(PATROL_TASK_PREFIX + taskId + ":" + instanceId, "cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
            redisTemplate.opsForHash().put(PATROL_TASK_PREFIX + taskId + ":" + instanceId, "cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM));
            redisTemplate.opsForHash().put(PATROL_TASK_PREFIX + taskId + ":" + instanceId, "resultDesc", isTemDif ? initInfo.get("valueTemp") : initInfo.get("value"));
            redisTemplate.opsForHash().put(PATROL_TASK_PREFIX + taskId + ":" + instanceId, "isWarn", "1");


            // newAlarm
            StaticContextAccessor.getBean(PatrolResultHandler.class).pushAlarmInfo(warnInfo.getWarnName(), warnInfo.getWarnContent());

            // 将告警信息放入redis
            Map<String, String> warnMap = putWarnToRedis(taskId, warnInfo);

            // 告警推送
            Map<String, String> infoMap = new HashMap<>(5);
            infoMap.put("alarmLevel", String.valueOf(warnInfo.getWarnLevel()));
            infoMap.put("flag", "warn");
            infoMap.put("defectModel", String.valueOf(warnInfo.getDefectModel()));
            infoMap.put("warnId", String.valueOf(warnInfo.getWarnId()));
            StaticContextAccessor.getBean(PatrolResultHandler.class).alarmPopUp(tStdDevicemete, infoMap);

            // 将产生的告警上送至上一级系统
            patrolResultHandler.alarmToUpSystem(warnInfo, taskId, instanceId);

            // 将产生的告警上送到算法管理平台
            alarmToAmPlatform(warnMap);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将产生的告警上送到算法管理平台
     * 这不是我写的 我从上个人那边copy的
     *
     * @param warnMap 告警信息
     */
    private void alarmToAmPlatform(Map<String, String> warnMap) {
        try {
            log.info("开始与算法管理平台交互");
            Alarm alarm = new Alarm();
            alarm.setBay_name(warnMap.get(""));
            alarm.setTime(warnMap.get("warnTime"));
            //获取原始路径
            String year = Integer.toString(LocalDate.now().getYear());
            String month = Integer.toString(LocalDate.now().getMonthValue());
            String taskidbak = warnMap.get("taskId");
            String instanceIdbak = warnMap.get("instanceId");
            Map<String, Object> cruiseResult2 = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskidbak + ":" + instanceIdbak);
            String devicename = threadMap.get("deviceName");
            alarm.setDevice_name(devicename);
            String origpicpath = cruiseResult2.get("origpic").toString();
            String[] str2 = origpicpath.split("/");
            String origpcimagename = str2[str2.length - 1];
            String remoteorigfilepath = applicationProperties.getManagerMqttConfig().getManagerServerFtpsRemotePath() + "/" + "different" + "/" + year + "/" + month + "/" + origpcimagename;
            //结果文件
            String resultImagebak = threadMap.get("absolutePath");
            String[] str3 = resultImagebak.split("/");
            //获取结果图名称，然后拼接远程文件全路径
            String resultimagename = str3[str3.length - 1];
            String remoteresultfilepath = applicationProperties.getManagerMqttConfig().getManagerServerFtpsRemotePath() + "/" + "different" + "/" + year + "/" + month + "/" + resultimagename;
            //原图  算法管理平台对应的原始文件路径
            alarm.setPic_raw(remoteresultfilepath);
            //机器人分析结果图 算法管理平台对应的原始文件路径
            alarm.setPic_different(remoteorigfilepath);
            //原始图片上传
            ftpsService.uploadFile("判别告警", origpicpath, remoteorigfilepath);
            //判别结果图片
            ftpsService.uploadFile("判别告警", resultImagebak, remoteresultfilepath);

            if (!ftpsService.fileExits(remoteorigfilepath)) {
                //原始图片上传
                ftpsService.uploadFile("判别告警", origpicpath, remoteorigfilepath);
            }
            if (!ftpsService.fileExits(remoteorigfilepath)) {
                //判别结果图片
                ftpsService.uploadFile("判别告警", resultImagebak, remoteresultfilepath);
            }
            alarmService.PushMsg(alarm);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将告警信息放入redis
     *
     * @param taskId   任务id
     * @param warnInfo 告警信息
     * @return Map<String, String>
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
        } catch (Exception e) {
            log.error("将告警信息放入redis异常：", e);
        }
        return warnMap;
    }
}
