package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.TCfgUnionRule;
import com.yjh.accesstcp.module.device.entity.TCruisePlan;
import com.yjh.accesstcp.module.device.entity.UPatrolPlanAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface UnionTaskDao {

    List<Long> selectPlanId(List<Map<String, Object>> list);

    void deletePlan(@Param("planIdList") Set<Long> planIdList);

    void deletePlanAttr(@Param("planIdList") Set<Long> planIdList);

    void deleteUnionRule(@Param("planIdList") Set<Long> planIdList);

    void insertPlan(TCruisePlan tCruisePlan);

    void batchInsertPlanAttr(List<UPatrolPlanAttr> list);

    void batchInsertRule(@Param("tCfgUnionRuleList") List<TCfgUnionRule> tCfgUnionRuleList);

    List<UPatrolPlanAttr> selectPatrolPlan(@Param("planId") Long planId,  @Param("instanceIdList") Set<Long> instanceIdList);

    void deleteAll();

}
