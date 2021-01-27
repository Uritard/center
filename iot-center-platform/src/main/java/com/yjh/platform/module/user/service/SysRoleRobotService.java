package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.entity.SysRoleRobot;
import com.yjh.platform.module.user.dao.SysRoleRobotDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author tt
* @since 2020-07-23
*/
@Service
public class SysRoleRobotService{

    @Autowired
    private SysRoleRobotDao sysRoleRobotDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleRobot sysRoleRobot) {
        return this.sysRoleRobotDao.insert(sysRoleRobot);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long roleId) {
        return this.sysRoleRobotDao.deleteByPrimaryId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleRobot sysRoleRobot) {
        return this.sysRoleRobotDao.update(sysRoleRobot);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysRoleRobot selectByPrimaryId(Long roleId) {
        return this.sysRoleRobotDao.selectByPrimaryId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleRobot> select(Long roleId, Long robotId) {
        List<SysRoleRobot> sysRoleRobotList = sysRoleRobotDao.select(roleId, robotId);
        return sysRoleRobotList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleRobot> selectByPage(SysRoleRobot sysRoleRobot) {
        List<SysRoleRobot> sysRoleRobotList = sysRoleRobotDao.selectByPage(sysRoleRobot);
        return sysRoleRobotList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleRobot> list) {
        return this.sysRoleRobotDao.batchInsert(list);
    }

}

