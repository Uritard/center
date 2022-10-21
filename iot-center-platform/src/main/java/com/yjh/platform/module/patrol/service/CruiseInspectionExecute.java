/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
public interface CruiseInspectionExecute extends InitializingBean {
    Logger log = LoggerFactory.getLogger(CruiseInspectionExecute.class);

    /**
     * 空的执行方式，不执行任何操作
     */
    NullableCruiseExecuteImpl NULLABLE_EXECUTE = new NullableCruiseExecuteImpl();

    /**
     * 测点执行
     * @param inspectionMap 测点数据
     */
    void execute(Map<String, String> inspectionMap);

    /**
     * 向页面 Websocket 发送消息
     * @param taskId 任务ID
     */
    default void sendWebsocket(String taskId) {
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

    /**
     * 向上级系统同步消息
     * @param inspectionMap 测点数据
     */
    default void sendTaskUpSyatem(Map<String, String> inspectionMap) {
        //巡视点结果上报站端
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> xmlItem = new HashMap<>();
        xmlBaseModel.setType("61");
        xmlItem.put("patroldevice_code", inspectionMap.getOrDefault("instanceId", ""));
        xmlItem.put("patroldevice_name", inspectionMap.getOrDefault("instanceName", ""));
        xmlItem.put("task_name", inspectionMap.getOrDefault("taskName", ""));
        xmlItem.put("task_code", inspectionMap.getOrDefault("taskCode", ""));
        xmlItem.put("device_name", inspectionMap.getOrDefault("cruiseName", ""));
        String deviceMeteId = inspectionMap.getOrDefault("deviceMeteId", "");
        xmlItem.put("device_id", deviceMeteId);
        xmlItem.put("material_id", inspectionMap.getOrDefault("realCode", ""));
        xmlItem.put("value", inspectionMap.getOrDefault("resultNum", ""));
        xmlItem.put("value_unit", inspectionMap.getOrDefault("resultNum", ""));
        xmlItem.put("unit", "");
        xmlItem.put("time", inspectionMap.getOrDefault("cruiseTime", ""));
        // todo
        xmlItem.put("recognition_type", "");
        xmlItem.put("file_type", "2");

        // 根据相机id获取相机pms编码 （仿照机器人编码）
        // redist
        // String cameraPmS = tCameraPresetDao.selectPMSByCameraId(tCameraPreset.getCameraId());
        // // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR/设备点位ID_编码_时间.jpg
        // String timeFormat = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // String tagPath =  "task/" + stationCode + "/" + timeFormat.substring(0,4) + "/" + timeFormat.substring(4,6) + "/" + timeFormat.substring(6,8)
        //     + "/" + taskId + "/CCD/" + deviceMeteId + "_" + cameraPmS + "_" + timeFormat + ".jpg";
        // log.info("imgPath==={},tagPath==={}", absPath, tagPath);
        // uploadFileToUpFtps(absPath, "/" + tagPath);

        // xmlItem.put("file_path", tagPath);
        xmlItem.put("rectangle", "");
        String startTime =
            StringUtils.replaceAll(inspectionMap.getOrDefault("startTime", DateFormatUtils.format(new Date(), "yyyyMMddhhmmss")), "- :",
                "");
        xmlItem.put("task_patrolled_id", inspectionMap.get("taskId") + "_" + startTime);
        xmlItem.put("data_type", "0x01");
        xmlItem.put("valid", "0");

        xmlItems.add(xmlItem);
        xmlBaseModel.setItems(xmlItems);
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        Map<String, List<XMLBaseModel>> cruiseResult = new HashMap<>();
        cruiseResult.put("list", list);
        log.info("信息上报：-" + cruiseResult);
        try {
            Constant.otherServer(cruiseResult, Constant.TCP_URL);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    class NullableCruiseExecuteImpl implements CruiseInspectionExecute {

        @Override
        public void execute(Map<String, String> inspectionMap) {
            log.warn("default execute，nothing done，please confirm the data: {}", JSON.toJSONString(inspectionMap));
        }

        @Override
        public void afterPropertiesSet() {
            // nothing to do
        }

    }
}
