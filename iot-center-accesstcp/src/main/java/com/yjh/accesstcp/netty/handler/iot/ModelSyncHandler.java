package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
public class ModelSyncHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    private final SendToUpSystemServices sendToUpSystemServices;
    private final RedisTemplate redisTemplate;

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        try {
            log.info("--模型同步--");
            //1-巡视主机模型 设备模型及设备点位模型文件中应至少包括设备信息及巡视点位信息 A.2.3
            //2-机器人模型 巡视设备模型文件中应至少包括巡视主机、 智能分析主机、 机器人、 无人机、 高清视频、声纹的属性信息 A.2.4
            //3-摄像机模型 同上
            //4-点位模型 同上
            //5-无人机模型  同上
            //6-声纹模型  同上
            //7-任务文件  任务模型 A.2.5
            //8-检修区域配置文件 检修区域模型 A.2.6
            //9-地图文件
            //10-设备资源信息配置文件
            //11-物联设备模型
            List<Map<String, Object>> list = sendToUpSystemServices.creatModelList(xmlBaseModel.getCommand());


            XMLBaseModel resModel = new XMLBaseModel().setType("251").setCommand("1".equals(Constant.edgeLevel()) ? "3" : "4").setCode("200").setItems(list);
            clientHandler.send(resModel, header.getSessionId(), false);
        } catch (Exception e) {
            log.info("模型同步错误" + e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.MODEL_SYNC, this);
    }
}
