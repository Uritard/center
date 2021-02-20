package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-06
*/
@Service
public class TAlgorithmInfoService{

    @Autowired
    private TAlgorithmInfoDao tAlgorithmInfoDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TAlgorithmInfo tAlgorithmInfo) {
        return this.tAlgorithmInfoDao.insert(tAlgorithmInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long algorithmId) {
        {//算法已配置了预置位
            List<Long> list = tAlgorithmInfoDao.selectHaveDevice(algorithmId);
            if(list!= null && list.size()>0){
                return -1;
            }
        }
        return this.tAlgorithmInfoDao.deleteByPrimaryId(algorithmId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TAlgorithmInfo tAlgorithmInfo) {
        return this.tAlgorithmInfoDao.update(tAlgorithmInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public TAlgorithmInfo selectByPrimaryId(Long algorithmId) {
        return this.tAlgorithmInfoDao.selectByPrimaryId(algorithmId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmInfo> select(Long algorithmId, String algorithmName, String aliasName, String describel, String algorithmCode, String analyseType,Integer isAi) {
        if("-1".equals(analyseType)){
            analyseType = null;
        }
        List<TAlgorithmInfo> tAlgorithmInfoList = tAlgorithmInfoDao.select(algorithmId, algorithmName, aliasName, describel, algorithmCode, analyseType,isAi);
        return tAlgorithmInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmInfo> selectByPage(TAlgorithmInfo tAlgorithmInfo) {
        List<TAlgorithmInfo> tAlgorithmInfoList = tAlgorithmInfoDao.selectByPage(tAlgorithmInfo);
        return tAlgorithmInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TAlgorithmInfo> list) {
        for(TAlgorithmInfo item:list){
            List<Long> listHave = tAlgorithmInfoDao.selectHaveDevice(item.getAlgorithmId());
            if(listHave!= null && listHave.size()>0){
                return -1;
            }
        }
        return this.tAlgorithmInfoDao.batchInsert(list);
    }



}

