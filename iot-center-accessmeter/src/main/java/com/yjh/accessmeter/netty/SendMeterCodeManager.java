package com.yjh.accessmeter.netty;

import com.yjh.accessmeter.common.Constant;
import com.yjh.accessmeter.configuration.DynamicTask;
import com.yjh.accessmeter.module.device.entity.TMeter;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2024/1/30
 * @since [产品/模块版本] （可选）
 */
@Component
@Slf4j
public class SendMeterCodeManager {

    @Autowired
    private ThreadPoolTaskScheduler asyncExecutor;
    @Autowired
    private DynamicTask dynamicTask;

    private final ConcurrentHashMap<String, InternalRunner> runnerMap = new ConcurrentHashMap<>();

    /**
     * 采集点量
     * 间隔2s采集一次 10s后结果都解析失败 结束采集
     * 中途采集成功 结束采集
     *
     * @param channel 通道
     * @param tMeter 电表信息
     */
    public void sendCollectMsg(Channel channel, TMeter tMeter) {
        try {
            for (DataType dataType : DataType.values()) {
                DLT645Message dlt645Message = new DLT645Message();
                dlt645Message.setControlCode("1997".equals(tMeter.getProtocol()) ? Constant.CONTROLL_CODE_REQUEST : Constant.CONTROLL_CODE_REQUEST_2007);
                dlt645Message.setAddress(tMeter.getAddress());
                dlt645Message.setDataType(dataType.getDataTypeValue(tMeter.getProtocol()));
                send(channel, dlt645Message);
                String key = dlt645Message.getAddress() + Arrays.toString(dlt645Message.getDataType());
                dynamicTask.startDelay(key, () -> this.terminate(dlt645Message), 10 * 1000);
                Thread.sleep(700);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void send(Channel channel, DLT645Message dlt645Message) {
        String key = dlt645Message.getAddress() + Arrays.toString(dlt645Message.getDataType());
        if (runnerMap.containsKey(key)) {
            return;
        }
        InternalRunner internalRunner = new InternalRunner(channel, dlt645Message, 2000);
        asyncExecutor.execute(internalRunner);
        runnerMap.put(key, internalRunner);
    }

    public void terminate(DLT645Message dlt645Message) {
        String key = dlt645Message.getAddress() + Arrays.toString(dlt645Message.getDataType());
        Optional.ofNullable(runnerMap.remove(key)).ifPresent(InternalRunner::terminate);
    }

    static class InternalRunner implements Runnable {
        private final AtomicBoolean keepRunning = new AtomicBoolean(true);
        private final long recoverDuration;
        private final Object waiter = new Object();
        private final Channel channel;
        private final DLT645Message dlt645Message;

        public InternalRunner(Channel channel, DLT645Message dlt645Message, long recoverDuration) {
            this.channel = channel;
            this.dlt645Message = dlt645Message;
            this.recoverDuration = recoverDuration;
        }

        @Override
        public void run() {
            while (keepRunning.get()) {
                channel.writeAndFlush(dlt645Message);
                synchronized (waiter) {
                    try {
                        waiter.wait(recoverDuration);
                    } catch (InterruptedException e) {
                        keepRunning.set(true);
                        log.warn("send meter code error", e);
                    }
                }
            }
        }

        void terminate() {
            keepRunning.set(false);
            synchronized (waiter) {
                waiter.notifyAll();
            }
        }
    }
}
