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


    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleDevice sysRoleDevice) {
        return this.sysRoleDeviceDao.insert(sysRoleDevice);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long roleId) {
        return this.sysRoleDeviceDao.deleteByPrimaryId(roleId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleDevice sysRoleDevice) {
        return this.sysRoleDeviceDao.update(sysRoleDevice);
    }


    @Transactional(rollbackFor = Exception.class)
    public SysRoleDevice selectByPrimaryId(Long roleId) {
        return this.sysRoleDeviceDao.selectByPrimaryId(roleId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleDevice> select(Long roleId, Long deviceId) {
        List<SysRoleDevice> sysRoleDeviceList = sysRoleDeviceDao.select(roleId, deviceId);
        return sysRoleDeviceList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleDevice> selectByPage(SysRoleDevice sysRoleDevice) {
        List<SysRoleDevice> sysRoleDeviceList = sysRoleDeviceDao.selectByPage(sysRoleDevice);
        return sysRoleDeviceList;
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleDevice> list) {
        return this.sysRoleDeviceDao.batchInsert(list);
    }

}

