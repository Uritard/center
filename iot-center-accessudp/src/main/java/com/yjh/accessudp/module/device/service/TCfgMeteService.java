package com.yjh.accessudp.module.device.service;

import com.yjh.accessudp.module.device.dao.TCfgMeteDao;
import com.yjh.accessudp.module.device.entity.SYAllInfo;
import com.yjh.accessudp.module.device.entity.TCfgDataCurrent;
import com.yjh.accessudp.module.device.entity.THisSignalData;
import com.yjh.accessudp.module.device.entity.THisTelemeterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        for (SYAllInfo syAllInfo:syAllInfoList) {
            this.tCfgMeteDao.insertForMete(syAllInfo);//mete
            this.tCfgMeteDao.insertForDevice(syAllInfo);//device
            if(syAllInfo.getMeteKind() == 1){//遥信
                tCfgMeteDao.insertForTelesignal(syAllInfo);
            }
            if(syAllInfo.getMeteKind() == 2){//遥测
                tCfgMeteDao.insertForTelemeter(syAllInfo);
            }
            if(syAllInfo.getMeteKind() == 3){//遥控
                tCfgMeteDao.insertForTelecontrol(syAllInfo);
            }
            tCfgMeteDao.insertForTeleadjust(syAllInfo);//遥调
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
            tCfgMeteDao.updateForTeleadjust(syAllInfo);//遥调
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
    public int deleteAll(){
        int i = 0;
        i = i+tCfgMeteDao.deleteForDeviceAll();
        i = i+tCfgMeteDao.deleteForMeteAll();
        i = i+tCfgMeteDao.deleteForTeleadjustAll();
        i = i+tCfgMeteDao.deleteForTelecontrolAll();
        i = i+tCfgMeteDao.deleteForTelemeterAll();
        i = i+tCfgMeteDao.deleteForTelesignalAll();
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

}

