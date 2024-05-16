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
public class NestRunDataHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机机巢运行数据了+++++++++++++++++");
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

        String robotCode = robotService.selectRobotOrEdgeRobot(xmlBaseModel, sendCode);
        List<Map<String, String>> nestOperationList = new ArrayList<>();

        for (Map<String, Object> res : xmlBaseModel.getItems()) {
            Map<String, String> nestOperationMap = new HashMap<>(16);
            nestOperationMap.put("nestName", res.get("nest_name").toString());
            nestOperationMap.put("nestCode", res.get("nest_code").toString());
            nestOperationMap.put("moduleNo", res.get("module_no").toString());
            nestOperationMap.put("type", res.get("type").toString());
            nestOperationMap.put("value", res.get("value").toString());
            nestOperationMap.put("valueUnit", res.get("value_unit").toString());
            nestOperationMap.put("unit", res.get("unit").toString());
            if (!unitCheck(nestOperationMap)) {
                continue;
            }
            nestOperationList.add(nestOperationMap);
        }
        log.info("nestOperationList: {}", JSON.toJSONString(nestOperationList));

        //完全解析完成的算成功  否则失败 返回 500给下级
        boolean isFull = xmlBaseModel.getItems().size() == nestOperationList.size();
        String operationXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(isFull, sendCode));
        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.AtomicSessionId.incrementAndGet(), sendSessionId, false, operationXmlString);
        RobotServerHandler.send(operationProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        for (Map<String, String> stringStringMap : nestOperationList) {
            String nestOperation = "nestOperation:" + robotCode + ":" + stringStringMap.get("type");
            redisTemplate.opsForHash().putAll(nestOperation, stringStringMap);
            redisTemplate.expire(nestOperation, 7, TimeUnit.DAYS);
        }
    }

    private static boolean unitCheck(Map<String, String> weatherMap) {
        String type = weatherMap.get("type");
        String unit = weatherMap.get("unit");
        String value = weatherMap.get("value");
        String valueUnit = weatherMap.get("valueUnit");

        if (StringUtils.isNotBlank(type)) {
            switch (type) {
                //电池电量
                case "1":
                    return "%".equalsIgnoreCase(unit) && valueUnit.equals(value + unit);
                //电池电压
                case "4":
                    return StringUtils.equalsAnyIgnoreCase(unit, "v", "kv", "伏", "千伏", "伏特") && valueUnit.equals(value + unit);
                //舱内温度
                case "5":
                    return StringUtils.equalsAnyIgnoreCase(unit, "℃", "℉", "摄氏度", "华氏度") && valueUnit.equals(value + unit);
                //舱内湿度
                case "6":
                    return StringUtils.equalsAnyIgnoreCase(unit, "%RH", "%") && valueUnit.equals(value + unit);
                //其他
                default:
                    return valueUnit.equals(value + unit);
            }
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.NEST_RUN_DATA.getCode(), this);
    }
}
