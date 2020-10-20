package com.yjh.accessvideo.module.device.service;


import com.yjh.accessvideo.module.device.dao.AnalyseDataOperateDao;
import com.yjh.accessvideo.module.device.entity.*;
import com.yjh.accessvideo.commons.logs.Logs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalyseDataOperateService {
    @Autowired
    private AnalyseDataOperateDao analyseDataOperateDao;

    @Logs(title = "告警信息插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertWarnInfo(TWarnInfo tWarnInfo){
       return this.analyseDataOperateDao.insertWarnInfo(tWarnInfo);
    }

    @Logs(title = "告警信息批量插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertWarnInfo(List<TWarnInfo> list){
        return this.analyseDataOperateDao.batchInsertWarnInfo(list);
    }

    @Logs(title = "算法结果插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertVideoAnalyseResult(TVideoAnalyseResult tVideoAnalyseResult){
        return this.analyseDataOperateDao.insertVideoAnalyseResult(tVideoAnalyseResult);
    }

    @Logs(title = "算法结果批量插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertVideoAnalyseResult(List<TVideoAnalyseResult> list){
        return this.analyseDataOperateDao.batchInsertVideoAnalyseResult(list);
    }

    @Logs(title = "任务结果信息插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseResult(TCruiseResult tCruiseResult){
        return this.analyseDataOperateDao.insertCruiseResult(tCruiseResult);
    }

    @Logs(title = "任务结果信息批量插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseResult(List<TCruiseResult> list){
        return this.analyseDataOperateDao.batchInsertCruiseResult(list);
    }

    @Logs(title = "巡视点-任务结果信息插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult){
        return this.analyseDataOperateDao.insertCruiseTaskResult(tCruiseTaskResult);
    }

    @Logs(title = "巡视点-任务结果信息批量插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseTaskResult(List<TCruiseTaskResult> list){
        return this.analyseDataOperateDao.batchInsertCruiseTaskResult(list);
    }

    @Logs(title = "巡视点详细结果信息插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseTaskResultDetail(TCruiseTaskResultDetail tCruiseTaskResultDetail){
        return this.analyseDataOperateDao.insertCruiseTaskResultDetail( tCruiseTaskResultDetail);
    }

    @Logs(title = "巡视点详细结果信息批量插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail>list){
        return this.analyseDataOperateDao.batchInsertCruiseTaskResultDetail(list);
    }

    @Logs(title = "巡视数据结果信息插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseDataResult(TCruiseDataResult tCruiseDataResult){
        return this.analyseDataOperateDao.insertCruiseDataResult(tCruiseDataResult);
    }

    @Logs(title = "巡视数据结果信息批量插入",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseDataResult(List<TCruiseDataResult>list){
        return this.analyseDataOperateDao.batchInsertCruiseDataResult(list);
    }

    @Logs(title = "标准测点信息查询",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TStdDevicemete selectByPrimaryIdDeviceMete(Long deviceMeteId){
        return this.analyseDataOperateDao.selectByPrimaryIdDeviceMete(deviceMeteId);
    }

    @Logs(title = "根据巡视点ID查询标准测点信息",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TStdDevicemete selectDeviceMeteByInstanceId(Long instanceId){
        return this.analyseDataOperateDao.selectDeviceMeteByInstanceId(instanceId);
    }


}

