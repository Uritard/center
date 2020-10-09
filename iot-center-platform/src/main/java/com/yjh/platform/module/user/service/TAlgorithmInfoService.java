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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TAlgorithmInfo tAlgorithmInfo) {
        return this.tAlgorithmInfoDao.insert(tAlgorithmInfo);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long algorithmId) {
        return this.tAlgorithmInfoDao.deleteByPrimaryId(algorithmId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TAlgorithmInfo tAlgorithmInfo) {
        return this.tAlgorithmInfoDao.update(tAlgorithmInfo);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TAlgorithmInfo selectByPrimaryId(Long algorithmId) {
        return this.tAlgorithmInfoDao.selectByPrimaryId(algorithmId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmInfo> select(Long algorithmId, String algorithmName, String aliasName, String describel, String algorithmCode, String analyseType) {
        if("-1".equals(analyseType)){
            analyseType = null;
        }
        List<TAlgorithmInfo> tAlgorithmInfoList = tAlgorithmInfoDao.select(algorithmId, algorithmName, aliasName, describel, algorithmCode, analyseType);
        return tAlgorithmInfoList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmInfo> selectByPage(TAlgorithmInfo tAlgorithmInfo) {
        List<TAlgorithmInfo> tAlgorithmInfoList = tAlgorithmInfoDao.selectByPage(tAlgorithmInfo);
        return tAlgorithmInfoList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TAlgorithmInfo> list) {
        return this.tAlgorithmInfoDao.batchInsert(list);
    }

}

