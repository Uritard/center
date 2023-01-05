package com.yjh.accessrobot.module.command.dao;


import com.yjh.accessrobot.module.command.entity.SysUser;
import com.yjh.accessrobot.module.command.entity.SysUserDevicePermission;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface SysUserDao {

    SysUser selectByPrimaryId(@Param(value = "userId") Long userId);

    List<Long> selectIdsByRoleId(@Param(value = "roleId")Long roleId);
    int batchInsertDevicePermission(@Param("list")List<SysUserDevicePermission> list);
}
