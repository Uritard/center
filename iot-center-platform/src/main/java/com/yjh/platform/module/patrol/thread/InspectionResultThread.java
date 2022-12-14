package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

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
    private final UPatrolTaskService uPatrolTaskService;
    private final AbstractVideoCruise abstractVideoCruise;

    private final TRobotInspectionDao tRobotInspectionDao;
    private final UPatrolResultDao uPatrolResultDao;
    private final AnalyseDataOperateDao analyseDataOperateDao;

    public InspectionResultThread(RobotPatrolTaskResult robotPatrolTaskResult, Map<String, String> infoMap,
                                  RedisTemplate redisTemplate, boolean changeTaskStatus){
        this.robotPatrolTaskResult = robotPatrolTaskResult;
        this.infoMap = infoMap;
        this.redisTemplate = redisTemplate;
        this.changeTaskStatus = changeTaskStatus;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        this.abstractVideoCruise = StaticContextAccessor.getBean(NormalVideoCruiseExecuteImpl.class);

        this.tRobotInspectionDao = StaticContextAccessor.getBean(TRobotInspectionDao.class);
        this.uPatrolResultDao = StaticContextAccessor.getBean(UPatrolResultDao.class);
        this.analyseDataOperateDao = StaticContextAccessor.getBean(AnalyseDataOperateDao.class);
    }

    @Override
    public void run(){

        try {
            log.info("开始处理巡检结果并对其标准化 >>>>>>> robotPatrolTaskResult==={}", JSON.toJSONString(robotPatrolTaskResult));
            String taskId = infoMap.get("taskId");
            String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"));
            /*if ("3".equals(sysLevel)) {
                upSystemDealWith(taskId);
                return;
            }*/
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
            // 上级系统没有存储对应值，DeviceId 就是下级的 instanceId
            TCruisePointInstance insInfo = null;
            if (StringUtils.isEmpty(instanceId)) {
                String originId = robotPatrolTaskResult.getDeviceId();
                insInfo = tRobotInspectionDao.selectRealInstance(originId, robotCode);
                log.info("instanceInfo: {}", JSON.toJSONString(insInfo));
                instanceId = String.valueOf(insInfo.getInstanceId());
            }

            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            if (MapUtils.isEmpty(tCruiseTaskResultMap) || !tCruiseTaskResultMap.containsKey("deviceId") || !tCruiseTaskResultMap.containsKey("cruiseId")) {
                tCruiseTaskResultMap = new HashMap<>(32);
                upSystemTaskInfoInitialize(tCruiseTaskResultMap, taskId, insInfo);
            }

            tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
            tCruiseTaskResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
            boolean isnormal = true;
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
            String valid = robotPatrolTaskResult.getValid();

            if (StringUtils.isNotEmpty(valid)) {
                switch (valid){
                    case "1":
                        computeEmpty(tCruiseTaskResultMap,"cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                        computeEmpty(tCruiseTaskResultMap,"cruiseAbnormal", "--");
                        break;
                    case "2":
                        computeEmpty(tCruiseTaskResultMap,"cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                        computeEmpty(tCruiseTaskResultMap,"cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM));
                        break;
                    case "0":
                    default:
                        computeEmpty(tCruiseTaskResultMap,"cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                        computeEmpty(tCruiseTaskResultMap,"cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL));
                        break;
                }
            } else if (isnormal) {
                computeEmpty(tCruiseTaskResultMap,"cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                computeEmpty(tCruiseTaskResultMap,"cruiseAbnormal", "--");
            } else {
                computeEmpty(tCruiseTaskResultMap,"cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                computeEmpty(tCruiseTaskResultMap, "cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DATAABNORMAL));
            }

            tCruiseTaskResultMap.put("picpath", infoMap.getOrDefault("relativePath", ""));
            tCruiseTaskResultMap.put("origpic", infoMap.getOrDefault("absolutePath", ""));
            tCruiseTaskResultMap.putIfAbsent("evaluationState", String.valueOf(EVALUATION_STATE_UN));
            tCruiseTaskResultMap.putIfAbsent("isWarn", "0");
            tCruiseTaskResultMap.put("recognitionType", robotPatrolTaskResult.getRecognitionType());
            tCruiseTaskResultMap.put("fileType", robotPatrolTaskResult.getFileType());
            tCruiseTaskResultMap.put("rectangle", robotPatrolTaskResult.getRectangle());
            tCruiseTaskResultMap.put("confidence", "");
            redisTemplate.opsForHash().putAll(redisKeyName, tCruiseTaskResultMap);
            Object waiter = MAP_LOCK.get(taskId + instanceId);
            if (Objects.nonNull(waiter)) {
                synchronized (waiter) {
                    waiter.notifyAll();
                }
                MAP_LOCK.remove(taskId + instanceId);
            }
            // 巡视主机下发的任务或者站端本体任务
            boolean flag = judgeTaskSourceHandler(taskId, instanceId, robotCode);
            if (!flag) {
                log.info("This is simulation tool task！！！ {}", taskId);
                simulationToolTaskHandler(infoMap.getOrDefault("absolutePath", ""), taskId, instanceId, robotPatrolTaskResult.getValue(), robotPatrolTaskResult.getFilePath());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
     * 判断任务是否为巡视主机下发的任务
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @param robotCode 机器人/无人机编码
     * @return boolean
     */
    private boolean judgeTaskSourceHandler(String taskId, String instanceId, String robotCode) {
        try {
            String sysLevel = (String)redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content");

            Integer robotType = uPatrolTaskService.selectRobotType(robotPatrolTaskResult.getSendCode());
            // 巡视结果只有file_path字段,没有origin_file_result_path和origin_file_path 为模拟工具
            boolean isSimulationTool = StringUtils.isNotEmpty(robotPatrolTaskResult.getFilePath())
                    && StringUtils.isEmpty(robotPatrolTaskResult.getOriginFileResultPath())
                    && StringUtils.isEmpty(robotPatrolTaskResult.getOriginFilePath())
                    // 且是E机器人
                    && Objects.equals(159, robotType)
                    // 上级系统不走算法处理，只存数据
                    && !"3".equals(sysLevel);

            log.info("simulation tool flag, isSimulationTool: {}, taskId: {}, sysLevel: {}", isSimulationTool, taskId, sysLevel);
            if (!isSimulationTool) {
                Integer flag = uPatrolTaskService.selectIsAlarmByTask(taskId, instanceId);
                if (flag > 0) {
                    log.info("taskId为{}巡视点instanceId为{}的点位产生了告警,需要更新图片", taskId, instanceId);
                    uPatrolTaskService.updatePicPath(taskId, instanceId, infoMap.get("relativePath"));
                }
                uPatrolTaskService.patrolTaskResultHandler(taskId, Long.valueOf(instanceId));
                // 区分是否为站端本体任务如果是站端本体任务,需更新任务状态
                // 221128，可以不用在这更新，机器人上传任务状态的消息会处理任务状态
                // UPatrolTask uPatrolTask = uPatrolTaskService.selectByPrimaryId(taskId);
                // boolean isSelfTask = Objects.equals(110, uPatrolTask.getTaskSource());
                // if (isSelfTask) {
                //     standTaskDealHandler(taskId, robotCode);
                // }
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
            uPatrolResultDao.update(uPatrolResult);
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
            // tCruiseTaskResultMap.put("cruiseTime", tCruiseTaskResultMap.get("time"));
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
