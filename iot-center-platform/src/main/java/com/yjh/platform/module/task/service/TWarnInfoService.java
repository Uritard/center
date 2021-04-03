package com.yjh.platform.module.task.service;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TCameraAlarmDao;
import com.yjh.platform.module.task.dao.TDefectInfoDao;
import com.yjh.platform.module.task.dao.TRobotAlarmDao;
import com.yjh.platform.module.task.dao.TWarnInfoDao;
import com.yjh.platform.module.task.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.util.*;

/**
* @author tt
* @since 2020-10-15
*/
@Service
public class TWarnInfoService{

    @Autowired
    private TWarnInfoDao tWarnInfoDao;
    @Autowired
    private TDefectInfoDao tDefectInfoDao;
    @Autowired
    private TCameraAlarmDao tCameraAlarmDao;
    @Autowired
    private TRobotAlarmDao tRobotAlarmDao;
    @Autowired
    private RedisTemplate redisTemplate;


    private Logger log = LoggerFactory.getLogger(TWarnInfoService.class);

    private DateTimeUtil dateTimeUtil;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TWarnInfo tWarnInfo) {
        return this.tWarnInfoDao.insert(tWarnInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long warnId) {
        return this.tWarnInfoDao.deleteByPrimaryId(warnId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TWarnInfo tWarnInfo) {
        return this.tWarnInfoDao.update(tWarnInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public TWarnInfo selectByPrimaryId(Long warnId) {
        return this.tWarnInfoDao.selectByPrimaryId(warnId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfo> select(Long warnId, Integer warnLevel, Date warnTime, Integer warnType, String warnName, String warnContent, Long deviceId, String cunstomId, Long instanceId, Long stdMeteId, Integer confMode, Integer isWarn, Integer dealType, String dealInfo, String dealPersonId, Date dealTime, Integer defectModel, Integer alarmSource, Integer warnSubtype, String deviceCode, String imagePath, String videoPath, String value, String outRange, String taskId) {
        List<TWarnInfo> tWarnInfoList = tWarnInfoDao.select(warnId, warnLevel, warnTime, warnType, warnName, warnContent, deviceId, cunstomId, instanceId, stdMeteId, confMode, isWarn, dealType, dealInfo, dealPersonId, dealTime, defectModel, alarmSource, warnSubtype, deviceCode, imagePath, videoPath, value, outRange, taskId);
        return tWarnInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfo> selectByPage(TWarnInfo tWarnInfo) {
        List<TWarnInfo> tWarnInfoList = tWarnInfoDao.selectByPage(tWarnInfo);
        return tWarnInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TWarnInfo> list) {
        return this.tWarnInfoDao.batchInsert(list);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfoDetail> selectWarnByPage(Integer warnLevel, Integer confMode, Integer alarmSource, Date startTime, Date endTime, String deviceName,String meteName) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("warnLevel", warnLevel);
        map.put("confMode", confMode);
        map.put("alarmSource", alarmSource);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("deviceName", deviceName);
        map.put("meteName", meteName);
        return tWarnInfoDao.selectAllWarn(map);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfoDetail> WarnConfirm(Integer warnLevel, Integer confMode, String startTime, String endTime, String deviceName,Integer defectType,String meteName) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("warnLevel", warnLevel);
        map.put("confMode", confMode);
        map.put("defectType", defectType);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("deviceName", deviceName);
        map.put("meteName", meteName);
        return tWarnInfoDao.WarnConfirm(map);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfo> countByAlarmSource() {
        Map<String, Integer> map = tWarnInfoDao.countByAlarmSource();
        List<TJContentInfo> tjContentInfoList = new ArrayList<>();
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Number mapValue = map.get(key);
            TJContentInfo tjContentInfo = new TJContentInfo();
            tjContentInfo.setCount(mapValue);
            tjContentInfo.setContent(key);
            tjContentInfoList.add(tjContentInfo);
        }
        return tjContentInfoList;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfoDetail> countByDeviceType(){
        List<String> monthDates = dateTimeUtil.getDayDateList(30);
        String firstTime1 = monthDates.get(0);
        Date startingTime = dateTimeUtil.parse(firstTime1);
        String endTime = dateTimeUtil.getDayBefore(startingTime);
        String startTime = monthDates.get(29);
        log.info("startTime==="+startTime);
        log.info("endTime==="+endTime);
        List<TJContentInfoDetail> tjContentInfoList = tWarnInfoDao.countByDeviceType(startTime,endTime);
        System.out.println("tjContentInfoList是："+tjContentInfoList);
        return tjContentInfoList;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfoDetail> countAlarmByDeviceType(){
        List<String> monthDates = dateTimeUtil.getDayDateList(30);
        String firstTime1 = monthDates.get(0);
        Date startingTime = dateTimeUtil.parse(firstTime1);
        String endTime = dateTimeUtil.getDayBefore(startingTime);
        String startTime = monthDates.get(29);
        log.info("startTime==="+startTime);
        log.info("endTime==="+endTime);
        List<TJContentInfoDetail> tjContentInfoList = tWarnInfoDao.countAlarmByDeviceType(startTime,endTime);
        System.out.println("tjContentInfoList是："+tjContentInfoList);
        return tjContentInfoList;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> countWarnAndDefectOnMonth() {
        List<WarnStatistical> WarnList = tWarnInfoDao.countWarnAndDefectOnMonth1();//近一月告警
        log.info("WarnList==="+WarnList);
        List<WarnStatistical> defectList = tWarnInfoDao.countWarnAndDefectOnMonth2();//近一月缺陷
        log.info("defectList==="+defectList);
        List<WarnStatistical> finalLst = new ArrayList<>();
        for (WarnStatistical ws : WarnList){
            for (WarnStatistical ws1 : defectList){
                if (ws.getTimeNode().equals(ws1.getTimeNode())){
                    WarnStatistical wsl = new WarnStatistical();
                    wsl.setTimeNode(ws.getTimeNode());
                    wsl.setCount(ws.getCount() + ws1.getCount());
                    finalLst.add(wsl);
                }
            }
        }
//        log.info("最后的list是==="+finalLst);
        return finalLst;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> countAllWarnOnMonth() {
        return tWarnInfoDao.countAllWarnOnMonth();
    }
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> countWarnOnMonth() {
        return tWarnInfoDao.countWarnOnMonth();
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfo> countWarnConfMode() {
        List<String> monthDates = dateTimeUtil.getDayDateList(30);
        String firstTime1 = monthDates.get(0);
        Date startingTime = dateTimeUtil.parse(firstTime1);
        String endTime = dateTimeUtil.getDayBefore(startingTime);
        String startTime = monthDates.get(29);
        log.info("startTime==="+startTime);
        log.info("endTime==="+endTime);
        Map<String, Integer> map = tWarnInfoDao.countWarnConfMode(startTime,endTime);
        List<TJContentInfo> tjContentInfoList = new ArrayList<>();
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Number mapValue =  map.get(key);
            TJContentInfo tjContentInfo = new TJContentInfo();
            tjContentInfo.setCount(mapValue);
            tjContentInfo.setContent(key);
            tjContentInfoList.add(tjContentInfo);
        }
        return tjContentInfoList;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfo> countWarnDefectConfMode() {
        List<String> monthDates = dateTimeUtil.getDayDateList(30);
        String firstTime1 = monthDates.get(0);
        Date startingTime = dateTimeUtil.parse(firstTime1);
        String endTime = dateTimeUtil.getDayBefore(startingTime);
        String startTime = monthDates.get(29);
        log.info("startTime==="+startTime);
        log.info("endTime==="+endTime);
        Map<String, Integer> map = tWarnInfoDao.countWarnDefectConfMode(startTime,endTime);
        log.info("统计的map==="+map);
        List<TJContentInfo> tjContentInfoList = new ArrayList<>();
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Number mapValue =  map.get(key);
            TJContentInfo tjContentInfo = new TJContentInfo();
            tjContentInfo.setCount(mapValue);
            tjContentInfo.setContent(key);
            tjContentInfoList.add(tjContentInfo);
        }
        return tjContentInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfoDetail> selectAlarmProcess(Long warnId) {
        return tWarnInfoDao.selectAlarmProcess(warnId);
    }
    @Transactional(rollbackFor = Exception.class)
    public int warnReview(Long warnId,String dealInfo,Integer dealType,String userId,Integer defectModel) throws Exception{
        Date date = new Date();
        TWarnInfo tWarnInfo = new TWarnInfo()
                .setWarnId(warnId)
                .setDealInfo(dealInfo)
                .setDealType(dealType)
                .setConfMode(275)
                .setDealTime(date)
                .setDefectModel(defectModel)
                .setDealPersonId(userId);
        int jieGuo = tWarnInfoDao.update(tWarnInfo);
        //处理告警完成之后
        if (jieGuo == 1) {
            //给前端推webSocket
            Map<String, String> jasonMap = new HashMap<>();
            jasonMap.put("type", "finishedOneAlarm");
            jasonMap.put("alarmId", warnId.toString());
            String json = JSON.toJSONString(jasonMap);
            log.info(("发送给前端的消息===" + json));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
        }
        return jieGuo;
    }
    @Transactional(rollbackFor = Exception.class)
    public int alarmAndDefectProcess(AlarmAndDefectProcess alarmAndDefectProcess,String userId) {
        Long warnId = alarmAndDefectProcess.getWarnId();
        Integer dealType = alarmAndDefectProcess.getDealType();
        String dealInfo = alarmAndDefectProcess.getDealInfo();
        Integer defectModel = alarmAndDefectProcess.getDefectModel();
        Date date = new Date();
        int jieGuo = 0;
        Integer warnFlag = Integer.valueOf(tWarnInfoDao.selectDictCodeByNote("其他","defect_model"));
        if (Objects.nonNull(defectModel)){
            if (defectModel.equals(warnFlag)){//告警产生的缺陷：其他
                TWarnInfo tWarnInfo = new TWarnInfo()
                        .setDealInfo(dealInfo)
                        .setWarnId(warnId)
                        .setDealType(dealType)
                        .setDealTime(date)
                        .setDealPersonId(userId)
                        .setConfMode(275);
                jieGuo = tWarnInfoDao.update(tWarnInfo);
                if (jieGuo == 1) {
                    sendWebSocket(warnId);
                }
            }else {
                TDefectInfo tDefectInfo = new TDefectInfo()
                        .setDefectId(warnId)
                        .setDealInfo(dealInfo)
                        .setDealType(dealType)
                        .setDealPersonId(userId)
                        .setDealTime(date)
                        .setConfMode(275);
                jieGuo = tDefectInfoDao.update(tDefectInfo);
                if (jieGuo == 1) {
                    sendWebSocket(warnId);
                }
            }
        }
        return jieGuo;
    }
    public void sendWebSocket(Long warnId){
        //给前端推webSocket
        Map<String,String> jasonMap=new HashMap<>();
        jasonMap.put("type","finishedOneAlarm");
        jasonMap.put("alarmId",warnId.toString());
        String json= JSON.toJSONString(jasonMap);
        System.out.println(("发送给前端的消息==="+json));
        try{
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
        }catch (Exception e){
            System.out.println("发送websocket出错");
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public int alarmProcess(TWarnInfo tWarnInfoTemp,String userId) {
        Long warnId = tWarnInfoTemp.getWarnId();
        Integer alarmSource = tWarnInfoTemp.getAlarmSource();
        Integer dealType = tWarnInfoTemp.getDealType();
        String dealInfo = tWarnInfoTemp.getDealInfo();
        Date date = new Date();
        int jieGuo = 0;
        if (alarmSource == 279 ) {
            TRobotAlarm tRobotAlarm = new TRobotAlarm();
            tRobotAlarm.setRobotAlarmId(warnId);
            tRobotAlarm.setDealInfo(dealInfo);
            tRobotAlarm.setDealType(dealType);
            tRobotAlarm.setAlarmState(275);
            tRobotAlarm.setDealTime(date);
            tRobotAlarm.setDealPersonId(userId);
            jieGuo  = tRobotAlarmDao.update(tRobotAlarm);
            //处理告警完成之后
            if(jieGuo == 1) {
            //给前端推webSocket
                Map<String,String> jasonMap=new HashMap<>();
                jasonMap.put("type","finishedOneAlarm");
                jasonMap.put("alarmId",warnId.toString());
                String json= JSON.toJSONString(jasonMap);
                log.info(("发送给前端的消息==="+json));
                try{
                    Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
                }catch (Exception e){
                    System.out.println("发送websocket出错");
                }
            }
        }else if (alarmSource == 888)
        {
            TCameraAlarm tCameraAlarm = new TCameraAlarm();
            tCameraAlarm.setCameraAlarmId(warnId);
            tCameraAlarm.setDealInfo(dealInfo);
            tCameraAlarm.setDealType(dealType);
            tCameraAlarm.setAlarmState(275);
            tCameraAlarm.setDealTime(date);
            tCameraAlarm.setDealPersonId(userId);
            jieGuo = tCameraAlarmDao.update(tCameraAlarm);
            //处理告警完成之后
            if(jieGuo == 1) {
                //给前端推webSocket
                Map<String,String> jasonMap=new HashMap<>();
                jasonMap.put("type","finishedOneAlarm");
                jasonMap.put("alarmId",warnId.toString());
                String json= JSON.toJSONString(jasonMap);
                log.info(("发送给前端的消息==="+json));
                try{
                    Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
                }catch (Exception e){
                    System.out.println("发送websocket出错");
                }
            }
        }else{
            TWarnInfo tWarnInfo = new TWarnInfo();
            tWarnInfo.setWarnId(warnId);
            tWarnInfo.setDealInfo(dealInfo);
            tWarnInfo.setDealType(dealType);
            tWarnInfo.setConfMode(275);
            tWarnInfo.setDealTime(date);
            tWarnInfo.setDealPersonId(userId);
            jieGuo = tWarnInfoDao.update(tWarnInfo);
            //处理告警完成之后
            if(jieGuo == 1) {
                //给前端推webSocket
                Map<String,String> jasonMap=new HashMap<>();
                jasonMap.put("type","finishedOneAlarm");
                jasonMap.put("alarmId",warnId.toString());
                String json= JSON.toJSONString(jasonMap);
                log.info(("发送给前端的消息==="+json));
                try{
                    Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
                }catch (Exception e){
                    System.out.println("发送websocket出错");
                }
            }
        }
        return jieGuo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer warnCountsNonIdentify(){
        //总告警数量=redis中的数量+数据库中的数量
//        Set<String> warnKeys=redisScan("warnInfo:");
//        Set<String> defectKeys=redisScan("defectInfo:");
//        return warnKeys.size() + defectKeys.size() + tWarnInfoDao.warnCountsNonIdentify();
        return tWarnInfoDao.warnCountsNonIdentify();
    }
    //告警弹窗
    @Transactional(rollbackFor = Exception.class)
    public TWarnInfoDetail selectWarnPopUp(String warnId,Integer defectModel) throws Exception{
        TWarnInfoDetail tWarnInfoDetail = new TWarnInfoDetail();
        Integer warnFlag = Integer.valueOf(tWarnInfoDao.selectDictCodeByNote("其他","defect_model"));
        if (defectModel.equals(warnFlag)){//告警信息
            tWarnInfoDetail = tWarnInfoDao.selectWarnPopUp(Long.valueOf(warnId));
            if (Objects.nonNull(tWarnInfoDetail.getDeviceCode())){
                tWarnInfoDetail.setCameraId(Long.valueOf(tWarnInfoDetail.getDeviceCode()));
            }
        }else {//缺陷信息
//            tWarnInfoDetail = tDefectInfoDao.selectWarnPopUp(warnId);
            Map<String, String> defectMap = redisTemplate.opsForHash().entries("defectInfo:"+warnId);
            String alarmLevelName = tWarnInfoDao.selectDictNoteByCode(defectMap.get("defectLevel"),"alarm_level");
            String alarmSourceName = tWarnInfoDao.selectDictNoteByCode(defectMap.get("alarmSource"),"alarm_source");
            String defectModelName = tWarnInfoDao.selectDictNoteByCode(defectMap.get("defectType"),"defect_model");
            Map<String,String> nameMap = tWarnInfoDao.selectName(Long.valueOf(defectMap.get("stdMeteId")));
            Long presetId = tWarnInfoDao.selectPresetId(Long.valueOf(defectMap.get("instanceId")));
            Long cameraId = tWarnInfoDao.selectCameraId(Long.valueOf(defectMap.get("instanceId")));

            tWarnInfoDetail.setWarnContent(defectMap.get("defectContent"));
            tWarnInfoDetail.setAlarmTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(defectMap.get("defectTime")));
            tWarnInfoDetail.setDeviceId(Long.valueOf(defectMap.get("deviceId")));
            tWarnInfoDetail.setDeviceName(nameMap.get("device_name"));
            tWarnInfoDetail.setInstanceId(Long.valueOf(defectMap.get("instanceId")));
            tWarnInfoDetail.setPresetId(presetId);
            tWarnInfoDetail.setCameraId(cameraId);
            tWarnInfoDetail.setStdMeteId(Long.valueOf(defectMap.get("stdMeteId")));
            tWarnInfoDetail.setMeteName(nameMap.get("mete_name"));
            tWarnInfoDetail.setAlarmLevel(Integer.valueOf(defectMap.get("defectLevel")));
            tWarnInfoDetail.setAlarmLevelName(alarmLevelName);
            tWarnInfoDetail.setRealCode(nameMap.get("real_code"));
            tWarnInfoDetail.setImagePath(defectMap.get("imagePath"));
            tWarnInfoDetail.setAlarmSource(Integer.valueOf(defectMap.get("alarmSource")));
            tWarnInfoDetail.setAlarmSourceName(alarmSourceName);
            tWarnInfoDetail.setDefectModel(Integer.valueOf(defectMap.get("defectType")));
            tWarnInfoDetail.setDefectModelName(defectModelName);
            tWarnInfoDetail.setCustomName(nameMap.get("custom_name"));

        }
        return tWarnInfoDetail;
    }



    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> selectCurrentWarn(){
        return (Map<String, String>)redisTemplate.opsForValue().get("currentWarn");
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> selectUnionWarn(){
        return (Map<String,String>)redisTemplate.opsForValue().get("currentUnion");
    }
    //读批量redis
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
}

