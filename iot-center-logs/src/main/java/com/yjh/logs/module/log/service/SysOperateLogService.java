package com.yjh.logs.module.log.service;

import com.yjh.logs.commons.logs.Logs;
import com.yjh.logs.commons.logs.OperateLogDto;
import com.yjh.logs.module.log.dao.SysOperateLogDao;
import com.yjh.logs.module.log.entity.SysOperateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
* @author tt
* @since 2021-01-14
*/
@Service
public class SysOperateLogService {

    @Autowired
    private SysOperateLogDao sysOperateLogDao;

    private Logger log = LoggerFactory.getLogger(SysOperateLogService.class);

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(OperateLogDto operateLogDto) {

        if(Objects.isNull(operateLogDto)){
            log.error("SysOperateLogService.addOperateLog >>> error: parameter is null.");
            return 1;
        }
        SysOperateLog sysOperateLog = operateLogDto.formatSysOpLog();
        return sysOperateLogDao.insert(sysOperateLog);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long logId) {
        return this.sysOperateLogDao.deleteByPrimaryId(logId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(SysOperateLog sysOperateLog) {
        return this.sysOperateLogDao.update(sysOperateLog);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public SysOperateLog selectByPrimaryId(Long logId) {
        return this.sysOperateLogDao.selectByPrimaryId(logId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysOperateLog> select(Long logId, String traceId, String logType, String ip, String title, Integer state, String content, Long userId, String userName, String requestOrigin, String requestPath, Integer requestMethod, Date createTime) {
        List<SysOperateLog> sysOperateLogList = sysOperateLogDao.select(logId, traceId, logType, ip, title, state, content, userId, userName, requestOrigin, requestPath, requestMethod, createTime);
        return sysOperateLogList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<SysOperateLog> selectByPage(SysOperateLog sysOperateLog) {
        List<SysOperateLog> sysOperateLogList = sysOperateLogDao.selectByPage(sysOperateLog);
        return sysOperateLogList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysOperateLog> list) {
        return this.sysOperateLogDao.batchInsert(list);
    }

}

