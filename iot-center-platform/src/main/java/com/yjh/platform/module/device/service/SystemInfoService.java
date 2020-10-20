package com.yjh.platform.module.device.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.SystemInfoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;



/**
 * @author lqh
 * @since 2020/10/20
 */
@Service
public class SystemInfoService {

    SystemInfoUtil systemInfoUtil = new SystemInfoUtil();

    @Logs(title = "获取内存信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> getMemory() throws Exception {
        return systemInfoUtil.getMemUsage();
    }
    @Logs(title = "获取cpu信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> getCPU() throws Exception {
        return systemInfoUtil.getcpuUsage();
    }
    @Logs(title = "获取磁盘信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> getSwap() throws Exception {
        return systemInfoUtil.getDeskUsage();
    }
}
