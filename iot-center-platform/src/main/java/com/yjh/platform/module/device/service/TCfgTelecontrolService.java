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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgTelecontrol tCfgTelecontrol) {
        return this.tCfgTelecontrolDao.insert(tCfgTelecontrol);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String deviceId) {
        return this.tCfgTelecontrolDao.deleteByPrimaryId(deviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgTelecontrol tCfgTelecontrol) {
        return this.tCfgTelecontrolDao.update(tCfgTelecontrol);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgTelecontrol selectByPrimaryId(String deviceId) {
        return this.tCfgTelecontrolDao.selectByPrimaryId(deviceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelecontrol> select(String deviceId, String meteId, String meteName, Integer meteIndex, Integer meteCid, Integer controlStatus, String enableString, String succeedString, String triggerString, Integer controlValue, String meteCode, String deviceType, String description, String describer) {
        List<TCfgTelecontrol> tCfgTelecontrolList = tCfgTelecontrolDao.select(deviceId, meteId, meteName, meteIndex, meteCid, controlStatus, enableString, succeedString, triggerString, controlValue, meteCode, deviceType, description, describer);
        return tCfgTelecontrolList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgTelecontrol> selectByPage(TCfgTelecontrol tCfgTelecontrol) {
        List<TCfgTelecontrol> tCfgTelecontrolList = tCfgTelecontrolDao.selectByPage(tCfgTelecontrol);
        return tCfgTelecontrolList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgTelecontrol> list) {
        return this.tCfgTelecontrolDao.batchInsert(list);
    }

}

