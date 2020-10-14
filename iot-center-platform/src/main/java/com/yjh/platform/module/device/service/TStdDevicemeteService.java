package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;

import java.math.BigDecimal;
import java.util.*;

import com.yjh.platform.module.device.entity.TStdDeviceMeteDetail;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TDictBusiness;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
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
public class TStdDevicemeteService{

    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private TStdDeviceDao tStdDeviceDao;
    @Autowired
    private TDictBusinessDao tDictBusinessDao;
    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;
    @Autowired
    private TCruisePlanAttrDao tCruisePlanAttrDao;


    @Logs(title = "插入", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdDeviceMeteDetail tStdDeviceMeteDetail) {
        //若插入的测点中的deviceId、customId存在于device表中，则不更新device表；否则进行更新
        TStdDevice tStdDevice=tStdDeviceDao.selectByUnionKeys(tStdDeviceMeteDetail.getDeviceId(),tStdDeviceMeteDetail.getCustomType());//查看当前设备ID和部位ID下的设备信息
        if(Objects.isNull(tStdDevice)){
            TStdDevice stdDevice=tStdDeviceDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceId());
            stdDevice.setCustomId(tStdDeviceMeteDetail.getCustomType());
            List<TDictBusiness> list = tDictBusinessDao.select(null,tStdDeviceMeteDetail.getCustomType(),null,null,null,null,null);
            stdDevice.setCustomName(list.get(0).getDictNote());
            tStdDeviceDao.add(stdDevice);
        }
        return this.tStdDevicemeteDao.add(tStdDeviceMeteDetail);
    }

    @Logs(title = "删除", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceMeteId) {
        TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(deviceMeteId);
        Long deviceId=tStdDeviceMete.getDeviceId();
        String customId=tStdDeviceMete.getCustomId();
        this.tStdDevicemeteDao.deleteByPrimaryId(deviceMeteId);

        if((tStdDevicemeteDao.selectDeviceMeteByDeviceCustom(deviceId,customId)).size()==0){
            tStdDeviceDao.deleteByUnionKeys(deviceId,customId);
        }
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        //tCruisePointInstance.setDeviceMeteId(deviceMeteId);
        List<Long> haveList = tStdDevicemeteDao.selectHave(deviceMeteId);
        tCruisePointInstanceDao.deleteByDeviceMeteId(deviceMeteId);//删除巡检点
        //删除关联的表
        if(haveList != null && haveList.size()>0){
            tCruisePlanAttrDao.deleteByInstanceId(haveList);
            tCruiseTaskAttrDao.deleteByInstanceId(haveList);
        }
        return this.tStdDevicemeteDao.deleteByPrimaryId(deviceMeteId);//删除测点
    }

    @Logs(title = "更新", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDeviceMeteDetail tStdDeviceMeteDetail) {
        TStdDevice tStdDevice=tStdDeviceDao.selectByUnionKeys(tStdDeviceMeteDetail.getDeviceId(),tStdDeviceMeteDetail.getCustomType());//查询新增的部位设备是否存在
        if(Objects.isNull(tStdDevice)){
            //新增没有 修改的部位 的设备
            TStdDevice stdDevice=tStdDeviceDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceId());
            stdDevice.setCustomId(tStdDeviceMeteDetail.getCustomType());
            List<TDictBusiness> list = tDictBusinessDao.select(null,tStdDeviceMeteDetail.getCustomType(),null,null,null,null,null);
            stdDevice.setCustomName(list.get(0).getDictNote());
            tStdDeviceDao.add(stdDevice);
        }else {
            TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceMeteId());//查询修改之前该标准测点的信息
            List<TStdDeviceMete> tStdDeviceMetes=tStdDevicemeteDao.selectByDevCus(tStdDeviceMete.getDeviceId(),tStdDeviceMete.getCustomId());
            if(tStdDeviceMetes.size()<=1){ //如果不存在其他 相同deviceId和customId的测点就删除该设备、否则保留
                tStdDeviceDao.deleteByUnionKeys(tStdDeviceMete.getDeviceId(),tStdDeviceMete.getCustomId());  //删除之前部位的设备
            }
        }
        return this.tStdDevicemeteDao.update(tStdDeviceMeteDetail);
    }

    @Logs(title = "主键查询", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceMete selectByPrimaryId(Long deviceMeteId) {
        return this.tStdDevicemeteDao.selectByPrimaryId(deviceMeteId);
    }

    @Logs(title = "查询", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> select(Long deviceMeteId, Long deviceId,String cusomId,Long meteId, String meteKind,String meteType, String meteName, Integer deviceType,  String positionType, String unit, String alarmNote, String alarmType, Float upEffect, Float downEffect, Integer alarmLevel, Float highLimit1, Float lowLimit1, Float highLimit2, Float lowLimit2, Integer alarmDelay, Integer alarmCnt, BigDecimal thresholdAbs, BigDecimal thresholdPer, Integer modulus, String remark,String stateZero,String stateOne,Integer  alarmState) {
        List<TStdDeviceMete> tStdDeviceMeteList = tStdDevicemeteDao.select(deviceMeteId, deviceId,cusomId, meteId, meteKind, meteType,meteName, deviceType,  positionType, unit, alarmNote, alarmType, upEffect, downEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark,stateZero,stateOne,alarmState);

        return tStdDeviceMeteList;
    }

    //告警规则未定
    @Logs(title = "分页查询", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMeteDetail> selectByPage(TStdDeviceMeteDetail tStdDeviceMeteDetail) {
//        if(tStdDeviceMeteDetail.getDeviceId() == null) {
//            if (ids.size() != 0) {
//                tStdDeviceMeteDetail.setIds(ids);
//            } else {
//                ids.add(tStdDeviceMeteDetail.getUpRegionId());
//                tStdDeviceMeteDetail.setIds(ids);
//            }
//        }else {
//            //ids.clear();
//            ids = new LinkedList<>();
//            ids.add(tStdDeviceMeteDetail.getDeviceId());
//            tStdDeviceMeteDetail.setIds(ids);
//            tStdDeviceMeteDetail.setUpRegionId(Long.valueOf(1));
//        }
        return tStdDevicemeteDao.selectByPage(tStdDeviceMeteDetail);
    }

    @Logs(title = "批量插入", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdDeviceMete> list) {
        return this.tStdDevicemeteDao.batchAdd(list);
    }

    @Logs(title = "根据设备模版ID查询对应测点", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectDevMeteByModelId(Long modelId) {
        return tStdDevicemeteDao.selectDevMeteByModelId(modelId);
    }

    @Logs(title = "新增/修改设备测点", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDevMete(List<TStdDeviceMete> list) {
        Long deviceId = list.get(0).getDeviceId();
        tStdDevicemeteDao.deleteByDevId(deviceId);
        return this.tStdDevicemeteDao.batchAdd(list);
    }

    @Logs(title = "根据设备Id删除设备测点", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByDevId(Long deviceId) {
        return this.tStdDevicemeteDao.deleteByDevId(deviceId);
    }

    @Logs(title = "设备ID与部位ID查询设备测点", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectByDevCus(Long deviceId,String customType) {
        return this.tStdDevicemeteDao.selectByDevCus(deviceId, customType);
    }


    @Logs(title = "查询生成预定义模板测点信息表",code = "device")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectPreDeviceMete(Long modelId,Long deviceId,Long customType){
        return this.tStdDevicemeteDao.selectPreDeviceMete(modelId,  deviceId, customType);
    }


    @Logs(title = "批量删除", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String list) {
        int deleteCount=0;
        List<String> list1= Arrays.asList(list.split(","));
        for (String item:list1) {
            tCruisePointInstanceDao.deleteByDeviceMeteId(Long.valueOf(item));//遍历删除巡视点

            TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(Long.valueOf(item));
            Long deviceId=tStdDeviceMete.getDeviceId();
            String customId=tStdDeviceMete.getCustomId();
            deleteCount=tStdDevicemeteDao.deleteByPrimaryId(Long.valueOf(item))+deleteCount;

            if((tStdDevicemeteDao.selectDeviceMeteByDeviceCustom(deviceId,customId)).size()==0){
                tStdDeviceDao.deleteByUnionKeys(deviceId,customId);
            }

        }
        return deleteCount;//批量删除标准测点
    }


}

