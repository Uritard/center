package com.yjh.platform.module.user.dao;

import java.util.List;

import com.yjh.platform.module.user.entity.SysRoleDevice;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-31
 */
@Repository
public interface SysRoleDeviceDao {

    int insert(SysRoleDevice sysRoleDevice);
    int deleteByPrimaryId(@Param(value = "roleId") Long roleId);
    int update(SysRoleDevice sysRoleDevice);
    SysRoleDevice selectByPrimaryId(@Param(value = "roleId") Long roleId);
    List<SysRoleDevice> select(@Param(value = "roleId") Long roleId,
                                @Param(value = "deviceId") Long deviceId);
    List<SysRoleDevice> selectByPage(SysRoleDevice sysRoleDevice);

    int batchInsert(@Param("list") List<SysRoleDevice> list);
}
