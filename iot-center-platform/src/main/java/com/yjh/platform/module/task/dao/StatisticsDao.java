package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.StatisticalDefectMapping;
import com.yjh.platform.module.task.entity.Statistics;
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
   * 查询机器人总天数，告警异常天数，正常天数 robot_type非空
   *
   * @return
   */
  List<Map<String, Object>> selectStatisticsRobot(@Param(value = "robotId") Long robotId);
  List<Long> selectAvailableRobotOrDrone(@Param("type")String type);

  /**
   * 查询机器人正常天数
   *
   * @return
   */
  List<Map<String, Object>> selectNormalDayRobot(@Param(value = "robotId") Long robotId);

  /**
   * 查询无人机总天数，告警异常天数，正常天数，drone_type 非空
   *
   * @return
   */
  List<Map<String, Object>> selectStatisticsDrone(@Param(value = "robotId") Long robotId);

  /**
   * 摄像机 可靠性
   *
   * @return map列表
   */
  List<Map<String, Object>> countCamera(@Param(value = "cameraId") Long cameraId,
                                        @Param("startIndex")int startIndex,
                                        @Param("endIndex")int endIndex);

  /**
   * 根据相机id查询录像机id
   *
   * @param cameraId
   * @return
   */
  Long selectRecordByCamera(@Param(value = "cameraId") Long cameraId);

  HashMap<String, Object> countInstanceLoss(
      @Param(value = "taskId") String taskId,
      @Param(value = "robotId") Long robotId,
      @Param(value = "startTime") String startTime,
      @Param(value = "endTime") String endTime);

  List<Statistics> countWarnCheckByDay(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countWarnCheckByWeek(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countWarnCheckByMonth(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countWarnAccuracyByDay(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countWarnAccuracyByWeek(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countWarnAccuracyByMonth(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countInstanceLossByDay(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countInstanceLossByWeek(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countInstanceLossByMonth(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countResultCheckByDay(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countResultCheckByWeek(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countResultCheckByMonth(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countTaskByDay(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countTaskByWeek(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countTaskByMonth(
      @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

  List<Statistics> countTaskFrequencyByDay(
          @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime
  );

  List<Statistics> countTaskFrequencyByWeek(
          @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime
  );

  List<Statistics> countTaskFrequencyByMonth(
          @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime
  );

  List<Statistics>countTaskExecutedDurationByDay(
          @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime
  );

  List<Statistics>countTaskExecutedDurationByWeek(
          @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime
  );

  List<Statistics>countTaskExecutedDurationByMonth(
          @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime
  );

  List<StatisticalDefectMapping>countFoundDefectByDay(
          @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime
  );
  List<StatisticalDefectMapping>countFoundDefectByWeek(
          @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime
  );
  List<StatisticalDefectMapping>countFoundDefectByMonth(
          @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime
  );
  List<Map<String,Object>> selectRobot();
  List<Map<String,Object>> selectDrone();

  List<Map<String,Object>> selectCamera();
}
