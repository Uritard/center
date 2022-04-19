package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.user.entity.TRobotInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

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
   * 查询机器人总天数，告警异常天数，正常天数 robot_type非空
   *
   * @return
   */
  List<Map<String, Object>> selectStatisticsRobot(@Param(value = "robotId") Long robotId);

  /**
   * 查询无人机总天数，告警异常天数，正常天数，drone_type 非空
   *
   * @return
   */
  List<Map<String, Object>> selectStatisticsDrone(@Param(value = "robotId") Long robotId);

  HashMap<String, Object> countTaskAttend(
      @Param(value = "robotId") Long robotId,
      @Param(value = "startTime") String startTime,
      @Param(value = "endTime") String endTime);

  HashMap<String, Object> countWarnCheck(
      @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

  HashMap<String, Object> countWarnAccuracy(
      @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

  HashMap<String, Object> countInstanceLoss(
      @Param(value = "taskId") String taskId,
      @Param(value = "robotId") Long robotId,
      @Param(value = "startTime") String startTime,
      @Param(value = "endTime") String endTime);

  HashMap<String, Object> countResultCheck(
      @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

  HashMap<String, Object> countTask(
      @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

  /**
   * 摄像机 可靠性
   *
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return map列表
   */
  List<Map<String, Object>> countCamera(
      @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime);

  Long selectRecordByCamera(@Param(value = "cameraId") Long cameraId);
}
