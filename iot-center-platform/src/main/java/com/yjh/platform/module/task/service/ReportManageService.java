package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.Report.ReportDataRepo;
import com.yjh.platform.common.utils.Report.ReportHelper;
import com.yjh.platform.module.task.dao.ReportManageDao;
import com.yjh.platform.module.task.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private DateTimeUtil dateTimeUtil;
    @Logs(title = "生成报表", code = "reportManage",content = "根据web传递的参数生成不同的报表")
    @Transactional(rollbackFor = Exception.class)
    public int reportGenerate(Date startTime,Date endTime,String deviceIdList,String reportName,String reportType,String reportPath) {
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
//        String fileName = reportName+"-"+nowTime+"-"+reportType+".xlsx";
//        String reportPath2 = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName;
        String reportPath2 = reportPath+"/"+fileName;
        log.info("reportPath2:"+reportPath2);
        File file = new File(reportPath2);

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
    public String reportDownload(String reportId,String reportPath) {
//        String time2 =  DateTimeUtil.changeTime2(startTime);
//        String fileName = reportName+"-"+time2+"-"+reportType+".xlsx";
//        String reportPathA = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/";
        String fileName = reportManageDao.selectReportEnvId(reportId);
        String filePath = reportPath+"/"+fileName;
//        String filePath = reportPathA+fileName;

        log.info("filePath:"+filePath);
        return filePath;
    }
//    @Logs(title = "查询报表生成记录", code = "reportManage",content = "通过web传递的参数查询报表记录")
//    @Transactional(rollbackFor = Exception.class)
//    public List<TReportInfo> reportSelect(String queryStr,String startTime,String endTime,String reportPath) {
//        HashMap<String, Object> map = new HashMap<>();
//        List<TReportInfo> fileNameList = new ArrayList<>();//文件名列表
//        List<String> folderNameList = new ArrayList<>();//文件夹名列表
//
//        String startTimeTemp = DateTimeUtil.changeTime2(startTime);//yyyyMMddHHmmss
//        String endTimeTemp = DateTimeUtil.changeTime2(endTime);//yyyyMMddHHmmss
//
//        String folderPath = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/";
////        File f = new File(reportPath+"/");
//        File f = new File(reportPath);
//
//
//        if (!f.exists()) { //路径不存在
//            map.put("retType", "1");
//        }else{
//            boolean flag = f.isDirectory();
//            if(flag==false){ //路径为文件
//                map.put("retType", "2");
//                map.put("fileName", f.getName());
//            }else{ //路径为文件夹
//                map.put("retType", "3");
//                File fa[] = f.listFiles();
//                queryStr = queryStr==null ? "" : queryStr;//若queryStr传入为null,则替换为空（indexOf匹配值不能为null）
//                for (int i = 0; i < fa.length; i++) {
//                    File fs = fa[i];
//                    if(fs.getName().indexOf(queryStr) != -1){
//                        if (fs.isDirectory()) {
//                            folderNameList.add(fs.getName());
//                        } else {
//                            TReportInfo reportForms = new TReportInfo();
//                            String fileName = fs.getName();
//                            String str[] = fileName.substring(0,fileName.length()-5).split("-");//根据-分割文件名
//                            String rName = str[0];
//                            String rDate = str[1];
//                            String rType = str[2];
//                            //没有时间过滤
//                            if (startTimeTemp == "" && endTimeTemp==""){
//                                reportForms.setReportName(rName);
//                                String time2 = DateTimeUtil.changeTime1(rDate);
////                                reportForms.setGenerateDate(time2);
//                                reportForms.setReportType(rType);
//                                fileNameList.add(reportForms);
//                            }else {
//                                //时间过滤
//                                long tTime1 = Long.parseLong(rDate);
//                                long tTime2 = Long.parseLong(startTimeTemp);
//                                long tTime3 = Long.parseLong(endTimeTemp);
//                                if (tTime1 >= tTime2 && tTime1 <= tTime3){
//                                    reportForms.setReportName(rName);
//                                    String time2 = DateTimeUtil.changeTime1(rDate);
////                                    reportForms.setGenerateDate(time2);
//                                    reportForms.setReportType(rType);
//                                    fileNameList.add(reportForms);
//                                }
//                            }
//                        }
//                    }
//                }
//                map.put("fileNameList", fileNameList);
//                map.put("folderNameList", folderNameList);
//            }
//        }
//        log.info("<----------map--------->的值为："+map);
//        log.info("fileNameList是："+fileNameList);
//        HashMap<String, Object> selectMap = new HashMap<>();
//        selectMap.put("reportName",queryStr);
//        selectMap.put("startTime",startTime);
//        selectMap.put("endTime",endTime);
//        List<TReportInfo> fileList = reportManageDao.reportSelect(selectMap);
//        return fileList;
//    }
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
    public int reportDelete(String reportId,String reportPath) {
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
//        String timeTemp =  DateTimeUtil.changeTime2(startTime);
//        String fileName = reportName+"-"+timeTemp+"-"+reportType+".xlsx";
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
    @Logs(title = "下载报告", code = "reportManage",content = "根据巡检任务生成报告并下载")
    @Transactional(rollbackFor = Exception.class)
    public String reportByTask(String taskId,String temporaryPath){
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
        //3.环境监测-暂无
//        List<EnvironmentResult> evnRecordList = new ArrayList<>();
//        recordData.setEvnRecordList(evnRecordList);
//        //4.关联设备-暂无
//        List<RelationDevice> rcpRecordList = new ArrayList<>();
//        recordData.setRcpRecordList(rcpRecordList);
        //5.明细-所选设备的所有测点巡检结果详情

        List<TCruiseDataResultDetail> tCDRDList =  reportManageDao.selectTaskResult(taskId);
        recordData.setTCDRDList(tCDRDList);
        String taskName = recordData.getTaskVO().getTaskName();
        String cruiseDate = dateTimeUtil.format(recordData.getTaskVO().getCruiseDate());
        String reportName =  taskName+ "_" +dateTimeUtil.changeTime2(cruiseDate) + ".xlsx";//报表名称
//        String filePath = "D:/MyDocuments/workspace_idea/IotCenterDev/temporaryFiles/";
        File temporaryFile = new File(temporaryPath);
        String reportPath2 = null;
        if (!temporaryFile.exists() && !temporaryFile.isDirectory())
        {
            System.out.println("不存在");
            temporaryFile.mkdir();
            reportPath2 = temporaryPath+"/"+reportName;
            File file = new File(reportPath2);
            ContentData contentData = ReportDataRepo.getData(recordData);
            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                    contentData.getElements(), file);
        }else {
            System.out.println("存在");
            reportPath2 = temporaryPath+"/"+reportName;
            File file = new File(reportPath2);
            ContentData contentData = ReportDataRepo.getData(recordData);
            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                    contentData.getElements(), file);
        }
        return reportPath2;
    }
}
