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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgTelesignal tCfgTelesignal) {
        return this.tCfgTelesignalDao.insert(tCfgTelesignal);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String deviceId) {
        return this.tCfgTelesignalDao.deleteByPrimaryId(deviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgTelesignal tCfgTelesignal) {
        return this.tCfgTelesignalDao.update(tCfgTelesignal);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgTelesignal selectByPrimaryId(String deviceId) {
        return this.tCfgTelesignalDao.selectByPrimaryId(deviceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelesignal> select(String deviceId, String meteId, String meteName, Integer upEffect, Integer lowEffect, Integer meteIndex, Integer meteCid, Integer signalKind, Integer lastValue, Date lastTime, Integer explainType, Integer reportType, Integer reportLevel, Integer maskType, String maskMid, String maskString, Integer maskValue, Integer delayTime, String meteCode, String deviceType, String description, Integer isshield, Long storageperiod, String describer, Integer alarmthresbhold, Integer alarmlevel, String linkMeteId) {
        List<TCfgTelesignal> tCfgTelesignalList = tCfgTelesignalDao.select(deviceId, meteId, meteName, upEffect, lowEffect, meteIndex, meteCid, signalKind, lastValue, lastTime, explainType, reportType, reportLevel, maskType, maskMid, maskString, maskValue, delayTime, meteCode, deviceType, description, isshield, storageperiod, describer, alarmthresbhold, alarmlevel, linkMeteId);
        return tCfgTelesignalList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelesignal> selectByPage(TCfgTelesignal tCfgTelesignal) {
        List<TCfgTelesignal> tCfgTelesignalList = tCfgTelesignalDao.selectByPage(tCfgTelesignal);
        return tCfgTelesignalList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgTelesignal> list) {
        return this.tCfgTelesignalDao.batchInsert(list);
    }

}

