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
import com.yjh.accessrobot.module.command.entity.TWarnInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/12/15
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@Service
public class ReviewWarnHandler implements MessageHandlerStrategy, InitializingBean {
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
        log.info("+++++++++++++++++巡视主机收到告警审核了+++++++++++++++++");
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
        byte[] cruiseResultProtocol = PlatformPacketUtil.createPacket(Constant.AtomicSessionId.incrementAndGet(), sendSessionId, false, cruiseResultXmlString);
        RobotServerHandler.send(cruiseResultProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        // 处理数据
        List<TWarnInfo> warnInfoList = new ArrayList<>();
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            TWarnInfo tWarnInfo = new TWarnInfo();
            tWarnInfo.setOriginId((String)item.get("origin_id"));
            tWarnInfo.setWarnName((String)item.get("warn_name"));
            tWarnInfo.setWarnLevel(NumberUtils.toInt((String)item.get("warn_level")));
            tWarnInfo.setWarnContent((String)item.get("warn_content"));
            tWarnInfo.setDealInfo((String)item.get("deal_info"));
            tWarnInfo.setDealType(NumberUtils.toInt((String)item.get("deal_type")));
            tWarnInfo.setOutRange((String)item.get("out_range"));
            tWarnInfo.setDealPersonId((String)item.get("deal_person_id"));
            tWarnInfo.setDealTime(DateTimeUtil.parse((String)item.get("deal_time")));
            tWarnInfo.setDefectModel(NumberUtils.toInt((String)item.get("defect_model")));
            tWarnInfo.setEdgeCode(sendCode);
            warnInfoList.add(tWarnInfo);
        }
        log.info("The review warnInfoList to platform is=={}", warnInfoList);

        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.WARN_REVIEW_PROCESS, warnInfoList, Result.class);
        }catch (Exception e){
            log.error("调用platform出错：{}", e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.REVIEW_WARN.getCode(), this);
    }
}
