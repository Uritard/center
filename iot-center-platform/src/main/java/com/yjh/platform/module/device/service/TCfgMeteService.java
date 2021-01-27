package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.*;
import com.yjh.platform.module.device.entity.*;

import java.util.List;
import java.util.Date;
import java.util.Map;

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
    @Autowired
    private TCfgDeviceDao tCfgDeviceDao;
    @Autowired
    private TCfgTeleadjustDao tCfgTeleadjustDao;
    @Autowired
    private TCfgTelecontrolDao tCfgTelecontrolDao;
    @Autowired
    private TCfgTelemeterDao tCfgTelemeterDao;
    @Autowired
    private TCfgTelesignalDao tCfgTelesignalDao;


    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgMete tCfgMete) {
        return this.tCfgMeteDao.insert(tCfgMete);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String meteId) {
        return this.tCfgMeteDao.deleteByPrimaryId(meteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgMete tCfgMete) {
        return this.tCfgMeteDao.update(tCfgMete);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCfgMete selectByPrimaryId(String meteId) {
        return this.tCfgMeteDao.selectByPrimaryId(meteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgMete> select(String meteId, String meteType, Integer meteKind, String meteName, String meteCode, String unit, String meteExplainType, Date createTime, Date updateTime, Integer modulus, String stationName, String stationId, Integer upEffect, Integer downEffect, Integer alarmlevel, Integer alarmthresbhold, String describer, Integer metePrecision, Float changeLimit, Float hilimit1, Float lolimit1, Float hilimit2, Float lolimit2, Float hilimit3, Float lolimit3, Float hilimit4, Float stander, Integer controlenable) {
        List<TCfgMete> tCfgMeteList = tCfgMeteDao.select(meteId, meteType, meteKind, meteName, meteCode, unit, meteExplainType, createTime, updateTime, modulus, stationName, stationId, upEffect, downEffect, alarmlevel, alarmthresbhold, describer, metePrecision, changeLimit, hilimit1, lolimit1, hilimit2, lolimit2, hilimit3, lolimit3, hilimit4, stander, controlenable);
        return tCfgMeteList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgMete> selectByPage(TCfgMete tCfgMete) {
        List<TCfgMete> tCfgMeteList = tCfgMeteDao.selectByPage(tCfgMete);
        return tCfgMeteList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgMete> list) {
        return this.tCfgMeteDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertForAll(Map<String, List<SYAllInfo>> syAllInfoMap) {
        List<SYAllInfo> syAllInfoList = syAllInfoMap.get("list");
        for (SYAllInfo syAllInfo:syAllInfoList) {
            this.tCfgMeteDao.insertForAll(syAllInfo);//mete
            this.tCfgDeviceDao.insertForAll(syAllInfo);//device
            if(syAllInfo.getMeteKind() == 1){//遥信
                tCfgTelesignalDao.insertForAll(syAllInfo);
            }
            if(syAllInfo.getMeteKind() == 2){//遥测
                tCfgTelemeterDao.insertForAll(syAllInfo);
            }
            if(syAllInfo.getMeteKind() == 3){//遥控
                tCfgTelecontrolDao.insertForAll(syAllInfo);
            }
            tCfgTeleadjustDao.insertForAll(syAllInfo);//遥调
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateForAll(Map<String, List<SYAllInfo>> syAllInfoMap) {
        List<SYAllInfo> syAllInfoList = syAllInfoMap.get("list");
        for (SYAllInfo syAllInfo:syAllInfoList) {
            this.tCfgMeteDao.updateForAll(syAllInfo);//mete
            this.tCfgDeviceDao.updateForAll(syAllInfo);//device
            if(syAllInfo.getMeteKind() == 1){//遥信
                tCfgTelesignalDao.updateForAll(syAllInfo);
            }
            if(syAllInfo.getMeteKind() == 2){//遥测
                tCfgTelemeterDao.updateForAll(syAllInfo);
            }
            if(syAllInfo.getMeteKind() == 3){//遥控
                tCfgTelecontrolDao.updateForAll(syAllInfo);
            }
            tCfgTeleadjustDao.updateForAll(syAllInfo);//遥调
        }
        return 1;
    }

}

