package com.yjh.accessvideo.videostreamer;

import com.yjh.accessvideo.module.control.entity.VideoInfo;
import com.yjh.accessvideo.processmonitoring.impl.AutoRecoveredProcessRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ProcessManager {

    @Resource
    private RedisTemplate redisTemplate;

    private final ConcurrentHashMap<String, AutoRecoveredProcessRunner> runnerMap = new ConcurrentHashMap<String, AutoRecoveredProcessRunner>();
    private final ConcurrentHashMap<String, AutoRecoveredProcessRunner> runnerBackMap = new ConcurrentHashMap<String, AutoRecoveredProcessRunner>();

    public void run(VideoInfo videoInfo) throws RuntimeException, Exception {
        if (Optional.ofNullable(runnerMap.get(videoInfo.getId())).isPresent()) {
            if (videoInfo.getBack()) {
                this.terminate(videoInfo.getId());
            }
            else {
                AutoRecoveredProcessRunner runner = runnerMap.get(videoInfo.getId());
                runner.setStartTime(System.currentTimeMillis());
                return;
            }
        }

        List<String> cmd = new ArrayList<>();
        Optional.ofNullable(videoInfo.getCommand()).ifPresent(command -> {
            Collections.addAll(cmd, command.split(" "));});

        if (cmd.size() > 3) {
            AutoRecoveredProcessRunner runner = new AutoRecoveredProcessRunner(5000);
            runner.run(cmd);
            runnerMap.put(videoInfo.getId(), runner);
        }
    }

    public void terminate(String cameraId) {
        Optional.ofNullable(runnerMap.get(cameraId)).ifPresent(runner -> runner.terminate());
        runnerMap.remove(cameraId);
    }

    public void terminateAll() {
        for (String cameraId : runnerMap.keySet()) {
            Optional.ofNullable(runnerMap.get(cameraId)).ifPresent(runner -> runner.terminate());
        }
        runnerMap.clear();
    }

    public boolean isAlive(Long cameraId) {
        return Optional.ofNullable(runnerMap.get(cameraId)).map(runner -> runner.isAlive()).orElse(false);
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 10 * 60 * 1000)
    public void checkOvertime() {
        for (String cameraId : runnerMap.keySet()) {
            Optional.ofNullable(runnerMap.get(cameraId)).ifPresent(runner -> {
                int runnedTime = Math.toIntExact((System.currentTimeMillis() - runner.getStartTime()) / (1000 * 60));
                int overTime = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:logoutTime", "content")));
                log.info("-------Process 相机{} runned {}分钟, overTime {}分钟", cameraId, runnedTime, overTime);
                if (runnedTime > overTime) {
                    terminate(cameraId);
                    log.info("cameraId: {} 关闭成功", cameraId);
                }
            });
        }
    }
}
