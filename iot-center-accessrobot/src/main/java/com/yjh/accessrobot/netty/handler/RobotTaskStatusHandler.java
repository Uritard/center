package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
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

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++收到下级的任务状态数据了+++++++++++++++++");
        // Deal with robot task status data
        String robotCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(robotCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(robotCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }

        // 给下级响应
        String taskStatusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] taskStatusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, taskStatusXmlString);
        RobotServerHandler.send(taskStatusProtocol, robotCode);
        log.info("本级系统给下级{}响应了", robotCode);

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
                String webSocketUrl = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:webSocketUrl","content"));
                Constant.postUrl(webSocketUrl, json);

                // StandTaskDealThread standTaskDealThread = new StandTaskDealThread(redisTemplate, taskCode, robotCode);
                // TaskExecutePool.getInstance().execute(standTaskDealThread);
            }
        }

        Long robotId = robotService.selectRobotIdByCode(xmlBaseModel.getSendCode());
        String robotTaskStatusUp =String.valueOf(redisTemplate.opsForHash().get("t_sys_param:robotTaskStatusUp","content"));

        if (robotId != null && "true".equals(robotTaskStatusUp)) {
            // 上一级系统无法获取机器人任务进度，需将任务状态数据上报上一级系统
            String recvCode = (String)redisTemplate.opsForHash().get("t_sys_param:upSystemReceiveCode","content");
            List<Map<String,Object>> upItems = new ArrayList<>(xmlBaseModel.getItems().size());
            Map<String,Object> downItem = xmlBaseModel.getItems().get(0);
            Map<String,Object> upItem = Maps.newHashMap();
            upItem.put("taskPatrolledId", downItem.get("task_patrolled_id") == null ? null:downItem.get("task_patrolled_id").toString());
            upItem.put("taskName", downItem.get("task_name") == null ? null:downItem.get("task_name").toString());
            upItem.put("taskCode", downItem.get("task_code") == null ? null:downItem.get("task_code").toString());
            upItem.put("taskState", downItem.get("task_state") == null ? null:downItem.get("task_state").toString());
            upItem.put("planStartTime", downItem.get("plan_start_time") == null ? null:downItem.get("plan_start_time").toString());
            upItem.put("robotCode", xmlBaseModel.getSendCode());
            upItem.put("startTime", downItem.get("start_time") == null ? null:downItem.get("start_time").toString());
            upItem.put("taskProgress", downItem.get("task_progress") == null ? null:downItem.get("task_progress").toString());
            upItem.put("taskEstimatedTime", downItem.get("task_estimated_time") == null ? null:downItem.get("task_estimated_time").toString());
            upItem.put("description", downItem.get("description") == null ? null:downItem.get("description").toString());

            XMLBaseModel upXmlBaseModel = new XMLBaseModel()
                .setSendCode(Constant.sendCode())
                .setReceiveCode(recvCode)
                .setType("411")
                .setItems(Collections.singletonList(upItem));

            Map<String ,List<XMLBaseModel>> map = Maps.newHashMap();
            map.put("list",Collections.singletonList(upXmlBaseModel));
            Constant.mapToOtherServer(map, Constant.TCP_URL);
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
