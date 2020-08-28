package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TCruiseDataResult;
import com.yjh.platform.module.task.dao.TCruiseDataResultDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-25
*/
@Service
public class TCruiseDataResultService{

    @Autowired
    private TCruiseDataResultDao tCruiseDataResultDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseDataResult tCruiseDataResult) {
        return this.tCruiseDataResultDao.insert(tCruiseDataResult);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long cruiseDataId) {
        return this.tCruiseDataResultDao.deleteByPrimaryId(cruiseDataId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseDataResult tCruiseDataResult) {
        return this.tCruiseDataResultDao.update(tCruiseDataResult);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseDataResult selectByPrimaryId(Long cruiseDataId) {
        return this.tCruiseDataResultDao.selectByPrimaryId(cruiseDataId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResult> select(Long cruiseDataId, String cruiseResultId, Long cruiseId, Integer pointType, String resultDesc, String resultNum, String modifyNum, String picpath, String personcheck, String origpic, Integer state, String evaluationState, Integer identifyState, Integer identifyResult, Date createtime, String remark) {
        List<TCruiseDataResult> tCruiseDataResultList = tCruiseDataResultDao.select(cruiseDataId, cruiseResultId, cruiseId, pointType, resultDesc, resultNum, modifyNum, picpath, personcheck, origpic, state, evaluationState, identifyState, identifyResult, createtime, remark);
        return tCruiseDataResultList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResult> selectByPage(TCruiseDataResult tCruiseDataResult) {
        List<TCruiseDataResult> tCruiseDataResultList = tCruiseDataResultDao.selectByPage(tCruiseDataResult);
        return tCruiseDataResultList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseDataResult> list) {
        return this.tCruiseDataResultDao.batchInsert(list);
    }

}

