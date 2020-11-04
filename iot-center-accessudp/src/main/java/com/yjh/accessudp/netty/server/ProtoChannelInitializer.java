package com.yjh.accessudp.netty.server;

import io.netty.channel.ChannelPipeline;

public abstract class ProtoChannelInitializer {
    public abstract void initChannel(ChannelPipeline pipeline) throws Exception;
}
