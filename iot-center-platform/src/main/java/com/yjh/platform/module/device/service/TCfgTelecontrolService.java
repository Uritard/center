package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TCfgTelecontrol;
import com.yjh.platform.module.device.dao.TCfgTelecontrolDao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-08-25
*/
@Service
public class TCfgTelecontrolService{

    @Autowired
    private TCfgTelecontrolDao tCfgTelecontrolDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgTelecontrol tCfgTelecontrol) {
        return this.tCfgTelecontrolDao.insert(tCfgTelecontrol);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String deviceId) {
        return this.tCfgTelecontrolDao.deleteByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgTelecontrol tCfgTelecontrol) {
        return this.tCfgTelecontrolDao.update(tCfgTelecontrol);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCfgTelecontrol selectByPrimaryId(String deviceId) {
        return this.tCfgTelecontrolDao.selectByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelecontrol> select(String deviceId, String meteId, String meteName, Integer meteIndex, Integer meteCid, Integer controlStatus, String enableString, String succeedString, String triggerString, Integer controlValue, String meteCode, String deviceType, String description, String describer) {
        List<TCfgTelecontrol> tCfgTelecontrolList = tCfgTelecontrolDao.select(deviceId, meteId, meteName, meteIndex, meteCid, controlStatus, enableString, succeedString, triggerString, controlValue, meteCode, deviceType, description, describer);
        return tCfgTelecontrolList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelecontrol> selectByPage(TCfgTelecontrol tCfgTelecontrol) {
        List<TCfgTelecontrol> tCfgTelecontrolList = tCfgTelecontrolDao.selectByPage(tCfgTelecontrol);
        return tCfgTelecontrolList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgTelecontrol> list) {
        return this.tCfgTelecontrolDao.batchInsert(list);
    }

}

