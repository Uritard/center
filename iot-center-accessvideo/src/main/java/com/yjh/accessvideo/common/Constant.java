package com.yjh.accessvideo.common;

import io.netty.bootstrap.Bootstrap;

import java.util.*;

public class Constant {

    public static Map<String, Integer> maps = new HashMap<>();

    public static Map<String, String> mapsForCamera = new HashMap<>();

    public static int connectTimeCounts = 0;

    //心跳报文
    public static final byte TYPET3 = 0x03;

    public static String sdkPath;//sdk路径

    public static String TASKID="";
    public static String INSTANCEID="";
    public static Integer ABNORMAL=0;
    public static Integer NORMAL=0;
    public static Set<String> cruiseKeys=new HashSet<>();

    public static Map<Integer, Bootstrap> bootstrapHashMap = new HashMap<>();
}
