package com.yjh.accessudp.module.device.service;

import com.yjh.accessudp.module.device.dao.SysLogsDao;
import com.yjh.accessudp.module.device.entity.SysLogs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public SysLogs selectByPrimaryId(String logId) {
        return this.sysLogsDao.selectByPrimaryId(logId);
    }

}

