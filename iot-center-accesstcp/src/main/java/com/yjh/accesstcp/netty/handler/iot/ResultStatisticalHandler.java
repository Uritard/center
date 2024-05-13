package com.yjh.accesstcp.netty.handler.iot;

import com.alibaba.fastjson.JSON;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

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
@RequiredArgsConstructor
public class ResultStatisticalHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    private final SendToUpSystemServices sendToUpSystemServices;

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        log.info("巡视结果统计查询：{}", JSON.toJSONString(xmlBaseModel));
        List<Map<String, Object>> list = null;
        if (xmlBaseModel.getItems() != null && xmlBaseModel.getItems().size() > 0) {
            Map<String, Object> item = xmlBaseModel.getItems().get(0);
            item.put("cmd", xmlBaseModel.getCommand());
            list = sendToUpSystemServices.resultStatistical(item);
        }

        if (list == null) {
            clientHandler.normalResponse("100", header.getSessionId());
        } else {
            XMLBaseModel resModel = new XMLBaseModel().setType("251").setCommand("4").setCode("200").setItems(list);
            clientHandler.send(resModel, header.getSessionId(), false);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.RESULT_STATISTICAL, this);
    }
}
