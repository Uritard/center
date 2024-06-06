package com.yjh.accessrobot.netty.handler;


import com.google.common.base.CaseFormat;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/6/5
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SystemCheckHandler implements MessageHandlerStrategy, InitializingBean {

    private final RedisTemplate redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++收到下级的系统自检数据了+++++++++++++++++");
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
        byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.AtomicSessionId.incrementAndGet(), sendSessionId, false, statusXmlString);
        RobotServerHandler.send(statusProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        if (CollectionUtils.isNotEmpty(xmlBaseModel.getItems())) {
            Map<String, Object> systemCheckMap = xmlBaseModel.getItems().get(0);
            Map<String, String> res = new HashMap<>(5);
            systemCheckMap.forEach((k, v) -> res.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k), String.valueOf(v)));
            res.put("edgeCode", sendCode);
            String key = "systemCheck:" + sendCode;
            redisTemplate.opsForHash().putAll(key, res);
            redisTemplate.expire(key, 7, TimeUnit.DAYS);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.SYSTEM_CHECK.getCode(), this);
    }
}
