package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.dao.TCfgDeviceDao;

import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-08-25
*/
@Service
public class TCfgDeviceService{

    @Autowired
    private TCfgDeviceDao tCfgDeviceDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgDevice tCfgDevice) {
        return this.tCfgDeviceDao.insert(tCfgDevice);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String deviceId) {
        return this.tCfgDeviceDao.deleteByPrimaryId(deviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgDevice tCfgDevice) {
        return this.tCfgDeviceDao.update(tCfgDevice);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgDevice selectByPrimaryId(String deviceId) {
        return this.tCfgDeviceDao.selectByPrimaryId(deviceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDevice> select(String deviceId, String deviceName, String deviceType, String deviceCode, String logicalType, Long projectId, String relationCode, Date createTime, Date updateTime, String remark) {
        List<TCfgDevice> tCfgDeviceList = tCfgDeviceDao.select(deviceId, deviceName, deviceType, deviceCode, logicalType, projectId, relationCode, createTime, updateTime, remark);
        return tCfgDeviceList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDevice> selectByPage(TCfgDevice tCfgDevice) {
        List<TCfgDevice> tCfgDeviceList = tCfgDeviceDao.selectByPage(tCfgDevice);
        return tCfgDeviceList;
    }


    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgDevice> list) {
        return this.tCfgDeviceDao.batchInsert(list);
    }


    public int createSYPoint(List<Map<String,String>> list){
        Map<String,String> mapAll = new HashMap<>();
        for (Map map:list) {
            mapAll.putAll(map);
        }
        //0：遥信，1：遥测，2：遥控，3：遥调，
        if(mapAll.get("meteKind").equals("0")){
            return this.insertIntoTelesignal(mapAll);
        }
        if(mapAll.get("meteKind").equals("1")){
            return this.insertIntoTelemeter(mapAll);
        }
        if(mapAll.get("meteKind").equals("2")){
            return this.insertIntoTelecontrol(mapAll);
        }
        if(mapAll.get("meteKind").equals("3")){
            return this.insertIntoTeleadjust(mapAll);
        }
        return 0;
    }



    @Logs(title = "摇调", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insertIntoTeleadjust(Map<String,String> mapAll){

        //做判断 四摇类型是否对应
        TCfgTeleadjust tCfgTeleadjust = new TCfgTeleadjust();
        tCfgTeleadjust.setDeviceId(mapAll.get("deviceId"));
        tCfgTeleadjust.setMeteId(mapAll.get("meteId"));
        tCfgTeleadjust.setMeteName(mapAll.get("meteName"));
        tCfgTeleadjust.setMeteCode(mapAll.get("meteCode"));
        tCfgTeleadjust.setUpEffect(Float.valueOf(mapAll.get("upEffect")));
        tCfgTeleadjust.setLowEffect(Float.valueOf(mapAll.get("lowEffect")));
        tCfgTeleadjust.setMetePrecision(Integer.valueOf(mapAll.get("metePrecision")));
        tCfgTeleadjust.setUnit(mapAll.get("unit"));
        tCfgTeleadjust.setDeviceType(mapAll.get("deviceType"));
        tCfgTeleadjust.setStander(Float.valueOf(mapAll.get("stander")));
        tCfgTeleadjust.setControlenable(Integer.valueOf(mapAll.get("controlenable")));

        return tCfgDeviceDao.insertIntoTeleadjust(tCfgTeleadjust);
    }

    @Logs(title = "摇信", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insertIntoTelesignal(Map<String,String> mapAll){

        TCfgTelesignal tCfgTelesignal = new TCfgTelesignal();
        tCfgTelesignal.setDeviceId(mapAll.get("deviceId"));
        tCfgTelesignal.setMeteId(mapAll.get("meteId"));
        tCfgTelesignal.setMeteName(mapAll.get("meteName"));
        tCfgTelesignal.setUpEffect(Integer.valueOf(mapAll.get("upEffect")));
        tCfgTelesignal.setLowEffect(Integer.valueOf(mapAll.get("lowEffect")));
        tCfgTelesignal.setMeteCode(mapAll.get("meteCode"));
        tCfgTelesignal.setDeviceType(mapAll.get("deviceType"));
        tCfgTelesignal.setAlarmthresbhold(Integer.valueOf(mapAll.get("alarmthresbhold")));
        tCfgTelesignal.setAlarmlevel(Integer.valueOf(mapAll.get("alarmlevel")));
        tCfgTelesignal.setDescriber(mapAll.get("describer"));

        return tCfgDeviceDao.insertIntoTelesignal(tCfgTelesignal);
    }

    @Logs(title = "摇测", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insertIntoTelemeter(Map<String,String> mapAll){

        TCfgTelemeter tCfgTelemeter = new TCfgTelemeter();
        tCfgTelemeter.setDeviceId(mapAll.get("deviceId"));
        tCfgTelemeter.setMeteId(mapAll.get("meteId"));
        tCfgTelemeter.setMeteName(mapAll.get("meteName"));
        tCfgTelemeter.setUpEffect(Float.valueOf(mapAll.get("upEffect")));
        tCfgTelemeter.setLowEffect(Float.valueOf(mapAll.get("lowEffect")));
        tCfgTelemeter.setMeteCode(mapAll.get("meteCode"));
        tCfgTelemeter.setDeviceType(mapAll.get("deviceType"));
        tCfgTelemeter.setMetePrecision(Integer.valueOf(mapAll.get("metePrecision")));
        tCfgTelemeter.setUnit(mapAll.get("unit"));
        tCfgTelemeter.setChangeLimit(Float.valueOf(mapAll.get("changeLimit")));
        tCfgTelemeter.setStander(Float.valueOf(mapAll.get("stander")));
        tCfgTelemeter.setHilimit1(Float.valueOf(mapAll.get("hilimit1")));
        tCfgTelemeter.setHilimit2(Float.valueOf(mapAll.get("hilimit2")));
        tCfgTelemeter.setHilimit3(Float.valueOf(mapAll.get("hilimit3")));
        tCfgTelemeter.setHilimit4(Float.valueOf(mapAll.get("hilimit4")));
        tCfgTelemeter.setLolimit1(Float.valueOf(mapAll.get("lolimit1")));
        tCfgTelemeter.setLolimit2(Float.valueOf(mapAll.get("lolimit2")));
        tCfgTelemeter.setLolimit3(Float.valueOf(mapAll.get("lolimit3")));

        return tCfgDeviceDao.insertIntoTelemeter(tCfgTelemeter);
    }

    @Logs(title = "摇控", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insertIntoTelecontrol(Map<String,String> mapAll){

        TCfgTelecontrol tCfgTelecontrol =new TCfgTelecontrol();
        tCfgTelecontrol.setDeviceId(mapAll.get("deviceId"));
        tCfgTelecontrol.setMeteId(mapAll.get("meteId"));
        tCfgTelecontrol.setMeteName(mapAll.get("meteName"));
        tCfgTelecontrol.setMeteCode(mapAll.get("meteCode"));
        tCfgTelecontrol.setDeviceType(mapAll.get("deviceType"));
        tCfgTelecontrol.setDescriber(mapAll.get("describer"));


        return tCfgDeviceDao.insertIntoTelecontrol(tCfgTelecontrol);
    }
}

