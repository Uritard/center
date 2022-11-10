package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.thread.StandTaskDealThread;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class RobotTaskStatusHandler implements MessageHandlerStrategy, InitializingBean {

    @Value("${other.webSocketUrl}")
    private String websocketUrl;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到任务状态数据了+++++++++++++++++");
        // Deal with robot task status data
        String robotCode = xmlBaseModel.getSendCode();
        Map<String, Object> taskStatusMap = new HashMap<>(16);
        taskStatusMap.put("taskPatrolled_id", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
        String taskName = xmlBaseModel.getItems().get(0).get("task_name").toString();
        taskStatusMap.put("taskName",taskName );
        String taskCode = String.valueOf(xmlBaseModel.getItems().get(0).get("task_code"));
        String taskId = taskCode;
        taskStatusMap.put("taskCode",  taskId);
        String taskState = xmlBaseModel.getItems().get(0).get("task_state").toString();
        taskStatusMap.put("taskState", taskState);
        taskStatusMap.put("planStartTime", xmlBaseModel.getItems().get(0).get("plan_start_time"));
        String startTime = xmlBaseModel.getItems().get(0).get("start_time").toString();
        taskStatusMap.put("startTime", DateTimeUtil.format(DateTimeUtil.parse(startTime)));
        taskStatusMap.put("taskProgress", xmlBaseModel.getItems().get(0).get("task_progress").toString());
        taskStatusMap.put("taskEstimatedTime", xmlBaseModel.getItems().get(0).containsKey("task_estimated_time") ?
                xmlBaseModel.getItems().get(0).get("task_estimated_time").toString() : "");
        taskStatusMap.put("description", xmlBaseModel.getItems().get(0).containsKey("description") ?
                xmlBaseModel.getItems().get(0).get("description").toString() : "");

        // 判断任务是否属于机器人本体任务
        Long robotId = robotService.selectIsRobotTask(taskId);
        if (Objects.nonNull(robotId)) {
            Map<String, String> jasonMap = new HashMap<>(2);
            jasonMap.put("type", "newTask");
            jasonMap.put("taskId", taskId);
            String json = JSON.toJSONString(jasonMap);
            Constant.postUrl(websocketUrl, json);

            StandTaskDealThread standTaskDealThread = new StandTaskDealThread(redisTemplate, taskId, robotCode);
            TaskExecutePool.getInstance().execute(standTaskDealThread);

            /// 以备后面做任务超时使用
            /*Map<String,Object> mapForTaskAreTime  = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
            Float tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));
            Map<String,String> mapForAbnormal = new HashMap<>();
            mapForAbnormal.put("taskStart",taskStatusMap.get("startTime").toString());
            mapForAbnormal.put("overDay",tasksAreTime.toString());
            String strForCountAbnormal = "countForAbnormal:"+taskStatusMap.get("taskCode").toString();
            redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);*/
        }

        Map<String, Object> listMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
        taskStatusMap.put("instanceList", Objects.isNull(listMap.get("instanceIdList")) ? "" : listMap.get("instanceIdList"));
        log.info("taskStatusMap==" + taskStatusMap);
        redisTemplate.opsForHash().putAll("RobotTaskStatus:" + robotCode + ":" + taskId, taskStatusMap);

        String taskStatusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] taskStatusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, taskStatusXmlString);
        RobotServerHandler.send(taskStatusProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);

        taskId = robotService.selectRealTaskId(taskCode);
        if(StringUtils.isEmpty(taskId)){
            taskId = taskCode;
            log.info("taskId is empty, use taskCode as taskId");
        }
        log.info("taskCode==={},taskId===={}", taskCode, taskId);
        Map<String, String> robotTaskStatus = new HashMap<>();
        if (Objects.nonNull(Constant.taskRobotMap.get(taskId))){
            robotTaskStatus = Constant.taskRobotMap.get(taskId);
        }
        robotTaskStatus.put(robotCode, taskState);
        Constant.taskRobotMap.put(taskId, robotTaskStatus);
        log.info("taskRobotMap Put {}", Constant.taskRobotMap.get(taskId));

        /// 本来好好的  由于机器人端乱上报消息  就不进行具体处理了
        //任务已执行和任务终止 更新任务结束时间
       if ("1".equals(taskState) || "4".equals(taskState)){
           Map<String, Object> taskMap = new HashMap<>();
           taskMap.put("taskId", taskId);
           taskMap.put("endTime", new Date());
           StaticContextAccessor.getBean(RobotService.class).updateTCruiseTask(taskMap);
           TCruiseResult tCruiseResult = StaticContextAccessor.getBean(RobotService.class).selectTaskResultId(taskId);
           if ("1".equals(taskState)){
               tCruiseResult.setCState(240); //任务已执行
           }else {
               tCruiseResult.setCState(242); //任务终止
           }
           int res = StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
           log.info("更新TCR的条数====" + res);
       }
        // 国网要求
        robotService.upToCruise(xmlBaseModel);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_TASK_STATUS.getCode(), this);
    }
}
