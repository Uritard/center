package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.module.device.utils.ValueUtil;
import com.yjh.accesstcp.netty.TCPClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;


/**
 * @author lqh
 * @since 2021/1/13
 */
@Slf4j
public class WeatherThread {

    private static final Map<String, Future<?>> RUNNER_MAP = new HashMap<>();

    private WeatherThread() {
        // do nothing
    }

    public static synchronized void addThread(TCPClientHandler clientHandler, TaskScheduler taskScheduler, long duration,
                                              RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices) {
        if (!RUNNER_MAP.containsKey(clientHandler.getServer())) {
            Future<?> future =
                    taskScheduler.scheduleAtFixedRate(new InternalRunner(clientHandler, redisTemplate, sendToUpSystemServices), Instant.now(), Duration.ofSeconds(duration));
            RUNNER_MAP.put(clientHandler.getServer(), future);
        } else {
            log.warn("微气象定时器已添加");
        }
    }

    public static synchronized void terminate(String serverCode) {
        Optional.ofNullable(RUNNER_MAP.remove(serverCode)).ifPresent(f -> f.cancel(true));
    }

    static class InternalRunner implements Runnable {
        private final TCPClientHandler clientHandler;
        private final RedisTemplate redisTemplate;
        private final SendToUpSystemServices sendToUpSystemServices;


        public InternalRunner(TCPClientHandler clientHandler, RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices) {
            this.clientHandler = clientHandler;
            this.redisTemplate = redisTemplate;
            this.sendToUpSystemServices = sendToUpSystemServices;
        }

        @Override
        public void run() {
            XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("21").setCode(Constant.stationCode());
            List<Map<String, Object>> onlinePatrolDevice = sendToUpSystemServices.selectOnlinePatrolDevice();
            for (Map<String, Object> device : onlinePatrolDevice) {
                String now = DateTimeUtil.format(new Date());
                List<Map<String, Object>> list = new ArrayList<>();
                //温度
                Map<String, Object> mapForTemperature = redisTemplate.opsForHash().entries("RobotWeather:" + device.get("robot_code") + ":1");
                Map<String, Object> map1 = createMap(now, 1, mapForTemperature, device);
                list.add(map1);
                //湿度
                Map<String, Object> mapForHumidity = redisTemplate.opsForHash().entries("RobotWeather:" + device.get("robot_code") + ":2");
                Map<String, Object> map2 = createMap(now, 2, mapForHumidity, device);
                list.add(map2);
                //风速
                Map<String, Object> mapForWindSpeed = redisTemplate.opsForHash().entries("RobotWeather:" + device.get("robot_code") + ":3");
                Map<String, Object> map3 = createMap(now, 3, mapForWindSpeed, device);
                list.add(map3);
                //雨量
                Map<String, Object> mapForPrecipitation = redisTemplate.opsForHash().entries("RobotWeather:" + device.get("robot_code") + ":4");
                Map<String, Object> map4 = createMap(now, 4, mapForPrecipitation, device);
                list.add(map4);
                //风向
                Map<String, Object> mapForWindDirection = redisTemplate.opsForHash().entries("RobotWeather:" + device.get("robot_code") + ":5");
                Map<String, Object> map5 = createMap(now, 5, mapForWindDirection, device);
                list.add(map5);
                //气压
                Map<String, Object> mapForAirPressure = redisTemplate.opsForHash().entries("RobotWeather:" + device.get("robot_code") + ":6");
                Map<String, Object> map6 = createMap(now, 6, mapForAirPressure, device);
                list.add(map6);
                if ("robot".equals(device.get("type"))) {
                    //氧气含量
                    Map<String, Object> mapForOxygen = redisTemplate.opsForHash().entries("RobotWeather:" + device.get("robot_code") + ":7");
                    if (mapForOxygen.isEmpty()) {
                        mapForOxygen.put("unit", "ppm");
                        mapForOxygen.put("value", "0");
                        mapForOxygen.put("valueUnit", "0ppm");
                    }

                    Map<String, Object> map7 = createMap(now, 7, mapForOxygen, device);
                    list.add(map7);
                    //SF6含量
                    Map<String, Object> mapForSf6 = redisTemplate.opsForHash().entries("RobotWeather:" + device.get("robot_code") + ":8");
                    if (mapForSf6.isEmpty()) {
                        mapForSf6.put("unit", "ppm");
                        mapForSf6.put("value", "0");
                        mapForSf6.put("valueUnit", "0ppm");
                    }
                    Map<String, Object> map8 = createMap(now, 8, mapForSf6, device);
                    list.add(map8);
                    xmlBaseModel.setItems(list);
                    clientHandler.send(xmlBaseModel, 0, true);
                }
            }
            log.info("--微气象信息已发送--");
        }
    }

    public static Map<String, Object> createMap(String now, Integer type, Map<String, Object> mapForRedis, Map<String, Object> device) {
        Map<String, Object> map = new HashMap<>(7);
        String patrolDeviceName = String.valueOf(device.get("robot_name"));
        String patrolDeviceCode = String.valueOf(device.get("robot_num"));
        map.put("patroldevice_name", patrolDeviceName);
        map.put("patroldevice_code", patrolDeviceCode);
        map.put("time", now);
        map.put("type", type);
        map.put("value", ValueUtil.Object2String(mapForRedis.get("value"), "--"));
        map.put("unit", ValueUtil.Object2String(mapForRedis.get("unit"), ""));
        map.put("value_unit", ValueUtil.Object2String(mapForRedis.get("valueUnit"), "--"));
        return map;
    }
}
