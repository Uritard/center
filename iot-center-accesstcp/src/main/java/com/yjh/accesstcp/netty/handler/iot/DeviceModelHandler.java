package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.AnalysisUnionTaskFileService;
import com.yjh.accesstcp.module.device.service.DeviceModelService;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author llf
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceModelHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {
    private final DeviceModelService deviceModelService;
    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        try {
            log.info("--设备模型下发指令--");
            Map<String, Object> item = xmlBaseModel.getItems().get(0);
            String filePath = String.valueOf(item.get("value"));
            if (StringUtils.isNotEmpty(filePath)) {
                switch (xmlBaseModel.getCommand()) {
                    //<1>: =标准点位模型文件
                    case "1":
                        log.info("标准点位模型文件 filePath {}", filePath);
                        deviceModelService.handleDeviceModelFile(filePath,"1");
                        break;
                    case "2":
                        log.info("巡视装置模型文件 filePath {}", filePath);
                        deviceModelService.handleDeviceModelFile(filePath,"2");
                        break;
                    case "3":
                        log.info("点位告警阈值配置模型文件 filePath {}", filePath);
                        deviceModelService.handleDeviceModelFile(filePath,"3");
                        break;
                    default:
                        break;
                }
            } else {
                log.error("联动文件下发 filePath is empty !!!");
            }
            clientHandler.normalResponse("200", header.getSessionId());
        } catch (Exception e) {
            log.info("联动文件下发指令" + e);
            clientHandler.normalResponse("400", header.getSessionId());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.DEVICEMODEL_SEND, this);
    }
}
