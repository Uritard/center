package com.yjh.accesstcp.netty.handler;

import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.BaseModel;
import com.yjh.accesstcp.netty.entiy.MessageHeader;

/**
 * @author YChen
 * @date 2021/12/14
 */
public interface MessageHandlerStrategy<T extends BaseModel> {

    /**
     * 根据type区别不同的消息进行处理
     *
     * @param clientHandler    消息处理类
     * @param xmlBaseModel     接收消息
     * @param header    消息头信息，如会话序列号
     */
    void handler(TCPClientHandler clientHandler, T xmlBaseModel, MessageHeader header);
}
