package com.yjh.platform.module.device.service;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.platform.module.device.entity.TVoiceDevice;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.task.dao.TCruiseDataResultDao;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDetailDao;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
import com.yjh.platform.module.task.entity.TCruiseResult;
import com.yjh.platform.module.task.entity.TCruiseTask;
import com.yjh.platform.module.task.entity.TCruiseTaskResultDetail;
import com.yjh.platform.module.task.service.RunAtNowTask;
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

    private TCruisePointInstanceNameDetail item;
    private TCruiseResultDao tCruiseResultDao;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM

    public VoiceTask(RedisTemplate redisTemplate,Long voiceDeviceId,String voicePath,String taskId,Long instanceId,
                     TCruiseDataResultDao tCruiseDataResultDao,TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao,
                     AudioDeviceManager audioDeviceManager,TCruiseResult tCruiseResult,TCruisePointInstanceNameDetail item,
                     TCruiseResultDao tCruiseResultDao){
        this.redisTemplate = redisTemplate;
        this.voiceDeviceId = voiceDeviceId;
        this.audioDeviceManager = audioDeviceManager;
        this.voicePath = voicePath;
        this.taskId = taskId;
        this.instanceId = instanceId;
        this.tCruiseDataResultDao = tCruiseDataResultDao;
        this.tCruiseTaskResultDetailDao = tCruiseTaskResultDetailDao;
        this.tCruiseResult = tCruiseResult;
        this.item = item;
        this.tCruiseResultDao = tCruiseResultDao;
    }


    @Override
    public void run() {
        // todo 任务暂停和终止未做
        TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
        tCruiseTaskResultDetail.setCruiseResultId(tCruiseResult.getTaskResultId()+item.getInstanceId().toString());
        tCruiseTaskResultDetail.setTaskResultId(tCruiseResult.getTaskResultId());
        tCruiseTaskResultDetail.setDeviceId(item.getDeviceId());
        tCruiseTaskResultDetail.setInstanceId(item.getInstanceId());
        tCruiseTaskResultDetail.setDeviceName(item.getDeviceName());
        tCruiseTaskResultDetail.setInstanceName(item.getInstanceName());

        TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
        tCruiseDataResult.setCruiseResultId(tCruiseResult.getTaskResultId()+instanceId);
        tCruiseDataResult.setCruiseId(item.getInstanceId());
        tCruiseDataResult.setCruiseType(item.getCruiseType());
        tCruiseDataResult.setCruiseName(item.getCruiseName());
        //1 开始录音
        VoiceDeviceAllInfoDetail voiceDevice = tVoiceDeviceService.selectByPrimaryId(voiceDeviceId);
        Optional<AudioDevice> audioDevice = audioDeviceManager.getAudioDevice(voiceDevice.getVoiceCode());
        Long maoForTime= Long.valueOf(redisTemplate.opsForHash().entries("t_sys_param:voiceDeviceTime").get("content").toString());
        String str = "t_cruise_task_result:"+taskId +":"+ instanceId;
        Map tCruiseTaskResultDetailMap = redisTemplate.opsForHash().entries(str);


        if(voiceDevice.getVoiceCode() != null && !"".equals(voiceDevice.getVoiceCode())){
            try {
                audioDevice.get().startRecording();

                Thread.sleep(maoForTime*1000);

                audioDevice.get().stopRecordingAndSave(voicePath);
                tCruiseDataResult.setCruiseResult(246);
                tCruiseDataResult.setVoicePath(voicePath);
                tCruiseTaskResultDetail.setCruiseStatus(252);
            } catch (Exception e) {
                log.info("声纹设备录音出错：{}", e);
                tCruiseDataResult.setCruiseResult(247);
                tCruiseDataResult.setCruiseAbnormal(248);
                tCruiseDataResult.setResultNum("录音失败");
                tCruiseDataResult.setVoicePath("--");
                tCruiseDataResult.setIsWarn(0);
                tCruiseTaskResultDetail.setCruiseStatus(254);
                tCruiseTaskResultDetail.setEndTime(new Date());
                tCruiseTaskResultDetail.setCruiseTime(new Date());

            }

        }else {
            tCruiseDataResult.setCruiseResult(247);
            tCruiseDataResult.setCruiseAbnormal(248);
            tCruiseDataResult.setResultNum("录音失败");
            tCruiseDataResult.setVoicePath("--");
            tCruiseDataResult.setIsWarn(0);
            tCruiseTaskResultDetail.setCruiseStatus(254);
            tCruiseTaskResultDetail.setEndTime(new Date());
            tCruiseTaskResultDetail.setCruiseTime(new Date());
        }

        Map tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
        tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
        tCruiseTaskResultDetailMap.put("taskId",taskId);
        tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));

        redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);

        tCruiseDataResultDao.insert(tCruiseDataResult);
        tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);

        String strForCountAbnormal = "countForAbnormal:" + taskId;
        Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
        Integer total = Integer.valueOf(abnormalCount.get("all").toString());
        Integer abnormal = Integer.valueOf(abnormalCount.get("abnormal").toString());
        Integer normal = Integer.valueOf(abnormalCount.get("normal").toString());

        if(tCruiseDataResult.getCruiseResult() == 246){
            normal = normal+1;
        }else {
            abnormal = abnormal +1;
        }
        if(abnormal + normal == total){
            //所有的点作完了
            tCruiseResult.setCState(240);
            tCruiseResult.setTaskWait(0);
            tCruiseResultDao.update(tCruiseResult);
        }else {
            Map<String, String> mapForAbnormal = new HashMap<>();
            mapForAbnormal.put("abnormal", abnormal.toString());
            mapForAbnormal.put("normal", normal.toString());
            redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
        }


    }
}
