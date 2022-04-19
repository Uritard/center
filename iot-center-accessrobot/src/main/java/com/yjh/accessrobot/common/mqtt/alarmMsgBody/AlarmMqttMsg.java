package com.yjh.accessrobot.common.mqtt.alarmMsgBody;

import lombok.Data;

@Data
public class AlarmMqttMsg {
    String province_name;
    String city_name;
    String volt_level;
    String  station_name;
    String section_ip;
    String node_id;
    String msg_type;
    Alarm alarm;

}
