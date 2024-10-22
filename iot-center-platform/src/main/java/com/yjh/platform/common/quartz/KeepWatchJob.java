package com.yjh.platform.common.quartz;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.NumberUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.service.TCameraInfoService;
import com.yjh.platform.module.video.service.CameraConService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
@Component
public class KeepWatchJob {

    private final RedisTemplate redisTemplate;

    private final TCameraPresetDao tCameraPresetDao;

    private final CameraConService cameraConService;

    public KeepWatchJob(RedisTemplate redisTemplate, TCameraPresetDao tCameraPresetDao, CameraConService cameraConService) {
        this.redisTemplate = redisTemplate;
        this.tCameraPresetDao = tCameraPresetDao;
        this.cameraConService = cameraConService;
    }

    @Scheduled(cron = "30 * * * * ?")
    public void keepWatchRunning() {
        try {
            //获取间隔时间
            long keepWatchTime = NumberUtils.toLong(SysParamConfig.getSysContent("keepWatchTime")) * 60 * 1000L;
            if (keepWatchTime <= 0) {
                if (Constant.logUpLv3()) {
                    log.warn("守望位未开启：{}", SysParamConfig.getSysContent("keepWatchTime"));
                }
                return;
            }
            List<TCameraPreset> cameraPresetList = tCameraPresetDao.selectKeepWatchList();
            long now = System.currentTimeMillis();
            String currDateStr = DateTimeUtil.format(new Date());
            cameraPresetList.forEach(preset -> {
                Long cameraId = preset.getCameraId();
                String str = TCameraInfoService.cameraStateKey + preset.getCameraIp();
                Map<String, String> map = redisTemplate.opsForHash().entries(str);
                if (MapUtils.isNotEmpty(map)) {
                    if ("0".equals(map.get("state"))) {
                        //摄像机空闲 回归守望预置位
                        String lastTime = map.get("lastTime");
                        long oldTime = -1L;
                        try {
                            if (StringUtils.isNotEmpty(lastTime)) {
                                oldTime = DateTimeUtil.parse(lastTime).getTime();
                            }
                        } catch (Exception e) {
                            log.info("摄像机id：{} 回到守望位错误：{}", cameraId, e);
                        }

                        if ((now - oldTime) >= keepWatchTime) {
                            log.info("摄像机id：{} 守望时间：{}", cameraId, currDateStr);
                            ThreadPoolUtil.COMMON_POOL.addThread(() -> {
                                try {
                                    cameraConService.moveToPresetForTask(preset.getPresetId(), preset.getCameraId());
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            });
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.info("摄像头守望出错，", e);
        }
    }
}
