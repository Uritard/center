package com.yjh.accesstcp.netty.handler.algorithm;

import com.yjh.accesstcp.module.device.callback.handler.SyncSendHandler;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class AlgorithmResponseHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        if ("4".equals(xmlBaseModel.getCommand())) {
            if ("200".equals(xmlBaseModel.getCode())) {
                log.info("算法平台---2514响应--200响应成功----");
                List<Map<String, Object>> items = xmlBaseModel.getItems();
                if (CollectionUtils.isEmpty(items)) {
                    return;
                }
                //同步请求操作
                Object waiter = SyncSendHandler.waitingMap.remove(header.getReceiveSessionId());
                if (Objects.nonNull(waiter)){
                    SyncSendHandler.msgMap.put(header.getReceiveSessionId(), items);
                    synchronized (waiter){
                        waiter.notifyAll();
                    }
                }
            } else {
                log.info("---2514响应--400拒绝----");
            }
        } else {
            log.info("---收到返回消息 type: {}, command: {} ----", xmlBaseModel.getType(), xmlBaseModel.getCommand());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.CLOUD, AlgorithmHandlerEnum.RESPONSE, this);
    }
}
