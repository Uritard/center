package com.yjh.platform.module.user.service;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.TStdDeviceDetail;
import com.yjh.platform.module.task.entity.StatisticalTools;
import com.yjh.platform.module.task.entity.TCruisePlanCountByPage;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.ThreeDimensionalDao;
import com.yjh.platform.module.user.entity.AlarmAndMeteInfo;
import com.yjh.platform.module.user.entity.CameraUnionDevice;
import com.yjh.platform.module.user.entity.ModelNameAndName;
import com.yjh.platform.module.user.entity.RoutePlan;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

/**
 * @author YChen
 * @date 2021/8/10
 */
@Service
public class ThreeDimensionalService {

    @Autowired
    private ThreeDimensionalDao threeDimensionalDao;
    @Autowired
    private TCameraScreenDao tCameraScreenDao;
    @Autowired
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(ThreeDimensionalService.class);

    @Transactional(rollbackFor = Exception.class)
    public String searchAndLocationFocus(String deviceName,Integer deviceType){
        return threeDimensionalDao.searchAndLocationFocus(deviceName,deviceType);
    }
    @Transactional(rollbackFor = Exception.class)
    public String roamingPathSave(List<RoutePlan> routePlanList){
        List<Map<String,String>> mapList = new ArrayList<>();
        routePlanList.forEach(res -> {
            Map<String,String> map = new HashMap<>();
            map.put("xCoordinate",res.getxCoordinate());
            map.put("yCoordinate",res.getyCoordinate());
            map.put("zCoordinate",res.getzCoordinate());
            map.put("order",res.getOrder()+"");
            mapList.add(map);
        });

        for (int i = 0; i < mapList.size(); i++) {
            redisTemplate.opsForHash().putAll("RoutePlan:"+i, mapList.get(i));
        }
        return "漫游路径保存成功";
    }
    @Transactional(rollbackFor = Exception.class)
    public List<RoutePlan> roamingPathPlanning(){
        List<RoutePlan> routePlanList = new ArrayList<>();
        Set<String> routeKey = redisScan("RoutePlan:");
        for (String key : routeKey) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
            RoutePlan routePlan = new RoutePlan();
            routePlan.setxCoordinate(redisInfoMap.get("xCoordinate"));
            routePlan.setyCoordinate(redisInfoMap.get("yCoordinate"));
            routePlan.setzCoordinate(redisInfoMap.get("zCoordinate"));
            routePlan.setOrder(Integer.parseInt(redisInfoMap.get("order")));
            routePlanList.add(routePlan);
        }
        //按照存储的顺序排序
        Collections.sort(routePlanList,new Comparator<RoutePlan>(){
            @Override
            public int compare(RoutePlan o1, RoutePlan o2) {
                int diff = o1.getOrder() - o2.getOrder();
                if (diff > 0){
                    return 1;
                }else if(diff < 0){
                    return -1;
                }
                return 0;
            }
        });
        log.info("routePlanList=="+routePlanList);
        return routePlanList;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<AlarmAndMeteInfo> viewPointInformation(String modelName){
        return threeDimensionalDao.viewPointInformation(modelName);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<AlarmAndMeteInfo> viewPointInfo(String modelName){
        return threeDimensionalDao.viewPointInfo(modelName);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<AlarmAndMeteInfo> viewAlarmInfo(String modelName){
        return threeDimensionalDao.viewAlarmInfo(modelName);
    }
    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceDetail viewDeviceInfo(String modelName){
        return threeDimensionalDao.viewDeviceInfo(modelName);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<CameraUnionDevice> viewCameraInfo(String modelName){
        //通过模型名称查询相关信息
        Map<String,Object> map = threeDimensionalDao.selectDeviceInfoByModelName(modelName);
        Long deviceId = NumberUtils.toLong(String.valueOf(map.get("id")));
        List<CameraUnionDevice> cameraUnionDeviceList = new ArrayList<>();
        if (map.containsKey("is_camera") && Objects.equals(1, map.get("is_camera"))){
            // 该模型就是摄像机  直接查询所有的预置位点
            cameraUnionDeviceList = threeDimensionalDao.selectCameraByCameraId(deviceId);
        }else {
            //关联的摄像机只能是通过设备下的某个测点配置了预置位成为巡检点才能找到。
            cameraUnionDeviceList = threeDimensionalDao.selectCameraByDeviceId(deviceId);
        }
        /*Map<String,String> map = new HashMap<>();
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId : recordIdList){
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",recordId );
            Result re = cameraStates(recordIdMap);
            if (Objects.nonNull(re)){
                map.putAll((Map<String,String>)re.getData());
            }
        }

        for (CameraUnionDevice xi : cameraUnionDeviceList){
            if (map.containsKey(xi.getCameraId().toString())){
                if ("0".equals(map.get(xi.getCameraId().toString()))){
                    cameraUnionDeviceList.remove(xi);
                }
            }else {
                cameraUnionDeviceList.remove(xi);
            }
        }*/

        return cameraUnionDeviceList;
    }
    private static Result cameraStates(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(Constant.CAMERA_STATES, Result.class,map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return re;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticalTools> statisticsAlarmInfo(){
        return threeDimensionalDao.statisticsAlarmInfo();
    }

    @Transactional(rollbackFor = Exception.class)
    public ModelNameAndName selectAllModelNameAndName(){
        ModelNameAndName modelNameAndName = new ModelNameAndName();
        List<Map<String, String>> modelNameList = threeDimensionalDao.selectAllModelName();
        modelNameAndName.setModelNameList(modelNameList);
        List<String> nameList = threeDimensionalDao.selectAllName();
        modelNameAndName.setNameList(nameList);
        return modelNameAndName;
    }

    public List<TCruisePlanCountByPage> selectByPlanPage(Map<String, Object> planMap){

        return threeDimensionalDao.selectByPlanPage(planMap);
    }

    //Redis数据库批量查询Key值游标
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }
}
