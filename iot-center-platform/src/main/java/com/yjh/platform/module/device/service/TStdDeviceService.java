package com.yjh.platform.module.device.service;

import com.github.pagehelper.parser.impl.HsqldbParser;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.dao.TStdDeviceAttrDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.dao.TStdDeviceDao;

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
        //tStdDevice.setCreateTime(tStdDeviceDetail.getCreateTime());
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

        int i = this.tStdDeviceDao.add(tStdDevice);
        TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
        tStdDeviceAttr.setDepartment(tStdDeviceDetail.getDepartment());
        tStdDeviceAttr.setDeviceId(Long.valueOf(tStdDevice.getDeviceId()));
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
        return this.tStdDeviceDao.deleteByPrimaryId(deviceId)+tStdDeviceAttrDao.deleteByPrimaryId(deviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDevice tStdDevice) {
        return this.tStdDeviceDao.update(tStdDevice);
    }

    @Logs(title = "更新设备及属性", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int updateAll(TStdDeviceDetail tStdDeviceDetail) {
        TStdDevice tStdDevice = new TStdDevice();
        tStdDevice.setAliasName(tStdDeviceDetail.getAliasName());
        tStdDevice.setCreateTime(tStdDeviceDetail.getCreateTime());
        tStdDevice.setCustomId(tStdDeviceDetail.getCustomId());
        tStdDevice.setCustomName(tStdDeviceDetail.getCustomName());
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

        TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
        tStdDeviceAttr.setDepartment(tStdDeviceDetail.getDepartment());
        tStdDeviceAttr.setDeviceId(Long.valueOf(tStdDevice.getDeviceId()));
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
        return this.tStdDeviceDao.update(tStdDevice)+ this.tStdDeviceAttrDao.update(tStdDeviceAttr);
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
    public List<TStdDeviceDetail> selectByPageAll(TStdDeviceDetail tStdDeviceDetail) {
        return this.tStdDeviceDao.selectByPageAll(tStdDeviceDetail);
    }

    @Logs(title = "根据设备ID查询上级区域信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdRegion selectRegionById(Long deviceId) {
        return this.tStdDeviceDao.selectRegionById(deviceId);
    }

    @Logs(title = "设备树查询(区域-间隔5-设备6-部位7,所有设备all-设备dev-摄像头camera-机器人-robot)", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectDevTree(String level, String deviceShow) {
        List<AreaInfo> listTree = new ArrayList<>();
        if (Objects.equals(deviceShow, "dev")) {
            switch (level) {
                case "7":
                    listTree = this.tStdDeviceDao.selectDevTreeCustom();
                    break;
                case "6":
                    listTree = this.tStdDeviceDao.selectDevTreeDevice();
                    break;
                case "5":
                    listTree = this.tStdDeviceDao.selectDevTreeRegion();
                    break;
                default:
                    throw new BusinessException("设备类型输入有误！");
            }
        } else if (Objects.equals(deviceShow, "camera")) {
            switch (level) {
                case "6":
                    listTree = this.tCameraInfoDao.selectCameraTreeDevice();
                    break;
                case "5":
                    listTree = this.tStdDeviceDao.selectDevTreeRegion();
                    break;
                default:
                    throw new BusinessException("设备类型输入有误！");
            }
        } else if (Objects.equals(deviceShow, "robot")) {
            switch (level) {
                case "6":
                    listTree = this.tStdDeviceDao.selectRobotTree();
                    break;
                default:
                    throw new BusinessException("类型输入有误！");
            }
        } else if (Objects.equals(deviceShow, "all")) {
            switch (level) {
                case "5":
                    listTree = this.tStdDeviceDao.selectDevTreeRegion();
                    break;
                case "7":
                    listTree = this.tStdDeviceDao.selectAllTreeCustom();
                    break;
                case "6":
                    listTree = this.tStdDeviceDao.selectAllTreeDevice();
                    break;
                default:
                    throw new BusinessException("设备类型输入有误！");
            }
        } else { throw new BusinessException("设备类型输入有误！");}
        List<AreaInfo> areaInfoCountryList = new ArrayList<>();
        System.out.println("listTree: "+listTree);
        for(Iterator<AreaInfo> it = listTree.iterator();it.hasNext();){
            AreaInfo areaInfoMap = it.next();
            if (Objects.equals(areaInfoMap.getUpId(), null)) {
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

    @Logs(title = "区域树模糊查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectRegionTreeByName(String regionName) {
        List<AreaInfo> listTree = new ArrayList<>();
        List<AreaInfo> listTreeAll = this.tStdDeviceDao.selectDevTreeRegion();
        if (Objects.equals(null, regionName) || regionName.equals("")) {
            List<AreaInfo> areaInfoCountryList = new ArrayList<>();
            for(Iterator<AreaInfo> it = listTreeAll.iterator();it.hasNext();){
                AreaInfo areaInfoMap = it.next();
                if (Objects.equals(areaInfoMap.getUpId(), null)) {
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
                if (areaInfoRegionCode.getUpId() != null) {
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
                if (Objects.equals(areaInfoMap.getUpId(), null)) {
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
        if (areaInfoAll.getUpId() != null) {
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
        return this.tStdDeviceDao.batchDelete(list1);
    }

}

