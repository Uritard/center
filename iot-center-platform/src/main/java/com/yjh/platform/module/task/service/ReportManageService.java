package com.yjh.platform.module.task.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.net.URLEncodeUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.common.utils.smUtil.report.ReportDataModel;
import com.yjh.platform.common.utils.smUtil.report.ReportDataRepo;
import com.yjh.platform.common.utils.smUtil.report.ReportHelper;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.entity.LineKeyValue;
import com.yjh.platform.module.patrol.entity.NonhomologousInfo;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.dao.ReportManageDao;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author YC
 * @date 2020/10/20 - 20:15
 */
@Service
public class ReportManageService {

    private final Logger log = LoggerFactory.getLogger(ReportManageService.class);
    @Autowired
    private ReportManageDao reportManageDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UPatrolResultDao uPatrolResultDao;
    @Autowired
    private UPatrolTaskService uPatrolTaskService;
    @Autowired
    private TStdRegionService stdRegionService;

    public static final Cache<String, Integer> REPORT_CACHE = CacheUtil.newFIFOCache(1000);

    @Transactional(rollbackFor = Exception.class)
    public int reportGenerate(Date startTime,Date endTime,String deviceIdList,String reportName,String reportType) {
        List<String> list = Arrays.asList(deviceIdList.split(","));
        ReportData recordData = new ReportData();

        // 明细
        List<TCruiseDataResultDetail> tCruiseDataResultDetailList =  uPatrolResultDao.selectDetail(list,startTime,endTime);
        String absPath = (String) redisTemplate.opsForHash().get("t_sys_param:prefixAbsolutePath", "content");
        String relPath = (String) redisTemplate.opsForHash().get("t_sys_param:prefixRelativePath", "content");
        // 相对路径替换绝对路径
        tCruiseDataResultDetailList.forEach(detail->{
            String resultPath = detail.getPicPath().replace(relPath, absPath);
            detail.setPicPath(resultPath);

            detail.setDataType(DictConvertUtil.DICT.covertToDict("cruiseType", detail.getCruiseType()));
        });
        recordData.setTCDRDList(tCruiseDataResultDetailList);

        // 概况
        TaskVO taskVO = getTaskVoDefined(null);
        taskVO.setCruiseStatistics(getTaskVoCount(tCruiseDataResultDetailList));
        recordData.setTaskVO(taskVO);

        // 报表名称:站所名称+报告名称+当前时间
        String stationName = (String) redisTemplate.opsForHash().get("t_sys_param:edgeName", "content");
        String fileNameTemp = stationName + "-" + reportName;
        String fileName = fileNameTemp + "-" + DateTimeUtil.format3(new Date()) + ".xlsx";

        String reportPathTemp = (String) redisTemplate.opsForHash().get("t_sys_param:reportReflect", "content");
        String reportPath = reportPathTemp + "/" + fileName;
        log.info("reportPath:{}", reportPath);
        File file = new File(reportPath);

        ContentData contentData = ReportDataRepo.getData(recordData);
        ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                contentData.getElements(), file,null);

        TReportInfo reportInfo = new TReportInfo()
                .setReportId(String.valueOf(UUID.randomUUID()).replace("-", ""))
                .setReportName(reportName)
                .setReportType(reportType)
                .setGenerateDate(new Date())
                .setReportEnvId(fileName);
        return reportManageDao.insertReport(reportInfo);
    }
    @Transactional(rollbackFor = Exception.class)
    public String reportDownload(String reportId) {
        String fileName = reportManageDao.selectReportEnvId(reportId);
        String reportPath = (String) redisTemplate.opsForHash().get("t_sys_param:reportRelative", "content");
        String filePath = reportPath + "/"+ fileName;
        log.info("filePath:{}", filePath);
        return filePath;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TReportInfo> reportSelect(String reportName,String startTime,String endTime){
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("reportName", reportName);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return reportManageDao.reportSelect(map);
    }
    @Transactional(rollbackFor = Exception.class)
    public int reportDelete(String reportId) {
        String reportPath = (String) redisTemplate.opsForHash().get("t_sys_param:reportReflect", "content");
        log.info("reportPath:{}", reportPath);

        String url = "ls "+reportPath+" | wc -w";
        long count = 0;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String lineForId = null;
            while ((lineForId = readerForId.readLine()) != null) {
                count = Long.parseLong(lineForId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("删除前文件的个数：{}", count);
        String fileName = reportManageDao.selectReportEnvId(reportId);
        String filePath = reportPath + "/" + fileName;
        log.info("filePath:{}", filePath);
        if(StringUtils.contains(filePath, "|")){
            log.error("路径错误，含非法字符串！{}", fileName);
            throw new BusinessException("路径错误，含非法字符串！");
        }
        String url2 = "rm -f "+filePath;
        try {
            Process processForId = Runtime.getRuntime().exec(url2);
            processForId.waitFor();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String url3 = "ls "+reportPath+" | wc -w";
        long count2 = 0;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", url3});
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String lineForId = null;
            while ((lineForId = readerForId.readLine()) != null) {
                count2 = Long.parseLong(lineForId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("删除后文件的个数：{}", count2);
        int res = 0;
        if (count - 1 == count2){
            res = reportManageDao.reportDelete(reportId);
        }
        return res;
    }

    public Integer reportCheckGenerate(String taskId, String userId){
        if (StringUtils.isEmpty(taskId)) {
            throw new BusinessException(ResultCodeEnum.PARAMERROR, "请选择正确的任务生成巡视报告");
        }
        synchronized (REPORT_CACHE) {
            Integer step = REPORT_CACHE.get(taskId);
            if (step != null) {
                log.warn("文件任务已经生成，等待执行完成...{}", step);
                return step;
            }
            REPORT_CACHE.put(taskId, 1);
        }

        ThreadPoolUtil.PATROL_POOL.addThread(()->{
            TaskVO taskVO = cruiseReportGenerate(taskId);
            pushDownload(taskVO, taskId, userId);
        });
        return 0;
    }

    public synchronized Integer reportGenerateProgress(String taskId) {
        return REPORT_CACHE.get(taskId);
    }

    public TaskVO cruiseReportGenerate(String taskId){

        try {
            // 明细
            List<TCruiseDataResultDetail> cruiseDataResultDetailList =  uPatrolResultDao.selectTaskResult(taskId);
            // 更新任务进度
            REPORT_CACHE.put(taskId, 15);
            log.info("查询任务结果完成，{}", taskId);
            List<String>  originalImgList = new ArrayList<>();
            boolean downResultPic = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("t_sys_param:downResultPic", "content"));
            Map<KeyValue<Long, String>, List<TCruiseDataResultDetail>> listMap = cruiseDataGroup(cruiseDataResultDetailList, originalImgList, downResultPic);
            REPORT_CACHE.put(taskId, 18);
            log.info("任务结果分组完成，{}", taskId);
            // UPatrolResult uPatrolResult = uPatrolResultDao.selectByPrimaryId(taskId);
            // String remark = uPatrolResult.getRemark();

            //顺便处理非同源合并问题
            List<NonhomologousInfo> nonList = uPatrolResultDao.selectWarnByTaskId(taskId);

            List<KeyValue<String, ContentData>> contentDataList = new ArrayList<>();
            TaskVO taskBaseVO = getTaskVoDefined(taskId);
            listMap.forEach((k, detailList)->{
                try {
                    TaskVO taskVO = new TaskVO();
                    BeanUtil.copyProperties(taskBaseVO, taskVO);
                    taskVO.setCruiseStatistics(getTaskVoCount(detailList));

                    ReportData recordData = new ReportData();
                    recordData.setTaskVO(taskVO);

                    recordData.setDownResultPic(downResultPic);

                    recordData.setTCDRDList(detailList);
                    recordData.setNonList(nonList);

                    ContentData contentData = ReportDataModel.getData(recordData);

                    contentDataList.add(new DefaultKeyValue<>(k.getValue(), contentData));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
            // 概况
            REPORT_CACHE.put(taskId, 25);
            log.info("任务概况统计计算完成，{}", taskId);

            String reportPath = (String) redisTemplate.opsForHash().get("t_sys_param:tempReflect", "content");
            File file = reportFile(taskBaseVO);
            if (!file.exists()) {
                FileUtil.mkdir(file.getParentFile());
                log.info("不存在，创建的文件绝对路径是==={}", file.getAbsolutePath());
            }else {
                log.info("存在，该文件绝对路径是==={}", file.getAbsolutePath());
            }

            ReportHelper.createDocument(contentDataList, file, taskId);
            try {
                //将任务下的巡视原图图片 打包成一份zip
                FileUtil.zip(originalImgList, taskId + ".zip", taskId, reportPath);
            }catch (Exception e){
                log.info("压缩任务下图片失败：",e);
            }
            return taskBaseVO;
        } catch (Exception e) {
            log.error("生成任务报告失败", e);
            throw new BusinessException("任务报告生成失败");
        } finally {
            REPORT_CACHE.remove(taskId);
            log.info("报告生成，删除任务缓存 {}", taskId);
        }
    }

    private File reportFile(TaskVO taskVO) {
        // 报告名称:站所名称+任务名称+巡视时间
        String fileNameTemp = taskVO.getStationName() + "-" + taskVO.getTaskName();
        String reportName = fileNameTemp + "-" + DateTimeUtil.format3(taskVO.getCruiseDate()) + ".xlsx";

        String reportPath = (String) redisTemplate.opsForHash().get("t_sys_param:tempReflect", "content");
        log.info("reportPath:{}", reportPath);

        String newReportPath = CommonUtils.concatPath(reportPath, reportName);
        return new File(newReportPath);
    }

    private void delaCount(TaskVO taskVO,List<TCruiseDataResultDetail> detailList){
        List<TCruiseDataResultDetail> abnormalList = new ArrayList<>();
        List<TCruiseDataResultDetail> normalList = new ArrayList<>();
        List<TCruiseDataResultDetail> unReviewList = new ArrayList<>();
//        for (TCruiseDataResultDetail cbsInspectionResultVo : tCDRDList) {
//            if (Objects.nonNull(cbsInspectionResultVo.getIdentifyResultName()) && !Objects.equals("正常", cbsInspectionResultVo.getIdentifyResultName())){
//                cbsInspectionResultVo.setIdentifyResultName("异常");
//                abnormalList.add(cbsInspectionResultVo);
//            }else if (Objects.nonNull(cbsInspectionResultVo.getIdentifyResultName()) && Objects.equals("正常", cbsInspectionResultVo.getIdentifyResultName())){
//                normalList.add(cbsInspectionResultVo);
//            }else if (Objects.nonNull(cbsInspectionResultVo.getEvaluationStateName()) && Objects.equals("未审核", cbsInspectionResultVo.getEvaluationStateName())){
//                cbsInspectionResultVo.setIdentifyResultName("待人工确认");
//                unReviewList.add(cbsInspectionResultVo);
//            }
//
//        }
        //根据要求 识别出来是异常 放在异常里
        // 点位状态 未执行、执行失败、未知 放在待人工确认里面
        // 正常的挡在正常里面
        for (TCruiseDataResultDetail cbsInspectionResultVo : detailList) {
            if (Objects.nonNull(cbsInspectionResultVo.getIdentifyResultName()) && !Objects.equals("正常", cbsInspectionResultVo.getIdentifyResultName())) {
                // i
                if (cbsInspectionResultVo.getCruiseState() == 253 ||
                        cbsInspectionResultVo.getCruiseState() == 254 ||
                        cbsInspectionResultVo.getCruiseState() == 255 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 248 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 249 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 251 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 410 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 411 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 412 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 350 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 351 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 352
                ) {
                    cbsInspectionResultVo.setIdentifyResultName("待人工确认");
                    unReviewList.add(cbsInspectionResultVo);
                } else {
                    cbsInspectionResultVo.setIdentifyResultName("异常");
                    abnormalList.add(cbsInspectionResultVo);
                }
            } else if (Objects.nonNull(cbsInspectionResultVo.getIdentifyResultName()) && Objects.equals("正常", cbsInspectionResultVo.getIdentifyResultName())) {
                if (cbsInspectionResultVo.getCruiseState() == 253 ||
                        cbsInspectionResultVo.getCruiseState() == 254 ||
                        cbsInspectionResultVo.getCruiseState() == 255 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 248 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 249 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 251 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 410 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 411 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 412 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 350 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 351 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 352
                ) {
                    cbsInspectionResultVo.setIdentifyResultName("待人工确认");
                    unReviewList.add(cbsInspectionResultVo);
                } else {
                    normalList.add(cbsInspectionResultVo);
                }
            } else {
                if (cbsInspectionResultVo.getCruiseState() == 253 ||
                        cbsInspectionResultVo.getCruiseState() == 254 ||
                        cbsInspectionResultVo.getCruiseState() == 255 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 248 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 249 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 251 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 410 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 411 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 350 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 351 ||
                        cbsInspectionResultVo.getCruiseAbnormal() == 352
                ) {
                    if (cbsInspectionResultVo.getIsWarn() == 1) {
                        abnormalList.add(cbsInspectionResultVo);
                    } else {
                        cbsInspectionResultVo.setIdentifyResultName("待人工确认");
                        unReviewList.add(cbsInspectionResultVo);
                    }
                } else {
                    normalList.add(cbsInspectionResultVo);
                }
            }
        }

        taskVO.setAbnormal(abnormalList.size());
        taskVO.setNormal(normalList.size());
        taskVO.setUnReview(unReviewList.size());
    }

    public TaskVO getTaskVO(String taskId,List<TCruiseDataResultDetail> tCruiseDataResultDetailList,String remark) {
        TaskVO taskVO = uPatrolResultDao.selectTaskNameAndTime(taskId);
        delaCount(taskVO,tCruiseDataResultDetailList);
        String stationName = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeName", "content"));
        String voltageClasses = redisTemplate.opsForHash().get("t_sys_param:stationVoltageGrade", "content") + "kV";
        String stationType = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:stationType", "content"));
        taskVO.setStationName(stationName);
        taskVO.setVoltageClasses(voltageClasses);
        taskVO.setStationType(stationType);

        String cruiseStatistics = "总点位" + taskVO.getTotal() + "个,已检点位" + taskVO.getAlready() + "个,未检点位" + taskVO.getWait()
                + "个,正常点位" + taskVO.getNormal() + "个,异常点位" + taskVO.getAbnormal() +  "个";
        if (Objects.nonNull(taskVO.getUnReview())){
            cruiseStatistics = cruiseStatistics +  ",待人工确认点位" + taskVO.getUnReview() + "个。";
        }
        taskVO.setCruiseStatistics(cruiseStatistics);
        if ("1".equals(remark)) {
            taskVO.setCruiseConclusion("已审核");
        } else {
            taskVO.setCruiseConclusion("未审核");
        }

        return taskVO;
    }

    public TaskVO getTaskVoDefined(String taskId) {
        // 报告名称:站所名称+任务名称+巡视时间
        TaskVO taskVOtemp;
        if (StringUtils.isEmpty(taskId)) {
            taskVOtemp = new TaskVO();
            String stationWeather = uPatrolTaskService.getStationWeather();
            taskVOtemp.setEnvInfo(stationWeather);
        } else {
            taskVOtemp = uPatrolResultDao.selectTaskNameAndTime(taskId);
        }

        String stationName = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeName", "content"));
        String voltageClasses = redisTemplate.opsForHash().get("t_sys_param:stationVoltageGrade", "content") + "kV";
        String stationType = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:stationType", "content"));
        taskVOtemp.setStationName(stationName);
        taskVOtemp.setVoltageClasses(voltageClasses);
        taskVOtemp.setStationType(stationType);

        return taskVOtemp;
    }

    public String getTaskVoCount(List<TCruiseDataResultDetail> tCruiseDataResultDetailList) {
        // 总点数
        long allCount = tCruiseDataResultDetailList.size();
        // 正常，巡视结果正常且无告警，且未审核或审核结果正常
        long normalCount = tCruiseDataResultDetailList.stream().filter(detail -> CommonUtils.equals(detail.getCruiseResult(), CruiseConstant.CRUISE_RESULT_NORMAL) && !CommonUtils.equals(detail.getIsWarn(), 1) && (CommonUtils.equals(detail.getEvaluationState(), CruiseConstant.EVALUATION_STATE_UN) || CommonUtils.equals(detail.getIdentifyResult(), CruiseConstant.IDENTIFY_RESULT_NORMAL))).count();
        // 待人工确认，未审核，且巡视结果异常
        long unReviewCount = tCruiseDataResultDetailList.stream().filter(detail -> CommonUtils.equals(detail.getEvaluationState(), CruiseConstant.EVALUATION_STATE_UN) && CommonUtils.equals(detail.getCruiseResult(), CruiseConstant.CRUISE_RESULT_ABNORMAL)).count();
        // 已检点数
        // long alreadyCount = tCruiseDataResultDetailList.stream().filter(detail ->
        //         !ArrayUtils.contains(new String[]{"超时", "任务终止", "设备检修中", "机器人离线,未执行", "机器人处于检修状态,未执行"}, detail.getResultDesc())).count();
        long alreadyCount = tCruiseDataResultDetailList.stream().filter(detail -> CommonUtils.equals(detail.getCruiseState(), CruiseConstant.CRUISE_STATE_DONE)).count();
        //未检点数
        long waitCount= allCount - alreadyCount;
        //异常点数
        long abnormalCount = allCount - normalCount - unReviewCount;

        StringJoiner stringJoiner = new StringJoiner(",","","。");
        stringJoiner.add("总点位" + allCount + "个");
        stringJoiner.add("已检点位" + alreadyCount + "个");
        stringJoiner.add("未检点位" + waitCount + "个");
        stringJoiner.add("正常点位" + normalCount + "个");
        stringJoiner.add("异常点位" + abnormalCount + "个");
        stringJoiner.add("待人工确认点位" + unReviewCount + "个");
        return stringJoiner.toString();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result downLoadCruiseReport(String taskId, String userId){
        if (StringUtils.isEmpty(taskId)) {
            throw new BusinessException(ResultCodeEnum.PARAMERROR, "请选择正确的任务生成巡视报告");
        }
        Result result = new Result();
        TaskVO taskVO = getTaskVoDefined(taskId);
        //自动生成巡视报告
        File file = reportFile(taskVO);
        Integer prog = reportGenerateProgress(taskId);
        if (!file.exists()) {
            prog = reportCheckGenerate(taskId, userId);
            if (prog > 0) {
                result.setMessage("任务报告正在生成中，请耐心等待，当前进度" + prog + "%");
            } else {
                result.setMessage("开始生成任务报告...");
            }
            log.info("生成巡视报告的进度是=={}", prog);
            return result;
        } else if (prog != null) {
            result.setMessage("任务报告正在生成中，请耐心等待，当前进度" + prog + "%");
            return result;
        } else {
            log.info("巡视报告已经生成，直接下载=={}", file.getAbsolutePath());
        }

        // 推送巡视报告下载
        ThreadPoolUtil.COMMON_POOL.addThread(()->pushDownload(taskVO, taskId, userId));

        result.setMessage("开始下载任务报告");
        return result;
    }

    private Map<KeyValue<Long, String>, List<TCruiseDataResultDetail>> cruiseDataGroup(
        List<TCruiseDataResultDetail> tCruiseDataResultDetailList, List<String> originalImgList, boolean downResultPic) {
        Map<Long, KeyValue<Long, String>> stationDownMap = stdRegionService.stationDownId();
        Map<KeyValue<Long, String>, List<TCruiseDataResultDetail>> listMap = new TreeMap<>(Comparator.comparing(KeyValue::getKey));

        String absPath = (String)redisTemplate.opsForHash().get("t_sys_param:prefixAbsolutePath", "content");
        String relPath = (String)redisTemplate.opsForHash().get("t_sys_param:prefixRelativePath", "content");

        Boolean reportGroupByStation = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:reportGroupByStation", "content"));
        // 相对路径替换绝对路径
        tCruiseDataResultDetailList.forEach(detail -> {
            String resultPath = detail.getPicPath().replace(relPath, absPath);
            detail.setPicPath(resultPath);

            String dowmPic = downResultPic ? resultPath : detail.getOriImg();
            if (!CommonUtils.isEmptyOrNullstr(dowmPic)) {
                originalImgList.add(dowmPic);
            }

            detail.setDataType(DictConvertUtil.DICT.covertToDict("cruiseType", detail.getCruiseType()));
            Long regionId = detail.getRegionId();

            KeyValue<Long, String> key;
            if (reportGroupByStation) {
                key = stationDownMap.getOrDefault(regionId, new LineKeyValue<>(regionId, detail.getRegionName()));
            } else {
                key = new LineKeyValue<>(0L, "巡检报告");
            }

            List<TCruiseDataResultDetail> list = listMap.computeIfAbsent(key, k -> new ArrayList<>());
            list.add(detail);
        });

        return listMap;
    }

    public void pushDownload(TaskVO taskVO, String taskId, String userId) {
        if (StringUtils.isNotEmpty(userId)) {
            try {
                // 报告名称:站所名称+任务名称+巡视时间
                String reportName = taskVO.getStationName() + "-" + taskVO.getTaskName() + "-" + DateTimeUtil.format3(taskVO.getCruiseDate()) + ".xlsx";

                String fileRelativePathTemp = SysParamConfig.getSysContent("meteModelPath");
                String fileRelativePath = CommonUtils.concatPath(fileRelativePathTemp, URLEncodeUtil.encode(reportName));
                log.info("excel文件下载路径是==={}", fileRelativePath);

                String zipName = taskId + ".zip";
                String zipRelativePath = CommonUtils.concatPath(fileRelativePathTemp, URLEncodeUtil.encode(zipName));
                log.info("zip文件下载路径是==={}", zipRelativePath);

                Map<String, Object> jasonMap = new HashMap<>(4);
                jasonMap.put("type", "cruiseDataReport");
                jasonMap.put("url", new String[]{fileRelativePath, zipRelativePath});
                log.info("发送给前端的消息 —— 巡视报告下载:{}", jasonMap);
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap, userId);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
