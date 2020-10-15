package com.yjh.platform.module.user.service;


import com.yjh.platform.common.utils.Object2Map;

import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.dao.TCameraInfoDao;

import java.util.*;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;




/**
* @author tt
* @since 2020-07-23
*/
@Service
public class TCameraInfoService {

    @Autowired
    private TCameraInfoDao tCameraInfoDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TCameraPresetDao tCameraPresetDao;


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

    @Logs(title = "批量删除", code = "TCameraPreset")
    @Transactional(rollbackFor = Exception.class)
    public int deleteSelectedCamera(String cameraIds) {
        List<String> list = Arrays.asList(cameraIds.split(","));
        return this.tCameraInfoDao.deleteSelectedCamera(list);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraInfo tCameraInfo) {
        return this.tCameraInfoDao.update(tCameraInfo);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCameraInfoByDict selectByPrimaryId(Long cameraId) {
        return this.tCameraInfoDao.selectByPrimaryId(cameraId);
    }

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

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> select(Long cameraId, String cameraName, String aliasName, String recordId,
                                          Long upRegionId, Integer channelNum, Integer smsId, Integer rmsId,
                                          Integer vendorId, Integer streamType, Integer protocolType, String cameraIp,
                                          String url, Integer port, Integer cameraType, Integer isControl, String latitude,
                                          String longitude, String address) {
        List<TCameraInfoByDict> tCameraInfoByDictList = tCameraInfoDao.select(cameraId, cameraName, aliasName,
                recordId, upRegionId, channelNum, smsId, rmsId, vendorId, streamType, protocolType, cameraIp, url,
                port, cameraType, latitude, longitude, address, isControl);
        return tCameraInfoByDictList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> selectByPage(TCameraInfo tCameraInfo, List<Long> upRegionIds) {
        if (upRegionIds.size() != 0) {
            tCameraInfo.setUpRegionIds(upRegionIds);
        } else {
            upRegionIds.add(tCameraInfo.getUpRegionId());
            tCameraInfo.setUpRegionIds(upRegionIds);
        }
        List<TCameraInfoByDict> tCameraInfoByDict = tCameraInfoDao.selectByPage(tCameraInfo);
        return tCameraInfoByDict;
    }

    @Logs(title = "查询当前任务下的摄像头信息")
    @Transactional(rollbackFor = Exception.class)
    public List<CameraInfo> selectCameraByTaskId(Long taskId) {
        return this.tCameraInfoDao.selectCameraByTaskId(taskId);
    }

    @Logs(title = "查询所有摄像头预置位信息树", code = "cameraInfo")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> selectPresetTree() {
        List<TCamreaPresetTree> cameraList = tCameraInfoDao.selectCameraId();
        List<TCamreaPresetTree> tCamreaPresetTreeList = tCameraInfoDao.batchSelectPreset();
        List<Map<String, Object>> cameraPresetTreeTemList = new ArrayList<>();
        for (TCamreaPresetTree tCamrea : cameraList) {
            Map<String, Object> cameraPresetTreeTem = new HashMap<>();
            List<Map<String, Object>> presetList = new ArrayList<>();
            Long cameraId = tCamrea.getCameraId();
            for (TCamreaPresetTree tCamreaPresetTree : tCamreaPresetTreeList) {
                Long cameraIdTem = tCamreaPresetTree.getCameraId();
                if (Objects.nonNull(cameraIdTem) && Objects.equals(cameraId, cameraIdTem)) {
                    Map<String, Object> presetMap = new HashMap<>();
                    presetMap.put("id", tCamreaPresetTree.getPresetId());
                    presetMap.put("label", tCamreaPresetTree.getPresetName());
                    presetMap.put("infoType", "preset");
                    presetList.add(presetMap);
                }
            }
            cameraPresetTreeTem.put("id", cameraId);
            cameraPresetTreeTem.put("label", tCamrea.getCameraName());
            cameraPresetTreeTem.put("infoType", "camera");
            cameraPresetTreeTem.put("children", presetList);
            cameraPresetTreeTemList.add(cameraPresetTreeTem);
        }
        Map<String, Object> cameraPresetTree = new HashMap<>();
        cameraPresetTree.put("children", cameraPresetTreeTemList);
        cameraPresetTree.put("label", "摄像机预置位树");
        cameraPresetTree.put("id", "-1");
        cameraPresetTree.put("infoType", "tree");
        List<Map<String, Object>> cameraPresetList = new ArrayList<>();
        cameraPresetList.add(cameraPresetTree);
        return cameraPresetList;
    }


    @Logs(title = "将cameraInfo数据放入redis", code = "cameraInfo")
    @Transactional(rollbackFor = Exception.class)
    public int intoRedis() {
        List<TCameraPreset> list =tCameraPresetDao.selectAll();
        for (TCameraPreset item:list) {
            TCameraInfo tCameraInfo = tCameraInfoDao.selectCamera(item.getCameraId());
            Map map = Object2Map.toStringMap(Object2Map.objectToMap(item,true));
            if(tCameraInfo != null){
                Map map2 = Object2Map.toStringMap(Object2Map.objectToMap(tCameraInfo,true));
                map.putAll(map2);
            }

            String str = "camera_info:"+item.getPresetId();
            redisTemplate.opsForHash().putAll(str, map);
        }
       return 1;
    }


}



