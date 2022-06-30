package com.yjh.accessvideo.common.mqtt.alarmMsgBody;

import lombok.Data;

import java.util.List;

@Data
public class AlarmMqttMsg {
    String province_name;
    String city_name;
    int volt_level;
    String station_name;
    String section_ip;
    String node_id;
    String msg_type;
    List<Alarm> alarm;

}
