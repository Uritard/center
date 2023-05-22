package com.yjh.platform.module.task.scheduled;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.quartz.SilentAlarmThread;
import com.yjh.platform.common.utils.HttpAysncClientUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.task.dao.TWarnInfoDao;
import com.yjh.platform.module.task.service.TWarnInfoService;
import com.yjh.platform.module.user.service.TCameraPresetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Value("${spring.websocket.send.url}")
    private String syncWebsocketUrl;
    @Autowired
    private TWarnInfoService tWarnInfoDao;

    @Async
    @Scheduled(cron = "${seconds.silent.task.cron}")
    public void startAlarmGuard() {
        // 静默任务开关
        String silentFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isSilentTask", "content"));
        if (Boolean.FALSE.toString().equals(silentFlag)) {
            log.info("isSilentTask is false");
            return;
        }

        List<Map<String, Object>> list = tCameraPresetService.selectCameraBySecondSilent();
        for (Map<String, Object> map : list) {
            process(map);
        }
    }

    private void process(Map<String, Object> map) {
        String ip = String.valueOf(map.get("camera_ip"));
        String port = String.valueOf(map.get("port"));
        String presetId = String.valueOf(map.get("preset_id"));
        String cameraId = String.valueOf(map.get("camera_id"));
        String user = String.valueOf(map.get("camera_manager"));
        String password = String.valueOf(map.get("camera_code"));

        Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("camera_info:" + cameraId);
        String state = redisInfoMap.get("state");

        if (StringUtils.isNotEmpty(presetId) && StringUtils.equals("0", state)) {
            String key = Constant.SILENT_SECOND + cameraId + ":" + presetId;
            if (redisTemplate.hasKey(key)) {
                log.info("已经发送过了");
                return ;
            } else {
                HashMap<String, Object> redisMap = new HashMap<>(3);
                redisMap.put("cameraId", cameraId);
                redisMap.put("presetId", presetId);
                redisTemplate.opsForHash().putAll(key, redisMap);
            }

            log.info("静默数据 ---------------- " + ip + ":" + port);
            HttpAysncClientUtil.HttpAysncInit(user, password);
            SilentAlarmThread silentAlarmThread = new SilentAlarmThread(ip, port, presetId, cameraId, redisTemplate, tCameraPresetService, applicationProperties, syncWebsocketUrl, tWarnInfoDao);
            Thread thread = new Thread(silentAlarmThread);
            thread.setDaemon(true);
            thread.start();
        }
    }
}
