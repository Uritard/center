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

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgDevice tCfgDevice) {
        return this.tCfgDeviceDao.insert(tCfgDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String deviceId) {
        return this.tCfgDeviceDao.deleteByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgDevice tCfgDevice) {
        return this.tCfgDeviceDao.update(tCfgDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCfgDevice selectByPrimaryId(String deviceId) {
        return this.tCfgDeviceDao.selectByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDevice> select(String deviceId, String deviceName, String deviceType, String deviceCode, String stationId, String relationCode, Date createTime, Date updateTime, String remark) {
        List<TCfgDevice> tCfgDeviceList = tCfgDeviceDao.select(deviceId, deviceName, deviceType, deviceCode, stationId, relationCode, createTime, updateTime, remark);
        return tCfgDeviceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<HashMap<String,Object>> selectByPage(TCfgDeviceDetail tCfgDeviceDetail) {
        if ("-1".equals(tCfgDeviceDetail.getMeteKind())){
            tCfgDeviceDetail.setMeteKind(null);
        }
        List<HashMap<String,Object>> list= tCfgDeviceDao.selectByPage(tCfgDeviceDetail);
        return list;
    }


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
        if(mapAll.get("meteKind").equals("1")){
            return this.insertIntoTelesignal(mapAll);
        }
        if(mapAll.get("meteKind").equals("2")){
            return this.insertIntoTelemeter(mapAll);
        }
        if(mapAll.get("meteKind").equals("3")){
            return this.insertIntoTelecontrol(mapAll);
        }
        if(mapAll.get("meteKind").equals("4")){
            return this.insertIntoTeleadjust(mapAll);
        }
        return 0;
    }



    @Transactional(rollbackFor = Exception.class)
    public int insertIntoTeleadjust(Map<String,String> mapAll){
        TCfgTeleadjust tCfgTeleadjust = new TCfgTeleadjust();
        tCfgTeleadjust.setDeviceId(mapAll.get("deviceId"));
        tCfgTeleadjust.setMeteId(mapAll.get("meteId"));
        tCfgTeleadjust.setMeteName(mapAll.get("meteName"));
        tCfgTeleadjust.setMeteCode(mapAll.get("meteCode"));
        tCfgTeleadjust.setUpEffect(Float.valueOf(mapAll.get("upEffect")));
        tCfgTeleadjust.setDownEffect(Float.valueOf(mapAll.get("downEffect")));
        tCfgTeleadjust.setMetePrecision(Integer.valueOf(mapAll.get("metePrecision")));
        tCfgTeleadjust.setUnit(mapAll.get("unit"));
        tCfgTeleadjust.setDeviceType(mapAll.get("deviceType"));
        tCfgTeleadjust.setStander(Float.valueOf(mapAll.get("stander")));
        tCfgTeleadjust.setControlenable(Integer.valueOf(mapAll.get("controlenable")));

        return tCfgDeviceDao.insertIntoTeleadjust(tCfgTeleadjust);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertIntoTelesignal(Map<String,String> mapAll){

        TCfgTelesignal tCfgTelesignal = new TCfgTelesignal();
        tCfgTelesignal.setDeviceId(mapAll.get("deviceId"));
        tCfgTelesignal.setMeteId(mapAll.get("meteId"));
        tCfgTelesignal.setMeteName(mapAll.get("meteName"));
        tCfgTelesignal.setUpEffect(Integer.valueOf(mapAll.get("upEffect")));
        tCfgTelesignal.setDownEffect(Integer.valueOf(mapAll.get("downEffect")));
        tCfgTelesignal.setMeteCode(mapAll.get("meteCode"));
        tCfgTelesignal.setDeviceType(mapAll.get("deviceType"));
        tCfgTelesignal.setAlarmthresbhold(Integer.valueOf(mapAll.get("alarmthresbhold")));
        tCfgTelesignal.setAlarmlevel(Integer.valueOf(mapAll.get("alarmlevel")));
        tCfgTelesignal.setDescriber(mapAll.get("describer"));

        return tCfgDeviceDao.insertIntoTelesignal(tCfgTelesignal);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertIntoTelemeter(Map<String,String> mapAll){

        TCfgTelemeter tCfgTelemeter = new TCfgTelemeter();
        tCfgTelemeter.setDeviceId(mapAll.get("deviceId"));
        tCfgTelemeter.setMeteId(mapAll.get("meteId"));
        tCfgTelemeter.setMeteName(mapAll.get("meteName"));
        tCfgTelemeter.setUpEffect(Float.valueOf(mapAll.get("upEffect")));
        tCfgTelemeter.setDownEffect(Float.valueOf(mapAll.get("downEffect")));
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

