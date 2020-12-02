package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.SysRoleCamera;
import com.yjh.platform.module.user.dao.SysRoleCameraDao;

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
public class SysRoleCameraService{

    @Autowired
    private SysRoleCameraDao sysRoleCameraDao;

    @Logs(title = "插入", code = "module", content = "新增角色相机关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleCamera sysRoleCamera) {
        return this.sysRoleCameraDao.insert(sysRoleCamera);
    }

    @Logs(title = "删除", code = "module", content = "删除角色相机关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long roleId) {
        return this.sysRoleCameraDao.deleteByPrimaryId(roleId);
    }

    @Logs(title = "更新", code = "module", content = "更新角色相机关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleCamera sysRoleCamera) {
        return this.sysRoleCameraDao.update(sysRoleCamera);
    }

    @Logs(title = "主键查询", code = "module", content = "根据角色ID查询角色相机关联关系")
    @Transactional(rollbackFor = Exception.class)
    public SysRoleCamera selectByPrimaryId(Long roleId) {
        return this.sysRoleCameraDao.selectByPrimaryId(roleId);
    }

    @Logs(title = "查询", code = "module", content = "根据角色ID查询和相机ID角色相机关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleCamera> select(Long roleId, Long cameraId) {
        List<SysRoleCamera> sysRoleCameraList = sysRoleCameraDao.select(roleId, cameraId);
        return sysRoleCameraList;
    }

    @Logs(title = "分页查询", code = "module", content = "分页查询相机关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleCamera> selectByPage(SysRoleCamera sysRoleCamera) {
        List<SysRoleCamera> sysRoleCameraList = sysRoleCameraDao.selectByPage(sysRoleCamera);
        return sysRoleCameraList;
    }

    @Logs(title = "批量插入", code = "module", content = "批量新增角色相机关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleCamera> list) {
        return this.sysRoleCameraDao.batchInsert(list);
    }

}

