/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.thread;

import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.task.entity.TaskSimpleInfo;
import com.yjh.platform.module.user.service.TCameraPresetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/11/1
 * @since [产品/模块版本] （可选）
 */
@Component
public class PatrolTimeoutScheduled {
    private static final Logger log = LoggerFactory.getLogger(PatrolTimeoutScheduled.class);

    private RedisTemplate redisTemplate;
    private UPatrolResultDao uPatrolResultDao;

    @Scheduled(cron = "0 */10 * * * ?")
    public void silentTaskScheduled() {
        log.info("超时判断定时任务");

        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String tasksAreTime = hashOperations.get("t_sys_param:tasksAreTime", "content");

        List<TaskSimpleInfo> runningList = uPatrolResultDao.selectTaskIsRunning();
        
        for (TaskSimpleInfo task : runningList) {

        }

    }
}
