package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.dao.TCameraInfoDao;

import java.util.List;

<<<<<<< Updated upstream
=======
import com.yjh.platform.module.user.entity.TCameraInfoByDict;
import com.yjh.platform.module.user.entity.TCameraPreset;
>>>>>>> Stashed changes
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
    public TCameraInfoByDict selectByPrimaryId(Long cameraId) {
        TCameraInfoByDict tCameraInfoByDict = this.tCameraInfoDao.selectByPrimaryId(cameraId);
        System.out.println("结果是："+tCameraInfoByDict);
        return tCameraInfoByDict;
//        return this.tCameraInfoDao.selectByPrimaryId(cameraId);
    }

<<<<<<< Updated upstream
=======
    @Logs(title = "根据间隔id查询所有摄像机信息", code = "TStdRegion")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> selectByRegionId(Long regionId) {
        List<TCameraInfoByDict> tCameraInfoByDictList = tCameraInfoDao.selectByRegionId(regionId);
        return tCameraInfoByDictList;
    }

    @Logs(title = "根据摄像机名称查询信息", code = "tCameraInfo")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> selectByCameraName(String cameraName) {
        List<TCameraInfoByDict> tCameraInfoByDictList = tCameraInfoDao.selectByCameraName(cameraName);
        return tCameraInfoByDictList;
    }

>>>>>>> Stashed changes
    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> select(Long cameraId, String cameraName, String aliasName, String recordId, Long upRegionId, Integer channelNum, Integer smsId, Integer rmsId, Integer vendorId, Integer streamType, Integer protocolType, String url, Integer port, Integer cameraType, Integer isControl) {
        List<TCameraInfoByDict> tCameraInfoByDictList = tCameraInfoDao.select(cameraId, cameraName, aliasName, recordId, upRegionId, channelNum, smsId, rmsId, vendorId, streamType, protocolType, url, port, cameraType, isControl);
        return tCameraInfoByDictList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> selectByPage(TCameraInfo tCameraInfo) {
        List<TCameraInfoByDict> tCameraInfoByDict = tCameraInfoDao.selectByPage(tCameraInfo);
        return tCameraInfoByDict;
    }

}

