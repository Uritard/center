/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.CruiseManualReview;
import com.yjh.accessrobot.module.command.entity.RobotPatrolTaskResult;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/12/15
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@Service
public class ReviewResultHandler implements MessageHandlerStrategy, InitializingBean {
    /**
     * 根据type区别不同的消息进行处理
     *
     * @param ctx                通道
     * @param robotServerHandler 接收消息处理类
     * @param xmlBaseModel       xml格式的内容
     * @param sendSessionId      发送会话序列号
     * @param receiveSessionId   接收会话序列号
     * @return void
     * @throws Exception
     */
    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId,
        long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到巡视结果了+++++++++++++++++");
        // Deal with robot task result data
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
        String cruiseResultXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true,sendCode));
        byte[] cruiseResultProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, cruiseResultXmlString);
        RobotServerHandler.send(cruiseResultProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        // 处理数据
        List<CruiseManualReview> resultList = new ArrayList<>();
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            CruiseManualReview taskResult = new CruiseManualReview();
            taskResult.setTaskId((String)item.get("task_code"));
            taskResult.setTaskResultId((String)item.get("task_patrolled_id"));
            taskResult.setInstanceId(NumberUtils.toLong((String)item.get("device_id")));
            taskResult.setEvaluationState(NumberUtils.toInt((String)item.get("evaluation_state")));
            taskResult.setEvaluationStateName((String)item.get("evaluation_state_name"));
            taskResult.setIdentifyResult(NumberUtils.toInt((String)item.get("identify_result")));
            taskResult.setIdentifyResultName((String)item.get("identify_result_name"));
            taskResult.setIdentifyState(NumberUtils.toInt((String)item.get("identify_state")));
            taskResult.setIdentifyStateName((String)item.get("identify_state_name"));
            taskResult.setPersonCheck((String)item.get("value"));
            taskResult.setCheckUser((String)item.get("check_user"));
            taskResult.setCheckDate(DateTimeUtil.parse((String)item.get("time")));
            taskResult.setSendCode(sendCode);
            resultList.add(taskResult);
        }
        log.info("The review resultList to platform is=={}", resultList);

        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_REVIEW_PROCESS, resultList, Result.class);
        }catch (Exception e){
            log.error("调用platform出错：{}", e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.REVIEW_RESULT.getCode(), this);
    }
}
