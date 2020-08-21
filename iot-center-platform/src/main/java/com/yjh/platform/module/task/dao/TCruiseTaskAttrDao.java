package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TCruiseTaskAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author wf
 * @since 2020-08-19
 */
@Repository
public interface TCruiseTaskAttrDao {
     //插入
    int insert(TCruiseTaskAttr tCruiseTaskAttr);
    //删除
    String deleteByPrimaryId(Map<String, Object> map);
    //修改
    int update(TCruiseTaskAttr tCruiseTaskAttr);

    TCruiseTaskAttr selectByPrimaryId(@Param(value = "TaskId") String TaskId);

    List<TCruiseTaskAttr> select(@Param(value = "TaskId") String TaskId,
                                 @Param(value = "InstanceId") Long InstanceId,
                                 @Param(value = "DeviceMeteId") Long DeviceMeteId,
                                 @Param(value = "DeviceId") Long DeviceId,
                                 @Param(value = "CustomId") String CustomId,
                                 @Param(value = "PointTaskId") String PointTaskId,
                                 @Param(value = "IfRobot") Integer IfRobot,
                                 @Param(value = "IfVideo") Integer IfVideo,
                                 @Param(value = "IfInferad") Integer IfInferad,
                                 @Param(value = "IfArtificial") Integer IfArtificial);

    //分页查询
    List<TCruiseTaskAttr> select(TCruiseTaskAttr tCruiseTaskAttr);
}
