package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.user.entity.TRobotInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhou Pengcheng
 * @since 2022-04-13
 */
@Repository
public interface StatisticsDao {

  List<TRobotInfo> selectByPage(TRobotInfo tRobotInfo);

  /**
   * 查询机器人总天数，告警异常天数，正常天数
   *
   * @return
   */
  List<Map<String, Object>> selectStatisticsRobot(@Param(value = "robotId") Long robotId);

  HashMap<String, Object> countTaskAttend(
      @Param(value = "robotId") Long robotId,
      @Param(value = "startTime") Date startTime,
      @Param(value = "endTime") Date endTime);

  HashMap<String, Object> countWarnCheck(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  HashMap<String, Object> countWarnAccuracy(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  HashMap<String, Object> countInstanceLoss(
      @Param(value = "taskId") String taskId,
      @Param(value = "startTime") Date startTime,
      @Param(value = "endTime") Date endTime);

  HashMap<String, Object> countResultCheck(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);
}
