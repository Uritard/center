package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.SysLogs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-12
 */
@Repository
public interface SysLogsDao {

    SysLogs selectByPrimaryId(@Param(value = "logId") String logId);
}
