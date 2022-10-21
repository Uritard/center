/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
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
@Component
public class NormalVideoCruiseExecuteImpl extends AbstractVideoCruise implements CruiseInspectionExecute {

    ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);

    public NormalVideoCruiseExecuteImpl(TAlgorithmInfoDao tAlgorithmInfoDao, RedisTemplate<String, Object> redisTemplate) {
        super(tAlgorithmInfoDao, redisTemplate);
    }

    @Override
    public void execute(Map<String, String> inspectionMap) {
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
    public void afterPropertiesSet() {
        CruiseExecuteFactory.CREATE.registerExecute(VIDEO, this);
    }
}
