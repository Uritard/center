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


    @Logs(title = "插入", code = "module", content = "新增角色区域关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleRegion sysRoleRegion) {
        return this.sysRoleRegionDao.insert(sysRoleRegion);
    }


    @Logs(title = "删除", code = "module", content = "删除角色区域关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long roleId) {
        return this.sysRoleRegionDao.deleteByPrimaryId(roleId);
    }


    @Logs(title = "更新", code = "module", content = "更新角色区域关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleRegion sysRoleRegion) {
        return this.sysRoleRegionDao.update(sysRoleRegion);
    }


    @Logs(title = "主键查询", code = "module", content = "根据角色ID查询角色区域关联关系")
    @Transactional(rollbackFor = Exception.class)
    public SysRoleRegion selectByPrimaryId(Long roleId) {
        return this.sysRoleRegionDao.selectByPrimaryId(roleId);
    }


    @Logs(title = "查询", code = "module", content = "根据角色ID和区域ID查询角色区域关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleRegion> select(Long roleId, Long regionId) {
        List<SysRoleRegion> sysRoleRegionList = sysRoleRegionDao.select(roleId, regionId);
        return sysRoleRegionList;
    }


    @Logs(title = "分页查询", code = "module", content = "分页查询角色区域关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleRegion> selectByPage(SysRoleRegion sysRoleRegion) {
        List<SysRoleRegion> sysRoleRegionList = sysRoleRegionDao.selectByPage(sysRoleRegion);
        return sysRoleRegionList;
    }

    @Logs(title = "批量插入", code = "module", content = "批量新增角色区域关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleRegion> list) {
        return this.sysRoleRegionDao.batchInsert(list);
    }

}

