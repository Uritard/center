package com.yjh.platform.common;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import com.yjh.platform.module.user.entity.Channel;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.validation.constraints.Max;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Constant {
    public static final String account_lock_times = "account_lock_times:userAccountID";

    public static final String SET_PRESET_URL = "http://iot-center-accessvideo/camera/v1/setPreset?cameraId={cameraId}&presetId={presetId}";

    public static final String CANCEL_PRESET_URL = "http://iot-center-accessvideo/camera/v1/cancelPreset?cameraId={cameraId}&presetId={presetId}";

    public static final String CAPTURE_PRESET_URL = "http://iot-center-accessvideo/camera/v1/capturePresetPicture?cameraId={cameraId}&presetId={presetId}";

    public static List<ConcurrentHashMap<String,Object>> taskMap = new LinkedList<>();

    public static final String CAMERA_STATES = "http://iot-center-accessvideo/camera/v1/getCameraStatus?recordId={recordId}";

    public static Map<String, Object> confirmImmediatelyMap = new HashMap<>();

    public static final String ROBOT_TASK_STATUS_URL = "http://iot-center-accessrobot/robot/v1/taskControl";

    public static final String ALGORITHM_URL ="http://iot-center-accessvideo/analysis/v1/algorithm";

    public static final String NVR_URL = "http://iot-center-accessvideo/camera/v1/getNVRStoreInfo?recordId={recordId}";

    public static final String NVR_REGISTER_URL = "http://iot-center-accessvideo/camera/v1/registerNVR?recordId={recordId}";

    public static Map<String,Object> weatherInfo = new HashMap<>();

    public static Map<String,Object> voiceMap = new HashMap<>();

    public static final String account_lock_time = "account_lock_time:userAccountID";

    public static final String WARN_JUDGE = "http://iot-center-accessvideo/AnalysisDataOperate/v1/warnInfo?value={value}&stdDeviceMeteName={stdDeviceMeteName}&meteKind={meteKind}&alarmState={alarmState}&stateZero={stateZero}&stateOne={stateOne}&alarmLevel={alarmLevel}&highLimit1={highLimit1}&lowLimit1={lowLimit1}&highLimit2={highLimit2}&lowLimit2={lowLimit2}&highLimit3={highLimit3}&lowLimit3={lowLimit3}&highLimit4={highLimit4}&lowLimit4={lowLimit4}";

    public static final String DIAGNOSE_CHANNEL_OPERATE="http://iot-center-accessvqd/channelOperate/v1/updateChannel";

    public static final String DIAGNOSE_CHANNEL_GET="http://iot-center-accessvqd/channelOperate/v1/getChannel?channelId={channelId}";

    public static final String DIAGNOSE_CHANNEL_DELETE="http://iot-center-accessvqd/channelOperate/v1/deleteChannel?channelId={channelId}";

    public static<T> Result otherServer(Map<String, List<T>> map, String url) throws Exception{
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        //re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);//江苏要求
        return re;
    }
    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

    public static Map<String,String> userInfo= new HashMap<>();
    public static final String START_CAMERA_URL = "http://iot-center-accessvideo/camera/v1/startRealPlay?cameraId={cameraId}";
    public static final String STOP_CAMERA_URL= "http://iot-center-accessvideo/camera/v1/stopRealPlay?cameraId={cameraId}&rtmpUrl={rtmpUrl}";
    public static final String START_ROBOT_CAMERA_URL = "http://iot-center-accessvideo/camera/v1/robotStartRealPlay?robotId={robotId}";

    public static final String Maintenance_Issued = "http://iot-center-accessrobot/robot/v1/deviceMaintenanceIssued";
    public static final String UDP_SEND ="http://iot-center-accessudp/sendFile/v1/sendFile";

    //请求视频诊断监控点新增与修改
    public static Result otherServerEntity(Channel channel, String url) throws Exception{
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, channel, Result.class);
        return re;
    }


    public static Result otherServerGet(String channelId, String url) throws Exception{
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(url,Result.class,channelId);
        return re;
    }

    //跨服删除
    public static void crossServerDelete(String url, Map map){
        StaticContextAccessor.getBean(ServiceRestTemplate.class).delete(url,map);
    }

    public static Map<String,Object> sequentialState = new HashMap<>();

    public static String isDecode;

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

    public static ConcurrentHashMap<String,Integer> taskStateMap=new ConcurrentHashMap<>();

    public static Map<String,Boolean> voiceDeviceState = new HashMap<>();

}
