package com.yjh.platform.module.task.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.StatisticsDao;
import com.yjh.platform.module.task.entity.Statistics;
import com.yjh.platform.module.user.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhou Pengcheng
 * @since 2022-04-13
 */
@Service
@Slf4j
public class StatisticsService {
  @Autowired private StatisticsDao statisticsDao;

  private static Result getNVRInfo(Long recordId) {
    Result re = null;
    try {
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

  public List<Map<String, Object>> selectStatisticsRobot(Long robotId, String type) {

    String robotStatus = null;
    Long lastOnlineTime = null;
    Long duration = null;
    Long offLineCount = null;
    Number commissionDay = null;
    Number cruiseDay = null;
    List<Map<String, Object>> list = null;
    if ("robot".equals(type)) {
      list = statisticsDao.selectStatisticsRobot(robotId);
    } else {
      list = statisticsDao.selectStatisticsDrone(robotId);
    }
    for (Map<String, Object> map : list) {
      List<TRobotInfo> tRobotInfoList =
          statisticsDao.selectByPage(new TRobotInfo().setRobotId((Long) map.get("robotId")));
      if (tRobotInfoList.size() > 0) {
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
        commissionDay = (Number) map.getOrDefault("commissionDay", 0);
        cruiseDay = (Number) map.getOrDefault("cruiseDay", 0);
        String cruiseAttend = commissionDay.intValue() == 0
                ? "N/A" : numberCover(cruiseDay.intValue(), commissionDay.intValue());
        map.put("cruisePercent", cruiseAttend);
        robotId = (Long) map.get("robotId");
        String beginDate = (String) map.get("commissionDate");

        HashMap<String, Object> percentMap =
            countInstanceLoss(
                null, robotId, beginDate, DateTimeUtil.getDateByLong(System.currentTimeMillis()));
        if (percentMap.size() > 0 && percentMap.get("percent") != null) {
          map.put("lossPercent", percentMap.get("percent"));
        }
      } else {
        log.error("查询不到robotId={}的机器人信息", robotId);
      }
    }
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
      String percent = String.format("%.2f", validNum * 100 / totalNum);
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
  public List<Map<String, Object>> countCamera(Long id) {
    List<Map<String, Object>> mapList = statisticsDao.countCamera(id);
    Map<Integer, Long> channelMap = new HashMap<Integer, Long>(16);
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
      Integer channelNum = (Integer) map.get("channelNum");
      Long recordId = statisticsDao.selectRecordByCamera(cameraId);
      map.put("recordId", recordId);
      if (recordId == null) {
        log.error("相机cameraId={}无对应的录像机", cameraId);
        continue;
      }
      channelMap.put(channelNum, recordId);
    }

    Set<Long> set = new HashSet();
    for (Map.Entry map : channelMap.entrySet()) {
      Long recordId = (Long) map.getValue();
      set.add(recordId);
    }
    Map<Integer, String> percentMap = new HashMap<>(8);
    for (Long recordId : set) {
      Result re = getNVRInfo(recordId);
      if (re == null) {
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
    return mapList;
  }

  /**
   * 巡视任务闭环率
   *
   * @return
   */
  public List<Statistics> countTask(Integer type, Integer year, Integer month) {

    Map<String, Object> objectMap = getDateByMonth(type, year, month);
    Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
    List<Statistics> result = new ArrayList<>();
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
  public List<Statistics> countInstanceLoss(Integer type, Integer year, Integer month) {
    Map<String, Object> objectMap = getDateByMonth(type, year, month);
    Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
    List<Statistics> result = new ArrayList<>();

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
    result.sort((t2, t1) -> t2.getDay().compareTo(t1.getDay()));
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
    result.sort((t2, t1) -> t2.getWeek().compareTo(t1.getWeek()));
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
    result.sort((t2, t1) -> t2.getMonth().compareTo(t1.getMonth()));
  }
  /**
   * 人工审核完成率
   *
   * @param type 类型 1:日历 2:周历 3:月历
   * @param month 月份
   * @return
   */
  public List<Statistics> countWarnCheck(Integer type, Integer year, Integer month) {
    Map<String, Object> objectMap = getDateByMonth(type, year, month);
    Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
    List<Statistics> result = new ArrayList<>();
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
  public List<Statistics> countWarnAccuracy(Integer type, Integer year, Integer month) {
    Map<String, Object> objectMap = getDateByMonth(type, year, month);
    Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
    List<Statistics> result = new ArrayList<>();

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
  public List<Statistics> countResultCheck(Integer type, Integer year, Integer month) {
    Map<String, Object> objectMap = getDateByMonth(type, year, month);
    Date startTime = (Date) objectMap.get("startTime"), endTime = (Date) objectMap.get("endTime");
    List<Statistics> result = new ArrayList<>();

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
      String percent = String.format("%.2f", validNum * 100 / totalNum);
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
}
