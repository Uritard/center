package com.yjh.accessudp.module.device.service;

import com.yjh.accessudp.module.device.dao.TCfgMeteDao;
import com.yjh.accessudp.module.device.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @author lqh
* @since 2020-08-25
*/
@Service
@Slf4j
public class TCfgMeteService {

    @Autowired
    private TCfgMeteDao tCfgMeteDao;


    /**
     * 处理信号数据
     * @param meteId  联动点位编码 对应主辅系统监控索引号
     * @param meteKind 信号类型 0，遥控；1，遥信；2，遥测
     * @param value 属性
     * @param commit 值描述
     * @param time 事件时标
     */
    @Transactional(rollbackFor = Exception.class)
    public int dealLinkageSignal(Integer meteId, Integer meteKind, String value, String commit, String time){
        try {
            if(meteKind == 0){
                //吧同步过来的遥控0 改为我们的遥控3
                meteKind = 3;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TCfgDataCurrent tCfgDataCurrent = this.selectByPrimaryIdTCfgDataCurrent(meteId.toString());
            if (tCfgDataCurrent == null) {
                tCfgDataCurrent = new TCfgDataCurrent();
                tCfgDataCurrent.setMeteId(Long.valueOf(meteId));
                tCfgDataCurrent.setDeviceId(Long.valueOf(this.selectByMeteId(meteId.toString())));
                tCfgDataCurrent.setMeteKind(meteKind);
                //tCfgDataCurrent.setRecordTime(sdf.parse(time));
                tCfgDataCurrent.setRecordTime(new Date());
                tCfgDataCurrent.setMeteComment(value);
                tCfgDataCurrent.setMeteValue(commit);
                this.insertTCfgDataCurrent(tCfgDataCurrent);
            } else {
                //库里已经有数据了
                tCfgDataCurrent.setLastMeteValue(tCfgDataCurrent.getMeteValue());
                tCfgDataCurrent.setRecordTime(sdf.parse(time));
                tCfgDataCurrent.setMeteComment(value);
                tCfgDataCurrent.setMeteValue(commit);
                this.updateTCfgDataCurrent(tCfgDataCurrent);
            }
//            Map<String, String> map = new HashMap<>();
//            map.put("meteId",meteId.toString());
//            union(map);
            //将实时表里的数据更新到历史表里
            this.insertIntoHis(tCfgDataCurrent);
        }catch (Exception e){
            log.error("联动信号数据处理失败！", e);
        }
        return 1;
    }
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

