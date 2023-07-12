package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

/**
 * @author lqh
 * @since 2021/3/9
 */
@lombok.extern.slf4j.Slf4j
public class NestRunThread implements Runnable{

    private RedisTemplate redisTemplate;
    private TCPClientHandler tcpClientHandler;
    private volatile boolean isThreadStart;
    private SendToUpSystemServices sendToUpSystemServices;

    public NestRunThread(TCPClientHandler tcpClientHandler, RedisTemplate redisTemplate, boolean isThreadStart, SendToUpSystemServices sendToUpSystemServices) {
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
                    log.info("NestRunThread is " + Thread.currentThread().getName() + Thread.currentThread().getId());
                    log.info("NestRunThread stop success!");
                    break;
                }
                String s = Constant.paramMap.get("nest_run_interval");
                if(s == null){
                    s= "30";
                }
                log.info("NestRunThread sleep {} s .......", s);
                Thread.sleep(Long.valueOf(s)*1000L);
                List<Map<String, Object>> onlinePatrolDevice = sendToUpSystemServices.selectOnlinePatrolDevice();
                for (Map<String, Object> device : onlinePatrolDevice) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    if (!Objects.equals("drone", device.get("type"))){
                        continue;
                    }
                    //电池电量
                    Map<String, Object> mapForSpeed = redisTemplate.opsForHash().entries("nestOperation:" + device.get("robot_code") + ":1");
                    Map<String, Object> map1 = createMap(1, mapForSpeed);
                    list.add(map1);
                    //电池使用状态
                    Map<String, Object> mapForMileage = redisTemplate.opsForHash().entries("nestOperation:" + device.get("robot_code") + ":2");
                    Map<String, Object> map2 = createMap(2, mapForMileage);
                    list.add(map2);
                    //电池状态
                    Map<String, Object> mapForCell = redisTemplate.opsForHash().entries("nestOperation:" + device.get("robot_code") + ":3");
                    Map<String, Object> map3 = createMap(3, mapForCell);
                    list.add(map3);
                    //电池电压
                    Map<String, Object> mapForHeadPitchAngle = redisTemplate.opsForHash().entries("nestOperation:" + device.get("robot_code") + ":4");
                    Map<String, Object> map8 = createMap(4, mapForHeadPitchAngle);
                    list.add(map8);
                    //舱内温度
                    Map<String, Object> mapForHeadRollAngle = redisTemplate.opsForHash().entries("nestOperation:" + device.get("robot_code") + ":5");
                    Map<String, Object> map9 = createMap(5, mapForHeadRollAngle);
                    list.add(map9);
                    //舱内湿度
                    Map<String, Object> mapForHeadYawAngle = redisTemplate.opsForHash().entries("nestOperation:" + device.get("robot_code") + ":6");
                    Map<String, Object> map10 = createMap(6, mapForHeadYawAngle);
                    list.add(map10);

                    sendToUpSystemServices.sendResponse(0L, "10004","",Constant.stationCode(),list, true);
                }
                log.info("--机巢信息已发送--");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static Map<String, Object> createMap(Integer type, Map<String, Object> mapForRedis){
        Map<String, Object> map = new HashMap<>(7);
        map.put("nest_name", Optional.ofNullable(mapForRedis.get("nestName")).orElse(""));
        map.put("nest_code", Optional.ofNullable(mapForRedis.get("nestCode")).orElse(""));
        map.put("module_no", Optional.ofNullable(mapForRedis.get("moduleNo")).orElse(""));
        map.put("type", type);
        map.put("value", Optional.ofNullable(mapForRedis.get("value")).orElse("0"));
        map.put("unit", Optional.ofNullable(mapForRedis.get("unit")).orElse(""));
        map.put("value_unit", Optional.ofNullable(mapForRedis.get("valueUnit")).orElse(""));
        return map;
    }
}
