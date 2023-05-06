package com.yjh.gateway.common.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author tt
 */
@ServerEndpoint("/ws/{userId}/{token}")
@Component
public class WebSocketServer {

    private static Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    @Resource
    private RedisTemplate redisTemplate;

    /**静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。*/
    private static int onlineCount = 0;
    /**concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。*/
    private static HashMap<String,WebSocketServer> webSocketMap = new HashMap<>();
    public HashMap<String, WebSocketServer> getWebSocketMap() { return webSocketMap; }
    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    /**接收userId*/
    private String userId="";
    /**接收tokenId*/
    private String token="";

    //初始化类
    private final static WebSocketServer webSocketServer = new WebSocketServer();
    public static WebSocketServer getInstance() {
        return webSocketServer;
    }

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("token") String token) {
        this.session = session;
        this.userId = userId;
        this.token = token;
        String key = userId + "_" + token;
        if(webSocketMap.containsKey(key)){
            webSocketMap.remove(key);
            webSocketMap.put(key,this);
            //加入set中
        }else{
            webSocketMap.put(key,this);
            //加入set中
            addOnlineCount();
            //在线数加1
        }
        UserStatusChange.logIn(userId, token);
        log.info("用户连接: {},当前在线人数为: {}", userId, getOnlineCount());

        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.error("用户: {},网络异常!!!!!!", userId);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if(webSocketMap.containsKey(userId + "_" + token)){
            webSocketMap.remove(userId + "_" + token);
            //从set中删除
            subOnlineCount();
        }
        UserStatusChange.logOut(userId, token);
        log.info("用户退出: {},当前在线人数为: {}", userId, getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("用户消息:"+userId+",报文:"+message);
        //可以群发消息
        //消息保存到数据库、redis
        if(StringUtils.isNotBlank(message)){
            if(StringUtils.startsWith(message, "ping")){
                UserStatusChange.logIn(userId, token);
                return;
            }
            try {
                //解析发送的报文
                JSONObject jsonObject = JSON.parseObject(message);
                //追加发送人(防止串改)
                jsonObject.put("fromUserId",this.userId);
                String toUserId=jsonObject.getString("toUserId");
                //传送给对应toUserId用户的websocket
                if(StringUtils.isNotBlank(toUserId)&&webSocketMap.containsKey(toUserId)){
                    webSocketMap.get(toUserId).sendMessage(jsonObject.toJSONString());
                }else{
                    log.error("请求的userId:"+toUserId+"不在该服务器上");
                    //否则不在这个服务器上，发送到mysql或者redis
                }
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:"+this.userId+",原因:"+error.getMessage());
        log.error(error.getMessage(), error);
    }
    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        log.info("发送消息到:"+userId+"，报文:"+message);
        this.session.getBasicRemote().sendText(message);
    }

    public static void sendMsg(String message){
        webSocketMap.forEach((k,v) ->{
            v.session.getAsyncRemote().sendText(message);
        });
    }

    public static void sendMsgBuffer(ByteBuffer bytesAtTime){
        webSocketMap.forEach((k,v) ->{
            v.session.getAsyncRemote().sendBinary(bytesAtTime);
        });
    }

    /**
     * 发送自定义消息
     * */
    public static void sendInfo(String message, @PathParam("userId") String uid) throws IOException {
        log.info("发送消息到: {}，报文: {}", uid, message);
        if(StringUtils.isNotBlank(uid)){
            AtomicBoolean b = new AtomicBoolean(true);
            String uidPre = uid + "_";
            webSocketMap.forEach((k,v) ->{
                if(StringUtils.startsWith(k, uidPre)){
                    v.session.getAsyncRemote().sendText(message);
                    b.set(false);
                }
            });
            if(b.get()){
                log.info("用户 {} 不在线！", uid);
            }
        } else {
            log.info("用户 {} 不在线！", uid);
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

}
