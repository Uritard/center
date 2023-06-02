package com.yjh.platform.module.device.service;

import com.google.common.collect.Maps;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.controller.TCruisePointInstanceController;
import com.yjh.platform.module.device.dao.TCruiseNonhomologousPointInstanceDao;
import com.yjh.platform.module.device.entity.TCruiseNonhomologousPointInstance;
import com.yjh.platform.module.device.entity.TCruiseNonhomologousWarnDO;
import com.yjh.platform.module.device.entity.TCruiseNonhomologousWarnInfo;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.patrol.entity.NonhomologousWarnEnum;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
        int checkExist = tCruiseNonhomologousPointInstanceDao.checkNonhomologousPointInstanceExist(tCruiseNonhomologousPointInstance);
        int result = 0;
        if(checkExist>0){
            throw new BusinessException("该非同源告警规则关联的巡视点已绑定其他非同源告警规则！");
        }else{
            tCruiseNonhomologousPointInstance.setOneCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseNonhomologousPointInstance.getInstanceIdOne()));
            tCruiseNonhomologousPointInstance.setTwoCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseNonhomologousPointInstance.getInstanceIdTwo()));
            result = this.tCruiseNonhomologousPointInstanceDao.insert(tCruiseNonhomologousPointInstance);
        }
        return result;
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
        tCruiseNonhomologousPointInstance.setOneCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseNonhomologousPointInstance.getInstanceIdOne()));
        tCruiseNonhomologousPointInstance.setTwoCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseNonhomologousPointInstance.getInstanceIdTwo()));
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
//
//        for (TCruiseNonhomologousWarnInfo info: list) {
//            if(NonhomologousWarnEnum.SANXIANG.getCode().equals(info.getWarnType())) {
//                List<Map<String,Object>> listMaps = tCruiseNonhomologousPointInstanceDao.getSanxiangInfo(info.getWarnId());
//                if (CollectionUtils.isNotEmpty(listMaps) && StringUtils.isEmpty(info.getOneCruiseDeviceName())) {
//                    String taskCode = listMaps.get(0).get("task_id").toString();
//                    Long inspectionId = (Long)listMaps.get(0).get("inspection_id");
//                    String robotName = tCruiseNonhomologousPointInstanceDao.getCruiseDeviceInfo(taskCode,inspectionId);
//                    StringJoiner meteNames = new StringJoiner("/");
//                    listMaps.forEach(map -> meteNames.add(map.get("mete_name").toString()));
//                    info.setDeviceName(listMaps.get(0) == null ? null : listMaps.get(0).get("device_name").toString());
//                    info.setOneCruiseDeviceName(robotName);
//                    info.setDeviceTypeName(listMaps.get(0) == null ? null : listMaps.get(0).get("device_type_name").toString());
//                    info.setDeviceMeteName(meteNames.toString());
//                }
//            }
//        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectWarnByPrimaryId(String warnId) {
        TCruiseNonhomologousWarnDO tCruiseNonhomologousWarnDO = tCruiseNonhomologousPointInstanceDao.getTCruiseNonhomologousWarnByPrimaryId(warnId);
        Map<String,Object> tCruiseNonhomologousWarnInfo = Maps.newHashMap();
        if (NonhomologousWarnEnum.SANXIANG.getCode().equals(tCruiseNonhomologousWarnDO.getWarnType())) {
            List<Map<String,Object>> listMaps = tCruiseNonhomologousPointInstanceDao.getSanxiangInfo(warnId);
            if (CollectionUtils.isNotEmpty(listMaps) && StringUtils.isEmpty(tCruiseNonhomologousWarnDO.getOneCruiseDeviceName())) {
                String taskCode = listMaps.get(0).get("task_id").toString();
                Long inspectionId = (Long)listMaps.get(0).get("inspection_id");
                String robotName = tCruiseNonhomologousPointInstanceDao.getCruiseDeviceInfo(taskCode,inspectionId);
                StringJoiner meteNames = new StringJoiner("/");
                listMaps.forEach(map -> meteNames.add(map.get("mete_name").toString()));
                tCruiseNonhomologousWarnInfo.put("deviceName",tCruiseNonhomologousWarnDO.getDeviceName());
                tCruiseNonhomologousWarnInfo.put("oneCruiseDeviceName",robotName);
                tCruiseNonhomologousWarnInfo.put("deviceMeteTime",meteNames.toString());
                tCruiseNonhomologousWarnInfo.put("warnTime", tCruiseNonhomologousWarnDO.getWarnTime());
                tCruiseNonhomologousWarnInfo.put("delayName", tCruiseNonhomologousWarnDO.getRegionName());
                tCruiseNonhomologousWarnInfo.put("customName", tCruiseNonhomologousWarnDO.getCustomName());
                tCruiseNonhomologousWarnInfo.put("warnContent", tCruiseNonhomologousWarnDO.getWarnContent());
            } else {
                tCruiseNonhomologousWarnInfo.put("deviceName",tCruiseNonhomologousWarnDO.getDeviceName());
                tCruiseNonhomologousWarnInfo.put("oneCruiseDeviceName",tCruiseNonhomologousWarnDO.getOneCruiseDeviceName());
                tCruiseNonhomologousWarnInfo.put("twoCruiseDeviceName",tCruiseNonhomologousWarnDO.getTwoCruiseDeviceName());
                tCruiseNonhomologousWarnInfo.put("threeCruiseDeviceName",tCruiseNonhomologousWarnDO.getThreeCruiseDeviceName());
                StringJoiner meteNames = new StringJoiner("/");
                listMaps.forEach(map -> meteNames.add(map.get("mete_name").toString()));
                tCruiseNonhomologousWarnInfo.put("deviceMeteTime",meteNames.toString());
                tCruiseNonhomologousWarnInfo.put("warnTime", tCruiseNonhomologousWarnDO.getWarnTime());
                tCruiseNonhomologousWarnInfo.put("delayName", tCruiseNonhomologousWarnDO.getRegionName());
                tCruiseNonhomologousWarnInfo.put("customName", tCruiseNonhomologousWarnDO.getCustomName());
                tCruiseNonhomologousWarnInfo.put("warnContent", tCruiseNonhomologousWarnDO.getWarnContent());
            }
        }
        else {
            tCruiseNonhomologousWarnInfo = tCruiseNonhomologousPointInstanceDao.selectWarnByPrimaryId(warnId);
        }
        List<Map<String,Object>> warnDetailInfo = tCruiseNonhomologousPointInstanceDao.selectWarnInspections(warnId);
        for(Map<String,Object> m : warnDetailInfo){
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(
                UPatrolTaskService.PATROL_TASK_PREFIX + m.get("taskId").toString() + ":" + m.get("instanceId").toString());
            if(redisInfoMap.size()>0){
                /*if (tCruiseNonhomologousWarnDO.getWarnType() == 6){
                    redisInfoMap.put("resultNum",dealResultNum(redisInfoMap.get("resultNum")));
                }*/
                m.putAll(redisInfoMap);
            }
        }
        if(tCruiseNonhomologousWarnInfo != null){
            tCruiseNonhomologousWarnInfo.put("warnInspectionsInfo",warnDetailInfo);
        }
        return tCruiseNonhomologousWarnInfo;
    }

    private String dealResultNum(String resultNum){
        String re = "";
        if(StringUtils.isEmpty(resultNum)){
            return re;
        }
        switch (resultNum){
            case "分":
            case "开":
            case "储能":
            case "远方":
                re="0";
                break;
            case "合":
            case "关":
            case "非储能":
            case "就地":
                re="1";
                break;
            default:
                re = resultNum;
                break;
        }
        return re;
    }

}
