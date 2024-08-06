/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.common.utils.mp3.VoiceAnalyseUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import com.yjh.platform.module.patrol.controller.VoicePrintDataCollectRespController;
import com.yjh.platform.module.patrol.entity.interlanalysis.Response;
import com.yjh.platform.module.patrol.entity.voice.ObjectData;
import com.yjh.platform.module.patrol.entity.voice.ObjectListData;
import com.yjh.platform.module.patrol.entity.voice.VoicePrintDataCollectReq;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.yjh.platform.module.patrol.CruiseConstant.*;
import static com.yjh.platform.module.patrol.CruiseConstant.TypeEnum.VOICE;

/**
 * <功能描述>
 *  采用http
 * @author Chenfei
 * @date 2022/10/31
 * @since [产品/模块版本] （可选）
 */
@Component
public class VoiceCruiseHttpExecuteImpl implements CruiseInspectionExecute {
    private final RedisTemplate<String, ?> redisTemplate;
    private final AudioDeviceManager audioDeviceManager;
    private final TVoiceDeviceService tVoiceDeviceService;
    private final PatrolResultHandler patrolResultHandler;
    private final ApplicationProperties applicationProperties;

    private final HashOperations<String, String, String> hashOperations;

    private boolean isEdge = false;

    public VoiceCruiseHttpExecuteImpl(RedisTemplate<String, ?> redisTemplate, AudioDeviceManager audioDeviceManager,
                                      TVoiceDeviceService tVoiceDeviceService, PatrolResultHandler patrolResultHandler,
                                              ApplicationProperties applicationProperties) {
        this.redisTemplate = redisTemplate;
        this.audioDeviceManager = audioDeviceManager;
        this.tVoiceDeviceService = tVoiceDeviceService;
        this.patrolResultHandler = patrolResultHandler;
        this.hashOperations = redisTemplate.opsForHash();
        this.applicationProperties = applicationProperties;
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
            return voiceRecord(inspectionMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return true;
    }

    private boolean voiceRecord(Map<String, String> inspectionMap) {
        isEdge = Constant.isEdge();

        Boolean isEnd = true;

        String dateT = DateTimeUtil.getDateTimeString();
        inspectionMap.put("cruiseTime", dateT);

        String taskId = inspectionMap.get("taskId");

        String voicePath = hashOperations.entries("t_sys_param:absVoicePath").get("content");
        long dateTime = System.currentTimeMillis();
        Long maoForTime = Long.parseLong(hashOperations.entries("t_sys_param:voiceDeviceTime").get("content"));
        long dateTimeAfter = dateTime + maoForTime * 1000;
        String timeAfterTem = new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime));
        Long cruiseId = MapUtils.getLongValue(inspectionMap, "cruiseId");
        String voiceName = cruiseId + "_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTime)) + "-" + new SimpleDateFormat(
            "yyyyMMdd_HHmm").format(new Date(dateTimeAfter));
        String voiceFilePath = voicePath + "/" + cruiseId + "/1/" + timeAfterTem + "/" + voiceName + ".wav";

        String dir = voicePath + "/" + cruiseId + "/1/" + timeAfterTem + "/";
        File dirF = new File(dir);
        if (!dirF.exists()){
            dirF.mkdirs();
        }

        log.info("声纹录音开始，taskId: {}，deviceId: {}, voicePath: {}", taskId, cruiseId, voiceFilePath);

        VoiceDeviceAllInfoDetail voiceDevice = tVoiceDeviceService.selectByPrimaryId(cruiseId);
        String voiceCode = voiceDevice.getVoiceCode();
        log.info("录音设备，taskId: {}，voiceCode: {}", taskId, voiceDevice.getVoiceCode());
        String voiceFileUrl = "";
        inspectionMap.put("objectId",voiceDevice.getVoiceCode());
        //1 开始录音 头都 http  只发送采集请求
        VoicePrintDataCollectReq req = new VoicePrintDataCollectReq();
        req.setRequestHostIp(applicationProperties.getAudioConfig().getRequestHostIp());
        req.setRequestHostPort(applicationProperties.getAudioConfig().getRequestHostPort());
        String requestId = UUID.randomUUID().toString() + "#"+taskId+"#"+inspectionMap.get("instanceId");
        req.setRequestId(requestId);
        List<ObjectListData> objectListData = new ArrayList<>();
        ObjectListData objectData = new ObjectListData();
        objectData.setObjectId(voiceDevice.getVoiceCode());
        objectData.setDuration(maoForTime.toString());
        objectListData.add(objectData);
        req.setObjectList(objectListData);
        String code = "-1";
        try {
            code = Constant.httpPost(applicationProperties.getAudioConfig().getVoiceprintDataCollectUrl(), JSONUtil.toJSONString(req));
            if (StringUtils.isEmpty(code)){
                code = "-1";
            }
            JSONObject jsonObject = JSON.parseObject(code);
            code = String.valueOf(jsonObject.get("code"));
        } catch (Exception e){
            log.error("发送声纹采集http请求出错: ",e);
        }
        // 未审核
        inspectionMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
        inspectionMap.put("voiceFilePath", voiceFilePath);
        log.info("声纹采集http请求返回：{}", code);
        inspectionMap.put("picpath", "--");
        inspectionMap.put("voicePath", "--");
        if ("200".equals(code)) {

//            VoicePrintDataCollectRespController.requestMapCruiseMap.put(requestId,inspectionMap);
            // 录音结果处理
            inspectionMap.put("resultDesc", "开始录音");
            inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_UN));

            isEnd = false;
        } else {
            // 录音结果处理
            inspectionMap.put("resultNum", "-1");
            inspectionMap.put("resultDesc", AbnormalResDescEnum.RECORDING_FAILURE.getDesc());
            inspectionMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_NOPIC));
            // 巡视结果，异常
            inspectionMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
            // 巡检数据状态，执行失败
            inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_FAILED));
        }
        // 数据存入 redis
        CruiseRedisStorage.offer(inspectionMap);

        return isEnd;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CruiseExecuteFactory.CREATE.registerExecute(VOICE, this);
    }
}
