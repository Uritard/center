package com.yjh.accessvideo.common.websocket;

import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.hik.transmit.VoiceTransConstant;
import com.yjh.accessvideo.module.control.service.VoiceComService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tt
 */
@ServerEndpoint("/wsvoice/{userId}/{token}/{deviceId}")
@Component
public class WebSocketServer {

    private static Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private static VoiceComService voiceComService;

    private BufferedOutputStream bos = null;

    /**静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。*/
    private static AtomicInteger onlineCount = new AtomicInteger(0);
    /**concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。*/
    private static Map<String,WebSocketServer> webSocketMap = new ConcurrentHashMap<>();

    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    /**接收userId*/
    private String userId="";
    /**接收tokenId*/
    private String token="";
    /**接收deviceId*/
    private String deviceId="";

    @Autowired
    public void setVoiceComService(VoiceComService voiceComService) {
        WebSocketServer.voiceComService = voiceComService;
    }

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("token") String token, @PathParam("deviceId") String deviceId) {
        this.session = session;
        this.userId = userId;
        this.token = token;
        this.deviceId = deviceId;
        String key = userId + "_" + token + "_" + deviceId;
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
        log.info("用户连接: {},当前在线人数为: {}", userId, getOnlineCount());

        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.error("用户: {},网络异常!!!!!!", userId);
        }
        initFileStream();
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        String key = userId + "_" + token + "_" + deviceId;
        if(webSocketMap.containsKey(key)){
            webSocketMap.remove(key);
            //从set中删除
            subOnlineCount();
        }
        log.info("用户退出: {},当前在线人数为: {}", userId, getOnlineCount());

        IOUtils.closeQuietly(bos);
        // 刷新页面断开所有设备的语音对讲
        voiceComService.stopVoiceTrans(VoiceTransConstant.CONSTANT, new Result());
    }

    /**
     * 初始化文件流
     */
    private void initFileStream() {
        String filePathTemp = System.getProperty("user.dir");
        String filePathName = filePathTemp + "/AudioFile/cnm.pcm";
        FileOutputStream fos = null;
        File file = new File(filePathName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            fos = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        bos = new BufferedOutputStream(fos);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param messageStr 客户端发送过来的音频数据
     */
    @OnMessage(maxMessageSize = 40960)
    public void onMessage(String messageStr) {
        // log.info("deviceId:{},data length:{}", this.deviceId, messageStr.length());
        try {
            // 前端发送的数据前面一段为0,去除无效数据
            byte[] message = Base64.getDecoder().decode(messageStr);
            if (Constant.logUpLv3()) {
                // 方便定位问题
                bos.write(message);
            }

            log.info("Sending data to access video service...{}, {}", this.deviceId, message.length);
            voiceComService.receiveAndSendVoiceData(message, NumberUtils.toLong(this.deviceId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

    public static void sendMsgBuffer(ByteBuffer bytesAtTime, String deviceId){
        webSocketMap.forEach((k, v) -> {
            if (StringUtils.equals(v.deviceId, deviceId)) {
                v.session.getAsyncRemote().sendBinary(bytesAtTime);
            }
        });
    }

    /**
     * 发送自定义消息
     * */
    public static void sendInfo(String message, @PathParam("userId") String uid) {
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
        return onlineCount.get();
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount.incrementAndGet();
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount.decrementAndGet();
    }

}
