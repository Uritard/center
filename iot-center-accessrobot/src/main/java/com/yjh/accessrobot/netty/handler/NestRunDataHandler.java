package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class NestRunDataHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机机巢运行数据了+++++++++++++++++");
        String sendCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(sendCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(sendCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }

        // 给下级响应
        String operationXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, operationXmlString);
        RobotServerHandler.send(operationProtocol, sendCode);
        log.info("巡视主机给下级{}响应了", sendCode);

        String robotCode = robotService.selectRobotOrEdgeRobot(xmlBaseModel, sendCode);
        List<Map<String, String>> nestOperationList = new ArrayList<>();
        xmlBaseModel.getItems().forEach(res -> {
            Map<String, String> nestOperationMap = new HashMap<>(16);
            nestOperationMap.put("nestName", res.get("nest_name").toString());
            nestOperationMap.put("nestCode", res.get("nest_code").toString());
            nestOperationMap.put("moduleNo", res.get("module_no").toString());
//                nestOperationMap.put("time", res.get("time").toString());
            nestOperationMap.put("type", res.get("type").toString());
            nestOperationMap.put("value", res.get("value").toString());
            nestOperationMap.put("valueUnit", res.get("value_unit").toString());
            nestOperationMap.put("unit", res.get("unit").toString());
            nestOperationList.add(nestOperationMap);
        });

        for (int i = 0; i < nestOperationList.size(); i++) {
            String nestOperation =  "nestOperation:" + robotCode + ":" + nestOperationList.get(i).get("type");
            redisTemplate.opsForHash().putAll(nestOperation, nestOperationList.get(i));
            redisTemplate.expire(nestOperation, 7, TimeUnit.DAYS);
        }

        // 国网要求
        robotService.upToCruise(xmlBaseModel);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.NEST_RUN_DATA.getCode(), this);
    }
}
