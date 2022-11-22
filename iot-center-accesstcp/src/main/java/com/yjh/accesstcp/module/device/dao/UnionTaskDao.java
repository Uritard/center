package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.TCfgUnionRule;
import com.yjh.accesstcp.module.device.entity.TCruisePlan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UnionTaskDao {

    List<Long> selectPlanId();

    void deletePlan(@Param("planIdList") Set<Long> planIdList);

    void deletePlanAttr(@Param("planIdList") Set<Long> planIdList);

    void deleteUnionRule(@Param("planIdList") Set<Long> planIdList);

    void insertPlan(TCruisePlan tCruisePlan);

    void batchInsertPlanAttr(@Param("planId") Long planId, @Param("instanceIdList") Set<Long> instanceIdList);

    void batchInsertRule(@Param("tCfgUnionRuleList") List<TCfgUnionRule> tCfgUnionRuleList);

}
