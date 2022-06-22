package com.yjh.accessrobot.netty.thread;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.commons.utils.file.FileUtil;
import com.yjh.accessrobot.configuration.UpFtpsConfig;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.module.command.service.RobotService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 分析机器人上报的未进行算法识别的巡视结果
 *
 * @author 丫C
 * @date 2022/4/17
 */
@lombok.extern.slf4j.Slf4j
public class AnalysisResultThread implements Runnable{

    /**
     * 算法接口url
     */
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    /**
     * 缺陷接口url
     */
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";

    private Map<String, String> cruiseResultMap;
    private String temporaryOriginPath;
    private String ftpFileName;
    private RedisTemplate redisTemplate;
    private String webSocketUrl;
    private UpFtpsConfig upFtpsConfig;

    private static final byte[] LOCK_FLAG = new byte[0];

    public AnalysisResultThread(UpFtpsConfig upFtpsConfig, Map<String, String> cruiseResultMap, String temporaryOriginPath, String ftpFileName, String webSocketUrl, RedisTemplate redisTemplate){
        this.cruiseResultMap = cruiseResultMap;
        this.temporaryOriginPath = temporaryOriginPath;
        this.ftpFileName = ftpFileName;
        this.redisTemplate = redisTemplate;
        this.webSocketUrl = webSocketUrl;
        this.upFtpsConfig = upFtpsConfig;
    }

    @Override
    public void run() {
        // 基本信息
        String taskId = cruiseResultMap.get("taskCode");
        String robotCode = cruiseResultMap.get("robotCode");
        String inspectionCode = cruiseResultMap.get("deviceId");
        Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:" + robotCode + ":" + taskId);
        Long instanceId = null;

        for (String key : robotInfoKeys) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
            if (Objects.equals(robotCode, redisInfoMap.get("robotCode"))
                    && Objects.equals(taskId, redisInfoMap.get("taskId"))
                    && Objects.equals(inspectionCode, redisInfoMap.get("inspectionCode"))) {
                instanceId = Long.valueOf(redisInfoMap.get("instanceId"));
            }
        }

        // 复制原图到算法分析指定的路径
        String resultImagePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")) + ftpFileName;
        try {
            FileUtil.copyFileUsingStream(temporaryOriginPath, resultImagePath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        // 如果是音频文件，单独再复制一份到指定文件夹
        String voicePath = "null";
        if (Objects.equals("3", cruiseResultMap.get("fileType"))){
            String resultVoicePath = redisTemplate.opsForHash().get("t_sys_param:absVoicePath", "content") + "/" + ftpFileName;
            voicePath = resultVoicePath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:absVoicePath", "content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:relativeVoicePath", "content")));
            try {
                FileUtil.copyFileUsingStream(temporaryOriginPath, resultVoicePath);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        // 标定文件
        String picModelPath = redisTemplate.opsForHash().get("t_sys_param:picModelPath", "content") + "/" + inspectionCode;

        // 测点信息
        TCruisePointInstanceDetail details = StaticContextAccessor.getBean(RobotService.class).selectForTask(instanceId);
        TStdDeviceMete tStdDevicemete = StaticContextAccessor.getBean(RobotService.class).selectDeviceMete(details.getDeviceMeteId());

        TCruiseResult tCruiseResult = StaticContextAccessor.getBean(RobotService.class).selectTaskResultId(taskId);
        String realCode = StaticContextAccessor.getBean(RobotService.class).selectRealCodeByInstanceId(instanceId);
        Map<String, String> tCruiseTaskResultMap = new HashMap<>(16);
        try {
            tCruiseTaskResultMap.put("realCode", realCode);
            tCruiseTaskResultMap.put("instanceId", String.valueOf(instanceId));
            tCruiseTaskResultMap.put("cruiseTime", cruiseResultMap.get("time"));
            tCruiseTaskResultMap.put("taskResultId", tCruiseResult.getTaskResultId());
            tCruiseTaskResultMap.put("taskName", tCruiseResult.getTaskName());
            tCruiseTaskResultMap.put("endTime", cruiseResultMap.get("time"));
            tCruiseTaskResultMap.put("cruiseStatus", "253");
            tCruiseTaskResultMap.put("evaluationState", "257");
            tCruiseTaskResultMap.put("createtime", cruiseResultMap.get("time"));
            tCruiseTaskResultMap.put("isWarn", "0");
            tCruiseTaskResultMap.put("taskId", taskId);
            tCruiseTaskResultMap.put("cruiseType", "228");
            tCruiseTaskResultMap.put("remark", "null");
            tCruiseTaskResultMap.put("origpic", resultImagePath);
            String picpath = resultImagePath.replace(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content")));
            tCruiseTaskResultMap.put("picpath", picpath);
            tCruiseTaskResultMap.put("voicePath", voicePath);
       }catch (Exception e){
           log.error("往redis插入值错误：{}", e);
       }

        if ( "on".equals(details.getIsAi()) || "on".equals(details.getIsJudge())) {
            // 配置了缺陷算法

            // 测点配置的算法类型
            List<TAlgorithmInfo> tAlgorithmInfoList = StaticContextAccessor.getBean(RobotService.class).selectByDeviceMeteId(details.getDeviceMeteId());

            addRequiredInfo(tAlgorithmInfoList, taskId, instanceId, tStdDevicemete, tCruiseTaskResultMap);
            packageAndInvoke(tAlgorithmInfoList, taskId, instanceId, resultImagePath, picModelPath, tStdDevicemete);
        } else {
            // 即拍照的点位和声音
            Map<String, Integer> map = updatePointStatusNum(taskId, tCruiseTaskResultMap, details);

            // 判断该点是否为最后一个
            processResult(map, taskId, tCruiseResult, tCruiseTaskResultMap, instanceId);
        }
    }

    /**
     * 增加video服务需要的字段信息
     *
     * @param tAlgorithmInfoList 配置的算法类型
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @param tStdDevicemete 测点信息
     * @param tCruiseTaskResultMap  结果map
     */
    private void addRequiredInfo(List<TAlgorithmInfo> tAlgorithmInfoList, String taskId, Long instanceId, TStdDeviceMete tStdDevicemete, Map<String,String> tCruiseTaskResultMap) {
        try {
            String recognitionMode = "0";
            if(!tAlgorithmInfoList.isEmpty()){
                recognitionMode = "1";
            }

            if("on".equals(tStdDevicemete.getIsAi()) || "on".equals(tStdDevicemete.getIsJudge())) {
                if ("1".equals(recognitionMode)) {
                    recognitionMode = "0";
                } else {
                    recognitionMode = "2";
                }
            }

            tCruiseTaskResultMap.put("recognitionMode", recognitionMode);
            String str = "t_cruise_task_result:" + taskId + ":" + instanceId;
            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);

            List<String> analysisInstanceList = new ArrayList<>();
            if(redisTemplate.hasKey("analysisList:" + taskId)) {
                redisTemplate.opsForList().leftPush("analysisList:" + taskId, String.valueOf(instanceId));
            } else {
                analysisInstanceList.add(String.valueOf(instanceId));
                redisTemplate.opsForList().leftPushAll("analysisList:" + taskId, analysisInstanceList);
            }
        }catch (Exception e){
            log.error("增加video服务需要的字段信息错误：{}", e);
        }
    }

    /**
     * 组装算法分析所需参数并调用接口
     *
     * @param tAlgorithmInfoList 配置的算法类型
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @param resultImagePath 原图
     * @param picModelPath 标定文件
     * @param tStdDevicemete 测点信息
     */
    private void packageAndInvoke(List<TAlgorithmInfo> tAlgorithmInfoList, String taskId, Long instanceId,
                                  String resultImagePath, String picModelPath, TStdDeviceMete tStdDevicemete) {
        for (TAlgorithmInfo tAlgorithmInfo : tAlgorithmInfoList) {
            Analysis analysis = new Analysis();
            analysis.setTaskId(taskId);
            analysis.setInstanceId(instanceId);
            analysis.setPicPath(resultImagePath);
            analysis.setAnalyseType(tAlgorithmInfo.getAnalyseType());
            analysis.setPicModelPath(picModelPath);
            analysis.setIsAi(tAlgorithmInfo.getIsAi());
            List<Analysis> analysisList = new ArrayList<>();
            analysisList.add(analysis);
            Map<String, List<Analysis>> analysisMap  = new HashMap<>(3);
            analysisMap.put("list", analysisList);
            log.info("调用video服务信息===={}", analysisMap);
            //0-缺陷 1-表记
            if(tAlgorithmInfo.getIsAi() == 1){
                analysis(analysisMap);
            }else {
                defect(analysisMap);
            }
        }

        if("on".equals(tStdDevicemete.getIsAi()) || "on".equals(tStdDevicemete.getIsJudge())){
            Analysis analysis = new Analysis();
            analysis.setTaskId(taskId);
            analysis.setInstanceId(instanceId);
            analysis.setPicPath(resultImagePath);
            if("on".equals(tStdDevicemete.getIsJudge())){
                analysis.setAnalyseType("11");
            }else {
                analysis.setAnalyseType("398");
            }
            analysis.setPicModelPath(picModelPath);
            analysis.setIsAi(0);
            List<Analysis> analysisList = new ArrayList<>();
            analysisList.add(analysis);
            Map<String, List<Analysis>> analysisMap  = new HashMap<>(3);
            analysisMap.put("list",analysisList);
            log.info("调用video服务信息===={}", analysisMap);
            defect(analysisMap);
        }
    }

    /**
     * 拍照和声音的正常点、异常点及所有点的数量更新和数据入库
     *
     * @param taskId 任务id
     * @param tCruiseTaskResultMap 巡视结果map
     * @param details 测点信息
     * @return Map<String, Integer>
     */
    private Map<String, Integer> updatePointStatusNum(String taskId,  Map<String, String> tCruiseTaskResultMap, TCruisePointInstanceDetail details) {
        tCruiseTaskResultMap.put("cruiseAbnormal", "null");
        tCruiseTaskResultMap.put("cruiseStatus", "252");
        tCruiseTaskResultMap.put("cruiseResult", "246");
        tCruiseTaskResultMap.put("resultDesc", "--");

        tCruiseTaskResultMap.put("cruiseTime", cruiseResultMap.get("time"));
        if (Objects.nonNull(details.getAnalyseType())){
            // 声音
            tCruiseTaskResultMap.put("resultNum", "已录音");
        }else {
            // 拍照
            tCruiseTaskResultMap.put("resultNum", "已拍照");
        }
        String str = "t_cruise_task_result:" + taskId + ":" + details.getInstanceId();
        redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);

        // 巡视结果上报站端
        cruiseResultToUpSystem(tCruiseTaskResultMap);

        // webSocket通知前端调用巡视监控的接口
        Map<String, Object> jasonMap = new HashMap<>(2);
        jasonMap.put("type", "finishedOneInstance");
        jasonMap.put("taskId", taskId);
        String json = JSON.toJSONString(jasonMap);
        log.info("做完一个点-前端推送：" + json);
        try {
            Constant.postUrl(webSocketUrl, json);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }

        Integer totalNum;
        Integer abnormalNum;
        Integer normalNum;
        synchronized (LOCK_FLAG){
            String strForCountAbnormal = "countForAbnormal:" + taskId;
            Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
             totalNum = Integer.valueOf(abnormalCount.get("all").toString());
             abnormalNum = Integer.valueOf(abnormalCount.get("abnormal").toString());
             normalNum = Integer.valueOf(abnormalCount.get("normal").toString());
            log.info("taskId为{}的总检测点数是==={}, 异常点数是==={}, 正常点数是==={}", taskId, totalNum, abnormalNum, normalNum);

            normalNum = normalNum + 1;
            log.info("taskId为{}的该点结果正常,这次变化的normal是==={}", taskId, normalNum);
            Map<String, String> mapForAbnormal = new HashMap<>(5);
            mapForAbnormal.put("abnormal", abnormalNum.toString());
            mapForAbnormal.put("normal", normalNum.toString());
            redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
        }
        // 存库
        storeCruiseResult(str);

        Map<String, Integer> map = new HashMap<>(7);
        map.put("totalNum", totalNum);
        map.put("abnormalNum", abnormalNum);
        map.put("normalNum", normalNum);

        return map;
    }

    /**
     * 巡视结果上报站端
     *
     * @param tCruiseTaskResultMap 巡视结果
     */
    private void cruiseResultToUpSystem(Map<String, String> tCruiseTaskResultMap) {
        try {
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String,Object>> xmlItems = new ArrayList<>();
            Map<String,Object> xmlItem = new HashMap<>();
            xmlBaseModel.setType("61");
            xmlItem.put("patroldevice_code", Optional.ofNullable(cruiseResultMap.get("patrolDeviceName")).orElse(""));
            xmlItem.put("patroldevice_name", Optional.ofNullable(cruiseResultMap.get("patrolDeviceCode")).orElse(""));
            xmlItem.put("task_name", Optional.ofNullable(cruiseResultMap.get("taskName")).orElse(""));
            xmlItem.put("task_code", Optional.ofNullable(cruiseResultMap.get("taskCode")).orElse(""));
            xmlItem.put("device_name", Optional.ofNullable(cruiseResultMap.get("deviceName")).orElse(""));
            xmlItem.put("device_id", Optional.ofNullable(cruiseResultMap.get("deviceId")).orElse(""));
            xmlItem.put("material_id", Optional.ofNullable(tCruiseTaskResultMap.get("realCode")).orElse(""));
            xmlItem.put("value","");
            xmlItem.put("value_unit", Optional.ofNullable(tCruiseTaskResultMap.get("resultNum")).orElse(""));
            xmlItem.put("unit","");
            xmlItem.put("time", Optional.ofNullable(cruiseResultMap.get("time")).orElse(""));
            xmlItem.put("recognition_type", Optional.ofNullable(cruiseResultMap.get("recognitionType")).orElse(""));
            xmlItem.put("file_type", Optional.ofNullable(cruiseResultMap.get("fileType")).orElse(""));
            String tagPath = "task/"+ cruiseResultMap.get("filePath");
            log.info("imgPath==={},tagPath==={}", temporaryOriginPath, tagPath);
            uploadFileToUpFtps(temporaryOriginPath, "/" + tagPath);
            xmlItem.put("file_path", tagPath);

            xmlItem.put("rectangle", Optional.ofNullable(cruiseResultMap.get("rectangle")).orElse(""));
            xmlItem.put("task_patrolled_id", Optional.ofNullable(cruiseResultMap.get("taskPatrolledId")).orElse(""));
            xmlItem.put("data_type","0x01");
            xmlItem.put("valid","1");

            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String,List<XMLBaseModel>> cruiseResult = new HashMap<>();
            cruiseResult.put("list",list);
            log.info("信息上报：-"+cruiseResult);
            Constant.otherServer(cruiseResult,Constant.TCP_URL);//江苏要求
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath 源文件地址
     * @param targetPathName 目标文件地址名称
     */
    private void uploadFileToUpFtps(String sourcePath, String targetPathName) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getKeypw(), upFtpsConfig.getUsername(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误:{}", e);
        }
    }

    /**
     * 最后一个巡视点结果处理
     *
     * @param map 正常、异常、全部点位数量
     * @param taskId 任务id
     * @param tCruiseResult 巡视结果
     * @param tCruiseTaskResultMap 巡视结果map
     * @param instanceId 巡视点id
     */
    private void processResult(Map<String, Integer> map, String taskId, TCruiseResult tCruiseResult, Map<String, String> tCruiseTaskResultMap, Long instanceId){
        try {
            Integer totalNum = map.get("totalNum");
            Integer abnormal = map.get("abnormalNum");
            Integer normal = map.get("normalNum");

            if (abnormal + normal == totalNum) {
                log.info("机器人该巡检点是最后一个点");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }

                TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult()
                        .setTaskId(taskId)
                        .setTaskName(tCruiseTaskResultMap.get("taskName"))
                        .setTaskAlarm(0)
                        .setTaskAbnormal(abnormal)
                        .setRunExecute(tCruiseTaskResultMap.get("if_run"))
                        .setCruiseTaskTime(DateTimeUtil.parse(tCruiseTaskResultMap.get("cruiseTaskTime")))
                        .setTaskResultId(tCruiseTaskResultMap.get("taskResultId"));
                log.info("任务为{}的tCruiseTaskResult内容是==={}", taskId, tCruiseTaskResult);
                StaticContextAccessor.getBean(RobotService.class).insertTCruiseTaskResult(tCruiseTaskResult);

                Integer taskWait = totalNum - normal - abnormal;
                tCruiseResult.setTaskWait(taskWait);

                tCruiseResult.setCState(240);
                tCruiseResult.setTaskCode(taskId);
                tCruiseResult.setCreateTime(DateTimeUtil.parse(tCruiseTaskResultMap.get("cruiseTaskTime")));
                log.info("任务为{}的tCruiseResult内容是==={}", taskId, tCruiseResult);
                // 更新TCR表
                int res = StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
                log.info("更新TCR的条数====" + res);

                // webSocket通知前端调用巡视监控的接口（任务完成）
                Map<String, Object> jasonMap2 = new HashMap<>(2);
                jasonMap2.put("type", "lastOneInstance");
                jasonMap2.put("taskId", taskId);
                String json2 = JSON.toJSONString(jasonMap2);
                log.info("最后一个点-前端推送：" + json2);
                try {
                    Constant.postUrl(webSocketUrl, json2);
                } catch (IOException | URISyntaxException e) {
                    log.error(e.getMessage(), e);
                }

                StaticContextAccessor.getBean(RobotService.class).updateIsWarn(taskId, instanceId, tCruiseTaskResultMap.get("cruiseResultId"));

            }else{
                log.info("机器人该巡检点不是最后一个点");
                Integer taskWait = totalNum - normal - abnormal;
                tCruiseResult.setTaskWait(taskWait);
                tCruiseResult.setCState(239);
                tCruiseResult.setTaskCode(taskId);
                log.info("任务为{}的tCruiseResult内容是==={}", taskId, tCruiseResult);
                StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 存储数据库
     *
     * @param redisName redis key
     */
    private void storeCruiseResult(String redisName){
        Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisName);
        List<TCruiseTaskResultDetail> detailList = new ArrayList<>();
        List<TCruiseDataResult> dataList = new ArrayList<>();
        List<String> cruiseResultIds = new ArrayList<>();

        try {
            log.info("TCTRD开始");
            TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
            tCruiseTaskResultDetail.setCruiseResultId(tCruiseTaskResultMap.get("taskResultId") + tCruiseTaskResultMap.get("instanceId"));
            tCruiseTaskResultDetail.setTaskResultId(tCruiseTaskResultMap.get("taskResultId"));
            tCruiseTaskResultDetail.setDeviceId(Long.valueOf(tCruiseTaskResultMap.get("deviceId")));
            tCruiseTaskResultDetail.setDeviceName(tCruiseTaskResultMap.get("deviceName"));
            tCruiseTaskResultDetail.setInstanceId(Long.valueOf(tCruiseTaskResultMap.get("instanceId")));
            tCruiseTaskResultDetail.setInstanceName(tCruiseTaskResultMap.get("instanceName"));
            tCruiseTaskResultDetail.setCruiseTime(DateTimeUtil.parse(tCruiseTaskResultMap.get("cruiseTime")));
            tCruiseTaskResultDetail.setEndTime(DateTimeUtil.parse(tCruiseTaskResultMap.get("endTime")));
            tCruiseTaskResultDetail.setCruiseStatus(Integer.valueOf(tCruiseTaskResultMap.get("cruiseStatus")));
            tCruiseTaskResultDetail.setRemark(tCruiseTaskResultMap.get("remark"));
            log.info("TCDRD内容：" + tCruiseTaskResultDetail);
            detailList.add(tCruiseTaskResultDetail);
            cruiseResultIds.add(tCruiseTaskResultDetail.getCruiseResultId());

            log.info("TCDR开始");
            TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
            tCruiseDataResult.setCruiseResultId(tCruiseTaskResultMap.get("taskResultId") + tCruiseTaskResultMap.get("instanceId"));
            tCruiseDataResult.setCruiseId(Long.valueOf(tCruiseTaskResultMap.get("instanceId")));
            tCruiseDataResult.setCruiseName(tCruiseTaskResultMap.get("cruiseName"));
            tCruiseDataResult.setCruiseType(Integer.valueOf(tCruiseTaskResultMap.get("cruiseType")));
            tCruiseDataResult.setResultDesc(tCruiseTaskResultMap.get("resultDesc"));
            tCruiseDataResult.setResultNum(tCruiseTaskResultMap.get("resultNum"));
            tCruiseDataResult.setPicpath(tCruiseTaskResultMap.get("picpath"));
            tCruiseDataResult.setOrigpic(tCruiseTaskResultMap.get("origpic"));
            tCruiseDataResult.setCruiseAbnormal(null);
            tCruiseDataResult.setEvaluationState(Integer.valueOf(tCruiseTaskResultMap.get("evaluationState")));
            tCruiseDataResult.setCreatetime(new Date());
            tCruiseDataResult.setIsWarn(Integer.valueOf((tCruiseTaskResultMap.get("isWarn"))));
            tCruiseDataResult.setCruiseResult(Integer.valueOf(tCruiseTaskResultMap.get("cruiseResult")));
            tCruiseDataResult.setVoicePath(tCruiseTaskResultMap.get("voicePath"));

            log.info("TCDR内容：" + tCruiseDataResult);
            dataList.add(tCruiseDataResult);

            log.info("两表开始插入");
            StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseTaskResultDetail(detailList);
            StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseDataResult(dataList);

            log.info("------------点结果插入后调用updateCruiseResultIds----------------------");
            Constant.otherServerList(cruiseResultIds, Constant.TASK_FINISH);
        }catch (Exception e){
            log.error("存储数据库出错：{}", e);
        }
    }

    /**
     * Redis数据库批量查询Key值游标
     * @param key redis的key
     * @return Set<String>
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
     * 表记分析
     * */
    private void analysis(Map<String, List<Analysis>> analysisMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(ALGORITHM_URL, analysisMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    /**
     * 缺陷分析
     * */
    private void defect(Map<String, List<Analysis>> analysisMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(DEFECT_URL, analysisMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
