package com.yjh.accesstcp.netty.handler.algorithm;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.utils.CollectionUtil;
import com.yjh.accesstcp.commons.utils.CommonUtils;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/5/8
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReliabilityIndexHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    private final SendToUpSystemServices sendToUpSystemServices;

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        String command = xmlBaseModel.getCommand();
        Map<String, Object> item = xmlBaseModel.getItems().get(0);
        String beginTime = String.valueOf(item.get("begin_time"));
        String endTime = String.valueOf(item.get("end_time"));
        List<Map<String, Object>> list;
        Map<String, Object> params = new HashMap<>(3);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        if (Constant.ONE.equals(command)) {
            params.put("type", "1");
        } else {
            params.put("type", "2");
        }
        Result re = Constant.otherServerPost(params, Constant.ALGORITHM_STATISTICS_URL);
        list = CommonUtils.castListMap(re.getData(), String.class, Object.class);
        XMLBaseModel resModel = new XMLBaseModel().setType("251").setCommand("4").setCode("200").setItems(list);
        clientHandler.send(resModel, header.getSessionId(), false);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.CLOUD, AlgorithmHandlerEnum.RELIABILITY_INDEX, this);
    }
}
