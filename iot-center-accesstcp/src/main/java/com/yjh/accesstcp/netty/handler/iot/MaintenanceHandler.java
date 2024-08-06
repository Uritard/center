package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/5/7
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MaintenanceHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {
    private final SendToUpSystemServices sendToUpSystemServices;
    private final RedisTemplate redisTemplate;

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        log.info("--检修区域--");
        Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);

        Constant.standardPoints();
        Constant.middlegroundIds();
        List<Map<String, Object>> items = xmlBaseModel.getItems();
        // 开关若打开则直接转发检修区域到下级
        String deviceMaintenance = (String) redisTemplate.opsForHash().get("t_sys_param:deviceMaintenance", "content");
        if (!Boolean.parseBoolean(deviceMaintenance)) {
            for (Map<String, Object> item : items) {
                int deviceLevel = MapUtils.getIntValue(item, "device_level", 3);
                String deviceList = MapUtils.getString(item, "device_list");
                String instanceIds = sendToUpSystemServices.convertInstances(deviceLevel, deviceList);
                item.put("device_list", instanceIds);
            }
        }

        robotMap.put("list", list);
        Result re = Constant.otherServer(robotMap, Constant.MAINTENANCE_URL);
        log.info("--响应检修--" + re);
        clientHandler.normalResponse("200", header.getSessionId());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.MAINTENANCE, this);
    }
}
