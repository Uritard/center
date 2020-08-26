package com.yjh.logs.module.log.dao;

import java.util.HashMap;
import java.util.List;

import com.yjh.logs.module.log.entity.SysLogs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-12
 */
@Repository
public interface SysLogsDao {

    int insert(SysLogs sysLogs);
    int deleteByPrimaryId(@Param(value = "logId") String logId);
    int update(SysLogs sysLogs);
    SysLogs selectByPrimaryId(@Param(value = "logId") String logId);
    List<SysLogs> select(HashMap<String,Object> map);
    List<SysLogs> selectByPage(SysLogs sysLogs);

    int batchInsert(List<SysLogs> list);
}