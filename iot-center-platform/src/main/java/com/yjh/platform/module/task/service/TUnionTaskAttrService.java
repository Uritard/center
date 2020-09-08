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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TUnionTaskAttr tUnionTaskAttr) {
        return this.tUnionTaskAttrDao.insert(tUnionTaskAttr);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String unionId) {
        return this.tUnionTaskAttrDao.deleteByPrimaryId(unionId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TUnionTaskAttr tUnionTaskAttr) {
        return this.tUnionTaskAttrDao.update(tUnionTaskAttr);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TUnionTaskAttr selectByPrimaryId(String unionId) {
        return this.tUnionTaskAttrDao.selectByPrimaryId(unionId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskAttr> select(String unionId, Long instanceId, Long deviceMeteId, String deviceCustomId, Long pointTaskId, Integer ifRobot, Integer ifVideo, Integer ifInferad, Integer ifArtificial) {
        List<TUnionTaskAttr> tUnionTaskAttrList = tUnionTaskAttrDao.select(unionId, instanceId, deviceMeteId, deviceCustomId, pointTaskId, ifRobot, ifVideo, ifInferad, ifArtificial);
        return tUnionTaskAttrList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskAttr> selectByPage(TUnionTaskAttr tUnionTaskAttr) {
        List<TUnionTaskAttr> tUnionTaskAttrList = tUnionTaskAttrDao.selectByPage(tUnionTaskAttr);
        return tUnionTaskAttrList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TUnionTaskAttr> list) {
        return this.tUnionTaskAttrDao.batchInsert(list);
    }

}

