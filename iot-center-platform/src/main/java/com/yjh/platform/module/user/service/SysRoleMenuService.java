package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.SysRoleMenu;
import com.yjh.platform.module.user.dao.SysRoleMenuDao;

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
public class SysRoleMenuService{

    @Autowired
    private SysRoleMenuDao sysRoleMenuDao;


    @Logs(title = "插入", code = "module", content = "新增角色菜单关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleMenu sysRoleMenu) {
        return this.sysRoleMenuDao.insert(sysRoleMenu);
    }


    @Logs(title = "删除", code = "module", content = "删除角色菜单关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long rpId) {
        return this.sysRoleMenuDao.deleteByPrimaryId(rpId);
    }

    @Logs(title = "主键删除", code = "module", content = "根据角色ID删除角色菜单关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByRoleId(Long roleId) {
        return this.sysRoleMenuDao.deleteByRoleId(roleId);
    }


    @Logs(title = "更新", code = "module", content = "更新角色菜单关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleMenu sysRoleMenu) {
        return this.sysRoleMenuDao.update(sysRoleMenu);
    }


    @Logs(title = "主键查询", code = "module", content = "根据角色ID查询角色菜单关联关系")
    @Transactional(rollbackFor = Exception.class)
    public SysRoleMenu selectByPrimaryId(Long rpId) {
        return this.sysRoleMenuDao.selectByPrimaryId(rpId);
    }


    @Logs(title = "查询", code = "module", content = "查询角色菜单关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleMenu> select(Long rpId, String menuCode, Integer sort, String elementCode, Long roleId) {
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuDao.select(rpId, menuCode, sort, elementCode, roleId);
        return sysRoleMenuList;
    }


    @Logs(title = "分页查询", code = "module", content = "分页查询角色菜单关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleMenu> selectByPage(SysRoleMenu sysRoleMenu) {
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuDao.selectByPage(sysRoleMenu);
        return sysRoleMenuList;
    }

    @Logs(title = "插入", code = "module", content = "批量新增角色菜单关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleMenu> list) {
        return this.sysRoleMenuDao.batchInsert(list);
    }

}

