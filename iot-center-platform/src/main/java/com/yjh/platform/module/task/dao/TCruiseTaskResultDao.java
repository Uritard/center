package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TCruiseTaskResult;
import com.yjh.platform.module.task.entity.TaskSimpleInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-25
 */
@Repository
public interface TCruiseTaskResultDao {

    int insert(TCruiseTaskResult tCruiseTaskResult);
    int deleteByPrimaryId(@Param(value = "taskResultId") String taskResultId);
    int update(TCruiseTaskResult tCruiseTaskResult);
    TCruiseTaskResult selectByPrimaryId(@Param(value = "taskResultId") String taskResultId);
    List<TCruiseTaskResult> select(@Param(value = "taskResultId") String taskResultId,
                                @Param(value = "taskId") String taskId,
                                   @Param(value = "taskName") String taskName,
                                @Param(value = "taskAbnormal") Integer taskAbnormal,
                                @Param(value = "taskAlarm") Integer taskAlarm,
                                @Param(value = "runExecute") String runExecute,
                                @Param(value = "cruiseTaskTime") Date cruiseTaskTime,
                                @Param(value = "taskStatus") Integer taskStatus,
                                @Param(value = "cruiseResult") Integer cruiseResult,
                                @Param(value = "remark") String remark);
    List<TCruiseTaskResult> selectByPage(TCruiseTaskResult tCruiseTaskResult);

    int batchInsert(List<TCruiseTaskResult> list);
    TaskSimpleInfo selectTaskStateByTaskId(@Param(value = "taskId")String taskId);
    //统计当前任务下所有点的数量
    Long selectCruiseCountsByTaskId(@Param(value = "taskId")String taskId);


}
