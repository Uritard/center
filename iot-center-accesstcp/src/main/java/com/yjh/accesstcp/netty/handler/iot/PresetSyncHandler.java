package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.utils.StaticContextAccessor;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.TCameraPresetService;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/5/7
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class PresetSyncHandler  implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        StaticContextAccessor.getBean(TCameraPresetService.class).updateCameraPresetInfo(xmlBaseModel);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.PRESET_SYNC, this);
    }
}
