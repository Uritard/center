package com.yjh.demo.ws;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * websocket连接会话管理
 *
 * @author zilong
 * @date 2022/7/20
 */
@Component
public class WsSessionManager {
    private final ConcurrentMap<Long, Boolean> sessionMap = new ConcurrentHashMap<>();

    public boolean hasAnyClient() {
        return !sessionMap.isEmpty();
    }

    public void addSession(long sessionId) {
        sessionMap.put(sessionId, Boolean.TRUE);
    }

    public void removeSession(long sessionId) {
        sessionMap.remove(sessionId);
    }
}
