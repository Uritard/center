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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.NumberFormat;
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
    @Autowired
    private StatisticsDao statisticsDao;

    @Autowired
    private UPatrolDeviceStaticsDao uPatrolDeviceStaticsDao;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static final String ROBOT = "robot";
    public static final String DRONE = "drone";

    public static final String CAMERA = "CAMERA";

    private Result getNVRInfo(Long recordId) {
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
        Map<String, Object> dateMap = new HashMap<>();
        dateMap.put("startTime", start);
        dateMap.put("endTime", end);
        if (type == 2) {
            calendar = Calendar.getInstance();
            dateMap.put("startWeek", startWeek);
            dateMap.put("endTime", calendar.getTime());
            dateMap.put("endWeek", calendar.get(Calendar.WEEK_OF_YEAR));
        }
        return dateMap;
    }

    private static List<String> getMonths(Date start, Date end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        List<String> result = new ArrayList<String>();
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

    public List<Map<String, Object>> selectStatisticsRobot(Long robotId, String type, int pageNum, int pageSize) {
        List<Map<String, Object>> list = null;
        if (Constant.isUpSystem()) {
            // 当前系统是上级系统,从数据中获取数据
            list = uPatrolDeviceStaticsDao.selectStatisticsRobot(robotId,ROBOT.equals(type) ? "0":"1");
        } else {
            list = packageInfo(type,robotId, pageNum, pageSize);
        }
/*        Long duration = null;
        String robotStatus = null;
        Long lastOnlineTime = null;
        Long offLineCount = null;
        Number commissionDay = null;
        Number cruiseDay = null;
        Number normalDay = null;
        List<Map<String, Object>> maps = statisticsDao.selectNormalDayRobot(robotId);
        for (Map<String, Object> map : list) {
            List<TRobotInfo> tRobotInfoList =
                    statisticsDao.selectByPage(new TRobotInfo().setRobotId((Long) map.get("robotId")));
            if (CollectionUtils.isEmpty(tRobotInfoList)) {
                log.error("查询不到robotId={}的机器人信息", robotId);
                continue;
            }
            robotStatus = tRobotInfoList.get(0).getRobotStatus();
            lastOnlineTime = tRobotInfoList.get(0).getLastOnlineTime();
            duration = tRobotInfoList.get(0).getDuration();
            offLineCount = tRobotInfoList.get(0).getOffLineCount();
            // 在线总时长
            if (duration != null) {
                long totalHour = duration / 1000 / 60 / 60;
                map.put("duration", totalHour);
            } else {
                map.put("duration", 0);
            }
            // 在线状态
            map.put("robotStatus", robotStatus);
            // 上次在线时间
            map.put("lastOnlineTime", lastOnlineTime);
            // 离线次数
            map.put("offLineCount", offLineCount);
            // 出勤率 投运期间累计正常巡检天数/总投运天数
            commissionDay =
                    Integer.parseInt(StringUtils.isEmpty(map.get(
                            "commissionDays").toString()) ? "0" : map.get("commissionDays").toString()) + 1;
            map.put("normalDay", 0);
            maps.forEach(data -> {
                if (data.get("robotId").equals(map.get("robotId"))) {
                    map.put("normalDay", data.get("normalDay"));
                }
            });
            normalDay =
                    Integer.parseInt(StringUtils.isEmpty(map.get(
                            "commissionDays").toString()) ? "0" : map.get("commissionDays").toString()) - Integer.parseInt(StringUtils.isEmpty(map.get(
                            "normalDay").toString()) ? "0" : map.get("normalDay").toString()) + 1;

            map.put("normalDay", normalDay);

            cruiseDay = (Number) map.getOrDefault("cruiseDay", 0);
            String cruiseAttend = commissionDay.intValue() == 0
                    ? "N/A" : numberCover(cruiseDay.intValue(), commissionDay.intValue());
            map.put("cruisePercent", cruiseAttend);
            robotId = (Long) map.get("robotId");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String beginDate = dateFormat.format(map.get("commissionDate"));

            HashMap<String, Object> percentMap =
                    countInstanceLoss(
                            null, robotId, beginDate, DateTimeUtil.getDateByLong(System.currentTimeMillis()));
            if (percentMap.size() > 0 && percentMap.get("percent") != null) {
                map.put("lossPercent", percentMap.get("percent"));
            }
            Map<String, String> cacheMap = Maps.newHashMap();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                cacheMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            redisTemplate.opsForHash().putAll("deviceStaticsInfo:robotId:" + map.get("robotId"), cacheMap);
        }*/
        return list;
    }

    /**
     * 巡视点位漏检率
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public HashMap<String, Object> countInstanceLoss(
            String taskId, Long robotId, String startTime, String endTime) {
        return dealCount(statisticsDao.countInstanceLoss(taskId, robotId, startTime, endTime));
    }

    private HashMap<String, Object> dealCount(HashMap<String, Object> countMap) {
        HashMap<String, Object> reMap = new HashMap<>();
        if (countMap.get("totalNum") != null && countMap.get("validNum") != null) {
            double totalNum = Double.parseDouble(countMap.get("totalNum").toString());
            double validNum = Double.parseDouble(countMap.get("validNum").toString());
            String percent = String.format("%.3f", validNum * 100 / totalNum);
            reMap.put("total_num", totalNum);
            reMap.put("valid_num", validNum);
            reMap.put("percent", percent + "%");
        }
        return reMap;
    }

    /**
     * 摄像机 巡检率，漏检率，巡检天数
     *
     * @return
     */
    public List<Map<String, Object>> countCamera(Long id, int pageNum, int pageSize) {
        List<Map<String, Object>> list = null;
        if (Constant.isUpSystem()) {
            // 当前系统是上级系统,从数据中获取数据
            list = uPatrolDeviceStaticsDao.selectStatisticsRobot(id,"2");
        } else {
            list = packageInfo(CAMERA, id, pageNum, pageSize);
        }

        return list;
/*
        List<Map<String, Object>> mapList = statisticsDao.countCamera(id);
        Set<Long> set = new HashSet();
        for (Map<String, Object> map : mapList) {
            double totalNum = Double.parseDouble(map.get("totalNum").toString());
            double lossNum = Double.parseDouble(map.get("lossNum").toString());
            if (totalNum != 0) {
                String lossPercent = String.format("%.2f", lossNum * 100 / totalNum);
                map.put("lossPercent", lossPercent + "%");
            }

            double commissionDay = Double.parseDouble(map.get("commissionDay").toString());
            double cruiseDay = Double.parseDouble(map.get("cruiseDay").toString());
            if (commissionDay != 0) {
                String cruisePercent = String.format("%.2f", cruiseDay * 100 / commissionDay);
                map.put("cruisePercent", cruisePercent + "%");
            }

            // 根据cameraId查询recordId，查询摄像机完整率
            Long cameraId = (Long) map.get("cameraId");
            Long recordId = statisticsDao.selectRecordByCamera(cameraId);
            map.put("recordId", recordId);
            if (recordId == null) {
                log.error("相机cameraId={}无对应的录像机", cameraId);
                continue;
            }
            set.add(recordId);
        }
        Map<Integer, String> percentMap = new HashMap<>(8);
        for (Long recordId : set) {
            Result re = getNVRInfo(recordId);
            if (re == null || re.getData() == null) {
                continue;
            }
            Map<String, Object> mapData = (Map<String, Object>) re.getData();

            List<Map<String, Object>> chanInfo = new ArrayList<>();
            if (mapData.get("channel") != null) {
                chanInfo = (List<Map<String, Object>>) mapData.get("channel");
                for (Map<String, Object> mapChannel : chanInfo) {
                    int intactTime = (int) mapChannel.get("intactTime");
                    int ipChanNum = (int) mapChannel.get("ipChanNum");
                    if (intactTime != 0) {
                        String intactPercent = String.format("%.2f", intactTime / 100d);
                        percentMap.put(ipChanNum, intactPercent + "%");
                    }
                }
            }
        }
        if (percentMap.size() > 0) {
            for (Map<String, Object> map : mapList) {
                int ipChanNum = (int) map.get("channelNum");
                map.put("intactPercent", percentMap.get(ipChanNum));
            }
        }
        mapList.forEach(map -> {
                    Map<String, String> tempMap = Maps.newHashMap();
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        tempMap.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                    redisTemplate.opsForHash().putAll("deviceStaticsInfo:cameraId:" + map.get("cameraId"), tempMap);
                }
        );
        return mapList;
*/
    }

    /**
     * 巡视任务闭环率
     *
     * @return
     */
    public List<Statistics> countTask(String robotCode, Integer type, Integer year, Integer month) {

        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");

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
     * @return
     */
    public List<Statistics> countInstanceLoss(String regionCode,Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
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
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
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
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
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
     * @return
     */
    public List<Statistics> countWarnCheck(String regionCode,Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
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
     * @return
     */
    public List<Statistics> countWarnAccuracy(String regionCode,Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
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
     * @return
     */
    public List<Statistics> countResultCheck(String regionCode,Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
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
        List<String> result = new ArrayList<String>();
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

    private String numberCover(Integer num1, Integer num2) {

        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format((float) num1 / (float) num2 * 100) + "%";
    }

    private List<Map<String, Object>> packageInfo(String key, Long id, int pageNum, int pageSize) {
        int startIndex = pageSize * (pageNum - 1);
        int endIndex = pageSize;

        List<Map<String, Object>> list = new ArrayList<>();
        log.info("开始时间：{},{}", DateTimeUtil.format(new Date()), System.currentTimeMillis());
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
                List<Map<String, Object>> deviceStaticsInfoList = uPatrolDeviceStaticsDao.selectRobotStaticsInfo(startIndex, endIndex,idList);
                for (Map<String, Object> deviceStaticsInfo : deviceStaticsInfoList){
                    String robotId = String.valueOf(deviceStaticsInfo.get("robotId"));

                    // 正常巡检天数 & 巡检出勤率
                    Map<String, Object> result = uPatrolDeviceStaticsDao.selectCommissionDays(NumberUtils.toLong(robotId));
                    deviceStaticsInfo.put("cruiseDay", result.getOrDefault("cruise_day", 0));
                    deviceStaticsInfo.put("cruisePercent", result.get("cruise_rate") == null ? 0 : result.get("cruise_rate") + "%");

                    deviceStaticsInfo.replaceAll((k, v) -> String.valueOf(v));
                    redisTemplate.opsForHash().putAll("deviceStaticsInfo:robotId:" + robotId, deviceStaticsInfo);
                }
                list.addAll(deviceStaticsInfoList);
            }
            log.info("结束时间：{},{}", DateTimeUtil.format(new Date()), System.currentTimeMillis());


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
        List<Map<String, Object>> mapList = statisticsDao.countCamera(id, startIndex, endIndex);

        Set<Long> set = new HashSet();
        for (Map<String, Object> map : mapList) {
            // 巡检出勤率
            double cruiseDay = NumberUtils.toDouble(map.get("cruiseDay").toString());
            double commissionDay = NumberUtils.toDouble(map.get("commissionDay").toString());
            if (commissionDay != 0) {
                String cruisePercent = String.format("%.3f", cruiseDay * 100 / commissionDay);
                map.put("cruisePercent", cruisePercent + "%");
            }

            // 漏检率
            double totalNum = NumberUtils.toDouble(map.get("totalNum").toString());
            double lossNum = NumberUtils.toDouble(map.get("lossNum").toString());
            if (totalNum != 0) {
                String lossPercent = String.format("%.3f", lossNum * 100 / totalNum);
                map.put("lossPercent", lossPercent + "%");
            }

            Long recordId = NumberUtils.toLong(map.get("recordId").toString());
            map.put("recordId", recordId);
            if (0 == recordId) {
                log.error("存在无对应的录像机");
                continue;
            }
            set.add(recordId);
        }
        // 下面的太ex了后面再优化
        Map<Integer, String> percentMap = new HashMap<>(8);
        for (Long recordId : set) {
            Result re = getNVRInfo(recordId);
            if (re == null || re.getData() == null) {
                continue;
            }
            Map<String, Object> mapData = (Map<String, Object>) re.getData();

            List<Map<String, Object>> chanInfo = new ArrayList<>();
            if (mapData.get("channel") != null) {
                chanInfo = (List<Map<String, Object>>) mapData.get("channel");
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
        if (percentMap.size() > 0) {
            for (Map<String, Object> map : mapList) {
                int ipChanNum = (int) map.get("channelNum");
                map.put("intactPercent", percentMap.get(ipChanNum));
            }
        }
        mapList.forEach(map -> {
                    Map<String, String> tempMap = Maps.newHashMap();
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        tempMap.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                    redisTemplate.opsForHash().putAll("deviceStaticsInfo:cameraId:" + map.get("cameraId"), tempMap);
                }
        );
        return mapList;
    }



    /**
     * 巡视任务执行次数
     *
     * @param type
     * @param year
     * @param month
     * @return
     */
    public List<Statistics> countTaskFrequency(Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
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
     * @param type
     * @param year
     * @param month
     * @return
     */
    public List<Statistics> countTaskExecutedDuration(Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
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
            Integer hourData = originDuration / 60;
            Integer minData = originDuration % 60;
            statistics.setDuration(hourData + "小时" + minData + "分钟");
        }
    }

    /**
     * 统计识别缺陷类型与数量
     * @param type
     * @param year
     * @param month
     * @return
     */
    public List<Statistics> countDefectsOfTask(Integer type, Integer year, Integer month) {
        Map<String, Object> objectMap = getDateByMonth(type, year, month);
        Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
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
        if(defectResult == null |defectResult.size()==0){
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
        if(defectInfos != null && defectInfos.size()>0){
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

        Set<String> instanceKey = redisTemplate.keys(stringBuffer.toString() + "*");
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
                }
                statistics.add(statistics1);
            });
        }
        if (type == 2){
            statistics.sort(Comparator.comparing(Statistics::getWeek));
        }
        return statistics;

    }

}
