package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.TAlgorithmConf;
import com.yjh.platform.module.user.dao.TAlgorithmConfDao;

import java.util.List;
import java.util.Date;

import com.yjh.platform.module.user.entity.TAlgorithmConfDetail;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-09-07
*/
@Service
public class TAlgorithmConfService{

    @Autowired
    private TAlgorithmConfDao tAlgorithmConfDao;

    @Transactional(rollbackFor = Exception.class)
    public int add(TAlgorithmConf tAlgorithmConf) {
        return this.tAlgorithmConfDao.add(tAlgorithmConf);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long presetId) {
        return this.tAlgorithmConfDao.deleteByPrimaryId(presetId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TAlgorithmConf tAlgorithmConf) {
        TAlgorithmConf tAlgorithmConfByselect = this.tAlgorithmConfDao.selectByPrimaryId(tAlgorithmConf.getPresetId());
        if(tAlgorithmConfByselect != null){
            return this.tAlgorithmConfDao.update(tAlgorithmConf);
        }
        return this.tAlgorithmConfDao.add(tAlgorithmConf);
    }

    @Transactional(rollbackFor = Exception.class)
    public TAlgorithmConf selectByPrimaryId(Long presetId) {
        return this.tAlgorithmConfDao.selectByPrimaryId(presetId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmConf> select(Long presetId, Long algorithmId, String configName, Integer status, Integer ifDel, Integer ifShow, String picUrl, Integer applyModule, Date createTime, Date updateTime) {
        List<TAlgorithmConf> tAlgorithmConfList = tAlgorithmConfDao.select(presetId, algorithmId, configName, status, ifDel, ifShow, picUrl, applyModule, createTime, updateTime);
        return tAlgorithmConfList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmConfDetail> selectByPage(TAlgorithmConfDetail tAlgorithmConfDetail) {
        if("-1".equals(tAlgorithmConfDetail.getAnalyseType())){
            tAlgorithmConfDetail.setAnalyseType(null);
        }
        List<TAlgorithmConfDetail> tAlgorithmConfList = tAlgorithmConfDao.selectByPage(tAlgorithmConfDetail);
        return tAlgorithmConfList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TAlgorithmConf> list) {
        return this.tAlgorithmConfDao.batchAdd(list);
    }

}

