package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.TCPClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @author lqh
 * @since 2021/3/9
 */
@Slf4j
public class NestRunThread{

    private static final Map<String, Future<?>> RUNNER_MAP = new HashMap<>();

    private NestRunThread() {
        // do nothing
    }

    public static synchronized void addThread(TCPClientHandler clientHandler, TaskScheduler taskScheduler, long duration,
                                              RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices) {
        if (!RUNNER_MAP.containsKey(clientHandler.getServer())) {
            Future<?> future =
                    taskScheduler.scheduleAtFixedRate(new InternalRunner(clientHandler, redisTemplate, sendToUpSystemServices), Instant.now(), Duration.ofSeconds(duration));
            RUNNER_MAP.put(clientHandler.getServer(), future);
        } else {
            log.warn("机巢运行数据定时器已添加");
        }
    }

    public static synchronized void terminate(String serverCode) {
        Optional.ofNullable(RUNNER_MAP.remove(serverCode)).ifPresent(f -> f.cancel(true));
    }

    static class InternalRunner implements Runnable {
        private final TCPClientHandler clientHandler;
        private final RedisTemplate redisTemplate;
        private final SendToUpSystemServices sendToUpSystemServices;
        private volatile String nowTime;

        public InternalRunner(TCPClientHandler clientHandler, RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices) {
            this.clientHandler = clientHandler;
            this.redisTemplate = redisTemplate;
            this.sendToUpSystemServices = sendToUpSystemServices;
        }

        @Override
        public void run() {
            XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("10004").setCode(Constant.stationCode());
            List<Map<String, Object>> onlinePatrolDevice = sendToUpSystemServices.selectOnlinePatrolDevice();
            for (Map<String, Object> device : onlinePatrolDevice) {
                List<Map<String, Object>> list = new ArrayList<>();
                if (!Objects.equals("drone", device.get("type"))){
                    continue;
                }
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

                xmlBaseModel.setItems(list);
                clientHandler.send(xmlBaseModel, 0, true);

                sendStatus(device);
            }
            log.info("--机巢信息已发送--");
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
            XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("20001").setCode(Constant.stationCode()).setItems(list);
            clientHandler.send(xmlBaseModel, 0, true);
        }

        private void addStateMap(List<Map<String, Object>> list, Map<String, Object> device, int type, String valueDef) {
            Map<String, Object> stateMap = redisTemplate.opsForHash().entries("nestStatus:" + device.get("robot_code") + ":" + type);
            if (MapUtils.isEmpty(stateMap)) {
                stateMap = new HashMap<>(8);
                stateMap.put("value", valueDef);
            }
            stateMap.put("nestCode", device.get("nest_code"));
            stateMap.put("nestName", device.get("nest_name"));
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
}
