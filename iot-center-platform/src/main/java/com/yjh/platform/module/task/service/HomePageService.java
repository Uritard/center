package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.dao.TVoiceDeviceDao;
import com.yjh.platform.module.device.entity.DeviceCountBean;
import com.yjh.platform.module.device.entity.StationVoltageData;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.device.entity.TaskInfoBean;
import com.yjh.platform.module.device.service.SystemInfoService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.dao.TDefectInfoDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TCameraGroupDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2020/12/14
 */
@Service
public class HomePageService {


    private Logger log = LoggerFactory.getLogger(HomePageService.class);
    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;
    @Autowired
    private TDefectInfoDao tDefectInfoDao;
    @Autowired
    private TCruiseTaskResultService tCruiseTaskResultService;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SystemInfoService systemInfoService;
    @Autowired
    private TCameraGroupDao tCameraGroupDao;
    @Autowired
    private TCameraScreenDao tCameraScreenDao;
    @Autowired
    private TStdRegionService tStdRegionService;
    @Autowired
    private TCameraInfoDao tCameraInfoDao;
    @Autowired
    private TVoiceDeviceDao tVoiceDeviceDao;
    @Autowired
    private UPatrolResultDao uPatrolResultDao;

    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> taskInfo(Integer date, String regionCode) {
        if (1 == date) {
            return tCruiseTaskDao.selectOnWeek();
        }
        if (2 == date) {
            return tCruiseTaskDao.selectOnMonth();
        }
        if (3 == date) {
            return tCruiseTaskDao.selectOnYear();
        }
        if (4 == date) {
            return tCruiseTaskDao.selectForSevenDay(regionCode);
        }
        if (5 == date) {
            return tCruiseTaskDao.selectForMonth(regionCode);
        }
        if (6 == date) {
            return tCruiseTaskDao.selectForYear(regionCode);
        }
        return null;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Integer>> countByDefectLevel() {
        return tDefectInfoDao.countByDefectLevel();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TaskOnExecuteInfo> taskOnExecute() throws ParseException {
        //查出正在执行的任务
        List<TaskOnExecuteInfo> listTask = tCruiseTaskDao.selectTaskOnExecute();
        log.info("任务list: " + listTask);
        for (TaskOnExecuteInfo item : listTask) {
            try {
//                CruiseResultCounter cruiseResultCounter = tCruiseTaskResultService.selectCruiseStatusCount(item.getTaskId());
                CruiseResultCounter cruiseResultCounter = tCruiseTaskResultService.selectCruiseStatusCountNew(item.getTaskId());
                item.setAlarmCount(cruiseResultCounter.getAlarmCount());
                item.setCruisedCount(cruiseResultCounter.getCruisedCount());
                item.setCruiseNotCount(cruiseResultCounter.getCruiseNotCount());
                item.setRunningTime(cruiseResultCounter.getRunningTime());
            } catch (Exception e) {
                log.info("出错了" + e);
                item.setAlarmCount(null);
                item.setCruisedCount(null);
                item.setCruiseNotCount(null);
                item.setRunningTime(null);
            }
            try {
                Map<String, Object> map = tCruiseTaskResultService.selectCruiseAdvance(item.getTaskId());
                Float i = (Float) map.get("rate");
                i = i * 100F;
                item.setTaskProgress(i.intValue());
            } catch (Exception e) {
                log.info("出错了" + e);
                item.setTaskProgress(0);
            }

        }
        return listTask;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> countByAlarmLevel() {
        return tDefectInfoDao.countByAlarmLevel();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<WarnInfoForHomePage> selectThereWarn(Integer alarmLevel) {
        return tDefectInfoDao.selectThereWarn(alarmLevel);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<RobotInfoForHomePage> robotInfoForHomePage(String robotPosition) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("#0");
        List<RobotInfoForHomePage> robotList = tRobotInfoDao.selectRobotInfo(robotPosition);
        for (RobotInfoForHomePage item : robotList) {
            //计算机器人投运时间
            if (item.getCommissionDate() == null) {
                item.setCommissionDate("");
            } else {
                Date startTime = simpleDateFormat.parse(item.getCommissionDate());
                Date endTime = new Date();
                long diff = endTime.getTime() - startTime.getTime();
                long days = diff / (1000 * 60 * 60 * 24);
//                long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
//                long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
                String usedTime = "--";
                if (days >= 0) {
                    usedTime = days + "天";
                }
                if (diff < 0) {
                    usedTime = "--";
                }
//                if(days == 0){
//                    usedTime ="0天";
//                }
//                if(hours != 0){
//                    usedTime =usedTime+hours+"小时";
//                }
//                if(minutes != 0){
//                    usedTime =usedTime+minutes+"分";
//                }
                item.setCommissionDate(usedTime);
            }


            Map<String, Object> mapForCell = redisTemplate.opsForHash().entries("RobotOperation:" + item.getRobotCode() + ":3");
            if (mapForCell.size() != 0) {
                Double value = Double.valueOf(mapForCell.get("value").toString());//电池电量
                String valueUnit = df.format(value) + "%";
                item.setBatteryLevel(valueUnit);
            } else {
                item.setBatteryLevel("");
            }
            Map<String, Object> mapForOnlineState = redisTemplate.opsForHash().entries("RobotStatus:" + item.getRobotCode() + ":2");
            if (mapForOnlineState.size() != 0) {
                String state = mapForOnlineState.get("value").toString();
                if ("0".equals(state)) {
                    state = "在线";
                } else if ("1".equals(state)) {
                    state = "离线";
                } else {
                    state = "";
                }
                item.setOnlineState(state);//网络状态
            } else {
                item.setOnlineState("");//网络状态
            }
            Map<String, Object> mapForMileage = redisTemplate.opsForHash().entries("RobotOperation:" + item.getRobotCode() + ":2");
            if (mapForMileage.size() != 0) {
                item.setMileage(mapForMileage.get("valueUnit").toString());//里程
            } else {
                item.setMileage("");//里程
            }
            Map<String, Object> mapForControlModel = redisTemplate.opsForHash().entries("RobotStatus:" + item.getRobotCode() + ":61");
            if (mapForControlModel.size() != 0) {
                String model = mapForControlModel.get("value").toString();
                if ("1".equals(model)) {
                    model = "任务模式";
                } else if ("2".equals(model)) {
                    model = "紧急定位模式";
                } else if ("3".equals(model)) {
                    model = "后台遥控模式";
                } else if ("4".equals(model)) {
                    model = "手持遥控模式";
                } else {
                    model = "";
                }
                item.setControlModel(model);//控制模式
            } else {
                item.setControlModel("");//控制模式
            }

            Map<String, Object> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:" + item.getRobotCode() + ":41");
            if (mapForRobotState.size() != 0) {
                String state = mapForRobotState.get("value").toString();
                if ("1".equals(state)) {
                    state = "空闲状态";
                } else if ("2".equals(state)) {
                    state = "巡视状态";
                } else if ("3".equals(state)) {
                    state = "充电状态";
                } else if ("4".equals(state)) {
                    state = "检修状态";
                } else {
                    state = "";
                }
                item.setRobotStates(state);//机器人状态
            } else {
                item.setRobotStates("");//机器人状态
            }
        }
        return robotList;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> stationInfo() throws Exception {
        DecimalFormat df = new DecimalFormat("#0.0");
        Map<String, Object> mapForRe = new HashMap<>();
        //获取投运时间 commissioningTime
        String commissioningTime = redisTemplate.opsForHash().entries("t_sys_param:commissioningTime").get("content").toString();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startTime = simpleDateFormat.parse(commissioningTime);
        //计算时间
        Date endTime = new Date();
        long diff = endTime.getTime() - startTime.getTime();
        long days = diff / (1000 * 60 * 60 * 24);
        mapForRe.put("timeValue", days);
        mapForRe.put("timeUnit", "天");
        //电压等级
        String stationVoltageGrade = redisTemplate.opsForHash().entries("t_sys_param:stationVoltageGrade").get("content").toString();
        mapForRe.put("stationVoltageGrade", stationVoltageGrade);
        //所属班所
        String fromWhatClass = redisTemplate.opsForHash().entries("t_sys_param:fromWhatClass").get("content").toString();
        mapForRe.put("fromWhatClass", fromWhatClass);
        //所有人员
        String allPeople = redisTemplate.opsForHash().entries("t_sys_param:allPeople").get("content").toString();
        mapForRe.put("allPeople", allPeople);
        //工作人员
        String workPeople = redisTemplate.opsForHash().entries("t_sys_param:workPeople").get("content").toString();
        mapForRe.put("workPeople", workPeople);
        //所有车辆
        String allCar = redisTemplate.opsForHash().entries("t_sys_param:allCar").get("content").toString();
        mapForRe.put("allCar", allCar);
        //工作车辆
        String workCar = redisTemplate.opsForHash().entries("t_sys_param:workCar").get("content").toString();
        mapForRe.put("workCar", workCar);
        return mapForRe;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> getCameraGroupInfo() {
        return tCameraGroupDao.selectGroupName();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> getCameraIdInfo() {
        Map<String, String> map = new HashMap<>();
        //获取相机的状态
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for (Long recordId : recordIdList) {
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId", recordId);
            Result re = cameraStates(recordIdMap);
//            if(re == null){
//                continue;
//            }
            log.info("re.getData()==========={}", re.getData());
            // 前提是video有非空返回值
            map.putAll((Map<String, String>) re.getData());
        }
        List<Long> re = new ArrayList<>();
        List<Long> cameraIdList = tCameraGroupDao.selectCameraIdInfo();
        for (Long item : cameraIdList) {
            //String str = map.get(item.toString());
            if ("1".equals(map.get(item.toString()))) {
                re.add(item);
            }
        }
        return re;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<String> getCameraIdInfoForGroup(Long groupId) {
        Map<String, String> map = new HashMap<>();
        //获取相机的状态
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for (Long recordId : recordIdList) {
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId", recordId);
            Result re = cameraStates(recordIdMap);
            if (re == null) {
                continue;
            }
            map.putAll((Map<String, String>) re.getData());
        }
        List<String> re = new ArrayList<>();
        String[] cameraIdList = tCameraGroupDao.selectCameraIdInfoForGroup(groupId).split(",");
        for (String item : cameraIdList) {
            //String str = map.get(item.toString());
            if ("1".equals(map.get(item))) {
                re.add(item);
            }
        }
        return re;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getWeatherInfo() throws Exception {
        //String url = "http://192.168.10.100:18713/format/v1/getWeatherInfo";
        String url = redisTemplate.opsForHash().entries("t_sys_param:weatherInfoService").get("content").toString();

        String services = HttpClientUtils.getInstance().getUrl(url, null);
        JSONObject jsonObject = JSONObject.parseObject(services);
        Map<String, Object> re = (Map<String, Object>) jsonObject.get("data");
        if (re == null || re.size() == 0) {
            return null;
        }
        return re;
    }

    private static Result cameraStates(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re = serviceRestTemplate.getForObject(Constant.CAMERA_STATES, Result.class, map);
            }
        } catch (Exception e) {

        }
        return re;
    }

    /**
     * 环境告警数据查询
     *
     * @date 2022
     */
    public List<HashMap<String, String>> envWarningQuery(JSONObject jsonObject) {
        log.info("envWarningQuery开始");
        List<HashMap<String, String>> map = tCruiseTaskDao.envWarningQuery(jsonObject);
        Long regionId = null;
        RegionPath regionPath = queryRegionPath(regionId);
        map.forEach(maps -> {
            maps.put("regionName", regionPath.getRegionName());
        });
        return map;
    }


    /**
     * 站所状况统计
     *
     * @date 2022/3/1
     */
    public List<StationCount> queryStations(Long regionId) {

        List<Long> regionIdList = tStdRegionService.selectDownId(regionId);
        StationCount sta = new StationCount();
        sta.setType("环控设备");
        //环控数量
        if (regionId == null) {
            RegionPath regionPath = tCruiseTaskDao.queryRegion(regionId);
            regionId = regionPath.getRegionId();
        }
        List<EnvDeviceStatus> list = queryWeatherInfo(regionId);
        if (list != null && list.size() > 0) {
            sta.setCount(list.size() + "");
        } else {
            sta.setCount("0");
        }
        List<StationCount> stationCounts = tCruiseTaskDao.queryStations(regionIdList);
        stationCounts.add(sta);
        return stationCounts;
    }

    public RegionPath queryRegionPath(Long regionId) {
        return tCruiseTaskDao.queryRegion(regionId);
    }

    /**
     * 告警统计内容
     *
     * @date 2022/3/1
     */
    public List<WarnInforForHomePages> deviceWarnInfo(String state) {
        return tDefectInfoDao.selectDeviceWarnInfo(state);
    }

    public List<EnvDeviceStatus> queryWeatherInfo(Long regionId) {
        String envDataJson = getRedisMapString("Weather", regionId.toString());
        List<EnvDeviceStatus> queryEnvDeviceInfo = null;
        if (StringUtils.isNotEmpty(envDataJson)) {
            JSONArray objects = JSONArray.parseArray(envDataJson);
            queryEnvDeviceInfo = objects.toJavaList(EnvDeviceStatus.class);
        }
        return queryEnvDeviceInfo;
    }

    /**
     * 获取redis集合值
     *
     * @param key
     * @return
     */
    private <T> T getRedisMapString(String key, String field) {
        return (T) redisTemplate.opsForHash().get(key, field);
    }

    public Map queryStationPosition() {
        Map<String, Object> map = new HashMap<>();
        map.put("list", tStdRegionService.queryStationPosition());
        map.put("adcode", redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "adcode", "content"));
        return map;
    }

    public List<StationVoltageData> queryStationVoltage() {
        return tStdRegionService.queryStationVoltage();
    }

    public List<DeviceCountBean> queryDeviceCountByType() {
        int cameraCount = tCameraInfoDao.selectCount();
        int voiceDeviceCount = tVoiceDeviceDao.selectCount();
        int robotCount = tRobotInfoDao.selectRobotCount();
        int droneCount = tRobotInfoDao.selectDroneCount();
        List<DeviceCountBean> deviceCountBeanList = Lists.newArrayList();
        deviceCountBeanList.add(new DeviceCountBean(cameraCount, "camera", "摄像头"));
        deviceCountBeanList.add(new DeviceCountBean(voiceDeviceCount, "voice", "声纹"));
        deviceCountBeanList.add(new DeviceCountBean(robotCount, "robot", "机器人"));
        deviceCountBeanList.add(new DeviceCountBean(droneCount, "drone", "无人机"));
        return deviceCountBeanList;
    }

    public TaskInfoBean queryTaskInfo() {
        return uPatrolResultDao.queryTaskInfo();
    }

    public List<TStdRegion> queryStationList() {
        List<TStdRegion> list = tStdRegionDao.selectByState(Constant.STATE_LOCAL);
        list.removeIf(tStdRegion -> tStdRegion.getUpRegionId() == -1);
        return list;
    }

}
