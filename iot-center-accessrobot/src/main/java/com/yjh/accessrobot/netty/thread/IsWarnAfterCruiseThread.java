package com.yjh.accessrobot.netty.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.TCruiseTask;
import com.yjh.accessrobot.module.command.entity.TStdDeviceMete;
import com.yjh.accessrobot.module.command.entity.TWarnInfo;
import com.yjh.accessrobot.module.command.service.RobotService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author YC
 * @date 2020/12/18 15:38
 * 机器人的巡视结果再做告警判断线程
 */
@lombok.extern.slf4j.Slf4j
public class IsWarnAfterCruiseThread implements Runnable{

    private Map<String,String> threadMap;

    private RedisTemplate redisTemplate;
    private String webSocketUrl;

    public IsWarnAfterCruiseThread(Map<String,String> threadMap, RedisTemplate redisTemplate,String webSocketUrl){
        this.threadMap = threadMap;
        this.redisTemplate = redisTemplate;
        this.webSocketUrl = webSocketUrl;
    }

    @Override
    public void run() {
        try {
            log.info("开始处理巡检结果并判断是否告警 >>>>>>> threadMap==={}",threadMap);
            String taskId = threadMap.get("taskCode");

            String robotCode = threadMap.getOrDefault("robotCode", "");
            if (StringUtils.isEmpty(robotCode)) {
                log.error("机器人编码为空");
                throw new RuntimeException("机器人编码为空");
            }

            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:"+ robotCode + ":" +taskId);
            //根据taskId查询相关内容
            TCruiseTask tCruiseTask = StaticContextAccessor.getBean(RobotService.class).selectTCruiseTask(taskId);
            log.info("taskId是: {}的任务数据tCruiseTask是: {}",taskId,tCruiseTask);

            if (Objects.isNull(tCruiseTask.getTaskType())) {
                for (String key : robotInfoKeys) {
                    Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                    if (Objects.equals(robotCode,redisInfoMap.get("robotCode"))
                            && Objects.equals(taskId,redisInfoMap.get("taskId"))
                            && Objects.equals(threadMap.get("deviceId"),redisInfoMap.get("inspectionCode"))) {
                        Long instanceId = Long.valueOf(redisInfoMap.get("instanceId"));
                        TStdDeviceMete tStdDevicemete = StaticContextAccessor.getBean(RobotService.class).selectDeviceMeteInfo(instanceId);

                        //该巡视点还在,能找到对应测点信息
                        if (Objects.nonNull(tStdDevicemete)) {
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

                            Result result = sendPostRequest(Constant.WARN_JUDGE, params);
                            Map<String, Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
                            log.info("object转map的东西==={}", map);
                            Boolean isWarN = (Boolean) map.get("isWarn");
                            String outRange = null;
                            if (Objects.nonNull(map.get("outRange"))) {
                                outRange = map.get("outRange").toString();
                            }

                            //组装告警基本信息
                            TWarnInfo warnInfo = new TWarnInfo();
                            warnInfo.setWarnTime(new Date());
                            warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
                            warnInfo.setCunstomId(tStdDevicemete.getCustomId());
                            warnInfo.setInstanceId(instanceId);
                            warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
                            //未核查
                            warnInfo.setConfMode(276);
                            Integer warnFlag = Integer.valueOf(StaticContextAccessor.getBean(RobotService.class).selectDictCodeByNote("其他", "defect_model"));
                            warnInfo.setDefectModel(warnFlag);
                            //主辅设备
                            warnInfo.setAlarmSource(282);
                            warnInfo.setImagePath(threadMap.get("relativePath"));
                            warnInfo.setValue(threadMap.get("value"));
                            warnInfo.setTaskId(taskId);
                            Long robotId = StaticContextAccessor.getBean(RobotService.class).selectRobotIdByCode(robotCode);
                            warnInfo.setDeviceCode(robotId.toString());

                            ifGeneratedAlarm(isWarN,map,taskId,warnInfo,outRange,tStdDevicemete);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 判断该点是否产生告警以及告警信息
     * @param isWarN 是否告警标识
     * @param map 根据告警规则判断的结果
     * @param taskId 任务id
     * @param warnInfo 告警信息
     * @param outRange 告警溢出值
     * @param tStdDevicemete 测点信息
     * @return void
     */
    private void ifGeneratedAlarm(Boolean isWarN,Map<String, Object> map,String taskId,TWarnInfo warnInfo,String outRange,TStdDeviceMete tStdDevicemete) throws Exception{
        if (Boolean.TRUE.equals(isWarN)) {
            warnInfo.setWarnName(map.get("warnName").toString());
            warnInfo.setWarnLevel(Integer.valueOf(map.get("warnLevel").toString()));
            warnInfo.setWarnContent(map.get("warnContent").toString());
            warnInfo.setOutRange(outRange);
            log.info("要插库的告警数据是===" + warnInfo);

            /// 留着备用
            /*Map<String,String> redisWarnInfoMap = new HashMap<>();
            redisWarnInfoMap.put("isWarn","1");
            redisTemplate.opsForHash().putAll("t_cruise_task_result:" + taskId + ":" + warnInfo.getInstanceId().toString(),redisWarnInfoMap);*/

            String warnName = "warnInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
            Map<String,String> warnMap = new HashMap<>(16);
            warnMap.put("deviceId", warnInfo.getDeviceId().toString());
            warnMap.put("customId",warnInfo.getCunstomId() );
            warnMap.put("instanceId",warnInfo.getInstanceId().toString());
            warnMap.put("stdMeteId", warnInfo.getStdMeteId().toString());
            warnMap.put("taskId", warnInfo.getTaskId());
            warnMap.put("value", warnInfo.getValue());
            warnMap.put("imagePath", warnInfo.getImagePath());
            warnMap.put("confMode", "276");
            warnMap.put("alarmSource", warnInfo.getAlarmSource().toString());
            warnMap.put("defectModel", warnInfo.getDefectModel().toString());
            warnMap.put("warnLevel",warnInfo.getWarnLevel().toString());
            warnMap.put("warnName", warnInfo.getWarnName());
            warnMap.put("warnTime",new SimpleDateFormat().format(warnInfo.getWarnTime()));
            warnMap.put("warnContent",warnInfo.getWarnContent());
            if (Objects.nonNull(warnInfo.getOutRange())){
                warnMap.put("outRange",warnInfo.getOutRange());
            }
            log.info("warnMap==="+warnMap);
            redisTemplate.opsForHash().putAll(warnName, warnMap);

            StaticContextAccessor.getBean(RobotService.class).insertWarn(warnInfo);
            Long warnId = StaticContextAccessor.getBean(RobotService.class).selectWarnId(warnInfo.getTaskId(),warnInfo.getInstanceId());
            log.info("warnId==="+warnId);

            Map<String,String> currentWarnInfo=new HashMap<>(16);
            currentWarnInfo.put("warnId",warnId.toString());
            currentWarnInfo.put("defectModel","450");
            currentWarnInfo.put("isPop","false");

            // webSocket通知前端刷新告警统计数量
            Map<String, Object> jasonMaps = new HashMap<>(16);
            jasonMaps.put("type", "newAlarm");
            jasonMaps.put("alarmName", warnInfo.getWarnName());
            jasonMaps.put("alarmTime", warnInfo.getWarnTime());
            jasonMaps.put("alarmContent", warnInfo.getWarnContent());
            String jsons = JSON.toJSONString(jasonMaps);
            log.info("告警生成-前端推送：" + jsons);
            Constant.postUrl(webSocketUrl, jsons);

            //判断该测点是否设置了告警推送,若是,则将配置的告警信息组成告警弹框所需内容推给前端;不是,不推
            String alarmNote = tStdDevicemete.getAlarmNote();
            Integer alarmLevel = tStdDevicemete.getAlarmLevel();
            Integer warnLevel = warnInfo.getWarnLevel();
            log.info("该测点是否配置了告警提示是===" + alarmNote);
            log.info("该测点告警推送配置的告警等级是===" + alarmLevel);
            log.info("产生的该条告警等级是===" + warnLevel);
            boolean one = (Objects.nonNull(alarmNote) && "1".equals(alarmNote));
            boolean two = (Objects.nonNull(alarmLevel) && (warnLevel.compareTo(alarmLevel) == 0 || warnLevel > alarmLevel));
            log.info("一层判断" + one + "二层判断"+two);

            if (Boolean.TRUE.equals(one) && Boolean.TRUE.equals(two)) {
                //webSocket通知前端调用查询告警弹框的接口
                Map<String, Object> jasonMaps2 = new HashMap<>(16);
                jasonMaps2.put("type", "alarmPopUp");
                jasonMaps2.put("warnId", warnInfo.getWarnId());
                jasonMaps2.put("defectModel", warnInfo.getDefectModel());
                String json = JSON.toJSONString(jasonMaps2);
                log.info("告警弹窗-前端推送：" + json);
                currentWarnInfo.put("isPop","true");
                Constant.postUrl(webSocketUrl,json);
            }

            redisTemplate.opsForValue().set("currentWarn",currentWarnInfo,3, TimeUnit.MINUTES);
        }
    }

    public Result sendPostRequest(String url, Map<String,Object> params) {
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(url, Result.class,params);
    }

    /**
     * Redis数据库批量查询Key值游标
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
