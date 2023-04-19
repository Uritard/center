package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.Object2Map;
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
public class RobotTaskStatusToCruiseHandler implements MessageHandlerStrategy, InitializingBean {
    public static final String ROBOT_OR_DRONE_TASK = "robotOrDroneTask:";

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++收到下级系统的任务状态数据了+++++++++++++++++");
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

        Map<String, Object> item = xmlBaseModel.getItems().get(0);
        String taskCode = item.get("taskCode").toString();
        String robotNum = item.get("robotCode").toString();
        Long robotId = robotService.selectRobotIdByCode(item.get("robotCode").toString());
        if (robotId != null){
            redisTemplate.opsForHash().putAll(ROBOT_OR_DRONE_TASK + taskCode + ":" + robotId, item);
            redisTemplate.opsForHash().putAll("RobotTaskStatus:" + robotNum + ":" + taskCode, item);
        }

        String systemLevel = redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content").toString();
         if (!"1".equals(systemLevel)) {
             String recvCode = (String)redisTemplate.opsForHash().get("t_sys_param:upSystemReceiveCode","content");
             XMLBaseModel upXmlBaseModel = new XMLBaseModel()
                 .setSendCode(Constant.sendCode)
                 .setReceiveCode(recvCode)
                 .setType("411")
                 .setItems(Collections.singletonList(item));

             Map<String ,List<XMLBaseModel>> map = Maps.newHashMap();
             map.put("list",Collections.singletonList(upXmlBaseModel));
             Constant.mapToOtherServer(map, Constant.TCP_URL);
         }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_TASK_STATUS_TO_CRUISE.getCode(), this);
    }
}
