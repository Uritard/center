package com.yjh.accessrobot.common;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constant {

    //心跳报文
    public static final byte HEARTBEAT = 0x03;

    public static Map<String, ChannelHandlerContext> maps = new HashMap<>();

    public static String Packet = "";

    public static long sendSessionId = 0L;//发送会话序列号

    public static Map<String, List<Long>> flagMap = new HashMap<>();


}
