package com.yjh.platform.module.task.service;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.task.dao.TCruiseDataResultDao;
import com.yjh.platform.module.task.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author czh
 * @since 2020-08-25
 */
@Service
public class TCruiseDataResultService {

    private TCruiseDataResultDao tCruiseDataResultDao;
    private TStdDevicemeteDao tStdDevicemeteDao;

    @Autowired
    public TCruiseDataResultService(TCruiseDataResultDao tCruiseDataResultDao, TStdDevicemeteDao tStdDevicemeteDao) {
        this.tCruiseDataResultDao = tCruiseDataResultDao;
        this.tStdDevicemeteDao = tStdDevicemeteDao;
    }

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
    public List<TCruiseDataResult> select(Long cruiseDataId, String cruiseResultId, Long cruiseId, String cruiseName, Integer cruiseType, Integer cruiseAbnormal, String resultDesc, String resultNum, String modifyNum, String picPathAnl, String picpath, String personcheck, String origPicAnl, String origpic, String evaluationState, Integer identifyState, Integer identifyResult, Date createtime, String remark, String checkUser, Date checkDate, Integer isWarn, Integer cruiseResult) {
        return tCruiseDataResultDao.select(cruiseDataId, cruiseResultId, cruiseId, cruiseName, cruiseType, cruiseAbnormal, resultDesc, resultNum, modifyNum, picPathAnl, picpath, personcheck, origPicAnl, origpic, evaluationState, identifyState, identifyResult, createtime, remark, checkUser, checkDate, isWarn, cruiseResult);
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
    public List<CruiseResultAnalyzeMeteInfo> selectCruiseResultAnalyze(List<Long> deviceIdList, Integer deviceType, String meteType, Integer meterType, Integer cruiseRes) {
        List<CruiseResultAnalyzeMeteInfo> cruiseResultAnalMeteInfoList = new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()) {
            //cruiseRes:-1全部,1正常,0异常
            if (cruiseRes == 1) {
                cruiseResultAnalMeteInfoList = tStdDevicemeteDao.selectCruiseResultAnalyze(deviceIdList, deviceType, meteType, meterType, cruiseRes);
            } else {
                cruiseResultAnalMeteInfoList = tStdDevicemeteDao.selectCruiseResultAnalyze2(deviceIdList, deviceType, meteType, meterType, cruiseRes);
            }
            for (CruiseResultAnalyzeMeteInfo item : cruiseResultAnalMeteInfoList) {
                if (item.getFinalState() == 246 || item.getFinalState() == 261) {
                    item.setFinalState(1);
                } else {
                    item.setFinalState(0);
                }
            }
        }
        return cruiseResultAnalMeteInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalMeteInfo> selectCruiseResultAnal(List<Long> deviceIds, String resultSwitch) {
        List<CruiseResultAnalMeteInfo> cruiseResultAnalMeteInfos = new ArrayList<>();

        //判断deviceId是否为空 决定 全查/条件查
        if (!deviceIds.isEmpty() && deviceIds.get(0) == 996) {
            cruiseResultAnalMeteInfos = tStdDevicemeteDao.selectDeviceMete();
            log.info("执行完成");
        } else {
            if (!deviceIds.isEmpty()) {
                cruiseResultAnalMeteInfos = tStdDevicemeteDao.selectDeviceMeteByDeviceId(deviceIds);
            }
        }
        //获取同一设备下的有巡检结果的标准测点
        List<CruiseResultAnalMeteInfo> abnormalFilters = new ArrayList<>();
        for (CruiseResultAnalMeteInfo deviceInfo : cruiseResultAnalMeteInfos) {
            //通过测点ID获取相应的符合条件的巡检点结果
            CruiseResultAnalMeteInfo cruiseResultAnalMeteInfo = tCruiseDataResultDao.selectMeteCruiseByDeviceId(deviceInfo.getDeviceId(), deviceInfo.getDeviceMeteId());
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
            if (resultSwitch.equals("abnormal") && deviceInfo.getFinalState() == 1) {
                abnormalFilters.add(deviceInfo);
            }
        }

        cruiseResultAnalMeteInfos.removeAll(abnormalFilters);
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
    public List<CruiseResultAnalyzeInfo> selectCruiseDataReport(Integer cType, String meteType, Integer meterType, String endTime, String startTime, List<Long> deviceIdList, String instanceName) {
        List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()) {
            cruiseResultAnalyzeInfoList = tCruiseDataResultDao.selectCruiseDataReport(cType, meteType, meterType, endTime, startTime, deviceIdList, instanceName);
            for (CruiseResultAnalyzeInfo cRAI : cruiseResultAnalyzeInfoList) {
                if (Objects.isNull(cRAI.getIdentifyResult())) {
                    cRAI.setIdentifyResultName(cRAI.getCruiseResultName());
                }
                if (Objects.isNull(cRAI.getPersonCheck())) {
                    cRAI.setPersonCheck(cRAI.getResultNum());
                }
            }
        }
        return cruiseResultAnalyzeInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalyzeInfo> selectCruiseDataResultByList2(Integer cruiseType, Integer cType, Long deviceMeteId, String meteType, Integer meterType, String endTime, String startTime, int pageNum, int pageSize) {

        List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = tCruiseDataResultDao.selectCruiseDataResultByList2(cruiseType, cType, deviceMeteId, meteType, meterType, endTime, startTime);
        for (CruiseResultAnalyzeInfo cruiseResultAnalInfo : cruiseResultAnalyzeInfoList) {
            if (Objects.isNull(cruiseResultAnalInfo.getIdentifyResult())) {
                cruiseResultAnalInfo.setIdentifyResultName(cruiseResultAnalInfo.getCruiseResultName());
            }
            if (Objects.isNull(cruiseResultAnalInfo.getPersonCheck())) {
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

        List<BrokenLineInfo> brokenLineInfos = tCruiseDataResultDao.selectBrokenLine(cruiseType, cType, deviceMeteId, startTime, endTime, meteType, meterType);
        for (BrokenLineInfo point : brokenLineInfos) {
            if (point.getResultNum().matches("^[a-zA-Z_\\u4e00-\\u9fa5_\\--]+$")) {
                point.setResultNum("0");
            }
        }
        return brokenLineInfos;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateCruiseAnalyze(List<String> cruiseResultIdList) {
        log.info("cruiseResultIdList===" + cruiseResultIdList);
        if (cruiseResultIdList != null && !cruiseResultIdList.isEmpty()) {
            List<TStdDeviceMeteUpdate> list = tCruiseDataResultDao.selectDeviceMeteList(cruiseResultIdList);
            log.info("list===" + list);

            for (TStdDeviceMeteUpdate res : list) {
                List<Long> deviceMeteIdList = tCruiseDataResultDao.selectAllDeviceMeteId();
                TStdDeviceMeteUpdate tStdDeviceMeteUpdate = new TStdDeviceMeteUpdate()
                        .setDeviceMeteId(res.getDeviceMeteId())
                        .setIdentifyResult(res.getIdentifyResult());
                if (Objects.nonNull(res.getUpdateTime())) {
                    tStdDeviceMeteUpdate.setUpdateTime(res.getUpdateTime());
                }
                if (Objects.nonNull(res.getCruiseResult())) {
                    tStdDeviceMeteUpdate.setCruiseResult(res.getCruiseResult());
                }
                if (Objects.nonNull(res.getPicPath())) {
                    tStdDeviceMeteUpdate.setPicPath(res.getPicPath());
                }
                log.info("此时的tStdDeviceMeteUpdate===" + tStdDeviceMeteUpdate);
                if (deviceMeteIdList.contains(res.getDeviceMeteId())) {
                    //更新
                    int updateRes = tCruiseDataResultDao.updateDeviceMeteUpdate(tStdDeviceMeteUpdate);
                    log.info(res.getDeviceMeteId() + "存在,更新值: " + updateRes);
                } else {
                    //插入
                    int insertRes = tCruiseDataResultDao.insertDeviceMeteUpdate(tStdDeviceMeteUpdate);
                    log.info(res.getDeviceMeteId() + "不存在,插入值: " + insertRes);
                }
            }
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<String> QueryDifferentiateResult(String taskId) {
        return tCruiseDataResultDao.selectResultImg(taskId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<FirAndPicInfo> selectByPicFirPage(String startDate, String fileName, String endDate, int pageNum, int pageSize) throws ParseException {
        TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
        tCruiseDataResult.setPageNum(pageNum);
        tCruiseDataResult.setPageSize(pageSize);
        tCruiseDataResult.setFirName(fileName);
        if (!startDate.isEmpty() && startDate != "") {
            tCruiseDataResult.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDate));
        }
        if (!endDate.isEmpty() && endDate != "") {
            tCruiseDataResult.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endDate));
        }
        List<FirAndPicInfo> listFir = new ArrayList<>();
        List<TCruiseDataResult> list = tCruiseDataResultDao.selectByPage(tCruiseDataResult);
        for (TCruiseDataResult t : list) {
            if (t.getResultPic() != null && t.getResultPic() != "") {
                File file = new File(t.getResultPic());
                FirAndPicInfo f = new FirAndPicInfo();
                f.setCruiseDataId(t.getCruiseDataId());
                String[] arr = file.getParent().split("/");
                f.setPicPath(arr[0] + "//" + arr[1] + "/" + arr[2] + "/resultImg" + "/" + t.getFirName() + ".jpg");
                f.setFirPath(t.getResultPic());
                f.setFirName(t.getFirName());
                f.setDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t.getFirDate()));
                listFir.add(f);
            }

        }
        return listFir;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<FirAndPicInfo> selectByCameraId(Long cameraId, String startDate, String endDate, String firName) {
        List<FirAndPicInfo> listFir = new ArrayList<>();
        List<TCruiseDataResult> list = tCruiseDataResultDao.selectByCameraId(cameraId,startDate,endDate,firName);
        for (TCruiseDataResult t : list) {
            if (t.getResultPic() != null && t.getResultPic() != "" &&!t.getResultPic().isEmpty()) {
                File file = new File(t.getResultPic());
                FirAndPicInfo f = new FirAndPicInfo();
                f.setCruiseDataId(t.getCruiseDataId());
                String[] arr = file.getParent().split("/");
                f.setPicPath(arr[0] + "//" + arr[1] + "/" + arr[2] + "/resultImg" + "/" + t.getFirName() + ".jpg");
                f.setFirPath(t.getResultPic());
                f.setFirName(t.getFirName());
                if (t.getFirDate()!=null){
                    f.setDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t.getFirDate()));
                }
                else {
                    f.setDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                }
                listFir.add(f);
            }
        }
        return listFir;
    }

}

