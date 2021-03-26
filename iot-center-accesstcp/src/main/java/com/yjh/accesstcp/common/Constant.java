package com.yjh.accesstcp.common;

import com.yjh.accesstcp.common.utils.StaticContextAccessor;
import com.yjh.accesstcp.commons.logs.SpringBeanUtils;
import com.yjh.accesstcp.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public static long sendSessionId = 0L;//发送会话序列号
    public static long receiveSessionId = 0L;//接受序列号

    public static String Packet = "";

    public static Map<String,String> paramMap =new ConcurrentHashMap<>();

    //机器人任务路径 todo 记得改
    public static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/upSystemCommand";

    //检修区域路径 todo 记得改
    public static final String MAINTENANCE_URL = "http://iot-center-platform/tDeviceMaintenance/v1/systemSend";

    public static<T> Result otherServer(Map<String, List<T>> map, String url) throws Exception{
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        return re;
    }

    //任务状态控制
    public static final String TASK_STATE_URL = "http://iot-center-platform/tCruiseTask/v1/upSystemCtrl";

    //任务下发
    public static final String TASK_ISSUE_URL = "http://iot-center-platform/tCruiseTask/v1/upSystemIssuedTask";
    public static  XMLBaseModel weatherXmlModel = null;
}
