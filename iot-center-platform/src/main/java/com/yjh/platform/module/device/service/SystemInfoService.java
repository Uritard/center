package com.yjh.platform.module.device.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.SystemInfoUtil;
import com.yjh.platform.module.device.controller.SystemInfoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



/**
 * @author lqh
 * @since 2020/10/20
 */
@Service
public class SystemInfoService {

    SystemInfoUtil systemInfoUtil = new SystemInfoUtil();

    private static final String SERVICE_URL = "http://iot-center-manager/managerService/v1/select";

    private Logger log = LoggerFactory.getLogger(SystemInfoService.class);

    @Logs(title = "获取内存信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> getMemory() throws Exception {
        return systemInfoUtil.getMemUsage();
    }
    @Logs(title = "获取cpu信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> getCPU() throws Exception {
        List<Map<String,String>> result = systemInfoUtil.getCpuUsage();
        List<Map<String,String>> result2 = new LinkedList<>();
        for (Map<String,String> item: result) {
            if (0 != Double.parseDouble(item.get("cpu"))){
                if("top".equals(item.get("command"))){
                    continue;
                }
                result2.add(item);
            }
        }
        return result2;
    }
    @Logs(title = "获取磁盘信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> getSwap() throws Exception {
        List<Map<String,String>> result = systemInfoUtil.getDeskUsage();
        List<Map<String,String>> result2 = new LinkedList<>();
        for (Map<String,String> item: result) {
            if (0 != Double.parseDouble(item.get("all"))){
                result2.add(item);
            }
        }
        return result2;
    }

    @Logs(title = "获取cpu利用率", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public double getCpuOnUse() throws Exception {
        return systemInfoUtil.getCpuOnUse();
    }

    @Logs(title = "获取硬盘利用率", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> getDeskOnUse() throws Exception {
        return systemInfoUtil.getDeskOnUse();
    }


    @Logs(title = "获取关键服务", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> getServices() {
        Result re = service();
        return (List<Map<String,Object>>)re.getData();

    }


    private  Result service() {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.getForObject(SERVICE_URL,Result.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }
}
