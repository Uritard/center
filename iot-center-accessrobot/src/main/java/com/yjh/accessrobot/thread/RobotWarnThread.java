package com.yjh.accessrobot.thread;

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
 * @date 2021/12/15
 */
@lombok.extern.slf4j.Slf4j
public class RobotWarnThread implements Runnable{

    private Map<String,String> threadMap;
    private RedisTemplate redisTemplate;
    private String webSocketUrl;

    public RobotWarnThread(Map<String,String> threadMap, RedisTemplate redisTemplate, String webSocketUrl){
        this.threadMap = threadMap;
        this.redisTemplate = redisTemplate;
        this.webSocketUrl = webSocketUrl;
    }

    @Override
    public void run() {
        try {
            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:"+threadMap.get("robotCode")+ ":" +threadMap.get("taskCode"));
            for (String key : robotInfoKeys) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                if (threadMap.get("robotCode").equals(redisInfoMap.get("robotCode"))
                        && threadMap.get("taskCode").equals(redisInfoMap.get("taskId"))
                        && threadMap.get("deviceId").equals(redisInfoMap.get("inspectionCode"))) {
                    Long instanceId = Long.valueOf(redisInfoMap.get("instanceId"));
                    TStdDeviceMete tStdDevicemete = StaticContextAccessor.getBean(RobotService.class).selectDeviceMeteInfo(instanceId);

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
                    warnInfo.setValue(threadMap.get("value"));
                    warnInfo.setTaskId(threadMap.get("taskCode"));
                    Long robotId = StaticContextAccessor.getBean(RobotService.class).selectRobotIdByCode(threadMap.get("robotCode"));
                    warnInfo.setDeviceCode(robotId.toString());
                    warnInfo.setWarnName(threadMap.get("content"));
                    warnInfo.setWarnLevel(133);
                    warnInfo.setWarnContent(threadMap.get("content"));
                    log.info("要插库的告警数据是===" + warnInfo);

                    String warnName = "warnInfo:" + threadMap.get("taskCode") + String.valueOf(UUID.randomUUID()).replace("-", "");
                    Map<String, String> warnMap = new HashMap<>();
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
                    Long warnId = StaticContextAccessor.getBean(RobotService.class).selectWarnId(warnInfo.getTaskId(), warnInfo.getInstanceId());
                    log.info("warnId===" + warnId);

                    Map<String, String> currentWarnInfo = new HashMap<>();
                    currentWarnInfo.put("warnId", warnId.toString());
                    currentWarnInfo.put("defectModel", "450");
                    currentWarnInfo.put("isPop", "false");

                    // webSocket通知前端刷新告警统计数量
                    Map<String, Object> jasonMaps = new HashMap<>();
                    jasonMaps.put("type", "newAlarm");
                    jasonMaps.put("alarmName", threadMap.get("content"));
                    jasonMaps.put("alarmTime", threadMap.get("time"));
                    jasonMaps.put("alarmContent", threadMap.get("content"));
                    String jsons = JSON.toJSONString(jasonMaps);
                    log.info("告警生成-前端推送：" + jsons);
                    Constant.postUrl(webSocketUrl, jsons);

                    //webSocket通知前端调用查询告警弹框的接口
                    currentWarnInfo.put("isPop", "true");
                    Map<String, Object> jasonMaps2 = new HashMap<>();
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
            log.error(e.getMessage());
        }
    }

    //Redis数据库批量查询Key值游标
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
