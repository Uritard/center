package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
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
public class LinkageTaskSendHandler implements MessageHandlerStrategy<XMLBaseModel>, InitializingBean {

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
            log.info("--响应联动任务--");
            if ("1".equals(xmlBaseModel.getCommand())) {
                //联动任务
                List<Map<String, Object>> list = xmlBaseModel.getItems();
                List<TCruiseTaskAdd> taskList = new ArrayList<>();
                // 解析报文 根据device_level转化
                list.forEach(item -> {
                    TCruiseTaskAdd tCruiseTaskAdd = sendToUpSystemServices.buildTaskInfo(item, item.get("device_level").toString(), true);
                    String taskId = item.get("task_code").toString();
                    boolean isRobotFlag = false;
                    boolean isEdgeFlag = false;
                    String robotDevice = "";
                    String edgeDevice = "";
                    List<String> insList = Arrays.asList(tCruiseTaskAdd.getDeviceList().split(","));
                    robotDevice = sendToUpSystemServices.selectIsRobotDevice(insList);
                    isRobotFlag = StringUtils.isNotEmpty(robotDevice);

                    edgeDevice = sendToUpSystemServices.selectIsDownSystem(insList);
                    isEdgeFlag = StringUtils.isNotEmpty(edgeDevice);

                    tCruiseTaskAdd.setUnionTaskStatus("1");
                    tCruiseTaskAdd.setAreaId(xmlBaseModel.getSendCode());
                    log.info("102联动任务组装好发送platform:{}", tCruiseTaskAdd);
                    taskList.add(tCruiseTaskAdd);
                    Map<String, List<TCruiseTaskAdd>> map = new HashMap<>();
                    map.put("list", taskList);
                    Result re = Constant.otherServer(map, Constant.TASK_ISSUE_URL);
                    //非机器人的和下级系统的直接返回 机器人点的等待机器人返回结果 下级的等下级返回
                    log.info("platform响应结果:{}", re);
                    Constant.getParamMap.put("sendSessionId", header.getSessionId());
                    if (!isRobotFlag && !isEdgeFlag) {
                        List<Map<String, Object>> xmlItems = new ArrayList<>();
                        Map<String, Object> xmlItem = new HashMap<>();
                        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
                        if (re == null) {
                            xmlItem.put("error_code", "3");
                            xmlItem.put("task_patrolled_id", Constant.stationCode() + "_" + taskId + "_" + simpleDateFormat2.format(new Date()));
                            xmlItems.add(xmlItem);
                        } else if (200 == re.getCode()) {
                            String taskPatrolledId = String.valueOf(((LinkedHashMap<?, ?>) re.getData()).get("task_patrolled_id"));
                            xmlItem.put("task_patrolled_id", taskPatrolledId);
                            xmlItem.put("error_code", "0");
                            xmlItems.add(xmlItem);
                        } else {
                            xmlItem.put("error_code", "1");
                            xmlItem.put("task_patrolled_id", Constant.stationCode() + "_" + taskId + "_" + simpleDateFormat2.format(new Date()));
                            xmlItems.add(xmlItem);
                        }
                        XMLBaseModel resModel = new XMLBaseModel().setType("251").setCommand("4").setCode("200").setItems(xmlItems);
                        clientHandler.send(resModel, header.getSessionId(), false);
                    }
                    log.info("--联动任务响应--" + re);
                });
            }
        } catch (Exception e) {
            log.error("任务执行失败", e);
            clientHandler.normalResponse("400", header.getSessionId());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(ProtocolEnum.IOT, IotHandlerEnum.LINKAGE_TASK_SEND, this);
    }

}
