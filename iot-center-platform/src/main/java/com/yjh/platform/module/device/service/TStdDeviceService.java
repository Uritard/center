package com.yjh.platform.module.device.service;

import com.google.common.collect.Lists;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.dao.*;
import com.yjh.platform.module.device.entity.*;

import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Function;
import java.util.stream.Collectors;


import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TDictBusiness;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-07-27
*/
@Service
@Slf4j
public class TStdDeviceService{

    @Autowired
    private TStdDeviceDao tStdDeviceDao;
    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Autowired
    private TStdDeviceAttrDao tStdDeviceAttrDao;
    @Autowired
    private TDictBusinessDao tDictBusinessDao;
    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;
    @Autowired
    private TStdMetemodelDetailDao tStdMetemodelDetailDao;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private TStdDevicemeteService tStdDevicemeteService;
    @Autowired
    private TCameraInfoDao tCameraInfoDao;
    @Autowired
    private TDeviceMaintenanceDao tDeviceMaintenanceDao;
    @Autowired
    private UPatrolTaskService uPatrolTaskService;
    @Autowired
    private RedisTemplate redisTemplate;


    @Transactional(rollbackFor = Exception.class)
    public TStdDevice selectByUnionKeys(Long deviceId){
        return tStdDeviceDao.selectByUnionKeys(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int add(TStdDevice tStdDevice) {
//        if(tStdDevice.getCustomId() == null){
//            String customId = defaultPart();
//            tStdDevice.setCustomId(customId);
//            List<TDictBusiness> list = tDictBusinessDao.select(null,customId,null,null,null,null,null);
//            tStdDevice.setCustomName(list.get(0).getDictNote());
//        }
        return this.tStdDeviceDao.add(tStdDevice);
    }

    // 随机找出一个部位充当默认部位
    private String defaultPart(){
        return tStdDeviceDao.defaultPart();
    }


//    @Transactional(rollbackFor = Exception.class)
//    public int addALL(TStdDeviceDetail tStdDeviceDetail) {
//
//        TStdDevice tStdDevice = new TStdDevice();
//        tStdDevice.setAliasName(tStdDeviceDetail.getAliasName());
//        tStdDevice.setCustomId(tStdDeviceDetail.getCustomId());
//        if(tStdDevice.getCustomId() == null){
//            //不传 设为本体，根据字典表查，暂定为101
//            tStdDevice.setCustomId("101");
//            List<TDictBusiness> list = tDictBusinessDao.select(null,"101",null,null,null,null,null);
//            tStdDevice.setCustomName(list.get(0).getDictNote());
//        }else{
//            tStdDevice.setCustomId(tStdDeviceDetail.getCustomId());
//            List<TDictBusiness> list = tDictBusinessDao.select(null,tStdDeviceDetail.getCustomId(),null,null,null,null,null);
//            tStdDevice.setCustomName(list.get(0).getDictNote());
//        }
//        tStdDevice.setCustomType(tStdDeviceDetail.getCustomType());
//        tStdDevice.setDeviceCode(tStdDeviceDetail.getDeviceCode());
//        tStdDevice.setDeviceId(tStdDeviceDetail.getDeviceId());
//        tStdDevice.setDeviceName(tStdDeviceDetail.getDeviceName());
//        tStdDevice.setDeviceType(tStdDeviceDetail.getDeviceType());
//        tStdDevice.setModelId(tStdDeviceDetail.getModelId());
//        tStdDevice.setPositionType(tStdDeviceDetail.getPositionType());
//        tStdDevice.setRegionPath(tStdDeviceDetail.getRegionPath());
//        tStdDevice.setStatus(tStdDeviceDetail.getStatus());
//        tStdDevice.setUpdateTime(tStdDeviceDetail.getUpdateTime());
//        tStdDevice.setUpRegionId(tStdDeviceDetail.getUpRegionId());
//        tStdDevice.setUpRegionName(tStdDeviceDetail.getUpRegionName());
//        tStdDevice.setPresetId(tStdDeviceDetail.getPresetId());
//        tStdDevice.setCameraId(tStdDeviceDetail.getCameraId());
//        this.tStdDeviceDao.add(tStdDevice);
//        Long deivceIdUnique=tStdDevice.getDeviceId();
//        List<TStdMeteModelDetail> tStdMeteModelDetailList = tStdMetemodelDetailDao.selectByPrimaryId(tStdDeviceDetail.getModelId());//根据模板ID查询测点模板
//        for (TStdMeteModelDetail tStdMeteModelDetailItem: tStdMeteModelDetailList) {
//            tStdMeteModelDetailItem.setDeviceId(deivceIdUnique);//测点模板结合设备ID形成标准测点
//            tStdDevicemeteDao.add(tStdMeteModelDetailItem);
//            if(Objects.isNull(tStdDeviceDao.selectByUnionKeys(tStdMeteModelDetailItem.getDeviceId(),tStdMeteModelDetailItem.getCustomType()))){//device表中没有新增设备的测点部位
//                TStdDevice stdDevice=new TStdDevice();
//                stdDevice.setDeviceId(deivceIdUnique);
//                stdDevice.setAliasName(tStdDeviceDetail.getAliasName());
//                stdDevice.setCreateTime(tStdDeviceDetail.getCreateTime());
//                stdDevice.setCustomId(tStdMeteModelDetailItem.getCustomType());
//                stdDevice.setCustomName(tStdMeteModelDetailItem.getCustomTypeName());
//                stdDevice.setCustomType(tStdDeviceDetail.getCustomType());
//                stdDevice.setDeviceCode(tStdDeviceDetail.getDeviceCode());
//                stdDevice.setDeviceName(tStdDeviceDetail.getDeviceName());
//                stdDevice.setDeviceType(tStdDeviceDetail.getDeviceType());
//                stdDevice.setModelId(tStdDeviceDetail.getModelId());
//                stdDevice.setPositionType(tStdDeviceDetail.getPositionType());
//                stdDevice.setRegionPath(tStdDeviceDetail.getRegionPath());
//                stdDevice.setStatus(tStdDeviceDetail.getStatus());
//                stdDevice.setUpdateTime(tStdDeviceDetail.getUpdateTime());
//                stdDevice.setUpRegionId(tStdDeviceDetail.getUpRegionId());
//                stdDevice.setUpRegionName(tStdDeviceDetail.getUpRegionName());
//                tStdDevice.setPresetId(tStdDeviceDetail.getPresetId());
//                tStdDevice.setCameraId(tStdDeviceDetail.getCameraId());
//                tStdDeviceDao.add(stdDevice);
//            }
//
//
//        }
//
//        TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
//        tStdDeviceAttr.setRealCode(tStdDeviceDetail.getRealCode());
//        tStdDeviceAttr.setDepartment(tStdDeviceDetail.getDepartment());
//        tStdDeviceAttr.setDeviceId(tStdDevice.getDeviceId());
//        tStdDeviceAttr.setDeviceModel(tStdDeviceDetail.getDeviceModel());
//        tStdDeviceAttr.setDisableDate(tStdDeviceDetail.getDisableDate());
//        tStdDeviceAttr.setIp(tStdDeviceDetail.getIp());
//        tStdDeviceAttr.setLastMaintenance(tStdDeviceDetail.getLastMaintenance());
//        tStdDeviceAttr.setLatitude(tStdDeviceDetail.getLatitude());
//        tStdDeviceAttr.setLongitude(tStdDeviceDetail.getLongitude());
//        tStdDeviceAttr.setMaintenanceCount(tStdDeviceDetail.getMaintenanceCount());
//        tStdDeviceAttr.setDeviceVendor(tStdDeviceDetail.getDeviceVendor());
//        tStdDeviceAttr.setUsedTime(tStdDeviceDetail.getUsedTime());
//        tStdDeviceAttr.setOrganization(tStdDeviceDetail.getOrganization());
//        tStdDeviceAttr.setVoltageLevel(tStdDeviceDetail.getVoltageLevel());
//        tStdDeviceAttr.setSequencePoint(tStdDeviceDetail.getSequencePoint());
//        tStdDeviceAttr.setResponsiblePerson(tStdDeviceDetail.getResponsiblePerson());
//        tStdDeviceAttr.setPmsId(tStdDeviceDetail.getPmsId());
//        tStdDeviceAttr.setPmsType(tStdDeviceDetail.getPmsType());
//        tStdDeviceAttr.setPort(tStdDeviceDetail.getPort());
//        tStdDeviceAttr.setProductionDate(tStdDeviceDetail.getProductionDate());
//        tStdDeviceAttr.setResponsiblePerson(tStdDeviceDetail.getResponsiblePerson());
//        tStdDeviceAttr.setAddress(tStdDeviceDetail.getAddress());
//        return tStdDeviceAttrDao.add(tStdDeviceAttr);
//    }
//
    @Transactional(rollbackFor = Exception.class)
    public int addALL(TStdDeviceDetail tStdDeviceDetail) {

        TStdDevice tStdDevice = new TStdDevice();
        tStdDevice.setRealCode(tStdDeviceDetail.getRealCode());
        tStdDevice.setAliasName(tStdDeviceDetail.getAliasName());
//        tStdDevice.setCustomId(tStdDeviceDetail.getCustomId());
//        if(tStdDevice.getCustomId() == null){
//            //不传 设为本体，根据字典表查，暂定为101
//            String customId = defaultPart();
//            tStdDevice.setCustomId(customId);
//            List<TDictBusiness> list = tDictBusinessDao.select(null,customId,null,null,null,null,null);
//            tStdDevice.setCustomName(list.get(0).getDictNote());
//        }else{
//            tStdDevice.setCustomId(tStdDeviceDetail.getCustomId());
//            List<TDictBusiness> list = tDictBusinessDao.select(null,tStdDeviceDetail.getCustomId(),null,null,null,null,null);
//            tStdDevice.setCustomName(list.get(0).getDictNote());
//        }
        tStdDevice.setCustomType(tStdDeviceDetail.getCustomType());
        tStdDevice.setDeviceCode(tStdDeviceDetail.getDeviceCode());
        tStdDevice.setDeviceId(tStdDeviceDetail.getDeviceId());
        tStdDevice.setDeviceName(tStdDeviceDetail.getDeviceName());
        tStdDevice.setDeviceType(tStdDeviceDetail.getDeviceType());
        tStdDevice.setModelId(tStdDeviceDetail.getModelId());
        tStdDevice.setPositionType(tStdDeviceDetail.getPositionType());
        tStdDevice.setRegionPath(tStdDeviceDetail.getRegionPath());
        tStdDevice.setStatus(tStdDeviceDetail.getStatus());
        tStdDevice.setUpdateTime(tStdDeviceDetail.getUpdateTime());
        tStdDevice.setUpRegionId(tStdDeviceDetail.getUpRegionId());
        tStdDevice.setUpRegionName(tStdDeviceDetail.getUpRegionName());
        tStdDevice.setPresetId(tStdDeviceDetail.getPresetId());
        tStdDevice.setCameraId(tStdDeviceDetail.getCameraId());
        this.tStdDeviceDao.add(tStdDevice);
        Long deivceIdUnique=tStdDevice.getDeviceId();

        //根据模板ID查询测点模板
        try {
            List<TStdMeteModelDetail> tStdMeteModelDetilList = tStdMetemodelDetailDao.selectByPrimaryId(tStdDeviceDetail.getModelId());
            List<List<TStdMeteModelDetail>> partitionList = Lists.partition(tStdMeteModelDetilList, 2000);
            //parties = 主线程+子线程
            final CyclicBarrier barrier = new CyclicBarrier(partitionList.size() + 1);
            partitionList.forEach(partition -> {
                new Thread(() -> {
                    try {
                        tStdDevicemeteDao.batchAddTStdMeteModelDetail(partition, deivceIdUnique);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        try {
                            barrier.await();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }).start();
            });
            barrier.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
        tStdDeviceAttr.setDepartment(tStdDeviceDetail.getDepartment());
        tStdDeviceAttr.setDeviceId(tStdDevice.getDeviceId());
        tStdDeviceAttr.setDeviceModel(tStdDeviceDetail.getDeviceModel());
        tStdDeviceAttr.setDisableDate(tStdDeviceDetail.getDisableDate());
        tStdDeviceAttr.setIp(tStdDeviceDetail.getIp());
        tStdDeviceAttr.setLastMaintenance(tStdDeviceDetail.getLastMaintenance());
        tStdDeviceAttr.setLatitude(tStdDeviceDetail.getLatitude());
        tStdDeviceAttr.setLongitude(tStdDeviceDetail.getLongitude());
        tStdDeviceAttr.setMaintenanceCount(tStdDeviceDetail.getMaintenanceCount());
        tStdDeviceAttr.setDeviceVendor(tStdDeviceDetail.getDeviceVendor());
        tStdDeviceAttr.setUsedTime(tStdDeviceDetail.getUsedTime());
        tStdDeviceAttr.setOrganization(tStdDeviceDetail.getOrganization());
        tStdDeviceAttr.setVoltageLevel(tStdDeviceDetail.getVoltageLevel());
        tStdDeviceAttr.setSequencePoint(tStdDeviceDetail.getSequencePoint());
        tStdDeviceAttr.setResponsiblePerson(tStdDeviceDetail.getResponsiblePerson());
        tStdDeviceAttr.setPmsId(tStdDeviceDetail.getPmsId());
        tStdDeviceAttr.setPmsType(tStdDeviceDetail.getPmsType());
        tStdDeviceAttr.setPort(tStdDeviceDetail.getPort());
        tStdDeviceAttr.setProductionDate(tStdDeviceDetail.getProductionDate());
        tStdDeviceAttr.setResponsiblePerson(tStdDeviceDetail.getResponsiblePerson());
        tStdDeviceAttr.setAddress(tStdDeviceDetail.getAddress());
        return tStdDeviceAttrDao.add(tStdDeviceAttr);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceId) {
        return this.tStdDeviceDao.deleteByPrimaryId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryIdALL(Long deviceId) {
        List<Long> devList = tStdDevicemeteDao.selectHaveInstanceId(deviceId);
        {//设备下存在测点
            if(devList != null && devList.size()>0){
             return -1;
            }
        }
        tStdDeviceAttrDao.deleteByPrimaryId(deviceId);//删除属性
        tStdDevicemeteService.deleteByDevId(deviceId);//删除测点

        tDeviceMaintenanceDao.deleteByDeviceId(deviceId);
        return this.tStdDeviceDao.deleteByPrimaryId(deviceId);//删除设备
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDevice tStdDevice) {
        return this.tStdDeviceDao.update(tStdDevice);
    }


    @Transactional(rollbackFor = Exception.class)
    public int updateAll(TStdDeviceDetail tStdDeviceDetail) {

        List<TStdDevice> tStdDevices=tStdDeviceDao.selectListByPrimaryId(tStdDeviceDetail.getDeviceId());
        for(TStdDevice tStdDevice:tStdDevices){
            tStdDevice.setRealCode(tStdDeviceDetail.getRealCode());
            tStdDevice.setAliasName(tStdDeviceDetail.getAliasName());
            tStdDevice.setCreateTime(tStdDeviceDetail.getCreateTime());
            tStdDevice.setDeviceCode(tStdDeviceDetail.getDeviceCode());
            tStdDevice.setDeviceName(tStdDeviceDetail.getDeviceName());
            tStdDevice.setUpdateTime(tStdDeviceDetail.getUpdateTime());
            tStdDevice.setPositionType(tStdDeviceDetail.getPositionType());
            tStdDevice.setRegionPath(tStdDeviceDetail.getRegionPath());
            tStdDevice.setStatus(tStdDeviceDetail.getStatus());
            tStdDevice.setPresetId(tStdDeviceDetail.getPresetId());
            tStdDevice.setCameraId(tStdDeviceDetail.getCameraId());
            tStdDeviceDao.update(tStdDevice);
        }
//        TStdDevice tStdDevice = tStdDeviceDao.selectByPrimaryId(tStdDeviceDetail.getDeviceId());
//        if(tStdDevice.getModelId() != tStdDeviceDetail.getModelId()){
//            tStdDeviceDao.deleteByPrimaryId(tStdDeviceDetail.getDeviceId());//删除所有设备
//            tStdDevicemeteDao.deleteByDevId(tStdDeviceDetail.getDeviceId());//删除该设备下所有测点
//
//            List<TStdMeteModelDetail> tStdMeteModelDetailList = tStdMetemodelDetailDao.selectByPrimaryId(tStdDeviceDetail.getModelId());//设备下的所有模板测点
//            for (TStdMeteModelDetail tStdMeteModelDetailItem: tStdMeteModelDetailList) {
//                tStdMeteModelDetailItem.setDeviceId(tStdDeviceDetail.getDeviceId());
//                tStdDevicemeteDao.add(tStdMeteModelDetailItem);//新增设备下模板对应的标准测点
//
//                if(Objects.isNull(tStdDeviceDao.selectByUnionKeys(tStdMeteModelDetailItem.getDeviceId(),tStdMeteModelDetailItem.getCustomType()))){
//                    TStdDevice stdDevice=new TStdDevice();
//                    stdDevice.setDeviceId(tStdDeviceDetail.getDeviceId());
//                    stdDevice.setAliasName(tStdDeviceDetail.getAliasName());
//                    stdDevice.setCreateTime(tStdDeviceDetail.getCreateTime());
//                    stdDevice.setCustomId(tStdMeteModelDetailItem.getCustomType());
//                    stdDevice.setCustomName(tStdMeteModelDetailItem.getCustomTypeName());
////                List<TDictBusiness> list = tDictBusinessDao.select(null,tStdMeteModelDetailItem.getCustomType(),null,null,null,null,null);
////                stdDevice.setCustomName(list.get(0).getDictNote());
//                    stdDevice.setCustomType(tStdDeviceDetail.getCustomType());
//                    stdDevice.setDeviceCode(tStdDeviceDetail.getDeviceCode());
//                    stdDevice.setDeviceName(tStdDeviceDetail.getDeviceName());
//                    stdDevice.setDeviceType(tStdDeviceDetail.getDeviceType());
//                    stdDevice.setModelId(tStdDeviceDetail.getModelId());
//                    stdDevice.setPositionType(tStdDeviceDetail.getPositionType());
//                    stdDevice.setRegionPath(tStdDeviceDetail.getRegionPath());
//                    stdDevice.setStatus(tStdDeviceDetail.getStatus());
//                    stdDevice.setUpdateTime(tStdDeviceDetail.getUpdateTime());
//                    stdDevice.setUpRegionId(tStdDeviceDetail.getUpRegionId());
//                    stdDevice.setUpRegionName(tStdDeviceDetail.getUpRegionName());
//                    tStdDeviceDao.add(stdDevice);
//                }
//
//            }
////        }
//
//        if(tStdDeviceDetail.getModelId() == null){
//            TStdDevice tStdDevice = new TStdDevice();
//            tStdDevice.setAliasName(tStdDeviceDetail.getAliasName());
//            tStdDevice.setCustomId(tStdDeviceDetail.getCustomId());
//            if(tStdDevice.getCustomId() == null){
//                //不传 设为本体，根据字典表查，暂定为101
//                tStdDevice.setCustomId("101");
//                List<TDictBusiness> list = tDictBusinessDao.select(null,"101",null,null,null,null,null);
//                tStdDevice.setCustomName(list.get(0).getDictNote());
//            }else{
//                tStdDevice.setCustomId(tStdDeviceDetail.getCustomId());
//                List<TDictBusiness> list = tDictBusinessDao.select(null,tStdDeviceDetail.getCustomId(),null,null,null,null,null);
//                tStdDevice.setCustomName(list.get(0).getDictNote());
//            }
//            tStdDevice.setCustomType(tStdDeviceDetail.getCustomType());
//            tStdDevice.setDeviceCode(tStdDeviceDetail.getDeviceCode());
//            tStdDevice.setDeviceId(tStdDeviceDetail.getDeviceId());
//            tStdDevice.setDeviceName(tStdDeviceDetail.getDeviceName());
//            tStdDevice.setDeviceType(tStdDeviceDetail.getDeviceType());
//            tStdDevice.setModelId(tStdDeviceDetail.getModelId());
//            tStdDevice.setPositionType(tStdDeviceDetail.getPositionType());
//            tStdDevice.setRegionPath(tStdDeviceDetail.getRegionPath());
//            tStdDevice.setStatus(tStdDeviceDetail.getStatus());
//            tStdDevice.setUpdateTime(tStdDeviceDetail.getUpdateTime());
//            tStdDevice.setUpRegionId(tStdDeviceDetail.getUpRegionId());
//            tStdDevice.setUpRegionName(tStdDeviceDetail.getUpRegionName());
//            this.tStdDeviceDao.add(tStdDevice);
//        }


        TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
        tStdDeviceAttr.setDepartment(tStdDeviceDetail.getDepartment());
        tStdDeviceAttr.setDeviceId(Long.valueOf(tStdDeviceDetail.getDeviceId()));
        tStdDeviceAttr.setDeviceModel(tStdDeviceDetail.getDeviceModel());
        tStdDeviceAttr.setDisableDate(tStdDeviceDetail.getDisableDate());
        tStdDeviceAttr.setIp(tStdDeviceDetail.getIp());
        tStdDeviceAttr.setLastMaintenance(tStdDeviceDetail.getLastMaintenance());
        tStdDeviceAttr.setLatitude(tStdDeviceDetail.getLatitude());
        tStdDeviceAttr.setLongitude(tStdDeviceDetail.getLongitude());
        tStdDeviceAttr.setMaintenanceCount(tStdDeviceDetail.getMaintenanceCount());
        tStdDeviceAttr.setDeviceVendor(tStdDeviceDetail.getDeviceVendor());
        tStdDeviceAttr.setUsedTime(tStdDeviceDetail.getUsedTime());
        tStdDeviceAttr.setOrganization(tStdDeviceDetail.getOrganization());
        tStdDeviceAttr.setVoltageLevel(tStdDeviceDetail.getVoltageLevel());
        tStdDeviceAttr.setSequencePoint(tStdDeviceDetail.getSequencePoint());
        tStdDeviceAttr.setResponsiblePerson(tStdDeviceDetail.getResponsiblePerson());
        tStdDeviceAttr.setPmsId(tStdDeviceDetail.getPmsId());
        tStdDeviceAttr.setPmsType(tStdDeviceDetail.getPmsType());
        tStdDeviceAttr.setPort(tStdDeviceDetail.getPort());
        tStdDeviceAttr.setProductionDate(tStdDeviceDetail.getProductionDate());
        tStdDeviceAttr.setResponsiblePerson(tStdDeviceDetail.getResponsiblePerson());
        tStdDeviceAttr.setAddress(tStdDeviceDetail.getAddress());
        return this.tStdDeviceAttrDao.update(tStdDeviceAttr);
    }


    @Transactional(rollbackFor = Exception.class)
    public TStdDevice selectByPrimaryId(Long deviceId) {
        return this.tStdDeviceDao.selectByPrimaryId(deviceId).get(0);
    }


    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceDetail selectByPrimaryIdAll(Long deviceId) {
        return this.tStdDeviceDao.selectByPrimaryIdAll(deviceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TStdDevice> select(Long deviceId, String deviceCode, String deviceName, String aliasName, Integer deviceType, String positionType, Long modelId, String regionPath, Long upRegionId, String upRegionName,  Integer customType, Integer status, Date updateTime, Date createTime) {
        List<TStdDevice> tStdDeviceList = tStdDeviceDao.select(deviceId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customType, status, updateTime, createTime);
        return tStdDeviceList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceDetail> selectAll(Long deviceId, String deviceCode, String deviceName, String aliasName, Integer deviceType, String positionType, Long modelId, String regionPath, Long upRegionId, String upRegionName, Integer customType, Integer status, Date updateTime, Date createTime,Integer deviceModel, String pmsType, String pmsId, String deviceVendor, Date productionDate, Date usedTime, Date disableDate, Date lastMaintenance, String maintenanceCount, String organization, String department, String responsiblePerson, String latitude, String longitude, String ip, Integer port, String voltageLevel, String sequencePoint, String realCode,String address) {
        return this.tStdDeviceDao.selectAll(deviceId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customType, status, updateTime, createTime,
                deviceModel, pmsType, pmsId, deviceVendor, productionDate, usedTime, disableDate, lastMaintenance, maintenanceCount, organization, department, responsiblePerson, latitude, longitude, ip, port, voltageLevel, sequencePoint, realCode,address);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TStdDevice> selectByPage(TStdDevice tStdDevice) {
        List<TStdDevice> tStdDeviceList = tStdDeviceDao.selectByPage(tStdDevice);
        return tStdDeviceList;
    }

    public List<Long> selectForPage(TStdDeviceDetail tStdDeviceDetail){
        return tStdDeviceDao.selectForPage( tStdDeviceDetail);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceDetail> selectByPageAll(TStdDeviceDetail tStdDeviceDetail,List<Long>listForPage) {
        return this.tStdDeviceDao.selectByPageAll(tStdDeviceDetail.getDeviceName(),tStdDeviceDetail.getDeviceType(),tStdDeviceDetail.getRealCode(),tStdDeviceDetail.getUpRegionId(),tStdDeviceDetail.getUpRegionIds(),listForPage,tStdDeviceDetail.getPageSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdRegion selectRegionById(Long deviceId) {
        return this.tStdDeviceDao.selectRegionById(deviceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectDevTree(String level, String deviceShow, String deviceType, String analyseType) {
        List<AreaInfo> listTree = new ArrayList<>();
        switch (level) {
            case "5":
                listTree = this.tStdDeviceDao.selectDevTreeRegion();
                break;
            case "6":
                if (Objects.equals(deviceShow, "dev")) { listTree = this.tStdDeviceDao.selectDevTreeDevice(null); }
                else if (Objects.equals(deviceShow, "camera")) { listTree = this.tCameraInfoDao.selectCameraTreeDevice(); }
                else if (Objects.equals(deviceShow, "robot")) { listTree = this.tStdDeviceDao.selectRobotTree(); }
                else if (Objects.equals(deviceShow, "all")) { listTree = this.tStdDeviceDao.selectAllTreeDevice(); }
                else { throw new BusinessException("设备树展示内容输入有误！"); }
                break;
            case "7":
                if (Objects.equals(deviceShow, "dev")) { listTree = this.tStdDeviceDao.selectDevTreeCustom(); }
                else if (Objects.equals(deviceShow, "all")) { listTree = this.tStdDeviceDao.selectAllTreeCustom(); }
                else { throw new BusinessException("设备树展示内容输入有误！"); }
                break;
            case "8":
                if (Objects.equals(deviceShow, "camera")) { listTree = this.tCameraInfoDao.selectCameraPresetTree(); }
                else if (Objects.equals(deviceShow, "robot")) { listTree = this.tStdDeviceDao.selectRobotInspectionTree(); }
                else if (Objects.equals(deviceShow, "all")) { listTree = this.tStdDeviceDao.selectAllMeteTree(); }
                else { throw new BusinessException("设备树展示内容输入有误！"); }
                break;
            case "9":
                if(StringUtils.isNotEmpty(deviceType)){
                    //送检查询筛选用
//                    Map<String, String> selectEdgeMap = redisTemplate.opsForHash().entries("t_sys_param:selectEdge");
//                    String selectEdge = selectEdgeMap.get("content");
//                    String edgeLevel = (String)redisTemplate.opsForHash().get("t_sys_param:edgeLevel","content");
//                    if ("1".equals(edgeLevel)){
//                        selectEdge = null;
//                    }
                    if (Objects.equals(deviceShow, "camera")) { listTree = this.tStdDeviceDao.selectCameraMeteCruiseTree(deviceType,analyseType); }
                    else if (Objects.equals(deviceShow, "robot")) { listTree = this.tStdDeviceDao.selectRobotMeteCruiseTree(deviceType,analyseType); }
                    else if (Objects.equals(deviceShow, "all")) { listTree = this.tStdDeviceDao.selectAllMeteCruiseTree(deviceType,analyseType,null); }
                    else { throw new BusinessException("设备树展示内容输入有误！"); }
                }else{
                    throw new BusinessException("设备树展示内容传参有误！");
                }
                break;
            default:
                throw new BusinessException("设备树展示层级输入有误！");
        }
        return assembleTrees(listTree);
//        List<AreaInfo> areaInfoCountryList = new ArrayList<>();
//        for(Iterator<AreaInfo> it = listTree.iterator();it.hasNext();){
//            AreaInfo areaInfoMap = it.next();
//            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
//                AreaInfo areaInfoCountry = new AreaInfo();
//                areaInfoCountry.setId(areaInfoMap.getId());
//                areaInfoCountry.setLabel(areaInfoMap.getLabel());
//                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
//                areaInfoCountryList.add(areaInfoCountry);
//            }
//        }
//        diGui(areaInfoCountryList, listTree);
//        return areaInfoCountryList;
    }

    public Map<Long,List<Long>> selectDevTreeByInstanceId(List<Long> instanceIdList){
        Map<Long,List<Long>> reMap = new HashMap<>();
        instanceIdList.forEach(instanceId ->{
            TCruisePointInstanceDetail insInfo = tStdDeviceDao.selectDevTreeDeviceByInstacneId(instanceId);
            if (insInfo != null){
                List<Long> insList = new ArrayList<>();
                insList.add(insInfo.getDeviceId());
                insList.add(insInfo.getDeviceMeteId());
                insList.add(insInfo.getInstanceId());
                List<Long> upRegionList = new ArrayList<>();
                upRegionList.add(insInfo.getUpRegionId());
                //  MySQL 8.0
//                List<Long> regionList  = tStdDeviceDao.selectUpIdByRegionList(upRegionList);

                List<Long> regionList = getRegionIdByLeafNode(new HashSet<>(upRegionList));
                Collections.reverse(regionList);
                regionList.remove(-1L);
                regionList.addAll(upRegionList);
                regionList.addAll(insList);
                reMap.put(instanceId,regionList);
            }

        });
        return reMap;
    }

    public Map<Long,List<Long>> selectDevTreeByDeviceId(List<Long> deviceIdList){
        Map<Long,List<Long>> reMap = new HashMap<>();
        deviceIdList.forEach(deviceId ->{
            if (deviceId != null){
               TStdRegion region =  tStdDeviceDao.selectRegionById(deviceId);
                List<Long> upRegionList = new ArrayList<>();
                upRegionList.add(region.getRegionId());
                //  MySQL 8.0
//                List<Long> regionList  = tStdDeviceDao.selectUpIdByRegionList(upRegionList);

                List<Long> regionList = getRegionIdByLeafNode(new HashSet<>(upRegionList));

                Collections.reverse(regionList);
                regionList.remove(-1L);
                regionList.addAll(upRegionList);
                regionList.add(deviceId);
                reMap.put(deviceId,regionList);
            }

        });
        return reMap;
    }

    public List<AreaInfo> selectDevTreeByName(String name, String type,String deviceShow,String deviceType){
        if (StringUtils.isEmpty(name)){
            return selectDevTreeNew("5",null,null,null,null,null);
        }
        List<AreaInfo> devTreeByName = new ArrayList<>();
        switch (type){
            case "region":
                //针对region的过滤
                List<AreaInfo> allTree = tStdDeviceDao.selectAreaTree();
                allTree = assembleTrees(allTree);
                if (StringUtils.isNotEmpty(name)){
                    if (!matchName(allTree.get(0),name)){
                        allTree.remove(0);
                    };
                }
                return allTree;
            case "dev":
                if ("camera".equals(deviceShow)){
                    //查相机设备
                    List<TCruisePointInstance> cameraList = tStdDeviceDao.selectCameraTreeDeviceByName(name);
                    if (CollectionUtils.isNotEmpty(cameraList)){
                        List<Long> regionList = tStdDeviceDao.selectRegionByDeviceList(cameraList);
                        //  MySQL 8.0
//                        regionList.addAll(tStdDeviceDao.selectUpIdByRegionList(regionList));
                        regionList = getRegionIdByLeafNode(new HashSet<>(regionList));
                        if (CollectionUtils.isNotEmpty(regionList)){
                            devTreeByName = tStdDeviceDao.selectDevTreeDeviceByNameTree(cameraList,regionList);
                        }
                    }
                }else if ("allDevice".equals(deviceShow)){
                    List<TCameraInfo> cameraList = tStdDeviceDao.selectAllPatrolDeviceByName(name);
                    if (CollectionUtils.isNotEmpty(cameraList)) {
                        List<Long> regionList = cameraList.stream().map(TCameraInfo::getUpRegionId).collect(Collectors.toList());
                        List<Long> allRegionList = getAllUpRegionId(regionList);
                        if (CollectionUtils.isNotEmpty(regionList)) {
                            devTreeByName = tStdDeviceDao.selectAllPatrolDeviceTreeByName(cameraList, allRegionList);
                        }
                    }
                }else {
                    //查设备
                    List<TCruisePointInstance> deviceList = tStdDeviceDao.selectDevTreeDeviceByName(name);
                    if (CollectionUtils.isNotEmpty(deviceList)){
                        List<Long> regionList = tStdDeviceDao.selectRegionByDeviceList(deviceList);
                        //  MySQL 8.0
//                        regionList.addAll(tStdDeviceDao.selectUpIdByRegionList(regionList));
                        regionList = getRegionIdByLeafNode(new HashSet<>(regionList));
                        if (CollectionUtils.isNotEmpty(regionList)){
                            devTreeByName = tStdDeviceDao.selectDevTreeDeviceByNameTree(deviceList,regionList);
                        }
                    }
                }
                break;
            case "ins":
                //查巡视点
                List<TCruisePointInstance> insList = tStdDeviceDao.selectAllMeteCruiseTreeByName(name,deviceType);
                if (CollectionUtils.isNotEmpty(insList)){
                    List<Long> regionList = tStdDeviceDao.selectRegionByDeviceList(insList);
                    //  MySQL 8.0
//                    regionList.addAll(tStdDeviceDao.selectUpIdByRegionList(regionList));
                    regionList = getRegionIdByLeafNode(new HashSet<>(regionList));
                    if (CollectionUtils.isNotEmpty(regionList)){

                        devTreeByName = tStdDeviceDao.selectAllMeteCruiseTreeByNameTree(insList,regionList);
                    }
                }
                break;
            default: throw new BusinessException("设备树展示层级输入有误！");
        }
        return assembleTrees(devTreeByName);
    }

    public List<Long> getRegionIdByLeafNode(Set<Long> regionParam) {
        Set<Long> regionTemp = new HashSet<>(regionParam);
        do {
            //通过regionList查询上层节点，后将结果放入插入参数继续查询，直到结果与入参一致
            regionParam.addAll(regionTemp);
            regionTemp.addAll(tCameraInfoDao.selectRegionListByUpRegionId(regionParam));
        } while (!regionParam.containsAll(regionTemp));
        return  new ArrayList<>(regionTemp);
    }

    private Boolean matchName(AreaInfo node,String regionName){
        if (node.getLabel().contains(regionName)){
            return true;
        }else {
            List<AreaInfo> child = node.getChildren();
            List<AreaInfo> newChild = new ArrayList<>();
            if (child != null && child.size() > 0){
                for (AreaInfo nodeItem : child){
                    if (matchName(nodeItem,regionName)){
                        newChild.add(nodeItem);
                    }
                }
            }
            node.setChildren(newChild);
            if (newChild.size() > 0){
                return true;
            }
            return false;
        }
    }

    //设备树查询(level：5-间隔，6-设备，7-部位，8-点位，9巡视点；deviceShow：all-所有，dev-设备，camera-摄像头，robot-机器人
    public List<AreaInfo> selectDevTreeNew(String level, String deviceShow,String deviceType, String analyseType,Long id,String customId){
        switch (level){
            case "5":
                return areaTree(deviceShow);
            case "6":
                return deviceTree(id,deviceShow);
            case "7":
                return customTree(id);
            case "8":
                return deviceMeteTree(id,deviceType,analyseType);
            case "9":
                return cruisePoint(id);
            case "66":
                return new ArrayList<>();
            default: throw new BusinessException("设备树展示层级输入有误！");
        }
    }

    public List<AreaInfo> selectDevTest(String level, String deviceShow,String deviceType, String analyseType, Long id){
        switch (level){
            case "5":
                return areaTree(deviceShow);
            case "6":
                return deviceTree(id,deviceShow);
            case "7":
                return customTree(id);
            case "8":
                return deviceMeteTree(id,deviceType,analyseType);
            case "9":
                return cruisePoint(id);
            case "66":
                return new ArrayList<>();
            default: throw new BusinessException("设备树展示层级输入有误！");
        }
    }

    private List<AreaInfo> areaTree(String deviceShow){
        List<AreaInfo> areaTree = tStdDeviceDao.selectDevTreeRegion();
        areaTree =  assembleTrees(areaTree);
        if (!StringUtils.isEmpty(deviceShow)){
            areaTree =  areaAddDeviceTree(areaTree,deviceShow);
        }
        return areaTree;
    }

    private List<AreaInfo> areaAddDeviceTree(List<AreaInfo> areaTree, String deviceShow){
        if (areaTree == null){
            return null;
        }
        areaTree.forEach(area ->{
            if ("region".equals(area.getInfoType())){
                if (area.getChildren() != null && area.getChildren().size() > 0){
                    switch (deviceShow){
                        case "all":
                            area.getChildren().addAll(tStdDeviceDao.selectAllByRegionId(area.getId()));
                            break;
                        case "allDevice":
                            area.getChildren().addAll(tStdDeviceDao.getRegionMonitorDevice(area.getId()));
                            break;
                        case "dev":
                            area.getChildren().addAll(tStdDeviceDao.selectDeviceByRegionId(area.getId()));
                            break;
                        case "camera":
                            area.getChildren().addAll(tStdDeviceDao.selectCameraByRegionId(area.getId()));
                            break;
                        case "robot":
                            area.getChildren().addAll(tStdDeviceDao.selectRobotByRegionId(area.getId()));
                            break;
                        default:throw new BusinessException("设备树展示内容输入有误！");
                    }
                    areaAddDeviceTree(area.getChildren(),deviceShow);
                }
            }
        });
        return areaTree;
    }

    public List<AreaInfo> deviceTree(Long upRegionId,String deviceShow){
        List<AreaInfo> deviceTree;
        switch (deviceShow){
            case "all":
                deviceTree = tStdDeviceDao.selectAllByRegionId(upRegionId);
                break;
            case "allDevice":
                deviceTree = tStdDeviceDao.getRegionMonitorDevice(upRegionId);
                break;
            case "dev":
                deviceTree = tStdDeviceDao.selectDeviceByRegionId(upRegionId);
                break;
            case "camera":
                deviceTree = tStdDeviceDao.selectCameraByRegionId(upRegionId);
                break;
            case "robot":
                deviceTree = tStdDeviceDao.selectRobotByRegionId(upRegionId);
                break;
            default:throw new BusinessException("设备树展示内容输入有误！");
        }
        return deviceTree;
    }

    public List<AreaInfo> customTree(Long deviceId){
        return tStdDeviceDao.selectCustomByRegionId(deviceId);
    }

    public List<AreaInfo> deviceMeteTree(Long deviceId,String deviceType, String analyseType){
        List<AreaInfo> areaInfos = tStdDeviceDao.selectDeviceMeteByDeviceAndCustom(deviceId,deviceType,analyseType);
        List<AreaInfo> uniqueAreaInfos = areaInfos.stream()
                .collect(Collectors.toMap(AreaInfo::getId, Function.identity(), (existing, replacement) -> {
                    if (StringUtils.isBlank(existing.getCameraId()) && StringUtils.isBlank(replacement.getCameraId())) {
                        return existing;
                    } else if (StringUtils.isNotBlank(existing.getCameraId()) && StringUtils.isNotBlank(replacement.getCameraId())) {
                        return existing;
                    } else if (StringUtils.isNotBlank(existing.getCameraId())) {
                        return existing;
                    } else {
                        return replacement;
                    }
                })).values().stream().collect(Collectors.toList());
        return uniqueAreaInfos;
    }

    public List<AreaInfo> cruisePoint(Long deviceMeteId){
        return tStdDeviceDao.selectCruisePointByDeviceMeteId(deviceMeteId);
    }

    public List<AreaInfo> assembleTrees(Collection<AreaInfo> trees) {
        if (CollectionUtils.isEmpty(trees)) {
            return Collections.emptyList();
        }

        // 构建树主键/实例映射表，并初始化树的子节点集合
        Map<?, AreaInfo> mapping = trees.stream().peek(tree -> tree.setChildren(new LinkedList<>()))
                .collect(Collectors.toMap(AreaInfo::getId, t -> t, (o, n) -> n));

        // 查找并关联树节点，返回所有没有父节点的树
        return trees.stream().filter(tree -> {
            AreaInfo parent = ifNull(tree.getUpId(), mapping::get);
            if (parent != null) {
                parent.getChildren().add(tree);
            }
            return Objects.isNull(parent);
        }).collect(Collectors.toList());
    }


    /**
     * 返回不为空的对象（如果第一个对象为空，则返回第二个对象）
     *
     * @param object   目标对象
     * @param function 目标对象方法
     * @param <T>      目标对象类型泛型
     * @param <R>      返回对象类型泛型
     * @return 返回对象
     */
    public static <T, R> R ifNull(T object, Function<T, R> function) {
        return object == null || function == null ? null : function.apply(object);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectOperationDevTree(Long regionId) {
        List<AreaInfo> listTreeAll = this.tStdDeviceDao.selectDevTreeDevice(null);
        List<AreaInfo> listTree = new ArrayList<>();
        List<AreaInfo> listTreeById = this.tStdDeviceDao.selectDevTreeDevice(regionId);

        for (AreaInfo areaInfo : listTreeById) {
            listTree.add(areaInfo);
            if (areaInfo.getUpId() != -1 && areaInfo.getUpId() != null) {
                Long areaInfoRegionCodeUpId = areaInfo.getUpId();
                System.out.println("areaInfoRegionCodeUpId: " + areaInfoRegionCodeUpId);
                for (AreaInfo areaInfoAll : listTreeAll) {
                    if (Objects.equals(areaInfoAll.getId(), areaInfoRegionCodeUpId)) {
                        listTree.add(areaInfoAll);
                        diGuiMoHu(areaInfoAll, listTreeAll, listTree);
                    }
                }
            }
        }
        listTree = listTree.stream().distinct().collect(Collectors.toList());
        System.out.println("listTree: " + listTree);
        List<AreaInfo> areaInfoCountryList = new ArrayList<>();
        for (AreaInfo areaInfoMap : listTree) {
            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId() == -1) {
                AreaInfo areaInfoCountry = new AreaInfo();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountryList.add(areaInfoCountry);
            }
        }
        diGui(areaInfoCountryList, listTree);
        return areaInfoCountryList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectRegionIdTree(Long UpRegionId) {
        List<Long> upRegionIds = new ArrayList<>();
        List<AreaInfo> upRegionTree = new ArrayList<>();
        upRegionTree = this.tStdDeviceDao.selectAllRegion();
        List<AreaInfo> upRegionList = new ArrayList<>();
        for (Iterator<AreaInfo> it = upRegionTree.iterator(); it.hasNext(); ) {
            AreaInfo areaInfoMap = it.next();
            if (areaInfoMap.getId().equals(UpRegionId)) {
                AreaInfo upRegion = new AreaInfo();
                upRegion.setId(areaInfoMap.getId());
                upRegionList.add(upRegion);
                upRegionIds.add(UpRegionId);
            }
        }
        Recursion(upRegionIds, upRegionList, upRegionTree);
        return upRegionIds;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectDevTaskTree(String taskId) {

        UPatrolTask uPatrolTask = uPatrolTaskService.selectByPrimaryId(taskId);
        // 周期间隔任务的taskId和taskCode不一致
        taskId = StringUtils.equals(uPatrolTask.getTaskCode(), taskId) ? taskId : uPatrolTask.getTaskCode();
        List<AreaInfo> listTree = this.tStdDeviceDao.selectDevTaskTree(taskId);

        List<AreaInfo> areaInfoCountryList = new ArrayList<>();
        for(Iterator<AreaInfo> it = listTree.iterator();it.hasNext();){
            AreaInfo areaInfoMap = it.next();
            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                AreaInfo areaInfoCountry = new AreaInfo();
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountryList.add(areaInfoCountry);
            }
        }
        diGui(areaInfoCountryList, listTree);
        return areaInfoCountryList;
    }

    private void Recursion(List<Long> upRegionIds, List<AreaInfo> upRegionList, List<AreaInfo> upRegionTree) {
        for(AreaInfo areaInfo : upRegionList){
            List<AreaInfo> childrenList = new ArrayList<>();
            for(Iterator<AreaInfo> it = upRegionTree.iterator();it.hasNext();) {
                AreaInfo areaInfoMap = it.next();
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    AreaInfo areaInfoTem = new AreaInfo();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    childrenList.add(areaInfoTem);
                    upRegionIds.add(areaInfoTem.getId());
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                Recursion(upRegionIds, childrenList, upRegionTree);
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectRegionTreeByName(String regionName) {
        List<AreaInfo> listTree = new ArrayList<>();
        List<AreaInfo> listTreeAll = this.tStdDeviceDao.selectDevTreeRegion();
        if (Objects.equals(null, regionName) || regionName.equals("")) {
            List<AreaInfo> areaInfoCountryList = new ArrayList<>();
            for(Iterator<AreaInfo> it = listTreeAll.iterator();it.hasNext();){
                AreaInfo areaInfoMap = it.next();
                if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                    AreaInfo areaInfoCountry = new AreaInfo();
                    areaInfoCountry.setLabel(areaInfoMap.getLabel());
                    areaInfoCountry.setId(areaInfoMap.getId());
                    areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                    areaInfoCountryList.add(areaInfoCountry);
                }
            }
            diGui(areaInfoCountryList, listTreeAll);
            return areaInfoCountryList;
        }
        List<AreaInfoRegionCode> listTreeByName = new ArrayList<>();
        listTreeByName = tStdRegionDao.selectRegTreeByRegName(regionName);
        if (listTreeByName.size()>0) {
            for (AreaInfoRegionCode areaInfoRegionCode : listTreeByName) {
                AreaInfo areaInfo = new AreaInfo();
                areaInfo.setId(areaInfoRegionCode.getId());
                areaInfo.setLabel(areaInfoRegionCode.getLabel());
                areaInfo.setUpName(areaInfoRegionCode.getUpName());
                areaInfo.setUpId(areaInfoRegionCode.getUpId());
                areaInfo.setInfoType(areaInfoRegionCode.getInfoType());
                listTree.add(areaInfo);
                if (areaInfoRegionCode.getUpId() != -1 && areaInfoRegionCode.getUpId() != null) {
                    Long areaInfoRegionCodeUpId = areaInfoRegionCode.getUpId();
                    System.out.println("areaInfoRegionCodeUpId: "+areaInfoRegionCodeUpId);
                    for (AreaInfo areaInfoAll : listTreeAll) {
                        if (Objects.equals(areaInfoAll.getId(), areaInfoRegionCodeUpId)) {
                            listTree.add(areaInfoAll);
                            diGuiMoHu(areaInfoAll, listTreeAll, listTree);
                        }
                    }
                }
            }
            listTree = listTree.stream().distinct().collect(Collectors.toList());
            System.out.println("listTree: "+listTree);
            List<AreaInfo> areaInfoCountryList = new ArrayList<>();
            for(Iterator<AreaInfo> it = listTree.iterator();it.hasNext();){
                AreaInfo areaInfoMap = it.next();
                if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                    AreaInfo areaInfoCountry = new AreaInfo();
                    areaInfoCountry.setId(areaInfoMap.getId());
                    areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                    areaInfoCountry.setLabel(areaInfoMap.getLabel());
                    areaInfoCountryList.add(areaInfoCountry);
                }
            }
            diGui(areaInfoCountryList, listTree);
            return areaInfoCountryList;
        } else {return listTree;}
    }


    private void diGuiMoHu(AreaInfo areaInfoAll, List<AreaInfo> listTreeAll, List<AreaInfo> listTree) {
        if (areaInfoAll.getUpId() != null && areaInfoAll.getUpId() != -1) {
            Long areaInfoRegionCodeUpId = areaInfoAll.getUpId();
            for (AreaInfo areaInfo : listTreeAll) {
                if (Objects.equals(areaInfo.getId(), areaInfoRegionCodeUpId)) {
                    listTree.add(areaInfo);
                    diGuiMoHu(areaInfo, listTreeAll, listTree);
                }
            }
        }
    }

    private void diGui(List<AreaInfo> areaInfoList, List<AreaInfo> listTree) {
        for(AreaInfo areaInfo : areaInfoList){
            List<AreaInfo> childrenList = new ArrayList<>();
            for(Iterator<AreaInfo> it = listTree.iterator();it.hasNext();){
                AreaInfo areaInfoMap = it.next();
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId()) && !Objects.equals(areaInfo.getUpId(), areaInfoMap.getUpId())) {
                    AreaInfo areaInfoTem = new AreaInfo();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    areaInfoTem.setLabel(areaInfoMap.getLabel());
                    areaInfoTem.setInfoType(areaInfoMap.getInfoType());
                    areaInfoTem.setUpName(areaInfoMap.getUpName());
                    areaInfoTem.setDeviceTypeId(areaInfoMap.getDeviceTypeId());
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree);
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public List<String> selectByModelId(Long modelId) {
        List<String> tStdDeviceList = tStdDeviceDao.selectByModelId(modelId);
        return tStdDeviceList;
    }


    @Transactional(rollbackFor = Exception.class)
    public int updateModelIdByDevCus(Long deviceId,Long modelId){
        return this.tStdDeviceDao.updateModelIdByDevCus(deviceId, modelId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String list){
        List<String> list1= Arrays.asList(list.split(","));
        for (String item:list1) {
            int re  = this.deleteByPrimaryIdALL(Long.valueOf(item));
            if(re == -1){
                return -1;
            }
        }
        return 1;
    }

    public List<Long> selectDeviceIdsByRegion(List<Long> regionIds){
        return tStdDeviceDao.selectDeviceIdsByRegion(regionIds);
    }
    public List<Long> selectDeviceIdListByRegion(List<Long> regionIds){
        return tStdDeviceDao.selectDeviceIdListByRegion(regionIds);
    }

    public List<AreaInfo> selectPatrolDeviceTree() {
        //查巡视设备
        List<AreaInfo> devTreeByName = tStdDeviceDao.selectPatrolDeviceTree();
        return assembleTrees(devTreeByName);
    }

    /**
     * 对巡视设备进行名称筛选
     * @param areaInfoList
     * @param name
     */
    public void filter(List<AreaInfo> areaInfoList, String name) {
        Iterator<AreaInfo> it = areaInfoList.iterator();
        while (it.hasNext()) {
            AreaInfo areaInfo = it.next();
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(areaInfo.getChildren())) {
                this.filter(areaInfo.getChildren(), name);
            }
            //根据输入的名称
            if (ArrayUtils.contains(new String[]{"camera", "robot", "voice"}, areaInfo.getInfoType())) {
                if (StringUtils.isNotEmpty(name) && !areaInfo.getLabel().contains(name)) {
                    it.remove();
                }
            }
        }
    }

    public List<AreaInfo> selectMeteTreeByDeviceName(String name){
        List<TCruisePointInstance> deviceList = tStdDeviceDao.selectDevTreeDeviceByName(name);
        if (CollectionUtils.isNotEmpty(deviceList)) {
            List<Long> regionList = tStdDeviceDao.selectRegionByDeviceList(deviceList);
            List<Long> allRegionList = getAllUpRegionId(regionList);
            List<AreaInfo> meteTreeByDeviceName = tStdDeviceDao.selectAllMeteCruiseTreeByNameTree(deviceList, allRegionList);
            return assembleTrees(meteTreeByDeviceName);
        }else {
            return new ArrayList<>();
        }
    }

    private List<Long> getAllUpRegionId(List<Long> regionList){
        List<AreaInfo> allRegionList = tStdDeviceDao.selectAllRegion();
        List<Long> reList = new ArrayList<>(regionList);
        for (Long regionId : regionList){
            Long id = getUpRegionId(allRegionList,regionId);
            if (reList.contains(id)){
                continue;
            }
            while (true){
                reList.add(id);
                id = getUpRegionId(allRegionList,reList.get(reList.size() - 1));
                if (id == -1){
                    break;
                }
            }
        }
        return reList;
    }

    private Long getUpRegionId(List<AreaInfo> regionList,Long regionId){
        for (AreaInfo areaInfo: regionList){
            if (Objects.equals(areaInfo.getId(), regionId)){
                return areaInfo.getUpId();
            }
        }
        return  -1L;
    }
}

