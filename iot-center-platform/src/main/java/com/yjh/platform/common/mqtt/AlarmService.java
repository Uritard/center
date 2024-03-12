package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.mqtt.PostMsgBody.PostBodyMsg;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.AlarmMqttMsg;
import com.yjh.platform.configuration.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
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
        PushMsg(Collections.singletonList(alarm));
    }

    public void PushMsg(List<Alarm> alarms) {
        if (applicationProperties.getManagerMqttConfig().isEnable()) {
            AlarmMqttMsg alarmMqttMsg = getMessgerInfo();
            log.info("cityname---: {}", alarmMqttMsg.getCity_name());
            alarmMqttMsg.addAllAlarm(alarms);
            postBodyMsg.setObject(alarmMqttMsg);

            mqttUtilsServer.pushMsg(applicationProperties.getManagerMqttConfig().getMqttTopic(), alarmMqttMsg, 2);
        }
    }
}
