package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
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
public class RobotRunDataHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId,
        long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++收到下级的运行数据了+++++++++++++++++");
        // Deal with robot operation data
        String stationCode = (String)redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
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
        String robotCode = robotService.selectRobotOrEdgeRobot(xmlBaseModel, sendCode);
        String onlineStatus = String.valueOf(redisTemplate.opsForValue().get("onlineStatus:"+ robotCode));

        if ("1".equals(onlineStatus)) {
            // 给下级响应
            String statusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(false, sendCode));
            byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, statusXmlString);
            RobotServerHandler.send(statusProtocol, sendCode);
            log.info("本级系统给下级{}响应了", sendCode);
            return;
        }


        List<Map<String, String>> robotOperationList = new ArrayList<>();

        for (Map<String, Object> res : xmlBaseModel.getItems()) {
            Map<String, String> robotOperationMap = new HashMap<>(16);
            // 2022过检 修改robot_name为patroldevice_name
            String patrolDeviceName = robotService.getRobotName(String.valueOf(res.get("patroldevice_code")));
            if (StringUtils.isNotEmpty(patrolDeviceName)) {
                res.put("patroldevice_name", patrolDeviceName);
            }
            robotOperationMap.put("patrolDeviceName", String.valueOf(res.get("patroldevice_name")));
            robotOperationMap.put("patrolDeviceCode", String.valueOf(res.get("patroldevice_code")));
            robotOperationMap.put("robotCode", robotCode);
            robotOperationMap.put("time", res.get("time").toString());
            robotOperationMap.put("type", res.get("type").toString());
            robotOperationMap.put("value", res.get("value").toString());
            robotOperationMap.put("valueUnit", res.get("value_unit").toString());
            robotOperationMap.put("unit", res.get("unit").toString());
            if (!unitCheck(robotOperationMap)) {
                continue;
            }
            robotOperationList.add(robotOperationMap);
        }
        log.info("robotOperationList: {}", JSON.toJSONString(robotOperationList));

        //完全解析完成的算成功  否则失败 返回 500给下级
        boolean isFull = xmlBaseModel.getItems().size() == robotOperationList.size();
        String operationXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(isFull, sendCode));
        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, operationXmlString);
        RobotServerHandler.send(operationProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        for (Map<String, String> stringStringMap : robotOperationList) {
            String robotOperation = "RobotOperation:" + robotCode + ":" + stringStringMap.get("type");
            redisTemplate.opsForHash().putAll(robotOperation, stringStringMap);
            redisTemplate.expire(robotOperation, 7, TimeUnit.DAYS);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_RUN_DATA.getCode(), this);
    }

    private static boolean unitCheck(Map<String, String> weatherMap) {
        String type = weatherMap.get("type");
        String unit = weatherMap.get("unit");
        String value = weatherMap.get("value");
        String valueUnit = weatherMap.get("valueUnit");

        if (StringUtils.isNotBlank(type)) {
            switch (type) {
                //水平速度
                case "1":
                //垂直速度
                case "4":
                    return StringUtils.equalsAnyIgnoreCase(unit, "m/s", "cm/s", "米/秒", "厘米/秒") && valueUnit.equals(value + unit);
                //行驶里程
                case "2":
                //飞行距离
                case "5":
                //飞行高度
                case "6":
                    return StringUtils.equalsAnyIgnoreCase(unit, "km", "m", "cm", "千米", "米", "厘米") && valueUnit.equals(value + unit);
                //电池电量
                case "3":
                    return "%".equalsIgnoreCase(unit) && valueUnit.equals(value + unit);
                //飞行时长
                case "7":
                    return StringUtils.equalsAnyIgnoreCase(unit, "h", "m", "min", "s", "小时", "分钟", "秒") && valueUnit.equals(value + unit);
                //云台俯仰角
                case "8":
                case "9":
                case "10":
                    return StringUtils.equalsAnyIgnoreCase(unit, "°", "度") && valueUnit.equals(value + unit);
                //充电电流
                case "11":
                    return StringUtils.equalsAnyIgnoreCase(unit, "A", "mA") && valueUnit.equals(value + unit);
                //其他
                default:
                    return false;
            }

        }
        return false;
    }
}
