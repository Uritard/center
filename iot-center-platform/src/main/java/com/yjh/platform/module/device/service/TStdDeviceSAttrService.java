package com.yjh.platform.module.device.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.dao.TStdDeviceAttrDao;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service
public class TStdDeviceSAttrService {


    @Autowired
    private TStdDeviceAttrDao tStdDeviceAttrDao;

    //插入
    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdDeviceAttr tStdDeviceAttr) {
        return this.tStdDeviceAttrDao.insert(tStdDeviceAttr);
    }

    //主键查询
    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceAttr selectByPrimaryId(Long deviceid){
        return this.tStdDeviceAttrDao.selectByPrimaryId(deviceid);
    }

    //查询
    @Logs(title = "查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceAttr> select(Long deviceId,Integer deviceSubtype,String serial,String manufacturer,String supplier,
                                       Date productionDate,Date openingDate, Date disableDate, Date lastMaintenance,
                                       String maintenanceCycle, String organization, String department, String responsiblePerson,
                                       String latitude, String longitude, String remark, String para1, String para2, String para3){
        List<TStdDeviceAttr> list = this.tStdDeviceAttrDao.select(deviceId,deviceSubtype,serial,manufacturer,supplier,
                productionDate,openingDate,disableDate,lastMaintenance,
                maintenanceCycle,organization,department,responsiblePerson,
                latitude,longitude,remark,para1,para2,para3);
        return list;
    }

    //分页查询
    @Logs(title = "分页查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceAttr> selectByPage(TStdDeviceAttr tStdDeviceAttr){
        return this.tStdDeviceAttrDao.selectByPage(tStdDeviceAttr);
    }

    //根据主键删除
    @Logs(title = "根据主键删除",code = "moudle")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryID(Long deviceID){
        return this.tStdDeviceAttrDao.deleteByPrimaryId(deviceID);
    }

    //修改
    @Logs(title = "修改",code = "moudle")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDeviceAttr tStdDeviceAttr){
        return this.tStdDeviceAttrDao.update(tStdDeviceAttr);
    }
}
