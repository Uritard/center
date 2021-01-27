package com.yjh.accessvqd.module.diagnose.dao;

import java.util.List;
import java.util.Date;

import com.yjh.accessvqd.module.diagnose.entity.NVRChannelTree;
import com.yjh.accessvqd.module.diagnose.entity.PlanInfo;
import com.yjh.accessvqd.module.diagnose.entity.Plans;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2021-01-19
 */
@Repository
public interface TDiagnosePlanDao {

    int insert(Plans plans);
    int deleteByPrimaryId(@Param(value = "diagnosePlanId") String diagnosePlanId);
    int update(Plans plans);
    Plans selectByPrimaryId(@Param(value = "diagnosePlanId") String diagnosePlanId);
    List<Plans> select(@Param(value = "diagnosePlanId") String diagnosePlanId,
                                @Param(value = "planName") String planName,
                                @Param(value = "planType") String planType,
                                @Param(value = "userId") Long userId);
    //分页条件查询
    List<Plans> selectByPage(@Param(value = "planName")String planName);
    //主键ID查询
    Plans selectByPlanId(@Param(value = "diagnosePlanId")String diagnosePlanId);

    int batchInsert(List<Plans> list);

    int updateEndTime(@Param(value = "diagnosePlanId")String diagnosePlanId,
                      @Param(value = "endTime")Date endTime);

    //最近一条任务信息
    PlanInfo selectLastPlan();

    //查询NVR-ChannelTree
    List<NVRChannelTree> selectNVRNode();
    List<NVRChannelTree> selectChannelNode(@Param(value = "recordId")String recordId);
}
