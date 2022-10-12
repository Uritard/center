package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.UPatrolResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Repository
public interface UPatrolResultDao {

    int add(UPatrolResult uPatrolResult);
    int deleteByPrimaryId(@Param(value = "taskId") String taskId);
    int update(UPatrolResult uPatrolResult);
    UPatrolResult selectByPrimaryId(@Param(value = "taskId") String taskId);
    List<UPatrolResult> select(@Param(value = "taskId") String taskId,
                                @Param(value = "taskCode") String taskCode,
                                @Param(value = "taskName") String taskName,
                                @Param(value = "areaId") String areaId,
                                @Param(value = "taskType") Integer taskType,
                                @Param(value = "executeType") Integer executeType,
                                @Param(value = "robotId") Long robotId,
                                @Param(value = "taskSource") Integer taskSource,
                                @Param(value = "taskLevel") Integer taskLevel,
                                @Param(value = "taskState") Integer taskState,
                                @Param(value = "modifyState") Integer modifyState,
                                @Param(value = "taskCount") Integer taskCount,
                                @Param(value = "taskWait") Integer taskWait,
                                @Param(value = "checkUser") String checkUser,
                                @Param(value = "checkDate") Date checkDate,
                                @Param(value = "weather") String weather,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "endTime") Date endTime,
                                @Param(value = "executeTime") Date executeTime,
                                @Param(value = "isReview") String isReview,
                                @Param(value = "taskAbnormal") Integer taskAbnormal,
                                @Param(value = "cruiseResult") Integer cruiseResult,
                                @Param(value = "remark") String remark);
    List<UPatrolResult> selectByPage(UPatrolResult uPatrolResult);

    int batchAdd(List<UPatrolResult> list);
    int batchDelete(List<String> list);
}
