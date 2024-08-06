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

    public static final String ZERO = "0";
    public static final String ONE = "1";
    public static final String TWO = "2";

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

    public static String Packet = "";

    public static Map<String,String> paramMap =new ConcurrentHashMap<>();

    //机器人任务路径 todo 记得改
    public static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/upSystemCommand";

    //检修区域路径 todo 记得改
    public static final String MAINTENANCE_URL = "http://iot-center-platform/tDeviceMaintenance/v1/systemSend";

    public static final String UDP_SEND ="http://iot-center-accessudp/sendFile/v1/sendFile";

    /**
     * 请求算法资源信息
     */
    public static final String ALGORITHM_RESOURCE_URL = "http://iot-center-platform/analysis/v1/algorithmResource";
    /**
     * 请求系统自检信息
     */
    public static final String SYSTEM_CHECK_URL = "http://iot-center-platform/systemInfo/v1/getSystemCheck";

    /**
     * 算法可靠性指标统计
     */
    public static final String ALGORITHM_STATISTICS_URL = "http://iot-center-platform/statistics/v1/algorithmStatics?type={type}&beginTime={beginTime}&endTime={endTime}";

    /**
     * platform接收一键顺控反馈文件
     */
    public static final String PLATFORM_SEND ="http://iot-center-platform/tSequentialConf/v1/receiveFile";
    /**
     * platform接收巡视结果审核
     */
    public static final String TASK_REVIEW_PROCESS = "http://iot-center-platform/uPatrolResult/v1/robotPatrolTaskReview";
    /**
     * platform接收下级系统的告警审核
     */
    public static final String WARN_REVIEW_PROCESS = "http://iot-center-platform/uPatrolResult/v1/robotWarnReview";

    public static<T> Result otherServer(Map<String, List<T>> map, String url) {
        Result re = new Result();
        try {
            re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

    public static<T> Result otherServerByList( List<T> list, String url) {
        Result re = new Result();
        try {
            re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, list, Result.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

    public static<T> Result otherServerPost(Map<String,Object> map, String url) {
        Result re = new Result();
        try {
            re = StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(url, Result.class ,map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

    public static <T> Result getForObject(String url) {
        Result re = new Result();
        try {
            re = StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(url, Result.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

    //任务状态控制
    public static final String TASK_STATE_URL = "http://iot-center-platform/uPatrolTask/v1/upSystemCtrl";

    public static  final Map<String, Long> getParamMap = new ConcurrentHashMap<>();
    //任务下发
    public static final String TASK_ISSUE_URL = "http://iot-center-platform/uPatrolTask/v1/upSystemIssuedTask";
    //任务删除
    public static final String TASK_DELETE_URL = "http://iot-center-platform/uPatrolTask/v1/upSystemDelete?taskId={taskId}&startTime={startTime}&endTime={endTime}&type={type}&source={source}";
    public static final String IOT_DEVICE_UPLOAD_URL = "http://iot-center-platform//tStdDevice/v1/uploadModel?type={type}";
    public static final String GET_WVP_SERVER_CONFIG = "http://iot-center-platform/camera/v1/getServerConfig";
    public static  XMLBaseModel weatherXmlModel = null;

    public static RedisTemplate redisTemplate;

    /**
     * 节点级别
     */
    public static String edgeLevel;


    /**
     * 获取节点级别
     */
    public static String edgeLevel() {
        try {
            edgeLevel = (String) redisTemplate.opsForHash().get("t_sys_param:edgeLevel", "content");
            log.debug("edgeLevel is {}", edgeLevel);
        } catch (Exception e) {
            edgeLevel = "";
        }
        return edgeLevel;
    }

    /**
     * 变电站编码
     */
    public static String stationCode;

    /**
     * 获取变电站编码
     */
    public static String stationCode() {
        try {
            stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
            log.debug("stationCode is {}", stationCode);
        } catch (Exception e) {
            stationCode = "";
        }
        return stationCode;
    }

    /**
     * 变电站名称
     */
    public static String stationName;

    /**
     * 获取变电站名称
     */
    public static String stationName() {
        try {
            stationName = (String) redisTemplate.opsForHash().get("t_sys_param:stationName", "content");
            log.debug("stationName is {}", stationName);
        } catch (Exception e) {
            stationName = "";
        }
        return stationName;
    }

    public static Boolean handlerNew = true;

    /**
     * handlerNew
     */
    public static boolean handlerNew() {
        try {
            handlerNew = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("systemConfigKey:robotServerConfig", "nettyHandlerNew"));
            log.debug("handlerNew is {}", handlerNew);
        } catch (Exception e) {
            handlerNew = true;
        }
        return handlerNew;
    }

    /**
     * 节点编码
     */
    public static String edgeCode;

    public static String edgeCode() {
        try {
            edgeCode = (String)redisTemplate.opsForHash().get("t_sys_param:edgeCode","content");
            log.debug("edgeCode is {}", edgeCode);
        } catch (Exception e) {
            edgeCode = "Client01";
        }
        return edgeCode;
    }

    public static String server;

    public static String server() {
        try {
            server = (String) redisTemplate.opsForHash().get("t_sys_param:upSystemReceiveCode","content");
            log.debug("server is {}", server);
        } catch (Exception e) {
            server = "Server01";
        }
        return server;
    }

    public static String algorithmServer;

    public static String algorithmServer() {
        try {
            algorithmServer = (String) redisTemplate.opsForHash().get("t_sys_param:upSystemAlgorithmReceiveCode","content");
            log.debug("algorithmServer is {}", algorithmServer);
        } catch (Exception e) {
            algorithmServer = "Cloud01";
        }
        return algorithmServer;
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
                log.debug("upSystemFlag is {}", upSystemFlag);
            } catch (Exception e) {
                upSystemFlag = "1";
            }
        }
        return upSystemFlag;
    }

    /**
     * 测点使用标准点位
     */
    public static boolean standardPoints() {
        boolean standardPoints;
        try {
            standardPoints = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:standardPoints", "content"));
            log.warn("standardPoints is {}", standardPoints);
        } catch (Exception e) {
            standardPoints = false;
        }
        return standardPoints;
    }

    /**
     * 是否使用业务中台id
     */
    public static boolean middlegroundIds() {
        boolean middlegroundIds;
        try {
            middlegroundIds = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("t_sys_param:middlegroundIds", "content"));
            log.debug("middlegroundIds is {}", middlegroundIds);
        } catch (Exception e) {
            middlegroundIds = false;
        }
        return middlegroundIds;
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
                log.debug("upSystemIp is {}", upSystemIp);
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
                upSystemPort = Integer.valueOf(redisTemplate.opsForHash().get("systemConfigKey:upSystem", "upSystemPort").toString());
                log.debug("upSystemPort is {}", upSystemPort);
            } catch (Exception e) {
                upSystemPort = 10011;
            }
        }
        return upSystemPort;
    }

    public static String managerSystemFlag;

    /**
     * 上级系统算法平台连接开关 1开 0关
     * @return
     */
    public static String managerSystemFlag() {
        if (managerSystemFlag == null) {
            try {
                managerSystemFlag = (String) redisTemplate.opsForHash().get("systemConfigKey:managerSystem", "managerSystemFlag");
                log.debug("managerSystemFlag is {}", managerSystemFlag);
            } catch (Exception e) {
                managerSystemFlag = "1";
            }
        }
        return managerSystemFlag;
    }


    /**
     * 上级系统算法IP
     */
    public static String managerSystemIp;

    /**
     * 上级系统算法IP
     * @return 上级系统算法IP
     */
    public static String managerSystemIp() {
        if (managerSystemIp == null) {
            try {
                managerSystemIp = (String) redisTemplate.opsForHash().get("systemConfigKey:managerSystem", "managerSystemIp");
                log.debug("upSystemIp is {}", managerSystemIp);
            } catch (Exception e) {
                managerSystemIp = "127.0.0.1";
            }
        }
        return managerSystemIp;
    }


    /**
     * 上级系统算法端口
     */
    public static Integer managerSystemPort;

    /**
     * 上级系统算法端口
     * @return 上级系统算法端口
     */
    public static Integer managerSystemPort() {
        if (managerSystemPort == null) {
            try {
                managerSystemPort = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:managerSystem", "managerSystemPort")));
                log.debug("managerSystemPort is {}", managerSystemPort);
            } catch (Exception e) {
                managerSystemPort = 10013;
            }
        }
        return managerSystemPort;
    }

    public static final String T_SYS_PARAM = "t_sys_param:";
    //主站任务下发到机器人
    public static final String ROBOT_TASK_ISSUE_URL = "http://iot-center-accessrobot/robot/v1/taskIssued";
}
