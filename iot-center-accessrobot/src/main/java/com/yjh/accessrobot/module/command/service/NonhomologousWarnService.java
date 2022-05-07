package com.yjh.accessrobot.module.command.service;


import com.yjh.accessrobot.module.command.dao.NonhomologousWarnDao;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author sunjinyan
 * @since 2022-04-08
 */
@Service
public class NonhomologousWarnService {

    private final Logger log = LoggerFactory.getLogger(NonhomologousWarnService.class);

    String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    private static final String OFF_LINE = "离线";

    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private NonhomologousWarnDao nonhomologousWarnDao;
    @Value("${other.webSocketUrl}")
    private String webSocketUrl;
    @Value("${netty.server.url}")
    private String serverUrl;
    @Value("${netty.server.ftps.port}")
    private String ftpsPort;
    @Value("${netty.server.ftps.username}")
    private String ftpsUserName;
    @Value("${netty.server.ftps.password}")
    private String ftpsPassWord;
    @Value("${netty.server.ftps.keypw}")
    private String key;
    @Value("${netty.server.ftps.local.path}")
    private String ftpsLocalPath;
    @Value("${netty.server.name}")
    private String sendCode;

    @Value("${other.webSocketUrl}")
    private String websocketUrl;


    // 通过 -?[0-9]+(\\\\.[0-9]+)? 进行匹配是否为数字
    private static final Pattern pattern = Pattern.compile("-?[0-9]+(\\\\.[0-9]+)?");
    /**
     * 非同源告警处理
     * @param cruiseResultMap 巡检结果
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertNonhomologousWarn(Map<String,String> cruiseResultMap,int isResult) {
        String warnId = String.valueOf(UUID.randomUUID()).replace("-", "");
        if(1==isResult){
            //查询非同源告警规则
            List<Map<String,Object>> nonhomologousInstances = nonhomologousWarnDao.getNonhomologousInspections(cruiseResultMap);
            for(Map<String,Object> nonhomologousInspections : nonhomologousInstances){
                if(!Objects.isNull(nonhomologousInspections.get("robotInspectionName"))&&!Objects.isNull(nonhomologousInspections.get("warnType"))){
                    switch (Integer.parseInt(nonhomologousInspections.get("warnType").toString())) {
                        // 红外
                        case 1:
                            if(!Objects.isNull(nonhomologousInspections.get("robotInspectionName"))&&!Objects.isNull(nonhomologousInspections.get("videoInspectionName"))){
                                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + cruiseResultMap.get("taskCode") + ":" + nonhomologousInspections.get("videoInstanceId").toString());
                                if(redisInfoMap!=null&&redisInfoMap.get("cruiseResultId")!=null&&!checkWarnExist(nonhomologousInspections.get("robotInstanceId").toString(),cruiseResultMap.get("taskCode"),nonhomologousInspections.get("instanceId").toString())){
                                    String warnThreshold = nonhomologousInspections.get("warnThreshold").toString();
                                    String robotInsResult = cruiseResultMap.get("value");
                                    String videoInsResult = redisInfoMap.get("resultNum");
                                    if(isNumeric(warnThreshold)&&isNumeric(robotInsResult)&&isNumeric(videoInsResult)){
                                        if(Math.abs(Double.parseDouble(robotInsResult)-Double.parseDouble(videoInsResult))>Double.parseDouble(warnThreshold)){
                                            Map<String,Object> warn = new HashMap<>();
                                            warn.put("warnId", warnId);
                                            warn.put("warnType",1);
                                            warn.put("instanceId",Double.parseDouble(nonhomologousInspections.get("instanceId").toString()));
                                            warn.put("warnContent","红外测温非同源结果差值超过阈值："+nonhomologousInspections.get("warnThreshold").toString());
                                            List<Map<String,Object>> insResults = new ArrayList<>();
                                            Map<String,Object> robotWarn = new HashMap<>();
                                            robotWarn.put("taskId",cruiseResultMap.get("taskCode"));
                                            robotWarn.put("inspectionId",nonhomologousInspections.get("robotInstanceId").toString());
                                            Map<String,Object> videoWarn = new HashMap<>();
                                            videoWarn.put("taskId",cruiseResultMap.get("taskCode"));
                                            videoWarn.put("inspectionId",nonhomologousInspections.get("videoInstanceId").toString());
                                            robotWarn.put("warnId",warn.get("warnId"));
                                            videoWarn.put("warnId",warn.get("warnId"));
                                            insResults.add(robotWarn);
                                            insResults.add(videoWarn);
                                            warn.put("resultsInfo",insResults);
                                            insertNonhomologousWarnInfo(warn);
                                        }
                                    }else{
                                        log.info("robotInsResult为==={},videoInsResult为==={},阈值是==={},红外非同源告警--数据非数字", robotInsResult, videoInsResult,warnThreshold);
                                    }
                                }else{
                                    log.info("非同源告警---结果为空或告警已存在,redismap==={}",redisInfoMap);
                                }
                            }else{
                                break;
                            }
                            break;
                        // 位置
                        case 2:
                            if(!Objects.isNull(nonhomologousInspections.get("robotInspectionName"))&&!Objects.isNull(nonhomologousInspections.get("videoInspectionName"))){
                                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + cruiseResultMap.get("taskCode") + ":" + nonhomologousInspections.get("videoInstanceId").toString());
                                if(redisInfoMap!=null&&redisInfoMap.get("cruiseResultId")!=null&&!checkWarnExist(nonhomologousInspections.get("robotInstanceId").toString(),cruiseResultMap.get("taskCode"),nonhomologousInspections.get("instanceId").toString())) {
                                    String robotInsResult = cruiseResultMap.get("value");
                                    String videoInsResult = redisInfoMap.get("resultNum");
                                    if (!Objects.equals(robotInsResult, videoInsResult)) {
                                        Map<String, Object> warn = new HashMap<>();
                                        warn.put("warnId", warnId);
                                        warn.put("warnType", 2);
                                        warn.put("instanceId", Long.parseLong(nonhomologousInspections.get("instanceId").toString()));
                                        warn.put("warnContent", "位置状态非同源结果不一致");
                                        List<Map<String, Object>> insResults = new ArrayList<>();
                                        Map<String, Object> robotWarn = new HashMap<>();
                                        robotWarn.put("taskId", cruiseResultMap.get("taskCode"));
                                        robotWarn.put("inspectionId", nonhomologousInspections.get("robotInstanceId").toString());
                                        Map<String, Object> videoWarn = new HashMap<>();
                                        videoWarn.put("taskId", cruiseResultMap.get("taskCode"));
                                        videoWarn.put("inspectionId", nonhomologousInspections.get("videoInstanceId").toString());
                                        robotWarn.put("warnId",warn.get("warnId"));
                                        videoWarn.put("warnId",warn.get("warnId"));
                                        insResults.add(robotWarn);
                                        insResults.add(videoWarn);
                                        warn.put("resultsInfo", insResults);
                                        insertNonhomologousWarnInfo(warn);
                                    } else {
                                        log.info("robotInsResult为==={},videoInsResult为==={},位置状态非同源告警--结果", robotInsResult, videoInsResult);
                                    }
                                }else{
                                    log.info("非同源告警---结果为空或告警已存在,redismap==={}",redisInfoMap);
                                }
                            }else{
                                break;
                            }
                            break;
                        // 表计-数显
                        case 3:
                            if(!Objects.isNull(nonhomologousInspections.get("robotInspectionName"))&&!Objects.isNull(nonhomologousInspections.get("videoInspectionName"))){
                                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + cruiseResultMap.get("taskCode") + ":" + nonhomologousInspections.get("videoInstanceId").toString());
                                if(redisInfoMap!=null&&redisInfoMap.get("cruiseResultId")!=null&&!checkWarnExist(nonhomologousInspections.get("robotInstanceId").toString(),cruiseResultMap.get("taskCode"),nonhomologousInspections.get("instanceId").toString())) {
                                    String robotInsResult = cruiseResultMap.get("value");
                                    String videoInsResult = redisInfoMap.get("resultNum");
                                    if (!Objects.equals(robotInsResult, videoInsResult)) {
                                        Map<String, Object> warn = new HashMap<>();
                                        warn.put("warnId", warnId);
                                        warn.put("warnType", 3);
                                        warn.put("instanceId", Long.parseLong(nonhomologousInspections.get("instanceId").toString()));
                                        warn.put("warnContent", "数显类表计识别非同源结果不一致");
                                        List<Map<String, Object>> insResults = new ArrayList<>();
                                        Map<String, Object> robotWarn = new HashMap<>();
                                        robotWarn.put("taskId", cruiseResultMap.get("taskCode"));
                                        robotWarn.put("inspectionId", nonhomologousInspections.get("robotInstanceId").toString());
                                        Map<String, Object> videoWarn = new HashMap<>();
                                        videoWarn.put("taskId", cruiseResultMap.get("taskCode"));
                                        videoWarn.put("inspectionId", nonhomologousInspections.get("videoInstanceId").toString());
                                        robotWarn.put("warnId",warn.get("warnId"));
                                        videoWarn.put("warnId",warn.get("warnId"));
                                        insResults.add(robotWarn);
                                        insResults.add(videoWarn);
                                        warn.put("resultsInfo", insResults);
                                        insertNonhomologousWarnInfo(warn);
                                    } else {
                                        log.info("robotInsResult为==={},videoInsResult为==={},表计-数显非同源告警--结果不一致", robotInsResult, videoInsResult);
                                    }
                                }else{
                                    log.info("非同源告警---结果为空或告警已存在,redismap==={}",redisInfoMap);
                                }
                            }else{
                                break;
                            }
                            break;
                        // 表计-指针
                        case 4:
                            if(!Objects.isNull(nonhomologousInspections.get("robotInspectionName"))&&!Objects.isNull(nonhomologousInspections.get("videoInspectionName"))){
                                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + cruiseResultMap.get("taskCode") + ":" + nonhomologousInspections.get("videoInstanceId").toString());
                                if(redisInfoMap!=null&&redisInfoMap.get("cruiseResultId")!=null&&!checkWarnExist(nonhomologousInspections.get("robotInstanceId").toString(),cruiseResultMap.get("taskCode"),nonhomologousInspections.get("instanceId").toString())){
                                    String warnThreshold = nonhomologousInspections.get("warnThreshold").toString();
                                    String robotInsResult = cruiseResultMap.get("value");
                                    String videoInsResult = redisInfoMap.get("resultNum");
                                    if(isNumeric(warnThreshold)&&isNumeric(robotInsResult)&&isNumeric(videoInsResult)){
                                        if(Math.abs(Double.parseDouble(robotInsResult)-Double.parseDouble(videoInsResult))>Double.parseDouble(warnThreshold)){
                                            Map<String,Object> warn = new HashMap<>();
                                            warn.put("warnId", warnId);
                                            warn.put("warnType",4);
                                            warn.put("instanceId",Long.parseLong(nonhomologousInspections.get("instanceId").toString()));
                                            warn.put("warnContent","指针类表计识别非同源结果差值超过阈值："+nonhomologousInspections.get("warnThreshold").toString());
                                            List<Map<String,Object>> insResults = new ArrayList<>();
                                            Map<String,Object> robotWarn = new HashMap<>();
                                            robotWarn.put("taskId",cruiseResultMap.get("taskCode"));
                                            robotWarn.put("inspectionId",nonhomologousInspections.get("robotInstanceId").toString());
                                            robotWarn.put("warnId",warn.get("warnId"));
                                            Map<String,Object> videoWarn = new HashMap<>();
                                            videoWarn.put("taskId",cruiseResultMap.get("taskCode"));
                                            videoWarn.put("inspectionId",nonhomologousInspections.get("videoInstanceId").toString());
                                            videoWarn.put("warnId",warn.get("warnId"));
                                            insResults.add(robotWarn);
                                            insResults.add(videoWarn);
                                            warn.put("resultsInfo",insResults);
                                            insertNonhomologousWarnInfo(warn);
                                        }
                                    }else{
                                        log.info("robotInsResult为==={},videoInsResult为==={},阈值是==={},表计-指针非同源告警--数据非数字", robotInsResult, videoInsResult,warnThreshold);
                                    }
                                }else{
                                    log.info("非同源告警---结果为空或告警已存在,redismap==={}",redisInfoMap);
                                }
                            }else{
                                break;
                            }
                            break;
                        // 相别
                        case 5:
                            break;
                        // 区间
                        case 6:
                                int timeType = Integer.parseInt(nonhomologousInspections.get("intervalType").toString());
                                Date endTime= new Date();
                                Date startTime = null;
                                switch (timeType) {
                                    // 天
                                    case 1:
                                        Calendar dayNow = Calendar.getInstance();
                                        dayNow.setTime(endTime);
                                        dayNow.add(Calendar.DAY_OF_YEAR, -1);//日期减1天
                                        startTime = dayNow.getTime();
                                        break;
                                    //周
                                    case 2:
                                        Calendar weekNow = Calendar.getInstance();
                                        weekNow.setTime(endTime);
                                        weekNow.add(Calendar.DAY_OF_YEAR, -7);//日期减7天
                                        startTime = weekNow.getTime();
                                        break;
                                    //月
                                    case 3:
                                        Calendar monthNow = Calendar.getInstance();
                                        monthNow.setTime(endTime);
                                        monthNow.add(Calendar.MONTH, -1);//日期减1月
                                        startTime = monthNow.getTime();
                                        break;
                                    default:
                                        break;
                                }
                                Map<String,Object> intervalResultsParam = new HashMap<>();
                                intervalResultsParam.put("type",6);
                                intervalResultsParam.put("startTime",startTime);
                                intervalResultsParam.put("endTime",endTime);
                                intervalResultsParam.put("inspectionId",nonhomologousInspections.get("robotInstanceId"));
                                //分时段查出历史结果
                                List<Map<String,Object>> intervalResults = nonhomologousWarnDao.selectWarnResults(intervalResultsParam);
                                if(intervalResults.size()==0){
                                    log.info("区间非同源告警--历史数据为空");
                                    break;
                                }
                                //加入当前结果对象
                                Map<String,Object> nowResult = new HashMap<>();
                                nowResult.put("taskId",cruiseResultMap.get("taskCode"));
                                nowResult.put("inspectionId",nonhomologousInspections.get("robotInstanceId").toString());
                                nowResult.put("resultValue",cruiseResultMap.get("value"));
                                //过滤非数字结果
                                intervalResults.add(nowResult);
                                List<Map<String,Object>> numResults= intervalResults.stream()
                                        .filter(map -> isNumeric(map.get("resultValue").toString()))
                                        .collect(Collectors.toList());
                                Optional<Map<String,Object>> maxValueResultInfo = numResults.stream()
                                        .collect(Collectors.reducing(
                                                (x,y) -> Double.parseDouble(x.get("resultValue").toString()) > Double.parseDouble(y.get("resultValue").toString()) ? x:y));
                                Optional<Map<String,Object>> minValueResultInfo = numResults.stream()
                                        .collect(Collectors.reducing(
                                                (x,y) -> Double.parseDouble(x.get("resultValue").toString()) < Double.parseDouble(y.get("resultValue").toString()) ? x:y));
                                String warnThreshold = nonhomologousInspections.get("warnThreshold").toString();
                                log.info("区间非同源判断：maxInsResult为==={},minInsResult为==={},阈值是==={}", maxValueResultInfo, minValueResultInfo,warnThreshold);
                                if((Double.parseDouble(maxValueResultInfo.get().get("resultValue").toString())-Double.parseDouble(minValueResultInfo.get().get("resultValue").toString()))>Double.parseDouble(warnThreshold)){
                                    Map<String,Object> warn = new HashMap<>();
                                    warn.put("warnId", warnId);
                                    warn.put("warnType",6);
                                    warn.put("instanceId",Long.parseLong(nonhomologousInspections.get("instanceId").toString()));
                                    warn.put("warnContent","时间范围内表计识别结果差值超过阈值："+nonhomologousInspections.get("warnThreshold").toString());
                                    List<Map<String,Object>> insResults = new ArrayList<>();
                                    intervalResults.stream().forEach(i -> i.put("warnId",warn.get("warnId")));
                                    insResults.addAll(numResults);
                                    warn.put("resultsInfo",insResults);
                                    insertNonhomologousWarnInfo(warn);
                                }
                            break;
                        // 五次
                        case 7:
                                Map<String,Object> fiveResultsParam = new HashMap<>();
                                fiveResultsParam.put("type",7);
                                fiveResultsParam.put("inspectionId",nonhomologousInspections.get("robotInstanceId"));
                                //分时段查出历史结果
                                List<Map<String,Object>> fiveResults = nonhomologousWarnDao.selectWarnResults(fiveResultsParam);
                                if(fiveResults.size()<4){
                                    log.info("最近五次非同源告警--历史数据不足");
                                    break;
                                }
                                //加入当前结果对象
                                Map<String,Object> latestResult = new HashMap<>();
                                latestResult.put("taskId",cruiseResultMap.get("taskCode"));
                                latestResult.put("inspectionId",nonhomologousInspections.get("robotInstanceId").toString());
                                latestResult.put("resultValue",cruiseResultMap.get("value"));
                                //过滤非数字结果
                                fiveResults.add(latestResult);

                                List<Map<String,Object>> distinctResults = fiveResults.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.get("resultValue").toString()))), ArrayList::new));
                                if(distinctResults.size()==1){
                                    Map<String,Object> warn = new HashMap<>();
                                    warn.put("warnId", warnId);
                                    warn.put("warnType",7);
                                    warn.put("instanceId",Long.parseLong(nonhomologousInspections.get("instanceId").toString()));
                                    warn.put("warnContent","累计五次表计结果一致");
                                    List<Map<String,Object>> insResults = new ArrayList<>();
                                    fiveResults.stream().forEach(i -> i.put("warnId",warn.get("warnId")));
                                    insResults.addAll(fiveResults);
                                    warn.put("resultsInfo",insResults);
                                    insertNonhomologousWarnInfo(warn);
                                }
                            break;
                        default: break;
                    }
                }
            }
        }else{
            //机器人直接告警（三相）
            Map<String,Object> warnInfo = new HashMap<>();
            warnInfo.put("warnId", warnId);
            warnInfo.put("warnType",5);
            warnInfo.put("instanceId",null);
            warnInfo.put("warnContent","机器人相别告警："+cruiseResultMap.get("content"));
            List<Map<String,Object>> insResults = new ArrayList<>();
            String[] deviceIds= cruiseResultMap.get("deviceId").split(",");
            for(String s : deviceIds){
                Map<String,Object> robotWarn = new HashMap<>();
                robotWarn.put("taskId",cruiseResultMap.get("taskCode"));
                robotWarn.put("inspectionId",nonhomologousWarnDao.getInstanceIdByDeviceId(s,cruiseResultMap.get("taskCode")));
                robotWarn.put("warnId",warnInfo.get("warnId"));
                insResults.add(robotWarn);
            }
            warnInfo.put("resultsInfo",insResults);
            insertNonhomologousWarnInfo(warnInfo);
        }
    }

    private boolean insertNonhomologousWarnInfo(Map<String,Object> warn){
        log.info("非同源告警---入库,warnmap==={}",warn);
        nonhomologousWarnDao.insertNonhomologousWarnInfo(warn);
        List<Map<String,Object>> insResults =(List<Map<String,Object>>) warn.get("resultsInfo");
        nonhomologousWarnDao.insertWarnInspections(insResults);
        return true;
    }

    private boolean checkWarnExist(String instanceId,String taskId,String nonInstanceId){
        boolean result = false;
        Map<String,Object> warnParam = new HashMap<>();
        warnParam.put("instanceId",instanceId);
        warnParam.put("taskId",taskId);
        warnParam.put("nonInstanceId",Long.parseLong(nonInstanceId));
        int warnNum = nonhomologousWarnDao.checkWarnExist(warnParam);
        if(warnNum > 0){
            result = true;
        }
        return result;
    }

    private boolean isNumber(String str){
        // 通过Matcher进行字符串匹配
        Matcher m = pattern.matcher(str);
        // 如果正则匹配通过 m.matches() 方法返回 true ，反之 false
        return m.matches();
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

