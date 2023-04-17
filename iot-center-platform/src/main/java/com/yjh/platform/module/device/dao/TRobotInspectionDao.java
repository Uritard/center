package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.task.entity.ConfirmImmediately;
import com.yjh.platform.module.user.entity.TRobotInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
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
    List<String> selectRobotTaskInstanceId(@Param(value = "list") List<Long> list,
                                         @Param(value = "robotCode") String robotCode);
    List<TCruisePointAttr> selectRobotTaskMessage(@Param(value = "list") String[] list);

    List<Robot> selectRobotInfo(@Param(value = "robotType") Integer robotType,
                                @Param(value = "userId")Long userId);

    List<Robot> selectDroneInfo(@Param(value = "droneType") Integer droneType,
                                @Param(value = "userId")Long userId);

    List<ConfirmImmediately> selectRobotInspectionIds();

    List<String> selectRobotIsRunning(@Param(value = "taskId") String taskId);

    String selectRobotCode(@Param(value = "robotId") Long robotId);

    String selectRobotTaskOnStart(@Param(value = "robotId") Long robotId);

    Map<String, Object> selectRobotOperationTaskOnStart(@Param(value = "robotId") Long robotId);

    TRobotInfo selectRobot(@Param(value = "robotId") Long robotId);

    TRobotInspectionTmp selectTRobotInspectionTmp(@Param(value = "inspectionId") Long inspectionId);
    List<TRobotInspectionTmp> selectTRobotInspectionByIds(@Param(value = "list")List<Long> inspectionId);

    Map<String,Object> selectInspection(@Param(value = "inspectionCode") String inspectionCode);

    /**
     * 通过机器人上报的任务id查询巡视主机上的任务id
     * @param robotTaskId 任务id
     * @return String
     */
    String selectRealTaskId(@Param(value = "robotTaskId")String robotTaskId, @Param(value = "executeTime") Date executeTime);
    /**
     * 通过机器人编码查询巡视机器人类型
     * @param robotCode 机器人编码
     * @return Integer
     */
    Integer selectRobotType(@Param(value = "robotCode") String robotCode);


    /**
     * 通过原deviceId，edgeCode查找deviceId
     * @param deviceId
     * @param edgeCode
     * @return
     */
    TCruisePointInstance selectRealInstance(@Param(value = "originId") String deviceId,@Param(value = "edgeCode") String edgeCode);

    TCruisePointInstance selectRealInstanceByDevicePoint(@Param(value = "devicePointId") String devicePointId);

    /**
     * 判断robotCode是上级系统传来的还是下级系统
     * @param robotCode
     * @return
     */
    int selectRobotCount(@Param(value = "edgeCode")String robotCode);

    int selectRegion(@Param(value = "edgeCode")String edgeCode);

    long selectRobotIdByRobotCode(String robotCode);


    String selectRobotCodeByInstanceId(@Param("instanceId")Long instanceId);

    String selectTypeByInstanceId(Long instanceId);
}
