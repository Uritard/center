package com.yjh.accesstcp.netty.entiy;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 〈功能详细描述〉
 *
 * @author fei23
 * @date 2022-5-8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class Message {
    /**
     * 发送会话序列号, long(八字节小头字节序)
     */
    private long sendSessionId;

    /**
     * 接收会话序列号, long(八字节小头字节序)
     */
    private long receiveSessionId;

    /**
     * 会话源标识, 一个字节。
     * Ox00标识会话请求， 0x01标识会话响应，其它异常会话。
     */
    private byte sessionType;

    /**
     * 交互内容（xml格式） xml，字符编码为UTF-8
     */
    private byte[] content;

    /**
     * 发送的报文的长度
     */
    private int length;

}
