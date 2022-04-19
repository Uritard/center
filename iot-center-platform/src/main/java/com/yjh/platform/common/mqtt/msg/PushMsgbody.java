package com.yjh.platform.common.mqtt.msg;

import lombok.Data;

@Data
public class PushMsgbody {
    String topic;
    Object object;
}
