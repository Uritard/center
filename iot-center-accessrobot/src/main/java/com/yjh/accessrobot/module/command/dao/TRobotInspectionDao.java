package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TRobotInspection;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author YC
 * @since 2020-11-19
 */
@Repository
public interface TRobotInspectionDao {

    int insert(TRobotInspection tRobotInspection);
    int deleteByPrimaryId(@Param(value = "inspectionId") Long inspectionId);
    int update(TRobotInspection tRobotInspection);
    TRobotInspection selectByPrimaryId(@Param(value = "inspectionId") Long inspectionId);
    List<TRobotInspection> select(@Param(value = "inspectionId") Long inspectionId,
                                  @Param(value = "inspectionCode") String inspectionCode,
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

    int batchInsertTRobotInspection(List<TRobotInspection> list);
    List<String> selectAllByRobotId(@Param(value = "robotId") Long robotId);

    List<Long> selectInspectionIdList(List<String> list);
    List<Long> selectInstanceIdList(List<Long> list);
    int batchDeleteTRobotInspection(List<Long> list);
    int batchDeleteTCruisePointInstance(List<Long> list);
    int batchDeleteTCruisePlanAttr(List<Long> list);
}
