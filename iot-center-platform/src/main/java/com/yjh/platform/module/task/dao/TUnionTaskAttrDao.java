package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TUnionTaskAttr;
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
public interface TUnionTaskAttrDao {
     //插入
    int insert(TUnionTaskAttr tUnionTaskAttr);
    //删除
    String deleteByPrimaryId(Map<String, Object> map);
    //修改
    int update(TUnionTaskAttr tUnionTaskAttr);

    TUnionTaskAttr selectByPrimaryId(@Param(value = "UnionId") String UnionId);

    List<TUnionTaskAttr> select(@Param(value = "UnionId") String UnionId,
                                @Param(value = "instanceId") Long instanceId,
                                @Param(value = "DeviceMeteId") Long DeviceMeteId,
                                @Param(value = "DeviceCustomId") String DeviceCustomId,
                                @Param(value = "PointTaskId") String PointTaskId,
                                @Param(value = "IfRobot") Integer IfRobot,
                                @Param(value = "IfVideo") Integer IfVideo,
                                @Param(value = "IfInferad") Integer IfInferad,
                                @Param(value = "IfArtificial") Integer IfArtificial);

    //分页查询
    List<TUnionTaskAttr> select(TUnionTaskAttr tUnionTaskAttr);
}
