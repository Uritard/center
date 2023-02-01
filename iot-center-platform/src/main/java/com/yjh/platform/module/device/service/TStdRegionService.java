package com.yjh.platform.module.device.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.AreaInfoRegionCode;
import com.yjh.platform.module.device.entity.StationVoltageData;
import com.yjh.platform.module.device.entity.TStdRegion;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author tt
* @since 2020-07-27
*/
@Service
public class TStdRegionService{

    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Resource
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TStdRegionService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdRegion tStdRegion) {
        if (StringUtils.isNotBlank(tStdRegion.getRegionCode())) {
            // 判断当前区域编码是否与本级系统的区域编码重复
            Integer count = this.countByRegionCode(tStdRegion.getRegionCode(), null);
            if (count > 0) {
                throw new BusinessException(201, "区域编码" + tStdRegion.getRegionCode() + "已存在");
            }
        }
        return this.tStdRegionDao.insert(tStdRegion);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long regionId) {
        List<Long> list = tStdRegionDao.selectDownId(regionId);
        if(list.size()>1){
            return -1;
        }
        List<Long> deviceList = tStdRegionDao.selectDevice(list);
        if(deviceList != null && deviceList.size() > 0){
            return -1;
        }
        return this.tStdRegionDao.deleteByPrimaryId(regionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TStdRegion tStdRegion) {
        String regionCode = tStdRegion.getRegionCode();
        if (StringUtils.isNotBlank(regionCode)) {
            List<TStdRegion> list = tStdRegionDao.selectIsIn(tStdRegion);
            if (CollectionUtils.isNotEmpty(list)) {
                throw new BusinessException(209,"编码与其他厂站区域重复");
            }
        }
        return this.tStdRegionDao.update(tStdRegion);
    }

//    @Transactional(rollbackFor = Exception.class)
//    public int update(Map<String, Object> map) {
//        if (map.get("regionCode").equals("")) map.replace("regionCode",0);
//        log.info("map: "+map);
//        return this.tStdRegionDao.updateByMap(map);
//    }

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
    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectDownId(Long  regionId) {
        return tStdRegionDao.selectDownId(regionId);
    }

    public int loadRegionIntoRedis() {
        List<TStdRegion> list = tStdRegionDao.select(null, null, null, null, null, null, null, null, null);
        Set<String> keys = redisTemplate.keys("region:*");
        // 删除所有区域信息重新加载
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
        for (TStdRegion item : list) {
            if (StringUtils.isNotEmpty(item.getRegionCode())) {
                Map<String, String> map = Object2Map.toStringMap(Object2Map.objectToMap(item, true));
                String str = "region:" + item.getRegionCode();
                redisTemplate.opsForHash().putAll(str, map);
            }
        }
        return 1;
    }

    public List<TStdRegion> cruiseTree() {
       List<TStdRegion> tStdRegionList = new ArrayList<>();

       TStdRegion tStdRegion = tStdRegionDao.selectCruiseTree(-1L);

       treeToList(tStdRegionList,tStdRegion);

       return tStdRegionList;
    }


    public List<TStdRegion> queryStationPosition() {
        return tStdRegionDao.selectByState(Constant.STATE_LOCAL);
    }

    public List<StationVoltageData> queryStationVoltage() {
        return tStdRegionDao.selectStationVoltageData();
    }

    // 判断当前区域编码是否在本级已经存在
    public Integer countByRegionCode(String regionCode, Long upRegionId) {
        return tStdRegionDao.countByRegionCode(regionCode, upRegionId);
    }

    private void treeToList(List<TStdRegion> tStdRegionList, TStdRegion tStdRegion) {
        if (CollectionUtils.isNotEmpty(tStdRegion.getChildren())) {
            List<TStdRegion> childrenList = tStdRegion.getChildren();
            for (TStdRegion stdRegion : childrenList) {
                if (StringUtils.isNotBlank(stdRegion.getRegionCode())) {
                    stdRegion.setChildren(new ArrayList<>());
                    tStdRegionList.add(stdRegion);
                }
                else {
                    treeToList(tStdRegionList,stdRegion);
                }
            }
        }
    }
}

