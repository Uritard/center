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

    @Logs(title = "告警信息插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertWarnInfo(TWarnInfo tWarnInfo) {
        return this.analyseDataOperateDao.insertWarnInfo(tWarnInfo);
    }

    @Logs(title = "告警信息批量插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertWarnInfo(List<TWarnInfo> list) {
        return this.analyseDataOperateDao.batchInsertWarnInfo(list);
    }

    @Logs(title = "算法结果插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertVideoAnalyseResult(TVideoAnalyseResult tVideoAnalyseResult) {
        return this.analyseDataOperateDao.insertVideoAnalyseResult(tVideoAnalyseResult);
    }

    @Logs(title = "算法结果批量插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertVideoAnalyseResult(List<TVideoAnalyseResult> list) {
        return this.analyseDataOperateDao.batchInsertVideoAnalyseResult(list);
    }

    @Logs(title = "巡视任务结果单查",code = "Analysis")
    @Transactional(rollbackFor =Exception.class)
    public TCruiseResult selectByPrimaryIdCruiseResult(String taskResultId){
        return this.analyseDataOperateDao.selectByPrimaryIdCruiseResult(taskResultId);
    }

    @Logs(title = "巡视任务结果修改",code = "")
    @Transactional(rollbackFor = Exception.class)
    public int updateCruiseResult(TCruiseResult tCruiseResult){
        return this.analyseDataOperateDao.updateCruiseResult(tCruiseResult);
    }


    @Logs(title = "任务-巡视点状态结果单查",code = "")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskResult selectByPrimaryIdCruiseTaskResult(String taskResultId){
        return this.analyseDataOperateDao.selectByPrimaryIdCruiseTaskResult(taskResultId);
    }

    @Logs(title ="任务-巡视点状态结构更新",code = "")
    @Transactional(rollbackFor = Exception.class)
    public int updateCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult){
        return this.analyseDataOperateDao.updateCruiseTaskResult(tCruiseTaskResult);
    }

    @Logs(title = "任务-巡视点状态结构新增插入",code = "")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult){
        return this.analyseDataOperateDao.insertCruiseTaskResult(tCruiseTaskResult);
    }

    @Logs(title = "巡视点详细结果信息插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseTaskResultDetail(TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        return this.analyseDataOperateDao.insertCruiseTaskResultDetail(tCruiseTaskResultDetail);
    }

    @Logs(title = "巡视点详细结果信息批量插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> list) {
        return this.analyseDataOperateDao.batchInsertCruiseTaskResultDetail(list);
    }

    @Logs(title = "巡视数据结果信息插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseDataResult(TCruiseDataResult tCruiseDataResult) {
        return this.analyseDataOperateDao.insertCruiseDataResult(tCruiseDataResult);
    }

    @Logs(title = "巡视数据结果信息批量插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseDataResult(List<TCruiseDataResult> list) {
        return this.analyseDataOperateDao.batchInsertCruiseDataResult(list);
    }

    @Logs(title = "标准测点信息查询", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TStdDevicemete selectByPrimaryIdDeviceMete(Long deviceMeteId) {
        return this.analyseDataOperateDao.selectByPrimaryIdDeviceMete(deviceMeteId);
    }

    @Logs(title = "根据巡视点ID查询标准测点信息", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TStdDevicemete selectDeviceMeteByInstanceId(Long instanceId) {
        return this.analyseDataOperateDao.selectDeviceMeteByInstanceId(instanceId);
    }

    @Logs(title = "根据巡视点ID查询巡视点信息",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePointInstance selectPointInstance(Long instanceId){
        return  this.analyseDataOperateDao.selectPointInstance(instanceId);
    }

    @Logs(title = "查询字典码",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String selectDictCode(String colName,String dictNote){
        return this.analyseDataOperateDao.selectDictCode(colName,dictNote);
    }

    @Logs(title = "查询任务下所有巡视点",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> selectCruiseByTask(String taskId){
        return this.analyseDataOperateDao.selectCruiseByTaskId(taskId);
    }

    @Logs(title = "告警判断", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int warnJudgement(Float value,
                                 Float highLimit1,
                                 Float lowLimit1,
                                 Float highLimit2,
                                 Float lowLimit2) {
        if(value>highLimit1 && value<highLimit2){
            return 1; //过高
        }else if(value<lowLimit1 && value>lowLimit2){
            return 2; //过低
        }else if(value>highLimit2){
            return 3; //超高
        }else if (value<lowLimit2){
            return 4; //超低
        }else
            return 0;

    }

}

