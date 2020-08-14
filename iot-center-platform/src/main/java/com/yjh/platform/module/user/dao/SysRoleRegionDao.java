package com.yjh.platform.module.user.dao;

import java.util.List;

import com.yjh.platform.module.user.entity.SysRoleRegion;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-31
 */
@Repository
public interface SysRoleRegionDao {

    int insert(SysRoleRegion sysRoleRegion);
    int deleteByPrimaryId(@Param(value = "roleId") Long roleId);
    int update(SysRoleRegion sysRoleRegion);
    SysRoleRegion selectByPrimaryId(@Param(value = "roleId") Long roleId);
    List<SysRoleRegion> select(@Param(value = "roleId") Long roleId,
                                @Param(value = "regionId") Long regionId);
    List<SysRoleRegion> selectByPage(SysRoleRegion sysRoleRegion);

    int batchInsert(@Param("list") List<SysRoleRegion> list);
}
