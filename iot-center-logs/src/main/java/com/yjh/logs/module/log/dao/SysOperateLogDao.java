package com.yjh.logs.module.log.dao;

import com.yjh.logs.module.log.entity.SysOperateLog;
import com.yjh.logs.module.log.entity.SysOperateLogDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tt
 * @since 2021-01-14
 */
@Repository
public interface SysOperateLogDao {

    int insert(SysOperateLog sysOperateLog);
    int deleteByPrimaryId(@Param(value = "logId") Long logId);
    int update(SysOperateLog sysOperateLog);
    SysOperateLog selectByPrimaryId(@Param(value = "logId") Long logId);
    List<SysOperateLog> select(@Param(value = "logId") Long logId,
                               @Param(value = "traceId") String traceId,
                               @Param(value = "logType") String logType,
                               @Param(value = "ip") String ip,
                               @Param(value = "title") String title,
                               @Param(value = "state") Integer state,
                               @Param(value = "content") String content,
                               @Param(value = "userId") Long userId,
                               @Param(value = "userName") String userName,
                               @Param(value = "requestOrigin") String requestOrigin,
                               @Param(value = "requestPath") String requestPath,
                               @Param(value = "requestMethod") Integer requestMethod,
                               @Param(value = "createTime") Date createTime);
    List<SysOperateLogDetail> selectByPage(@Param(value = "userName") String userName,
                                           @Param(value = "title") String title,
                                           @Param(value = "startTime") Date startTime,
                                           @Param(value = "endTime") Date endTime);

    int batchInsert(List<SysOperateLog> list);
}
