package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TRobotMapNode;
import org.apache.commons.collections4.SetUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author quzhihui
 * @date 2022/10/8 - 17:57
 */
@Repository
public interface TRobotMapNodeDao {

    int batchInsertTMapNodes(List<TRobotMapNode> list);

    int deleteByRobotId(@Param("robotId") Long robotId);

    List<TRobotMapNode> selectByRobotId(@Param("robotId") Long robotId);

    int updateByPrimaryKey(TRobotMapNode tRobotMapNode);

    int deleteByRobotIdAndNodeId(@Param(value = "robotId") Long robotId, @Param("nodeIdList") Collection<String> nodeIdList);

    int deleteRobotDeviceConfig(@Param(value = "robotId") Long robotId, @Param("nodeIdList") Collection<String> nodeIdList);

    int updateTStdDevice(@Param(value = "robotId") Long robotId, @Param("nodeIdList") Collection<String> nodeIdList);
}
