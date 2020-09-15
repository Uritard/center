package com.yjh.accessvideo.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/*import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import io.netty.handler.ipfilter.RuleBasedIpFilter;*/

/*import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;*/

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class IEC104ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    //private Map<Object, IEC104ServerHandler> iec104ServerHandlerMap = new HashMap<Object, IEC104ServerHandler>();

    private RedisTemplate redisTemplate;
    private RedisTemplate redisTemplate2;

    public IEC104ServerChannelInitializer(RedisTemplate redisTemplate, RedisTemplate redisTemplate2) {
        this.redisTemplate = redisTemplate;
        this.redisTemplate2 = redisTemplate2;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //发送字符串
        //channel.pipeline().addLast("decoder",new StringDecoder(CharsetUtil.UTF_8));
        //channel.pipeline().addLast("encoder",new StringEncoder(CharsetUtil.UTF_8));
        //ip过滤
        //IpSubnetFilterRule rule1 = new IpSubnetFilterRule("192.168.8.1", 24, IpFilterRuleType.ACCEPT);
        //IpSubnetFilterRule rule2 = new IpSubnetFilterRule("100.125.*", 32, IpFilterRuleType.REJECT);
        //IpFilterRule acceptAll = new IpFilterRule() {
        //    @Override
        //    public boolean matches(InetSocketAddress remoteAddress) {
        //        return true;
        //    }
        //    @Override
        //    public IpFilterRuleType ruleType() {
        //        return IpFilterRuleType.ACCEPT;
        //    }
        //};
        //RuleBasedIpFilter filter = new RuleBasedIpFilter(rule2,acceptAll);
        //channel.pipeline().addLast("ipFilter", filter);
        //ip过滤

        IEC104ServerHandler iec104ServerHandler = new IEC104ServerHandler();
        iec104ServerHandler.setRedisTemplate(redisTemplate);
        iec104ServerHandler.setRedisTemplate2(redisTemplate2);
        //初始化不需要管理Map，注释掉
        //iec104ServerHandlerMap.put(channel.id(), iec104ServerHandler);
        channel.pipeline().addLast(iec104ServerHandler);

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(null != ctx.pipeline().channel()){
            log.info("IEC104ServerChannelInitializer channelInactive is not null ,remove ");
            //IEC104ServerHandler iec104ServerHandler = iec104ServerHandlerMap.get(ctx.pipeline().channel().id());
            //iec104ServerHandler = null;
            //iec104ServerHandlerMap.remove(ctx.pipeline().channel().id());
        }else{
            log.info("IEC104ServerChannelInitializer channelInactive is null");
        }
        ctx.close().sync();
        ctx.flush();
    }

    //public Map<Object, IEC104ServerHandler> getIec104ServerHandlerMap() { return iec104ServerHandlerMap; }

    /*public RedisTemplate getRedisTemplate() { return redisTemplate; }

    public void setRedisTemplate(RedisTemplate redisTemplate) { this.redisTemplate = redisTemplate; }

    public RedisTemplate getRedisTemplate2() { return redisTemplate2; }

    public void setRedisTemplate2(RedisTemplate redisTemplate2) { this.redisTemplate2 = redisTemplate2; }

    public DeviceService getDeviceService() { return deviceService; }

    public void setDeviceService(DeviceService deviceService) { this.deviceService = deviceService; }*/
}
