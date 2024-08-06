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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ModelChangeHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        String command = xmlBaseModel.getCommand();
        // 无人机机巢控制参数校验
        if (StringUtils.equals("20005", xmlBaseModel.getType())) {
            String value = String.valueOf(xmlBaseModel.getItems().get(0).get("value"));
            switch (xmlBaseModel.getCommand()) {
                case "1":
                case "3":
                    if (!ArrayUtils.contains(new String[]{"1", "2", "3"}, value)) {
                        clientHandler.normalResponse("400", header.getSessionId());
                        return;
                    }
                    break;
                case "2":
                    if (!ArrayUtils.contains(new String[]{"1", "2"}, value)) {
                        clientHandler.normalResponse("400", header.getSessionId());
                        return;
                    }
                    break;
                default:
                    clientHandler.normalResponse("400", header.getSessionId());
                    return;
            }
        }
        log.info("--响应控制 控制下发--");
        Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        robotMap.put("list", list);
        //国网要求
        Result re = Constant.otherServer(robotMap, Constant.ROBOT_TASK_URL);
        log.info("==控制响应== {}", re);
        log.error("巡视下发设备控制指令 command: {}", command);
        clientHandler.normalResponse("200", header.getSessionId());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.MODEL_CHANGE, this);
    }
}
