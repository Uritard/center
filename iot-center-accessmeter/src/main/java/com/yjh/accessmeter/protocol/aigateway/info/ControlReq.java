package com.yjh.accessmeter.protocol.aigateway.info;

import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @Author: lqh
 * @Date: 2024/10/22
 */
@Data
public class ControlReq extends MqttMessage {
    private String gatewayId;
    private String type;
    private Detail detail;
    private String value;
    private Long timestamp;

    @Data
    public static class Detail{
        private String dotName;
        private String nodeId;
    }
}
