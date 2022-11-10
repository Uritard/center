/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.yjh.platform.module.patrol.CruiseConstant;

import java.util.HashMap;
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

    private static final Map<CruiseConstant.TypeEnum, CruiseInspectionExecute> CRUISE_INSPECTION_EXECUTE_MAP = new HashMap<>(16);

    public CruiseInspectionExecute createExecute(CruiseConstant.TypeEnum cruiseTypeEnum) {

        return CRUISE_INSPECTION_EXECUTE_MAP.getOrDefault(cruiseTypeEnum, CruiseInspectionExecute.NULLABLE_EXECUTE);
    }

    public void registerExecute(CruiseConstant.TypeEnum type, CruiseInspectionExecute execute) {
        CRUISE_INSPECTION_EXECUTE_MAP.put(type, execute);
    }
}
