package com.yjh.accessvideo.common;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class Constant {

    public static final String USER_COUNT = "statistics:userCount";

    public static final String DEVICE_COUNT = "statistics:deviceCount";

    public static final String SZ_COUNT = "statistics:szCount";

    public static final String API_COUNT = "statistics:apiCount";

    public static Map<String, Integer> maps = new HashMap<>();

    public static Map<String, String> mapsForCamera = new HashMap<>();

    public static int connectTimeCounts = 0;
}
