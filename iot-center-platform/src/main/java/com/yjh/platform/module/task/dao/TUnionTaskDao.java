package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TUnionTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-09-04
 */
@Repository
public interface TUnionTaskDao {

    int insert(TUnionTask tUnionTask);
    int deleteByPrimaryId(@Param(value = "unionId") String unionId);
    int update(TUnionTask tUnionTask);
    TUnionTask selectByPrimaryId(@Param(value = "unionId") String unionId);
    List<TUnionTask> select(@Param(value = "unionId") String unionId,
                            @Param(value = "planId") Long planId,
                            @Param(value = "areaId") String areaId,
                            @Param(value = "name") String name,
                            @Param(value = "type") Integer type,
                            @Param(value = "ifRun") Integer ifRun,
                            @Param(value = "robotId") Long robotId,
                            @Param(value = "dateType") Integer dateType,
                            @Param(value = "remark1") Integer remark1,
                            @Param(value = "remark2") Integer remark2,
                            @Param(value = "remark3") String remark3,
                            @Param(value = "taskType") Integer taskType,
                            @Param(value = "startTime") Date startTime,
                            @Param(value = "createTime") Date createTime);
    List<TUnionTask> selectByPage(TUnionTask tUnionTask);

    int batchInsert(List<TUnionTask> list);
}
