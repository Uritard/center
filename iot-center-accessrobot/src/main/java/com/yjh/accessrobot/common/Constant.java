package com.yjh.accessrobot.common;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class Constant {

    //心跳报文
    public static final byte HEARTBEAT = 0x03;

    public static Map<String, ChannelHandlerContext> maps = new HashMap<>();

    public static final String filePath = "/home/yjh_iot_center/ftps/";
}
