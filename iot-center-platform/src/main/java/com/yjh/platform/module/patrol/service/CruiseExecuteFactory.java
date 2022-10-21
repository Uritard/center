/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
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

    private static Map<CruiseConstant.TypeEnum, CruiseInspectionExecute> cruiseInspectionExecuteMap = new HashMap<>(16);

    public CruiseInspectionExecute createExecute(CruiseConstant.TypeEnum cruiseTypeEnum) {

        return cruiseInspectionExecuteMap.getOrDefault(cruiseTypeEnum, CruiseInspectionExecute.NULLABLE_EXECUTE);
    }

    public void registerExecute(CruiseConstant.TypeEnum type, CruiseInspectionExecute execute) {
        cruiseInspectionExecuteMap.put(type, execute);
    }
}
