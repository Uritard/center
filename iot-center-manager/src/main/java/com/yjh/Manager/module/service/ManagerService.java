package com.yjh.Manager.module.service;

import com.alibaba.fastjson.JSON;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author tt
 * @since 2020-08-17
 */
@Service
public class ManagerService {

    private Logger log = LoggerFactory.getLogger(ManagerService.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    @Transactional(rollbackFor = Exception.class)
    public List<Map> select() {
        List<Application> sortedApplications = EurekaServerContextHolder.getInstance().getServerContext().getRegistry().getSortedApplications();
        List<Map> list = new ArrayList<>();
        for (Application application : sortedApplications){
            String name = application.getName();
            List<InstanceInfo> instances = application.getInstances();
            for (InstanceInfo instanceInfo : instances){
                Map<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("status", instanceInfo.getStatus());
                map.put("ip", instanceInfo.getIPAddr());
                map.put("port", instanceInfo.getPort());
                map.put("instanceId", instanceInfo.getInstanceId());
                map.put("url", instanceInfo.getHomePageUrl());
                list.add(map);
            }
        }
        log.info("instanceInfoList: "+list);
        return list;
    }

    public Object getList(String targetName) {
        List<Map<String, Object>> servicesList = new ArrayList<>();
        //获取服务名称
        List<String> serviceNames = discoveryClient.getServices();
        for (String serviceName : serviceNames) {
            //获取服务中的实例列表
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceName);
            for (ServiceInstance serviceInstance : serviceInstances) {
                String serviceInstanceStr = JSON.toJSONString(serviceInstance);
                if (serviceInstanceStr != null) {
                    Map<String, Object> serviceInstanceMap = (Map<String, Object>) JSON.parse(serviceInstanceStr);
                    if (serviceInstanceMap != null) {
                        Map<String, Object> instanceInfoMap = (Map<String, Object>) JSON.parse(serviceInstanceMap.get("instanceInfo").toString());
                        String appName = (String) instanceInfoMap.get("appName");
                        if (targetName == null) {
                            servicesList.add(instanceInfoMap);
                            servicesList.add(serviceInstanceMap);
                        } else if (appName.contains(targetName.trim())) {
                            servicesList.add(instanceInfoMap);
                            servicesList.add(serviceInstanceMap);
                        }
                    }
                }
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", 0);
        map.put("count", servicesList.size());
        map.put("data", servicesList);
        return map;
    }

}

