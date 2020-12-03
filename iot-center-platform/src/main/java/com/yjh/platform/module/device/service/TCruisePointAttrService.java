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

    @Logs(title = "插入", code = "module",content = "根据页面传入的参数新增数据")
    @Transactional(rollbackFor = Exception.class)
    public int add(TCruisePointAttr tCruisePointAttr) {
        return this.tCruisePointAttrDao.add(tCruisePointAttr);
    }

    @Logs(title = "删除", code = "module",content = "根据页面传入的参数删除数据")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long instanceId) {
        return this.tCruisePointAttrDao.deleteByPrimaryId(instanceId);
    }

    @Logs(title = "更新", code = "module",content = "根据页面传入的参数更新数据")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePointAttr tCruisePointAttr) {
        return this.tCruisePointAttrDao.update(tCruisePointAttr);
    }

    @Logs(title = "主键查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePointAttr selectByPrimaryId(Long instanceId) {
        return this.tCruisePointAttrDao.selectByPrimaryId(instanceId);
    }

    @Logs(title = "查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointAttr> select(Long instanceId, String instanceName, String attrName, String attrValue, String remark1) {
        List<TCruisePointAttr> tCruisePointAttrList = tCruisePointAttrDao.select(instanceId, instanceName, attrName, attrValue, remark1);
        return tCruisePointAttrList;
    }

    @Logs(title = "分页查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointAttr> selectByPage(TCruisePointAttr tCruisePointAttr) {
        List<TCruisePointAttr> tCruisePointAttrList = tCruisePointAttrDao.selectByPage(tCruisePointAttr);
        return tCruisePointAttrList;
    }

    @Logs(title = "批量插入", code = "module",content = "根据页面传入的参数批量插入数据")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCruisePointAttr> list) {
        return this.tCruisePointAttrDao.batchAdd(list);
    }

    @Logs(title = "批量删除", code = "module",content = "根据页面传入的参数批量删除数据")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String instanceId) {
    List<String> list1= Arrays.asList(instanceId.split(","));
    return this.tCruisePointAttrDao.batchDelete(list1);
    }



}

