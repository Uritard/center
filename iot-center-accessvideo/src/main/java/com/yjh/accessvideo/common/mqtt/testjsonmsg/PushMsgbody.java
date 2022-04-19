package com.yjh.accessvideo.common.mqtt.testjsonmsg;

import lombok.Data;

@Data
public class PushMsgbody {
    String topic;
    Object object;
}
