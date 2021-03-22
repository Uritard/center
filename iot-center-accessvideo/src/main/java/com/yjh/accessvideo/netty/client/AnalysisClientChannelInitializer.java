package com.yjh.accessvideo.netty.client;

import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by tt on 2019/7/31.
 */
public class AnalysisClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RedisTemplate redisTemplate;
    private AnalyseDataOperateService analyseDataOperateService;
    private String syncWebsocketUrl;
    public AnalysisClientChannelInitializer(RedisTemplate redisTemplate,AnalyseDataOperateService analyseDataOperateService,String syncWebsocketUrl) {
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateService=analyseDataOperateService;
        this.syncWebsocketUrl=syncWebsocketUrl;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        //p.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        //p.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        p.addLast(new AnalysisClientHandler(redisTemplate,analyseDataOperateService,syncWebsocketUrl));
    }
}
