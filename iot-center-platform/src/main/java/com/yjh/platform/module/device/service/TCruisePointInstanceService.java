package com.yjh.platform.module.device.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.controller.TCruisePointInstanceController;
import com.yjh.platform.module.device.dao.*;
import com.yjh.platform.module.device.entity.*;

import java.util.*;

import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.task.entity.TCruisePlanAttr;
import com.yjh.platform.module.task.entity.TCruiseTaskAttr;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TCameraPreset;
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

    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;

    @Autowired
    private TCruisePlanAttrDao tCruisePlanAttrDao;

    @Autowired
    private TCameraPresetDao tCameraPresetDao;

    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;

    @Autowired
    private TStdDeviceDao tStdDeviceDao;

    @Autowired
    private TCruisePointAttrDao tCruisePointAttrDao;

    private Logger log = LoggerFactory.getLogger(TCruisePointInstanceController.class);


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
    public int delete(TCruisePointInstance tCruisePointInstance) {
        return this.tCruisePointInstanceDao.delete(tCruisePointInstance);
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
    public List<TCruisePointInstance> select(Long instanceId, Long deviceMeteId, String stationId, String stationName, Long deviceId, String customId, String dataFormat, Integer identifyType, Integer identifySonType, Integer cruiseType, Long cruiseId, String cruiseName, String cruiseContent, String positionType, String unit, Integer ifSy, Integer syType, Integer ifVideotape, String videotapeTime, String textDesc, String sort) {
        List<TCruisePointInstance> tCruisePointInstanceList = tCruisePointInstanceDao.select(instanceId, deviceMeteId, stationId, stationName, deviceId, customId, dataFormat, identifyType, identifySonType, cruiseType, cruiseId, cruiseName, cruiseContent, positionType, unit, ifSy, syType, ifVideotape, videotapeTime, textDesc, sort);
        return tCruisePointInstanceList;
    }

    private void getConForCruisePoint(Integer length,TStdDeviceMeteForPointDetail tStdDeviceMeteForPointDetailItem,List<TStdDeviceMeteForPointDetail> listAll){
        //机器人
        if("228".equals(tStdDeviceMeteForPointDetailItem.getCruiseType())) {
            listAll.get(length).getRobotType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map = new HashMap<>();
            map.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getRobotType().getList().add(map);
            listAll.get(length).getRobotType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
        //视频 红外
        if("229".equals(tStdDeviceMeteForPointDetailItem.getCruiseType())) {
            listAll.get(length).getCameraType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map1 = new HashMap<>();
            map1.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getCameraType().getList().add(map1);
            listAll.get(length).getCameraType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
//        if("红外巡检".equals(tStdDeviceMeteForPointDetailItem.getCruiseTypeName())) {
//            listAll.get(length).getInfraredType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
//            Map<Object,Object> map2 = new HashMap<>();
//            map2.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
//            listAll.get(length).getInfraredType().getList().add(map2);
//            listAll.get(length).getInfraredType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
//        }
        //在线监控  内容未作
        if("231".equals((tStdDeviceMeteForPointDetailItem.getCruiseType()))) {
            listAll.get(length).getVoiceType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().getList().add(map3);
            listAll.get(length).getVoiceType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
        //scada 内容未作
        if("233".equals((tStdDeviceMeteForPointDetailItem.getCruiseType()))) {
            listAll.get(length).getVoiceType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().getList().add(map3);
            listAll.get(length).getVoiceType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
    }
    private int isIn(List<TStdDeviceMeteForPointDetail> listAll,Long deviceMeteId){
        int i = -1;
        for (int j = 0; j < listAll.size(); j++) {
            if(listAll.get(j).getDeviceMeteId().equals(deviceMeteId)){
                return j;
            }
        }
        return i;
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
            List<TStdDeviceMeteForPointDetail> listAll = new LinkedList<>();
            if(listForPage != null && listForPage.size()!=0) {
                List<TStdDeviceMeteForPointDetail> list = tCruisePointInstanceDao.selectCruisePointByPage(listForPage);
                listAll.add(list.get(0));
                if (list.get(0).getCruiseType() != null) {
                    getConForCruisePoint(0, list.get(0), listAll);
                }
                list.remove(0);
                int size = 0;
                for (TStdDeviceMeteForPointDetail tStdDeviceMeteForPointDetailItem : list) {
                    int length = isIn(listAll,tStdDeviceMeteForPointDetailItem.getDeviceMeteId());
                    if (length != -1) {
                        getConForCruisePoint(length, tStdDeviceMeteForPointDetailItem, listAll);
                    } else {
                        listAll.add(tStdDeviceMeteForPointDetailItem);
                        getConForCruisePoint(listAll.size()-1, tStdDeviceMeteForPointDetailItem, listAll);
                    }
                    size++;
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
        //机器人
        if("228".equals(tCfgMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getRobotType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map = new HashMap<>();
            map.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getRobotType().getList().add(map);
            listAll.get(length).getRobotType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
        //视频 红外
        if("229".equals(tCfgMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getCameraType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map1 = new HashMap<>();
            map1.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getCameraType().getList().add(map1);
            listAll.get(length).getCameraType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
//        if("红外巡检".equals(tStdDeviceMeteForPointDetailItem.getCruiseTypeName())) {
//            listAll.get(length).getInfraredType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
//            Map<Object,Object> map2 = new HashMap<>();
//            map2.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
//            listAll.get(length).getInfraredType().getList().add(map2);
//            listAll.get(length).getInfraredType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
//        }
        //在线监控  内容未作
        if("231".equals((tCfgMeteForPointDetailItem.getCruiseTypeName()))) {
            listAll.get(length).getVoiceType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().getList().add(map3);
            listAll.get(length).getVoiceType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
        //scada 内容未作
        if("233".equals((tCfgMeteForPointDetailItem.getCruiseTypeName()))) {
            listAll.get(length).getVoiceType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().getList().add(map3);
            listAll.get(length).getVoiceType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
    }
    @Logs(title = "告警联动分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Result selectSYCruisePointByPage(TCfgMeteForPointDetail tCfgMeteForPointDetail,int pageNum,int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<String> listForPage = tCruisePointInstanceDao.selectSYForCruiseByPage(tCfgMeteForPointDetail);
            resultMap.put("count", page.getTotal());
            List<TCfgMeteForPointDetail> listAll = new LinkedList<>();
            if(listForPage.size() != 0) {
                List<TCfgMeteForPointDetail> list = tCruisePointInstanceDao.selectSYCruisePointByPage(listForPage);
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
    public List<TCruisePointInstance> selectByPage(TCruisePointInstance tCruisePointInstance) {
        List<TCruisePointInstance> list = tCruisePointInstanceDao.selectByPage(tCruisePointInstance);
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


    @Logs(title = "巡检点关联配置",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int instanceUpdate(TCruisePointInstanceDetail tCruisePointInstanceDetail){
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(tCruisePointInstanceDetail.getDeviceMeteId());
        tCruisePointInstance.setDeviceId(tCruisePointInstanceDetail.getDeviceId());
        tCruisePointInstance.setCustomId(tCruisePointInstanceDetail.getCustomId());
        tCruisePointInstance.setCruiseType(tCruisePointInstanceDetail.getCruiseType());
        tCruisePointInstance.setIfSy(1);
        tCruisePointInstance.setUnit(tCruisePointInstanceDetail.getUnit());
        tCruisePointInstance.setPositionType(tCruisePointInstanceDetail.getPositionType());
        TStdRegion tStdRegion = tCruisePointInstanceDao.selectTSRegionForStation();
        tCruisePointInstance.setStationId(tStdRegion.getStationId());
        tCruisePointInstance.setStationName(tStdRegion.getStationName());
        List<Long> cruiseIdList = tCruisePointInstanceDao.selectCruiseId(tCruisePointInstanceDetail);//已经有了的巡检点
        Integer cruiseType = tCruisePointInstanceDetail.getCruiseType();
        int result = -1;
        //巡检点配置
        if (tCruisePointInstanceDetail.getIds().size()!=0){
            for (Long id:tCruisePointInstanceDetail.getIds()) {
                if(cruiseIdList != null && cruiseIdList.size()>0){
                    if(cruiseIdList.contains(id)){
                        cruiseIdList.remove(id);
                        continue;
                    }else {
                        TCruisePointAttr tCruisePointAttr = new TCruisePointAttr();
                        if(cruiseType == 229){//视频
                            TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(id);
                            tCruisePointInstance.setCruiseId(id);
                            tCruisePointInstance.setCruiseName(tCameraPreset.getPresetName());
                            tCruisePointAttr.setInstanceName(tCruisePointInstanceDetail.getMeteName()+tCameraPreset.getPresetName());
                        }
                        if(cruiseType == 228){//机器人
                            TRobotInspection tRobotInspection = tRobotInspectionDao.selectByPrimaryId(id);
                            tCruisePointInstance.setCruiseId(id);
                            tCruisePointInstance.setCruiseName(tRobotInspection.getInspectionName());
                            tCruisePointAttr.setInstanceName(tCruisePointInstanceDetail.getMeteName()+tRobotInspection.getInspectionName());
                        }
//                if(cruiseType == 230){//红外
//                    TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(id);
//                    tCruisePointInstance.setCruiseId(id);
//                    tCruisePointInstance.setCruiseName(tCameraPreset.getPresetName());
//                }
                        if(cruiseType == 232){//声纹
//                    TStdDevice tStdDevice = tStdDeviceDao.selectByPrimaryId(id);
//                    tCruisePointInstance.setCruiseName(tStdDevice.getDeviceName());
                        }
                        //231 在线监控 232 声纹
                        //新增配置号的巡检点
                        result =  tCruisePointInstanceDao.insert(tCruisePointInstance);
                        tCruisePointAttr.setInstanceId(tCruisePointInstance.getInstanceId());
                        tCruisePointAttrDao.add(tCruisePointAttr);
                    }
                }else {
                    TCruisePointAttr tCruisePointAttr = new TCruisePointAttr();
                    if(cruiseType == 229){//视频
                        TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(id);
                        tCruisePointInstance.setCruiseId(id);
                        tCruisePointInstance.setCruiseName(tCameraPreset.getPresetName());
                        tCruisePointAttr.setInstanceName(tCruisePointInstanceDetail.getMeteName()+tCameraPreset.getPresetName());
                    }
                    if(cruiseType == 228){//机器人
                        TRobotInspection tRobotInspection = tRobotInspectionDao.selectByPrimaryId(id);
                        tCruisePointInstance.setCruiseId(id);
                        tCruisePointInstance.setCruiseName(tRobotInspection.getInspectionName());
                        tCruisePointAttr.setInstanceName(tCruisePointInstanceDetail.getMeteName()+tRobotInspection.getInspectionName());
                    }
//                if(cruiseType == 230){//红外
//                    TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(id);
//                    tCruisePointInstance.setCruiseId(id);
//                    tCruisePointInstance.setCruiseName(tCameraPreset.getPresetName());
//                }
//                    if(cruiseType == 232){//声纹
//                    TStdDevice tStdDevice = tStdDeviceDao.selectByPrimaryId(id);
//                    tCruisePointInstance.setCruiseName(tStdDevice.getDeviceName());
//                    }
                    //231 在线监控 232 声纹
                    //新增配置号的巡检点
                    result =  tCruisePointInstanceDao.insert(tCruisePointInstance);
                    tCruisePointAttr.setInstanceId(tCruisePointInstance.getInstanceId());
                    tCruisePointAttrDao.add(tCruisePointAttr);
                }

            }
        }
        //删除关联表的巡检实例
        if(cruiseIdList != null && cruiseIdList.size()>0){
            List<Long> instanceIdList = tCruisePointInstanceDao.selectInstanceId(cruiseIdList,tCruisePointInstanceDetail.getDeviceMeteId());
            tCruisePointInstanceDao.deleteByInstanceId(instanceIdList);
            tCruisePointAttrDao.deleteByInstanceId(instanceIdList);
            tCruisePlanAttrDao.deleteByInstanceId(instanceIdList);
            tCruiseTaskAttrDao.deleteByInstanceId(instanceIdList);
        }
        return result;
    }



    @Logs(title = "告警联动配置",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int warnInspectUpdate(TCruisePointInstanceDetail tCruisePointInstanceDetail){
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(tCruisePointInstanceDetail.getDeviceMeteId());
        tCruisePointInstance.setDeviceId(tCruisePointInstanceDetail.getDeviceId());

        tCruisePointInstance.setCruiseType(tCruisePointInstanceDetail.getCruiseType());
        tCruisePointInstance.setIfSy(0);
        tCruisePointInstance.setSyType(tCruisePointInstanceDetail.getMeteKind());
        tCruisePointInstance.setUnit(tCruisePointInstanceDetail.getUnit());
        tCruisePointInstance.setStationId(tCruisePointInstanceDetail.getStationId());
        tCruisePointInstance.setStationName(tCruisePointInstanceDetail.getStationName());
        this.delete(tCruisePointInstance);
        Integer cruiseType = tCruisePointInstanceDetail.getCruiseType();
        int result = -1;
        if (tCruisePointInstanceDetail.getIds().size()!=0){
            for (Long id:tCruisePointInstanceDetail.getIds()) {
                if(cruiseType == 229){//视频
                    TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(id);
                    tCruisePointInstance.setCruiseId(id);
                    tCruisePointInstance.setCruiseName(tCameraPreset.getPresetName());
                }
                if(cruiseType == 228){//机器人
                    TRobotInspection tRobotInspection = tRobotInspectionDao.selectByPrimaryId(id);
                    tCruisePointInstance.setCruiseId(id);
                    tCruisePointInstance.setCruiseName(tRobotInspection.getInspectionName());
                }
//                if(cruiseType == 230){//红外
//                    TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(id);
//                    tCruisePointInstance.setCruiseId(id);
//                    tCruisePointInstance.setCruiseName(tCameraPreset.getPresetName());
//                }
                if(cruiseType == 232){//声纹
                    TStdDevice tStdDevice = tStdDeviceDao.selectByPrimaryId(id);
                    tCruisePointInstance.setCruiseName(tStdDevice.getDeviceName());
                }
                //231 在线监控 232 声纹
                result =  tCruisePointInstanceDao.insert(tCruisePointInstance);
            }
        }
        return result;
    }




    @Logs(title="统计当前任务下的巡检点数量",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> selectCruiseCount(String taskId){
        Map<String,Integer>map=new HashMap<>();
        map.put("Count",this.tCruisePointInstanceDao.selectCruiseCount(taskId));
        return map;
    }

    @Logs(title = "查询各种巡检类型下的巡检点数量",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseCountOfType> selectCruiseCountByType(String taskId){
        return this.tCruisePointInstanceDao.selectCruiseCountByType(taskId);
    }
}