package com.yjh.logs.common;

import com.alibaba.fastjson.JSON;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Constant {

    public static final String USER_COUNT = "statistics:userCount";

    public static final String DEVICE_COUNT = "statistics:deviceCount";

    public static final String SZ_COUNT = "statistics:szCount";

    public static final String API_COUNT = "statistics:apiCount";

    public static final String kafka_COUNT = "statistics:oc_data_kafka_offset";

    //设备在redis中的键名-hash  deviceId,deviceName,
    public static final String REDIS_DEVICE = "dmp_device_base:ACCESSCODE_DEVICECODE";
    //网关设备在redis中的键名 -hash
    public static final String REDIS_GW_DEVICE = "dmp_device_gw:CODE";

    public static final String REDIS_GW_HISTORY = "dmp_history_gw:CODE";

    // 自定义属性
    public static final String DYNAMIC = "dynamic";

    //自定义遥脉，遥测，遥信，遥调,遥控
    public static final String YX="YX";
    public static final String YC="YC";
    public static final String YM="YM";
    public static final String YT="YT";
    public static final String YK="YK";


    // 系统属性
    public static final String SYSTEM = "system";

    public static final String REDIS_DEVICE_IOCENTER = "iotcenter_device_base:ACCESSCODE_DEVICECODE";

    public static final String REDIS_DEVICE_IOCENTER_EXPRESSION = "iotcenter_device_expression:ACCESSCODE_DEVICECODE";

    public static final String REDIS_SYS_ROLE_AUTH = "sys_role_auth:ID";

    public static final String DATACENTER_DATA_CHECK = "datacenter_data_check:ACCESSCODE_DEVICECODE";

    public static final String DATACENTER_DATA_PRECISION = "datacenter_data_percision:ACCESSCODE_DEVICECODE";

    public static final String REDIS_DEVICE_REDIS = "bdp_device_id:CODE";

    public static final String DEVICE_STATE_OFFLINE = "OFFLINE";


    public static final String TYPE_OC = "OC";

    public static final String TYPE_104 = "104";

    public static final String WEB_SCOKET = "http://192.168.9.40:18711/tCruiseTask/v1/syncWebsocket";

    public static String WEBSOCKET_URL="";

    public static RedisTemplate redisTemplate;

    public static Boolean apiPermissions=false;

    public static String websocketSendMsg(String url, Map<String,String> map) throws IOException {
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
}
