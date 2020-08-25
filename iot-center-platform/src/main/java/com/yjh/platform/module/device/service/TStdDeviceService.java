package com.yjh.platform.module.device.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.AreaInfoRegionCode;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.dao.TStdDeviceDao;

import java.util.*;
import java.util.stream.Collectors;


import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
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
    private TCameraInfoDao tCameraInfoDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdDevice tStdDevice) {
        return this.tStdDeviceDao.add(tStdDevice);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceId) {
        return this.tStdDeviceDao.deleteByPrimaryId(deviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDevice tStdDevice) {
        return this.tStdDeviceDao.update(tStdDevice);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdDevice selectByPrimaryId(Long deviceId) {
        return this.tStdDeviceDao.selectByPrimaryId(deviceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDevice> select(Long deviceId, String customId, String deviceCode, String deviceName, String aliasName, Integer deviceType, String positionType, Long modelId, String regionPath, Long upRegionId, String upRegionName, String customName, Integer customType, Integer status, Date updateTime, Date createTime) {
        List<TStdDevice> tStdDeviceList = tStdDeviceDao.select(deviceId, customId, deviceCode, deviceName, aliasName, deviceType, positionType, modelId, regionPath, upRegionId, upRegionName, customName, customType, status, updateTime, createTime);
        return tStdDeviceList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdDevice> selectByPage(TStdDevice tStdDevice) {
        List<TStdDevice> tStdDeviceList = tStdDeviceDao.selectByPage(tStdDevice);
        return tStdDeviceList;
    }

    @Logs(title = "根据设备ID查询上级区域信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdRegion selectRegionById(Long deviceId) {
        return this.tStdDeviceDao.selectRegionById(deviceId);
    }

    @Logs(title = "设备树查询(区域-间隔5-设备6-部位7,所有设备all-设备dev-摄像头camera)", code = "module")
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
    public int batchDelete(List<String> list){
        return this.tStdDeviceDao.batchDelete(list);
    }

}

