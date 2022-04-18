package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.controller.TCruisePointInstanceController;
import com.yjh.platform.module.device.dao.TCruiseNonhomologousPointInstanceDao;
import com.yjh.platform.module.device.entity.TCruiseNonhomologousPointInstance;
import com.yjh.platform.module.device.entity.TCruiseNonhomologousWarnInfo;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sunjinyan
 * @since 2022-04-08
 */
@Service
public class TCruiseNonhomologousPointInstanceService {

    @Autowired
    private TCruiseNonhomologousPointInstanceDao tCruiseNonhomologousPointInstanceDao;

    @Autowired
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TCruisePointInstanceController.class);


    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance) {
        return this.tCruiseNonhomologousPointInstanceDao.insert(tCruiseNonhomologousPointInstance);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long instanceId) {
        return this.tCruiseNonhomologousPointInstanceDao.deleteByPrimaryId(instanceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(TCruisePointInstance tCruisePointInstance) {
        return this.tCruiseNonhomologousPointInstanceDao.delete(tCruisePointInstance);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance) {
        return this.tCruiseNonhomologousPointInstanceDao.update(tCruiseNonhomologousPointInstance);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruisePointInstance selectByPrimaryId(Long instanceId) {
        return this.tCruiseNonhomologousPointInstanceDao.selectByPrimaryId(instanceId);
    }

//    @Transactional(rollbackFor = Exception.class)
//    public List<TCruisePointInstance> select(Long instanceId, String stationId, String stationName, String robotDeviceId, String videoPresetId, Integer identifyType, Integer identifySonType, Integer cruiseType, Long cruiseId, String cruiseName, String warnThreshold, Integer ifSy, Integer syType, String warnLevel) {
//        List<TCruisePointInstance> tCruisePointInstanceList = tCruiseNonhomologousPointInstanceDao.select(instanceId, stationId, stationName, robotDeviceId, videoPresetId, identifyType, identifySonType, cruiseType, cruiseId, cruiseName, warnThreshold, ifSy, syType, warnLevel);
//        return tCruisePointInstanceList;
//    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseNonhomologousPointInstance> selectByPage(TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance) {
        List<TCruiseNonhomologousPointInstance> list = tCruiseNonhomologousPointInstanceDao.selectByPage(tCruiseNonhomologousPointInstance);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseNonhomologousWarnInfo> selectWarnByPage(TCruiseNonhomologousWarnInfo tCruiseNonhomologousWarnInfo) {
        List<TCruiseNonhomologousWarnInfo> list = tCruiseNonhomologousPointInstanceDao.selectWarnByPage(tCruiseNonhomologousWarnInfo);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectWarnByPrimaryId(String warnId) {
        Map<String,Object> tCruiseNonhomologousWarnInfo = tCruiseNonhomologousPointInstanceDao.selectWarnByPrimaryId(warnId);
        List<Map<String,Object>> warnDetailInfo = tCruiseNonhomologousPointInstanceDao.selectWarnInspections(warnId);
        for(Map<String,Object> m : warnDetailInfo){
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + m.get("taskId").toString() + ":" + m.get("instanceId").toString());
            if(redisInfoMap.size()>0){
                m.putAll(redisInfoMap);
            }
        }
        if(tCruiseNonhomologousWarnInfo != null){
            tCruiseNonhomologousWarnInfo.put("warnInspectionsInfo",warnDetailInfo);
        }
        return tCruiseNonhomologousWarnInfo;
    }

}
