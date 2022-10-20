/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;

import java.util.Map;

/**
 * 红外相机处理
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
public class InfraredVideoCruiseExecuteImpl extends AbstractVideoCruise implements CruiseInspectionExecute {

    ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);

    @Override
    public void execute(Map<String, String> inspectionMap, long waitTime) {
        videoExecute(inspectionMap);
    }

    @Override
    public void sendWebsocket(String taskId) {
        CruiseInspectionExecute.super.sendWebsocket(taskId);
    }

    @Override
    public void sendTaskUpSyatem(Map<String, String> inspectionMap) {
        CruiseInspectionExecute.super.sendTaskUpSyatem(inspectionMap);
    }

    /**
     * 红外相机转到预置位，红外相机抓图时进行了处理，不需要转动预置位
     * @param map
     */
    @Override
    protected void moveWait(Map<String, Object> map) {
        // noting to do.
    }

    /**
     * 红外抓图，转到预置位抓图两步合一
     */
    @Override
    protected Result capture(Map<String, Object> map) {
        Result re = null;
        try {
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(RED_MOVE_URL, Result.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

    @Override
    protected void analysisExt(Analysis analysis, JSONObject captureResult) {
        String picPath = captureResult.getString("picPath");
        String csvPath = captureResult.getString("csvPath");
        String dataPath = captureResult.getString("dataPath");

        analysis.setPicPath(picPath);
        analysis.setCsvPath(csvPath);
        analysis.setDataPath(dataPath);
        log.info("算法信息(红外)： {}", JSON.toJSONString(analysis));
    }
}
