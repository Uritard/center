package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
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
                    log.info("Thread is " + Thread.currentThread().getName() + Thread.currentThread().getId());
                    log.info("Thread stop success!");
                }
                {
                    String s = Constant.paramMap.get("weather_interval");

                    Thread.sleep(NumberUtils.toLong(s,30) *1000L);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String now = simpleDateFormat.format(new Date());
                    //获取redis的天气信息
                    Map<String, String> mapForWeather = redisTemplate.opsForHash().entries("weatherInfoForLastValue");
                    List<Map<String, Object>> list = new ArrayList<>();
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("patroldevice_name","");
                    map1.put("patroldevice_code","");
                    map1.put("time", now);
                    map1.put("type", 1);
                    map1.put("value", mapForWeather.get("humidity"));
                    map1.put("unit", mapForWeather.get("humidityUnit"));
                    map1.put("value_unit", mapForWeather.get("humidity") + mapForWeather.get("humidityUnit"));
                    list.add(map1);

                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("patroldevice_name","");
                    map2.put("patroldevice_code","");
                    map2.put("time", now);
                    map2.put("type", 2);
                    map2.put("value", mapForWeather.get("temperature"));
                    map2.put("unit", mapForWeather.get("temperatureUnit"));
                    map2.put("value_unit", mapForWeather.get("temperature") + mapForWeather.get("temperatureUnit"));
                    list.add(map2);

                    Map<String, Object> map3 = new HashMap<>();
                    map3.put("patroldevice_name","");
                    map3.put("patroldevice_code","");
                    map3.put("time", now);
                    map3.put("type", 3);
                    map3.put("value", mapForWeather.get("windSpeed"));
                    map3.put("unit", mapForWeather.get("windSpeedUnit"));
                    map3.put("value_unit", mapForWeather.get("windSpeed") + mapForWeather.get("windSpeedUnit"));
                    list.add(map3);

                    Map<String, Object> map4 = new HashMap<>();
                    map4.put("patroldevice_name","");
                    map4.put("patroldevice_code","");
                    map4.put("time", now);
                    map4.put("type", 4);
                    map4.put("value", mapForWeather.get("precipitation"));
                    map4.put("unit", mapForWeather.get("precipitationUnit"));
                    map4.put("value_unit", mapForWeather.get("precipitation") + mapForWeather.get("precipitationUnit"));
                    list.add(map4);

                    Map<String, Object> map5 = new HashMap<>();
                    map5.put("patroldevice_name","");
                    map5.put("patroldevice_code","");
                    map5.put("time", now);
                    map5.put("type", 5);
                    map5.put("value", mapForWeather.get("windDirection"));
                    map5.put("unit", mapForWeather.get("windDirectionUnit"));
                    map4.put("value_unit", mapForWeather.get("windDirection") + mapForWeather.get("windDirectionUnit"));
                    list.add(map5);

                    Map<String, Object> map6 = new HashMap<>();
                    map6.put("patroldevice_name","");
                    map6.put("patroldevice_code","");
                    map6.put("time", now);
                    map6.put("type", 6);
                    map6.put("value", mapForWeather.get("airPressure"));
                    map6.put("unit", mapForWeather.get("airPressureUnit"));
                    map6.put("value_unit", mapForWeather.get("airPressure") + mapForWeather.get("airPressureUnit"));
                    list.add(map6);

                    Map<String, Object> map7 = new HashMap<>();
                    map7.put("patroldevice_name","");
                    map7.put("patroldevice_code","");
                    map7.put("time", now);
                    map7.put("type", 7);
                    map7.put("value", mapForWeather.get("oxygen"));
                    map7.put("unit", mapForWeather.get("oxygenUnit"));
                    map7.put("value_unit", mapForWeather.get("oxygen") + mapForWeather.get("oxygenUnit"));
                    list.add(map7);

                    Map<String, Object> map8 = new HashMap<>();
                    map8.put("patroldevice_name","");
                    map8.put("patroldevice_code","");
                    map8.put("time", now);
                    map8.put("type", 8);
                    map8.put("value", mapForWeather.get("sf6"));
                    map8.put("unit", mapForWeather.get("sf6Unit"));
                    map8.put("value_unit", mapForWeather.get("sf6") + mapForWeather.get("sf6Unit"));
                    list.add(map8);

                    sendToUpSystemServices.sendResponse(0L, "21","",Constant.stationCode,list, true);
                    log.info("--天气信息已发送--");
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
