package com.yjh.logs.module.log.dao;

import com.yjh.logs.module.log.entity.LongAnalyseDetail;
import com.yjh.logs.module.log.entity.SysLog;
import com.yjh.logs.module.log.entity.SysLogDetail;
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
public interface SysLogDao {

    int insert(SysLog sysLog);
    int deleteByPrimaryId(@Param(value = "logId") Long logId);
    int update(SysLog sysLog);
    SysLog selectByPrimaryId(@Param(value = "logId") Long logId);
    List<SysLog> select(@Param(value = "logId") Long logId,
                               @Param(value = "logType") String logType,
                               @Param(value = "ip") String ip,
                               @Param(value = "title") String title,
                               @Param(value = "state") Integer state,
                               @Param(value = "content") String content,
                               @Param(value = "userId") Long userId,
                               @Param(value = "userName") String userName,
                               @Param(value = "requestOrigin") String requestOrigin,
                               @Param(value = "requestPath") String requestPath,
                               @Param(value = "requestMethod") String requestMethod,
                               @Param(value = "createTime") Date createTime);
    List<SysLogDetail> selectByPage(@Param(value = "userName") String userName,
                                    @Param(value = "title") String title,
                                    @Param(value = "startTime") Date startTime,
                                    @Param(value = "endTime") Date endTime,
                                    @Param(value = "logType") String logType);

    int batchInsert(List<SysLog> list);
    List<LongAnalyseDetail> logAnalyze();
}
