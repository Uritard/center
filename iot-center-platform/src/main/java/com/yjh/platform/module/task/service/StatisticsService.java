package com.yjh.platform.module.task.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.StatisticsDao;
import com.yjh.platform.module.user.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
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

  public List<Map<String, Object>> selectStatisticsRobot(Long robotId, String type) {

    String robotStatus = null;
    Long lastOnlineTime = null;
    Long duration = null;
    Long offLineCount = null;
    Number commissionDay = null;
    Number taskDay = null;
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
          map.put("duration", totalHour + "小时");
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
        commissionDay = map.get("commissionDay") != null ? (Number) map.get("commissionDay") : 0;
        taskDay = map.get("taskDay") != null ? (Number) map.get("taskDay") : 0;
        String cruiseAttend =
            commissionDay.intValue() == 0
                ? "N/A"
                : numberCover(taskDay.intValue(), commissionDay.intValue());
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
   * 摄像机 巡检率，漏检率，巡检天数
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public List<Map<String, Object>> countCamera(String startTime, String endTime) {
    List<Map<String, Object>> mapList = statisticsDao.countCamera(startTime, endTime);
    for (Map<String, Object> map : mapList) {
      Double totalNum = Double.valueOf(map.get("totalNum").toString());
      Double validNum = Double.valueOf(map.get("validNum").toString());
      String lossPercent = String.format("%.3f", validNum * 100 / totalNum);
      map.put("lossPercent", lossPercent + "%");

      Double allDay = Double.valueOf(map.get("allDay").toString());
      Double cruiseDay = Double.valueOf(map.get("validNum").toString());
      String cruisePercent = String.format("%.3f", cruiseDay * 100 / allDay);
      map.put("cruisePercent", cruisePercent + "%");
      // 根据cameraId查询recordId，查询摄像机完整率
      Long cameraId = (Long) map.get("camera_id");
      Long recordId = statisticsDao.selectRecordByCamera(cameraId);
      map.put("recordId", recordId);
      if (recordId == null) {
        log.error("相机cameraId={}无对应的录像机", cameraId);
        continue;
      }
      Result re = getNVRInfo(recordId);
      if (re == null) {
        continue;
      }
      Map<String, String> mapData = (Map<String, String>) re.getData();
      int intactTime =
          mapData.get("intactTime") == null ? 0 : Integer.parseInt(mapData.get("intactTime"));
      if (intactTime != 0) {
        String intactPercent = String.format("%.3f", intactTime / 100d);
        map.put("intactPercent", intactPercent + "%");
      }
    }
    return mapList;
  }

  /**
   * 巡视任务闭环率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countTask(String startTime, String endTime) {

    return dealCount(statisticsDao.countTask(startTime, endTime));
  }

  /**
   * 巡检出勤率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countTaskAttend(Long robotId, String startTime, String endTime) {

    return dealCount(statisticsDao.countTaskAttend(robotId, startTime, endTime));
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
    return dealCount(statisticsDao.countInstanceLoss(taskId, null, startTime, endTime));
  }
  /**
   * 人工审核完成率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countWarnCheck(String startTime, String endTime) {
    return dealCount(statisticsDao.countWarnCheck(startTime, endTime));
  }
  /**
   * 巡视告警准确率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countWarnAccuracy(String startTime, String endTime) {
    return dealCount(statisticsDao.countWarnAccuracy(startTime, endTime));
  }
  /**
   * 巡视结果人工审核完成率
   *
   * @param startTime
   * @param endTime
   * @return
   */
  public HashMap<String, Object> countResultCheck(String startTime, String endTime) {
    return dealCount(statisticsDao.countResultCheck(startTime, endTime));
  }
}
