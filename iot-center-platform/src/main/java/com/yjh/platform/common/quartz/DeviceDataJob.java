package com.yjh.platform.common.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Configuration
public class DeviceDataJob extends QuartzJobBean {

    @Autowired
    RedisTemplate redisTemplate;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeviceDataJob.class);

//    @Scheduled(fixedRate = 20000)
    public void executeInternal(JobExecutionContext context) {
        try {
           log.info("正在进行定时任务");
           log.info("完成定时任务执行");
        } catch (Exception e) {
            log.error("定时任务异常" + e);
        }

    }
}
