/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessrobot.netty.handler.simple;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.enumeration.UpgradeStatusEnum;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.entity.ModelCommand;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.proxy.PlatformProxy;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.entiy.UpgradeResult;
import com.yjh.accessrobot.netty.handler.MessageHandlerStrategy;
import com.yjh.accessrobot.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.yjh.accessrobot.module.command.service.RobotService.SYNC_MODE_CACHE;

/**
 * 简易机器人模型请求
 * @author Chenfei
 * @date 2025-07-08
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RobotModelRequest implements MessageHandlerStrategy, InitializingBean {
    private final TRobotInfoDao tRobotInfoDao;
    private final PlatformProxy platformProxy;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId,
        long receiveSessionId) throws Exception {
        String robotCode = xmlBaseModel.getSendCode();
        log.info("巡视主机收到简易机器人模型请求: {}", robotCode);
        boolean flag = false;
        try {
            if (!Constant.robotRegisterFlag.getOrDefault(robotCode, false)) {
                return;
            }
            TRobotInfo robotInfo = tRobotInfoDao.selectRobotInfoByCode(robotCode);
            // 调用 platform 下发点位模型
            ModelCommand command = new ModelCommand().setRobotId(robotInfo.getRobotId());
            platformProxy.simpleModelSend(command);

            flag = true;
        } catch (Exception e) {
            log.error("处理简易机器人模型请求异常", e);
        } finally {
            String responseXml = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(flag, robotCode));
            byte[] responseByte = PlatformPacketUtil.createPacket(Constant.AtomicSessionId.incrementAndGet(), sendSessionId, false, responseXml);
            RobotServerHandler.send(responseByte, robotCode);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.MODEL_REQUEST.getCode(), this);
    }
}
