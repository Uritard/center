package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.dao.TCameraRecorderDao;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderByDict;
import com.yjh.platform.module.user.entity.TCameraRecorderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
* @author yc
* @since 2020-08-24
*/
@Service
public class TCameraRecorderService {

    @Autowired
    private TCameraRecorderDao tCameraRecorderDao;

    @Logs(title = "插入", code = "TCameraRecorder")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraRecorder tCameraRecorder) {
        return this.tCameraRecorderDao.insert(tCameraRecorder);
    }

    @Logs(title = "删除", code = "TCameraRecorder")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long recordId) {
        return this.tCameraRecorderDao.deleteByPrimaryId(recordId);
    }

    @Logs(title = "批量删除", code = "TCameraRecorder")
    @Transactional(rollbackFor = Exception.class)
    public int deleteSelectedRecord(String recordIds) {
        List<String> list= Arrays.asList(recordIds.split(","));
        return this.tCameraRecorderDao.deleteSelectedRecord(list);
    }

    @Logs(title = "更新", code = "TCameraRecorder")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraRecorder tCameraRecorder) {
        return this.tCameraRecorderDao.update(tCameraRecorder);
    }

    @Logs(title = "主键查询", code = "TCameraRecorder")
    @Transactional(rollbackFor = Exception.class)
    public TCameraRecorderByDict selectByPrimaryId(Long recordId) {
        return this.tCameraRecorderDao.selectByPrimaryId(recordId);
    }

    @Logs(title = "查询", code = "TCameraRecorder")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorderByDict> select(Long recordId, String recordName, String recorderType, String aliasName, String recordIp, String protocol, Integer httpPort, Integer transPort, Integer rtspPort, String userName, String pwd, String protocolUrl, Integer maxChannel, Integer hddSize, Integer bufferDay, Integer timeLong) {
        List<TCameraRecorderByDict> tCameraRecorderByDictList = tCameraRecorderDao.select(recordId, recordName, recorderType, aliasName, recordIp, protocol, httpPort, transPort, rtspPort, userName, pwd, protocolUrl, maxChannel, hddSize, bufferDay, timeLong);
        return tCameraRecorderByDictList;
    }

    @Logs(title = "分页查询", code = "TCameraRecorder")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorderByDict> selectByPage(TCameraRecorder tCameraRecorder) {
        List<TCameraRecorderByDict> tCameraRecorderByDictList = tCameraRecorderDao.selectByPage(tCameraRecorder);
        return tCameraRecorderByDictList;
    }
    @Logs(title = "查询部分信息", code = "TCameraRecorder")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorderDetail> selectIdAndName(){
        return tCameraRecorderDao.selectIdAndName();
    }

    @Logs(title = "批量插入", code = "TCameraRecorder")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCameraRecorder> list) {
        return this.tCameraRecorderDao.batchInsert(list);
    }

}

