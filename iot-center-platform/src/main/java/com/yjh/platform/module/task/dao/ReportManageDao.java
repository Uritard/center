package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TestReportMange;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author YC
 * @date 2020/10/20 - 20:21
 */
@Repository
public interface ReportManageDao {
    List<TestReportMange> selectAll();
}
