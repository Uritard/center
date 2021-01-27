package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TCruiseTaskAttr;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-09-04
*/
@Service
public class TCruiseTaskAttrService{

    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTaskAttr tCruiseTaskAttr) {
        return this.tCruiseTaskAttrDao.insert(tCruiseTaskAttr);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskId) {
        return this.tCruiseTaskAttrDao.deleteByPrimaryId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTaskAttr tCruiseTaskAttr) {
        return this.tCruiseTaskAttrDao.update(tCruiseTaskAttr);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskAttr selectByPrimaryId(String taskId) {
        return this.tCruiseTaskAttrDao.selectByPrimaryId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskAttr> select(String taskId, Long instanceId, Long deviceMeteId, Long deviceId, String customId, String pointTaskId, Integer ifRobot, Integer ifVideo, Integer ifInferad, Integer ifArtificial) {
        List<TCruiseTaskAttr> tCruiseTaskAttrList = tCruiseTaskAttrDao.select(taskId, instanceId, deviceMeteId, deviceId, customId, pointTaskId, ifRobot, ifVideo, ifInferad, ifArtificial);
        return tCruiseTaskAttrList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskAttr> selectByPage(TCruiseTaskAttr tCruiseTaskAttr) {
        List<TCruiseTaskAttr> tCruiseTaskAttrList = tCruiseTaskAttrDao.selectByPage(tCruiseTaskAttr);
        return tCruiseTaskAttrList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTaskAttr> list) {
        return this.tCruiseTaskAttrDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public Set<Long> selectInstanceIdByTask(String taskId){
        return tCruiseTaskAttrDao.selectInstanceIdByTask(taskId);

    }

}

