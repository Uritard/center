package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;

import com.yjh.platform.module.task.entity.InstanceTree;
import com.yjh.platform.module.task.entity.TCruisePlan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-09-07
 */
@Repository
public interface TCruisePlanDao {

    int insert(TCruisePlan tCruisePlan);
    int deleteByPrimaryId(@Param(value = "planId") Long planId);
    int update(TCruisePlan tCruisePlan);
    TCruisePlan selectByPrimaryId(@Param(value = "planId") Long planId);
    List<TCruisePlan> select(@Param(value = "planId") Long planId,
                             @Param(value = "planName") String planName,
                             @Param(value = "type") Integer type,
                             @Param(value = "planPointTypes") String planPointTypes,
                             @Param(value = "createTime") Date createTime,
                             @Param(value = "updateTime") Date updateTime);
    List<TCruisePlan> selectByPage(TCruisePlan tCruisePlan);

    int batchInsert(List<TCruisePlan> list);
    List<InstanceTree> findInstanceTree();
}
