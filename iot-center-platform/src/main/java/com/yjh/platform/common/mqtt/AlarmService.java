package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.mqtt.PostMsgBody.PostBodyMsg;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.AlarmMqttMsg;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.configuration.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
public class AlarmService {
    public static final String HEART = "heart";
    @Autowired
    private ApplicationProperties applicationProperties;

    @Qualifier("algorithmMqtt")
    @Autowired
    MqttUtilsServer mqttUtilsServer;

    private PostBodyMsg postBodyMsg;

    private AlarmMqttMsg getMessgerInfo() {
        AlarmMqttMsg alarmMqttMsg = new AlarmMqttMsg();
        postBodyMsg = new PostBodyMsg();
        postBodyMsg.setTopic(applicationProperties.getManagerMqttConfig().getMqttTopic());
        alarmMqttMsg.setMsg_type("alarm");
        alarmMqttMsg.setProvince_name(encode(applicationProperties.getManagerMqttConfig().getMqttProvinceName()));
        alarmMqttMsg.setCity_name(encode(applicationProperties.getManagerMqttConfig().getMqttCityName()));
        alarmMqttMsg.setStation_name(encode(applicationProperties.getManagerMqttConfig().getMqttStationName()));
        alarmMqttMsg.setSection_name(encode(applicationProperties.getManagerMqttConfig().getMqttSectionName()));
        alarmMqttMsg.setSection_ip(applicationProperties.getManagerMqttConfig().getMqttSectionIp());
        alarmMqttMsg.setNode_id(encode(applicationProperties.getManagerMqttConfig().getMqttNodeId()));
        alarmMqttMsg.setVolt_level(applicationProperties.getManagerMqttConfig().getVoltLevel());
        alarmMqttMsg.setAlarm(new ArrayList<>());

        return alarmMqttMsg;
    }

    private String encode(String str) {
        return str;
    }

    /**
     * 转发给platform的mqtt/postMqttMsg接口。后续需要各模块统一一下，是否都走platform
     *
     * @param alarm 告警具体内推，再这里根据alarm拼装mqqt的告警消息postBodyMsg
     */
    public void PushMsg(Alarm alarm) {
        AlarmMqttMsg alarmMqttMsg = getMessgerInfo();
        System.out.println("cityname---:" + alarmMqttMsg.getCity_name());
        alarmMqttMsg.addAlarm(alarm);
        postBodyMsg.setObject(alarmMqttMsg);

        mqttUtilsServer.pushMsg(applicationProperties.getManagerMqttConfig().getMqttTopic(), alarmMqttMsg, 1);

    }
}
