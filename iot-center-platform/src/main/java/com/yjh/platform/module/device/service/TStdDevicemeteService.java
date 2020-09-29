package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.yjh.platform.module.device.entity.TStdDeviceMeteDetail;
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

    @Logs(title = "插入", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdDeviceMete tStdDeviceMete) {
        return this.tStdDevicemeteDao.add(tStdDeviceMete);
    }

    @Logs(title = "删除", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceMeteId) {
        return this.tStdDevicemeteDao.deleteByPrimaryId(deviceMeteId)+tCruisePointInstanceDao.deleteByDeviceMeteId(deviceMeteId);
    }

    @Logs(title = "更新", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDeviceMete tStdDeviceMete) {
        return this.tStdDevicemeteDao.update(tStdDeviceMete);
    }

    @Logs(title = "主键查询", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceMete selectByPrimaryId(Long deviceMeteId) {
        return this.tStdDevicemeteDao.selectByPrimaryId(deviceMeteId);
    }

    @Logs(title = "查询", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> select(Long deviceMeteId, Long deviceId, String customType,Long meteId, String meteKind,String meteType, String meteName, Integer deviceType,  String positionType, String unit, String alarmNote, String alarmType, Float upEffect, Float downEffect, Integer alarmLevel, Float highLimit1, Float lowLimit1, Float highLimit2, Float lowLimit2, Integer alarmDelay, Integer alarmCnt, BigDecimal thresholdAbs, BigDecimal thresholdPer, Integer modulus, String remark,String stateZero,String stateOne,Integer  alarmState) {
        List<TStdDeviceMete> tStdDeviceMeteList = tStdDevicemeteDao.select(deviceMeteId, deviceId, customType, meteId, meteKind, meteType,meteName, deviceType,  positionType, unit, alarmNote, alarmType, upEffect, downEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark,stateZero,stateOne,alarmState);
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
    public List<TStdDeviceMete> selectByDevCus(Long deviceId,Long customType) {
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
        List<String> list1= Arrays.asList(list.split(","));
        for (String item:list1) {
            tCruisePointInstanceDao.deleteByDeviceMeteId(Long.valueOf(item));
        }
        return this.tStdDevicemeteDao.batchDelete(list1);
    }


}

