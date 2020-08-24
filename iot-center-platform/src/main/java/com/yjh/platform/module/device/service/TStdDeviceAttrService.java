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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdDeviceAttr tStdDeviceAttr) {
        return this.tStdDeviceAttrDao.insert(tStdDeviceAttr);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceId) {
        return this.tStdDeviceAttrDao.deleteByPrimaryId(deviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDeviceAttr tStdDeviceAttr) {
        return this.tStdDeviceAttrDao.update(tStdDeviceAttr);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceAttr selectByPrimaryId(Long deviceId) {
        return this.tStdDeviceAttrDao.selectByPrimaryId(deviceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceAttr> select(Long deviceId, Integer deviceModel, String pmsType, String pmsId, String manufacturer, Date productionDate, Date openingDate, Date disableDate, Date lastMaintenance, String maintenanceCount, String organization, String department, String responsiblePerson, String latitude, String longitude, String ip, Integer port, String para1, String para2, String para3) {
        List<TStdDeviceAttr> tStdDeviceAttrList = tStdDeviceAttrDao.select(deviceId, deviceModel, pmsType, pmsId, manufacturer, productionDate, openingDate, disableDate, lastMaintenance, maintenanceCount, organization, department, responsiblePerson, latitude, longitude, ip, port, para1, para2, para3);
        return tStdDeviceAttrList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceAttr> selectByPage(TStdDeviceAttr tStdDeviceAttr) {
        List<TStdDeviceAttr> tStdDeviceAttrList = tStdDeviceAttrDao.selectByPage(tStdDeviceAttr);
        return tStdDeviceAttrList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TStdDeviceAttr> list) {
        return this.tStdDeviceAttrDao.batchInsert(list);
    }

}

