package com.yjh.platform.module.patrol.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.common.utils.ImageConverter;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.common.utils.smUtil.report.ExportUtil;
import com.yjh.platform.common.utils.smUtil.report.FileUtil;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.patrol.dao.UPatrolDataResultDao;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
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
    public List<CruiseResultAnalyzeMeteInfo> selectCruiseResultAnalyze(List<Long> deviceIdList, Integer deviceType, String meteType, Integer meterType, Integer cruiseRes, Long customId,String meteName) {
        List<CruiseResultAnalyzeMeteInfo> cruiseResultAnalMeteInfoList = new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()) {
            //cruiseRes:-1全部,1正常,0异常
            if (cruiseRes == 1) {
                cruiseResultAnalMeteInfoList = tStdDevicemeteDao.selectCruiseResultAnalyze(deviceIdList, deviceType, meteType, meterType, cruiseRes, customId, meteName);
            } else {
                cruiseResultAnalMeteInfoList = tStdDevicemeteDao.selectCruiseResultAnalyze2(deviceIdList, deviceType, meteType, meterType, cruiseRes, customId, meteName);
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
        DictConvertUtil.DictOptional optional = DictConvertUtil
            .optional("cruiseType")
            .add("planType","taskType","cTypeName")
            .add("identifyResult")
            .add("cruiseResult")
            .add("meteType")
            .add("meterType")
            .add("deviceType")
            .add("alarmLevel");
        DictConvertUtil.DICT.covertToDict(cruiseResultAnalyzeInfoList,optional);

        return cruiseResultAnalyzeInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> exportCruiseDataReport(Integer cType, String meteType, Integer meterType, String endTime, String startTime, List<Long> deviceIdList, String instanceName, String stationName) {

        List<Map<String, Object>> cruiseResultAnalyzeInfoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(deviceIdList)) {

            cruiseResultAnalyzeInfoList = uPatrolDataResultDao.exportCruiseDataReport(cType, meteType, meterType, endTime, startTime, deviceIdList, instanceName, stationName);

            List<TStdRegion> stdRegionList = tStdRegionDao.selectAll();
            Map<Long, TStdRegion> regionMaps = stdRegionList.stream().collect(Collectors.toMap(TStdRegion::getRegionId, Function.identity()));


            cruiseResultAnalyzeInfoList.forEach(cruiseResultAnalyzeInfoMap -> {
                if (Objects.isNull(cruiseResultAnalyzeInfoMap.get("identifyResult"))) {
                    cruiseResultAnalyzeInfoMap.put("identifyResult", cruiseResultAnalyzeInfoMap.get("identifyResult"));
                }
                if (Objects.isNull(cruiseResultAnalyzeInfoMap.get("personCheck"))) {
                    cruiseResultAnalyzeInfoMap.put("personCheck", cruiseResultAnalyzeInfoMap.get("personCheck"));
                }

                if (Objects.nonNull(cruiseResultAnalyzeInfoMap.get("regionId"))) {
                    TStdRegion tStdRegion = regionMaps.get(MapUtils.getLongValue(cruiseResultAnalyzeInfoMap, "regionId"));
                    Long upRegionId = tStdRegion.getUpRegionId();
                    TStdRegion up = regionMaps.get(upRegionId);
                    if (Objects.nonNull(up)) {
                        cruiseResultAnalyzeInfoMap.put("regionName", up.getRegionName());
                    } else {
                        cruiseResultAnalyzeInfoMap.put("regionName", tStdRegion.getRegionName());
                    }
                }
                cruiseResultAnalyzeInfoMap.put("evaluationState", "257".equals(MapUtils.getString(cruiseResultAnalyzeInfoMap, "evaluationState")) ? "未审核" : "已审核");
            });
        }
        DictConvertUtil.DictOptional optional = DictConvertUtil
                .optional("cruiseType")
                .add("planType", "taskType", "cTypeName")
                .add("identifyResult")
                .add("cruiseResult")
                .add("meteType")
                .add("meterType")
                .add("deviceType")
                .add("alarmLevel");
        DictConvertUtil.DICT.covertToDict(cruiseResultAnalyzeInfoList, optional);

        return cruiseResultAnalyzeInfoList;
    }

    @Async
    public void createCruiseDataReport(String userId, List<Map<String, Object>> cruiseResultAnalyzeInfoList, String typeString) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    //创建本地文件路径
                    Map<String, String> resMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
                    String filePathAndName = resMap.get("content") + File.separator;
                    //创建文件夹
                    FileUtil.createDirectory(filePathAndName);
                    //拼接Excel文件名
                    String fileName = "巡检点列表数据" + System.currentTimeMillis() + ".xlsx";
                    String fileNamePath = filePathAndName + fileName;

                    //设置表头
                    List<List<String>> heads = Lists.newArrayList();
                    List<String> strings = Arrays.asList(typeString.split(","));
                    strings.forEach(s -> heads.add(Lists.newArrayList(s)));
                    //设置内容
                    List<List<String>> contents = Lists.newArrayList();
                    cruiseResultAnalyzeInfoList.forEach(cruiseResult -> {
                        List<String> content = Lists.newArrayList();
                        strings.forEach(s -> content.add(String.valueOf(cruiseResult.getOrDefault(ExportUtil.map.get(s), ""))));
                        contents.add(content);
                    });
                    ExcelWriter excelWriter = EasyExcel.write(fileNamePath).build();
                    WriteSheet writeSheet = EasyExcel.writerSheet(0, "巡检点列表数据")
                            .includeColumnFiledNames(ExportUtil.getCruiseDataReportModel(strings))
                            .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
                            .registerWriteHandler(new SimpleRowHeightStyleStrategy((short) 25, (short) 100))
                            .registerConverter(new ImageConverter())
                            .head(heads).registerWriteHandler(ExportUtil.getCellStyle()).build();
                    excelWriter.write(contents, writeSheet);
                    //关闭写excel
                    excelWriter.finish();
                    Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:meteModelPath");
                    String fileRelativePath = map.get("content") + "/" + fileName;

                    Map<String, String> jasonMap = new HashMap<>(2);
                    jasonMap.put("type", "cruiseDataReport");
                    jasonMap.put("url", fileRelativePath);
                    log.info("发送给前端的消息:{}", jasonMap);
                    Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap, userId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
        ThreadPoolUtil.COMMON_POOL.addThread(runnable);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultAnalyzeInfo> selectCruiseDataResultByList2(Integer cruiseType, Integer cType, Long deviceMeteId, String meteType, Integer meterType, String endTime, String startTime, int pageNum, int pageSize) {

        List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = uPatrolDataResultDao.selectCruiseDataResultByList2(cruiseType, cType, deviceMeteId, meteType, meterType, endTime, startTime);
        DictConvertUtil.optional("cruiseType").add("planType", "taskType", "cTypeName").add("identifyResult")
            .add("cruiseResult").add("meteType").add("meterType").covertToDict(cruiseResultAnalyzeInfoList);

        for (CruiseResultAnalyzeInfo cruiseResultAnalInfo : cruiseResultAnalyzeInfoList) {
            if (Objects.isNull(cruiseResultAnalInfo.getIdentifyResult())) {
                cruiseResultAnalInfo.setIdentifyResultName(cruiseResultAnalInfo.getCruiseResultName());
            }
            if (StringUtils.isNotEmpty(cruiseResultAnalInfo.getPersonCheck())) {
                cruiseResultAnalInfo.setResultDesc(cruiseResultAnalInfo.getPersonCheck());
            }
            if (StringUtils.isEmpty(cruiseResultAnalInfo.getModifyNum())) {
                cruiseResultAnalInfo.setModifyNum(cruiseResultAnalInfo.getResultNum());
            }
            if (CommonUtils.isEmptyOrNullstr(cruiseResultAnalInfo.getMeterTypeName())) {
                cruiseResultAnalInfo.setMeterTypeName(cruiseResultAnalInfo.getMeteTypeName());
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
        DictConvertUtil.optional("cruiseType").add("planType", "taskType", "cTypeName").add("meteType").add("meterType")
            .covertToDict(brokenLineInfos);

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

    public void updateCruiseAnalyze(String taskId) {
        log.info("updateCruiseAnalyze taskId==={}", taskId);
        if (StringUtils.isNotEmpty(taskId)) {
            try {
                // 查询不在 t_std_devicemete_update 表中的新节点
                List<TStdDeviceMeteUpdate> list = uPatrolDataResultDao.selectDeviceMeteList(taskId);
                log.info("list==={}", JSON.toJSONString(list));
                List<Long> deviceMeteIdList = uPatrolDataResultDao.selectAllDeviceMeteId();
                // 批量更新点位图片和状态
                uPatrolDataResultDao.updateDeviceMeteUpdate(taskId);

                // 新节点插入
                for (TStdDeviceMeteUpdate res : list) {

                    TStdDeviceMeteUpdate tStdDeviceMeteUpdate =
                        new TStdDeviceMeteUpdate().setDeviceMeteId(res.getDeviceMeteId()).setIdentifyResult(res.getIdentifyResult());
                    if (StringUtils.isNotEmpty(res.getUpdateTime())) {
                        tStdDeviceMeteUpdate.setUpdateTime(res.getUpdateTime());
                    }
                    if (Objects.nonNull(res.getCruiseResult())) {
                        tStdDeviceMeteUpdate.setCruiseResult(res.getCruiseResult());
                    }
                    if (StringUtils.isNotEmpty(res.getPicPath())) {
                        tStdDeviceMeteUpdate.setPicPath(res.getPicPath());
                    }
                    log.info("此时的tStdDeviceMeteUpdate=== {}", tStdDeviceMeteUpdate);
                    if (!deviceMeteIdList.contains(res.getDeviceMeteId())) {
                        // 插入新的点位数据
                        int insertRes = uPatrolDataResultDao.insertDeviceMeteUpdate(tStdDeviceMeteUpdate);
                        log.info("{}不存在,插入结果: {}", res.getDeviceMeteId(), insertRes);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}

