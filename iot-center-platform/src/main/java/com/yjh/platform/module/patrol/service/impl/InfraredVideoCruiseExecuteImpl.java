/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
import com.yjh.platform.module.user.entity.TAlgorithmMeteInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.yjh.platform.module.patrol.CruiseConstant.TypeEnum.INFRARED;

/**
 * 红外相机处理
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
@Component("normalVideoCruiseExecute")
public class InfraredVideoCruiseExecuteImpl extends AbstractVideoCruise implements CruiseInspectionExecute {

    public InfraredVideoCruiseExecuteImpl(TAlgorithmInfoDao tAlgorithmInfoDao, RedisTemplate<String, Object> redisTemplate,
        ServiceRestTemplate serviceRestTemplate, PatrolResultHandler patrolResultHandler) {
        super(tAlgorithmInfoDao, redisTemplate, serviceRestTemplate, patrolResultHandler);
    }

    @Override
    public boolean execute(Map<String, String> inspectionMap) {
        return videoExecute(inspectionMap);
    }

    /**
     * 红外相机转到预置位，红外相机抓图时进行了处理，不需要转动预置位
     *
     * @param map 预置位信息
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
            log.info("红外抓图： {}", RED_MOVE_URL);
            if (null != serviceRestTemplate) {
                re = serviceRestTemplate.getForObject(RED_MOVE_URL, Result.class, map);
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

    @Override
    protected Map<String, String> resultRecognition(Map<String, String> inspectionMap) {
        String resultValue = inspectionMap.get("resultNum");
        return patrolResultHandler.normalRecognitionHandler(resultValue, inspectionMap, null);
    }

    @Override
    public TAlgorithmMeteInfo needAnalysis(String deviceMeteId, String resultNum) {
        if ("已拍照".equals(resultNum)) {
            return super.needAnalysis(deviceMeteId, resultNum);
        }
        // dlt 红外 抓图能直接获取到数值，不需要进行算法处理，直接返回 null
        return null;
    }

    @Override
    public void afterPropertiesSet() {
        CruiseExecuteFactory.CREATE.registerExecute(INFRARED, this);
    }
}
