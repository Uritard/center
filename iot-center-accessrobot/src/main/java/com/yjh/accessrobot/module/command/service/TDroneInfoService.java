package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TDroneInfoDao;
import com.yjh.accessrobot.module.command.entity.TDroneInfo;
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
public class TDroneInfoService {

    @Autowired
    private TDroneInfoDao tDroneInfoDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TDroneInfo tDroneInfo) {
        return this.tDroneInfoDao.insert(tDroneInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long droneId) {
        return this.tDroneInfoDao.deleteByPrimaryId(droneId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TDroneInfo tDroneInfo) {
        return this.tDroneInfoDao.update(tDroneInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public TDroneInfo selectByPrimaryId(Long droneId) {
        return this.tDroneInfoDao.selectByPrimaryId(droneId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TDroneInfo> select(Long droneId, String droneCode, String droneName, String droneStatus, Integer droneType, String droneIp, Integer dronePort, String lightIp, String lightPort, String lightUsername, String lightPassword, String lnferadIp, Integer inferadPort, String inferadUsername, String inferadPassword, String photePath, String createBy, Date createDate, String updateBy, Date updateDate, String droneFactory, String isUse, Date commissionDate, Long upRegionId, String dronePosition, String remarks) {
//        List<TDroneInfo> tDroneInfoList = tDroneInfoDao.select(droneId, droneCode, droneName, droneStatus, droneType, droneIp, dronePort, lightIp, lightPort, lightUsername, lightPassword, lnferadIp, inferadPort, inferadUsername, inferadPassword, photePath, createBy, createDate, updateBy, updateDate, droneFactory, isUse, commissionDate, upRegionId, dronePosition, remarks);
//        return tDroneInfoList;
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TDroneInfo> selectByPage(TDroneInfo tDroneInfo) {
        List<TDroneInfo> tDroneInfoList = tDroneInfoDao.selectByPage(tDroneInfo);
        return tDroneInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TDroneInfo> list) {
        return this.tDroneInfoDao.batchInsert(list);
    }

}

