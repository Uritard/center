package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.TSysParam;
import com.yjh.platform.module.user.dao.TSysParamDao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-07
*/
@Service
public class TSysParamService{

    @Autowired
    private TSysParamDao tSysParamDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TSysParam tSysParam) {
        return this.tSysParamDao.insert(tSysParam);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer paramId) {
        return this.tSysParamDao.deleteByPrimaryId(paramId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TSysParam tSysParam) {
        return this.tSysParamDao.update(tSysParam);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByPrimaryId(Integer paramId) {
        return this.tSysParamDao.selectByPrimaryId(paramId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TSysParam> select(Integer paramId, String paramType, String paramName, String content, String remark) {
        List<TSysParam> tSysParamList = tSysParamDao.select(paramId, paramType, paramName, content, remark);
        return tSysParamList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TSysParam> selectByPage(TSysParam tSysParam) {
        List<TSysParam> tSysParamList = tSysParamDao.selectByPage(tSysParam);
        return tSysParamList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TSysParam> list) {
        return this.tSysParamDao.batchInsert(list);
    }

}

