package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.extern.slf4j.Slf4j;
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
public class TaskControlHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        log.info("--响应控制--");
        Map<String, List<XMLBaseModel>> map = new HashMap<>(1);
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        map.put("list", list);
        Result re = Constant.otherServer(map, Constant.TASK_STATE_URL);
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>(1);
        if (StringUtils.equals("1", xmlBaseModel.getCommand())) {
            item.put("task_patrolled_id", re.getData());
        } else {
            item.put("task_patrolled_id", xmlBaseModel.getCode());
        }
        items.add(item);

        XMLBaseModel resModel = new XMLBaseModel().setType("251").setCommand("4").setCode("200").setItems(items);
        clientHandler.send(resModel, header.getSessionId(), false);
        log.info("==任务控制响应==" + re);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.TASK_CONTROL, this);
    }
}
