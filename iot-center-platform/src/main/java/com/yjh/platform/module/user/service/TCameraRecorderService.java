package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.dao.TCameraRecorderDao;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.dao.TCameraInfoDao;

import java.util.List;

import com.yjh.platform.module.user.entity.TCameraRecorder;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author czh
 * @since 2020-08-13
 */
@Service
public class TCameraRecorderService{

    @Autowired
    private TCameraRecorderDao tCameraRecorderDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraRecorder tCameraRecorder) {
        return this.tCameraRecorderDao.insert(tCameraRecorder);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long recordId) {
        return this.tCameraRecorderDao.deleteByPrimaryId(recordId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraRecorder tCameraRecorder) {
        return this.tCameraRecorderDao.update(tCameraRecorder);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCameraRecorder selectByPrimaryId(Long recordId) {
        return this.tCameraRecorderDao.selectByPrimaryId(recordId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorder> select(Long recordId, String recordName, String aliasName, String recordIp, String protocal, Integer httpPort, Integer transPort, Integer rtspPort, String userName, String pwd,String root, Integer maxChannel, Integer hddSize, Integer
            buffer_day, Integer timeLong) {
        List<TCameraRecorder> tCameraRecorderList = tCameraRecorderDao.select(recordId, recordName, aliasName, recordIp, protocal, httpPort, transPort, rtspPort, userName, pwd,root, maxChannel, hddSize, buffer_day, timeLong);
        return tCameraRecorderList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorder> selectByPage(TCameraRecorder tCameraRecorder) {
        List<TCameraRecorder> tCameraRecorderList = tCameraRecorderDao.selectByPage(tCameraRecorder);
        return tCameraRecorderList;
    }

}

