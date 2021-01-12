package com.yjh.platform.module.user.service;

import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.module.user.entity.TSysParam;
import com.yjh.platform.module.user.dao.TSysParamDao;

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


    @Logs(title = "插入", code = "module", content = "新增系统参数信息")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TSysParam tSysParam) {
         this.tSysParamDao.insert(tSysParam);
         return this.insertIntoRedis();
    }


    @Logs(title = "删除", code = "module", content = "删除系统参数信息")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer paramId) {
        this.tSysParamDao.deleteByPrimaryId(paramId);
        return this.insertIntoRedis();
    }

    @Logs(title = "更新", code = "module", content = "更新系统参数信息")
    @Transactional(rollbackFor = Exception.class)
    public int update(TSysParam tSysParam) {
        this.tSysParamDao.update(tSysParam);
        return this.insertIntoRedis();
    }


    @Logs(title = "查询", code = "module", content = "根据参数ID查询系统参数信息")
    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByPrimaryId(Integer paramId) {
        return this.tSysParamDao.selectByPrimaryId(paramId);
    }


    @Logs(title = "查询", code = "module", content = "查询系统参数信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TSysParam> select(Integer paramId,String paramCode, String paramType, String paramName, String content, String remark) {
        List<TSysParam> tSysParamList = tSysParamDao.select(paramId,paramCode,paramType, paramName, content, remark);
        return tSysParamList;
    }


    @Logs(title = "分页查询", code = "module", content = "分页查询系统参数信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TSysParam> selectByPage(Integer paramId,String paramCode, String paramType, String paramName, String content, String remark) {
        List<TSysParam> tSysParamList = tSysParamDao.selectByPage(paramId,paramCode, paramType, paramName, content, remark);
        return tSysParamList;
    }


    @Logs(title = "新增", code = "module", content = "批量新增系统参数信息")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TSysParam> list) {
        this.tSysParamDao.batchInsert(list);
        return this.insertIntoRedis();
    }

    @Logs(title = "新增", code = "module", content = "新增系统参数缓存")
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


}

