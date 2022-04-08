package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TDroneRegionDao;
import com.yjh.accessrobot.module.command.entity.TDroneRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author YC
* @since 2021-01-28
*/
@Service
public class TDroneRegionService {

    @Autowired
    private TDroneRegionDao tDroneRegionDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TDroneRegion tDroneRegion) {
        return this.tDroneRegionDao.insert(tDroneRegion);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String regionId) {
        return this.tDroneRegionDao.deleteByPrimaryId(regionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TDroneRegion tDroneRegion) {
        return this.tDroneRegionDao.update(tDroneRegion);
    }

    @Transactional(rollbackFor = Exception.class)
    public TDroneRegion selectByPrimaryId(String regionId) {
        return this.tDroneRegionDao.selectByPrimaryId(regionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TDroneRegion> select(String regionId, String regionName, Integer sort, Integer deviceType, String upRegionId, String upRegionIds, Integer regionType, String stationId, Integer state, Long droneId, Date createTime) {
        List<TDroneRegion> tDroneRegionList = tDroneRegionDao.select(regionId, regionName, sort, deviceType, upRegionId, upRegionIds, regionType, stationId, state, droneId,createTime);
        return tDroneRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TDroneRegion> selectByPage(TDroneRegion tDroneRegion) {
        List<TDroneRegion> tDroneRegionList = tDroneRegionDao.selectByPage(tDroneRegion);
        return tDroneRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TDroneRegion> list) {
        return this.tDroneRegionDao.batchInsert(list);
    }

}

