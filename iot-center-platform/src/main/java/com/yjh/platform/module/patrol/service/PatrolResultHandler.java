package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.thread.InspectionResultThread;
import com.yjh.platform.module.patrol.thread.IsWarnAfterCruiseThread;
import com.yjh.platform.module.patrol.thread.NonhomologousWarnThread;
import com.yjh.platform.module.patrol.thread.RobotInspectionWarnThread;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.threadpool.TaskExecutePool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    Logger log = LoggerFactory.getLogger(PatrolResultHandler.class);

    public PatrolResultHandler(RedisTemplate redisTemplate, TRobotInspectionDao tRobotInspectionDao,
                               AnalyseDataOperateService analyseDataOperateService,
                               ProcessResultToUpSystem processResultToUpSystem, UPatrolTaskService uPatrolTaskService) {
        this.redisTemplate = redisTemplate;
        this.tRobotInspectionDao = tRobotInspectionDao;
        this.analyseDataOperateService = analyseDataOperateService;
        this.processResultToUpSystem = processResultToUpSystem;
        this.uPatrolTaskService = uPatrolTaskService;
    }

    /**
     * 处理机器人/无人机测点告警
     *
     * @param alarmList 机器人/无人机测点告警
     */
    public void robotPatrolTaskAlarm(List<RobotPatrolTaskAlarm> alarmList) {
        if (alarmList.isEmpty()) {
            return;
        }
        log.info("alarmList=={}", alarmList);
        try {
            for (RobotPatrolTaskAlarm taskAlarm : alarmList) {
                // 通过上报的任务id查询巡视主机上的任务id
                String taskCode = taskAlarm.getTaskCode();
                String taskId = tRobotInspectionDao.selectRealTaskId(taskCode);
                if (StringUtils.isEmpty(taskId)) {
                    taskId = taskCode;
                    log.info("taskId is empty, use taskCode as taskId");
                }
                log.info("taskCode==={},taskId===={}", taskCode, taskId);
                taskAlarm.setTaskCode(taskId);

        for (RobotPatrolTaskAlarm taskAlarm : alarmList) {
            // 通过上报的任务id查询巡视主机上的任务id
            String taskCode = taskAlarm.getTaskCode();
            String edgeCode = taskAlarm.getRobotCode();
            String originId = taskAlarm.getDeviceId();
            String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"
            ));
            String deviceId = originId;
            // 1的情况不用考虑，2的情况需要查t_std_region，有就是下级传的；t_robot_info有，就是上级
            if ("2".equals(sysLevel) && tRobotInspectionDao.selectRobot(edgeCode) > 0 || "3".equals(sysLevel)) {
                deviceId = tRobotInspectionDao.selectRealInstanceId(originId, edgeCode);
            }
            taskAlarm.setDeviceId(deviceId);
            String taskId = tRobotInspectionDao.selectRealTaskId(taskCode);
            if (StringUtils.isEmpty(taskId)) {
                taskId = taskCode;
                log.info("taskId is empty, use taskCode as taskId");
            }
            log.info("taskCode==={},taskId===={}", taskCode, taskId);

            RobotInspectionWarnThread robotWarnThread = new RobotInspectionWarnThread(taskAlarm, redisTemplate,
                    analyseDataOperateService);
            TaskExecutePool.getInstance().execute(robotWarnThread);

            boolean flag = ArrayUtils.contains(new String[]{"3", "4", "9"}, taskAlarm.getAlarmType());
            if (flag) {
                //非同源告警处理
                NonhomologousWarnThread nonhomologousWarnThread = new NonhomologousWarnThread(taskAlarm,
                        redisTemplate, 0);
                TaskExecutePool.getInstance().execute(nonhomologousWarnThread);
            }
        }catch (Exception e){
            log.error("处理机器人/无人机测点告警异常:", e);
        }
    }

    /**
     * 处理机器人/无人机巡视结果
     *
     * @param resultList 机器人/无人机巡视结果
     */
    public void robotPatrolTaskResult(List<RobotPatrolTaskResult> resultList) {
        if (resultList.isEmpty()) {
            return;
        }
        log.info("resultList=={}", resultList);
        try {
            for (RobotPatrolTaskResult robotPatrolTaskResult : resultList) {
                Map<String, String> infoMap = new HashMap<>(8);

                // 通过上报的任务id查询巡视主机上的任务id
                String taskCode = robotPatrolTaskResult.getTaskCode();
                String taskId = tRobotInspectionDao.selectRealTaskId(taskCode);
                if (StringUtils.isEmpty(taskId)) {
                    taskId = taskCode;
                    log.info("taskId is empty, use taskCode as taskId");
                }
                log.info("report taskCode==={},patrol taskId===={}", taskCode, taskId);
                infoMap.put("taskId", taskId);

                // 文件处理
                Map<String, String> isAlarmMap = resultFileHandler(robotPatrolTaskResult, infoMap);
                // 告警处理
                alarmHandlerAfterCruise(robotPatrolTaskResult, taskId, isAlarmMap);
                // 巡视结果处理
                InspectionResultThread cruiseResultDealThread = new InspectionResultThread(robotPatrolTaskResult, infoMap, redisTemplate, true);
                ThreadPoolUtil.PATROL_POOL.addThread(cruiseResultDealThread);
                // 非同源告警处理
                RobotPatrolTaskAlarm taskAlarm = new RobotPatrolTaskAlarm();
                taskAlarm.setTaskCode(robotPatrolTaskResult.getTaskCode());
                taskAlarm.setValue(robotPatrolTaskResult.getValue());
                taskAlarm.setDeviceId(robotPatrolTaskResult.getDeviceId());
                NonhomologousWarnThread nonhomologousWarnThread = new NonhomologousWarnThread(taskAlarm, redisTemplate, 1);
                ThreadPoolUtil.PATROL_POOL.addThread(nonhomologousWarnThread);
            }
            log.info("taskCode==={},taskId===={}", taskCode, taskId);
            infoMap.put("taskId", taskId);
            String originId = robotPatrolTaskResult.getDeviceId();
            String edgeCode = robotPatrolTaskResult.getRobotCode();
            String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"
            ));
            String deviceId = originId;
            // 1的情况不用考虑，2的情况需要查t_std_region，有就是下级传的；t_robot_info有，就是上级
            if ("2".equals(sysLevel) && tRobotInspectionDao.selectRobot(edgeCode) > 0 || "3".equals(sysLevel)) {
                deviceId = tRobotInspectionDao.selectRealInstanceId(originId, edgeCode);
            }
            robotPatrolTaskResult.setDeviceId(deviceId);

            // 文件处理
            Map<String, String> isAlarmMap = resultFileHandler(robotPatrolTaskResult, infoMap);
            if(!"3".equals(sysLevel)){
                // 告警处理
                alarmHandlerAfterCruise(robotPatrolTaskResult, taskId, isAlarmMap);
                // 非同源告警处理
                RobotPatrolTaskAlarm taskAlarm = new RobotPatrolTaskAlarm();
                taskAlarm.setTaskCode(robotPatrolTaskResult.getTaskCode());
                taskAlarm.setValue(robotPatrolTaskResult.getValue());
                taskAlarm.setDeviceId(deviceId);
                NonhomologousWarnThread nonhomologousWarnThread = new NonhomologousWarnThread(taskAlarm, redisTemplate, 1);
                ThreadPoolUtil.PATROL_POOL.addThread(nonhomologousWarnThread);
            }
            // 巡视结果处理
            InspectionResultThread cruiseResultDealThread = new InspectionResultThread(robotPatrolTaskResult, infoMap
                    , redisTemplate, true);
            ThreadPoolUtil.PATROL_POOL.addThread(cruiseResultDealThread);
        }
    }

    /**
     * 根据巡视结果判断是否生成告警
     *
     * @param robotPatrolTaskResult 机器人/无人机巡视结果
     * @param taskId 任务id
     * @param isAlarmMap 告警信息map
     */
    private void alarmHandlerAfterCruise(RobotPatrolTaskResult robotPatrolTaskResult, String taskId, Map<String,
            String> isAlarmMap) {
        try {
            isAlarmMap.put("robotCode", robotPatrolTaskResult.getRobotCode());
            isAlarmMap.put("taskCode", taskId);
            isAlarmMap.put("deviceId", robotPatrolTaskResult.getDeviceId());
            isAlarmMap.put("value", robotPatrolTaskResult.getValue());
            isAlarmMap.put("deviceName", robotPatrolTaskResult.getDeviceName());
            isAlarmMap.put("recognitionType", robotPatrolTaskResult.getRecognitionType());
            isAlarmMap.put("fileType", robotPatrolTaskResult.getFileType());
            isAlarmMap.put("time", robotPatrolTaskResult.getTime());
            isAlarmMap.put("taskName", robotPatrolTaskResult.getTaskName());
            IsWarnAfterCruiseThread isWarnAfterCruiseThread = new IsWarnAfterCruiseThread(isAlarmMap, redisTemplate);
            ThreadPoolUtil.PATROL_POOL.addThread(isWarnAfterCruiseThread);
        } catch (Exception e) {
            log.error("机器人/无人机告警处理异常：", e);
        }
    }

    /**
     * 对机器人/无人机结果文件处理
     *
     * @param robotPatrolTaskResult 机器人/无人机巡视结果
     * @param infoMap               任务结果其他信息
     * @return Map<String, String>
     */
    private Map<String, String> resultFileHandler(RobotPatrolTaskResult robotPatrolTaskResult,
                                                  Map<String, String> infoMap) {
        Map<String, String> isAlarmMap = new HashMap<>(16);
        String taskId = infoMap.get("taskId");

        String ftpImageRelative = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageRelative",
                "content"));
        String ftpImageAbsolute = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute",
                "content"));
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

            switch (fileType) {
                case "1":
                    String descFirFilePath = developAbsoluteUrl + "/FIR/" + fileName;
                    FileUtil.copyFileUsingStream(temporaryFilePath, descFirFilePath);
                    infoMap.put("relativePath", developRelativeUrl + "/FIR/" + fileName);
                    infoMap.put("absolutePath", descFirFilePath);

                    isAlarmMap.put("relativePath", developRelativeUrl + "/FIR/" + fileName);
                    isAlarmMap.put("absolutePath", descFirFilePath);
                    break;
                case "2":
                    String descCcdFilePath = developAbsoluteUrl + "/CCD/" + fileName;
                    FileUtil.copyFileUsingStream(temporaryFilePath, descCcdFilePath);
                    infoMap.put("relativePath", developRelativeUrl + "/CCD/" + fileName);
                    infoMap.put("absolutePath", descCcdFilePath);

                    isAlarmMap.put("relativePath", developRelativeUrl + "/CCD/" + fileName);
                    isAlarmMap.put("absolutePath", descCcdFilePath);
                    break;
                case "3":
                    String descAudioFilePath = developAbsoluteUrl + "/Audio/" + fileName;
                    FileUtil.copyFileUsingStream(temporaryFilePath, descAudioFilePath);
                    infoMap.put("relativePath", developRelativeUrl + "/Audio/" + fileName);
                    infoMap.put("absolutePath", descAudioFilePath);
                    break;
                case "4":
                    String descVideoFilePath = developAbsoluteUrl + "/Video/" + fileName;
                    FileUtil.copyFileUsingStream(temporaryFilePath, descVideoFilePath);
                    infoMap.put("relativePath", developRelativeUrl + "/Video/" + fileName);
                    infoMap.put("absolutePath", descVideoFilePath);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("机器人/无人机文件处理异常：", e);
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
        log.info("resultList=={}", resultList);

        try {
            for (AnalysePatrolTaskResult patrolTaskResult : resultList) {
                String taskId = patrolTaskResult.getTaskId();
                String instanceId = patrolTaskResult.getInstanceId();
                String analyseType = patrolTaskResult.getAnalyseType();
                String analyseResultImg = patrolTaskResult.getAnalyseResultImg();
                String resultValue = patrolTaskResult.getResultValue();
                String resultDesc = patrolTaskResult.getResultDesc();
                String conf = patrolTaskResult.getConf();
                String firDocPath = patrolTaskResult.getFirDocPath();

                String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
                Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(redisKeyName);
                log.info("Read from redis cruiseResultMap is：{}", cruiseResultMap);

                String resultImage = cruiseResultMap.get("picpath");
                if (StringUtils.isNotEmpty(analyseResultImg)) {
                    resultImage = analyseResultImg;
                }

                if ("11".equals(analyseType) && "".equals(resultImage)) {
                    continue;
                }
                TStdDeviceMete tStdDevicemete =
                        analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(instanceId));
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
                cruiseResultMap.put("isWarn", StringUtils.isNotEmpty(cruiseResultMap.get("isWarn")) ? "0" : "1");
                cruiseResultMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
                log.info("cruiseResultMap==={}", cruiseResultMap);
                redisTemplate.opsForHash().putAll(redisKeyName, cruiseResultMap);

//                // webSocket通知前端调用巡视监控的接口
//                Map<String, String> jasonMap = new HashMap<>(3);
//                jasonMap.put("type", "finishedOneInstance");
//                jasonMap.put("taskId", taskId);
//                log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMap));
//                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

                uPatrolTaskService.patrolTaskResultHandler(taskId, Long.valueOf(instanceId));

                // 缺陷和判别上报算法管理平台
                processResultToUpSystem.defectToAlgorithmM(taskId, instanceId, msgId);
            }
        } catch (Exception e) {
            log.error("处理算法分析后的巡视结果异常：", e);
        }
    }

    /**
     * 识别结果处理
     *
     * @param analyseResultImg 巡视结果图
     * @param resultValue      巡视结果值
     * @param cruiseResultMap  redis中巡视点结果信息
     * @param tStdDevicemete   测点信息
     * @return String
     */
    private String recognitionHandler(String analyseResultImg, String resultValue,
                                      Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete,
                                      String firDocPath) {
        String resultImage;
        try {
            if (StringUtils.isNotEmpty(analyseResultImg)) {
                resultImage = analyseResultImg.replaceAll((String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":meterResultImg", "content"), (String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":meterResultRealImg", "content"));
                log.info("recognition image=={}", resultImage);

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
            } else {
                normalRecognitionHandler(resultValue, cruiseResultMap, tStdDevicemete, firDocPath);
            }
        } catch (Exception e) {
            log.error("识别结果处理异常：", e);
        }
    }

    public Map<String, String> normalRecognitionHandler(String resultValue, Map<String, String> cruiseResultMap,
                                                        String firDocPath) {
        TStdDeviceMete tStdDevicemete =
                analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(cruiseResultMap.get(
                        "instanceId")));
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
    private Map<String, String> normalRecognitionHandler(String resultValue, Map<String, String> cruiseResultMap,
                                                         TStdDeviceMete tStdDevicemete, String firDocPath) {
        try {
            // 红外会返回两个温度(如：12.3,2.3),所以需要这样取值
            String resultStringValue = resultValue.split(",")[0];
            log.info("resultStringValue=={}, resultValue=={}", resultStringValue, resultValue);

            if (resultStringValue.matches("^([0-9]{1,})$|^([0-9]{1,}[.][0-9]*)$|^(-[0-9]{1,})$|^(-[0-9]{1,}[.][0-9]*)"
                    + "$|[\\u4E00-\\u9FA5]+")) {
                // 判断是否为红外识别且获取FIR文件 放入缓存中
                if (StringUtils.isNotEmpty(firDocPath)) {
                    firDocPath = firDocPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param" +
                            ":infraredStorePath", "content")), String.valueOf(redisTemplate.opsForHash().get(
                                    "t_sys_param:infraredRealPath", "content")));
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
                int warnFlag = analyseDataOperateService.warnSettings(meteKind, stateZero, alarmState, highLimit1,
                        lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
                if (warnFlag == 1) {
                    Map<String, String> warnMap = new HashMap<>();
                    String warnName =
                            "warnInfo:" + cruiseResultMap.get("taskId") + String.valueOf(UUID.randomUUID()).replace(
                                    "-", "");

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

                    String alarmLevel = "";
                    switch (meteKind) {
                        case "1":
                            int warnRuleFlag = analyseDataOperateService.warnJudgementTelesignaling(resultValue,
                                    stateZero, stateOne, alarmState);
                            if (warnRuleFlag == 1) {
                                log.info("Alarm value is reached(遥信)");
                                alarmLevel = String.valueOf(tStdDevicemete.getAlarmLevel());
                                warnMap.put("warnLevel", String.valueOf(tStdDevicemete.getAlarmLevel()));
                                warnMap.put("warnName", meteName + "状态异常");
                                warnMap.put("warnTime", DateTimeUtil.format(new Date()));
                                warnMap.put("outRange", "--");
                                if (alarmState == 0) {
                                    warnMap.put("warnContent",
                                            meteName + ":" + stateZero + "--" + analyseDataOperateService.selectDictNote(String.valueOf(tStdDevicemete.getAlarmLevel()), "alarm_level"));
                                } else {
                                    warnMap.put("warnContent",
                                            meteName + ":" + stateOne + "--" + analyseDataOperateService.selectDictNote(String.valueOf(tStdDevicemete.getAlarmLevel()), "alarm_level"));
                                }
                                log.info("warnMap=={}", warnMap);
                                redisTemplate.opsForHash().putAll(warnName, warnMap);

                                cruiseResultMap.put("isWarn", "1");
                                cruiseResultMap.put("resultNum", resultStringValue);
                                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM));
                            } else {
                                log.info("Alarm value is not reached(遥信)");
                                cruiseResultMap.put("resultNum", resultValue);
                                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                                cruiseResultMap.put("cruiseAbnormal", "--");
                            }
                            break;
                        case "2":
                            Float resultValueMeter = NumberUtils.toFloat(resultStringValue);
                            int warnRuleMeter = analyseDataOperateService.warnJudgement(resultValueMeter, highLimit1,
                                    lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
                            log.info("warnRuleMeter=={}", warnRuleMeter);

                            if (warnRuleMeter > 0) {
                                log.info("Alarm value is reached(遥测)");
                                warnMap.put("warnName", meteName + "数据异常");
                                warnMap.put("warnTime", DateTimeUtil.format(new Date()));

                                switch (warnRuleMeter) {
                                    case 1:
                                        warnMap.put("warnLevel", analyseDataOperateService.selectDictCode(
                                                "alarm_level", "预警"));
                                        warnMap.put("warnContent", meteName + ":" + resultValue + "--" + "预警");
                                        alarmLevel = "1";
                                        warnMap.put("outRange", resultValueMeter >= highLimit1 ?
                                                String.valueOf(resultValueMeter - highLimit1) :
                                                String.valueOf(lowLimit1 - resultValueMeter));
                                        break;
                                    case 2:
                                        warnMap.put("warnLevel", analyseDataOperateService.selectDictCode(
                                                "alarm_level", "一般告警"));
                                        warnMap.put("warnContent", meteName + ":" + resultValue + "--" + "一般告警");
                                        alarmLevel = "2";
                                        warnMap.put("outRange", resultValueMeter >= highLimit2 ?
                                                String.valueOf(resultValueMeter - highLimit2) :
                                                String.valueOf(lowLimit2 - resultValueMeter));
                                        break;
                                    case 3:
                                        warnMap.put("warnLevel", analyseDataOperateService.selectDictCode(
                                                "alarm_level", "严重告警"));
                                        warnMap.put("warnContent", meteName + ":" + resultValue + "--" + "严重告警");
                                        alarmLevel = "3";
                                        warnMap.put("outRange", resultValueMeter >= highLimit3 ?
                                                String.valueOf(resultValueMeter - highLimit3) :
                                                String.valueOf(lowLimit3 - resultValueMeter));
                                        break;
                                    case 4:
                                        warnMap.put("warnLevel", analyseDataOperateService.selectDictCode(
                                                "alarm_level", "危急告警"));
                                        warnMap.put("warnContent", meteName + ":" + resultValue + "--" + "危急告警");
                                        alarmLevel = "4";
                                        warnMap.put("outRange", resultValueMeter >= highLimit4 ?
                                                String.valueOf(resultValueMeter - highLimit4) :
                                                String.valueOf(lowLimit4 - resultValueMeter));
                                        break;
                                    default:
                                        break;
                                }
                                log.info("warnMap=={}", warnMap);
                                redisTemplate.opsForHash().putAll(warnName, warnMap);

                                cruiseResultMap.put("isWarn", "1");
                                cruiseResultMap.put("resultNum", resultValue);
                                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM));
                            } else {
                                log.info("Alarm value is not reached(遥测)");
                                cruiseResultMap.put("resultNum", resultValue);
                                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                                cruiseResultMap.put("cruiseAbnormal", "--");
                            }
                            break;
                        default:
                            break;
                    }

                    Map<String, String> warningMsg = redisTemplate.opsForHash().entries(warnName);
                    log.info("warnMsg==={}", warningMsg);
                    if (!warningMsg.isEmpty()) {
                        TWarnInfo tWarnInfo = getWarnInfo(warningMsg);
                        analyseDataOperateService.insertWarnInfo(tWarnInfo);

                        Map<String, String> infoMap = new HashMap<>(5);
                        infoMap.put("alarmLevel", String.valueOf(tWarnInfo.getWarnLevel()));
                        infoMap.put("flag", "warn");
                        infoMap.put("defectModel", String.valueOf(tWarnInfo.getDefectModel()));
                        alarmPopUp(tStdDevicemete, infoMap);

                        pushAlarmInfo(warningMsg.get("warnName"), warningMsg.get("warnContent"));

                        // 告警上报上一级系统
                        processResultToUpSystem.alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, tWarnInfo);
                    }
                } else {
                    cruiseResultMap.put("resultNum", resultValue);
                    cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                    cruiseResultMap.put("cruiseAbnormal", "--");
                }
            } else {
                cruiseResultMap.put("resultNum", resultValue);
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL));
            }
        } catch (Exception e) {
            log.error("正常识别结果处理异常：", e);
        }
        log.info("cruiseResultMap=={}", cruiseResultMap);
        return cruiseResultMap;
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
    private void defectHandler(String msgID, String analyseResultImg, String resultValueItem,
                               Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete) {
        String resultImage;
        try {
            if (StringUtils.isNotEmpty(analyseResultImg)) {
                resultImage = analyseResultImg.replaceAll((String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":defectResultImg", "content"), (String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":defectResultRealImg", "content"));
            } else {
                resultImage = analyseResultImg.replaceAll((String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":resultImgPath", "content"), (String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":resultImgRealPath", "content"));
            }
            log.info("defect image=={}", resultImage);

            log.info("Defect data is ==={}", resultValueItem);
            String resultValue = analyseDataOperateService.resolveDefectResult(resultValueItem);
            log.info("Parse defect data is ==={}", resultValue);

            cruiseResultMap.put("resultNum", resultValue);
            cruiseResultMap.put("picpath", resultImage);
            if (!"null".equals(resultValue) && !(resultValue.contains("device"))) {
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DEFECT));

                List<TDefectInfo> defectInfoList = new ArrayList<>();
                String[] resultArr = resultValue.split("\\s+");
                if (resultArr.length == 1) {
                    log.info("Only one defect is generated！！！");
                    // 缺陷信息存redis
                    String redisKeyTemp = String.valueOf(UUID.randomUUID()).replace("-", "");
                    Map<String, String> defectMap = getDefectMap(analyseResultImg, resultValueItem, cruiseResultMap,
                            tStdDevicemete, resultValue);
                    log.info("defectMap=={}", JSON.toJSONString(defectMap));
                    redisTemplate.opsForHash().putAll("defectInfo:" + cruiseResultMap.get("taskId") + ":" + redisKeyTemp, defectMap);
                    redisTemplate.opsForHash().putAll("defect:" + msgID + ":" + redisKeyTemp, defectMap);

                    TDefectInfo tDefectInfo = getDefectInfo(defectMap);
                    log.info("tDefectInfo=={}", JSON.toJSONString(tDefectInfo));
                    defectInfoList.add(tDefectInfo);

                    // 缺陷告警推送
                    Map<String, String> infoMap = new HashMap<>(5);
                    infoMap.put("alarmLevel", defectMap.get("defectLevel"));
                    infoMap.put("flag", "defect");
                    infoMap.put("defectModel", defectMap.get("defectType"));
                    alarmPopUp(tStdDevicemete, infoMap);

                    pushAlarmInfo(resultValue, tStdDevicemete.getMeteName() + "--" + resultValue);

                } else if (resultArr.length > 1) {
                    log.info("Multiple defects are generated！！！");
                    String defectNames = "";
                    for (String res : resultArr) {
                        // 缺陷信息存redis
                        String redisKeyTemp = String.valueOf(UUID.randomUUID()).replace("-", "");
                        Map<String, String> defectMap = getDefectMap(analyseResultImg, resultValueItem,
                                cruiseResultMap, tStdDevicemete, res);
                        log.info("defectMap=={}", JSON.toJSONString(defectMap));
                        redisTemplate.opsForHash().putAll("defectInfo:" + cruiseResultMap.get("taskId") + ":" + redisKeyTemp, defectMap);
                        redisTemplate.opsForHash().putAll("defect:" + msgID + ":" + redisKeyTemp, defectMap);

                        TDefectInfo tDefectInfo = getDefectInfo(defectMap);
                        log.info("tDefectInfo=={}", JSON.toJSONString(tDefectInfo));
                        defectInfoList.add(tDefectInfo);

                        // 缺陷告警推送
                        Map<String, String> infoMap = new HashMap<>(5);
                        infoMap.put("alarmLevel", defectMap.get("defectLevel"));
                        infoMap.put("flag", "defect");
                        infoMap.put("defectModel", defectMap.get("defectType"));
                        alarmPopUp(tStdDevicemete, infoMap);
                        defectNames = defectNames + res + " ";
                    }

                    pushAlarmInfo(defectNames, tStdDevicemete.getMeteName() + "--" + defectNames);
                }

                // 缺陷批量实时入库
                if (CollectionUtils.isNotEmpty(defectInfoList)) {
                    analyseDataOperateService.batchInsertDefectInfo(defectInfoList);
                }
                log.info("--------缺陷入库完成-----");
            } else {
                cruiseResultMap.put("resultNum", resultValue.contains("device") ? resultValue.replaceAll("device",
                        "") : "--");
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                cruiseResultMap.put("cruiseAbnormal", "--");
            }
        } catch (Exception e) {
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
        } catch (Exception e) {
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
            String warnId = String.valueOf(analyseDataOperateService.selectCurrentWarn());
            currentWarnInfo.put("warnId", warnId);
            currentWarnInfo.put("defectModel", infoMap.get("defectModel"));
            currentWarnInfo.put("isPop", "false");

            Integer alarmLevel = NumberUtils.toInt(infoMap.get("alarmLevel"));
            boolean isSet = StringUtils.isNotEmpty(tStdDevicemete.getAlarmNote()) && StringUtils.equals("1",
                    tStdDevicemete.getAlarmNote());
            boolean reachDefectLevel = Objects.equals(133, alarmLevel);
            boolean reachAlarmLevel =
                    Objects.nonNull(tStdDevicemete.getAlarmLevel()) && (alarmLevel.compareTo(tStdDevicemete.getAlarmLevel()) == 0 || alarmLevel > tStdDevicemete.getAlarmLevel());

            boolean reachWarnCondition;
            if (StringUtils.equals("warn", infoMap.get("flag"))) {
                reachWarnCondition = Boolean.TRUE.equals(isSet) && Boolean.TRUE.equals(reachAlarmLevel);
            } else if (StringUtils.equals("defect", infoMap.get("flag"))) {
                reachWarnCondition = Boolean.TRUE.equals(isSet) && Boolean.TRUE.equals(reachDefectLevel);
            } else {
                reachWarnCondition = true;
            }

            if (Boolean.TRUE.equals(reachWarnCondition)) {
                //webSocket通知前端调用查询告警弹框的接口
                Map<String, String> jasonMaps = new HashMap<>();
                jasonMaps.put("type", "alarmPopUp");
                jasonMaps.put("warnId", warnId);
                jasonMaps.put("defectModel", infoMap.get("defectModel"));
                log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMaps));
                currentWarnInfo.put("isPop", "true");
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps);
            }
            redisTemplate.opsForValue().set("currentWarn", currentWarnInfo, 3, TimeUnit.MINUTES);
        } catch (Exception e) {
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
        } catch (Exception e) {
            log.error("组装缺陷info信息异常：", e);
        }
        return tDefectInfo;
    }

    /**
     * 缺陷map信息组装
     *
     * @param analyseResultImg 缺陷结果图片
     * @param resultValue      缺陷结果
     * @param cruiseResultMap  redis中巡视点结果信息
     * @param tStdDevicemete   测点信息
     * @return Map<String, String>
     */
    private Map<String, String> getDefectMap(String analyseResultImg, String resultValueItem,
                                             Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete,
                                             String resultValue) {
        log.info("analyseResultImg:{},resultValue:{}", analyseResultImg, resultValue);
        Map<String, String> defectMap = new HashMap<>(16);
        try {
            defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_model", resultValue));
            defectMap.put("defectLevel", analyseDataOperateService.selectAlgorithmDefectInfo(defectMap.get(
                    "defectType")));
            defectMap.put("defectContent", resultValue);
            defectMap.put("deviceId", cruiseResultMap.get("deviceId"));
            defectMap.put("instanceId", cruiseResultMap.get("instanceId"));
            defectMap.put("customId", tStdDevicemete.getCustomId());
            defectMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
            defectMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未核查"));
            defectMap.put("imagePath", analyseResultImg);
            defectMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
            defectMap.put("defectTime", DateTimeUtil.format(new Date()));
            defectMap.put("value", resultValueItem);
        } catch (Exception e) {
            log.error("组装缺陷map信息异常：", e);
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
    private void distinguishHandler(String msgID, String analyseResultImg, String resultValue,
                                    Map<String, String> cruiseResultMap) {
        String resultImage;
        try {
            if (StringUtils.isNotEmpty(analyseResultImg)) {
                resultImage = analyseResultImg.replaceAll((String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":judgeResultImg", "content"), (String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":judgeResultRealImg", "content"));
            } else {
                resultImage = analyseResultImg.replaceAll((String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":resultImgPath", "content"), (String) redisTemplate.opsForHash().get("t_sys_param" +
                        ":resultImgRealPath", "content"));
            }
            log.info("distinguish image=={}", resultImage);

            resultValue = analyseDataOperateService.resolveDefectResult(resultValue);
            log.info("Parse distinguish data is==={}", resultValue);

            cruiseResultMap.put("resultNum", resultValue);
            cruiseResultMap.put("picpath", resultImage);
            cruiseResultMap.put("cruiseResultMap", StringUtils.equals("abnormal", resultValue) ?
                    String.valueOf(CRUISE_RESULT_ABNORMAL) : String.valueOf(CRUISE_RESULT_NORMAL));
            cruiseResultMap.put("cruiseAbnormal", "--");

            //判别异常
            String msgName = "msg:" + msgID + ":" + String.valueOf(UUID.randomUUID()).replace("-", "");
            redisTemplate.opsForHash().put(msgName, "value", resultValue);
        } catch (Exception e) {
            log.error("判别结果处理异常：", e);
        }
    }

}
