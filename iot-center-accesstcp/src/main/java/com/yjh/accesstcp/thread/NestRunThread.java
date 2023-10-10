package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
    private volatile String nowTime;

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

                    sendStatus(device);
                }
                log.info("--机巢信息已发送--");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void sendStatus(Map<String, Object> device) {
        List<Map<String, Object>> list = new ArrayList<>();

        nowTime = DateTimeUtil.getDateTimeString();

        addStateMap(list, device, 1, "1");
        addStateMap(list, device, 2, "0");
        addStateMap(list, device, 3, "1");
        addStateMap(list, device, 4, "0");
        addStateMap(list, device, 5, "1");
        sendToUpSystemServices.sendResponse(0L, "20001", "", Constant.stationCode(), list, true);
    }

    private void addStateMap(List<Map<String, Object>> list, Map<String, Object> device, int type, String valueDef) {
        Map<String, Object> stateMap = redisTemplate.opsForHash().entries("RobotStatus:" + device.get("robot_code") + ":" + type);
        if (MapUtils.isEmpty(stateMap)) {
            stateMap = new HashMap<>(8);
            stateMap.put("value", valueDef);
        }
        stateMap.put("patrolDeviceCode", device.get("robot_num"));
        stateMap.put("patrolDeviceName", device.get("robot_name"));
        Map<String, Object> mapUp = createMap(type, stateMap, nowTime);
        list.add(mapUp);
    }

    public Map<String, Object> createMap(Integer type, Map<String, Object> mapForRedis){
        return createMap(type, mapForRedis, null);
    }

    public Map<String, Object> createMap(Integer type, Map<String, Object> mapForRedis, String nowTime){
        Map<String, Object> map = new HashMap<>(16);
        map.put("nest_name", Optional.ofNullable(mapForRedis.get("nestName")).orElse(""));
        map.put("nest_code", Optional.ofNullable(mapForRedis.get("nestCode")).orElse(""));
        if (StringUtils.isNotEmpty(nowTime)) {
            map.put("time", nowTime);
        } else {
            map.put("module_no", Optional.ofNullable(mapForRedis.get("moduleNo")).orElse(""));
        }
        map.put("type", type);
        map.put("value", Optional.ofNullable(mapForRedis.get("value")).orElse("0"));
        map.put("unit", Optional.ofNullable(mapForRedis.get("unit")).orElse(""));
        map.put("value_unit", Optional.ofNullable(mapForRedis.get("valueUnit")).orElse(""));
        return map;
    }
}
