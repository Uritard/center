package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;

import com.yjh.platform.module.task.entity.InstanceTree;
import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.task.entity.TCruisePlanCount;
import com.yjh.platform.module.task.entity.TCruisePlanCountByPage;
import com.yjh.platform.module.user.entity.TDictBusiness;
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
    TCruisePlanCount selectByPrimaryId(@Param(value = "planId") Long planId);
    List<TCruisePlan> select(@Param(value = "planId") Long planId,
                             @Param(value = "planName") String planName,
                             @Param(value = "type") Integer type,
                             @Param(value = "planPointTypes") String planPointTypes,
                             @Param(value = "createTime") Date createTime,
                             @Param(value = "updateTime") Date updateTime);
    List<TCruisePlanCount> selectByPage(TCruisePlan tCruisePlan);
    List<TCruisePlanCountByPage> selectByPlanPage(TCruisePlan tCruisePlan);

    int batchInsert(List<TCruisePlan> list);
    List<InstanceTree> findInstanceTree(@Param("deviceIdList") List<Long> deviceIdList);
    List<TDictBusiness>selectCruiseType();
    List<TDictBusiness>selectCruiseTypeChild(@Param("dictCode") String dictCode);
}
