package com.yjh.accessmeter.protocol.aigateway.info;

import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Map;

/**
 * @Author: lqh
 * @Date: 2024/10/22
 */
@Data
public class ControlReq {
    private String msgType;
    private String mid;
    private String serviceId;
    private String cmd;
    private String deviceId;
    private Map<String,String> paras;

}
