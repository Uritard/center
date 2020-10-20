package com.yjh.accessvideo.module.device.service;

import com.yjh.accessvideo.module.device.entity.TCruiseDataResult;
import com.yjh.accessvideo.module.device.dao.TCruiseDataResultDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-10-20
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
    public List<TCruiseDataResult> select(Long cruiseDataId, String cruiseResultId, Long cruiseId, Integer cruiseType, String resultDesc, String resultNum, String modifyNum, String picpath, String personCheck, String origpic, Integer state, Integer evaluationState, Integer identifyState, Integer identifyResult, Date createtime, String remark, String checkUser, Date checkDate) {
        List<TCruiseDataResult> tCruiseDataResultList = tCruiseDataResultDao.select(cruiseDataId, cruiseResultId, cruiseId, cruiseType, resultDesc, resultNum, modifyNum, picpath, personCheck, origpic, state, evaluationState, identifyState, identifyResult, createtime, remark, checkUser, checkDate);
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

