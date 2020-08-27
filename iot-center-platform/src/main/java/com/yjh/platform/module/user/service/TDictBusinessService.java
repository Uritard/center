package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.TDictBusiness;
import com.yjh.platform.module.user.dao.TDictBusinessDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-04
*/
@Service
public class TDictBusinessService{

    @Autowired
    private TDictBusinessDao tDictBusinessDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TDictBusiness tDictBusiness) {
        return this.tDictBusinessDao.insert(tDictBusiness);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer dictId) {
        return this.tDictBusinessDao.deleteByPrimaryId(dictId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TDictBusiness tDictBusiness) {
        return this.tDictBusinessDao.update(tDictBusiness);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TDictBusiness selectByPrimaryId(Integer dictId) {
        return this.tDictBusinessDao.selectByPrimaryId(dictId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDictBusiness> select(Integer dictId, String dictCode, String colName, String dictNote, Integer upDict, String remark, Long sort) {
        List<TDictBusiness> tDictBusinessList = tDictBusinessDao.select(dictId, dictCode, colName, dictNote, upDict, remark, sort);
        return tDictBusinessList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDictBusiness> selectByPage(TDictBusiness tDictBusiness) {
        List<TDictBusiness> tDictBusinessList = tDictBusinessDao.selectByPage(tDictBusiness);
        return tDictBusinessList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TDictBusiness> list) {
        return this.tDictBusinessDao.batchInsert(list);
    }

    @Logs(title = "多类型查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectQuery() {
        Map<String,Object> result = new HashMap<>();
        List<TDictBusiness> tDictBusinessList = tDictBusinessDao.select(null,null,null,null,null,null,null);
        List<TDictBusiness> robotPosition = new ArrayList<>();
        List<TDictBusiness> robotType = new ArrayList<>();
        List<TDictBusiness> robotFactory = new ArrayList<>();
        List<TDictBusiness> robotUse = new ArrayList<>();
        for(TDictBusiness tDictBusiness : tDictBusinessList){
            if(tDictBusiness.getColName().equalsIgnoreCase("robot_position")){
                robotPosition.add(tDictBusiness);
            }else if (tDictBusiness.getColName().equalsIgnoreCase("robot_type")){
                robotType.add(tDictBusiness);
            }else if (tDictBusiness.getColName().equalsIgnoreCase("robot_factory")){
                robotFactory.add(tDictBusiness);
            }else if (tDictBusiness.getColName().equalsIgnoreCase("robot_use")){
                robotUse.add(tDictBusiness);
            }
        }
        result.put("robotPosition",robotPosition);
        result.put("robotType",robotType);
        result.put("robotFactory",robotFactory);
        result.put("robotUse",robotUse);
        return result;
    }

}

