package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.AreaInfoRegionCode;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.device.dao.TStdRegionDao;

import java.util.*;

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

    @Logs(title = "插入", code = "module",content = "根据页面传入的参数新增数据")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TStdRegion tStdRegion) {
        return this.tStdRegionDao.insert(tStdRegion);
    }

    @Logs(title = "删除", code = "module",content = "根据页面传入的参数删除数据")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long regionId) {
        return this.tStdRegionDao.deleteByPrimaryId(regionId);
    }

    @Logs(title = "更新", code = "module",content = "根据页面传入的参数修改数据")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdRegion tStdRegion) {
        return this.tStdRegionDao.update(tStdRegion);
    }

    @Logs(title = "主键查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public TStdRegion selectByPrimaryId(Long regionId) {
        return this.tStdRegionDao.selectByPrimaryId(regionId);
    }

    @Logs(title = "查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdRegion> select(Long regionId, String regionName, Integer sort, Long upRegionId, String upRegionIds, Integer regionCode, String stationId, Integer state, Date createTime) {
        List<TStdRegion> tStdRegionList = tStdRegionDao.select(regionId, regionName, sort, upRegionId, upRegionIds, regionCode, stationId, state, createTime);
        return tStdRegionList;
    }

    @Logs(title = "分页查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdRegion> selectByPage(TStdRegion tStdRegion) {
        List<TStdRegion> tStdRegionList = tStdRegionDao.selectByPage(tStdRegion);
        return tStdRegionList;
    }

    @Logs(title = "根据区域名称模糊查询区域树", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoRegionCode> selectRegTreeByRegName(String regionName) {
        List<AreaInfoRegionCode> listTree = this.tStdRegionDao.selectRegTreeByRegName(regionName);
        List<AreaInfoRegionCode> areaInfoCountryList = new ArrayList<>();
        Integer level = 1;
        for(Iterator<AreaInfoRegionCode> it = listTree.iterator(); it.hasNext();){
            AreaInfoRegionCode areaInfo = it.next();
            if (areaInfo.getUpId() == null || areaInfo.getUpId() == 0) {
                areaInfo.setLevel(1);
                areaInfoCountryList.add(areaInfo);
            }
        }
        diGui(areaInfoCountryList, listTree, level);
        return areaInfoCountryList;
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

}

