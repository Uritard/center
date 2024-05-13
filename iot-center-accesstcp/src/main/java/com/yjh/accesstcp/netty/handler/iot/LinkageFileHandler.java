package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.TCruiseTaskAdd;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.AnalysisUnionTaskFileService;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

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
public class LinkageFileHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    private final SendToUpSystemServices sendToUpSystemServices;
    private final AnalysisUnionTaskFileService analysisUnionTaskFileService;

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        try {
            log.info("--联动文件下发指令--");
            Map<String, Object> item = xmlBaseModel.getItems().get(0);
            String filePath = String.valueOf(item.get("file_path"));
            if (StringUtils.isNotEmpty(filePath)) {
                switch (xmlBaseModel.getCommand()) {
                    //<1>: =联动配置文件
                    case "1":
                        log.info("联动配置文件{}", filePath);
                        analysisUnionTaskFileService.handleUnionTaskFile(filePath);
                        break;
                    case "2":
                    case "3":
                        //<2>: =一键顺控视频确认反馈信息文件
                        //<3>: =反向联动信息转发文件
                        sendToUpSystemServices.dealLinkage(filePath);
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
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.LINKAGE_FILE, this);
    }

}
