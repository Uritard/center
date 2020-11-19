package com.yjh.platform.module.task.service;

import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.task.controller.TCruiseDataResultController;
import com.yjh.platform.module.task.entity.BrokenLineInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalMeteInfo;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
import com.yjh.platform.module.task.dao.TCruiseDataResultDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yjh.platform.module.user.dao.TDictBusinessDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Autowired
    private TDictBusinessDao tDictBusinessDao;

    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;
    private Object Comparator;
    private Object CruiseResultAnalMeteInfo;

    private Logger log = LoggerFactory.getLogger(TCruiseDataResultService.class);

    @Logs(title = "插入", code = "TCruiseDataResult",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseDataResult tCruiseDataResult) {
        return this.tCruiseDataResultDao.insert(tCruiseDataResult);
    }

    @Logs(title = "删除", code = "TCruiseDataResult",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long cruiseDataId) {
        return this.tCruiseDataResultDao.deleteByPrimaryId(cruiseDataId);
    }

    @Logs(title = "更新", code = "TCruiseDataResult",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseDataResult tCruiseDataResult) {
        return this.tCruiseDataResultDao.update(tCruiseDataResult);
    }

    @Logs(title = "主键查询", code = "TCruiseDataResult",content = "根据主键查询")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseDataResult selectByPrimaryId(Long cruiseDataId) {
        return this.tCruiseDataResultDao.selectByPrimaryId(cruiseDataId);
    }

    @Logs(title = "查询", code = "TCruiseDataResult",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResult> select(Long cruiseDataId, String cruiseResultId, Long cruiseId, Integer cruiseType, String resultDesc, String resultNum, String modifyNum, String picpath, String personcheck, String origpic, Integer state, String evaluationState, Integer identifyState, Integer identifyResult, Date createtime, String remark) {
        List<TCruiseDataResult> tCruiseDataResultList = tCruiseDataResultDao.select(cruiseDataId, cruiseResultId, cruiseId, cruiseType, resultDesc, resultNum, modifyNum, picpath, personcheck, origpic, state, evaluationState, identifyState, identifyResult, createtime, remark);
        return tCruiseDataResultList;
    }

    @Logs(title = "分页查询", code = "TCruiseDataResult",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResult> selectByPage(TCruiseDataResult tCruiseDataResult) {
        List<TCruiseDataResult> tCruiseDataResultList = tCruiseDataResultDao.selectByPage(tCruiseDataResult);
        return tCruiseDataResultList;
    }

    @Logs(title = "批量插入", code = "TCruiseDataResult",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseDataResult> list) {
        return this.tCruiseDataResultDao.batchInsert(list);
    }



    @Logs(title = "巡视结果查询-测点查询",code = "TCruiseDataResult",content = "巡视结果测点查询")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalMeteInfo> selectCruiseResultAnal(Long deviceId)  {
        List<CruiseResultAnalMeteInfo> cruiseResultAnalMeteInfos;
        //判断deviceId是否为空 决定 全查/条件查
        if(deviceId==null){
            cruiseResultAnalMeteInfos=tStdDevicemeteDao.selectDeviceMete();
        }else {
            cruiseResultAnalMeteInfos=tStdDevicemeteDao.selectDeviceMeteByDeviceId(deviceId);
        }
     //获取同一设备下的有巡检结果的标准测点

     for(CruiseResultAnalMeteInfo deviceInfo:cruiseResultAnalMeteInfos){

             //通过测点ID获取相应的符合条件的巡检点结果
             CruiseResultAnalMeteInfo cruiseResultAnalMeteInfo=tCruiseDataResultDao.selectMeteCruiseByDeviceId(deviceInfo.getDeviceId(),deviceInfo.getDeviceMeteId());
             log.info("CrusieResultAnalMeteInfo:"+cruiseResultAnalMeteInfo);
             deviceInfo.setInstanceId(cruiseResultAnalMeteInfo.getInstanceId());
             deviceInfo.setState(cruiseResultAnalMeteInfo.getState());
             deviceInfo.setStateName(cruiseResultAnalMeteInfo.getStateName());
             deviceInfo.setEndTime(cruiseResultAnalMeteInfo.getEndTime());
             deviceInfo.setPicPath(cruiseResultAnalMeteInfo.getPicPath());
             deviceInfo.setCruiseName(cruiseResultAnalMeteInfo.getCruiseName());
             deviceInfo.setIdentifyResult(cruiseResultAnalMeteInfo.getIdentifyResult());
             deviceInfo.setIdentifyResultName(cruiseResultAnalMeteInfo.getIdentifyResultName());
             if(Objects.isNull(deviceInfo.getIdentifyResult())){
                 if(deviceInfo.getState()==247){
                     deviceInfo.setFinalState(1);
                 }else {
                     deviceInfo.setFinalState(0);
                 }
             }else {
                 if(deviceInfo.getIdentifyResult()==261){
                     deviceInfo.setFinalState(1);
                 }else {
                     deviceInfo.setFinalState(0);
                 }

             }
     }

     //按时间降序排列
//        Collections.sort(cruiseResultAnalMeteInfos, new Comparator<CruiseResultAnalMeteInfo>() {
//            @Override
//            public int compare(CruiseResultAnalMeteInfo o1, CruiseResultAnalMeteInfo o2) {
//                int flag = o1.getEndTime().compareTo(o2.getEndTime());
//                if(flag == -1){
//                    flag = 1;
//                }else if(flag == 1){
//                    flag = -1;
//                }
//                return flag;
//            }
//        });
       return cruiseResultAnalMeteInfos;
    }


    @Logs(title = "获取当前测点下的巡检结果",code = "TCruiseDataResult",content = "获取测点巡检结果")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalInfo> selectCruiseDataResultByList(Integer cruiseType,
                                                                   Integer cType,
                                                                   Long deviceMeteId,
                                                                   Date endDate,
                                                                   Date startDate){
        List<CruiseResultAnalInfo> cruiseResultAnalInfos=tCruiseDataResultDao.selectCruiseDataResultByList(cruiseType,cType,deviceMeteId,endDate,startDate);

        return cruiseResultAnalInfos;
    }

    @Logs(title = "获取折线图元素信息",code = "TCruiseDataResult",content = "获取折线图信息")
    @Transactional(rollbackFor = Exception.class)
    public List<BrokenLineInfo> selectBrokenLine(Integer cruiseType,
                                                 Integer cType,
                                                 Long deviceMeteId,
                                                 Date endDate,
                                                 Date startDate){
        List<BrokenLineInfo> brokenLineInfos=tCruiseDataResultDao.selectBrokenLine(cruiseType,cType,deviceMeteId,endDate,startDate);
        return brokenLineInfos;
    }

}

