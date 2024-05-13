package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.entity.DeviceModel;
import com.yjh.accesstcp.module.device.entity.TCruiseTaskAdd;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
public class TaskSendHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

    private final SendToUpSystemServices sendToUpSystemServices;

    @Override
    public void handler(TCPClientHandler clientHandler, XMLBaseModel xmlBaseModel, MessageHeader header) {
        List<Map<String, Object>> itemList = xmlBaseModel.getItems();
        if (CollectionUtils.isEmpty(itemList)) {
            log.error("下发任务不正确，无法执行");
            clientHandler.normalResponse("400", header.getSessionId());
            return;
        }
        try {
            log.info("--响应任务--");
            if ("1".equals(xmlBaseModel.getCommand())) {
                log.info("--响应任务 任务下发--");
                List<Map<String, Object>> list = xmlBaseModel.getItems();
                List<TCruiseTaskAdd> taskList = new ArrayList<>();
                // 解析报文 根据device_level转化
                list.forEach(item -> {
                    TCruiseTaskAdd tCruiseTaskAdd = sendToUpSystemServices.buildTaskInfo(item, item.get("device_level").toString(), false);
                    tCruiseTaskAdd.setAreaId(xmlBaseModel.getSendCode());
                    log.info("101任务组装好发送platform:{}", tCruiseTaskAdd);
                    taskList.add(tCruiseTaskAdd);
                    Map<String, List<TCruiseTaskAdd>> map = new HashMap<>();
                    map.put("list", taskList);
                    Result re;
                    log.info("是否删除任务 isenable:{}", "0".equals(tCruiseTaskAdd.getIsenable()));
                    if ("0".equals(tCruiseTaskAdd.getIsenable())) {
                        re = Constant.otherServer(map, Constant.TASK_ISSUE_URL);
                    } else {
                        Map<String, Object> param = new HashMap<>(2);
                        param.put("taskId", tCruiseTaskAdd.getTaskId());
                        param.put("startTime", getInvalidTime(tCruiseTaskAdd.getInvalidStartTime(), tCruiseTaskAdd.getInvalidEndTime()));
                        param.put("source", "");
                        re = Constant.otherServerPost(param, Constant.TASK_DELETE_URL);
                    }
                    clientHandler.normalResponse("200", header.getSessionId());
                    log.info("--响应任务下发--" + re);
                });
                //存储A接口任务信息
                sendToUpSystemServices.saveAInterfaceTaskInfo(list);

            }
        } catch (Exception e) {
            log.error("任务执行失败", e);
            clientHandler.normalResponse("400", header.getSessionId());
        }
    }

    /**
     * 不可用时间计算
     *
     * @param invalidStartTime
     * @param invalidEndTime
     * @return
     */
    public static String getInvalidTime(String invalidStartTime, String invalidEndTime) {
        if (StringUtils.isNotBlank(invalidStartTime) && StringUtils.isNotBlank(invalidEndTime)) {
            if (invalidStartTime.equals(invalidEndTime)) {
                return invalidStartTime;
            } else {
                return "-1";
            }
        } else {
            return "-1";
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.TASK_SEND, this);
    }
}
