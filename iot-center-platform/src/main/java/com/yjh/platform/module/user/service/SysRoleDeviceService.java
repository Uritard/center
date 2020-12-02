package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.SysRoleDevice;
import com.yjh.platform.module.user.dao.SysRoleDeviceDao;

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
public class SysRoleDeviceService{

    @Autowired
    private SysRoleDeviceDao sysRoleDeviceDao;

    @Logs(title = "插入", code = "module", content = "新增角色设备关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleDevice sysRoleDevice) {
        return this.sysRoleDeviceDao.insert(sysRoleDevice);
    }

    @Logs(title = "删除", code = "module", content = "删除角色设备关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long roleId) {
        return this.sysRoleDeviceDao.deleteByPrimaryId(roleId);
    }

    @Logs(title = "更新", code = "module", content = "更新角色设备关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleDevice sysRoleDevice) {
        return this.sysRoleDeviceDao.update(sysRoleDevice);
    }

    @Logs(title = "主键查询", code = "module", content = "根据角色ID查询角色设备关联关系")
    @Transactional(rollbackFor = Exception.class)
    public SysRoleDevice selectByPrimaryId(Long roleId) {
        return this.sysRoleDeviceDao.selectByPrimaryId(roleId);
    }

    @Logs(title = "查询", code = "module", content = "根据角色ID和设备ID查询角色设备关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleDevice> select(Long roleId, Long deviceId) {
        List<SysRoleDevice> sysRoleDeviceList = sysRoleDeviceDao.select(roleId, deviceId);
        return sysRoleDeviceList;
    }

    @Logs(title = "分页查询", code = "module", content = "分页查询角色设备关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleDevice> selectByPage(SysRoleDevice sysRoleDevice) {
        List<SysRoleDevice> sysRoleDeviceList = sysRoleDeviceDao.selectByPage(sysRoleDevice);
        return sysRoleDeviceList;
    }

    @Logs(title = "批量插入", code = "module", content = "批量新增角色设备关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleDevice> list) {
        return this.sysRoleDeviceDao.batchInsert(list);
    }

}

