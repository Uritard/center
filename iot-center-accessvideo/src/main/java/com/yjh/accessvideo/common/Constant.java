package com.yjh.accessvideo.common;

import io.netty.bootstrap.Bootstrap;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class Constant {

    public static Map<String, Integer> maps = new HashMap<>();

    public static Map<String, String> mapsForCamera = new HashMap<>();

    public static int connectTimeCounts = 0;

    //心跳报文
    public static final byte TYPET3 = 0x03;

    public static String sdkPath;//sdk路径

    public static String TASKID="";
    public static String INSTANCEID="";
    public static AtomicInteger ABNORMAL=new AtomicInteger(0);
    public static AtomicInteger NORMAL=new AtomicInteger(0);
//    public static AtomicInteger NORMAL; //线程安全Integer
//    public static Set<String> cruiseKeys=new HashSet<>();
    public static CopyOnWriteArraySet<String> cruiseKeys=new CopyOnWriteArraySet<>();//线程安全Set


    public static Map<Integer, Bootstrap> bootstrapHashMap = new HashMap<>();
}
