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


    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleMenu sysRoleMenu) {
        return this.sysRoleMenuDao.insert(sysRoleMenu);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long rpId) {
        return this.sysRoleMenuDao.deleteByPrimaryId(rpId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByRoleId(Long roleId) {
        return this.sysRoleMenuDao.deleteByRoleId(roleId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleMenu sysRoleMenu) {
        return this.sysRoleMenuDao.update(sysRoleMenu);
    }


    @Transactional(rollbackFor = Exception.class)
    public SysRoleMenu selectByPrimaryId(Long rpId) {
        return this.sysRoleMenuDao.selectByPrimaryId(rpId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleMenu> select(Long rpId, String menuCode, Integer sort, String elementCode, Long roleId) {
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuDao.select(rpId, menuCode, sort, elementCode, roleId);
        return sysRoleMenuList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleMenu> selectByPage(SysRoleMenu sysRoleMenu) {
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuDao.selectByPage(sysRoleMenu);
        return sysRoleMenuList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleMenu> list) {
        return this.sysRoleMenuDao.batchInsert(list);
    }

}

