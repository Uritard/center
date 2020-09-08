package com.yjh.platform.module.task.dao;

import java.util.List;

import com.yjh.platform.module.task.entity.TUnionTaskAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-09-04
 */
@Repository
public interface TUnionTaskAttrDao {

    int insert(TUnionTaskAttr tUnionTaskAttr);
    int deleteByPrimaryId(@Param(value = "unionId") String unionId);
    int update(TUnionTaskAttr tUnionTaskAttr);
    TUnionTaskAttr selectByPrimaryId(@Param(value = "unionId") String unionId);
    List<TUnionTaskAttr> select(@Param(value = "unionId") String unionId,
                                @Param(value = "instanceId") Long instanceId,
                                @Param(value = "deviceMeteId") Long deviceMeteId,
                                @Param(value = "deviceCustomId") String deviceCustomId,
                                @Param(value = "pointTaskId") Long pointTaskId,
                                @Param(value = "ifRobot") Integer ifRobot,
                                @Param(value = "ifVideo") Integer ifVideo,
                                @Param(value = "ifInferad") Integer ifInferad,
                                @Param(value = "ifArtificial") Integer ifArtificial);
    List<TUnionTaskAttr> selectByPage(TUnionTaskAttr tUnionTaskAttr);

    int batchInsert(List<TUnionTaskAttr> list);
}
