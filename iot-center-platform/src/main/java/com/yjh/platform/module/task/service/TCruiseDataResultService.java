package com.yjh.platform.module.task.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.task.dao.TCruiseDataResultDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author czh
 * @since 2020-08-25
 */
@Service
public class TCruiseDataResultService {

    @Autowired
    private TCruiseDataResultDao tCruiseDataResultDao;

    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;

    @Autowired
    private TStdDeviceDao tStdDeviceDao;

    @Autowired
    private TStdRegionDao tStdRegionDao;

    private Logger log = LoggerFactory.getLogger(TCruiseDataResultService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseDataResult tCruiseDataResult) {
        return this.tCruiseDataResultDao.insert(tCruiseDataResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long cruiseDataId) {
        return this.tCruiseDataResultDao.deleteByPrimaryId(cruiseDataId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseDataResult tCruiseDataResult) {
        return this.tCruiseDataResultDao.update(tCruiseDataResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseDataResult selectByPrimaryId(Long cruiseDataId) {
        return this.tCruiseDataResultDao.selectByPrimaryId(cruiseDataId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResult> select(Long cruiseDataId, String cruiseResultId, Long cruiseId,String cruiseName, Integer cruiseType, Integer cruiseAbnormal,String resultDesc, String resultNum, String modifyNum, String picpath, String personcheck, String origpic, String evaluationState, Integer identifyState, Integer identifyResult, Date createtime, String remark,String checkUser,Date checkDate,Integer isWarn ,Integer cruiseResult ) {
        return tCruiseDataResultDao.select(cruiseDataId, cruiseResultId, cruiseId, cruiseName, cruiseType,  cruiseAbnormal,resultDesc, resultNum, modifyNum, picpath, personcheck, origpic, evaluationState, identifyState, identifyResult, createtime, remark,checkUser,checkDate,isWarn,cruiseResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResult> selectByPage(TCruiseDataResult tCruiseDataResult) {
        return tCruiseDataResultDao.selectByPage(tCruiseDataResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseDataResult> list) {
        return this.tCruiseDataResultDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectCruiseResultAnalyze(Long regionId,Integer deviceType,String meteType,Integer meterType,Integer cruiseRes,int pageNum,int pageSize) {
        List<Long> regionIdList = new ArrayList<>();
        List<Long> deviceIdList = new ArrayList<>();
        if (regionId == null){
            regionIdList = tStdRegionDao.selectDownId(regionId);//查询该regionId的子节点
            deviceIdList =  tStdDeviceDao.selectDeviceIdListByRegion(regionIdList);
            log.info("regionIdList是==="+regionIdList);
        }else {
            regionIdList = tStdRegionDao.selectDownId(regionId);//查询该regionId的子节点
            if (regionIdList != null && regionIdList.size() > 0){
                deviceIdList =  tStdDeviceDao.selectDeviceIdListByRegion(regionIdList);
            }else {
                deviceIdList.add(regionId);
            }
            log.info("regionIdList是==="+regionIdList);
        }
        log.info("deviceIdList是==="+deviceIdList);

        Map<String, Object> resultMap = new HashMap<>();
        List<CruiseResultAnalyzeMeteInfo> cruiseResultAnalMeteInfoList = new ArrayList<>();
        Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
        if (deviceIdList != null && deviceIdList.size() > 0){
            cruiseResultAnalMeteInfoList = tStdDevicemeteDao.selectCruiseResultAnalyze(deviceIdList,deviceType,meteType,meterType);
        }

        //获取同一设备下的有巡检结果的标准测点
        List<CruiseResultAnalyzeMeteInfo> abnormalFilters=new ArrayList<>();
        for (CruiseResultAnalyzeMeteInfo deviceInfo : cruiseResultAnalMeteInfoList) {
            log.info("设备信息：" + deviceInfo);
            //通过测点ID获取相应的符合条件的巡检点结果
            CruiseResultAnalyzeMeteInfo cruiseResultAnalMeteInfo = tCruiseDataResultDao.selectMeteCruiseByDeviceId2(deviceInfo.getDeviceId(), deviceInfo.getDeviceMeteId());
            log.info("CrusieResultAnalMeteInfo:" + cruiseResultAnalMeteInfo);
            deviceInfo.setInstanceId(cruiseResultAnalMeteInfo.getInstanceId());
            deviceInfo.setCruiseResult(cruiseResultAnalMeteInfo.getCruiseResult());
            deviceInfo.setCruiseResultName(cruiseResultAnalMeteInfo.getCruiseResultName());
            deviceInfo.setCruiseTime(cruiseResultAnalMeteInfo.getCruiseTime());
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
            //cruiseRes:-1全部,1正常,0异常
            if (cruiseRes != -1 && cruiseRes != deviceInfo.getFinalState()){
                abnormalFilters.add(deviceInfo);
            }
        }
        log.info("abnormalFilters==="+abnormalFilters);
        cruiseResultAnalMeteInfoList.removeAll(abnormalFilters);

        //按巡检时间逆序排列
        Collections.sort(cruiseResultAnalMeteInfoList, new Comparator<CruiseResultAnalyzeMeteInfo>() {
            @Override
            public int compare(CruiseResultAnalyzeMeteInfo o1, CruiseResultAnalyzeMeteInfo o2) {
                Date date1 = o1.getCruiseTime();
                Date date2 = o2.getCruiseTime();
                int flag = date1.compareTo(date2);
                flag = -flag;
                return flag;
            }
        });
        resultMap.put("count",page.getTotal());
        resultMap.put("list", cruiseResultAnalMeteInfoList);
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalMeteInfo> selectCruiseResultAnal(List<Long>deviceIds,String resultSwitch) {
        List<CruiseResultAnalMeteInfo> cruiseResultAnalMeteInfos=new ArrayList<>();

        //判断deviceId是否为空 决定 全查/条件查
        if (deviceIds.size() > 0&&deviceIds.get(0)==996) {
            cruiseResultAnalMeteInfos = tStdDevicemeteDao.selectDeviceMete();
            log.info("执行完成");
        }else {
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

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectCruiseDataReport( Integer cType, String meteType, Integer meterType, String endTime, String startTime,Long regionId,String instanceName,int pageNum,int pageSize) {

        List<Long> regionIdList = new ArrayList<>();
        List<Long> deviceIdList = new ArrayList<>();
        if (regionId == null) {
            regionIdList = tStdRegionDao.selectDownId(regionId);//查询该regionId的子节点
            deviceIdList = tStdDeviceDao.selectDeviceIdListByRegion(regionIdList);
            log.info("regionIdList是===" + regionIdList);
        }else {
            regionIdList = tStdRegionDao.selectDownId(regionId);//查询该regionId的子节点
            if (regionIdList != null&& regionIdList.size() > 0){
                deviceIdList =  tStdDeviceDao.selectDeviceIdListByRegion(regionIdList);
            }else {
                deviceIdList.add(regionId);
            }
            log.info("regionIdList是==="+regionIdList);
        }

        log.info("deviceIdList是==="+deviceIdList);
        Map<String, Object> resultMap = new HashMap<>();
        Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
        List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = new ArrayList<>();
        if (deviceIdList != null&& deviceIdList.size() > 0) {
            cruiseResultAnalyzeInfoList = tCruiseDataResultDao.selectCruiseDataReport(cType, meteType, meterType, endTime, startTime, deviceIdList, instanceName);
            for (CruiseResultAnalyzeInfo cRAI : cruiseResultAnalyzeInfoList) {
                if (Objects.isNull(cRAI.getIdentifyResult())) {
                    cRAI.setIdentifyResultName(cRAI.getCruiseResultName());
                }
                if (Objects.isNull(cRAI.getPersonCheck())){
                    cRAI.setPersonCheck(cRAI.getResultNum());
                }
            }
        }
        resultMap.put("count", page.getTotal());
        resultMap.put("list", cruiseResultAnalyzeInfoList);

        return resultMap;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalyzeInfo> selectCruiseDataResultByList2(Integer cruiseType, Integer cType, Long deviceMeteId, String meteType, Integer meterType, String endTime, String startTime,int pageNum,int pageSize){

        List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = tCruiseDataResultDao.selectCruiseDataResultByList2(cruiseType, cType, deviceMeteId,meteType ,meterType,endTime, startTime);
        for (CruiseResultAnalyzeInfo cruiseResultAnalInfo : cruiseResultAnalyzeInfoList) {
            if (Objects.isNull(cruiseResultAnalInfo.getIdentifyResult())) {
                cruiseResultAnalInfo.setIdentifyResultName(cruiseResultAnalInfo.getCruiseResultName());
            }
            if (Objects.isNull(cruiseResultAnalInfo.getPersonCheck())){
                cruiseResultAnalInfo.setPersonCheck(cruiseResultAnalInfo.getResultNum());
            }
        }

        return cruiseResultAnalyzeInfoList;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<BrokenLineInfo> selectBrokenLine(Integer cruiseType,
                                                 Integer cType,
                                                 Long deviceMeteId,
                                                 String startTime,
                                                 String endTime,
                                                 String meteType,
                                                 Integer meterType) {
        return tCruiseDataResultDao.selectBrokenLine(cruiseType, cType, deviceMeteId, startTime, endTime,meteType,meterType);
    }

}

