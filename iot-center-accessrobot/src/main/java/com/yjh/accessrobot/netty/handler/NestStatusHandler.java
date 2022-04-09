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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class NestStatusHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler nestServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机机巢状态数据了+++++++++++++++++");
        String robotCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(robotCode, false)) {
            List<Map<String, String>> nestStatusList = new ArrayList<>();
            xmlBaseModel.getItems().forEach(res -> {
                Map<String, String> nestStatusMap = new HashMap<>(16);
                nestStatusMap.put("nestName", res.get("nest_name").toString());
                nestStatusMap.put("nestCode", res.get("nest_code").toString());
                nestStatusMap.put("time", res.get("time").toString());
                nestStatusMap.put("type", res.get("type").toString());
                nestStatusMap.put("value", res.get("value").toString());
                nestStatusMap.put("valueUnit", res.get("value_unit").toString());
                nestStatusMap.put("unit", res.get("unit").toString());
                nestStatusList.add(nestStatusMap);

                // 国网要求
                robotService.upToCruise(xmlBaseModel);
            });
            log.info("无人机机巢状态数据是：" + nestStatusList);

            for (int i = 0; i < nestStatusList.size(); i++) {
                redisTemplate.opsForHash().putAll("nestStatus:" + robotCode + ":" + nestStatusList.get(i).get("type"), nestStatusList.get(i));
            }
            // 无人机机巢状态更新同步到主表形成绑定关系
            if (nestStatusList.size() > 0) {
                robotService.updateNestInfo(robotCode, String.valueOf(nestStatusList.get(0).get("nestCode")), String.valueOf(nestStatusList.get(0).get("nestName")));
            }
            String statusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
            byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, statusXmlString);
            RobotServerHandler.send(statusProtocol, robotCode);
            log.info("巡视主机给无人机{}响应了", robotCode);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.NEST_STATUS.getCode(), this);
    }
}
