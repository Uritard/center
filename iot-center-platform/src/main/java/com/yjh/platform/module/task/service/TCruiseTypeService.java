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

    @Transactional(rollbackFor = Exception.class)
    public int add(Integer cruiseType,String instanceList,String remark) {
        if("".equals(instanceList)){
           return this.deleteByPrimaryId(cruiseType);
        }
        List<String> list1= Arrays.asList(instanceList.split(","));
        List<TCruiseTypeDetail> list = new ArrayList<>();
        for (String item:list1) {
            TCruiseTypeDetail tCruiseType= new TCruiseTypeDetail();
            tCruiseType.setSubType(cruiseType);
            tCruiseType.setInstanceId(Long.valueOf(item));
            tCruiseType.setRemark(remark);
            list.add(tCruiseType);
        }
        this.deleteByPrimaryId(cruiseType);
        return this.tCruiseTypeDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer subType) {
        return this.tCruiseTypeDao.deleteByPrimaryId(subType);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseType tCruiseType) {
        return this.tCruiseTypeDao.update(tCruiseType);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseType selectByPrimaryId(Integer subType) {
        return this.tCruiseTypeDao.selectByPrimaryId(subType);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTypeDetail> select(Integer subType) {
        List<TCruiseTypeDetail> tCruiseTypeList = new ArrayList<>();
        if(subType == 213 ){
            //全面巡视
            tCruiseTypeList = tCruiseTypeDao.select(subType);
            if(tCruiseTypeList == null || tCruiseTypeList.size()==0){
                //库里没有
                tCruiseTypeList = tCruiseTypeDao.selectAll();
            }
        }
        if(subType == 322 ){
            //红外测温
            tCruiseTypeList = tCruiseTypeDao.select(subType);
            if(tCruiseTypeList == null || tCruiseTypeList.size()==0){
                //库里没有
                tCruiseTypeList = tCruiseTypeDao.selectByAnalyse();
            }
        }
        if(subType == 323 ){
            //油温有位
            tCruiseTypeList = tCruiseTypeDao.select(subType);
            if(tCruiseTypeList == null || tCruiseTypeList.size()==0){
                //库里没有
                tCruiseTypeList = tCruiseTypeDao.selectByMeterTypeByOil();
            }
        }
        if(subType == 324 ){
            //避雷器
            tCruiseTypeList = tCruiseTypeDao.select(subType);
            if(tCruiseTypeList == null || tCruiseTypeList.size()==0){
                //库里没有
                tCruiseTypeList = tCruiseTypeDao.selectByMeterTypeByArrester();
            }
        }
        if(subType == 325 ){
            //SF6
            tCruiseTypeList = tCruiseTypeDao.select(subType);
            if(tCruiseTypeList == null || tCruiseTypeList.size()==0){
                //库里没有
                tCruiseTypeList = tCruiseTypeDao.selectByMeterTypeBySF6();
            }
        }
        if(subType == 326 ){
            //液压表
            tCruiseTypeList = tCruiseTypeDao.select(subType);
            if(tCruiseTypeList == null || tCruiseTypeList.size()==0){
                //库里没有
                tCruiseTypeList = tCruiseTypeDao.selectByMeterTypeByYY();
            }
        }
        if(subType == 327 ){
            //位置状态识别
            tCruiseTypeList = tCruiseTypeDao.select(subType);
            if(tCruiseTypeList == null || tCruiseTypeList.size()==0){
                //库里没有
                tCruiseTypeList = tCruiseTypeDao.selectByAnalyseByWg();
            }
        }
        this.batchAdd(tCruiseTypeList);
        return tCruiseTypeList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTypeDetail> selectByPage(TCruiseType tCruiseType) {
        List<TCruiseTypeDetail> tCruiseTypeList = tCruiseTypeDao.selectByPage(tCruiseType);
        return tCruiseTypeList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCruiseTypeDetail> list) {
        return this.tCruiseTypeDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String subType) {
    List<String> list1= Arrays.asList(subType.split(","));
    return this.tCruiseTypeDao.batchDelete(list1);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectIdList(Integer subType) {
        return this.tCruiseTypeDao.selectIdList(subType);
    }

}

