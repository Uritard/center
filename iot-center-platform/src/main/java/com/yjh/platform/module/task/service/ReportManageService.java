package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.Report.ReportDataRepo;
import com.yjh.platform.common.utils.Report.ReportHelper;
import com.yjh.platform.module.task.dao.ReportManageDao;
import com.yjh.platform.module.task.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author YC
 * @date 2020/10/20 - 20:15
 */
@Service
public class ReportManageService {

    @Autowired
    private ReportManageDao reportManageDao;
    @Logs(title = "生成报表", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseDataResultDetail> reportGenerate(Date startTime,Date endTime,String deviceIdList,String reportName,String reportType,String reportPath) {
//        List<String> list = Arrays.asList(deviceIdList.split(","));

        //巡检记录报表对象
        ReportData recordData = new ReportData();
        //1.总体情况
        TaskVO taskVO = new TaskVO();
        taskVO.setCruiseDate(new Date());
        recordData.setTaskVO(taskVO);
        //2.分项预览
        List<CheckPointType> cpTypeItems = new ArrayList<>();
        recordData.setCpTypeItems(cpTypeItems);
        //3.环境监测
        List<EnvironmentResult> evnRecordList = new ArrayList<>();
        recordData.setEvnRecordList(evnRecordList);
        //4.关联设备
        List<RelationDevice> rcpRecordList = new ArrayList<>();
        recordData.setRcpRecordList(rcpRecordList);
        //5.明细，一次巡检任务的所有测点巡检结果详情
        List<TCruiseDataResultDetail> tCDRDList =  reportManageDao.reportGenerate(startTime,endTime);
        recordData.setTCDRDList(tCDRDList);
        System.out.println("<--------recordData------->:"+recordData);

        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String nowTime = f.format(date);

        String fileName = reportName+"-"+nowTime+"-"+reportType;//报表名称
        String reportPath2 = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName+".xlsx";
        File file = new File(reportPath2);

        ContentData contentData = ReportDataRepo.getData(recordData);
        System.out.println("<--------contentData------->:"+contentData);

        ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                contentData.getElements(), file);

//        DownloadUtil.downloadFile(reportPath, file.getName(), response, request);

        return tCDRDList;
    }
    @Logs(title = "下载报表", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public String reportDownload(String reportName,String reportType,String startTime,String reportPath) {
        String time2 =  DateTimeUtil.changeTime2(startTime);
        String fileName = reportName+"-"+time2+"-"+reportType+".xlsx";
        String reportPatha = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/";
        String filePath = reportPatha+fileName;
//        String filePath = reportPath+fileName;
        return filePath;
    }
    @Logs(title = "查询报表生成记录", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<ReportForms> reportSelect(String queryStr,String startTime,String endTime,String reportPath) {
        HashMap<String, Object> map = new HashMap<>();
        List<ReportForms> fileNameList = new ArrayList<>();//文件名列表
        List<String> folderNameList = new ArrayList<>();//文件夹名列表

        String startTime1 = DateTimeUtil.changeTime2(startTime);//yyyyMMddHHmmss
        String endTime1 = DateTimeUtil.changeTime2(endTime);//yyyyMMddHHmmss

        String folderPath = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/";
        File f = new File(folderPath);

        if (!f.exists()) { //路径不存在
            map.put("retType", "1");
        }else{
            boolean flag = f.isDirectory();
            if(flag==false){ //路径为文件
                map.put("retType", "2");
                map.put("fileName", f.getName());
            }else{ //路径为文件夹
                map.put("retType", "3");
                File fa[] = f.listFiles();
                queryStr = queryStr==null ? "" : queryStr;//若queryStr传入为null,则替换为空（indexOf匹配值不能为null）
                for (int i = 0; i < fa.length; i++) {
                    File fs = fa[i];
                    if(fs.getName().indexOf(queryStr) != -1){
                        if (fs.isDirectory()) {
                            folderNameList.add(fs.getName());
                        } else {
                            ReportForms reportForms = new ReportForms();
                            String fileName = fs.getName();
                            String str[] = fileName.substring(0,fileName.length()-5).split("-");//根据-分割文件名
                            String rName = str[0];
                            String rDate = str[1];
                            String rType = str[2];
                            //没有时间过滤
                            if (startTime1 == "" && endTime1==""){
                                reportForms.setReportName(rName);
                                String time2 = DateTimeUtil.changeTime1(rDate);
                                reportForms.setStartTime(time2);
                                reportForms.setReportType(rType);
                                fileNameList.add(reportForms);
                            }else {
                                //时间过滤
                                long tTime1 = Long.parseLong(rDate);
                                long tTime2 = Long.parseLong(startTime1);
                                long tTime3 = Long.parseLong(endTime1);
                                if (tTime1 >= tTime2 && tTime1 <= tTime3){
                                    reportForms.setReportName(rName);
                                    String time2 = DateTimeUtil.changeTime1(rDate);
                                    reportForms.setStartTime(time2);
                                    reportForms.setReportType(rType);
                                    fileNameList.add(reportForms);
                                }
                            }
                        }
                    }
                }
                map.put("fileNameList", fileNameList);
                map.put("folderNameList", folderNameList);
            }
        }
        System.out.println("<----------map--------->的值为："+map);
        return fileNameList;

    }
    @Logs(title = "删除报表", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public boolean reportDelete(String reportName,String reportType, String startTime,String reportPath) {
        String time2 =  DateTimeUtil.changeTime2(startTime);
        String fileName = reportName+"-"+time2+"-"+reportType+".xlsx";
        String filePathAndName = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName;
//        String filePathAndName = reportPath+fileName;
        return delete(filePathAndName);
    }
    public static boolean delete(String filePathAndName)
    {
        boolean result = false;
        try {
            File myDelFile = new File(filePathAndName);
            result = myDelFile.delete();
        } catch (Exception e) {
            System.out.println("删除文件操作出错");
            e.printStackTrace();
        }
        return result;
    }
    @Logs(title = "批量删除报表", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public boolean reportBatchDelete(String reportName,String reportType, String startTime,String reportPath) {
        String time2 =  DateTimeUtil.changeTime2(startTime);
        String fileName = reportName+"-"+time2+"-"+reportType+".xlsx";
        String filePathAndName = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName;
//        String filePathAndName = reportPath+fileName;
        return delete(filePathAndName);
    }
}
