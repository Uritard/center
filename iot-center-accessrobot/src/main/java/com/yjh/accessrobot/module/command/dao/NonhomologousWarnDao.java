package com.yjh.accessrobot.module.command.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author sunjinyan
 * @since 2022-04-08
 */
@Repository
public interface NonhomologousWarnDao {

    int insertNonhomologousWarnInfo(Map<String,Object> warn);

    int insertWarnInspections(List<Map<String,Object>> insResults);

    int checkWarnExist(Map<String,Object> warnParam);

    List<Map<String,Object>> getNonhomologousInspections(Map<String,String> cruiseResultMap);

    List<Map<String,Object>> selectWarnResults(Map<String,Object> intervalResultsParam);

    Long getInstanceIdByDeviceId(@Param(value = "deviceId") String deviceId,
                                 @Param(value = "taskId") String taskId);
}
