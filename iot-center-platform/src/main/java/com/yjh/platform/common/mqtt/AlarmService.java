package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.mqtt.PostMsgBody.PostBodyMsg;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.AlarmMqttMsg;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
public class AlarmService {
    public static final String HEART = "heart";
    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.province_name}")
    private String provinceName;

    @Value("${mqtt.city_name}")
    private String cityName;

    @Value("${mqtt.station_name}")
    private String stationName;
    @Value("${mqtt.section_name}")
    private String sectionName;

    @Value("${mqtt.section_ip}")
    private String sectionIP;

    @Value("${mqtt.node_id}")
    private String nodeId;

    @Value("${mqtt.volt_level}")
    private int voltLevel;

    @Qualifier("algorithmMqtt")
    @Autowired
    MqttUtilsServer mqttUtilsServer;

    private PostBodyMsg postBodyMsg;
    private AlarmMqttMsg getMessgerInfo() {
        AlarmMqttMsg alarmMqttMsg =new AlarmMqttMsg();
        postBodyMsg=  new PostBodyMsg();
        postBodyMsg.setTopic(topic);
        alarmMqttMsg.setMsg_type("alarm");
        alarmMqttMsg.setProvince_name(encode(provinceName));
        alarmMqttMsg.setCity_name(encode(cityName));
        alarmMqttMsg.setStation_name(encode(stationName));
        alarmMqttMsg.setSection_name(encode(sectionName));
        alarmMqttMsg.setSection_ip(sectionIP);
        alarmMqttMsg.setNode_id(encode(nodeId));
        alarmMqttMsg.setVolt_level(voltLevel);
        alarmMqttMsg.setAlarm(new ArrayList<>());

        return alarmMqttMsg;
    }

    private String encode(String str){
        return str;
    }
    /**
     * 转发给platform的mqtt/postMqttMsg接口。后续需要各模块统一一下，是否都走platform
     * @param alarm 告警具体内推，再这里根据alarm拼装mqqt的告警消息postBodyMsg
     */
    public void PushMsg(Alarm alarm){
        AlarmMqttMsg alarmMqttMsg = getMessgerInfo();
        System.out.println("cityname---:"+alarmMqttMsg.getCity_name());
        alarmMqttMsg.addAlarm(alarm);
        postBodyMsg.setObject(alarmMqttMsg);

        mqttUtilsServer.pushMsg(topic, alarmMqttMsg,1);

    }
}
