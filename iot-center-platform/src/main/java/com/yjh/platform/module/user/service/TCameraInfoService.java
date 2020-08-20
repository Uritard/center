package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.dao.TCameraInfoDao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-07-23
*/
@Service
public class TCameraInfoService{

    @Autowired
    private TCameraInfoDao tCameraInfoDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraInfo tCameraInfo) {
        return this.tCameraInfoDao.insert(tCameraInfo);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long cameraId) {
        return this.tCameraInfoDao.deleteByPrimaryId(cameraId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraInfo tCameraInfo) {
        return this.tCameraInfoDao.update(tCameraInfo);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCameraInfo selectByPrimaryId(Long cameraId) {
        return this.tCameraInfoDao.selectByPrimaryId(cameraId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfo> select(Long cameraId, String cameraName, String aliasName, String recordId, Long upRegionId, Integer channelNum, Integer smsId, Integer rmsId, String factoryName, Integer streamType, Integer protocolType, String url, Integer port, Integer cameraType, Integer isControl) {
        List<TCameraInfo> tCameraInfoList = tCameraInfoDao.select(cameraId, cameraName, aliasName, recordId, upRegionId, channelNum, smsId, rmsId, factoryName, streamType, protocolType, url, port, cameraType, isControl);
        return tCameraInfoList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfo> selectByPage(TCameraInfo tCameraInfo) {
        List<TCameraInfo> tCameraInfoList = tCameraInfoDao.selectByPage(tCameraInfo);
        return tCameraInfoList;
    }

}

