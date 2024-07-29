package com.yjh.platform.module.patrol.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.generator.ObjectGenerator;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.algorithm.AlgorithmService;
import com.yjh.platform.common.Constant;
import com.yjh.platform.algorithm.FtpsService;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.Defect;
import com.yjh.platform.common.mqtt.alarmMsgBody.Different;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.dao.TVoiceDeviceDao;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfo;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskDao;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.task.entity.CruiseManualReview;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.service.TAlgorithmInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.yjh.platform.module.patrol.CruiseConstant.*;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_SUMMARY_PREFIX;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * 将各类结果上报其他系统
 *
 * @author 丫C
 * @date 2022/5/31
 */
@Service
public class ProcessResultToUpSystem {

    private final RedisTemplate redisTemplate;
    private final AnalyseDataOperateDao analyseDataOperateDao;
    private final AnalyseDataOperateService analyseDataOperateService;
    private final FtpsService ftpsService;
    private final ApplicationProperties applicationProperties;
    private final AlgorithmService algorithmService;
    private final TRobotInfoDao tRobotInfoDao;
    private final UPatrolTaskDao uPatrolTaskDao;
    private final TAlgorithmInfoService algorithmInfo;
    private final TCameraInfoDao tCameraInfoDao;
    private final TVoiceDeviceDao tVoiceDeviceDao;
    private static final String CCD_PATH = "/CCD/";
    private static final String FIR_PATH = "/FIR/";
    private static final String AUDIO_PATH = "/Audio/";

    private final Logger log = LoggerFactory.getLogger(ProcessResultToUpSystem.class);

    private static final Map<String, List<Map<String, String>>> DEFECT_MAP = new ConcurrentHashMap<>(128);
    private static final Map<String, List<String>> DISTING_MAP = new ConcurrentHashMap<>(128);


    public ProcessResultToUpSystem(RedisTemplate redisTemplate, AnalyseDataOperateDao analyseDataOperateDao,
                                   AnalyseDataOperateService analyseDataOperateService, FtpsService ftpsService,
                                   AlgorithmService algorithmService, ApplicationProperties applicationProperties,
                                   TRobotInfoDao tRobotInfoDao, UPatrolTaskDao uPatrolTaskDao, TAlgorithmInfoService algorithmInfo,
                                   TCameraInfoDao tCameraInfoDao, TVoiceDeviceDao tVoiceDeviceDao) {
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateDao = analyseDataOperateDao;
        this.analyseDataOperateService = analyseDataOperateService;
        this.ftpsService = ftpsService;
        this.algorithmService = algorithmService;
        this.applicationProperties = applicationProperties;
        this.tRobotInfoDao = tRobotInfoDao;
        this.uPatrolTaskDao = uPatrolTaskDao;
        this.algorithmInfo = algorithmInfo;
        this.tCameraInfoDao = tCameraInfoDao;
        this.tVoiceDeviceDao = tVoiceDeviceDao;
    }

    public void alarmAndResultToUpSystem(Map<String, String> cruiseResultMap, String alarmLevel, TWarnInfo tWarnInfo){
        ThreadPoolUtil.PATROL_POOL.addThread(() ->{
            alarmAndResultToUpSystem(Collections.singletonList(cruiseResultMap), alarmLevel, tWarnInfo);
        });
    }

    /**
     * 告警或结果上报上一级系统
     *
     * @param cruiseResultList 巡视结果map
     * @param alarmLevel 告警等级
     * @param tWarnInfo 告警信息
     */
    public XMLBaseModel alarmAndResultToUpSystem(List<Map<String, String>> cruiseResultList, String alarmLevel, TWarnInfo tWarnInfo){
        if (Constant.fastTurbo() || !applicationProperties.getUpSystemFtps().isEnable()) {
            // 压测模式，结果不上报上级系统
            return null;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        List<Map<String, String>> cruiseResultNewList = new ArrayList<>();
        try {
            String edgeCode = SysParamConfig.getSysContent("edgeCode");
            String stationCode = SysParamConfig.getSysContent("edgeId");
            String taskId = Optional.ofNullable(cruiseResultList.get(0).get("taskId")).orElse("");
            UPatrolTask uPatrolTask = StaticContextAccessor.getBean(UPatrolTaskService.class).selectByPrimaryId(taskId);
            String taskCode = uPatrolTask.getTaskCode();
            String key = PATROL_SUMMARY_PREFIX+taskId;
            String taskPatrolledId = String.valueOf(redisTemplate.opsForHash().get(key,"task_patrolled_id"));
            if (CommonUtils.isEmptyOrNullstr(taskPatrolledId)) {
                taskPatrolledId = stationCode + "_" + taskCode + "_" + DateTimeUtil.format3(uPatrolTask.getStartTime());
            }

            Map<Long, TRobotInfo> allRobot = tRobotInfoDao.selectAll();

            for(Map<String, String> cruiseResultMap : cruiseResultList) {
                if (Constant.logUpLv3()) {
                    log.info("cruiseResultMap=={}", cruiseResultMap);
                }
                String resultNum = Optional.ofNullable(cruiseResultMap.get("resultNum")).orElse("");
                // 局放一个点多个结果单独处理
                if (resultNum.contains("局放频次")){
                    String[] split = resultNum.split(",");
                    for (String s : split) {
                        Map<String, String> cruiseResultNewMap = new HashMap<>(cruiseResultMap);
                        String value = "";
                        String valueType = "";
                        String unit = "";
                        if (s.contains("频次")) {
                            value = getNumeric(s);
                            valueType = "11";
                            unit = StringUtils.substringAfter(s, value);
                        } else if (s.contains("峰值")) {
                            value = getNumeric(s);
                            valueType = "12";
                            unit = StringUtils.substringAfter(s, value);
                        } else {
                            value = getNumeric(s);
                            valueType = "13";
                            unit = StringUtils.substringAfter(s, value);
                        }
                        cruiseResultNewMap.put("resultNum", value);
                        cruiseResultNewMap.put("valueType", valueType);
                        cruiseResultNewMap.put("unit", unit);
                        cruiseResultNewList.add(cruiseResultNewMap);
                    }
                    continue;
                }
                Map<String, Object> xmlItem = new HashMap<>(16);
                String instanceId = Optional.ofNullable(cruiseResultMap.get("instanceId")).orElse("");
                String devicePointId = Optional.ofNullable(cruiseResultMap.get("devicePointId")).orElse("");
                String simpleDateFormat = DateTimeUtil.format3(new Date());

                Map<String, String> typeAndPathName = getTypeAndPathName(cruiseResultMap);
                // Map<String, String> patrolDevice = analyseDataOperateDao.selectPatrolDevice(instanceId);
                String patroldeviceCode = patroldeviceCode(cruiseResultMap, allRobot);

                xmlItem.put("patroldevice_code", patroldeviceCode);
                xmlItem.put("patroldevice_name", MapUtils.getString(cruiseResultMap, "cruiseDeviceName"));
                xmlItem.put("task_name", Optional.ofNullable(cruiseResultMap.get("taskName")).orElse(""));
                xmlItem.put("device_name", Optional.ofNullable(cruiseResultMap.get("instanceName")).orElse(""));
                xmlItem.put("device_id", Constant.standardPoints() ? devicePointId : instanceId);
                xmlItem.put("time", CommonUtils.isEmptyOrNullstr(cruiseResultMap.get("cruiseTime")) ? DateTimeUtil.getDateTimeString() : cruiseResultMap.get("cruiseTime"));
                xmlItem.put("file_type", typeAndPathName.getOrDefault("fileType", ""));
                xmlItem.put("recognition_type", typeAndPathName.getOrDefault("recognitionType", ""));
                xmlItem.put("task_code", taskCode);

                xmlItem.put("task_patrolled_id", taskPatrolledId);
                xmlItem.put("unit", cruiseResultMap.getOrDefault("unit", ""));
                // todo 处理单点多结果

                // 文件后缀
                String fileExt = StringUtils.substringAfterLast(cruiseResultMap.get("picpath"), ".");
                fileExt = StringUtils.isEmpty(fileExt) ? "" : "." + fileExt;
                // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR/设备点位ID_编码_时间.jpg
                String tagBasePath = StringUtils.isEmpty(String.valueOf(xmlItem.getOrDefault("file_type", ""))) ? "" : stationCode + "/" + simpleDateFormat.substring(0, 4) + "/" + simpleDateFormat.substring(4, 6) + "/" + simpleDateFormat.substring(6,
                        8) + "/" + taskCode + typeAndPathName.get("fileNamePath") + instanceId + "_" + edgeCode + "_" + simpleDateFormat ;
                String tagPath = StringUtils.isEmpty(String.valueOf(xmlItem.getOrDefault("file_type", ""))) ? "" : stationCode + "/" + simpleDateFormat.substring(0, 4) + "/" + simpleDateFormat.substring(4, 6) + "/" + simpleDateFormat.substring(6,
                        8) + "/" + taskCode + typeAndPathName.get("fileNamePath") + instanceId + "_" + edgeCode + "_" + simpleDateFormat + fileExt;

                Map<String, String> resMap;
                String allFilePath = cruiseResultMap.getOrDefault("allFilePath", "");
                if (Objects.isNull(tWarnInfo)) {
                    xmlBaseModel.setType("61");
                    resMap = packageCruiseResultInfo(taskId, instanceId, cruiseResultMap, xmlItem, tagPath,allFilePath,tagBasePath);
                } else {
                    xmlBaseModel.setType("62");
                    xmlItem.put("link_point", "");
                    String isTemdif = typeAndPathName.getOrDefault("isTemdif", "0");
                    if (StringUtils.isNotEmpty(tagPath)){
                        tagPath = "alarm/" + tagPath;
                    }
                    if (tWarnInfo.getWarnTime() != null){
                        xmlItem.put("time",DateTimeUtil.format(tWarnInfo.getWarnTime()));
                    }
                    resMap = packageAlarmInfo(alarmLevel, tWarnInfo, xmlItem, tagPath, isTemdif, edgeCode,allFilePath,tagBasePath);
                }
                if (Constant.logUpLv3()) {
                    log.info("imgPath==={},tagPath==={}", resMap.get("imgPath"), resMap.get("tagPath"));
                }
                if(StringUtils.isEmpty(allFilePath)){
                    analyseDataOperateService.uploadFileToUpFtps(resMap.get("imgPath"), "/" + resMap.get("tagPath"));
                }

                xmlItems.add(xmlItem);
            }

            if (CollectionUtils.isNotEmpty(cruiseResultNewList)){
                alarmAndResultToUpSystem(cruiseResultNewList, alarmLevel, tWarnInfo);
            }
            xmlBaseModel.setItems(xmlItems);
            if (Constant.isEdge()) {
                xmlBaseModel.setCommand("1");
            }
            if (StringUtils.isBlank(xmlBaseModel.getType())){
                return null;
            }
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            map.put("list", list);
            if (Constant.logUpLv2()) {
                log.info("The {} information to be reported one level up is==={}",
                    StringUtils.equals("61", xmlBaseModel.getType()) ? "cruiseResult" : "alarm", map);
            }
            Constant.otherServer(map, Constant.TCP_URL);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return xmlBaseModel;
    }

    /**
     * 查询巡视点关联的算法类型
     * @param instanceId 巡视点id
     * @return String
     */
    public String getAlgorithmTypeMap(String instanceId) {
        String analyseType = "";
        try {
            if (StringUtils.isEmpty(instanceId)){
                throw new Exception("instanceId is empty,please check with it...");
            }

            // is_ai为on缺陷,is_judge为on判别,algorithm_id非空为表计
            Map<String, Object> algorithmTypeMap = analyseDataOperateDao.selectAlgorithmByInstanceId(NumberUtils.toLong(instanceId));
            log.info("algorithmTypeMap==={}", JSON.toJSONString(algorithmTypeMap));

            // 判断巡视点配置的算法类型 398-缺陷 11判别 1-12表计
            if (Objects.equals("on",  algorithmTypeMap.get("is_ai"))){
                analyseType = "398";
            }else if (Objects.nonNull(algorithmTypeMap.get("algorithm_id"))){
                analyseType = String.valueOf(algorithmTypeMap.get("algorithm_type"));
            }else {
                analyseType = "11";
            }
        }catch (Exception e){
            log.error("判断巡视点配置的算法类型异常：", e);
        }
        return analyseType;
    }

    /**
     * 组装识别类型和文件类型
     *
     * @param cruiseResultMap redis的数据
     * @return Map<String, String>
     */
    private HashMap<String, String> getTypeAndPathName(Map<String, String> cruiseResultMap){
        HashMap<String, String> map = new HashMap<>(4);
        // 识别类型、文件类型、文件名命名
        String recognitionType = cruiseResultMap.getOrDefault("recognitionType", "3");
        String fileType = cruiseResultMap.getOrDefault("fileType", "2");
        String isTemdif = cruiseResultMap.getOrDefault("isTemdif", "0");
        String fileNamePath = CCD_PATH;
        if (StringUtils.equals("4", recognitionType)){
            fileNamePath = FIR_PATH;
        }else if (StringUtils.equals("5", recognitionType)){
            fileNamePath = AUDIO_PATH;
        }
        map.put("recognitionType", recognitionType);
        map.put("fileNamePath", fileNamePath);
        map.put("fileType", fileType);
        map.put("isTemdif", isTemdif);
        return map;
    }

    private String patroldeviceCode(Map<String, ?extends Object> cruiseResultMap, Map<Long, TRobotInfo> allRobot){
        HashMap<String, String> map = new HashMap<>(4);
        // 识别类型、文件类型、文件名命名
        int cruiseType = MapUtils.getInteger(cruiseResultMap, "cruiseType");
        CruiseConstant.TypeEnum type = CruiseConstant.TypeEnum.getEnum(cruiseType);

        String deviceCode = MapUtils.getString(cruiseResultMap, "cruiseDeviceId");
        if (type == CruiseConstant.TypeEnum.ROBOT || type == CruiseConstant.TypeEnum.UAV) {
            deviceCode = Optional.ofNullable(allRobot.get(NumberUtils.toLong(deviceCode))).map(TRobotInfo::getRobotNum).orElse(deviceCode);
        }
        if (type == TypeEnum.INFRARED || type == TypeEnum.VIDEO){
            TCameraInfo cameraInfo = tCameraInfoDao.selectCamera(Long.valueOf(deviceCode));
            deviceCode = cameraInfo.getBcameraChannelId() == null ? (cameraInfo.getCameraChannelId() == null ? deviceCode:cameraInfo.getCameraChannelId()):cameraInfo.getBcameraChannelId();
        }
        if (type == TypeEnum.VOICE){
            Long voiceId = MapUtils.getLong(cruiseResultMap,"cruiseId");
            VoiceDeviceAllInfo voice = tVoiceDeviceDao.selectById(voiceId);
            deviceCode = voice.getVoiceCode() == null ? deviceCode:voice.getVoiceCode();
        }

        return deviceCode;
    }

    /**
     * 组装告警信息
     *
     * @param alarmLevel 告警等级
     * @param tWarnInfo 告警信息
     * @param xmlItem
     * @param tagPath
     * @return Map<String, String>
     */
    private Map<String, String> packageAlarmInfo(String alarmLevel, TWarnInfo tWarnInfo, Map<String, Object> xmlItem,
                                                 String tagPath, String isTemdif, String edgeCode,String allFilePath,String tagBasePath) {
        if (StringUtils.isNotEmpty(allFilePath)){
            String[] allFilePathList = allFilePath.split(",");
            String filePath = "";
            for (String path : allFilePathList){
                String fileExt = StringUtils.substringAfterLast(path, ".");
                fileExt = StringUtils.isEmpty(fileExt) ? "" : "." + fileExt;
                tagPath = tagBasePath + fileExt;
                String picPath = replaceResultImgPath(path, false);
                //文件上报放在这里
                analyseDataOperateService.uploadFileToUpFtps(picPath,tagPath);
                filePath = filePath+","+tagPath;
            }
            tagPath = filePath.replaceFirst(",","");
        }

        Map<String, String> resultPathMap = new HashMap<>(4);
        String imagePath = tWarnInfo.getImagePath();
        log.info("imagePath=={}", imagePath);
        imagePath = replaceResultImgPath(imagePath, false);

        //是否为温差任务标志位 0否 1是
        String zero = "0";
        try {
            String recognitionType = String.valueOf(xmlItem.get("recognition_type"));
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
                    if (zero.equals(isTemdif)) {
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
            xmlItem.put("file_path", StringUtils.contains(imagePath, ".") ? tagPath : "");
            // 1-预警 2-一般 3-严重 4-危急
            xmlItem.put("alarm_level", Optional.ofNullable(alarmLevel).orElse(""));
            xmlItem.put("alarm_type", alarmType);
            xmlItem.put("value", Optional.ofNullable(tWarnInfo.getValue()).orElse(""));
            String resultNum = Optional.ofNullable(tWarnInfo.getValue()).orElse("");
            xmlItem.put("value_unit", resultNum + xmlItem.getOrDefault("unit", ""));
            xmlItem.put("content", Optional.ofNullable(tWarnInfo.getWarnContent()).orElse(""));
            String defectType = Optional.ofNullable(tWarnInfo.getWarnContent()).orElse("");
            StringBuilder sb = new StringBuilder();
            String[] defects = defectType.split(" ");
            for (String defect : defects) {
                String defectDesc = algorithmInfo.getAlgorithmName(defect);
                if (StringUtils.isNotEmpty(defectDesc)){
                    sb.append(",").append(defectDesc);
                }
            }
            defectType  = sb.toString().replaceFirst(",","");
            xmlItem.put("defect_type", defectType);
            xmlItem.put("origin_id", tWarnInfo.getWarnId());
            xmlItem.put("edge_code", edgeCode);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        resultPathMap.put("imgPath", imagePath);
        resultPathMap.put("tagPath", tagPath);
        return resultPathMap;
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

    /**
     * 组装巡视结果信息
     *
     * @param cruiseResultMap
     * @param xmlItem
     * @param tagPath
     * @return Map<String, String>
     */
    private Map<String, String> packageCruiseResultInfo(String taskId, String instanceId, Map<String, String> cruiseResultMap,
                                                        Map<String, Object> xmlItem, String tagPath,String allFilePath,String tagBasePath) {
        if (StringUtils.isNotEmpty(allFilePath)){
            String[] allFilePathList = allFilePath.split(",");
            String filePath = "";
            for (String path : allFilePathList){
                String fileExt = StringUtils.substringAfterLast(path, ".");
                fileExt = StringUtils.isEmpty(fileExt) ? "" : "." + fileExt;
                tagPath = tagBasePath + fileExt;
                String picPath = replaceResultImgPath(path, false);
                //文件上报放在这里
                analyseDataOperateService.uploadFileToUpFtps(picPath,tagPath);
                filePath = filePath+","+tagPath;
            }
            tagPath = filePath.replaceFirst(",","");
        }

        Map<String, String> resultPathMap = new HashMap<>(8);
        String picPath = String.valueOf(redisTemplate.opsForHash().get(PATROL_TASK_PREFIX + taskId + ":" + instanceId, "picpath"));
        log.info("picPath=={}", picPath);
        picPath = replaceResultImgPath(picPath, false);

        try {
            String materialId = analyseDataOperateService.selectMaterialId(NumberUtils.toLong(cruiseResultMap.get("deviceId")));
            String resultNum = Optional.ofNullable(cruiseResultMap.get("resultNum")).orElse("");
            String resultDesc = Optional.ofNullable(cruiseResultMap.get("resultDesc")).orElse("");
            String valueType = Optional.ofNullable(cruiseResultMap.get("valueType")).orElse("0");

            String cruiseType = Optional.ofNullable(cruiseResultMap.get("cruiseType")).orElse("");
            CruiseConstant.TypeEnum cruiseTypeEnum = CruiseConstant.TypeEnum.getEnum(NumberUtils.toInt(cruiseType));
            switch (cruiseTypeEnum){
                case VIDEO:
                case INFRARED:
                    cruiseType = "1";
                    break;
                case ROBOT:
                    cruiseType = "2";
                    break;
                case UAV:
                    cruiseType = "3";
                    break;
                case VOICE:
                    cruiseType = "4";
                    break;
                case ONLINE:
                    cruiseType = "5";
                    break;
                default:
                    break;
            }
            String defectType = resultDesc;
            StringBuilder sb = new StringBuilder();
                String[] defects = defectType.split(" ");
                for (String defect : defects) {
                    String defectDesc = algorithmInfo.getAlgorithmName(defect);
                    if (StringUtils.isNotEmpty(defectDesc)){
                        sb.append(",").append(defectDesc);
                    }
                }
                defectType  = sb.toString().replaceFirst(",","");
            xmlItem.put("defect_type", defectType);
            String resultValue = resultDesc.replace(String.valueOf(xmlItem.getOrDefault("unit", "")), "");
            if (StringUtils.containsAny(resultDesc, "dB", "Hz")) {
                resultValue = resultValue.replaceAll("[dBDbFHz:]", "");
            }
            xmlItem.put("file_path", StringUtils.contains(picPath, ".") ? tagPath : "");
            xmlItem.put("material_id", Optional.ofNullable(materialId).orElse(""));
            xmlItem.put("value", resultValue);
            xmlItem.put("value_unit", resultDesc);
            xmlItem.put("value_type", valueType);
            String fileType = (String)xmlItem.get("file_type");
            String rectangle = "";
            if (StringUtils.containsAny(fileType, "1", "2", "5")) {
                // 坐标随机
                int x1 = RandomUtils.nextInt(200, 500);
                int y1 = RandomUtils.nextInt(160, 340);
                int x2 = RandomUtils.nextInt(x1 + 139, x1 + 541);
                int y2 = RandomUtils.nextInt(y1 + 97, y1 + 453);
                rectangle = x1+","+y1+";" +x2+","+y1+";" +x1+","+y2+";" +x2+","+y2;
                rectangle = StringUtils.isBlank(cruiseResultMap.get("rectangle")) ? rectangle : cruiseResultMap.get("rectangle");
            }
            xmlItem.put("rectangle", rectangle);
            xmlItem.put("data_type", cruiseType);
            String valid = "1";
            if (MapUtils.getIntValue(cruiseResultMap,"cruiseResult") != CRUISE_RESULT_NORMAL) {
                valid = "0";
                if (MapUtils.getIntValue(cruiseResultMap,"cruiseAbnormal") != CRUISE_ABNORMAL_REQUESTFAILED ||
                        MapUtils.getIntValue(cruiseResultMap,"cruiseAbnormal") != CRUISE_ABNORMAL_ANALYSEFAILED) {
                    valid = "2";
                }
            }
            xmlItem.put("valid", valid);
            xmlItem.put("abnormal_type", cruiseResultMap.get("cruiseAbnormal"));
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        resultPathMap.put("imgPath", picPath);
        resultPathMap.put("tagPath", tagPath);
        return resultPathMap;
    }

    /**
     * 缺陷及判别结果上报上一级系统
     *
     * @param cruiseResultMap redis的结果
     * @param resultList 缺陷或判别结果
     * @return void
     */
    @Async
    public void distinguishToUpSystem(Map<String, String> cruiseResultMap, List<String> resultList){
        log.info("cruiseResultMap=={},resultList=={}", cruiseResultMap, resultList);
        try {
            for (String value : resultList){
                // 判别告警等级暂定为一般
                String alarmLevel = "1";
                TWarnInfo tWarnInfo = new TWarnInfo();
                tWarnInfo.setImagePath(cruiseResultMap.get("picpath"));

                // 判别
                tWarnInfo.setValue("图像有差异");
                tWarnInfo.setWarnContent("图像有差异");

                alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, tWarnInfo);
            }
        }catch (Exception e){
            log.error("缺陷及判别结果上报站端异常：" , e);
        }
    }

    public void defectOrDistinguishWarn(Long Id,String warnContent,
                                         Integer warnSubType,Integer level,Map<String, String> cruiseResultMap){
        try {
            TWarnInfo tWarnInfo = new TWarnInfo();
            tWarnInfo.setWarnId(Id);
            tWarnInfo.setImagePath(cruiseResultMap.get("picpath"));
            // 缺陷
            tWarnInfo.setValue(warnContent);
            tWarnInfo.setWarnContent(warnContent);

            tWarnInfo.setWarnSubtype(warnSubType);
            String alarmLevel = "1";
            switch (level) {
                case 130:
                case 131:
                    alarmLevel = "1";
                    break;
                case 132:
                    alarmLevel = "2";
                    break;
                case 133:
                    alarmLevel = "3";
                    break;
                default:
                    break;
            }
            alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, tWarnInfo);
        }catch (Exception e){
            log.info("缺陷告警和判别告警上报上级系统失败：",e);
        }
    }

    @Async
    public void defectToUpSystem(Map<String, String> cruiseResultMap, List<Map<String, String>> resultList){
        log.info("cruiseResultMap=={},resultList=={}", cruiseResultMap, resultList);
        try {
            for (Map<String, String> redisInfoMap : resultList){
                // 判别告警等级暂定为一般
                String alarmLevel = "2";
                TWarnInfo tWarnInfo = new TWarnInfo();
                tWarnInfo.setImagePath(cruiseResultMap.get("picpath"));

                // 缺陷
                tWarnInfo.setValue(Optional.ofNullable(redisInfoMap.get("defectContent")).orElse(""));
                tWarnInfo.setWarnContent(Optional.ofNullable(redisInfoMap.get("defectContent")).orElse(""));

                tWarnInfo.setWarnSubtype(Integer.valueOf(Optional.ofNullable(redisInfoMap.get("defectType")).orElse("450")));
                String defectLevel = redisInfoMap.get("defectLevel");
                switch (defectLevel){
                    case "130":
                    case "131":
                        alarmLevel = "1";
                        break;
                    case "132":
                        alarmLevel = "2";
                        break;
                    case "133":
                        alarmLevel = "3";
                        break;
                    default:
                        break;
                }
                alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, tWarnInfo);
            }
        }catch (Exception e){
            log.error("缺陷及判别结果上报站端异常：" , e);
        }
    }

    /**
     * 缺陷和判别上报算法管理平台
     *
     * @param cruiseResultMap 任务结果
     * @param msgId
     */
    public void defectToAlgorithmM(Map<String, String> cruiseResultMap, String msgId, String analyseType) {
        if (Constant.fastTurbo()) {
            // 压测模式，结果不上报上级系统
            return;
        }

        // msg：判别告警 defect：缺陷告警
        // Set<String> differentList= redisScan( "msg:" + msgId);
        // Set<String> defectList = redisScan("defect:" + msgId);
        String ftpsRemotePath = applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath();

        String nowTime = DateTimeUtil.getDateofFormatString();
        String yearMonth = DateTimeUtil.getMonthDateString();
        boolean normal = true;
        // Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId+":"+ instanceId);
        Long instanceId = MapUtils.getLong(cruiseResultMap, "instanceId");
        HashMap<String, String> nameMap = analyseDataOperateService.selectDeviceNameInfo(instanceId);
        Alarm alarmDetail = new Alarm();
        String picF = nowTime + "_" + nameMap.get("upRegionName") + "_" + nameMap.get("deviceName") + "_" + nameMap.get("meteName") + "_";

        // 先取出算法平台返回的resultinfo中的结果图片路径
        String resultImage;
        String deviceName = Optional.ofNullable(cruiseResultMap.get("deviceName")).orElse("");
        String origPicPath = Optional.ofNullable(cruiseResultMap.get("origpic")).orElse("");
        resultImage = replaceResultImgPath(cruiseResultMap.get("picpath"), false);

        List<String> diffList = DISTING_MAP.remove(msgId);
        if(CollectionUtils.isNotEmpty(diffList) && applicationProperties.getManagerAlgorithmConfig().isEnable()){
            log.info("判别告警类型:开始向算法管理平台发送图片和mqtt消息");
            normal = false;

            String remoteorigfilepath = ftpsRemotePath + "/" +"判别"+"/"+yearMonth+"/"+picF+"原图.jpg";
            //,获取结果路径.并拼接算法管理平台对应远程文件路径
            String remotefilepath=ftpsRemotePath + "/" +"判别"+"/"+yearMonth+"/"+picF+"判别告警.jpg";
            //,获取基准路径.并拼接算法管理平台所需要的基准文件路径
            TCruisePointInstance tCruisePointInstance = analyseDataOperateService.selectPointInstance(instanceId);
            // 获取巡视点位id
            String cruiseId=String.valueOf(tCruisePointInstance.getCruiseid());
            String judgeBaseImagepath= SysParamConfig.getSysContent("presetImgPath");
            //判定基准图路径位presetImgPath+巡视点+巡视点.jpg
            judgeBaseImagepath=judgeBaseImagepath+"/"+cruiseId+"/"+cruiseId+".jpg";
            //拼接算法管理平台分析告警结果图片地址
            String remotebaseimagicpath=ftpsRemotePath + "/" +"判别"+"/"+yearMonth+"/"+picF+"判别基准.jpg";

            List<Different> defectTempList = new ArrayList<>();

            for (String resultValue : diffList) {
                log.info("判别结果：{}",resultValue);
                Different different = new Different();
                String[] re = resultValue.split(",");
                if(re.length > 4){
                    different.setX1((int) NumberUtils.toDouble(re[1]));
                    different.setY1((int) NumberUtils.toDouble(re[2]));
                    different.setX2((int) NumberUtils.toDouble(re[3]));
                    different.setY2((int) NumberUtils.toDouble(re[4]));
                }else {
                    different.setX1((int) NumberUtils.toDouble(resultValue));
                    different.setY1(0);
                    different.setX2(0);
                    different.setY2(0);
                }
                defectTempList.add(different);
                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                alarmDetail.setDevice_name(deviceName);
                alarmDetail.setPoint_name(nameMap.get("meteName"));
                alarmDetail.setTime(DateTimeUtil.format(new Date()));
                // 原图
                alarmDetail.setPic_raw(remoteorigfilepath);
                // 判别基准图
                alarmDetail.setPic_diff_base(remotebaseimagicpath);
                // 判别结果图
                alarmDetail.setPic_different(remotefilepath);
                // 缺陷告警图
                alarmDetail.setPic_defect("");
            }
            alarmDetail.setDifferent(defectTempList);
            // 原始图片上传
            ftpsService.uploadFile("判别告警", origPicPath, remoteorigfilepath);
            // 判别基准图片上传
            ftpsService.uploadFile("判别告警", judgeBaseImagepath, remotebaseimagicpath);
            // 判别结果图片上传
            ftpsService.uploadFile("判别告警", resultImage, remotefilepath);
            log.info("判别与算法主机origpicpath:{}， remoteorigfilepath:{}", origPicPath, remoteorigfilepath);
            log.info("判别与算法主机judgeBaseImagepath:{}， remotebaseimagicpath:{}", judgeBaseImagepath, remotebaseimagicpath);
            log.info("判别与算法主机resultImage:{}， remotefilepath:{}", resultImage, remotefilepath);
            //可靠性 文件是否传输成功
            if( !ftpsService.fileExits(remoteorigfilepath)){
                log.info("原图上传失败，再次上传， origPicPath:{}, remoteorigfilepath:{}", origPicPath, remoteorigfilepath);
                ftpsService.uploadFile("判别告警", origPicPath, remoteorigfilepath);
            }
            if( !ftpsService.fileExits(remotebaseimagicpath)){
                log.info("判别基准图上传失败，再次上传， judgeBaseImagepath:{}, remotebaseimagicpath:{}", judgeBaseImagepath, remotebaseimagicpath);
                ftpsService.uploadFile("判别告警", judgeBaseImagepath, remotebaseimagicpath);
            }
            if( !ftpsService.fileExits(remotefilepath)){
                log.info("判别告警图上传失败，再次上传， resultImage:{}, remotefilepath:{}", resultImage, remotefilepath);
                ftpsService.uploadFile("判别告警", resultImage, remotefilepath);
            }
            algorithmService.pushAlarmMsg(alarmDetail);
            log.info("判别告警发送算法管理平台结束");
        }

//        // 判别上报上一级系统 不在这里上报
//        if (CollectionUtils.isNotEmpty(diffList) && Constant.upSystemFlag()) {
//            distinguishToUpSystem(cruiseResultMap, diffList);
//        }

        List<Map<String, String>> defectList = DEFECT_MAP.remove(msgId);
        if(CollectionUtils.isNotEmpty(defectList) && applicationProperties.getManagerAlgorithmConfig().isEnable()) {
            log.info("缺陷告警类型:开始向算法管理平台发送图片和mqtt消息");
            normal = false;

            //拼接算法管理平台原始图片推送地址
            String remoteorigfilepath = ftpsRemotePath + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "原图.jpg";
            //拼接算法管理平台分析告警结果图片地址
            String remotefilepath = ftpsRemotePath + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "缺陷告警.jpg";

            List<Defect> defectTempList = new ArrayList<>();
            for (Map<String, String> defectMap : defectList) {
                // 获取返回的resultvalue值，这个值就是缺陷和判别的x,y位置信息
                String resultinfo = defectMap.get("value");
                log.info("缺陷结果：{}", resultinfo);

                // 目前格式："wcaqm,1049.0,216.0,1211.0,389.0,0.8829"
                String[] arr1 = resultinfo.split(",");
                Defect defect = new Defect();
                if (arr1.length >= 6) {
                    defect.setX1((int)NumberUtils.toDouble(arr1[1]));
                    defect.setY1((int)NumberUtils.toDouble(arr1[2]));
                    defect.setX2((int)NumberUtils.toDouble(arr1[3]));
                    defect.setY2((int)NumberUtils.toDouble(arr1[4]));

                    int confidence = (int)(NumberUtils.toDouble(arr1[5]) * 100);
                    defect.setConfidence(confidence);
                    // i = i + 6;
                }
                defect.setType(arr1[0]);
                defect.setDesc(
                    defectMap.get("defectContent") + "(坐标位置 " + defect.getX1() + "," + defect.getY1() + "," + defect.getX2() + ","
                        + defect.getY2() + ";" + "置信度 " + defect.getConfidence() + "%)");
                defectTempList.add(defect);

                alarmDetail.setDefect(defectTempList);
                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                alarmDetail.setDevice_name(deviceName);
                alarmDetail.setPoint_name(nameMap.get("meteName"));
                alarmDetail.setTime(DateTimeUtil.format(new Date()));
                // 原图
                alarmDetail.setPic_raw(remoteorigfilepath);
                // 判别基准图
                alarmDetail.setPic_diff_base("");
                // 判别结果图
                alarmDetail.setPic_different("");
                // 缺陷告警图
                alarmDetail.setPic_defect(remotefilepath);
            }
            // 原始图片上传
            ftpsService.uploadFile("缺陷告警", origPicPath, remoteorigfilepath);
            // 缺陷结果图片上传
            ftpsService.uploadFile("缺陷告警", resultImage, remotefilepath);
            if (!ftpsService.fileExits(remoteorigfilepath)) {
                log.info("缺陷原图上传失败，再次上传， origPicPath:{}, remoteorigfilepath:{}", origPicPath, remoteorigfilepath);
                ftpsService.uploadFile("缺陷告警", origPicPath, remoteorigfilepath);
            }
            if (!ftpsService.fileExits(remotefilepath)) {
                log.info("缺陷告警图上传失败，再次上传， resultImage:{}, remotefilepath:{}", resultImage, remotefilepath);
                ftpsService.uploadFile("缺陷告警", resultImage, remotefilepath);
            }

            log.info("缺陷与算法主机：origpicpath:{}，remoteorigfilepath:{}", origPicPath, remoteorigfilepath);
            log.info("缺陷与算法主机：resultImage:{}, remotefilepath:{}", resultImage, remotefilepath);
            algorithmService.pushAlarmMsg(alarmDetail);
            log.info("缺陷告警发送算法管理平台结束");

        }
//        if (CollectionUtils.isNotEmpty(defectList) && Constant.upSystemFlag()) {
//            // 缺陷上报上一级系统  不在这里上报了
//            defectToUpSystem(cruiseResultMap, defectList);
//        }
        //缺陷 如果正常需要进行正常样本上报
        if (normal && "398".equals(analyseType) && applicationProperties.getManagerAlgorithmConfig().isEnable()) {
            String remoteorigfilepath = ftpsRemotePath + "/" + "正常" + "/" + yearMonth + "/" + picF + "原图.jpg";
            alarmDetail.setBay_name(nameMap.get("upRegionName"));
            alarmDetail.setDevice_name(deviceName);
            alarmDetail.setPoint_name(nameMap.get("meteName"));
            alarmDetail.setTime(DateTimeUtil.format(new Date()));
            // 原始图片上传
            ftpsService.uploadFile("正常原图", origPicPath, remoteorigfilepath);
            if( !ftpsService.fileExits(remoteorigfilepath)){
                log.info("原图上传失败，再次上传， origPicPath:{}, remoteorigfilepath:{}", origPicPath, remoteorigfilepath);
                ftpsService.uploadFile("正常原图", origPicPath, remoteorigfilepath);
            }
            // 原图
            alarmDetail.setPic_raw(remoteorigfilepath);
            algorithmService.pushNormalMsg(alarmDetail);
        }
    }

    /**
     * 审核结果上报上级系统
     * 若 remark==null，则表示上级未传审核意见，需主动去数据库查询
     */
    @Async
    public XMLBaseModel reviewToUpSystem(List<CruiseManualReview> cruiseResultList, String remark, boolean all){
        if (!applicationProperties.getUpSystemFtps().isEnable()) {
            return null;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();

        String robotTaskStatusUp =SysParamConfig.getSysContent("robotTaskStatusUp");

        if ("true".equals(robotTaskStatusUp)) {
            try {
                CruiseManualReview cruiseMap = cruiseResultList.get(0);
                if (remark == null) {
                    remark = uPatrolTaskDao.selectForTaskId(cruiseMap.getTaskId()).getRemark();
                }
                String taskPatrolledId = getTaskPatrolledId(cruiseMap.getTaskId());
                if (all) {
                    String ids =
                        cruiseResultList.stream().map(this::getUpDeviceId).filter(StringUtils::isNotEmpty).collect(Collectors.joining(","));
                    Map<String, Object> xmlItem = new HashMap<>(16);

                    xmlItem.put("task_patrolled_id", taskPatrolledId);
                    xmlItem.put("device_id", ids);
                    xmlItem.put("data_update", "");
                    xmlItem.put("manual_review_conclusion", remark);
                    xmlItem.put("confirm_people", cruiseMap.getCheckUser());
                    xmlItem.put("confirm_date", DateUtil.now());
                    xmlItems.add(xmlItem);
                } else {
                    for (CruiseManualReview cruiseResultMap : cruiseResultList) {
                        Map<String, Object> xmlItem = new HashMap<>(16);
                        String instanceId = getUpDeviceId(cruiseResultMap);
                        xmlItem.put("task_patrolled_id", taskPatrolledId);
                        xmlItem.put("device_id", instanceId);
                        xmlItem.put("data_update", cruiseResultMap.getPersonCheck());
                        xmlItem.put("manual_review_conclusion", remark);
                        xmlItem.put("confirm_people", cruiseResultMap.getCheckUser());
                        xmlItem.put("confirm_date", DateTimeUtil.format(cruiseResultMap.getCheckDate()));
                        xmlItems.add(xmlItem);
                    }
                }

                xmlBaseModel.setItems(xmlItems);
                xmlBaseModel.setType("67");
                List<XMLBaseModel> list = new ArrayList<>();
                list.add(xmlBaseModel);
                Map<String, List<XMLBaseModel>> map = new HashMap<>();
                map.put("list", list);
                log.info("The review information to be reported one level up is==={}", map);
                Constant.otherServer(map, Constant.TCP_URL);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return xmlBaseModel;
    }

    @Nullable
    private String getTaskPatrolledId(String taskId) {
        String key = PATROL_SUMMARY_PREFIX + taskId;
        String taskPatrolledId = (String)redisTemplate.opsForHash().get(key, "task_patrolled_id");
        if (CommonUtils.isEmptyOrNullstr(taskPatrolledId)) {
            String stationCode = SysParamConfig.getSysContent("edgeId");
            UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);
            taskPatrolledId = stationCode + "_" + task.getTaskCode() + "_" + DateTimeUtil.format3(task.getStartTime());
        }
        return taskPatrolledId;
    }

    private String getUpDeviceId(CruiseManualReview cruiseResultMap) {
        String taskId = cruiseResultMap.getTaskId();
        String instanceId = Optional.ofNullable(cruiseResultMap.getInstanceId()).map(Object::toString).orElse("");
        if (Constant.standardPoints()) {
            Map<String, String> resultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
            log.info("resultMap=={}", resultMap);
            instanceId = Optional.ofNullable(resultMap.get("devicePointId")).orElse("");
        }
        return instanceId;
    }

    /**
     * 审核巡视结果产生新的告警上报
     * @return
     */
    @Async
    public void reviewCreateAlarmToUpSystem(TWarnInfo warnInfo){
        if (!applicationProperties.getUpSystemFtps().isEnable()) {
            return;
        }

        List<Map<String,String>> tWarnInfoList = analyseDataOperateDao.selectWarnAndResultListByIds(warnInfo.getInstanceId(),warnInfo.getTaskId());

        for (Map<String,String> cruiseResultMap : tWarnInfoList) {
            //构建cruiseResultMap
            cruiseResultMap.put("recognition_type",StringUtils.isNotEmpty(cruiseResultMap.get("meteType")) ?
                    RecognitionTypeEnum.getProRecognize(cruiseResultMap.get("meteType")).getProtocolRecognize() : "2");
            String fileType = getFileType(ValueUtil.toInteger(cruiseResultMap.get("cruiseType"),229),cruiseResultMap.get("meteType"));
            cruiseResultMap.put("fileType",fileType);
            cruiseResultMap.put("isTemdif","0");
            cruiseResultMap.put("taskId",warnInfo.getTaskId());
            cruiseResultMap.put("time",DateTimeUtil.format(warnInfo.getWarnTime()));
            String alarmLevel = "";
            switch (ValueUtil.toInteger(warnInfo.getWarnLevel(),133)) {
                case 130:
                case 131:
                    alarmLevel = "1";
                    break;
                case 132:
                    alarmLevel = "2";
                    break;
                case 133:
                    alarmLevel = "3";
                    break;
                default:
                    break;
            }
            alarmAndResultToUpSystem(Collections.singletonList(Object2Map.toStringMap(cruiseResultMap)),alarmLevel,warnInfo);
        }

    }
    public String getFileType(Integer cruiseType, String meteType) {
        TypeEnum cruiseTypeEnum = TypeEnum.getEnum(cruiseType);
        switch (cruiseTypeEnum){
            case VOICE:
                return "3";
            case INFRARED:
                return "1";
            case VIDEO:
                return "220".equals(meteType) ? "5" : "523".equals(meteType) ? "4" : "2";
            case ROBOT:
            case UAV:
                if ("220".equals(meteType)) {
                    return "5";
                }
                if ("222".equals(meteType)) {
                    return "1";
                }
                if ("223".equals(meteType)) {
                    return "3";
                }
                if ("523".equals(meteType)) {
                    return "4";
                }
                if (StringUtils.equalsAny(meteType, "219", "221", "433")) {
                    return "2";
                }
                return "";
            default:
                return "";
        }
    }

    /**
     * 告警审核上报
     * @param warnIdList
     * @param defect
     * @return
     */
    @Async
    public void reviewAlarmToUpSystem(List<Long> warnIdList ,boolean defect){
        if (!applicationProperties.getUpSystemFtps().isEnable()) {
            return;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        String robotTaskStatusUp =SysParamConfig.getSysContent("robotTaskStatusUp");
        if ("true".equals(robotTaskStatusUp) && CollectionUtils.isNotEmpty(warnIdList)) {
            try {
                if (defect) {
                    List<TDefectInfo> tDefectInfoList = analyseDataOperateDao.selectDefectListByIds(warnIdList);
                    for (TDefectInfo tDefectInfo : tDefectInfoList) {
                        xmlItems.add(getReviewAlarmXml(String.valueOf(tDefectInfo.getTaskId()), String.valueOf(Constant.standardPoints()?tDefectInfo.getDevicePointId():tDefectInfo.getDeviceId()),
                                tDefectInfo.getUserName(), DateTimeUtil.format(tDefectInfo.getDealTime()), tDefectInfo.getDealType() == 286?"1":"2"));
                    }
                } else {
                    List<TWarnInfo> tWarnInfoList = analyseDataOperateDao.selectWarnListByIds(warnIdList);
                    for (TWarnInfo tWarnInfo : tWarnInfoList) {
                        xmlItems.add(getReviewAlarmXml(String.valueOf(tWarnInfo.getTaskId()), String.valueOf(Constant.standardPoints()?tWarnInfo.getDevicePointId():tWarnInfo.getDeviceId()),
                                tWarnInfo.getUserName(), DateTimeUtil.format(tWarnInfo.getDealTime()), tWarnInfo.getDealType() == 286?"1":"2"));
                    }
                }
                xmlBaseModel.setItems(xmlItems);
                xmlBaseModel.setType("64");
                List<XMLBaseModel> list = new ArrayList<>();
                list.add(xmlBaseModel);
                Map<String, List<XMLBaseModel>> map = new HashMap<>(4);
                map.put("list", list);
                log.info("The review warn to be reported one level up is==={}", map);
                Constant.otherServer(map, Constant.TCP_URL);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 告警审核xml整理
     * @return
     */
    public Map<String, Object> getReviewAlarmXml(String taskId, String deviceId, String dealPersonId, String dealTime,
                                                 String isWarn) {

        String taskPatrolledId = getTaskPatrolledId(taskId);
        Map<String, Object> xmlItem = new HashMap<>(8);
        xmlItem.put("task_patrolled_id", taskPatrolledId);
        xmlItem.put("device_id", deviceId);
        xmlItem.put("is_alarm", isWarn);
        xmlItem.put("confirm_people", dealPersonId);
        xmlItem.put("confirm_date", dealTime);
        return xmlItem;
    }

    /**
     * Redis数据库批量查询Key值游标
     * @param key redis的key
     * @return Set<String>
     */
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match(key + "*");
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
     * 图片路径的绝对地址与相对地址的转换
     *
     * @param analyseResultImg 图片路径
     * @param flag true-绝对转相对 false-相对转绝对
     * @return java.lang.String
     */
    public String replaceResultImgPath(String analyseResultImg, boolean flag) {
        if (CommonUtils.isEmptyOrNullstr(analyseResultImg)){
            return "";
        }
        String resultImage = analyseResultImg;
        try {
            String absPath = SysParamConfig.getSysContent("prefixAbsolutePath");
            String relPath = SysParamConfig.getSysContent("prefixRelativePath");
            if (StringUtils.startsWithAny(analyseResultImg, absPath, relPath)) {
                resultImage = flag ? analyseResultImg.replace(absPath, relPath) : analyseResultImg.replace(relPath, absPath);
            } else {
                String filePath = SysParamConfig.getSysContent("fileAbsPath");
                String fileUrl = SysParamConfig.getSysContent("fileRealPath");
                assert fileUrl != null; assert filePath != null;
                resultImage = flag ? analyseResultImg.replace(filePath, fileUrl) : analyseResultImg.replace(fileUrl, filePath);
            }
            if (Constant.logUpLv3()) {
                log.info("The image path after replacement is=={}", resultImage);
            }
        }catch (Exception e){
            log.error("图片路径转换异常", e);
        }
        return resultImage;
    }

    /**
     * 从字符串中获取数字
     * @param str 传入的值
     * @return java.lang.String
     */
    public static String getNumeric(String str){
        str = str.trim();
        String str2 = "";
        if(str != null && !"".equals(str)){
            for(int i = 0; i < str.length(); i++){
                if((str.charAt(i) >= 48 && str.charAt(i) <= 57) || str.charAt(i) == '.'){
                    str2 += str.charAt(i);
                }
            }
        }
        return str2;
    }

    public void addDefect(String msgId, Map<String, String> defect) {
        List<Map<String, String>> list = DEFECT_MAP.computeIfAbsent(msgId, t->new ArrayList<>());
        list.add(defect);
    }

    public void addDisting(String msgId, String disting) {
        List<String> list = DISTING_MAP.computeIfAbsent(msgId, t->new ArrayList<>());
        list.add(disting);
    }

    /**
     * 清理数据，避免内存溢出
     */
    public void clearMsg(String msgId) {
        try {
            DISTING_MAP.remove(msgId);
            DEFECT_MAP.remove(msgId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
