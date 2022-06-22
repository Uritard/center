package com.yjh.accessvideo.common.mqtt.alarmMsgBody;

import lombok.Data;

@Data
public class Defect {
    String type;
    int x1;
    int y1;
    int x2;
    int y2;
    int confidence;
    String desc;
}
