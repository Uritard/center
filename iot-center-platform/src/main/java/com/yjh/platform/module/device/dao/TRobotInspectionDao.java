package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.Robot;
import com.yjh.platform.module.device.entity.TCruisePointAttr;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.device.entity.TRobotInspectionTmp;
import com.yjh.platform.module.task.entity.ConfirmImmediately;
import com.yjh.platform.module.user.entity.TRobotInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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
                                  @Param(value = "meterType") Integer meterType,
                                  @Param(value = "appearanceType") Integer appearanceType,
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

    List<Robot> selectRobotInfo(@Param(value = "robotType") Integer robotType);

    List<Robot> selectDroneInfo(@Param(value = "droneType") Integer droneType);

    List<ConfirmImmediately> selectRobotInspectionIds();

    List<String> selectRobotIsRunning(@Param(value = "taskId") String taskId);

    String selectRobotCode(@Param(value = "robotId") Long robotId);

    String selectRobotTaskOnStart(@Param(value = "robotId") Long robotId);

    Map<String, Object> selectRobotOperationTaskOnStart(@Param(value = "robotId") Long robotId);

    TRobotInfo selectRobot(@Param(value = "robotId") Long robotId);

    TRobotInspectionTmp selectTRobotInspectionTmp(@Param(value = "inspectionId") Long inspectionId);

    Map<String,Object> selectInspection(@Param(value = "inspectionCode") String inspectionCode);

}
