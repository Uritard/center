package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.dao.TWarnInfoDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-24
*/
@Service
public class TWarnInfoService{

    @Autowired
    private TWarnInfoDao tWarnInfoDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TWarnInfo tWarnInfo) {
        return this.tWarnInfoDao.insert(tWarnInfo);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long warnId) {
        return this.tWarnInfoDao.deleteByPrimaryId(warnId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TWarnInfo tWarnInfo) {
        return this.tWarnInfoDao.update(tWarnInfo);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TWarnInfo selectByPrimaryId(Long warnId) {
        return this.tWarnInfoDao.selectByPrimaryId(warnId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfo> select(Long warnId, Integer warnLevel, Date warnTime, Integer warnType, Long deviceId, String cunstomId, Long instanceId, Long stdMeteId, Integer confMode, Date confTime, String confUserId, String confInfo, Integer ifWarnDisable, Integer alarmSource, Integer warnSubtype, String deviceCode, String imagePath, String videoPath, String value, Integer defect, Integer defectLevel, String outRange, String linkMessage) {
        List<TWarnInfo> tWarnInfoList = tWarnInfoDao.select(warnId, warnLevel, warnTime, warnType, deviceId, cunstomId, instanceId, stdMeteId, confMode, confTime, confUserId, confInfo, ifWarnDisable, alarmSource, warnSubtype, deviceCode, imagePath, videoPath, value, defect, defectLevel, outRange, linkMessage);
        return tWarnInfoList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfo> selectByPage(TWarnInfo tWarnInfo) {
        List<TWarnInfo> tWarnInfoList = tWarnInfoDao.selectByPage(tWarnInfo);
        return tWarnInfoList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TWarnInfo> list) {
        return this.tWarnInfoDao.batchInsert(list);
    }

}

