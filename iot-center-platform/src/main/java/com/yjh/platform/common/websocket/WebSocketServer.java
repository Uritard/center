package com.yjh.platform.common.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint("/webSocket/{group}")
@Component
@Slf4j
public class WebSocketServer {

    private static AtomicInteger onlineNum = new AtomicInteger();

    private static ConcurrentHashMap<String,WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    protected Session session;
    protected final String id = UUIDUtils.getUUID();

    @OnOpen
    public void onOpen(@PathParam("group") String group, Session session){// ws://192.168.9.40:18711/ws
        addOnlineCount();
        this.session = session;
        webSocketMap.put(this.id,this);
        log.info("当前人数为: {}",onlineNum);
    }

    @OnClose
    public void onClose(@PathParam("group") String group){
        subOnlineCount();
        webSocketMap.remove(this.id);
        log.info("断开连接，当前人数为: {}",onlineNum);
    }

    @OnError
    public void onError(Throwable throwable){
        log.error("websocket报错，内容:{}",throwable);
    }

    @OnMessage
    public void onMessage(String message){
        log.info("收到信息:{}",message);
    }

    public static void sendMessage(String message){
        webSocketMap.forEach((k,v) ->{
            v.session.getAsyncRemote().sendText(message);
        });
    }

    private static void addOnlineCount(){
        onlineNum.incrementAndGet();
    }

    private static void subOnlineCount() {
        onlineNum.decrementAndGet();
    }

}
