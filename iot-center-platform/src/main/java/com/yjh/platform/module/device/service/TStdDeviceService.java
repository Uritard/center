package com.yjh.platform.module.device.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.dao.*;
import com.yjh.platform.module.device.entity.*;

import java.util.*;
import java.util.stream.Collectors;


import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-07-27
*/
@Service
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


    @Transactional(rollbackFor = Exception.class)
    public TStdDevice selectByUnionKeys(Long deviceId,String customId){
        return tStdDeviceDao.selectByUnionKeys(deviceId, customId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int add(TStdDevice tStdDevice) {
        if(tStdDevice.getCustomId() == null){
            tStdDevice.setCustomId("101");
            List<TDictBusiness> list = tDictBusinessDao.select(null,"101",null,null,null,null,null);
            tStdDevice.setCustomName(list.get(0).getDictNote());
        }
        return this.tStdDeviceDao.add(tStdDevice);
    }


    @Transactional(rollbackFor = Exception.class)
    public int addALL(TStdDeviceDetail tStdDeviceDetail) {

        TStdDevice tStdDevice = new TStdDevice();
        tStdDevice.setAliasName(tStdDeviceDetail.getAliasName());
        tStdDevice.setCustomId(tStdDeviceDetail.getCustomId());
        if(tStdDevice.getCustomId() == null){
            //不传 设为本体，根据字典表查，暂定为101
            tStdDevice.setCustomId("101");
            List<TDictBusiness> list = tDictBusinessDao.select(null,"101",null,null,null,null,null);
            tStdDevice.setCustomName(list.get(0).getDictNote());
        }else{
            tStdDevice.setCustomId(tStdDeviceDetail.getCustomId());
            List<TDictBusiness> list = tDictBusinessDao.select(null,tStdDeviceDetail.getCustomId(),null,null,null,null,null);
            tStdDevice.setCustomName(list.get(0).getDictNote());
        }
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
        this.tStdDeviceDao.add(tStdDevice);
        Long deivceIdUnique=tStdDevice.getDeviceId();
        List<TStdMeteModelDetail> tStdMeteModelDetailList = tStdMetemodelDetailDao.selectByPrimaryId(tStdDeviceDetail.getModelId());//根据模板ID查询测点模板
        for (TStdMeteModelDetail tStdMeteModelDetailItem: tStdMeteModelDetailList) {
            tStdMeteModelDetailItem.setDeviceId(deivceIdUnique);//测点模板结合设备ID形成标准测点
            tStdDevicemeteDao.add(tStdMeteModelDetailItem);
            if(Objects.isNull(tStdDeviceDao.selectByUnionKeys(tStdMeteModelDetailItem.getDeviceId(),tStdMeteModelDetailItem.getCustomType()))){//device表中没有新增设备的测点部位
                TStdDevice stdDevice=new TStdDevice();
                stdDevice.setDeviceId(deivceIdUnique);
                stdDevice.setAliasName(tStdDeviceDetail.getAliasName());
                stdDevice.setCreateTime(tStdDeviceDetail.getCreateTime());
                stdDevice.setCustomId(tStdMeteModelDetailItem.getCustomType());
                stdDevice.setCustomName(tStdMeteModelDetailItem.getCustomTypeName());
                stdDevice.setCustomType(tStdDeviceDetail.getCustomType());
                stdDevice.setDeviceCode(tStdDeviceDetail.getDeviceCode());
                stdDevice.setDeviceName(tStdDeviceDetail.getDeviceName());
                stdDevice.setDeviceType(tStdDeviceDetail.getDeviceType());
                stdDevice.setModelId(tStdDeviceDetail.getModelId());
                stdDevice.setPositionType(tStdDeviceDetail.getPositionType());
                stdDevice.setRegionPath(tStdDeviceDetail.getRegionPath());
                stdDevice.setStatus(tStdDeviceDetail.getStatus());
                stdDevice.setUpdateTime(tStdDeviceDetail.getUpdateTime());
                stdDevice.setUpRegionId(tStdDeviceDetail.getUpRegionId());
                stdDevice.setUpRegionName(tStdDeviceDetail.getUpRegionName());
                tStdDeviceDao.add(stdDevice);
            }


        }

        TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
        tStdDeviceAttr.setRealCode(tStdDeviceDetail.getRealCode());
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
        List<Long> devList = tStdDevicemeteDao.selectByDevId(deviceId);
        {//设备下存在测点
            if(devList != null && devList.size()>0){
             return -1;
            }
        }
        tStdDeviceAttrDao.deleteByPrimaryId(deviceId);//删除属性
        for (Long item: devList) {
            tStdDevicemeteService.deleteByPrimaryId(item);
        }
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
            tStdDevice.setAliasName(tStdDeviceDetail.getAliasName());
            tStdDevice.setCreateTime(tStdDeviceDetail.getCreateTime());
            tStdDevice.setDeviceCode(tStdDeviceDetail.getDeviceCode());
            tStdDevice.setDeviceName(tStdDeviceDetail.getDeviceName());
            tStdDevice.setUpdateTime(tStdDeviceDetail.getUpdateTime());
            tStdDevice.setPositionType(tStdDeviceDetail.getPositionType());
            tStdDevice.setRegionPath(tStdDeviceDetail.getRegionPath());
            tStdDevice.setStatus(tStdDeviceDetail.getStatus());
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
        tStdDeviceAttr.setRealCode(tStdDeviceDetail.getRealCode());
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
        return this.tStdDeviceDao.selectByPrimaryId(deviceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceDetail selectByPrimaryIdAll(Long deviceId) {
        return this.tStdDeviceDao.selectByPrimaryIdAll(deviceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TStdDevice> select(Long deviceId, String customId, String deviceCode, String deviceName, String aliasName, Integer deviceType, String positionType, Long modelId, String regionPath, Long upRegionId, String upRegionName, String customName, Integer customType, Integer status, Date updateTime, Date createTime) {
        List<TStdDevice> tStdDeviceList = tStdDeviceDao.select(deviceId, customId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customName, customType, status, updateTime, createTime);
        return tStdDeviceList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceDetail> selectAll(Long deviceId, String customId, String deviceCode, String deviceName, String aliasName, Integer deviceType, String positionType, Long modelId, String regionPath, Long upRegionId, String upRegionName, String customName, Integer customType, Integer status, Date updateTime, Date createTime,Integer deviceModel, String pmsType, String pmsId, String deviceVendor, Date productionDate, Date usedTime, Date disableDate, Date lastMaintenance, String maintenanceCount, String organization, String department, String responsiblePerson, String latitude, String longitude, String ip, Integer port, String voltageLevel, String sequencePoint, String realCode,String address) {
        return this.tStdDeviceDao.selectAll(deviceId, customId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customName, customType, status, updateTime, createTime,
                deviceModel, pmsType, pmsId, deviceVendor, productionDate, usedTime, disableDate, lastMaintenance, maintenanceCount, organization, department, responsiblePerson, latitude, longitude, ip, port, voltageLevel, sequencePoint, realCode,address);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TStdDevice> selectByPage(TStdDevice tStdDevice) {
        List<TStdDevice> tStdDeviceList = tStdDeviceDao.selectByPage(tStdDevice);
        return tStdDeviceList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceDetail> selectByPageAll(TStdDeviceDetail tStdDeviceDetail, List<Long> upRegionIds) {
        if(upRegionIds.size() != 0){
            tStdDeviceDetail.setUpRegionIds(upRegionIds);
        }else {
            upRegionIds.add(tStdDeviceDetail.getUpRegionId());
            tStdDeviceDetail.setUpRegionIds(upRegionIds);
        }
        return this.tStdDeviceDao.selectByPageAll(tStdDeviceDetail);
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdRegion selectRegionById(Long deviceId) {
        return this.tStdDeviceDao.selectRegionById(deviceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectDevTree(String level, String deviceShow) {
        List<AreaInfo> listTree = new ArrayList<>();
        switch (level) {
            case "5":
                listTree = this.tStdDeviceDao.selectDevTreeRegion();
                break;
            case "6":
                if (Objects.equals(deviceShow, "dev")) { listTree = this.tStdDeviceDao.selectDevTreeDevice(); }
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
            default:
                throw new BusinessException("设备树展示层级输入有误！");
        }

        List<AreaInfo> areaInfoCountryList = new ArrayList<>();
        for(Iterator<AreaInfo> it = listTree.iterator();it.hasNext();){
            AreaInfo areaInfoMap = it.next();
            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                AreaInfo areaInfoCountry = new AreaInfo();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
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
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    AreaInfo areaInfoTem = new AreaInfo();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    areaInfoTem.setLabel(areaInfoMap.getLabel());
                    areaInfoTem.setInfoType(areaInfoMap.getInfoType());
                    areaInfoTem.setUpName(areaInfoMap.getUpName());
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
    public int updateModelIdByDevCus(Long deviceId,Long customId,Long modelId){
        return this.tStdDeviceDao.updateModelIdByDevCus(deviceId, customId, modelId);
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

}

