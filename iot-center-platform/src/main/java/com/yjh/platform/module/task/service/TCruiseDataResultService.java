package com.yjh.platform.module.task.service;

import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
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
public class TCruiseDataResultService {

    @Autowired
    private TCruiseDataResultDao tCruiseDataResultDao;
    @Autowired
    private TDictBusinessDao tDictBusinessDao;

    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;
    private Object Comparator;
    private Object CruiseResultAnalMeteInfo;

    @Autowired
    private TStdDeviceDao tStdDeviceDao;

    @Autowired
    private TStdRegionDao tStdRegionDao;

    private Logger log = LoggerFactory.getLogger(TCruiseDataResultService.class);

    @Logs(title = "插入", code = "TCruiseDataResult", content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseDataResult tCruiseDataResult) {
        return this.tCruiseDataResultDao.insert(tCruiseDataResult);
    }

    @Logs(title = "删除", code = "TCruiseDataResult", content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long cruiseDataId) {
        return this.tCruiseDataResultDao.deleteByPrimaryId(cruiseDataId);
    }

    @Logs(title = "更新", code = "TCruiseDataResult", content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseDataResult tCruiseDataResult) {
        return this.tCruiseDataResultDao.update(tCruiseDataResult);
    }

    @Logs(title = "主键查询", code = "TCruiseDataResult", content = "根据主键查询")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseDataResult selectByPrimaryId(Long cruiseDataId) {
        return this.tCruiseDataResultDao.selectByPrimaryId(cruiseDataId);
    }

    @Logs(title = "查询", code = "TCruiseDataResult", content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResult> select(Long cruiseDataId, String cruiseResultId, Long cruiseId,String cruiseName, Integer cruiseType, Integer cruiseAbnormal,String resultDesc, String resultNum, String modifyNum, String picpath, String personcheck, String origpic, String evaluationState, Integer identifyState, Integer identifyResult, Date createtime, String remark,String checkUser,Date checkDate,Integer isWarn ,Integer cruiseResult ) {
        List<TCruiseDataResult> tCruiseDataResultList = tCruiseDataResultDao.select(cruiseDataId, cruiseResultId, cruiseId, cruiseName, cruiseType,  cruiseAbnormal,resultDesc, resultNum, modifyNum, picpath, personcheck, origpic, evaluationState, identifyState, identifyResult, createtime, remark,checkUser,checkDate,isWarn,cruiseResult);
        return tCruiseDataResultList;
    }

    @Logs(title = "分页查询", code = "TCruiseDataResult", content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResult> selectByPage(TCruiseDataResult tCruiseDataResult) {
        List<TCruiseDataResult> tCruiseDataResultList = tCruiseDataResultDao.selectByPage(tCruiseDataResult);
        return tCruiseDataResultList;
    }

    @Logs(title = "批量插入", code = "TCruiseDataResult", content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseDataResult> list) {
        return this.tCruiseDataResultDao.batchInsert(list);
    }


    @Logs(title = "巡视结果查询-测点查询", code = "TCruiseDataResult", content = "巡视结果测点查询")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalMeteInfo> selectCruiseResultAnal(Long deviceId,String resultSwitch) {
        List<CruiseResultAnalMeteInfo> cruiseResultAnalMeteInfos=new ArrayList<>();
        List<Long> deviceIds = new ArrayList<>();

        //判断deviceId是否为空 决定 全查/条件查
        if (Objects.isNull(deviceId)) {
            cruiseResultAnalMeteInfos = tStdDevicemeteDao.selectDeviceMete();
            log.info("执行完成");
        } else {
            if (Objects.nonNull(tStdDeviceDao.selectByPrimaryId(deviceId))) {
                deviceIds.add(deviceId);
            } else {
                List<Long> regionIds = new ArrayList<>();
                regionIds.add(deviceId);
                if (tStdDeviceDao.selectDeviceIdsByRegion(regionIds).size() != 0) {
                    log.info("case1");
                    deviceIds.addAll(tStdDeviceDao.selectDeviceIdsByRegion(regionIds));
                } else {
                    log.info("case2");
                    regionIds.clear();
                    List<Long> upRegionIds = new ArrayList<>();//组织区域上层ID
                    List<Long> upRegionIdsTem=new ArrayList<>();
                    upRegionIds.add(deviceId);
                    regionIds.addAll(tStdRegionDao.selectRegionByUpId(upRegionIds));
                    upRegionIdsTem.addAll(tStdRegionDao.selectRegionByUpId(upRegionIds));
                    while (upRegionIdsTem.size()!=0){
                        upRegionIdsTem.addAll(tStdRegionDao.selectRegionByUpId(upRegionIdsTem));
                        regionIds.addAll(tStdRegionDao.selectRegionByUpId(upRegionIdsTem));
                        upRegionIdsTem.removeAll(regionIds);
                        if(upRegionIdsTem.size()==0)
                            break;
                    }

                    if(regionIds.size()!=0){
                        if (tStdDeviceDao.selectDeviceIdsByRegion(regionIds).size() != 0) {
                            deviceIds = tStdDeviceDao.selectDeviceIdsByRegion(regionIds);
                            log.info("jumpOut:" + deviceIds.size());
                        }
                    }


                }
            }


            for (Long deviceId1 : deviceIds) {
                log.info("device:-----" + deviceId1);
            }
            if(deviceIds.size()>0){
                cruiseResultAnalMeteInfos = tStdDevicemeteDao.selectDeviceMeteByDeviceId(deviceIds);
            }
        }
        //获取同一设备下的有巡检结果的标准测点
       List<CruiseResultAnalMeteInfo> abnormalFilters=new ArrayList<>();
        for (CruiseResultAnalMeteInfo deviceInfo : cruiseResultAnalMeteInfos) {
            log.info("设备信息：" + deviceInfo);
            //通过测点ID获取相应的符合条件的巡检点结果
            CruiseResultAnalMeteInfo cruiseResultAnalMeteInfo = tCruiseDataResultDao.selectMeteCruiseByDeviceId(deviceInfo.getDeviceId(), deviceInfo.getDeviceMeteId());
            log.info("CrusieResultAnalMeteInfo:" + cruiseResultAnalMeteInfo);
            deviceInfo.setInstanceId(cruiseResultAnalMeteInfo.getInstanceId());
            deviceInfo.setCruiseResult(cruiseResultAnalMeteInfo.getCruiseResult());
            deviceInfo.setCruiseResultName(cruiseResultAnalMeteInfo.getCruiseResultName());
            deviceInfo.setEndTime(cruiseResultAnalMeteInfo.getEndTime());
            deviceInfo.setPicPath(cruiseResultAnalMeteInfo.getPicPath());
            deviceInfo.setCruiseName(cruiseResultAnalMeteInfo.getCruiseName());
            deviceInfo.setIdentifyResult(cruiseResultAnalMeteInfo.getIdentifyResult());
            deviceInfo.setIdentifyResultName(cruiseResultAnalMeteInfo.getIdentifyResultName());
            //根据巡视点的-算法数据结果状态和最终审核结果判断最终的展示状态结果
            if (Objects.isNull(deviceInfo.getIdentifyResult())) {
                if (deviceInfo.getCruiseResult() == 246) {
                    deviceInfo.setFinalState(1);
                } else {
                    deviceInfo.setFinalState(0);
                }
            } else {
                if (deviceInfo.getIdentifyResult() == 261) {
                    deviceInfo.setFinalState(1);
                } else {
                    deviceInfo.setFinalState(0);
                }

            }


            log.info("resultSwitch-------------------------------:"+resultSwitch);
            if(resultSwitch.equals("abnormal")){
                if(deviceInfo.getFinalState()==1){
                    abnormalFilters.add(deviceInfo);
                }
            }
        }

        cruiseResultAnalMeteInfos.removeAll(abnormalFilters);

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


    @Logs(title = "获取当前测点下的巡检结果", code = "TCruiseDataResult", content = "获取测点巡检结果")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalInfo> selectCruiseDataResultByList(Integer cruiseType,
                                                                   Integer cType,
                                                                   Long deviceMeteId,
                                                                   Date endDate,
                                                                   Date startDate) {
        List<CruiseResultAnalInfo> cruiseResultAnalInfos = tCruiseDataResultDao.selectCruiseDataResultByList(cruiseType, cType, deviceMeteId, endDate, startDate);
        for (CruiseResultAnalInfo cruiseResultAnalInfo : cruiseResultAnalInfos) {
            if (cruiseResultAnalInfo.getIdentifyResult() == 0 || Objects.isNull(cruiseResultAnalInfo.getIdentifyResult())) {
                cruiseResultAnalInfo.setIdentifyResultName(cruiseResultAnalInfo.getCruiseResultName());
            }
        }

        return cruiseResultAnalInfos;
    }

    @Logs(title = "获取折线图元素信息", code = "TCruiseDataResult", content = "获取折线图信息")
    @Transactional(rollbackFor = Exception.class)
    public List<BrokenLineInfo> selectBrokenLine(Integer cruiseType,
                                                 Integer cType,
                                                 Long deviceMeteId,
                                                 Date endDate,
                                                 Date startDate) {
        List<BrokenLineInfo> brokenLineInfos = tCruiseDataResultDao.selectBrokenLine(cruiseType, cType, deviceMeteId, endDate, startDate);
        return brokenLineInfos;
    }

}

