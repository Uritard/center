package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TestReportMange;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @date 2020/10/20 - 20:21
 */
@Repository
public interface ReportManageDao {
    List<Map<String, Object>> selectHHH();
    List<Map<String, Object>> reportGenerate();
//    List<TestReportMange> reportGenerate(@Param(value = "startTime") Date startTime,
//                                    @Param(value = "endTime") Date endTime,
//                                    @Param(value = "list") List<String> list,
//                                    @Param(value = "reportName") String reportName);

}
