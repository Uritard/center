package com.yjh.accesstcp.module.device.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.CreateModeXMLUtil;
import com.yjh.accesstcp.common.utils.ZipUtil;
import com.yjh.accesstcp.commons.result.BusinessException;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.result.ResultCodeEnum;
import com.yjh.accesstcp.commons.utils.CommonUtils;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.dao.*;
import com.yjh.accesstcp.module.device.entity.*;
import com.yjh.accesstcp.module.device.service.impl.UpType;
import com.yjh.accesstcp.module.device.service.uphandler.tek.UrlPathHandler;
import com.yjh.accesstcp.module.device.utils.FtpsUtil;
import com.yjh.accesstcp.module.device.utils.ValueUtil;
import com.yjh.accesstcp.netty.NettyClient;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.algorithm.StateGridAlgorithmHandlerImpl;
import com.yjh.accesstcp.netty.iot.StateGridADecoder;
import com.yjh.accesstcp.netty.iot.StateGridAHandlerImpl;
import com.yjh.accesstcp.thread.ReContentManager;
import com.yjh.accesstcp.thread.RegisterManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lqh
 * @since 2021/1/12
 */
@Service
@Slf4j
public class SendToUpSystemServices {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TStdRegionMapper tStdRegionMapper;
    @Autowired
    private TCameraRecorderDao tCameraRecorderDao;
    @Autowired
    private TCameraInfoMapper tCameraInfoMapper;
    @Autowired
    private TRobotInfoMapper tRobotInfoMapper;
    @Autowired
    private TVoiceDeviceMapper tVoiceDeviceMapper;
    @Autowired
    private SendToUpSystemDao sendToUpSystemDao;
    @Autowired
    private AnalysisUnionTaskFileService analysisUnionTaskFileService;
    @Autowired
    private RegisterManager registerManager;
    @Autowired
    private StatisticsDao statisticsDao;

    @Autowired
    private PatrolTaskDao patrolTaskDao;
    @Autowired
    private UrlPathHandler urlPathHandler;


    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String TASK_PRIORITY_REDIS_KEY="task_priority_config:";

    public static final String DAY_FORMAT = ",'%Y-%m-%d')";
    public static final String WEEK_FORMAT = " ,'%u') + 1";
    public static final String MONTH_FORMAT = " ,'%Y-%m')";

    @Transactional(rollbackFor = Exception.class)
    public int sendResponse(long receiveSessionId, String type, String command, String code, List<Map<String, Object>> items, boolean isSend) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(Constant.upSystemIp(), Constant.upSystemPort());
        TCPClientHandler tcpClientHandler = TCPClientHandler.getTcpClientHandlerHashMap(inetSocketAddress);
        if (tcpClientHandler != null) {
            if (code == null || StringUtils.isEmpty(code)) {
                code = Constant.stationCode();
            }
            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setType(type)
                    .setCommand(command)
                    .setCode(code)
                    .setItems(items);
            tcpClientHandler.send(xmlBaseModel, receiveSessionId, true);
            return 1;
        } else {
            log.error("服务未连接，请重试，port: {}", Constant.upSystemPort());
            // 把本级没有上报成功的巡视点结果暂存起来,重连服务后再上报上一级系统
            if ("61".equals(type)) {
                saveCruiseResultForMoment(items);
            }
            return -1;
        }
    }

    public Map<String, Object> creatModel(String type) {
        try {
            String stationCode =  (String)redisTemplate.opsForHash().get("t_sys_param:edgeId","content");
            Map<String,Object> map = new HashMap<>();
            Map<String,String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String path = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS") ? "C:\\robotData\\Model"+"/"+stationCode+"/Model":mapForPath.get("content")+"/"+stationCode+"/Model";
            log.info("模型文件路径："+path);

            switch (type) {
                case "1":
                    //巡视主机模型
                    String hostModelTargetPath = String.format(stationCode + "/Model/host_model.xml");
                    uploadFileToUpFtps(createHostModel(path, stationCode), hostModelTargetPath);
                    map.put("host_file_path", hostModelTargetPath);
                    break;
                case "2":
                    //机器人模型
                    String robotModelTargetPath = String.format(stationCode + "/Model/robot_model.xml");
                    uploadFileToUpFtps(createRobotModel(path), robotModelTargetPath);
                    map.put("robot_file_path", robotModelTargetPath);
                    break;
                case "3":
                    //摄像机模型
                    String videoModelTargetPath = String.format(stationCode + "/Model/video_model.xml");
                    uploadFileToUpFtps(createCameraModel(path), videoModelTargetPath);
                    map.put("video_file_path", videoModelTargetPath);
                    break;
                case "4":
                    //点位模型
                    String deviceModelTargetPath = String.format(stationCode + "/Model/device_model.xml");
                    uploadFileToUpFtps(createDeviceModel(path, stationCode), deviceModelTargetPath);
                    map.put("device_file_path", deviceModelTargetPath);
                    break;
                case "5":
                    //无人机
                    String droneModelTargetPath = String.format(stationCode + "/Model/drone_model.xml");
                    uploadFileToUpFtps(createDroneModel(path), droneModelTargetPath);
                    map.put("drone_file_path", droneModelTargetPath);
                    break;
                case "6":
                    //声纹模型
                    String voiceModelTargetPath = String.format(stationCode + "/Model/voice_model.xml");
                    uploadFileToUpFtps(createVoiceModel(path), voiceModelTargetPath);
                    map.put("voice_file_path", voiceModelTargetPath);
                    break;
                case "7":
                    //任务模型
                    String taskModelTargetPath = String.format(stationCode + "/Model/task_model.xml");
                    uploadFileToUpFtps(createTaskModel(path,stationCode), taskModelTargetPath);
                    map.put("task_file_path", taskModelTargetPath);
                    break;
                case "8":            //检修区域模型
                    String overhaulareaModelTargetPath = String.format(stationCode + "/Model/overhaularea_model.xml");
                    uploadFileToUpFtps(createMaintenanceModel(path, stationCode), overhaulareaModelTargetPath);
                    map.put("overhaularea_file_path", overhaulareaModelTargetPath);
                    break;
                case "9":
                    String mapRealPath = sendToUpSystemDao.selectMapPath();
                    String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute").get("content"));
                    String filePathMap = Constant.FTP_IMAGE_RELATIVE;
                    String mapAbsPath = mapRealPath.replace(filePathMap, fileFtpPathMap);
                    String mapModelTargetPath = stationCode + mapRealPath.replace(filePathMap, "");
                    uploadFileToUpFtps(mapAbsPath, mapModelTargetPath);
                    map.put("map_file_path", mapModelTargetPath);
                    break;
                case "10":
                    //设备资源信息配置文件
                    if ("1".equals(Constant.edgeLevel())) {
                        //边缘节点 10:设备资源信息配置文件
                        String sourceFilePath = stationCode + "/Model/source_file_model.cime";
                        String sourceModelMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sourceFilePath").get("content"));
                        String sourceModelPath = sourceModelMap + "source_file_model.cime";
                        uploadFileToUpFtps(sourceModelPath, sourceFilePath);
                        map.put("file_path", sourceFilePath);
                    } else {
                        //10:维护记录文件
                        String maintenanceModelTargetPath = stationCode + "/Model/maintenance_model.xml";
                        uploadFileToUpFtps(createMaintenanceModel(path), maintenanceModelTargetPath);
                        map.put("file_path", maintenanceModelTargetPath);
                    }
                    break;
                case "1001":
                    // 区域模型
                    String regionModelTargetPath = String.format(stationCode + "/Model/region_model.xml");
                    uploadFileToUpFtps(createRegionModel(path), regionModelTargetPath);
                    map.put("region_path", regionModelTargetPath);
                    break;
                case "1002":
                    // 录像机模型
                    String recordModelTargetPath = String.format(stationCode + "/Model/record_model.xml");
                    uploadFileToUpFtps(createRecordFile(path), recordModelTargetPath);
                    map.put("record_file_path", recordModelTargetPath);
                    break;
                default:
                    break;
            }


            return map;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    public List<Map<String, Object>> creatModelList(String type) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String stationCode = Constant.stationCode();
            Map<String,Object> map = new HashMap<>();
            Map<String,String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String path = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS") ? "C:\\robotData\\Model"+"/"+stationCode+"/Model":mapForPath.get("content")+"/"+stationCode+"/Model";
            log.info("模型文件路径："+path);

            switch (type) {
                case "1":
                    //巡视主机模型
                    String hostModelTargetPath = String.format(stationCode + "/Model/host_model.xml");
                    uploadFileToUpFtps(createHostModel(path, stationCode), hostModelTargetPath);
                    map.put("host_file_path", hostModelTargetPath);
                    list.add(map);
                    break;
                case "2":
                    //机器人模型
                    String robotModelTargetPath = String.format(stationCode + "/Model/robot_model.xml");
                    uploadFileToUpFtps(createRobotModel(path), robotModelTargetPath);
                    map.put("robot_file_path", robotModelTargetPath);
                    list.add(map);
                    break;
                case "3":
                    //摄像机模型
                    String videoModelTargetPath = String.format(stationCode + "/Model/video_model.xml");
                    uploadFileToUpFtps(createCameraModel(path), videoModelTargetPath);
                    map.put("video_file_path", videoModelTargetPath);
                    list.add(map);
                    break;
                case "4":
                    //点位模型
                    String deviceModelTargetPath = String.format(stationCode + "/Model/device_model.xml");
                    uploadFileToUpFtps(createDeviceModel(path, stationCode), deviceModelTargetPath);
                    map.put("device_file_path", deviceModelTargetPath);
                    list.add(map);
                    break;
                case "5":
                    //无人机
                    String droneModelTargetPath = String.format(stationCode + "/Model/drone_model.xml");
                    uploadFileToUpFtps(createDroneModel(path), droneModelTargetPath);
                    map.put("drone_file_path", droneModelTargetPath);
                    list.add(map);
                    break;
                case "6":
                    //声纹模型
                    String voiceModelTargetPath = String.format(stationCode + "/Model/voice_model.xml");
                    uploadFileToUpFtps(createVoiceModel(path), voiceModelTargetPath);
                    map.put("voice_file_path", voiceModelTargetPath);
                    list.add(map);
                    break;
                case "7":
                    //任务模型
                    String taskModelTargetPath = String.format(stationCode + "/Model/task_model.xml");
                    uploadFileToUpFtps(createTaskModel(path,stationCode), taskModelTargetPath);
                    map.put("task_file_path", taskModelTargetPath);
                    list.add(map);
                    break;
                case "8":            //检修区域模型
                    String overhaulareaModelTargetPath = String.format(stationCode + "/Model/overhaularea_model.xml");
                    uploadFileToUpFtps(createMaintenanceModel(path, stationCode), overhaulareaModelTargetPath);
                    map.put("overhaularea_file_path", overhaulareaModelTargetPath);
                    list.add(map);
                    break;
                case "9":
                    List<String> mapRealPathList = sendToUpSystemDao.selectMapPathAll();
                    if (CollectionUtils.isNotEmpty(mapRealPathList)) {
                        for (String mapRealPath : mapRealPathList) {
                            Map<String,Object> mapInfo = new HashMap<>();
                            String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute").get("content"));
                            String filePathMap = Constant.FTP_IMAGE_RELATIVE;
                            String mapAbsPath = mapRealPath.replace(filePathMap, fileFtpPathMap);
                            String mapModelTargetPath = stationCode + mapRealPath.replace(filePathMap, "");
                            uploadFileToUpFtps(mapAbsPath, mapModelTargetPath);
                            mapInfo.put("map_file_path", mapModelTargetPath);
                            list.add(mapInfo);
                        }
                    }
                    break;
                case "10":
                    if ("1".equals(Constant.edgeLevel())) {
                        //设备资源信息配置文件
                        String sourceFilePath = String.format(stationCode + "/Model/source_file_model.cime");
                        String sourceModelMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sourceFilePath").get("content"));
                        String sourceModelPath = sourceModelMap + "source_file_model.cime";
                        uploadFileToUpFtps(sourceModelPath, sourceFilePath);
                        map.put("source_file_path", sourceFilePath);
                    } else {
                        //10:维护记录文件
                        String maintenanceModelTargetPath = stationCode + "/Model/maintenance_model.xml";
                        uploadFileToUpFtps(createMaintenanceModel(path), maintenanceModelTargetPath);
                        map.put("maintenance_file_path", maintenanceModelTargetPath);
                    }
                    list.add(map);
                    break;
                case "11":
                    //联动配置文件
                    String linkageModelTargetPath = stationCode + "/Model/effect_model.xml";
                    uploadFileToUpFtps(createLinkageModel(path), linkageModelTargetPath);
                    map.put("effect_file_path", linkageModelTargetPath);
                    list.add(map);
                    break;
                case "12":
                    //告警阈值模型
                    String alarmThresholdTargetPath = stationCode + "/Model/alarm_threshold_" + Constant.stationCode()+ ".xml";
                    uploadFileToUpFtps(createAlarmThresholdModel(path), alarmThresholdTargetPath);
                    map.put("alarm_threshold_file_path", alarmThresholdTargetPath);
                    list.add(map);
                    break;
                case "1001":
                    // 区域模型
                    String regionModelTargetPath = String.format(stationCode + "/Model/region_model.xml");
                    uploadFileToUpFtps(createRegionModel(path), regionModelTargetPath);
                    map.put("region_file_path", regionModelTargetPath);
                    list.add(map);
                    break;
                case "1002":
                    // 录像机模型
                    String recordModelTargetPath = String.format(stationCode + "/Model/record_model.xml");
                    uploadFileToUpFtps(createRecordFile(path), recordModelTargetPath);
                    map.put("record_file_path", recordModelTargetPath);
                    list.add(map);
                    break;
                case "1003":
                    // 物联设备模型
                    Map<String,Object> param = new HashMap<>();
                    param.put("type",type);
                    Constant.otherServerPost(param,Constant.IOT_DEVICE_UPLOAD_URL);
                    break;
                default:
                    break;
            }


            return list;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Async
    public void creatFile(String type) {
        try {
            String stationCode =  Constant.stationCode();
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            Map<String, String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String path = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS") ? "D:\\testform\\robotTemplate" : mapForPath.get("content") + "/" + stationCode + "/Model";

            map.put("time", DateTimeUtil.getDateTimeString());
            map.put("type", type);
            log.info("模型文件路径：" + path);
            switch (type) {
                case "1":
                    //点位模型
                    // map.put("device_file_path",createDeviceModel(path));
                    String deviceModelTargetPath = String.format(stationCode + "/Model/device_model.xml");
                    uploadFileToUpFtps(createDeviceModel(path, stationCode), deviceModelTargetPath);
                    map.put("file_path", deviceModelTargetPath);
                    list.add(map);
                    break;
                case "2":
                    //巡视主机模型
                    String hostModelTargetPath = String.format(stationCode + "/Model/host_model.xml");
                    uploadFileToUpFtps(createHostModel(path, stationCode), hostModelTargetPath);
                    map.put("file_path", hostModelTargetPath);
                    list.add(map);
                    break;
                case "3":
                    //机器人模型
                    String robotModelTargetPath = String.format(stationCode + "/Model/robot_model.xml");
                    uploadFileToUpFtps(createRobotModel(path), robotModelTargetPath);
                    map.put("file_path", robotModelTargetPath);
                    list.add(map);
                    break;
                case "4":
                    //摄像机模型
                    String videoModelTargetPath = String.format(stationCode + "/Model/video_model.xml");
                    uploadFileToUpFtps(createCameraModel(path), videoModelTargetPath);
                    map.put("file_path", videoModelTargetPath);
                    list.add(map);
                    break;
                case "5":
                    //无人机
                    String droneModelTargetPath = String.format(stationCode + "/Model/drone_model.xml");
                    uploadFileToUpFtps(createDroneModel(path), droneModelTargetPath);
                    map.put("file_path", droneModelTargetPath);
                    list.add(map);
                    break;
                case "6":
                    //声纹模型
                    String voiceModelTargetPath = String.format(stationCode + "/Model/voice_model.xml");
                    uploadFileToUpFtps(createVoiceModel(path), voiceModelTargetPath);
                    map.put("file_path", voiceModelTargetPath);
                    list.add(map);
                    break;
                case "7":
                    //任务模型
                    String taskModelTargetPath = String.format(stationCode + "/Model/task_model.xml");
                    uploadFileToUpFtps(createTaskModel(path, stationCode), taskModelTargetPath);
                    map.put("file_path", taskModelTargetPath);
                    list.add(map);
                    break;
                case "8":
                    //检修区域模型
                    String overhaulareaModelTargetPath = String.format(stationCode + "/Model/overhaularea_model.xml");
                    uploadFileToUpFtps(createMaintenanceModel(path, stationCode), overhaulareaModelTargetPath);
                    map.put("file_path", overhaulareaModelTargetPath);
                    list.add(map);
                    break;
                case "9":
                    List<String> mapRealPaths = sendToUpSystemDao.selectMapPathAll();
                    for (String mapRealPath:mapRealPaths) {
                        String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute").get("content"));
                        String filePathMap = Constant.FTP_IMAGE_RELATIVE;
                        String mapAbsPath = mapRealPath.replace(filePathMap, fileFtpPathMap);
                        String mapModelTargetPath = stationCode + mapRealPath.replace(filePathMap, "");
                        uploadFileToUpFtps(mapAbsPath, mapModelTargetPath);
                        Map<String,Object> mapMap = new HashMap<>();
                        mapMap.put("type", type);
                        mapMap.put("file_path", mapModelTargetPath);
                        list.add(mapMap);
                    }
                    break;
                case "10":
                    if ("1".equals(Constant.edgeLevel())) {
                        //边缘节点 10:设备资源信息配置文件
                        String sourceFilePath = stationCode + "/Model/source_file_model.cime";
                        String sourceModelMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sourceFilePath").get("content"));
                        String sourceModelPath = sourceModelMap + "source_file_model.cime";
                        uploadFileToUpFtps(sourceModelPath, sourceFilePath);
                        map.put("file_path", sourceFilePath);
                    } else {
                        //10:维护记录文件
                        String maintenanceModelTargetPath = stationCode + "/Model/maintenance_model.xml";
                        uploadFileToUpFtps(createMaintenanceModel(path), maintenanceModelTargetPath);
                        map.put("file_path", maintenanceModelTargetPath);
                    }
                    list.add(map);
                    break;
                case "11":
                    //联动配置文件
                    String linkageModelTargetPath = stationCode + "/Model/effect_model.xml";
                    uploadFileToUpFtps(createLinkageModel(path), linkageModelTargetPath);
                    map.put("file_path", linkageModelTargetPath);
                    list.add(map);
                    break;
                case "12":
                    //告警阈值模型
                    String alarmThresholdTargetPath = stationCode + "/Model/alarm_threshold_" + Constant.stationCode()+ ".xml";
                    uploadFileToUpFtps(createAlarmThresholdModel(path), alarmThresholdTargetPath);
                    map.put("file_path", alarmThresholdTargetPath);
                    list.add(map);
                    break;
                case "1001":
                    // 区域模型
                    String regionModelTargetPath = String.format(stationCode + "/Model/region_model.xml");
                    uploadFileToUpFtps(createRegionModel(path), regionModelTargetPath);
                    map.put("file_path", regionModelTargetPath);
                    list.add(map);
                    break;
                case "1002":
                    // 录像机模型
                    String recordModelTargetPath = String.format(stationCode + "/Model/record_model.xml");
                    uploadFileToUpFtps(createRecordFile(path), recordModelTargetPath);
                    map.put("file_path", recordModelTargetPath);
                    list.add(map);
                    break;
                default:
                    break;
            }
            sendResponse(0L, "11", "", stationCode, list, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private String createAlarmThresholdModel(String path) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        List<TCruisePointInstanceMeteDetail> meteDetails = sendToUpSystemDao.selectAlarmThresholdModel();
        meteDetails.forEach(t->{
            String alarmType = recognitionTypeToAlarmType(t.getRecognitionType(), String.valueOf(t.getIsTemdif()));
            String alarmDesc = "";
            //遥信 (tele-signal) - requires alarm_level and includes it in output
            if (t.getMeteKind() == 1 && t.getAlarmLevel() != null && t.getAlarmLevel() > 130) {
                int alarmLevel = t.getAlarmLevel() == 131 ? 1 : t.getAlarmLevel() == 132 ? 2 : 3;
                String alarmState = t.getAlarmState() == 0 ? t.getStateZero() : t.getStateOne();
                alarmDesc = "告警级别：" + getAlarmLevel(alarmLevel) + ";告警状态：" + alarmState;
                Map<String, Object> item = getAlarmMapWithLevel(t, 1, alarmState, alarmLevel, alarmDesc, alarmType);
                list.add(item);
            }
            //遥测 (telemetry) - does NOT require alarm_level and does NOT include it in output
            if (t.getMeteKind() == 2) {
                if (t.getHighLimit2() != null) {
                    alarmDesc =  "告警上限：" + t.getHighLimit2();
                    Map<String, Object> itemHigh = getAlarmMapWithoutLevel(t, 6, t.getHighLimit2(), alarmDesc, alarmType);
                    list.add(itemHigh);
                }
                if (t.getLowLimit2() != null) {
                    alarmDesc =  "告警下限：" + t.getLowLimit2();
                    Map<String, Object> itemLow = getAlarmMapWithoutLevel(t, 7, t.getLowLimit2(), alarmDesc, alarmType);
                    list.add(itemLow);
                }
                if (t.getHighLimit3() != null) {
                    alarmDesc =  "告警上限：" + t.getHighLimit3();
                    Map<String, Object> itemHigh = getAlarmMapWithoutLevel(t, 6, t.getHighLimit3(), alarmDesc, alarmType);
                    list.add(itemHigh);
                }
                if (t.getLowLimit3() != null) {
                    alarmDesc =  "告警下限：" + t.getLowLimit3();
                    Map<String, Object> itemLow = getAlarmMapWithoutLevel(t, 7, t.getLowLimit3(), alarmDesc, alarmType);
                    list.add(itemLow);
                }
                if (t.getHighLimit4() != null) {
                    alarmDesc =  "告警上限：" + t.getHighLimit4();
                    Map<String, Object> itemHigh = getAlarmMapWithoutLevel(t, 6, t.getHighLimit4(), alarmDesc, alarmType);
                    list.add(itemHigh);
                }
                if (t.getLowLimit4() != null) {
                    alarmDesc =  "告警下限：" + t.getLowLimit4();
                    Map<String, Object> itemLow = getAlarmMapWithoutLevel(t, 7, t.getLowLimit4(), alarmDesc, alarmType);
                    list.add(itemLow);
                }
            }
        });
        return CreateModeXMLUtil.createXmlFile(list, path, "alarm_threshold_" + Constant.stationCode() + ".xml", "Alarm_Threshold");
    }

    private String getAlarmLevel(int alarmLevel) {
        switch (alarmLevel) {
            case 2:
                return "严重";
            case 3:
                return "危急";
            default:
                return "一般";
        }
    }

    public static String recognitionTypeToAlarmType(String recognitionType,String isTemdif){
        String alarmType = "";
        switch (recognitionType){
            case "1":
            case "11":
            case "12":
            case "13":
                //仪表越限报警
                alarmType = "7";
                break;
            case "2":
                //变位报警
                alarmType = "10";
                break;
            case "3":
                //外观异常
                alarmType = "6";
                break;
            case "4":
                if ("0".equals(isTemdif)) {
                    //超温报警
                    alarmType = "1";
                } else {
                    //温升报警
                    alarmType = "2";
                }
                break;
            case "5":
                //声音异常
                alarmType = "5";
                break;
            default:
                break;
        }
        return alarmType;
    }

    public Map<String, Object> getAlarmMap(TCruisePointInstanceMeteDetail detail, Integer rule,
                                           Object value, Integer level, String alarmDesc, String alarmType) {
        Map<String, Object> item = new HashMap<>(11);
        if (Constant.standardPoints()){
            item.put("device_id", detail.getDevicePointId());
        } else {
            item.put("device_id", detail.getInstanceId());
        }
        item.put("device_name", detail.getMeteName());
        item.put("defect_type", "");
        item.put("station_code", Constant.stationCode());
        item.put("station_name", Constant.stationName());
        item.put("decide_rule", rule);
        item.put("decision_value_class", 1);
        item.put("alarm_desc", alarmDesc);
        item.put("alarm_type", alarmType);
        item.put("base_line_value", value);
        item.put("alarm_level", level);
        return item;
    }

    /**
     * Create alarm map WITH alarm_level field for 遥信 (tele-signal) points
     */
    public Map<String, Object> getAlarmMapWithLevel(TCruisePointInstanceMeteDetail detail, Integer rule,
                                           Object value, Integer level, String alarmDesc, String alarmType) {
        Map<String, Object> item = buildBaseAlarmMap(detail, rule, value, alarmDesc, alarmType);
        item.put("alarm_level", level);
        return item;
    }

    /**
     * Create alarm map WITHOUT alarm_level field for 遥测 (telemetry) points
     */
    public Map<String, Object> getAlarmMapWithoutLevel(TCruisePointInstanceMeteDetail detail, Integer rule,
                                                     Object value, String alarmDesc, String alarmType) {
        // Note: alarm_level is intentionally NOT included for telemetry points
        return buildBaseAlarmMap(detail, rule, value, alarmDesc, alarmType);
    }

    /**
     * Build base alarm map with common fields
     */
    private Map<String, Object> buildBaseAlarmMap(TCruisePointInstanceMeteDetail detail, Integer rule,
                                                   Object value, String alarmDesc, String alarmType) {
        Map<String, Object> item = new HashMap<>(11);
        if (Constant.standardPoints()){
            item.put("device_id", detail.getDevicePointId());
        } else {
            item.put("device_id", detail.getInstanceId());
        }
        item.put("device_name", detail.getMeteName());
        item.put("defect_type", "");
        item.put("station_code", Constant.stationCode());
        item.put("station_name", Constant.stationName());
        item.put("decide_rule", rule);
        item.put("decision_value_class", 1);
        item.put("alarm_desc", alarmDesc);
        item.put("alarm_type", alarmType);
        item.put("base_line_value", value);
        return item;
    }

    private String createLinkageModel(String path) throws Exception {
        List<Map<String, Object>> list = sendToUpSystemDao.selectLinkageModel(Constant.standardPoints());
        return CreateModeXMLUtil.createXmlFile(list, path, "effect_model.xml", "Effect_Config");
    }

    private String createMaintenanceModel(String path) throws Exception {
        List<Map<String, Object>> list = sendToUpSystemDao.selectMaintenanceModel(Constant.stationName(), Constant.stationCode());
        return CreateModeXMLUtil.createXmlFile(list, path, "maintenance_model.xml", "Maintenance_Record");
    }


    public String createRecordFile(String path) throws Exception {
        List<TCameraRecorder> tCameraRecorderList = tCameraRecorderDao.selectAll();
        Result re = Constant.getForObject(Constant.GET_WVP_SERVER_CONFIG);
        JSONObject obj = (JSONObject) JSON.toJSON(re.getData());
        tCameraRecorderList.forEach(tCameraRecorder -> tCameraRecorder.setDeviceChannel(String.valueOf(obj.get("username"))));
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        List<Map<String, Object>> list = tCameraRecorderList.stream().map((Function<TCameraRecorder, Map<String, Object>>) tCameraRecorder -> {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(tCameraRecorder, serializeConfig);
            return jsonObject.toJavaObject(Map.class);
        }).collect(Collectors.toList());
        return CreateModeXMLUtil.createXmlFile(list, path, "record_model.xml", "PatrolDevice_Model");
    }

    @Transactional(rollbackFor = Exception.class)
    public int sendXML(XMLBaseModel xmlBaseModel) {
        long sessionId =0L;
        boolean isSend = true;
        if (CollectionUtils.isNotEmpty(xmlBaseModel.getItems()) && xmlBaseModel.getItems().get(0).containsKey("error_code")){
            Long sId = Constant.getParamMap.remove("sendSessionId");
            sessionId = sId == null ? 0L : sId;
            if (sessionId == 0L) {
                log.warn("非上级系统系统下发的任务，无须上报");
                return 0;
            }
            isSend = false;
        }
        return this.sendResponse(sessionId, xmlBaseModel.getType(), xmlBaseModel.getCommand(), xmlBaseModel.getCode(), xmlBaseModel.getItems(), isSend);
    }

    @Transactional(rollbackFor = Exception.class)
    public String putFile(String filePath, String targetPath) {
        log.info("---------文件上传区域巡视主机, filepath:{}, targetPath: {}", filePath, targetPath);
        File file = new File(filePath);
        if (file.exists()){
            uploadFileToUpFtps(filePath, targetPath);
            return "文件上传成功！";
        }else {
            return "文件不存在！";
        }
    }

    public String createDeviceModel(String path, String stationCode) throws Exception {
//        Map<String, String> selectEdgeMap = redisTemplate.opsForHash().entries("t_sys_param:selectEdge");
//        String selectEdge = selectEdgeMap.get("content");
        String edgeLevel = Constant.edgeLevel();
//        if ("1".equals(edgeLevel)){
//            selectEdge = null;
//        }
        String presetImgPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:presetImgPath", "content"));
        List<Map<String, Object>> list = sendToUpSystemDao.selectDeviceModel(Constant.standardPoints(),Constant.middlegroundIds(), null, Constant.PRESET_REAL_IMG_PATH, presetImgPath);
        String stationName = getStationName();
        list.forEach(item->{
            item.put("station_code",stationCode);
            item.put("station_name", stationName);
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device_code","");
            jsonObject.put("device_pos","");
            jsonObject.put("robot_code","");
            jsonObject.put("robot_pos","");
            jsonObject.put("uav_code","");
            jsonObject.put("uav_pos","");
            if (item.get("cruise_type").equals(228)){// 机器人
                item.put("save_type_list","jpg");
                item.put("data_type","2");
                jsonObject.put("robot_code", item.get("robot_num"));
                jsonObject.put("robot_pos",item.get("inspection_id"));
            } else if (item.get("cruise_type").equals(229) || item.get("cruise_type").equals(230)){//视屏 红外
                item.put("save_type_list","jpg");
                item.put("data_type","1");
                if (CommonUtils.isEmptyOrNullstr(String.valueOf(item.get("b_camera_channel_id")))) {
                    jsonObject.put("device_code", item.get("camera_channel_id"));
                } else {
                    jsonObject.put("device_code", item.get("b_camera_channel_id"));
                }
                jsonObject.put("device_pos",item.get("preset_num"));
            }else if (item.get("cruise_type").equals(232)){//声纹
                item.put("save_type_list","wav");
                item.put("data_type","8");
                jsonObject.put("voice_code",item.get("voice_id"));
                jsonObject.put("voice_pos",item.get("voice_id"));
            }else if (item.get("cruise_type").equals(524)){// 无人机
                item.put("save_type_list","jpg");
                item.put("data_type","4");
                jsonObject.put("uav_code", item.get("robot_num"));
                jsonObject.put("uav_pos",item.get("inspection_id"));
            }else if (item.get("cruise_type").equals(231)){// 主辅系统
                item.put("save_type_list","");
                item.put("data_type","16");
                jsonObject.put("online_code",item.get("mete_id"));
                jsonObject.put("online_pos",item.get("mete_id"));
            }

            item.remove("cruise_type");
            item.remove("camera_id");
            item.remove("preset_num");
            item.remove("robot_code");
            item.remove("inspection_id");
            jsonArray.add(jsonObject);
            item.put("video_pos",jsonArray.toJSONString());
            if ("1".equals(edgeLevel) && Objects.nonNull(item.get("preset_img")) && Objects.nonNull(item.get("local_path"))){
                item.put("preset_img", "/" + stationCode + item.get("preset_img"));
                String localPath = String.valueOf(item.get("local_path")).replace(Constant.PRESET_REAL_IMG_PATH,presetImgPath);
                String targetPath = String.valueOf(item.get("preset_img"));
                uploadFileToUpFtps(localPath, targetPath);
            }
            item.remove("local_path");
        });
        return CreateModeXMLUtil.createXmlFile(list, path, "device_model.xml", "Device_Model");
    }

    private String getStationName() {
        Map<String, String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:stationName");
        return mapForPath.get("content");
    }

    public String createRobotModel(String path) throws Exception {
        //机器人模型
        List<RobotModel> robotModelList = tRobotInfoMapper.selectAllRobot();
        String stationCode = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "edgeId", "content");
        String stationName = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "stationName", "content");


        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        List<Map<String, Object>> list = robotModelList.stream().peek(robotModel -> {
            robotModel.setStationCode(stationCode);
            robotModel.setStationName(stationName);
            robotModel.setMountPatroldeviceCode("");
            if (robotModel.getPhotePath() != null){
                robotModel.setPhotePath(robotModel.getPhotePath().replace(Constant.FTP_IMAGE_RELATIVE,absoluteImgMap.get("content")));
            }
        }).map((Function<RobotModel, Map<String, Object>>) robotModel -> {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(robotModel, serializeConfig);
            return jsonObject.toJavaObject(Map.class);
        }).collect(Collectors.toList());
        return CreateModeXMLUtil.createXmlFile(list, path, "robot_model.xml", "PatrolDevice_Model");
    }

    public String createCameraModel(String path) throws Exception {
        //摄像机模型
        List<CameraModel> cameraModelList = tCameraInfoMapper.selectAll();
        String stationCode = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "edgeId", "content");
        String stationName = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "stationName", "content");
        Result re = Constant.getForObject(Constant.GET_WVP_SERVER_CONFIG);
        JSONObject obj = (JSONObject) JSON.toJSON(re.getData());
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        List<Map<String, Object>> list = cameraModelList.stream().peek(cameraModel -> {
            if ("11".equals(cameraModel.getType())){
                cameraModel.setCameraChannelId(String.valueOf(obj.get("username")));
            }
            cameraModel.setStationCode(stationCode);
            cameraModel.setStationName(stationName);
        }).map((Function<CameraModel, Map<String, Object>>) cameraModel -> {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(cameraModel, serializeConfig);
            return jsonObject.toJavaObject(Map.class);
        }).collect(Collectors.toList());
        return CreateModeXMLUtil.createXmlFile(list, path, "video_model.xml", "PatrolDevice_Model");
    }

    public String createRegionModel(String path) throws Exception {
        String stationCode = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "edgeId", "content");
        String stationName = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "stationName", "content");
        List<TStdRegion> tStdRegionList = tStdRegionMapper.selectAll(stationCode, stationName);
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        List<Map<String, Object>> list = tStdRegionList.stream().map((Function<TStdRegion, Map<String, Object>>) tStdRegion -> {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(tStdRegion, serializeConfig);
            return jsonObject.toJavaObject(Map.class);
        }).collect(Collectors.toList());
        return CreateModeXMLUtil.createXmlFile(list, path, "region_model.xml", "PatrolDevice_Model");

    }

    public String createDroneModel(String path) throws Exception {
        //无人机模型
        List<RobotModel> robotModelList = tRobotInfoMapper.selectAllDrone();
        String stationCode = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "edgeId", "content");
        String stationName = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "stationName", "content");
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        List<Map<String, Object>> list = robotModelList.stream().peek(robotModel -> {
            robotModel.setStationCode(stationCode);
            robotModel.setStationName(stationName);
        }).map((Function<RobotModel, Map<String, Object>>) robotModel -> {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(robotModel, serializeConfig);
            return jsonObject.toJavaObject(Map.class);
        }).collect(Collectors.toList());
        return CreateModeXMLUtil.createXmlFile(list, path, "drone_model.xml", "PatrolDevice_Model");
    }

    public String createVoiceModel(String path) throws Exception {
        //声纹模型
        List<VoiceDeviceModel> voiceDeviceModelList =tVoiceDeviceMapper.selectAll();
        String stationCode = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "edgeId", "content");
        String stationName = (String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "stationName", "content");
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        List<Map<String, Object>> list = voiceDeviceModelList.stream().peek(voiceDeviceModel -> {
            voiceDeviceModel.setStationCode(stationCode);
            voiceDeviceModel.setStationName(stationName);
            voiceDeviceModel.setMountPatroldeviceCode("");
        }).map((Function<VoiceDeviceModel, Map<String, Object>>) voiceDeviceModel -> {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(voiceDeviceModel, serializeConfig);
            return jsonObject.toJavaObject(Map.class);
        }).collect(Collectors.toList());
        return CreateModeXMLUtil.createXmlFile(list, path, "voice_model.xml", "PatrolDevice_Model");
    }

    public String createTaskModel(String path, String stationCode) throws Exception {
        //任务模型
        boolean standardPoints = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("t_sys_param:standardPoints", "content"));
        String stationName = getStationName();
        List<Map<String, Object>> list = patrolTaskDao.selectTaskInfo(DateUtil.dateSecond());
        //CronExpression expression;
        for (Map<String, Object> item : list) {
            String taskId = item.get("task_code").toString();
            List<UPatrolPlanAttr> instanceIdList = patrolTaskDao.selectInstanceId(taskId);
            item.put("station_code", stationCode);
            item.put("station_name", stationName);
            String deviceList;
            if (standardPoints) {
                deviceList = instanceIdList.stream().map(UPatrolPlanAttr::getDevicePointId).collect(Collectors.joining(","));
            } else {
                deviceList = instanceIdList.stream().map(UPatrolPlanAttr::getInstanceId).map(String::valueOf).collect(Collectors.joining(","));
            }

            item.put("device_list", deviceList);

            // 填充任务模型中时间相关字段
            fillTaskModelField(item);

        }
        return CreateModeXMLUtil.createXmlFile(list, path, "task_model.xml", "Task_Model");

    }

    public String createMaintenanceModel(String path, String stationCode) throws Exception {
        //检修区域模型
        List<MaintenanceModel> infoList = sendToUpSystemDao.selectMaintenanceInfo();
        List<Map<String, Object>> finalList = new ArrayList<>();
        String stationName = getStationName();
        infoList.forEach(item -> {
            Map<String, Object> maintenanceMap = new HashMap<>();
            maintenanceMap.put("station_code", stationCode);
            maintenanceMap.put("station_name", stationName);
            maintenanceMap.put("config_code", item.getConfigCode());
            maintenanceMap.put("enable", "1");
            maintenanceMap.put("start_time", item.getStartTime());
            maintenanceMap.put("end_time", item.getEndTime());
            maintenanceMap.put("device_level", item.getDeviceLevel());
            int deviceLevel = NumberUtils.toInt(item.getDeviceLevel(), 3);
            maintenanceMap.put("device_list", maintenanceDeviceIds(deviceLevel, item.getDeviceIds()));


            maintenanceMap.put("coordinate_pixel", item.getCoordinatePixel());
            finalList.add(maintenanceMap);
        });
        return CreateModeXMLUtil.createXmlFile(finalList, path, "overhaularea_model.xml", "Effect_Config");


    }

    // TODO
    public String maintenanceDeviceIds(int deviceLevel, String ids) {
        List<Map<String, Object>> deviceInspectionList = sendToUpSystemDao.selectStandardPointsByInstanceId(ids);

            /*
        middlegroundIds = true
        间隔：t_std_region.up_region_ids
        主设备：t_std_device_attr.pms_id
        部件：t_std_devicemete.component_id
         */
        String key = "instance_id";
        switch (deviceLevel) {
            case 1:
                // 间隔
                if (Constant.middlegroundIds()) {
                    key = "middle_bay_id";
                } else {
                    key = "bay_id";
                }
                break;
            case 2:
                // 主设备
                if (Constant.middlegroundIds()) {
                    key = "middle_device_id";
                } else {
                    key = "main_device_id";
                }
                break;
            case 3:
                // 设备点位
                // 如果从上级系统下发  点位为机器人的id 对应t_std_devicemete表中的device_point_id 需要转为 巡视系统的instanceId
                if (Constant.standardPoints()) {
                    key = "device_point_id";
                }
                break;
            case 4:
                // 设备部件
                if (Constant.middlegroundIds()) {
                    key = "middle_component_id";
                } else {
                    key = "component_id";
                }
                break;
            default:
                key = "instance_id";
                break;
        }
        final String k = key;
        return deviceInspectionList.stream().map(r -> MapUtils.getString(r, k)).distinct().collect(Collectors.joining(","));
    }

    public String createHostModel(String path, String stationCode) throws Exception {
        //巡视主机模型

        String edgeLevel = (String)redisTemplate.opsForHash().get("t_sys_param:edgeLevel","content");
        String productionDate = (String)redisTemplate.opsForHash().entries("t_sys_param:commissioningTime").get("content");

        List<Map<String, Object>> finalList = new ArrayList<>();
        Map<String, Object> host = new HashMap<>();
        host.put("station_name", getStationName());
        host.put("station_code", stationCode);
        host.put("patroldevice_code", "YJH-001");
        host.put("device_model", "YJH");
        host.put("manufacturer", "亿嘉和");
        host.put("use_unit", "亿嘉和");
        host.put("device_source", "亿嘉和");
        host.put("commissioning_time", productionDate);
        host.put("production_date", productionDate);
        host.put("production_code", "001");
        host.put("istransport", "");
        host.put("use_mode", "");
        host.put("video_mode", "");
        host.put("place", "");
        if ("1".equals(edgeLevel)){
            host.put("type", "21");
            host.put("patroldevice_name", "边缘节点");
        } else {
            host.put("type", "20");
            host.put("patroldevice_name", "巡视主机");
        }
        host.put("patroldevice_info", "");
        host.put("mount_patroldevice_code", "");
        host.put("robots_code", "");
        finalList.add(host);
        return CreateModeXMLUtil.createXmlFile(finalList, path, "host_model.xml", "PatrolDevice_Model");
    }

    public String createMapModel() {
        String mapRealPath = sendToUpSystemDao.selectMapPath();
        String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath").get("content"));
        String filePathMap = Constant.FTP_IMAGE_RELATIVE;
        String mapAbsPath = mapRealPath.replace(filePathMap, fileFtpPathMap);
        return mapAbsPath;
    }

    public List<Map<String, Object>> resultStatistical(Map<String, Object> item) {
        String startTime = null;
        String endTime = null;
        String type = null;
        String year = null;
        String month = null;
        String cmd = null;
        // 1 - 巡视任务执行闭环率 任务执行闭环率=闭环任务数量/执行任务总数*100% 任务正常的判据（自主启停）；断点续传任务认为是闭环任务
        // 2 - 巡视告警人工审核完成率 巡视告警人工审核完成率=（已审核告警数量/告警总数）*100%
        // 3 - 巡视告警准确率 告警准确率=（已审核未人工修正的告警数量/已审核告警总数）*100%
        // 4 - 巡视结果人工审核完成率 巡视结果人工审核完成率 =  （已审核巡检结果数量/总巡检结果数量）*100%
        // 5 - 巡视点位漏检率  漏检率=（漏检点位数量/巡视点位总数量）*100%\

        if (item.get("begin_time") != null) {
            startTime = item.get("begin_time").toString();
        }
        if (item.get("end_time") != null) {
            endTime = item.get("end_time").toString();
        }
        if (item.get("type") != null) {
            type = item.get("type").toString();
        }
        if (item.get("cmd") != null) {
            cmd = item.get("cmd").toString();
        }
        if (item.get("year") != null) {
            year = item.get("year").toString();
        }
        if (item.get("month") != null) {
            month = item.get("month").toString();
        }
        List<Map<String, Object>> resultStatistical = new ArrayList<>();
        switch (cmd) {
            case "1":
                resultStatistical.addAll(countTask(type,startTime, endTime));
                break;
            case "2":
                resultStatistical.addAll(countWarnCheck(type,startTime, endTime));
                break;
            case "3":
                resultStatistical.addAll(countWarnAccuracy(type,startTime, endTime));
                break;
            case "4":
                resultStatistical.addAll(countResultCheck(type,startTime, endTime));
                break;
            case "5":
                resultStatistical.addAll(countInstanceLoss(type,startTime, endTime));
                break;
            default:
                break;
        }
        if(CollectionUtils.isNotEmpty(resultStatistical)) {
            for (Map<String,Object> st : resultStatistical) {
                dealPercent(st);
            }
            Map<String,Object> st = resultStatistical.get(0);

            // 常州对接南瑞上级不可以有这两个参数
            if (!Constant.nariUpSystem()) {
                st.put("command", cmd);
                st.put("type", type);
                st.put("param_year", year);
                st.put("param_month", month);
                st.put("startTime", startTime);
                st.put("endTime", endTime);
            }
        }
        return resultStatistical;
    }

    private HashMap<String, Object> dealCount(HashMap<String, Object> countMap) {
        String totalNumStr = MapUtils.getString(countMap, "totalNum", "0");
        String validNumStr = MapUtils.getString(countMap, "validNum", "0");
        double totalNum = NumberUtils.toDouble(totalNumStr);
        double validNum = NumberUtils.toDouble(validNumStr);
        String percent = totalNum > 0 ? String.format("%.3f", validNum * 100 / totalNum) : "0.000";
        HashMap<String, Object> reMap = new HashMap<>();
        reMap.put("total_num", totalNumStr);
        reMap.put("valid_num", validNumStr);
        reMap.put("percent", percent + "%");
        return reMap;
    }

    public List<Map<String, Object>> countTask(String type, String startTime, String endTime) {
        // 巡视任务闭环率
        List<Map<String, Object>> result = new ArrayList<>();
        if (StringUtils.isBlank(type)) {
            result.add(dealCount(sendToUpSystemDao.countTask(startTime, endTime)));
        } else {
            switch (type) {
                case "1":
                    result = statisticsDao.countTask(startTime, endTime, DAY_FORMAT, "day");
                    dealDay(result, startTime, endTime);
                    break;
                case "2":
                    result = statisticsDao.countTask(startTime, endTime, WEEK_FORMAT, "week");
                    dealWeek(result, startTime, endTime);
                    break;
                case "3":
                    result = statisticsDao.countTask(startTime, endTime, MONTH_FORMAT, "month");
                    dealMonth(result, startTime, endTime);
                    break;
                default:
                    break;
            }
        }


        return result;
    }

    public List<Map<String, Object>> countWarnCheck(String type, String startTime, String endTime) {
        // 巡视任务闭环率
        List<Map<String, Object>> result = new ArrayList<>();
        if (StringUtils.isBlank(type)) {
            result.add(dealCount(sendToUpSystemDao.countWarnCheck(startTime, endTime)));
        } else {

            switch (type) {
                case "1":
                    result = statisticsDao.countWarnCheckByMonth(startTime, endTime, DAY_FORMAT, "day");
                    dealDay(result, startTime, endTime);
                    break;
                case "2":
                    result = statisticsDao.countWarnCheckByMonth(startTime, endTime, WEEK_FORMAT, "week");
                    dealWeek(result, startTime, endTime);
                    break;
                case "3":
                    result = statisticsDao.countWarnCheckByMonth(startTime, endTime, MONTH_FORMAT, "month");
                    dealMonth(result, startTime, endTime);
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    public List<Map<String, Object>> countLabelAccuracy(String startTime, String endTime) {
        List<Map<String, Object>> result = new ArrayList<>();

        return result;
    }

    public List<Map<String, Object>> countWarnAccuracy(String type, String startTime, String endTime) {
        // 巡视告警准确率
        // 巡视任务闭环率
        List<Map<String, Object>> result = new ArrayList<>();
        if (StringUtils.isBlank(type)) {
            result.add(dealCount(sendToUpSystemDao.countWarnAccuracy(startTime, endTime)));
        } else {
            switch (type) {
                case "1":
                    result = statisticsDao.countWarnAccuracyByMonth(startTime, endTime, DAY_FORMAT, "day");
                    dealDay(result, startTime, endTime);
                    break;
                case "2":
                    result = statisticsDao.countWarnAccuracyByMonth(startTime, endTime, WEEK_FORMAT, "week");
                    dealWeek(result, startTime, endTime);
                    break;
                case "3":
                    result = statisticsDao.countWarnAccuracyByMonth(startTime, endTime, MONTH_FORMAT, "month");
                    dealMonth(result, startTime, endTime);
                    break;
                default:
                    break;
            }
        }


        return result;
    }

    public List<Map<String, Object>> countInstanceLoss(String type, String startTime, String endTime) {
        // 巡视点位漏检率
        List<Map<String, Object>> result = new ArrayList<>();
        if (StringUtils.isBlank(type)) {
            result.add(dealCount(sendToUpSystemDao.countInstanceLoss(startTime, endTime)));
        } else {
            switch (type) {
                case "1":
                    result = statisticsDao.countInstanceLossByMonth(startTime, endTime, DAY_FORMAT, "day");
                    dealDay(result, startTime, endTime);
                    break;
                case "2":
                    result = statisticsDao.countInstanceLossByMonth(startTime, endTime, WEEK_FORMAT, "week");
                    dealWeek(result, startTime, endTime);
                    break;
                case "3":
                    result = statisticsDao.countInstanceLossByMonth(startTime, endTime, MONTH_FORMAT, "month");
                    dealMonth(result, startTime, endTime);
                    break;
                default:
                    break;
            }
        }


        return result;
    }

    public List<Map<String, Object>> countResultCheck(String type, String startTime, String endTime) {
        // 巡视结果人工审核完成率
        List<Map<String, Object>> result = new ArrayList<>();
        if (StringUtils.isBlank(type)) {
            result.add(dealCount(sendToUpSystemDao.countResultCheck(startTime, endTime)));
        } else {
            switch (type) {
                case "1":
                    result = statisticsDao.countResultCheckByDay(startTime, endTime, DAY_FORMAT, "day");
                    dealDay(result, startTime, endTime);
                    break;
                case "2":
                    result = statisticsDao.countResultCheckByDay(startTime, endTime, WEEK_FORMAT, "week");
                    dealWeek(result, startTime, endTime);
                    break;
                case "3":
                    result = statisticsDao.countResultCheckByDay(startTime, endTime, MONTH_FORMAT, "month");
                    dealMonth(result, startTime, endTime);
                    break;
                default:
                    break;
            }
        }


        return result;
    }

    private List<String> getBetweenDates(Date start, Date end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<String> result = new ArrayList<String>();
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(start);
        tempStart.add(Calendar.DATE, 0);

        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(end);
        while (tempStart.before(tempEnd)) {
            result.add(sdf.format(tempStart.getTime()));
            tempStart.add(Calendar.DAY_OF_YEAR, +1);
        }
        return result;
    }

    private void dealPercent(Map<String,Object> statistics) {
        if (statistics.get("totalNum") != null) {
            Double totalNum = Double.valueOf(statistics.get("totalNum").toString());
            Double validNum = Double.valueOf(statistics.get("validNum").toString());
            String percent = String.format("%.2f", validNum * 100 / totalNum);
            statistics.put("percent",percent + "%");
        }
    }

    private void dealDay(List<Map<String,Object>> result, String startTimeString,String endTimeString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date startTime = null;
        Date endTime = null;
        try {
            startTime = simpleDateFormat.parse(startTimeString);
            endTime =simpleDateFormat.parse(endTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> dateList = getBetweenDates(startTime, endTime);
        for (String date : dateList) {
            boolean b = result.stream().anyMatch(m -> m.get("day").equals(date));
            if (!b) {
                Map<String,Object> st = Maps.newHashMap();
                st.put("day",date);
                result.add(st);
            }
        }
        result.sort(Comparator.comparing(e->e.get("day").toString()));
    }

    private void dealWeek(List<Map<String,Object>> result, String startTimeString,String endTimeString) {
        Date startTime = null;
        Date endTime = null;
        Integer startWeek = 0;
        Integer endWeek = 0;
        try {
            startTime = simpleDateFormat.parse(startTimeString);
            endTime =simpleDateFormat.parse(endTimeString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);
            startWeek = calendar.get(Calendar.WEEK_OF_YEAR);
            calendar.setTime(endTime);
            endWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        result.forEach(map-> map.put("week", Double.valueOf(map.get("week").toString()).intValue()));
        if (result.size() + 1 < (endWeek - startWeek)) {
            for (Integer i = startWeek; i <= endWeek; i++) {
                int f1 = i;
                boolean b = result.stream().anyMatch(m -> m.get("week").equals(f1));
                if (!b) {
                    Map<String,Object> st = Maps.newHashMap();
                    st.put("week",i);
                    result.add(st);
                }
            }
        }
        result.sort(Comparator.comparing(e->e.get("week").toString()));
    }

    private void dealMonth(List<Map<String,Object>> result, String startTimeString,String endTimeString) {
        Date startTime = null;
        Date endTime = null;
        try {
        startTime = simpleDateFormat.parse(startTimeString);
        endTime =simpleDateFormat.parse(endTimeString);
    } catch (ParseException e) {
        e.printStackTrace();
    }
        List<String> months = getMonths(startTime, endTime);
        for (String date : months) {
            boolean b = result.stream().anyMatch(m -> m.get("month").equals(date));
            if (!b) {
                Map<String,Object> st = Maps.newHashMap();
                st.put("month",date);
                result.add(st);
            }
        }
        result.sort(Comparator.comparing(e->e.get("month").toString()));
    }



    private static List<String> getMonths(Date start, Date end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        List<String> result = new ArrayList<String>();
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(start);
        tempStart.add(Calendar.DATE, 0);

        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(end);
        while (tempStart.before(tempEnd)) {
            result.add(sdf.format(tempStart.getTime()));
            tempStart.add(Calendar.MONTH, +1);
        }
        return result;
    }


    public String downloadFile(String type) throws Exception {
            String stationCode =  (String)redisTemplate.opsForHash().get("t_sys_param:edgeId","content");
            Map<String, String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String path = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS") ? "C:\\robotData\\Model" : mapForPath.get("content") + "/" + stationCode + "/Model";
            Map<String,String> mapForMapPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String mapAbsPath = mapForMapPath.get("content");
            log.info("模型文件路径：" + path);
            switch (type) {
                case "4":
                    //点位模型
                    return createDeviceModel(path, stationCode);
                case "1":
                    //巡视主机模型
                    return createHostModel(path, stationCode);
                case "2":
                    //机器人模型
                    return createRobotModel(path);
                case "3":
                    //摄像机模型
                    return createCameraModel(path);
                case "5":
                    //无人机
                    return createDroneModel(path);
                case "6":
                    //声纹模型
                    return createVoiceModel(path);
                case "7":
                    //任务模型
                    return createTaskModel(path, stationCode);
                case "8":
                    //检修区域模型
                    return createMaintenanceModel(path, stationCode);
                case "9":
                    List<String> mapFileList = sendToUpSystemDao.selectMapPathAll();
                    if (mapFileList.isEmpty()){
                        throw new BusinessException("不存在地图文件");
                    }
                    String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute").get("content"));
                    String filePathMap = Constant.FTP_IMAGE_RELATIVE;
                    List<String> ftpFilePathList = new ArrayList<>(mapFileList.size());
                    for (String mapFilePath : mapFileList) {
                        ftpFilePathList.add(mapFilePath.replace(filePathMap, fileFtpPathMap));
                    }

                    String dirPath = mapAbsPath + "/Model/";
//                    dirPath = "C:/robotData/Model/";
                    String zipName = "map_model.zip";
                    ZipUtil.compressFiles(dirPath, zipName, ftpFilePathList);
                    return dirPath+zipName;
                case "10":
                    if ("1".equals(Constant.edgeLevel())) {
                        //边缘节点 10:设备资源信息配置文件
                        String sourcePath = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sourceFilePath").get("content"));
                        String fileName = "source_file_model.cime";
                        List<String> fileList = new ArrayList<>();
                        fileList.add(sourcePath + fileName);
                        String sourceDirPath = mapAbsPath + "/Model/";
                        String sourceZipName = "source_model.zip";
                        File file = new File(sourcePath + fileName);
                        if (!file.exists()) {
                            throw new BusinessException("设备资源文件不存在");
                        }
                        ZipUtil.compressFiles(sourceDirPath, sourceZipName, fileList);
                        return sourceDirPath + sourceZipName;
                    } else {
                        //10:维护记录文件
                        String sourceDirPath = createMaintenanceModel(path);
                        File file = cn.hutool.core.util.ZipUtil.zip(sourceDirPath);
                        sourceDirPath = file.getPath();
                        return sourceDirPath;
                    }
                case "11":
                    //联动配置文件
                    return createLinkageModel(path);
                case "12":
                    //告警阈值模型
                    return createAlarmThresholdModel(path);
                default:
                    break;
            }

        return "";
    }


    /**
     * 处理一键顺控视频确认反馈信息文件/反向联动信息转发文件
     * @param filePath 文件地址 需从巡视主机下载
     */
    public void dealLinkage(String filePath) {
        log.info("一键顺控视频确认反馈信息文件/反向联动信息转发文件{}", filePath);
        try {
            String fileName = StringUtils.substringAfter(filePath, "/linkage/");
            log.info("fileName {}", fileName);
            String localFilePath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath","content") + "/linkage/" + fileName;
            log.info("localFilePath {}", localFilePath);
            downloadFile(localFilePath, filePath);
            Map<String, List<String>> mapForSend = new HashMap<>(1);
            List<String> list = new ArrayList<>();
            list.add(localFilePath);
            mapForSend.put("list", list);
            Constant.otherServer(mapForSend, Constant.UDP_SEND);
            // 发送到platform告知已完成
            Constant.otherServer(mapForSend, Constant.PLATFORM_SEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询所有在线设备
     * @return
     */
    public List<Map<String, Object>> selectOnlinePatrolDevice(){
        return sendToUpSystemDao.selectOnlinePatrolDevice();
    }
    /**
     * 查询上级系统同步任务的巡检点信息
     * @param list
     * @return
     */
    public List<TCruisePointInstanceNameDetail> selectForTask(List<Long> list){
        return sendToUpSystemDao.selectForTask(list);
    }

    /**
     * 查询上级系统同步任务的巡检点信息
     * @param devicePointId
     * @return
     */
    public List<String> selectForTaskInstanceId(String devicePointId) {
        return sendToUpSystemDao.selectForTaskInstanceId(devicePointId);
    }

    /**
     * 查询上级系统同步任务需转发的区域机器人Code
     * @param list
     * @return
     */
    public List<String> selectForRobotTask(List<Long> list){
        return sendToUpSystemDao.selectRobotCodeForUpperTask(list);
    }

    public List<Long> selectRobotTaskInstanceId(List<Long> list, String robotCode){
        return sendToUpSystemDao.selectRobotTaskInstanceId(list, robotCode);
    }

    /** 查询当前控制设备所属的边缘节点*/
    public String selectEdgeCodeOfRobotOrDrone(String robotCode){
        return sendToUpSystemDao.selectEdgeCodeOfRobotOrDrone(robotCode);
    }

    public Integer getUpperTaskLevel(String level) {
        int taskLevel = 2;
        Object level2 = redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + level, "level");
        if (Objects.nonNull(level2)) {
            taskLevel = Integer.parseInt(String.valueOf(level2));
        }
        return taskLevel;
    }


    public TCruiseTaskAdd buildTaskInfo(Map<String, Object> item, String deviceLevel, Boolean isLingAge) {

        TCruiseTaskAdd tCruiseTaskAdd = covertBean(item, isLingAge, Constant.edgeLevel());
        String deviceList = MapUtils.getString(item, "device_list");
        String instanceIds = convertInstances(NumberUtils.toInt(deviceLevel, 3), deviceList);
        if (StringUtils.isBlank(instanceIds)) {
            throw new BusinessException(ResultCodeEnum.INVALIDREQUEST.getCode(), "点位ID错误");
        }

        tCruiseTaskAdd.setDeviceList(instanceIds);
        item.put("device_list", instanceIds);

        return tCruiseTaskAdd;
    }

    public String convertInstances(int deviceLevel, String deviceList) {
        List<String> instanceIds;
            /*
        middlegroundIds = true
        间隔：t_std_region.up_region_ids
        主设备：t_std_device_attr.pms_id
        部件：t_std_devicemete.component_id
         */
        switch (deviceLevel) {
            case 1:
                // 间隔
                Map<String, String> regionIds = new HashMap<>();
                regionIds.put("upRegionIds", deviceList);
                if (Constant.middlegroundIds()){
                    instanceIds = sendToUpSystemDao.selectInstanceIdsByUpRegionIds(deviceList);
                }else {
                    instanceIds = this.selectInstanceIdsByRegionOrDevice(regionIds);
                }
                break;
            case 2:
                // 主设备
                Map<String, String> deviceIds = new HashMap<>();
                deviceIds.put("deviceIds", deviceList);
                if (Constant.middlegroundIds()){
                    instanceIds = sendToUpSystemDao.selectInstanceIdsByPmsId(deviceList);
                }else {
                    instanceIds = this.selectInstanceIdsByRegionOrDevice(deviceIds);
                }
                break;
            case 3:
                // 设备点位
                // 如果从上级系统下发  点位为机器人的id 对应t_std_devicemete表中的device_point_id 需要转为 巡视系统的instanceId
                if (Constant.standardPoints()) {
                    instanceIds = this.selectForTaskInstanceId(deviceList);
                } else {
                    return deviceList;
                }
                break;
            case 4:
                // 设备部件
                if (Constant.middlegroundIds()){
                    instanceIds = sendToUpSystemDao.selectInstanceIdsByMiddlegroundComponent(deviceList);
                }else {
                    List<String> list = Arrays.asList(deviceList.split(","));
                    Map<String, String> deviceListMap = list.stream().collect(Collectors.toMap(e -> e.split("_")[0],
                        e -> e.split("_")[1], (a, b) -> a + "," + b));
                    List<DeviceModel> deviceModels = new ArrayList<>();
                    deviceListMap.forEach((k, v) -> {
                        DeviceModel dm = new DeviceModel();
                        dm.setDeviceId(k);
                        dm.setComponentId(v);
                        deviceModels.add(dm);
                    });
                    instanceIds = this.selectInstanceIdsByComponent(deviceModels);
                }
                break;
            default:
                return deviceList;
        }

        return StringUtils.join(instanceIds, ",");
    }

    /**
     * @param item
     * @param linkage 是否为联动任务
     * @return
     */
    public TCruiseTaskAdd covertBean(Map<String, Object> item, Boolean linkage, String edgeLevel) {
        TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
        tCruiseTaskAdd.setTaskId(item.get("task_code").toString());
        tCruiseTaskAdd.setTaskName(item.get("task_name").toString());
        tCruiseTaskAdd.setCreateTime(item.containsKey("create_time") ? DateTimeUtil.getDate(item.get("create_time").toString()) : new Date());
        tCruiseTaskAdd.setType(item.containsKey("type") ? Integer.parseInt(item.get("type").toString()) : 1);
        tCruiseTaskAdd.setCycleExecuteTime(item.getOrDefault("cycle_execute_time", "").toString());
        tCruiseTaskAdd.setCycleMonth(item.getOrDefault("cycle_month", "").toString());
        tCruiseTaskAdd.setCycleWeek(item.getOrDefault("cycle_week", "").toString());
        tCruiseTaskAdd.setCycleEndTime(item.getOrDefault("cycle_end_time", "").toString());
        tCruiseTaskAdd.setCycleStartTime(item.getOrDefault("cycle_start_time", "").toString());
        tCruiseTaskAdd.setIntervalExecuteTime(item.getOrDefault("interval_execute_time", "").toString());
        tCruiseTaskAdd.setIntervalNumber(item.getOrDefault("interval_number", "").toString());
        tCruiseTaskAdd.setIntervalType(item.getOrDefault("interval_type", "").toString());
        tCruiseTaskAdd.setIntervalStartTime(item.getOrDefault("interval_start_time", "").toString());
        tCruiseTaskAdd.setIntervalEndTime(item.getOrDefault("interval_end_time", "").toString());
        tCruiseTaskAdd.setIsenable(item.getOrDefault("isenable", "").toString());
        tCruiseTaskAdd.setInvalidStartTime(item.getOrDefault("invalid_start_time", "").toString());
        tCruiseTaskAdd.setInvalidEndTime(item.getOrDefault("invalid_end_time", "").toString());
        tCruiseTaskAdd.setCreator(MapUtils.getString(item, "creator"));

        String priority = item.getOrDefault("priority", "").toString();

        // 周期任务需要处理
        String cycleExecuteTime = tCruiseTaskAdd.getCycleExecuteTime();
        if (StringUtils.isNotEmpty(cycleExecuteTime)) {
            // cycleExecuteTime = cycleExecuteTime.startsWith("0") ? cycleExecuteTime.substring(1, 2) : cycleExecuteTime.substring(0,2);
            tCruiseTaskAdd.setCycleExecuteTime(cycleExecuteTime);
        }
        String cycleWeek = tCruiseTaskAdd.getCycleWeek();
        StringJoiner str = new StringJoiner(",");
        if (StringUtils.isNotEmpty(cycleWeek)) {
            String[] split = cycleWeek.split(",");
            for (int i = 0; i < split.length; i++) {
                split[i] = String.valueOf((NumberUtils.toInt(split[i]) + 1) == 8 ? 1 : (NumberUtils.toInt(split[i]) + 1));
                str.add(split[i]);
            }
            tCruiseTaskAdd.setCycleWeek(str.toString());
        }
        String level;
        //联动任务立即执行
        log.info("linkage:{}", linkage);
        if (linkage) {
            log.info("配置联动任务立即任务");
            level = String.valueOf(this.getUpperTaskLevel("904"));
            tCruiseTaskAdd.setIfRun(173);
            tCruiseTaskAdd.setStartTime(new Date());
        } else {
            level = String.valueOf(this.getUpperTaskLevel("902"));
            if (StringUtils.isNotEmpty(MapUtils.getString(item, "fixed_start_time"))) {
                long fixedStartTime = DateTimeUtil.parse(String.valueOf(item.get("fixed_start_time"))).getTime();
                log.info("fixedStartTime=={},当前时间:{}", fixedStartTime, System.currentTimeMillis());
                if (Math.abs(System.currentTimeMillis() - fixedStartTime) <= (3 * 60 * 1000)) {
                    // 立即(fixed_start_time和当前时间相差3min)
                    tCruiseTaskAdd.setIfRun(173);
                } else {
                    // 定期
                    tCruiseTaskAdd.setIfRun(174);
                }
                tCruiseTaskAdd.setStartTime(DateTimeUtil.getDate(item.get("fixed_start_time").toString()));
            } else {
                if (StringUtils.isNotEmpty(tCruiseTaskAdd.getCycleStartTime())) {
                    tCruiseTaskAdd.setIfRun(172);
                    tCruiseTaskAdd.setStartTime(DateTimeUtil.getDate(tCruiseTaskAdd.getCycleStartTime()));
                } else if (StringUtils.isNotEmpty(tCruiseTaskAdd.getIntervalStartTime())) {
                    tCruiseTaskAdd.setIfRun(172);
                    tCruiseTaskAdd.setStartTime(DateTimeUtil.getDate(tCruiseTaskAdd.getIntervalStartTime()));
                }
            }
        }

        priority = !StringUtils.equalsAny(priority, "1", "2", "3", "4") || "2".equals(edgeLevel) ? level : priority;
        tCruiseTaskAdd.setTaskLevel(NumberUtils.toInt(priority));

        return tCruiseTaskAdd;
    }

    /**
     * 将不能上报的巡视结果先存在redis,等和上一级系统连接之后再上传
     */
    private void saveCruiseResultForMoment(List<Map<String, Object>> items){
        for (Map<String, Object> map : items) {
            String redisKey = "FAILED:" + map.get("task_code") + "+" + map.get("device_id");
            redisTemplate.opsForHash().putAll(redisKey, map);
        }
    }

    /**
     * 将没有成功上报的巡视结果重新上报
     */
    public void failReportCruiseResult(){
        Set<String> failedInfoKeys = redisScan("FAILED:");
        if (CollectionUtils.isEmpty(failedInfoKeys)){
            log.info("FAILED is empty,no fail report cruise result...");
            return;
        }

        for (String key : failedInfoKeys) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
            log.info("redisInfoMap===={}", redisInfoMap);

            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String,Object>> xmlItems = new ArrayList<>();
            Map<String,Object> xmlItem = new HashMap<>();
            xmlBaseModel.setType("61");
            xmlBaseModel.setCommand("1");
            xmlItem.put("patroldevice_name", Optional.ofNullable(redisInfoMap.get("patroldevice_name")).orElse(""));
            xmlItem.put("patroldevice_code", Optional.ofNullable(redisInfoMap.get("patroldevice_name")).orElse(""));
            xmlItem.put("task_name", Optional.ofNullable(redisInfoMap.get("task_name")).orElse(""));
            xmlItem.put("task_code", Optional.ofNullable(redisInfoMap.get("task_code")).orElse(""));
            xmlItem.put("device_name", Optional.ofNullable(redisInfoMap.get("device_name")).orElse(""));
            xmlItem.put("device_id", Optional.ofNullable(redisInfoMap.get("device_id")).orElse(""));
            xmlItem.put("material_id", Optional.ofNullable(redisInfoMap.get("material_id")).orElse(""));
            xmlItem.put("value", Optional.ofNullable(redisInfoMap.get("value")).orElse(""));
            xmlItem.put("value_unit", Optional.ofNullable(redisInfoMap.get("value_unit")).orElse(""));
            xmlItem.put("unit", Optional.ofNullable(redisInfoMap.get("unit")).orElse(""));
            xmlItem.put("time", Optional.ofNullable(redisInfoMap.get("time")).orElse(""));
            xmlItem.put("recognition_type", Optional.ofNullable(redisInfoMap.get("recognition_type")).orElse(""));
            xmlItem.put("file_type", Optional.ofNullable(redisInfoMap.get("file_type")).orElse(""));
            xmlItem.put("file_path", Optional.ofNullable(redisInfoMap.get("file_path")).orElse(""));
            xmlItem.put("rectangle", Optional.ofNullable(redisInfoMap.get("rectangle")).orElse(""));
            xmlItem.put("task_patrolled_id", Optional.ofNullable(redisInfoMap.get("task_patrolled_id")).orElse(""));
            xmlItem.put("data_type", Optional.ofNullable(redisInfoMap.get("data_type")).orElse(""));
            xmlItem.put("valid", Optional.ofNullable(redisInfoMap.get("valid")).orElse(""));

            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String,List<XMLBaseModel>> cruiseResult = new HashMap<>();
            cruiseResult.put("list",list);
            log.info("failure info 上报：{}", cruiseResult);
            sendXML(xmlBaseModel);
        }
    }

    /**
     * Redis数据库批量查询Key值游标
     *
     * @param key redis的key
     * @return Set<String>
     */
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }
            return keys;
        });
    }

    /**
     * 根据定时任务表达式，反推并填充其他时间字段
     *
     * @param map map
     */
    private void fillTaskModelField(Map<String, Object> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }

        String timeStr = "";
        Object timeObj = map.get("time");
        if (timeObj != null) {
            timeStr = timeObj.toString();
        }

        if (StringUtils.isEmpty(timeStr)) {
            return;
        }

        int taskType = getTaskType(timeStr);
        switch (taskType) {
            case 1:
                processIntervalHourTask(map);
                break;
            case 2:
                processIntervalDayTask(map);
                break;
            case 3:
                processCycleTask(map);
                break;
            default:
                break;
        }
//        if (-1 != taskType) {
            map.put("fixed_start_time", "");
//        }
        map.remove("end_time");
        map.remove("time");
    }

    /**
     * 根据任务定时执行的表达式，判断任务类型
     * 表达式有：“8 8 *\/2 * * ?     0 0 0 *\/3 * ?  0 0 5,6 ? 1,2 4,5  等类型
     * 1-间隔时 2-间隔天 3-周期
     * @param timeStr timeStr
     * @return result
     */
    private Integer getTaskType(String timeStr) {
        if (StringUtils.isEmpty(timeStr)) {
            return -1;
        }

        if (timeStr.startsWith("interval_1")) {
            return 1;
        } else if (timeStr.startsWith("interval_2")) {
            return 2;
        } else if (timeStr.contains("?")) {
            return 3;
        } else {
            return -1;
        }
    }

    /**
     * 获取间隔任务执行时间，以小时为单位
     *
     * 格式有：8 8 *\/2 * * ?     0 0 *\/3 * * ?
     *
     * @param timeStr timeStr
     * @return result
     */
    private String getIntervalHour(String timeStr) {
        String[] arr = timeStr.split(" ");
        if (arr == null || arr.length != 6) {
            return "";
        }

        String[] arrSub = arr[2].split("/");
        if (arrSub == null || arrSub.length != 2) {
            return "";
        }

        return arrSub[1];
    }

    /**
     * 获取间隔任务执行时间，以天为单位
     * 格式为：0 0 0 *\/3 * ?
     *
     * @param timeStr timeStr
     * @return result
     */
    private String getIntervalDay(String timeStr) {
        String[] arr = timeStr.split(" ");
        if (arr == null || arr.length != 6) {
            return "";
        }

        String[] arrSub = arr[3].split("/");
        if (arrSub == null || arrSub.length != 2) {
            return "";
        }

        return arrSub[1];
    }


    /**
     * 获取间隔任务间隔数量
     * 格式为：interval_2,2   interval_1,5
     *
     * @param timeStr timeStr
     * @return result
     */
    private String getIntervalDayOrHour(String timeStr) {
        String[] arr = timeStr.split(",");
        return arr[1];
    }

    /**
     * 获取周期任务执行时间
     * 格式为：0 0 5,6 ? 1,2 4,5     0 0 1,3,4,14 ? 1,3,5 2,3,4,5,6,7
     *
     * @param timeStr timeStr
     * @param map map
     */
    private void processCycleDate(String timeStr, Map<String, Object> map) {
        String[] arr = timeStr.split(" ");
        if (arr.length != 6) {
            return;
        }
        String hour = StringUtils.substringBefore(arr[2], ",");
        String executeTime = StringUtils.leftPad(hour, 2, "0") + ":" + StringUtils.leftPad(arr[1], 2, "0") + ":" + StringUtils.leftPad(arr[0], 2, "0");
        map.put("cycle_execute_time", executeTime);
        map.put("cycle_month", "*".equals(arr[4]) ? "1,2,3,4,5,6,7,8,9,10,11,12" : arr[4]);
        map.put("cycle_week", "*".equals(arr[5]) ? "1,2,3,4,5,6,7" : arr[5]);
    }

    /**
     * 根据开始日期时间，获取执行开始时间
     *
     * @param map map
     * @return result
     */
    private String getExecuteTime(Map<String, Object> map) {
        String startTimeObj = MapUtils.getString(map, "fixed_start_time");
        if (StringUtils.isEmpty(startTimeObj)) {
            return "";
        }

        String[] arr = startTimeObj.split(" ");
        if (arr.length != 2) {
            return "";
        }

        return arr[1];
    }

    /**
     * 处理间隔任务 天
     *
     * @param map map
     */
    private void processIntervalDayTask(Map<String, Object> map) {
        String timeStr = MapUtils.getString(map, "time");
        String intervalNumber = getIntervalDayOrHour(timeStr);
        String executeTime = getExecuteTime(map);
        map.put("interval_execute_time", executeTime);
        map.put("interval_number", intervalNumber);
        map.put("interval_type", "2");
        map.put("interval_start_time", map.get("fixed_start_time"));
        map.put("interval_end_time", map.get("end_time"));
    }

    /**
     * 处理间隔任务 小时
     *
     * @param map map
     */
    private void processIntervalHourTask(Map<String, Object> map) {
        String timeStr = map.get("time").toString();
        String intervalNumber = getIntervalDayOrHour(timeStr);
        String executeTime = getExecuteTime(map);
        map.put("interval_execute_time", executeTime);
        map.put("interval_number", intervalNumber);
        map.put("interval_type", "1");
        map.put("interval_start_time", map.get("fixed_start_time"));
        map.put("interval_end_time", map.get("end_time"));
    }

    /**
     * 处理周期任务
     *
     * @param map map
     */
    private void processCycleTask(Map<String, Object> map) {
        String timeStr = MapUtils.getString(map, "time");
        processCycleDate(timeStr, map);
        map.put("cycle_start_time", map.get("fixed_start_time"));
        map.put("cycle_end_time", map.get("end_time"));
    }

    /**
     * 查询点位是否有给机器人发送的
     * @param instanceIds
     * @return
     */
    public String selectIsRobotDevice(List<String> instanceIds) {
        return sendToUpSystemDao.selectIsRobotDevice(instanceIds);
    }
    /**
     * 查询点位是否有下级系统
     * @param instanceIds
     * @return
     */
    public String selectIsDownSystem(List<String> instanceIds) {
        return sendToUpSystemDao.selectIsDownSystem(instanceIds);
    }

    /**
     * ftps 上传
     * @param sourcePath
     * @param targetPathName
     */
    private void uploadFileToUpFtps(String sourcePath, String targetPathName) {
        try {
            if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {
                return;
            }
            Map<String, String> upSystemFtps = redisTemplate.opsForHash().entries("systemConfigKey:upSystem");
            String upSystemFtpsFlag = upSystemFtps.get("upSystemFlag");
            if ("0".equals(upSystemFtpsFlag)){
                log.info("上级系统开关未开! {}",upSystemFtpsFlag);
                return;
            }
            String upSystemFtpsIp = upSystemFtps.get("upSystemFtpsIp");
            String upSystemFtpsPort = upSystemFtps.get("upSystemFtpsPort");
            String upSystemFtpsUsername = upSystemFtps.get("upSystemFtpsUsername");
            String upSystemFtpsPassword = upSystemFtps.get("upSystemFtpsPassword");
            boolean resolveLocal = Boolean.parseBoolean(upSystemFtps.get("upSystemFtpsResolveLocal"));
            FtpsUtil.putFile(sourcePath, targetPathName, upSystemFtpsIp, Integer.parseInt(upSystemFtpsPort),
                    upSystemFtpsUsername, upSystemFtpsPassword, resolveLocal);
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误：", e);
        }
    }

    /**
     * 上级系统 算法交互 ftps 上传
     * @param sourcePath
     * @param targetPathName
     */
    private void uploadFileToUpCloudFtps(String sourcePath, String targetPathName) {
        try {
            if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {
                return;
            }
            Map<String, String> upCloudSystemFtps = redisTemplate.opsForHash().entries("systemConfigKey:upSystemAlgorithm");
            String upCloudSystemFtpsFlag = upCloudSystemFtps.get("upCloudSystemFlag");
            if ("0".equals(upCloudSystemFtpsFlag)){
                log.info("上级系统开关未开! {}",upCloudSystemFtpsFlag);
                return;
            }
            String upCloudSystemFtpsIp = upCloudSystemFtps.get("upCloudSystemFtpsIp");
            String upCloudSystemFtpsPort = upCloudSystemFtps.get("upCloudSystemFtpsPort");
            String upCloudSystemFtpsUsername = upCloudSystemFtps.get("upCloudSystemFtpsUsername");
            String upCloudSystemFtpsPassword = upCloudSystemFtps.get("upCloudSystemFtpsPassword");
            boolean resolveLocal = Boolean.parseBoolean(upCloudSystemFtps.get("upCloudSystemFtpsResolveLocal"));
            FtpsUtil.putFile(sourcePath, targetPathName, upCloudSystemFtpsIp, Integer.parseInt(upCloudSystemFtpsPort),
                    upCloudSystemFtpsUsername, upCloudSystemFtpsPassword, resolveLocal);
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误：", e);
        }
    }

    /**
     * ftps下载
     * @param sourcePath
     * @param targetPathName
     */
    private void downloadFile(String sourcePath, String targetPathName) {
        try {
            if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {
                return;
            }
            Map<String, String> upSystemFtps = redisTemplate.opsForHash().entries("systemConfigKey:upSystem");
            String upSystemFtpsIp = upSystemFtps.get("upSystemFtpsIp");
            String upSystemFtpsPort = upSystemFtps.get("upSystemFtpsPort");
            String upSystemFtpsUsername = upSystemFtps.get("upSystemFtpsUsername");
            String upSystemFtpsPassword = upSystemFtps.get("upSystemFtpsPassword");
            boolean resolveLocal = Boolean.parseBoolean(upSystemFtps.get("upSystemFtpsResolveLocal"));
            FtpsUtil.downloadFile(sourcePath, targetPathName, upSystemFtpsIp, Integer.parseInt(upSystemFtpsPort),
                    upSystemFtpsUsername, upSystemFtpsPassword, resolveLocal);
        } catch (Exception e) {
            log.error("将文件从上级系统ftp服务器下载错误：", e);
        }
    }

    /**
     * 更新TCP连接
     */
    public void updateTcpContent() {
        try {
            //上级系统
            String flag = (String) redisTemplate.opsForHash().get("systemConfigKey:upSystem", "upSystemFlag");
            String ip = (String) redisTemplate.opsForHash().get("systemConfigKey:upSystem", "upSystemIp");
            String port = (String) redisTemplate.opsForHash().get("systemConfigKey:upSystem", "upSystemPort");
            if (Objects.nonNull(flag) && Objects.nonNull(ip) && Objects.nonNull(port)) {
                SocketAddress socketAddress = new InetSocketAddress(ip, Integer.parseInt(port));
                SocketAddress oldSocketAddr = new InetSocketAddress(Constant.upSystemIp(), Constant.upSystemPort());
                int upflag = NumberUtils.toInt(flag);
                if (!Constant.upSystemFlag().equals(flag)) {
                    Constant.upSystemFlag = flag;
                    //flag 改动 由0 -> 1 启动 由1 -> 0 关闭
                    if (UpType.STATE_GRID.getType() == upflag) {
                        this.start(socketAddress, "upSystem");
                    } else if (UpType.TEK.getType() == upflag) {
                        this.stop(oldSocketAddr);
                        urlPathHandler.authToken();
                    } else {
                        this.stop(oldSocketAddr);
                    }
                }
                boolean isChange = !Constant.ZERO.equals(flag) && (!Constant.upSystemIp().equals(ip) || !String.valueOf(Constant.upSystemPort()).equals(port));
                if (isChange) {
                    Constant.upSystemIp = ip;
                    Constant.upSystemPort = Integer.valueOf(port);
                    if (UpType.STATE_GRID.getType() == upflag) {
                        //ip 端口修改 变更连接
                        // 关闭连接时已进行判断是否存在，不在此处判断
                        this.stop(oldSocketAddr);
                        this.start(socketAddress, "upSystem");
                    } else {
                        urlPathHandler.authToken();
                    }
                }
            }
            //算法管理平台
            flag = (String) redisTemplate.opsForHash().get("systemConfigKey:managerSystem", "managerSystemFlag");
            ip = (String) redisTemplate.opsForHash().get("systemConfigKey:managerSystem", "managerSystemIp");
            port = (String) redisTemplate.opsForHash().get("systemConfigKey:managerSystem", "managerSystemPort");
            if (Objects.nonNull(flag) && Objects.nonNull(ip) && Objects.nonNull(port)) {
                SocketAddress managerSystemSocketAddress = new InetSocketAddress(ip, Integer.parseInt(port));
                if (!Constant.managerSystemFlag().equals(flag)) {
                    //flag 改动 由0 -> 1 启动 由1 -> 0 关闭
                    if (Constant.ONE.equals(flag)) {
                        this.start(managerSystemSocketAddress, "managerSystem");
                    } else {
                        this.stop(managerSystemSocketAddress);
                    }
                    Constant.managerSystemFlag = flag;
                }
                boolean isChange = Constant.ONE.equals(flag) && !Constant.managerSystemIp().equals(ip) || !String.valueOf(Constant.managerSystemPort()).equals(port);
                if (isChange) {
                    //ip 端口修改 变更连接
                    if (Objects.nonNull(NettyClient.BOOTSTRAP_MAP.get(managerSystemSocketAddress))) {
                        this.stop(managerSystemSocketAddress);
                        this.start(managerSystemSocketAddress, "managerSystem");
                    } else {
                        this.start(managerSystemSocketAddress, "managerSystem");
                    }
                    Constant.managerSystemIp = ip;
                    Constant.managerSystemPort = Integer.valueOf(port);
                }
            }
        }catch (Exception e){
            log.error("更新TCP连接失败");
        }

    }

    /**
     * 开启连接
     *
     * @param socketAddress
     * @param type 1 上级系统 2算法平台
     */
    private void start(SocketAddress socketAddress, String type){
        if (Objects.nonNull(NettyClient.BOOTSTRAP_MAP.get(socketAddress))){
            log.info("重新连接 {} ", socketAddress);
            ReContentManager.reContent(socketAddress, NettyClient.BOOTSTRAP_MAP.get(socketAddress));
        } else {
            log.info("新建连接 {} ", socketAddress);
            NettyClient nettyClient;
            if ("upSystem".equals(type)) {
                nettyClient = new NettyClient(StateGridADecoder.class, new StateGridAHandlerImpl());
            } else {
                nettyClient = new NettyClient(StateGridADecoder.class, new StateGridAlgorithmHandlerImpl());
            }
            nettyClient.start(socketAddress);
        }
    }

    /**
     * 关闭连接
     *
     * @param socketAddress
     */
    private void stop(SocketAddress socketAddress) {
        log.info("关闭连接 {} ", socketAddress);
        if (Objects.nonNull(NettyClient.BOOTSTRAP_MAP.remove(socketAddress))) {
            TCPClientHandler tcpClientHandler = TCPClientHandler.getTcpClientHandlerHashMap(socketAddress);
            try {
                if (tcpClientHandler != null) {
                    tcpClientHandler.getChannel().close().sync();
                    tcpClientHandler.getChannel().flush();
                    registerManager.terminate(tcpClientHandler.getServer());
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        ReContentManager.terminate(socketAddress);
    }

    public void saveAInterfaceTaskInfo(List<Map<String,Object>> items){
        try{
            List<AInterfaceTaskInfo> list = new ArrayList<>();
            for (Map<String,Object> item: items) {
                AInterfaceTaskInfo aInterfaceTaskInfo = new AInterfaceTaskInfo();
                aInterfaceTaskInfo.setType(ValueUtil.Object2String(item.get("type"),""));
                aInterfaceTaskInfo.setTaskCode(ValueUtil.Object2String(item.get("task_code"),""));
                aInterfaceTaskInfo.setTaskName(ValueUtil.Object2String(item.get("task_name"),""));
                aInterfaceTaskInfo.setPriority(ValueUtil.Object2String(item.get("priority"),""));
                aInterfaceTaskInfo.setDeviceLevel(ValueUtil.Object2String(item.get("device_level"),""));
                aInterfaceTaskInfo.setDeviceList(ValueUtil.Object2String(item.get("device_list"),""));
                aInterfaceTaskInfo.setFixedStartTime(ValueUtil.Object2String(item.get("fixed_start_time"),""));
                aInterfaceTaskInfo.setCycleMonth(ValueUtil.Object2String(item.get("cycle_month"),""));
                aInterfaceTaskInfo.setCycleWeek(ValueUtil.Object2String(item.get("cycle_week"),""));
                aInterfaceTaskInfo.setCycleExecuteTime(ValueUtil.Object2String(item.get("cycle_execute_time"),""));
                aInterfaceTaskInfo.setCycleStartTime(ValueUtil.Object2String(item.get("cycle_start_time"),""));
                aInterfaceTaskInfo.setCycleEndTime(ValueUtil.Object2String(item.get("cycle_end_time"),""));
                aInterfaceTaskInfo.setIntervalNumber(ValueUtil.Object2String(item.get("interval_number"),""));
                aInterfaceTaskInfo.setIntervalType(ValueUtil.Object2String(item.get("interval_type"),""));
                aInterfaceTaskInfo.setIntervalExecuteTime(ValueUtil.Object2String(item.get("interval_execute_time"),""));
                aInterfaceTaskInfo.setIntervalStartTime(ValueUtil.Object2String(item.get("interval_start_time"),""));
                aInterfaceTaskInfo.setIntervalEndTime(ValueUtil.Object2String(item.get("interval_end_time"),""));
                aInterfaceTaskInfo.setInvalidStartTime(ValueUtil.Object2String(item.get("invalid_start_time"),""));
                aInterfaceTaskInfo.setInvalidEndTime(ValueUtil.Object2String(item.get("invalid_end_time"),""));
                aInterfaceTaskInfo.setIsenable(ValueUtil.Object2String(item.get("isenable"),""));
                aInterfaceTaskInfo.setCreator(ValueUtil.Object2String(item.get("creator"),""));
                aInterfaceTaskInfo.setCreateTime(ValueUtil.Object2String(item.get("create_time"),""));
                if (StringUtils.isNotEmpty(aInterfaceTaskInfo.getIsenable()) && !"0".equals(aInterfaceTaskInfo.getIsenable())){
                    //删除操作
                    patrolTaskDao.deleteAInterfaceTask(aInterfaceTaskInfo.getTaskCode());
                } else {
                    list.add(aInterfaceTaskInfo);
                }
            }
            if (!list.isEmpty()){
                patrolTaskDao.batchAddAInterfaceTask(list);
            }
        }catch (Exception e){
            log.info("存储A接口任务出错：{}",items);
        }

    }

    public AInterfaceTaskInfo selectAInterfaceTask(String taskCode) {
        return patrolTaskDao.selectAInterfaceTask(taskCode);
    }

    public List<String> selectInstanceIdsByRegionOrDevice(Map<String, String> idMap) {
        return sendToUpSystemDao.selectInstanceIdsByRegionOrDevice(idMap);
    }

    public List<String> selectInstanceIdsByComponent(List<DeviceModel> deviceModels) {
        return sendToUpSystemDao.selectInstanceIdsByComponent(deviceModels);
    }
}
