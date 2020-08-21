package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.task.entity.TCruiseTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author wf
 * @since 2020-08-19
 */
@Repository
public interface TCruiseTaskDao {
     //插入
    int insert(TCruiseTask tCruiseTask);
    //删除
    String deleteByPrimaryId(@Param(value = "TaskId") String TaskId);
    //修改
    int update(TCruiseTask tCruiseTask);

    TCruiseTask selectByPrimaryId(@Param(value = "TaskId") String TaskId);

    List<TCruiseTask> select(@Param(value = "TaskId") String TaskId,
                             @Param(value = "PlanId") Long PlanId,
                             @Param(value = "AreaId") String AreaId,
                             @Param(value = "Name") String Name,
                             @Param(value = "Type") Integer Type,
                             @Param(value = "IfRun") Integer IfRun,
                             @Param(value = "RobotId") Long RobotId,
                             @Param(value = "Datetype") Integer Datetype,
                             @Param(value = "Remark1") Integer Remark1,
                             @Param(value = "TaskType") Integer TaskType,
                             @Param(value = "StartTime") Date StartTime,
                             @Param(value = "CreateTime") Date CreateTime);

    //分页查询
    List<TCruiseTask> select(TCruiseTask tCruiseTask);
}
