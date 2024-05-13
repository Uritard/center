package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/5/7
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class MaintenanceHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        log.info("--检修区域--");
        Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        robotMap.put("list", list);
        Result re = Constant.otherServer(robotMap, Constant.MAINTENANCE_URL);
        log.info("--响应检修--" + re);
        clientHandler.normalResponse("200", header.getSessionId());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.MAINTENANCE, this);
    }
}
