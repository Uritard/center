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
import org.springframework.beans.factory.annotation.Value;
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
public class RobotStatusHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到机器人状态数据了+++++++++++++++++");
        // Deal with robot status data
        if(StringUtils.isBlank(xmlBaseModel.getCode())){
            String stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
            xmlBaseModel.setCode(stationCode);
        }

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
        String statusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, statusXmlString);
        RobotServerHandler.send(statusProtocol, robotCode);
        log.info("巡视主机给下级{}响应了", robotCode);

        List<Map<String, String>> robotStatusList = new ArrayList<>();
        xmlBaseModel.getItems().forEach(res -> {
            Map<String, String> robotStatusMap = new HashMap<>(16);
            // 2022过检 robot_name修改为patroldevice_name
            robotStatusMap.put("patrolDeviceName", String.valueOf(res.get("patroldevice_name")));
            robotStatusMap.put("patrolDeviceCode", String.valueOf(res.get("patroldevice_code")));
            robotStatusMap.put("robotCode",robotCode);
            robotStatusMap.put("time", res.get("time").toString());
            robotStatusMap.put("type", res.get("type").toString());
            robotStatusMap.put("value", res.get("value").toString());
            robotStatusMap.put("valueUnit", res.containsKey("value_unit") ? String.valueOf(res.get("value_unit")) : "");
            robotStatusMap.put("unit", res.containsKey("unit") ? String.valueOf(res.get("unit")) : "");
            robotStatusList.add(robotStatusMap);

        });
        log.info("机器人状态数据是：" + robotStatusList);

        for (int i = 0; i < robotStatusList.size(); i++) {
            String robotStatus = "RobotStatus:" + robotCode + ":" + robotStatusList.get(i).get("type");
            redisTemplate.opsForHash().putAll(robotStatus, robotStatusList.get(i));
            redisTemplate.expire(robotStatus, 7, TimeUnit.DAYS);
        }

        // 国网要求
        robotService.upToCruise(xmlBaseModel);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_STATUS.getCode(), this);
    }
}
