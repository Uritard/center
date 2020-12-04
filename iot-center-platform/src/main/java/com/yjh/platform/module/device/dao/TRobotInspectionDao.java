package com.yjh.platform.module.device.dao;

import java.util.List;

import com.yjh.platform.module.device.entity.Robot;
import com.yjh.platform.module.device.entity.RobotTaskMessage;
import com.yjh.platform.module.device.entity.TCruisePointAttr;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.task.entity.ConfirmImmediately;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-08
 */
@Repository
public interface TRobotInspectionDao {

    int insert(TRobotInspection tRobotInspection);
    int deleteByPrimaryId(@Param(value = "inspectionId") Long inspectionId);
    int update(TRobotInspection tRobotInspection);
    TRobotInspection selectByPrimaryId(@Param(value = "inspectionId") Long inspectionId);
    List<TRobotInspection> select(@Param(value = "inspectionId") Long inspectionId,
                                @Param(value = "inspectionCode")String inspectionCode,
                                @Param(value = "robotId") Long robotId,
                                @Param(value = "inspectionName") String inspectionName,
                                @Param(value = "inspectionType") Integer inspectionType,
                                @Param(value = "alarmTop") String alarmTop,
                                @Param(value = "alarmBottom") String alarmBottom,
                                @Param(value = "defaultValue") String defaultValue,
                                @Param(value = "inspectionPosition") Integer inspectionPosition,
                                @Param(value = "collectStatus") Integer collectStatus,
                                @Param(value = "calibrationStatus") Integer calibrationStatus,
                                @Param(value = "unit") String unit);
    List<TRobotInspection> selectByPage(TRobotInspection tRobotInspection);

    int batchInsert(List<TRobotInspection> list);
    List<String> selectForRobotTask(@Param(value = "list") List<Long> list);
    List<Long> selectRobotTaskInstanceId(@Param(value = "list") List<Long> list,
                                       @Param(value = "robotCode") String robotCode);
    List<TCruisePointAttr> selectRobotTaskMessage(@Param(value = "list") String[] list);

    List<Robot> selectRobotInfo();

    List<ConfirmImmediately> selectRobotInspectionIds();

    List<String> selectRobotIsRunning(@Param(value = "taskId") String taskId);

    String selectRobotCode(@Param(value = "robotId") Long robotId);

}
