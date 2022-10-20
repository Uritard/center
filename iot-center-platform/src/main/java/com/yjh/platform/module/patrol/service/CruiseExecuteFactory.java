/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
public enum CruiseExecuteFactory {
    /**
     *
     */
    CREATE;

    CruiseInspectionExecute createExecute(int cruiseType) {
        CruiseInspectionExecute execute;
        switch (cruiseType) {
            case 229: // 视频
            case 230: // 红外

                break;
            case 232: // 声纹

                break;
            case 231: // 在线监控
            default:
                execute = (Map<String, String> inspectionMap) -> CruiseInspectionExecute.log.warn("default execute，nothing done，please confirm the data: {}",
                    JSON.toJSONString(inspectionMap));
        }
        return execute;
    }
}
