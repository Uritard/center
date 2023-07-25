package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.patrol.dao.UPatrolDeviceStaticsDao;
import com.yjh.platform.module.task.dao.StatisticsDao;
import com.yjh.platform.module.task.entity.ExportedStatisticsTableVo;
import com.yjh.platform.module.task.entity.StatisticalDefectMapping;
import com.yjh.platform.module.task.entity.Statistics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhou Pengcheng
 * @since 2022-04-13
 */
@Service
@Slf4j
public class StatisticsService {
    private final StatisticsDao statisticsDao;
    private final UPatrolDeviceStaticsDao uPatrolDeviceStaticsDao;
    private final RedisTemplate<String, Object> redisTemplate;

    public static final String ROBOT = "robot";
    public static final String DRONE = "drone";
    public static final String CAMERA = "CAMERA";

    public StatisticsService(StatisticsDao statisticsDao, UPatrolDeviceStaticsDao uPatrolDeviceStaticsDao, RedisTemplate<String, Object> redisTemplate) {
        this.statisticsDao = statisticsDao;
        this.uPatrolDeviceStaticsDao = uPatrolDeviceStaticsDao;
        this.redisTemplate = redisTemplate;
    }

    private Result getNVRInfo(Object recordId) {
        Result re = new Result();
        try {
            String entries = (String) redisTemplate.opsForValue().get("recorderInfo:" + recordId);
            if (!StringUtils.isEmpty(entries)) {
                re.setData(JSON.parse(entries));
                return re;
            }
            log.info("缓存中未获取到数据");
            ServiceRestTemplate serviceRestTemplate =
                    SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re = serviceRestTemplate.getForObject(Constant.NVR_URL, Result.class, recordId);
            }
        } catch (Exception e) {
            log.error("获取录像机数据发生异常{}", e.getMessage());
        }
        return re;
    }

    /**
     * type =1,获取月开始结束时间<br>
     * type =2,获取年到当前时间的开始结束时间<br>
     * type =3 ,获取年开始结束时间<br>
     */
    public static Map<String, Object> getDateByMonth(int type, int year, int month) {
        // 获取当前分区的日历信息
        Calendar calendar = Calendar.getInstance();
        // 设置年
        calendar.set(Calendar.YEAR, year);

        if (type == 1) {
            // 设置月，月份从0开始
            calendar.set(Calendar.MONTH, month - 1);
        } else {
            calendar.set(Calendar.MONTH, 0);
        }
        // 设置为指定月的第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        // 将小时至0
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        // 将分钟至0
        calendar.set(Calendar.MINUTE, 0);
        // 将秒至0
        calendar.set(Calendar.SECOND, 0);
        // 将毫秒至0
        calendar.set(Calendar.MILLISECOND, 0);
        // 获取指定月第一天的时间
        Date start = calendar.getTime();
        int startWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        // 设置日历天数为当前月实际天数的最大值，即指定月份的最后一天
        if (type == 3) {
            calendar.set(Calendar.MONTH, 11);
        }
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        // 将小时至23
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        // 将分钟至59
        calendar.set(Calendar.MINUTE, 59);
        // 将秒至59
        calendar.set(Calendar.SECOND, 59);
        // 将毫秒至999
        calendar.set(Calendar.MILLISECOND, 999);
        // 获取最后一天的时间
        Date end = calendar.getTime();
        Map<String, Object> dateMap = new HashMap<>(8);
        dateMap.put("startTime", start);
        dateMap.put("endTime", end);
        if (type == 2) {
            calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            dateMap.put("startWeek", startWeek);
            dateMap.put("endTime", calendar.getTime());
            dateMap.put("endWeek", calendar.get(Calendar.WEEK_OF_YEAR));
        }
        return dateMap;
    }

    private static List<String> getMonths(Date start, Date end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        List<String> result = new ArrayList<>();
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(start);
        tempStart.add(Calendar.DATE, 0);

        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(end);
        while (tempStart.before(tempEnd)) {
            result.add(sdf.format(tempStart.getTime()));
            tempStart.add(Calendar.MONTH, +1);
        }
        return result;
    }

    public List<Map<String, Object>> selectStatisticsRobot(Long robotId, String type) {
        List<Map<String, Object>> list;
        if (Constant.isUpSystem()) {
            // 当前系统是上级系统,从数据中获取数据
            list = uPatrolDeviceStaticsDao.selectStatisticsRobot(robotId,ROBOT.equals(type) ? "0":"1");
        } else {
            list = packageInfo(type,robotId);
        }
        return list;
    }

    /**
     * 摄像机 巡检率，漏检率，巡检天数
     *
     * @return List<Map<String, Object>>
     */
    public List<Map<String, Object>> countCamera(Long id) {
        List<Map<String, Object>> list;
        if (Constant.isUpSystem()) {
            // 当前系统是上级系统,从数据中获取数据
            list = uPatrolDeviceStaticsDao.selectStatisticsRobot(id,"2");
        } else {
            list = packageInfo(CAMERA, id);
        }

        return list;
    }

    /**
     * 巡视任务闭环率
     *
     * @return List<Statistics>
     */
    public List<Statistics> countTask(String robotCode, Integer type, Integer year, Integer month) {

        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");

        List<Statistics> result = new ArrayList<>();
        //处理上级系统逻辑
        String systemLevel  = Constant.getLevelEdge();
        log.info("systemLevel:{}",systemLevel);
        if ("3".equals(systemLevel)) {
            return cruiseStatistics(robotCode,year,month,type,Constant.TASK_CHECK,startTime,endTime);
        }
        switch (type) {
            case 1:
                result = statisticsDao.countTaskByDay(startTime, endTime);
                dealDay(result, objectMap);
                break;
            case 2:
                result = statisticsDao.countTaskByWeek(startTime, endTime);
                dealWeek(result, objectMap);
                break;
            case 3:
                result = statisticsDao.countTaskByMonth(startTime, endTime);
                dealMonth(result, objectMap);
                break;
            default:
                break;
        }
        for (Statistics st : result) {
            dealPercent(st);
        }

        return result;
    }

    /**
     * 巡视点位漏检率
     *
     * @return List<Statistics>
     */
    public List<Statistics> countInstanceLoss(String regionCode,Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");
        List<Statistics> result = new ArrayList<>();
        //处理上级系统逻辑
        String systemLevel  = Constant.getLevelEdge();
        log.info("systemLevel:{}",systemLevel);
        if ("3".equals(systemLevel)) {
            return cruiseStatistics(regionCode,year,month,type,Constant.INSTANCE_LOSS,startTime,endTime);
        }
        switch (type) {
            case 1:
                result = statisticsDao.countInstanceLossByDay(startTime, endTime);
                dealDay(result, objectMap);
                break;
            case 2:
                result = statisticsDao.countInstanceLossByWeek(startTime, endTime);
                dealWeek(result, objectMap);
                break;
            case 3:
                result = statisticsDao.countInstanceLossByMonth(startTime, endTime);
                dealMonth(result, objectMap);
                break;
            default:
                break;
        }
        for (Statistics st : result) {
            dealPercent(st);
        }

        return result;
    }

    private void dealDay(List<Statistics> result, Map<String, Object> objectMap) {
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");
        List<String> dateList = getBetweenDates(startTime, endTime);
        for (String date : dateList) {
            boolean b = result.stream().anyMatch(m -> m.getDay().equals(date));
            if (!b) {
                Statistics st = new Statistics();
                st.setDay(date);
                result.add(st);
            }
        }
        result.sort(Comparator.comparing(Statistics::getDay));
    }

    private void dealWeek(List<Statistics> result, Map<String, Object> objectMap) {
        Integer startWeek = (Integer) objectMap.get("startWeek");
        Integer endWeek = (Integer) objectMap.get("endWeek");
        if (result.size() + 1 < (endWeek - startWeek)) {
            for (int i = startWeek; i <= endWeek; i++) {
                int f1 = i;
                boolean b = result.stream().anyMatch(m -> m.getWeek().equals(f1));
                if (!b) {
                    Statistics st = new Statistics();
                    st.setWeek(i);
                    result.add(st);
                }
            }
        }
        result.sort(Comparator.comparing(Statistics::getWeek));
    }

    private void dealMonth(List<Statistics> result, Map<String, Object> objectMap) {
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");
        List<String> getMonths = getMonths(startTime, endTime);
        for (String date : getMonths) {
            boolean b = result.stream().anyMatch(m -> m.getMonth().equals(date));
            if (!b) {
                Statistics st = new Statistics();
                st.setMonth(date);
                result.add(st);
            }
        }
        result.sort(Comparator.comparing(Statistics::getMonth));
    }

    /**
     * 人工审核完成率
     *
     * @param type  类型 1:日历 2:周历 3:月历
     * @param month 月份
     * @return List<Statistics>
     */
    public List<Statistics> countWarnCheck(String regionCode,Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");
        List<Statistics> result = new ArrayList<>();
        //处理上级系统逻辑
        String systemLevel  = Constant.getLevelEdge();
        log.info("systemLevel:{}",systemLevel);
        if ("3".equals(systemLevel)) {
            return cruiseStatistics(regionCode,year,month,type,Constant.WARN_CHECK,startTime,endTime);
        }
        switch (type) {
            case 1:
                result = statisticsDao.countWarnCheckByDay(startTime, endTime);
                dealDay(result, objectMap);
                break;
            case 2:
                result = statisticsDao.countWarnCheckByWeek(startTime, endTime);
                dealWeek(result, objectMap);
                break;
            case 3:
                result = statisticsDao.countWarnCheckByMonth(startTime, endTime);
                dealMonth(result, objectMap);
                break;
            default:
                break;
        }
        for (Statistics st : result) {
            dealPercent(st);
        }

        return result;
    }

    /**
     * 巡视告警准确率
     *
     * @return List<Statistics>
     */
    public List<Statistics> countWarnAccuracy(String regionCode,Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");
        List<Statistics> result = new ArrayList<>();
        //处理上级系统逻辑
        String systemLevel  = Constant.getLevelEdge();
        log.info("systemLevel:{}",systemLevel);
        if ("3".equals(systemLevel)) {
            return cruiseStatistics(regionCode,year,month,type,Constant.WARN_ACCURACY,startTime,endTime);
        }
        switch (type) {
            case 1:
                result = statisticsDao.countWarnAccuracyByDay(startTime, endTime);
                dealDay(result, objectMap);
                break;
            case 2:
                result = statisticsDao.countWarnAccuracyByWeek(startTime, endTime);
                dealWeek(result, objectMap);
                break;
            case 3:
                result = statisticsDao.countWarnAccuracyByMonth(startTime, endTime);
                dealMonth(result, objectMap);
                break;
            default:
                break;
        }
        for (Statistics st : result) {
            dealPercent(st);
        }

        return result;
    }

    /**
     * 巡视结果人工审核完成率
     *
     * @return List<Statistics>
     */
    public List<Statistics> countResultCheck(String regionCode,Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");
        List<Statistics> result = new ArrayList<>();
        //处理上级系统逻辑
        String systemLevel  = Constant.getLevelEdge();
        log.info("systemLevel:{}",systemLevel);
        if ("3".equals(systemLevel)) {
            return cruiseStatistics(regionCode,year,month,type,Constant.RESULT_CHECK,startTime,endTime);
        }
        switch (type) {
            case 1:
                result = statisticsDao.countResultCheckByDay(startTime, endTime);
                dealDay(result, objectMap);
                break;
            case 2:
                result = statisticsDao.countResultCheckByWeek(startTime, endTime);
                dealWeek(result, objectMap);
                break;
            case 3:
                result = statisticsDao.countResultCheckByMonth(startTime, endTime);
                dealMonth(result, objectMap);
                break;
            default:
                break;
        }
        for (Statistics st : result) {
            dealPercent(st);
        }

        return result;
    }

    private List<String> getBetweenDates(Date start, Date end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<String> result = new ArrayList<>();
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(start);
        tempStart.add(Calendar.DATE, 0);

        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(end);
        while (tempStart.before(tempEnd)) {
            result.add(sdf.format(tempStart.getTime()));
            tempStart.add(Calendar.DAY_OF_YEAR, +1);
        }
        return result;
    }

    private void dealPercent(Statistics statistics) {
        if (statistics.getTotalNum() != null) {
            Double totalNum = Double.valueOf(statistics.getTotalNum());
            Double validNum = Double.valueOf(statistics.getValidNum());
            String percent = String.format("%.3f", validNum * 100 / totalNum);
            statistics.setPercent(percent + "%");
        }
    }

    private List<Map<String, Object>> packageInfo(String key, Long id) {
        List<Map<String, Object>> list = new ArrayList<>();
        log.info("处理开始时间：{},{}", DateTimeUtil.format(new Date()), System.currentTimeMillis());
        // 机器人无人机处理
        if (ROBOT.equals(key) || DRONE.equals(key)) {
            List<Long> idList;
            if (id != null) {
                idList = new ArrayList<>();
                idList.add(id);
            } else {
                idList = statisticsDao.selectAvailableRobotOrDrone(key);
            }

            if (!CollectionUtils.isEmpty(idList)) {
                List<Map<String, Object>> deviceStaticsInfoList = uPatrolDeviceStaticsDao.selectRobotStaticsInfoByOptimize(idList);

                // 将List<Map<String, Object>>转为List<Map<String, String>>
                List<Map<String, String>> deviceStaticsInfoTempList = deviceStaticsInfoList.stream()
                        .map(map -> map.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry :: getKey, entry -> String.valueOf(entry.getValue()))))
                        .collect(Collectors.toList());
                // 批量插入redis
                pipelinePutStaticsInfo(deviceStaticsInfoTempList,"robotId");
                list.addAll(deviceStaticsInfoList);
            }
            log.info("机器人无人机处理结束时间：{},{}", DateTimeUtil.format(new Date()), System.currentTimeMillis());

            // By-zx
            /*if (!CollectionUtils.isEmpty(idList)) {
                idList.forEach(robotId -> {
                    Map<String, Object> deviceStaticsInfo = uPatrolDeviceStaticsDao.selectRobotInfo(robotId);
                    Integer normalDays = uPatrolDeviceStaticsDao.selectNormalDays(deviceStaticsInfo.get("deviceCode").toString());
                    deviceStaticsInfo.put("duration", deviceStaticsInfo.get("duration") == null ?0:deviceStaticsInfo.get("duration").toString() + "分钟");
                    // 正常巡检天数 & 巡检出勤率
                    Map<String, Object> result = uPatrolDeviceStaticsDao.selectCommissionDays(robotId);
                    // 累计连续正常运行天数
                    deviceStaticsInfo.put("normalDay", normalDays == null ? result.get("commission_days"):normalDays);

                    deviceStaticsInfo.put("cruiseDay", result.get("cruise_day") == null ? 0 : result.get("cruise_day"));
                    deviceStaticsInfo.put("cruisePercent", result.get("cruise_rate") == null ? 0 : result.get("cruise_rate").toString() + "%");
                    Map<String, String> cacheMap = Maps.newHashMap();
                    for (Map.Entry<String, Object> entry : deviceStaticsInfo.entrySet()) {
                        cacheMap.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                    redisTemplate.opsForHash().putAll("deviceStaticsInfo:robotId:" + robotId, cacheMap);
                    list.add(deviceStaticsInfo);
                });
            }*/
            return list;
        }
        // 摄像机处理
        List<Map<String, Object>> deviceStaticsInfoList = statisticsDao.selectCameraStaticsInfoByOptimize(id);

        List<Object> recordIdList = deviceStaticsInfoList.stream().map(map -> map.get("recordId")).distinct().collect(Collectors.toList());

        // 计算完整率
        Map<Integer, String> percentMap = new HashMap<>(8);
        for (Object recordId : recordIdList) {
            Result re = getNVRInfo(recordId);
            if (re == null || re.getData() == null) {
                continue;
            }
            Map<String, Object> mapData = (Map<String, Object>) re.getData();

            if (mapData.get("channel") != null) {
                List<Map<String, Object>> chanInfo = (List<Map<String, Object>>) mapData.get("channel");
                for (Map<String, Object> mapChannel : chanInfo) {
                    int intactTime = (int) mapChannel.get("intactTime");
                    int ipChanNum = (int) mapChannel.get("ipChanNum");
                    if (intactTime != 0) {
                        String intactPercent = String.format("%.3f", intactTime / 100d);
                        percentMap.put(ipChanNum, intactPercent + "%");
                    }
                }
            }
        }
        deviceStaticsInfoList.stream().filter(map -> MapUtils.isNotEmpty(percentMap)).forEach(map -> {
            int ipChanNum = (int) map.get("channelNum");
            map.put("intactPercent", percentMap.getOrDefault(ipChanNum, "0.000%"));
        });

        // 将List<Map<String, Object>>转为List<Map<String, String>>
        List<Map<String, String>> deviceStaticsInfoTempList = deviceStaticsInfoList.stream()
                .map(map -> map.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry :: getKey, entry -> String.valueOf(entry.getValue()))))
                .collect(Collectors.toList());
        // 批量插入redis
        pipelinePutStaticsInfo(deviceStaticsInfoTempList,"cameraId");

        log.info("相机处理结束时间：{},{}", DateTimeUtil.format(new Date()), System.currentTimeMillis());
        return deviceStaticsInfoList;
    }


    /**
     * 巡视任务执行次数
     *
     * @param type  类型
     * @param year  年
     * @param month 月
     * @return List<Statistics>
     */
    public List<Statistics> countTaskFrequency(Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");
        List<Statistics> result = new ArrayList<>();
        switch (type) {
            case 1:
                result = statisticsDao.countTaskFrequencyByDay(startTime, endTime);
                dealDay(result, objectMap);
                break;
            case 2:
                result = statisticsDao.countTaskFrequencyByWeek(startTime, endTime);
                dealWeek(result, objectMap);
                break;
            case 3:
                result = statisticsDao.countTaskFrequencyByMonth(startTime, endTime);
                dealMonth(result, objectMap);
                break;
            default:
                break;
        }

        return result;
    }

    /**
     * 统计任务执行时间
     *
     * @param type  类型
     * @param year  年
     * @param month 月
     * @return List<Statistics>
     */
    public List<Statistics> countTaskExecutedDuration(Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");
        List<Statistics> result = new ArrayList<>();
        switch (type) {
            case 1:
                result = statisticsDao.countTaskExecutedDurationByDay(startTime, endTime);
                dealDay(result, objectMap);
                break;
            case 2:
                result = statisticsDao.countTaskExecutedDurationByWeek(startTime, endTime);
                dealWeek(result, objectMap);
                break;
            case 3:
                result = statisticsDao.countTaskExecutedDurationByMonth(startTime, endTime);
                dealMonth(result, objectMap);
                break;
            default:
                break;
        }

        for (Statistics item : result) {
            formatDuration(item);
        }
        return result;
    }

    private void formatDuration(Statistics statistics) {
        Integer originDuration = statistics.getValidNum();
        if (originDuration != null && originDuration > 0) {
            int hourData = originDuration / 60;
            int minData = originDuration % 60;
            statistics.setDuration(hourData + "小时" + minData + "分钟");
        }
    }

    /**
     * 统计识别缺陷类型与数量
     *
     * @param type  类型
     * @param year  年
     * @param month 月
     * @return List<Statistics>
     */
    public List<Statistics> countDefectsOfTask(Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime");
        Date endTime = (Date) objectMap.get("endTime");
        List<Statistics> result = new ArrayList<>();
        switch (type) {
            case 1:
                result = dealDefect(statisticsDao.countFoundDefectByDay(startTime, endTime));
                dealDay(result, objectMap);
                break;
            case 2:
                result =dealDefect(statisticsDao.countFoundDefectByWeek(startTime, endTime));
                dealWeek(result, objectMap);
                break;
            case 3:
                result = dealDefect(statisticsDao.countFoundDefectByMonth(startTime, endTime));
                dealMonth(result, objectMap);
                break;
            default:
                break;
        }
        return result;
    }

    private List<Statistics> dealDefect(List<StatisticalDefectMapping> defectResult) {
        List<Statistics> result = new ArrayList<>();
        if(defectResult.isEmpty()){
            return result;
        }
        if(defectResult.iterator().next().getDay() != null){
            Map<String,List<StatisticalDefectMapping>> dayMap = defectResult.stream()
                    .collect(Collectors.groupingBy(StatisticalDefectMapping::getDay));
            dayMap.forEach((date,defectInfos)->{
                Statistics item = new Statistics();
                item.setDay(date);
                item.setDefectMappings(defectInfos);
                item.setTotalNum(defectInfos.size());
                result.add(item);
            });
        }else if (defectResult.iterator().next().getWeek() != null){
            Map<Integer,List<StatisticalDefectMapping>> weekMap = defectResult.stream()
                    .collect(Collectors.groupingBy(StatisticalDefectMapping::getWeek));
            weekMap.forEach((date,defectInfos)->{
                Statistics item = new Statistics();
                item.setWeek(date);
                item.setDefectMappings(defectInfos);
                item.setTotalNum(defectInfos.size());
                result.add(item);
            });
        }else {
            Map<String,List<StatisticalDefectMapping>> monthMap = defectResult.stream()
                    .collect(Collectors.groupingBy(StatisticalDefectMapping::getMonth));
            monthMap.forEach((date,defectInfos)->{
                Statistics item = new Statistics();
                item.setMonth(date);
                item.setDefectMappings(defectInfos);
                item.setTotalNum(defectInfos.size());
                result.add(item);
            });
        }
        return result;
    }

    public List<ExportedStatisticsTableVo> exportTable(Integer type, Integer year, Integer month) {

        List<ExportedStatisticsTableVo> tableElements = new ArrayList<>();
        List<Statistics> resultOfFrequency = countTaskFrequency(type, year, month);
        List<Statistics> resultOfDuration = countTaskExecutedDuration(type, year, month);
        List<Statistics> resultOfDefect = countDefectsOfTask(type, year, month);

        for(int offset = 0; offset<resultOfFrequency.size(); offset++){
            ExportedStatisticsTableVo item = new ExportedStatisticsTableVo();
            Statistics frequency = resultOfFrequency.get(offset);
            Statistics duration = resultOfDuration.get(offset);
            Statistics defect = resultOfDefect.get(offset);

            item.setFrequency(frequency.getValidNum());
            item.setDuration(duration.getDuration());
            createDefectInfo(defect.getDefectMappings(),item);
            item.setDefectMappings(defect.getDefectMappings());
            if(frequency.getDay() != null){
                item.setDate(frequency.getDay());
            }else if(frequency.getWeek() != null){
                item.setDate(frequency.getWeek().toString());
            }else {
                item.setDate(frequency.getMonth());
            }
            tableElements.add(item);
        }

        return tableElements;
    }

    private void createDefectInfo(List<StatisticalDefectMapping> defectInfos, ExportedStatisticsTableVo vo){
        if(!CollectionUtils.isEmpty(defectInfos)){
            for (StatisticalDefectMapping defectItem:defectInfos){
                if(vo.getDefectAndCount()==null){
                    vo.setDefectAndCount(defectItem.getDefectType()+":"+defectItem.getCount()+"个 ");
                }else {
                    vo.setDefectAndCount(vo.getDefectAndCount()+defectItem.getDefectType()+":"+defectItem.getCount()+"个 ");
                }
            }
        }
    }


    private List<Statistics> cruiseStatistics(String robotCode, Integer year, Integer month, Integer type, Integer command, Date beginDate, Date endDate) {
        Map<String, String> map = Maps.newHashMap();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        map.put("robotCode", robotCode);
        map.put("type", String.valueOf(type));
        map.put("command", String.valueOf(command));
        map.put("year", String.valueOf(year));
        map.put("month", String.valueOf(month));
        map.put("beginDate", simpleDateFormat.format(beginDate));
        map.put("endDate", simpleDateFormat.format(endDate));
        Constant.otherServerMap(map, Constant.STATISTIC);
        List<Statistics> statistics = new ArrayList<>();
        //取缓存数据
        StringBuffer stringBuffer = new StringBuffer("statisticForUpSystem:");
        stringBuffer.append(robotCode);
        stringBuffer.append("_");
        stringBuffer.append(command);
        stringBuffer.append(type);
        stringBuffer.append(":");
        stringBuffer.append(year);
        stringBuffer.append("_");
        stringBuffer.append(month);
        stringBuffer.append(":");

        Set<String> instanceKey = redisTemplate.keys(stringBuffer + "*");
        if (!CollectionUtils.isEmpty(instanceKey)) {
            TreeSet<String> sortedSet = new TreeSet<>(instanceKey);

            sortedSet.forEach(instance -> {
                Map<Object, Object> resultMap = redisTemplate.opsForHash().entries(instance);
                Statistics statistics1 = new Statistics();
                statistics1.setTotalNum(resultMap.get("totalNum") == null ? null : Integer.valueOf(resultMap.get("totalNum").toString()));
                statistics1.setValidNum(resultMap.get("validNum") == null ? null : Integer.valueOf(resultMap.get("validNum").toString()));
                statistics1.setPercent(resultMap.get("percent") == null ? null : resultMap.get("percent").toString());
                switch (type) {
                    case 1:
                        statistics1.setDay(resultMap.get("day").toString());
                        break;
                    case 2:
                        statistics1.setWeek(Integer.valueOf(resultMap.get("week").toString()));
                        break;
                    case 3:
                        statistics1.setMonth(resultMap.get("month").toString());
                        break;
                    default:
                        break;
                }
                statistics.add(statistics1);
            });
        }
        if (type == 2){
            statistics.sort(Comparator.comparing(Statistics::getWeek));
        }
        return statistics;
    }

    @Async
    public void pipelinePutStaticsInfo(List<Map<String, String>> storageList, String deviceType) {
        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    storageList.forEach(sm -> putRedis(sm, operations, deviceType));
                    return null;
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void putRedis(Map<String, String> m, RedisOperations operations, String deviceTypeFlag) {
        try {
            if (MapUtils.isEmpty(m)) {
                log.warn("detail map is empty.");
                return;
            }

            String deviceId = m.get(deviceTypeFlag);
            String key = "deviceStaticsInfo:" + deviceTypeFlag + ":" + deviceId;
            operations.opsForHash().putAll(key, m);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
