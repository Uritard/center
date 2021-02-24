package com.yjh.accessvideo.common;

import com.yjh.accessvideo.common.logs.SpringBeanUtils;
import com.yjh.accessvideo.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.module.device.entity.XMLBaseModel;
import io.netty.bootstrap.Bootstrap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class Constant {

    public static Map<String, Integer> maps = new ConcurrentHashMap<>();

    public static Map<String, String> mapsForCamera = new HashMap<>();

    public static Map<String, String> mapsForRobot = new HashMap<>();

    public static int connectTimeCounts = 0;

    //心跳报文
    public static final byte TYPET3 = 0x03;

    public static String sdkPath;//sdk路径

    public static String TASKID="";
    public static String INSTANCEID="";
    public static  AtomicInteger FLAG;
//    public static AtomicInteger ABNORMAL=new AtomicInteger(0);
//    public static AtomicInteger NORMAL=new AtomicInteger(0);
////    public static AtomicInteger NORMAL; //线程安全Integer
////    public static Set<String> cruiseKeys=new HashSet<>();
//    public static CopyOnWriteArraySet<String> cruiseKeys=new CopyOnWriteArraySet<>();//线程安全Set


    public static Map<Integer, Bootstrap> bootstrapHashMap = new HashMap<>();

    public static<T> Result otherServer(Map<String, List<T>> map, String url) throws Exception{
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        return re;
    }
    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";


    public static<T> Result otherServerList( List<T> list, String url) throws Exception{
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, list, Result.class);
        return re;
    }
    public static final String TASK_FINISH="http://iot-center-platform/tCruiseDataResult/v1/updateCruiseAnalyze?cruiseResultIdList";
}
