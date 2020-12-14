package com.yjh.accessvideo.module.device.service;


import com.yjh.accessvideo.module.device.dao.AnalyseDataOperateDao;
import com.yjh.accessvideo.module.device.entity.*;
import com.yjh.accessvideo.commons.logs.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AnalyseDataOperateService {
    @Autowired
    private AnalyseDataOperateDao analyseDataOperateDao;

    private Logger log = LoggerFactory.getLogger(AnalyseDataOperateService.class);

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

    @Logs(title = "巡视任务结果单查", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectByPrimaryIdCruiseResult(String taskResultId) {
        return this.analyseDataOperateDao.selectByPrimaryIdCruiseResult(taskResultId);
    }

    @Logs(title = "巡视任务结果修改", code = "")
    @Transactional(rollbackFor = Exception.class)
    public int updateCruiseResult(TCruiseResult tCruiseResult) {
        return this.analyseDataOperateDao.updateCruiseResult(tCruiseResult);
    }


    @Logs(title = "任务-巡视点状态结果单查", code = "")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskResult selectByPrimaryIdCruiseTaskResult(String taskResultId) {
        return this.analyseDataOperateDao.selectByPrimaryIdCruiseTaskResult(taskResultId);
    }

    @Logs(title = "任务-巡视点状态结构更新", code = "")
    @Transactional(rollbackFor = Exception.class)
    public int updateCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult) {
        return this.analyseDataOperateDao.updateCruiseTaskResult(tCruiseTaskResult);
    }

    @Logs(title = "任务-巡视点状态结构新增插入", code = "")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult) {
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

    @Logs(title = "根据巡视点ID查询巡视点信息", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePointInstance selectPointInstance(Long instanceId) {
        return this.analyseDataOperateDao.selectPointInstance(instanceId);
    }

    @Logs(title = "查询字典码", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String selectDictCode(String colName, String dictNote) {
        return this.analyseDataOperateDao.selectDictCode(colName, dictNote);
    }

    @Logs(title = "查询字典内容",code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String selectDictNote(String dictCode,String colName){
        return analyseDataOperateDao.selectDictNote(dictCode, colName);
    }

    @Logs(title = "查询任务下所有巡视点", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> selectCruiseByTask(String taskId) {
        return this.analyseDataOperateDao.selectCruiseByTaskId(taskId);
    }

    @Logs(title = "插入缺陷信息")
    @Transactional(rollbackFor = Exception.class)
    public int insertDefectInfo(TDefectInfo tDefectInfo) {
        return this.analyseDataOperateDao.insertDefectInfo(tDefectInfo);
    }

    @Logs(title = "插入缺陷信息")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertDefectInfo(List<TDefectInfo> list) {
        return this.analyseDataOperateDao.batchInsertDefectInfo(list);
    }


    @Logs(title = "表计识别-告警判断-数值结果判断", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int warnJudgement(Float value,
                             Float highLimit1,
                             Float lowLimit1,
                             Float highLimit2,
                             Float lowLimit2,
                             Float highLimit3,
                             Float lowLimit3,
                             Float highLimit4,
                             Float lowLimit4) {
        log.info("value"+value);
        log.info("highLimit1"+highLimit1);
        log.info("lowLimit1"+lowLimit1);
        log.info("highLimit2"+highLimit2);
        log.info("lowLimit2"+lowLimit2);

        if(value<highLimit1 && value>lowLimit1){
            return 0;//正常
        }else if (value<=lowLimit4 || value>=highLimit4){
                return 4;//危急
        }else if(value<=lowLimit3 || value>=highLimit3){
            return 3;//严重
        }else if(value<=lowLimit2 || value>=highLimit2){
            return 2;//一般
        }else if(value<=lowLimit1 || value>=highLimit1){
            return 1;//预警
        }
        return 0;
    }

    @Logs(title = "表计识别-告警判断-文字结果判断",code = "")
    @Transactional(rollbackFor = Exception.class)
    public int warnJudgementTelesignaling(String value,String stateOne,String stateTwo,Integer alarmState){
        log.info("value"+value);
        log.info("stateOne"+stateOne);
        log.info("stateTwo"+stateTwo);
        log.info("alarmState"+alarmState);
        int finalResult=0;//0-非告警 1-告警
            switch (alarmState){
                case 0:
                    if(value.equals(stateOne)){
                        finalResult=1;
                    }else {
                       finalResult=0;
                    }
                    break;
                case 1:
                    if(value.equals(stateTwo)){
                       finalResult=1;
                    }else {
                       finalResult=0;
                    }
                    break;
            }
            return finalResult;
    }

    // TODO: 2020/11/21 表计识别结果告警判断--文字结果判断
    
    
    @Logs(title = "缺陷识别结果解析", code = "")
    @Transactional(rollbackFor = Exception.class)
    public String  resolveDefectResult(String resultValue) {
        log.info("----缺陷识别结果解析---resultValue:" + resultValue);
        String defectValue = "";
        String value = resultValue;
        String finalValue = value.replaceAll("[0-9]", "");
        String[] str2 = finalValue.split("\\s+");
        for (int i = 0; i < str2.length; i++) {
            switch (str2[i]) {
                case "wcaqm":
                    defectValue = defectValue + "未穿安全帽" + " ";
                    break;
                case "wcgz":
                    defectValue = defectValue + "未穿工装" + " ";
                    break;
                case "rydd":
                    defectValue = defectValue + "人员倒地" + " ";
                    break;
                case "xy":
                    defectValue = defectValue + "吸烟" + " ";
                    break;
                case "sly_dmyw":
                    defectValue = defectValue + "地面油污" + " ";
                    break;
                case "yw_nc":
                    defectValue = defectValue + "鸟窝" + " ";
                    break;
                case "yw_gkxfw":
                    defectValue = defectValue + "飘挂物" + " ";
                    break;
                case "jyz_bmwh":
                    defectValue = defectValue + "绝缘子-表面污秽" + " ";
                    break;
                case "jyz_pl":
                    defectValue = defectValue + "绝缘子-破裂" + " ";
                    break;
                case "jyz_lw":
                    defectValue = defectValue + "绝缘子-裂纹" + " ";
                    break;
                case "hxq_gjbs":
                    defectValue = defectValue + "呼吸器-硅胶变色" + " ";
                    break;
                case "hxq_gjtps":
                    defectValue = defectValue + "呼吸器-硅胶筒破损" + " ";
                    break;
                case "ywzt_yfyc":
                    defectValue = defectValue + "油位状态-油位异常" + " ";
                    break;
                case "bj_bpmh":
                    defectValue = defectValue + "表计-表盘模糊" + " ";
                    break;
                case "bj_bpps":
                    defectValue = defectValue + "表计-表盘破损" + " ";
                    break;
                case "bj_wkps":
                    defectValue = defectValue + "表计-外壳破损" + " ";
                    break;
                case "mcqdmsh":
                    defectValue = defectValue + "门窗墙地面损坏" + " ";
                    break;
                case "gbps":
                    defectValue = defectValue + "盖板破损" + " ";
                    break;
                case "gjptwss":
                    defectValue = defectValue + "构架爬梯未上锁" + " ";
                    break;
                case "xmbhyc":
                    defectValue = defectValue + "箱门闭合异常" + " ";
                    break;
                case "jsxs":
                    defectValue = defectValue + "金属锈蚀" + " ";
                    break;
                default:
                    return "null";
            }
        }
        return defectValue;
    }

}

