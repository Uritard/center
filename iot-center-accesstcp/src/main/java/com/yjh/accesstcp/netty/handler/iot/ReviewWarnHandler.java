package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.entity.TWarnInfo;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReviewWarnHandler  implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {
    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        log.info("--告警确认--");
        String sendCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(sendCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }

        // 处理数据
        List<TWarnInfo> warnInfoList = new ArrayList<>();
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            TWarnInfo tWarnInfo = new TWarnInfo();
            tWarnInfo.setTaskId((String) item.get("task_patrolled_id"));
            tWarnInfo.setInstanceIds((String)item.get("device_id"));
            tWarnInfo.setIsWarn(NumberUtils.toInt((String)item.get("is_alarm")));
            tWarnInfo.setDealPersonId((String)item.get("confirm_people"));
            tWarnInfo.setDealTime(DateTimeUtil.parse((String)item.get("confirm_date")));
            tWarnInfo.setEdgeCode(sendCode);
            warnInfoList.add(tWarnInfo);
        }
        log.info("The review warnList to platform is=={}", xmlBaseModel);
        Result re = Constant.otherServerByList(warnInfoList, Constant.WARN_REVIEW_PROCESS);
        log.info("--响应告警确认--" + re);
        clientHandler.normalResponse("200", header.getSessionId());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.REVIEW_WARN, this);
    }
}
