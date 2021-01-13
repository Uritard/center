package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.IdAndNameDetail;
import com.yjh.platform.module.device.entity.TDeviceMaintenance;
import com.yjh.platform.module.device.dao.TDeviceMaintenanceDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Arrays;

import com.yjh.platform.module.device.entity.TDeviceMaintenanceDetail;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2021-01-11
*/
@Service
public class TDeviceMaintenanceService{

    @Autowired
    private TDeviceMaintenanceDao tDeviceMaintenanceDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TDeviceMaintenance tDeviceMaintenance) {
//        {
//            "deviceIdList": [
//            100000028,100000034
//  ],
//        "maintenanceId": 1,
//            "maintenanceName": "清华1号",
//                "maintenanceStart": "2021-01-11 06:36:54.759Z",
//                "maintenanceStop": "2021-02-11 06:36:54.759Z"
//        }
        tDeviceMaintenance.setDeviceId(tDeviceMaintenance.getDeviceIdList().get(0));
        this.tDeviceMaintenanceDao.add(tDeviceMaintenance);
        Long id = tDeviceMaintenance.getMaintenanceId();
        if(tDeviceMaintenance.getMaintenanceStart() == null){
            tDeviceMaintenance.setMaintenanceStart(new Date());
        }
        this.tDeviceMaintenanceDao.deleteByPrimaryId(tDeviceMaintenance.getMaintenanceId());

        List<Long> list = tDeviceMaintenance.getDeviceIdList();
        List<TDeviceMaintenance> addList = new ArrayList<>();
        for(Long item:list){
            TDeviceMaintenance deviceMaintenanceItem = new TDeviceMaintenance();
            deviceMaintenanceItem.setDeviceId(item)
                    .setMaintenanceName(tDeviceMaintenance.getMaintenanceName())
                    .setIsValid(tDeviceMaintenance.getIsValid())
                    .setMaintenanceStart(tDeviceMaintenance.getMaintenanceStart())
                    .setMaintenanceId(id)
                    .setMaintenanceStop(tDeviceMaintenance.getMaintenanceStop());
            addList.add(deviceMaintenanceItem);
        }
        return this.tDeviceMaintenanceDao.batchAdd(addList);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long maintenanceId) {
        return this.tDeviceMaintenanceDao.deleteByPrimaryId(maintenanceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TDeviceMaintenance tDeviceMaintenance) {
        List<Long> list = tDeviceMaintenance.getDeviceIdList();
        List<TDeviceMaintenance> addList = new ArrayList<>();
        this.deleteByPrimaryId(tDeviceMaintenance.getMaintenanceId());
        if(tDeviceMaintenance.getMaintenanceStart() == null){
            tDeviceMaintenance.setMaintenanceStart(new Date());
        }
        for(Long item:list){
            TDeviceMaintenance deviceMaintenanceItem = new TDeviceMaintenance();
            deviceMaintenanceItem.setDeviceId(item)
                    .setMaintenanceName(tDeviceMaintenance.getMaintenanceName())
                    .setIsValid(tDeviceMaintenance.getIsValid())
                    .setMaintenanceStart(tDeviceMaintenance.getMaintenanceStart())
                    .setMaintenanceId(tDeviceMaintenance.getMaintenanceId())
                    .setMaintenanceStop(tDeviceMaintenance.getMaintenanceStop());
            addList.add(deviceMaintenanceItem);
        }
        return this.tDeviceMaintenanceDao.batchAdd(addList);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDeviceMaintenance> selectByPrimaryId(Long maintenanceId) {
        List<TDeviceMaintenance> tDeviceMaintenanceList = tDeviceMaintenanceDao.selectByPrimaryId(maintenanceId);
        Date now = new Date();
        List<TDeviceMaintenance> re =new ArrayList<>();
        for(TDeviceMaintenance item: tDeviceMaintenanceList){
            if (item.getMaintenanceStop().compareTo(now) <= 0  ){
                item.setEffectiveState(407);
                item.setEffectiveStateName("已失效");
            }
            if(item.getMaintenanceStart().compareTo(now) >= 0  ){
                item.setEffectiveState(408);
                item.setEffectiveStateName("已生效");
            }
            if(item.getMaintenanceStart().compareTo(now) <= 0  && item.getMaintenanceStop().compareTo(now) >= 0){
                item.setEffectiveState(406);
                item.setEffectiveStateName("已生效");
            }
            re.add(item);
            //&& endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0)
        }
        return re;
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDeviceMaintenance> select(Long maintenanceId, String maintenanceName, Long deviceId, Integer isValid, Date maintenanceStart, Date maintenanceStop,Integer effectiveState) {
        List<TDeviceMaintenance> tDeviceMaintenanceList = tDeviceMaintenanceDao.select(maintenanceId,maintenanceName,deviceId,isValid,maintenanceStart,maintenanceStop);
        Date now = new Date();
        List<TDeviceMaintenance> re =new ArrayList<>();
        for(TDeviceMaintenance item: tDeviceMaintenanceList){
            if (item.getMaintenanceStop().compareTo(now) <= 0  ){
                item.setEffectiveState(407);
                item.setEffectiveStateName("已失效");
            }
            if(item.getMaintenanceStart().compareTo(now) >= 0  ){
                item.setEffectiveState(408);
                item.setEffectiveStateName("已生效");
            }
            if(item.getMaintenanceStart().compareTo(now) <= 0  && item.getMaintenanceStop().compareTo(now) >= 0){
                item.setEffectiveState(406);
                item.setEffectiveStateName("已生效");
            }
            if(effectiveState != null && item.getEffectiveState() != effectiveState){
                continue;
            }
            re.add(item);
            //&& endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0)
        }
        return re;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDeviceMaintenanceDetail> selectByPage(String maintenanceName,Integer effectiveState) {
        List<TDeviceMaintenanceDetail> tDeviceMaintenanceList = tDeviceMaintenanceDao.selectByPage(maintenanceName);
        Date now = new Date();
        List<TDeviceMaintenanceDetail> re =new ArrayList<>();
        for(TDeviceMaintenanceDetail item: tDeviceMaintenanceList){
            if (item.getMaintenanceStop().compareTo(now) <= 0  ){
                item.setEffectiveState(407);
                item.setEffectiveStateName("已失效");
            }
            if(item.getMaintenanceStart().compareTo(now) >= 0  ){
                item.setEffectiveState(408);
                item.setEffectiveStateName("未生效");
            }
            if(item.getMaintenanceStart().compareTo(now) <= 0  && item.getMaintenanceStop().compareTo(now) >= 0){
                item.setEffectiveState(406);
                item.setEffectiveStateName("已生效");
            }
            if(effectiveState != null && item.getEffectiveState() != effectiveState){
                continue;
            }
            List<IdAndNameDetail> list = this.tDeviceMaintenanceDao.selectIdAndName(item.getMaintenanceId());
            item.setDeviceInfo(list);
            re.add(item);
            //&& endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0)
        }
        return re;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TDeviceMaintenance> list) {
        return this.tDeviceMaintenanceDao.batchAdd(list);
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String maintenanceId) {
    List<String> list1= Arrays.asList(maintenanceId.split(","));
    return this.tDeviceMaintenanceDao.batchDelete(list1);
    }


    @Logs(title = "查询区域下的设备", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<IdAndNameDetail> selectDevice(String deviceIds) {
        String[] list = deviceIds.split(",");
        List<Long> idList = new ArrayList<>();
        for (String item: list) {
            idList.add(Long.valueOf(item));
        }
        if(idList != null && idList.size() != 0){
            return this.tDeviceMaintenanceDao.selectDevice(idList);
        }
        return null;
    }

    @Logs(title = "查询设备下的巡视点", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<IdAndNameDetail> selectInstance(Long deviceId) {
        return this.tDeviceMaintenanceDao.selectInstance(deviceId);
    }

}

