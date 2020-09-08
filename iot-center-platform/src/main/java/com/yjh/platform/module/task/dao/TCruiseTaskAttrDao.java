package com.yjh.platform.module.task.dao;

import java.util.List;

import com.yjh.platform.module.task.entity.TCruiseTaskAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-09-04
 */
@Repository
public interface TCruiseTaskAttrDao {

    int insert(TCruiseTaskAttr tCruiseTaskAttr);
    int deleteByPrimaryId(@Param(value = "taskId") String taskId);
    int update(TCruiseTaskAttr tCruiseTaskAttr);
    TCruiseTaskAttr selectByPrimaryId(@Param(value = "taskId") String taskId);
    List<TCruiseTaskAttr> select(@Param(value = "taskId") String taskId,
                                 @Param(value = "instanceId") Long instanceId,
                                 @Param(value = "deviceMeteId") Long deviceMeteId,
                                 @Param(value = "deviceId") Long deviceId,
                                 @Param(value = "customId") String customId,
                                 @Param(value = "pointTaskId") String pointTaskId,
                                 @Param(value = "ifRobot") Integer ifRobot,
                                 @Param(value = "ifVideo") Integer ifVideo,
                                 @Param(value = "ifInferad") Integer ifInferad,
                                 @Param(value = "ifArtificial") Integer ifArtificial);
    List<TCruiseTaskAttr> selectByPage(TCruiseTaskAttr tCruiseTaskAttr);

    int batchInsert(List<TCruiseTaskAttr> list);
}
