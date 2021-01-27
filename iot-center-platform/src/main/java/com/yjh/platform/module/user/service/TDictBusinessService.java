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

    @Transactional(rollbackFor = Exception.class)
    public int insert(TDictBusiness tDictBusiness) {
        return this.tDictBusinessDao.insert(tDictBusiness);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer dictId) {
        return this.tDictBusinessDao.deleteByPrimaryId(dictId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TDictBusiness tDictBusiness) {
        return this.tDictBusinessDao.update(tDictBusiness);
    }

    @Transactional(rollbackFor = Exception.class)
    public TDictBusiness selectByPrimaryId(Integer dictId) {
        return this.tDictBusinessDao.selectByPrimaryId(dictId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TDictBusiness> select(Integer dictId, String dictCode, String colName, String dictNote, Integer upDict, String remark, Long sort) {
        List<TDictBusiness> tDictBusinessList = tDictBusinessDao.select(dictId, dictCode, colName, dictNote, upDict, remark, sort);
        return tDictBusinessList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TDictBusiness> selectByPage(TDictBusiness tDictBusiness) {
        List<TDictBusiness> tDictBusinessList = tDictBusinessDao.selectByPage(tDictBusiness);
        return tDictBusinessList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TDictBusiness> list) {
        return this.tDictBusinessDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public  List<TDictBusiness> selectQuery(List<String> colNames) {
        return tDictBusinessDao.selectQuery(colNames);
    }

}

