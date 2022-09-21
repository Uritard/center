package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.entity.SysRole;
import com.yjh.platform.module.user.entity.SysRoleMonitorDevice;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface SysRoleDao {

    int insert(SysRole sysRole);
    int deleteByPrimaryId(@Param(value = "roleId") Long roleId);
    int update(SysRole sysRole);
    SysRole selectByPrimaryId(@Param(value = "roleId") Long roleId);
    List<SysRole> select(@Param(value = "roleId") Long roleId,
                         @Param(value = "roleName") String roleName,
                         @Param(value = "createTime") Date createTime,
                         @Param(value = "creatorId") Long creatorId,
                         @Param(value = "sysState") Integer sysState);
    List<SysRole> selectByPage(SysRole sysRole);

    List<String> selectRelationMenu(@Param(value = "roleId") Long roleId);
    List<Map<String, Object>> selectRelationRegion(@Param(value = "roleId") Long roleId);
    List<Map<String, Object>> selectRelationCamera(@Param(value = "roleId") Long roleId);
    List<Map<String, Object>> selectRelationDevice(@Param(value = "roleId") Long roleId);
    List<AreaInfo> selectRelationAuthor(@Param(value = "roleId") Long roleId);
    int updateRoleDevice(List<AreaInfo> list, Long roleId);
    List<Long> selectMonitorDeviceIdByRoleId(@Param(value = "roleId") Long roleId);
    Integer deleteMonitorDeviceRelationByRoleId(@Param(value = "roleId") Long roleId);
    Integer batchInsertMonitorDeviceRelation(List<SysRoleMonitorDevice> list);
}
