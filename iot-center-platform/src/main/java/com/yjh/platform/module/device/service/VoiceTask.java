package com.yjh.platform.module.device.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.Object2Map;
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
import com.yjh.platform.module.task.service.TCruiseTaskService;
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

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM

    public VoiceTask(RedisTemplate redisTemplate,Long voiceDeviceId,String voicePath,String taskId,Long instanceId,
                     TCruiseDataResultDao tCruiseDataResultDao,TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao,
                     AudioDeviceManager audioDeviceManager,TCruiseResult tCruiseResult,TCruisePointInstanceNameDetail tCruisePointInstanceNameDetail,
                     TCruiseResultDao tCruiseResultDao,TVoiceDeviceService tVoiceDeviceService,Map<Long,List<TCruisePointInstanceNameDetail>> voiceinstanceList
                    ,TCruiseTaskResultDao tCruiseTaskResultDao,TCruiseTaskResult tCruiseTaskResult,TCruiseTaskService tCruiseTaskService){
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


                Map tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail, true));
                String str = "t_cruise_task_result:" + taskId + ":" + item.getInstanceId();
                Map tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult, true));
                tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                tCruiseTaskResultDetailMap.put("taskId", taskId);
                tCruiseTaskResultDetailMap.put("device_mete_id", item.getDeviceMeteId() == null ? "" : item.getDeviceMeteId().toString());
                tCruiseTaskResultDetailMap.put("realCode", item.getRealCode() == null ? "" : item.getRealCode());

                if (isok) {
                    tCruiseDataResult.setCruiseResult(246);
                    tCruiseDataResult.setResultNum("录音成功");
                    tCruiseDataResult.setVoicePath(voicePath);
                    tCruiseTaskResultDetail.setCruiseStatus(252);
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
}
