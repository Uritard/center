package com.yjh.accessvqd.netty.server;

import com.yjh.accessvqd.module.diagnose.dao.TDiagnosePlanDao;
import com.yjh.accessvqd.module.diagnose.service.ChanResultService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/*import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import io.netty.handler.ipfilter.RuleBasedIpFilter;*/


/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class VqdServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RedisTemplate redisTemplate;
    private ChanResultService chanResultService;
    private TDiagnosePlanDao tDiagnosePlanDao;
    private int dataKey;

    public VqdServerChannelInitializer(ChanResultService chanResultService, RedisTemplate redisTemplate, TDiagnosePlanDao tDiagnosePlanDao,int dataKey) {
        this.redisTemplate = redisTemplate;
        this.chanResultService = chanResultService;
        this.tDiagnosePlanDao = tDiagnosePlanDao;
        this.dataKey=dataKey;
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

        VqdServerHandler vqdServerHandler = new VqdServerHandler();
        vqdServerHandler.setRedisTemplate(redisTemplate);
        vqdServerHandler.setChanResultService(chanResultService);
        vqdServerHandler.settDiagnosePlanDao(tDiagnosePlanDao);
        vqdServerHandler.setDataKey(dataKey);
        channel.pipeline().addLast(vqdServerHandler);

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(null != ctx.pipeline().channel()){
            log.info("IEC104ServerChannelInitializer channelInactive is not null ,remove ");
        }else{
            log.info("IEC104ServerChannelInitializer channelInactive is null");
        }
        ctx.close().sync();
        ctx.flush();
    }
}
