package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.yjh.accessrobot.netty.thread.TaskStatusThread;
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

        // 给机器人响应
        String taskStatusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] taskStatusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, taskStatusXmlString);
        RobotServerHandler.send(taskStatusProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);

        // 任务状态数据上报上一级系统
        robotService.upToCruise(xmlBaseModel);

        // 处理数据
        List<RobotPatrolTaskStatus> statusList = new ArrayList<>();
        for(Map<String, Object> item : xmlBaseModel.getItems()) {
            Map<String, Object> taskStatusMap = new HashMap<>(16);
            taskStatusMap.put("robotCode", robotCode);
            taskStatusMap.put("taskPatrolled_id", String.valueOf(item.get("task_patrolled_id")));
            String taskName = String.valueOf(item.get("task_name"));
            taskStatusMap.put("taskName", taskName);
            String taskId = String.valueOf(item.get("task_code"));
            taskStatusMap.put("taskCode", taskId);
            String taskState = String.valueOf(item.get("task_state"));
            taskStatusMap.put("taskState", taskState);
            taskStatusMap.put("planStartTime", String.valueOf(item.get("plan_start_time")));
            String startTime = String.valueOf(item.get("start_time"));
            taskStatusMap.put("startTime", DateTimeUtil.format(DateTimeUtil.parse(startTime)));
            taskStatusMap.put("taskProgress", String.valueOf(item.get("task_progress")));
            taskStatusMap.put("taskEstimatedTime", item.containsKey("task_estimated_time") ?
                    String.valueOf(item.get("task_estimated_time")) : "");
            taskStatusMap.put("description", item.containsKey("description") ?
                    String.valueOf(item.get("description")) : "");

            String toJSON = JSONObject.toJSONString(taskStatusMap);
            RobotPatrolTaskStatus taskStatus = JSONObject.toJavaObject(JSON.parseObject(toJSON), RobotPatrolTaskStatus.class);
            statusList.add(taskStatus);

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
            }

            Map<String, Object> listMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
            taskStatusMap.put("instanceList", Objects.isNull(listMap.get("instanceIdList")) ? "" : listMap.get("instanceIdList"));
            log.info("taskStatusMap==" + taskStatusMap);
            redisTemplate.opsForHash().putAll("RobotTaskStatus:" + robotCode + ":" + taskId, taskStatusMap);

            Map<String, String> robotTaskStatus = new HashMap<>();
            if (Objects.nonNull(Constant.taskRobotMap.get(taskId))) {
                robotTaskStatus = Constant.taskRobotMap.get(taskId);
            }
            robotTaskStatus.put(robotCode, taskState);
            Constant.taskRobotMap.put(taskId, robotTaskStatus);
            log.info("taskId is {} ,taskRobotMap is {}", taskId, Constant.taskRobotMap.get(taskId));

            // 任务已执行和任务终止 更新任务结束时间
            if (/*"1".equals(taskState) ||*/ "4".equals(taskState)) {
                Map<String, Object> taskMap = new HashMap<>(2);
                taskMap.put("taskId", taskId);
                taskMap.put("endTime", new Date());
                robotService.updateTCruiseTask(taskMap);
                //操作类的任务以及只有机器人的巡检点的任务才做处理

                TCruiseTask tCruiseTask = robotService.selectTCruiseTask(taskId);
                boolean operationTaskFlag = robotService.selectDictCodeByColName("operation_task").contains(tCruiseTask.getType());
                if (operationTaskFlag) {
                    TCruiseResult tCruiseResult = robotService.selectTaskResultId(taskId);
                    if ("1".equals(taskState)) {
                        //任务已执行
                        tCruiseResult.setCState(240);
                    } else {
                        //任务终止
                        tCruiseResult.setCState(242);
                    }
                    int res = robotService.updateTCruiseResult(tCruiseResult);
                    log.info("更新TCR的条数====" + res);
                }
            }

            TaskStatusThread taskStatusThread = new TaskStatusThread(redisTemplate, robotService, taskStatusMap, websocketUrl);
            TaskExecutePool.getInstance().execute(taskStatusThread);
        }

        // 是否升级任务流程
        String isUpgradeTask = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isUpgradeTask", "content"));
        if (StringUtils.equals("true", isUpgradeTask)){
            // 将任务状态发送至platform处理
            try {
                StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_STATUS_PROCESS, statusList, Result.class);
            }catch (Exception e){
                log.error("调用platform出错：{}", e.getMessage());
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_TASK_STATUS.getCode(), this);
    }
}
