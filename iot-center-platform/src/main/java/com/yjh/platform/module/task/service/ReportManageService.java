package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.Report.ReportDataRepo;
import com.yjh.platform.common.utils.Report.ReportHelper;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.task.dao.ReportManageDao;
import com.yjh.platform.module.task.dao.TCruiseDataResultDao;
import com.yjh.platform.module.task.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
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
    private TStdDevicemeteDao tStdDevicemeteDao;
    @Autowired
    private TCruiseDataResultDao tCruiseDataResultDao;

    private DateTimeUtil dateTimeUtil;
    @Logs(title = "生成报表", code = "reportManage",content = "根据web传递的参数生成不同的报表")
    @Transactional(rollbackFor = Exception.class)
    public int reportGenerate(Date startTime,Date endTime,String deviceIdList,String reportName,String reportType) {
        List<String> list = Arrays.asList(deviceIdList.split(","));
        //巡检记录报表对象
        ReportData recordData = new ReportData();
        //1.总体情况
        TaskVO taskVO = new TaskVO();
        //站所名称
        String stationName = reportManageDao.selectStationName();
        taskVO.setStationName(stationName);
        //测点数
        Integer meteNum = reportManageDao.selectMeteNum(list);
        taskVO.setMeteNum(meteNum);
        //未处理数
        Integer abnormalNum = reportManageDao.selectAbnormalNum(list);
        taskVO.setAbnormalNum(abnormalNum);
        //关联测点数
        taskVO.setMeteRelationNum(0);
        //任务名称
//        taskVO.setTaskName("全站巡视");
        recordData.setTaskVO(taskVO);
        //2.分项预览
        List<CheckPointType> cpTypeItems = reportManageDao.selectMeteType(list);
        recordData.setCpTypeItems(cpTypeItems);
        //3.环境监测-暂无
//        List<EnvironmentResult> evnRecordList = new ArrayList<>();
//        recordData.setEvnRecordList(evnRecordList);
//        //4.关联设备-暂无
//        List<RelationDevice> rcpRecordList = new ArrayList<>();
//        recordData.setRcpRecordList(rcpRecordList);
        //5.明细-所选设备的所有测点巡检结果详情

        List<TCruiseDataResultDetail> tCDRDList =  reportManageDao.selectDetail(list,startTime,endTime);
        recordData.setTCDRDList(tCDRDList);

        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String nowTime = f.format(date);

//        String fileName = nowTime+"-"+reportType+".xlsx";
        //生成随机的文件名称
        String fileName = String.valueOf(UUID.randomUUID()).replace("-", "")+".xlsx";
//        String fileName = reportName+"_"+reportType+"_"+nowTime+".xlsx";
//        String reportPath2 = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName;
        String finalFileName = null;
        try {
            finalFileName = new String(fileName.getBytes("UTF-8"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //从缓存中获取系统参数
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:reportReflect");
        String reportPath = map.get("content")+"/"+finalFileName;
        log.info("reportPath:"+reportPath);
        File file = new File(reportPath);

        ContentData contentData = ReportDataRepo.getData(recordData);

        ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                contentData.getElements(), file);
//        log.info("start rename.... ");
//        try {
//            Thread.sleep(3000);
//            String url2 = "mv "+ reportPath+"/"+fileName +" "+reportPath+"/"+reportName+"-"+nowTime+"-"+reportType+".xlsx";
//            log.info("url2: "+url2);
//            Runtime.getRuntime().exec(url2);
//        } catch (Exception e) {
//            e.getMessage();
//        }
//        log.info("rename success.... ");
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
                .setGenerateDate(date)
                .setReportEnvId(fileName);
        int res = reportManageDao.insertReport(reportInfo);
        return res;
    }
    @Logs(title = "下载报表", code = "reportManage",content = "通过web传递的参数下载报表")
    @Transactional(rollbackFor = Exception.class)
    public String reportDownload(String reportId) {
//        String reportPathA = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/";
//        String filePath = reportPathA+fileName;

        String fileName = reportManageDao.selectReportEnvId(reportId);
        //从缓存中获取系统参数
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:reportRelative");
        String reportPath = map.get("content");
        log.info("reportPath:"+reportPath);
        String filePath = reportPath+"/"+fileName;

        log.info("filePath:"+filePath);
        return filePath;
    }
    @Logs(title = "查询报表生成记录", code = "reportManage",content = "通过web传递的参数查询报表记录")
    @Transactional(rollbackFor = Exception.class)
    public List<TReportInfo> reportSelect(String reportName,String startTime,String endTime){
        HashMap<String, Object> map = new HashMap<>();
        map.put("reportName", reportName);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return reportManageDao.reportSelect(map);
    }
    @Logs(title = "删除报表", code = "reportManage",content = "通过web传递的参数删除报表记录")
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
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String lineForId = null;
            while ((lineForId = readerForId.readLine()) != null) {
                count = Long.parseLong(lineForId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("删除前文件的个数："+count);
        String fileName = reportManageDao.selectReportEnvId(reportId);
//        String filePath = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName;
        String filePath = reportPath+"/"+fileName;
        log.info("filePath:"+filePath);
        String url2 = "rm -f "+filePath;
        try {
            Process processForId = Runtime.getRuntime().exec(url2);
            processForId.waitFor();
        } catch (Exception e) {
            e.getMessage();
        }
        String url3 = "ls "+reportPath+" | wc -w";
        long count2 = 0;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", url3});
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
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
//    @Logs(title = "批量删除报表", code = "reportManage",content = "通过web传递的参数批量删除报表记录")
//    @Transactional(rollbackFor = Exception.class)
//    public boolean reportBatchDelete(String reportPath,List<TReportInfo> fileNameList) {
//        for (TReportInfo res:fileNameList) {
//            String reportName = res.getReportName();
//            String reportType = res.getReportType();
//            Date startTime = res.getGenerateDate();
//            String time2 =  DateTimeUtil.changeTime2(startTime);
//            String fileName = reportName+"-"+time2+"-"+reportType+".xlsx";
//            String filePathAndName = reportPath+"/"+fileName;
//           String filePathAndName = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName;
//           String url = "rm -f "+filePathAndName;
//            try {
//                Process processForId = Runtime.getRuntime().exec(url);
//                processForId.waitFor();
//            } catch (Exception e) {
//                e.getMessage();
//            }
//        }
//        return  true;
//    }
    @Logs(title = "生成巡视报告", code = "reportManage",content = "根据巡检任务生成报告")
    @Transactional(rollbackFor = Exception.class)
    public String cruiseReportGenerate(String taskId){
        ReportData recordData = new ReportData();
        //1.总体情况
        TaskVO taskVO = new TaskVO();
        //站所名称
        String stationName = reportManageDao.selectStationName();
        taskVO.setStationName(stationName);
        //测点数
        Integer meteNum = reportManageDao.selectMeteNumByTask(taskId);
        taskVO.setMeteNum(meteNum);
        //未处理数
        Integer abnormalNum = reportManageDao.selectAbnormalNumByTask(taskId);
        taskVO.setAbnormalNum(abnormalNum);
        //关联测点数
        taskVO.setMeteRelationNum(0);
        //任务名称、巡检时间
        TaskVO taskNameAndTime = reportManageDao.selectTaskNameAndTime(taskId);
        taskVO.setTaskName(taskNameAndTime.getTaskName());
        taskVO.setCruiseDate(taskNameAndTime.getCruiseDate());
        recordData.setTaskVO(taskVO);
        //2.分项预览
        List<CheckPointType> cpTypeItems = reportManageDao.selectMeteType2(taskId);
        recordData.setCpTypeItems(cpTypeItems);
        //5.明细-所选设备的所有测点巡检结果详情

        List<TCruiseDataResultDetail> tCDRDList =  reportManageDao.selectTaskResult(taskId);
        recordData.setTCDRDList(tCDRDList);
//        String taskName = recordData.getTaskVO().getTaskName();
//        String cruiseDate = dateTimeUtil.format(recordData.getTaskVO().getCruiseDate());
//        String reportName =  taskName+ "_" +dateTimeUtil.changeTime2(cruiseDate) + ".xlsx";//报表名称
        String reportName =  taskId + ".xlsx";//报表名称

        String filePath = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile";

        String finalFileName = null;
        try {
            finalFileName = new String(reportName.getBytes("UTF-8"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //从缓存中获取系统参数
//        Map<String,String> redisMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
//        String reportPath = redisMap.get("content");
//        log.info("reportPath:"+reportPath);

        File temporaryFile = new File(filePath);
        String reportPath2 = null;
        if (!temporaryFile.exists() && !temporaryFile.isDirectory())
        {
            temporaryFile.mkdir();
            reportPath2 = filePath+"/"+finalFileName;
            log.info("不存在，创建的文件绝对路径是==="+reportPath2);
            File file = new File(filePath);
            ContentData contentData = ReportDataRepo.getData(recordData);
            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                    contentData.getElements(), file);
        }else {
            reportPath2 = filePath+"/"+finalFileName;
            log.info("存在，该文件绝对路径是==="+reportPath2);
            File file = new File(reportPath2);
            ContentData contentData = ReportDataRepo.getData(recordData);
            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                    contentData.getElements(), file);
        }
        //从缓存中获取系统参数
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:meteModelPath");
        String fileRelativePath = map.get("content") + "/" + finalFileName;
        log.info("该文件相对路径是==="+fileRelativePath);
        return fileRelativePath;
    }

    @Logs(title = "下载巡视报告", code = "test",content = "啥也不是")
    @Transactional(rollbackFor = Exception.class)
    public String downLoadCruiseReport(String taskId){
        String reportName = taskId + ".xlsx";//报表名称
        //从缓存中获取系统参数
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:meteModelPath");
        String fileRelativePath = map.get("content") + "/" + reportName;
        log.info("该文件相对路径是==="+fileRelativePath);
        return fileRelativePath;
    }
    @Logs(title = "啥也不是", code = "test",content = "啥也不是")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResultDetail> test(String taskId){
        List<TCruiseDataResultDetail> tCDRDList =  reportManageDao.selectTaskResult(taskId);
        return tCDRDList;
    }

    @Logs(title = "巡视结果分析报表生成下载",code = "reportManage",content = "分析报表下载")
    @Transactional(rollbackFor = Exception.class)
    public String cruiseResultAnalyseReporter(Long deviceMeteId){

        ResultAnalyseReport resultAnalyseReport=new ResultAnalyseReport();
        DeviceBaseReport deviceBaseReport=tStdDeviceDao.selectDeviceBase(deviceMeteId);
        DeviceMeteBaseReport deviceMeteBaseReport=tStdDeviceDao.selectDeviceMeteBase(deviceMeteId);
        List<CruiseResultDetailReport> cruiseResultDetailReports=new ArrayList<>();
        List<CruiseResultAnalInfo> cruiseResultAnalInfos=tCruiseDataResultDao.selectCruiseDataResultByList(-1,-1,deviceMeteId,null,null);
        for(CruiseResultAnalInfo cruiseTem:cruiseResultAnalInfos){
            CruiseResultDetailReport cruiseResultDetailReport=new CruiseResultDetailReport();
            cruiseResultDetailReport.setCruiseName(cruiseTem.getCruiseName());
            cruiseResultDetailReport.setCruiseTypeName(cruiseTem.getCruiseTypeName());
            cruiseResultDetailReport.setResultNum(cruiseTem.getResultNum());
            cruiseResultDetailReport.setEndTime(cruiseTem.getEndTime());
            cruiseResultDetailReport.setIdentifyResultName(cruiseTem.getIdentifyResultName());
            cruiseResultDetailReport.setCTypeName(cruiseTem.getCTypeName());
            cruiseResultDetailReport.setPicPath(cruiseTem.getPicPath());
            cruiseResultDetailReports.add(cruiseResultDetailReport);
        }

        resultAnalyseReport.setDeviceBaseR(deviceBaseReport);
        resultAnalyseReport.setDeivceMeteBaseR(deviceMeteBaseReport);
        resultAnalyseReport.setCRDR(cruiseResultDetailReports);


        String deviceMeteName=deviceMeteBaseReport.getDeviceMeteName();
        String cruiseDate = dateTimeUtil.format(cruiseResultDetailReports.get(0).getEndTime());
        String reportName =  deviceMeteName+ "_" +dateTimeUtil.changeTime2(cruiseDate) + ".xlsx";//报表名称


        String finalFileName = null;
        try {
            finalFileName = new String(reportName.getBytes("UTF-8"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //从缓存中获取系统参数
        Map<String,String> redisMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String reportPath = redisMap.get("content");
        log.info("reportPath:"+reportPath);

        File temporaryFile = new File(reportPath);
        String reportPath2 = null;
        if (!temporaryFile.exists() && !temporaryFile.isDirectory())
        {
            temporaryFile.mkdir();
            reportPath2 = reportPath+"/"+finalFileName;
            log.info("不存在，创建的文件绝对路径是==="+reportPath2);
            File file = new File(reportPath2);
//            ContentData contentData = ReportDataRepo.getData(resultAnalyseReport);
//            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
//                    contentData.getElements(), file);
        }else {
            reportPath2 = reportPath+"/"+finalFileName;
            log.info("存在，该文件绝对路径是==="+reportPath2);
            File file = new File(reportPath2);
//            ContentData contentData = ReportDataRepo.getData(resultAnalyseReport);
//            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
//                    contentData.getElements(), file);
        }
        //从缓存中获取系统参数
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:meteModelPath");
        String fileRelativePath = map.get("content") + "/" + finalFileName;
        log.info("该文件相对路径是==="+fileRelativePath);
        return fileRelativePath;


    }
}
