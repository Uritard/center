package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.UPatrolTaskAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Repository
public interface UPatrolTaskAttrDao {

    int add(UPatrolTaskAttr uPatrolTaskAttr);
    int deleteByPrimaryId(@Param(value = "taskId") String taskId);
    int update(UPatrolTaskAttr uPatrolTaskAttr);
    UPatrolTaskAttr selectByPrimaryId(@Param(value = "taskId") String taskId);
    List<UPatrolTaskAttr> select(@Param(value = "taskId") String taskId,
                                @Param(value = "instanceId") Long instanceId,
                                @Param(value = "deviceMeteId") Long deviceMeteId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "customId") String customId,
                                @Param(value = "pointTaskId") String pointTaskId,
                                @Param(value = "ifRobot") Integer ifRobot,
                                @Param(value = "ifVideo") Integer ifVideo,
                                @Param(value = "ifInferad") Integer ifInferad,
                                @Param(value = "ifArtificial") Integer ifArtificial);
    List<UPatrolTaskAttr> selectByPage(UPatrolTaskAttr uPatrolTaskAttr);

    int batchAdd(List<UPatrolTaskAttr> list);
    int batchDelete(List<String> list);
}
