package com.yjh.platform.module.task.dao;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.module.task.entity.TCruisePlan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author wf
 * @since 2020-08-19
 */
@Repository
public interface TCruisePlanDao {
     //插入
    int insert(TCruisePlan tCruisePlan);
    //删除
    String delete(Map<String, Object> map);
    //修改
    int update(TCruisePlan tCruisePlan);

    TCruisePlan selectByPrimaryId(@Param(value = "PlanId") Long PlanId);

    List<TCruisePlan> select(@Param(value = "PlanId") Long PlanId,
                             @Param(value = "InstanceId") Long InstanceId,
                             @Param(value = "PointType") Integer PointType,
                             @Param(value = "AreaId") String AreaId,
                             @Param(value = "CruiseRegionIds") String CruiseRegionIds,
                             @Param(value = "ExceptionType") Integer ExceptionType,
                             @Param(value = "RobotId") Long RobotId,
                             @Param(value = "Position") String Position,
                             @Param(value = "AlgorithmId") Integer AlgorithmId,
                             @Param(value = "AlgorithmName") String AlgorithmName,
                             @Param(value = "InferadAnalyze") String InferadAnalyze,
                             @Param(value = "IrTempBox") String IrTempBox,
                             @Param(value = "CreateTime") Date CreateTime,
                             @Param(value = "UpdateTime") Date UpdateTime);

    //分页查询
    List<TCruisePlan> select(TCruisePlan tCruisePlan);

    List<TCruisePlan> SelectPlanQuery(JSONObject jsonObject);
}
