/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
public interface CruiseInspectionExecute extends InitializingBean {
    Logger log = LoggerFactory.getLogger(CruiseInspectionExecute.class);

    /**
     * 空的执行方式，不执行任何操作
     */
    NullableCruiseExecuteImpl NULLABLE_EXECUTE = new NullableCruiseExecuteImpl();

    /**
     * 测点执行
     *
     * @param inspectionMap 测点数据
     * @return 测点是否已经执行结束，true 表示不需要算法返回，false 表示需要算法返回
     */
    boolean execute(Map<String, String> inspectionMap);

    class NullableCruiseExecuteImpl implements CruiseInspectionExecute {

        @Override
        public boolean execute(Map<String, String> inspectionMap) {
            log.warn("default execute，nothing done，please confirm the data: {}", JSON.toJSONString(inspectionMap));
            return true;
        }

        @Override
        public void afterPropertiesSet() {
            // nothing to do
        }

    }
}
