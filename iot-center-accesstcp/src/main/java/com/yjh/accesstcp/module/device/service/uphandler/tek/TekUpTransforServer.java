/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler.tek;

import cn.hutool.core.date.DateUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.BusinessException;
import com.yjh.accesstcp.module.device.entity.AInterfaceTaskInfo;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.module.device.service.uphandler.DefaultEmptyHandlerImpl;
import com.yjh.accesstcp.module.device.service.uphandler.tek.entity.TaskPlan;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import com.yjh.accesstcp.netty.handler.iot.IotHandlerEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 对上级系统下发命令进行处理转换，转为国网规范消息
 * @author Chenfei
 * @date 2024/12/5
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TekUpTransforServer {
    private final DefaultEmptyHandlerImpl defaultEmptyHandler;
    private final SendToUpSystemServices sendToUpSystemServices;

    private static final Cache<String, String> TASK_IPLAN_CACHE = CacheBuilder.newBuilder().expireAfterWrite(7, TimeUnit.DAYS).build();

    /**
     * 将科大下发协议转换成电网协议
     * @param taskPlan 科大协议内容
     * @return 成功 1   失败 -1
     */
    public int sendTask(TaskPlan taskPlan) {
        MessageHeader header = new MessageHeader().setSessionId(1L);
        // 设置任务下发报文 101 command 1
        XMLBaseModel xmlBaseModel =
            new XMLBaseModel().setSendCode(Constant.server()).setType(IotHandlerEnum.TASK_SEND.getType()).setCommand("1");

        // 具体协议信息，点位内容
        List<Map<String, Object>> items = packageItem(taskPlan);
        xmlBaseModel.setItems(items);

        // 调用内部电网协议逻辑处理任务下发
        String type = xmlBaseModel.getType();
        MessageHandlerStrategy<XMLBaseModel> messageHandlerStrategy =
            (MessageHandlerStrategy<XMLBaseModel>)MessageHandlerStrategyFactory.getStrategyType(ProtocolEnum.IOT, type);
        if (Optional.ofNullable(messageHandlerStrategy).isPresent()) {
            messageHandlerStrategy.handler(defaultEmptyHandler, xmlBaseModel, header);
        } else {
            throw new BusinessException("协议处理错误");
        }
        return 1;
    }

    /**
     * 点位信息转换
     * @param taskPlan 科大协议内容
     * @return 电网点位内容
     */
    private List<Map<String, Object>> packageItem(TaskPlan taskPlan) {

        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(32);

        String deviceIdList = String.join(",", taskPlan.getInfoCodeList());

        try {
            map.put("task_code", taskPlan.getTaskNo());
            map.put("task_name", taskPlan.getTaskName());
            map.put("device_list", deviceIdList);
            log.info("This is a normal task！！！！！！！！！！！！！");
            map.put("plan_code", taskPlan.getPlanNo());
            // 任务类型 1 例行  2 特殊  3 专项巡视  4 自定义巡视
            map.put("type", "4");
            // 优先级
            map.put("priority", "2");
            // 设备层级   3 设备点位
            map.put("device_level", "3");
            // 定期和立即任务参数 1-定时下发 2-立即下发
            String taskTime = taskPlan.getTaskType() == 2 ? DateUtil.now() : taskPlan.getTaskTime();
            map.put("fixed_start_time", taskTime);
            // 周期任务参数
            map.put("cycle_month", "");
            map.put("cycle_week", "");
            map.put("cycle_execute_time", "");
            map.put("cycle_start_time", "");
            map.put("cycle_end_time", "");
            // 间隔任务参数
            map.put("interval_number", "");
            map.put("interval_type", "");
            map.put("interval_execute_time", "");
            map.put("interval_start_time", "");
            map.put("interval_end_time", "");

            map.put("invalid_start_time", "");
            map.put("invalid_end_time", "");
            map.put("isenable", "0");
            map.put("creator", taskPlan.getPlanNo());
            map.put("create_time", DateUtil.now());
            mapList.add(map);

            TASK_IPLAN_CACHE.put(taskPlan.getTaskNo(), taskPlan.getPlanNo());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return mapList;
    }

    public String getTaskPlanCode(String taskCode) {
        try {
            return TASK_IPLAN_CACHE.get(taskCode, () -> {
                try {
                    AInterfaceTaskInfo aInterfaceTaskInfo = sendToUpSystemServices.selectAInterfaceTask(taskCode);
                    return aInterfaceTaskInfo.getCreator();
                } catch (Exception e) {
                    log.error("根据task_code查询A接口任务出错：", e);
                    return "";
                }
            });
        } catch (ExecutionException e) {
            log.error("获取 planCode 失败", e);
        }
        return StringUtils.EMPTY;
    }

}
