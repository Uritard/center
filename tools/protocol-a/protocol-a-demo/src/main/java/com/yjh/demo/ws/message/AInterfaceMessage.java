//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.yjh.demo.ws.message;

import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.Msg;
import com.yjh.messager.api.msg.Msg.Inbound;
import lombok.*;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class AInterfaceMessage extends BaseMessage implements Msg.Outbound {
    
    /**
     * 数据体
     */
    private AInterfaceData data;

    @Data
    @AllArgsConstructor
    @Accessors(chain = true)
    @NoArgsConstructor
    public static final class AInterfaceData {
        String xml;
    }
}
