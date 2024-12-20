package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.AInterfaceTaskInfo;
import com.yjh.accesstcp.module.device.entity.SysLogs;
import com.yjh.accesstcp.module.device.entity.UPatrolPlanAttr;
import com.yjh.accesstcp.module.device.entity.UPatrolTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tt
 * @since 2020-08-12
 */
@Repository
public interface PatrolTaskDao {

    List<Map<String,Object>> selectTaskInfo(@Param(value = "endTime") Date endTime);

    List<UPatrolPlanAttr> selectInstanceId(@Param(value = "taskId") String taskId);

    HashMap<String,Object> countInstanceLoss(@Param(value = "startTime")String startTime,
                                             @Param(value = "endTime")String endTime );

    HashMap<String,Object> countResultCheck(@Param(value = "startTime")String startTime,
                                            @Param(value = "endTime")String endTime );

    int batchAddAInterfaceTask(List<AInterfaceTaskInfo> list);
    int updateAInterfaceTask(AInterfaceTaskInfo aInterfaceTaskInfo);
    int deleteAInterfaceTask(@Param(value = "taskCode") String taskCode);
    AInterfaceTaskInfo selectAInterfaceTask(@Param(value = "taskCode") String taskCode);

    UPatrolTask selectTaskIdByTaskCode(@Param(value = "robotTaskId") String robotTaskId,
        @Param(value = "executeTime") Date executeTime);

    List<Integer> selectCruiseType(@Param(value = "taskId") String taskId);
}
