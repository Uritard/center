/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.user.entity.TRobotInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

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
    int update(UPatrolTask uPatrolTask);
    UPatrolTask selectByPrimaryId(@Param(value = "taskId") String taskId);
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
    List<String> selectPlanRunningTask(@Param(value = "taskLevel")Integer taskLevel);
    List<Long> selectInsByTask(@Param(value = "taskId")String taskId);

    int countInstance(@Param(value = "taskId") String taskId);

    UPatrolResult selectForTaskId(@Param(value = "taskId") String taskId);

    Integer selectIsAlarmByTask(@Param(value = "taskId") String taskId,
                                @Param(value = "instanceId") Long instanceId);

    Integer updatePicPath(@Param(value = "taskId") String taskId,
                          @Param(value = "instanceId") Long instanceId,
                          @Param(value = "imagePath") String imagePath);
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

}
