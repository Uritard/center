package com.yjh.platform.common.quartz;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.websocket.WebSocketServer;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lqh
 * @since 2020/11/19
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Configuration
public class CheckTaskAreJob extends QuartzJobBean {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CheckTaskAreJob.class);

    public void executeInternal(JobExecutionContext context) {
        String taskId = context.getMergedJobDataMap().getString("taskId");
        Map<String,Object> jsonMap=new HashMap<>();
        jsonMap.put("type","taskAre");
        jsonMap.put("taskId",taskId);
        String jsonForTaskAre= JSON.toJSONString(jsonMap);
        log.info("任务超期的消息：   "+jsonForTaskAre);
        WebSocketServer.sendMsg(jsonForTaskAre);
    }
}
