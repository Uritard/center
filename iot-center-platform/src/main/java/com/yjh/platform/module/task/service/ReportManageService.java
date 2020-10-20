package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.dao.ReportManageDao;
import com.yjh.platform.module.task.entity.TestReportMange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author YC
 * @date 2020/10/20 - 20:15
 */
@Service
public class ReportManageService {

    @Autowired
    private ReportManageDao reportManageDao;
    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TestReportMange> selectAll() {
        List<TestReportMange> testReportMangeList = reportManageDao.selectAll();
        return testReportMangeList;
    }
}
