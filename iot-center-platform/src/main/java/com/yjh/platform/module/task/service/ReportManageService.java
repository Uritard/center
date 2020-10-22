package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.EasyPoiUtil;
import com.yjh.platform.module.task.dao.ReportManageDao;
import com.yjh.platform.module.task.entity.ReportForms;
import com.yjh.platform.module.task.entity.TestReportMange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<Map<String, Object>> reportGenerate(Date startTime,Date endTime,String deviceIdList,String reportName) {
//        List<String> list = Arrays.asList(deviceIdList.split(","));
//        List<TestReportMange> resultList = reportManageDao.reportGenerate(startTime,endTime,list,reportName);

//        List<Map<String, Object>> reportResultList =  reportManageDao.selectHHH();//获取报表所需信息
        List<Map<String, Object>> reportResultList =  reportManageDao.reportGenerate();//获取报表所需信息

//        for (int i = 0;i <= reportResultList.size()-1;i++){
//            byte[] imageFromNetByUrl = EasyPoiUtil.getImageFromNetByUrl((String) reportResultList.get(i).get("image_path"));
//            reportResultList.get(i).put("image_path",imageFromNetByUrl);
//        }
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String nowTime = f.format(date);

        EasyPoiUtil.exportExcel(reportResultList, "巡检记录报表",reportName+"-巡检报告",  true);

        return reportResultList;
    }
    @Logs(title = "查询报表生成记录", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<ReportForms> reportSelect(String reportName, Date startTime, Date endTime) {
        List<ReportForms> reportFormsList = new ArrayList<>();
        return reportFormsList;
    }
}
