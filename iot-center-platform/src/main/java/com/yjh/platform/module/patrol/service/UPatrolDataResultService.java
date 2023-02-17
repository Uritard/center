package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.patrol.dao.UPatrolDataResultDao;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * @author czh
 * @since 2020-08-25
 */
@Service
public class UPatrolDataResultService {

    private final UPatrolDataResultDao uPatrolDataResultDao;
    private final TStdDevicemeteDao tStdDevicemeteDao;
    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String REGION_PREFIX = "region";

    @Autowired
    public UPatrolDataResultService(UPatrolDataResultDao uPatrolDataResultDao, TStdDevicemeteDao tStdDevicemeteDao, RestTemplate restTemplate) {
        this.uPatrolDataResultDao = uPatrolDataResultDao;
        this.tStdDevicemeteDao = tStdDevicemeteDao;
    }

    private Logger log = LoggerFactory.getLogger(UPatrolDataResultService.class);

    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalyzeMeteInfo> selectCruiseResultAnalyze(List<Long> deviceIdList, Integer deviceType, String meteType, Integer meterType, Integer cruiseRes, Long customId) {
        List<CruiseResultAnalyzeMeteInfo> cruiseResultAnalMeteInfoList = new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()) {
            //cruiseRes:-1全部,1正常,0异常
            if (cruiseRes == 1) {
                cruiseResultAnalMeteInfoList = tStdDevicemeteDao.selectCruiseResultAnalyze(deviceIdList, deviceType, meteType, meterType, cruiseRes, customId);
            } else {
                cruiseResultAnalMeteInfoList = tStdDevicemeteDao.selectCruiseResultAnalyze2(deviceIdList, deviceType, meteType, meterType, cruiseRes, customId);
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
    public List<CruiseResultAnalyzeInfo> selectCruiseDataReport(Integer cType, String meteType, Integer meterType, String endTime, String startTime, List<Long> deviceIdList, String instanceName, String stationName) {


        List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(deviceIdList)) {

            cruiseResultAnalyzeInfoList = uPatrolDataResultDao.selectCruiseDataReport(cType, meteType, meterType, endTime, startTime, deviceIdList, instanceName, stationName);

            List<TStdRegion> stdRegionList = tStdRegionDao.selectAll();
            Map<Long,TStdRegion> regionMaps = stdRegionList.stream().collect(Collectors.toMap(TStdRegion::getRegionId,Function.identity()));
            for (CruiseResultAnalyzeInfo cruiseResultAnalyzeInfo : cruiseResultAnalyzeInfoList) {
                if (Objects.isNull(cruiseResultAnalyzeInfo.getIdentifyResult())) {
                    cruiseResultAnalyzeInfo.setIdentifyResultName(cruiseResultAnalyzeInfo.getIdentifyResultName());
                }
                if (Objects.isNull(cruiseResultAnalyzeInfo.getPersonCheck())) {
                    cruiseResultAnalyzeInfo.setPersonCheck(cruiseResultAnalyzeInfo.getPersonCheck());
                }

                if (Objects.nonNull(cruiseResultAnalyzeInfo.getRegionId())) {
                    TStdRegion tStdRegion = regionMaps.get(cruiseResultAnalyzeInfo.getRegionId());
                    Long upRegionId = tStdRegion.getUpRegionId();
                    TStdRegion up = regionMaps.get(upRegionId);
                    if (Objects.nonNull(up)){
                        cruiseResultAnalyzeInfo.setRegionName(up.getRegionName() );
                    } else {
                        cruiseResultAnalyzeInfo.setRegionName(tStdRegion.getRegionName());
                    }
                }
                cruiseResultAnalyzeInfo.setEvaluationState("257".equals(cruiseResultAnalyzeInfo.getEvaluationState()) ? "未审核" : "已审核");
            }
        }
        return cruiseResultAnalyzeInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalyzeInfo> selectCruiseDataResultByList2(Integer cruiseType, Integer cType, Long deviceMeteId, String meteType, Integer meterType, String endTime, String startTime, int pageNum, int pageSize) {

        List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = uPatrolDataResultDao.selectCruiseDataResultByList2(cruiseType, cType, deviceMeteId, meteType, meterType, endTime, startTime);
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

        List<BrokenLineInfo> brokenLineInfos = uPatrolDataResultDao.selectBrokenLine(cruiseType, cType, deviceMeteId, startTime, endTime, meteType, meterType);
        for (BrokenLineInfo point : brokenLineInfos) {
            if (point.getResultNum().matches("^[a-zA-Z_\\u4e00-\\u9fa5_\\--]+$")) {
                point.setResultNum("0");
            }
        }
        return brokenLineInfos;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<FirAndPicInfo> selectByCameraId(Long cameraId, String startDate, String endDate, String firName) {
        List<FirAndPicInfo> listFir = new ArrayList<>();
        List<TCruiseDataResult> list = uPatrolDataResultDao.selectByCameraId(cameraId, startDate, endDate, firName);
        for (TCruiseDataResult t : list) {
            if (t.getResultPic() != null && t.getResultPic() != "" && !t.getResultPic().isEmpty()) {
                File file = new File(t.getResultPic());
                FirAndPicInfo f = new FirAndPicInfo();
                f.setCruiseDataId(t.getCruiseDataId());
                String[] arr = file.getParent().split("/");
                f.setPicPath(arr[0] + "//" + arr[1] + "/" + arr[2] + "/resultImg" + "/" + t.getFirName() + ".jpg");
                f.setFirPath(t.getResultPic());
                f.setFirName(t.getFirName());
                if (t.getFirDate() != null) {
                    f.setDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t.getFirDate()));
                } else {
                    f.setDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                }
                listFir.add(f);
            }
        }
        return listFir;
    }

    public int updateCruiseAnalyze(String taskId) {
        log.info("updateCruiseAnalyze taskId==={}", taskId);
        if (StringUtils.isNotEmpty(taskId)) {
            List<TStdDeviceMeteUpdate> list = uPatrolDataResultDao.selectDeviceMeteList(taskId);
            log.info("list==={}", JSON.toJSONString(list));

            for (TStdDeviceMeteUpdate res : list) {
                List<Long> deviceMeteIdList = uPatrolDataResultDao.selectAllDeviceMeteId();
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
                    int updateRes = uPatrolDataResultDao.updateDeviceMeteUpdate(tStdDeviceMeteUpdate);
                    log.info(res.getDeviceMeteId() + "存在,更新值: " + updateRes);
                } else {
                    //插入
                    int insertRes = uPatrolDataResultDao.insertDeviceMeteUpdate(tStdDeviceMeteUpdate);
                    log.info(res.getDeviceMeteId() + "不存在,插入值: " + insertRes);
                }
            }
        }
        return 1;
    }
}

