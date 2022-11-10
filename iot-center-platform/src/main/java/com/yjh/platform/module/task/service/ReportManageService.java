package com.yjh.platform.module.task.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.smUtil.report.ReportDataModel;
import com.yjh.platform.common.utils.smUtil.report.ReportDataRepo;
import com.yjh.platform.common.utils.smUtil.report.ReportHelper;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.task.dao.ReportManageDao;
import com.yjh.platform.module.task.dao.TCruiseDataResultDao;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author YC
 * @date 2020/10/20 - 20:15
 */
@Service
public class ReportManageService {

    private Logger log = LoggerFactory.getLogger(ReportManageService.class);
    @Autowired
    private ReportManageDao reportManageDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TStdDeviceDao tStdDeviceDao;
    @Autowired
    private TCruiseDataResultDao tCruiseDataResultDao;
    @Autowired
    private UPatrolResultDao uPatrolResultDao;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    private DateTimeUtil dateTimeUtil;
    @Transactional(rollbackFor = Exception.class)
    public int reportGenerate(Date startTime,Date endTime,String deviceIdList,String reportName,String reportType) {
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> relativeImgMap1 = redisTemplate.opsForHash().entries("t_sys_param:meterResultRealImg");
        Map<String, String> absoluteImgMap1 = redisTemplate.opsForHash().entries("t_sys_param:meterResultImg");
        Map<String, String> relativeImgMap2 = redisTemplate.opsForHash().entries("t_sys_param:judgeResultRealImg");
        Map<String, String> absoluteImgMap2 = redisTemplate.opsForHash().entries("t_sys_param:judgeResultImg");
        Map<String, String> relativeImgMap3 = redisTemplate.opsForHash().entries("t_sys_param:defectResultRealImg");
        Map<String, String> absoluteImgMap3 = redisTemplate.opsForHash().entries("t_sys_param:defectResultImg");
        List<String> list = Arrays.asList(deviceIdList.split(","));
        //巡检记录报表对象
        ReportData recordData = new ReportData();
        //1.总体情况
        TaskVO taskVO = new TaskVO();
        //站所名称
        String stationName = reportManageDao.selectStationName();
        taskVO.setStationName(stationName);
        //测点数
        Integer meteNum = reportManageDao.selectMeteNum(list,startTime,endTime);
        taskVO.setMeteNum(meteNum);
        recordData.setTaskVO(taskVO);
        //2.分项预览
        List<CheckPointType> cpTypeItems = reportManageDao.selectMeteType(list,startTime,endTime);
        recordData.setCpTypeItems(cpTypeItems);
        //3.明细-所选设备的所有测点巡检结果详情
        List<TCruiseDataResultDetail> tCDRDList =  reportManageDao.selectDetail(list,startTime,endTime);
        for (TCruiseDataResultDetail tcdr : tCDRDList){
            String relativePath = tcdr.getPicPath();
            if (!"228".equals(tcdr.getCruiseType().toString())){
                if (Objects.nonNull(relativePath) && !"null".equals(relativePath) && !"--".equals(relativePath)){
                    String aaa[] =  relativePath.split("/");
                    String type = aaa[5];
                    if ("defect".equals(type)){//缺陷
                        relativePath = relativePath.replace(relativeImgMap3.get("content"),absoluteImgMap3.get("content"));
                    }else if ("meter".equals(type)){//表計
                        relativePath = relativePath.replace(relativeImgMap1.get("content"),absoluteImgMap1.get("content"));
                    }else if ("panbie".equals(type)){//判別
                        relativePath = relativePath.replace(relativeImgMap2.get("content"),absoluteImgMap2.get("content"));
                    }
                }
            }else {
                if (Objects.nonNull(relativePath) && !"null".equals(relativePath)){
                    relativePath = relativePath.replace(relativeImgMap.get("content"),absoluteImgMap.get("content"));
                }
            }
            tcdr.setPicPath(relativePath);
        }
        recordData.setTCDRDList(tCDRDList);

//        String fileName = sdf.format(new Date())+"-"+reportType+".xlsx";
        /*String finalFileName = reportName+"_"+reportType+"_"+sdf.format(new Date())+".xlsx";
          String finalFileName = null;
        try {
            finalFileName = new String(fileName.getBytes(),"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        String fileName = "Report-"+sdf.format(new Date())+".xlsx";

//        String reportPath = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName;
//        String fileName = String.valueOf(UUID.randomUUID()).replace("-", "")+".xlsx";
//        String finalFileName = toUTF8(fileName);

        //从缓存中获取系统参数
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:reportReflect");
        String reportPath = map.get("content")+"/"+fileName;
        log.info("reportPath:"+reportPath);
        File file = new File(reportPath);

        ContentData contentData = ReportDataRepo.getData(recordData);

        ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                contentData.getElements(), file);
        /*log.info("start rename.... ");
        try {
            Thread.sleep(1000);
            String url2 = "mv "+ reportPath +" "+map.get("content")+"/"+finalFileName;
            log.info("url2: "+url2);
            Runtime.getRuntime().exec(url2);
        } catch (Exception e) {
            e.getMessage();
        }
        log.info("rename success.... ");*/
//        DownloadUtil.downloadFile(reportPath, file.getName(), response, request);
//        String url = "ls "+reportPath+" | wc -w";
//        long count = 0;
//        try {
//            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
//            BufferedReader readerForId = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
//            String lineForId = null;
//            while ((lineForId = readerForId.readLine()) != null) {
//                 count = Long.parseLong(lineForId);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
        //从缓存中获取系统参数
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:reportRelative");
        String reportPath = map.get("content");
        String filePath = reportPath+"/"+fileName;
        log.info("filePath:"+filePath);
        return filePath;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TReportInfo> reportSelect(String reportName,String startTime,String endTime){
        HashMap<String, Object> map = new HashMap<>();
        map.put("reportName", reportName);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return reportManageDao.reportSelect(map);
    }
    @Transactional(rollbackFor = Exception.class)
    public int reportDelete(String reportId) {
        //从缓存中获取系统参数
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:reportReflect");
        String reportPath = map.get("content");
        log.info("reportPath:"+reportPath);

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
        log.info("删除前文件的个数："+count);
        String fileName = reportManageDao.selectReportEnvId(reportId);
        String filePath = reportPath+"/"+fileName;
        log.info("filePath:"+filePath);
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
        log.info("删除后文件的个数："+count2);
        int res = 0;
        if (count - 1 == count2){
            res = reportManageDao.reportDelete(reportId);
        }
        return res;
    }
    @Transactional(rollbackFor = Exception.class)
    public String cruiseReportGenerate(String taskId){

        ReportData recordData = new ReportData();
        // 1.总体情况
        TaskVO taskVO = getTaskVO(taskId);
        recordData.setTaskVO(taskVO);

        // 2.分项预览
        List<CheckPointType> cpTypeItems = reportManageDao.selectMeteType2(taskId);
        recordData.setCpTypeItems(cpTypeItems);
        // 3.明细-所选设备的所有测点巡检结果详情
        List<TCruiseDataResultDetail> tCruiseDataResultDetailList =  reportManageDao.selectTaskResult(taskId);
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:prefixAbsolutePath");
        String absPath = map.get("content");
        Map<String,String> entries = redisTemplate.opsForHash().entries("t_sys_param:prefixRelativePath");
        String relPath = entries.get("content");
        // 相对路径替换绝对路径
        tCruiseDataResultDetailList.forEach(detail->{
            String resultPath = detail.getPicPath().replace(relPath, absPath);
            detail.setPicPath(resultPath);
        });
        recordData.setTCDRDList(tCruiseDataResultDetailList);

        /*String taskName = recordData.getTaskVO().getTaskName();
        String cruiseDate = sdf.format(recordData.getTaskVO().getCruiseDate());
        String reportName =  taskName+ "_" +dateTimeUtil.changeTime2(cruiseDate) + ".xlsx";//报表名称
        String finalFileName = null;
        try {
            finalFileName = new String(reportName.getBytes(StandardCharsets.UTF_8),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

//        String reportPath = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile";
//        String reportName = "Task-"+ "_" +sdf.format(new Date())+".xlsx";
        // 巡视报告名称
        String reportName =  taskId +".xlsx";

        // 从缓存中获取系统参数
        Map<String,String> redisMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String reportPath = redisMap.get("content");
        log.info("reportPath:"+reportPath);

        File temporaryFile = new File(reportPath);
        String newReportPath = null;
        if (!temporaryFile.exists() && !temporaryFile.isDirectory())
        {
            temporaryFile.mkdir();
            newReportPath = reportPath+"/"+reportName;
            log.info("不存在，创建的文件绝对路径是==="+newReportPath);
            File file = new File(reportPath);
            // 南瑞要求
//            ContentData contentData = ReportDataRepo.getData(recordData);
            // 北京要求
            ContentData contentData = ReportDataModel.getData(recordData);

            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                    contentData.getElements(), file);
        }else {
            newReportPath = reportPath+"/"+reportName;
            log.info("存在，该文件绝对路径是==="+newReportPath);
            File file = new File(newReportPath);
            // 南瑞要求
//            ContentData contentData = ReportDataRepo.getData(recordData);
            // 北京要求
            ContentData contentData = ReportDataModel.getData(recordData);

            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                    contentData.getElements(), file);
        }
        //从缓存中获取系统参数
//        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:meteModelPath");
//        String fileRelativePath = map.get("content") + "/" + reportName;
//        log.info("该文件相对路径是==="+fileRelativePath);
        return newReportPath;
    }

    public TaskVO getTaskVO(String taskId) {
//        TaskVO taskVO = reportManageDao.selectTaskNameAndTime(taskId);
        TaskVO taskVO = uPatrolResultDao.selectTaskNameAndTime(taskId);
        String stationName = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:stationName", "content"));
        taskVO.setStationName(stationName);
        // 测点数
        Integer meteNum = reportManageDao.selectMeteNumByTask(taskId);
        taskVO.setMeteNum(meteNum);
        String voltageClasses = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:stationVoltageGrade", "content")) + "kV";
        String stationType = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:stationType", "content"));
        taskVO.setVoltageClasses(voltageClasses);
        taskVO.setStationType(stationType);
        String temperature = String.valueOf(redisTemplate.opsForHash().entries("stationWeather:" + "1").get("valueUnit"));
        String airPressure = String.valueOf(redisTemplate.opsForHash().entries("stationWeather:" + "6").get("valueUnit"));
        String windSpeed = String.valueOf(redisTemplate.opsForHash().entries("stationWeather:" + "3").get("valueUnit"));
        String envInfo = "气温" + temperature + ",气压" + airPressure + ",风速" + windSpeed;
        taskVO.setEnvInfo(envInfo);
        String CruiseStatistics = "总点位" + taskVO.getTotal() + "个,已检点位" + taskVO.getAlready() + "个,未检点位" + taskVO.getWait()
                + "个,正常点位" + taskVO.getNormal() + "个,异常点位" + taskVO.getAbnormal() +  "个";
        if (Objects.nonNull(taskVO.getUnReview())){
            CruiseStatistics = CruiseStatistics +  ",待人工确认点位" + taskVO.getUnReview() + "个。";
        }
        taskVO.setCruiseStatistics(CruiseStatistics);
        taskVO.setCruiseConclusion("已审核");
        return taskVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public String downLoadCruiseReport(String taskId){
//        String flag = reportManageDao.selectReviewTaskFlag(taskId);
        String flag = uPatrolResultDao.selectReviewTaskFlag(taskId);
        if ("0".equals(flag)) {
            return "0";
        }
        String reportName = taskId + ".xlsx";
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:meteModelPath");
        String fileRelativePath = map.get("content") + "/" + reportName;
        log.info("该文件相对路径是==="+fileRelativePath);
        return fileRelativePath;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResultDetail> test(String taskId){
        return reportManageDao.selectTaskResult(taskId);
    }

    /*
     *转UTF-8
     * */
    public String toUTF8(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = Character.toString(c).getBytes(StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }
}
