package com.yjh.platform.common;

import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.task.entity.XMLBaseModel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constant {
    public static final String account_lock_times = "account_lock_times:userAccountID";

    public static final String SET_PRESET_URL = "http://iot-center-accessvideo/camera/v1/setPreset?cameraId={cameraId}&presetId={presetId}";

    public static final String CANCEL_PRESET_URL = "http://iot-center-accessvideo/camera/v1/cancelPreset?cameraId={cameraId}&presetId={presetId}";

    public static final String CAPTURE_PRESET_URL = "http://iot-center-accessvideo/camera/v1/capturePresetPicture?cameraId={cameraId}&presetId={presetId}";

    public static List<ConcurrentHashMap<String,Object>> taskMap = new LinkedList<>();

    public static final String CAMERA_STATES = "http://iot-center-accessvideo/camera/v1/getCameraStatus?recordId={recordId}";

    public static Map<String, Object> confirmImmediatelyMap = new HashMap<>();

    public static final String ROBOT_TASK_STATUS_URL = "http://iot-center-accessrobot/robot/v1/taskControl";

    public static final String ALGORITHM_URL ="http://iot-center-accessvideo/analysis/v1/algorithm";

    public static final String NVR_URL = "http://iot-center-accessvideo/camera/v1/getNVRStoreInfo?recordId={recordId}";

    public static final String NVR_REGISTER_URL = "http://iot-center-accessvideo/camera/v1/registerNVR?recordId={recordId}";

    public static Map<String,Object> weatherInfo = new HashMap<>();

    public static final String account_lock_time = "account_lock_time:userAccountID";


    public static final String WARN_JUDGE = "http://iot-center-accessvideo/AnalysisDataOperate/v1/warnInfo?value={value}&stdDeviceMeteName={stdDeviceMeteName}&meteKind={meteKind}&alarmState={alarmState}&stateZero={stateZero}&stateOne={stateOne}&alarmLevel={alarmLevel}&highLimit1={highLimit1}&lowLimit1={lowLimit1}&highLimit2={highLimit2}&lowLimit2={lowLimit2}&highLimit3={highLimit3}&lowLimit3={lowLimit3}&highLimit4={highLimit4}&lowLimit4={lowLimit4}";

    public static<T> Result otherServer(Map<String, List<T>> map, String url) throws Exception{
        Result re = new Result();
        ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        if (null != serviceRestTemplate) {
            re = serviceRestTemplate.postForObject(url, map, Result.class);
        }
        return re;
    }
    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

    public static Map<String,String> userInfo= new HashMap<>();
    public static final String START_CAMERA_URL = "http://iot-center-accessvideo/camera/v1/startRealPlay?cameraId={cameraId}";
    public static final String START_ROBOT_CAMERA_URL = "http://iot-center-accessvideo/camera/v1/robotStartRealPlay?robotId={robotId}";

    public static final String Maintenance_Issued = "http://iot-center-accessrobot/robot/v1/deviceMaintenanceIssued";
    public static final String UDP_SEND ="http://iot-center-accessudp/sysLogs/v1/sendFile";
}
