package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.task.entity.AfterManualReviewInfo;
import com.yjh.platform.module.task.entity.CruiseResultDetail;
import com.yjh.platform.module.task.entity.TCruiseResultExpand;
import com.yjh.platform.module.task.entity.TaskSimpleInfo;
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

    Long selectDeviceMeteId(@Param(value = "instanceId") Long instanceId);

    AfterManualReviewInfo selectJudgeCondition(@Param(value = "instanceId") Long instanceId,
                                               @Param(value = "taskId") String taskId);

    List<TCruiseResultExpand> selectTaskByPage(@Param(value = "taskName") String taskName,
                                               @Param(value = "cState") Integer cState,
                                               @Param(value = "cType") Integer cType,
                                               @Param(value = "deviceType") Integer deviceType,
                                               @Param(value = "startDate") String startDate,
                                               @Param(value = "endDate") String endDate,
                                               @Param(value = "list") List<Long> list,
                                               @Param(value = "meteType") Integer meteType,
                                               @Param(value = "customId") String customId,
                                               @Param(value = "isCheck") Integer isCheck);

    List<CruiseResultDetail> selectCruiseByPage(@Param(value = "taskResultId") String taskResultId,
                                                @Param(value = "cruiseType") Integer cruiseType,
                                                @Param(value = "cruiseResult") Integer cruiseResult,
                                                @Param(value = "deviceType") Integer deviceType,
                                                @Param(value = "startTime") String startTime,
                                                @Param(value = "endTime") String endTime,
                                                @Param(value = "list") List<Long> list,
                                                @Param(value = "customId") String customId);

    List<CruiseResultDetail> selectAbnormalResult(@Param(value = "taskResultId") String taskResultId,
                                                  @Param(value = "cruiseType") Integer cruiseType,
                                                  @Param(value = "cruiseResult") Integer cruiseResult,
                                                  @Param(value = "deviceType") Integer deviceType,
                                                  @Param(value = "instanceName") String instanceName,
                                                  @Param(value = "startTime") String startTime,
                                                  @Param(value = "endTime") String endTime,
                                                  @Param(value = "list") List<Long> list,
                                                  @Param(value = "customId") String customId);

    //查询正在执行的任务
    List<TaskSimpleInfo> selectTaskIsRunning();


    /**
     * 更新任务结果表数据
     *
     * @param uPatrolResult 任务结果数据
     * @return int
     */
    int updateUPatrolResult(UPatrolResult uPatrolResult);

}
