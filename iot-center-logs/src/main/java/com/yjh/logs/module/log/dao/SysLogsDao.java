package com.yjh.logs.module.log.dao;

import java.util.List;
import java.util.Date;
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
    List<SysLogs> select(@Param(value = "logId") String logId,
                                @Param(value = "logType") String logType,
                                @Param(value = "ip") String ip,
                                @Param(value = "title") String title,
                                @Param(value = "state") Integer state,
                                @Param(value = "content") String content,
                                @Param(value = "userId") Long userId,
                                @Param(value = "userName") String userName,
                                @Param(value = "createTime") Date createTime);
    List<SysLogs> selectByPage(SysLogs sysLogs);

    int batchInsert(List<SysLogs> list);
}
