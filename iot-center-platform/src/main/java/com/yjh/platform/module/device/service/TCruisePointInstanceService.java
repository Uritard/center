package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import java.util.*;

import com.yjh.platform.module.device.entity.TCruisePointInstanceDetail;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TDictBusiness;
import lombok.extern.java.Log;
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

    @Autowired
    private TDictBusinessDao tDictBusinessDao;

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
    public List<TCruisePointInstance> select(Long instanceId, Long deviceMeteId, String stationId, String stationName, Long deviceId, String customId, String dataFormat, Integer identifyType, Integer identifySonType, Integer cruiseType, Long cruiseId, String cruiseName, String cruiseContent, String positionType, String unitVal, String unitName, Integer ifSy, Integer syType, Integer ifVideotape, String videotapeTime, String textDesc, String sort) {
        List<TCruisePointInstance> tCruisePointInstanceList = tCruisePointInstanceDao.select(instanceId, deviceMeteId, stationId, stationName, deviceId, customId, dataFormat, identifyType, identifySonType, cruiseType, cruiseId, cruiseName, cruiseContent, positionType, unitVal, unitName, ifSy, syType, ifVideotape, videotapeTime, textDesc, sort);
        return tCruisePointInstanceList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstanceDetail> selectByPage(TCruisePointInstanceDetail tCruisePointInstanceDetail) {
        List<TCruisePointInstanceDetail> list = tCruisePointInstanceDao.selectByPage(tCruisePointInstanceDetail);
        return list;
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

    @Logs(title = "标准设备测点关联机器人巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int STDMateUnionTRInspection(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("deviceMeteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        tCruisePointInstance.setCustomId(mapAll.get("customId"));
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("inspectionId")));
        tCruisePointInstance.setCruiseName(mapAll.get("inspectionName"));
        tCruisePointInstance.setCruiseType(4002);
        tCruisePointInstance.setIfSy(1);
        tCruisePointInstance.setDataFormat(mapAll.get("dataFormat"));
        tCruisePointInstance.setIdentifyType(Integer.valueOf(mapAll.get("identifyType")));
        tCruisePointInstance.setIfVideotape(Integer.valueOf(mapAll.get("ifVideotape")));
        tCruisePointInstance.setVideotapeTime(mapAll.get("videotapeTime"));
        tCruisePointInstance.setCruiseContent(mapAll.get("cruiseContent"));
        tCruisePointInstance.setUnitVal(mapAll.get("unit"));
        tCruisePointInstance.setTextDesc(mapAll.get("textDesc"));
        tCruisePointInstance.setPositionType(mapAll.get("positionType"));
        //间接的数据
        //station_id station_name unit_name
        TStdRegion tStdRegion = tCruisePointInstanceDao.selectTSRegionForStation();
        String str = mapAll.get("unit");
        TDictBusiness tDictBusiness = tCruisePointInstanceDao.selectTDBusinessForUnitName(mapAll.get("unit"));
        tCruisePointInstance.setUnitName(tDictBusiness.getDictNote());
        tCruisePointInstance.setStationId(tStdRegion.getStationId());
        tCruisePointInstance.setStationName(tStdRegion.getStationName());
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "遥测量测点关联机器人巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int telemeterUnionTRInspection(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("inspectionId")));
        tCruisePointInstance.setCruiseName(mapAll.get("inspectionName"));
        tCruisePointInstance.setCruiseType(4002);
        tCruisePointInstance.setIfSy(0);

        List<TDictBusiness> list2 = tDictBusinessDao.select(null,null,null,"遥测",null,null,null);
        tCruisePointInstance.setSyType(Integer.valueOf(list2.get(0).getDictCode()));
        tCruisePointInstance.setStationId(mapAll.get("stationId"));
        tCruisePointInstance.setStationName(mapAll.get("stationName"));
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("meteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        tCruisePointInstance.setUnitVal(mapAll.get("unit"));
        TDictBusiness tDictBusiness = tCruisePointInstanceDao.selectTDBusinessForUnitName(mapAll.get("unit"));
        tCruisePointInstance.setUnitName(tDictBusiness.getDictNote());
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "遥控量测点关联机器人巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int telecontrolUnionTRInspection(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("inspectionId")));
        tCruisePointInstance.setCruiseName(mapAll.get("inspectionName"));
        tCruisePointInstance.setCruiseType(4002);
        tCruisePointInstance.setIfSy(0);
        List<TDictBusiness> list2 = tDictBusinessDao.select(null,null,null,"遥控",null,null,null);
        tCruisePointInstance.setSyType(Integer.valueOf(list2.get(0).getDictCode()));
        tCruisePointInstance.setStationId(mapAll.get("stationId"));
        tCruisePointInstance.setStationName(mapAll.get("stationName"));
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("meteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "遥调量测点关联机器人巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int teleadjustUnionTRInspection(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("inspectionId")));
        tCruisePointInstance.setCruiseName(mapAll.get("inspectionName"));
        tCruisePointInstance.setCruiseType(4002);
        tCruisePointInstance.setIfSy(0);
        List<TDictBusiness> list2 = tDictBusinessDao.select(null,null,null,"遥调",null,null,null);
        tCruisePointInstance.setSyType(Integer.valueOf(list2.get(0).getDictCode()));
        tCruisePointInstance.setStationId(mapAll.get("stationId"));
        tCruisePointInstance.setStationName(mapAll.get("stationName"));
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("meteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "遥信量测点关联机器人巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int telesignalUnionTRInspection(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("inspectionId")));
        tCruisePointInstance.setCruiseName(mapAll.get("inspectionName"));
        tCruisePointInstance.setCruiseType(4002);
        tCruisePointInstance.setIfSy(0);
        List<TDictBusiness> list2 = tDictBusinessDao.select(null,null,null,"遥信",null,null,null);
        tCruisePointInstance.setSyType(Integer.valueOf(list2.get(0).getDictCode()));
        tCruisePointInstance.setStationId(mapAll.get("stationId"));
        tCruisePointInstance.setStationName(mapAll.get("stationName"));
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("meteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }


    @Logs(title = "标准设备测点关联摄像头预置位巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int STDMateUnionTCPreset(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("deviceMeteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        tCruisePointInstance.setCustomId(mapAll.get("customId"));
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("presetId")));
        tCruisePointInstance.setCruiseName(mapAll.get("presetName"));
        tCruisePointInstance.setCruiseType(4001);
        tCruisePointInstance.setIfSy(1);
        tCruisePointInstance.setDataFormat(mapAll.get("dataFormat"));
        tCruisePointInstance.setIdentifyType(Integer.valueOf(mapAll.get("identifyType")));
        tCruisePointInstance.setIfVideotape(Integer.valueOf(mapAll.get("ifVideotape")));
        tCruisePointInstance.setVideotapeTime(mapAll.get("videotapeTime"));
        tCruisePointInstance.setCruiseContent(mapAll.get("cruiseContent"));
        //tCruisePointInstance.setUnitVal(mapAll.get("unit"));
        tCruisePointInstance.setTextDesc(mapAll.get("textDesc"));
        tCruisePointInstance.setPositionType(mapAll.get("positionType"));
        //间接的数据
        //station_id station_name
        TStdRegion tStdRegion = tCruisePointInstanceDao.selectTSRegionForStation();
//        TDictBusiness tDictBusiness = tCruisePointInstanceDao.selectTDBusinessForUnitName(mapAll.get("uint"));
//        tCruisePointInstance.setUnitName(tDictBusiness.getDictNote());
        tCruisePointInstance.setStationId(tStdRegion.getStationId());
        tCruisePointInstance.setStationName(tStdRegion.getStationName());
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "遥测量测点关联摄像头预置位巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int telemeterUnionTCPreset(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("meteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("presetId")));
        tCruisePointInstance.setCruiseName(mapAll.get("presetName"));
        tCruisePointInstance.setCruiseType(4001);
        tCruisePointInstance.setIfSy(0);
        List<TDictBusiness> list2 = tDictBusinessDao.select(null,null,null,"遥测",null,null,null);
        tCruisePointInstance.setSyType(Integer.valueOf(list2.get(0).getDictCode()));
        tCruisePointInstance.setStationId(mapAll.get("stationId"));
        tCruisePointInstance.setStationName(mapAll.get("stationName"));
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "遥控量测点关联摄像头预置位巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int telecontrolUnionTCPreset(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("meteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("presetId")));
        tCruisePointInstance.setCruiseName(mapAll.get("presetName"));
        tCruisePointInstance.setCruiseType(4001);
        tCruisePointInstance.setIfSy(0);
        List<TDictBusiness> list2 = tDictBusinessDao.select(null,null,null,"遥控",null,null,null);
        tCruisePointInstance.setSyType(Integer.valueOf(list2.get(0).getDictCode()));
        tCruisePointInstance.setStationId(mapAll.get("stationId"));
        tCruisePointInstance.setStationName(mapAll.get("stationName"));
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "遥调量测点关联摄像头预置位巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int teleadjustUnionTCPreset(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("meteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("presetId")));
        tCruisePointInstance.setCruiseName(mapAll.get("presetName"));
        tCruisePointInstance.setCruiseType(4001);
        tCruisePointInstance.setIfSy(0);
        List<TDictBusiness> list2 = tDictBusinessDao.select(null,null,null,"遥调",null,null,null);
        tCruisePointInstance.setSyType(Integer.valueOf(list2.get(0).getDictCode()));
        tCruisePointInstance.setStationId(mapAll.get("stationId"));
        tCruisePointInstance.setStationName(mapAll.get("stationName"));
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "遥信量测点关联摄像头预置位巡检实例",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int telesignalUnionTCPreset(List<Map<String,String> > list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //传入的数据
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("meteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
        tCruisePointInstance.setCruiseId(Long.valueOf(mapAll.get("presetId")));
        tCruisePointInstance.setCruiseName(mapAll.get("presetName"));
        tCruisePointInstance.setCruiseType(4001);
        tCruisePointInstance.setIfSy(0);
        List<TDictBusiness> list2 = tDictBusinessDao.select(null,null,null,"遥信",null,null,null);
        tCruisePointInstance.setSyType(Integer.valueOf(list2.get(0).getDictCode()));
        tCruisePointInstance.setStationId(mapAll.get("stationId"));
        tCruisePointInstance.setStationName(mapAll.get("stationName"));
        return tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title="统计当前任务下的巡检点数量",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int selectCruiseCount(Long taskId){
        return this.tCruisePointInstanceDao.selectCruiseCount(taskId);
    }

    @Logs(title = "查询各种巡检类型下的巡检点数量",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int selectCruiseCountByType(Long taskId,Integer cruiseType){
        return this.tCruisePointInstanceDao.selectCruiseCountByType(taskId,cruiseType);
    }
}