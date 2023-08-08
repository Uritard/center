package com.yjh.accessrobot.common;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
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
import java.net.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author YJH
 */
@Slf4j
public class Constant {
    /**
     * 本系统数据
     */
    public static final int STATE_LOCAL = 1;
    /**
     * 下级系统数据
     */
    public static final int STATE_SUB = 0;
    public static final String ROBOT = "robot";
    public static final String DRONE = "drone";
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

    public static Map<String, List<Long>> flagMap = new ConcurrentHashMap<>();

    /**
     * 任务与机器人对应集合（一条任务  多个机器人执行）
     */
    public static Map<String, Map<String, String>> taskRobotMap = new ConcurrentHashMap<>();

    public static  ExpiringMap<String, String> map = ExpiringMap.builder()
            .maxSize(100)
            .expiration(120000, TimeUnit.MILLISECONDS)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    public static String SEND_ROBOT_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

    public static String TASK_SHUT_DOWN_URL = "http://iot-center-platform/uPatrolTask/v1/taskShutDown?taskId={taskId}&content={content}";

    /**
     * platform接收机器人/无人机巡视结果接口
     */
    public static final String TASK_RESULT_PROCESS = "http://iot-center-platform/uPatrolTask/v1/robotPatrolTaskResult";
    /**
     * platform接收静默监视数据接口
     */
    public static final String SILENT_INFO_PROCESS = "http://iot-center-platform/uPatrolTask/v1/robotSilentInfo";
    /**
     * platform接收下级系统的巡视结果审核
     */
    public static final String TASK_REVIEW_PROCESS = "http://iot-center-platform/uPatrolResult/v1/robotPatrolTaskReview";
    /**
     * platform接收机器人/无人机任务状态接口
     */
    public static final String TASK_STATUS_PROCESS = "http://iot-center-platform/uPatrolTask/v1/robotPatrolTaskStatus";
    /**
     * platform接收机器人/无人机测点告警接口
     */
    public static final String POINT_ALARM_PROCESS = "http://iot-center-platform/uPatrolTask/v1/robotPatrolTaskAlarm";

    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

    /**
     * platform接收机器人/无人机/摄像机等巡视统计信息接口
     */
    public static final String CRUISE_DEVICE_STATICS_PROCESS = "http://iot-center-platform/uPatrolDataResult/v1" +
            "/dealDeviceStaticsInfo";

    public static<T> Result otherServerList(List<T> list, String url){
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, list, Result.class);
    }
    public static final String TASK_FINISH="http://iot-center-platform/tCruiseDataResult/v1/updateCruiseAnalyze";

    public static final String WEATHER_URL="http://iot-center-platform/homePage/v1/getWeatherInfoForService";

    public static final String SOURCE_FILE_URL="http://iot-center-accessudp/sendFile/v1/dealSourceFile";

    public static final String DEAL_LINKAGE_SIGNAL_URL="http://iot-center-accessudp/sendFile/v1/dealLinkageSignal";

    public static final String SEQUENCE_URL = "http://iot-center-platform/tSequentialConf/v1/sequential?meteId={meteId}";

    public static final String SEQUENCE_REC_URL = "http://iot-center-platform/tSequentialConf/v1/sequentialRec";

    public static final String UNION_URL = "http://iot-center-platform/tCfgDataCurrent/v1/unionTest?meteId={meteId}";

    public static Result mapToOtherServer(Object o, String url){
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, o, Result.class);
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

    public static RedisTemplate redisTemplate;

    public static Map<Integer, Map<ChannelFuture, ServerBootstrap>> futureServerBootstrapHashMap = new ConcurrentHashMap<>();

    public static Boolean apiPermissions;

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

    /**
     * 发送方唯一标识
     */
    public static String sendCode;

    /**
     * 获取发送方唯一标识
     */
    public static String sendCode() {
        try {
            sendCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeCode", "content");
            log.warn("sendCode is {}", sendCode);
        } catch (Exception e) {
            sendCode = "";
        }
        return sendCode;
    }

    /**
     * 变电站名称
     */
    public static String stationCode;

    /**
     * 获取变电站名称
     */
    public static String stationCode() {
        try {
            stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
            log.warn("stationCode is {}", stationCode);
        } catch (Exception e) {
            stationCode = "";
        }
        return stationCode;
    }

    public static Boolean handlerNew;

    /**
     * handlerNew
     */
    public static boolean handlerNew() {
        try {
            handlerNew = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("systemConfigKey:robotServerConfig", "nettyHandlerNew"));
            log.warn("handlerNew is {}", handlerNew);
        } catch (Exception e) {
            handlerNew = true;
        }
        return handlerNew;
    }

    /**
     * robot服务监听端口
     */
    public static Integer port;

    /**
     * robot服务监听端口
     */
    public static Integer port() {
        if (port == null){
            try {
                port = Integer.parseInt(String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:robotServerConfig", "nettyServerPort")));
                log.warn("port is {}", port);
            } catch (Exception e) {
                port = 10011;
            }
        }
        return port;
    }


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

    public static void modelUpload(String type){
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("type", type);
            Result re = StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(TCP_MODEL_URL, Result.class,param);//江苏要求
            log.info("modelUpload result: {}", JSON.toJSONString(re));
        }catch (Exception e){
            log.error("模型文件上传失败", e);
        }
    }

    public static final String TCP_MODEL_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/modelUpload?type={type}";
    public static final String WARN_JUDGE = "http://iot-center-accessvideo/AnalysisDataOperate/v1/warnInfo?value={value}&stdDeviceMeteName={stdDeviceMeteName}&meteKind={meteKind}&alarmState={alarmState}&stateZero={stateZero}&stateOne={stateOne}&alarmLevel={alarmLevel}&highLimit1={highLimit1}&lowLimit1={lowLimit1}&highLimit2={highLimit2}&lowLimit2={lowLimit2}&highLimit3={highLimit3}&lowLimit3={lowLimit3}&highLimit4={highLimit4}&lowLimit4={lowLimit4}";

    public static String REGION_REFRESH_URL = "http://iot-center-platform/tStdRegion/v1/refreshRegion";

    public static final String NVR_REGISTER_URL = "http://iot-center-accessvideo/camera/v1/registerNVR?recordId={recordId}";

    /**
     * 是否开启修改同步模型
     */
    private static Boolean updateSyncModel;

    public static boolean updateSyncModel() {
        if (updateSyncModel == null) {
            try {
                updateSyncModel = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:updateSyncModel", "content"));
                log.warn("updateSyncModel is {}", updateSyncModel);
            } catch (Exception e) {
                updateSyncModel = false;
            }
        }
        return updateSyncModel;
    }

    public static String getLocalIp() throws SocketException {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipaddress = inetAddress.getHostAddress();
                            if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
                                log.info(ipaddress);
                                ip = ipaddress;
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ip = "127.0.0.1";
            log.error(ex.getMessage(), ex);
        }
        return ip;
    }
}
