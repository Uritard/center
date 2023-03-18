package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.thread.*;
import com.yjh.platform.module.task.entity.TWarnInfo;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yjh.platform.module.patrol.CruiseConstant.*;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * @author 丫C
 * @date 2022/11/8
 */
@Service
public class PatrolResultHandler {

    private final RedisTemplate redisTemplate;
    private final TRobotInspectionDao tRobotInspectionDao;
    private final AnalyseDataOperateService analyseDataOperateService;
    private final ProcessResultToUpSystem processResultToUpSystem;
    private final UPatrolTaskService uPatrolTaskService;
    private final TVoiceDeviceService tVoiceDeviceService;
    private final TCruisePointInstanceDao tCruisePointInstanceDao;

    private static final String METER = "meter";

    Logger log = LoggerFactory.getLogger(PatrolResultHandler.class);

    public PatrolResultHandler(RedisTemplate redisTemplate, TRobotInspectionDao tRobotInspectionDao, AnalyseDataOperateService analyseDataOperateService, ProcessResultToUpSystem processResultToUpSystem,
        UPatrolTaskService uPatrolTaskService, TVoiceDeviceService tVoiceDeviceService, TCruisePointInstanceDao tCruisePointInstanceDao) {
        this.redisTemplate = redisTemplate;
        this.tRobotInspectionDao = tRobotInspectionDao;
        this.analyseDataOperateService = analyseDataOperateService;
        this.processResultToUpSystem = processResultToUpSystem;
        this.uPatrolTaskService = uPatrolTaskService;
        this.tVoiceDeviceService = tVoiceDeviceService;
        this.tCruisePointInstanceDao = tCruisePointInstanceDao;
    }

    /**
     * 处理下级系统的测点告警
     *
     * @param alarmList 下级系统的测点告警
     */
    public void robotPatrolTaskAlarm(List<RobotPatrolTaskAlarm> alarmList) {
        if (alarmList.isEmpty()) {
            return;
        }
        log.info("robotPatrolTaskAlarm alarmList=={}", alarmList);
        try {
        for (RobotPatrolTaskAlarm taskAlarm : alarmList) {
            // 通过上报的任务id查询本级系统上的任务id
            String taskCode = taskAlarm.getTaskCode();
            // 增加时间判断，避免预先初始化导致数据传入下一个任务
            String timeStr = StringUtils.substringAfterLast(taskAlarm.getTaskPatrolledId(), "_");
            Date date = DateTimeUtil.parseFormat(timeStr, DateTimeUtil.getDateTimePattern3());
            String taskId = tRobotInspectionDao.selectRealTaskId(taskCode, date);
            if (StringUtils.isEmpty(taskId)) {
                taskId = taskCode;
                log.info("taskId is empty, use taskCode as taskId");
            }
            log.info("taskCode==={},taskId===={}", taskCode, taskId);
            taskAlarm.setTaskCode(taskId);

            boolean flag = ArrayUtils.contains(new String[]{"3", "4", "9"}, taskAlarm.getAlarmType());
            if (flag) {
                //非同源告警处理
                NonhomologousWarnThread nonhomologousWarnThread = new NonhomologousWarnThread(taskAlarm,
                    redisTemplate, 0);
                ThreadPoolUtil.PATROL_POOL.addThread(nonhomologousWarnThread);
                return;
            }
            RobotInspectionWarnThread robotWarnThread = new RobotInspectionWarnThread(taskAlarm, redisTemplate,
                    analyseDataOperateService, taskCode);
            ThreadPoolUtil.PATROL_POOL.addThread(robotWarnThread);

        }
        }catch (Exception e){
            log.error("处理下级系统的测点告警异常:", e);
        }
    }

    /**
     * 处理下级系统的巡视结果
     *
     * @param resultList 下级系统的巡视结果
     */
    public void robotPatrolTaskResult(List<RobotPatrolTaskResult> resultList) {
        if (resultList.isEmpty()) {
            return;
        }

        String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"));
        log.info("robotPatrolTaskResult resultList=={}", resultList);

        //上级系统处理逻辑，因缺少attr表数据，需将任务信息放入redis
        if ("3".equals(sysLevel)) {
            for (RobotPatrolTaskResult robotPatrolTaskResult : resultList) {
                if (StringUtils.isNotBlank(robotPatrolTaskResult.getPatrolDeviceCode())) {
                    redisTemplate.opsForValue().set("robotTaskUpInfo:" + robotPatrolTaskResult.getPatrolDeviceCode() ,robotPatrolTaskResult.getTaskCode());
                }
            }
        }


        // 将重复的deviceId挑出来
        List<Map.Entry<String, Long>> entryList = resultList.stream().collect(Collectors.groupingBy(RobotPatrolTaskResult::getDeviceId, Collectors.counting()))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).collect(Collectors.toList());

        HashMap<String, List<RobotPatrolTaskResult>> multipleValuesResultMap = new HashMap<>();
        for (RobotPatrolTaskResult robotPatrolTaskResult : resultList) {
            try {
                Map<String, String> infoMap = new HashMap<>(8);

                // 通过上报的任务id查询本级系统上的任务id
                String taskCode = robotPatrolTaskResult.getTaskCode();
                // 一键顺控文件
                if (StringUtils.equals("1001", robotPatrolTaskResult.getRecognitionType())) {
                    SequenceThread sequenceThread = new SequenceThread(redisTemplate, taskCode, robotPatrolTaskResult.getFilePath());
                    ThreadPoolUtil.PATROL_POOL.addThread(sequenceThread);
                    continue;
                }
                // 增加时间判断，避免预先初始化导致数据传入下一个任务
                String timeStr = StringUtils.substringAfterLast(robotPatrolTaskResult.getTaskPatrolledId(), "_");
                Date date = DateTimeUtil.parseFormat(timeStr, DateTimeUtil.getDateTimePattern3());
                String taskId = tRobotInspectionDao.selectRealTaskId(taskCode, date);
                if (StringUtils.isEmpty(taskId)) {
                    taskId = taskCode;
                    log.info("taskId is empty, use taskCode as taskId");
                }
                log.info("taskCode==={},taskId===={}", taskCode, taskId);
                infoMap.put("taskId", taskId);
                infoMap.put("taskCode", taskCode);


                //device_id转换
                String robotCode = robotPatrolTaskResult.getSendCode();
                String redisKey = "Robot_SPAndIN_Info:" + robotCode + ":" + robotPatrolTaskResult.getDeviceId();
                Map<String,String> robotInfoKeyMap = redisTemplate.opsForHash().entries(redisKey);
                String instanceId = robotInfoKeyMap.get("instanceId");
                // 上级系统没有存储对应值，DeviceId 就是下级的 instanceId
                TCruisePointInstance instance;
                if (StringUtils.isEmpty(instanceId)) {
                    String originId = robotPatrolTaskResult.getDeviceId();
                    if (Constant.standardPoints()) {
                        // 如果上级下发的是 device_point_id，那么用 device_point_id 查询instanceId
                        instance = tRobotInspectionDao.selectRealInstanceByDevicePoint(originId);
                    } else {
                        instance = tRobotInspectionDao.selectRealInstance(originId, robotCode);
                    }
                    log.info("instanceInfo: {}", JSON.toJSONString(instance));
                    instanceId = String.valueOf(instance.getInstanceId());
                } else {
                    instance = tCruisePointInstanceDao.selectByPrimaryId(NumberUtils.toLong(instanceId));
                }
                log.info("taskId===={}, instanceId: {}", taskId, instanceId);

                instance = Optional.ofNullable(instance).orElse(new TCruisePointInstance());
                // 文件处理
                Map<String, String> isAlarmMap = resultFileHandler(robotPatrolTaskResult, infoMap, instance);

                // 除了不带机器人/无人机的边缘节点与节点之间不需要处理告警
                if ("2".equals(sysLevel) && !ArrayUtils.contains(new Integer[]{TypeEnum.ROBOT.getCode(), TypeEnum.UAV.getCode()}, instance.getCruiseType())){
                    log.info("No alarms need to be handled...");
                }else if(!Constant.fastTurbo() && !"3".equals(sysLevel)) {
                    // 告警处理
                    alarmHandlerAfterCruise(robotPatrolTaskResult, taskId, isAlarmMap, instanceId);
                }

                infoMap.put("instanceId", instanceId);

                //机器人是有值的处理非同源
                if ("2".equals(sysLevel) && ArrayUtils.contains(new Integer[]{TypeEnum.ROBOT.getCode(), TypeEnum.UAV.getCode()}, instance.getCruiseType()) && !Constant.fastTurbo()){
                    // 只有巡视主机 非同源告警处理
                    RobotPatrolTaskAlarm taskAlarm = new RobotPatrolTaskAlarm();
                    taskAlarm.setTaskCode(robotPatrolTaskResult.getTaskCode());
                    taskAlarm.setValue(robotPatrolTaskResult.getValue());
                    taskAlarm.setDeviceId(instanceId);
                    NonhomologousWarnThread nonhomologousWarnThread = new NonhomologousWarnThread(taskAlarm, redisTemplate, 1);
                    ThreadPoolUtil.PATROL_POOL.addThread(nonhomologousWarnThread);
                }

                boolean isJFRepeat = false;
                for (Map.Entry<String, Long> entry : entryList) {
                    if (StringUtils.equals(entry.getKey(), robotPatrolTaskResult.getDeviceId())){
                        isJFRepeat = true;
                        break;
                    }
                }

                // 巡视结果处理
                TStdDeviceMete stdDeviceMete = uPatrolTaskService.selectDeviceMeteInfo(Long.valueOf(instanceId));
                if (ArrayUtils.contains(new String[]{"690", "691", "692"}, stdDeviceMete.getMeteType()) && isJFRepeat) {
                    List<RobotPatrolTaskResult> list = multipleValuesResultMap.computeIfAbsent(robotPatrolTaskResult.getDeviceId(), v -> new ArrayList<>());
                    list.add(robotPatrolTaskResult);
                    continue;
                }
                InspectionResultThread cruiseResultDealThread =
                    new InspectionResultThread(robotPatrolTaskResult, infoMap, instance, redisTemplate, true);
                ThreadPoolUtil.PATROL_POOL.addThread(cruiseResultDealThread);

            } catch (Exception e) {
                log.error("处理下级系统的巡视结果异常:", e);
            }
        }

        if (MapUtils.isEmpty(multipleValuesResultMap)){
            return;
        }

        multipleValuesResultMap.keySet().forEach(
                key ->{
                    List<RobotPatrolTaskResult> robotPatrolTaskResults = multipleValuesResultMap.get(key);
                    StringJoiner resultNum = new StringJoiner(",");
                    for (RobotPatrolTaskResult robotPatrolTaskResult : robotPatrolTaskResults){
                        String value = robotPatrolTaskResult.getValue();
                        switch (robotPatrolTaskResult.getValueType()) {
                            case "11":
                                value = "局放频次:" + value;
                                break;
                            case "12":
                                value = "放电峰值:" + value;
                                break;
                            case "13":
                                value = "信号均值:" + value;
                                break;
                            default:
                                break;
                        }
                        resultNum.add(value);
                    }
                    robotPatrolTaskResults.get(0).setValue(resultNum.toString());
                    robotPatrolTaskResult(Collections.singletonList(robotPatrolTaskResults.get(0)));
                }
        );
    }

    /**
     * 根据巡视结果判断是否生成告警
     *
     * @param robotPatrolTaskResult 下级系统的巡视结果
     * @param taskId 任务id
     * @param isAlarmMap 告警信息map
     */
    private void alarmHandlerAfterCruise(RobotPatrolTaskResult robotPatrolTaskResult, String taskId, Map<String, String> isAlarmMap, String instanceId) {
        try {
            isAlarmMap.put("robotCode", robotPatrolTaskResult.getSendCode());
            isAlarmMap.put("taskCode", taskId);
            isAlarmMap.put("instanceId", instanceId);
            isAlarmMap.put("value", robotPatrolTaskResult.getValue());
            isAlarmMap.put("deviceName", robotPatrolTaskResult.getDeviceName());
            isAlarmMap.put("recognitionType", robotPatrolTaskResult.getRecognitionType());
            isAlarmMap.put("fileType", robotPatrolTaskResult.getFileType());
            isAlarmMap.put("time", robotPatrolTaskResult.getTime());
            isAlarmMap.put("taskName", robotPatrolTaskResult.getTaskName());
            IsWarnAfterCruiseThread isWarnAfterCruiseThread = new IsWarnAfterCruiseThread(isAlarmMap, redisTemplate);
            ThreadPoolUtil.PATROL_POOL.addThread(isWarnAfterCruiseThread);
        }catch (Exception e){
            log.error("下级系统的告警处理异常：", e);
        }
    }

    /**
     * 对下级系统的结果文件处理
     *
     * @param robotPatrolTaskResult 下级系统的巡视结果
     * @param infoMap               任务结果其他信息
     * @return Map<String, String>
     */
    private Map<String, String> resultFileHandler(RobotPatrolTaskResult robotPatrolTaskResult, Map<String, String> infoMap, TCruisePointInstance instance) {
        Map<String, String> isAlarmMap = new HashMap<>(16);
        String taskId = infoMap.get("taskId");

        String ftpImageRelative = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageRelative", "content"));
        String ftpImageAbsolute = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute", "content"));
        String ftpsFilePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content"));
        String filePathTemp = new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + taskId;

        try {
            // 文件路径
            String filePath = robotPatrolTaskResult.getFilePath();
            String temporaryFilePath = ftpsFilePath + "/" + filePath;
            log.info("temporaryFilePath==={}", temporaryFilePath);
            String fileName = filePath.trim().substring(filePath.trim().lastIndexOf("/") + 1);

            // 1.红外 2.可见光 3.音频 4.视频
            String fileType = robotPatrolTaskResult.getFileType();
            String developAbsoluteUrl = ftpImageAbsolute + "/" + filePathTemp;
            String developRelativeUrl = ftpImageRelative + "/" + filePathTemp;

            boolean isAlarm = false;
            String descFilePath;
            String descRelativeUrl;
            switch (fileType) {
                case "1":
                    descFilePath = developAbsoluteUrl + "/FIR/" + fileName;
                    descRelativeUrl = developRelativeUrl + "/FIR/" + fileName;
                    isAlarm = true;
                    break;
                case "2":
                case "5":
                    descFilePath = developAbsoluteUrl + "/CCD/" + fileName;
                    descRelativeUrl = developRelativeUrl + "/CCD/" + fileName;
                    isAlarm = true;
                    break;
                case "3":
                    if (instance != null && instance.getCruiseType() == TypeEnum.VOICE.getCode()) {
                        String voicePath = (String)redisTemplate.opsForHash().get("t_sys_param:absVoicePath", "content");
                        String voiceUrl = (String)redisTemplate.opsForHash().get("t_sys_param:relativeVoicePath", "content");
                        String timeAfterTem = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                        String voiceAbsPath = "/" + instance.getCruiseId() + "/1/" + timeAfterTem + "/" + fileName;
                        descFilePath = voicePath + voiceAbsPath;
                        // descFilePath = developAbsoluteUrl + "/Audio/" + fileName;
                        descRelativeUrl = voiceUrl + voiceAbsPath;
                    } else {
                        descFilePath = developAbsoluteUrl + "/Audio/" + fileName;
                        descRelativeUrl = developRelativeUrl + "/Audio/" + fileName;
                    }
                    break;
                case "4":
                    descFilePath = developAbsoluteUrl + "/Video/" + fileName;
                    descRelativeUrl = developRelativeUrl + "/Video/" + fileName;
                    break;
                default:
                    descFilePath = developAbsoluteUrl + "/CCD/" + fileName;
                    descRelativeUrl = developRelativeUrl + "/CCD/" + fileName;
                    break;
            }

            FileUtil.copyFileUsingStream(temporaryFilePath, descFilePath);
            infoMap.put("relativePath", descRelativeUrl);
            infoMap.put("absolutePath", descFilePath);
            if (isAlarm) {
                isAlarmMap.put("relativePath", descRelativeUrl);
                isAlarmMap.put("absolutePath", descFilePath);
            }
        } catch (Exception e) {
            log.error("下级系统的文件处理异常：", e);
        }
        return isAlarmMap;
    }

    /**
     * 处理算法分析后的巡视结果
     *
     * @param resultList 算法分析返回的巡视结果
     */
    public void analysePatrolTaskResult(List<AnalysePatrolTaskResult> resultList) {
        if (resultList.isEmpty()) {
            return;
        }
        log.info("analysePatrolTaskResult resultList=={}", resultList);

        try {
            String taskId = resultList.get(0).getTaskId();
            String instanceId = resultList.get(0).getInstanceId();
            String analyseType = resultList.get(0).getAnalyseType();
            String analyseResultImg = resultList.get(0).getAnalyseResultImg();
            String resultDesc = resultList.get(0).getResultDesc();
            String conf = resultList.get(0).getConf();
            String firDocPath = resultList.get(0).getFirDocPath();

            StringJoiner str = new StringJoiner(",");
            for (AnalysePatrolTaskResult patrolTaskResult : resultList) {
                str.add(patrolTaskResult.getResultValue());
            }
            String resultValue = str.toString();
            log.info("resultValue=={}", resultValue);

            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            log.info("Read from redis cruiseResultMap is：{}", cruiseResultMap);

            TStdDeviceMete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(instanceId));
            log.info("tStdDeviceMete==={}", JSON.toJSONString(tStdDevicemete));

            String msgId = String.valueOf(UUID.randomUUID());
            if (StringUtils.equals("11", analyseType)) {
                log.info("taskId is {} instanceId is {}:distinguish", taskId, instanceId);
                distinguishHandler(msgId, analyseResultImg, resultValue, cruiseResultMap);
            } else if (StringUtils.equals("398", analyseType)) {
                log.info("taskId is {} instanceId is {}:defect", taskId, instanceId);
                defectHandler(msgId, analyseResultImg, resultValue, cruiseResultMap, tStdDevicemete);
            } else {
                log.info("taskId is {} instanceId is {}:recognition", taskId, instanceId);
                recognitionHandler(analyseResultImg, resultValue, cruiseResultMap, tStdDevicemete, firDocPath);
            }

            cruiseResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
            cruiseResultMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
            log.info("cruiseResultMap==={}", cruiseResultMap);
            redisTemplate.opsForHash().putAll(redisKeyName, cruiseResultMap);

            uPatrolTaskService.patrolTaskResultHandler(taskId, Long.valueOf(instanceId));

            // 缺陷和判别上报算法管理平台
            processResultToUpSystem.defectToAlgorithmM(taskId, instanceId, msgId);

        } catch (Exception e) {
            log.error("处理算法分析后的巡视结果异常：", e);
        }
    }

    /**
     * 识别结果处理
     *
     * @param analyseResultImg 巡视结果图
     * @param resultValue      巡视结果值
     * @param cruiseResultMap  redis中巡视点结果信息ma
     * @param tStdDevicemete   测点信息
     * @return String
     */
    private void recognitionHandler(String analyseResultImg, String resultValue,
                                      Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete,
                                      String firDocPath) {
        String resultImage;
        try {
            if(StringUtils.contains(analyseResultImg, METER)){
                resultImage = processResultToUpSystem.replaceResultImgPath(analyseResultImg, true);
                cruiseResultMap.put("picpath", resultImage);
            }

            if (StringUtils.equals("数据错误", resultValue)) {
                cruiseResultMap.put("resultNum", resultValue + ",缺少标定文件");
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL));
            } else if (StringUtils.equals("未获得读数", resultValue)) {
                cruiseResultMap.put("resultNum", resultValue + ",识别失败");
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL));
            } else if (AlgorithmExceptionEnum.isIncludeContent(resultValue)){
                cruiseResultMap.put("resultNum", resultValue);
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL));
            }else{
                normalRecognitionHandler(resultValue, cruiseResultMap, tStdDevicemete, firDocPath);
            }
        } catch (Exception e) {
            log.error("识别结果处理异常：", e);
        }
    }

    public Map<String, String> normalRecognitionHandler(String resultValue, Map<String, String> cruiseResultMap, String firDocPath) {
        TStdDeviceMete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(cruiseResultMap.get("instanceId")));
        return normalRecognitionHandler(resultValue, cruiseResultMap, tStdDevicemete, firDocPath);
    }

    /**
     * 正常识别结果处理
     *
     * @param resultValue     算法分析返回的巡视结果
     * @param cruiseResultMap redis中巡视点结果信息
     * @param tStdDevicemete  测点信息
     * @return Map<String, String>
     */
    private Map<String, String> normalRecognitionHandler(String resultValue, Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete, String firDocPath) {
        try {
            // 红外会返回两个温度(如：12.3,2.3),所以需要这样取值
            String resultStringValue = resultValue.split(",")[0];
            log.info("resultStringValue=={}, resultValue=={}", resultStringValue, resultValue);

            if (resultStringValue.matches("^([0-9]{1,})$|^([0-9]{1,}[.][0-9]*)$|^(-[0-9]{1,})$|^(-[0-9]{1,}[.][0-9]*)$|[\\u4E00-\\u9FA5]+")) {
                // 判断是否为红外识别且获取FIR文件 放入缓存中
                if(StringUtils.isNotEmpty(firDocPath)) {
                    firDocPath = firDocPath.replaceAll(
                            String.valueOf(redisTemplate.opsForHash().get("t_sys_param:infraredStorePath", "content")),
                            String.valueOf(redisTemplate.opsForHash().get("t_sys_param:infraredRealPath", "content")));
                    cruiseResultMap.put("firDocPath", firDocPath);
                    File file = new File(firDocPath);
                    // 放入文件名
                    cruiseResultMap.put("firName", file.getName().substring(0, file.getName().lastIndexOf(".")));
                    log.info("FIR-Doc-----:{}", firDocPath);
                }

                String meteKind = String.valueOf(tStdDevicemete.getMeteKind());
                String stateZero = tStdDevicemete.getStateZero();
                String stateOne = tStdDevicemete.getStateOne();
                Integer alarmState = tStdDevicemete.getAlarmState();
                Float highLimit1 = tStdDevicemete.getHighLimit1();
                Float lowLimit1 = tStdDevicemete.getLowLimit1();
                Float highLimit2 = tStdDevicemete.getHighLimit2();
                Float lowLimit2 = tStdDevicemete.getLowLimit2();
                Float highLimit3 = tStdDevicemete.getHighLimit3();
                Float lowLimit3 = tStdDevicemete.getLowLimit3();
                Float highLimit4 = tStdDevicemete.getHighLimit4();
                Float lowLimit4 = tStdDevicemete.getLowLimit4();
                String meteName = tStdDevicemete.getMeteName();
                int warnFlag = analyseDataOperateService.warnSettings(meteKind, stateZero, alarmState, highLimit1, lowLimit1,
                        highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);

                // 正常结果
                cruiseResultMap.put("resultNum", resultValue);
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                cruiseResultMap.put("cruiseAbnormal", "--");

                if (warnFlag == 1){
                    Map<String, String> warnMap = new HashMap<>(16);
                    String warnName = "warnInfo:" + cruiseResultMap.get("taskId") + String.valueOf(UUID.randomUUID()).replace("-", "");

                    warnMap.put("deviceId", String.valueOf(cruiseResultMap.get("deviceId")));
                    warnMap.put("customId", tStdDevicemete.getCustomId());
                    warnMap.put("instanceId", cruiseResultMap.get("instanceId"));
                    warnMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
                    warnMap.put("taskId", cruiseResultMap.get("taskId"));
                    warnMap.put("value", resultStringValue);
                    warnMap.put("imagePath", cruiseResultMap.get("picpath"));
                    warnMap.put("confMode", "276");
                    warnMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
                    warnMap.put("defectModel", analyseDataOperateService.selectDictCode("defect_model", "其他"));

                    switch (meteKind){
                        case "1":
                            int warnRuleFlag = analyseDataOperateService.warnJudgementTelesignaling(resultValue, stateZero, stateOne, alarmState);
                            if (warnRuleFlag != 1){
                                log.info("Alarm value is not reached(遥信)");
                                break;
                            }
                            log.info("Alarm value is reached(遥信)");
                            warnMap.put("warnLevel", String.valueOf(tStdDevicemete.getAlarmLevel()));
                            warnMap.put("warnName", meteName + "状态异常");
                            warnMap.put("warnTime", DateTimeUtil.format(new Date()));
                            warnMap.put("outRange", "--");
                            if (alarmState == 0) {
                                warnMap.put("warnContent", meteName + ":" + stateZero + "--" + analyseDataOperateService.selectDictNote(String.valueOf(tStdDevicemete.getAlarmLevel()), "alarm_level"));
                            } else {
                                warnMap.put("warnContent", meteName + ":" + stateOne + "--" + analyseDataOperateService.selectDictNote(String.valueOf(tStdDevicemete.getAlarmLevel()), "alarm_level"));
                            }
                            log.info("warnMap=={}", warnMap);
                            redisTemplate.opsForHash().putAll(warnName, warnMap);

                            cruiseResultMap.put("isWarn", "1");
                            cruiseResultMap.put("resultNum", resultStringValue);
                            cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                            cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM));
                            break;
                        case "2":
                            // 如果不是数字 不用判断是否告警了
                            if(!NumberUtils.isCreatable(resultStringValue)){
                                log.info("Data non-numeric===={}", resultStringValue);
                                break;
                            }
                            Float resultValueMeter = NumberUtils.toFloat(resultStringValue);
                            Map<String, String> initInfo = new HashMap<>(16);
                            initInfo.put("valueTemp", String.valueOf(resultValueMeter));

                            String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"));
                            boolean isTemDif = StringUtils.equals("2", sysLevel)
                                    && 1 == tStdDevicemete.getIsTemdif()
                                    && Objects.equals("222", tStdDevicemete.getMeteType());
                            if (isTemDif){
                                // 配置了红外温差任务用差值去判断告警
                                String temperature = String.valueOf(redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("temperature", ""));
                                if (!CommonUtils.isEmptyOrNullstr(temperature)){
                                    double abs = Math.abs(Double.parseDouble(temperature) - Double.parseDouble(resultStringValue));
                                    String valueTemp = new DecimalFormat("#0.00").format(Double.valueOf(abs));
                                    String temperatureTemp = new DecimalFormat("#0.00").format(Double.valueOf(temperature));

                                    initInfo.put("valueTemp", valueTemp);
                                    initInfo.put("temperature", temperatureTemp);
                                    initInfo.put("warnName", meteName + "温差任务");
                                    initInfo.put("warnContent", "传感器环境温度与测温产生温差:环境" + temperatureTemp + "--测温" + resultStringValue + "--温差" + valueTemp);
                                    initInfo.put("outRange", String.valueOf(abs));
                                }
                            }

                            int warnRuleMeter = analyseDataOperateService.warnJudgement(Float.valueOf(initInfo.get("valueTemp")), highLimit1, lowLimit1,
                                    highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
                            log.info("warnRuleMeter=={}", warnRuleMeter);

                            if (warnRuleMeter == 0){
                                log.info("Alarm value is not reached(遥测)");
                                break;
                            }

                            log.info("Alarm value is reached(遥测)");
                            warnMap.put("warnName", isTemDif ? initInfo.get("warnName") : meteName + "数据异常");
                            warnMap.put("warnTime", DateTimeUtil.format(new Date()));

                            String warnLevel = "";
                            String warnContent = "";
                            String outRange = "";

                            switch (warnRuleMeter) {
                                case 1:
                                    warnLevel = analyseDataOperateService.selectDictCode("alarm_level", "预警");
                                    warnContent = isTemDif ?
                                            initInfo.get("warnContent") : meteName + ":" + resultValue + "--" + "预警";
                                    outRange = isTemDif ?
                                            initInfo.get("outRange") : resultValueMeter >= highLimit1 ?
                                            String.valueOf(resultValueMeter - highLimit1) : String.valueOf(lowLimit1 - resultValueMeter);
                                    break;
                                case 2:
                                    warnLevel = analyseDataOperateService.selectDictCode("alarm_level", "一般告警");
                                    warnContent = isTemDif ?
                                            initInfo.get("warnContent") : meteName + ":" + resultValue + "--" + "一般告警";
                                    outRange = isTemDif ?
                                            initInfo.get("outRange") : resultValueMeter >= highLimit2 ?
                                            String.valueOf(resultValueMeter - highLimit2) : String.valueOf(lowLimit2 - resultValueMeter);
                                    break;
                                case 3:
                                    warnLevel = analyseDataOperateService.selectDictCode("alarm_level", "严重告警");
                                    warnContent = isTemDif ?
                                            initInfo.get("warnContent") : meteName + ":" + resultValue + "--" + "严重告警";
                                    outRange = isTemDif ?
                                            initInfo.get("outRange") : resultValueMeter >= highLimit3 ?
                                            String.valueOf(resultValueMeter - highLimit3) : String.valueOf(lowLimit3 - resultValueMeter);
                                    break;
                                case 4:
                                    warnLevel = analyseDataOperateService.selectDictCode("alarm_level", "危急告警");
                                    warnContent = isTemDif ?
                                            initInfo.get("warnContent") : meteName + ":" + resultValue + "--" + "危急告警";
                                    outRange = isTemDif ?
                                            initInfo.get("outRange") : resultValueMeter >= highLimit4 ?
                                            String.valueOf(resultValueMeter - highLimit4) : String.valueOf(lowLimit4 - resultValueMeter);
                                    break;
                                default:
                                    break;
                            }

                            warnMap.put("warnLevel", warnLevel);
                            warnMap.put("warnContent", warnContent);
                            warnMap.put("outRange", outRange);

                            log.info("warnMap=={}", warnMap);
                            // 将告警信息放入redis
                            redisTemplate.opsForHash().putAll(warnName, warnMap);

                            cruiseResultMap.put("isWarn", "1");
                            cruiseResultMap.put("resultDesc", isTemDif ? initInfo.get("valueTemp") : "");
                            cruiseResultMap.put("resultNum", resultValue);
                            cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                            cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM));
                            break;
                        default:
                            break;
                    }

                    Map<String, String> warningMsg = redisTemplate.opsForHash().entries(warnName);
                    log.info("warnMsg==={}", warningMsg);
                    if (!warningMsg.isEmpty()) {
                        TWarnInfo tWarnInfo =  getWarnInfo(warningMsg);
                        analyseDataOperateService.insertWarnInfo(tWarnInfo);

                        // 告警推送
                        Map<String, String> infoMap = new HashMap<>(5);
                        infoMap.put("alarmLevel", String.valueOf(tWarnInfo.getWarnLevel()));
                        infoMap.put("flag", "warn");
                        infoMap.put("defectModel", String.valueOf(tWarnInfo.getDefectModel()));
                        infoMap.put("warnId", String.valueOf(tWarnInfo.getWarnId()));
                        alarmPopUp(tStdDevicemete, infoMap);

                        // newAlarm
                        pushAlarmInfo(warningMsg.get("warnName"), warningMsg.get("warnContent"));

                        // 将产生的告警上送至上一级系统
                        alarmToUpSystem(tWarnInfo, tWarnInfo.getTaskId(), tWarnInfo.getInstanceId());
                    }
                }

                if (!ArrayUtils.contains(new Integer[] {TypeEnum.ROBOT.getCode(), TypeEnum.UAV.getCode()}, MapUtils.getInteger(cruiseResultMap, "cruiseType"))) {
                    RobotPatrolTaskAlarm robotPatrolTaskAlarm = new RobotPatrolTaskAlarm();
                    robotPatrolTaskAlarm.setTaskCode(cruiseResultMap.get("taskId"));
                    robotPatrolTaskAlarm.setValue(resultStringValue);
                    robotPatrolTaskAlarm.setDeviceId(cruiseResultMap.get("instanceId"));
                    NonhomologousWarnThread nonhomologousWarnThread = new NonhomologousWarnThread(robotPatrolTaskAlarm, redisTemplate, 1);
                    ThreadPoolUtil.PATROL_POOL.addThread(nonhomologousWarnThread);
                }
            }else {
                cruiseResultMap.put("resultNum", resultValue);
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL));
            }
        }catch (Exception e){
            log.error("正常识别结果处理异常：", e);
        }
        log.info("cruiseResultMap=={}", cruiseResultMap);
        return cruiseResultMap;
    }

    public Map<String, String> voiceAlarmHandler(String resultValue, Map<String, String> cruiseResultMap) {
        if (!StringUtils.containsAny(resultValue, "DB:", "F:")) {
            log.warn("voice is not analyse, resultValue: {}, cruiseResultMap: {}", resultValue, JSON.toJSONString(cruiseResultMap));
        }
        Long cruiseId = org.apache.commons.collections4.MapUtils.getLongValue(cruiseResultMap, "cruiseId");
        VoiceDeviceAllInfoDetail voiceDevice = tVoiceDeviceService.selectByPrimaryId(cruiseId);

        String[] restVals = resultValue.split(" ");
        Map<String, Integer> voiceMap = new HashMap<>(8);
        for (String ret : restVals) {
            String[] rets = StringUtils.split(ret,":");
            if (rets.length >= 2) {
                voiceMap.put(rets[0], NumberUtils.toInt(rets[1]));
            }
        }

        int warnDbVal = NumberUtils.toInt(voiceDevice.getDbValue());
        int maxDbVal = voiceMap.getOrDefault("DB", 0);
        if (maxDbVal > warnDbVal) {
            voiceResultHandler(String.valueOf(maxDbVal), cruiseResultMap, maxDbVal - warnDbVal, "分贝");
        }

        int warnfVal = NumberUtils.toInt(voiceDevice.getfValue());
        int maxfVal = voiceMap.getOrDefault("F", 0);
        if (maxfVal > warnfVal) {
            voiceResultHandler(String.valueOf(maxfVal), cruiseResultMap, maxfVal - warnfVal, "频率");
        }
        log.info("voice point info, maxDbVal: {}, warnDbVal: {}, maxfVal: {}, warnfVal: {}, restVals: {}", maxDbVal, warnDbVal, maxfVal, warnfVal, JSON.toJSONString(voiceMap));
        return cruiseResultMap;
    }

    public Map<String, String> voiceResultHandler(String resultValue, Map<String, String> cruiseResultMap, int outRang, String alarmPrefix) {
        try {

            TStdDeviceMete tStdDevicemete =
                analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(cruiseResultMap.get("instanceId")));

            String meteName = tStdDevicemete.getMeteName();

            Map<String, String> warnMap = new HashMap<>();
            String warnName = "warnInfo:" + cruiseResultMap.get("taskId") + String.valueOf(UUID.randomUUID()).replace("-", "");

            warnMap.put("deviceId", String.valueOf(cruiseResultMap.get("deviceId")));
            warnMap.put("customId", tStdDevicemete.getCustomId());
            warnMap.put("instanceId", cruiseResultMap.get("instanceId"));
            warnMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
            warnMap.put("taskId", cruiseResultMap.get("taskId"));
            warnMap.put("value", resultValue);
            warnMap.put("imagePath", cruiseResultMap.get("picpath"));
            warnMap.put("voicePath", cruiseResultMap.get("voicePath"));
            warnMap.put("confMode", "276");
            warnMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
            warnMap.put("defectModel", analyseDataOperateService.selectDictCode("defect_model", "其他"));

            log.info("Alarm value is reached(遥测)");
            warnMap.put("warnName", meteName + alarmPrefix + "数据异常");
            warnMap.put("warnTime", DateTimeUtil.format(new Date()));

            warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
            warnMap.put("warnContent", meteName + alarmPrefix + ":" + resultValue + "--" + "一般告警");
            warnMap.put("outRange", String.valueOf(outRang));

            log.info("warnMap=={}", warnMap);
            redisTemplate.opsForHash().putAll(warnName, warnMap);

            // 巡视结果告警处理
            cruiseResultMap.put("isWarn", "1");
            cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
            cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM));

            // 告警入库
            TWarnInfo tWarnInfo = getWarnInfo(warnMap);
            analyseDataOperateService.insertWarnInfo(tWarnInfo);

            Map<String, String> infoMap = new HashMap<>(5);
            infoMap.put("alarmLevel", String.valueOf(tWarnInfo.getWarnLevel()));
            infoMap.put("flag", "warn");
            infoMap.put("defectModel", String.valueOf(tWarnInfo.getDefectModel()));
            infoMap.put("warnId", String.valueOf(tWarnInfo.getWarnId()));
            alarmPopUp(tStdDevicemete, infoMap);

            // newAlarm
            pushAlarmInfo(warnMap.get("warnName"), warnMap.get("warnContent"));

            // 告警上报上一级系统
            alarmToUpSystem(tWarnInfo, tWarnInfo.getTaskId(), tWarnInfo.getInstanceId());

        } catch (Exception e) {
            log.error("声纹告警处理结果异常：", e);
        }
        log.info("cruiseResultMap=={}", cruiseResultMap);
        return cruiseResultMap;
    }

    /**
     * 将产生的告警上送至上级系统
     *
     * @param warnInfo 告警信息
     * @param taskId 任务id
     * @param instanceId 巡视点id
     */
    public void alarmToUpSystem(TWarnInfo warnInfo, String taskId, Long instanceId){
        try{
            String alarmLevel = "";
            switch (warnInfo.getWarnLevel()){
                case 130:
                    alarmLevel = "1";
                    break;
                case 131:
                    alarmLevel = "2";
                    break;
                case 132:
                    alarmLevel = "3";
                    break;
                case 133:
                    alarmLevel = "4";
                    break;
                default:
                    break;
            }

            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            processResultToUpSystem.alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, warnInfo);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 告警info信息组装
     *
     * @param warningMsg 缺陷map信息
     * @return TWarnInfo
     */
    private TWarnInfo getWarnInfo(Map<String, String> warningMsg) {
        TWarnInfo tWarnInfo = new TWarnInfo();
        try {
            tWarnInfo.setWarnLevel(NumberUtils.toInt(warningMsg.get("warnLevel")));
            tWarnInfo.setDeviceId(NumberUtils.toLong(warningMsg.get("deviceId")));
            tWarnInfo.setCunstomId(warningMsg.get("customId"));
            tWarnInfo.setInstanceId(NumberUtils.toLong(warningMsg.get("instanceId")));
            tWarnInfo.setStdMeteId(NumberUtils.toLong(warningMsg.get("stdMeteId")));
            tWarnInfo.setTaskId(warningMsg.get("taskId"));
            tWarnInfo.setValue(warningMsg.get("value"));
            tWarnInfo.setConfMode(NumberUtils.toInt(warningMsg.get("confMode")));
            tWarnInfo.setImagePath(warningMsg.get("imagePath"));
            tWarnInfo.setAlarmSource(NumberUtils.toInt(warningMsg.get("alarmSource")));
            tWarnInfo.setWarnName(warningMsg.get("warnName"));
            tWarnInfo.setOutRange(warningMsg.get("outRange"));
            tWarnInfo.setWarnContent(warningMsg.get("warnContent"));
            tWarnInfo.setDefectModel(NumberUtils.toInt(warningMsg.get("defectModel")));
            tWarnInfo.setWarnTime(DateTimeUtil.parse(warningMsg.get("warnTime")));
        }catch (Exception e){
            log.error("告警info信息组装异常:",e);
        }
        return tWarnInfo;
    }

    /**
     * 缺陷结果处理
     *
     * @param msgID            随机数
     * @param analyseResultImg 巡视结果图
     * @param resultValueItem  巡视结果值
     * @param cruiseResultMap  redis中巡视点结果信息
     * @param tStdDevicemete   测点信息
     */
    private void defectHandler(String msgID, String analyseResultImg, String resultValueItem, Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete)  {
        String resultImage;
        try {
            resultImage = processResultToUpSystem.replaceResultImgPath(analyseResultImg, true);
            log.info("Defect data is ==={}", resultValueItem);
            String resultValue = analyseDataOperateService.resolveDefectResult(resultValueItem);
            log.info("Parse defect data is ==={}", resultValue);

            cruiseResultMap.put("resultNum", resultValue);
            cruiseResultMap.put("picpath", resultImage);
            if (!"null".equals(resultValue) && !(resultValue.contains("device"))) {
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DEFECT));

                String[] resultArr = resultValue.split("\\s+");
                if (resultArr.length == 1) {
                    log.info("Only one defect is generated！！！");
                    // 缺陷信息存redis
                    String redisKeyTemp = String.valueOf(UUID.randomUUID()).replace("-", "");
                    Map<String, String> defectMap = getDefectMap(resultImage, resultValueItem, cruiseResultMap, tStdDevicemete, resultValue);
                    cruiseResultMap.put("isWarn", "1");
                    log.info("defectMap=={}", JSON.toJSONString(defectMap));
                    redisTemplate.opsForHash().putAll("defectInfo:" + cruiseResultMap.get("taskId") + ":" + redisKeyTemp, defectMap);
                    // 为上报到算法管理平台暂存数据
                    redisTemplate.opsForHash().putAll("defect:" + msgID + ":" + redisKeyTemp, defectMap);

                    TDefectInfo tDefectInfo = getDefectInfo(defectMap);
                    log.info("tDefectInfo=={}", JSON.toJSONString(tDefectInfo));
                    analyseDataOperateService.insertDefectInfo(tDefectInfo);

                    // 缺陷告警推送
                    Map<String, String> infoMap = new HashMap<>(5);
                    infoMap.put("alarmLevel", defectMap.get("defectLevel"));
                    infoMap.put("flag", "defect");
                    infoMap.put("defectModel", defectMap.get("defectType"));
                    infoMap.put("warnId", String.valueOf(tDefectInfo.getDefectId()));
                    alarmPopUp(tStdDevicemete, infoMap);

                    // newAlarm
                    pushAlarmInfo(resultValue, tStdDevicemete.getMeteName() + "--" + resultValue);

                } else if (resultArr.length > 1) {
                    log.info("Multiple defects are generated！！！");
                    String defectNames = "";
                    for (String res : resultArr) {
                        // 缺陷信息存redis
                        String redisKeyTemp = String.valueOf(UUID.randomUUID()).replace("-", "");
                        Map<String, String> defectMap = getDefectMap(resultImage, resultValueItem, cruiseResultMap, tStdDevicemete, res);
                        cruiseResultMap.put("isWarn", "1");
                        log.info("defectMap=={}", JSON.toJSONString(defectMap));
                        redisTemplate.opsForHash().putAll("defectInfo:" + cruiseResultMap.get("taskId") + ":" + redisKeyTemp, defectMap);
                        // 为上报到算法管理平台暂存数据
                        redisTemplate.opsForHash().putAll("defect:" + msgID + ":" + redisKeyTemp, defectMap);

                        TDefectInfo tDefectInfo = getDefectInfo(defectMap);
                        log.info("tDefectInfo=={}", JSON.toJSONString(tDefectInfo));
                        analyseDataOperateService.insertDefectInfo(tDefectInfo);

                        // 缺陷告警推送
                        Map<String, String> infoMap = new HashMap<>(5);
                        infoMap.put("alarmLevel", defectMap.get("defectLevel"));
                        infoMap.put("flag", "defect");
                        infoMap.put("defectModel", defectMap.get("defectType"));
                        infoMap.put("warnId", String.valueOf(tDefectInfo.getDefectId()));
                        alarmPopUp(tStdDevicemete, infoMap);

                        defectNames = defectNames + res + " ";
                    }

                    // newAlarm
                    pushAlarmInfo(defectNames, tStdDevicemete.getMeteName() + "--" + defectNames);
                }
            }else {
                cruiseResultMap.put("resultNum", resultValue.contains("device") ? resultValue.replaceAll("device","") : "--");
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                cruiseResultMap.put("cruiseAbnormal", "--");
            }
        }catch (Exception e){
            log.error("缺陷结果处理异常：", e);
        }
    }

    /**
     * webSocket通知前端刷新告警统计数量
     *
     * @param resultValue 告警名称
     * @param warnContent 告警内容
     * @return void
     */
    public void pushAlarmInfo(String resultValue, String warnContent) {
        try {
            Map<String, String> jasonMaps = new HashMap<>(5);
            jasonMaps.put("type", "newAlarm");
            jasonMaps.put("alarmName", resultValue);
            jasonMaps.put("alarmTime", DateTimeUtil.format(new Date()));
            jasonMaps.put("alarmContent", warnContent);
            log.info("告警生成-前端推送：{}", JSON.toJSONString(jasonMaps));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 判断该测点是否设置了告警推送
     * 若是,则将配置的告警信息组成告警弹框所需内容推给前端
     *
     * @param tStdDevicemete 测点信息
     * @param infoMap        告警信息
     */
    public void alarmPopUp(TStdDeviceMete tStdDevicemete, Map<String, String> infoMap) {
        // 弹框PlanB-推送
        Map<String, String> currentWarnInfo = new HashMap<>();
        try {
            String warnId = infoMap.get("warnId");
            currentWarnInfo.put("warnId", warnId);
            currentWarnInfo.put("defectModel", infoMap.get("defectModel"));
            currentWarnInfo.put("isPop", "false");

            Integer alarmLevel = NumberUtils.toInt(infoMap.get("alarmLevel"));
//            boolean isSet = StringUtils.isNotEmpty(tStdDevicemete.getAlarmNote()) && StringUtils.equals("1", tStdDevicemete.getAlarmNote());
            boolean reachDefectLevel = Objects.equals(133, alarmLevel);
            boolean reachAlarmLevel = Objects.nonNull(tStdDevicemete.getAlarmLevel()) &&
                    (alarmLevel.compareTo(tStdDevicemete.getAlarmLevel()) == 0 || alarmLevel > tStdDevicemete.getAlarmLevel());

            boolean reachWarnCondition;
            if (StringUtils.equals("warn", infoMap.get("flag"))){
                reachWarnCondition =  Boolean.TRUE.equals(reachAlarmLevel);
            }else if (StringUtils.equals("defect", infoMap.get("flag"))){
                reachWarnCondition = Boolean.TRUE.equals(reachDefectLevel);
            }else {
                reachWarnCondition = true;
            }

            if (Boolean.TRUE.equals(reachWarnCondition)){
                //webSocket通知前端调用查询告警弹框的接口
                Map<String, String> jasonMaps = new HashMap<>();
                jasonMaps.put("type", "alarmPopUp");
                jasonMaps.put("warnLevel", infoMap.getOrDefault("alarmLevel", ""));
                jasonMaps.put("warnType", "1");
                jasonMaps.put("warnId", warnId);
                jasonMaps.put("defectModel", infoMap.get("defectModel"));
                log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMaps));
                currentWarnInfo.put("isPop","true");
                String edgeLevel = String.valueOf(ValueUtil.getOrDefault(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"),""));
                if (!Constant.LEVEL_UP_SYSTEM.equals(edgeLevel)){
                    Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps);
                }
            }
            redisTemplate.opsForValue().set("currentWarn", currentWarnInfo, 3, TimeUnit.MINUTES);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 缺陷info信息组装
     *
     * @param defectMap 缺陷map信息
     * @return TDefectInfo
     */
    private TDefectInfo getDefectInfo(Map<String, String> defectMap) {
        TDefectInfo tDefectInfo = new TDefectInfo();
        try {
            tDefectInfo.setDefectLevel(NumberUtils.toInt(defectMap.get("defectLevel")));
            tDefectInfo.setDefectTime(DateTimeUtil.parse(defectMap.get("defectTime")));
            tDefectInfo.setDefectType(NumberUtils.toInt(defectMap.get("defectType")));
            tDefectInfo.setDefectContent(defectMap.get("defectContent"));
            tDefectInfo.setDeviceId(NumberUtils.toLong(defectMap.get("deviceId")));
            tDefectInfo.setInstanceId(NumberUtils.toLong(defectMap.get("instanceId")));
            tDefectInfo.setCunstomId(defectMap.get("customId"));
            tDefectInfo.setStdMeteId(NumberUtils.toLong(defectMap.get("stdMeteId")));
            tDefectInfo.setConfMode(NumberUtils.toInt(defectMap.get("confMode")));
            tDefectInfo.setImagePath(defectMap.get("imagePath"));
            tDefectInfo.setAlarmSource(NumberUtils.toInt(defectMap.get("alarmSource")));
        }catch (Exception e){
            log.error("缺陷info信息组装异常：", e);
        }
        return tDefectInfo;
    }

    /**
     * 缺陷map信息组装
     *
     * @param resultImage 缺陷结果图片
     * @param resultValue      缺陷结果
     * @param cruiseResultMap  redis中巡视点结果信息
     * @param tStdDevicemete   测点信息
     * @return Map<String, String>
     */
    private Map<String, String> getDefectMap(String resultImage, String resultValueItem,
                                             Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete,
                                             String resultValue) {
        log.info("resultImage:{},resultValue:{}", resultImage, resultValue);
        Map<String, String> defectMap = new HashMap<>(16);
        try {
            defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_model", resultValue));
            defectMap.put("defectLevel", analyseDataOperateService.selectAlgorithmDefectInfo(defectMap.get("defectType")));
            defectMap.put("defectContent", resultValue);
            defectMap.put("deviceId", cruiseResultMap.get("deviceId"));
            defectMap.put("instanceId", cruiseResultMap.get("instanceId"));
            defectMap.put("customId", tStdDevicemete.getCustomId());
            defectMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
            defectMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未核查"));
            defectMap.put("imagePath", resultImage);
            defectMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
            defectMap.put("defectTime", DateTimeUtil.format(new Date()));
            defectMap.put("value", resultValueItem);
        }catch (Exception e){
            log.error("缺陷map信息组装异常：" , e);
        }
        return defectMap;
    }

    /**
     * 判别结果处理
     *
     * @param msgID            随机数
     * @param analyseResultImg 巡视结构图
     * @param resultValue      巡视结果值
     * @param cruiseResultMap  redis中巡视点结果信息
     */
    private void distinguishHandler(String msgID, String analyseResultImg, String resultValue, Map<String, String> cruiseResultMap) {
        String resultImage;
        try {
            resultImage = processResultToUpSystem.replaceResultImgPath(analyseResultImg, true);
            resultValue = analyseDataOperateService.resolveDefectResult(resultValue);
            log.info("Parse distinguish data is==={}", resultValue);

            cruiseResultMap.put("resultNum", resultValue.contains("device") ? resultValue.replaceAll("device","") : resultValue);
            cruiseResultMap.put("picpath", resultImage);
            boolean abnormal = StringUtils.equals("图像有差异", resultValue);
            cruiseResultMap.put("cruiseResult", String.valueOf(abnormal ? CRUISE_RESULT_ABNORMAL : CRUISE_RESULT_NORMAL));
            cruiseResultMap.put("cruiseAbnormal", abnormal ? String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL) : "--");

            // 判别异常 为上报到算法管理平台暂存数据
            if (StringUtils.equals("图像有差异", resultValue)){
                String msgName = "msg:" + msgID + ":" + String.valueOf(UUID.randomUUID()).replace("-", "");
                redisTemplate.opsForHash().put(msgName, "value", resultValue);
            }
        }catch (Exception e){
            log.error("判别结果处理异常：", e);
        }
    }
}
