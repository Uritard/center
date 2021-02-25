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
    public int add(Integer cruiseType,String instanceList) {
        if("".equals(instanceList)){
           return this.deleteByPrimaryId(cruiseType);
        }
        List<String> list1= Arrays.asList(instanceList.split(","));//修改或者新增的
        List<Long> listNew = new ArrayList<>();
        for(String item:list1){
            listNew.add(Long.valueOf(item));
        }
        List<TCruiseTypeDetail> list = new ArrayList<>();
        if(list1 != null && list1.size()>0){
            tCruiseTypeDao.batchDeleteByInstance(listNew);//先把库里有listNew的点删除掉
        }
        List<Long> listHave= tCruiseTypeDao.selectIdList(cruiseType);//此时查出来的是要标记删除的
        //此时listHave中剩下的都是此次新增中所不需要的点 需要标记删除
        tCruiseTypeDao.tagsDeleted(listHave);//将库里的标记删除了
        for (String item:list1) {
            TCruiseTypeDetail tCruiseType= new TCruiseTypeDetail();
            tCruiseType.setSubType(cruiseType);
            tCruiseType.setInstanceId(Long.valueOf(item));
            list.add(tCruiseType);
        }
        return this.tCruiseTypeDao.batchAdd(list);//将此次的点新增
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer subType) {
        return this.tCruiseTypeDao.deleteByPrimaryId(subType);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(Integer subType) {
        List<TCruiseTypeDetail> tCruiseTypeList = new ArrayList<>();
        if(subType == 213 ){
            //库里有的  包括标记删除的
            List<Long> listHave = tCruiseTypeDao.selectIdListByAll(subType);
            //巡视表里有的
            List<TCruiseTypeDetail> listNew = tCruiseTypeDao.selectAll();
            List<TCruiseTypeDetail> listForAdd = new ArrayList<>();
            if(listHave != null && listHave.size()>0 ){
                for(int i = 0;i < listNew.size();i++){
                    if( !listHave.contains(listNew.get(i).getInstanceId())){
                        listForAdd.add(listNew.get(i));//找出listNew中不存在于listHave中的点
                    }
                }
            }
            //将删除已存在的listNew插库
            if(listForAdd != null && listForAdd.size()>0){
                return tCruiseTypeDao.batchAdd(listForAdd);
            }else {
                return 1;
            }
        }
        if(subType == 322 ){
            //库里有的  包括标记删除的
            List<Long> listHave = tCruiseTypeDao.selectIdListByAll(subType);
            //巡视表里有的
            List<TCruiseTypeDetail> listNew = tCruiseTypeDao.selectByAnalyse();
            List<TCruiseTypeDetail> listForAdd = new ArrayList<>();
            if(listHave != null && listHave.size()>0 ){
                for(int i = 0;i < listNew.size();i++){
                    if( !listHave.contains(listNew.get(i).getInstanceId())){
                        listForAdd.add(listNew.get(i));//找出listNew中不存在于listHave中的点
                    }
                }
            }
            //将删除已存在的listNew插库
            if(listForAdd != null && listForAdd.size()>0){
                return tCruiseTypeDao.batchAdd(listForAdd);
            }else {
                return 1;
            }
        }
        if(subType == 323 ){
            //库里有的  包括标记删除的
            List<Long> listHave = tCruiseTypeDao.selectIdListByAll(subType);
            //巡视表里有的
            List<TCruiseTypeDetail> listNew = tCruiseTypeDao.selectByMeterTypeByOil();
            List<TCruiseTypeDetail> listForAdd = new ArrayList<>();
            if(listHave != null && listHave.size()>0 ){
                for(int i = 0;i < listNew.size();i++){
                    if( !listHave.contains(listNew.get(i).getInstanceId())){
                        listForAdd.add(listNew.get(i));//找出listNew中不存在于listHave中的点
                    }
                }
            }
            //将删除已存在的listNew插库
            if(listForAdd != null && listForAdd.size()>0){
                return tCruiseTypeDao.batchAdd(listForAdd);
            }else {
                return 1;
            }
        }
        if(subType == 324 ){
            //库里有的  包括标记删除的
            List<Long> listHave = tCruiseTypeDao.selectIdListByAll(subType);
            //巡视表里有的
            List<TCruiseTypeDetail> listNew = tCruiseTypeDao.selectByMeterTypeByArrester();
            List<TCruiseTypeDetail> listForAdd = new ArrayList<>();
            if(listHave != null && listHave.size()>0 ){
                for(int i = 0;i < listNew.size();i++){
                    if( !listHave.contains(listNew.get(i).getInstanceId())){
                        listForAdd.add(listNew.get(i));//找出listNew中不存在于listHave中的点
                    }
                }
            }
            //将删除已存在的listNew插库
            if(listForAdd != null && listForAdd.size()>0){
                return tCruiseTypeDao.batchAdd(listForAdd);
            }else {
                return 1;
            }
        }
        if(subType == 325 ){
            //库里有的  包括标记删除的
            List<Long> listHave = tCruiseTypeDao.selectIdListByAll(subType);
            //巡视表里有的
            List<TCruiseTypeDetail> listNew = tCruiseTypeDao.selectByMeterTypeBySF6();
            List<TCruiseTypeDetail> listForAdd = new ArrayList<>();
            if(listHave != null && listHave.size()>0 ){
                for(int i = 0;i < listNew.size();i++){
                    if( !listHave.contains(listNew.get(i).getInstanceId())){
                        listForAdd.add(listNew.get(i));//找出listNew中不存在于listHave中的点
                    }
                }
            }
            //将删除已存在的listNew插库
            if(listForAdd != null && listForAdd.size()>0){
                return tCruiseTypeDao.batchAdd(listForAdd);
            }else {
                return 1;
            }
        }
        if(subType == 326 ){
            //库里有的  包括标记删除的
            List<Long> listHave = tCruiseTypeDao.selectIdListByAll(subType);
            //巡视表里有的
            List<TCruiseTypeDetail> listNew = tCruiseTypeDao.selectByMeterTypeByYY();
            List<TCruiseTypeDetail> listForAdd = new ArrayList<>();
            if(listHave != null && listHave.size()>0 ){
                for(int i = 0;i < listNew.size();i++){
                    if( !listHave.contains(listNew.get(i).getInstanceId())){
                        listForAdd.add(listNew.get(i));//找出listNew中不存在于listHave中的点
                    }
                }
            }
            //将删除已存在的listNew插库
            if(listForAdd != null && listForAdd.size()>0){
                return tCruiseTypeDao.batchAdd(listForAdd);
            }else {
                return 1;
            }
        }
        if(subType == 327 ){
            //库里有的  包括标记删除的
            List<Long> listHave = tCruiseTypeDao.selectIdListByAll(subType);
            //巡视表里有的
            List<TCruiseTypeDetail> listNew = tCruiseTypeDao.selectByAnalyseByWg();
            List<TCruiseTypeDetail> listForAdd = new ArrayList<>();
            if(listHave != null && listHave.size()>0 ){
                for(int i = 0;i < listNew.size();i++){
                    if( !listHave.contains(listNew.get(i).getInstanceId())){
                        listForAdd.add(listNew.get(i));//找出listNew中不存在于listHave中的点
                    }
                }
            }
            //将删除已存在的listNew插库
            if(listForAdd != null && listForAdd.size()>0){
                return tCruiseTypeDao.batchAdd(listForAdd);
            }else {
                return 1;
            }
        }
        return 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTypeDetail> selectByPrimaryId(Integer subType) {
        return this.tCruiseTypeDao.selectByPrimaryId(subType);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTypeDetail> select(Integer subType) {
        return tCruiseTypeDao.select(subType);
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
    List<Long> list = new ArrayList<>();
    for(String item:list1){
        list.add(Long.valueOf(item));
    }
    return this.tCruiseTypeDao.batchDelete(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectIdList(Integer subType) {
        return this.tCruiseTypeDao.selectIdList(subType);
    }



}

