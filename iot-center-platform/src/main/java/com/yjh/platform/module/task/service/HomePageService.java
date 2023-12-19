package com.yjh.platform.module.task.service;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.module.device.dao.TMeterDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.dao.TVoiceDeviceDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.service.SystemInfoService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.iot.dao.TIotDeviceDataMapper;
import com.yjh.platform.module.iot.entity.IotDeviceDataEx;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.task.dal.TStdWeatherLogDO;
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.entity.input.RegionVideo;
import com.yjh.platform.module.user.dao.TCameraGroupDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.video.controller.CameraConController;
import com.yjh.platform.module.video.service.CameraConService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Autowired
    private CameraConService cameraConService;
    @Resource
    private TStdWeatherLogDao tStdWeatherLogDao;
    @Resource
    private TStdDevicemeteDao tStdDevicemeteDao;
    @Resource
    private TWarnInfoDao tWarnInfoDao;
    @Autowired
    private TMeterDao tMeterDao;
    @Autowired
    private TWarnInfoService warnInfoService;

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
//        log.info("任务list: " + listTask);
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
                Float i = Float.parseFloat(map.get("rate").toString());
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

    public List<Map<String, Object>> countAllByAlarmLevelOnMonth(Integer type) {
        Integer nearDays = warnInfoService.nearDays(type);
        return tDefectInfoDao.countAllByAlarmLevel(nearDays);
    }


    public List<Map<String, Object>> countAllByAlarmType(Integer type) {
        Integer nearDays = warnInfoService.nearDays(type);
        return tDefectInfoDao.countAllByAlarmType(nearDays);
    }


    public String[][] countWarnByStationOnMonth(String alarmSource, Integer type) {
        Integer nearDays = warnInfoService.nearDays(type);
        List<WarnStatistical> alarmList = new ArrayList<>();
        //静默告警字典值
        int jm = 689;
        if (StringUtils.isNotBlank(alarmSource)) {
            switch (alarmSource) {
                case "巡视":
                    alarmList = tWarnInfoDao.countWarnByStationOnMonth(null, nearDays);
                    alarmList = alarmList.stream().filter(w -> w.getAlarmSource() != jm).collect(Collectors.toList());
                    List<WarnStatistical> defectList = tWarnInfoDao.countDefectByStationOnMonth(null, nearDays);
                    alarmList.addAll(defectList);
                    break;
                case "监控":
                    alarmList = tWarnInfoDao.countMonByStationOnMonth(nearDays);
                    break;
                case "入侵":
                    alarmList = tWarnInfoDao.countWarnByStationOnMonth(null, nearDays);
                    alarmList = alarmList.stream().filter(w -> w.getAlarmSource() == jm).collect(Collectors.toList());
                    break;
                default:
                    alarmList = tWarnInfoDao.countAllByStationOnMonth(nearDays);
                    break;
            }
        } else {
            alarmList = tWarnInfoDao.countAllByStationOnMonth(nearDays);
        }

        List<TStdRegion> list = tStdRegionDao.selectStations();

        Map<Long, KeyValue<Long, String>> stationMap = tStdRegionService.stationDownId();
        Map<Long, Integer> stationWarnCount = warnInfoService.alarmLoop(alarmList, stationMap);

        String[][] dataSet = new String[2][list.size() + 1];
        String[] products = dataSet[0];
        String[] warns = dataSet[1];
        products[0] = "product";
        warns[0] = "告警";
        for (int i = 0; i < list.size(); i++) {
            int idx = i + 1;
            TStdRegion region = list.get(i);
            if (region != null) {
                products[idx] = region.getRegionName();
                warns[idx] = Optional.ofNullable(stationWarnCount.get(region.getRegionId())).orElse(0).toString();
            }
        }
        return dataSet;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<WarnInfoForHomePage> selectThereWarn(Integer alarmLevel) {
        return tDefectInfoDao.selectThereWarn(alarmLevel);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> robotInfoForHomePage() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("#0");
        List<RobotInfoForHomePage> robotList = tRobotInfoDao.selectRobotInfo();
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
                Double value = NumberUtils.toDouble(MapUtils.getString(mapForCell, "value"));//电池电量
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
        List<Map<String, Object>> resultList = Lists.newArrayList();
        Map<String, List<RobotInfoForHomePage>> resultMap = robotList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(RobotInfoForHomePage::getPositionName));
        resultMap.forEach((k, v) -> {
            Map<String, Object> map = new HashMap<>(3);
            map.put("name", k);
            map.put("value", getRobotPositionValue(k));
            map.put("list", v);
            resultList.add(map);
        });
        return resultList;
    }

    private String getRobotPositionValue(String positionName) {
        switch (positionName) {
            case "室内轮式":
                return "tab_indoor";
            case "室外轮式":
                return "tab_outdoor";
            default:
                return "tab_tunnel";
        }
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

    public List<String> getCameraIdInfo() {
        Map<String, String> map = new HashMap<>();
        //获取相机的状态
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for (Long recordId : recordIdList) {
            Map<String, String> reMap = cameraConService.getCameraStatus(recordId);
            if (reMap != null) {
                map.putAll(reMap);
            }
        }
        List<String> re = new ArrayList<>();
        List<Long> cameraIdList = tCameraGroupDao.selectCameraIdInfo();
        for (Long item : cameraIdList) {
            if ("1".equals(map.get(item.toString()))) {
                re.add(String.valueOf(item));
            }
            // 最多取3个，避免取太多流消耗资源且无意义
            if (re.size() >= 3) {
                break;
            }
        }
        // 没有取得摄像机，取机器人
        if (re.size() < 3) {
            List<TRobotInfo> robotList = tRobotInfoDao.selectRobotOnline(-1, -1, "在线", -1, null);
            re.addAll(robotList.stream().limit(3 - re.size()).map(r -> r.getRobotId() + "9901").collect(Collectors.toList()));
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

    @Transactional(rollbackFor = Exception.class)
    public List<String> getCameraIdInfoForGroup(Long groupId) {
        Map<String, String> map = new HashMap<>();
        //获取相机的状态
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for (Long recordId : recordIdList) {
            Map<String, String> reMap = cameraConService.getCameraStatus(recordId);
            if (reMap != null) {
                map.putAll(reMap);
            }

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


    /**
     * 环境告警数据查询
     *
     * @date 2022
     */
    public List<HashMap<String, String>> envWarningQuery(JSONObject jsonObject) {
        log.info("envWarningQuery开始");
        return tCruiseTaskDao.envWarningQuery(jsonObject);
    }


    /**
     * 站所状况统计
     *
     * @param type 1 机器人拆分统计  2 机器人合并统计
     * @date 2022/3/1
     */
    public List<StationCount> queryStations(Long regionId, String type) {

        List<Long> regionIdList = tStdRegionService.selectDownId(regionId);
        StationCount sta = new StationCount();
        sta.setType("环控设备");
        int staSize;
        //环控数量
        List<Long> envReginIdList = tRobotInfoDao.selectAllEnvRegionId(regionIdList);
        staSize = envReginIdList.stream().map(this::queryWeatherInfo).filter(list -> list != null && list.size() > 0).mapToInt(List::size).sum();
        sta.setCount(staSize);
        List<StationCount> stationCounts;
        if ("1".equals(type)) {
            stationCounts = tCruiseTaskDao.queryStations(regionIdList);
        } else {
            stationCounts = tCruiseTaskDao.queryStations2(regionIdList);
        }
        stationCounts.add(sta);
        return stationCounts;
    }

    /**
     * 查询站所数据详情
     *
     * @param regionId 区域ID
     * @return
     */
    public StationDetail queryStationDetail(Long regionId) {
        StationDetail stationDetail = new StationDetail();
        stationDetail.setStationCounts(this.queryStations(regionId, "2"));
        List<Long> regionIdList = tStdRegionService.selectDownId(regionId);
        List<EnvDeviceStatus> envDeviceStatusList = this.queryWeatherInfos(regionIdList);
        envDeviceStatusList = envDeviceStatusList.stream().filter(e -> StringUtils.equals(e.getShowType(), "2")).limit(6).collect(Collectors.toList());
        stationDetail.setEnvDeviceStatuses(envDeviceStatusList);
        List<RegionVideo> regionVideoList = tCruiseTaskDao.queryRegionVideoList(regionIdList);
        Map<String, String> map = new HashMap<>();
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for (Long recordId : recordIdList) {
            Map<String, String> recordIdMap = cameraConService.getCameraStatus(recordId);
            if (recordIdMap == null) {
                continue;
            }
            map.putAll(recordIdMap);
        }
        regionVideoList = regionVideoList.stream().filter(r -> StringUtils.equals(map.get(r.getCameraId().toString()), "1")).limit(6).collect(Collectors.toList());
        stationDetail.setRegionVideos(regionVideoList);
        return stationDetail;
    }

    public List<RegionPath> queryRegionList() {
        return tCruiseTaskDao.queryRegionList();
    }

    public String queryRegionName() {
        return tCruiseTaskDao.queryRegionName();
    }

    /**
     * 告警统计内容
     *
     * @date 2022/3/1
     */
    public List<WarnInforForHomePages> deviceWarnInfo(String state) {
        return tDefectInfoDao.selectDeviceWarnInfo(state);
    }

    public List<EnvDeviceStatus> queryWeatherInfos(List<Long> regionIdList) {
        List<TStdRegion> regions = tStdRegionService.queryAll();
        Map<Long, String> regionMap = regions.stream().collect(Collectors.toMap(TStdRegion::getRegionId, TStdRegion::getRegionName));

        List<Long> envReginIdList = tRobotInfoDao.selectAllEnvRegionId(regionIdList);
        List<EnvDeviceStatus> envDeviceStatusList = new ArrayList<>();
        for (Long aLong : envReginIdList) {
            List<EnvDeviceStatus> envDeviceStatuses = queryWeatherInfo(aLong, regionMap.getOrDefault(aLong, ""));
            if (CollectionUtils.isNotEmpty(envDeviceStatuses)) {
                envDeviceStatusList.addAll(envDeviceStatuses);
            }
        }
        return envDeviceStatusList;
    }

    public List<EnvDeviceStatus> queryWeatherInfo(Long regionId) {
        return queryWeatherInfo(regionId, null);
    }

    public List<EnvDeviceStatus> queryWeatherInfo(Long regionId, String regionName) {
        String envDataJson = getRedisMapString("Weather", regionId.toString());
        List<EnvDeviceStatus> queryEnvDeviceInfo = new ArrayList<>();

        if (StringUtils.isNotEmpty(envDataJson)) {
            JSONArray objects = JSONArray.parseArray(envDataJson);
            queryEnvDeviceInfo = objects.toJavaList(EnvDeviceStatus.class);
        }
        if (StringUtils.isNotEmpty(regionName)) {
            queryEnvDeviceInfo.forEach(d -> d.setRegionName(regionName));
        }
        return queryEnvDeviceInfo;
    }

    /**
     * 每10分钟获取信息
     */
    @Scheduled(cron = "0 0/10 * * * ?")
//    @Scheduled(cron = "0 * * * * ?")
    public void getWeatherInfoSchedule() {
        log.info("开始同步缓存数据");
        // 获取所有区域
        List<TStdRegion> regionList = tStdRegionService.queryAll();
        List<Long> regionIds = regionList.stream().map(TStdRegion::getRegionId).collect(Collectors.toList());
        List<TStdWeatherLogDO> weatherLogs = new ArrayList<>();
        for (Long regionId : regionIds) {
            String envDataJson = (String) redisTemplate.opsForHash().get("Weather", regionId.toString());
            if (StringUtils.isNotEmpty(envDataJson)) {
                JSONArray objects = JSONArray.parseArray(envDataJson);
                weatherLogs.addAll(objects.toJavaList(TStdWeatherLogDO.class));
            }
        }
        if (!CollectionUtils.isEmpty(weatherLogs)) {
            tStdWeatherLogDao.batchInsert(weatherLogs);
        }
    }

    public List<EnvDeviceStatus> queryWeatherLog(QueryWeatherLogReq req) {
        Asserts.notNull(req.getDeviceId(), "设备参数为空");
        Asserts.notNull(req.getRobotCode(), "机器人编号为空");
        TStdWeatherLogDO tStdWeatherLogDO = new TStdWeatherLogDO();
        tStdWeatherLogDO.setDeviceId(req.getDeviceId());
        tStdWeatherLogDO.setRobotCode(req.getRobotCode());
        tStdWeatherLogDO.setStartTime(req.getStartTime());
        tStdWeatherLogDO.setEndTime(req.getEndTime());
        List<TStdWeatherLogDO> list = tStdWeatherLogDao.list(tStdWeatherLogDO);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<EnvDeviceStatus> resultList = list.stream().map((e) -> {
            EnvDeviceStatus envDeviceStatus = new EnvDeviceStatus();
            BeanUtil.copyProperties(e, envDeviceStatus);
            envDeviceStatus.setCreateTime(CommonUtils.formatDate(e.getCreateTime()));
            if ("正常".equals(envDeviceStatus.getDeviceValue())) {
                envDeviceStatus.setDeviceValue("0");
            } else if ("异常".equals(envDeviceStatus.getDeviceValue())) {
                envDeviceStatus.setDeviceValue("1");
            }
            return envDeviceStatus;
        }).collect(Collectors.toList());
        return resultList;
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
        TaskInfoBean taskInfoBean = uPatrolResultDao.queryTaskInfo();
        List<StationCount> list = this.queryStations(null, "1");
        taskInfoBean.setTotalDeviceCount(list.stream().mapToInt(StationCount::getCount).sum());
        taskInfoBean.setTotalPointCount(tStdDevicemeteDao.selectStdDeviceMeteCount());
        TaskInfoBean taskInfoBean2 = tWarnInfoDao.selectAllCount();
        taskInfoBean.setCurAlarmCount(taskInfoBean2.getCurAlarmCount());
        taskInfoBean.setUntreatedAlarmCount(taskInfoBean2.getUntreatedAlarmCount());
        return taskInfoBean;
    }

    public List<TStdRegion> queryStationList() {
        List<TStdRegion> list = tStdRegionDao.selectStations();
        list.removeIf(tStdRegion -> tStdRegion.getUpRegionId() == -1);
        list.stream().forEach(e -> {
            if (e.getCommissioningTime() != null) {
                LocalDate date = LocalDate.parse(e.getCommissioningTime());
                // 获取当前时间
                LocalDate now = LocalDate.now();
                // 计算当前时间与输入时间之间的天数
                Long days = now.until(date, ChronoUnit.DAYS);
                e.setCommissioningDays(Math.abs(days));
            }
        });
        return list;
    }

//    public List<Map<String,Float>> countPowerTotal(Integer type){
//        //type 1-根据区域进行统计 2-根据节点进行统计
////        if (1 == type){
////            return tMeterDao.countPowerTotalByRegion();
//////        } else {
//////            return tMeterDao.countPowerTotalByEdge();
//////        }
//
//    }

    public Map<String, Object> countPowerTotalByDay() {
        LocalDate today = LocalDate.now();
        // 获取昨天的日期
        LocalDate yesterday = today.minusDays(1);
        // 昨天的开始时间，即00:00:00
        LocalDateTime yesterdayStart = LocalDateTime.of(yesterday, LocalTime.MIN);
        // 昨天的结束时间，即23:59:59.999999999
        LocalDateTime yesterdayEnd = LocalDateTime.of(yesterday, LocalTime.MAX);

        LocalDate beforeYesterday = today.minusDays(2);
        // 昨天的开始时间，即00:00:00
        LocalDateTime beforeYesterdayStart = LocalDateTime.of(beforeYesterday, LocalTime.MIN);
        // 昨天的结束时间，即23:59:59.999999999
        LocalDateTime beforeYesterdayEnd = LocalDateTime.of(beforeYesterday, LocalTime.MAX);

        List<Map<String, String>> yesterdayList = tMeterDao.countPowerTotalByEdge(yesterdayStart, yesterdayEnd);
        List<Map<String, String>> beforeYesterdayList = tMeterDao.countPowerTotalByEdge(beforeYesterdayStart, beforeYesterdayEnd);

        Map<String, Object> reMap = new HashMap<>(4);
        dealPowerInfo(yesterdayList, beforeYesterdayList, reMap);

        reMap.put("yesterdayList", yesterdayList);

        return reMap;
    }

    private void dealPowerInfo(List<Map<String, String>> yesterday,
                               List<Map<String, String>> beforeYesterdayList,
                               Map<String, Object> reMap) {
        DecimalFormat df = new DecimalFormat("#0.00");
        //1.计算总量
        Double yesterdayAll = 0d;
        for (Map<String, String> item : yesterday) {
            yesterdayAll = yesterdayAll + ValueUtil.toDouble(item.get("allTotal"), 0d);
        }
        reMap.put("yesterdayAllTotal", df.format(yesterdayAll));
        Double beforeYesterdayAll = 0d;
        for (Map<String, String> item : beforeYesterdayList) {
            beforeYesterdayAll = beforeYesterdayAll + ValueUtil.toDouble(item.get("allTotal"), 0d);
        }
        reMap.put("beforeYesterdayAll", df.format(beforeYesterdayAll));
        //2.计算百分比
        for (Map<String, String> item : yesterday) {
            Double value = ValueUtil.toDouble(item.get("allTotal"), 0d);
            double percentage = value * 100 / yesterdayAll;
            item.put("percentage", String.valueOf(Math.round(percentage)));
            item.put("yesterdayTotal", item.get("allTotal"));
            String beforeYesterdayTotal = "0";
            item.put("beforeYesterdayTotal", beforeYesterdayTotal);
            for (Map<String, String> beforeItem : beforeYesterdayList) {
                if (beforeItem.containsValue(item.get("regionName"))) {
                    item.put("beforeYesterdayTotal", beforeItem.get("allTotal"));
                    Double beforeValue = ValueUtil.toDouble(item.get("allTotal"), 0d);
                    double beforeYesterdayPercentage = beforeValue * 100 / beforeYesterdayAll;
                    item.put("beforeYesterdayPercentage", String.valueOf(Math.round(beforeYesterdayPercentage)));
                    break;
                }
            }
        }
    }

    public List<HomeDeviceInfo> deviceInfo(Integer type, String edgeCode) {
        List<TRobotInfo> robotInfoList = tRobotInfoDao.selectAllRobotByEdgeCodeOrType(edgeCode, type);
        DictConvertUtil.optional("robotType").covertToDict(robotInfoList);
        List<HomeDeviceInfo> homeDeviceInfoList = new ArrayList<>();
        for (TRobotInfo robotInfo : robotInfoList) {
            HomeDeviceInfo homeDeviceInfo = new HomeDeviceInfo();
            homeDeviceInfo.setRobotName(robotInfo.getRobotName());
            Map<String, Object> robotStatus = redisTemplate.opsForHash().entries("RobotStatus:" + robotInfo.getRobotCode() + ":2");
            String robotState = "离线";
            if (robotStatus.size() != 0 && Optional.ofNullable(robotStatus.get("value")).isPresent()) {
                if (!"1".equals(String.valueOf(robotStatus.get("value")))) {
                    Map<String, Object> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:" + robotInfo.getRobotCode() + ":41");
                    /**
                     * <41>: = 运行状态
                     * * <1>: = 空闲状态
                     * * <2>: = 巡视状态
                     * * <3>: = 充电状态
                     * * <4>: = 检修状态
                     */
                    if (mapForRobotState.size() != 0 && Optional.ofNullable(mapForRobotState.get("value")).isPresent()) {
                        String value = String.valueOf(mapForRobotState.get("value"));
                        switch (value) {
                            case "1":
                                robotState = "空闲状态";
                                break;
                            case "2":
                                robotState = "巡视状态";
                                break;
                            case "3":
                                robotState = "充电状态";
                                break;
                            case "4":
                                robotState = "检修状态";
                                break;
                            default:
                                robotState = "离线";
                                break;
                        }
                    }
                }

            }
            homeDeviceInfo.setRobotId(robotInfo.getOriginId());
            homeDeviceInfo.setRobotCode(robotInfo.getRobotCode());
            homeDeviceInfo.setState(robotState);
            homeDeviceInfo.setRobotType(robotInfo.getRobotTypeName());
            homeDeviceInfoList.add(homeDeviceInfo);
        }

        return homeDeviceInfoList;
    }

    public List<TStdRegion> getEnvRegion(){
        return tStdRegionDao.getEnvRegion();
    }

    public List<IotDeviceDataEx> getEnvByRegion(Long regionId){
        List<IotDeviceDataEx> deviceDataExList = tStdRegionDao.getEnvByRegion(regionId);
        deviceDataExList.forEach(data ->{
            if (data.getChannelNum() != null){
                String key = Constant.envKey+regionId;
                Map<String,String> map = redisTemplate.opsForHash().entries(key);
                String value = map.get(data.getIotDeviceId()+":"+data.getChannelNum());
                data.setValue(value);
            }

        });
        return deviceDataExList;
    }
}
