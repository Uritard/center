package com.yjh.platform.module.user.dao;

import java.util.List;

import com.yjh.platform.module.user.entity.SysRoleCamera;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface SysRoleCameraDao {

    int insert(SysRoleCamera sysRoleCamera);
    int deleteByPrimaryId(@Param(value = "roleId") Long roleId);
    int update(SysRoleCamera sysRoleCamera);
    SysRoleCamera selectByPrimaryId(@Param(value = "roleId") Long roleId);
    List<SysRoleCamera> select(@Param(value = "roleId") Long roleId,
                                @Param(value = "cameraId") Long cameraId);
    List<SysRoleCamera> selectByPage(SysRoleCamera sysRoleCamera);

    int batchInsert(@Param("list") List<SysRoleCamera> list);
}
