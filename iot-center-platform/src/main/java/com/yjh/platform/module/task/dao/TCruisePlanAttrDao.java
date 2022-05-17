package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TCruisePlanAttr;
import com.yjh.platform.module.task.entity.TCruisePlanAttrDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-09-04
 */
@Repository
public interface TCruisePlanAttrDao {

    int insert(TCruisePlanAttr tCruisePlanAttr);
    int deleteByPrimaryId(@Param(value = "planId") Long planId);
    int update(TCruisePlanAttr tCruisePlanAttr);
    List<TCruisePlanAttrDetail> selectByPrimaryId(@Param(value = "planId") Long planId);
    List<TCruisePlanAttr> select(@Param(value = "planId") Long planId,
                                 @Param(value = "instanceId") Long instanceId,
                                 @Param(value = "pointType") Integer pointType,
                                 @Param(value = "areaId") String areaId,
                                 @Param(value = "cruiseRegionIds") String cruiseRegionIds,
                                 @Param(value = "exceptionType") Integer exceptionType,
                                 @Param(value = "robotId") Long robotId,
                                 @Param(value = "position") String position,
                                 @Param(value = "algorithmId") Long algorithmId,
                                 @Param(value = "inferadAnalyze") String inferadAnalyze,
                                 @Param(value = "irTempBox") String irTempBox,
                                 @Param(value = "createTime") Date createTime,
                                 @Param(value = "updateTime") Date updateTime,
                                 @Param(value = "subType") Integer subType);
    List<Long> batchSelectAttr(@Param("instanceList") List<Long> instanceList);
    List<TCruisePlanAttr> selectByPage(TCruisePlanAttr tCruisePlanAttr);

    int batchInsert(@Param(value = "tCruisePlanAttrList") List<TCruisePlanAttr> tCruisePlanAttrList);
    int deleteByInstanceId(@Param(value = "list") List<Long> list);
    List<Long> selectByPlanId(@Param(value = "planId") Long planId);

    List<String> selectPlanRunningTask(@Param(value = "taskLevel")Integer taskLevel);
}
