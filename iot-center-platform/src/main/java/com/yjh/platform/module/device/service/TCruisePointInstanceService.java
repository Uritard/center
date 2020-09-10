package com.yjh.platform.module.device.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.controller.TCruisePointInstanceController;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import java.util.*;

import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TDictBusiness;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author tt
 * @since 2020-08-08
 */
@Service
public class TCruisePointInstanceService{

    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;

    private Logger log = LoggerFactory.getLogger(TCruisePointInstanceController.class);

    @Autowired
    private TDictBusinessDao tDictBusinessDao;

    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruisePointInstance tCruisePointInstance) {
        return this.tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Logs(title = "根据主键删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long instanceId) {
        return this.tCruisePointInstanceDao.deleteByPrimaryId(instanceId);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int delete(TStdDeviceMeteForPointDetail tStdDeviceMeteForPointDetail) {
        return this.tCruisePointInstanceDao.delete(tStdDeviceMeteForPointDetail);
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

    private void getConForCruisePoint(Integer length,TStdDeviceMeteForPointDetail tStdDeviceMeteForPointDetailItem,List<TStdDeviceMeteForPointDetail> listAll){
        if("机器人巡检".equals(tStdDeviceMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getRobotType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map = new HashMap<>();
            map.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getRobotType().getList().add(map);
            listAll.get(length).getRobotType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
        if("视频巡检".equals(tStdDeviceMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getCameraType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map1 = new HashMap<>();
            map1.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getCameraType().getList().add(map1);
            listAll.get(length).getCameraType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
        if("红外巡检".equals(tStdDeviceMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getInfraredType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map2 = new HashMap<>();
            map2.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getInfraredType().getList().add(map2);
            listAll.get(length).getInfraredType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
        if("声纹巡检".equals(tStdDeviceMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getVoiceType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().getList().add(map3);
            listAll.get(length).getVoiceType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
    }
    @Logs(title = "巡检点分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Result selectCruisePointByPage(TStdDeviceMete tStdDeviceMete,int pageNum,int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<Long> listForPage = tCruisePointInstanceDao.selectForCruiseByPage(tStdDeviceMete);
            resultMap.put("count", page.getTotal());
            List<TStdDeviceMeteForPointDetail> list = tCruisePointInstanceDao.selectCruisePointByPage(listForPage);
            List<TStdDeviceMeteForPointDetail> listAll = new LinkedList<>();
            listAll.add(list.get(0));
            if (list.get(0).getCruiseType() != null) {
                getConForCruisePoint(0, list.get(0), listAll);
            }
            list.remove(0);
            for (TStdDeviceMeteForPointDetail tStdDeviceMeteForPointDetailItem : list) {
                int length = listAll.size() - 1;
                if (tStdDeviceMeteForPointDetailItem.getDeviceMeteId().equals(listAll.get(length).getDeviceMeteId())) {
                    getConForCruisePoint(length, tStdDeviceMeteForPointDetailItem, listAll);
                } else {
                    listAll.add(tStdDeviceMeteForPointDetailItem);
                    getConForCruisePoint(length + 1, tStdDeviceMeteForPointDetailItem, listAll);
                }
            }
            resultMap.put("list", listAll);
            result.setData(resultMap);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例分页查询失败描述：", e);
        }
        return result;
    }

    private void getConSYForCruisePoint(Integer length,TCfgMeteForPointDetail tCfgMeteForPointDetailItem,List<TCfgMeteForPointDetail> listAll){
        if("机器人巡检".equals(tCfgMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getRobotType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map = new HashMap<>();
            map.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getRobotType().getList().add(map);
            listAll.get(length).getRobotType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
        if("视频巡检".equals(tCfgMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getCameraType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map1 = new HashMap<>();
            map1.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getCameraType().getList().add(map1);
            listAll.get(length).getCameraType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
        if("红外巡检".equals(tCfgMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getInfraredType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map2 = new HashMap<>();
            map2.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getInfraredType().getList().add(map2);
            listAll.get(length).getInfraredType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
        if("声纹巡检".equals(tCfgMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getVoiceType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().getList().add(map3);
            listAll.get(length).getVoiceType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
    }
    @Logs(title = "告警联动分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Result selectSYCruisePointByPage(TCfgMete tCfgMete,int pageNum,int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<String> listForPage = tCruisePointInstanceDao.selectSYForCruiseByPage(tCfgMete);
            resultMap.put("count", page.getTotal());

            List<TCfgMeteForPointDetail> list = tCruisePointInstanceDao.selectSYCruisePointByPage(listForPage);
            List<TCfgMeteForPointDetail> listAll = new LinkedList<>();
            listAll.add(list.get(0));
            if (list.get(0).getCruiseType() != null) {
                getConSYForCruisePoint(0, list.get(0), listAll);
            }
            list.remove(0);
            for (TCfgMeteForPointDetail tCfgMeteForPointDetailItem : list) {
                int length = listAll.size() - 1;
                if (tCfgMeteForPointDetailItem.getMeteId().equals(listAll.get(length).getMeteId())) {
                    getConSYForCruisePoint(length, tCfgMeteForPointDetailItem, listAll);
                } else {
                    listAll.add(tCfgMeteForPointDetailItem);
                    getConSYForCruisePoint(length + 1, tCfgMeteForPointDetailItem, listAll);
                }
            }
            resultMap.put("list", listAll);
            result.setData(resultMap);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例分页查询失败描述：", e);
        }
        return result;
    }


    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstanceDetail> selectByPage(TCruisePointInstanceDetail tCruisePointInstanceDetail) {
        List<TCruisePointInstanceDetail> list = tCruisePointInstanceDao.selectByPage(tCruisePointInstanceDetail);
        List<TCruisePointInstanceDetail> listAll = new LinkedList<>();
        listAll.add(list.get(0));
        if(list.get(0).getCruiseType() != null){
            System.out.println(list.get(0).getCruiseType());
            list.get(0).getListType().add(list.get(0).getCruiseType());
            list.get(0).getListType().add(list.get(0).getCruiseId());
            list.get(0).getListType().add(list.get(0).getCruiseTypeName());
        }
        list.remove(0);
        for (TCruisePointInstanceDetail tCruisePointInstanceDetail1:list) {
            int length = listAll.size()-1;
            if(tCruisePointInstanceDetail1.getDeviceMeteId().equals(listAll.get(length).getDeviceMeteId())){
                if(tCruisePointInstanceDetail1.getCruiseType() != null) {
                    listAll.get(length).getListType().add(tCruisePointInstanceDetail1.getCruiseType());
                    listAll.get(length).getListType().add(tCruisePointInstanceDetail1.getCruiseId());
                    listAll.get(length).getListType().add(tCruisePointInstanceDetail1.getCruiseTypeName());
                }
            }else {
                listAll.add(tCruisePointInstanceDetail1);
            }
        }
        return listAll;
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
        tCruisePointInstance.setIfSy(1);
//        tCruisePointInstance.setDataFormat(mapAll.get("dataFormat"));
//        tCruisePointInstance.setIdentifyType(Integer.valueOf(mapAll.get("identifyType")));
//        tCruisePointInstance.setIfVideotape(Integer.valueOf(mapAll.get("ifVideotape")));
//        tCruisePointInstance.setVideotapeTime(mapAll.get("videotapeTime"));
//        tCruisePointInstance.setCruiseContent(mapAll.get("cruiseContent"));
//        tCruisePointInstance.setTextDesc(mapAll.get("textDesc"));
//        tCruisePointInstance.setPositionType(mapAll.get("positionType"));
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
        tCruisePointInstance.setIfSy(0);

        List<TDictBusiness> list2 = tDictBusinessDao.select(null,null,null,"遥测",null,null,null);
        tCruisePointInstance.setSyType(Integer.valueOf(list2.get(0).getDictCode()));
        tCruisePointInstance.setStationId(mapAll.get("stationId"));
        tCruisePointInstance.setStationName(mapAll.get("stationName"));
        tCruisePointInstance.setDeviceMeteId(Long.valueOf(mapAll.get("meteId")));
        tCruisePointInstance.setDeviceId(Long.valueOf(mapAll.get("deviceId")));
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
        tCruisePointInstance.setIfSy(1);
//        tCruisePointInstance.setDataFormat(mapAll.get("dataFormat"));
//        tCruisePointInstance.setIdentifyType(Integer.valueOf(mapAll.get("identifyType")));
//        tCruisePointInstance.setIfVideotape(Integer.valueOf(mapAll.get("ifVideotape")));
//        tCruisePointInstance.setVideotapeTime(mapAll.get("videotapeTime"));
//        tCruisePointInstance.setCruiseContent(mapAll.get("cruiseContent"));
//        tCruisePointInstance.setTextDesc(mapAll.get("textDesc"));
//        tCruisePointInstance.setPositionType(mapAll.get("positionType"));
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
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
        List<TDictBusiness> listCruiseType = tDictBusinessDao.select(null,null,null,mapAll.get("cruiseType"),null,null,null);
        tCruisePointInstance.setCruiseType(Integer.valueOf(listCruiseType.get(0).getDictCode()));
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