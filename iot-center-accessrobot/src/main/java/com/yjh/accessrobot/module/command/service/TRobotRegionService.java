package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TRobotRegionDao;
import com.yjh.accessrobot.module.command.entity.TRobotRegion;
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
public class TRobotRegionService {

    @Autowired
    private TRobotRegionDao tRobotRegionDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotRegion tRobotRegion) {
        return this.tRobotRegionDao.insert(tRobotRegion);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String regionId) {
        return this.tRobotRegionDao.deleteByPrimaryId(regionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotRegion tRobotRegion) {
        return this.tRobotRegionDao.update(tRobotRegion);
    }

    @Transactional(rollbackFor = Exception.class)
    public TRobotRegion selectByPrimaryId(String regionId) {
        return this.tRobotRegionDao.selectByPrimaryId(regionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRobotRegion> select(String regionId, String regionName, Integer sort, Integer deviceType, String upRegionId, String upRegionIds, Integer regionType, String stationId, Integer state, Long robotId, Date createTime) {
        List<TRobotRegion> tRobotRegionList = tRobotRegionDao.select(regionId, regionName, sort, deviceType, upRegionId, upRegionIds, regionType, stationId, state, robotId,createTime);
        return tRobotRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRobotRegion> selectByPage(TRobotRegion tRobotRegion) {
        List<TRobotRegion> tRobotRegionList = tRobotRegionDao.selectByPage(tRobotRegion);
        return tRobotRegionList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotRegion> list) {
        return this.tRobotRegionDao.batchInsert(list);
    }

}

