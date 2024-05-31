package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.entity.CruiseManualReview;
import com.yjh.accesstcp.module.device.entity.TCruiseTaskAdd;
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

@Service
@Slf4j
public class ReviewResultHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {
    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        log.info("--巡视结果确认--");
        String sendCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(sendCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }

        // 处理数据
        List<CruiseManualReview> resultList = new ArrayList<>();
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            CruiseManualReview taskResult = new CruiseManualReview();
            taskResult.setTaskResultId((String)item.get("task_patrolled_id"));
            taskResult.setInstanceIds((String)item.get("device_id"));
            taskResult.setPersonCheck((String)item.get("data_update"));
            taskResult.setRemark((String)item.get("manual_review_conclusion"));
            taskResult.setCheckUser((String)item.get("confirm_people"));
            taskResult.setCheckDate(DateTimeUtil.parse((String)item.get("confirm_date")));
            taskResult.setSendCode(sendCode);
            resultList.add(taskResult);
        }
        log.info("The review resultList to platform is=={}", resultList);
        Map<String, List<CruiseManualReview>> map = new HashMap<>();
        map.put("list", resultList);
        log.info("The review resultList to platform is=={}", xmlBaseModel);
        Result re = Constant.otherServerByList(resultList, Constant.TASK_REVIEW_PROCESS);
        log.info("--响应巡视结果确认--" + re);
        clientHandler.normalResponse("200", header.getSessionId());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.REVIEW_RESULT, this);
    }
}
