package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskResult;
import com.yjh.platform.module.patrol.entity.TCruisePointInstanceDetail;
import com.yjh.platform.module.patrol.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.user.entity.TAlgorithmMeteInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import static com.yjh.platform.module.patrol.CruiseConstant.*;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.MAP_LOCK;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * @author YC
 * @date 2020/12/8 9:58
 * 机器人巡视结果处理线程
 */
@Slf4j
public class InspectionResultThread implements Runnable{

    private RedisTemplate redisTemplate;
    private Boolean changeTaskStatus;
    private final RobotPatrolTaskResult robotPatrolTaskResult;
    private final Map<String, String> infoMap;
    private final TCruisePointInstance insInfo;
    private final UPatrolTaskService uPatrolTaskService;
    private final AnalyseDataOperateDao analyseDataOperateDao;
    private final PatrolResultHandler resultHandler;

    private static ReentrantLock lock = new ReentrantLock();

    public InspectionResultThread(RobotPatrolTaskResult robotPatrolTaskResult, Map<String, String> infoMap,
                                  TCruisePointInstance insInfo,
                                  RedisTemplate redisTemplate, boolean changeTaskStatus){
        this.robotPatrolTaskResult = robotPatrolTaskResult;
        this.infoMap = infoMap;
        this.insInfo = insInfo;
        this.redisTemplate = redisTemplate;
        this.changeTaskStatus = changeTaskStatus;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        this.analyseDataOperateDao = StaticContextAccessor.getBean(AnalyseDataOperateDao.class);
        this.resultHandler = StaticContextAccessor.getBean(PatrolResultHandler.class);
    }

    @Override
    public void run(){

        try {
            log.info("开始处理巡检结果并对其标准化 >>>>>>> robotPatrolTaskResult==={}", JSON.toJSONString(robotPatrolTaskResult));
            String taskId = infoMap.get("taskId");
            String robotCode = robotPatrolTaskResult.getSendCode();
            String instanceId = infoMap.get("instanceId");
            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            if (MapUtils.isEmpty(tCruiseTaskResultMap) || !tCruiseTaskResultMap.containsKey("deviceId") || !tCruiseTaskResultMap.containsKey("cruiseId")) {
                tCruiseTaskResultMap = new HashMap<>(32);
                upSystemTaskInfoInitialize(tCruiseTaskResultMap, taskId, insInfo);
            }

            tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
            tCruiseTaskResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
            setCruiseResult(tCruiseTaskResultMap, instanceId);

            tCruiseTaskResultMap.put("picpath", infoMap.getOrDefault("relativePath", ""));
            tCruiseTaskResultMap.put("origpic", infoMap.getOrDefault("absolutePath", ""));
            if ("3".equals(robotPatrolTaskResult.getFileType())) {
                // 声音文件处理
                tCruiseTaskResultMap.put("voicePath", infoMap.getOrDefault("relativePath", ""));
            }
            tCruiseTaskResultMap.putIfAbsent("evaluationState", String.valueOf(EVALUATION_STATE_UN));
            tCruiseTaskResultMap.putIfAbsent("isWarn", "0");
            tCruiseTaskResultMap.put("recognitionType", robotPatrolTaskResult.getRecognitionType());
            tCruiseTaskResultMap.put("fileType", robotPatrolTaskResult.getFileType());
            tCruiseTaskResultMap.put("rectangle", robotPatrolTaskResult.getRectangle());
            tCruiseTaskResultMap.put("valueType", robotPatrolTaskResult.getValueType());
            redisTemplate.opsForHash().putAll(redisKeyName, tCruiseTaskResultMap);
            Object waiter = MAP_LOCK.get(taskId + instanceId);
            if (Objects.nonNull(waiter)) {
                synchronized (waiter) {
                    waiter.notifyAll();
                }
                MAP_LOCK.remove(taskId + instanceId);
            }

            // 是否为本级系统下发给下级系统的任务
            boolean flag = judgeTaskSourceHandler(taskId, instanceId, robotCode, tCruiseTaskResultMap.get("cruiseType"));
            if (Boolean.FALSE.equals(flag)) {
                log.info("This is simulation tool task！！！ {}", taskId);
                simulationToolTaskHandler(infoMap.getOrDefault("absolutePath", ""), taskId, instanceId, robotPatrolTaskResult.getValue(), robotPatrolTaskResult.getFilePath());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 判断当前点位的异常状态和点位结果是否正常
     */
    private void setCruiseResult(Map<String, String> tCruiseTaskResultMap, String instanceId) {
        String taskId = infoMap.get("taskId");

        boolean isnormal = true;

        Integer type = uPatrolTaskService.selectRobotType(robotPatrolTaskResult.getSendCode());
        boolean isSimulationTool = Objects.equals(810, type) || Objects.equals(811, type);
        // 非模拟工具上来的结果
        if (Boolean.FALSE.equals(isSimulationTool)) {
            if (StringUtils.isNotEmpty(robotPatrolTaskResult.getValue())) {
                tCruiseTaskResultMap.put("resultNum", robotPatrolTaskResult.getValue());
                log.info("taskId is {},instanceId is {},the result is normal", taskId, instanceId);
            } else {
                // value无值且resultNum为--，若结果非音频文件，则为异常情况
                tCruiseTaskResultMap.put("resultNum", "--");
                if (!"3".equals(robotPatrolTaskResult.getFileType())) {
                    isnormal = false;
                    log.info("taskId is {},instanceId is {},the result is abnormal", taskId, instanceId);
                }
            }
        }
        String cruiseResult;
        String cruiseAbnormal = "--";

        String valid = robotPatrolTaskResult.getValid();
        if (StringUtils.isNotEmpty(robotPatrolTaskResult.getAbnormalType())) {
            cruiseAbnormal = robotPatrolTaskResult.getAbnormalType();
        }
        if (StringUtils.isNotEmpty(valid)) {
            switch (valid) {
                case "1":
                    isnormal = true;
                    break;
                case "2":
                    isnormal = false;
                    cruiseAbnormal = String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM);
                    break;
                case "0":
                default:
                    isnormal = false;
                    cruiseAbnormal = StringUtils.isNotEmpty(cruiseAbnormal) ? cruiseAbnormal : String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL);
                    break;
            }
        } else {
            cruiseAbnormal = isnormal ? String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL) : "--";
        }
        cruiseResult = String.valueOf(isnormal ? CRUISE_RESULT_NORMAL : CRUISE_RESULT_ABNORMAL);

        computeEmpty(tCruiseTaskResultMap, "cruiseResult", cruiseResult);
        computeEmpty(tCruiseTaskResultMap, "cruiseAbnormal", cruiseAbnormal);
    }

    private void upSystemTaskInfoInitialize(Map<String, String> map, String taskId, TCruisePointInstance insInfo) {
        log.info("upSystemTaskInfoInitialize, taskId: {}", taskId);
        if(insInfo == null){
            log.error("task upSystem is error. taskId: {}", taskId);
        } else {
            String instanceId = String.valueOf(insInfo.getInstanceId());
            map.put("deviceId", String.valueOf(insInfo.getDeviceId()));
            map.put("instanceId", instanceId);
            map.put("cruiseName", insInfo.getCruiseName());
            map.put("deviceName", analyseDataOperateDao.selectPatrolDevice(instanceId).get("deviceName"));
            map.put("cruiseId", String.valueOf(insInfo.getCruiseId()));
            map.put("cruiseType", String.valueOf(insInfo.getCruiseType()));
        }

        map.put("instanceName", robotPatrolTaskResult.getDeviceName());
        map.put("picPathAnl", "");
        map.putIfAbsent("remark", "");
        map.putIfAbsent("points", "");
        map.putIfAbsent("origConfirmPicPath", "");
        map.putIfAbsent("firDate", "");
        map.putIfAbsent("confirmPicPath", "");
        map.putIfAbsent("modifyNum", "");
        map.putIfAbsent("origPicAnl", "");
        map.putIfAbsent("identifyResult", "");
        map.putIfAbsent("personCheck", "");
        map.putIfAbsent("createtime", DateTimeUtil.getDateTimeString());
        map.putIfAbsent("identifyState", "");
        map.putIfAbsent("resultPic", "");
        map.putIfAbsent("firName", "");
        map.putIfAbsent("resultDesc", "");
        map.putIfAbsent("checkDate", "");
        map.put("cruiseTime", robotPatrolTaskResult.getTime());
        map.putIfAbsent("checkUser", "");
        map.putIfAbsent("cameraId", "");
        map.putIfAbsent("voicePath", "");
        map.put("taskId", taskId);
    }

    /**
     * 判断任务是否为本级系统下发给下级系统的任务
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @param sendCode 下级唯一标识
     * @return boolean
     */
    private boolean judgeTaskSourceHandler(String taskId, String instanceId, String sendCode, String cruiseType) {
        try {
            String sysLevel = (String)redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content");
            // 巡视主机任务终止的结果消息不调用算法
            boolean isInterrupt = StringUtils.containsAny(robotPatrolTaskResult.getValue(), "任务终止", "超时");

            CruiseConstant.TypeEnum cruiseTypeEnum = CruiseConstant.TypeEnum.getEnum(NumberUtils.toInt(cruiseType));
            Integer type = uPatrolTaskService.selectRobotType(sendCode);
            // 机器人类型为模拟机器人/模拟无人机 为模拟工具
            boolean isSimulationTool = Objects.equals(810, type) || Objects.equals(811, type);
            // 如果是节点 也走模拟工具的逻辑
            boolean needAnalysis = type == null && !"3".equals(sysLevel) && (ArrayUtils.contains(new TypeEnum[]{TypeEnum.INFRARED, TypeEnum.VIDEO, TypeEnum.VOICE}, cruiseTypeEnum));
            isSimulationTool = (isSimulationTool || needAnalysis) && !isInterrupt;
            log.info("simulation tool flag, isSimulationTool: {}, taskId: {}, robotType: {}, sysLevel: {}, cruiseType: {}, isInterrupt: {}", isSimulationTool, taskId, type, sysLevel, cruiseType, isInterrupt);
            if (Boolean.FALSE.equals(isSimulationTool)) {
                Integer flag = uPatrolTaskService.selectIsAlarmByTask(taskId, instanceId);
                if (flag > 0) {
                    log.info("taskId为{}巡视点instanceId为{}的点位产生了告警,需要更新图片", taskId, instanceId);
                    uPatrolTaskService.updatePicPath(taskId, instanceId, infoMap.get("relativePath"));
                }
                uPatrolTaskService.patrolTaskResultHandler(taskId, Long.valueOf(instanceId));
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 工具或下级上报的巡视结果处理
     *
     * @param originPath 巡视结果文件全路径
     * @param taskId     任务id
     * @param instanceId 巡视点id
     * @param value 巡视结果值
     * @param filePath 上报的巡视结果文件路径
     */
    private void simulationToolTaskHandler(String originPath, String taskId, String instanceId, String value, String filePath) {
        try {
            // 复制图片到算法分析指定的路径
            String ftpFileName = originPath.trim().substring(originPath.trim().lastIndexOf("/") + 1);
            String resultImagePath = redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content") + ftpFileName;
            FileUtil.copyFileUsingStream(originPath, resultImagePath);

            // 复制原图到算法分析指定的路径
            if (StringUtils.isNotEmpty(ftpFileName) && StringUtils.isNotEmpty(value) && !ftpsTurbo()){
                log.info("ftpFileName and value is not empty...");
                log.info("resultImagePath=={}", resultImagePath);
                try {
                    uPatrolTaskService.downloadPicture(resultImagePath,filePath);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            TCruisePointInstanceDetail details = uPatrolTaskService.selectForTask(Long.valueOf(instanceId));

            Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
            // 巡检点类型
            int cruiseType = MapUtils.getIntValue(tCruiseTaskResultMap, "cruiseType");

            tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
            tCruiseTaskResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_UN));
            tCruiseTaskResultMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
            tCruiseTaskResultMap.put("isWarn", "0");
            if (TypeEnum.VOICE.getCode() != cruiseType) {
                tCruiseTaskResultMap.put("origpic", resultImagePath);
                String picPath =
                    resultImagePath.replace(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")), String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content")));
                tCruiseTaskResultMap.put("picpath", picPath);
            }
            AbstractVideoCruise abstractVideoCruise = AbstractVideoCruise.Factory.getVideoCruise(cruiseType);
            boolean fileFound = new File(resultImagePath).exists();

            // 判断是否有配置算法
            TAlgorithmMeteInfo algorithm = abstractVideoCruise.needAnalysis(String.valueOf(details.getDeviceMeteId()), value);
            if (algorithm != null && TypeEnum.VOICE.getCode() != cruiseType && fileFound) {
                JSONObject jsonForRe = new JSONObject();
                jsonForRe.put("absPath", resultImagePath);
                Long preset = analyseDataOperateDao.selectPresetIdByInstanceId(instanceId);
                if (preset == null){
                    preset = 0L;
                }
                log.info("presetId====== {}",preset);
                abstractVideoCruise.algorithmAnalysis(tCruiseTaskResultMap, preset, taskId, jsonForRe, algorithm);
            } else {
                updatePointStatusNum(taskId, tCruiseTaskResultMap, details, value, fileFound);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 未配置算法点位结果处理
     *
     * @param taskId               任务id
     * @param tCruiseTaskResultMap 巡视结果map
     * @param details              测点信息
     * @param value                值
     * @param fileFound            文件是否能找到
     */
    private void updatePointStatusNum(String taskId,  Map<String, String> tCruiseTaskResultMap, TCruisePointInstanceDetail details,
                                      String value, boolean fileFound) {
        log.info("====This is the result of no algorithm===");
        try {
            tCruiseTaskResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
            switch (value){
                // 目前异常情况会出现的结果 后续再更新
                case "抓图失败":
                    tCruiseTaskResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_NOPIC));
                    break;
                case "机器人离线,未执行":
                    tCruiseTaskResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_OFFLINE));
                    break;
                case "机器人处于检修状态,未执行":
                case "设备检修中":
                    tCruiseTaskResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_OVERHAUL));
                    break;
                case "任务终止":
                    tCruiseTaskResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_INTERRUPT));
                    break;
                default:
                    tCruiseTaskResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                    tCruiseTaskResultMap.put("cruiseAbnormal", "null");
                    break;
            }
            tCruiseTaskResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
            tCruiseTaskResultMap.put("resultDesc", "--");
            tCruiseTaskResultMap.put("resultNum", StringUtils.isNotEmpty(value) ?
                    value : Objects.isNull(details.getAnalyseType()) || 13 == details.getAnalyseType() ? "已录音" : "已拍照");
            String str = PATROL_TASK_PREFIX + taskId + ":" + details.getInstanceId();
            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);

            String resultValue = tCruiseTaskResultMap.get("resultNum");

            int cruiseType = MapUtils.getIntValue(tCruiseTaskResultMap, "cruiseType");
            if (fileFound){
                if (TypeEnum.VOICE.getCode() != cruiseType) {
                    resultHandler.normalRecognitionHandler(resultValue, tCruiseTaskResultMap, null);
                } else {
                    // 声纹告警处理
                    resultHandler.voiceAlarmHandler(resultValue, tCruiseTaskResultMap);
                }
            }
            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);
            uPatrolTaskService.patrolTaskResultHandler(taskId, details.getInstanceId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 如果 ftpsTurbo 为 true，则表示设置了文件盘共享，不使用 ftps 对文件进行传输拷贝
     */
    public boolean ftpsTurbo() {
        boolean ftpsTurbo = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:ftpsTurbo", "content"));
        log.warn("ftpsTurbo is {}", ftpsTurbo);
        return ftpsTurbo;
    }

    public void computeEmpty(Map<String, String> map, String key, String value) {
        map.compute(key, (k, v) -> {
            if (CommonUtils.isEmptyOrNullstr(v)) {
                return value;
            }
            return v;
        });
    }
}
