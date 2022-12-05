package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.UPatrolPlanAttr;
import com.yjh.platform.module.task.entity.TCruisePlanAttrDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Repository
public interface UPatrolPlanAttrDao {

    int add(UPatrolPlanAttr uPatrolPlanAttr);
    int deleteByPrimaryId(@Param(value = "planId") Long planId);
    int update(UPatrolPlanAttr uPatrolPlanAttr);
    List<UPatrolPlanAttr> selectByPlanId(@Param(value = "planId") Long planId);
    List<UPatrolPlanAttr> select(@Param(value = "planId") Long planId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "deviceName") String deviceName,
                                @Param(value = "instanceId") Long instanceId,
                                @Param(value = "instanceName") String instanceName,
                                @Param(value = "positionId") Long positionId,
                                @Param(value = "positionName") String positionName,
                                @Param(value = "robotId") Long robotId,
                                @Param(value = "areaId") String areaId,
                                @Param(value = "pointType") Integer pointType,
                                @Param(value = "cruiseRegionIds") String cruiseRegionIds,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "updateTime") Date updateTime);
    List<UPatrolPlanAttr> selectByPage(UPatrolPlanAttr uPatrolPlanAttr);

    int batchAdd(List<UPatrolPlanAttr> list);
    int batchDelete(List<String> list);
    List<Long> seletcInsByPlan (@Param(value = "planId") Long planId);
    List<TCruisePlanAttrDetail> selectByPrimaryId(@Param(value = "planId") Long planId);
    int deleteByInstanceId(@Param(value = "list") List<Long> list);
}
