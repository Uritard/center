package com.yjh.accessvideo.common.mqtt;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * MQTT服务类
 *
 * @author jeff
 * @since 2022/04/11
 */
@Service
@Slf4j
public class MqttUtilsServer {

    @Value("${mqtt.host}")
    private String host;
    @Value("${mqtt.serverClientId}")
    private String serverClientId;
    @Value("${mqtt.user}")
    private String user;
    @Value("${mqtt.pwd}")
    private String pwd;

    private
    MqttClient mqttClient;

    MqttUtilsServer(@Value("${mqtt.host}") String host, @Value("${mqtt.serverClientId}") String serverClientId
            , @Value("${mqtt.user}") String user, @Value("${mqtt.pwd}") String pwd) {
        log.info("start init mqttserver");
        try {
            mqttClient = new MqttClient(host, serverClientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setUserName(user);
            options.setPassword(pwd.toCharArray());
            // 设置超时时间
            options.setConnectionTimeout(60);
            // 设置会话心跳时间
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(true);
            mqttClient.setCallback(new PushCallback());
            mqttClient.connect(options);
            // return this.mqttTopic;
        } catch (Exception e) {
            log.error("connetct mqttserver-异常\nclientId:{}\ntheme:{}\nexception:{}", serverClientId, e.toString());
        }
        //  return null;

    }

    /**
     * 订阅主题
     */
    public boolean subscribe(String theme) {
        if (!this.mqttClient.isConnected()) {
            log.info("订阅重新连接");
            this.reconnection();
        }
        int[] qos = {1};
        String[] topic1 = {theme};
        try {
            mqttClient.subscribe(topic1, qos);
            log.info("get message");
            return true;

        } catch (Exception e) {
            log.error("推送消息--异常\nclientId:{}\nexception:{}", e.toString());
            return false;
        }
    }


    /**
     * 推送消息
     */
    public boolean pushMsg(String theme, Object msg,int qos) {
        if (!this.mqttClient.isConnected()) {
            log.info("连接断开，重连接");
            this.reconnection();
        }
        MqttMessage message = new MqttMessage();
        //保证消息能到达一次
        message.setQos(qos);
        message.setRetained(true);
        byte[] msgbytes = JSON.toJSONString(msg).getBytes();
        message.setPayload(msgbytes);
        try {
            log.info("pubushi:{}", JSON.toJSONString(msg));
            MqttTopic mqtttopic = this.mqttClient.getTopic(theme);
            MqttDeliveryToken token = mqtttopic.publish(message);
            token.waitForCompletion();
            if (!token.isComplete()) {
                log.error("推送消息--失败--msg：{}", JSON.toJSONString(message));
                return false;
            }
        } catch (Exception e) {
            log.error("推送消息--异常\nclientId:{}\nexception:{}", e.toString());
        }
        return true;
    }

    public void shutdown() {
        try {
            this.mqttClient.disconnect();
        } catch (Exception e) {
            log.error("关闭连接\nclientId:{}\nexception:{}", e.toString());
        }
    }

    public void reconnection() {
        log.info("reconnection mqtt");
        try {
            mqttClient = new MqttClient(host, serverClientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setUserName(user);
            options.setPassword(pwd.toCharArray());
            // 设置超时时间
            options.setConnectionTimeout(60);
            // 设置会话心跳时间
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(true);
            mqttClient.setCallback(new PushCallback());
            mqttClient.connect(options);
            // return this.mqttTopic;
        } catch (Exception e) {
            log.error("connetct mqttserver-异常\nclientId:{}\ntheme:{}\nexception:{}", serverClientId, e.toString());
        }

    }

}