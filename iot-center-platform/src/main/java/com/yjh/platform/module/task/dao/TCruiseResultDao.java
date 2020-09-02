package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TCruiseResult;
import com.yjh.platform.module.task.entity.TCruiseTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-25
 */
@Repository
public interface TCruiseResultDao {

    int insert(TCruiseResult tCruiseResult);
    int deleteByPrimaryId(@Param(value = "taskResultId") String taskResultId);
    int update(TCruiseResult tCruiseResult);
    TCruiseResult selectByPrimaryId(@Param(value = "taskResultId") String taskResultId);
    List<TCruiseResult> select(@Param(value = "taskResultId") String taskResultId,
                                @Param(value = "taskId") String taskId,
                                @Param(value = "areaId") String areaId,
                                @Param(value = "cType") Integer cType,
                                @Param(value = "cState") Integer cState,
                                @Param(value = "modifyState") Integer modifyState,
                                @Param(value = "taskCount") Integer taskCount,
                                @Param(value = "taskWait") Integer taskWait,
                                @Param(value = "checkUser") String checkUser,
                                @Param(value = "checkDate") Date checkDate,
                                @Param(value = "weather") String weather,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "executeTime") Date executeTime,
                                @Param(value = "taskCode") String taskCode,
                                @Param(value = "remark") String remark);
    List<TCruiseResult> selectByPage(TCruiseResult tCruiseResult);

    int batchInsert(List<TCruiseResult> list);

    List<TCruiseTask> selectTaskIsRunning();
}
