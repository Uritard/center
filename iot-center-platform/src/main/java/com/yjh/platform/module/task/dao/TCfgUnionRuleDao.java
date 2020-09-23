package com.yjh.platform.module.task.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TCfgUnionRule;
import com.yjh.platform.module.task.entity.TCfgUnionRuleDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-09-18
 */
@Repository
public interface TCfgUnionRuleDao {

    int add(TCfgUnionRule tCfgUnionRule);
    int deleteByPrimaryId(@Param(value = "ruleId") Long ruleId);
    int update(TCfgUnionRule tCfgUnionRule);
    TCfgUnionRule selectByPrimaryId(@Param(value = "ruleId") Long ruleId);
    List<TCfgUnionRule> select(@Param(value = "ruleId") Long ruleId,
                                @Param(value = "planId") Long planId,
                                @Param(value = "ruleName") String ruleName,
                                @Param(value = "ruleType") String ruleType,
                                @Param(value = "ruleContent") String ruleContent,
                                @Param(value = "ruleDelay") Integer ruleDelay,
                                @Param(value = "description") String description,
                                @Param(value = "inputParam") String inputParam,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "updateTime") Date updateTime);
    List<TCfgUnionRuleDetail> selectByPage(TCfgUnionRule tCfgUnionRule);

    int batchAdd(List<TCfgUnionRule> list);
    int batchDelete(List<String> list);

    List<HashMap<String,Object>> selectForTCfgMete(String meteId);
    List<HashMap<String,Object>> selectForTCPlan(Long planId);
    List<TCfgUnionRule> selectUnionRuleByMeteId(@Param(value ="inputParam" )String inputParam);


}
