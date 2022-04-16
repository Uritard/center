package com.yjh.platform.common.mqtt;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 回调方法
 */
@Slf4j
public class PushCallback implements MqttCallback {
    @Override
    public void connectionLost(Throwable cause) {
        //连接丢失后，一般在这里面进行重连
        log.info("PushCallback--连接断开" + JSON.toJSONString(cause));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (!token.isComplete()) {
            log.info("PushCallback--deliveryComplete--异常：" + JSON.toJSONString(token));
        }
    }

    @Override
    public void messageArrived(String theme, MqttMessage message) {
        try {
            log.info("【订阅回调】\ttheme：{}\tqos：{}\npayload：{}", theme, message.getQos(), new String(message.getPayload()));
        } catch (Exception e) {
            log.error("【订阅回调】--异常\ttheme：{}\nmessage：{}", theme, JSON.toJSONString(message));
        }
    }
}
