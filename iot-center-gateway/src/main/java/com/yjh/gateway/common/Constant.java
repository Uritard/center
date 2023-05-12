package com.yjh.gateway.common;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YJH
 */
public class Constant {

    public static final String USER_COUNT = "statistics:userCount";

    public static final String DEVICE_COUNT = "statistics:deviceCount";

    public static final String SZ_COUNT = "statistics:szCount";

    public static final String API_COUNT = "statistics:apiCount";

    public static final String kafka_COUNT = "statistics:oc_data_kafka_offset";

    /**
     * 设备在redis中的键名-hash  deviceId,deviceName,
     */
    public static final String REDIS_DEVICE = "dmp_device_base:ACCESSCODE_DEVICECODE";
    /**
     * 网关设备在redis中的键名 -hash
     */
    public static final String REDIS_GW_DEVICE = "dmp_device_gw:CODE";

    public static final String REDIS_GW_HISTORY = "dmp_history_gw:CODE";

    /**
     * 自定义属性
     */
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

    public static String isDecode;

    public static String isUkey;

    public static String[] FIELDS_NOT_VALIDAT;

    /**
     * ukey序列号
     */
    public static final Map<String, String> UKEY_XLH = new HashMap<String, String>() {
        {
            put("10001", "A99B4B794F101786");
            put("10004", "2735428C3E687671");
        }
    };
    /**
     * ukey公钥
     */
    public static final Map<String, String> UKEY_GY = new HashMap<String, String>() {
        {
            put("10001", "04deeafe50247551be7bbf7658402db06b9fb5490471a3dca87b2e6c68b54bcc61a9529d8ba5877da05cff226433799b4ad65953db2d00af7262bcaaa3442544a2");
            put("10004", "0461fb6367aefc6db728b8bd889349c25fac42c94a78c9d564af02feba1613d9cbb5f6a62151941873e5b2428033413ab7502b25dfde03c51bdcc4fb3027cb3bd0");
        }
    };

    /**
     * video服务接收语音数据接口
     */
    public static String VIDEO_RECEIVE_DATA = "";

    public static void sendAudioDataToVideo(byte[] bytes) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(Constant.VIDEO_RECEIVE_DATA);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "binary/octet-stream");
            OutputStream outStream = connection.getOutputStream();
            outStream.write(bytes);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
