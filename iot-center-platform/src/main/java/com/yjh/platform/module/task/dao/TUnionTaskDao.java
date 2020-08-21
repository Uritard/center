package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TUnionTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author wf
 * @since 2020-08-19
 */
@Repository
public interface TUnionTaskDao {
     //插入
    int insert(TUnionTask tUnionTask);
    //删除
    String deleteByPrimaryId(@Param(value = "UnionId") String UnionId);
    //修改
    int update(TUnionTask tUnionTask);

    TUnionTask selectByPrimaryId(@Param(value = "UnionId") String UnionId);

    List<TUnionTask> select (@Param(value = "UnionId") String UnionId,
                             @Param(value = "PlanId") Long PlanId,
                             @Param(value = "AreaId") String AreaId,
                             @Param(value = "Name") String Name,
                             @Param(value = "Type") Integer Type,
                             @Param(value = "IfRun") Integer IfRun,
                             @Param(value = "RobotId") Long RobotId,
                             @Param(value = "Datetype") Integer Datetype,
                             @Param(value = "Remark1") Integer Remark1,
                             @Param(value = "Remark2") Integer Remark2,
                             @Param(value = "Remark3") Integer Remark3,
                             @Param(value = "TaskType") Integer TaskType,
                             @Param(value = "StartTime") Date StartTime,
                             @Param(value = "CreateTime") Date CreateTime);

    //分页查询
    List<TUnionTask> select(TUnionTask tUnionTask);
}
