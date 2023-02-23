package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.module.command.service.TCameraPresetService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class CameraPresetSycTaskHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private TCameraPresetService tCameraPresetService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("边缘节点收到巡视系统同步过来的相机预置位更新的指令,这是边缘节点的响应");
        tCameraPresetService.updateCameraPresetInfo(xmlBaseModel, receiveSessionId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.CAMERA_PRESET_UPDATE.getCode(), this);
    }
}
