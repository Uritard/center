package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.TAlgorithmConfBakDao;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;

import java.math.BigDecimal;
import java.util.*;

import com.yjh.platform.module.task.dao.TCruiseTypeDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.entity.TDictBusiness;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger log = LoggerFactory.getLogger(TStdDevicemeteService.class);
    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private TStdDeviceDao tStdDeviceDao;
    @Autowired
    private TDictBusinessDao tDictBusinessDao;
    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;
    @Autowired
    private TCruisePlanAttrDao tCruisePlanAttrDao;
    @Autowired
    private TAlgorithmConfBakDao tAlgorithmConfBakDao;
    @Autowired
    private TCruiseTypeDao tCruiseTypeDao;


    @Logs(title = "插入", code = "device",content = "根据页面传入的参数新增数据")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdDeviceMeteDetail tStdDeviceMeteDetail) {


        //若插入的测点中的deviceId、customId存在于device表中，则不更新device表；否则进行更新
        TStdDevice tStdDevice=tStdDeviceDao.selectByUnionKeys(tStdDeviceMeteDetail.getDeviceId(),tStdDeviceMeteDetail.getCustomType());//查看当前设备ID和部位ID下的设备信息
        if(Objects.isNull(tStdDevice)){
            TStdDevice stdDevice=tStdDeviceDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceId());
            stdDevice.setCustomId(tStdDeviceMeteDetail.getCustomType());
            List<TDictBusiness> list = tDictBusinessDao.select(null,tStdDeviceMeteDetail.getCustomType(),null,null,null,null,null);
            stdDevice.setCustomName(list.get(0).getDictNote());
            tStdDeviceDao.add(stdDevice);
            //测点中不存在本体部位 且 新增的测点部位不为本体 则删除之前设备的本体部位
//            if(tStdDevicemeteDao.selectByDevCus(tStdDeviceMeteDetail.getDeviceId(),"101").size()==0 && !(tStdDeviceMeteDetail.getCustomType().equals("101"))){
//                tStdDeviceDao.deleteByUnionKeys(tStdDeviceMeteDetail.getDeviceId(),"101");  //删除之前本体部位的设备
//            }
        }
        this.tStdDevicemeteDao.add(tStdDeviceMeteDetail);
        //配置算法
        if(tStdDeviceMeteDetail.getAnalyseType() != null){
            TAlgorithmInfo tAlgorithmInfo = tAlgorithmConfBakDao.selectByAnalyseType(tStdDeviceMeteDetail.getAnalyseType());
            TAlgorithmConfBak tAlgorithmConfBak = new TAlgorithmConfBak();
            if(tAlgorithmInfo != null){
                tAlgorithmConfBak.setAlgorithmId(tAlgorithmInfo.getAlgorithmId());
                tAlgorithmConfBak.setDeviceMeteId(tStdDeviceMeteDetail.getDeviceMeteId());
                tAlgorithmConfBakDao.add(tAlgorithmConfBak);
            }
        }
        return  1;
    }

    @Logs(title = "删除", code = "device",content = "根据页面传入的参数删除数据")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceMeteId) {
        //删除测点配置的算法
        tAlgorithmConfBakDao.deleteByPrimaryId(deviceMeteId);
//        TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(deviceMeteId);
//        Long deviceId=tStdDeviceMete.getDeviceId();
//        String customId=tStdDeviceMete.getCustomId();
        this.tStdDevicemeteDao.deleteByPrimaryId(deviceMeteId);//删除当前标准测点

//        if((tStdDevicemeteDao.selectDeviceMeteByDeviceCustom(deviceId,customId)).size()==0){//判断是否存在与被删除测点相同设备Id和部位Id的测点
//
//            if(tStdDevicemeteDao.selectByDevId(deviceId).size()==0){ //判断当前设备下是否有测点，若不存在则清空device并新增一个本体部位设备
//                TStdDevice tStdDevice=tStdDeviceDao.selectByPrimaryId(deviceId);
//                tStdDevice.setCustomId("101");
//                tStdDevice.setCustomName("本体");
//                tStdDeviceDao.deleteByUnionKeys(deviceId,customId);
//                tStdDeviceDao.add(tStdDevice);
//            }else {                                               //若存在则删除与当前测点关联的设备信息
//                tStdDeviceDao.deleteByUnionKeys(deviceId,customId);
//            }
//
//        }
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        //tCruisePointInstance.setDeviceMeteId(deviceMeteId);
        List<Long> haveList = tStdDevicemeteDao.selectHave(deviceMeteId);
        //删除关联的表
        if(haveList != null && haveList.size()>0){
            tCruisePointInstanceDao.deleteByInstanceId(haveList);//删除巡检点
            tCruisePlanAttrDao.deleteByInstanceId(haveList);
            //tCruiseTaskAttrDao.deleteByInstanceId(haveList);
            tCruiseTypeDao.deleteForInstanceId(haveList);
        }
        return this.tStdDevicemeteDao.deleteByPrimaryId(deviceMeteId);//删除测点
    }

    @Logs(title = "更新", code = "device",content = "根据页面传入的参数更新数据")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDeviceMeteDetail tStdDeviceMeteDetail) {
        //修改测点配置的算法
        //配置算法
        if(tStdDeviceMeteDetail.getAnalyseType() != null){
            TAlgorithmConfBak tAlgorithmConfBak = tAlgorithmConfBakDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceMeteId());
            if(tAlgorithmConfBak == null){
                tAlgorithmConfBak = new TAlgorithmConfBak();
                TAlgorithmInfo tAlgorithmInfo = tAlgorithmConfBakDao.selectByAnalyseType(tStdDeviceMeteDetail.getAnalyseType());
                if(tAlgorithmInfo != null){
                    tAlgorithmConfBak.setAlgorithmId(tAlgorithmInfo.getAlgorithmId());
                    tAlgorithmConfBak.setDeviceMeteId(tStdDeviceMeteDetail.getDeviceMeteId());
                    tAlgorithmConfBakDao.add(tAlgorithmConfBak);
                }

            }else {
                TAlgorithmInfo tAlgorithmInfo = tAlgorithmConfBakDao.selectByAnalyseType(tStdDeviceMeteDetail.getAnalyseType());
                if(tAlgorithmInfo != null){
                    tAlgorithmConfBak.setAlgorithmId(tAlgorithmInfo.getAlgorithmId());
                    tAlgorithmConfBak.setDeviceMeteId(tStdDeviceMeteDetail.getDeviceMeteId());
                    tAlgorithmConfBakDao.update(tAlgorithmConfBak);
                }
            }
        }else {
            TAlgorithmConfBak tAlgorithmConfBak = tAlgorithmConfBakDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceMeteId());
            if(tAlgorithmConfBak != null){
                tAlgorithmConfBakDao.deleteByPrimaryId(tAlgorithmConfBak.getDeviceMeteId());
            }
        }
        TStdDevice tStdDevice=tStdDeviceDao.selectByUnionKeys(tStdDeviceMeteDetail.getDeviceId(),tStdDeviceMeteDetail.getCustomType());//查询新增的部位设备是否存在
        if(Objects.isNull(tStdDevice)){
            //新增没有的 修改的部位 的设备
            TStdDevice stdDevice=tStdDeviceDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceId());
            stdDevice.setCustomId(tStdDeviceMeteDetail.getCustomType());
            List<TDictBusiness> list = tDictBusinessDao.select(null,tStdDeviceMeteDetail.getCustomType(),null,null,null,null,null);
            stdDevice.setCustomName(list.get(0).getDictNote());
            tStdDeviceDao.add(stdDevice);
            //对修改之前的测点对应的部位设备进行判断与操作
            TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceMeteId());//查询修改之前该标准测点的信息
            log.info("查询修改之前该标准测点的信息");
            List<TStdDeviceMete> tStdDeviceMetes=tStdDevicemeteDao.selectByDevCus(tStdDeviceMete.getDeviceId(),tStdDeviceMete.getCustomId());
            log.info("相同deviceID和customId下有多少点");
            if(tStdDeviceMetes.size()<=1){ //如果不存在其他 相同deviceId和customId的测点就删除该设备、否则保留
                tStdDeviceDao.deleteByUnionKeys(tStdDeviceMete.getDeviceId(),tStdDeviceMete.getCustomId());  //删除之前部位的设备
            }
        }else {
            TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceMeteId());//查询修改之前该标准测点的信息
            if(!(tStdDeviceMeteDetail.getCustomType().equals(tStdDeviceMete.getCustomId()))){
                List<TStdDeviceMete> tStdDeviceMetes=tStdDevicemeteDao.selectByDevCus(tStdDeviceMete.getDeviceId(),tStdDeviceMete.getCustomId());
                if(tStdDeviceMetes.size()<=1 || tStdDeviceMetes.size()==1){ //如果不存在其他 相同deviceId和customId的测点就删除该设备、否则保留
                    tStdDeviceDao.deleteByUnionKeys(tStdDeviceMete.getDeviceId(),tStdDeviceMete.getCustomId());  //删除之前部位的设备
                }
            }
            
        }
        return this.tStdDevicemeteDao.update(tStdDeviceMeteDetail);
    }

    @Logs(title = "主键查询", code = "device",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceMete selectByPrimaryId(Long deviceMeteId) {
        return this.tStdDevicemeteDao.selectByPrimaryId(deviceMeteId);
    }

    @Logs(title = "查询", code = "device",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> select(Long deviceMeteId, Long deviceId,String cusomId,Long meteId, String meteKind,String meteType, String meteName, Integer deviceType,  String positionType,Integer analyseType, String unit, String alarmNote, String alarmType, Float upEffect, Float downEffect, Integer alarmLevel, Float highLimit1, Float lowLimit1, Float highLimit2, Float lowLimit2, Integer alarmDelay, Integer alarmCnt, BigDecimal thresholdAbs, BigDecimal thresholdPer, Integer modulus, String remark,String stateZero,String stateOne,Integer  alarmState) {
        List<TStdDeviceMete> tStdDeviceMeteList = tStdDevicemeteDao.select(deviceMeteId, deviceId,cusomId, meteId, meteKind, meteType,meteName, deviceType,  positionType,analyseType, unit, alarmNote, alarmType, upEffect, downEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark,stateZero,stateOne,alarmState);

        return tStdDeviceMeteList;
    }

    //告警规则未定
    @Logs(title = "分页查询", code = "device",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMeteDetail> selectByPage(TStdDeviceMeteDetail tStdDeviceMeteDetail) {
//        if(tStdDeviceMeteDetail.getDeviceId() == null) {
//            if (ids.size() != 0) {
//                tStdDeviceMeteDetail.setIds(ids);
//            } else {
//                ids.add(tStdDeviceMeteDetail.getUpRegionId());
//                tStdDeviceMeteDetail.setIds(ids);
//            }
//        }else {
//            //ids.clear();
//            ids = new LinkedList<>();
//            ids.add(tStdDeviceMeteDetail.getDeviceId());
//            tStdDeviceMeteDetail.setIds(ids);
//            tStdDeviceMeteDetail.setUpRegionId(Long.valueOf(1));
//        }
        return tStdDevicemeteDao.selectByPage(tStdDeviceMeteDetail);
    }

    @Logs(title = "批量插入", code = "device",content = "根据页面传入的参数批量插入数据")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdDeviceMete> list) {
        return this.tStdDevicemeteDao.batchAdd(list);
    }

    @Logs(title = "根据设备模版ID查询对应测点", code = "device",content = "根据页面传入的参数据设备模版ID查询对应测点")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectDevMeteByModelId(Long modelId) {
        return tStdDevicemeteDao.selectDevMeteByModelId(modelId);
    }

    @Logs(title = "新增/修改设备测点", code = "device",content = "根据页面传入的参数修改数据")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDevMete(List<TStdDeviceMete> list) {
        Long deviceId = list.get(0).getDeviceId();
        tStdDevicemeteDao.deleteByDevId(deviceId);
        return this.tStdDevicemeteDao.batchAdd(list);
    }

    @Logs(title = "根据设备Id删除设备测点", code = "device",content = "根据页面传入的参数根据设备Id删除设备测点")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByDevId(Long deviceId) {
        return this.tStdDevicemeteDao.deleteByDevId(deviceId);
    }

    @Logs(title = "设备ID与部位ID查询设备测点", code = "device",content = "根据页面传入的参数查询设备ID与部位ID查询设备测点")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectByDevCus(Long deviceId,String customType) {
        return this.tStdDevicemeteDao.selectByDevCus(deviceId, customType);
    }


    @Logs(title = "查询生成预定义模板测点信息表",code = "device",content = "根据页面传入的参数查询查询生成预定义模板测点信息表")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectPreDeviceMete(Long modelId,Long deviceId,Long customType){
        return this.tStdDevicemeteDao.selectPreDeviceMete(modelId,  deviceId, customType);
    }


    @Logs(title = "批量删除", code = "device",content = "根据页面传入的参数批量删除数据")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String list) {
        int deleteCount=0;
        List<String> list1= Arrays.asList(list.split(","));
        for (String item:list1) {
            tAlgorithmConfBakDao.deleteByPrimaryId(Long.valueOf(item));
            List<Long> haveList = tStdDevicemeteDao.selectHave(Long.valueOf(item));
            tCruisePointInstanceDao.deleteByDeviceMeteId(Long.valueOf(item));//遍历删除巡检点
            //删除关联的表
            if(haveList != null && haveList.size()>0){
                tCruisePlanAttrDao.deleteByInstanceId(haveList);
                //tCruiseTaskAttrDao.deleteByInstanceId(haveList);
                tCruiseTypeDao.deleteForInstanceId(haveList);
            }

//            TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(Long.valueOf(item));//根据devicemeteId查测点
//            Long deviceId=tStdDeviceMete.getDeviceId();
//            String customId=tStdDeviceMete.getCustomId();
            deleteCount=tStdDevicemeteDao.deleteByPrimaryId(Long.valueOf(item))+deleteCount;

//            if((tStdDevicemeteDao.selectDeviceMeteByDeviceCustom(deviceId,customId)).size()==0){
//                tStdDeviceDao.deleteByUnionKeys(deviceId,customId);
//            }

        }
        return deleteCount;//批量删除标准测点
    }


}

