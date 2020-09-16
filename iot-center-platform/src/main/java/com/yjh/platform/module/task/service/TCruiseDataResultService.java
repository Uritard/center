package com.yjh.platform.module.task.service;

import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.task.entity.BrokenLineInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalInfo;
import com.yjh.platform.module.task.entity.CruiseResultAnalMeteInfo;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
import com.yjh.platform.module.task.dao.TCruiseDataResultDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private TStdDevicemeteDao tStdDevicemeteDao;
    private Object Comparator;
    private Object CruiseResultAnalMeteInfo;

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

    @Logs(title = "巡视结果查询-测点查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalMeteInfo> selectCruiseResultAnal(Long deviceId) throws ParseException {
     //获取同一设备下的所有测点信息
        List<CruiseResultAnalMeteInfo> cruiseResultAnalMeteInfos=tStdDevicemeteDao.selectDeviceMeteByDeviceId(deviceId);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     for(CruiseResultAnalMeteInfo deviceInfo:cruiseResultAnalMeteInfos){

             //通过测点ID获取相应的符合条件的巡检点结果
             CruiseResultAnalMeteInfo cruiseResultAnalMeteInfo=tCruiseDataResultDao.selectMeteCruiseByDeviceId(deviceInfo.getDeviceId(),deviceInfo.getDeviceMeteId());
             deviceInfo.setInstanceId(cruiseResultAnalMeteInfo.getInstanceId());
             deviceInfo.setIdentifyState(cruiseResultAnalMeteInfo.getIdentifyState());
             deviceInfo.setIdentifyStateName(cruiseResultAnalMeteInfo.getIdentifyStateName());
             deviceInfo.setDate(simpleDateFormat.format(cruiseResultAnalMeteInfo.getEndTime()));//时间戳转换
             deviceInfo.setEndTime(cruiseResultAnalMeteInfo.getEndTime());
             deviceInfo.setPicPath(cruiseResultAnalMeteInfo.getPicPath());
             deviceInfo.setInstanceName(cruiseResultAnalMeteInfo.getInstanceName());
     }
     //按时间降序排列
        Collections.sort(cruiseResultAnalMeteInfos, new Comparator<CruiseResultAnalMeteInfo>() {
            @Override
            public int compare(CruiseResultAnalMeteInfo o1, CruiseResultAnalMeteInfo o2) {
                int flag = o1.getEndTime().compareTo(o2.getEndTime());
                if(flag == -1){
                    flag = 1;
                }else if(flag == 1){
                    flag = -1;
                }
                return flag;
            }
        });
       return cruiseResultAnalMeteInfos;
    }


    @Logs(title = "获取当前测点下的巡检结果")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalInfo> selectCruiseDataResultByList(CruiseResultAnalInfo cruiseResultAnalInfo){
        List<CruiseResultAnalInfo> cruiseResultAnalInfos=tCruiseDataResultDao.selectCruiseDataResultByList(cruiseResultAnalInfo);
        for(CruiseResultAnalInfo cruiseResultAnalInfo1:cruiseResultAnalInfos){
            cruiseResultAnalInfo1.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cruiseResultAnalInfo1.getEndTime()));
        }
        return cruiseResultAnalInfos;
    }

    @Logs(title = "获取折线图元素信息")
    @Transactional(rollbackFor = Exception.class)
    public List<BrokenLineInfo> selectBrokenLine(BrokenLineInfo brokenLineInfo){
        List<BrokenLineInfo> brokenLineInfos=tCruiseDataResultDao.selectBrokenLine(brokenLineInfo);
        for(BrokenLineInfo brokenLineInfo1:brokenLineInfos){
            brokenLineInfo1.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(brokenLineInfo1.getEndTime()));
        }
        return brokenLineInfos;
    }

}

