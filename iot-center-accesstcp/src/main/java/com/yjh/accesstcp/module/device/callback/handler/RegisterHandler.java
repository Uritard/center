package com.yjh.accesstcp.module.device.callback.handler;

import com.yjh.accesstcp.module.device.callback.CallbackHandlerStrategy;
import com.yjh.accesstcp.module.device.callback.CallbackHandlerStrategyFactory;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.thread.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/5/10
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterHandler implements CallbackHandlerStrategy<XMLBaseModel> {

    private final RegisterManager register;
    private final TaskScheduler taskScheduler;
    private final RedisTemplate redisTemplate;
    private final SendToUpSystemServices sendToUpSystemServices;

    public final static Map<String, Long> INTERVAL_MAP = new ConcurrentHashMap<>(8);

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        String cmd = "4";
        String rSend = "100";
        String success = "200";
        String serverName = clientHandler.getServer();
        if (cmd.equals(xmlBaseModel.getCommand())) {
            // 100:需要重发注册消息 200:服务端响应 我方开启心跳
            if (rSend.equals(xmlBaseModel.getCode())) {
                log.info(serverName + "---注册响应---200需要重发注册消息----");
                register.register(clientHandler);
            } else if (success.equals(xmlBaseModel.getCode())) {
                log.info(serverName + "---注册响应--200响应成功----");
                register.terminate(clientHandler.getServer());
                List<Map<String, Object>> items = xmlBaseModel.getItems();
                if (CollectionUtils.isEmpty(items)) {
                    return;
                }
                for (Map<String, Object> item : items) {
                    item.forEach((k, v) -> INTERVAL_MAP.put(k, NumberUtils.toLong((String) v)));
                }
                //心跳线程发心跳
                String heartBeat = "heart_beat_interval";
                if (INTERVAL_MAP.containsKey(heartBeat)) {
                    HeartBeatThead.addThread(clientHandler, taskScheduler, INTERVAL_MAP.getOrDefault(heartBeat, 60L));
                }
                //运行参数线程
                String runParams = "run_params_interval";
                if (INTERVAL_MAP.containsKey(runParams)) {
                    RunParamsThread.addThread(clientHandler, taskScheduler, INTERVAL_MAP.getOrDefault(runParams, 30L));
                }
                //运行数据
                String running = "patroldevice_run_interval";
                if (INTERVAL_MAP.containsKey(running)) {
                    RunningThread.addThread(clientHandler, taskScheduler, INTERVAL_MAP.getOrDefault(running, 60L), redisTemplate, sendToUpSystemServices);
                }
                //微气象数据
                String weather = "weather_interval";
                if (INTERVAL_MAP.containsKey(weather)) {
                    WeatherThread.addThread(clientHandler, taskScheduler, INTERVAL_MAP.getOrDefault(weather, 60L), redisTemplate, sendToUpSystemServices);
                }
                //机巢数据
                String nest = "nest_run_interval";
                if (INTERVAL_MAP.containsKey(nest)) {
                    NestRunThread.addThread(clientHandler, taskScheduler, INTERVAL_MAP.getOrDefault(nest, 60L), redisTemplate, sendToUpSystemServices);
                }
            } else {
                log.info(serverName + "---注册响应--400拒绝注册----");
                //注册拒绝 继续注册
                register.register(clientHandler);
            }
        } else {
            log.info("---收到 {} 的返回消息 type: {}, command: {} ----", serverName, xmlBaseModel.getType(), xmlBaseModel.getCommand());
        }
    }

    /**
     * 根据 rootName sessionId 创建回调
     *
     * @param callbackKey rootName + sessionId
     */
    public void createCallback(String callbackKey) {
        CallbackHandlerStrategyFactory.createCallback(callbackKey, this);
    }

}
