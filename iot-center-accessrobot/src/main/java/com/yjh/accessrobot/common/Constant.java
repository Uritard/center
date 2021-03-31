package com.yjh.accessrobot.common;

import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import io.netty.channel.ChannelHandlerContext;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Constant {

    //心跳报文
    public static final byte HEARTBEAT = 0x03;

    public static Map<String, ChannelHandlerContext> maps = new HashMap<>();
    public static AtomicInteger heartNum = new AtomicInteger(0);
    public static int flag = 0;
    public static String Packet = "";
    public static int registerCount = 1;


    public static long sendSessionId = 0L;//发送会话序列号

    public static Map<String, List<Long>> flagMap = new HashMap<>();

    public static  ExpiringMap<String, String> map = ExpiringMap.builder()
            .maxSize(100)
            .expiration(120000, TimeUnit.MILLISECONDS)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    public static String SEND_ROBOT_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

    public static<T> Result otherServer(Map<String, List<T>> map, String url) throws Exception{
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        return re;
    }

    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

//    public static Map<String,String> robotResultMap =  new HashMap<>();

    //算法接口
    public static final String algorithmUrl = "http://iot-center-accessvideo/analysis/v1/algorithm";
    //缺陷接口
    public static final String defectUrl = "http://iot-center-accessvideo/analysis/v1/defect";

    public static<T> Result otherServerList( List<String> list, String url){
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, list, Result.class);
    }
    public static final String TASK_FINISH="http://iot-center-platform/tCruiseDataResult/v1/updateCruiseAnalyze";

    public static final String WEATHER_URL="http://iot-center-platform/homePage/v1/getWeatherInfoForService";
    public static Result weatherServer(Map<String,String> map, String url){
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        return re;
    }
    public static String getUrl(String url, String json) {
        CloseableHttpClient client = HttpClients.createDefault();
        String result = "";
        try {
            URI uri = new URIBuilder(url).setParameter("json", json).build();
            HttpPost httpGet = new HttpPost(uri);
            httpGet.addHeader("Content-type", "application/json;charset=utf-8");
            httpGet.setHeader("Accept", "application/json");
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {e.getMessage();}
        return result;
    }
    public static final String WARN_JUDGE = "http://iot-center-accessvideo/AnalysisDataOperate/v1/warnInfo?value={value}&stdDeviceMeteName={stdDeviceMeteName}&meteKind={meteKind}&alarmState={alarmState}&stateZero={stateZero}&stateOne={stateOne}&alarmLevel={alarmLevel}&highLimit1={highLimit1}&lowLimit1={lowLimit1}&highLimit2={highLimit2}&lowLimit2={lowLimit2}&highLimit3={highLimit3}&lowLimit3={lowLimit3}&highLimit4={highLimit4}&lowLimit4={lowLimit4}";

    public static String robotCode="";
}
