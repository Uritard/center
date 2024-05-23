/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.yjh.commons.ValueUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.feign.UdpProxy;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yjh.platform.module.patrol.CruiseConstant.TypeEnum.ONLINE;
import static com.yjh.platform.module.patrol.CruiseConstant.TypeEnum.VIDEO;

/**
 * 在线监控处理
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
@Primary
@Component
public class OnLineMonitoringExecuteImpl implements CruiseInspectionExecute {

    private final UdpProxy udpProxy;

    private final ApplicationProperties applicationProperties;

    public OnLineMonitoringExecuteImpl(UdpProxy udpProxy, ApplicationProperties applicationProperties) {
        this.udpProxy = udpProxy;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public boolean execute(Map<String, String> inspectionMap) {
        log.info("no thing to do !");
        return true;
    }

    @Override
    public boolean execute(List<Map<String, String>> inspectionList) {
        List<Long> meteIds = new ArrayList<>();
        for (Map<String,String> item : inspectionList){
            meteIds.add(ValueUtil.toLong(item.get("cruiseId")));
        }
        Map<String,Object> param = new HashMap<>(2);

        param.put("meteIds",meteIds);
        param.put("filePath",applicationProperties.getSequentialConfig().getDataCallPath()+applicationProperties.getSequentialConfig().getDataCallName());
        udpProxy.dataCall(param);
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CruiseExecuteFactory.CREATE.registerExecute(ONLINE, this);
    }
}
