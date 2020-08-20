package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.user.entity.TCameraPreset;
import org.apache.poi.poifs.filesystem.Entry;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-08
*/
@Service
public class TCruisePointInstanceService{

    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruisePointInstance tCruisePointInstance) {
        return this.tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long instanceId) {
        return this.tCruisePointInstanceDao.deleteByPrimaryId(instanceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePointInstance tCruisePointInstance) {
        return this.tCruisePointInstanceDao.update(tCruisePointInstance);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePointInstance selectByPrimaryId(Long instanceId) {
        return this.tCruisePointInstanceDao.selectByPrimaryId(instanceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> select(Long instanceId, Long deviceMeteId, String stationId, String stationName, Long deviceId, String customId, String dataFormat, Integer identifyType, Integer identifySonType, Integer cruiseType, Long cruiseId, String cruiseName, String cruiseContent, String fluctuatingValue, String unitVal, String unitName, Integer ifSy, Integer syType, Integer ifVideotape, String videotapeTime, String textDesc, String sort) {
        List<TCruisePointInstance> tCruisePointInstanceList = tCruisePointInstanceDao.select(instanceId, deviceMeteId, stationId, stationName, deviceId, customId, dataFormat, identifyType, identifySonType, cruiseType, cruiseId, cruiseName, cruiseContent, fluctuatingValue, unitVal, unitName, ifSy, syType, ifVideotape, videotapeTime, textDesc, sort);
        return tCruisePointInstanceList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> selectByPage(TCruisePointInstance tCruisePointInstance) {
        List<TCruisePointInstance> tCruisePointInstanceList = tCruisePointInstanceDao.selectByPage(tCruisePointInstance);
        return tCruisePointInstanceList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruisePointInstance> list) {
        return this.tCruisePointInstanceDao.batchInsert(list);
    }

    @Logs(title = "标准测点关联机器人巡检点", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> StdMeteUnionInspectionId(Long deviceId) {
        List<TCruisePointInstance> tCruisePointInstanceList = tCruisePointInstanceDao.StdMeteUnionInspectionId(deviceId);
        return tCruisePointInstanceList;
    }

    @Logs(title = "创建机器人巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int STDMateUnionTRInspection(List<Map<String,Object> > list){
        Map<String,Object> map1 = list.get(0);//TRobotInspection的数据
        Map<String,Object> map2 = list.get(1);//TStdDeviceMete的数据
        TStdDeviceMete tStdDeviceMete = new TStdDeviceMete();
        TRobotInspection tRobotInspection = new TRobotInspection();
        //获取TStdDeviceMete的数据
        tStdDeviceMete.setDeviceMeteId(Long.valueOf(map2.get("deviceMeteId").toString()));
        tStdDeviceMete.setDeviceId(Long.valueOf(map2.get("deviceId").toString()));
        tStdDeviceMete.setCustomId((String)map2.get("CustomId"));
        //获取TRobotInspection的数据
        tRobotInspection.setInspectionId(Long.valueOf(map1.get("presetId").toString()));
        tRobotInspection.setInspectionName((String) map1.get("inspectionName"));
        //构造TCruisePointInstance数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(tStdDeviceMete.getDeviceMeteId());
        tCruisePointInstance.setDeviceId(tStdDeviceMete.getDeviceId());
        tCruisePointInstance.setCustomId(tStdDeviceMete.getCustomId());

        tCruisePointInstance.setCruiseName(tRobotInspection.getInspectionName());
        tCruisePointInstance.setCruiseId(tRobotInspection.getInspectionId());
        tCruisePointInstance.setCruiseType(4002);
        return tCruisePointInstanceDao.sTDMateUnionTRInspection(tCruisePointInstance);
    }

    @Logs(title = "创建摄像头预置位巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int STDMateUnionTCPreset(List<Map<String,Object> > list){
        Map<String,Object> map1 = list.get(0);//TCameraPreset的数据
        Map<String,Object> map2 = list.get(1);//TStdDeviceMete的数据
        TStdDeviceMete tStdDeviceMete = new TStdDeviceMete();
        TCameraPreset tCameraPreset = new TCameraPreset();
        //获取TStdDeviceMete的数据
        tStdDeviceMete.setDeviceMeteId(Long.valueOf(map2.get("deviceMeteId").toString()));
        tStdDeviceMete.setDeviceId(Long.valueOf(map2.get("deviceId").toString()));
        tStdDeviceMete.setCustomId((String)map2.get("CustomId"));
        //取得TCameraPreset的数据
        tCameraPreset.setPresetId(Long.valueOf(map1.get("presetId").toString()));
        tCameraPreset.setPresetName(map1.get("presetName").toString());
        //构造TCruisePointInstance数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(tStdDeviceMete.getDeviceMeteId());
        tCruisePointInstance.setDeviceId(tStdDeviceMete.getDeviceId());
        tCruisePointInstance.setCustomId(tStdDeviceMete.getCustomId());
        
        tCruisePointInstance.setCruiseId(tCameraPreset.getPresetId());
        tCruisePointInstance.setCruiseName(tCameraPreset.getPresetName());
        tCruisePointInstance.setCruiseType(4001);
        return tCruisePointInstanceDao.STDMateUnionTCPreset(tCruisePointInstance);
    }
}

