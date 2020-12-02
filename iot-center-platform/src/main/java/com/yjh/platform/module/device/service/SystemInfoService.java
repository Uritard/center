package com.yjh.platform.module.device.service;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.common.utils.SystemInfoUtil;
import com.yjh.platform.configuration.PrivilegeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Value("${spring.Key.Services.path}")
    private  String SERVICE_URL;

    private Logger log = LoggerFactory.getLogger(SystemInfoService.class);

    @Logs(title = "获取内存信息", code = "module")
    @PrivilegeInfo(roleIds = "1234,1235")
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
    @PrivilegeInfo(roleIds = "1234,1235")
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
    @PrivilegeInfo(roleIds = "1234,1235")
    @Transactional(rollbackFor = Exception.class)
    public double getCpuOnUse() throws Exception {
        return systemInfoUtil.getCpuOnUse();
    }

    @Logs(title = "获取硬盘利用率", code = "module")
    @PrivilegeInfo(roleIds = "1234,1235")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> getDeskOnUse() throws Exception {
        return systemInfoUtil.getDeskOnUse();
    }


    @Logs(title = "获取关键服务", code = "module")
    @PrivilegeInfo(roleIds = "1234,1235")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> getServices() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long currentTime =System.currentTimeMillis();
        //Result re = service();
        String services = HttpClientUtils.getInstance().getUrl(SERVICE_URL, null);
        JSONObject jsonObject =JSONObject.parseObject(services);
        List<Map<String,Object>> re= (List<Map<String,Object>>) jsonObject.get("data");
        Date now = new Date();
        for(Map<String,Object> item :re){
           // long createTime = simpleDateFormat.parse((Date)item.get("registerTime"));
            log.info("registerTime:  "+item.get("registerTime").toString());
            String used = item.get("registerTime").toString();
            log.info("used:  "+used);
            long diff = now.getTime()-simpleDateFormat.parse(used).getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
            long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
            String usedTime= "";
            if(days != 0){
                usedTime =usedTime+days+"天";
            }
            if(hours != 0){
                usedTime =usedTime+hours+"小时";
            }
            if(minutes != 0){
                usedTime =usedTime+minutes+"分";
            }
            item.put("usedTime",usedTime);
        }
        log.info("resg     "+re);
        return re;

    }

}
