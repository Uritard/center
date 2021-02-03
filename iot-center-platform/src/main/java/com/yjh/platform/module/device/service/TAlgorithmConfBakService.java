package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TAlgorithmConfBak;
import com.yjh.platform.module.device.dao.TAlgorithmConfBakDao;

import java.util.List;
import java.util.Date;
import java.util.Arrays;

import com.yjh.platform.module.user.entity.TDictBusiness;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-12-22
*/
@Service
public class TAlgorithmConfBakService{

    @Autowired
    private TAlgorithmConfBakDao tAlgorithmConfBakDao;


    @Transactional(rollbackFor = Exception.class)
    public int add(TAlgorithmConfBak tAlgorithmConfBak) {
        return this.tAlgorithmConfBakDao.add(tAlgorithmConfBak);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceMeteId) {
        return this.tAlgorithmConfBakDao.deleteByPrimaryId(deviceMeteId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(TAlgorithmConfBak tAlgorithmConfBak) {
        return this.tAlgorithmConfBakDao.update(tAlgorithmConfBak);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmConfBak> selectByPrimaryId(Long deviceMeteId) {
        return this.tAlgorithmConfBakDao.selectByPrimaryId(deviceMeteId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmConfBak> select(Long deviceMeteId, Long algorithmId, String configName, Integer status, Integer ifDel, Integer ifShow, String picUrl, Integer applyModule, Date createTime, Date updateTime) {
        List<TAlgorithmConfBak> tAlgorithmConfBakList = tAlgorithmConfBakDao.select(deviceMeteId, algorithmId, configName, status, ifDel, ifShow, picUrl, applyModule, createTime, updateTime);
        return tAlgorithmConfBakList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmConfBak> selectByPage(TAlgorithmConfBak tAlgorithmConfBak) {
        List<TAlgorithmConfBak> tAlgorithmConfBakList = tAlgorithmConfBakDao.selectByPage(tAlgorithmConfBak);
        return tAlgorithmConfBakList;
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TAlgorithmConfBak> list) {
        return this.tAlgorithmConfBakDao.batchAdd(list);
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String deviceMeteId) {
    List<String> list1= Arrays.asList(deviceMeteId.split(","));
    return this.tAlgorithmConfBakDao.batchDelete(list1);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TDictBusiness> selectAnalyseType() {
        return this.tAlgorithmConfBakDao.selectAnalyseType();
    }


}

