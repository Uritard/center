package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.Robot;
import com.yjh.platform.module.device.entity.TCruisePointAttr;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.task.entity.ConfirmImmediately;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
                                  @Param(value = "componentId") String componentId,
                                  @Param(value = "componentName") String componentName,
                                  @Param(value = "bayId") String bayId,
                                  @Param(value = "bayName") String bayName,
                                  @Param(value = "mainDeviceId") String mainDeviceId,
                                  @Param(value = "mainDeviceName") String mainDeviceName,
                                  @Param(value = "deviceType") String deviceType,
                                  @Param(value = "meterType") String meterType,
                                  @Param(value = "appearanceType") String appearanceType,
                                  @Param(value = "saveTypeList") String saveTypeList,
                                  @Param(value = "recognitionTypeList") String recognitionTypeList,
                                  @Param(value = "phase") String phase,
                                  @Param(value = "deviceInfo") String deviceInfo);
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
