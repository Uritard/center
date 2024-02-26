package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Map;

import com.yjh.platform.module.user.entity.MenuForHave;
import com.yjh.platform.module.user.entity.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.user.entity.SystemMenuTreeNode;
import org.apache.ibatis.annotations.MapKey;
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
    List<String> selectByRoleId(@Param(value = "roleId") Long roleId, @Param(value = "menuType") Integer menuType);
    List<MenuForHave> selectOtherRoleHave(@Param(value = "roleId") Long roleId);
    List<SystemMenuTreeNode> selectTreeMenu(@Param("parentId")Long parentId);

    List<Map<String, String>> selectMenuFirstChild();
}
