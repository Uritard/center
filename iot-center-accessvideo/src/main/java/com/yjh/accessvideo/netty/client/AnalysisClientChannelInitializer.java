package com.yjh.accessvideo.netty.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by tt on 2019/7/31.
 */
public class AnalysisClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    public AnalysisClientChannelInitializer() { }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        //p.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        //p.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        p.addLast(new AnalysisClientHandler());
    }
}
