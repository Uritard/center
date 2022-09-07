//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.yjh.demo.ws;

import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.demo.ws.message.BatchTaskMessage;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.MessageTypeRegistry;


import java.util.HashMap;
import java.util.Map;

public class WsMessageTypeRegistry implements MessageTypeRegistry {
    private static final Map<String, Class<? extends BaseMessage>> MSG_TYPE_CLASS_MAP = new HashMap();
    private static final Map<Class<? extends BaseMessage>, String> CLASS_MSG_TYPE_MAP = new HashMap();

    public WsMessageTypeRegistry() {
    }

    public Object getMsgType(BaseMessage message) {
        return CLASS_MSG_TYPE_MAP.get(message.getClass());
    }

    public Class<? extends BaseMessage> getMessageClass(Object msgType) {
        if (!(msgType instanceof String)) {
            throw new IllegalArgumentException("msgType should be String");
        } else {
            return (Class)MSG_TYPE_CLASS_MAP.get((String)msgType);
        }
    }

    static {
        MSG_TYPE_CLASS_MAP.put("0003", AInterfaceMessage.class);
        MSG_TYPE_CLASS_MAP.put("0004", BatchTaskMessage.class);

        MSG_TYPE_CLASS_MAP.forEach((robotMsgType, aClass) -> {
            CLASS_MSG_TYPE_MAP.put(aClass, robotMsgType);
        });
    }
}
