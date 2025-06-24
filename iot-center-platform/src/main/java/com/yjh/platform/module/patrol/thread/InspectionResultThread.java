package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.ResultConvertUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskResult;
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

    /**
     * 下级上报结果，成功
     */
    public static final String RESULT_VALID_SUCCESS = "1";
    /**
     * 下级上报结果，采集失败
     */
    public static final String RESULT_VALID_FAILED_COLLECTION = "0";
    /**
     * 下级上报结果，分析失败
     */
    public static final String RESULT_VALID_FAILED_ANALYSIS = "2";

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
            
            log.info("start subordinate task！！！ {}", taskId);
            subordinateTaskHandler(infoMap.getOrDefault("absolutePath", ""), taskId, robotPatrolTaskResult.getValue(), tCruiseTaskResultMap);
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
                case RESULT_VALID_SUCCESS:
                    break;
                //分析失败
                case RESULT_VALID_FAILED_ANALYSIS:
                    if (robotInfo.getApiType() == null || robotInfo.getApiType() == 2024){
                        //2024版本协议
                        isnormal = false;
                        cruiseAbnormal = StringUtils.isNotEmpty(cruiseAbnormal) ? cruiseAbnormal : String.valueOf(CRUISE_ABNORMAL_ANALYSEFAILED);
                    }
                    break;
                //采集失败
                case RESULT_VALID_FAILED_COLLECTION:
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

        resultHandler.updateCruiseStatus(tCruiseTaskResultMap);
        return resultHandler.whetherDiscarded(oldCruiseResult, cruiseResult);
    }

    /**
     * 下级上报的巡视结果处理
     *
     * @param originPath 巡视结果文件全路径
     * @param taskId     任务id
     * @param value 巡视结果值
     * @param taskResultMap 巡视结果缓存
     */
    private void subordinateTaskHandler(String originPath, String taskId, String value, Map<String, String> taskResultMap) {

        String sysLevel = Constant.getLevelEdge();
        String instanceId = MapUtils.getString(taskResultMap, "instanceId");

        // 当前为巡视系统 并且 巡视结果为空值 或者 配置需要算法识别的结果
        // 判断是否开启了强制分析功能，如果启用强制分析，则无论结果值是什么，都去判断是否调用算法，所有配置算法的无论下级上传是什么都会进行分析
        boolean resultAnalyse =
            StringUtils.isEmpty(value) || StringUtils.equalsAny(value, Constant.getNeedAnalyseResult())
                || SysParamConfig.getBooleanContent("forceAnalysis");
        boolean needAnalysis = Constant.isHost() && resultAnalyse;

        try {
            // 巡检点类型
            int cruiseType = MapUtils.getIntValue(taskResultMap, "cruiseType");

            taskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
            taskResultMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));

            AbstractVideoCruise abstractVideoCruise = AbstractVideoCruise.Factory.getVideoCruise(cruiseType);
            File file = new File(originPath);
            boolean fileFound = !file.isDirectory() && file.exists();

            // 判断是否有配置算法
            TAlgorithmMeteInfo algorithm = abstractVideoCruise.needAnalysis(MapUtils.getString(taskResultMap, "deviceMeteId"), value);
            log.info("algorithm:【{}】,cruiseType:【{}】,fileFound:【{}】,sysLevel:【{}】", algorithm, cruiseType, fileFound, sysLevel);
            // 调用算法的条件:配置了算法 + resultImg能找到文件 + 巡视主机 + 正常的点
            // 原来排除了声纹点位，增加了逻辑复杂度，取消针对声纹的特殊处理，全部根据是否配置了算法来判断
            if (algorithm != null && fileFound && Constant.isHost() && !needAnalysis) {
                Map<String, String> jsonForRe = new HashMap<>(4);
                jsonForRe.put("absPath", originPath);
                String preset = analyseDataOperateDao.selectPresetIdByInstanceId(instanceId);
                if (preset == null) {
                    preset = MapUtils.getString(taskResultMap, "devicePointId");
                }
                log.info("presetId====== {}", preset);
                boolean result = abstractVideoCruise.algorithmAnalysis(taskResultMap, preset, taskId, jsonForRe, algorithm);
                log.info("调用算法结果: {}", result);
                // 数据存入 redis
                CruiseRedisStorage.offer(taskResultMap);
                if (result) {
                    uPatrolTaskService.patrolTaskResultHandler(taskResultMap);
                }
            } else {
                updatePointStatusNum(taskResultMap, value, fileFound);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 未配置算法点位结果处理
     *
     * @param tCruiseTaskResultMap 巡视结果map
     * @param value                值
     * @param fileFound            文件是否能找到
     */
    private void updatePointStatusNum(Map<String, String> tCruiseTaskResultMap, String value, boolean fileFound) {
        log.info("====This is the result of no algorithm==={}", value);

        try {
            int cruiseType = MapUtils.getIntValue(tCruiseTaskResultMap, "cruiseType");
            if (TypeEnum.INFRARED.getCode() == cruiseType && StringUtils.equalsAny(value, Constant.getNeedAnalyseResult())) {
                // 如果是红外，且没有配置算法，并且结果是已拍照/拍照等需要识别字段，则num为-1，因为他需要调用算法获取结果或者直接得到结果
                tCruiseTaskResultMap.put("resultNum", CruiseConstant.FAILED_VALUE);
            }
            String resultValue = CommonUtils.defaultEmpty(tCruiseTaskResultMap.get("resultNum"));

            // 原来声纹不需要经过这一步处理的，但后来考虑了一下，声纹其实也可以走这一步处理，为了结果的一致性，声纹这里不做额外的排除
            // 结果值是正常的进行告警判断
            if (RESULT_VALID_SUCCESS.equals(robotPatrolTaskResult.getValid())) {
                // 正常结果告警处理
                resultHandler.normalRecognitionHandler(resultValue, tCruiseTaskResultMap, null);
            }
            CruiseRedisStorage.offer(tCruiseTaskResultMap);
            uPatrolTaskService.patrolTaskResultHandler(tCruiseTaskResultMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
