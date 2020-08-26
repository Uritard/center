package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TCfgTelemeter;
import com.yjh.platform.module.device.dao.TCfgTelemeterDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-08-25
*/
@Service
public class TCfgTelemeterService{

    @Autowired
    private TCfgTelemeterDao tCfgTelemeterDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgTelemeter tCfgTelemeter) {
        return this.tCfgTelemeterDao.insert(tCfgTelemeter);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String deviceId) {
        return this.tCfgTelemeterDao.deleteByPrimaryId(deviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgTelemeter tCfgTelemeter) {
        return this.tCfgTelemeterDao.update(tCfgTelemeter);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgTelemeter selectByPrimaryId(String deviceId) {
        return this.tCfgTelemeterDao.selectByPrimaryId(deviceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelemeter> select(String deviceId, String meteId, String meteName, Float upEffect, Float lowEffect, Integer metePrecision, String unit, Integer meteIndex, Integer meteCid, Float limitBand, Float changeLimit, String validMid, String validString, Float invalidValue, Float lastValue, Date lastTime, String meteCode, String deviceType, String description, Float hilimit1, Float lolimit1, Float hilimit2, Float lolimit2, Float hilimit3, Float lolimit3, Float hilimit4, Float lolimit4, Float stander, Integer isshield, Long storageperiod, String linkMeteId) {
        List<TCfgTelemeter> tCfgTelemeterList = tCfgTelemeterDao.select(deviceId, meteId, meteName, upEffect, lowEffect, metePrecision, unit, meteIndex, meteCid, limitBand, changeLimit, validMid, validString, invalidValue, lastValue, lastTime, meteCode, deviceType, description, hilimit1, lolimit1, hilimit2, lolimit2, hilimit3, lolimit3, hilimit4, lolimit4, stander, isshield, storageperiod, linkMeteId);
        return tCfgTelemeterList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelemeter> selectByPage(TCfgTelemeter tCfgTelemeter) {
        List<TCfgTelemeter> tCfgTelemeterList = tCfgTelemeterDao.selectByPage(tCfgTelemeter);
        return tCfgTelemeterList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgTelemeter> list) {
        return this.tCfgTelemeterDao.batchInsert(list);
    }

}

