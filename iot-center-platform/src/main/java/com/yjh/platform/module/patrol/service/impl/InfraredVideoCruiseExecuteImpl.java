/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.ResultConvertUtil;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
import com.yjh.platform.module.user.entity.TAlgorithmMeteInfo;
import com.yjh.platform.module.video.service.CameraConService;
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
@Component("infraredVideoCruiseExecute")
public class InfraredVideoCruiseExecuteImpl extends AbstractVideoCruise implements CruiseInspectionExecute {

    public InfraredVideoCruiseExecuteImpl(TAlgorithmInfoDao tAlgorithmInfoDao, RedisTemplate<String, Object> redisTemplate,
        CameraConService cameraConService, PatrolResultHandler patrolResultHandler) {
        super(tAlgorithmInfoDao, redisTemplate, cameraConService, patrolResultHandler);
    }

    @Override
    public boolean execute(Map<String, String> inspectionMap) {
        return videoExecute(inspectionMap);
    }

    /**
     * 红外抓图，转到预置位抓图两步合一
     */
    @Override
    protected Map<String, String> capture(String parentPath, Long cameraId, String meteName) {
        return cameraConService.givePicFir(parentPath, cameraId, meteName);
    }

    @Override
    protected void analysisExt(Analysis analysis, Map<String, String> ret) {
        /*String picPath = captureResult.getString("picPath");
        String csvPath = captureResult.getString("csvPath");
        String dataPath = captureResult.getString("dataPath");*/

        String picPath = ret.get("absPath");

        analysis.setPicPath(picPath);
        /*analysis.setCsvPath(csvPath);
        analysis.setDataPath(dataPath);*/
        log.info("算法信息(红外)： {}", JSON.toJSONString(analysis));
    }

    @Override
    protected Map<String, String> resultRecognition(Map<String, String> inspectionMap) {
        String resultValue = inspectionMap.get("resultNum");
        if ("已拍照".equals(resultValue)) {
            inspectionMap.put("resultDesc", resultValue);
            inspectionMap.put("resultNum", "-1");
        } else {
            String resultDesc = ResultConvertUtil.convertDesc(resultValue, inspectionMap.getOrDefault("unit", ""));
            inspectionMap.put("resultDesc", resultDesc);
        }
        return patrolResultHandler.normalRecognitionHandler(resultValue, inspectionMap, null);
    }

    @Override
    public TAlgorithmMeteInfo needAnalysis(String deviceMeteId, String resultNum) {
        if ("已拍照".equals(resultNum) || CommonUtils.isEmptyOrNullstr(resultNum)) {
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
