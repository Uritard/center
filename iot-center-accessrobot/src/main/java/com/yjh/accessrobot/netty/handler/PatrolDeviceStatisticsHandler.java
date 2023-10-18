package com.yjh.accessrobot.netty.handler;

import com.google.common.collect.Maps;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/9/19
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@Service

public class PatrolDeviceStatisticsHandler implements MessageHandlerStrategy, InitializingBean {
    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("上级系统收到巡视设备统计信息");
        List<Map<String, Object>> items = xmlBaseModel.getItems();
        Map<String,Object> map = Maps.newHashMap();
        map.put("regionCode",xmlBaseModel.getSendCode());
        map.put("list",items);
        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.CRUISE_DEVICE_STATICS_PROCESS, map, Result.class);
        }catch (Exception e){
            log.error("调用platform出错：{}", e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.PATROL_DEVICE_STATISTICS.getCode(), this);
    }
}
