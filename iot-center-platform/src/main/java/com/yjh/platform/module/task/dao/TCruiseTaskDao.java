package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TCruiseTask;
import com.yjh.platform.module.task.entity.TCruiseTaskCount;
import com.yjh.platform.module.task.entity.TCruiseTaskList;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author tt
 * @since 2020-08-27
 */
@Repository
public interface TCruiseTaskDao {

    int insert(TCruiseTask tCruiseTask);
    int deleteByPrimaryId(@Param(value = "taskId") String taskId);
    int update(TCruiseTask tCruiseTask);
    TCruiseTask selectByPrimaryId(@Param(value = "taskId") String taskId);
    List<TCruiseTask> select(@Param(value = "taskId") String taskId,
                             @Param(value = "taskName") String taskName,
                             @Param(value = "planId") Long planId,
                             @Param(value = "areaId") String areaId,
                             @Param(value = "type") Integer type,
                             @Param(value = "ifRun") Integer ifRun,
                             @Param(value = "robotId") Long robotId,
                             @Param(value = "dateType") String dateType,
                             @Param(value = "taskType") Integer taskType,
                             @Param(value = "startTime") Date startTime,
                             @Param(value = "createTime") Date createTime);
    List<TCruiseTask> selectByPage(TCruiseTask tCruiseTask);

    int batchInsert(List<TCruiseTask> list);

    List<TCruiseTaskCount> taskCount(@Param(value = "startTime") Date startTime,
                                     @Param(value = "endTime") Date endTime);

    List<TCruiseTaskList> selectPointStatus(String taskId);
    List<TCruiseTaskCount> taskCountByCondition(HashMap<String,Object> map);
}
