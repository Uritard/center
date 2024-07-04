package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.TCPClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lqh
 * @since 2021/3/9
 */
@Slf4j
public class RunningThread {

    private static final Map<String, Future<?>> RUNNER_MAP = new HashMap<>();

    private RunningThread() {
        // do nothing
    }

    public static synchronized void addThread(TCPClientHandler clientHandler, TaskScheduler taskScheduler, long duration,
                                              RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices) {
        if (!RUNNER_MAP.containsKey(clientHandler.getServer())) {
            Future<?> future =
                    taskScheduler.scheduleAtFixedRate(new InternalRunner(clientHandler, redisTemplate, sendToUpSystemServices), Instant.now(), Duration.ofSeconds(duration));
            RUNNER_MAP.put(clientHandler.getServer(), future);
        } else {
            log.warn("运行数据定时器已添加");
        }
    }

    public static synchronized void terminate(String serverCode) {
        Optional.ofNullable(RUNNER_MAP.remove(serverCode)).ifPresent(f -> f.cancel(true));
    }

    static class InternalRunner implements Runnable {
        private final TCPClientHandler clientHandler;
        private final RedisTemplate redisTemplate;
        private final SendToUpSystemServices sendToUpSystemServices;
        private final AtomicInteger counter = new AtomicInteger(1);
        private volatile String nowTime;

        public InternalRunner(TCPClientHandler clientHandler, RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices) {
            this.clientHandler = clientHandler;
            this.redisTemplate = redisTemplate;
            this.sendToUpSystemServices = sendToUpSystemServices;
        }

        @Override
        public void run() {
            try {
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
                    XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("2").setCode(Constant.stationCode()).setItems(list);
                    clientHandler.send(xmlBaseModel, 0, true);
                    // 发送状态信息
                    sendStatus(device);
                }
                int count = counter.incrementAndGet();
                if (count > 3) {
                    counter.set(count % 3);
                }
                log.info("--运行数据已发送--");
            } catch (Exception e) {
                log.error("运行数据发送失败", e);
            }
        }

        private void sendStatus(Map<String, Object> device) {
            if (counter.get() % 3 > 0) {
                return;
            }
            List<Map<String, Object>> list = new ArrayList<>();

            nowTime = DateTimeUtil.getDateTimeString();

            addStateMap(list, device, 2, "0");
            addStateMap(list, device, 41, "1");
            addStateMap(list, device, 61, "1");
            XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("1").setCode(Constant.stationCode());
            clientHandler.send(xmlBaseModel, 0L, true);
        }

        private void addStateMap(List<Map<String, Object>> list, Map<String, Object> device, int type, String valueDef) {
            Map<String, Object> stateMap = redisTemplate.opsForHash().entries("RobotStatus:" + device.get("robot_code") + ":" + type);
            if (MapUtils.isEmpty(stateMap)) {
                stateMap = new HashMap<>(8);
                stateMap.put("value", valueDef);
            }
            stateMap.put("patrolDeviceCode", device.get("robot_num"));
            stateMap.put("patrolDeviceName", device.get("robot_name"));
            Map<String, Object> mapUp = createMap(nowTime, type, stateMap);
            list.add(mapUp);
        }

        public static Map<String, Object> createMap(String now, Integer type, Map<String, Object> mapForRedis) {
            Map<String, Object> map = new HashMap<>(16);
            map.put("patroldevice_name", Optional.ofNullable(mapForRedis.get("patrolDeviceName")).orElse(""));
            map.put("patroldevice_code", Optional.ofNullable(mapForRedis.get("patrolDeviceCode")).orElse(""));
            map.put("time", now);
            map.put("type", type);
            String value = (String) Optional.ofNullable(mapForRedis.get("value")).orElse("0");
            map.put("value", value);
            map.put("unit", Optional.ofNullable(mapForRedis.get("unit")).orElse(""));
            map.put("value_unit", Optional.ofNullable(mapForRedis.get("valueUnit")).orElse(value));
            return map;
        }

    }
}
