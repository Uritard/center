/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/20
 * @since [产品/模块版本] （可选）
 */
public interface AnalyticsService extends InitializingBean {
    Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    /**
     * 表计算法分析
     * @param analysis 分析参数
     * @return 返回结果
     */
    Result analytics(List<Analysis> analysis);

    /**
     * 缺陷和判别算法
     * @param analysis 分析参数
     * @return 返回结果
     */
    Result defect(List<Analysis> analysis);

}
