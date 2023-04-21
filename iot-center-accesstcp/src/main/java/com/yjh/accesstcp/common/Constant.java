package com.yjh.accesstcp.common;

import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.yjh.accesstcp.common.utils.StaticContextAccessor;
import com.yjh.accesstcp.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
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

    public static String TIME= "";

    public static Map<Integer, Bootstrap> bootstrapHashMap = new HashMap<>();

    public static AtomicLong sendSessionId = new AtomicLong(0L);//发送会话序列号
    // public static long receiveSessionId = 0L;//接受序列号

    public static String Packet = "";

    public static Map<String,String> paramMap =new ConcurrentHashMap<>();

    //机器人任务路径 todo 记得改
    public static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/upSystemCommand";

    //检修区域路径 todo 记得改
    public static final String MAINTENANCE_URL = "http://iot-center-platform/tDeviceMaintenance/v1/systemSend";

    public static final String UDP_SEND ="http://iot-center-accessudp/sendFile/v1/sendFile";

    public static<T> Result otherServer(Map<String, List<T>> map, String url) throws Exception{
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        return re;
    }

    //任务状态控制
    public static final String TASK_STATE_URL = "http://iot-center-platform/uPatrolTask/v1/upSystemCtrl";

    public static  final Map<String, Long> getParamMap = new ConcurrentHashMap<>();
    //任务下发
    public static final String TASK_ISSUE_URL = "http://iot-center-platform/uPatrolTask/v1/upSystemIssuedTask";
    public static  XMLBaseModel weatherXmlModel = null;

    public static RedisTemplate redisTemplate;

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
            log.info("stationCode is {}", stationCode);
        } catch (Exception e) {
            stationCode = "";
        }
        return stationCode;
    }

    public static Boolean handlerNew = true;

    /**
     * handlerNew
     */
    public static boolean handlerNew() {
        try {
            handlerNew = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("systemConfigKey:robotServerConfig", "nettyHandlerNew"));
            log.info("handlerNew is {}", handlerNew);
        } catch (Exception e) {
            handlerNew = true;
        }
        return handlerNew;
    }

    public static String cruise;

    public static String cruise() {
        try {
            cruise = (String)redisTemplate.opsForHash().get("t_sys_param:edgeCode","content");
            log.info("cruise is {}", cruise);
        } catch (Exception e) {
            cruise = "Client01";
        }
        return cruise;
    }

    public static String server;

    public static String server() {
        try {
            server = (String) redisTemplate.opsForHash().get("t_sys_param:upSystemReceiveCode","content");
            log.info("server is {}", server);
        } catch (Exception e) {
            server = "Server01";
        }
        return server;
    }

    public static String upSystemFlag;

    /**
     * 上级系统连接开关 1开 0关
     * @return
     */
    public static String upSystemFlag() {
        if (upSystemFlag == null) {
            try {
                upSystemFlag = (String) redisTemplate.opsForHash().get("systemConfigKey:upSystem", "upSystemFlag");
                log.info("upSystemFlag is {}", upSystemFlag);
            } catch (Exception e) {
                upSystemFlag = "1";
            }
        }
        return upSystemFlag;
    }

    /**
     * 上级系统IP
     */
    public static String upSystemIp;

    /**
     * 上级系统IP
     * @return
     */
    public static String upSystemIp() {
        if (upSystemIp == null) {
            try {
                upSystemIp = (String) redisTemplate.opsForHash().get("systemConfigKey:upSystem", "upSystemIp");
                log.info("upSystemIp is {}", upSystemIp);
            } catch (Exception e) {
                upSystemIp = "127.0.0.1";
            }
        }
        return upSystemIp;
    }


    /**
     * 上级系统端口
     */
    public static Integer upSystemPort;

    /**
     * 上级系统端口
     * @return
     */
    public static Integer upSystemPort() {
        if (upSystemPort == null) {
            try {
                upSystemPort = (Integer) redisTemplate.opsForHash().get("systemConfigKey:upSystem", "upSystemPort");
                log.info("upSystemPort is {}", upSystemPort);
            } catch (Exception e) {
                upSystemPort = 10011;
            }
        }
        return upSystemPort;
    }


    public static final String T_SYS_PARAM = "t_sys_param:";
    //主站任务下发到机器人
    public static final String ROBOT_TASK_ISSUE_URL = "http://iot-center-accessrobot/robot/v1/taskIssued";
}
