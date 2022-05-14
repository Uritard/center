package com.yjh.accessudp.module.device.service;

import com.yjh.accessudp.module.device.dao.TCfgMeteDao;
import com.yjh.accessudp.module.device.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @author lqh
* @since 2020-08-25
*/
@Service
public class TCfgMeteService {

    @Autowired
    private TCfgMeteDao tCfgMeteDao;


    @Transactional(rollbackFor = Exception.class)
    public int insertForAll(List<SYAllInfo> syAllInfoList) {
        List<SYAllInfo> list1 = new ArrayList<>();
        List<SYAllInfo> list2 = new ArrayList<>();
        List<SYAllInfo> list3 = new ArrayList<>();
        List<SYAllInfo> list4 = new ArrayList<>();
        this.tCfgMeteDao.insertForMete(syAllInfoList);//mete
        this.tCfgMeteDao.insertForDevice(syAllInfoList);//device
        for (SYAllInfo syAllInfo:syAllInfoList) {
            if(syAllInfo.getMeteKind() == 1){//遥信
                list1.add(syAllInfo);
                //tCfgMeteDao.insertForTelesignal(syAllInfo);
                continue;
            }
            if(syAllInfo.getMeteKind() == 2){//遥测
                list2.add(syAllInfo);
                //tCfgMeteDao.insertForTelemeter(syAllInfo);
                continue;
            }
            if(syAllInfo.getMeteKind() == 3){//遥控
                list3.add(syAllInfo);
                //tCfgMeteDao.insertForTelecontrol(syAllInfo);
                continue;
            }
            if(syAllInfo.getMeteKind() == 4){//遥调
                list4.add(syAllInfo);
                //tCfgMeteDao.insertForTeleadjust(syAllInfo);//遥调
                continue;
            }
            //tCfgMeteDao.insertForTeleadjust(syAllInfo);//遥调
        }
        if(list1.size()>0){
            tCfgMeteDao.insertForTelesignal(list1);
        }
        if(list2.size()>0){
            tCfgMeteDao.insertForTelemeter(list2);
        }
        if(list3.size()>0){
            tCfgMeteDao.insertForTelecontrol(list3);
        }
        if(list4.size()>0){
            tCfgMeteDao.insertForTeleadjust(list4);//遥调
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateForAll(SYAllInfo syAllInfo) {
            this.tCfgMeteDao.updateForMete(syAllInfo);//mete
            this.tCfgMeteDao.updateForDevice(syAllInfo);//device
            if(syAllInfo.getMeteKind() == 1){//遥信
                tCfgMeteDao.updateForTelesignal(syAllInfo);
            }
            if(syAllInfo.getMeteKind() == 2){//遥测
                tCfgMeteDao.updateForTelemeter(syAllInfo);
            }
            if(syAllInfo.getMeteKind() == 3){//遥控
                tCfgMeteDao.updateForTelecontrol(syAllInfo);
            }
        if(syAllInfo.getMeteKind() == 4){//遥控
            tCfgMeteDao.updateForTeleadjust(syAllInfo);//遥调
        }
            //tCfgMeteDao.updateForTeleadjust(syAllInfo);//遥调
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public String selectByMeteId(String meteId){
        return tCfgMeteDao.selectByMeteId(meteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCfgDataCurrent selectByPrimaryIdTCfgDataCurrent(String meteId){
        return tCfgMeteDao.selectByPrimaryIdTCfgDataCurrent(Long.valueOf(meteId));
    }
    @Transactional(rollbackFor = Exception.class)
    public int insertTCfgDataCurrent(TCfgDataCurrent tCfgDataCurrent){
        return tCfgMeteDao.insertTCfgDataCurrent(tCfgDataCurrent);
    }
    @Transactional(rollbackFor = Exception.class)
    public int updateTCfgDataCurrent(TCfgDataCurrent tCfgDataCurrent){
        return tCfgMeteDao.updateTCfgDataCurrent(tCfgDataCurrent);
    }
    @Transactional(rollbackFor = Exception.class)
    public int deleteAll(List<SYAllInfo> list){
        int i = 0;
        i = i+tCfgMeteDao.deleteForDeviceAll(list);
        i = i+tCfgMeteDao.deleteForMeteAll(list);
        i = i+tCfgMeteDao.deleteForTeleadjustAll(list);
        i = i+tCfgMeteDao.deleteForTelecontrolAll(list);
        i = i+tCfgMeteDao.deleteForTelemeterAll(list);
        i = i+tCfgMeteDao.deleteForTelesignalAll(list);
        return i;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertIntoHis(TCfgDataCurrent tCfgDataCurrent){
        if(tCfgDataCurrent.getMeteKind() == 2){
            THisTelemeterData tHisTelemeterData = new THisTelemeterData();
            tHisTelemeterData.setMeteId(tCfgDataCurrent.getMeteId());
            tHisTelemeterData.setDeviceId(tCfgDataCurrent.getDeviceId());
            tHisTelemeterData.setRecordTime(tCfgDataCurrent.getRecordTime());
            tHisTelemeterData.setMeteKind(tCfgDataCurrent.getMeteKind());
            tHisTelemeterData.setMeteValue(tCfgDataCurrent.getMeteValue());
            tHisTelemeterData.setLastMeteValue(tCfgDataCurrent.getLastMeteValue());
            return tCfgMeteDao.insertIntoTHisTelemeterData(tHisTelemeterData);
        }
        THisSignalData tHisSignalData = new THisSignalData();
        tHisSignalData.setMeteId(tCfgDataCurrent.getMeteId());
        tHisSignalData.setDeviceId(tCfgDataCurrent.getDeviceId());
        tHisSignalData.setRecordTime(tCfgDataCurrent.getRecordTime());
        tHisSignalData.setMeteKind(tCfgDataCurrent.getMeteKind());
        tHisSignalData.setMeteValue(tCfgDataCurrent.getMeteValue());
        tHisSignalData.setLastMeteValue(tCfgDataCurrent.getLastMeteValue());
        return tCfgMeteDao.insertIntoTHisSignalData(tHisSignalData);
    }
    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByParamType(String paramType){
        return this.tCfgMeteDao.selectByParamType(paramType);
    }

}

