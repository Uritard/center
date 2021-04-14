package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.module.device.service.SysLogsService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
/*import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import io.netty.handler.ipfilter.RuleBasedIpFilter;*/
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class RobotServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RedisTemplate redisTemplate;
    private SysLogsService sysLogsService;
    private String serverName;
    private RobotService robotService;
    private String websocketUrl;

    public RobotServerChannelInitializer(String serverName, RedisTemplate redisTemplate, SysLogsService sysLogsService,RobotService robotService,String websocketUrl) {
        this.serverName = serverName;
        this.redisTemplate = redisTemplate;
        this.sysLogsService = sysLogsService;
        this.robotService = robotService;
        this.websocketUrl = websocketUrl;
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

        //SocketChannel建立连接后的管道
        RobotServerHandler robotServerHandler = new RobotServerHandler();
        robotServerHandler.setServerName(serverName);
        robotServerHandler.setRedisTemplate(redisTemplate);
        robotServerHandler.setSysLogsService(sysLogsService);
        robotServerHandler.setRobotService(robotService);
        robotServerHandler.setWebSocketUrl(websocketUrl);
        channel.pipeline().addLast(robotServerHandler);//3.配置通信数据的处理逻辑,可以addLast多个

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
