package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.entity.enums.VoiceType;
import com.yjh.platform.module.patrol.entity.voice.*;
import com.yjh.platform.module.patrol.thread.CruiseRedisStorage;
import com.yjh.platform.scheduled.ScheduledMapConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.*;

import static com.yjh.platform.module.patrol.CruiseConstant.AbnormalResDescEnum.ANALYSE_REQFAILED;
import static com.yjh.platform.module.patrol.CruiseConstant.AbnormalResDescEnum.ANALYSISING;
import static com.yjh.platform.module.patrol.CruiseConstant.*;

/**
 * @Author: lqh
 * @Date: 2024/05/28
 */
@Slf4j
@Service
public class VoicePrintDataCollectRespService {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private PatrolResultHandler patrolResultHandler;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UPatrolTaskService uPatrolTaskService;

    public Map<String,Object> voiceprintDataCollectRetNotify(VoicePrintDataCollectRetNotifyResp resp) {
        log.info("声纹采集数据返回: {}", resp);
        Map<String, Object> re = new HashMap<>();
        re.put("code",200);
        String requestId = resp.getRequestId();
        String[] reList = requestId.split("#");
        String taskId = reList[1];
        String instanceId = reList[2];
        String key = UPatrolTaskService.PATROL_TASK_PREFIX + taskId + ":" + instanceId;

        Map<String,String> inspectionMap = redisTemplate.opsForHash().entries(key);
        String voiceFilePath = Constant.isWindows() ? "D:\\TestProject\\file\\"+System.currentTimeMillis()+".wav":inspectionMap.get("voiceFilePath");

//        if (resp.getResultList() == null || resp.getResultList().size() != 1) {
//            log.info("采集结果返回错误！");
//            re.put("code",400);
//            // 录音结果处理
//            warnResult(inspectionMap,CruiseConstant.AbnormalResDescEnum.RECORDING_FAILURE.getDesc());
//            return re;
//        }
        String objectId = inspectionMap.get("objectId");
        boolean isContains = false;
        for (ResultListData resultListData : resp.getResultList()) {
            String cruiseId = resultListData.getObjectId();
            String code = resultListData.getCode();
            if ("2000".equals(code) && objectId.equals(cruiseId)) {
                isContains = true;
                for (ResultData resultData : resultListData.getResults()) {
                    String fileName = resultData.getFilename();
                    String fileUrl = resultData.getFileUrl();
                    try {
                        URL httpUrl = new URL(fileUrl);
                        File voiceSaveFile = new File(voiceFilePath);
                        voiceSaveFile.createNewFile();
                        FileUtils.copyURLToFile(httpUrl, voiceSaveFile);

                        String absVoicePath = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:absVoicePath").get("content"));
                        String voiceFileUrl = voiceFilePath.replace(absVoicePath, Constant.RELATIVE_VOICE_PATH);
                        inspectionMap.put("picpath", voiceFileUrl);
                        inspectionMap.put("voicePath", voiceFileUrl);
                        inspectionMap.put("resultDesc", "录音成功");
                        inspectionMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                        inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
                        inspectionMap.put("endTime", DateTimeUtil.getDateTimeString());

                        ScheduledMapConfig.schedule(5,1, t->{analyse(taskId, fileUrl, instanceId,inspectionMap);});
                    } catch (Exception e) {
                        log.error("声纹文件下载失败！", e);
                        // 录音结果处理
                        warnResult(inspectionMap,CruiseConstant.AbnormalResDescEnum.RECORDING_FAILURE.getDesc());
                    }
                }
            }
            if (!isContains) {
                // 录音结果处理
                warnResult(inspectionMap,CruiseConstant.AbnormalResDescEnum.RECORDING_FAILURE.getDesc());
                return re;
            }
        }

        CruiseRedisStorage.offer(inspectionMap);
        return re;
    }

    private void warnResult(Map<String,String> inspectionMap,String resultDesc){
        inspectionMap.put("resultNum", "-1");
        inspectionMap.put("resultDesc", resultDesc);
        inspectionMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_NOPIC));
        // 巡视结果，异常
        inspectionMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
        // 巡检数据状态，执行失败
        inspectionMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_FAILED));
        uPatrolTaskService.patrolTaskResultHandler(inspectionMap);
        CruiseRedisStorage.offer(inspectionMap);
    }

    private void analyse(String taskId, String url, String instanceId,Map<String,String> inspectionMap) {


        VoicePrintAnalyseReq req = new VoicePrintAnalyseReq();
        req.setRequestHostIp(applicationProperties.getAudioConfig().getRequestHostIp());
        req.setRequestHostPort(applicationProperties.getAudioConfig().getRequestHostPort());
        String requestId = UUID.randomUUID().toString()+ "#"+taskId+"#"+inspectionMap.get("instanceId");
        req.setRequestId(requestId);
        List<ObjectData> objectList = new ArrayList<>();
        ObjectData objectData = new ObjectData();
        objectData.setObjectId(inspectionMap.get("objectId"));
        objectData.setVoiceUrl(url);
        objectData.setTypeList(VoiceType.getAllType());
        objectData.setVoiceCollectTime(inspectionMap.get("cruiseTime"));
        objectList.add(objectData);
        req.setObjectList(objectList);
        String code = "-1";
        try {
            code = Constant.httpPost(applicationProperties.getAudioConfig().getVoiceprintAnalyseUrl(), JSON.toJSONString(req));
            if (StringUtils.isEmpty(code)){
                code = "-1";
            }
            JSONObject jsonObject = JSON.parseObject(code);
            code = String.valueOf(jsonObject.get("code"));
        } catch (Exception e) {
            log.error("调用声纹分析失败:", e);
            code = "-1";
        }
        if ("200".equals(code)){
            // 录音结果处理
            inspectionMap.put("resultDesc", ANALYSISING.getDesc());
        } else {
            // 录音结果处理
            warnResult(inspectionMap,ANALYSE_REQFAILED.getDesc());
        }
        CruiseRedisStorage.offer(inspectionMap);

    }


    public Map<String,Object> voiceprintAnalyseRetNotify(VoicePrintAnalyseRetNotifyResp resp) {
        log.info("声纹分析数据返回: {}", resp);
        Map<String,Object> re = new HashMap<>();
        re.put("code",200);
        String requestId = resp.getRequestId();
        String[] reList = requestId.split("#");
        String taskId = reList[1];
        String instanceId = reList[2];
        String key = UPatrolTaskService.PATROL_TASK_PREFIX + taskId + ":" + instanceId;

        Map<String,String> inspectionMap = redisTemplate.opsForHash().entries(key);
        String objectId = inspectionMap.get("objectId");
        boolean isContains = false;
        for (ResultListData resultListData : resp.getResultList()) {
            String cruiseId = resultListData.getObjectId();
            String code = resultListData.getCode();
            if ("2000".equals(code) && objectId.equals(cruiseId)) {
                isContains = true;
                StringBuilder type = new StringBuilder();
                StringJoiner typeName = new StringJoiner(" ");
                StringBuilder points = new StringBuilder();
                int num = 0;
                boolean isWarn = false;
                for (ResultData resultData : resultListData.getResults()) {
                    if ("1".equals(resultData.getValue())) {
                        type.append(resultData.getType()).append(",");
                        typeName.add(resultData.getDesc());
                        points.append(resultData.getStartTime()).append(",").append(resultData.getEndTime()).append(";");
                        num ++;
                        isWarn = true;
                    }
                }

                if (isWarn){
                    patrolResultHandler.voiceResultHandler(typeName.toString(), inspectionMap, 0, "声纹分析");
                } else {
                    typeName.add("正常");
                }
                String typeNameStr =  typeName.toString();
                CommonUtils.clearLastChar(points);

                inspectionMap.put("resultNum", String.valueOf(num));
                inspectionMap.put("resultDesc", typeNameStr);
                inspectionMap.put("points", points.toString());
            }
            if (!isContains) {
                // 录音结果处理
                warnResult(inspectionMap,ANALYSE_REQFAILED.getDesc());
                return re;
            }
        }
        CruiseRedisStorage.offer(inspectionMap);
        uPatrolTaskService.patrolTaskResultHandler(inspectionMap);
        return re;
    }
}
