package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.patrol.dao.NonhomologousWarnDao;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * @author sunjinyan
 * @since 2022-04-08
 * 机器人侧非同源告警处理线程
 */
@lombok.extern.slf4j.Slf4j
public class NonhomologousWarnThread implements Runnable{

    private final RedisTemplate redisTemplate;
    private final RobotPatrolTaskResult robotPatrolTaskResult;
    private final int isResult;
    private final NonhomologousWarnDao nonhomologousWarnDao;

    public NonhomologousWarnThread(RobotPatrolTaskResult robotPatrolTaskResult, RedisTemplate redisTemplate, int isResult){
        this.robotPatrolTaskResult = robotPatrolTaskResult;
        this.redisTemplate = redisTemplate;
        this.isResult = isResult;
        this.nonhomologousWarnDao = StaticContextAccessor.getBean(NonhomologousWarnDao.class);
    }

    @Override
    public void run(){
        try {
            log.info("开始处理巡检结果并生成相应的非同源告警 >>>>>>> robotPatrolTaskResult==={}, isResult ==={}", JSON.toJSONString(robotPatrolTaskResult), isResult);
            String taskCode = robotPatrolTaskResult.getTaskCode();
            String robotInsResult = robotPatrolTaskResult.getValue();

            String warnId = String.valueOf(UUID.randomUUID()).replace("-", "");
            if (0 == isResult){
                // 三相告警
                Map<String,Object> warnInfo = new HashMap<>(6);
                warnInfo.put("warnId", warnId);
                warnInfo.put("warnType", 5);
                warnInfo.put("instanceId", null);
//                warnInfo.put("warnContent", "机器人相别告警：" + cruiseResultMap.get("content"));
                List<Map<String, Object>> insResults = new ArrayList<>();
                String[] deviceIdArray = robotPatrolTaskResult.getDeviceId().split(",");
                for(String deviceId : deviceIdArray){
                    Map<String, Object> robotWarn = new HashMap<>(4);
                    robotWarn.put("taskId", taskCode);
                    robotWarn.put("inspectionId", nonhomologousWarnDao.getInstanceIdByDeviceId(deviceId, taskCode));
                    robotWarn.put("warnId", warnId);
                    insResults.add(robotWarn);
                }
                warnInfo.put("resultsInfo", insResults);
                insertNonhomologousWarnInfo(warnInfo);
            }else {
                // 需要判断的非同源告警
                judgeNonhomologousWarn(taskCode, robotInsResult, warnId);
            }
        } catch (Exception e) {
            log.error("非同源告警处理异常：", e);
        }
    }

    private void judgeNonhomologousWarn(String taskCode, String robotInsResult, String warnId) {
        // 查询非同源告警规则
        Map cruiseResultMap = Collections.EMPTY_MAP;
        List<Map<String, Object>> list = nonhomologousWarnDao.getNonhomologousInspections(cruiseResultMap);
        for(Map<String, Object> map : list){
            if (!Objects.isNull(map.get("robotInspectionName")) && !Objects.isNull(map.get("warnType"))){
                String warnType = String.valueOf(map.get("warnType"));
                String warnThreshold = String.valueOf(map.get("warnThreshold"));
                String robotInstanceId = String.valueOf(map.get("robotInstanceId"));
                String videoInstanceId = String.valueOf(map.get("videoInstanceId"));
                String instanceId = String.valueOf(map.get("instanceId"));

                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskCode + ":" + videoInstanceId);
                // 1-红外 2-位置 3-表计数显 4-表计指针 5-相别 6-区间 7-五次不变
                switch (warnType){
                    case "1":
                        if (Objects.isNull(map.get("videoInspectionName"))){
                            break;
                        }
                        if(!redisInfoMap.isEmpty() && StringUtils.isNotEmpty(redisInfoMap.get("cruiseResultId"))
                                && !checkWarnExist(robotInstanceId, taskCode, instanceId)){

                            String videoInsResult = StringUtils.substringBefore(redisInfoMap.get("resultNum"), ",");
                            if(isNumeric(warnThreshold) && isNumeric(robotInsResult) && isNumeric(videoInsResult)){
                                if(Math.abs(Double.parseDouble(robotInsResult) - Double.parseDouble(videoInsResult)) > Double.parseDouble(warnThreshold)){
                                    Map<String,Object> warn = new HashMap<>(4);
                                    warn.put("warnId", warnId);
                                    warn.put("warnType", 1);
                                    warn.put("instanceId", Double.parseDouble(instanceId));
                                    warn.put("warnContent", "红外测温非同源结果差值超过阈值：" + warnThreshold);
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
                            }else{
                                log.info("robotInsResult为==={},videoInsResult为==={},阈值是==={},红外非同源告警--数据非数字", robotInsResult, videoInsResult, warnThreshold);
                            }
                        }else{
                            log.info("非同源告警---结果为空或告警已存在,redismap==={}", redisInfoMap);
                        }
                        break;
                    case "2":
                        if (Objects.isNull(map.get("videoInspectionName"))){
                            break;
                        }
                        if(!redisInfoMap.isEmpty() && StringUtils.isNotEmpty(redisInfoMap.get("cruiseResultId"))
                                && !checkWarnExist(robotInstanceId, taskCode, instanceId)){

                            String videoInsResult = redisInfoMap.get("resultNum");
                            if (!Objects.equals(robotInsResult, videoInsResult)) {
                                Map<String, Object> warn = new HashMap<>(4);
                                warn.put("warnId", warnId);
                                warn.put("warnType", 2);
                                warn.put("instanceId", Long.parseLong(instanceId));
                                warn.put("warnContent", "位置状态非同源结果不一致");
                                List<Map<String, Object>> insResults = new ArrayList<>();
                                Map<String, Object> robotWarn = new HashMap<>(4);
                                robotWarn.put("taskId", taskCode);
                                robotWarn.put("inspectionId", robotInstanceId);
                                robotWarn.put("warnId", warnId);
                                insResults.add(robotWarn);

                                Map<String, Object> videoWarn = new HashMap<>(4);
                                videoWarn.put("taskId", taskCode);
                                videoWarn.put("inspectionId", videoInstanceId);
                                videoWarn.put("warnId", warnId);
                                insResults.add(videoWarn);

                                warn.put("resultsInfo", insResults);
                                insertNonhomologousWarnInfo(warn);
                            } else {
                                log.info("robotInsResult为==={},videoInsResult为==={},位置状态非同源告警--结果", robotInsResult, videoInsResult);
                            }
                        }else{
                            log.info("非同源告警---结果为空或告警已存在,redismap==={}", redisInfoMap);
                        }
                        break;
                    case "3":
                        if (Objects.isNull(map.get("videoInspectionName"))){
                            break;
                        }
                        if(!redisInfoMap.isEmpty() && StringUtils.isNotEmpty(redisInfoMap.get("cruiseResultId"))
                                && !checkWarnExist(robotInstanceId, taskCode, instanceId)){

                            String videoInsResult = redisInfoMap.get("resultNum");
                            if (!Objects.equals(robotInsResult, videoInsResult)) {
                                Map<String, Object> warn = new HashMap<>(4);
                                warn.put("warnId", warnId);
                                warn.put("warnType", 3);
                                warn.put("instanceId", Long.parseLong(instanceId));
                                warn.put("warnContent", "数显类表计识别非同源结果不一致");
                                List<Map<String, Object>> insResults = new ArrayList<>();
                                Map<String, Object> robotWarn = new HashMap<>(4);
                                robotWarn.put("taskId", taskCode);
                                robotWarn.put("inspectionId", robotInstanceId);
                                robotWarn.put("warnId", warnId);
                                insResults.add(robotWarn);

                                Map<String, Object> videoWarn = new HashMap<>(4);
                                videoWarn.put("taskId", taskCode);
                                videoWarn.put("inspectionId", videoInstanceId);
                                videoWarn.put("warnId", warnId);
                                insResults.add(videoWarn);

                                warn.put("resultsInfo", insResults);
                                insertNonhomologousWarnInfo(warn);
                            } else {
                                log.info("robotInsResult为==={},videoInsResult为==={},表计-数显非同源告警--结果不一致", robotInsResult, videoInsResult);
                            }
                        }else{
                            log.info("非同源告警---结果为空或告警已存在,redismap==={}", redisInfoMap);
                        }
                        break;
                    case "4":
                        if (Objects.isNull(map.get("videoInspectionName"))){
                            break;
                        }
                        if(!redisInfoMap.isEmpty() && StringUtils.isNotEmpty(redisInfoMap.get("cruiseResultId"))
                                && !checkWarnExist(robotInstanceId, taskCode, instanceId)){

                            String videoInsResult = redisInfoMap.get("resultNum");
                            if(isNumeric(warnThreshold) && isNumeric(robotInsResult) && isNumeric(videoInsResult)){
                                if(Math.abs(Double.parseDouble(robotInsResult) - Double.parseDouble(videoInsResult)) > Double.parseDouble(warnThreshold)){
                                    Map<String,Object> warn = new HashMap<>(4);
                                    warn.put("warnId", warnId);
                                    warn.put("warnType", 4);
                                    warn.put("instanceId", Long.parseLong(instanceId));
                                    warn.put("warnContent", "指针类表计识别非同源结果差值超过阈值：" + warnThreshold);
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
                            }else{
                                log.info("robotInsResult为==={},videoInsResult为==={},阈值是==={},表计-指针非同源告警--数据非数字", robotInsResult, videoInsResult,warnThreshold);
                            }
                        }else{
                            log.info("非同源告警---结果为空或告警已存在,redismap==={}", redisInfoMap);
                        }
                        break;
                    case "5":
                        break;
                    case "6":
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
                        Optional<Map<String,Object>> maxValueResultInfo = numResults.stream()
                                .collect(Collectors.reducing(
                                        (x,y) -> Double.parseDouble(x.get("resultValue").toString()) > Double.parseDouble(y.get("resultValue").toString()) ? x:y));
                        Optional<Map<String,Object>> minValueResultInfo = numResults.stream()
                                .collect(Collectors.reducing(
                                        (x,y) -> Double.parseDouble(x.get("resultValue").toString()) < Double.parseDouble(y.get("resultValue").toString()) ? x:y));
                        log.info("区间非同源判断：maxInsResult为==={},minInsResult为==={},阈值是==={}", maxValueResultInfo, minValueResultInfo, warnThreshold);
                        if((Double.parseDouble(maxValueResultInfo.get().get("resultValue").toString()) -
                                Double.parseDouble(minValueResultInfo.get().get("resultValue").toString())) >
                                Double.parseDouble(warnThreshold)){
                            Map<String,Object> warn = new HashMap<>(4);
                            warn.put("warnId", warnId);
                            warn.put("warnType", 6);
                            warn.put("instanceId", Long.parseLong(instanceId));
                            warn.put("warnContent", "时间范围内表计识别结果差值超过阈值：" + warnThreshold);
                            List<Map<String,Object>> insResults = new ArrayList<>();
                            intervalResults.stream().forEach(i -> i.put("warnId", warn.get("warnId")));
                            insResults.addAll(numResults);
                            warn.put("resultsInfo", insResults);
                            insertNonhomologousWarnInfo(warn);
                        }
                        break;
                    case "7":
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
                            List<Map<String,Object>> insResults = new ArrayList<>();
                            fiveResults.stream().forEach(i -> i.put("warnId", warn.get("warnId")));
                            insResults.addAll(fiveResults);
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

}
