package com.yjh.logs.module.log.service;

import com.yjh.logs.module.log.entity.SysLogs;
import com.yjh.logs.module.log.dao.SysLogsDao;

import java.util.*;

import com.yjh.logs.module.log.entity.SysLogsTime;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author tt
 * @since 2020-08-12
 */
@Service
public class SysLogsService{

    @Autowired
    private SysLogsDao sysLogsDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(SysLogs sysLogs) {
        UUID uuid = UUID.randomUUID();
        sysLogs.setLogId(uuid.toString());
        sysLogs.setCreateTime(new Date());
        return this.sysLogsDao.insert(sysLogs);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String logId) {
        return this.sysLogsDao.deleteByPrimaryId(logId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(SysLogs sysLogs) {
        return this.sysLogsDao.update(sysLogs);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysLogs selectByPrimaryId(String logId) {
        return this.sysLogsDao.selectByPrimaryId(logId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysLogs> select(String logId, String logType, String ip, String title, Integer state, String content, Long userId, String userName, Date createTime) {
        List<SysLogs> sysLogsList = sysLogsDao.select(logId,logType,ip,title,state,content,userId,userName,createTime);
        return sysLogsList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysLogs> selectByPage(SysLogsTime sysLogsTime) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("logId",sysLogsTime.getLogId());
        map.put("logType",sysLogsTime.getLogType());
        map.put("ip",sysLogsTime.getIp());
        map.put("title",sysLogsTime.getTitle());
        map.put("state",sysLogsTime.getState());
        map.put("content",sysLogsTime.getContent());
        map.put("userId",sysLogsTime.getUserId());
        map.put("userName",sysLogsTime.getUserName());
        map.put("createTime",sysLogsTime.getCreateTime());
        map.put("startTime",sysLogsTime.getStartTime());
        map.put("endTime",sysLogsTime.getEndTime());
        List<SysLogs> sysLogsList = sysLogsDao.selectByPage(map);
        return sysLogsList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysLogs> list) {
        return this.sysLogsDao.batchInsert(list);
    }

}