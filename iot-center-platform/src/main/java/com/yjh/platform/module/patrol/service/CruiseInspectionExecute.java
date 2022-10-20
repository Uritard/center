/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.module.patrol.thread.LocalCruiseExecutThread;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
@Service
public interface CruiseInspectionExecute {
    Logger log = LoggerFactory.getLogger(CruiseInspectionExecute.class);

    /**
     * 巡视结果正常
     */
    int CRUISE_RESULT_NORMAL = 246;
    /**
     * 巡视结果异常
     */
    int CRUISE_RESULT_ABNORMAL = 247;

    /**
     * 审核状态，未审核
     */
    int EVALUATION_STATE_UN = 257;

    /**
     * 巡检数据状态，已执行
     */
    int CRUISE_STATE_DONE = 252;
    /**
     * 巡检数据状态，未执行
     */
    int CRUISE_STATE_UN = 253;
    /**
     * 巡检数据状态，执行失败
     */
    int CRUISE_STATE_FAILED = 254;

    /**
     * 异常原因，检修
     */
    int CRUISE_ABNORMAL_OVERHAUL = 410;
    /**
     * 异常原因，离线
     */
    int CRUISE_ABNORMAL_OFFLINE = 411;
    /**
     * 异常原因，抓图失败
     */
    int CRUISE_ABNORMAL_NOPIC = 248;

    /**
     * 巡视类型，视频（可见光）
     */
    int CRUISE_TYPE_VIDEO = 229;
    /**
     * 巡视类型，红外
     */
    int CRUISE_TYPE_INFRARED = 230;
    /**
     * 巡视类型，声纹
     */
    int CRUISE_TYPE_VOICE = 232;
    /**
     * 巡视类型，机器人
     */
    int CRUISE_TYPE_ROBOT = 228;
    /**
     * 巡视类型，无人机
     */
    int CRUISE_TYPE_UAV = 524;
    /**
     * 巡视类型，在线监控
     */
    int CRUISE_TYPE_ONLINE = 231;

    void execute(Map<String, String> inspectionMap, long waitTime);

    default void sendWebsocket(String taskId){
        try {
            Map<String, String> jasonMapOnFinished = new HashMap<>();
            jasonMapOnFinished.put("type", "finishedOneInstance");
            jasonMapOnFinished.put("taskId", taskId);
            String jsonMessage = JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息：" + jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapOnFinished);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    default void sendTaskUpSyatem(Map<String, String> inspectionMap){
        //巡视点结果上报站端
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> xmlItem = new HashMap<>();
        xmlBaseModel.setType("61");
        xmlItem.put("patroldevice_code", "");
        xmlItem.put("task_name", taskName);
        xmlItem.put("task_code", "");
        xmlItem.put("device_name", item.getCruiseName());
        xmlItem.put("device_id", item.getInstanceId());
        xmlItem.put("material_id", item.getRealCode());
        xmlItem.put("value", "");
        xmlItem.put("value_unit", "");
        xmlItem.put("unit", "");
        xmlItem.put("time", cruiseTime);
        //todo
        xmlItem.put("recognition_type", "");
        xmlItem.put("file_type", "2");
        xmlItem.put("file_path", urlPath);
        xmlItem.put("rectangle", "");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
        xmlItem.put("task_patrolled_id", taskId + "_" + simpleDateFormat2.format(tCruiseTask.getStartTime()));
        xmlItem.put("data_type", "0x01");
        xmlItem.put("valid", "0");

        xmlItems.add(xmlItem);
        xmlBaseModel.setItems(xmlItems);
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        Map<String, List<XMLBaseModel>> cruiseResult = new HashMap<>();
        cruiseResult.put("list", list);
        log.info("信息上报：-" + cruiseResult);
        Constant.otherServer(cruiseResult,Constant.TCP_URL);//江苏要求
    }
}
