package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.SysRoleRegion;
import com.yjh.platform.module.user.dao.SysRoleRegionDao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-07-31
*/
@Service
public class SysRoleRegionService{

    @Autowired
    private SysRoleRegionDao sysRoleRegionDao;


    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleRegion sysRoleRegion) {
        return this.sysRoleRegionDao.insert(sysRoleRegion);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long roleId) {
        return this.sysRoleRegionDao.deleteByPrimaryId(roleId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleRegion sysRoleRegion) {
        return this.sysRoleRegionDao.update(sysRoleRegion);
    }


    @Transactional(rollbackFor = Exception.class)
    public SysRoleRegion selectByPrimaryId(Long roleId) {
        return this.sysRoleRegionDao.selectByPrimaryId(roleId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleRegion> select(Long roleId, Long regionId) {
        List<SysRoleRegion> sysRoleRegionList = sysRoleRegionDao.select(roleId, regionId);
        return sysRoleRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleRegion> selectByPage(SysRoleRegion sysRoleRegion) {
        List<SysRoleRegion> sysRoleRegionList = sysRoleRegionDao.selectByPage(sysRoleRegion);
        return sysRoleRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleRegion> list) {
        return this.sysRoleRegionDao.batchInsert(list);
    }

}

