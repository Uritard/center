package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.SilentInfo;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 巡视主机接收边缘节点消息
 *
 * @author quzhihui
 * @date 2022/8/19 - 15:41
 */
@Slf4j
@Service
public class SilentMonitoringHandler implements MessageHandlerStrategy, InitializingBean {

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到静默监视的数据了+++++++++++++++++ xmlBaseModel:{}", JSON.toJSONString(xmlBaseModel));
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
        String operationXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.AtomicSessionId.incrementAndGet(), sendSessionId, false, operationXmlString);
        RobotServerHandler.send(operationProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        List<SilentInfo> silentInfos = new ArrayList<>();
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            if (!checkParam(item)){
                log.info("静默监视参数非法！{}",item);
                continue;
            }
            SilentInfo silentInfo = new SilentInfo();
            silentInfo.setPatrolDeviceCode(String.valueOf(item.get("patroldevice_code")))
                    .setDeviceName(String.valueOf(item.get("device_name")))
                    .setDeviceId(String.valueOf(item.get("device_id")))
                    .setTime(String.valueOf(item.get("time")))
                    .setRectangle(String.valueOf(item.get("rectangle")))
                    .setFileType(String.valueOf(item.get("file_type")))
                    .setFilePath(String.valueOf(item.get("file_path")))
                    .setMonitorType(String.valueOf(item.get("monitor_type")))
                    .setEdgeCode(sendCode);
            silentInfos.add(silentInfo);
        }
        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.SILENT_INFO_PROCESS, silentInfos, Result.class);
        }catch (Exception e){
            log.error("调用platform出错：{}", e.getMessage());
        }
    }

    private Boolean checkParam(Map<String, Object> item){
        String fileType = String.valueOf(item.get("file_type"));
        String filePath = String.valueOf(item.get("file_path"));
        String typeAll = "24";
        if (! typeAll.contains(fileType)){
            return false;
        }
        if (fileType.equals("2")){
            if(!filePath.endsWith(".jpg")){
                return false;
            }
        }
        if (fileType.equals("4")){
            if(!filePath.endsWith(".mp4")){
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.SILENT_MONITORING_DATA.getCode(), this);
    }
}
