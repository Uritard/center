package com.yjh.accessvideo.module.device.dao;

import java.util.List;
import java.util.Date;
import com.yjh.accessvideo.module.device.entity.TCruiseTaskResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-10-20
 */
@Repository
public interface TCruiseTaskResultDao {

    int insert(TCruiseTaskResult tCruiseTaskResult);
    int deleteByPrimaryId(@Param(value = "taskResultId") String taskResultId);
    int update(TCruiseTaskResult tCruiseTaskResult);
    TCruiseTaskResult selectByPrimaryId(@Param(value = "taskResultId") String taskResultId);
    List<TCruiseTaskResult> select(@Param(value = "taskResultId") String taskResultId,
                                @Param(value = "taskId") String taskId,
                                @Param(value = "taskAbnormal") Integer taskAbnormal,
                                @Param(value = "taskAlarm") Integer taskAlarm,
                                @Param(value = "runExecute") String runExecute,
                                @Param(value = "cruiseTaskTime") Date cruiseTaskTime,
                                @Param(value = "taskStatus") Integer taskStatus,
                                @Param(value = "cruiseResult") Integer cruiseResult,
                                @Param(value = "remark") String remark);
    List<TCruiseTaskResult> selectByPage(TCruiseTaskResult tCruiseTaskResult);

    int batchInsert(List<TCruiseTaskResult> list);
}
