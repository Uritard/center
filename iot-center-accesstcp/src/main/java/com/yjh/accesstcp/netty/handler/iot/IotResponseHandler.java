package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import com.yjh.accesstcp.thread.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/15
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IotResponseHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        if ("4".equals(xmlBaseModel.getCommand())) {
            // 100:需要重发注册消息 200:服务端响应 我方开启心跳
            if ("100".equals(xmlBaseModel.getCode())) {
                log.info("---Iot响应---200需要重发注册消息----");
            } else if ("200".equals(xmlBaseModel.getCode())) {
                log.info("---Iot响应--200响应成功----");
            } else {
                log.info("---Iot响应--400拒绝注册----");
            }
        } else {
            log.info("---收到返回消息 type: {}, command: {} ----", xmlBaseModel.getType(), xmlBaseModel.getCommand());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.RESPONSE, this);
    }
}
