package com.yjh.accesstcp.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.TCPClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/6/5
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class SystemCheckThread {

    private static final Map<String, Future<?>> RUNNER_MAP = new HashMap<>();

    private SystemCheckThread() {
        // do nothing
    }

    public static synchronized void addThread(TCPClientHandler clientHandler, TaskScheduler taskScheduler, long duration) {
        if (!RUNNER_MAP.containsKey(clientHandler.getServer())) {
            Future<?> future =
                    taskScheduler.scheduleAtFixedRate(new InternalRunner(clientHandler), Instant.now(), Duration.ofSeconds(duration));
            RUNNER_MAP.put(clientHandler.getServer(), future);
        } else {
            log.warn("系统自检定时器已添加");
        }
    }

    public static synchronized void terminate(String serverCode) {
        Optional.ofNullable(RUNNER_MAP.remove(serverCode)).ifPresent(f -> f.cancel(true));
    }

    static class InternalRunner implements Runnable {
        private final TCPClientHandler clientHandler;

        public InternalRunner(TCPClientHandler clientHandler) {
            this.clientHandler = clientHandler;
        }

        @Override
        public void run() {
            try {
                Result re = Constant.getForObject(Constant.SYSTEM_CHECK_URL);
                List<Map<String,Object>> items = new ArrayList<>();
                JSONObject obj = (JSONObject) JSON.toJSON(re.getData());
                items.add(obj);
                XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("200").setCode(Constant.stationCode()).setItems(items);
                clientHandler.send(xmlBaseModel, 0, true);
                log.info("--系统自检信息已发送--");
            } catch (Exception e) {
                log.error("系统自检信息发送失败", e);
            }
        }

    }
}
