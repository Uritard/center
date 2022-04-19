package com.yjh.accessvideo.common;

import com.alibaba.fastjson.JSON;
import com.sun.jna.NativeLong;
import com.yjh.accessvideo.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.hik.HCNetSDK;
import io.netty.bootstrap.Bootstrap;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Constant {

    public static Map<String, Integer> maps = new ConcurrentHashMap<>();

    public static Map<Long, HCNetSDK.NET_DVR_DEVICEINFO_V40> deviceMaps = new ConcurrentHashMap<>();

    public static Map<String, String> mapsForCamera = new HashMap<>();

    public static Map<String, String> mapsForHistory = new HashMap<>();

    /**
     录像信息
     */
    public static Map<String, NativeLong> recordLongMap = new HashMap<>();

//    public static int connectTimeCounts = 0;

    //心跳报文
    public static final byte TYPET3 = 0x03;

//    public static String sdkPath;

    public static String TASKID="";
    public static String INSTANCEID="";
//    public static  AtomicInteger FLAG;
//    public static AtomicInteger ABNORMAL=new AtomicInteger(0);
//    public static AtomicInteger NORMAL=new AtomicInteger(0);
//    public static AtomicInteger NORMAL; //线程安全Integer
//    public static Set<String> cruiseKeys=new HashSet<>();
//    public static CopyOnWriteArraySet<String> cruiseKeys=new CopyOnWriteArraySet<>();//线程安全Set


    public static Map<Integer, Bootstrap> bootstrapHashMap = new HashMap<>();

    public static boolean apiPermissions;

    public static String WEBSOCKET_URL="";

    public static RedisTemplate redisTemplate;

    public static String websocketSendMsg(String url, Map<String,String>map) throws IOException {
        //将websocket信息写入redis
        String json= JSON.toJSONString(map);
        //WebSocketServer.sendMsg(json);
        // String.valueOf(map);
        if("logError".equals(map.get("type")) || "newTask".equals(map.get("type")) || "newLinkage".equals(map.get("type"))
                || "newAlarm".equals(map.get("type"))  || "alarmPopUp".equals(map.get("type"))  || "linkagePopUp".equals(map.get("type"))){
            redisTemplate.opsForValue().set(map.get("type"),String.valueOf(map),5, TimeUnit.MINUTES);
        }

        CloseableHttpClient client = HttpClients.createDefault();
        String result = "";
        try {
            URI uri = new URIBuilder(url).setParameter("json", json).build();
            HttpPost httpGet = new HttpPost(uri);
            httpGet.addHeader("Content-type", "application/json;charset=utf-8");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {e.getMessage();}
        return result;
        //return "666";
    }

    public static<T> Result otherServer(Map<String, List<T>> map, String url) throws Exception{
        Result re = new Result();
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);//江苏要求
        return re;
    }
    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";


    public static final String GET_LOW_TASK_GO_ON = "http://iot-center-platform-qh/tCruiseTask/v1/lowTaskGoOn";

    public static<T> Result otherServerList( List<T> list, String url) {
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, list, Result.class);
        return re;
    }

    public static final String TASK_FINISH="http://iot-center-platform/tCruiseDataResult/v1/updateCruiseAnalyze?cruiseResultIdList";
}
