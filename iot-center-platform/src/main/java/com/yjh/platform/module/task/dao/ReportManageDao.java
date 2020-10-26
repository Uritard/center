package com.yjh.platform.module.task.dao;

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
    List<Map<String, Object>> reportGenerate(@Param(value = "startTime")Date startTime,
                                             @Param(value = "endTime")Date endTime);
}
