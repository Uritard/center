package com.yjh.platform.module.device.service;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.common.utils.SystemInfoUtil;
import com.yjh.platform.module.user.dao.TCameraRecorderDao;
import com.yjh.platform.module.user.entity.TCameraRecorderDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author lqh
 * @since 2020/10/20
 */
@Service
public class SystemInfoService {

    SystemInfoUtil systemInfoUtil = new SystemInfoUtil();

    @Value("${spring.Key.Services.path}")
    private  String SERVICE_URL;

    @Autowired
    private TCameraRecorderDao tCameraRecorderDao;
    @Resource
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(SystemInfoService.class);

    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> getMemory() throws Exception {
        Map<String,String> menUsage=systemInfoUtil.getMemUsage();
        Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:MemoryFreeMin");
        Double cpuFreeMin=Double.parseDouble(map.get("content"));
        if(Double.parseDouble(menUsage.get("free"))/Double.parseDouble(menUsage.get("total"))*100<cpuFreeMin){
            menUsage.put("code","01");
        }else{
            menUsage.put("code","02");
        }
        return menUsage;
    }
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

    @Transactional(rollbackFor = Exception.class)
    public  Map<String,Object> getCpuOnUse() throws Exception {
        Map<String,Object> cpuOnUsemap=new HashMap<>();
        Double cpuOnUse=systemInfoUtil.getCpuOnUse();
        cpuOnUsemap.put("use",cpuOnUse);
        Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:cpuFreeMin");
        Double cpuFreeMin=Double.parseDouble(map.get("content"));
        if((100-cpuOnUse)<cpuFreeMin){
            cpuOnUsemap.put("code","01");
        }else{
            cpuOnUsemap.put("code","02");
        }
        return cpuOnUsemap;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> getDeskOnUse() throws Exception {
        Map<String,Object> deskOnUse=systemInfoUtil.getDeskOnUse();
        Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:DiskFreeMin");
        Double diskFreeMin=Double.parseDouble(map.get("content"));
        if(Double.parseDouble(deskOnUse.get("use").toString())/Double.parseDouble(deskOnUse.get("all").toString())*100>(100-diskFreeMin)){
            deskOnUse.put("code","01");
        }else{
            deskOnUse.put("code","02");
        }
        return deskOnUse;
    }


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
            //log.info("registerTime:  "+item.get("registerTime").toString());
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

    //获取NVR容量
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> getNVRInfo() {
        List<TCameraRecorderDetail> list = tCameraRecorderDao.selectIdAndName();
        List<Map<String,String>> reList = new ArrayList<>();
        for (TCameraRecorderDetail item:list) {
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",item.getRecordId() );
            Result re = getNVRInfo(recordIdMap);

            Map<String,String> map = (Map<String,String>)re.getData();
            if(map.get("errorMessage: ") != null){
                continue;
            }
            Integer use = Integer.valueOf(map.get("freeTotal"));
            Integer all = Integer.valueOf(map.get("capacityTotal"));
            Integer other = all - use;
            map.put("use",other.toString());
            map.put("recoderName",item.getRecordName());
            reList.add(map);
        }
        return reList;
    }
    private static Result getNVRInfo(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(Constant.NVR_URL, Result.class,map);
            }
        } catch (Exception e) {

        }
        return re;
    }


}
