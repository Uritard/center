package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import com.yjh.platform.module.device.dao.TStdDeviceAttrDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-08-24
*/
@Service
public class TStdDeviceAttrService{

    @Autowired
    private TStdDeviceAttrDao tStdDeviceAttrDao;


    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdDeviceAttr tStdDeviceAttr) {
        return this.tStdDeviceAttrDao.add(tStdDeviceAttr);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceId) {
        return this.tStdDeviceAttrDao.deleteByPrimaryId(deviceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDeviceAttr tStdDeviceAttr) {
        return this.tStdDeviceAttrDao.update(tStdDeviceAttr);
    }


    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceAttr selectByPrimaryId(Long deviceId) {
        return this.tStdDeviceAttrDao.selectByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceAttr> select(Long deviceId, Integer deviceModel, String pmsType, String pmsId, String deviceVendor, Date productionDate, Date usedTime, Date disableDate, Date lastMaintenance, String maintenanceCount, String organization, String department, String responsiblePerson, String latitude, String longitude, String ip, Integer port, String voltageLevel, String sequencePoint, String realCode,String address) {
        List<TStdDeviceAttr> tStdDeviceAttrList = tStdDeviceAttrDao.select(deviceId, deviceModel, pmsType, pmsId, deviceVendor, productionDate, usedTime, disableDate, lastMaintenance, maintenanceCount, organization, department, responsiblePerson, latitude, longitude, ip, port, voltageLevel, sequencePoint, realCode,address);
        return tStdDeviceAttrList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceAttr> selectByPage(TStdDeviceAttr tStdDeviceAttr) {
        List<TStdDeviceAttr> tStdDeviceAttrList = tStdDeviceAttrDao.selectByPage(tStdDeviceAttr);
        return tStdDeviceAttrList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TStdDeviceAttr> list) {
        return this.tStdDeviceAttrDao.batchInsert(list);
    }

}

