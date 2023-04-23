/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
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
        ServiceRestTemplate serviceRestTemplate, PatrolResultHandler patrolResultHandler) {
        super(tAlgorithmInfoDao, redisTemplate, serviceRestTemplate, patrolResultHandler);
    }

    @Override
    public boolean execute(Map<String, String> inspectionMap) {
        return videoExecute(inspectionMap);
    }

    /**
     * 相机转到预置位，普通相机需要等待几秒，让相机到达预置位
     */
    @Override
    protected void moveWait(Map<String, Object> map) {
        try {
            if (null != serviceRestTemplate) {
                serviceRestTemplate.getForObject(MOVE_URL, Result.class, map);
                Thread.sleep(waitTime);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 可见光相机抓图
     */
    @Override
    protected Result capture(Map<String, Object> map) {
        Result re = null;
        try {
            if (null != serviceRestTemplate) {
                re = serviceRestTemplate.getForObject(PICTURE_URL, Result.class, map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

    /**
     * 可见光相机不用附加额外参数
     */
    @Override
    protected void analysisExt(Analysis analysis, JSONObject captureResult) {
        // noting to do.
    }

    @Override
    protected Map<String, String> resultRecognition(Map<String, String> inspectionMap) {
        String resultValue = inspectionMap.get("resultNum");
        if ("已拍照".equals(resultValue)) {
            inspectionMap.put("resultDesc", resultValue);
        }
        return inspectionMap;
    }

    @Override
    public void afterPropertiesSet() {
        CruiseExecuteFactory.CREATE.registerExecute(VIDEO, this);
    }
}
