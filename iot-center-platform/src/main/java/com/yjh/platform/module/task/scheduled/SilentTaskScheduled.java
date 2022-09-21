package com.yjh.platform.module.task.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.user.service.TCameraPresetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 丫C
 * @date 2022/4/8
 */
@Component("SilentTaskScheduled")
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class SilentTaskScheduled {

    private final RedisTemplate redisTemplate;
    private final TCameraPresetService tCameraPresetService;

    @Value("${silent.task.cron}")
    private String silentTaskTime;

    /**
     * 调用相机转到预置位接口
     */
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPresetForTask?presetId={presetId}&cameraId={cameraId}";
    /**
     * 调用相机抓图接口
     */
    private static final String CAPTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePictureForTask?cameraId={cameraId}&meteName={meteName}";
    /**
     * 调用智能分析主机图像分析接口
     */
    private static final String ANALYSE_URL = "http://iot-center-accessvideo/picAnalyseNoDetection";

    private static final String MSG = "success";
    private static final String FLAG = "false";

    @Async
    @Scheduled(cron = "${silent.task.cron}")
    public void silentTaskScheduled() {
        log.info("定时任务");
        // 静默任务开关
        String silentFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isSilentTask","content"));
        if (StringUtils.equals(FLAG, silentFlag)){
            return;
        }

        // 分析主机开关
        String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelDefectAnalysis","content"));
        if (StringUtils.equals(FLAG, flag)){
            return;
        }
        List<Map<String, Object>> list = tCameraPresetService.selectCameraBySilent();
        for (Map<String, Object> map : list) {
            String cameraId = String.valueOf(map.get("camera_id"));
            String presetId = String.valueOf(map.get("preset_id"));
            String presetName = String.valueOf(map.get("preset_name"));

            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("camera_info:" + cameraId);
            String state = redisInfoMap.get("state");
            String lastTime = redisInfoMap.get("lastTime");

//            Integer keepSilent = Integer.parseInt(silentTaskTime.substring(4,5)) * 60 * 1000;
            // 相机状态为闲置(state为0闲置,为1占用)时,做静默任务
            if (StringUtils.equals("0", state) && StringUtils.isNotEmpty(presetId)){

//                long oldTime = -1L;
//                try {
//                    if (lastTime != null && !"".equals(lastTime)) {
//                        oldTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastTime).getTime();
//                    }
//                } catch (Exception e) {
//                    log.info("摄像机id：{} 回到静默位错误：{}", cameraId, e);
//                }
//
//                if ((System.currentTimeMillis() - oldTime) < keepSilent) {
//                    log.info("cameraId为{}的相机在被控制", cameraId);
//                    continue;
//                }
                log.info("cameraId为{},presetId为{}的相机准备做静默任务", cameraId, presetId);
                try {
                    HashMap<String, Object> moveMap = new HashMap<>(5);
                    moveMap.put("cameraId", cameraId);
                    moveMap.put("presetId", presetId);
                    // 转预置位 先霸占相机
                    redisInfoMap.put("state", "1");
                    redisTemplate.opsForHash().putAll("camera_info:" + cameraId, redisInfoMap);
                    moveToPreset(moveMap);
                    // 等待摄像头转到预置位
                    Long waitTime = Long.valueOf(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:waitTime", "content")));
                    TimeUnit.MILLISECONDS.sleep(waitTime);
                    HashMap<String, Object> captureMap = new HashMap<>(3);
                    captureMap.put("cameraId", cameraId);
                    captureMap.put("meteName", presetName);
                    // 拍照
                    Result result = capturePicture(captureMap);
                    // 将相机状态置为闲置
                    redisInfoMap.put("state", "0");
                    redisInfoMap.put("lastTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    redisTemplate.opsForHash().putAll("camera_info:" + cameraId, redisInfoMap);

                    // 分析
                    analysePicture(result, Long.valueOf(presetId));
                }catch (Exception e) {
                    log.error("设置摄像机状态出错" + e.getMessage());
                    redisInfoMap.put("state", "0");
                    redisTemplate.opsForHash().putAll("camera_info:" + cameraId, redisInfoMap);
                }
            }
        }
    }

    /**
     * 根据相机拍照结果发送算法进行分析
     * @param result 相机抓图返回结果
     * @param presetId 预置位id  充当巡视点ID
     */
    private void analysePicture(Result result, Long presetId){
        if(Objects.nonNull(result) && Objects.equals(MSG, result.getMessage())){
            JSONObject jsonForRe = (JSONObject) JSON.toJSON(result.getData());
            String absPath = String.valueOf(jsonForRe.get("absPath"));
            // 调用算法接口分析结果
            List<Analysis> analysisList = new ArrayList<>();
            Analysis analysis = new Analysis()
                    // 暂定静默监视识别类型为12,没有实际意义
                    .setAnalyseType("12")
                    .setInstanceId(presetId)
                    .setTaskId("jm")
                    .setPicPath(absPath);
            analysisList.add(analysis);
            restTemplatePost(ANALYSE_URL, analysisList);
        }else {
            // 抓图失败 逻辑处理
            log.info("抓图失败");
        }
    }

    /**
     * 相机转到预置位
     *
     * @param map 相机id与预置位id
     * @return Boolean
     */
    private Boolean moveToPreset(HashMap<String,Object> map) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.getForObject(MOVE_URL, String.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    /**
     * 相机抓图
     *
     * @param map 相机id
     * @return void
     */
    private Result capturePicture(HashMap<String,Object> map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(CAPTURE_URL, Result.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

    /**
     * 请求其他服务
     *
     * @param url 请求地址
     * @param analysisList 请求参数
     * @return String
     */
    public void restTemplatePost(String url, List<Analysis> analysisList ) {
        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForEntity(url, analysisList, List.class);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
