package com.yjh.accessvideo.common.mqtt;

import com.yjh.accessvideo.common.logs.SpringBeanUtils;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.AlarmMqttMsg;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.PostBodyMsg;
import com.yjh.accessvideo.commons.restTemplate.ServiceRestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
public class AlarmService {
    public static final String HEART = "heart";
    @Value("${mqtt.topic}")
    private String Topic;

    @Value("${mqtt.province_name}")
    private String provinceName;

    @Value("${mqtt.city_name}")
    private String cityName;

    @Value("${mqtt.station_name}")
    private String stationName;

    @Value("${mqtt.section_ip}")
    private String sectionIP;

    @Value("${mqtt.node_id}")
    private String nodeId;

    @Value("${mqtt.volt_level}")
    private int voltLevel;
    private AlarmMqttMsg alarmMqttMsg;
    private PostBodyMsg postBodyMsg;
    private void  getMessgerInfo() {
        alarmMqttMsg =new AlarmMqttMsg();
        postBodyMsg=  new PostBodyMsg();
        postBodyMsg.setTopic(Topic);
        alarmMqttMsg.setMsg_type("alarm");
        alarmMqttMsg.setProvince_name(encode(provinceName));
        alarmMqttMsg.setCity_name(encode(cityName));
        alarmMqttMsg.setStation_name(encode(stationName));
        alarmMqttMsg.setSection_ip(sectionIP);
        alarmMqttMsg.setNode_id(nodeId);
        alarmMqttMsg.setVolt_level(voltLevel);
        alarmMqttMsg.setAlarm(new ArrayList<>());
    }
    private String encode(String str){
        return new String(str.getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
    }

    /**
     * 转发给platform的mqtt/postMqttMsg接口。后续需要各模块统一一下，是否都走platform
     * @param alarm 告警具体内推，再这里根据alarm拼装mqqt的告警消息postBodyMsg
     */
    public void PushMsg(Alarm alarm ){
        getMessgerInfo();
        System.out.println("cityname---:"+alarmMqttMsg.getCity_name());
        alarmMqttMsg.addAlarm(alarm);
        final String MQTT_URL = "http://iot-center-platform/mqtt/postMqttMsg";
        ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        postBodyMsg.setObject(alarmMqttMsg);
        if (null != serviceRestTemplate) {
            serviceRestTemplate.postForObject(MQTT_URL, postBodyMsg, Boolean.class);
        }

    }
}
