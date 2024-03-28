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
                    String patrolDeviceCode = MapUtils.getString(device, "robot_code");
                    //电池电量
                    addNestItem(list, device, 1);
                    //电池使用状态
                    addNestItem(list, device, 2);
                    //电池状态
                    addNestItem(list, device, 3);
                    //电池电压
                    addNestItem(list, device, 4);
                    //舱内温度
                    addNestItem(list, device, 5);
                    //舱内湿度
                    addNestItem(list, device, 6);

                    sendToUpSystemServices.sendResponse(0L, "10004","",Constant.stationCode(),list, true);

                    sendStatus(device);
                }
                log.info("--机巢信息已发送--");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void addNestItem(List<Map<String, Object>> list, Map<String, Object> device, Integer type) {
        String patrolDeviceCode = MapUtils.getString(device, "robot_code");
        Map<String, Object> mapForRedis = redisTemplate.opsForHash().entries("nestOperation:" + patrolDeviceCode + ":" + type);
        mapForRedis.put("nestName", device.get("nest_name"));
        mapForRedis.put("nestCode", device.get("nest_code"));
        Map<String, Object> map = createMap(type, mapForRedis);
        list.add(map);
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
        stateMap.put("nestCode", device.get("nest_name"));
        stateMap.put("nestName", device.get("nest_code"));
        Map<String, Object> mapUp = createMap(type, stateMap, nowTime);
        list.add(mapUp);
    }

    public Map<String, Object> createMap(Integer type, Map<String, Object> mapForRedis){
        return createMap(type, mapForRedis, null);
    }

    public Map<String, Object> createMap(Integer type, Map<String, Object> mapForRedis, String nowTime){
        if (MapUtils.isEmpty(mapForRedis)) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>(16);
        map.put("nest_name", Optional.ofNullable(mapForRedis.get("nestName")).orElse(""));
        map.put("nest_code", Optional.ofNullable(mapForRedis.get("nestCode")).orElse(""));
        if (StringUtils.isNotEmpty(nowTime)) {
            map.put("time", nowTime);
        } else {
            map.put("module_no", Optional.ofNullable(mapForRedis.get("moduleNo")).orElse(""));
        }
        map.put("type", type);
        String value = (String)Optional.ofNullable(mapForRedis.get("value")).orElse("1");
        map.put("value", value);
        String unit = (String)Optional.ofNullable(mapForRedis.get("unit")).orElse("");
        map.put("unit", unit);
        map.put("value_unit", value + unit);
        return map;
    }
}
