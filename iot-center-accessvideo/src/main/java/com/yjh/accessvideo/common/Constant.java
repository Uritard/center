package com.yjh.accessvideo.common;

import java.util.HashMap;
import java.util.Map;

public class Constant {

    public static Map<String, Integer> maps = new HashMap<>();

    public static Map<String, String> mapsForCamera = new HashMap<>();

    public static int connectTimeCounts = 0;

    //心跳报文
    public static final byte TYPET3 = 0x03;
}
