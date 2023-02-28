/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.mp3.VoiceAnalyseUtil;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.thread.CruiseRedisStorage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
    private final AudioDeviceManager audioDeviceManager;
    private final TVoiceDeviceService tVoiceDeviceService;
    private final PatrolResultHandler patrolResultHandler;

    private final HashOperations<String, String, String> hashOperations;

    private boolean isEdge = false;

    public VoiceCruiseExecuteImpl(RedisTemplate<String, ?> redisTemplate, AudioDeviceManager audioDeviceManager,
        TVoiceDeviceService tVoiceDeviceService, PatrolResultHandler patrolResultHandler) {
        this.redisTemplate = redisTemplate;
        this.audioDeviceManager = audioDeviceManager;
        this.tVoiceDeviceService = tVoiceDeviceService;
        this.patrolResultHandler = patrolResultHandler;
        this.hashOperations = redisTemplate.opsForHash();
    }

    /**
     * 测点执行
     *
     * @param inspectionMap 测点数据
     */
    @Override
    public boolean execute(Map<String, String> inspectionMap) {
        try {
            log.info("声纹任务开始执行...");
            voiceRecord(inspectionMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 数据存入 redis
        CruiseRedisStorage.offer(inspectionMap);
        return true;
    }

    private void voiceRecord(Map<String, String> inspectionMap) {
        String sysLevel = (String)redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content");
        isEdge = "1".equals(sysLevel);

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

        log.info("声纹录音开始，taskId: {}，deviceId: {}, voicePath: {}", taskId, cruiseId, voiceFilePath);
        //1 开始录音
        VoiceDeviceAllInfoDetail voiceDevice = tVoiceDeviceService.selectByPrimaryId(cruiseId);
        String voiceCode = voiceDevice.getVoiceCode();
        AudioDevice audioDevice = audioDeviceManager.getAudioDevice(voiceCode);
        log.info("录音设备，taskId: {}，voiceCode: {}", taskId, voiceDevice.getVoiceCode());
        boolean isok = false;
        String voiceFileUrl = "";
        if (StringUtils.isNotEmpty(voiceCode)) {
            try {
                audioDevice.startRecording();

                Thread.sleep(maoForTime * 1000);

                audioDevice.stopRecordingAndSave(voiceFilePath);
                isok = true;
                //文件替换
                String absVoicePath = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:absVoicePath").get("content"));
                String relativeVoicePath =
                    String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:relativeVoicePath").get("content"));
                voiceFileUrl = voiceFilePath.replace(absVoicePath, relativeVoicePath);
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
            inspectionMap.put("picpath", voiceFileUrl);
            inspectionMap.put("voicePath", voiceFileUrl);
            // 巡检数据状态，已经执行
            inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));

            voiceAnalyse(inspectionMap, voiceFilePath, voiceDevice);
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

    public String voiceAnalyse(Map<String, String> cruiseResultMap, String voicePath, VoiceDeviceAllInfoDetail voiceDevice){

        // 频率数组
        List<Integer> fList = null;
        VoiceAnalyseUtil voiceAnalyseUtil = new VoiceAnalyseUtil(voicePath);
        try {
            fList = voiceAnalyseUtil.analyticalDecibelsPl();
        }catch (Exception e){
            throw new BusinessException(209,"音频文件读取异常");
        }

        int fWarn = warning(fList, voiceDevice.getfValue(), cruiseResultMap, "频率");

        List<Integer> dbList = null;
        try {
            dbList = voiceAnalyseUtil.analyticalDecibels();
        }catch (Exception e){
            throw new BusinessException(209,"音频文件读取异常");
        }

        int dbWarn = warning(dbList, voiceDevice.getDbValue(), cruiseResultMap, "分贝");

        String retVal = "DB:" + dbWarn + "  F:" + fWarn;
        cruiseResultMap.put("resultNum", retVal);

        return retVal;
    }

    private int warning(List<Integer> dbList, String warnDb, Map<String, String> cruiseResultMap,  String alarmPrefix) {
        if (CollectionUtils.isEmpty(dbList) || StringUtils.isEmpty(warnDb)) {
            return 0;
        }
        int warnVal = NumberUtils.toInt(warnDb);
        int maxVal = dbList.stream().max(Comparator.comparingInt(Integer::intValue)).orElse(0);
        if (!isEdge && maxVal > warnVal) {
            patrolResultHandler.voiceResultHandler(String.valueOf(maxVal), cruiseResultMap, maxVal - warnVal, alarmPrefix);
        }
        return maxVal;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CruiseExecuteFactory.CREATE.registerExecute(VOICE, this);
    }
}
