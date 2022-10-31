package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author YC
 * @date 2020/12/8 9:58
 * 机器人巡视结果处理线程
 */
@lombok.extern.slf4j.Slf4j
public class InspectionResultThread implements Runnable{

    private RedisTemplate redisTemplate;
    private Boolean changeTaskStatus;
    private RobotPatrolTaskResult robotPatrolTaskResult;
    private Map<String, String> infoMap;
    private UPatrolTaskService uPatrolTaskService;
    public static final String PATROL_TASK_PREFIX = "patrol_task_result:";

    public InspectionResultThread(RobotPatrolTaskResult robotPatrolTaskResult, Map<String, String> infoMap, RedisTemplate redisTemplate, boolean changeTaskStatus){
        this.robotPatrolTaskResult = robotPatrolTaskResult;
        this.infoMap = infoMap;
        this.redisTemplate = redisTemplate;
        this.changeTaskStatus = changeTaskStatus;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
    }

    @Override
    public void run(){
        try {
            log.info("开始处理巡检结果并对其标准化 >>>>>>> robotPatrolTaskResult==={}", robotPatrolTaskResult);
            String taskId = infoMap.get("taskId");
            String instanceId;
            String robotCode = robotPatrolTaskResult.getRobotCode();

            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:" + robotCode + ":" + taskId);
            // 上报结果的巡视点,即已经做过的点
            List<Long> instanceIdList = Optional.ofNullable(Constant.flagMap.get(taskId)).orElse(new ArrayList<>());
            // 遍历在下任务时提前组装好的巡检点信息
            for (String key : robotInfoKeys){
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);

                String redisKeyName = PATROL_TASK_PREFIX + taskId + ":";
                if (Objects.equals(robotCode, redisInfoMap.get("robotCode"))
                        && Objects.equals(taskId, redisInfoMap.get("taskId"))
                        && Objects.equals(robotPatrolTaskResult.getDeviceId(), redisInfoMap.get("inspectionCode"))) {
                    instanceId = redisInfoMap.get("instanceId");
                    instanceIdList.add(Long.valueOf(instanceId));
                    Constant.flagMap.put(taskId, instanceIdList);

                    Map<String, Object> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName + instanceId);
                    tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
                    tCruiseTaskResultMap.put("cruiseStatus", "252");

                    if (StringUtils.isNotEmpty(robotPatrolTaskResult.getValue())) {
                        tCruiseTaskResultMap.put("resultNum", robotPatrolTaskResult.getValue());
                        tCruiseTaskResultMap.put("cruiseResult", "246");
                        tCruiseTaskResultMap.put("cruiseAbnormal", "null");
                        log.info("taskId为{},instanceId为{}的该点结果正常", taskId, instanceId);
                    }else {
                        // value无值且resultNum为--，若结果非音频文件，则为异常情况
                        tCruiseTaskResultMap.put("resultNum", "--");
                        if ("3".equals(robotPatrolTaskResult.getFileType())){
                            tCruiseTaskResultMap.put("cruiseResult", "246");
                            tCruiseTaskResultMap.put("cruiseAbnormal","null");
                            log.info("taskId为{},instanceId为{}的该点结果正常", taskId, instanceId);
                        }else {
                            tCruiseTaskResultMap.put("cruiseResult","247");
                            tCruiseTaskResultMap.put("cruiseAbnormal","249");

                            log.info("taskId为{},instanceId为{}的该点结果异常", taskId, instanceId);
                        }
                    }
                    tCruiseTaskResultMap.put("picpath", infoMap.get("relativePath"));
                    tCruiseTaskResultMap.put("origpic", infoMap.containsKey("absolutePath") ? infoMap.get("absolutePath") : "");
                    tCruiseTaskResultMap.put("evaluationState", "257");
                    tCruiseTaskResultMap.put("isWarn", "0");
                    tCruiseTaskResultMap.put("recognitionType", robotPatrolTaskResult.getRecognitionType());
                    tCruiseTaskResultMap.put("fileType", robotPatrolTaskResult.getFileType());
                    tCruiseTaskResultMap.put("rectangle", robotPatrolTaskResult.getRectangle());
                    tCruiseTaskResultMap.put("confidence", "");

                    redisTemplate.opsForHash().putAll(PATROL_TASK_PREFIX + taskId + ":" + instanceId, tCruiseTaskResultMap);
                    log.info("taskId为{}巡视点instanceId为{}的点位已更新redis", taskId, instanceId);

                    // 巡视主机下发的任务或者站端本体任务
                    boolean flag = judgeTaskSourceHandler(taskId, instanceId, robotCode, robotInfoKeys);
                    if (Boolean.FALSE.equals(flag)){
                        simulationToolTaskHandler(infoMap.get("absolutePath"), taskId, instanceId);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 判断任务是否为巡视主机下发的任务
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @param robotCode 机器人/无人机编码
     * @param robotInfoKeys redis有用的值
     * @return boolean
     */
    private boolean judgeTaskSourceHandler(String taskId, String instanceId, String robotCode, Set<String> robotInfoKeys) {
        try {
            Integer robotType = uPatrolTaskService.selectRobotType(robotPatrolTaskResult.getRobotCode());
            boolean isSimulationTool = StringUtils.isNotEmpty(robotPatrolTaskResult.getFilePath())
                    && StringUtils.isEmpty(robotPatrolTaskResult.getOriginFileResultPath())
                    && StringUtils.isEmpty(robotPatrolTaskResult.getOriginFilePath())
                    // 且是E机器人
                    && Objects.equals(159, robotType);
            UPatrolTask uPatrolTask = uPatrolTaskService.selectByPrimaryId(taskId);
            boolean isSelfTask = Objects.equals(110, uPatrolTask.getTaskSource());
            if (Boolean.TRUE.equals(isSimulationTool)) {
                // 真实设备上报的巡视结果
                Map<String, String> jasonMap = new HashMap<>(2);
                jasonMap.put("type", "finishedOneInstance");
                jasonMap.put("taskId", taskId);
                log.info("做完一个点-前端推送：{}", JSON.toJSONString(jasonMap));
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

                Integer flag = uPatrolTaskService.selectIsAlarmByTask(taskId, instanceId);
                if (flag > 0){
                    log.info("taskId为{}巡视点instanceId为{}的点位产生了告警,需要更新图片", taskId, instanceId);
                    uPatrolTaskService.updatePicPath(taskId, instanceId, infoMap.get("relativePath"));
                }
                uPatrolTaskService.patrolTaskResultHandler(taskId, Long.valueOf(instanceId));
                // 区分是否为站端本体任务如果是站端本体任务,需更新任务状态
                if (Boolean.TRUE.equals(isSelfTask)){
                    standTaskDealHandler(taskId, robotCode, robotInfoKeys);
                }
                return true;
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 更新站端本体任务状态
     * @param taskId 任务id
     * @param robotCode 机器人/无人机编码
     * @param robotInfoKeys redis有用的值
     */
    private void standTaskDealHandler(String taskId, String robotCode, Set<String> robotInfoKeys){
        try {
            Map<String, Object> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
            log.info("redisInfoMap=={}", JSON.toJSONString(redisInfoMap));
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
            Set<String> cruiseKey = redisScan(PATROL_TASK_PREFIX + taskId);
            if (robotInfoKeys.size() == cruiseKey.size()){
                taskState = 240;
                TimeUnit.SECONDS.sleep(2);
            }
            UPatrolResult uPatrolResult = StaticContextAccessor.getBean(UPatrolResultDao.class).selectByPrimaryId(taskId);
            uPatrolResult.setTaskState(taskState);
            uPatrolResult.setExecuteTime(new Date());
            StaticContextAccessor.getBean(UPatrolResultDao.class).updateUPatrolResult(uPatrolResult);
        }catch (Exception e){
            log.error("更新站端本体任务状态异常：", e);
        }
    }

    /**
     * 区分是模拟工具上报巡视结果还是真实的
     *
     * @param originPath 巡视结果文件全路径
     * @param taskId 任务id
     * @param instanceId 巡视点id
     */
    private void simulationToolTaskHandler(String originPath, String taskId, String instanceId) {
        try {
            // 复制图片到算法分析指定的路径
            String ftpFileName = originPath.trim().substring(originPath.trim().lastIndexOf("/") + 1);
            String resultImagePath = redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content") + ftpFileName;
            FileUtil.copyFileUsingStream(resultImagePath, resultImagePath);
            // 标定文件
            String picModelPath = redisTemplate.opsForHash().get("t_sys_param:picModelPath", "content") + "/" + "inspectionCode";
            TCruisePointInstanceDetail details = uPatrolTaskService.selectForTask(Long.valueOf(instanceId));
            TStdDeviceMete tStdDevicemete = uPatrolTaskService.selectDeviceMete(details.getDeviceMeteId());

            Map<String, Object> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
            tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
            tCruiseTaskResultMap.put("cruiseStatus", "253");
            tCruiseTaskResultMap.put("evaluationState", "257");
            tCruiseTaskResultMap.put("isWarn", "0");
            tCruiseTaskResultMap.put("origpic", resultImagePath);
            String picPath = resultImagePath.replace(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content")));
            tCruiseTaskResultMap.put("picpath", picPath);


            if (StringUtils.equals("on", details.getIsAi()) || StringUtils.equals("on", details.getIsJudge())) {
                // 配置了缺陷或判别算法的点位
//                List<TAlgorithmInfo> tAlgorithmInfoList = uPatrolTaskService.selectByDeviceMeteId(details.getDeviceMeteId());
//
//                packageAndInvoke(tAlgorithmInfoList, taskId, instanceId, resultImagePath, picModelPath, tStdDevicemete);
            } else {
                // 即拍照的点位和声音
                updatePointStatusNum(taskId, tCruiseTaskResultMap, details);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 拍照和声音点位结果redis更新
     *
     * @param taskId 任务id
     * @param tCruiseTaskResultMap 巡视结果map
     * @param details 测点信息
     */
    private void updatePointStatusNum(String taskId,  Map<String, Object> tCruiseTaskResultMap, TCruisePointInstanceDetail details) {
        try {
            tCruiseTaskResultMap.put("cruiseAbnormal", "null");
            tCruiseTaskResultMap.put("cruiseStatus", "252");
            tCruiseTaskResultMap.put("cruiseResult", "246");
            tCruiseTaskResultMap.put("resultDesc", "--");
            tCruiseTaskResultMap.put("cruiseTime", tCruiseTaskResultMap.get("time"));
            tCruiseTaskResultMap.put("resultNum", Objects.nonNull(details.getAnalyseType()) ? "已录音" : "已拍照");
            String str = PATROL_TASK_PREFIX + taskId + ":" + details.getInstanceId();
            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);

            // webSocket通知前端调用巡视监控的接口
            Map<String, String> jasonMap = new HashMap<>(2);
            jasonMap.put("type", "finishedOneInstance");
            jasonMap.put("taskId", taskId);
            log.info("做完一个点-前端推送：{}", JSON.toJSONString(jasonMap));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

            // todo：巡视结果上报上一级系统

            uPatrolTaskService.patrolTaskResultHandler(taskId, details.getInstanceId());
        }catch (Exception e){
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
}
