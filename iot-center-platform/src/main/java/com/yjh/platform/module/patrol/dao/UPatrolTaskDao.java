/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.TCruisePointInstanceDetail;
import com.yjh.platform.module.patrol.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.task.entity.TCruiseTaskCount;
import com.yjh.platform.module.task.entity.TCruiseTaskList;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/11
 * @since [产品/模块版本] （可选）
 */
@Repository
public interface UPatrolTaskDao {

    int add(UPatrolTask uPatrolTask);
    int deleteByPrimaryId(@Param(value = "taskId") String taskId);
    int deleteInitByPrimaryId(@Param(value = "taskId") String taskId);
    int update(UPatrolTask uPatrolTask);
    UPatrolTask selectByPrimaryId(@Param(value = "taskId") String taskId);
    UPatrolTask selectThisTaskByTaskCode(@Param(value = "taskCode") String taskCode);
    List<UPatrolTask> select(@Param(value = "taskId") String taskId,
                             @Param(value = "taskCode") String taskCode,
                             @Param(value = "taskName") String taskName,
                             @Param(value = "planId") Long planId,
                             @Param(value = "areaId") String areaId,
                             @Param(value = "taskType") Integer taskType,
                             @Param(value = "executeType") Integer executeType,
                             @Param(value = "robotId") Long robotId,
                             @Param(value = "dateType") String dateType,
                             @Param(value = "taskSource") Integer taskSource,
                             @Param(value = "taskLevel") Integer taskLevel,
                             @Param(value = "startTime") Date startTime,
                             @Param(value = "createTime") Date createTime,
                             @Param(value = "endTime") Date endTime,
                             @Param(value = "createUserId") Long createUserId);
    List<UPatrolTask> selectByPage(UPatrolTask uPatrolTask);

    int batchAdd(List<UPatrolTask> list);
    int batchDelete(List<String> list);
    List<String> selectPlanRunningTask(@Param(value = "lowerLevel")Integer lowerLevel, @Param(value = "highLevel")Integer highLevel);

    List<String> selectPlanRunningOrPauseTask(@Param(value = "lowerLevel")Integer lowerLevel, @Param(value = "highLevel")Integer highLevel);
    List<Long> selectInsByTask(@Param(value = "taskId")String taskId);

    int countInstance(@Param(value = "taskId") String taskId);

    UPatrolResult selectForTaskId(@Param(value = "taskId") String taskId);

    Integer selectIsAlarmByTask(@Param(value = "taskId") String taskId,
                                @Param(value = "instanceId") Long instanceId);

    Integer updatePicPath(@Param(value = "taskId") String taskId,
                          @Param(value = "instanceId") Long instanceId,
                          @Param(value = "imagePath") String imagePath);
    /**
     * 根据任务id和巡检点id查询告警id查询该点是否为告警
     * @param instanceId 巡视点id
     * @param taskId 任务id
     * @return int
     */
    int selectIsWarnByTaskId(@Param(value = "instanceId")Long instanceId,
                             @Param(value = "taskId")String taskId);

    /**
     * 更新任务结果表
     * @param cruiseDataId 巡视点结果id
     * @return int
     */
    int updateIsWarnByCruiseDataId(@Param(value = "cruiseDataId")Long cruiseDataId);

    /**
     * 根据巡视点id查询测点信息
     * @param instanceId 巡视点id
     * @return TStdDeviceMete
     */
    TStdDeviceMete selectDeviceMeteInfo(@Param(value = "instanceId")Long instanceId);

    /**
     * 查询字典id
     * @param dictNote 字典编码
     * @param colName 字典列名
     * @return String
     */
    String selectDictCodeByNote(@Param(value = "dictNote")String dictNote,
                                @Param(value = "colName")String colName);

    /**
     * 根据机器人实物id查询机器人信息
     * @param robotCode 机器人实物id
     * @return TRobotInfo 机器人信息
     */
    TRobotInfo selectRobotInfoByCode(@Param(value = "robotCode") String robotCode);

    List<TCruiseTaskCount> taskCount(@Param(value = "startTime") Date startTime,
                                     @Param(value = "endTime") Date endTime);
    List<TCruiseTaskList> selectPointStatus(String taskId);

    List<TCruiseTaskCount> afterTaskCount(HashMap<String,Object> map);

    String selectTaskByRobotTaskCode(@Param(value = "taskCode") String taskCode);

    /**
     * 根据巡视点查询相关信息
     * @param instanceId 巡视点id
     * @return TCruisePointInstanceDetail
     */
    TCruisePointInstanceDetail selectForTask(@Param(value = "instanceId")Long instanceId);

    /**
     * 根据测点id查询测点信息
     * @param deviceMeteId 测点id
     * @return TStdDeviceMete
     */
    TStdDeviceMete selectDeviceMete(@Param(value = "deviceMeteId") Long deviceMeteId);

    /**
     * 根据任务id查询已经有结果且已入库的巡视点
     * @param taskId 任务id
     * @return List<Long>
     */
    List<Long> selectInstanceForTaskGoOn(@Param(value = "taskId") String taskId);

    UPatrolTask selectTaskByTaskCode(@Param(value = "taskCode") String taskCode);

    String selectTaskCodeByTaskId(@Param(value = "taskId") String taskId);

    List<Map<String,Object>> selectForSequenceInfoByMeteId(@Param(value = "cfgDeviceId") String cfgDeviceId);

    List<String> selectRobotIsRunning(@Param(value = "taskId") String taskId);

    List<String> selectEdgeIsRunning(@Param(value = "taskId") String taskId);

    List<String> selectRobotTaskInstanceList(@Param(value = "taskId") String taskId);

    String selectRobotTaskOnStart(@Param(value = "robotId") Long robotId);
    String selectCurrentTaskId(@Param(value = "taskCode") String taskCode);

    Map<String,String> selectRobotTaskOnStartV2(@Param(value = "robotId") Long robotId);


    List<Long> selectRobotOrDroneInsByTaskId(@Param("taskId") String taskId);

    List<Long>  selectInstanceIdByTaskId(@Param("taskId") String taskId);

    List<Map<String, Object>> selectTaskPriorityConfigList();

    void updateTaskPriorityConfig(@Param(value = "type")Integer type,
                                  @Param(value = "level")Integer level);

}
