package com.yjh.platform.module.patrol.service;

import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.common.utils.smUtil.report.ExportUtil;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.patrol.dao.UPatrolDataResultDao;
import com.yjh.platform.module.patrol.entity.LineKeyValue;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                    FileUtil.mkdir(filePathAndName);
                    //拼接Excel文件名
                    String fileName = "巡检点列表数据" + System.currentTimeMillis() + ".xlsx";
                    String fileNamePath = filePathAndName + fileName;

                    //设置表头
                    List<List<String>> heads = Lists.newArrayList();
                    List<String> strings = Arrays.asList(typeString.split(","));
                    strings.forEach(s -> heads.add(Lists.newArrayList(s)));
                    //设置内容
                    List<List<Object>> contents = Lists.newArrayList();
                    cruiseResultAnalyzeInfoList.forEach(cruiseResult -> {
                        List<Object> content = Lists.newArrayList();
                        strings.forEach(s -> {
                            String value = String.valueOf(cruiseResult.getOrDefault(ExportUtil.map.get(s), ""));
                            if (StringUtils.contains(s, "图片")) {
                                content.add(new ImageFile(value));
                            } else {
                                content.add(value);
                            }
                        });
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
    public List<CruiseResultAnalyzeInfo> selectCruiseDataResultByList(Integer cruiseType, Integer cType, String deviceMeteIds, String meteType, Integer meterType, String endTime, String startTime) {

        List<CruiseResultAnalyzeInfo> cruiseResultAnalyzeInfoList = uPatrolDataResultDao.selectCruiseDataResultByList(cruiseType, cType,
            splitToLong(deviceMeteIds), meteType, meterType, endTime, startTime);
        DictConvertUtil.optional("cruiseType").add("identifyResult")
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
    public List<KeyValue<String, List<List<String>>>> selectBrokenLine(Integer cruiseType, Integer cType, String deviceMeteIds, String startTime, String endTime,
        String meteType, Integer meterType) {
        List<Long> meteIds = splitToLong(deviceMeteIds);
        List<BrokenLineInfo> brokenLineInfos =
            uPatrolDataResultDao.selectBrokenLine(cruiseType, cType, meteIds, startTime, endTime, meteType, meterType);

        Map<Long, List<BrokenLineInfo>> group = brokenLineInfos.stream().collect(Collectors.groupingBy(BrokenLineInfo::getDeviceMeteId));

        return dataSetParse(meteIds, group);
    }

    public boolean checkDate(String startTime, String endTime) {
        if (StringUtils.isAnyEmpty(startTime, endTime)) {
            return true;
        }
        Date start = DateTimeUtil.parse(startTime);
        Date toEnd = DateUtils.addMonths(start, 3);
        Date end = DateTimeUtil.parse(endTime);
        if (DateUtils.truncatedCompareTo(toEnd, end, Calendar.DAY_OF_MONTH) < 0) {
            throw new BusinessException(ResultCodeEnum.CODE10005.getCode(), "日期间隔不可以大于3个月");
        }
        return true;
    }

    private List<KeyValue<String, List<List<String>>>> dataSetParse(List<Long> meteIds, Map<Long, List<BrokenLineInfo>> group) {
        List<KeyValue<String, List<List<String>>>> dataset = new ArrayList<>();
        Map<Long, KeyValue<String, List<List<String>>>> kvMap = new HashMap<>(8);
        // 对分组后数据进行按组遍历
        group.forEach((k, v) -> {
            Long deviceMeteId = v.get(0).getDeviceMeteId();
            String meteName = v.get(0).getMeteName();

            List<List<String>> line = new ArrayList<>();
            List<String> fir = new ArrayList<>();
            Map<Date, String[]> dateIdx = new LinkedHashMap<>(64);
            Map<KeyValue<String, String>, Integer> deviceMap = new HashMap<>(8);

            // 第一行 ["product","2023-07-25 11:36:55","2023-07-25 11:41:15","2023-07-25 15:59:46","2023-07-26 11:25:44","2023-07-26 16:34:32"]
            fir.add("product");
            line.add(fir);

            // 记录巡视设备编号
            int idx = 1;
            // 解析成时间对应数组，数组内对应不同巡视设备的巡视值
            for (BrokenLineInfo vb : v) {
                KeyValue<String, String> cruiseDevice = new LineKeyValue<>(vb.getCruiseDeviceId(), vb.getCruiseDeviceName());
                Date time = vb.getCruiseTime();
                // 获取巡视设备序号，若不存在，则表示需新增一个巡视设备编号
                Integer dex = deviceMap.get(cruiseDevice);
                if (dex == null) {
                    dex = idx++;
                    deviceMap.put(cruiseDevice, dex);
                }
                String[] dateLine = dateIdx.get(time);
                // 根据巡视设备编号判断数组是否需要进行扩容
                if (dateLine == null || dateLine.length < idx) {
                    String[] newLine = new String[idx];
                    if (dateLine != null) {
                        System.arraycopy(dateLine, 0, newLine, 0, dateLine.length);
                    }
                    dateLine = newLine;
                    dateIdx.put(time, dateLine);
                }
                // 将巡视设备编号作为数组下标，存入巡视设备对应巡视值，对“,”分割的值取前面部分（当前特指红外测点）
                dateLine[dex] = StringUtils.substringBefore(vb.getResultNum(), ",");
            }

            // 初始化剩余行信息
            // ["大黑红外","-1","40.29,25.54","39.49,25.09","39.72,24.88","39.70,24.98","39.78,24.81"]
            // ["大白可见光","20","20",null,"20",null,"20"]
            List<String>[] dataLine = new List[deviceMap.size()];
            for (int i = 0; i < dataLine.length; i++) {
                dataLine[i] = new ArrayList<>();
            }

            // 将时间格式对应的测点转为需要的测点开头的数组
            dateIdx.forEach((dk, dv) -> {
                fir.add(DateTimeUtil.format(dk));
                int i = 0;
                for (Map.Entry<KeyValue<String, String>, Integer> entry : deviceMap.entrySet()) {
                    Integer iv = entry.getValue();
                    KeyValue<String, String> ik = entry.getKey();
                    if (dataLine[i].isEmpty()) {
                        dataLine[i].add(ik.getValue());
                    }
                    // 取值时判断一下下标是否超过数组长度，若两种巡视设备值没有交叉，则会导致数组长度不满足要求
                    dataLine[i].add(dv.length <= iv ? null : dv[iv]);
                    i++;
                }

            });
            line.addAll(Arrays.asList(dataLine));

            kvMap.put(deviceMeteId, new DefaultKeyValue<>(meteName, line));
        });
        // 按传入顺序组装数据返回
        for (Long id : meteIds) {
            if (kvMap.containsKey(id)) {
                dataset.add(kvMap.get(id));
            }
        }
        return dataset;
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
                // 批量更新点位图片和状态
                uPatrolDataResultDao.updateDeviceMeteUpdate(taskId);

                // 新节点插入
                if (CollectionUtils.isNotEmpty(list)) {
                    log.info("点位不存在，插入新数据，list==={}", JSON.toJSONString(list));
                    uPatrolDataResultDao.batchInsertDeviceMeteUpdate(list);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public List<Long> splitToLong(String strs){
        String[] ss = StringUtils.split(strs, ",");
        return Arrays.stream(ss).map(NumberUtils::toLong).collect(Collectors.toList());
    }

    public void cruiseDataReport(Integer cruiseType, Integer cType, String deviceMeteIds, String meteType, Integer meterType,
        String endTime, String startTime, String userId) {
        List<Long> meteIds = splitToLong(deviceMeteIds);
        List<BrokenLineInfo> brokenLineInfos =
            uPatrolDataResultDao.selectBrokenLine(cruiseType, cType, meteIds, startTime, endTime, meteType, meterType);
        DictConvertUtil.optional("cruiseType").add("meteType").add("meterType").add("identifyResult").add("cruiseResult")
            .covertToDict(brokenLineInfos);

        Runnable runnable = () -> {
            try {
                Map<Long, List<BrokenLineInfo>> group = brokenLineInfos.stream().peek(m -> {
                    if (CommonUtils.isEmptyOrNullstr(m.getMeterTypeName())) {
                        m.setMeterTypeName(m.getMeteTypeName());
                    }
                    if (Objects.isNull(m.getIdentifyResult())) {
                        m.setIdentifyResultName(m.getCruiseResultName());
                    }
                }).collect(Collectors.groupingBy(BrokenLineInfo::getDeviceMeteId));

                //创建本地文件路径
                String filePathAndName = (String)redisTemplate.opsForHash().get("t_sys_param:tempReflect", "content") + File.separatorChar;
                //创建文件夹
                FileUtil.mkdir(filePathAndName);
                //拼接Excel文件名
                String fileName = "巡视结果分析" + System.currentTimeMillis() + ".xlsx";
                String fileNamePath = filePathAndName + fileName;

                String absPath = (String) redisTemplate.opsForHash().get("t_sys_param:prefixAbsolutePath", "content");
                String relPath = (String) redisTemplate.opsForHash().get("t_sys_param:prefixRelativePath", "content");

                //设置表头
                List<List<String>> heads = Lists.newArrayList();
                List<String> strings = Arrays.asList("巡视点名称", "设备", "识别类型", "数据来源", "值", "识别结果", "巡视时间", "图片");
                strings.forEach(s -> heads.add(Lists.newArrayList(s)));
                //设置内容
                List<List<Object>> contents = Lists.newArrayList();
                brokenLineInfos.forEach(lineInfo -> {
                    List<Object> content = Lists.newArrayList();
                    content.add(lineInfo.getInstanceName());
                    content.add(lineInfo.getDeviceName());
                    content.add(lineInfo.getMeterTypeName());
                    content.add(lineInfo.getCruiseTypeName());
                    content.add(lineInfo.getResultDesc());
                    content.add(lineInfo.getIdentifyResultName());
                    content.add(lineInfo.getCruiseTime());

                    String imagePath = StringUtils.replace(lineInfo.getPicPath(), relPath, absPath);
                    content.add(new ImageFile(imagePath));
                    contents.add(content);
                });
                ExcelWriter excelWriter = EasyExcel.write(fileNamePath).build();
                WriteSheet writeSheet =
                    EasyExcel.writerSheet(0, "巡视结果").includeColumnFiledNames(ExportUtil.getCruiseDataReportModel(strings))
                        .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
                        .registerWriteHandler(new SimpleRowHeightStyleStrategy((short)25, (short)100))
                        .registerConverter(new ImageConverter()).head(heads).registerWriteHandler(ExportUtil.getCellStyle()).build();
                excelWriter.write(contents, writeSheet);

                Workbook workbook = excelWriter.writeContext().writeWorkbookHolder().getWorkbook();
                Drawing<?> drawing = excelWriter.writeContext().writeSheetHolder().getSheet().createDrawingPatriarch();
                CreationHelper creationHelper = workbook.getCreationHelper();

                List<byte[]> imageBytes = buildImages(meteIds, group);
                for (int i = 0; i < imageBytes.size(); i++) {
                    int pictureIdx = workbook.addPicture(imageBytes.get(i), Workbook.PICTURE_TYPE_JPEG);
                    ClientAnchor anchor = creationHelper.createClientAnchor();
                    anchor.setCol1(1);
                    anchor.setRow1(contents.size() + 3 + (i * 31)); // 替换 数据的最后一行
                    Picture picture = drawing.createPicture(anchor, pictureIdx);
                    picture.resize(1.0); // 图片缩放比例
                }

                //关闭写excel
                excelWriter.finish();

                String fileRelativePath = redisTemplate.opsForHash().get("t_sys_param:meteModelPath", "content") + "/" + fileName;
                Map<String, String> jasonMap = new HashMap<>(2);
                jasonMap.put("type", "cruiseDataReport");
                jasonMap.put("url", fileRelativePath);
                log.info("发送给前端的消息:{}", jasonMap);
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap, userId);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        };
        ThreadPoolUtil.COMMON_POOL.addThread(runnable);
    }

    private List<byte[]> buildImages(List<Long> meteIds, Map<Long, List<BrokenLineInfo>> group) {
        List<byte[]> imageBytes = Lists.newArrayList();

        List<KeyValue<String, List<List<String>>>> dataset = dataSetParse(meteIds, group);

        for (KeyValue<String, List<List<String>>> data : dataset) {
            String name = data.getKey();

            List<List<String>> dataLine = data.getValue();
            //x轴名称列表
            List<String> xAxisNameList =
                dataLine.get(0).stream().skip(1).map(m -> StringUtils.substring(m, 5)).collect(Collectors.toList());
            //图例名称列表
            List<String> legendNameList = new ArrayList<>();
            //数据列表
            List<List<?>> dataList = new ArrayList<>();
            for (int i = 1; i < dataLine.size(); i++) {
                List<String> line = dataLine.get(i);
                legendNameList.add(line.remove(0));
                dataList.add(line);
            }

            try {
                byte[] imageByte =
                    GenerateChartUtil.createLineChart(name, legendNameList, xAxisNameList, dataList, JFreeChartUtil.createChartTheme("宋体"),
                        "", "", 1000, 600);
                imageBytes.add(imageByte);
            } catch (Exception e) {
                log.error("生成图片失败", e);
            }
        }

        return imageBytes;
    }
}

