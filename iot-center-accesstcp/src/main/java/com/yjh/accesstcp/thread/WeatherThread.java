package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;


/**
 * @author lqh
 * @since 2021/1/13
 */
@lombok.extern.slf4j.Slf4j
public class WeatherThread implements Runnable{

    private RedisTemplate redisTemplate;
    private TCPClientHandler tcpClientHandler;
    private volatile boolean isThreadStart;
    private SendToUpSystemServices sendToUpSystemServices;

    public WeatherThread(TCPClientHandler tcpClientHandler,RedisTemplate redisTemplate, boolean isThreadStart,SendToUpSystemServices sendToUpSystemServices) {
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
                    log.info("WeatherThread is " + Thread.currentThread().getName() + Thread.currentThread().getId());
                    log.info("WeatherThread stop success!");
                    break;
                }
                String s = Constant.paramMap.get("weather_interval");
                if(s == null){
                    s= "30";
                }
                Thread.sleep(Long.valueOf(s)*1000L);
                {
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
                            Map<String, Object> map7 = createMap(now, 7, mapForOxygen, device);
                            list.add(map7);
                            //SF6含量
                            Map<String, Object> mapForSf6 = redisTemplate.opsForHash().entries("RobotWeather:" + device.get("robot_code") + ":8");
                            Map<String, Object> map8 = createMap(now, 8, mapForSf6, device);
                            list.add(map8);
                        }
                        sendToUpSystemServices.sendResponse(0L, "21","",Constant.stationCode(),list, true);
                    }
                    log.info("--天气信息已发送--");
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static Map<String, Object> createMap(String now, Integer type, Map<String, Object> mapForRedis, Map<String, Object> device){
        Map<String, Object> map = new HashMap<>(7);
        String patrolDeviceName = String.valueOf(device.get("robot_name"));
        String patrolDeviceCode = String.valueOf(device.get("robot_num"));
        map.put("patroldevice_name", patrolDeviceName);
        map.put("patroldevice_code", patrolDeviceCode);
        map.put("time", now);
        map.put("type", type);
        map.put("value", Optional.ofNullable(mapForRedis.get("value")).orElse("0"));
        map.put("unit", Optional.ofNullable(mapForRedis.get("unit")).orElse(""));
        map.put("value_unit", Optional.ofNullable(mapForRedis.get("valueUnit")).orElse(""));
        return map;
    }
}
