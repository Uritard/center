package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TCruisePointAttr;
import com.yjh.platform.module.device.dao.TCruisePointAttrDao;

import java.util.List;
import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-10-13
*/
@Service
public class TCruisePointAttrService{

    @Autowired
    private TCruisePointAttrDao tCruisePointAttrDao;


    @Transactional(rollbackFor = Exception.class)
    public int add(TCruisePointAttr tCruisePointAttr) {
        return this.tCruisePointAttrDao.add(tCruisePointAttr);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long instanceId) {
        return this.tCruisePointAttrDao.deleteByPrimaryId(instanceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePointAttr tCruisePointAttr) {
        return this.tCruisePointAttrDao.update(tCruisePointAttr);
    }


    @Transactional(rollbackFor = Exception.class)
    public TCruisePointAttr selectByPrimaryId(Long instanceId) {
        return this.tCruisePointAttrDao.selectByPrimaryId(instanceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointAttr> select(Long instanceId, String instanceName, String attrName, String attrValue, String remark1) {
        List<TCruisePointAttr> tCruisePointAttrList = tCruisePointAttrDao.select(instanceId, instanceName, attrName, attrValue, remark1);
        return tCruisePointAttrList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointAttr> selectByPage(TCruisePointAttr tCruisePointAttr) {
        List<TCruisePointAttr> tCruisePointAttrList = tCruisePointAttrDao.selectByPage(tCruisePointAttr);
        return tCruisePointAttrList;
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCruisePointAttr> list) {
        return this.tCruisePointAttrDao.batchAdd(list);
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String instanceId) {
    List<String> list1= Arrays.asList(instanceId.split(","));
    return this.tCruisePointAttrDao.batchDelete(list1);
    }



}

