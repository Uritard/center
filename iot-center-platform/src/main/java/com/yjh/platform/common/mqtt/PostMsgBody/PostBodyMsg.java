package com.yjh.platform.common.mqtt.PostMsgBody;

import lombok.Data;

@Data
public class PostBodyMsg {
    String topic;
    Object object;
}
