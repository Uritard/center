package com.yjh.accesstcp.module.device.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.google.common.collect.Sets;
import com.google.common.collect.Maps;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.CreateModeXMLUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.common.utils.ZipUtil;
import com.yjh.accesstcp.commons.result.BusinessException;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.dao.*;
import com.yjh.accesstcp.module.device.entity.*;
import com.yjh.accesstcp.module.device.utils.FtpsUtil;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.security.NoSuchAlgorithmException;
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

    @Value("${netty.server.port}")
    private int port;
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
    private FtpsUtil ftpsUtil;

    @Autowired
    private SendToUpSystemDao sendToUpSystemDao;

    @Autowired
            private StatisticsDao statisticsDao;


    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String TASK_PRIORITY_REDIS_KEY="task_priority_config:";

    public static final String DAY_FORMAT = ",'%Y-%m-%d')";
    public static final String WEEK_FORMAT = " ,'%u') + 1";
    public static final String MONTH_FORMAT = " ,'%Y-%m')";

    @Transactional(rollbackFor = Exception.class)
    public int sendResponse(long receiveSessionId, String type, String command, String code, List<Map<String, Object>> items, boolean isSend) {

        String sendCode = (String)redisTemplate.opsForHash().get("t_sys_param:edgeCode","content");
        String recvCode = (String)redisTemplate.opsForHash().get("t_sys_param:upSystemReceiveCode","content");
        String stationCode =  (String)redisTemplate.opsForHash().get("t_sys_param:edgeId","content");

        byte[] bytes = new byte[]{};
        long sendSessionId;
        TCPClientHandler tcpClientHandler = TCPClientHandler.getTCPClientHandlerHashMap().get(port);
        if (tcpClientHandler != null) {
            sendSessionId = Constant.sendSessionId.incrementAndGet();
        } else {
            // 刷新sendSessionId
            log.error("服务未连接，请重试，port: {}", port);
            Constant.sendSessionId.set(0L);

            // 把本级没有上报成功的巡视点结果暂存起来,重连服务后再上报上一级系统
            if ("61".equals(type)){
                saveCruiseResultForMoment(items);
            }
            return -1;
        }
        if (code == null || StringUtils.isEmpty(code)) {
            code = stationCode;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(recvCode)
                .setType(type)
                .setCommand(command)
                .setCode(code)
                .setItems(items);
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("发送给上级系统的消息：{}\nsendSessionId:{}, receiveSessionId:{}", xml, sendSessionId, receiveSessionId);
        bytes = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, isSend, xml);
        tcpClientHandler.send(bytes);
        return 1;
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
                    ftpsUtil.putFile(createHostModel(path, stationCode), hostModelTargetPath);
                    map.put("host_file_path", hostModelTargetPath);
                    break;
                case "2":
                    //机器人模型
                    String robotModelTargetPath = String.format(stationCode + "/Model/robot_model.xml");
                    ftpsUtil.putFile(createRobotModel(path), robotModelTargetPath);
                    map.put("robot_file_path", robotModelTargetPath);
                    break;
                case "3":
                    //摄像机模型
                    String videoModelTargetPath = String.format(stationCode + "/Model/video_model.xml");
                    ftpsUtil.putFile(createCameraModel(path), videoModelTargetPath);
                    map.put("video_file_path", videoModelTargetPath);
                    break;
                case "4":
                    //点位模型
                    String deviceModelTargetPath = String.format(stationCode + "/Model/device_model.xml");
                    ftpsUtil.putFile(createDeviceModel(path, stationCode), deviceModelTargetPath);
                    map.put("device_file_path", deviceModelTargetPath);
                    break;
                case "5":
                    //无人机
                    String droneModelTargetPath = String.format(stationCode + "/Model/drone_model.xml");
                    ftpsUtil.putFile(createDroneModel(path), droneModelTargetPath);
                    map.put("drone_file_path", droneModelTargetPath);
                    break;
                case "6":
                    //声纹模型
                    String voiceModelTargetPath = String.format(stationCode + "/Model/voice_model.xml");
                    ftpsUtil.putFile(createVoiceModel(path), voiceModelTargetPath);
                    map.put("voice_file_path", voiceModelTargetPath);
                    break;
                case "7":
                    //任务模型
                    String taskModelTargetPath = String.format(stationCode + "/Model/task_model.xml");
                    ftpsUtil.putFile(createTaskModel(path,stationCode), taskModelTargetPath);
                    map.put("task_file_path", taskModelTargetPath);
                    break;
                case "8":            //检修区域模型
                    String overhaulareaModelTargetPath = String.format(stationCode + "/Model/overhaularea_model.xml");
                    ftpsUtil.putFile(createMaintenanceModel(path, stationCode), overhaulareaModelTargetPath);
                    map.put("overhaularea_file_path", overhaulareaModelTargetPath);
                    break;
                case "9":
                    String mapRealPath = sendToUpSystemDao.selectMapPath();
                    String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute").get("content"));
                    String filePathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative").get("content"));
                    String mapAbsPath = mapRealPath.replace(filePathMap, fileFtpPathMap);
                    String mapModelTargetPath = stationCode + mapRealPath.replace(filePathMap, "");
                    ftpsUtil.putFile(mapAbsPath, mapModelTargetPath);
                    map.put("map_file_path", mapModelTargetPath);
                    break;
                case "10":
                    //设备资源信息配置文件
                    String sourceFilePath = String.format(stationCode + "/Model/source_file_model.cime");
                    String sourceModelMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sourceFilePath").get("content"));
                    String sourceModelPath = sourceModelMap + "source_file_model.cime";
                    ftpsUtil.putFile(sourceModelPath, sourceFilePath);
                    map.put("source_file_path",sourceFilePath);
                    break;
                case "1001":
                    // 区域模型
                    String regionModelTargetPath = String.format(stationCode + "/Model/region_model.xml");
                    ftpsUtil.putFile(createRegionModel(path), regionModelTargetPath);
                    map.put("region_path", regionModelTargetPath);
                    break;
                case "1002":
                    // 录像机模型
                    String recordModelTargetPath = String.format(stationCode + "/Model/record_model.xml");
                    ftpsUtil.putFile(createRecordFile(path), recordModelTargetPath);
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

    @Async
    public void creatFile(String type) {
        try {
            String stationCode =  (String)redisTemplate.opsForHash().get("t_sys_param:edgeId","content");
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            Map<String, String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String path = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS") ? "D:\\testform\\robotTemplate" : mapForPath.get("content") + "/" + stationCode + "/Model";
            list.add(map);

            map.put("time", DateTimeUtil.getDateTimeString());
            map.put("type", type);
            log.info("模型文件路径：" + path);
            switch (type) {
                case "1":
                    //点位模型
                    // map.put("device_file_path",createDeviceModel(path));
                    String deviceModelTargetPath = String.format(stationCode + "/Model/device_model.xml");
                    ftpsUtil.putFile(createDeviceModel(path, stationCode), deviceModelTargetPath);
                    map.put("file_path", deviceModelTargetPath);
                    break;
                case "2":
                    //巡视主机模型
                    String hostModelTargetPath = String.format(stationCode + "/Model/host_model.xml");
                    ftpsUtil.putFile(createHostModel(path, stationCode), hostModelTargetPath);
                    map.put("file_path", hostModelTargetPath);
                    break;
                case "3":
                    //机器人模型
                    String robotModelTargetPath = String.format(stationCode + "/Model/robot_model.xml");
                    ftpsUtil.putFile(createRobotModel(path), robotModelTargetPath);
                    map.put("file_path", robotModelTargetPath);
                    break;
                case "4":
                    //摄像机模型
                    String videoModelTargetPath = String.format(stationCode + "/Model/video_model.xml");
                    ftpsUtil.putFile(createCameraModel(path), videoModelTargetPath);
                    map.put("file_path", videoModelTargetPath);
                    break;
                case "5":
                    //无人机
                    String droneModelTargetPath = String.format(stationCode + "/Model/drone_model.xml");
                    ftpsUtil.putFile(createDroneModel(path), droneModelTargetPath);
                    map.put("file_path", droneModelTargetPath);
                    break;
                case "6":
                    //声纹模型
                    String voiceModelTargetPath = String.format(stationCode + "/Model/voice_model.xml");
                    ftpsUtil.putFile(createVoiceModel(path), voiceModelTargetPath);
                    map.put("file_path", voiceModelTargetPath);
                    break;
                case "7":
                    //任务模型
                    String taskModelTargetPath = String.format(stationCode + "/Model/task_model.xml");
                    ftpsUtil.putFile(createTaskModel(path, stationCode), taskModelTargetPath);
                    map.put("file_path", taskModelTargetPath);
                    break;
                case "8":
                    //检修区域模型
                    String overhaulareaModelTargetPath = String.format(stationCode + "/Model/overhaularea_model.xml");
                    ftpsUtil.putFile(createMaintenanceModel(path, stationCode), overhaulareaModelTargetPath);
                    map.put("file_path", overhaulareaModelTargetPath);
                    break;
                case "9":
                    String mapRealPath = sendToUpSystemDao.selectMapPath();
                    String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath").get("content"));
                    String filePathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative").get("content"));
                    String mapAbsPath = mapRealPath.replace(filePathMap, fileFtpPathMap);
                    String mapModelTargetPath = stationCode + mapRealPath.replace(filePathMap, "");
                    ftpsUtil.putFile(mapAbsPath, mapModelTargetPath);
                    map.put("file_path", mapModelTargetPath);
                    break;
                case "10":
                    //设备资源信息配置文件
                    String sourceFilePath = String.format(stationCode + "/Model/source_file_model.cime");
                    String sourceModelMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sourceFilePath").get("content"));
                    String sourceModelPath = sourceModelMap + "source_file_model.cime";
                    ftpsUtil.putFile(sourceModelPath, sourceFilePath);
                    map.put("file_path",sourceFilePath);
                    break;
                case "1001":
                    // 区域模型
                    String regionModelTargetPath = String.format(stationCode + "/Model/region_model.xml");
                    ftpsUtil.putFile(createRegionModel(path), regionModelTargetPath);
                    map.put("file_path", regionModelTargetPath);
                    break;
                case "1002":
                    // 录像机模型
                    String recordModelTargetPath = String.format(stationCode + "/Model/record_model.xml");
                    ftpsUtil.putFile(createRecordFile(path), recordModelTargetPath);
                    map.put("file_path", recordModelTargetPath);
                    break;
                default:
                    break;
            }
            sendResponse(0L, "11", "", stationCode, list, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String createRecordFile(String path) throws Exception {
        List<TCameraRecorder> tCameraRecorderList = tCameraRecorderDao.selectAll();
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
        return this.sendResponse(0L, xmlBaseModel.getType(), xmlBaseModel.getCommand(), xmlBaseModel.getCode(), xmlBaseModel.getItems(), true);
    }

    public String createDeviceModel(String path, String stationCode) throws Exception {
        List<Map<String,Object>> list = sendToUpSystemDao.selectDeviceModel();
        list.forEach(item->{
            item.put("station_code",stationCode);
            item.put("station_name", getStationName());
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
                item.put("data_type","01");
                jsonObject.put("robot_code", item.get("robot_num"));
                jsonObject.put("robot_pos",item.get("inspection_id"));
            } else if (item.get("cruise_type").equals(229) || item.get("cruise_type").equals(230)){//视屏 红外
                item.put("save_type_list","jpg");
                item.put("data_type","1");
                jsonObject.put("device_code",item.get("camera_id"));
                jsonObject.put("device_pos",item.get("preset_id"));
            }else if (item.get("cruise_type").equals(232)){//声纹
                item.put("save_type_list","wav");
                item.put("data_type","0001");
                jsonObject.put("voice_code",item.get("voice_id"));
                jsonObject.put("voice_pos",item.get("voice_id"));
            }else if (item.get("cruise_type").equals(524)){// 无人机
                item.put("save_type_list","jpg");
                item.put("data_type","001");
                jsonObject.put("uav_code", item.get("robot_num"));
                jsonObject.put("uav_pos",item.get("inspection_id"));
            }
            item.remove("cruise_type");
            item.remove("camera_id");
            item.remove("preset_id");
            item.remove("robot_code");
            item.remove("inspection_id");
            jsonArray.add(jsonObject);
            item.put("video_pos",jsonArray.toJSONString());
            String edgeLevel = (String)redisTemplate.opsForHash().get("t_sys_param:edgeLevel","content");
            if ("1".equals(edgeLevel) && Objects.nonNull(item.get("preset_img")) && Objects.nonNull(item.get("local_path"))){
                item.put("preset_img", "/" + stationCode + item.get("preset_img"));
                String localPath = String.valueOf(item.get("local_path"));
                String targetPath = String.valueOf(item.get("preset_img"));
                try {
                    ftpsUtil.putFile(localPath, targetPath);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
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

        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        List<Map<String, Object>> list = robotModelList.stream().peek(robotModel -> {
            robotModel.setStationCode(stationCode);
            robotModel.setStationName(stationName);
            robotModel.setPhotePath(robotModel.getPhotePath().replace(relativeImgMap.get("content"),absoluteImgMap.get("content")));
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
        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        List<Map<String, Object>> list = cameraModelList.stream().peek(cameraModel -> {
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
        }).map((Function<VoiceDeviceModel, Map<String, Object>>) voiceDeviceModel -> {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(voiceDeviceModel, serializeConfig);
            return jsonObject.toJavaObject(Map.class);
        }).collect(Collectors.toList());
        return CreateModeXMLUtil.createXmlFile(list, path, "voice_model.xml", "PatrolDevice_Model");
    }

    public String createTaskModel(String path, String stationCode) throws Exception {
        //任务模型
        List<Map<String, Object>> list = sendToUpSystemDao.selectTaskInfo();
        //CronExpression expression;
        for (Map<String, Object> item : list) {
            String taskId = item.get("task_code").toString();
            List<Long> instanceIdList = sendToUpSystemDao.selectInstanceId(taskId);
            item.put("station_code", stationCode);
            item.put("station_name", getStationName());
            item.put("device_list", instanceIdList.toString().replaceFirst("\\[", "").replace("]", "").replace(" ", ""));
//            if(!"".equals(item.get("time"))){
//                //计算 todo 内容不完整
//                String time = item.get("time").toString();
//                //SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss");
//                //String s = Pattern.compile("[^0-9]").matcher(str).replaceAll("");
//
//                    item.put("cycle_month", "");
//                    item.put("cycle_week","");
//                    item.put("cycle_execute_time","");
//
//                item.put("cycle_start_time",simpleDateFormat.format(new Date()));
//                item.put("cycle_end_time","2025-01-01 00:00:00");
//                item.put("interval_number","1");
//                item.put("interval_type","2");
//                item.put("interval_execute_time","00:00:00");
//                item.put("interval_start_time",simpleDateFormat.format(new Date()));
//                item.put("interval_end_time","2025-01-01 00:00:00");
//                item.put("station_name",stationName);
//                item.put("station_code",stationCode);
//                item.put("invalid_start_time","");
//                item.put("invalid_end_time","");
//            }
        }
        return CreateModeXMLUtil.createXmlFile(list, path, "task_model.xml", "Task_Model");

    }

    public String createMaintenanceModel(String path, String stationCode) throws Exception {
        //检修区域模型
        List<MaintenanceModel> infoList = sendToUpSystemDao.selectMaintenanceInfo();
        List<Map<String, Object>> finalList = new ArrayList<>();
        infoList.forEach(item -> {
            Map<String, Object> maintenanceMap = new HashMap<>();
            maintenanceMap.put("station_code", stationCode);
            maintenanceMap.put("station_name", getStationName());
            maintenanceMap.put("config_code", item.getConfigCode());
            maintenanceMap.put("enable", "1");
            maintenanceMap.put("start_time", item.getStartTime());
            maintenanceMap.put("end_time", item.getEndTime());
            maintenanceMap.put("device_level", item.getDeviceLevel());
            maintenanceMap.put("device_list", item.getDeviceIds());
            maintenanceMap.put("coordinate_pixel", item.getCoordinatePixel());
            finalList.add(maintenanceMap);
        });
        return CreateModeXMLUtil.createXmlFile(finalList, path, "overhaularea_model.xml", "Effect_Config");


    }

    public String createHostModel(String path, String stationCode) throws Exception {
        //巡视主机模型
        List<Map<String, Object>> finalList = new ArrayList<>();
        Map<String, Object> host = new HashMap<>();
        host.put("station_name", getStationName());
        host.put("station_code", stationCode);
        host.put("patroldevice_name", "巡视主机");
        host.put("patroldevice_code", "YJH-001");
        host.put("device_model", "YJH");
        host.put("manufacturer", "亿嘉和");
        host.put("use_unit", "亿嘉和");
        host.put("device_source", "亿嘉和");
        host.put("production_date", "2020-08-06 12:00:00");
        host.put("production_code", "001");
        host.put("istransport", "");
        host.put("use_mode", "");
        host.put("video_mode", "");
        host.put("place", "");
        host.put("type", "20");
        host.put("patroldevice_info", "");
        host.put("robots_code", "");
        finalList.add(host);
        return CreateModeXMLUtil.createXmlFile(finalList, path, "host_model.xml", "PatrolDevice_Model");
    }

    public String createMapModel() {
        String mapRealPath = sendToUpSystemDao.selectMapPath();
        String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath").get("content"));
        String filePathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative").get("content"));
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

            st.put("command",cmd);
            st.put("type",type);
            st.put("param_year",year);
            st.put("param_month",month);
            st.put("startTime",startTime);
            st.put("endTime",endTime);
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

    public List<Map<String, Object>> countWarnAccuracy(String type, String startTime, String endTime) {
        // 巡视告警准确率
        // 巡视任务闭环率
        List<Map<String, Object>> result = new ArrayList<>();
        if (StringUtils.isBlank(type)) {
            result.add(dealCount(sendToUpSystemDao.countWarnCheck(startTime, endTime)));
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
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            Map<String, String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String path = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS") ? "C:\\robotData\\Model" : mapForPath.get("content") + "/" + stationCode + "/Model";
            list.add(map);
            Map<String,String> mapForMapPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String mapAbsPath = mapForMapPath.get("content");
            map.put("time", DateTimeUtil.getDateTimeString());
            map.put("type", type);
            log.info("模型文件路径：" + path);
            switch (type) {
                case "4":
                    //点位模型
                    // map.put("device_file_path",createDeviceModel(path));
                    String deviceModelTargetPath = String.format(stationCode + "/Model/device_model.xml");
                    return createDeviceModel(path, stationCode);
                case "1":
                    //巡视主机模型
                    String hostModelTargetPath = String.format(stationCode + "/Model/host_model.xml");
                    return createHostModel(path, stationCode);
                case "2":
                    //机器人模型
                    String robotModelTargetPath = String.format(stationCode + "/Model/robot_model.xml");
                    return createRobotModel(path);
                case "3":
                    //摄像机模型
                    String videoModelTargetPath = String.format(stationCode + "/Model/video_model.xml");
                    return createCameraModel(path);
                case "5":
                    //无人机
                    String droneModelTargetPath = String.format(stationCode + "/Model/drone_model.xml");
                    return createDroneModel(path);
                case "6":
                    //声纹模型
                    String voiceModelTargetPath = String.format(stationCode + "/Model/voice_model.xml");
                    return createVoiceModel(path);
                case "7":
                    //任务模型
                    String taskModelTargetPath = String.format(stationCode + "/Model/task_model.xml");
                    return createTaskModel(path, stationCode);
                case "8":
                    //检修区域模型
                    String overhaulareaModelTargetPath = String.format(stationCode + "/Model/overhaularea_model.xml");
                    return createMaintenanceModel(path, stationCode);
                case "9":
                    List<String> mapFileList = sendToUpSystemDao.selectMapPathALl();
                    if (mapFileList.isEmpty()){
                        throw new BusinessException("不存在地图文件");
                    }
                    String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath").get("content"));
                    String filePathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative").get("content"));
                    mapFileList.forEach(mapFilePath ->{
                        mapFilePath.replace(filePathMap, fileFtpPathMap);
                    });
                    String dirPath = mapAbsPath + "/Model/";
//                    dirPath = "C:/robotData/Model/";
                    String zipName = "map_model.zip";
                    ZipUtil.compressFiles(dirPath, zipName, mapFileList);
                    return dirPath+zipName;
                case "10":
                    String sourcePath = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sourceFilePath").get("content"));
                    String fileName = "source_file_model.cime";
                    List<String> fileList = new ArrayList<>();
                    fileList.add(sourcePath+fileName);
                    String sourceDirPath = mapAbsPath + "/Model/";
                    String sourceZipName = "source_model.zip";
                    File file = new File(sourcePath+fileName);
                    if (!file.exists()){
                        throw new BusinessException("设备资源文件不存在");
                    }
                    ZipUtil.compressFiles(sourceDirPath, sourceZipName, fileList);
                    return sourceDirPath+sourceZipName;
                default:
                    break;
            }

        return "";
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

    public Integer getUpperTaskLevel(){
        Integer taskLevel = 2;
        Object level2 = redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY+"902","level");
        if (Objects.nonNull(level2)){
            taskLevel = Integer.valueOf(String.valueOf(level2));
        }

        return taskLevel;
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
}
