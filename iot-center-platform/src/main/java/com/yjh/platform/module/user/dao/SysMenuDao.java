package com.yjh.platform.module.user.dao;

import java.util.List;

import com.yjh.platform.module.user.entity.SysMenu;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-07-23
 */
@Repository
public interface SysMenuDao {

    int insert(SysMenu sysMenu);
    int deleteByPrimaryId(@Param(value = "menuId") Long menuId);
    int update(SysMenu sysMenu);
    SysMenu selectByPrimaryId(@Param(value = "menuId") Long menuId);
    List<SysMenu> select(@Param(value = "menuId") Long menuId,
                                @Param(value = "menuName") String menuName,
                                @Param(value = "menuCode") String menuCode,
                                @Param(value = "upId") Long upId,
                                @Param(value = "iconCode") String iconCode,
                                @Param(value = "iconUrl") String iconUrl,
                                @Param(value = "menuType") Integer menuType,
                                @Param(value = "menuLevel") Integer menuLevel,
                                @Param(value = "elementCode") String elementCode,
                                @Param(value = "state") Integer state,
                                @Param(value = "sort") Integer sort,
                                @Param(value = "linkType") Integer linkType,
                                @Param(value = "url") String url,
                                @Param(value = "creatorId") Long creatorId,
                                @Param(value = "sysState") Integer sysState);
    List<SysMenu> selectByPage(SysMenu sysMenu);
}
