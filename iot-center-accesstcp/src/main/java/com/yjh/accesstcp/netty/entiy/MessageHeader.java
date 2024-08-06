package com.yjh.accesstcp.netty.entiy;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/16
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class MessageHeader {
    /**
     * (发送)会话序列号, long(八字节小头字节序)
     */
    private long sessionId;

    /**
     * 接收会话序列号, long(八字节小头字节序)
     */
    private long receiveSessionId;

    /**
     * 会话源标识, 一个字节。
     * Ox00标识会话请求， 0x01标识会话响应，其它异常会话。
     */
    private byte sessionType;
}
