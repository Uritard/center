package com.yjh.platform.module.patrol.thread;

import cn.hutool.core.compiler.CompilerUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.dao.NonhomologousWarnDao;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskAlarm;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.patrol.service.AnalyseDataOperateService;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.dao.TCruiseTriphaseRuleDao;
import com.yjh.platform.module.task.entity.TCruiseTriphaseRule;
import com.yjh.platform.module.task.entity.TWarnInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

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

    private static final String CCD_PATH = "/CCD/";
    private static final String FIR_PATH = "/FIR/";
    private static final String AUDIO_PATH = "/Audio/";

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
                    List<Map<String,Object>> mapList = nonhomologousWarnDao.getInstanceIdByDeviceId(robotPatrolTaskAlarm.getDeviceId(), taskCode);
                    //机器人三相 必须存在三个值再处理
                    if (CollectionUtils.isNotEmpty(mapList) && mapList.size() > 2){
                        mapList.forEach(warnMap -> {
                            warnMap.put("warnId", warnId);
                            insResults.add(warnMap);
                        });
                        warnInfo.put("resultsInfo", insResults);
                        warnInfo.put("value", robotPatrolTaskAlarm.getValue());
                        warnInfo.put("deviceMeteId", mapList.get(0).get("deviceMeteId"));
                        warnInfo.put("one", mapList.get(0).get("inspectionId"));
                        warnInfo.put("two", mapList.get(1).get("inspectionId"));
                        warnInfo.put("three", mapList.get(2).get("inspectionId"));
                        warnInfo.put("oneCruiseDeviceName", mapList.get(0).get("inspectionName"));
                        warnInfo.put("twoCruiseDeviceName", mapList.get(1).get("inspectionName"));
                        warnInfo.put("threeCruiseDeviceName", mapList.get(2).get("inspectionName"));
                        insertNonhomologousWarnInfo(warnInfo, "9");
                    }
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
                float threshold = NumberUtils.toFloat(warnThreshold);
                String robotInstanceId = String.valueOf(map.get("instanceIdOne"));
                String videoInstanceId = String.valueOf(map.get("instanceIdTwo"));
                String instanceId = String.valueOf(map.get("instanceId"));
                Long deviceMeteId = Long.valueOf(String.valueOf(map.get("deviceMeteId")));
                String oneCruiseName = String.valueOf(map.get("oneCruiseName"));
                String twoCruiseName = String.valueOf(map.get("twoCruiseName"));

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
                                float dval = Math.abs(NumberUtils.toFloat(robotInsResult) - NumberUtils.toFloat(videoInsResult));
                                if(dval - threshold > 1e-5){
                                    String dvalStr = CommonUtils.percentFormat(dval, "#.##");
                                    String warnContent = "红外测温非同源结果差值超过阈值告警：" + dvalStr + "，阈值：" + warnThreshold;
                                    insertWarnInfo(warnId, instanceId, deviceMeteId, warnContent, taskCode, robotInstanceId, videoInstanceId, 1, "1", dvalStr, oneCruiseName, twoCruiseName);
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
                                String warnContent = "位置状态非同源结果不一致: " + robotInsResult + " —— " + videoInsResult;
                                insertWarnInfo(warnId, instanceId, deviceMeteId, warnContent, taskCode, robotInstanceId, videoInstanceId, 2, "10", robotInsResult, oneCruiseName, twoCruiseName);
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
                                String warnContent = "数显类表计识别非同源结果不一致: " + robotInsResult + " —— " + videoInsResult;
                                insertWarnInfo(warnId, instanceId, deviceMeteId, warnContent, taskCode, robotInstanceId, videoInstanceId, 3, "7", robotInsResult, oneCruiseName, twoCruiseName);
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
                                float dval = Math.abs(NumberUtils.toFloat(robotInsResult) - NumberUtils.toFloat(videoInsResult));
                                if(dval - threshold > 1e-5){
                                    String dvalStr = CommonUtils.percentFormat(dval, "#.##");
                                    String warnContent = "指针类表计识别非同源结果差值超过阈值告警：" + dvalStr + "，阈值：" + warnThreshold;
                                    insertWarnInfo(warnId, instanceId, deviceMeteId, warnContent, taskCode, robotInstanceId, videoInstanceId, 4, "7", dvalStr, oneCruiseName, twoCruiseName);
                                }
                            }else{
                                log.info("robotInsResult为==={},videoInsResult为==={},阈值是==={},表计-指针非同源告警--数据非数字", robotInsResult, videoInsResult, warnThreshold);
                            }
                        }else{
                            log.info("非同源告警---结果为空或告警已存在!");
                        }
                        break;
                    case "6":
                        if(!isNumeric(robotInsResult)){
                            //特殊走特殊的逻辑
                            if (Constant.nonhomologousWarn.contains(robotInsResult)){
                               Map<String,String> lastValue = nonhomologousWarnDao.selectLastResultNum(robotInstanceId,Constant.nonhomologousWarn.split(","));
                               if (lastValue != null){
                                   log.info("lastValue===={}  thisValue==={} lastTaskId==={}",lastValue.get("lastValue"),robotInsResult,lastValue.get("taskId"));
                                   if (StringUtils.isNotEmpty(lastValue.get("lastValue")) && !robotInsResult.equals(lastValue.get("lastValue"))){
                                       //告警
                                       Map<String,Object> warn = new HashMap<>(4);
                                       warn.put("warnId", warnId);
                                       warn.put("warnType", 6);
                                       warn.put("instanceId", Long.parseLong(instanceId));
                                       warn.put("warnContent", "时间范围内识别结果趋势不一致：" + lastValue.get("lastValue")+"->"+robotInsResult);
                                       warn.put("value",robotInsResult);

                                       List<Map<String,Object>> resultsInfo = new ArrayList<>();
                                       HashMap<String,Object> mapItem1 = new HashMap<>();
                                       mapItem1.put("warnId",warnId);
                                       mapItem1.put("taskId",taskCode);
                                       mapItem1.put("inspectionId",robotInstanceId);
                                       resultsInfo.add(mapItem1);

                                       HashMap<String,Object> mapItem2 = new HashMap<>();
                                       mapItem2.put("warnId",warnId);
                                       mapItem2.put("taskId",lastValue.get("taskId"));
                                       mapItem2.put("inspectionId",robotInstanceId);
                                       resultsInfo.add(mapItem2);
                                       warn.put("resultsInfo",resultsInfo);
                                       warn.put("deviceMeteId", deviceMeteId);
                                       warn.put("oneCruiseDeviceName", oneCruiseName);
                                       warn.put("twoCruiseDeviceName", twoCruiseName);
                                       insertNonhomologousWarnInfo(warn, "10");
                                   }
                               }
                            }
                            log.info("区间非同源告警--数据非指定汉字");
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
                        float dval = NumberUtils.toFloat(maxValueResultInfo.get().get("resultValue").toString()) -
                                NumberUtils.toFloat(minValueResultInfo.get().get("resultValue").toString());
                        if(dval - threshold > 1e-5){
                            String dvalStr = CommonUtils.percentFormat(dval, "#.##");
                            Map<String,Object> warn = new HashMap<>(4);
                            warn.put("warnId", warnId);
                            warn.put("warnType", 6);
                            warn.put("instanceId", Long.parseLong(instanceId));
                            warn.put("warnContent", "时间范围内表计识别结果差值超过阈值告警：" + dvalStr + "，阈值：" + warnThreshold);
                            intervalResults.forEach(i -> i.put("warnId", warn.get("warnId")));
                            List<Map<String, Object>> insResults = new ArrayList<>(numResults);
                            warn.put("resultsInfo", insResults);
                            warn.put("value", dvalStr);
                            warn.put("deviceMeteId", deviceMeteId);
                            warn.put("oneCruiseDeviceName", oneCruiseName);
                            warn.put("twoCruiseDeviceName", twoCruiseName);
                            insertNonhomologousWarnInfo(warn, "10");
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
                            warn.put("warnContent", "累计五次表计结果一致告警：" + distinctResults.get(0).get("resultValue"));
                            fiveResults.forEach(i -> i.put("warnId", warn.get("warnId")));
                            List<Map<String, Object>> insResults = new ArrayList<>(fiveResults);
                            warn.put("resultsInfo", insResults);
                            warn.put("value",latestResult);
                            warn.put("deviceMeteId", deviceMeteId);
                            warn.put("oneCruiseDeviceName", oneCruiseName);
                            warn.put("twoCruiseDeviceName", twoCruiseName);
                            insertNonhomologousWarnInfo(warn, "10");
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private boolean judgeTriphaseWarn(String taskCode, String robotResult, String warnId) {
        // 查询非同源告警规则
        String instanceId = robotPatrolTaskAlarm.getDeviceId();
        List<Map<String, Object>> list = nonhomologousWarnDao.selectTriphaseInspections(instanceId);
        log.info("三相告警配置 === instanceId: {}, {}", instanceId, JSON.toJSONString(list));
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }

        CruiseConstant.AbnormalResDescEnum resDescEnum = CruiseConstant.AbnormalResDescEnum.getEnum(robotResult);
        if (resDescEnum != null){
            return false;
        }
        String robotInsResult = StringUtils.substringBefore(robotResult, ",");
        boolean retFlag = false;
        for (Map<String, Object> map : list) {
            long triphaseId = MapUtils.getLong(map, "triphaseId");
            long deviceMeteId = MapUtils.getLong(map, "deviceMeteId");
            String triphaseKey = TRIPHASE_PREFIX + taskCode + ":" + triphaseId;
            Object lock = TRIPHASE_LOCK.computeIfAbsent(triphaseKey, v -> new Object());

            int triphaseType = MapUtils.getInteger(map, "triphaseType");
            float warnThreshold = MapUtils.getFloat(map, "warnThreshold", 0.0f);
            float fruit;
            String triphaseName;
            Map<String, String> triphaseRetMap;
            Map<String, String> triphaseNameMap = new HashMap<>();
            triphaseNameMap.put(MapUtils.getString(map, "instanceOneId"), MapUtils.getString(map, "instanceOneName"));
            triphaseNameMap.put(MapUtils.getString(map, "instanceTwoId"), MapUtils.getString(map, "instanceTwoName"));
            triphaseNameMap.put(MapUtils.getString(map, "instanceTriId"), MapUtils.getString(map, "instanceTriName"));
            synchronized (lock) {
                triphaseRetMap = redisTemplate.opsForHash().entries(triphaseKey);
                if (MapUtils.isEmpty(triphaseRetMap)) {
                    triphaseRetMap = new HashMap<>();
                    triphaseRetMap.put(MapUtils.getString(map, "instanceOneId"), "");
                    triphaseRetMap.put(MapUtils.getString(map, "instanceTwoId"), "");
                    triphaseRetMap.put(MapUtils.getString(map, "instanceTriId"), "");
                }
                triphaseRetMap.put(instanceId, robotInsResult);

                redisTemplate.opsForHash().putAll(triphaseKey, triphaseRetMap);

                if (triphaseRetMap.containsValue("")) {
                    continue;
                }
                String unit = "";
                if (NumberUtils.isCreatable(robotInsResult)) {
                    Set<Map.Entry<String, String>> triphaseStream = triphaseRetMap.entrySet();
                    Map.Entry<String, String> maxRet = triphaseStream.stream().max(Comparator.comparingDouble(v->NumberUtils.toFloat(v.getValue()))).get();
                    Map.Entry<String, String> minRet = triphaseStream.stream().min(Comparator.comparingDouble(v->NumberUtils.toFloat(v.getValue()))).get();
                    float max = NumberUtils.toFloat(maxRet.getValue());
                    float min = NumberUtils.toFloat(minRet.getValue());
                    float avg = (float)triphaseStream.stream().mapToDouble(v -> NumberUtils.toFloat(v.getValue())).average().orElse(0.0D);
                    boolean chooseMax = max - avg >= avg -min;
                    String alarmType = "4";
                    switch (triphaseType) {
                        case 1:
                            fruit = (max - 0F > 0.01D) ? (max - min) * 100 / max : 0.0F;
                            triphaseName = "三相不平衡";
                            unit = "%";
                            break;
                        case 2:
                        default:
                            fruit = max - min;
                            triphaseName = "三相温差";
                            alarmType = "3";
                            break;
                    }
                    log.info("三相告警结果值 ===max: {}, min: {}, fruit:{}, threshold: {}, {}", max, min, fruit, warnThreshold, JSON.toJSONString(triphaseRetMap));

                    TRIPHASE_LOCK.remove(triphaseKey);
                    if (fruit - warnThreshold > 1e-5) {
                        Map<String, Object> warnInfo = new HashMap<>(8);
                        warnInfo.put("warnId", warnId + triphaseType);
                        warnInfo.put("warnType", 5);
                        warnInfo.put("instanceId", triphaseId);
                        String insName = chooseMax ? triphaseNameMap.get(maxRet.getKey()) + "偏高" : triphaseNameMap.get(minRet.getKey()) + "偏低";
                        String warnContent = triphaseName + "【" + insName + "】告警：" + CommonUtils.percentFormat(fruit, "#.##") + unit + ", 阈值: " + warnThreshold + unit;
                        warnInfo.put("warnContent", warnContent);
                        List<Map<String, Object>> insResults = new ArrayList<>();
                        for (String insId : triphaseRetMap.keySet()) {
                            Map<String, Object> robotWarn = new HashMap<>(4);
                            robotWarn.put("taskId", taskCode);
                            robotWarn.put("inspectionId", insId);
                            robotWarn.put("warnId", warnId + triphaseType);
                            insResults.add(robotWarn);
                        }
                        warnInfo.put("resultsInfo", insResults);
                        warnInfo.put("value",CommonUtils.percentFormat(fruit, "#.##"));
                        warnInfo.put("deviceMeteId", deviceMeteId);
                        warnInfo.put("oneCruiseDeviceName", MapUtils.getString(map, "instanceOneName"));
                        warnInfo.put("twoCruiseDeviceName", MapUtils.getString(map, "instanceTwoName"));
                        warnInfo.put("threeCruiseDeviceName", MapUtils.getString(map, "instanceTriName"));
                        insertNonhomologousWarnInfo(warnInfo, alarmType);
                        retFlag = true;
                    }
                } else if (Constant.nonhomologousWarn.contains(robotInsResult)) {
                    log.info("三相告警值不是数字 === instanceId: {}, {}", instanceId, robotInsResult);
                    Map<Object, Long> countDiff = triphaseRetMap.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.counting()));
                    String diffVal = "";

                    TRIPHASE_LOCK.remove(triphaseKey);

                    if (countDiff.size() > 1){
                        for (Map.Entry<Object, Long> entry:countDiff.entrySet()) {
                            if (entry.getValue() == 1) {
                                diffVal = (String)entry.getKey();
                                break;
                            }
                        }
                        String name = triphaseNameMap.get(instanceId);
                        for (Map.Entry<String, String> entry : triphaseRetMap.entrySet()) {
                            if (StringUtils.equals(entry.getValue(), diffVal)) {
                                String key = entry.getKey();
                                name = triphaseNameMap.get(key);
                                break;
                            }
                        }
                        diffVal = StringUtils.isEmpty(diffVal) ? robotInsResult : diffVal;
                        triphaseName = "三相不一致,【" + name + "】告警: " + diffVal;

                        Map<String, Object> warnInfo = new HashMap<>(8);
                        warnInfo.put("warnId", warnId + triphaseType);
                        warnInfo.put("warnType", 5);
                        warnInfo.put("instanceId", triphaseId);
                        warnInfo.put("warnContent", triphaseName);
                        List<Map<String, Object>> insResults = new ArrayList<>();
                        for (String insId : triphaseRetMap.keySet()) {
                            Map<String, Object> robotWarn = new HashMap<>(4);
                            robotWarn.put("taskId", taskCode);
                            robotWarn.put("inspectionId", insId);
                            robotWarn.put("warnId", warnId + triphaseType);
                            insResults.add(robotWarn);
                        }
                        warnInfo.put("resultsInfo", insResults);
                        warnInfo.put("value", diffVal);
                        warnInfo.put("deviceMeteId", deviceMeteId);
                        warnInfo.put("oneCruiseDeviceName", MapUtils.getString(map, "instanceOneName"));
                        warnInfo.put("twoCruiseDeviceName", MapUtils.getString(map, "instanceTwoName"));
                        warnInfo.put("threeCruiseDeviceName", MapUtils.getString(map, "instanceTriName"));
                        insertNonhomologousWarnInfo(warnInfo, "4");
                        retFlag = true;
                    }
                } else {
                    log.info("三相告警值不支持 === instanceId: {}, {}", instanceId, robotInsResult);
                }
            }
        }
        return retFlag;
    }

    private boolean insertNonhomologousWarnInfo(Map<String,Object> warn, String alarmType){
        log.info("非同源告警---入库, warnmap==={}", warn);
        nonhomologousWarnDao.insertNonhomologousWarnInfo(warn);
        List<Map<String,Object>> insResults =(List<Map<String,Object>>) warn.get("resultsInfo");
        nonhomologousWarnDao.insertWarnInspections(insResults);
        log.info("非同源上报参数：warn={}，taskId={},instanceId={}",
                warn,robotPatrolTaskAlarm.getTaskCode(),robotPatrolTaskAlarm.getDeviceId());
        warnToUpSystem(warn, alarmType,
                robotPatrolTaskAlarm.getTaskCode(), robotPatrolTaskAlarm.getDeviceId());
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

    private void insertWarnInfo(String warnId, String instanceId,  Long deviceMeteId, String warnContent, String taskCode,
                                String robotInstanceId, String videoInstanceId, int warnType, String alarmType, String value,
                                String oneCruiseDeviceName, String twoCruiseDeviceName){
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
        warn.put("value", value);
        warn.put("deviceMeteId", deviceMeteId);
        warn.put("oneCruiseDeviceName", oneCruiseDeviceName);
        warn.put("twoCruiseDeviceName", twoCruiseDeviceName);
        insertNonhomologousWarnInfo(warn, alarmType);
    }

    private void warnToUpSystem(Map<String,Object> warn, String alarmType, String taskId,
                                String instanceId){
        try {
            String warnContent = warn.get("warnContent").toString();
            String warnType = warn.get("warnType").toString();
            String value = warn.get("value").toString();
            String warnInstanceId = warn.get("instanceId").toString();
            List<String> insList = new ArrayList<>();
            Map<String,Object> insMap;
            boolean robot3x = warn.containsKey("one") && warn.containsKey("two") && warn.containsKey("three");
            if ("5".equals(warnType)){
                if (robot3x){
                    insMap = warn;
                    instanceId = MapUtils.getString(warn, "one");
                }else {
                    insMap = nonhomologousWarnDao.selectInsListByTri(warnInstanceId);
                }
            } else {
                insMap = nonhomologousWarnDao.selectInsList(warnInstanceId);
            }
            if (!insMap.isEmpty()){
                insList.add(Optional.ofNullable(insMap.get("one")).orElse("").toString());
                insList.add(Optional.ofNullable(insMap.get("two")).orElse("").toString());
                insList.add(Optional.ofNullable(insMap.get("three")).orElse("").toString());
            }

            Map<String, Object> xmlItem = new HashMap<>(16);
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> xmlItems = new ArrayList<>();

            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
            log.info("cruiseResultMap=={}", cruiseResultMap);
            String devicePointId = Optional.ofNullable(cruiseResultMap.get("devicePointId")).orElse("");
            String simpleDateFormat = DateTimeUtil.format3(new Date());

            Map<String, String> patrolDevice = StaticContextAccessor.getBean(AnalyseDataOperateDao.class).selectPatrolDevice(instanceId);
            HashMap<String, String> typeAndPathName = getTypeAndPathName(cruiseResultMap);
            String taskCode = StaticContextAccessor.getBean(UPatrolTaskService.class).selectTaskCodeByTaskId(taskId);
            String stationCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeId", "content"));

            xmlItem.put("patroldevice_code", MapUtils.getString(patrolDevice, "patroldevice_code"));
            xmlItem.put("patroldevice_name", MapUtils.getString(patrolDevice, "patroldevice_name"));
            xmlItem.put("task_name", Optional.ofNullable(cruiseResultMap.get("taskName")).orElse(""));
            xmlItem.put("device_name", Optional.ofNullable(cruiseResultMap.get("instanceName")).orElse(""));
            xmlItem.put("device_id", robot3x ? robotPatrolTaskAlarm.getDeviceId() : Constant.standardPoints() ? devicePointId : instanceId);
            xmlItem.put("time", Optional.ofNullable(cruiseResultMap.get("cruiseTime")).orElse(""));
            xmlItem.put("file_type", typeAndPathName.getOrDefault("fileType", ""));
            xmlItem.put("recognition_type", typeAndPathName.getOrDefault("recognitionType", ""));
            xmlItem.put("task_code", taskCode);
            xmlItem.put("task_patrolled_id", stationCode + "_" + taskCode + "_" + cruiseResultMap.getOrDefault("startTime", simpleDateFormat));

            dealImg(xmlItem,insList,taskId);

            xmlBaseModel.setType("62");
            String alarmLevel = "2";
            String unit = cruiseResultMap.getOrDefault("unit", "");
            if (StringUtils.isEmpty(value)){
                value = Optional.ofNullable(cruiseResultMap.get("resultNum")).orElse("");
            }
            packageAlarmInfo(alarmLevel, value, unit, warnContent,  alarmType, xmlItem);

            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            String sysLevel = Constant.getLevelEdge();
            if (StringUtils.equals("1", sysLevel)) {
                xmlBaseModel.setCommand("1");
            }
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            map.put("list", list);
            log.info("非同源告警上报==={}", map);
            Constant.otherServer(map, Constant.TCP_URL);
        }catch (Exception e){
            log.info("非同源告警上报出错：",e);
        }
    }

    private void dealImg(Map<String, Object> xmlItem,List<String> insList,String taskId){
        log.info("处理图片：{}",insList);
        StringBuilder allTar = new StringBuilder("");
        StringBuilder allFileType = new StringBuilder("");
        String taskCode = StaticContextAccessor.getBean(UPatrolTaskService.class).selectTaskCodeByTaskId(taskId);
        String stationCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeId", "content"));
        String edgeCode = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeCode").get("content"));
        String simpleDateFormat = DateTimeUtil.format3(new Date());
        for (String instanceId:insList) {
            if (StringUtils.isNotEmpty(instanceId)){
                Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
                log.info("多张图片 cruiseResultMap=={}", cruiseResultMap);
                if (StringUtils.isNotEmpty(cruiseResultMap.get("picpath"))){
                    HashMap<String, String> typeAndPathName = getTypeAndPathName(cruiseResultMap);
                    // 文件后缀
                    String fileExt = StringUtils.substringAfterLast(cruiseResultMap.get("picpath"), ".");
                    fileExt = StringUtils.isEmpty(fileExt) ? "" : "." + fileExt;
                    // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR/设备点位ID_编码_时间.jpg
                    String tagPath = "alarm/"+stationCode + "/" + simpleDateFormat.substring(0, 4) + "/" + simpleDateFormat.substring(4, 6) + "/" + simpleDateFormat.substring(6,
                            8) + "/" + taskCode + typeAndPathName.get("fileNamePath") + instanceId + "_" + edgeCode + "_" + simpleDateFormat + fileExt;

                    String imgPath = Optional.ofNullable(cruiseResultMap.get("picpath")).orElse("");
                    imgPath = replaceResultImgPath(imgPath, false);
                    String fileType= typeAndPathName.getOrDefault("fileType", "2");

                    log.info("多张图片 imgPath==={},tagPath==={}", imgPath, tagPath);
                    StaticContextAccessor.getBean(AnalyseDataOperateService.class).uploadFileToUpFtps(imgPath, tagPath);
                    allTar.append(","+tagPath);
                    allFileType.append(","+fileType);
                }
            }
        }
        xmlItem.put("file_path", allTar.toString().replaceFirst(",",""));
        xmlItem.put("file_type", allFileType.toString().replaceFirst(",",""));
    }

    /**
     * 组装告警信息
     *
     * @param alarmLevel 告警等级
     * @param xmlItem
     * @return Map<String, String>
     */
    private Map<String, String> packageAlarmInfo(String alarmLevel,
                                                 String value, String unit,
                                                 String warnContent,
                                                 String alarmType,
                                                 Map<String, Object> xmlItem) {
        Map<String, String> resultPathMap = new HashMap<>(4);

        try {
            String val = Optional.ofNullable(value).orElse("");
            // 1-预警 2-一般 3-严重 4-危急
            xmlItem.put("alarm_level", Optional.ofNullable(alarmLevel).orElse(""));
            xmlItem.put("alarm_type", alarmType);
            xmlItem.put("value", val);
            xmlItem.put("unit", unit);
            xmlItem.put("value_unit", val + unit);
            xmlItem.put("content", warnContent);
            xmlItem.put("defect_type", "");
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return resultPathMap;
    }

    /**
     * 图片路径的绝对地址与相对地址的转换
     *
     * @param analyseResultImg 图片路径
     * @param flag true-绝对转相对 false-相对转绝对
     * @return java.lang.String
     */
    public String replaceResultImgPath(String analyseResultImg, boolean flag) {
        String resultImage = analyseResultImg;
        try {
            HashOperations<String, String, String> operations = redisTemplate.opsForHash();
            Map<String,String> map = operations.entries("t_sys_param:prefixAbsolutePath");
            String absPath = map.get("content");
            Map<String,String> entries = operations.entries("t_sys_param:prefixRelativePath");
            String relPath = entries.get("content");
            if (StringUtils.startsWithAny(analyseResultImg, absPath, relPath)) {
                resultImage = flag ? analyseResultImg.replace(absPath, relPath) : analyseResultImg.replace(relPath, absPath);
            } else {
                String filePath = operations.get("t_sys_param:fileAbsPath", "content");
                String fileUrl = operations.get("t_sys_param:fileRealPath", "content");
                assert fileUrl != null; assert filePath != null;
                resultImage = flag ? analyseResultImg.replace(filePath, fileUrl) : analyseResultImg.replace(fileUrl, filePath);
            }

            log.info("The image path after replacement is=={}", resultImage);
        }catch (Exception e){
            log.error("图片路径转换异常", e);
        }
        return resultImage;
    }

    /**
     * 组装识别类型和文件类型
     *
     * @param cruiseResultMap redis的数据
     * @return Map<String, String>
     */
    private HashMap<String, String> getTypeAndPathName(Map<String, String> cruiseResultMap){
        HashMap<String, String> map = new HashMap<>(4);
        // 识别类型、文件类型、文件名命名
        String recognitionType = cruiseResultMap.getOrDefault("recognitionType", "3");
        String fileType = cruiseResultMap.getOrDefault("fileType", "2");

        String fileNamePath = CCD_PATH;
        if (StringUtils.equals("4", recognitionType)){
            fileNamePath = FIR_PATH;
        }else if (StringUtils.equals("5", recognitionType)){
            fileNamePath = AUDIO_PATH;
        }
        map.put("recognitionType", recognitionType);
        map.put("fileNamePath", fileNamePath);
        map.put("fileType", fileType);
        return map;
    }

}
