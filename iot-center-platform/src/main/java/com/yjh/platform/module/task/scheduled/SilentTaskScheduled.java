package com.yjh.platform.module.task.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.user.service.TCameraPresetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 丫C
 * @date 2022/4/8
 */
@Component("SilentTaskScheduled")
@Slf4j
public class SilentTaskScheduled {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private TCameraPresetService tCameraPresetService;

    /**
     * 调用相机转到预置位接口
     */
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPresetForTask?presetId={presetId}&cameraId={cameraId}";
    /**
     * 调用相机抓图接口
     */
    private static final String CAPTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePictureForTask?cameraId={cameraId}";

    private static final String msg = "success";

    @PostConstruct
    public void init(){
        //TODO something
    }

    @Scheduled(cron = "0/${silent.task.interval} * * * * ?")
    public void SilentTaskScheduled() {

        log.info("定时任务");
//        List<Map<String, Long>> list = tCameraPresetService.selectCameraBySilent();
//        for (Map<String, Long> map : list) {
//            String cameraId = String.valueOf(map.get("camera_id"));
//            String presetId = String.valueOf(map.get("preset_id"));
//
//            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("camera_info:" +cameraId);
//            String state = redisInfoMap.get("state");
//
//            // 相机状态为闲置(state为0闲置,为1占用)时,做静默任务
//            if (StringUtils.equals("0", state) && StringUtils.isNotEmpty(presetId)){
//                log.info("cameraId为{},presetId为{}的相机准备做静默任务", cameraId, presetId);
//                try {
//                    HashMap<String, Object> moveMap = new HashMap<>(5);
//                    moveMap.put("cameraId", cameraId);
//                    moveMap.put("presetId", presetId);
//                    // 转预置位 先霸占相机
//                    redisInfoMap.put("state", "1");
//                    redisTemplate.opsForHash().putAll("camera_info:" + cameraId, redisInfoMap);
//                    moveToPreset(moveMap);
//                    // 等待摄像头转到预置位
//                    Map<String, Object> mapForWaitTime = redisTemplate.opsForHash().entries("t_sys_param:waitTime");
//                    Long waitTime = Long.valueOf((String) mapForWaitTime.get("content"));
//                    TimeUnit.MILLISECONDS.sleep(waitTime);
//                    HashMap<String, Object> captureMap = new HashMap<>(3);
//                    captureMap.put("cameraId", cameraId);
//                    // 拍照
//                    Result result = capturePicture(captureMap);
//                    // 将相机状态置为闲置
//                    redisInfoMap.put("state", "0");
//                    redisTemplate.opsForHash().putAll("camera_info:" + cameraId, redisInfoMap);
//                    // 分析
//                    analysePicture(result);
//                }catch (Exception e) {
//                    log.error("设置摄像机状态出错" + e);
//                    redisInfoMap.put("state", "0");
//                    redisTemplate.opsForHash().putAll("camera_info:" + cameraId, redisInfoMap);
//                }
//            }
//        }
    }

    /**
     * 根据相机拍照结果发送算法进行分析
     * @param result 相机抓图返回结果
     * @return void
     */
    private void analysePicture(Result result){
        String absPath = "";

        if(Objects.nonNull(result) && Objects.equals(msg, result.getMessage())){
            JSONObject jsonForRe = (JSONObject) JSON.toJSON(result.getData());
            absPath = String.valueOf(jsonForRe.get("absPath"));
            // 调用算法接口分析结果
            PicAnalyseRequest request = new PicAnalyseRequest();
            request.setRequestHostIp("123");
            request.setRequestHostPort("456");
            request.setRequestId(UUID.randomUUID() + "#jm");
            AnalyseObject analyseObject = new AnalyseObject();
            analyseObject.setObjectId("1");
            ArrayList<String> typeList = new ArrayList<>();
            typeList.add("");
            analyseObject.setTypeList(typeList);
            ArrayList<String> imageUrlList = new ArrayList<>();
            imageUrlList.add(absPath);
            analyseObject.setImageUrlList(imageUrlList);


        }else {
            // 抓图失败 逻辑处理
            // TODO something
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

}
