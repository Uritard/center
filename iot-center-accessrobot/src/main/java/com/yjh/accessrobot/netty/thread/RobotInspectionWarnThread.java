package com.yjh.accessrobot.netty.thread;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.entity.TStdDeviceMete;
import com.yjh.accessrobot.module.command.entity.TWarnInfo;
import com.yjh.accessrobot.module.command.service.RobotService;
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
 * @author YChen
 * @date 2021/12/16
 * 机器人巡视结果的告警处理线程
 */
@lombok.extern.slf4j.Slf4j
public class RobotInspectionWarnThread implements Runnable{

    private Map<String,String> warnResultMap;
    private RedisTemplate redisTemplate;
    private String webSocketUrl;

    public RobotInspectionWarnThread(Map<String,String> warnResultMap, RedisTemplate redisTemplate, String webSocketUrl){
        this.warnResultMap = warnResultMap;
        this.redisTemplate = redisTemplate;
        this.webSocketUrl = webSocketUrl;
    }

    @Override
    public void run() {
        try {
            log.info("开始处理巡视结果产生的告警数据 >>>>>>> warnResultMap==={}",warnResultMap);
            String taskId = warnResultMap.get("taskCode");

            String robotCode = warnResultMap.getOrDefault("robotCode", "");
            if (StringUtils.isEmpty(robotCode)) {
                log.error("机器人编码为空");
                throw new RuntimeException("机器人编码为空");
            }

            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:" + robotCode + ":" + taskId);
            for (String key : robotInfoKeys) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                if (Objects.equals(robotCode,redisInfoMap.get("robotCode"))
                        && Objects.equals(taskId,redisInfoMap.get("taskId"))
                        && Objects.equals(warnResultMap.get("deviceId"),redisInfoMap.get("inspectionCode"))) {
                    Long instanceId = Long.valueOf(redisInfoMap.get("instanceId"));
                    TStdDeviceMete tStdDevicemete = StaticContextAccessor.getBean(RobotService.class).selectDeviceMeteInfo(instanceId);

                    storeWarnInfo(tStdDevicemete, instanceId, taskId, robotCode);
                    Long warnId = StaticContextAccessor.getBean(RobotService.class).selectWarnId(taskId, instanceId);
                    log.info("warnId===" + warnId);

                    Map<String, String> currentWarnInfo = new HashMap<>(16);
                    currentWarnInfo.put("warnId", warnId.toString());
                    currentWarnInfo.put("defectModel", "450");
                    currentWarnInfo.put("isPop", "false");

                    // webSocket通知前端刷新告警统计数量
                    Map<String, Object> jasonMaps = new HashMap<>(16);
                    jasonMaps.put("type", "newAlarm");
                    jasonMaps.put("alarmName", warnResultMap.get("content"));
                    jasonMaps.put("alarmTime", warnResultMap.get("time"));
                    jasonMaps.put("alarmContent", warnResultMap.get("content"));
                    String jsons = JSON.toJSONString(jasonMaps);
                    log.info("告警生成-前端推送：" + jsons);
                    Constant.postUrl(webSocketUrl, jsons);

                    //webSocket通知前端调用查询告警弹框的接口
                    currentWarnInfo.put("isPop", "true");
                    Map<String, Object> jasonMaps2 = new HashMap<>(16);
                    jasonMaps2.put("type", "alarmPopUp");
                    jasonMaps2.put("warnId", warnId);
                    jasonMaps2.put("defectModel", 450);
                    String json = JSON.toJSONString(jasonMaps2);
                    log.info("发送给前端的消息：" + json);
                    Constant.postUrl(webSocketUrl, json);

                    //最近一条告警信息 入缓存
                    redisTemplate.opsForValue().set("currentWarn", currentWarnInfo, 3, TimeUnit.MINUTES);
                    log.info("currentWarnInfo666" + currentWarnInfo);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 存储告警基本信息
     * @param tStdDevicemete 测点信息
     * @param instanceId 巡检点id
     * @param taskId 任务id
     * @param robotCode 机器人唯一标识
     * @return void
     */
    private void storeWarnInfo(TStdDeviceMete tStdDevicemete, Long instanceId, String taskId, String robotCode){
        TWarnInfo warnInfo = new TWarnInfo();
        warnInfo.setWarnTime(new Date());
        warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
        warnInfo.setCunstomId(tStdDevicemete.getCustomId());
        warnInfo.setInstanceId(instanceId);
        warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
        warnInfo.setConfMode(276);
        Integer warnFlag = Integer.valueOf(StaticContextAccessor.getBean(RobotService.class).selectDictCodeByNote("其他", "defect_model"));
        warnInfo.setDefectModel(warnFlag);
        warnInfo.setAlarmSource(282);
        warnInfo.setValue(warnResultMap.get("value"));
        warnInfo.setTaskId(taskId);
        Long robotId = StaticContextAccessor.getBean(RobotService.class).selectRobotIdByCode(robotCode);
        warnInfo.setDeviceCode(robotId.toString());
        warnInfo.setWarnName(warnResultMap.get("content"));
        Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId.toString());
        warnInfo.setImagePath(redisInfoMap.get("picpath"));
        if (Objects.nonNull(warnResultMap.get("alarmType")) && !StringUtils.isEmpty(warnResultMap.get("alarmLevel"))) {
            int warnLevel = StaticContextAccessor.getBean(RobotService.class).selectDictCode("alarmLevel", warnResultMap.get("alarmLevel"), "alarm_level");
            warnInfo.setWarnLevel(warnLevel);
        }
        warnInfo.setWarnContent(warnResultMap.get("content"));
        if (Objects.nonNull(warnResultMap.get("alarmType")) && !StringUtils.isEmpty(warnResultMap.get("alarmType"))) {
            int warnType = StaticContextAccessor.getBean(RobotService.class).selectDictCode("pointAlarmType", warnResultMap.get("alarmType"), "point_alarm_type");
            warnInfo.setWarnType(warnType);
        }
        log.info("要插库的告警数据是==={}", warnInfo);

        String warnName = "warnInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
        Map<String, String> warnMap = new HashMap<>(16);
        warnMap.put("deviceId", warnInfo.getDeviceId().toString());
        warnMap.put("customId", warnInfo.getCunstomId());
        warnMap.put("instanceId", warnInfo.getInstanceId().toString());
        warnMap.put("stdMeteId", warnInfo.getStdMeteId().toString());
        warnMap.put("taskId", warnInfo.getTaskId());
        warnMap.put("value", warnInfo.getValue());
        warnMap.put("confMode", "276");
        warnMap.put("alarmSource", warnInfo.getAlarmSource().toString());
        warnMap.put("defectModel", warnInfo.getDefectModel().toString());
        warnMap.put("warnLevel", warnInfo.getWarnLevel().toString());
        warnMap.put("warnName", warnInfo.getWarnName());
        warnMap.put("warnTime", new SimpleDateFormat().format(warnInfo.getWarnTime()));
        warnMap.put("warnContent", warnInfo.getWarnContent());
        log.info("warnMap===" + warnMap);
        redisTemplate.opsForHash().putAll(warnName, warnMap);

        StaticContextAccessor.getBean(RobotService.class).insertWarn(warnInfo);
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
