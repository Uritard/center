package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.RobotPatrolTaskStatus;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.thread.StandTaskDealThread;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
        if (StringUtils.isEmpty(robotCode)) {
            log.error("机器人/无人机编码为空");
            throw new RuntimeException("机器人/无人机编码为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(robotCode, false))) {
            log.error("机器人/无人机未注册或未连接");
            throw new RuntimeException("机器人/无人机未注册或未连接");
        }

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
            RobotPatrolTaskStatus taskStatus = new RobotPatrolTaskStatus();
            taskStatus.setRobotCode(robotCode);
            taskStatus.setTaskPatrolledId(String.valueOf(item.get("task_patrolled_id")));
            taskStatus.setTaskName(String.valueOf(item.get("task_name")));
            String taskCode = String.valueOf(item.get("task_code"));
            taskStatus.setTaskCode(taskCode);
            taskStatus.setTaskState(String.valueOf(item.get("task_state")));
            taskStatus.setPlanStartTime(String.valueOf(item.get("plan_start_time")));
            taskStatus.setStartTime(DateTimeUtil.format(DateTimeUtil.parse(String.valueOf(item.get("start_time")))));
            taskStatus.setTaskProgress(String.valueOf(item.get("task_progress")));
            taskStatus.setTaskEstimatedTime(Objects.nonNull(item.get("task_estimated_time")) ? String.valueOf(item.get("task_estimated_time")) : "");
            taskStatus.setDescription(Objects.nonNull(item.get("description")) ? String.valueOf(item.get("description")) : "");
            statusList.add(taskStatus);

            // 判断任务是否属于机器人本体任务
            Long robotId = robotService.selectIsRobotTask(taskCode);
            if (Objects.nonNull(robotId)) {
                Map<String, String> jasonMap = new HashMap<>(2);
                jasonMap.put("type", "newTask");
                jasonMap.put("taskId", taskCode);
                String json = JSON.toJSONString(jasonMap);
                Constant.postUrl(websocketUrl, json);

                StandTaskDealThread standTaskDealThread = new StandTaskDealThread(redisTemplate, taskCode, robotCode);
                TaskExecutePool.getInstance().execute(standTaskDealThread);
            }
        }
        log.info("The statusList to platform is=={}", statusList);

        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_STATUS_PROCESS, statusList, Result.class);
        }catch (Exception e){
            log.error("调用platform出错：{}", e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_TASK_STATUS.getCode(), this);
    }
}
