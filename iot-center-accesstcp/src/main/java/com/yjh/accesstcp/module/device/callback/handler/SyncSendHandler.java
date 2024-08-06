package com.yjh.accesstcp.module.device.callback.handler;

import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.TCPClientHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@RequiredArgsConstructor
@Slf4j
public class SyncSendHandler {

    private final TCPClientHandler tcpClientHandler;
    private final Object waiter = new Object();

    public static Map<Long, Object> waitingMap = new ConcurrentHashMap<>();

    public static Map<Long, List<Map<String, Object>>> msgMap = new ConcurrentHashMap<>();

    public List<Map<String, Object>> send(XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId, boolean isSend) {
        waitingMap.put(sendSessionId, waiter);
        tcpClientHandler.send(xmlBaseModel, sendSessionId, receiveSessionId, isSend);
        try {
            synchronized (waiter) {
                waiter.wait(10000);
            }
        } catch (InterruptedException e) {
            log.error("同步发送报文错误", e);
        }
        return msgMap.remove(sendSessionId);
    }
}
