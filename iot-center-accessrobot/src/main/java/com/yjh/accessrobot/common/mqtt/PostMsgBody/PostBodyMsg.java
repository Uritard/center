package com.yjh.accessrobot.common.mqtt.PostMsgBody;

import lombok.Data;

@Data
public class PostBodyMsg {
    String topic;
    Object object;
}
