package com.yjh.platform.module.task.scheduled;

import com.yjh.commons.core.NamedThreadFactory;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.quartz.SilentAlarmThread;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.configuration.ThreadPoolConfig;
import com.yjh.platform.module.task.service.AlarmShieldService;
import com.yjh.platform.module.task.service.TWarnInfoService;
import com.yjh.platform.module.user.service.TCameraInfoService;
import com.yjh.platform.module.user.service.TCameraPresetService;
import com.yjh.platform.module.video.entity.CameraConInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/24
 * @since [产品/模块版本] （可选）
 */
@Component
@Slf4j
public class SecondLevelSilentTaskScheduled {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TCameraPresetService tCameraPresetService;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private TWarnInfoService tWarnInfoDao;
    @Autowired
    private AlarmShieldService alarmShieldService;

    private static final Map<String, SilentAlarmThread> SILENT_THREAD_MAP = new HashMap<>();

    private static final ThreadPoolExecutor THREAD_POOLS = new ThreadPoolExecutor(4, 4, ThreadPoolConfig.getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000),
        new NamedThreadFactory("SILENT-POOL"), new ThreadPoolExecutor.AbortPolicy());

    @Scheduled(cron = "${seconds.silent.task.cron}")
    public void startAlarmGuard() {
        // 静默任务开关
        String silentFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isSilentTask", "content"));
        if (Boolean.FALSE.toString().equals(silentFlag)) {
            log.info("isSilentTask is false");
            return;
        }
        heartbeatCheck();
        List<CameraConInfo> list = tCameraPresetService.selectCameraBySecondSilent();
        for (CameraConInfo conInfo : list) {
            process(conInfo);
        }
    }

    private void process(CameraConInfo conInfo) {
        String ip = conInfo.getCameraIp();
        Integer port = conInfo.getPort();
        Long presetId = conInfo.getPresetId();
        Long cameraId = conInfo.getCameraId();

        Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(TCameraInfoService.cameraStateKey + ip);
        String state = redisInfoMap.get("state");

        if (presetId != null && presetId > 0 && StringUtils.equals("0", state)) {
            String key = Constant.SILENT_SECOND + cameraId + ":" + presetId;
            if (redisTemplate.hasKey(key)) {
                log.info("已经发送过了");
                return ;
            } else {
                Map<String, String> redisMap = new HashMap<>(4);
                redisMap.put("cameraId", String.valueOf(cameraId));
                redisMap.put("presetId", String.valueOf(presetId));
                redisTemplate.opsForHash().putAll(key, redisMap);
            }

            log.info("静默数据 ---------------- " + ip + ":" + port);

            SilentAlarmThread silentAlarmThread = new SilentAlarmThread(conInfo, redisTemplate, tCameraPresetService, applicationProperties, tWarnInfoDao,alarmShieldService);
            SILENT_THREAD_MAP.put(key, silentAlarmThread);
            THREAD_POOLS.execute(silentAlarmThread);
        }
    }

    private void heartbeatCheck() {
        long currTime = System.currentTimeMillis();
        SILENT_THREAD_MAP.forEach((k, v) -> {
            try {
                if (currTime - v.reciveTime() > 5 * 60 * 1000L) {
                    v.stopAlarmGuard();
                }
            } catch (Exception e) {
                log.error("校验心跳失败", e);
            }
        });
    }

    @PreDestroy
    public void stop() {
        SILENT_THREAD_MAP.forEach((k, v) -> v.stopAlarmGuard());
        THREAD_POOLS.shutdown();
    }
}
