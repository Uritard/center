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

    @Logs(title = "插入", code = "module", content = "新增角色机器人关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysRoleRobot sysRoleRobot) {
        return this.sysRoleRobotDao.insert(sysRoleRobot);
    }

    @Logs(title = "删除", code = "module", content = "删除角色机器人关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long roleId) {
        return this.sysRoleRobotDao.deleteByPrimaryId(roleId);
    }

    @Logs(title = "更新", code = "module", content = "更新角色机器人关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysRoleRobot sysRoleRobot) {
        return this.sysRoleRobotDao.update(sysRoleRobot);
    }

    @Logs(title = "主键查询", code = "module", content = "根据角色ID查询角色机器人关联关系")
    @Transactional(rollbackFor = Exception.class)
    public SysRoleRobot selectByPrimaryId(Long roleId) {
        return this.sysRoleRobotDao.selectByPrimaryId(roleId);
    }

    @Logs(title = "查询", code = "module", content = "查询角色机器人关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleRobot> select(Long roleId, Long robotId) {
        List<SysRoleRobot> sysRoleRobotList = sysRoleRobotDao.select(roleId, robotId);
        return sysRoleRobotList;
    }

    @Logs(title = "分页查询", code = "module", content = "分页查询角色机器人关联关系")
    @Transactional(rollbackFor = Exception.class)
    public List<SysRoleRobot> selectByPage(SysRoleRobot sysRoleRobot) {
        List<SysRoleRobot> sysRoleRobotList = sysRoleRobotDao.selectByPage(sysRoleRobot);
        return sysRoleRobotList;
    }

    @Logs(title = "批量插入", code = "module", content = "批量新增新增角色机器人关联关系")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysRoleRobot> list) {
        return this.sysRoleRobotDao.batchInsert(list);
    }

}

