/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
import com.yjh.platform.module.video.service.CameraConService;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.yjh.platform.module.patrol.CruiseConstant.TypeEnum.VIDEO;

/**
 * 普通可见光相机处理
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
@Primary
@Component("normalVideoCruiseExecute")
public class NormalVideoCruiseExecuteImpl extends AbstractVideoCruise implements CruiseInspectionExecute {

    public NormalVideoCruiseExecuteImpl(TAlgorithmInfoDao tAlgorithmInfoDao, RedisTemplate<String, Object> redisTemplate,
        CameraConService cameraConService, PatrolResultHandler patrolResultHandler) {
        super(tAlgorithmInfoDao, redisTemplate, cameraConService, patrolResultHandler);
    }

    @Override
    public boolean execute(Map<String, String> inspectionMap) {
        return videoExecute(inspectionMap);
    }

    /**
     * 可见光相机抓图
     */
    @Override
    protected Map<String, String> capture(String parentPath, Long cameraId, String meteName) {
        return cameraConService.capturePicture(parentPath, null, cameraId, meteName);
    }

    /**
     * 可见光相机不用附加额外参数
     */
    @Override
    protected void analysisExt(Analysis analysis, Map<String, String> ret) {
        // noting to do.
    }

    @Override
    protected Map<String, String> resultRecognition(Map<String, String> inspectionMap) {
        String resultValue = inspectionMap.get("resultNum");
        if ("已拍照".equals(resultValue)) {
            inspectionMap.put("resultDesc", resultValue);
            inspectionMap.put("resultNum", "1");
        }
        return inspectionMap;
    }

    @Override
    public void afterPropertiesSet() {
        CruiseExecuteFactory.CREATE.registerExecute(VIDEO, this);
    }
}
