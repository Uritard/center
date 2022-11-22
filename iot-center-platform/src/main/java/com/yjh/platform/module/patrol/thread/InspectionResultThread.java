package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskResult;
import com.yjh.platform.module.patrol.entity.TCruisePointInstanceDetail;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.patrol.service.impl.NormalVideoCruiseExecuteImpl;
import com.yjh.platform.module.user.entity.TAlgorithmMeteInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

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
    private final UPatrolTaskService uPatrolTaskService;
    private final AbstractVideoCruise abstractVideoCruise;

    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;

    @Autowired
    private UPatrolResultDao uPatrolResultDao;

    @Autowired
    private AnalyseDataOperateDao analyseDataOperateDao;
    public InspectionResultThread(RobotPatrolTaskResult robotPatrolTaskResult, Map<String, String> infoMap,
                                  RedisTemplate redisTemplate, boolean changeTaskStatus){
        this.robotPatrolTaskResult = robotPatrolTaskResult;
        this.infoMap = infoMap;
        this.redisTemplate = redisTemplate;
        this.changeTaskStatus = changeTaskStatus;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        this.abstractVideoCruise = StaticContextAccessor.getBean(NormalVideoCruiseExecuteImpl.class);
    }

    @Override
    public void run(){

        try {
            log.info("开始处理巡检结果并对其标准化 >>>>>>> robotPatrolTaskResult==={}", JSON.toJSONString(robotPatrolTaskResult));
            String taskId = infoMap.get("taskId");
            String edgeCode = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeCode").get("content"));
            String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"
            ));
            if ("2".equals(sysLevel) && (tRobotInspectionDao.selectRobotCount(edgeCode) > 0 || tRobotInspectionDao.selectRegion(edgeCode) > 0) || "3".equals(sysLevel)) {
                upSystemDealWith(taskId);
                return;
            }
            String robotCode = robotPatrolTaskResult.getSendCode();

            // taskId是巡视主机的id,robotTaskId是机器人上报的id
            String robotTaskId = infoMap.get("taskCode");
            log.info("robotTaskId==={}", robotTaskId);

            UPatrolTask uPatrolTaskTemp = uPatrolTaskService.selectTaskByTaskCode(robotTaskId);
            log.info("uPatrolTaskTemp=={}", uPatrolTaskTemp);
            if (Objects.nonNull(uPatrolTaskTemp) && StringUtils.isNotEmpty(uPatrolTaskTemp.getDateType())){
                boolean moreTime = uPatrolTaskTemp.getDateType().split(" ")[2].contains(",");
                if (moreTime) {
                    robotTaskId = taskId;
                }
            }
            log.info("robotTaskId=={}", robotTaskId);

            String redisKey = "Robot_SPAndIN_Info:" + robotCode + ":" + robotTaskId + ":" + robotPatrolTaskResult.getDeviceId();
            Map<String,String> robotInfoKeyMap = redisTemplate.opsForHash().entries(redisKey);
            String instanceId = robotInfoKeyMap.get("instanceId");

            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
            tCruiseTaskResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));

            if (StringUtils.isNotEmpty(robotPatrolTaskResult.getValue())) {
                tCruiseTaskResultMap.put("resultNum", robotPatrolTaskResult.getValue());
                tCruiseTaskResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                tCruiseTaskResultMap.put("cruiseAbnormal", "--");
                log.info("taskId is {},instanceId is {},the result is normal", taskId, instanceId);
            }else {
                // value无值且resultNum为--，若结果非音频文件，则为异常情况
                tCruiseTaskResultMap.put("resultNum", "--");
                if ("3".equals(robotPatrolTaskResult.getFileType())){
                    tCruiseTaskResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                    tCruiseTaskResultMap.put("cruiseAbnormal", "--");
                    log.info("taskId is {},instanceId is {},the result is normal", taskId, instanceId);
                }else {
                    tCruiseTaskResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                    tCruiseTaskResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL));
                    log.info("taskId is {},instanceId is {},the result is abnormal", taskId, instanceId);
                }
            }
            tCruiseTaskResultMap.put("picpath", infoMap.get("relativePath"));
            tCruiseTaskResultMap.put("origpic", infoMap.containsKey("absolutePath") ? infoMap.get("absolutePath") : "");
            tCruiseTaskResultMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
            tCruiseTaskResultMap.put("isWarn", "0");
            tCruiseTaskResultMap.put("recognitionType", robotPatrolTaskResult.getRecognitionType());
            tCruiseTaskResultMap.put("fileType", robotPatrolTaskResult.getFileType());
            tCruiseTaskResultMap.put("rectangle", robotPatrolTaskResult.getRectangle());
            tCruiseTaskResultMap.put("confidence", "");
            redisTemplate.opsForHash().putAll(redisKeyName, tCruiseTaskResultMap);

            // 巡视主机下发的任务或者站端本体任务
            boolean flag = judgeTaskSourceHandler(taskId, instanceId, robotCode);
            if (!flag) {
                log.info("This is simulation tool task！！！");
                simulationToolTaskHandler(infoMap.get("absolutePath"), taskId, instanceId, robotPatrolTaskResult.getValue(), robotPatrolTaskResult.getFilePath());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void upSystemDealWith(String taskId) {

        String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + robotPatrolTaskResult.getDeviceId();

        Map<String, String> map = new HashMap<>(8);
        if (StringUtils.isNotEmpty(robotPatrolTaskResult.getValue())) {
            map.put("resultNum", robotPatrolTaskResult.getValue());
            map.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
            map.put("cruiseAbnormal", "--");
        } else {
            // value无值且resultNum为--，若结果非音频文件，则为异常情况
            map.put("resultNum", "--");
            if ("3".equals(robotPatrolTaskResult.getFileType())) {
                map.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                map.put("cruiseAbnormal", "--");
            } else {
                map.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                map.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL));
            }
        }
        map.put("picPathAnl", "");
        map.put("cruiseType", "");
        map.put("instanceName", "");
        map.put("remark", "");
        map.put("deviceId", analyseDataOperateDao.selectPatrolDevice(robotPatrolTaskResult.getDeviceId()).get(
                "deviceId"));
        map.put("deviceName", robotPatrolTaskResult.getPatrolDeviceName());
        map.put("points", "");
        map.put("serialVersionUID", "");
        map.put("origConfirmPicPath", "");
        map.put("firDate", "");
        map.put("instanceId", robotPatrolTaskResult.getDeviceId());
        map.put("confirmPicPath", "");
        map.put("modifyNum", "");
        map.put("origPicAnl", "");
        map.put("identifyResult", "");
        map.put("personCheck", "");
        map.put("createtime", "");
        map.put("identifyState", "");
        map.put("cruiseDataId", "");
        map.put("resultPic", "");
        map.put("cruiseName", "");
        map.put("firName", "");
        map.put("resultDesc", "");
        map.put("checkDate", "");
        map.put("cruiseTime", robotPatrolTaskResult.getTime());
        map.put("cruiseId", "");
        map.put("checkUser", "");
        map.put("cameraId", "");
        map.put("voicePath", "");
        map.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
        map.put("taskId", taskId);
        map.put("picpath", infoMap.get("relativePath"));
        map.put("origpic", infoMap.containsKey("absolutePath") ? infoMap.get("absolutePath") : "");
        map.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
        map.put("isWarn", "0");
        map.put("recognitionType", robotPatrolTaskResult.getRecognitionType());
        map.put("fileType", robotPatrolTaskResult.getFileType());
        map.put("rectangle", robotPatrolTaskResult.getRectangle());
        map.put("confidence", "");
        redisTemplate.opsForHash().putAll(redisKeyName, map);
    }

    /**
     * 判断任务是否为巡视主机下发的任务
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @param robotCode 机器人/无人机编码
     * @return boolean
     */
    private boolean judgeTaskSourceHandler(String taskId, String instanceId, String robotCode) {
        try {
            Integer robotType = uPatrolTaskService.selectRobotType(robotPatrolTaskResult.getSendCode());
            // 巡视结果只有file_path字段,没有origin_file_result_path和origin_file_path 为模拟工具
            boolean isSimulationTool = StringUtils.isNotEmpty(robotPatrolTaskResult.getFilePath())
                    && StringUtils.isEmpty(robotPatrolTaskResult.getOriginFileResultPath())
                    && StringUtils.isEmpty(robotPatrolTaskResult.getOriginFilePath())
                    // 且是E机器人
                    && Objects.equals(159, robotType);
            UPatrolTask uPatrolTask = uPatrolTaskService.selectByPrimaryId(taskId);
            boolean isSelfTask = Objects.equals(110, uPatrolTask.getTaskSource());
            if (!isSimulationTool) {
                Integer flag = uPatrolTaskService.selectIsAlarmByTask(taskId, instanceId);
                if (flag > 0) {
                    log.info("taskId为{}巡视点instanceId为{}的点位产生了告警,需要更新图片", taskId, instanceId);
                    uPatrolTaskService.updatePicPath(taskId, instanceId, infoMap.get("relativePath"));
                }
                uPatrolTaskService.patrolTaskResultHandler(taskId, Long.valueOf(instanceId));
                // 区分是否为站端本体任务如果是站端本体任务,需更新任务状态
                if (isSelfTask) {
                    standTaskDealHandler(taskId, robotCode);
                }
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 更新站端本体任务状态
     * @param taskId 任务id
     * @param robotCode 机器人/无人机编码
     */
    private void standTaskDealHandler(String taskId, String robotCode){
        try {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
            log.info("redisInfoMap=={}", redisInfoMap);
            Integer taskState = Integer.valueOf(String.valueOf(redisInfoMap.get("taskState")));
            if (Objects.isNull(taskState)){
                return;
            }
            // 238-任务未开始,239-正在执行,240-已执行,241-任务暂停,242-任务终止,244-任务超期
            switch (taskState){
                case 1:
                    taskState = 240;
                    break;
                case 2:
                    taskState = 239;
                    break;
                case 3:
                    taskState = 241;
                    break;
                case 4:
                    taskState = 242;
                    break;
                case 5:
                    taskState = 238;
                    break;
                case 6:
                    taskState = 244;
                    break;
                default:
                    break;
            }
            UPatrolResult uPatrolResult = new UPatrolResult().setTaskId(taskId);
            uPatrolResult.setTaskState(taskState);
            uPatrolResult.setExecuteTime(new Date());
            StaticContextAccessor.getBean(UPatrolResultDao.class).update(uPatrolResult);
        }catch (Exception e){
            log.error("更新站端本体任务状态异常：", e);
        }
    }

    /**
     * 工具上报的巡视结果处理
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
            FileUtil.copyFileUsingStream(resultImagePath, resultImagePath);

            // 复制原图到算法分析指定的路径
            if (StringUtils.isNotEmpty(ftpFileName) && StringUtils.isNotEmpty(value) && !ftpsTurbo()){
                log.info("ftpFileName and value is not empty...");
                log.info("resultImagePath=={}", resultImagePath);
                try {
                    // 调用video服务 将要分析的图片从ftps下载到本地
                    Map<String, String> params = Maps.newLinkedHashMap();
                    params.put("source", resultImagePath);
                    params.put("target", filePath);
                    SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class).postForObject(Constant.VIDEO_DOWNLOAD_FILE, params, String.class);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            TCruisePointInstanceDetail details = uPatrolTaskService.selectForTask(Long.valueOf(instanceId));

            Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
            tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
            tCruiseTaskResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_UN));
            tCruiseTaskResultMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
            tCruiseTaskResultMap.put("isWarn", "0");
            tCruiseTaskResultMap.put("origpic", resultImagePath);
            String picPath = resultImagePath.replace(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content")));
            tCruiseTaskResultMap.put("picpath", picPath);

            // 判断是否有配置算法
            TAlgorithmMeteInfo algorithm = abstractVideoCruise.needAnalysis(String.valueOf(details.getDeviceMeteId()));
            if (algorithm != null) {
                JSONObject jsonForRe = new JSONObject();
                jsonForRe.put("absPath", resultImagePath);
                abstractVideoCruise.algorithmAnalysis(tCruiseTaskResultMap, 0, taskId, jsonForRe, algorithm);
            } else {
                updatePointStatusNum(taskId, tCruiseTaskResultMap, details);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 拍照和声音点位结果redis更新
     *
     * @param taskId               任务id
     * @param tCruiseTaskResultMap 巡视结果map
     * @param details              测点信息
     */
    private void updatePointStatusNum(String taskId,  Map<String, String> tCruiseTaskResultMap, TCruisePointInstanceDetail details) {
        try {
            tCruiseTaskResultMap.put("cruiseAbnormal", "null");
            tCruiseTaskResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
            tCruiseTaskResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
            tCruiseTaskResultMap.put("resultDesc", "--");
            tCruiseTaskResultMap.put("cruiseTime", tCruiseTaskResultMap.get("time"));
            tCruiseTaskResultMap.put("resultNum", Objects.nonNull(details.getAnalyseType()) ? "已录音" : "已拍照");
            String str = PATROL_TASK_PREFIX + taskId + ":" + details.getInstanceId();
            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);

            uPatrolTaskService.patrolTaskResultHandler(taskId, details.getInstanceId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Redis数据库批量查询Key值游标
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
     * 如果 ftpsTurbo 为 true，则表示设置了文件盘共享，不使用 ftps 对文件进行传输拷贝
     */
    public boolean ftpsTurbo() {
        boolean ftpsTurbo = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:ftpsTurbo", "content"));
        log.warn("ftpsTurbo is {}", ftpsTurbo);
        return ftpsTurbo;
    }
}
