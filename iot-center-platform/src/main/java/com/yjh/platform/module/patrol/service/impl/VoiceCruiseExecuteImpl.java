/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.patrol.thread.CruiseRedisStorage;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.yjh.platform.module.patrol.CruiseConstant.*;
import static com.yjh.platform.module.patrol.CruiseConstant.TypeEnum.VOICE;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/31
 * @since [产品/模块版本] （可选）
 */
@Component
public class VoiceCruiseExecuteImpl implements CruiseInspectionExecute {
    private final RedisTemplate<String, ?> redisTemplate;
    private AudioDeviceManager audioDeviceManager;
    private TVoiceDeviceService tVoiceDeviceService;

    private final HashOperations<String, String, String> hashOperations;

    public VoiceCruiseExecuteImpl(RedisTemplate<String, ?> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    /**
     * 测点执行
     *
     * @param inspectionMap 测点数据
     */
    @Override
    public boolean execute(Map<String, String> inspectionMap) {
        voiceRecord(inspectionMap);

        // 数据存入 redis
        CruiseRedisStorage.offer(inspectionMap);
        return true;
    }

    private void voiceRecord(Map<String, String> inspectionMap) {
        String dateT = DateTimeUtil.getDateTimeString();
        inspectionMap.put("cruiseTime", dateT);

        String taskId = inspectionMap.get("taskId");

        String voicePath = hashOperations.entries("t_sys_param:absVoicePath").get("content");
        long dateTime = System.currentTimeMillis();
        long maoForTime = Long.parseLong(hashOperations.entries("t_sys_param:voiceDeviceTime").get("content"));
        long dateTimeAfter = dateTime + maoForTime * 1000;
        String timeAfterTem = new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime));
        Long cruiseId = MapUtils.getLongValue(inspectionMap, "cruiseId");
        String voiceName = cruiseId + "_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTime)) + "-" + new SimpleDateFormat(
            "yyyyMMdd_HHmm").format(new Date(dateTimeAfter));
        String voiceFilePath = voicePath + "/" + cruiseId + "/1/" + timeAfterTem + "/" + voiceName + ".wav";

        //1 开始录音
        VoiceDeviceAllInfoDetail voiceDevice = tVoiceDeviceService.selectByPrimaryId(cruiseId);
        AudioDevice audioDevice = audioDeviceManager.getAudioDevice(voiceDevice.getVoiceCode());

        boolean isok = false;
        if (voiceDevice.getVoiceCode() != null && !"".equals(voiceDevice.getVoiceCode())) {
            try {
                audioDevice.startRecording();

                Thread.sleep(maoForTime * 1000);

                audioDevice.stopRecordingAndSave(voicePath);
                isok = true;
                //文件替换
                String absVoicePath = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:absVoicePath").get("content"));
                String relativeVoicePath =
                    String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:relativeVoicePath").get("content"));
                voiceFilePath = voiceFilePath.replace(absVoicePath, relativeVoicePath);
            } catch (Exception e) {
                log.warn("声纹设备录音出错", e);
            }
        }

        inspectionMap.put("endTime", DateTimeUtil.getDateTimeString());
        // 未审核
        inspectionMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
        inspectionMap.put("isWarn", "0");
        if (isok) {
            // 录音结果处理
            inspectionMap.put("resultNum", "录音成功");
            // 巡视结果，正常
            inspectionMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
            inspectionMap.put("picpath", voiceFilePath);
            inspectionMap.put("voicePath", voiceFilePath);
            // 巡检数据状态，已经执行
            inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
        } else {
            // 录音结果处理
            inspectionMap.put("resultNum", "录音失败");
            // 巡视结果，正常
            inspectionMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
            inspectionMap.put("picpath", "--");
            inspectionMap.put("voicePath", "--");
            // 巡检数据状态，已经执行
            inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_FAILED));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CruiseExecuteFactory.CREATE.registerExecute(VOICE, this);
    }
}
