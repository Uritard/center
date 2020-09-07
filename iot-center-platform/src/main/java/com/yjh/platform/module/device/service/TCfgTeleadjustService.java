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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgTeleadjust tCfgTeleadjust) {
        return this.tCfgTeleadjustDao.insert(tCfgTeleadjust);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String deviceId) {
        return this.tCfgTeleadjustDao.deleteByPrimaryId(deviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgTeleadjust tCfgTeleadjust) {
        return this.tCfgTeleadjustDao.update(tCfgTeleadjust);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgTeleadjust selectByPrimaryId(String deviceId) {
        return this.tCfgTeleadjustDao.selectByPrimaryId(deviceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTeleadjust> select(String deviceId, String meteId, String meteName, Float upEffect, Float downEffect, Integer metePrecision, String unit, Integer meteIndex, Integer meteCid, Integer adjustKind, Float lastValue, Date lastTime, String meteCode, String deviceType, String description, Float stander, Integer controlenable) {
        List<TCfgTeleadjust> tCfgTeleadjustList = tCfgTeleadjustDao.select(deviceId, meteId, meteName, upEffect, downEffect, metePrecision, unit, meteIndex, meteCid, adjustKind, lastValue, lastTime, meteCode, deviceType, description, stander, controlenable);
        return tCfgTeleadjustList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTeleadjust> selectByPage(TCfgTeleadjust tCfgTeleadjust) {
        List<TCfgTeleadjust> tCfgTeleadjustList = tCfgTeleadjustDao.selectByPage(tCfgTeleadjust);
        return tCfgTeleadjustList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgTeleadjust> list) {
        return this.tCfgTeleadjustDao.batchInsert(list);
    }

}

