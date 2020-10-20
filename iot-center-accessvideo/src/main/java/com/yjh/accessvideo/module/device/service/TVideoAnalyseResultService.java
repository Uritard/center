package com.yjh.accessvideo.module.device.service;

import com.yjh.accessvideo.module.device.entity.TVideoAnalyseResult;
import com.yjh.accessvideo.module.device.dao.TVideoAnalyseResultDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-10-19
*/
@Service
public class TVideoAnalyseResultService{

    @Autowired
    private TVideoAnalyseResultDao tVideoAnalyseResultDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TVideoAnalyseResult tVideoAnalyseResult) {
        return this.tVideoAnalyseResultDao.insert(tVideoAnalyseResult);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long algorithmResultId) {
        return this.tVideoAnalyseResultDao.deleteByPrimaryId(algorithmResultId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TVideoAnalyseResult tVideoAnalyseResult) {
        return this.tVideoAnalyseResultDao.update(tVideoAnalyseResult);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TVideoAnalyseResult selectByPrimaryId(Long algorithmResultId) {
        return this.tVideoAnalyseResultDao.selectByPrimaryId(algorithmResultId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TVideoAnalyseResult> select(Long algorithmResultId, Date analyseTime, String analyseConfId, String algorithmResult, String algorithmPicture, Integer algorithmStatus, String resultRate, String resultDescribe, Integer reserver) {
        List<TVideoAnalyseResult> tVideoAnalyseResultList = tVideoAnalyseResultDao.select(algorithmResultId, analyseTime, analyseConfId, algorithmResult, algorithmPicture, algorithmStatus, resultRate, resultDescribe, reserver);
        return tVideoAnalyseResultList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TVideoAnalyseResult> selectByPage(TVideoAnalyseResult tVideoAnalyseResult) {
        List<TVideoAnalyseResult> tVideoAnalyseResultList = tVideoAnalyseResultDao.selectByPage(tVideoAnalyseResult);
        return tVideoAnalyseResultList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TVideoAnalyseResult> list) {
        return this.tVideoAnalyseResultDao.batchInsert(list);
    }

}

