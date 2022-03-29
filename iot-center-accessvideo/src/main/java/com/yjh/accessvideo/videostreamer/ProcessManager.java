package com.yjh.accessvideo.videostreamer;

import com.yjh.accessvideo.module.control.entity.VideoInfo;
import com.yjh.accessvideo.processmonitoring.impl.AutoRecoveredProcessRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ProcessManager {

    @Value("${overTime}")
    private Integer overTime;

    private final ConcurrentHashMap<Long, AutoRecoveredProcessRunner> runnerMap = new ConcurrentHashMap<Long, AutoRecoveredProcessRunner>();

    public void run(VideoInfo videoInfo) throws RuntimeException, Exception {
        if (Optional.ofNullable(runnerMap.get(videoInfo.getId())).isPresent()) {
            return;
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

    public void terminate(Long cameraId) {
        Optional.ofNullable(runnerMap.get(cameraId)).ifPresent(runner -> runner.terminate());
        runnerMap.remove(cameraId);
    }

    public void terminateAll() {
        for (Long cameraId : runnerMap.keySet()) {
            Optional.ofNullable(runnerMap.get(cameraId)).ifPresent(runner -> runner.terminate());
        }
        runnerMap.clear();
    }

    public boolean isAlive(Long cameraId) {
        return Optional.ofNullable(runnerMap.get(cameraId)).map(runner -> runner.isAlive()).orElse(false);
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 10 * 60 * 1000)
    public void checkOvertime() {
        for (Long cameraId : runnerMap.keySet()) {
            Optional.ofNullable(runnerMap.get(cameraId)).ifPresent(runner -> {
                Integer runnedTime = Math.toIntExact((System.currentTimeMillis() - runner.getStartTime()) / (1000 * 60));
                log.info("-------Process 相机{} runned {}分钟", cameraId, runnedTime);
                if (runnedTime > overTime) {
                    terminate(cameraId);
                }
            });
        }
    }
}
