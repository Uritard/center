package com.yjh.platform.module.task.service;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.platform.module.task.entity.TCruiseTaskResult;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDao;

import java.lang.reflect.Array;
import java.util.*;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
* @author czh
* @since 2020-08-25
*/
@Service
public class TCruiseTaskResultService{

    @Autowired
    private TCruiseTaskResultDao tCruiseTaskResultDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTaskResult tCruiseTaskResult) {
        return this.tCruiseTaskResultDao.insert(tCruiseTaskResult);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskResultId) {
        return this.tCruiseTaskResultDao.deleteByPrimaryId(taskResultId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTaskResult tCruiseTaskResult) {
        return this.tCruiseTaskResultDao.update(tCruiseTaskResult);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskResult selectByPrimaryId(String taskResultId) {
        return this.tCruiseTaskResultDao.selectByPrimaryId(taskResultId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResult> select(String taskResultId, String taskId, Integer taskAbnormal, Integer taskAlarm, String runExecute, Date cruiseTaskTime, Integer taskStatus, Integer cruiseResult, String remark) {
        List<TCruiseTaskResult> tCruiseTaskResultList = tCruiseTaskResultDao.select(taskResultId, taskId, taskAbnormal, taskAlarm, runExecute, cruiseTaskTime, taskStatus, cruiseResult, remark);
        return tCruiseTaskResultList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResult> selectByPage(TCruiseTaskResult tCruiseTaskResult) {
        List<TCruiseTaskResult> tCruiseTaskResultList = tCruiseTaskResultDao.selectByPage(tCruiseTaskResult);
        return tCruiseTaskResultList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTaskResult> list) {
        return this.tCruiseTaskResultDao.batchInsert(list);
    }

    @Logs(title = "Redis数据库批量查询Key值游标",code = "module")
    @Transactional(rollbackFor =Exception.class)
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }
    @Logs(title = "获取当前任务的巡检点执行信息")
    @Transactional(rollbackFor = Exception.class)
    public List<Map> selectCruiseTaskResult(Long taskId){
        Set<String> keyResult=redisScan("t_cruise_task_result*");

        List<Map> dataResult=new ArrayList<>();
        for(String key:keyResult){
            Map<String,Object> resultMap=redisTemplate.opsForHash().entries(key);
            dataResult.add(resultMap);
        }
        return dataResult;
    }

}

