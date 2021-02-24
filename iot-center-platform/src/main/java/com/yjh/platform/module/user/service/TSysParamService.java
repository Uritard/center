package com.yjh.platform.module.user.service;

import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.module.user.entity.TSysParam;
import com.yjh.platform.module.user.dao.TSysParamDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
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
    @Autowired
    private RedisTemplate redisTemplate;


    @Transactional(rollbackFor = Exception.class)
    public int insert(TSysParam tSysParam) {
         this.tSysParamDao.insert(tSysParam);
         return this.insertIntoRedis();
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer paramId) {
        this.tSysParamDao.deleteByPrimaryId(paramId);
        return this.insertIntoRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TSysParam tSysParam) {
        this.tSysParamDao.update(tSysParam);
        return this.insertIntoRedis();
    }


    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByPrimaryId(Integer paramId) {
        return this.tSysParamDao.selectByPrimaryId(paramId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TSysParam> select(Integer paramId,String paramCode, String paramType, String paramName, String content, String remark) {
        List<TSysParam> tSysParamList = tSysParamDao.select(paramId,paramCode,paramType, paramName, content, remark);
        return tSysParamList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TSysParam> selectByPage(Integer paramId,String paramCode, String paramType, String paramName, String content, String remark) {
        List<TSysParam> tSysParamList = tSysParamDao.selectByPage(paramId,paramCode, paramType, paramName, content, remark);
        return tSysParamList;
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TSysParam> list) {
        this.tSysParamDao.batchInsert(list);
        return this.insertIntoRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertIntoRedis(){
        List<TSysParam> list = this.tSysParamDao.selectAll();
        for (TSysParam item:list) {
            Map map = Object2Map.toStringMap(Object2Map.objectToMap(item,true));
            String str = "t_sys_param:"+item.getParamCode();
            redisTemplate.opsForHash().putAll(str, map);
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectQuery(List<String> params) {
        List<TSysParam> list = this.tSysParamDao.selectQuery(params);
        Map<String,Object> mapFOrRe = new HashMap<>();
        for (TSysParam item: list){
            mapFOrRe.put(item.getParamCode(),item.getContent());
        }
        return mapFOrRe;
    }

    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByPrimaryCode() {
        return this.tSysParamDao.selectByPrimaryCode();
    }

}

