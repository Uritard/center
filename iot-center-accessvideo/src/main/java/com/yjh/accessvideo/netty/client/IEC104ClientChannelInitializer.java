package com.yjh.accessvideo.netty.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by tt on 2019/7/31.
 */
public class IEC104ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private String gatewayName;
    private int changeDataNum;

    public IEC104ClientChannelInitializer() { }

    public IEC104ClientChannelInitializer(String gatewayName, int changeDataNum) {
        this.gatewayName = gatewayName;
        this.changeDataNum = changeDataNum;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        //p.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        //p.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        p.addLast(new IEC104ClientHandler(gatewayName, changeDataNum));
    }
}
