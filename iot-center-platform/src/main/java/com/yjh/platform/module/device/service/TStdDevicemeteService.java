package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;

import java.math.BigDecimal;
import java.util.List;

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

    @Logs(title = "插入", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdDeviceMete tStdDeviceMete) {
        return this.tStdDevicemeteDao.insert(tStdDeviceMete);
    }

    @Logs(title = "删除", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceMeteId) {
        return this.tStdDevicemeteDao.deleteByPrimaryId(deviceMeteId);
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
    public List<TStdDeviceMete> select(Long deviceMeteId, Long deviceId, String customId, Long meteId, Integer meteType, String meteName, Integer deviceType, Integer customType, String positionType, String unit, String alarmNote, String alarmType, Float upEffect, Float lowEffect, Integer alarmLevel, Float highLimit1, Float lowLimit1, Float highLimit2, Float lowLimit2, Integer alarmDelay, Integer alarmCnt, BigDecimal thresholdAbs, BigDecimal thresholdPer, Integer modulus, String remark) {
        List<TStdDeviceMete> tStdDeviceMeteList = tStdDevicemeteDao.select(deviceMeteId, deviceId, customId, meteId, meteType, meteName, deviceType, customType, positionType, unit, alarmNote, alarmType, upEffect, lowEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark);
        return tStdDeviceMeteList;
    }

    @Logs(title = "分页查询", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectByPage(TStdDeviceMete tStdDeviceMete) {
        return tStdDevicemeteDao.selectByPage(tStdDeviceMete);
    }

    @Logs(title = "批量插入", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TStdDeviceMete> list) {
        return this.tStdDevicemeteDao.batchInsert(list);
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
        return this.tStdDevicemeteDao.batchInsert(list);
    }

    @Logs(title = "根据设备Id删除设备测点", code = "device")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByDevId(Long deviceId) {
        return this.tStdDevicemeteDao.deleteByDevId(deviceId);
    }

}

