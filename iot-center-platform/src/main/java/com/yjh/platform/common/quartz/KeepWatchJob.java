package com.yjh.platform.common.quartz;

import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TCameraPreset;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2022/4/8
 */
@Slf4j
public class KeepWatchJob implements Runnable {

    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPresetForTask?presetId={presetId}&cameraId={cameraId}";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TCameraInfoDao tCameraInfoDao;

    @Autowired
    private TCameraPresetDao tCameraPresetDao;

    public KeepWatchJob(RedisTemplate redisTemplate,TCameraInfoDao tCameraInfoDao,TCameraPresetDao tCameraPresetDao){
        this.tCameraInfoDao = tCameraInfoDao;
        this.redisTemplate = redisTemplate;
        this.tCameraPresetDao = tCameraPresetDao;
    }

    //相机转到预置位
    private static void move(HashMap<String,Object> map) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.getForObject(MOVE_URL, String.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        while (true) {
            List<Long> cameraIdList = tCameraInfoDao.selectCameraAll();
            cameraIdList.forEach(cameraId -> {
                        String str = "camera_info:" + cameraId;
                        Map<String, String> map = redisTemplate.opsForHash().entries(str);
                        if (map != null && map.size() > 0) {
                            if ("0".equals(map.get("state"))) {
                                //摄像机空闲 回归守望预置位
                                TCameraPreset preset = tCameraPresetDao.selectKeepWatch(cameraId);
                                if (preset != null) {
                                    HashMap<String, Object> moveMap = new HashMap<>();
                                    moveMap.put("presetId", preset.getPresetId());
                                    moveMap.put("cameraId", preset.getCameraId());
                                    move(moveMap);
                                }
                            }
                        }
                    }
            );
            //获取间隔时间
            Map<String,String> mapForKeepWatchTime  = redisTemplate.opsForHash().entries("t_sys_param:keepWatchTime");
            Integer keepWatchTime = Integer.parseInt(mapForKeepWatchTime.get("content"))*60*1000;
            try {
                Thread.sleep(keepWatchTime);
            }catch (Exception e){
                log.info("摄像头获取手守望时间出错，{}",e);
            }
        }
    }
}
