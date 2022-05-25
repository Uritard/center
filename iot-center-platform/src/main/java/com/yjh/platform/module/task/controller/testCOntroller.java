package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.utils.smUtil.report.ReportDataModel;
import com.yjh.platform.common.utils.smUtil.report.ReportHelper;
import com.yjh.platform.module.task.entity.ContentData;
import com.yjh.platform.module.task.entity.ReportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * @author 丫C
 * @date 2022/4/18
 */
@RestController
public class testCOntroller {

    @Autowired
    RedisTemplate redisTemplate;

    public static void main(String[] args) {
        ReportData recordData = new ReportData();

        ContentData contentData = ReportDataModel.getData(recordData);

        String reportPath = "D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/" + "test.xlsx";
        File file = new File(reportPath);

        ReportHelper.createDocument(contentData.getRowCount(), contentData.getColumnCount(),
                contentData.getElements(), file);
    }
}
