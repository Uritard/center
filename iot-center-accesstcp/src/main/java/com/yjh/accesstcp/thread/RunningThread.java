package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author lqh
 * @since 2021/3/9
 */
@lombok.extern.slf4j.Slf4j
public class RunningThread implements Runnable{

    private RedisTemplate redisTemplate;
    private TCPClientHandler tcpClientHandler;
    private volatile boolean isThreadStart;
    private SendToUpSystemServices sendToUpSystemServices;

    public RunningThread(TCPClientHandler tcpClientHandler,RedisTemplate redisTemplate, boolean isThreadStart,SendToUpSystemServices sendToUpSystemServices) {
        this.isThreadStart = isThreadStart;
        this.redisTemplate = redisTemplate;
        this.sendToUpSystemServices = sendToUpSystemServices;
        this.tcpClientHandler = tcpClientHandler;
    }
    @Override
    public void run() {
        while (isThreadStart) {
            try {

                if (!tcpClientHandler.getIsThreadStart()) {
                    isThreadStart = false;
                    log.info("Thread is " + Thread.currentThread().getName() + Thread.currentThread().getId());
                    log.info("Thread stop success!");
                }

                String s = Constant.paramMap.get("patroldevice_run_interval");
                if(s == null){
                    s= "30";
                }
                Thread.sleep(Long.valueOf(s)*1000L);
                {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String now = simpleDateFormat.format(new Date());
                    List<Map<String, Object>> onlinePatrolDevice = sendToUpSystemServices.selectOnlinePatrolDevice();
                    for (Map<String, Object> device : onlinePatrolDevice) {
                        List<Map<String, Object>> list = new ArrayList<>();
                        //运行速度
                        Map<String, Object> mapForSpeed = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":1");
                        Map<String, Object> map1 = createMap(now, 1, mapForSpeed);
                        list.add(map1);
                        //行驶里程
                        Map<String, Object> mapForMileage = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":2");
                        Map<String, Object> map2 = createMap(now, 2, mapForMileage);
                        list.add(map2);
                        //电池电量
                        Map<String, Object> mapForCell = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":3");
                        Map<String, Object> map3 = createMap(now, 3, mapForCell);
                        list.add(map3);
                        if ("drone".equals(device.get("type"))) {
                            //垂直速度
                            Map<String, Object> mapForVerticalSpeed = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":4");
                            Map<String, Object> map4 = createMap(now, 4, mapForVerticalSpeed);
                            list.add(map4);
                            //飞行距离
                            Map<String, Object> mapForFlyDistance = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":5");
                            Map<String, Object> map5 = createMap(now, 5, mapForFlyDistance);
                            list.add(map5);
                            //飞行高度
                            Map<String, Object> mapForFlyHeight = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":6");
                            Map<String, Object> map6 = createMap(now, 6, mapForFlyHeight);
                            list.add(map6);
                            //飞行时长
                            Map<String, Object> mapForFlyTime = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":7");
                            Map<String, Object> map7 = createMap(now, 7, mapForFlyTime);
                            list.add(map7);
                        }
                        //云台俯仰角
                        Map<String, Object> mapForHeadPitchAngle = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":8");
                        Map<String, Object> map8 = createMap(now, 8, mapForHeadPitchAngle);
                        list.add(map8);
                        //云台横滚角
                        Map<String, Object> mapForHeadRollAngle = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":9");
                        Map<String, Object> map9 = createMap(now, 9, mapForHeadRollAngle);
                        list.add(map9);
                        //云台偏航角
                        Map<String, Object> mapForHeadYawAngle = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":10");
                        Map<String, Object> map10 = createMap(now, 10, mapForHeadYawAngle);
                        list.add(map10);
                        if ("robot".equals(device.get("type"))) {
                            //充电电流
                            Map<String, Object> mapForChargeCurrent = redisTemplate.opsForHash().entries("RobotOperation:" + device.get("robot_code") + ":11");
                            Map<String, Object> map11 = createMap(now, 11, mapForChargeCurrent);
                            list.add(map11);
                        }
                        sendToUpSystemServices.sendResponse(0L, "2","",Constant.stationCode,list, true);
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static Map<String, Object> createMap(String now, Integer type, Map<String, Object> mapForRedis){
        Map<String, Object> map = new HashMap<>(7);
        map.put("patroldevice_name", Optional.ofNullable(mapForRedis.get("patrolDeviceName")).orElse(""));
        map.put("patroldevice_code", Optional.ofNullable(mapForRedis.get("patrolDeviceCode")).orElse(""));
        map.put("time", now);
        map.put("type", type);
        map.put("value", Optional.ofNullable(mapForRedis.get("value")).orElse("0"));
        map.put("unit", Optional.ofNullable(mapForRedis.get("unit")).orElse(""));
        map.put("value_unit", Optional.ofNullable(mapForRedis.get("valueUnit")).orElse(""));
        return map;
    }
}
