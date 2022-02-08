package com.yjh.accessrobot.netty.server;

import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;

import java.net.InetSocketAddress;

public class IpFilterRuleHandler implements IpFilterRule {

    @Override
    public boolean matches(InetSocketAddress remoteAddress) {

        // ip转成long类型
        String ip = remoteAddress.getHostString();
        long ipLong = Long.parseLong(ip);

        // 创建一个要过滤的ip段
        for (int i=1; i <= 255; i++) {
            IpRange ipRange = new IpRange("100.125." + i + ".1", "100.125." + i + ".255");
            long ipStart = Long.parseLong(ipRange.getIpStart());
            long ipEnd = Long.parseLong(ipRange.getIpEnd());
            // 比较ip区间
            if (ipLong >= ipStart && ipLong <= ipEnd) {
                // 返回true则执行过滤器
                return true;
            }
        }
        // 返回false表示不执行过滤器
        return false;

    }

    @Override
    public IpFilterRuleType ruleType() {
        // 返回拒绝则表示拒绝连接，反之返回接受则表示可以连接
        return IpFilterRuleType.REJECT;
    }

}
