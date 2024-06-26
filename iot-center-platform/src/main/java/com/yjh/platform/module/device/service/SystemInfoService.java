package com.yjh.platform.module.device.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.common.utils.SystemInfoUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.user.dao.TCameraRecorderDao;
import com.yjh.platform.module.user.dao.TSysParamDao;
import com.yjh.platform.module.user.entity.TCameraRecorderDetail;
import com.yjh.platform.module.video.service.CameraConService;
import com.yjh.video.api.CameraVendor;
import com.yjh.video.api.entity.RecordEntity;
import com.yjh.video.api.entity.response.DeviceStatusResp;
import com.yjh.video.api.result.Result;
import com.yjh.video.api.service.IRecordService;
import com.yjh.video.api.service.VideoServiceFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    @Autowired
    private TSysParamDao sysParamDao;
    @Autowired
    private CameraConService cameraConService;
    @Autowired
    private TStdRegionDao tStdRegionDao;

    private final static Cache<String, List<Map<String, Object>>> RECORDER_INFO_CACHE = CacheUtil.newTimedCache(5*60*1000);
    private final static String RECORDER_KEY = "RECORDER_INFO";

    private Logger log = LoggerFactory.getLogger(SystemInfoService.class);

    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> getMemory(HttpServletRequest request,Long userId) throws Exception {
        Map<String,String> menUsage=systemInfoUtil.getMemUsage();
        Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:MemoryFreeMin");
        Double cpuFreeMin=Double.parseDouble(map.get("content"));
        if(Double.parseDouble(menUsage.get("free"))/Double.parseDouble(menUsage.get("total"))*100<cpuFreeMin){
            String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userId,"userName"));
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.set("logType", "5");
            param.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            param.set("title", "内存最小空闲报警");
            param.set("state", 1);
            param.set("userId", userId);
            param.set("userName", userName);
            param.set("requestOrigin", request.getRequestURL());
            param.set("requestPath", request.getRequestURI());
            param.set("requestMethod", request.getMethod());
            param.set("content", "内存最小空闲报警");
            LogsAspect logsAspects = new LogsAspect();
            logsAspects.post(param);
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
    public  Map<String,Object> getCpuOnUse(HttpServletRequest request,Long userId) throws Exception {
        Map<String,Object> cpuOnUsemap=new HashMap<>();
        Double cpuOnUse=systemInfoUtil.getCpuOnUse();
        cpuOnUsemap.put("use",cpuOnUse);
        Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:cpuFreeMin");
        Double cpuFreeMin=Double.parseDouble(map.get("content"));
        if((100-cpuOnUse)<cpuFreeMin){
            String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userId,"userName"));
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.set("logType", "5");
            param.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            param.set("title", "cpu最小空闲报警");
            param.set("state", 1);
            param.set("userId", userId);
            param.set("userName", userName);
            param.set("requestOrigin", request.getRequestURL());
            param.set("requestPath", request.getRequestURI());
            param.set("requestMethod", request.getMethod());
            param.set("content", "cpu最小空闲报警");
            LogsAspect logsAspects = new LogsAspect();
            logsAspects.post(param);
            cpuOnUsemap.put("code","01");
        }else{
            cpuOnUsemap.put("code","02");
        }
        return cpuOnUsemap;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object>  getDeskOnUse(HttpServletRequest request,Long userId) throws Exception {
        Map<String,Object> deskOnUse=systemInfoUtil.getDeskOnUse();
        Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:DiskFreeMin");
        Double diskFreeMin=Double.parseDouble(map.get("content"));
        if(Double.parseDouble(deskOnUse.get("use").toString())/Double.parseDouble(deskOnUse.get("all").toString())*100>(100-diskFreeMin)){
            String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userId,"userName"));
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.set("logType", "5");
            param.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            param.set("title", "磁盘最小空闲报警");
            param.set("state", 1);
            param.set("userId", userId);
            param.set("userName", userName);
            param.set("requestOrigin", request.getRequestURL());
            param.set("requestPath", request.getRequestURI());
            param.set("requestMethod", request.getMethod());
            param.set("content", "磁盘最小空闲报警");
            LogsAspect logsAspects = new LogsAspect();
            logsAspects.post(param);
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
    public synchronized List<Map<String, Object>> getNVRInfo(boolean force) {
        if (!force) {
            List<Map<String, Object>> cache = RECORDER_INFO_CACHE.get(RECORDER_KEY, false);
            if (cache != null) {
                return cache;
            }
        }

        List<TCameraRecorderDetail> list = tCameraRecorderDao.selectIdAndName();
        List<Map<String, Object>> reList = recordInfoList(list, true);

        ThreadPoolUtil.COMMON_POOL.addThread(() -> recordInfoList(list, false));

        return reList;
    }

    private List<Map<String, Object>> recordInfoList(List<TCameraRecorderDetail> list, boolean needCache) {
        String nvrFreeMin = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:nvrFreeMin", "content"));
        List<Map<String, Object>>  reList = new ArrayList<>();

        Map<String, Boolean> deviceStatusRespMap = recordStatus(list);

        for (TCameraRecorderDetail item : list) {
            Map<String, Object> map = null;
            if (deviceStatusRespMap.getOrDefault(item.getDeviceChannel(), false)) {
                if (needCache) {
                    String jsonStr = (String)redisTemplate.opsForValue().get("recorderInfo:" + item.getRecordId());
                    if (StringUtils.isNotEmpty(jsonStr)) {
                        map = JSON.parseObject(jsonStr);
                    }
                }
                if (map == null) {
                    map = cameraConService.getNVRStoreAndChanle(item.getRecordId());
                }
            } else {
                log.info("NVR 不在线: {}", item);
                map = new HashMap<>(4);
                map.put("errorMessage", "录像机不在线");
                map.put("status", "离线");
            }

            if (map.get("freeTotal") != null && map.get("capacityTotal") != null) {
                Long use = MapUtils.getLong(map, "freeTotal");
                Long all = MapUtils.getLong(map, "capacityTotal");
                long other = all - use;
                map.put("use", other);
                map.put("recorderName", item.getRecordName());
                map.put("nvrFreeMin", nvrFreeMin);
            } else {
                map.put("nvrFreeMin", nvrFreeMin);
                map.put("recorderId", item.getRecordId().toString());
                map.put("recorderName", item.getRecordName());
                map.put("freeTotal", "0");
                map.put("capacityTotal", "0");
                map.put("use", "0");
            }

            reList.add(map);
        }
        //接口返回100情况不入JVM缓存
        if (reList.stream().noneMatch(m -> m.containsKey("code"))){
            RECORDER_INFO_CACHE.put(RECORDER_KEY, reList);
        }
        return reList;
    }

    private Map<String, Boolean> recordStatus(List<TCameraRecorderDetail> list) {
        List<String> deviceList = list.stream().map(TCameraRecorderDetail::getDeviceChannel).collect(Collectors.toList());

        IRecordService iRecordService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IRecordService.class);
        RecordEntity build = RecordEntity.builder().deviceIdList(deviceList).build();
        Result<List<DeviceStatusResp>> deviceStatusRespResult = iRecordService.queryNVRStatus(build);

        Map<String, Boolean> deviceStatusRespMap;
        if (deviceStatusRespResult.isSuccess()) {
            List<DeviceStatusResp> deviceStatusRespList = deviceStatusRespResult.getData();
            deviceStatusRespMap = deviceStatusRespList.stream()
                .collect(Collectors.toMap(DeviceStatusResp::getDeviceId, DeviceStatusResp::getOnLine, (e1, e2) -> e1));
        } else {
            deviceStatusRespMap = Collections.emptyMap();
        }
        return deviceStatusRespMap;
    }

    public Object getLogsStorageInfo(HttpServletRequest request, Long userId) {
        Map<String,Integer> storageInfo = new HashMap<>();
        String logsMaxStorage = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:logMaximumStorage","content"));
        Integer usedCapacity = sysParamDao.getUsedLogCapacity();
        if(logsMaxStorage !=null && usedCapacity != null){
            storageInfo.put("used",usedCapacity);
            storageInfo.put("total",Integer.parseInt(logsMaxStorage));
            if(usedCapacity>= Integer.parseInt(logsMaxStorage)){
                String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userId,"userName"));
                MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
                param.set("logType", "5");
                param.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
                param.set("title", "审计日志容量达到限定阈值");
                param.set("state", 2);
                param.set("userId", userId);
                param.set("userName", userName);
                param.set("requestOrigin", request.getRequestURL());
                param.set("requestPath", request.getRequestURI());
                param.set("requestMethod", request.getMethod());
                param.set("content", "审计记录达到阈值上限报警");
                LogsAspect logsAspects = new LogsAspect();
                logsAspects.post(param);
            }
        }
        return storageInfo;
    }

    /**
     * 本机系统自检信息
     *
     * @return
     * @throws Exception
     */
    public Map<String, Object> getSystemCheck() throws Exception {
        Map<String, Object> res = Maps.newHashMap();
        SystemInfo systemInfo = new SystemInfo();

        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks);
        res.put("cpu", Integer.parseInt(format0Val(cpuUsage * 100)) + "");
        //内存
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        // 总内存
        long totalMemory = memory.getTotal();
        // 可用内存
        long availableMemory = memory.getAvailable();
        // 已使用内存
        long usedMemory = totalMemory - availableMemory;
        double memoryUsageRate = ((double) usedMemory / totalMemory);
        double memoryTotal = (double) totalMemory / (1024 * 1024 * 1024);
        res.put("memory_total", Integer.parseInt((format0Val(memoryTotal))) + "GB");
        res.put("memory_rate", Integer.parseInt(format0Val(memoryUsageRate * 100)) + "");

        FileSystem fileSystem = FileSystems.getDefault();
        Iterable<FileStore> fileStores = fileSystem.getFileStores();
        long totalAll = 0;
        long usable = 0;
        for (FileStore store : fileStores) {
            // 总空间
            long totalSpace = store.getTotalSpace();
            totalAll = totalAll + totalSpace;
            // 可用空间
            long usableSpace = store.getUsableSpace();
            usable = usable + usableSpace;
        }
        double diskUsageRate = ((double) (totalAll - usable) / totalAll);
        double diskTotal = (double) totalAll / (1024 * 1024 * 1024);
        res.put("disk_total", Integer.parseInt(format0Val(diskTotal)) + "GB");
        res.put("disk_rate", Integer.parseInt(format0Val(diskUsageRate * 100)) + "");
        return res;
    }

    public static String format0Val(Object num) {
        DecimalFormat df = new DecimalFormat("0");
        return df.format(num);
    }

    /**
     * 获取边缘节点系统自检信息
     *
     * @return
     */
    public List<Map<String, String>> getEdgeSystemCheck() {
        List<Map<String, String>> result = new ArrayList<>();
        List<TStdRegion> list = tStdRegionDao.selectAllEdgeRegion();
        List<String> keys = list.stream().map(t -> "systemCheck:" + t.getRegionCode()).distinct().collect(Collectors.toList());
        List<Map<String, String>> edgeSystemCheckList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>) connection -> {
            keys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });
        Map<String, Map<String, String>> redisMap = edgeSystemCheckList.stream()
                .filter(t -> !t.isEmpty()).collect(Collectors.toMap(t -> t.get("edgeCode"), Function.identity()));
        list.forEach(tStdRegion -> {
            Map<String, String> res = Maps.newHashMap();
            res.put("edgeName", tStdRegion.getRegionName());
            res.put("edgeStatus", StringUtils.isNotEmpty(tStdRegion.getEdgeStatus()) ? tStdRegion.getEdgeStatus() : "离线");
            if (redisMap.containsKey(tStdRegion.getRegionCode())){
                res.putAll(redisMap.get(tStdRegion.getRegionCode()));
            } else {
                res.put("edgeCode", tStdRegion.getRegionCode());
            }
            result.add(res);
        });

        return result;
    }
}
