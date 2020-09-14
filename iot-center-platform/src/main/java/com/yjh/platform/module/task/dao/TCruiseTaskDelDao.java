package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TCruiseTaskDel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author tt
 * @since 2020-09-14
 */
@Repository
public interface TCruiseTaskDelDao {

    int insert(TCruiseTaskDel tCruiseTaskDel);
    int deleteByPrimaryId(@Param(value = "taskId") String taskId);
    int update(TCruiseTaskDel tCruiseTaskDel);
    TCruiseTaskDel selectByPrimaryId(@Param(value = "taskId") String taskId);
    List<TCruiseTaskDel> select(@Param(value = "taskId") String taskId,
                                @Param(value = "delTime") Date delTime,
                                @Param(value = "createTime") Date createTime);
    List<TCruiseTaskDel> selectByPage(TCruiseTaskDel tCruiseTaskDel);

    int batchInsert(List<TCruiseTaskDel> list);
    List<TCruiseTaskDel> slectByTimeZone(@Param(value = "startTime") Date startTime,
                                         @Param(value = "endTime") Date endTime);
}
