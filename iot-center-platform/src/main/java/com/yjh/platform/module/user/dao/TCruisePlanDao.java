package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.user.entity.TCruisePlan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-08-19
 */
@Repository
public interface TCruisePlanDao {

    int insert(TCruisePlan tCruisePlan);
    int deleteByPrimaryId(@Param(value = "planId") Long planId);
    int update(TCruisePlan tCruisePlan);
    TCruisePlan selectByPrimaryId(@Param(value = "planId") Long planId);
    List<TCruisePlan> select(@Param(value = "planId") Long planId,
                                @Param(value = "instanceId") Long instanceId,
                                @Param(value = "pointType") Integer pointType,
                                @Param(value = "cruiseRegionIds") String cruiseRegionIds,
                                @Param(value = "exceptionType") Integer exceptionType,
                                @Param(value = "robotId") Long robotId,
                                @Param(value = "position") String position,
                                @Param(value = "algorithmId") Integer algorithmId,
                                @Param(value = "algorithmName") String algorithmName,
                                @Param(value = "inferadAnalyze") String inferadAnalyze,
                                @Param(value = "irTempBox") String irTempBox,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "updateTime") Date updateTime);
    List<TCruisePlan> selectByPage(TCruisePlan tCruisePlan);

    int batchInsert(List<TCruisePlan> list);
}
