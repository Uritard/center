package com.yjh.platform.module.device.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.configuration.UpFtpsConfig;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.platform.module.device.entity.TVoiceDevice;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.task.dao.TCruiseDataResultDao;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDao;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDetailDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.RunAtNowTask;
import com.yjh.platform.module.task.service.TCruiseDataResultService;
import com.yjh.platform.module.task.service.TCruiseTaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

public class VoiceTask implements Runnable{


    private RedisTemplate redisTemplate;

    private AudioDeviceManager audioDeviceManager;

    private Long voiceDeviceId;

    private TVoiceDeviceService tVoiceDeviceService;

    private String voicePath;
    private String taskId;
    private Long instanceId;
    private TCruiseDataResultDao tCruiseDataResultDao;
    private TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao;
    private Logger log = LoggerFactory.getLogger(VoiceTask.class);

    private TCruiseResult tCruiseResult;

    private TCruisePointInstanceNameDetail tCruisePointInstanceNameDetail;
    private TCruiseResultDao tCruiseResultDao;
    private Map<Long,List<TCruisePointInstanceNameDetail>> voiceinstanceList;
    private TCruiseTaskResultDao tCruiseTaskResultDao;
    private TCruiseTaskResult tCruiseTaskResult;
    private TCruiseTaskService tCruiseTaskService;
    private TCruiseDataResultService tCruiseDataResultService;
    private String stationCode;
    private UpFtpsConfig upFtpsConfig;


    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM

    public VoiceTask(String stationCode, RedisTemplate redisTemplate,Long voiceDeviceId,String voicePath,String taskId,Long instanceId,
                     TCruiseDataResultDao tCruiseDataResultDao,TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao,
                     AudioDeviceManager audioDeviceManager,TCruiseResult tCruiseResult,TCruisePointInstanceNameDetail tCruisePointInstanceNameDetail,
                     TCruiseResultDao tCruiseResultDao,TVoiceDeviceService tVoiceDeviceService,Map<Long,List<TCruisePointInstanceNameDetail>> voiceinstanceList
                    ,TCruiseTaskResultDao tCruiseTaskResultDao,TCruiseTaskResult tCruiseTaskResult,
                     TCruiseTaskService tCruiseTaskService,TCruiseDataResultService tCruiseDataResultService){
        this.stationCode = stationCode;
        this.redisTemplate = redisTemplate;
        this.voiceDeviceId = voiceDeviceId;
        this.audioDeviceManager = audioDeviceManager;
        this.voicePath = voicePath;
        this.taskId = taskId;
        this.instanceId = instanceId;
        this.tCruiseDataResultDao = tCruiseDataResultDao;
        this.tCruiseTaskResultDetailDao = tCruiseTaskResultDetailDao;
        this.tCruiseResult = tCruiseResult;
        this.tCruisePointInstanceNameDetail = tCruisePointInstanceNameDetail;
        this.tCruiseResultDao = tCruiseResultDao;
        this.tVoiceDeviceService = tVoiceDeviceService;
        this.voiceinstanceList = voiceinstanceList;
        this.tCruiseTaskResultDao = tCruiseTaskResultDao;
        this.tCruiseTaskResult = tCruiseTaskResult;
        this.tCruiseTaskService = tCruiseTaskService;
        this.tCruiseDataResultService = tCruiseDataResultService;
        this.upFtpsConfig = StaticContextAccessor.getBean(UpFtpsConfig.class);
    }


    @Override
    public void run() {

        try {
            //1 开始录音
            VoiceDeviceAllInfoDetail voiceDevice = tVoiceDeviceService.selectByPrimaryId(voiceDeviceId);
            AudioDevice audioDevice = audioDeviceManager.getAudioDevice(voiceDevice.getVoiceCode());
            Long maoForTime = Long.valueOf(redisTemplate.opsForHash().entries("t_sys_param:voiceDeviceTime").get("content").toString());

            boolean isok = false;
            if (voiceDevice.getVoiceCode() != null && !"".equals(voiceDevice.getVoiceCode())) {
                try {
                    audioDevice.startRecording();

                    Thread.sleep(maoForTime * 1000);

                    audioDevice.stopRecordingAndSave(voicePath);
                    isok = true;
                    //文件替换
                    String absVoicePath= String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:absVoicePath").get("content"));
                    String relativeVoicePath = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:relativeVoicePath").get("content"));
                    voicePath = voicePath.replace(absVoicePath,relativeVoicePath);
                } catch (Exception e) {
                    log.warn("声纹设备录音出错", e);

                }
            }
            for (TCruisePointInstanceNameDetail item : voiceinstanceList.get(voiceDeviceId)) {
                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                tCruiseTaskResultDetail.setCruiseResultId(tCruiseResult.getTaskResultId() + item.getInstanceId().toString());
                tCruiseTaskResultDetail.setTaskResultId(tCruiseResult.getTaskResultId());
                tCruiseTaskResultDetail.setDeviceId(item.getDeviceId());
                tCruiseTaskResultDetail.setInstanceId(item.getInstanceId());
                tCruiseTaskResultDetail.setDeviceName(item.getDeviceName());
                tCruiseTaskResultDetail.setInstanceName(item.getInstanceName());
                tCruiseTaskResultDetail.setCruiseStatus(253);
                tCruiseTaskResultDetail.setCruiseResultId(tCruiseResult.getTaskResultId() + item.getInstanceId().toString());
                tCruiseTaskResultDetail.setTaskResultId(tCruiseResult.getTaskResultId());
                tCruiseTaskResultDetail.setDeviceId(item.getDeviceId());
                tCruiseTaskResultDetail.setInstanceId(item.getInstanceId());
                tCruiseTaskResultDetail.setDeviceName(item.getDeviceName());
                tCruiseTaskResultDetail.setInstanceName(item.getInstanceName());

                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                tCruiseDataResult.setCruiseResultId(tCruiseResult.getTaskResultId() + item.getInstanceId().toString());
                tCruiseDataResult.setCruiseId(item.getInstanceId());
                tCruiseDataResult.setCruiseType(item.getCruiseType());
                tCruiseDataResult.setCruiseName(item.getCruiseName());
                tCruiseDataResult.setCruiseResultId(tCruiseResult.getTaskResultId() + instanceId);
                tCruiseDataResult.setCruiseId(item.getInstanceId());
                tCruiseDataResult.setCruiseType(item.getCruiseType());
                tCruiseDataResult.setCruiseName(item.getCruiseName());
                tCruiseDataResult.setIsWarn(0);

                Map tCruiseTaskResultDetailMap = Object2Map.objectToMap(tCruiseTaskResultDetail, true);
                String str = "t_cruise_task_result:" + taskId + ":" + item.getInstanceId();
                Map tCruiseDataResultMap = Object2Map.objectToMap(tCruiseDataResult, true);
                tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                tCruiseTaskResultDetailMap.put("taskId", taskId);
                tCruiseTaskResultDetailMap.put("device_mete_id", item.getDeviceMeteId() == null ? "" : item.getDeviceMeteId().toString());
                tCruiseTaskResultDetailMap.put("realCode", item.getRealCode() == null ? "" : item.getRealCode());

                if (isok) {
                    tCruiseDataResult.setCruiseResult(246);
                    tCruiseDataResult.setResultNum("录音成功");
                    tCruiseDataResult.setVoicePath(voicePath);
                    tCruiseDataResult.setPicpath(voicePath);
                    tCruiseTaskResultDetail.setCruiseStatus(252);
                    tCruiseDataResult.setEvaluationState(257);
                    tCruiseTaskResultDetail.setCruiseTime(new Date());
                } else {
                    tCruiseDataResult.setCruiseResult(247);
                    tCruiseDataResult.setCruiseAbnormal(248);
                    tCruiseDataResult.setResultNum("录音失败");
                    tCruiseDataResult.setVoicePath("--");
                    tCruiseDataResult.setIsWarn(0);
                    tCruiseTaskResultDetail.setCruiseStatus(254);
                    tCruiseTaskResultDetail.setEndTime(new Date());
                    tCruiseTaskResultDetail.setCruiseTime(new Date());
                }

                tCruiseDataResultDao.insert(tCruiseDataResult);
                tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);
                List<String> cruiseResultIdList = new ArrayList();
                cruiseResultIdList.add(tCruiseDataResult.getCruiseResultId());
                tCruiseDataResultService.updateCruiseAnalyze(cruiseResultIdList);

                // 巡视结果上报上级系统
                cruiseResultToUpSystem(voiceDevice, item, tCruiseDataResult);

                String strForCountAbnormal = "countForAbnormal:" + taskId;
                Map<String, String> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
                Integer total = Integer.valueOf(abnormalCount.get("all"));
                Integer abnormal = Integer.valueOf(abnormalCount.get("abnormal"));
                Integer normal = Integer.valueOf(abnormalCount.get("normal"));

                if (tCruiseDataResult.getCruiseResult() == 246) {
                    normal = normal + 1;
                } else {
                    abnormal = abnormal + 1;
                }
                if (abnormal + normal == total) {
                    //所有的点作完了

                    tCruiseTaskResult.setTaskAbnormal(abnormal);
                    tCruiseTaskResult.setCruiseTaskTime(simpleDateFormat.parse(abnormalCount.get("taskStart")));
                    tCruiseTaskResult.setTaskStatus(240);
                    tCruiseTaskResult.setTaskName(tCruiseResult.getTaskName());
                    if (abnormal != 0) {
                        tCruiseTaskResult.setCruiseResult(247);
                    } else {
                        tCruiseTaskResult.setCruiseResult(246);
                    }

                    tCruiseTaskResultDao.insert(tCruiseTaskResult);

                    tCruiseResult.setCState(240);
                    tCruiseResult.setTaskWait(0);
                    tCruiseResultDao.update(tCruiseResult);

                    taskGoOn(taskId);

                    Map<String, String> jsonForLastMap = new HashMap<>();
                    jsonForLastMap.put("type", "lastOneInstance");
                    jsonForLastMap.put("taskId", taskId);
                    String jsonForLast = JSON.toJSONString(jsonForLastMap);
                    log.info("发送给前端的消息：" + jsonForLast);
                    Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jsonForLastMap);
                } else {
                    Map<String, String> mapForAbnormal = new HashMap<>();
                    mapForAbnormal.put("abnormal", abnormal.toString());
                    mapForAbnormal.put("normal", normal.toString());
                    redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
                }

                // webSocket通知前端调用巡视监控的接口
                Map<String,String> jasonMapOnFinished=new HashMap<>();
                jasonMapOnFinished.put("type","finishedOneInstance");
                jasonMapOnFinished.put("taskId",taskId);
                String jsonMessage= JSON.toJSONString(jasonMapOnFinished);
                log.info("发送给前端的消息："+jsonMessage);
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);
            }

        }catch (Exception e){
            log.info("声纹结果处理出错：{}",e);
        }
    }

    /**
     * 结果上报上级系统
     *
     * @param voiceDevice 声纹设备信息
     * @param item 结果信息
     * @param tCruiseDataResult 巡视点结果
     * @return void
     */
    private void cruiseResultToUpSystem(VoiceDeviceAllInfoDetail voiceDevice, TCruisePointInstanceNameDetail item, TCruiseDataResult tCruiseDataResult) {
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String,Object>> xmlItems = new ArrayList<>();
        Map<String,Object> xmlItem = new HashMap<>();
        try {
            xmlBaseModel.setType("61");
            xmlItem.put("patroldevice_code", Optional.ofNullable(String.valueOf(item.getInstanceId())).orElse(""));
            xmlItem.put("patroldevice_name", Optional.ofNullable(item.getInstanceName()).orElse(""));
            xmlItem.put("task_name", Optional.ofNullable(tCruiseResult.getTaskName()).orElse(""));
            xmlItem.put("task_code", Optional.ofNullable(taskId).orElse(""));
            xmlItem.put("device_name", Optional.ofNullable(item.getCruiseName()).orElse(""));
            xmlItem.put("device_id", Optional.ofNullable(String.valueOf(item.getDeviceMeteId())).orElse(""));
            xmlItem.put("material_id", Optional.ofNullable(item.getRealCode()).orElse(""));
            xmlItem.put("value","");
            xmlItem.put("value_unit", Optional.ofNullable(tCruiseDataResult.getResultNum()).orElse(""));
            xmlItem.put("unit","");
            xmlItem.put("time", DateTimeUtil.format(new Date()));
            // 识别类型为声音检测
            xmlItem.put("recognition_type","5");
            // 采集文件类型为音频
            xmlItem.put("file_type","2");

            if (Objects.equals("--", tCruiseDataResult.getVoicePath())){
                xmlItem.put("file_path", "");
            }else {
                // 声纹设备编码 （仿照机器人编码）
                String voiceCode = voiceDevice.getVoiceCode();
                String deviceMeteId = String.valueOf(item.getDeviceMeteId());
                // 声纹音频文件路径
                String absPath = voicePath.replaceAll(
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:relativeVoicePath","content")),
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:absVoicePath","content")));

                // 文件格式：变电站编码/年/月/日/巡视任务编码/Audio/设备点位ID_编码_时间.jpg
                String timeFormat = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String tagPath =  "task/" + stationCode + "/" + timeFormat.substring(0,4) + "/" + timeFormat.substring(4,6) + "/" + timeFormat.substring(6,8)
                        + "/" + taskId + "/Audio/" + deviceMeteId + "_" + voiceCode + "_" + timeFormat + ".wav";
                log.info("imgPath==={},tagPath==={}", absPath, tagPath);
                uploadFileToUpFtps(absPath, "/" + tagPath);
                xmlItem.put("file_path",tagPath);
            }

            xmlItem.put("rectangle","");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
            xmlItem.put("task_patrolled_id",taskId+"_"+simpleDateFormat2.format(new Date()));
            xmlItem.put("data_type","0x04");
            xmlItem.put("valid","1");

            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String,List<XMLBaseModel>> cruiseResult = new HashMap<>();
            cruiseResult.put("list",list);
            log.info("信息上报：-"+cruiseResult);
//            Constant.otherServer(cruiseResult,Constant.TCP_URL);//江苏要求
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    private void taskGoOn(String taskId){
        String lowTaskKey = "lowTask:" + taskId;
        List<String> lowTaskList = redisTemplate.opsForList().range(lowTaskKey, 0, -1);
        if (lowTaskList != null && lowTaskList.size() > 0) {
            lowTaskList.forEach(lowTask -> {
                try {
                    tCruiseTaskService.taskGoOn(lowTask);
                }catch (Exception e){
                    log.info("低优先级任务继续出错：{}",e);
                }
            });
        }
    }

    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath 源文件地址
     * @param targetPathName 目标文件地址名称
     */
    public void uploadFileToUpFtps(String sourcePath, String targetPathName) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getKeypw(), upFtpsConfig.getUsername(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误:{}", e);
        }
    }

}
