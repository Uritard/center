package com.yjh.platform.module.patrol.thread;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskResult;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yjh.platform.common.utils.ImageSplitUtil.convertPath;
import static com.yjh.platform.module.patrol.CruiseConstant.*;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_SUMMARY_PREFIX;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * <功能描述> 简易机器人巡视结果处理线程
 *
 * @author shaobinfen
 * @date 2025/6/17
 * @since [产品/模块版本](可选)
 */
@SuppressWarnings("unchecked")
@Slf4j
public class SimpleInspectionResultThread implements Runnable {

    private final RedisTemplate redisTemplate;

    private final List<RobotPatrolTaskResult> resultList;

    private final PatrolResultHandler resultHandler;

    private final UPatrolTaskService uPatrolTaskService;

    private final TStdDeviceDao tStdDeviceDao;

    public SimpleInspectionResultThread(RedisTemplate redisTemplate, List<RobotPatrolTaskResult> resultList, PatrolResultHandler resultHandler) {
        this.redisTemplate = redisTemplate;
        this.resultList = resultList;
        this.resultHandler = resultHandler;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        this.tStdDeviceDao = StaticContextAccessor.getBean(TStdDeviceDao.class);
    }

    @Override
    public void run() {
        List<String> instanceList = new ArrayList<>();
        try {
            String taskId = "", inspectionId = "", absolutePath = "",convertPath = "", taskType = "" ;
            boolean result = false; //默认调用算法服务成功
            for (RobotPatrolTaskResult robotPatrolTaskResult : resultList) {
                log.info("开始处理巡检结果并对其标准化 >>>>>>> robotPatrolTaskResult==={}", JSON.toJSONString(robotPatrolTaskResult));
                Map<String, String> infoMap = new HashMap<>(8);
                resultHandler.infoMapInitialize(infoMap, robotPatrolTaskResult.getTaskCode(), robotPatrolTaskResult.getTaskPatrolledId());
                taskId = infoMap.get("taskId");
                taskType = String.valueOf(redisTemplate.opsForHash().get(PATROL_SUMMARY_PREFIX + taskId, "taskType")); ;
                TCruisePointInstance instance = resultHandler.obtainCruisePointInfoAndUpdate(robotPatrolTaskResult);
                String instanceId = ObjectUtils.isEmpty(instance.getInstanceId()) ? "" : String.valueOf(instance.getInstanceId());
                //906为SI300初始任务，传入的是deviceId，无法根据deviceId得到instance
                if (ObjectUtils.isEmpty(instanceId) && String.valueOf(INITIAL_PATROL).equals(taskType)) {
                    instanceId = robotPatrolTaskResult.getDeviceId();
                    //将地图保存在simplePic路径下
                    String simplePicPath = SysParamConfig.getSysContent("simplePicPath") + instanceId + "/base_img.jpg";
                    uPatrolTaskService.downloadPicture(simplePicPath, robotPatrolTaskResult.getFilePath());
                    convertPath = convertPath(simplePicPath, SysParamConfig.getSysContent("simplePicPath"), Constant.SIMPLE_PIC);
                }

                instanceList.add(instanceId);
                infoMap.put("instanceId", instanceId);
                // 文件处理
                resultHandler.resultFileHandler(robotPatrolTaskResult, infoMap, instance);
                absolutePath = infoMap.get("absolutePath");
                String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
                Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
                resultHandler.cruiseTaskResultInitialize(tCruiseTaskResultMap, infoMap, robotPatrolTaskResult, taskId, instance);
                String oldCruiseResult = tCruiseTaskResultMap.get("cruiseResult");
                //所有子点位的cruiseId相同
                inspectionId = tCruiseTaskResultMap.get("cruiseId");
                boolean isNormal = true;
                String cruiseAbnormal = "";
                String valid = robotPatrolTaskResult.getValid();
                if (StrUtil.isEmpty(valid)) {
                    cruiseAbnormal = "0";
                } else if (StrUtil.equals(valid, "0")) {
                    isNormal = false;
                    cruiseAbnormal = String.valueOf(CRUISE_ABNORMAL_NOPIC);
                } else if (StrUtil.equals(valid, "2")) {
                    isNormal = false;
                    cruiseAbnormal = String.valueOf(CRUISE_ABNORMAL_ANALYSEFAILED);
                }
                String cruiseResult = String.valueOf(isNormal ? CRUISE_RESULT_NORMAL : CRUISE_RESULT_ABNORMAL);
                if (resultHandler.whetherDiscarded(oldCruiseResult, cruiseResult)) {
                    log.info("本测点 {} 结果下级已经返回过并且正常，本次返回的结果不正确，本次结果不处理！", instanceId);
                    return;
                }
                tCruiseTaskResultMap.put("cruiseResult", cruiseResult);
                tCruiseTaskResultMap.put("cruiseAbnormal", cruiseAbnormal);
                resultHandler.updateCruiseStatus(tCruiseTaskResultMap);
                tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
                tCruiseTaskResultMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
                tCruiseTaskResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_ANALYSE_DOING));
                tCruiseTaskResultMap.put("resultDesc", AbnormalResDescEnum.ANALYSISING.getDesc());
                resultHandler.saveTaskResultAndNotify(tCruiseTaskResultMap, taskId, instanceId, redisKeyName);
                resultHandler.addCruiseAnalysisScore(taskId, instanceId);
            }
            //最后一个子点位处理完成开始调用算法识别服务
            AbstractVideoCruise abstractVideoCruise = AbstractVideoCruise.Factory.getVideoCruise(228);
            File file = new File(absolutePath);
            boolean fileFound = !file.isDirectory() && file.exists();
            if (fileFound && Constant.isHost() && !String.valueOf(INITIAL_PATROL).equals(taskType)) {
                Map<String, String> jsonForRe = new HashMap<>(4);
                jsonForRe.put("absPath", absolutePath);
                result = abstractVideoCruise.newAlgorithmAnalysis(inspectionId, taskId, jsonForRe);
            }
            for (int i = 0; i < resultList.size(); i++) {
                String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceList.get(i);
                Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
                if (result || String.valueOf(INITIAL_PATROL).equals(taskType)) {
                    TStdDevice tStdDevice = new TStdDevice().setDeviceId(Long.valueOf(instanceList.get(0))).setRegionPath(convertPath);
                    tStdDeviceDao.update(tStdDevice);
                    resultHandler.callFailedHandle(tCruiseTaskResultMap);
                    CruiseRedisStorage.offer(tCruiseTaskResultMap);
                    uPatrolTaskService.patrolTaskResultHandler(tCruiseTaskResultMap);
                } else {
                    tCruiseTaskResultMap.put("cruiseResult", "");
                    CruiseRedisStorage.offer(tCruiseTaskResultMap);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
