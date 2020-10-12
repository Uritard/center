package com.yjh.platform.common.quartz;

import com.yjh.platform.common.utils.StaticContextAccessor;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;

@Service
public class JobManager {
    // 日志模块
    private static Logger logger = LoggerFactory.getLogger(JobManager.class);


    @Autowired
    private Scheduler scheduler;
    @Value("${data.period.upload.interval}")
    private String dataUploadInterval;

    /**
     * 任务是否存在
     *
     * @param jobName
     * @param jobGroup
     * @return
     * @throws Exception
     */
    public boolean checkExists(String jobName, String jobGroup) throws Exception {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        JobKey jobKey = new JobKey(jobName, jobGroup);
        if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
            return true;
        }
        return false;
    }

    /**
     * 验证表达式时间是否有效
     *
     * @param cronExpression
     * @return
     */
    public boolean isValidExpression(String cronExpression) {
        CronTriggerImpl trigger = new CronTriggerImpl();
        try {
            trigger.setCronExpression(cronExpression);
            Date date = trigger.computeFirstFireTime(null);
            return date != null && date.after(new Date());
        } catch (Exception e) {
            logger.error("[TaskUtils.isValidExpression]:failed. throw ex:", e);
        }
        return false;
    }

    /**
     * 新建一个任务
     */
    public String addJob(QuartzTask quartzTask) throws Exception {
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzTask.getJobName(), quartzTask.getJobGroup());
        JobKey jobKey = new JobKey(quartzTask.getJobName(), quartzTask.getJobGroup());
        if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
            logger.error("OC Access DataQuery Job already exist");
            return "false";
        }
        logger.info("dataUploadInterval :" + dataUploadInterval);

        //创建一个jobDetail的实例，将该实例与HelloJob Class绑定
        JobDetail jobDetail = JobBuilder.newJob(DeviceDataJob.class).withIdentity(quartzTask.getJobName(), quartzTask.getJobGroup()).build();
        //创建一个Trigger触发器的实例，定义该job立即执行，并且每2秒执行一次，一直执行
        SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(quartzTask.getJobName(), quartzTask.getJobGroup()).startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(Integer.parseInt(dataUploadInterval)).repeatForever()).build();
        scheduler.scheduleJob(jobDetail, trigger);
        return "success";
    }

    /**
     *新建一个定时巡视任务
     */
    public String addCruiseTaskJob(QuartzTask quartzTask) throws Exception {
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzTask.getJobName(), quartzTask.getJobGroup());
        JobKey jobKey = new JobKey(quartzTask.getJobName(), quartzTask.getJobGroup());
        logger.info("jobKey: "+jobKey+"  triggerKey: "+triggerKey);

        if (StaticContextAccessor.getBean(Scheduler.class).checkExists(jobKey) && StaticContextAccessor.getBean(Scheduler.class).checkExists(triggerKey)) {
            logger.error("OC Access DataQuery Job already exist");
            return "false";
        }
        logger.info("dataUploadInterval :" + dataUploadInterval);
        JobDetail jobDetail = JobBuilder.newJob(CruiseTaskJob.class).withIdentity(quartzTask.getJobName(), quartzTask.getJobGroup()).build();
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(quartzTask.getJobName(), quartzTask.getJobGroup())
                .withSchedule(cronSchedule(quartzTask.getCronExpression()))
                .build();

        StaticContextAccessor.getBean(Scheduler.class).scheduleJob(jobDetail, cronTrigger);
        return "success";
    }

    /**
     *新建一个立即巡视任务
     */
    public String addCruiseTaskJobNow(QuartzTask quartzTask) throws Exception {
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzTask.getJobName(), quartzTask.getJobGroup());
        JobKey jobKey = new JobKey(quartzTask.getJobName(), quartzTask.getJobGroup());
        logger.info("jobKey: "+jobKey+"  triggerKey: "+triggerKey);

        if (StaticContextAccessor.getBean(Scheduler.class).checkExists(jobKey) && StaticContextAccessor.getBean(Scheduler.class).checkExists(triggerKey)) {
            logger.error("OC Access DataQuery Job already exist");
            return "false";
        }
        logger.info("dataUploadInterval :" + dataUploadInterval);
        JobDetail jobDetail = JobBuilder.newJob(CruiseTaskJob.class).withIdentity(quartzTask.getJobName(), quartzTask.getJobGroup()).build();
        SimpleTrigger simpleTrigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger3", "group1")
                .startAt(new Date())
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(3)
                                .withRepeatCount(0))//重复执行的次数，因为加入任务的时候马上执行了，所以不需要重复，否则会多一次。
                .build();

        StaticContextAccessor.getBean(Scheduler.class).scheduleJob(jobDetail, simpleTrigger);
        return "success";
    }
}
