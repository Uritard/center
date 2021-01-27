package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TUnionTaskAttr;
import com.yjh.platform.module.task.dao.TUnionTaskAttrDao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-09-04
*/
@Service
public class TUnionTaskAttrService{

    @Autowired
    private TUnionTaskAttrDao tUnionTaskAttrDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TUnionTaskAttr tUnionTaskAttr) {
        return this.tUnionTaskAttrDao.insert(tUnionTaskAttr);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String unionId) {
        return this.tUnionTaskAttrDao.deleteByPrimaryId(unionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TUnionTaskAttr tUnionTaskAttr) {
        return this.tUnionTaskAttrDao.update(tUnionTaskAttr);
    }

    @Transactional(rollbackFor = Exception.class)
    public TUnionTaskAttr selectByPrimaryId(String unionId) {
        return this.tUnionTaskAttrDao.selectByPrimaryId(unionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskAttr> select(String unionId, Long instanceId, Long deviceMeteId, String deviceCustomId, Long pointTaskId, Integer ifRobot, Integer ifVideo, Integer ifInferad, Integer ifArtificial) {
        List<TUnionTaskAttr> tUnionTaskAttrList = tUnionTaskAttrDao.select(unionId, instanceId, deviceMeteId, deviceCustomId, pointTaskId, ifRobot, ifVideo, ifInferad, ifArtificial);
        return tUnionTaskAttrList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskAttr> selectByPage(TUnionTaskAttr tUnionTaskAttr) {
        List<TUnionTaskAttr> tUnionTaskAttrList = tUnionTaskAttrDao.selectByPage(tUnionTaskAttr);
        return tUnionTaskAttrList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TUnionTaskAttr> list) {
        return this.tUnionTaskAttrDao.batchInsert(list);
    }

}

