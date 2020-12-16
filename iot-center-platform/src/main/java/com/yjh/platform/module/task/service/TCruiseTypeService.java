package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TCruiseType;
import com.yjh.platform.module.task.dao.TCruiseTypeDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.yjh.platform.module.task.entity.TCruiseTypeDetail;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-11-17
*/
@Service
public class TCruiseTypeService{

    @Autowired
    private TCruiseTypeDao tCruiseTypeDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(Integer cruiseType,String instanceList,String remark) {
        List<String> list1= Arrays.asList(instanceList.split(","));
        List<TCruiseType> list = new ArrayList<>();
        for (String item:list1) {
            TCruiseType tCruiseType= new TCruiseType();
            tCruiseType.setSubType(cruiseType);
            tCruiseType.setInstanceId(Long.valueOf(item));
            tCruiseType.setRemark(remark);
            list.add(tCruiseType);
        }
        this.deleteByPrimaryId(cruiseType);
        return this.tCruiseTypeDao.batchAdd(list);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer subType) {
        return this.tCruiseTypeDao.deleteByPrimaryId(subType);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseType tCruiseType) {
        return this.tCruiseTypeDao.update(tCruiseType);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseType selectByPrimaryId(Integer subType) {
        return this.tCruiseTypeDao.selectByPrimaryId(subType);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTypeDetail> select(Integer subType) {
        List<TCruiseTypeDetail> tCruiseTypeList = tCruiseTypeDao.select(subType);
        return tCruiseTypeList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTypeDetail> selectByPage(TCruiseType tCruiseType) {
        List<TCruiseTypeDetail> tCruiseTypeList = tCruiseTypeDao.selectByPage(tCruiseType);
        return tCruiseTypeList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCruiseType> list) {
        return this.tCruiseTypeDao.batchAdd(list);
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String subType) {
    List<String> list1= Arrays.asList(subType.split(","));
    return this.tCruiseTypeDao.batchDelete(list1);
    }

    @Logs(title = "查询类型下的所有id", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectIdList(Integer subType) {
        return this.tCruiseTypeDao.selectIdList(subType);
    }

}

