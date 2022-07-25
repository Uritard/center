package com.yjh.accessrobot.common;

import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import io.netty.channel.ChannelHandlerContext;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
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
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author YJH
 */
public class Constant {
    public static Map<String, ChannelHandlerContext> maps = new ConcurrentHashMap<>();
    /**
     * 巡视主机发送会话序列号
     */
    public static volatile long sendSessionId = 0L;

    public static AtomicLong AtomicSessionId = new AtomicLong(0);
    /**
     * 机器人注册次数
     */
    public static Map<String, Integer> robotRegisterCounts = new ConcurrentHashMap<>();
    /**
     * 机器人是否注册标识
     */
    public static Map<String, Boolean> robotRegisterFlag = new ConcurrentHashMap<>();
    /**
     * 不同通道处理不同消息，key为机器人code，value为通道号
     */
    public static Map<String, String> robotChannels = new ConcurrentHashMap<>();
    /**
     * 机器人收不到心跳次数
     */
    public static Map<String, Integer> robotRemoveCounts = new ConcurrentHashMap<>();
    /**
     * 心跳线程存活标识
     */
    public static Map<String, Boolean> robotThreadFlag = new ConcurrentHashMap<>();
    /**
     * 机器人收到心跳次数
     */
    public static Map<String, Integer> robotHeartBeatCounts = new ConcurrentHashMap<>();

    public static Map<String, List<Long>> flagMap = new ConcurrentHashMap<>();

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

    //添加操作任务模型入口
    public static final String PLAN_URL="http://iot-center-platform/tCruisePlan/v1/add";
    //查询操作任务模型入口
    public static final String GET_PLAN_LIST = "http://iot-center-platform/tCruisePlan/v1/selectByRobotId?robotId={robotId}";
    //删除
    public static final String DELETE_PLAN_LIST = "http://iot-center-platform/tCruisePlan/v1/deleteByPlanCode?planCode={planCode}";

    public static final String GET_LOW_TASK_GO_ON = "http://iot-center-platform/tCruiseTask/v1/lowTaskGoOn";

    public static Result restTemplatePost(String url, Map<String, Object> map) {
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
    }

    public static Result restTemplateGet(String url, Map<String, Object> params) {
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(url, Result.class, params);
    }

    public static void restTemplateDelete(String url, Map<String, Object> params) {
        StaticContextAccessor.getBean(ServiceRestTemplate.class).delete(url, params);
    }

    public static boolean apiPermissions;

    public static String WEBSOCKET_URL="";

    public static RedisTemplate redisTemplate;

    //请求webSocket发送方法
    public static String postUrl(String url, String json) throws IOException, URISyntaxException {
        System.out.println("webSocketUrl="+url+",json="+json);
        CloseableHttpClient client = HttpClients.createDefault();
        URI uri = new URIBuilder(url).setParameter("json", json).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }
    public static final String WARN_JUDGE = "http://iot-center-accessvideo/AnalysisDataOperate/v1/warnInfo?value={value}&stdDeviceMeteName={stdDeviceMeteName}&meteKind={meteKind}&alarmState={alarmState}&stateZero={stateZero}&stateOne={stateOne}&alarmLevel={alarmLevel}&highLimit1={highLimit1}&lowLimit1={lowLimit1}&highLimit2={highLimit2}&lowLimit2={lowLimit2}&highLimit3={highLimit3}&lowLimit3={lowLimit3}&highLimit4={highLimit4}&lowLimit4={lowLimit4}";

    public static String robotCode="";

    public static String sendCode="";

    public static boolean handlerNew = true;
}
