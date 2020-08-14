package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TStdMete;
import com.yjh.platform.module.device.dao.TStdMeteDao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-07
*/
@Service
public class TStdMeteService{

    @Autowired
    private TStdMeteDao tStdMeteDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdMete tStdMete) {
        return this.tStdMeteDao.insert(tStdMete);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long stdMeteId) {
        return this.tStdMeteDao.deleteByPrimaryId(stdMeteId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdMete tStdMete) {
        return this.tStdMeteDao.update(tStdMete);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdMete selectByPrimaryId(Long stdMeteId) {
        return this.tStdMeteDao.selectByPrimaryId(stdMeteId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMete> select(Long stdMeteId, Integer deviceType, String meteCode, String meteType, String meteName, String alarmNote, String alarmExplain, String alarmType, String unit, Float upEffect, Float lowEffect, Integer alarmLevel, Integer alarmLimit, Integer alarmDelay, Integer alarmCnt, BigDecimal thresholdAbs, BigDecimal thresholdPer, Integer modulus, String remark) {
        List<TStdMete> tStdMeteList = tStdMeteDao.select(stdMeteId, deviceType, meteCode, meteType, meteName, alarmNote, alarmExplain, alarmType, unit, upEffect, lowEffect, alarmLevel, alarmLimit, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark);
        return tStdMeteList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMete> selectByPage(TStdMete tStdMete) {
        List<TStdMete> tStdMeteList = tStdMeteDao.selectByPage(tStdMete);
        return tStdMeteList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TStdMete> list) {
        return this.tStdMeteDao.batchInsert(list);
    }

}

