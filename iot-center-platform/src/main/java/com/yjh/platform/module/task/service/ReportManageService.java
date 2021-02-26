package com.yjh.platform.module.task.service;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.Report.ReportDataRepo;
import com.yjh.platform.common.utils.Report.ReportHelper;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
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
    private TStdDevicemeteDao tStdDevicemeteDao;
    @Autowired
    private TCruiseDataResultDao tCruiseDataResultDao;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    private DateTimeUtil dateTimeUtil;
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
        Integer meteNum = reportManageDao.selectMeteNum(list,startTime,endTime);
        taskVO.setMeteNum(meteNum);
        recordData.setTaskVO(taskVO);
        //2.分项预览
        List<CheckPointType> cpTypeItems = reportManageDao.selectMeteType(list,startTime,endTime);
        recordData.setCpTypeItems(cpTypeItems);
        //3.明细-所选设备的所有测点巡检结果详情
        List<TCruiseDataResultDetail> tCDRDList =  reportManageDao.selectDetail(list,startTime,endTime);
        recordData.setTCDRDList(tCDRDList);

//        String fileName = sdf.format(new Date())+"-"+reportType+".xlsx";
        String fileName = reportName+"_"+reportType+"_"+sdf.format(new Date())+".xlsx";
        //生成随机的文件名称
//        String fileName = String.valueOf(UUID.randomUUID()).replace("-", "")+".xlsx";

//        String reportPathLocal = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName;
        /*String finalFileName = null;
        try {
            finalFileName = new String(fileName.getBytes(StandardCharsets.UTF_8),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

        String finalFileName = toUTF8(fileName);

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
        //1.总体情况
        TaskVO taskVO = new TaskVO();
        //测点数
        Integer meteNum = reportManageDao.selectMeteNumByTask(taskId);
        taskVO.setMeteNum(meteNum);
        //站所名称、任务名称、巡检时间
        TaskVO someThing = reportManageDao.selectTaskNameAndTime(taskId);
        taskVO.setStationName(someThing.getStationName());
        taskVO.setTaskName(someThing.getTaskName());
        taskVO.setCruiseDate(someThing.getCruiseDate());
        recordData.setTaskVO(taskVO);
        //2.分项预览
        List<CheckPointType> cpTypeItems = reportManageDao.selectMeteType2(taskId);
        recordData.setCpTypeItems(cpTypeItems);
        //3.明细-所选设备的所有测点巡检结果详情
        List<TCruiseDataResultDetail> tCDRDList =  reportManageDao.selectTaskResult(taskId);
        recordData.setTCDRDList(tCDRDList);
        /*String taskName = recordData.getTaskVO().getTaskName();
        String cruiseDate = sdf.format(recordData.getTaskVO().getCruiseDate());
        String reportName =  taskName+ "_" +dateTimeUtil.changeTime2(cruiseDate) + ".xlsx";//报表名称*/
        String reportName =  taskId + ".xlsx";//报表名称

//        String filePathLocal = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile";

        String finalFileName = null;
        try {
            finalFileName = new String(reportName.getBytes(StandardCharsets.UTF_8),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //从缓存中获取系统参数
        Map<String,String> redisMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String reportPath = redisMap.get("content");
        log.info("reportPath:"+reportPath);

        File temporaryFile = new File(reportPath);
        String newReportPath = null;
        if (!temporaryFile.exists() && !temporaryFile.isDirectory())
        {
            temporaryFile.mkdir();
            newReportPath = reportPath+"/"+finalFileName;
            log.info("不存在，创建的文件绝对路径是==="+newReportPath);
            File file = new File(reportPath);
            ContentData contentData = ReportDataRepo.getData(recordData);
            ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                    contentData.getElements(), file);
        }else {
            newReportPath = reportPath+"/"+finalFileName;
            log.info("存在，该文件绝对路径是==="+newReportPath);
            File file = new File(newReportPath);
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

    @Transactional(rollbackFor = Exception.class)
    public String downLoadCruiseReport(String taskId){
        String reportName = taskId + ".xlsx";//报表名称
        //从缓存中获取系统参数
        Map<String,String> map = redisTemplate.opsForHash().entries("t_sys_param:meteModelPath");
        String fileRelativePath = map.get("content") + "/" + reportName;
        log.info("该文件相对路径是==="+fileRelativePath);
        return fileRelativePath;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResultDetail> test(String taskId){
        return reportManageDao.selectTaskResult(taskId);
    }
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
