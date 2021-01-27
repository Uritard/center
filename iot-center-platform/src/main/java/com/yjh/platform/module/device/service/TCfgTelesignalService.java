package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TCfgTelesignal;
import com.yjh.platform.module.device.dao.TCfgTelesignalDao;

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
public class TCfgTelesignalService{

    @Autowired
    private TCfgTelesignalDao tCfgTelesignalDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgTelesignal tCfgTelesignal) {
        return this.tCfgTelesignalDao.insert(tCfgTelesignal);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String deviceId) {
        return this.tCfgTelesignalDao.deleteByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgTelesignal tCfgTelesignal) {
        return this.tCfgTelesignalDao.update(tCfgTelesignal);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCfgTelesignal selectByPrimaryId(String deviceId) {
        return this.tCfgTelesignalDao.selectByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelesignal> select(String deviceId, String meteId, String meteName, Integer upEffect, Integer downEffect, Integer meteIndex, Integer meteCid, Integer signalKind, Integer lastValue, Date lastTime, Integer explainType, Integer reportType, Integer reportLevel, Integer maskType, String maskMid, String maskString, Integer maskValue, Integer delayTime, String meteCode, String deviceType, String description, Integer isshield, Long storageperiod, String describer, Integer alarmthresbhold, Integer alarmlevel, String linkMeteId) {
        List<TCfgTelesignal> tCfgTelesignalList = tCfgTelesignalDao.select(deviceId, meteId, meteName, upEffect, downEffect, meteIndex, meteCid, signalKind, lastValue, lastTime, explainType, reportType, reportLevel, maskType, maskMid, maskString, maskValue, delayTime, meteCode, deviceType, description, isshield, storageperiod, describer, alarmthresbhold, alarmlevel, linkMeteId);
        return tCfgTelesignalList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelesignal> selectByPage(TCfgTelesignal tCfgTelesignal) {
        List<TCfgTelesignal> tCfgTelesignalList = tCfgTelesignalDao.selectByPage(tCfgTelesignal);
        return tCfgTelesignalList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgTelesignal> list) {
        return this.tCfgTelesignalDao.batchInsert(list);
    }

}

