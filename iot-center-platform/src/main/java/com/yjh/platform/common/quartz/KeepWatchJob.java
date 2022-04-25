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

import java.text.SimpleDateFormat;
import java.util.Date;
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

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public KeepWatchJob(RedisTemplate redisTemplate, TCameraInfoDao tCameraInfoDao, TCameraPresetDao tCameraPresetDao) {
        this.tCameraInfoDao = tCameraInfoDao;
        this.redisTemplate = redisTemplate;
        this.tCameraPresetDao = tCameraPresetDao;
    }

    //相机转到预置位
    private static void move(HashMap<String, Object> map) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.getForObject(MOVE_URL, String.class, map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        while (true) {
            List<Long> cameraIdList = tCameraInfoDao.selectCameraAll();
            //获取间隔时间
            Map<String, String> mapForKeepWatchTime = redisTemplate.opsForHash().entries("t_sys_param:keepWatchTime");
            Integer keepWatchTime = Integer.parseInt(mapForKeepWatchTime.get("content")) * 60 * 1000;
            Date start = new Date();
            cameraIdList.forEach(cameraId -> {
                        String str = "camera_info:" + cameraId;
                        Map<String, String> map = redisTemplate.opsForHash().entries(str);
                        if (map != null && map.size() > 0) {
                            if ("0".equals(map.get("state"))) {
                                //摄像机空闲 回归守望预置位
                                String lastTime = map.get("lastTime");
                                long oldTime = -1L;
                                try {
                                    if (lastTime != null && !"".equals(lastTime)) {
                                        oldTime = simpleDateFormat.parse(lastTime).getTime();
                                    }
                                } catch (Exception e) {
                                    log.info("摄像机id：{} 回到守望位错误：{}", cameraId, e);
                                }
                                long now = new Date().getTime();
                                if ((now - oldTime) >= keepWatchTime) {
                                    TCameraPreset preset = tCameraPresetDao.selectKeepWatch(cameraId);
                                    if (preset != null) {
                                        HashMap<String, Object> moveMap = new HashMap<>();
                                        moveMap.put("presetId", preset.getPresetId());
                                        moveMap.put("cameraId", preset.getCameraId());
                                        log.info("守望时间："+new Date());
                                        new Thread(()->{
                                            move(moveMap);
                                        }).start();
                                        map.put("lastTime",simpleDateFormat.format(new Date()));
                                        redisTemplate.opsForHash().putAll(str,map);
                                    }
                                }

                            }
                        }
                    }
            );

            try {
                Date End = new Date();
                log.info("代码执行开始时间："+start);
                log.info("代码执行结束时间："+End);

                Thread.sleep(10*1000);//10秒查一次
            } catch (Exception e) {
                log.info("摄像头获取手守望时间出错，{}", e);
            }
        }
    }
}
