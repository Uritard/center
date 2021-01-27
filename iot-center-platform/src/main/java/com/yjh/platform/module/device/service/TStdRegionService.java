package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.AreaInfoRegionCode;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.device.dao.TStdRegionDao;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-07-27
*/
@Service
public class TStdRegionService{

    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdRegion tStdRegion) {
        return this.tStdRegionDao.insert(tStdRegion);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long regionId) {
        List<Long> list = tStdRegionDao.selectDownId(regionId);
        if(list != null && list.size() > 0){
            return tStdRegionDao.batchDelete(list);
        }else {
            return this.tStdRegionDao.deleteByPrimaryId(regionId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TStdRegion tStdRegion) {
        return this.tStdRegionDao.update(tStdRegion);
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdRegion selectByPrimaryId(Long regionId) {
        return this.tStdRegionDao.selectByPrimaryId(regionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdRegion> select(Long regionId, String regionName, Integer sort, Long upRegionId, String upRegionIds, Integer regionCode, String stationId, Integer state, Date createTime) {
        List<TStdRegion> tStdRegionList = tStdRegionDao.select(regionId, regionName, sort, upRegionId, upRegionIds, regionCode, stationId, state, createTime);
        return tStdRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdRegion> selectByPage(TStdRegion tStdRegion) {
        List<TStdRegion> tStdRegionList = tStdRegionDao.selectByPage(tStdRegion);
        return tStdRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoRegionCode> selectRegTreeByRegName(String regionName) {

        List<AreaInfoRegionCode> listTree = new ArrayList<>();
        List<AreaInfoRegionCode> listTreeAll = this.tStdRegionDao.selectAreaTree();
        if (Objects.equals(null, regionName) || regionName.equals("")) {
            List<AreaInfoRegionCode> areaInfoRegionCodeList = new ArrayList<>();
            Integer level = 1;
            for (AreaInfoRegionCode areaInfoRegionCode:listTreeAll) {
                if (areaInfoRegionCode.getUpId()==null || areaInfoRegionCode.getUpId()==-1) {
                    areaInfoRegionCode.setLevel(level);
                    areaInfoRegionCodeList.add(areaInfoRegionCode);
                }
            }
            diGui(areaInfoRegionCodeList, listTreeAll, level);
            return areaInfoRegionCodeList;
        }

        List<AreaInfoRegionCode> listTreeByName = this.tStdRegionDao.selectRegTreeByRegName(regionName);;
        if (listTreeByName.size()>0) {
            for (AreaInfoRegionCode areaInfoRegionCode : listTreeByName) {
                listTree.add(areaInfoRegionCode);
                if (areaInfoRegionCode.getUpId() != null && areaInfoRegionCode.getUpId() != -1) {
                    Long areaInfoRegionCodeUpId = areaInfoRegionCode.getUpId();
                    System.out.println("areaInfoRegionCodeUpId: "+areaInfoRegionCodeUpId);
                    for (AreaInfoRegionCode areaInfoRegionCodeAll : listTreeAll) {
                        if (Objects.equals(areaInfoRegionCodeUpId, areaInfoRegionCodeAll.getId())) {
                            listTree.add(areaInfoRegionCodeAll);
                            diGuiMoHu(areaInfoRegionCodeAll, listTreeAll, listTree);
                        }
                    }
                }
            }
        }
        listTree = listTree.stream().distinct().collect(Collectors.toList());
        System.out.println("listTree: "+listTree);
        List<AreaInfoRegionCode> areaInfoRegionCodeStartList = new ArrayList<>();
        Integer level = 1;
        for (AreaInfoRegionCode areaInfoRegionCode:listTree) {
            if (areaInfoRegionCode.getUpId()==null || areaInfoRegionCode.getUpId()==-1) {
                areaInfoRegionCode.setLevel(level);
                areaInfoRegionCodeStartList.add(areaInfoRegionCode);
            }
        }
        diGui(areaInfoRegionCodeStartList, listTree, level);
        return areaInfoRegionCodeStartList;

    }

    private void diGui(List<AreaInfoRegionCode> areaInfoList, List<AreaInfoRegionCode> listTree, Integer level) {
        for(AreaInfoRegionCode areaInfo : areaInfoList){
            List<AreaInfoRegionCode> childrenList = new ArrayList<>();
            for (AreaInfoRegionCode areaInfoMap : listTree) {
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    areaInfoMap.setLevel(areaInfo.getLevel()+1);
                    childrenList.add(areaInfoMap);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree, level);
            }
        }
    }

    private void diGuiMoHu(AreaInfoRegionCode areaInfoRegionCodeAll, List<AreaInfoRegionCode> listTreeAll, List<AreaInfoRegionCode> listTree) {
        if (areaInfoRegionCodeAll.getUpId() != null && areaInfoRegionCodeAll.getUpId() != -1) {
            Long areaInfoRegionCodeAllUpId = areaInfoRegionCodeAll.getUpId();
            for (AreaInfoRegionCode areaInfoRegionCode : listTreeAll) {
                if (Objects.equals(areaInfoRegionCodeAllUpId, areaInfoRegionCode.getId())) {
                    listTree.add(areaInfoRegionCode);
                    diGuiMoHu(areaInfoRegionCode, listTreeAll, listTree);
                }
            }
        }
    }

}

