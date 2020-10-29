package com.yjh.platform.module.device.service;

import com.github.pagehelper.parser.impl.HsqldbParser;
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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdDevice tStdDevice) {
        if(tStdDevice.getCustomId() == null){
            tStdDevice.setCustomId("101");
            List<TDictBusiness> list = tDictBusinessDao.select(null,"101",null,null,null,null,null);
            tStdDevice.setCustomName(list.get(0).getDictNote());
        }
        return this.tStdDeviceDao.add(tStdDevice);
    }

    @Logs(title = "插入设备及属性", code = "module")
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

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceId) {
        return this.tStdDeviceDao.deleteByPrimaryId(deviceId);
    }

    @Logs(title = "删除设备及属性", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryIdALL(Long deviceId) {
                tStdDeviceAttrDao.deleteByPrimaryId(deviceId);//删除属性
                List<Long> devList = tStdDevicemeteDao.selectByDevId(deviceId);
        for (Long item: devList) {
            tStdDevicemeteService.deleteByPrimaryId(item);
        }
        return this.tStdDeviceDao.deleteByPrimaryId(deviceId);//删除设备
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDevice tStdDevice) {
        return this.tStdDeviceDao.update(tStdDevice);
    }

    @Logs(title = "更新设备及属性", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int updateAll(TStdDeviceDetail tStdDeviceDetail) {
//        TStdDevice tStdDevice = tStdDeviceDao.selectByPrimaryId(tStdDeviceDetail.getDeviceId());
//        if(tStdDevice.getModelId() != tStdDeviceDetail.getModelId()){
            tStdDeviceDao.deleteByPrimaryId(tStdDeviceDetail.getDeviceId());//删除所有设备
            tStdDevicemeteDao.deleteByDevId(tStdDeviceDetail.getDeviceId());//删除该设备下所有测点
            List<TStdMeteModelDetail> tStdMeteModelDetailList = tStdMetemodelDetailDao.selectByPrimaryId(tStdDeviceDetail.getModelId());
            for (TStdMeteModelDetail tStdMeteModelDetailItem: tStdMeteModelDetailList) {
                tStdMeteModelDetailItem.setDeviceId(tStdDeviceDetail.getDeviceId());
                tStdDevicemeteDao.add(tStdMeteModelDetailItem);

                if(Objects.isNull(tStdDeviceDao.selectByUnionKeys(tStdMeteModelDetailItem.getDeviceId(),tStdMeteModelDetailItem.getCustomType()))){
                    TStdDevice stdDevice=new TStdDevice();
                    stdDevice.setDeviceId(tStdDeviceDetail.getDeviceId());
                    stdDevice.setAliasName(tStdDeviceDetail.getAliasName());
                    stdDevice.setCreateTime(tStdDeviceDetail.getCreateTime());
                    stdDevice.setCustomId(tStdMeteModelDetailItem.getCustomType());
                    stdDevice.setCustomName(tStdMeteModelDetailItem.getCustomTypeName());
//                List<TDictBusiness> list = tDictBusinessDao.select(null,tStdMeteModelDetailItem.getCustomType(),null,null,null,null,null);
//                stdDevice.setCustomName(list.get(0).getDictNote());
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
//        }

        if(tStdDeviceDetail.getModelId() == null){
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
        }




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

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdDevice selectByPrimaryId(Long deviceId) {
        return this.tStdDeviceDao.selectByPrimaryId(deviceId);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceDetail selectByPrimaryIdAll(Long deviceId) {
        return this.tStdDeviceDao.selectByPrimaryIdAll(deviceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDevice> select(Long deviceId, String customId, String deviceCode, String deviceName, String aliasName, Integer deviceType, String positionType, Long modelId, String regionPath, Long upRegionId, String upRegionName, String customName, Integer customType, Integer status, Date updateTime, Date createTime) {
        List<TStdDevice> tStdDeviceList = tStdDeviceDao.select(deviceId, customId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customName, customType, status, updateTime, createTime);
        return tStdDeviceList;
    }

    @Logs(title = "查询设备及属性", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceDetail> selectAll(Long deviceId, String customId, String deviceCode, String deviceName, String aliasName, Integer deviceType, String positionType, Long modelId, String regionPath, Long upRegionId, String upRegionName, String customName, Integer customType, Integer status, Date updateTime, Date createTime,Integer deviceModel, String pmsType, String pmsId, String deviceVendor, Date productionDate, Date usedTime, Date disableDate, Date lastMaintenance, String maintenanceCount, String organization, String department, String responsiblePerson, String latitude, String longitude, String ip, Integer port, String voltageLevel, String sequencePoint, String realCode,String address) {
        return this.tStdDeviceDao.selectAll(deviceId, customId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customName, customType, status, updateTime, createTime,
                deviceModel, pmsType, pmsId, deviceVendor, productionDate, usedTime, disableDate, lastMaintenance, maintenanceCount, organization, department, responsiblePerson, latitude, longitude, ip, port, voltageLevel, sequencePoint, realCode,address);
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDevice> selectByPage(TStdDevice tStdDevice) {
        List<TStdDevice> tStdDeviceList = tStdDeviceDao.selectByPage(tStdDevice);
        return tStdDeviceList;
    }

    @Logs(title = "分页查询设备及属性", code = "module")
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

    @Logs(title = "根据设备ID查询上级区域信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdRegion selectRegionById(Long deviceId) {
        return this.tStdDeviceDao.selectRegionById(deviceId);
    }

    @Logs(title = "设备树查询", code = "module")
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

    @Logs(title = "区域设备树", code = "module")
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

    @Logs(title = "区域树模糊查询", code = "module")
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

    @Logs(title = "根据ModelId查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectByModelId(Long modelId) {
        List<String> tStdDeviceList = tStdDeviceDao.selectByModelId(modelId);
        return tStdDeviceList;
    }

    @Logs(title = "根据设备ID和部位ID修改设备的模板ID")
    @Transactional(rollbackFor = Exception.class)
    public int updateModelIdByDevCus(Long deviceId,Long customId,Long modelId){
        return this.tStdDeviceDao.updateModelIdByDevCus(deviceId, customId, modelId);
    }

    @Logs(title = "批量删除",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String list){
        List<String> list1= Arrays.asList(list.split(","));
        for (String item:list1) {
            tStdDeviceAttrDao.deleteByPrimaryId(Long.valueOf(item));
            tStdDevicemeteDao.deleteByDeviceId(Long.valueOf(item));
            tCruisePointInstanceDao.deleteByDeviceId(Long.valueOf(item));
        }
        return this.tStdDeviceDao.batchDelete(list1);
    }

}

