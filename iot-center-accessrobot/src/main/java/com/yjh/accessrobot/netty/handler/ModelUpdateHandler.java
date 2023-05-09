package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author hyh
 * @since 2022/8/19
 **/
@Slf4j
@Service
public class ModelUpdateHandler implements MessageHandlerStrategy, InitializingBean {

    @Resource
    private RobotService robotService;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        //<1>: =设备点位模型
        //<2>: =边缘节点模型
        //<3>: =机器人模型
        //<4>: =摄像机模型
        //<5>: =无人机模型
        //<6>: =声纹模型
        //<8>: =检修区域配置模型
        //<9>: =地图文件
        //<10>:=设备资源信息配置文件
        //1001 区域同步
        //边缘节点唯一标识
        String sendCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(sendCode, false)) {
            log.info("巡视主机收到边缘节点{}的模型更新指令了", sendCode);
            if (xmlBaseModel.getItems().get(0).containsKey("type") && xmlBaseModel.getItems().get(0).containsKey("file_path")) {
                String type = String.valueOf(xmlBaseModel.getItems().get(0).get("type"));
                if ("9".equals(type)) {
                    for(Map<String,Object> map:xmlBaseModel.getItems()) {
                        String filePath = String.valueOf(map.get("file_path"));
                        robotService.syncModelUpdate(type, filePath, sendCode);
                    }
                } else {
                    String filePath = String.valueOf(xmlBaseModel.getItems().get(0).get("file_path"));
                    robotService.syncModelUpdate(type, filePath, sendCode);
                }
            }
        }
        String reXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] reProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, reXmlString);
        RobotServerHandler.send(reProtocol, sendCode);
        log.info("巡视主机给机器人{}响应了", sendCode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.MODEL_UPDATE.getCode(), this);
    }
}
