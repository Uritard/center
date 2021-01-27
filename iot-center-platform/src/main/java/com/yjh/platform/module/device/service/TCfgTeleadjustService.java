package com.yjh.platform.module.device.service;


import com.yjh.platform.module.device.dao.TCfgTeleadjustDao;
import java.util.List;
import java.util.Date;

import com.yjh.platform.module.device.entity.TCfgTeleadjust;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-08-25
*/
@Service
public class TCfgTeleadjustService{

    @Autowired
    private TCfgTeleadjustDao tCfgTeleadjustDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgTeleadjust tCfgTeleadjust) {
        return this.tCfgTeleadjustDao.insert(tCfgTeleadjust);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String deviceId) {
        return this.tCfgTeleadjustDao.deleteByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgTeleadjust tCfgTeleadjust) {
        return this.tCfgTeleadjustDao.update(tCfgTeleadjust);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCfgTeleadjust selectByPrimaryId(String deviceId) {
        return this.tCfgTeleadjustDao.selectByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTeleadjust> select(String deviceId, String meteId, String meteName, Float upEffect, Float downEffect, Integer metePrecision, String unit, Integer meteIndex, Integer meteCid, Integer adjustKind, Float lastValue, Date lastTime, String meteCode, String deviceType, String description, Float stander, Integer controlenable) {
        List<TCfgTeleadjust> tCfgTeleadjustList = tCfgTeleadjustDao.select(deviceId, meteId, meteName, upEffect, downEffect, metePrecision, unit, meteIndex, meteCid, adjustKind, lastValue, lastTime, meteCode, deviceType, description, stander, controlenable);
        return tCfgTeleadjustList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTeleadjust> selectByPage(TCfgTeleadjust tCfgTeleadjust) {
        List<TCfgTeleadjust> tCfgTeleadjustList = tCfgTeleadjustDao.selectByPage(tCfgTeleadjust);
        return tCfgTeleadjustList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgTeleadjust> list) {
        return this.tCfgTeleadjustDao.batchInsert(list);
    }

}

