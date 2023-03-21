package com.yjh.platform.module.patrol.thread;

import cn.hutool.core.math.MathUtil;
import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.NonhomologousWarnDao;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskAlarm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yjh.platform.module.patrol.CruiseConstant.CRUISE_STATE_DONE;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * @author sunjinyan
 * @since 2022-04-08
 * 机器人侧非同源告警处理线程
 */
@Slf4j
public class NonhomologousWarnThread implements Runnable{

    private final RedisTemplate redisTemplate;
    private final RobotPatrolTaskAlarm robotPatrolTaskAlarm;
    private final int isResult;
    private final NonhomologousWarnDao nonhomologousWarnDao;

    public static final String TRIPHASE_PREFIX = "TRIPHASE_RULE:";
    private static final Map<String, Object> TRIPHASE_LOCK = new ConcurrentHashMap<>(32);

    public NonhomologousWarnThread(RobotPatrolTaskAlarm robotPatrolTaskAlarm, RedisTemplate redisTemplate, int isResult){
        this.robotPatrolTaskAlarm = robotPatrolTaskAlarm;
        this.redisTemplate = redisTemplate;
        this.isResult = isResult;
        this.nonhomologousWarnDao = StaticContextAccessor.getBean(NonhomologousWarnDao.class);
    }

    @Override
    public void run(){
        try {
            log.info("开始处理巡检结果并生成相应的非同源告警 >>>>>>> robotPatrolTaskAlarm==={}, isResult ==={}", JSON.toJSONString(robotPatrolTaskAlarm), isResult);
            String taskCode = robotPatrolTaskAlarm.getTaskCode();
            String robotInsResult = robotPatrolTaskAlarm.getValue();

            String warnId = String.valueOf(UUID.randomUUID()).replace("-", "");
            if (0 == isResult){
                // 三相告警
                //只处理系统下发任务产生的三相告警
                if (StringUtils.isNotBlank(robotPatrolTaskAlarm.getTaskCode())) {
                    Map<String,Object> warnInfo = new HashMap<>(6);
                    warnInfo.put("warnId", warnId);
                    warnInfo.put("warnType", 5);
                    warnInfo.put("instanceId", null);
                    warnInfo.put("warnContent", "机器人相别告警：" + robotPatrolTaskAlarm.getContent());
                    List<Map<String, Object>> insResults = new ArrayList<>();
                    String[] deviceIdArray = robotPatrolTaskAlarm.getDeviceId().split(",");
                    for(String deviceId : deviceIdArray){
                        Map<String, Object> robotWarn = new HashMap<>(4);
                        robotWarn.put("taskId", taskCode);
                        robotWarn.put("inspectionId", nonhomologousWarnDao.getInstanceIdByDeviceId(deviceId, taskCode));
                        robotWarn.put("warnId", warnId);
                        insResults.add(robotWarn);
                    }
                    warnInfo.put("resultsInfo", insResults);
                    insertNonhomologousWarnInfo(warnInfo);
                }

            } else {
                if (!judgeTriphaseWarn(taskCode, robotInsResult, warnId)) {
                    // 需要判断的非同源告警
                    judgeNonhomologousWarn(taskCode, robotInsResult, warnId);
                }
            }
        } catch (Exception e) {
            log.error("非同源告警处理异常：", e);
        }
    }

    private void judgeNonhomologousWarn(String taskCode, String robotInsResult, String warnId) {
        // 查询非同源告警规则
        Map<String, String> cruiseResultMap = new HashMap<>(4);
        cruiseResultMap.put("deviceId", robotPatrolTaskAlarm.getDeviceId());
        List<Map<String, Object>> list = nonhomologousWarnDao.getNonhomologousInspections(cruiseResultMap);
        log.info("非同源告警配置 === {}", JSON.toJSONString(list));
        for(Map<String, Object> map : list){
            if (!Objects.isNull(map.get("oneCruiseName")) && !Objects.isNull(map.get("warnType"))){
                String warnType = String.valueOf(map.get("warnType"));
                String warnThreshold = String.valueOf(map.get("warnThreshold"));
                String robotInstanceId = String.valueOf(map.get("instanceIdOne"));
                String videoInstanceId = String.valueOf(map.get("instanceIdTwo"));
                String instanceId = String.valueOf(map.get("instanceId"));

                String oldId = MapUtils.getString(map, "oldId");
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskCode + ":" + oldId);
                String videoInsResult = redisInfoMap.get("resultNum");
                int cruiseStatus = MapUtils.getIntValue(redisInfoMap,"cruiseStatus", CruiseConstant.CRUISE_STATE_UN);
                boolean isRunning = StringUtils.isNotEmpty(oldId) && (MapUtils.isEmpty(redisInfoMap) || CruiseConstant.CRUISE_STATE_UN == cruiseStatus || CommonUtils.isEmptyOrNullstr(videoInsResult));
                if (isRunning) {
                    log.info("robotInsResult === {}, videoInsResult === {}, 阈值 === {}, cruiseStatus === {}，redisInfoMap === {}, 非同源告警另一个任务未完成", robotInsResult, videoInsResult, warnThreshold, cruiseStatus, JSON.toJSONString(redisInfoMap));
                    continue;
                }
                log.info("非同源告警---,redisInfoMap==={}", redisInfoMap);
                // 1-红外 2-位置 3-表计数显 4-表计指针 5-相别 6-区间 7-五次不变
                switch (warnType){
                    case "1":
                        if(!checkWarnExist(robotInstanceId, taskCode, instanceId)){

                            videoInsResult = StringUtils.substringBefore(videoInsResult, ",");
                            if(isNumeric(warnThreshold) && isNumeric(robotInsResult) && isNumeric(videoInsResult)){
                                if(Math.abs(Double.parseDouble(robotInsResult) - Double.parseDouble(videoInsResult)) > Double.parseDouble(warnThreshold)){
                                    String warnContent = "红外测温非同源结果差值超过阈值：" + warnThreshold;
                                    insertWarnInfo(warnId, instanceId, warnContent, taskCode, robotInstanceId, videoInstanceId, 1);
                                }
                            }else{
                                log.info("robotInsResult为==={},videoInsResult为==={},阈值是==={},红外非同源告警--数据非数字", robotInsResult, videoInsResult, warnThreshold);
                            }
                        }else{
                            log.info("非同源告警---结果为空或告警已存在!");
                        }
                        break;
                    case "2":
                        if(!checkWarnExist(robotInstanceId, taskCode, instanceId)){

                            if (!Objects.equals(robotInsResult, videoInsResult)) {
                                String warnContent = "位置状态非同源结果不一致";
                                insertWarnInfo(warnId, instanceId, warnContent, taskCode, robotInstanceId, videoInstanceId, 2);
                            } else {
                                log.info("robotInsResult为==={},videoInsResult为==={},位置状态非同源告警--结果", robotInsResult, videoInsResult);
                            }
                        }else{
                            log.info("非同源告警---结果为空或告警已存在!");
                        }
                        break;
                    case "3":
                        if(!checkWarnExist(robotInstanceId, taskCode, instanceId)){

                            if (!Objects.equals(robotInsResult, videoInsResult)) {
                                String warnContent = "数显类表计识别非同源结果不一致";
                                insertWarnInfo(warnId, instanceId, warnContent, taskCode, robotInstanceId, videoInstanceId, 3);
                            } else {
                                log.info("robotInsResult为==={},videoInsResult为==={},表计-数显非同源告警--结果不一致", robotInsResult, videoInsResult);
                            }
                        }else{
                            log.info("非同源告警---结果为空或告警已存在!");
                        }
                        break;
                    case "4":
                        if(!checkWarnExist(robotInstanceId, taskCode, instanceId)){

                            if(isNumeric(warnThreshold) && isNumeric(robotInsResult) && isNumeric(videoInsResult)){
                                if(Math.abs(Double.parseDouble(robotInsResult) - Double.parseDouble(videoInsResult)) > Double.parseDouble(warnThreshold)){
                                    String warnContent = "指针类表计识别非同源结果差值超过阈值：" + warnThreshold;
                                    insertWarnInfo(warnId, instanceId, warnContent, taskCode, robotInstanceId, videoInstanceId, 4);
                                }
                            }else{
                                log.info("robotInsResult为==={},videoInsResult为==={},阈值是==={},表计-指针非同源告警--数据非数字", robotInsResult, videoInsResult,warnThreshold);
                            }
                        }else{
                            log.info("非同源告警---结果为空或告警已存在!");
                        }
                        break;
                    case "6":
                        if(!isNumeric(robotInsResult)){
                            log.info("区间非同源告警--数据非数字");
                            break;
                        }
                        // 1-天 2-周 3-月
                        String timeType = String.valueOf(map.get("intervalType"));
                        Date endTime= new Date();
                        Date startTime = null;
                        switch (timeType) {
                            case "1":
                                Calendar dayNow = Calendar.getInstance();
                                dayNow.setTime(endTime);
                                dayNow.add(Calendar.DAY_OF_YEAR, -1);
                                startTime = dayNow.getTime();
                                break;
                            case "2":
                                Calendar weekNow = Calendar.getInstance();
                                weekNow.setTime(endTime);
                                weekNow.add(Calendar.DAY_OF_YEAR, -7);
                                startTime = weekNow.getTime();
                                break;
                            case "3":
                                Calendar monthNow = Calendar.getInstance();
                                monthNow.setTime(endTime);
                                monthNow.add(Calendar.MONTH, -1);
                                startTime = monthNow.getTime();
                                break;
                            default:
                                break;
                        }
                        Map<String,Object> intervalResultsParam = new HashMap<>(6);
                        intervalResultsParam.put("type", 6);
                        intervalResultsParam.put("startTime", startTime);
                        intervalResultsParam.put("endTime", endTime);
                        intervalResultsParam.put("inspectionId", robotInstanceId);
                        // 分时段查出历史结果
                        List<Map<String,Object>> intervalResults = nonhomologousWarnDao.selectWarnResults(intervalResultsParam);
                        if(intervalResults.isEmpty()){
                            log.info("区间非同源告警--历史数据为空");
                            break;
                        }
                        // 加入当前结果对象
                        Map<String,Object> nowResult = new HashMap<>(4);
                        nowResult.put("taskId", taskCode);
                        nowResult.put("inspectionId", robotInstanceId);
                        nowResult.put("resultValue", robotInsResult);
                        // 过滤非数字结果
                        intervalResults.add(nowResult);
                        List<Map<String,Object>> numResults= intervalResults.stream()
                                .filter(mapItem -> isNumeric(mapItem.get("resultValue").toString()))
                                .collect(Collectors.toList());
                        Optional<Map<String,Object>> maxValueResultInfo = numResults.stream().reduce((x, y) -> Double.parseDouble(x.get("resultValue").toString()) > Double.parseDouble(y.get("resultValue").toString()) ? x : y);
                        Optional<Map<String,Object>> minValueResultInfo = numResults.stream().reduce((x, y) -> Double.parseDouble(x.get("resultValue").toString()) < Double.parseDouble(y.get("resultValue").toString()) ? x : y);
                        log.info("区间非同源判断：maxInsResult为==={},minInsResult为==={},阈值是==={}", maxValueResultInfo, minValueResultInfo, warnThreshold);
                        if((Double.parseDouble(maxValueResultInfo.get().get("resultValue").toString()) -
                                Double.parseDouble(minValueResultInfo.get().get("resultValue").toString())) >
                                Double.parseDouble(warnThreshold)){
                            Map<String,Object> warn = new HashMap<>(4);
                            warn.put("warnId", warnId);
                            warn.put("warnType", 6);
                            warn.put("instanceId", Long.parseLong(instanceId));
                            warn.put("warnContent", "时间范围内表计识别结果差值超过阈值：" + warnThreshold);
                            intervalResults.forEach(i -> i.put("warnId", warn.get("warnId")));
                            List<Map<String, Object>> insResults = new ArrayList<>(numResults);
                            warn.put("resultsInfo", insResults);
                            insertNonhomologousWarnInfo(warn);
                        }
                        break;
                    case "7":
                        if(!isNumeric(robotInsResult)){
                            log.info("最近五次非同源告警--数据非数字");
                            break;
                        }
                        Map<String,Object> fiveResultsParam = new HashMap<>(4);
                        fiveResultsParam.put("type", 7);
                        fiveResultsParam.put("inspectionId", robotInstanceId);
                        //分时段查出历史结果
                        List<Map<String,Object>> fiveResults = nonhomologousWarnDao.selectWarnResults(fiveResultsParam);
                        if(fiveResults.size() < 4){
                            log.info("最近五次非同源告警--历史数据不足");
                            break;
                        }
                        //加入当前结果对象
                        Map<String,Object> latestResult = new HashMap<>(4);
                        latestResult.put("taskId", taskCode);
                        latestResult.put("inspectionId", robotInstanceId);
                        latestResult.put("resultValue", robotInsResult);
                        //过滤非数字结果
                        fiveResults.add(latestResult);

                        List<Map<String,Object>> distinctResults = fiveResults.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                                new TreeSet<>(Comparator.comparing(o -> o.get("resultValue").toString()))), ArrayList::new));
                        if(distinctResults.size() == 1){
                            Map<String,Object> warn = new HashMap<>(4);
                            warn.put("warnId", warnId);
                            warn.put("warnType", 7);
                            warn.put("instanceId", Long.parseLong(instanceId));
                            warn.put("warnContent", "累计五次表计结果一致");
                            fiveResults.forEach(i -> i.put("warnId", warn.get("warnId")));
                            List<Map<String, Object>> insResults = new ArrayList<>(fiveResults);
                            warn.put("resultsInfo", insResults);
                            insertNonhomologousWarnInfo(warn);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private boolean judgeTriphaseWarn(String taskCode, String robotInsResult, String warnId) {
        // 查询非同源告警规则
        String instanceId = robotPatrolTaskAlarm.getDeviceId();
        List<Map<String, Object>> list = nonhomologousWarnDao.selectTriphaseInspections(instanceId);
        log.info("三相告警配置 === instanceId: {}, {}", instanceId, JSON.toJSONString(list));
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }

        CruiseConstant.AbnormalResDescEnum resDescEnum = CruiseConstant.AbnormalResDescEnum.getEnum(robotInsResult);
        if (resDescEnum != null){
            return false;
        }

        boolean retFlag = false;
        for (Map<String, Object> map : list) {
            long triphaseId = MapUtils.getLong(map, "triphaseId");
            String triphaseKey = TRIPHASE_PREFIX + taskCode + ":" + triphaseId;
            Object lock = TRIPHASE_LOCK.computeIfAbsent(triphaseKey, v -> new Object());
            int triphaseType = MapUtils.getInteger(map, "triphaseType");
            float warnThreshold = MapUtils.getFloat(map, "warnThreshold");
            float fruit;
            String triphaseName;
            Map<String, String> triphaseRetMap;
            synchronized (lock) {
                triphaseRetMap = redisTemplate.opsForHash().entries(triphaseKey);
                if (MapUtils.isEmpty(triphaseRetMap)) {
                    triphaseRetMap = new HashMap<>();
                    triphaseRetMap.put(MapUtils.getString(map, "instanceOneId"), "");
                    triphaseRetMap.put(MapUtils.getString(map, "instanceTwoId"), "");
                    triphaseRetMap.put(MapUtils.getString(map, "instanceTriId"), "");
                }
                if (NumberUtils.isCreatable(robotInsResult)) {
                    triphaseRetMap.put(instanceId, robotInsResult);
                } else {
                    log.info("三相告警值不是数字 === instanceId: {}, {}", instanceId, robotInsResult);
                }
                redisTemplate.opsForHash().putAll(triphaseKey, triphaseRetMap);

                if (triphaseRetMap.containsValue("")) {
                    continue;
                }
                double[] triphaseResults = triphaseRetMap.values().stream().mapToDouble(NumberUtils::toFloat).toArray();
                float max = (float)NumberUtils.max(triphaseResults);
                float min = (float)NumberUtils.min(triphaseResults);
                switch (triphaseType) {
                    case 1:
                        fruit = (max - 0F > 0.01D) ? (max - min) * 100 / max : 0.0F;
                        triphaseName = "三相不平衡";
                        break;
                    case 2:
                    default:
                        fruit = max - min;
                        triphaseName = "三相温差";
                        break;
                }
                log.info("三相告警结果值 ===max: {}, min: {}, fruit:{}, threshold: {}, {}", max, min, fruit, warnThreshold, JSON.toJSONString(triphaseRetMap));
            }
            TRIPHASE_LOCK.remove(triphaseKey);
            if (fruit > warnThreshold) {
                Map<String, Object> warnInfo = new HashMap<>(8);
                warnInfo.put("warnId", warnId + triphaseType);
                warnInfo.put("warnType", 5);
                warnInfo.put("instanceId", instanceId);
                warnInfo.put("warnContent", triphaseName + "告警：" + CommonUtils.percentFormat(fruit - warnThreshold, "#.##"));
                List<Map<String, Object>> insResults = new ArrayList<>();
                for (String insId : triphaseRetMap.keySet()) {
                    Map<String, Object> robotWarn = new HashMap<>(4);
                    robotWarn.put("taskId", taskCode);
                    robotWarn.put("inspectionId", insId);
                    robotWarn.put("warnId", warnId + triphaseType);
                    insResults.add(robotWarn);
                }
                warnInfo.put("resultsInfo", insResults);
                insertNonhomologousWarnInfo(warnInfo);
                retFlag = true;
            }
        }

        return retFlag;
    }

    private boolean insertNonhomologousWarnInfo(Map<String,Object> warn){
        log.info("非同源告警---入库, warnmap==={}", warn);
        nonhomologousWarnDao.insertNonhomologousWarnInfo(warn);
        List<Map<String,Object>> insResults =(List<Map<String,Object>>) warn.get("resultsInfo");
        nonhomologousWarnDao.insertWarnInspections(insResults);
        return true;
    }

    private boolean checkWarnExist(String instanceId, String taskId, String nonInstanceId){
        boolean result = false;
        Map<String, Object> warnParam = new HashMap<>(6);
        warnParam.put("instanceId", instanceId);
        warnParam.put("taskId", taskId);
        warnParam.put("nonInstanceId", Long.parseLong(nonInstanceId));
        int warnNum = nonhomologousWarnDao.checkWarnExist(warnParam);
        if(warnNum > 0){
            result = true;
        }
        return result;
    }

    public static boolean isNumeric(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        String regx = "[+-]*\\d+\\.?\\d*[Ee]*[+-]*\\d+";
        Pattern pattern = Pattern.compile(regx);
        boolean isNumber = pattern.matcher(str).matches();
        if (isNumber) {
            return isNumber;
        }
        regx = "^[-\\+]?[.\\d]*$";
        pattern = Pattern.compile(regx);
        return pattern.matcher(str).matches();
    }

    private void insertWarnInfo(String warnId, String instanceId, String warnContent, String taskCode, String robotInstanceId, String videoInstanceId, int warnType){
        Map<String,Object> warn = new HashMap<>(4);
        warn.put("warnId", warnId);
        warn.put("warnType", warnType);
        warn.put("instanceId", Long.parseLong(instanceId));
        warn.put("warnContent", warnContent);
        List<Map<String,Object>> insResults = new ArrayList<>();
        Map<String,Object> robotWarn = new HashMap<>(4);
        robotWarn.put("taskId", taskCode);
        robotWarn.put("inspectionId", robotInstanceId);
        robotWarn.put("warnId", warnId);
        insResults.add(robotWarn);

        Map<String,Object> videoWarn = new HashMap<>(4);
        videoWarn.put("taskId", taskCode);
        videoWarn.put("inspectionId", videoInstanceId);
        videoWarn.put("warnId", warnId);
        insResults.add(videoWarn);

        warn.put("resultsInfo", insResults);
        insertNonhomologousWarnInfo(warn);
    }

}
