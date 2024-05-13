package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.utils.BeanToMapUtil;
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
 * @date 2024/5/8
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class RunParamsThread {
    private static final Map<String, Future<?>> RUNNER_MAP = new HashMap<>();

    private RunParamsThread() {
        // do nothing
    }

    public static synchronized void addThread(TCPClientHandler clientHandler, TaskScheduler taskScheduler, long duration) {
        if (!RUNNER_MAP.containsKey(clientHandler.getServer())) {
            Future<?> future =
                    taskScheduler.scheduleAtFixedRate(new InternalRunner(clientHandler), Instant.now(), Duration.ofSeconds(duration));
            RUNNER_MAP.put(clientHandler.getServer(), future);
        } else {
            log.warn("心跳定时器已添加");
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
                Result re = Constant.getForObject(Constant.ALGORITHM_RESOURCE_URL);
                List<Map<String,Object>> items = new ArrayList<>();
                items.add(BeanToMapUtil.convertBean2Map(re.getData()));
                XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("313").setCode(Constant.stationCode()).setItems(items);
                clientHandler.send(xmlBaseModel, 0, true);
                log.info("--算法资源信息已发送--");
            } catch (Exception e) {
                log.error("算法资源信息发送失败", e);
            }
        }

    }
}
