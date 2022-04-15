package com.yjh.platform.module.task.service;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.StatisticsDao;
import com.yjh.platform.module.user.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhou Pengcheng
 * @since 2022-04-13
 */
@Service
@Slf4j
public class StatisticsService {
  @Autowired private StatisticsDao statisticsDao;

  public List<Map<String, Object>> selectStatisticsRobot(Long robotId) {

    String robotStatus = null;
    Long lastOnlineTime = null;
    Long duration = null;
    Long offLineCount = null;
    Number runDay = null;
    Number taskDay = null;
    List<Map<String, Object>> list = statisticsDao.selectStatisticsRobot(robotId);
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
          map.put("duration", totalHour + "小时");
        } else {
          map.put("duration", 0);
        }
        // 在线状态
        map.put("robotStatus", robotStatus);
        // 上次在线时间
        map.put(
            "lastOnlineTime",
            lastOnlineTime == null ? null : DateTimeUtil.getDateByLong(lastOnlineTime));
        // 离线次数
        map.put("offLineCount", offLineCount);
        // 出勤率投运期间累计正常巡检天数/总投运天数
        runDay = map.get("runDay") != null ? (Number) map.get("runDay") : 0;
        taskDay = map.get("taskDay") != null ? (Number) map.get("taskDay") : 0;
        String cruiseAttend = taskDay.intValue() == 0 ? "N/A" : numberCover(taskDay.intValue(), runDay.intValue());
        map.put("cruiseAttend", cruiseAttend);
        countTaskAttend((Long) map.get("robotId"), (Date) map.get("commissionDate"),new Date());
      } else {
        log.error("查询不到robotId={}的机器人信息", robotId);
      }
    }
    return list;
  }

  private String numberCover(Integer num1, Integer num2) {

    // 创建一个数值格式化对象
    NumberFormat numberFormat = NumberFormat.getInstance();
    // 设置精确到小数点后2位
    numberFormat.setMaximumFractionDigits(2);
    return numberFormat.format((float) num1 / (float) num2 * 100) + "%";
  }

  private HashMap<String, Object> dealCount(HashMap<String, Object> countMap) {
    Double totalNum = Double.valueOf(countMap.get("totalNum").toString());
    Double validNum = Double.valueOf(countMap.get("validNum").toString());
    String percent = String.format("%.3f", validNum * 100 / totalNum);
    HashMap<String, Object> reMap = new HashMap<>();
    reMap.put("total_num", totalNum);
    reMap.put("valid_num", validNum);
    reMap.put("percent", percent + "%");
    return reMap;
  }

  /**
   * 巡检出勤率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countTaskAttend(Long robotId, Date startTime, Date endTime) {

    return dealCount(statisticsDao.countTaskAttend(robotId, startTime, endTime));
  }
  /**
   * 巡视点位漏检率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countInstanceLoss(String taskId, Date startTime, Date endTime) {
    return dealCount(statisticsDao.countInstanceLoss(taskId, startTime, endTime));
  }
  /**
   * 人工审核完成率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countWarnCheck(Date startTime, Date endTime) {
    return dealCount(statisticsDao.countWarnCheck(startTime, endTime));
  }
  /**
   * 巡视告警准确率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countWarnAccuracy(Date startTime, Date endTime) {
    return dealCount(statisticsDao.countWarnAccuracy(startTime, endTime));
  }
  /**
   * 巡视结果人工审核完成率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countResultCheck(Date startTime, Date endTime) {
    return dealCount(statisticsDao.countResultCheck(startTime, endTime));
  }
}
