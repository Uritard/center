package com.yjh.accessvideo.common;

import com.alibaba.fastjson.JSON;
import com.sun.jna.Pointer;
import com.yjh.accessvideo.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.hik.HCNetSDK;
import io.netty.bootstrap.Bootstrap;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author YJH
 */
@Slf4j
public class Constant {

    public static Map<String, Integer> maps = new ConcurrentHashMap<>();
    /**
     * 海康设备用户句柄集合
     */
    public static Map<Long, Integer> hikDeviceUserIdMaps = new ConcurrentHashMap<>();
    /**
     * 海康设备语音对讲句柄集合
     */
    public static Map<Integer, Integer> hikDeviceVoiceComHandleMaps = new ConcurrentHashMap<>();
    /**
     * 海康设备语音转发句柄集合
     */
    public static Map<Integer, Integer> hikDeviceVoiceTransHandleMaps = new ConcurrentHashMap<>();
    /**
     * 海康设备音频编码集合
     */
    public static Map<Long, Integer> hikDeviceEncodeFormatMaps = new ConcurrentHashMap<>();
    /**
     * 音频解码句柄
     */
    public static Pointer pDecHandle = null;
    /**
     * 系统架构
     */
    public static String SYSTEM_ARCH = null;

    public static FileOutputStream outputStream = null;
    public static FileOutputStream outputStreamPcm = null;


    public static Map<Integer, Long> DVRMaps = new ConcurrentHashMap<>();

    public static Map<Long, HCNetSDK.NET_DVR_DEVICEINFO_V40> deviceMaps = new ConcurrentHashMap<>();

    public static Map<String, String> mapsForCamera = new HashMap<>();

    public static Map<String, String> mapsForHistory = new HashMap<>();

    /**
     录像信息
     */
    public static Map<String, Integer> recordLongMap = new HashMap<>();
    /**
     * 心跳报文
     */
    public static final byte TYPET3 = 0x03;

    public static Map<Integer, Bootstrap> bootstrapHashMap = new HashMap<>();

    public static boolean apiPermissions;

    /**
     * 接口鉴权是否打开
     */
    public static boolean apiPermissions() {
        try {
            apiPermissions = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("systemConfigKey:otherConfig", "springInterfaceApi"));
            log.warn("apiPermissions is {}", apiPermissions);
        } catch (Exception e) {
            apiPermissions = false;
        }
        return apiPermissions;
    }

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

    public static InputStream websocketSendMsgBuffer(String url, byte[] bytes){
        HttpURLConnection con = null;
        InputStream inputStream = null;
        //尝试发送请求
        try {
            URL u = new URL(url);
            con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "binary/octet-stream");
            OutputStream outStream = con.getOutputStream();
            outStream.write(bytes);
            outStream.flush();
            outStream.close();
            //读取返回内容
            inputStream = con.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return inputStream;
    }

    public static<T> Result otherServer(Map<String, List<T>> map, String url) throws Exception{
        Result re = new Result();
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        return re;
    }
    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";


    public static final String GET_LOW_TASK_GO_ON = "http://iot-center-platform/tCruiseTask/v1/lowTaskGoOn";

    public static<T> Result otherServerList( List<T> list, String url) {
        Result re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, list, Result.class);
        return re;
    }

    public static final String TASK_FINISH="http://iot-center-platform/tCruiseDataResult/v1/updateCruiseAnalyze?cruiseResultIdList";

    public static final String picRecBack = "http://iot-center-platform/tSequentialConf/v1/sequentialRecBack";

    public static void  otherServerMap( Map<String,String> map, String url) {
        try{
            if (url.contains("/copy-ftps")){
                return;
            }
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url,map, Result.class);
        }catch (Exception e){
           log.info("接口错误：", e);
        }
    }

    public static String algorithmTestPicPath= "";
    public static String algorithmTestBasePicPath= "";

    public static final String COPY_FILE_URL = "http://iot-center-platform/file/v1/copy-ftps";
    /**
     * 如果 ftpsTurbo 为 true，则表示设置了文件盘共享，不使用 ftps 对文件进行传输拷贝
     */
    public static boolean ftpsTurbo() {
        boolean ftpsTurbo = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:ftpsTurbo", "content"));
        log.warn("ftpsTurbo is {}", ftpsTurbo);
        return ftpsTurbo;
    }

}
