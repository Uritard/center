/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.manager.common.handler;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/11/18
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class InstanceUpHandler {
    private final SpringClientFactory springClientFactory;

    @EventListener
    public void handleResultEvent(EurekaInstanceRegisteredEvent event) {
        try {
            InstanceInfo info = event.getInstanceInfo();
            //
            Applications apps = DiscoveryManager.getInstance().getEurekaClient().getApplications();
            Application app = apps.getRegisteredApplications(info.getAppName());
            if (app != null) {
                app.addInstance(info);
            } else {
                app = new Application(info.getAppName());
                app.addInstance(info);
                apps.addApplication(app);
            }
            apps.shuffleInstances(false);

            log.info(">>>>>>>>>>>>>>>>>>> 服务注册成功: {}", event);
            ILoadBalancer ribbonLoadBalancer = springClientFactory.getLoadBalancer(info.getAppName());

            if (ribbonLoadBalancer instanceof DynamicServerListLoadBalancer) {
                DynamicServerListLoadBalancer listLoadBalancer = (DynamicServerListLoadBalancer)ribbonLoadBalancer;
                listLoadBalancer.updateListOfServers();
            }

        } catch (Exception e) {
            log.error("处理注册信息出错：{},错误：", event, e);
        }
    }
}
