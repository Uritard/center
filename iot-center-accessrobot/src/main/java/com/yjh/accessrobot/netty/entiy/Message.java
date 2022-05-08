package com.yjh.accessrobot.netty.entiy;

/**
 * 〈功能详细描述〉
 *
 * @author fei23
 * @date 2022-5-8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
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

    public Message() {

    }

    public Message(long sendSessionId, long receiveSessionId, byte sessionType, int length) {
        this.sendSessionId = sendSessionId;
        this.receiveSessionId = receiveSessionId;
        this.sessionType = sessionType;
        this.length = length;
    }

    public long getSendSessionId() {
        return sendSessionId;
    }

    public void setSendSessionId(long sendSessionId) {
        this.sendSessionId = sendSessionId;
    }

    public long getReceiveSessionId() {
        return receiveSessionId;
    }

    public void setReceiveSessionId(long receiveSessionId) {
        this.receiveSessionId = receiveSessionId;
    }

    public byte getSessionType() {
        return sessionType;
    }

    public void setSessionType(byte sessionType) {
        this.sessionType = sessionType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
