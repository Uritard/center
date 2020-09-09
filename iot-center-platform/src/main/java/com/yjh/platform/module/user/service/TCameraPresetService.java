package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.entity.TCameraPresetExpand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
* @author yc
* @since 2020-08-18
*/
@Service
public class TCameraPresetService {

    @Autowired
    private TCameraPresetDao tCameraPresetDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraPreset tCameraPreset) {
        return this.tCameraPresetDao.insert(tCameraPreset);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long presetId) {
        return this.tCameraPresetDao.deleteByPrimaryId(presetId);
    }

    @Logs(title = "批量删除", code = "TCameraPreset")
    @Transactional(rollbackFor = Exception.class)
    public int deleteSelectedPreset(String presetIds) {
        List<String> list= Arrays.asList(presetIds.split(","));
        return this.tCameraPresetDao.deleteSelectedPreset(list);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraPreset tCameraPreset) {
        return this.tCameraPresetDao.update(tCameraPreset);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCameraPreset selectByPrimaryId(Long presetId) {
        return this.tCameraPresetDao.selectByPrimaryId(presetId);
    }

    @Logs(title = "根据摄像机id查询所有预置位信息", code = "TCameraInfo")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraPreset> selectByCameraId(Long cameraId) {
        List<TCameraPreset> tCameraPresetList = tCameraPresetDao.selectByCameraId(cameraId);
        return tCameraPresetList;
    }

    @Logs(title = "根据预置位名称查询信息", code = "TCameraPreset")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraPreset> selectByPresetName(String presetName) {
        List<TCameraPreset> tCameraPresetList = tCameraPresetDao.selectByPresetName(presetName);
        return tCameraPresetList;
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraPreset> select(Long presetId, Long cameraId, Integer presetNum, String presetName, String creatorUser, Date creatorTime, Integer isUse, String presetImg, Integer inspectionPostion, Integer collectStatus, Integer calibrationStatus) {
        List<TCameraPreset> tCameraPresetList = tCameraPresetDao.select(presetId, cameraId, presetNum, presetName, creatorUser, creatorTime, isUse, presetImg, inspectionPostion, collectStatus, calibrationStatus);
        return tCameraPresetList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraPresetExpand> selectByPage(TCameraPreset tCameraPreset) {
        List<TCameraPresetExpand> tCameraPresetExpandList = tCameraPresetDao.selectByPage(tCameraPreset);
        return tCameraPresetExpandList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCameraPreset> list) {
        return this.tCameraPresetDao.batchInsert(list);
    }

}

