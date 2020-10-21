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
    public Map<String,String> getMemory() throws Exception {
        return systemInfoUtil.getMemUsage();
    }
    @Logs(title = "获取cpu信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> getCPU() throws Exception {
        List<Map<String,String>> result = systemInfoUtil.getCpuUsage();
        for (Map<String,String> item: result) {
            if (0L == Long.parseLong(item.get("cpu"))){
                    result.remove(item);
            }
        }
        return result;
    }
    @Logs(title = "获取磁盘信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> getSwap() throws Exception {
        List<Map<String,String>> result = systemInfoUtil.getDeskUsage();
        for (Map<String,String> item: result) {
            if (0L == Long.parseLong(item.get("all"))){
                result.remove(item);
            }
        }
        return result;
    }
}
