package com.yjh.accessvideo.common.mqtt.alarmMsgBody;

import lombok.Data;

@Data
public class PostBodyMsg {
    String topic;
    Object object;
}
