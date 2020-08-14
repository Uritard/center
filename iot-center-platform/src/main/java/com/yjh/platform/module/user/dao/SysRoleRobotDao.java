package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.SysRoleRobot;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface SysRoleRobotDao {

    int insert(SysRoleRobot sysRoleRobot);
    int deleteByPrimaryId(@Param(value = "roleId") Long roleId);
    int update(SysRoleRobot sysRoleRobot);
    SysRoleRobot selectByPrimaryId(@Param(value = "roleId") Long roleId);
    List<SysRoleRobot> select(@Param(value = "roleId") Long roleId,
                              @Param(value = "robotId") Long robotId);
    List<SysRoleRobot> selectByPage(SysRoleRobot sysRoleRobot);

    int batchInsert(@Param("list") List<SysRoleRobot> list);
}
