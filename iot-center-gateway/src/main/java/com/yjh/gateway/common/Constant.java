package com.yjh.gateway.common;

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
}
