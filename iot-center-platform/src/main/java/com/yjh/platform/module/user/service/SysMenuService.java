package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.SysMenu;
import com.yjh.platform.module.user.dao.SysMenuDao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-07-23
*/
@Service
public class SysMenuService{

    @Autowired
    private SysMenuDao sysMenuDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysMenu sysMenu) {
        return this.sysMenuDao.insert(sysMenu);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long menuId) {
        return this.sysMenuDao.deleteByPrimaryId(menuId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysMenu sysMenu) {
        return this.sysMenuDao.update(sysMenu);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public SysMenu selectByPrimaryId(Long menuId) {
        return this.sysMenuDao.selectByPrimaryId(menuId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysMenu> select(Long menuId, String menuName, String menuCode, Long upId, String iconCode, String iconUrl, Integer menuType, Integer menuLevel, String elementCode, Integer state, Integer sort, Integer linkType, String url, Long creatorId, Integer sysState) {
        List<SysMenu> sysMenuList = sysMenuDao.select(menuId, menuName, menuCode, upId, iconCode, iconUrl, menuType, menuLevel, elementCode, state, sort, linkType, url, creatorId, sysState);
        return sysMenuList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysMenu> selectByPage(SysMenu sysMenu) {
        List<SysMenu> sysMenuList = sysMenuDao.selectByPage(sysMenu);
        return sysMenuList;
    }

}

