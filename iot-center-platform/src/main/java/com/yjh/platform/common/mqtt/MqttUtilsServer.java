package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT服务类
 *
 * @author jeff
 * @since 2022/04/11
 */
@Slf4j
public class MqttUtilsServer {

    private String host;
    private String serverClientId;
    private String user;
    private String pwd;

    private MqttClient mqttClient;

    public MqttUtilsServer(String host, String serverClientId, String user, String pwd) {
        log.info("start init mqttserver");
        this.host = host;
        this.serverClientId = serverClientId;
        this.user = user;
        this.pwd = pwd;
        initClient(host, serverClientId, user, pwd);
    }

    private void initClient(String host, String serverClientId, String user, String pwd) {
        try {
            this.mqttClient = new MqttClient(host, serverClientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setUserName(user);
            options.setPassword(pwd.toCharArray());
            // 设置超时时间
            options.setConnectionTimeout(60);
            // 设置会话心跳时间
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(true);
            this.mqttClient.setCallback(new PushCallback());
            this.mqttClient.connect(options);
        } catch (Exception e) {
            log.error("connetct mqttserver-异常\nclientId:{}\nexception:{}", serverClientId, e.getMessage());
        }
    }

    /**
     * 订阅主题
     */
    public boolean subscribe(String topic) {
        if (!this.mqttClient.isConnected()) {
            log.info("订阅重新连接");
            this.reconnection();
        }
        int[] qos = {1};
        String[] topic1 = {topic};
        try {
            this.mqttClient.subscribe(topic1, qos);
            log.info("get message");
            return true;
        } catch (Exception e) {
            log.error("订阅消息--异常\nclientId:{}\nexception:{}", mqttClient.getClientId(), e.getMessage());
            return false;
        }
    }


    /**
     * 推送消息
     */
    public boolean pushMsg(String topic, Object msg, int qos) {
        if (!this.mqttClient.isConnected()) {
            log.info("连接断开，重连接");
            this.reconnection();
        }
        MqttMessage message = new MqttMessage();
        //保证消息能到达一次
        message.setQos(qos);
        message.setRetained(true);
        String jsonStr = JSONUtil.toJSONString(msg);
        byte[] msgBytes = jsonStr.getBytes();
        message.setPayload(msgBytes);
        try {
            log.info("发送MQTT消息, 话题:{}, 消息:{},发送给：{}", topic, jsonStr,host);
            MqttTopic mqtttopic = this.mqttClient.getTopic(topic);
            MqttDeliveryToken token = mqtttopic.publish(message);
            token.waitForCompletion();
            if (!token.isComplete()) {
                log.error("推送消息--失败--msg：{}", jsonStr);
                return false;
            }
        } catch (Exception e) {
            log.error("推送消息--异常\nclientId:{}\nexception:{}", mqttClient.getClientId(), e.getMessage());
        }
        return true;
    }

    public void shutdown() {
        try {
            this.mqttClient.disconnect();
        } catch (Exception e) {
            log.error("关闭连接\nclientId:{}\nexception:{}", mqttClient.getClientId(), e.getMessage());
        }
    }

    public void reconnection() {
        log.info("reconnection mqtt");
        initClient(host, serverClientId, user, pwd);
    }

}
