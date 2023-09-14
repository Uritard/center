package com.yjh.platform.common;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.user.entity.Channel;
import io.netty.bootstrap.Bootstrap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Constant {
    public static final String account_lock_times = "account_lock_times:userAccountID";

//    public static final String SET_PRESET_URL = "http://iot-center-accessvideo/camera/v1/setPreset?cameraId={cameraId}&presetId={presetId}&presetName={meteName}";
//    public static final String GET_PRESET_PTZ_AND_PIC_URL = "http://iot-center-accessvideo/camera/v1/getPresetPTZAndPic?cameraId={cameraId}&presetId={presetId}&presetName={meteName}";
//
//    public static final String CANCEL_PRESET_URL = "http://iot-center-accessvideo/camera/v1/cancelPreset?cameraId={cameraId}&presetId={presetId}";
//
//    public static final String CAPTURE_PRESET_URL = "http://iot-center-accessvideo/camera/v1/capturePresetPicture?cameraId={cameraId}&presetId={presetId}&meteName={meteName}&edgeCode={edgeCode}";

    public static final String SEND_METER_URL ="http://iot-center-accessmeter/tMeterCollect/v1" ;
    public static final String T_SYS_PARAM = "t_sys_param:";
    /**
     * 边缘节点
     */
    public static final String LEVEL_EDGE = "1";
    /**
     * 巡视主机
     */
    public static final String LEVEL_HOST = "2";
    /*
    *  上级系统层级
    */
    public static final String LEVEL_UP_SYSTEM = "3";
    public static final String SILENT_SECOND = "silent_second:";
    public static final Integer INTEGER_1 = 1;

    public static final long MANAGER_USER_ID = 10001L;

    public static List<ConcurrentHashMap<String,Object>> taskMap = new LinkedList<>();

    public static Map<String, Object> confirmImmediatelyMap = new HashMap<>();

    public static final String ROBOT_TASK_STATUS_URL = "http://iot-center-accessrobot/robot/v1/taskControl";

    public static final String ROBOT_CONFIRM_MSG_URL = "http://iot-center-accessrobot/robot/v1/sendConfirmMsg";

    public static final String STATISTIC = "http://iot-center-accessrobot/robot/v1/cruiseStatistic";

    public static final String CAMERA_PRESET_UPDATE_URL = "http://iot-center-accessrobot/robot/v1/sycPresetInfo";

    // public static final String ALGORITHM_URL ="http://iot-center-accessvideo/analysis/v1/algorithm";

    // public static final String NVR_URL = "http://iot-center-accessvideo/camera/v2/getNVRStoreInfo?recordId={recordId}";

    // public static final String NVR_REGISTER_URL = "http://iot-center-accessvideo/camera/v1/registerNVR?recordId={recordId}";

    public static Map<String,Object> weatherInfo = new HashMap<>();

    public static Map<Long,Object> voiceMap = new HashMap<>();
    public static volatile boolean isThreadStart;

    public static final String account_lock_time = "account_lock_time:userAccountID";

    public static final String WARN_JUDGE = "http://iot-center-platform/analysis/v1/warnInfo?value={value}&valueDesc={valueDesc}&stdDeviceMeteName={stdDeviceMeteName}&meteKind={meteKind}&alarmState={alarmState}&stateZero={stateZero}&stateOne={stateOne}&alarmLevel={alarmLevel}&highLimit1={highLimit1}&lowLimit1={lowLimit1}&highLimit2={highLimit2}&lowLimit2={lowLimit2}&highLimit3={highLimit3}&lowLimit3={lowLimit3}&highLimit4={highLimit4}&lowLimit4={lowLimit4}";

    // public static final String DIAGNOSE_CHANNEL_OPERATE="http://iot-center-accessvqd/channelOperate/v1/updateChannel";

    // public static final String DIAGNOSE_CHANNEL_GET="http://iot-center-accessvqd/channelOperate/v1/getChannel?channelId={channelId}";

    // public static final String DIAGNOSE_CHANNEL_DELETE="http://iot-center-accessvqd/channelOperate/v1/deleteChannel?channelId={channelId}";

    // public static final String CAMERA_STREAM_STOP="http://iot-center-accessvideo/camera/v1/stopStream?cameraId={cameraId}";

    // public static final String CAMERA_STREAM_STOP_ALL="http://iot-center-accessvideo/camera/v1/stopAllStream";

    public static final String DRONE_VIDEO="http://iot-center-platform/camera/v1/droneStartRealPlay?robotId={robotId}";

    public static<T> Result otherServer(Map<String, List<T>> map, String url) {
        Result re = new Result();
        try {
            re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("request: {} \nresult: {}", url, JSON.toJSONString(re));
        return re;
    }
    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

    public static final String TCP_UPLOAD_FILE = "http://iot-center-accesstcp/sendToUpSystem/v1/uploadFile";

    public static final String TCP_MODEL_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/modelUpload?type={type}";
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
    public static Result videoServer(String type,String url,Object... objects){
        try {
            return StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(url, Result.class,objects);//江苏要求
        }catch (Exception e){
            log.error("录像文件查询失败", e);
        }
        return null;
    }

    public static Map<String,String> userInfo= new HashMap<>();
//    public static final String START_CAMERA_URL = "http://iot-center-accessvideo/camera/v1/startRealPlay?cameraId={cameraId}";
//    public static final String STOP_CAMERA_URL= "http://iot-center-accessvideo/camera/v1/stopRealPlay?cameraId={cameraId}&rtmpUrl={rtmpUrl}";
//    public static final String START_ROBOT_CAMERA_URL = "http://iot-center-accessvideo/camera/v1/robotStartRealPlay?robotId={robotId}";
//     public static final String PLAY_BACK_FILE_LIST_URL = "http://iot-center-accessvideo/camera/v1/getFileList?cameraId={cameraId}&startTime={startTime}&endTime={endTime}";

    public static final String Maintenance_Issued = "http://iot-center-accessrobot/robot/v1/deviceMaintenanceIssued";
    public static final String LINKAGE_FILE_TRANSFER = "http://iot-center-accessrobot/robot/v1/linkageFileTransfer";
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

    public static String nonhomologousWarn = "分,合,开,关,储能,非储能,远方,就地";

    public static RedisTemplate redisTemplate;

    private static Boolean packetLog;

    private static Boolean hasEncoding;

    private static Boolean fastTurbo;

    private static Boolean standardPoints;

    private static String systemLevel;
    /**
     * 是否开启修改同步模型
     */
    private static Boolean updateSyncModel;
    /**
     * 下级上报结束等待次数
     */
    private static int endWaitTimes;


    public static String websocketSendMsg(String url, Map<String, String> map) {
        String result = null;
        try {
            result = websocketSendMsg(url, map, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public static String websocketSendMsg(String url, Map<String, String> map, String userId) throws IOException {
        // 将WebSocket信息写入Redis
        String json = JSON.toJSONString(map);
        String type = "type";
        if (ArrayUtils.contains(new String[]{"logError", "newTask", "newLinkage", "newAlarm", "alarmPopUp", "linkagePopUp"}, map.get(type))) {
            redisTemplate.opsForValue().set(map.get("type"), String.valueOf(map), 5, TimeUnit.MINUTES);
        }

        CloseableHttpClient client = HttpClients.createDefault();
        String result = "";
        try {
            URIBuilder uriBuilder = new URIBuilder(url).setParameter("json", json);
            if (userId != null) {
                uriBuilder.addParameter("userId", userId);
            }
            URI uri = uriBuilder.build();
            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader("Content-type", "application/json;charset=utf-8");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public static boolean isPacketLog() {
        if (packetLog == null) {
            try {
                packetLog = "true".equals(redisTemplate.opsForHash().get("t_sys_param:packetLog", "content"));
            } catch (Exception e) {
                packetLog = true;
            }
        }
        return packetLog;
    }

    public static boolean hasEncoding() {
        if (hasEncoding == null) {
            try {
                hasEncoding = "true".equals(redisTemplate.opsForHash().get("t_sys_param:voiceNeedEncoding", "content"));
            } catch (Exception e) {
                hasEncoding = true;
            }
        }
        return hasEncoding;
    }

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

    /**
     * 0: 默认多通道 0.1 0.2 0.1 0.2 0.1 0.2
     * -1: 单通道，拆分拼接，121212
     * -2: 强制单声道，通道1写完写通道2，111222
     * 1: 单通道，只获取一个通道数据 111
     * 2: 多声道，拆分拼接，121212
     */
    public static int voiceChtype() {
        int chtype = 0;
        try {
            chtype = NumberUtils.toInt((String)redisTemplate.opsForHash().get("t_sys_param:voiceChtype", "content"), chtype);
        } catch (Exception e) {
            log.warn("voiceChtype not setting, voiceChtype: {} ", chtype);
        }
        return chtype;
    }

    /**
     * 声纹采样前后字段反转，500 声纹接口采样需前后反转
     */
    public static boolean voiceByteReverse() {
        boolean reverse = false;
        try {
            reverse = "true".equals(redisTemplate.opsForHash().get("t_sys_param:voiceByteReverse", "content"));
        } catch (Exception e) {
            log.warn("voiceByteReverse not setting, voiceByteReverse: {} ", reverse);
        }
        return reverse;
    }

    /**
     * 是否只获取单通道数据
     */
    public static boolean voiceChannelOne() {

        return Constant.voiceChtype() > 0;
    }

    /**
     * 急速模式，不上传上级系统，减少日志
     */
    public static boolean fastTurbo() {
        if (fastTurbo == null) {
            try {
                fastTurbo = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:fastTurbo", "content"));
                log.warn("fastTurbo is {}", fastTurbo);
            } catch (Exception e) {
                fastTurbo = false;
            }
        }
        return fastTurbo;
    }

    /**
     * 使用标准点位下发任务
     */
    public static boolean standardPoints() {
        if (standardPoints == null) {
            try {
                standardPoints = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:standardPoints", "content"));
                log.warn("standardPoints is {}", standardPoints);
            } catch (Exception e) {
                standardPoints = false;
            }
        }
        return standardPoints;
    }

    public static int endWaitTimes() {
        if (endWaitTimes == 0) {
            try {
                endWaitTimes = NumberUtils.toInt((String)redisTemplate.opsForHash().get("t_sys_param:endWaitTimes", "content"), 8);
                log.warn("taskFinishWaitTimes is {}", endWaitTimes);
            } catch (Exception e) {
                endWaitTimes = 8;
            }
        }
        return endWaitTimes;
    }

    public static void refreshSwitchCach() {
        packetLog = "true".equals(redisTemplate.opsForHash().get("t_sys_param:packetLog", "content"));
        hasEncoding = "true".equals(redisTemplate.opsForHash().get("t_sys_param:voiceNeedEncoding", "content"));
        fastTurbo = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:fastTurbo", "content"));
        standardPoints = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:standardPoints", "content"));
        updateSyncModel = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:updateSyncModel", "content"));
        endWaitTimes = NumberUtils.toInt((String)redisTemplate.opsForHash().get("t_sys_param:endWaitTimes", "content"), 8);
        logLevel = NumberUtils.toInt((String)redisTemplate.opsForHash().get("t_sys_param:logLevel", "content"), LOG_LV_ONE);
    }

    /**
     * 获取系统级别
     */
    public static String getLevelEdge() {
        if (StringUtils.isEmpty(systemLevel)) {
            try {
                systemLevel = (String)redisTemplate.opsForHash().get("t_sys_param:edgeLevel", "content");
            } catch (Exception e) {
                systemLevel = LEVEL_HOST;
            }
        }
        return systemLevel;
    }

    /**
     * 是否边缘节点
     */
    public static boolean isEdge() {
        return LEVEL_EDGE.equals(getLevelEdge());
    }

    /**
     * 是否巡视主机
     */
    public static boolean isHost() {
        return LEVEL_HOST.equals(getLevelEdge());
    }

    /**
     * 是否上级系统
     */
    public static boolean isUpSystem() {
        return LEVEL_UP_SYSTEM.equals(getLevelEdge());
    }

    public static ConcurrentHashMap<String,Integer> taskStateMap=new ConcurrentHashMap<>();

    /**
     * 用户角色
     */
    public static final Map<String, String> YHJS = new HashMap<String, String>() {
        {
            put("1234", "管理员");
            put("1235", "业务员");
            put("1236", "审计员");
        }
    };

    public static Boolean apiPermissions=false;


    public static String filePath;

    public static final String TCP_MODEL_DOWNLOAD_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/modelDownload";

    public static<T> Result mapToOtherServer(Map<String, Object> map, String url) {
        Result re = new Result();
        try {
            re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

    /**
     * 更新缓存 通知 tcp服务重新建立连接
     */
    public static final String UPDATE_TCP_CONTENT = "http://iot-center-accesstcp/sendToUpSystem/v1/updateTcpContent";

    /**
     * 更新缓存 通知 robot服务重新建立连接
     */
    public static final String UPDATE_ROBOT_SERVER = "http://iot-center-accessrobot/robot/v1/updateRobotServer";

    public static<T> void getToOtherServer(String url) throws Exception{
        Result re = new Result();
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(url, Result.class);
    }

    public static String algorithmTestPicPath= "";

    public static Map<Integer, Bootstrap> bootstrapHashMap = new HashMap<>();

    //心跳报文
    public static final byte TYPET3 = 0x03;

    public static<T> Result otherServerList( List<T> list, String url) {
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, list, Result.class);
        return re;
    }

    public static final String GET_LOW_TASK_GO_ON = "http://iot-center-platform/tCruiseTask/v1/lowTaskGoOn";

    public static void  otherServerMap( Map<String,String> map, String url) {
        try{
            Result re = new Result();
            //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
            //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url,map, Result.class);
        }catch (Exception e){
            log.info("一键顺控："+e);
        }
    }

    public static final String picRecBack = "http://iot-center-platform/tSequentialConf/v1/sequentialRecBack";

    public static final String TASK_FINISH="http://iot-center-platform/tCruiseDataResult/v1/updateCruiseAnalyze?cruiseResultIdList";

    /**
     * 本系统数据
     */
    public static final int STATE_LOCAL = 1;
    /**
     * 下级系统数据
     */
    public static final int STATE_SUB = 0;

    // public static final String VIDEO_DOWNLOAD_FILE = "http://iot-center-accessvideo/intel-analysis/downloadPicture";


    public static final int TASK_CHECK = 1;
    public static final int RESULT_CHECK = 2;
    public static final int WARN_ACCURACY = 3;
    public static final int WARN_CHECK = 4;
    public static final int INSTANCE_LOSS = 5;

    /**
     * 日志级别 1
     */
    public static final int LOG_LV_ONE = 1;
    /**
     * 日志级别 2
     */
    public static final int LOG_LV_TWO = 2;
    /**
     * 日志级别 3
     */
    public static final int LOG_LV_TRI = 3;
    /**
     * 日志级别，级别越高，日志越详细，日志级别为0表示关闭自定义级别日志
     */
    private static Integer logLevel;

    /**
     * 获取日志级别
     */
    public static int logLevel() {
        if (logLevel == null) {
            try {
                logLevel = NumberUtils.toInt((String)redisTemplate.opsForHash().get("t_sys_param:logLevel", "content"), LOG_LV_ONE);
            } catch (Exception e) {
                logLevel = LOG_LV_ONE;
            }
        }
        return logLevel;
    }

    public static boolean logUpLv1() {
        return Constant.logLevel() >= Constant.LOG_LV_ONE;
    }

    public static boolean logUpLv2() {
        return Constant.logLevel() >= Constant.LOG_LV_TWO;
    }

    public static boolean logUpLv3() {
        return Constant.logLevel() >= Constant.LOG_LV_TRI;
    }
}

