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
public class NestStatusHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler nestServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机机巢状态数据了+++++++++++++++++");
        String stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
        xmlBaseModel.setCode(stationCode);
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
        String statusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, statusXmlString);
        RobotServerHandler.send(statusProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        String robotCode = robotService.selectRobotOrEdgeRobot(xmlBaseModel, sendCode);
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
        });
        log.info("无人机机巢状态数据是：" + nestStatusList);

        for (int i = 0; i < nestStatusList.size(); i++) {
            String nestStatus = "nestStatus:" + robotCode + ":" + nestStatusList.get(i).get("type");
            redisTemplate.opsForHash().putAll(nestStatus, nestStatusList.get(i));
            redisTemplate.expire(nestStatus, 7, TimeUnit.DAYS);
        }
        // 无人机机巢状态更新同步到主表形成绑定关系
//        if (nestStatusList.size() > 0) {
//            robotService.updateNestInfo(robotCode, String.valueOf(nestStatusList.get(0).get("nestCode")), String.valueOf(nestStatusList.get(0).get("nestName")));
//        }
        // 国网要求
        robotService.upToCruise(xmlBaseModel);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.NEST_STATUS.getCode(), this);
    }
}
