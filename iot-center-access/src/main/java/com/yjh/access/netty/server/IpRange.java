package com.yjh.access.netty.server;

public class IpRange {

    // 开始ip
    private String ipStart;

    // 结束ip
    private String ipEnd;

    //构造方法
    public IpRange(String ipStart, String ipEnd) {

        this.ipStart = ipStart;
        this.ipEnd = ipEnd;
    }

    public String getIpStart() {

        return ipStart;
    }

    public String getIpEnd() {

        return ipEnd;
    }

}

