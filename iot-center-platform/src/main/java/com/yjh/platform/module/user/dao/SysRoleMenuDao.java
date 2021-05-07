package com.yjh.platform.module.user.dao;

import java.util.List;

import com.yjh.platform.module.user.entity.MenuForHave;
import com.yjh.platform.module.user.entity.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface SysRoleMenuDao {

    int insert(SysRoleMenu sysRoleMenu);
    int deleteByPrimaryId(@Param(value = "rpId") Long rpId);
    int deleteByRoleId(@Param(value = "roleId") Long roleId);
    int update(SysRoleMenu sysRoleMenu);
    SysRoleMenu selectByPrimaryId(@Param(value = "rpId") Long rpId);
    List<SysRoleMenu> select(@Param(value = "rpId") Long rpId,
                             @Param(value = "menuCode") String menuCode,
                             @Param(value = "sort") Integer sort,
                             @Param(value = "elementCode") String elementCode,
                             @Param(value = "roleId") Long roleId);
    List<SysRoleMenu> selectByPage(SysRoleMenu sysRoleMenu);

    int batchInsert(@Param("list") List<SysRoleMenu> list);
    List<String> selectByRoleId(@Param(value = "roleId") Long roleId);
    List<MenuForHave> selectOtherRoleHave(@Param(value = "roleId") Long roleId);
}
