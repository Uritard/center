package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TVideoAlgoResult;
import com.yjh.platform.module.task.dao.TVideoAlgoResultDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-24
*/
@Service
public class TVideoAlgoResultService{

    @Autowired
    private TVideoAlgoResultDao tVideoAlgoResultDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TVideoAlgoResult tVideoAlgoResult) {
        return this.tVideoAlgoResultDao.insert(tVideoAlgoResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long id) {
        return this.tVideoAlgoResultDao.deleteByPrimaryId(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TVideoAlgoResult tVideoAlgoResult) {
        return this.tVideoAlgoResultDao.update(tVideoAlgoResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public TVideoAlgoResult selectByPrimaryId(Long id) {
        return this.tVideoAlgoResultDao.selectByPrimaryId(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TVideoAlgoResult> select(Long pointId, String taskId, Long planId, Long deviceId, Long presetId, Long deviceMeteId, String picUrl, String cusId, Long algorithmId, String status, String analyseResult, String picOrignal, Integer evaluationState, String signpic, String algorithmType, String algorithmSonType, Date executeTime, Date createTime) {
        List<TVideoAlgoResult> tVideoAlgoResultList = tVideoAlgoResultDao.select(pointId, taskId, planId, deviceId, presetId, deviceMeteId, picUrl, cusId, algorithmId, status, analyseResult, picOrignal, evaluationState, signpic, algorithmType, algorithmSonType, executeTime, createTime);
        return tVideoAlgoResultList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TVideoAlgoResult> selectByPage(TVideoAlgoResult tVideoAlgoResult) {
        List<TVideoAlgoResult> tVideoAlgoResultList = tVideoAlgoResultDao.selectByPage(tVideoAlgoResult);
        return tVideoAlgoResultList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TVideoAlgoResult> list) {
        return this.tVideoAlgoResultDao.batchInsert(list);
    }

}

