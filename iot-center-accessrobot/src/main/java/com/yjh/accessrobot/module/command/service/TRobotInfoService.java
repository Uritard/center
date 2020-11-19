package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.commons.logs.Logs;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author YC
* @since 2020-11-19
*/
@Service
public class TRobotInfoService {

    @Autowired
    private TRobotInfoDao tRobotInfoDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInfo tRobotInfo) {
        return this.tRobotInfoDao.insert(tRobotInfo);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.deleteByPrimaryId(robotId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInfo tRobotInfo) {
        return this.tRobotInfoDao.update(tRobotInfo);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TRobotInfo selectByPrimaryId(Long robotId) {
        return this.tRobotInfoDao.selectByPrimaryId(robotId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> select(Long robotId, String robotCode, String robotName, String robotStatus, Integer robotType, String robotIp, Integer robotPort, String lightIp, String lightPort, String lightUsername, String lightPassword, String lnferadIp, Integer inferadPort, String inferadUsername, String inferadPassword, String photePath, String createBy, Date createDate, String updateBy, Date updateDate, String robotFactory, String isUse, Date commissionDate, Long upRegionId, String robotPosition, String remarks) {
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.select(robotId, robotCode, robotName, robotStatus, robotType, robotIp, robotPort, lightIp, lightPort, lightUsername, lightPassword, lnferadIp, inferadPort, inferadUsername, inferadPassword, photePath, createBy, createDate, updateBy, updateDate, robotFactory, isUse, commissionDate, upRegionId, robotPosition, remarks);
        return tRobotInfoList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInfo> selectByPage(TRobotInfo tRobotInfo) {
        List<TRobotInfo> tRobotInfoList = tRobotInfoDao.selectByPage(tRobotInfo);
        return tRobotInfoList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInfo> list) {
        return this.tRobotInfoDao.batchInsert(list);
    }

}

