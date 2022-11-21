package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hyh
 * @since 2022/8/24
 **/
@Slf4j
@Service
public class LinkageSignalHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        String sendCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(sendCode, false)) {
            try {
                log.info("收到边缘节点 {} 的联动信号数据了", sendCode);
                String meteId = String.valueOf(xmlBaseModel.getItems().get(0).get("source_code"));
                String meteKind = String.valueOf(xmlBaseModel.getItems().get(0).get("source_type"));
                String value = String.valueOf(xmlBaseModel.getItems().get(0).get("source_attr"));
                String commit = String.valueOf(xmlBaseModel.getItems().get(0).get("source_value"));
                String time = String.valueOf(xmlBaseModel.getItems().get(0).get("source_time"));
                //处理联动信号数据 入库
                Map<String, Object> params = new HashMap<>(5);
                String edgeCode = (String)redisTemplate.opsForHash().get("region:idRefCode", sendCode);
                params.put("meteId", edgeCode + meteId);
                params.put("meteKind", meteKind);
                params.put("value", value);
                params.put("commit", commit);
                params.put("time", time);
                log.info("联动信号 {}", params);
                Constant.mapToOtherServer(params, Constant.DEAL_LINKAGE_SIGNAL_URL);
                if ("0".equals(meteKind)) {
                    //告知前端调用接口
                    Constant.restTemplateGet(Constant.SEQUENCE_URL, params);
                    Constant.restTemplateGet(Constant.UNION_URL, params);
                } else if ("1".equals(meteKind) && "变位".equals(value)) {
                    //不调用拍照  等边缘节点上报巡检结果（视频文件） 再做分析
                    log.info("收到一键顺控变位信号，等待边缘节点上报视频结果");
                } else {
                    Constant.restTemplateGet(Constant.UNION_URL, params);
                }
            } catch (Exception e) {
                log.error("联动信号处理失败！", e);
            }
        }
        String reXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] reProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, reXmlString);
        RobotServerHandler.send(reProtocol, sendCode);
        log.info("巡视主机给机器人{}响应了", sendCode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.LINKAGE_SIGNAL.getCode(), this);
    }
}
