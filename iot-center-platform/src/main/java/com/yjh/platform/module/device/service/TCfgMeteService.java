package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TCfgMete;
import com.yjh.platform.module.device.dao.TCfgMeteDao;

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
public class TCfgMeteService{

    @Autowired
    private TCfgMeteDao tCfgMeteDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgMete tCfgMete) {
        return this.tCfgMeteDao.insert(tCfgMete);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String meteId) {
        return this.tCfgMeteDao.deleteByPrimaryId(meteId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgMete tCfgMete) {
        return this.tCfgMeteDao.update(tCfgMete);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgMete selectByPrimaryId(String meteId) {
        return this.tCfgMeteDao.selectByPrimaryId(meteId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgMete> select(String meteId, String meteType, Integer meteKind, String meteName, String meteCode, String unit, String meteExplainType, Date createTime, Date updateTime, Integer modulus, String stationName, String stationId, Integer upEffect, Integer lowEffect, Integer alarmlevel, Integer alarmthresbhold, String describer, Integer metePrecision, Float changeLimit, Float hilimit1, Float lolimit1, Float hilimit2, Float lolimit2, Float hilimit3, Float lolimit3, Float hilimit4, Float stander, Integer controlenable) {
        List<TCfgMete> tCfgMeteList = tCfgMeteDao.select(meteId, meteType, meteKind, meteName, meteCode, unit, meteExplainType, createTime, updateTime, modulus, stationName, stationId, upEffect, lowEffect, alarmlevel, alarmthresbhold, describer, metePrecision, changeLimit, hilimit1, lolimit1, hilimit2, lolimit2, hilimit3, lolimit3, hilimit4, stander, controlenable);
        return tCfgMeteList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgMete> selectByPage(TCfgMete tCfgMete) {
        List<TCfgMete> tCfgMeteList = tCfgMeteDao.selectByPage(tCfgMete);
        return tCfgMeteList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgMete> list) {
        return this.tCfgMeteDao.batchInsert(list);
    }

}

