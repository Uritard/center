package com.yjh.accessvideo.common.mqtt.alarmMsgBody;

import lombok.Data;

@Data
public class Defect {
    String type;
    String x1;
    String y1;
    String x2;
    String y2;
    String  confidence;
    String  desc;
}
