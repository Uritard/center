package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                            //特殊走特殊的逻辑
                            if (Constant.nonhomologousWarn.contains(robotInsResult)){
                               String lastValue = nonhomologousWarnDao.selectLastResultNum(robotInstanceId,Constant.nonhomologousWarn.split(","));
                                if (StringUtils.isNotEmpty(lastValue) && !lastValue.equals(robotInsResult)){
                                    //告警
                                    Map<String,Object> warn = new HashMap<>(4);
                                    warn.put("warnId", warnId);
                                    warn.put("warnType", 6);
                                    warn.put("instanceId", Long.parseLong(instanceId));
                                    warn.put("warnContent", "时间范围内识别结果趋势不一致：" + warnThreshold);
                                    insertNonhomologousWarnInfo(warn);
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
            float warnThreshold = MapUtils.getFloat(map, "warnThreshold", 0.0f);
            float fruit;
            String triphaseName;
            Map<String, String> triphaseRetMap;
            synchronized (lock) {
                triphaseRetMap = redisTemplate.opsForHash().entries(triphaseKey);
                if (MapUtils.isEmpty(triphaseRetMap)) {
                    triphaseRetMap = new HashMap<>();
                    triphaseRetMap.put(StringUtils.substringBefore(MapUtils.getString(map, "instanceOneId"), ","), "");
                    triphaseRetMap.put(StringUtils.substringBefore(MapUtils.getString(map, "instanceTwoId"), ","), "");
                    triphaseRetMap.put(StringUtils.substringBefore(MapUtils.getString(map, "instanceTriId"), ","), "");
                }
                triphaseRetMap.put(instanceId, robotInsResult);

                redisTemplate.opsForHash().putAll(triphaseKey, triphaseRetMap);

                if (triphaseRetMap.containsValue("")) {
                    continue;
                }

                if (NumberUtils.isCreatable(robotInsResult)){
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

                    TRIPHASE_LOCK.remove(triphaseKey);
                    if (fruit > warnThreshold) {
                        Map<String, Object> warnInfo = new HashMap<>(8);
                        warnInfo.put("warnId", warnId + triphaseType);
                        warnInfo.put("warnType", 5);
                        warnInfo.put("instanceId", triphaseId);
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
                }else {
                    log.info("三相告警值不是数字 === instanceId: {}, {}", instanceId, robotInsResult);
                    Map<String, String> triphaseRetMapItem = new HashMap<>(4);
                    long valueIsNotEmptyCount = triphaseRetMap.values().stream().filter(trm -> !trm.isEmpty()).count();
                    triphaseRetMap.forEach((key, value) -> {
                        if (valueIsNotEmptyCount > 1 && StringUtils.isNotEmpty(value)){
                            triphaseRetMapItem.put(value, key);
                        }
                    });
                    triphaseName = "三相不一致";
                    TRIPHASE_LOCK.remove(triphaseKey);

                    if (triphaseRetMapItem.size() > 1){
                        Map<String, Object> warnInfo = new HashMap<>(8);
                        warnInfo.put("warnId", warnId + triphaseType);
                        warnInfo.put("warnType", 5);
                        warnInfo.put("instanceId", triphaseId);
                        warnInfo.put("warnContent", triphaseName + "告警");
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
            }
        }
        return retFlag;
    }

    private boolean insertNonhomologousWarnInfo(Map<String,Object> warn){
        log.info("非同源告警---入库, warnmap==={}", warn);
        nonhomologousWarnDao.insertNonhomologousWarnInfo(warn);
        List<Map<String,Object>> insResults =(List<Map<String,Object>>) warn.get("resultsInfo");
        nonhomologousWarnDao.insertWarnInspections(insResults);
        log.info("非同源上报参数：warnContent={}，warnType={}，taskId={},instanceId={},triphase_id={}",
                warn.get("warnContent").toString(),warn.get("warnType").toString(),robotPatrolTaskAlarm.getTaskCode(),robotPatrolTaskAlarm.getDeviceId(),warn.get("instanceId"));
        warnToUpSystem(warn.get("warnContent").toString(),warn.get("warnType").toString(),
                robotPatrolTaskAlarm.getTaskCode(),robotPatrolTaskAlarm.getDeviceId(),
                warn.get("instanceId").toString());
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

    private void warnToUpSystem(String warnContent,String warnType,String taskId,String instanceId,String triphaseId){
        try {
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
            String edgeCode = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeCode").get("content"));

            xmlItem.put("patroldevice_code", MapUtils.getString(patrolDevice, "patroldevice_code"));
            xmlItem.put("patroldevice_name", MapUtils.getString(patrolDevice, "patroldevice_name"));
            xmlItem.put("task_name", Optional.ofNullable(cruiseResultMap.get("taskName")).orElse(""));
            xmlItem.put("device_name", Optional.ofNullable(cruiseResultMap.get("instanceName")).orElse(""));
            xmlItem.put("device_id", Constant.standardPoints() ? devicePointId : instanceId);
            xmlItem.put("time", Optional.ofNullable(cruiseResultMap.get("cruiseTime")).orElse(""));
            xmlItem.put("file_type", typeAndPathName.getOrDefault("fileType", ""));
            xmlItem.put("recognition_type", typeAndPathName.getOrDefault("recognitionType", ""));
            xmlItem.put("task_code", taskCode);
            xmlItem.put("task_patrolled_id", stationCode + "_" + taskCode + "_" + cruiseResultMap.getOrDefault("startTime", simpleDateFormat));

            // 文件后缀
            String fileExt = StringUtils.substringAfterLast(cruiseResultMap.get("picpath"), ".");
            fileExt = StringUtils.isEmpty(fileExt) ? "" : "." + fileExt;
            // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR/设备点位ID_编码_时间.jpg
            String tagPath = "alarm/"+stationCode + "/" + simpleDateFormat.substring(0, 4) + "/" + simpleDateFormat.substring(4, 6) + "/" + simpleDateFormat.substring(6,
                    8) + "/" + taskCode + typeAndPathName.get("fileNamePath") + instanceId + "_" + edgeCode + "_" + simpleDateFormat + fileExt;
            xmlBaseModel.setType("62");

            String value = Optional.ofNullable(cruiseResultMap.get("resultNum")).orElse("");
            String imgPath = Optional.ofNullable(cruiseResultMap.get("picPath")).orElse("");
            String alarmLevel = "2";

            Map<String, String> resMap = packageAlarmInfo(alarmLevel, value, warnContent, imgPath, warnType,triphaseId,xmlItem, tagPath);
            log.info("imgPath==={},tagPath==={}", resMap.get("imgPath"), resMap.get("tagPath"));
            StaticContextAccessor.getBean(AnalyseDataOperateService.class).uploadFileToUpFtps(resMap.get("imgPath"), tagPath);
            xmlItem.put("file_path", tagPath);
            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"));
            if (StringUtils.equals("1", sysLevel)) {
                xmlBaseModel.setCommand("1");
            }
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            map.put("list", list);
            log.info("The {} information to be reported one level up is==={}",
                    StringUtils.equals("61", xmlBaseModel.getType()) ? "cruiseResult" : "alarm", map);
            Constant.otherServer(map, Constant.TCP_URL);
        }catch (Exception e){
            log.info("非同源告警上报出错：",e);
        }
    }

    /**
     * 组装告警信息
     *
     * @param alarmLevel 告警等级
     * @param xmlItem
     * @param tagPath
     * @return Map<String, String>
     */
    private Map<String, String> packageAlarmInfo(String alarmLevel,
                                                 String value,
                                                 String warnContent,
                                                 String imagePath,
                                                 String warType,
                                                 String triphaseId,
                                                 Map<String, Object> xmlItem, String tagPath) {
        Map<String, String> resultPathMap = new HashMap<>(4);
        log.info("imagePath=={}", imagePath);
        imagePath = replaceResultImgPath(imagePath, false);

        try {
            String alarmType = "";
            switch (warType){
                case "1":
                    alarmType = "1";
                    break;
                case "2":
                    alarmType = "10";
                    break;
                case "5":
                    TCruiseTriphaseRule rule = StaticContextAccessor.getBean(TCruiseTriphaseRuleDao.class).selectByPrimaryId(Long.valueOf(triphaseId));
                    alarmType = "3";
                    if (rule.getTriphaseType() == 3){
                        alarmType = "4";
                    }
                    break;
                case "3":
                case "4":
                case "7":
                case "6":
                    alarmType = "10";
                    break;
                default:
                    break;
            }
            // 1-预警 2-一般 3-严重 4-危急
            xmlItem.put("alarm_level", Optional.ofNullable(alarmLevel).orElse(""));
            xmlItem.put("alarm_type", alarmType);
            xmlItem.put("value", Optional.ofNullable(value).orElse(""));
            xmlItem.put("unit", "");
            xmlItem.put("value_unit", "");
            xmlItem.put("content", warnContent);
            xmlItem.put("defect_type", "");
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        resultPathMap.put("imgPath", imagePath);
        resultPathMap.put("tagPath", tagPath);
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
