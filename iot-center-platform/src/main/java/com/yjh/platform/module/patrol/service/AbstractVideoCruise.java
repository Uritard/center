/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.service.impl.InfraredVideoCruiseExecuteImpl;
import com.yjh.platform.module.patrol.service.impl.NormalVideoCruiseExecuteImpl;
import com.yjh.platform.module.patrol.thread.CruiseRedisStorage;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
import com.yjh.platform.module.user.entity.TAlgorithmMeteInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static com.yjh.platform.module.patrol.CruiseConstant.*;
import static com.yjh.platform.module.patrol.CruiseConstant.AnalyticsEnum.HTTP;
import static com.yjh.platform.module.patrol.CruiseConstant.AnalyticsEnum.TCP;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/19
 * @since [产品/模块版本] （可选）
 */
public abstract class AbstractVideoCruise {
    Logger log = LoggerFactory.getLogger(AbstractVideoCruise.class);

    /**
     * 相机抓图
     */
    public static final String PICTURE_URL =
        "http://iot-center-accessvideo/camera/v1/capturePictureForTask?cameraId={cameraId}&meteName={meteName}";
    /**
     * 相机转到预置位
     */
    public static final String MOVE_URL =
        "http://iot-center-accessvideo/camera/v1/moveToPresetForTask?presetId={presetId}&cameraId={cameraId}";
    /**
     * 算法接口
     */
    public static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    /**
     * 缺陷接口
     */
    public static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";
    /**
     * 机器人任务路径
     */
    public static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/taskIssued";

    /**
     * 红外相机拍图
     */
    public static final String RED_MOVE_URL =
        "http://iot-center-accessvideo/camera/v1/givePicFir?presetId={presetId}&cameraId={cameraId}&meteName={meteName}";
    private final TAlgorithmInfoDao tAlgorithmInfoDao;
    private final RedisTemplate<String, ?> redisTemplate;
    protected final RestTemplate serviceRestTemplate;
    protected final PatrolResultHandler patrolResultHandler;
    private final HashOperations<String, String, String> hashOperations;

    private static String picModelPath;
    private static boolean intelDefectAnalysis;
    private static boolean intelAlgorithmAnalysis;
    protected static long waitTime = 10000;
    private static String presetImgPath;

    protected AbstractVideoCruise(TAlgorithmInfoDao tAlgorithmInfoDao, RedisTemplate<String, ?> redisTemplate,
        RestTemplate serviceRestTemplate, PatrolResultHandler patrolResultHandler) {
        this.tAlgorithmInfoDao = tAlgorithmInfoDao;
        this.redisTemplate = redisTemplate;
        this.serviceRestTemplate = serviceRestTemplate;
        this.patrolResultHandler = patrolResultHandler;
        this.hashOperations = redisTemplate.opsForHash();

        resetParams();
    }

    public void resetParams() {
        //模板图片路径
        picModelPath = hashOperations.get("t_sys_param:picModelPath", "content");
        // 预置位图片路径
        presetImgPath = hashOperations.get("t_sys_param:presetImgPath", "content");
        // 缺陷是否使用分析主机
        intelDefectAnalysis = Boolean.parseBoolean(hashOperations.get("t_sys_param:isIntelDefectAnalysis", "content"));
        // 表计使用分析主机
        intelAlgorithmAnalysis = Boolean.parseBoolean(hashOperations.get("t_sys_param:isIntelAlgorithmAnalysis", "content"));
        waitTime = NumberUtils.toLong(hashOperations.get("t_sys_param:waitTime", "content"), waitTime);
    }

    protected boolean videoExecute(Map<String, String> inspectionMap) {
        long presetId = MapUtils.getLongValue(inspectionMap, "cruiseId");
        String cameraId = inspectionMap.get("cameraId");
        String presetName = inspectionMap.get("cruiseName");
        String taskName = inspectionMap.get("taskName");
        String taskId = inspectionMap.get("taskId");

        String dateTime = DateTimeUtil.getDateTimeString();
        // inspectionMap.put("startTime",simpleDateFormat.format(date));
        inspectionMap.put("createtime", dateTime);
        inspectionMap.put("cruiseTime", dateTime);

        if (presetId > 0 && StringUtils.isNotEmpty(cameraId)) {

            Result re = null;
            boolean waitFlag = waitCamera2(taskId, cameraId, presetName);
            if (waitFlag) {
                try {
                    //1.转到预置位
                    Map<String, Object> moveMap = new HashMap<>();
                    moveMap.put("presetId", presetId);
                    moveMap.put("cameraId", cameraId);
                    log.info("params for preset: {}", JSON.toJSONString(moveMap));
                    // 移动相机
                    moveWait(moveMap);

                    String instanceName = inspectionMap.get("instanceName");

                    HashMap<String, Object> captureMap = new HashMap<>();
                    captureMap.put("presetId", presetId);
                    captureMap.put("cameraId", cameraId);
                    captureMap.put("meteName", instanceName);
                    //2.抓图
                    re = capture(captureMap);
                } catch (Exception e) {
                    log.error("设置摄像机状态出错", e);
                } finally {
                    redisTemplate.opsForHash().put("camera_info:" + cameraId, "state", "0");
                }
            }

            log.info("capture result: {}", JSON.toJSONString(re));
            String nowTime = DateTimeUtil.getDateTimeString();
            try {
                boolean isEnded = true;
                // 抓图失败处理
                boolean picError = re == null || !"success".equals(re.getMessage());
                JSONObject jsonForRe = null;
                String resultNum = "已拍照";

                if (picError) {
                    inspectionMap.put("resultNum", "-1");
                    inspectionMap.put("resultDesc", AbnormalResDescEnum.CAPTURE_FAILURE.getDesc());
                    inspectionMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_NOPIC));
                    // 巡视结果，异常
                    inspectionMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                    // 未审核
                    inspectionMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
                    inspectionMap.put("picpath", "--");
                    // 巡检数据状态，执行失败
                    inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_FAILED));
                    inspectionMap.put("cruiseTime", nowTime);
                } else {
                    jsonForRe = (JSONObject)JSONObject.toJSON(re.getData());
                    String urlPath = jsonForRe.getString("urlPath");
                    String absPath = jsonForRe.getString("absPath");
                    if (StringUtils.isNotEmpty(jsonForRe.getString("resultNum"))) {
                        resultNum = jsonForRe.getString("resultNum");
                    }

                    // 拍照结果处理
                    inspectionMap.put("resultNum", "-1");
                    inspectionMap.put("resultDesc", resultNum);
                    // 巡视结果，正常
                    inspectionMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                    // 未审核
                    inspectionMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
                    inspectionMap.put("picpath", urlPath);
                    inspectionMap.put("origpic", absPath);
                    // 巡检数据状态，已经执行
                    inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
                    inspectionMap.put("endTime", nowTime);
                    inspectionMap.put("cruiseTime", nowTime);
                }

                // 判断当前节点级别,如果是边缘节点,直接处理拍照的结果
                if (!picError && !Constant.isEdge()) {
                    // 判断是否有配置算法
                    TAlgorithmMeteInfo algorithm = needAnalysis(inspectionMap.getOrDefault("deviceMeteId", "-1"), resultNum);
                    if (algorithm != null) {
                        log.info("request algorithm: {}", JSON.toJSONString(algorithm));
                        // 算法分析
                        isEnded = algorithmAnalysis(inspectionMap, String.valueOf(presetId), taskId, jsonForRe, algorithm);
                    } else {
                        // 如果不进行算法处理，则本级处理结果信息
                        inspectionMap.put("resultNum", resultNum);
                        resultRecognition(inspectionMap);
                    }
                }
                // 数据存入 redis
                CruiseRedisStorage.offer(inspectionMap);
                return isEnded;
            } catch (Exception e) {
                log.error("摄像机处理出错", e);
                // 数据存入 redis
                CruiseRedisStorage.offer(inspectionMap);
            }
        }
        return true;
    }

    public TAlgorithmMeteInfo needAnalysis(String deviceMeteId) {
        return needAnalysis(deviceMeteId, "已拍照");

    }

    public TAlgorithmMeteInfo needAnalysis(String deviceMeteId, String resultNum) {
        List<TAlgorithmMeteInfo> algorithmList = tAlgorithmInfoDao.selectAlgorithmMete(deviceMeteId);
        // 配置了算法
        boolean isAnalyse = CollectionUtils.isNotEmpty(algorithmList) && (algorithmList.get(0).getMeteAnalyse() != null || "on".equals(
            algorithmList.get(0).getMeteAi()) || "on".equals(algorithmList.get(0).getMeteJudge()));

        if (isAnalyse) {
            return algorithmList.get(0);
        } else {
            return null;
        }

    }

    /**
     * 算法分析
     */
    public boolean algorithmAnalysis(Map<String, String> inspectionMap, String presetId, String taskId, JSONObject jsonForRe,
        TAlgorithmMeteInfo algorithm) {

        try {
            Analysis analysis = new Analysis();
            analysis.setTaskId(taskId);
            analysis.setInstanceId(MapUtils.getLong(inspectionMap, "instanceId"));
            analysis.setPicPath(jsonForRe.getString("absPath"));

            analysis.setPicModelPath(picModelPath + "/" + presetId);
            // 判别该点为本级系统的点还是下级系统的
            String edgeCode = tAlgorithmInfoDao.selectEdgeCodeByInstanceId(analysis.getInstanceId());
            if (StringUtils.isNotEmpty(edgeCode)) {
                String stationId = String.valueOf(redisTemplate.opsForHash().entries("region:" + edgeCode).get("stationId"));
                analysis.setReferenceImage(presetImgPath + "/" + stationId + "/" + presetId + "/" + presetId + ".jpg");
            }else {
                analysis.setReferenceImage(presetImgPath + "/" + presetId + "/" + presetId + ".jpg");
            }

            // 调用算法中
            inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_ANALYSE_DOING));
            inspectionMap.put("resultDesc", AbnormalResDescEnum.ANALYSISING.getDesc());
            // 表计
            if (StringUtils.isNotEmpty(algorithm.getMeteAnalyse())) {
                analysis.setAnalyseType(algorithm.getMeteAnalyse());
            } else if ("on".equals(algorithm.getMeteJudge())) {
                // 判别
                analysis.setAnalyseType("11");
            } else {
                analysis.setAnalyseType("398");
            }
            int isAi = algorithm.getIsAi() == null ? 0 : algorithm.getIsAi();
            analysis.setIsAi(isAi);
            analysis.setDevicePointId(algorithm.getDevicePointId());
            List<Analysis> analysisList = new ArrayList<>();
            analysisList.add(analysis);
            log.info("算法信息：   {}", JSON.toJSONString(analysisList));
            // analyseType：11-判别 398-缺陷 其他-表计
            Result result;
            if (StringUtils.equals("11", analysis.getAnalyseType()) || StringUtils.equals("398", analysis.getAnalyseType())) {
                result = defect(analysisList);
            } else {
                // 算法额外参数设置，红外
                analysisExt(analysis, jsonForRe);
                result = analysis(analysisList);
            }
            log.info("调用算法：   {}\n=========={}", JSON.toJSONString(analysisList), JSON.toJSONString(result));

            if (200 == result.getCode()) {
                // 算法调用正常则将巡视结果置为空
                inspectionMap.put("cruiseResult", "");
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 拍照结果处理
        inspectionMap.put("resultNum", "-1");
        // 巡视结果，异常
        inspectionMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
        // 调用算法失败
        inspectionMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_REQUESTFAILED));
        // 异常值，调用算法失败
        inspectionMap.put("resultDesc", AbnormalResDescEnum.ANALYSE_REQFAILED.getDesc());
        // 巡检数据状态，已经执行
        inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
        return true;
    }

    protected boolean waitCamera(String taskName, String cameraId, String presetName) {

        Map<String, String> mapForCameraState = hashOperations.entries("camera_info:" + cameraId);
        int cameraState = Integer.parseInt(mapForCameraState.get("state"));
        boolean waitFlag = true;
        if (cameraState == 1) {//摄像头在任务中
            int waitCount = 0;
            while (cameraState == 1) {
                try {
                    Thread.sleep(waitTime + 1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    break;
                }
                log.info("任务：{} 在 {} 时已经等待了 {} 预置位,摄像机Id {} {}s", taskName, DateTimeUtil.getDateTimeString(), cameraId, presetName,
                    ((waitCount + 1) * waitTime + 1000) / 1000);
                Map<String, String> mapForCameraStateForGet = hashOperations.entries("camera_info:" + cameraId);
                cameraState = Integer.parseInt(mapForCameraStateForGet.get("state"));
                waitCount = waitCount + 1;
                if (waitCount == 30) {
                    log.info("任务：{} 已经等待了 {} 秒,仍未等待到 {} 预置位,摄像机Id {} 退出等待", taskName, ((waitCount + 1) * waitTime + 1000) / 1000,
                        presetName, cameraId);
                    waitFlag = false;
                    break;
                }
            }
        }
        if (waitFlag) {
            redisTemplate.opsForHash().put("camera_info:" + cameraId, "state", "1");
        }
        return waitFlag;
    }

    protected boolean waitCamera2(String taskId, String cameraId, String presetName) {

        String script =
            "if redis.call('hget', KEYS[1], KEYS[2]) == ARGV[1] then return redis.call('hset', KEYS[1], KEYS[2], ARGV[2]) else return -1 end";

        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long cameraState = redisTemplate.execute(redisScript, Arrays.asList("camera_info:" + cameraId, "state"), 0, 1);
        cameraState = cameraState == null ? -1 : cameraState;
        boolean waitFlag = true;
        if (cameraState == -1) {
            //摄像头在任务中
            int waitCount = 0;
            while (cameraState == -1) {
                try {
                    Thread.sleep(waitTime + 1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    break;
                }
                log.info("任务：【{}】 在 【{}】 时已经等待了 【{}】 预置位,摄像机Id 【{}】 【{}】s", taskId, DateTimeUtil.getDateTimeString(), presetName,
                    cameraId, ((waitCount + 1) * waitTime + 1000) / 1000);
                cameraState = redisTemplate.execute(redisScript, Arrays.asList("camera_info:" + cameraId, "state"), 0, 1);
                cameraState = cameraState == null ? -1 : cameraState;

                waitCount = waitCount + 1;
                if (waitCount == 30) {
                    log.info("任务：【{}】 已经等待了 【{}】 秒,仍未等待到 【{}】 预置位,摄像机Id 【{}】 退出等待", taskId, ((waitCount + 1) * waitTime + 1000) / 1000,
                        presetName, cameraId);
                    waitFlag = false;
                    break;
                }
            }
        }
        return waitFlag;

    }

    /**
     * 表计 算法识别
     */
    public Result analysis(List<Analysis> analysisList) {
        AnalyticsEnum analytics = getAnalytics(false);
        return AnalyticsFactory.getAnalytics(analytics).analytics(analysisList);
    }

    /**
     * 缺陷/判别 算法识别
     */
    public Result defect(List<Analysis> analysisList) {
        AnalyticsEnum analytics = getAnalytics(true);
        return AnalyticsFactory.getAnalytics(analytics).defect(analysisList);
    }

    private AnalyticsEnum getAnalytics(boolean isDefect) {
        boolean isHttp = (isDefect && intelDefectAnalysis) || (!isDefect && intelAlgorithmAnalysis);
        return isHttp ? HTTP : TCP;
    }


    /**
     * 相机转到预置位
     *
     * @param map 相机参数
     */
    protected abstract void moveWait(Map<String, Object> map);

    /**
     * 相机抓图
     *
     * @param map 抓图参数
     * @return 抓图结果
     */
    protected abstract Result capture(Map<String, Object> map);

    /**
     * 算法分析额外信息处理
     *
     * @param analysis      调用算法信息
     * @param captureResult 抓图返回结果
     */
    protected abstract void analysisExt(Analysis analysis, JSONObject captureResult);

    protected abstract Map<String, String> resultRecognition(Map<String, String> inspectionMap);

    public static class AnalyticsFactory {
        private static final Map<CruiseConstant.AnalyticsEnum, AnalyticsService> ANALYTICS_SERVICE_MAP = new EnumMap<>(CruiseConstant.AnalyticsEnum.class);

        private AnalyticsFactory(){
            // nothing
        }

        public static AnalyticsService getAnalytics(CruiseConstant.AnalyticsEnum anayEnum) {

            return ANALYTICS_SERVICE_MAP.get(anayEnum);
        }

        public static void registerAnalytics(CruiseConstant.AnalyticsEnum analyticsEnum, AnalyticsService service) {
            ANALYTICS_SERVICE_MAP.put(analyticsEnum, service);
        }
    }

    public static class Factory {
        private Factory(){
            // nothing
        }

        public static AbstractVideoCruise getVideoCruise(CruiseConstant.TypeEnum cruiseType) {
            AbstractVideoCruise videoCruise;
            switch (cruiseType){
                case VIDEO:
                    videoCruise = StaticContextAccessor.getBean(NormalVideoCruiseExecuteImpl.class);
                    break;
                case INFRARED:
                default:
                    videoCruise = StaticContextAccessor.getBean(InfraredVideoCruiseExecuteImpl.class);
                    break;
            }
            return videoCruise;
        }

        public static AbstractVideoCruise getVideoCruise(int cruiseType) {
            CruiseConstant.TypeEnum cruiseTypeEnum = TypeEnum.getEnum(cruiseType);

            return getVideoCruise(cruiseTypeEnum);
        }
    }
}
