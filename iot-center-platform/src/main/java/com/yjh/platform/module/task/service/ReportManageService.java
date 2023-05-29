package com.yjh.platform.module.task.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.smUtil.report.ReportDataModel;
import com.yjh.platform.common.utils.smUtil.report.ReportDataRepo;
import com.yjh.platform.common.utils.smUtil.report.ReportHelper;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.entity.NonhomologousInfo;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.dao.ReportManageDao;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.lang3.ArrayUtils;
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
        });
        recordData.setTCDRDList(tCruiseDataResultDetailList);

        // 概况
        TaskVO taskVO = getTaskVoDefined(tCruiseDataResultDetailList);
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
    @Transactional(rollbackFor = Exception.class)
    public String cruiseReportGenerate(String taskId){
        ReportData recordData = new ReportData();

        // 明细
        List<TCruiseDataResultDetail> tCruiseDataResultDetailList =  uPatrolResultDao.selectTaskResult(taskId);
        UPatrolResult uPatrolResult = uPatrolResultDao.selectByPrimaryId(taskId);
        String remark = uPatrolResult.getRemark();

        //顺便处理非同源合并问题
        List<NonhomologousInfo> nonList = uPatrolResultDao.selectWarnByTaskId(taskId);
        // 概况
        TaskVO taskVO = getTaskVO(taskId,tCruiseDataResultDetailList,remark);
        recordData.setTaskVO(taskVO);

        String absPath = (String) redisTemplate.opsForHash().get("t_sys_param:prefixAbsolutePath", "content");
        String relPath = (String) redisTemplate.opsForHash().get("t_sys_param:prefixRelativePath", "content");
        List<String>  originalImgList = new ArrayList<>();
        // 相对路径替换绝对路径
        tCruiseDataResultDetailList.forEach(detail->{
            String resultPath = detail.getPicPath().replace(relPath, absPath);
            detail.setPicPath(resultPath);
            if (Objects.isNull(detail.getOriImg())) {
                originalImgList.add(null);
            } else {
                originalImgList.add(detail.getOriImg());
            }
        });
        recordData.setTCDRDList(tCruiseDataResultDetailList);
        recordData.setNonList(nonList);

        // 报告名称:站所名称+任务名称+巡视时间
        String fileNameTemp = taskVO.getStationName() + "-" + taskVO.getTaskName();
        String reportName = fileNameTemp + "-" + DateTimeUtil.format3(taskVO.getCruiseDate()) + ".xlsx";

        String reportPath = (String) redisTemplate.opsForHash().get("t_sys_param:tempReflect", "content");
        log.info("reportPath:{}", reportPath);

        File temporaryFile = new File(reportPath);
        String newReportPath = null;
        if (!temporaryFile.exists() && !temporaryFile.isDirectory()) {
            temporaryFile.mkdir();
            newReportPath = reportPath + "/" + reportName;
            log.info("不存在，创建的文件绝对路径是==={}", newReportPath);
            File file = new File(reportPath);
            ContentData contentData = ReportDataModel.getData(recordData);

            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                    contentData.getElements(), file,taskId);
        }else {
            newReportPath = reportPath + "/" + reportName;
            log.info("存在，该文件绝对路径是==={}", newReportPath);
            File file = new File(newReportPath);
            ContentData contentData = ReportDataModel.getData(recordData);

            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                    contentData.getElements(), file,taskId);
        }
        try {
            //将任务下的巡视原图图片 打包成一份zip
            FileUtil.zip(originalImgList, taskId + ".zip", taskId, reportPath);
        }catch (Exception e){
            log.info("压缩任务下图片失败：",e);
        }
        return newReportPath;
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

    public TaskVO getTaskVoDefined(List<TCruiseDataResultDetail> tCruiseDataResultDetailList) {
        TaskVO taskVO = new TaskVO();
        String stationName = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeName", "content"));
        String voltageClasses = redisTemplate.opsForHash().get("t_sys_param:stationVoltageGrade", "content") + "kV";
        String stationType = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:stationType", "content"));
        taskVO.setStationName(stationName);
        taskVO.setVoltageClasses(voltageClasses);
        taskVO.setStationType(stationType);

        long allCount = tCruiseDataResultDetailList.size();
        long normalCount = tCruiseDataResultDetailList.stream().filter(detail -> StringUtils.equals("正常", detail.getIdentifyResultName())).count();
        long abnormalCount = allCount - normalCount;
        long unReviewCount = tCruiseDataResultDetailList.stream().filter(detail -> StringUtils.equals("未审核", detail.getEvaluationStateName())).count();
        long alreadyCount = tCruiseDataResultDetailList.stream().filter(detail ->
                !ArrayUtils.contains(new String[]{"超时", "任务终止", "设备检修中", "机器人离线,未执行", "机器人处于检修状态,未执行"}, detail.getResultNum())).count();
        long waitCount= allCount - alreadyCount;

        String cruiseStatistics = "总点位" + allCount + "个" +
                ",已检点位" + alreadyCount + "个" +
                ",未检点位" + waitCount + "个" +
                ",正常点位" + normalCount + "个" +
                ",异常点位" + abnormalCount +  "个";
        if (0 != unReviewCount){
            cruiseStatistics = cruiseStatistics +  ",待人工确认点位" + unReviewCount + "个。";
        }
        taskVO.setCruiseStatistics(cruiseStatistics);
        // 当前站内环境信息
        String stationWeather = uPatrolTaskService.getStationWeather();
        taskVO.setEnvInfo(stationWeather);
        return taskVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public Result  downLoadCruiseReport(String taskId,String remark){
        Result result = new Result();
        if ("0".equals(remark)) {
            //自动生成巡视报告
            String reportFilePath = cruiseReportGenerate(taskId);
            log.info("自动生成巡视报告的路径是=={}", reportFilePath);
        }

        // 报告名称:站所名称+任务名称+巡视时间
        TaskVO taskVO = uPatrolResultDao.selectTaskNameAndTime(taskId);
        String stationName = (String) redisTemplate.opsForHash().get("t_sys_param:edgeName", "content");
        String fileNameTemp = stationName + "-" + taskVO.getTaskName();
        String reportName = fileNameTemp + "-" + DateTimeUtil.format3(taskVO.getCruiseDate()) + ".xlsx";

        String fileRelativePathTemp = (String) redisTemplate.opsForHash().get("t_sys_param:meteModelPath", "content");
        String fileRelativePath = fileRelativePathTemp + "/" + reportName;
        log.info("excel文件相对路径是==={}", fileRelativePath);

        String zipName = taskId + ".zip";
        String zipRelativePath = fileRelativePathTemp + "/" + zipName;
        log.info("zip文件相对路径是==={}", zipRelativePath);

        Map<String, String> fileMap = new HashMap<>(2);
        fileMap.put("reportPath", fileRelativePath);
        fileMap.put("zipPath", zipRelativePath);
        result.setData(fileMap);
        return result;
    }

}
