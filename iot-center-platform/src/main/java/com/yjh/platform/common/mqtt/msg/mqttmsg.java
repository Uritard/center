package com.yjh.platform.common.mqtt.msg;


import lombok.Data;

/**
 * @author:jeff
 * @Description: 心跳消息
 */
@Data
public class mqttmsg {
    String province_name;
    String city_name;
    String volt_level;
    String  station_name;
    String section_ip;
    String node_id;
    String msg_type;
    String time;


//"province_name":"江苏"
//        "city_name":"南京",
//        "volt_level":500,
//        "station_name":"220kVXX 变电站",
//        "section_ip":"10.10.10.10",
//        "node_id":"边缘节点标识",
//        "msg_type":"heart",
//        "time":"2021-11-23 10:15:30"

}
