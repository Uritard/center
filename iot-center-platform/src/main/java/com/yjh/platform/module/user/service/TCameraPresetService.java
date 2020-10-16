package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.CameraInfo;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.entity.TCameraPresetExpand;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
* @author yc
* @since 2020-08-18
*/
@Service
public class TCameraPresetService {

    @Autowired
    private TCameraPresetDao tCameraPresetDao;
    @Autowired
    private TCameraInfoDao tCameraInfoDao;

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

    @Logs(title = "查询最后一条记录", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCameraPreset selectLastOne() {
        return this.tCameraPresetDao.selectLastOne();
    }

    @Logs(title = "获得预置位树", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public AreaInfo selectPresetTree(Long cameraId) {
        TCameraInfo tCameraInfo = tCameraInfoDao.selectCamera(cameraId);
        List<TCameraPreset> presetList = tCameraPresetDao.selectByCameraId(cameraId);
        AreaInfo camera = new AreaInfo();
        camera.setUpId(-1L);
        camera.setId(cameraId);
        camera.setLabel(tCameraInfo.getCameraName());
        camera.setInfoType("infoType");
        List<AreaInfo> child = new LinkedList<>();
        for (TCameraPreset item:presetList) {
            AreaInfo preset = new AreaInfo();
            preset.setUpId(cameraId);
            preset.setId(item.getPresetId());
            preset.setLabel(item.getPresetName());
            preset.setInfoType("infoType");
            preset.setUpName(tCameraInfo.getCameraName());
            child.add(preset);
        }
        camera.setChildren(child);
        return camera;
    }

}

