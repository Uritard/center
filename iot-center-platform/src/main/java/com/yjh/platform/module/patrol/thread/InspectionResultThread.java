package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.ResultConvertUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskAlarm;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskResult;
import com.yjh.platform.module.patrol.entity.TCruisePointInstanceDetail;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TAlgorithmMeteInfo;
import com.yjh.platform.module.user.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.yjh.platform.module.patrol.CruiseConstant.*;
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
    private final ApplicationEventPublisher eventPublisher;
    private final TRobotInfoDao tRobotInfoDao;

    public InspectionResultThread(RobotPatrolTaskResult robotPatrolTaskResult, Map<String, String> infoMap,
                                  TCruisePointInstance insInfo,
                                  RedisTemplate redisTemplate, boolean changeTaskStatus,
                                  ApplicationEventPublisher eventPublisher, PatrolResultHandler resultHandler){
        this.robotPatrolTaskResult = robotPatrolTaskResult;
        this.infoMap = infoMap;
        this.insInfo = insInfo;
        this.redisTemplate = redisTemplate;
        this.changeTaskStatus = changeTaskStatus;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        this.analyseDataOperateDao = StaticContextAccessor.getBean(AnalyseDataOperateDao.class);
        this.tRobotInfoDao = StaticContextAccessor.getBean(TRobotInfoDao.class);
        this.resultHandler = resultHandler;
        this.eventPublisher = eventPublisher;
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
            resultHandler.cruiseTaskResultInitialize(tCruiseTaskResultMap, infoMap, robotPatrolTaskResult, taskId, insInfo);
            // 巡视结果、异常原因、执行状态处理
            boolean res = setCruiseResult(tCruiseTaskResultMap, instanceId);
            if (res) {
                log.info("本测点 {} 结果下级已经返回过并且正常，本次返回的结果不正确，本次结果不处理！", instanceId);
                return;
            }
            resultHandler.saveTaskResultAndNotify(tCruiseTaskResultMap, taskId, instanceId, redisKeyName);
            // 是否为本级系统下发给下级系统的任务
            boolean flag = judgeTaskSourceHandler(taskId, instanceId, robotCode, tCruiseTaskResultMap.get("cruiseType"), robotPatrolTaskResult.getValue());
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
    private boolean setCruiseResult(Map<String, String> tCruiseTaskResultMap, String instanceId) {
        String taskId = infoMap.get("taskId");
        String oldCruiseResult = tCruiseTaskResultMap.get("cruiseResult");

        boolean isnormal = true;
        String sendCode = robotPatrolTaskResult.getSendCode();
        Integer countRegion = uPatrolTaskService.getCruiseDeviceInfo(sendCode);
        String robotCode = sendCode;
        if (countRegion != 0) {
            robotCode = uPatrolTaskService.selectRobotCodeByInstanceId(Long.valueOf(instanceId));
        }
        TRobotInfo robotInfo = tRobotInfoDao.selectByRobotCode(robotCode);
        String val = robotPatrolTaskResult.getValue();
        //D200局放 单独处理结果
        if (ResultConvertUtil.FIFTY.equals(robotPatrolTaskResult.getFileType()) && tCruiseTaskResultMap.get("origpic").contains(".txt")) {
            Pair<String, String> result = ResultConvertUtil.dealJudgment(tCruiseTaskResultMap.get("origpic"), val, tCruiseTaskResultMap.getOrDefault("unit", ""));
            tCruiseTaskResultMap.put("resultNum", result.getKey());
            tCruiseTaskResultMap.put("resultDesc", result.getValue());
        } else {
            tCruiseTaskResultMap.put("resultNum", ResultConvertUtil.convertResult(val));
            tCruiseTaskResultMap.put("resultDesc", ResultConvertUtil.convertDesc(val, tCruiseTaskResultMap.getOrDefault("unit", "")));
        }
        log.info("taskId is {},instanceId is {},the result is normal", taskId, instanceId);
        String cruiseResult;
        String cruiseAbnormal = "";

        String valid = robotPatrolTaskResult.getValid();
        if (StringUtils.isNotEmpty(robotPatrolTaskResult.getAbnormalType())) {
            cruiseAbnormal = robotPatrolTaskResult.getAbnormalType();
        }
        if (StringUtils.isNotEmpty(valid)) {
            switch (valid) {
                //成功
                case "1":
                    break;
                //分析失败
                case "2":
                    if (robotInfo.getApiType() == null || robotInfo.getApiType() == 2024){
                        //2024版本协议
                        isnormal = false;
                        cruiseAbnormal = StringUtils.isNotEmpty(cruiseAbnormal) ? cruiseAbnormal : String.valueOf(CRUISE_ABNORMAL_ANALYSEFAILED);
                    }
                    break;
                //采集失败
                case "0":
                default:
                    isnormal = false;
                    if (robotInfo.getApiType() == null || robotInfo.getApiType() == 2024){
                        //2024版本协议
                        cruiseAbnormal = StringUtils.isNotEmpty(cruiseAbnormal) ? cruiseAbnormal : String.valueOf(CRUISE_ABNORMAL_NOPIC);
                    } else {
                        cruiseAbnormal = StringUtils.isNotEmpty(cruiseAbnormal) ? cruiseAbnormal : String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL);
                    }
                    break;
            }
        } else {
            cruiseAbnormal = "0";
        }
        cruiseResult = String.valueOf(isnormal ? CRUISE_RESULT_NORMAL : CRUISE_RESULT_ABNORMAL);
        if (!isnormal) {
            tCruiseTaskResultMap.put("resultNum", "-1");
            tCruiseTaskResultMap.put("resultDesc", val);
        }

        tCruiseTaskResultMap.put("cruiseResult", cruiseResult);
        tCruiseTaskResultMap.put("cruiseAbnormal", cruiseAbnormal);
//        computeEmpty(tCruiseTaskResultMap, "cruiseResult", cruiseResult);
//        computeEmpty(tCruiseTaskResultMap, "cruiseAbnormal", cruiseAbnormal);
        resultHandler.updateCruiseStatus(tCruiseTaskResultMap);
        return resultHandler.whetherDiscarded(oldCruiseResult, cruiseResult);
    }

    /**
     * 判断任务结果来源
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @param sendCode 下级唯一标识
     * @return boolean
     */
    private boolean judgeTaskSourceHandler(String taskId, String instanceId, String sendCode, String cruiseType, String value) {
        try {
            String sysLevel = Constant.getLevelEdge();

            //当前为巡视系统 并且 巡视结果为空值 或者 配置需要算法识别的结果
            boolean resultAnalyse = StringUtils.isEmpty(value) || StringUtils.equalsAny(value, Constant.getNeedAnalyseResult());
            boolean needAnalysis = Constant.isHost() && resultAnalyse;
            log.info("simulation tool flag, needAnalysis: {}, taskId: {}, sysLevel: {}, cruiseType: {}", needAnalysis, taskId, sysLevel, cruiseType);
            if (Boolean.FALSE.equals(needAnalysis)) {
                //不需要算法处理的->非同源 ->结果处理
                if (!Constant.fastTurbo()) {
                    RobotPatrolTaskAlarm taskAlarm = new RobotPatrolTaskAlarm();
                    taskAlarm.setTaskCode(taskId);
                    value = ResultConvertUtil.convertResult(value);
                    if ("0".equals(robotPatrolTaskResult.getValid())) {
                        value = CruiseConstant.FAILED_VALUE;
                    }
                    taskAlarm.setValue(value);
                    taskAlarm.setValueUnit(robotPatrolTaskResult.getValue());
                    taskAlarm.setDeviceId(instanceId);
                    NonhomologousWarnThread nonhomologousWarnThread = new NonhomologousWarnThread(taskAlarm, redisTemplate, 1);
                    ThreadPoolUtil.PATROL_POOL.addThread(nonhomologousWarnThread);
                }

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
            TCruisePointInstanceDetail details = uPatrolTaskService.selectForTask(Long.valueOf(instanceId));

            Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
            // 巡检点类型
            int cruiseType = MapUtils.getIntValue(tCruiseTaskResultMap, "cruiseType");

            tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
            tCruiseTaskResultMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));

            AbstractVideoCruise abstractVideoCruise = AbstractVideoCruise.Factory.getVideoCruise(cruiseType);
            File file = new File(originPath);
            boolean fileFound = !file.isDirectory() && file.exists();

            // 判断是否有配置算法
            TAlgorithmMeteInfo algorithm = abstractVideoCruise.needAnalysis(String.valueOf(details.getDeviceMeteId()), value);
            String sysLevel = Constant.getLevelEdge();
            boolean isInterrupt = "0".equals(robotPatrolTaskResult.getValid());
            log.info("algorithm:【{}】,cruiseType:【{}】,fileFound:【{}】,sysLevel:【{}】,isInterrupt:【{}】",
                    algorithm, cruiseType, fileFound, sysLevel, isInterrupt);
            // 调用算法的条件:配置了算法 + 非声纹的点 + resultImg能找到文件 + 巡视主机 + 正常的点
            if (algorithm != null && TypeEnum.VOICE.getCode() != cruiseType && fileFound && Constant.isHost() && !isInterrupt) {
                Map<String, String> jsonForRe = new HashMap<>(4);
                jsonForRe.put("absPath", originPath);
                String preset = analyseDataOperateDao.selectPresetIdByInstanceId(instanceId);
                if (preset == null){
                    preset = details.getDevicePointId();
                }
                log.info("presetId====== {}",preset);
                boolean result = abstractVideoCruise.algorithmAnalysis(tCruiseTaskResultMap, preset, taskId, jsonForRe, algorithm);
                log.info("调用算法结果: {}", result);
                // 数据存入 redis
                CruiseRedisStorage.offer(tCruiseTaskResultMap);
                if (result) {
                    uPatrolTaskService.patrolTaskResultHandler(tCruiseTaskResultMap);
                }
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
        log.info("====This is the result of no algorithm==={}", value);
        try {
            int cruiseType = MapUtils.getIntValue(tCruiseTaskResultMap, "cruiseType");
            if (TypeEnum.INFRARED.getCode() == cruiseType && "已拍照".equals(value)) {
                // 如果是红外，且没有配置算法，并且结果是已拍照，则num为-1，因为他需要调用算法获取结果或者直接得到结果
                tCruiseTaskResultMap.put("resultNum", "-1");
            }
            String resultValue = CommonUtils.defaultEmpty(tCruiseTaskResultMap.get("resultNum"));

            if (fileFound){
                if (TypeEnum.VOICE.getCode() != cruiseType) {
                    resultHandler.normalRecognitionHandler(resultValue, tCruiseTaskResultMap, null);
                // } else {
                //     // 声纹告警处理
                //     resultHandler.voiceAlarmHandler(resultValue, tCruiseTaskResultMap);
                }
            }
            String str = PATROL_TASK_PREFIX + taskId + ":" + details.getInstanceId();
            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);
            uPatrolTaskService.patrolTaskResultHandler(tCruiseTaskResultMap);
            //todo 告警处理
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
