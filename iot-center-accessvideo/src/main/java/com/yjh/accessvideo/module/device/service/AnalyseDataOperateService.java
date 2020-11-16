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

    @Logs(title = "插入缺陷信息")
    @Transactional(rollbackFor = Exception.class)
    public int insertDefectInfo(TDefectInfo tDefectInfo){
        return this.analyseDataOperateDao.insertDefectInfo(tDefectInfo);
    }

    @Logs(title = "插入缺陷信息")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertDefectInfo(List<TDefectInfo> list){
        return this.analyseDataOperateDao.batchInsertDefectInfo(list);
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

    @Logs(title = "缺陷识别结果解析",code = "")
    @Transactional(rollbackFor = Exception.class)
    public String resolveDefectResult(String resultValue){
        log.info("----缺陷识别结果解析---resultValue:"+resultValue);
        String defectValue="";
        String value = resultValue;
        String value1 = value.replaceAll("[^a-z^A-Z]", "");
        Set<Integer> index = new HashSet<>();
        index.add(value1.indexOf("wcaqm"));
        index.add(value1.indexOf("wcgz"));
        index.add(value1.indexOf("yydd"));
        index.add(value1.indexOf("xy"));
        index.add(value1.indexOf("slydmyw"));
        index.add(value1.indexOf("ywnc"));
        index.add(value1.indexOf("ywgkxfw"));
        index.add(value1.indexOf("jyzbmwh"));
        index.add(value1.indexOf("jyzpl"));
        index.add(value1.indexOf("jyzlw"));
        index.add(value1.indexOf("hxqgjbs"));
        index.add(value1.indexOf("hxqgjtps"));
        index.add(value1.indexOf("ywztyfyc"));
        index.add(value1.indexOf("bjbpmh"));
        index.add(value1.indexOf("bjbpps"));
        index.add(value1.indexOf("bjwkps"));
        index.add(value1.indexOf("mcqdmsh"));
        index.add(value1.indexOf("gbps"));
        index.add(value1.indexOf("gjptwss"));
        index.add(value1.indexOf("xmbhyc"));
        index.add(value1.indexOf("jsxs"));
        log.info("----缺陷识别结果解析---Set:"+index);

        index.remove(-1);
        if(index.size()==0){
            return "null";
        }

        log.info("发生错误？"+index.size());

        String[] str1 = value.split("[0-9]");
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < str1.length; i++) {
            stringBuffer.append(str1[i]);
        }
        String value2 = stringBuffer.toString();
        String[] str2 = value2.split(" ");
        stringBuffer.delete(0, stringBuffer.length() - 1);
        for (int i = 0; i < str2.length; i++) {
            stringBuffer.append(str2[i]);
        }


        for(Integer ind:index){
            if (ind.equals(-1)){
                continue;
            }else {
                switch (str2[ind]){
                    case "wcaqm":
                        defectValue=defectValue+"未穿安全帽"+" ";
                        break;
                    case "wcgz":
                        defectValue=defectValue+"未穿工装"+" ";
                        break;
                    case "rydd":
                        defectValue=defectValue+"人员倒地"+" ";
                        break;
                    case "xy":
                        defectValue=defectValue+"吸烟"+" ";
                        break;
                    case "slydmyw":
                        defectValue=defectValue+"地面油污"+" ";
                        break;
                    case "ywnc":
                        defectValue=defectValue+"鸟窝"+" ";
                        break;
                    case "ywgkxfw":
                        defectValue=defectValue+"飘挂物"+" ";
                        break;
                    case "jyzbmwh":
                        defectValue=defectValue+"绝缘子-表面污秽"+" ";
                        break;
                    case "jyzpl":
                        defectValue=defectValue+"绝缘子-破裂"+" ";
                        break;
                    case "jyzlw":
                        defectValue=defectValue+"绝缘子-裂纹"+" ";
                        break;
                    case "hxqgjbs":
                        defectValue=defectValue+"呼吸器-硅胶变色"+" ";
                        break;
                    case "hxqgjtps":
                        defectValue=defectValue+"呼吸器-硅胶筒破损"+" ";
                        break;
                    case "ywztyfyc":
                        defectValue=defectValue+"油位状态-油位异常"+" ";
                        break;
                    case "bjbpmh":
                        defectValue=defectValue+"表计-表盘模糊"+" ";
                        break;
                    case "bjbpps":
                        defectValue=defectValue+"表计-表盘破损"+" ";
                        break;
                    case "bjwkps":
                        defectValue=defectValue+"表计-外壳破损"+" ";
                        break;
                    case "mcqdmsh":
                        defectValue=defectValue+"门窗墙地面损坏"+" ";
                        break;
                    case "gbps":
                        defectValue=defectValue+"盖板破损"+" ";
                        break;
                    case "gjptwss":
                        defectValue=defectValue+"构架爬梯未上锁"+" ";
                        break;
                    case "xmbhyc":
                        defectValue=defectValue+"箱门闭合异常"+" ";
                        break;
                    case "jsxs":
                        defectValue=defectValue+"金属锈蚀"+" ";
                        break;
                }
            }
        }
        return defectValue;
    }

}

