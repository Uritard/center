package com.yjh.platform.module.patrol.service;


import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.configuration.UpFtpsConfig;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.task.entity.TStdDevicemete;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 算法结果分析功能集
 * @author YJH
 */
@Service
public class AnalyseDataOperateService {

    @Autowired
    private AnalyseDataOperateDao analyseDataOperateDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UpFtpsConfig upFtpsConfig;

    private final Logger log = LoggerFactory.getLogger(AnalyseDataOperateService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insertWarnInfo(TWarnInfo tWarnInfo) {
        return this.analyseDataOperateDao.insertWarnInfo(tWarnInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertWarnInfo(List<TWarnInfo> list) {
        return this.analyseDataOperateDao.batchInsertWarnInfo(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertVideoAnalyseResult(TVideoAnalyseResult tVideoAnalyseResult) {
        return this.analyseDataOperateDao.insertVideoAnalyseResult(tVideoAnalyseResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertVideoAnalyseResult(List<TVideoAnalyseResult> list) {
        return this.analyseDataOperateDao.batchInsertVideoAnalyseResult(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectByPrimaryIdCruiseResult(String taskResultId, String taskId) {
        return this.analyseDataOperateDao.selectByPrimaryIdCruiseResult(taskResultId,taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateCruiseResult(TCruiseResult tCruiseResult) {
        return this.analyseDataOperateDao.updateCruiseResult(tCruiseResult);
    }


    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskResult selectByPrimaryIdCruiseTaskResult(String taskResultId) {
        return this.analyseDataOperateDao.selectByPrimaryIdCruiseTaskResult(taskResultId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult) {
        return this.analyseDataOperateDao.updateCruiseTaskResult(tCruiseTaskResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult) {
        return this.analyseDataOperateDao.insertCruiseTaskResult(tCruiseTaskResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseTaskResultDetail(TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        return this.analyseDataOperateDao.insertCruiseTaskResultDetail(tCruiseTaskResultDetail);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return this.analyseDataOperateDao.batchInsertCruiseTaskResultDetail(list);
        }
        return -1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseDataResult(TCruiseDataResult tCruiseDataResult) {
        return this.analyseDataOperateDao.insertCruiseDataResult(tCruiseDataResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseDataResult(List<TCruiseDataResult> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return this.analyseDataOperateDao.batchInsertCruiseDataResult(list);
        }
        return -1;
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceMete selectByPrimaryIdDeviceMete(Long deviceMeteId) {
        return this.analyseDataOperateDao.selectByPrimaryIdDeviceMete(deviceMeteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceMete selectDeviceMeteByInstanceId(Long instanceId) {
        return this.analyseDataOperateDao.selectDeviceMeteByInstanceId(instanceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruisePointInstance selectPointInstance(Long instanceId) {
        return this.analyseDataOperateDao.selectPointInstance(instanceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public String selectDictCode(String colName, String dictNote) {
        return this.analyseDataOperateDao.selectDictCode(colName, dictNote);
    }

    @Transactional(rollbackFor = Exception.class)
    public String selectDictNote(String dictCode, String colName) {
        return analyseDataOperateDao.selectDictNote(dictCode, colName);
    }

    public String selectAlarmLevel(String colName, String dictNote){
        return analyseDataOperateDao.selectAlarmLevel(colName, dictNote);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> selectCruiseByTask(String taskId) {
        return this.analyseDataOperateDao.selectCruiseByTaskId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertDefectInfo(TDefectInfo tDefectInfo) {
        return this.analyseDataOperateDao.insertDefectInfo(tDefectInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertDefectInfo(List<TDefectInfo> list) {
        return this.analyseDataOperateDao.batchInsertDefectInfo(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public String selectAlgorithmDefectInfo(String defectType){
        Integer defectLevel=analyseDataOperateDao.selectAlgorithmDefectInfo(Integer.valueOf(defectType));
        log.info("defectType:+======"+defectType);
        log.info("defectLevel:======"+defectLevel);
        return defectLevel.toString();
    }

    @Transactional(rollbackFor = Exception.class)
    public String selectLaterTaskCruiseResult(String taskId){
        return analyseDataOperateDao.selectLaterTaskCruiseResult(taskId);
    }

    /**
     *Fun 算法综合 状态点判断
     * @param mode 识别模式: 1,2:表计、缺陷 ———— -1，-2：逆表计、逆缺陷 ———— 0：双识别
     * @param flag 1：正常点 0:异常点
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Integer> mutiAlgoCount(String mode,Integer flag,String cruiseRedisKey){

        Integer normal=0;
        Integer abnormal=0;

        switch (mode){
            case "1":
            case "2":
                if(flag==1){
                    normal++;
                }else {
                    abnormal++;
                }
                break;
            case "-1":
            case "-2":
                String temResult=redisTemplate.opsForHash().get(cruiseRedisKey,"temResult").toString();
                if(flag==1){
                    if(temResult.equals("1")){
                        normal++;
                    }else {
                        abnormal++;
                    }
                }else {
                    abnormal++;
                }
                break;
            case "0":
                if(flag==1){
                    // 临时值为正常点
                    redisTemplate.opsForHash().put(cruiseRedisKey,"temResult","1");
                }else {
                    // 临时值为异常点
                    redisTemplate.opsForHash().put(cruiseRedisKey,"temResult","0");
                }
                break;
            default:
                break;
        }

        List<Integer> result=new ArrayList<>();
        result.add(normal);
        result.add(abnormal);
        log.info("-------------异常点判断-----------");
        return result;

    }

    /**
     *双算法 综合巡视结果解析与获取
     * @param mode 识别模式
     * @param cruiseRedisKey redis Key名
     * @param cruiseResult  巡视结果
     * @param abnormalType  异常类型
     * @param resultValue   巡视值
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> doubleResultHandle(String mode,String cruiseRedisKey,String cruiseResult,String abnormalType,String resultValue){
        Map<String,String> handleResultMap=new HashMap<>();
        String resultNum="--";
        String cruiseResultFinal="null";
        String abnormalTypeFinal="null";
        String abnormalTypeTem="null";
        String cruiseResultTem=selectDictCode("cruise_result",cruiseResult);

        if(abnormalType.equals("--")){
            abnormalTypeTem=abnormalType;
        }else {
            abnormalTypeTem=selectDictCode("abnormal_type",abnormalType);
        }
        switch (mode){
            case "0":
            case "1":
            case "2":
                resultNum=resultValue;
                cruiseResultFinal=cruiseResultTem;
                abnormalTypeFinal=abnormalTypeTem;
                break;
            case "-1":
            case "-2":
                if(redisTemplate.opsForHash().get(cruiseRedisKey,"resultNum").toString().contains("--")){
                    resultNum=resultValue;
                }else {
                    if(resultValue.contains("--")){
                        resultNum=redisTemplate.opsForHash().get(cruiseRedisKey,"resultNum").toString();
                    }else {
                        resultNum=(redisTemplate.opsForHash().get(cruiseRedisKey,"resultNum").toString())+","+resultValue;
                    }
                }
                if((redisTemplate.opsForHash().get(cruiseRedisKey,"cruiseResult").toString()).equals(selectDictCode("cruise_result","正常"))){
                    cruiseResultFinal=cruiseResultTem;
                }else {
                    cruiseResultFinal=redisTemplate.opsForHash().get(cruiseRedisKey,"cruiseResult").toString();
                }
                // TODO: 2021/3/1 巡视监控页面需对该变量进行字符串解析

                if(redisTemplate.opsForHash().get(cruiseRedisKey,"cruiseAbnormal").toString().contains("--")){
                    abnormalTypeFinal=abnormalTypeTem;
                }else {
                    if(abnormalTypeTem.contains("--")){
                        abnormalTypeFinal=redisTemplate.opsForHash().get(cruiseRedisKey,"cruiseAbnormal").toString();
                    }else {
                        abnormalTypeFinal=(redisTemplate.opsForHash().get(cruiseRedisKey,"cruiseAbnormal").toString())+","+abnormalTypeTem;
                    }
                }
                break;
            default:
                break;
        }

        handleResultMap.put("resultNum",resultNum);
        handleResultMap.put("cruiseResult",cruiseResultFinal);
        handleResultMap.put("cruiseAbnormal",abnormalTypeFinal);
        log.info("----------巡视结果集产生---------");
        log.info("doubleResultMap----------:"+handleResultMap);
        return handleResultMap;
    }

    /**
     * -------算法服务报文反拆包-------
     * @param body 算法服务返回的报文消息体（信息不完整的消息报文）
     * @return  完整的合并拆包信息的结果报文
     */
    public String nonUnpacking(String body) {
        // body清除空格
        body = body.replaceAll("\\s++", "");
        // 处理结果初始化
        String usefulBody = "";
        if (body.matches("\\{\"msgData.*?\"2\"}") || body.matches("\\{\"msgType.*?}}}}")) {
            // 数据结果整包
            log.info("整包数据...");
            usefulBody = body;
        } else if (body.matches("\\{\"msgData.*?") || body.matches("\\{\"msgType.*?")) {
            // 结果半包
            if (body.contains("\"msgType\":\"4\"")) {
                // 注册消息
            } else if (body.contains("\"msgType\":\"6\"")) {
                // 结束消息
                usefulBody = body;
            } else {
                // 数据结果（上半包）
                redisTemplate.opsForHash().put("algoResponse", "A", body);
                log.info("获取上半包数据");
                if(Objects.nonNull(redisTemplate.opsForHash().get("algoResponse","B"))){
                    // 开始合体
                    usefulBody = (redisTemplate.opsForHash().entries("algoResponse")).get("A").toString().concat(body);
                    log.info("success:" + usefulBody);
                    redisTemplate.delete("algoResponse");
                }
            }
        } else if (body.matches(".*?\"2\"}") || body.matches(".*?}")) {
            // 数据结果（下半包）
            redisTemplate.opsForHash().put("algoResponse", "B", body);
            log.info("获取下半包数据");
            if (Objects.nonNull(redisTemplate.opsForHash().get("algoResponse","A"))){
                // 开始合体
                usefulBody = (redisTemplate.opsForHash().entries("algoResponse")).get("A").toString().concat(body);
                log.info("success:" + usefulBody);
                redisTemplate.delete("algoResponse");
            }
        }
        log.info("usefulBody:" + usefulBody);
        return usefulBody;
    }


    /**
     * ------巡视点告警配置判断（综合告警预判断）-------
     *
     * @param value 值
     * @param stdDeviceMeteName 测点名称
     * @param meteKind 测点类型
     * @param alarmState 遥信预告警状态
     * @param stateZero 遥信状态0
     * @param stateOne 遥信状态1
     * @param alarmLevel 告警级别
     * @param highLimit1 遥测 上1 阈值
     * @param lowLimit1 遥测 下1 阈值
     * @param highLimit2 遥测 上2 阈值
     * @param lowLimit2 遥测 下2 阈值
     * @param highLimit3 遥测 上3 阈值
     * @param lowLimit3 遥测 下3 阈值
     * @param highLimit4 遥测 上4 阈值
     * @param lowLimit4 遥测 下4 阈值
     * @return  是否产生告警  0/1
     */
   public Map<String,Object> alarmJudge(String value, String stdDeviceMeteName,
                                        String meteKind,
                                        Integer alarmState, String stateZero,
                                        String stateOne, Integer alarmLevel,
                                        Float highLimit1, Float lowLimit1,
                                        Float highLimit2, Float lowLimit2,
                                        Float highLimit3, Float lowLimit3,
                                        Float highLimit4, Float lowLimit4){
       Map<String,Object> resultMap = new HashMap<>(8);
       try {
           int flag = warnSettings(meteKind, stateZero, alarmState, highLimit1, lowLimit1, highLimit2, lowLimit2,
                   highLimit3, lowLimit3, highLimit4, lowLimit4);
           if (flag !=1 ){
               // 未配置告警规则
               resultMap.put("isWarn", false);
               resultMap.put("warnLevel", 0);
               resultMap.put("warnName", null);
               resultMap.put("warnContent", null);
               resultMap.put("outRange", null);
               resultMap.put("warnTime", null);
               return resultMap;
           }
           // 告警信息拼装并返回
           warnInfoSetting(value, stdDeviceMeteName, meteKind, alarmState, stateZero, stateOne, alarmLevel,
                   highLimit1, lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4, resultMap);
       }catch (Exception e){
           log.error(e.getMessage(), e);
       }

       return resultMap;
   }

    private Map<String,Object> warnInfoSetting(String value, String stdDeviceMeteName, String meteKind,
                                               Integer alarmState, String stateZero, String stateOne,
                                               Integer alarmLevel, Float highLimit1, Float lowLimit1,
                                               Float highLimit2, Float lowLimit2, Float highLimit3,
                                               Float lowLimit3, Float highLimit4, Float lowLimit4,
                                               Map<String, Object> resultMap) {
        Boolean isWarn = false;
        Integer warnLevel = 0;
        String warnName = null;
        String warnContent = null;
        //超越浮动值
        String outRange = null;
        Date warnTime = null;
        switch (meteKind) {
            case "1":
                warnLevel = alarmLevel;
                if (warnJudgementTelesignaling(value, stateZero, stateOne, alarmState) == 1) {
                    isWarn = true;
                    warnName = stdDeviceMeteName;
                    switch (alarmState) {
                        case 0:
                            warnContent = stdDeviceMeteName + ":" + stateZero + "--" + "状态" + selectDictNote(warnLevel.toString(), "alarm_level");
                            break;
                        case 1:
                            warnContent = stdDeviceMeteName + ":" + stateOne + "--" + "状态" + selectDictNote(warnLevel.toString(), "alarm_level");
                        default:
                            break;
                    }
                    warnTime = new Date();
                }
                break;
            case "2":
                if (value.matches("^[a-zA-Z_\\u4e00-\\u9fa5_\\--]+$")) {
                    break;
                } else {
                    int level = warnJudgement(Float.valueOf(value), highLimit1, lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
                    log.info("level-------------:" + level);
                    if (level > 0) {
                        isWarn = true;
                        warnName = stdDeviceMeteName + "数据异常";
                        warnTime = new Date();
                        Float resultValueMeter = Float.valueOf(value);
                        switch (level) {
                            case 1:
                                warnLevel = Integer.valueOf(selectDictCode("alarm_level", "预警"));
                                warnContent = stdDeviceMeteName + ":" + value + "--" + "预警";
                                if (resultValueMeter >= highLimit1) {
                                    outRange = String.valueOf(resultValueMeter - highLimit1);
                                } else {
                                    outRange = String.valueOf(lowLimit1 - resultValueMeter);
                                }
                                break;
                            case 2:
                                warnLevel = Integer.valueOf(selectDictCode("alarm_level", "一般告警"));
                                warnContent = stdDeviceMeteName + ":" + value + "--" + "一般告警";
                                if (resultValueMeter >= highLimit2) {
                                    outRange = String.valueOf(resultValueMeter - highLimit2);
                                } else {
                                    outRange = String.valueOf(lowLimit2 - resultValueMeter);
                                }
                                break;
                            case 3:
                                warnLevel = Integer.valueOf(selectDictCode("alarm_level", "严重告警"));
                                warnContent = stdDeviceMeteName + ":" + value + "-" + "严重告警";
                                if (resultValueMeter >= highLimit3) {
                                    outRange = String.valueOf(resultValueMeter - highLimit3);
                                } else {
                                    outRange = String.valueOf(lowLimit3 - resultValueMeter);
                                }
                                break;
                            case 4:
                                warnLevel = Integer.valueOf(selectDictCode("alarm_level", "危急告警"));
                                warnContent = stdDeviceMeteName + ":" + value + "-" + "危急告警";
                                if (resultValueMeter >= highLimit4) {
                                    outRange = String.valueOf(resultValueMeter - highLimit4);
                                } else {
                                    outRange = String.valueOf(lowLimit4 - resultValueMeter);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                break;
            default:
                break;
        }
        resultMap.put("isWarn", isWarn);
        resultMap.put("warnLevel", warnLevel);
        resultMap.put("warnName", warnName);
        resultMap.put("warnContent", warnContent);
        resultMap.put("outRange", outRange);
        resultMap.put("warnTime", warnTime);
        return resultMap;
    }


    /**
     * 判断是否配置了告警规则
     */
    @Transactional(rollbackFor = Exception.class)
    public int warnSettings(String meteKind, String stateZero, Integer alarmState,
                            Float highLimit1, Float lowLimit1,
                            Float highLimit2, Float lowLimit2,
                            Float highLimit3, Float lowLimit3,
                            Float highLimit4, Float lowLimit4) {
        Set<Float> alarmMeter = new HashSet<>();
        alarmMeter.add(highLimit1);
        alarmMeter.add(lowLimit1);
        alarmMeter.add(highLimit2);
        alarmMeter.add(lowLimit2);
        alarmMeter.add(highLimit3);
        alarmMeter.add(lowLimit3);
        alarmMeter.add(highLimit4);
        alarmMeter.add(lowLimit4);
        alarmMeter.remove(null);
        log.info("alarmMeter:{}", alarmMeter);

        int warnFlag = 0;
        if (Objects.isNull(meteKind)){
            return warnFlag;
        }

        switch (meteKind){
            case "1":
                if(Objects.nonNull(stateZero) && Objects.nonNull(alarmState)){
                    warnFlag=1;
                }
                break;
            case "2":
                if(alarmMeter.size() > 0){
                    warnFlag=1;
                }
                break;
            default:
                break;
        }
        return warnFlag;
    }


    /**
     * -------表计识别 数据告警判断 --------
     * @return 返回遥测告警等级或未告警  4:危急  3:严重  2:一般  1:预警  0:正常
     */
    @Transactional(rollbackFor = Exception.class)
    public int warnJudgement(Float value,
                             Float highLimit1, Float lowLimit1,
                             Float highLimit2, Float lowLimit2,
                             Float highLimit3, Float lowLimit3,
                             Float highLimit4, Float lowLimit4) {
        log.info("value:{},highLimit1:{},lowLimit1:{},highLimit2:{},lowLimit2:{},highLimit3:{},lowLimit3:{},highLimit4:{},lowLimit4:{}",
                value, highLimit1, lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
        Boolean emergency1 = false;
        Boolean emergency2 = false;
        Boolean worse1 = false;
        Boolean worse2 = false;
        Boolean general1 = false;
        Boolean general2 = false;
        Boolean warns1 = false;
        Boolean warns2 = false;

        if (Objects.nonNull(lowLimit4)) {
            emergency1 = value <= lowLimit4;
        }
        if (Objects.nonNull(highLimit4)) {
            emergency2 = value >= highLimit4;
        }
        if (Objects.nonNull(lowLimit3)){
            worse1 = value <= lowLimit3;
        }
        if (Objects.nonNull(highLimit3)) {
            worse2 = value >= highLimit3;
        }
        if (Objects.nonNull(lowLimit2)) {
            general1 = value <= lowLimit2;
        }
        if (Objects.nonNull(highLimit2)){
            general2 = value >= highLimit2;
        }
        if (Objects.nonNull(lowLimit1)){
            warns1 = value <= lowLimit1;
        }
        if (Objects.nonNull(highLimit1)) {
            warns2 = value >= highLimit1;
        }
        if (emergency1 || emergency2) {
            return 4;
        } else if (worse1 || worse2) {
            return 3;
        } else if (general1 || general2) {
            return 2;
        } else if (warns1 || warns2) {
            return 1;
        } else {
            return 0;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int warnJudgementTelesignaling(String value, String stateOne, String stateTwo, Integer alarmState) {
        log.info("value:{},stateOne:{},stateTwo:{},alarmState:{}", value, stateOne, stateTwo, alarmState);
        int finalResult = 0;
        // 0-非告警 1-告警
        switch (alarmState) {
            case 0:
                if (value.equals(stateOne)) {
                    finalResult = 1;
                } else {
                    finalResult = 0;
                }
                break;
            case 1:
                if (value.equals(stateTwo)) {
                    finalResult = 1;
                } else {
                    finalResult = 0;
                }
                break;
            default:
                break;
        }
        log.info("finalResult=={}", finalResult);
        return finalResult;
    }

    /**
     * -----缺陷识别结果解析（标签数据转化文字描述）------
     * @param resultValueOrigin 缺陷算法识别结果(逗号分隔)
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public  String resolveDefectResult(String resultValueOrigin) {
        String resultValue = resultValueOrigin.replaceAll(","," ");
        log.info("----缺陷识别结果解析---resultValue:{}", resultValue);
        String defectValue = "";
        //算法设备识别专用
        String defectRealValue = "1";
        List<String> flags = new ArrayList<>();
        String value = resultValue;
        String finalValue = value.replaceAll("[0-9]", "")
                .replaceAll("\\.", "")
                .replace("-","");
        String[] str2 = finalValue.split("\\s+");
        for (int i = 0; i < str2.length; i++) {
            switch (str2[i]) {
                case "wcaqm":
                    defectValue = defectValue + "未穿安全帽" + " ";
                    flags.add("1");
                    break;
                case "wcgz":
                    defectValue = defectValue + "未穿工装" + " ";
                    flags.add("1");
                    break;
                case "rydd":
                    defectValue = defectValue + "人员倒地" + " ";
                    flags.add("1");
                    break;
                case "xy":
                    defectValue = defectValue + "吸烟" + " ";
                    flags.add("1");
                    break;
                case "sly_dmyw":
                    defectValue = defectValue + "地面油污" + " ";
                    flags.add("1");
                    break;
                case "yw_nc":
                    defectValue = defectValue + "异物-鸟巢" + " ";
                    flags.add("1");
                    break;
                case "yw_gkxfw":
                    defectValue = defectValue + "异物-挂空悬浮物" + " ";
                    flags.add("1");
                    break;
                case "bmwh":
                    defectValue = defectValue + "绝缘子表面污秽" + " ";
                    flags.add("1");
                    break;
                case "jyz_pl":
                    defectValue = defectValue + "绝缘子-破裂" + " ";
                    flags.add("1");
                    break;
                case "jyz_lw":
                    defectValue = defectValue + "绝缘子-裂纹" + " ";
                    flags.add("1");
                    break;
                case "hxq_gjbs":
                    defectValue = defectValue + "呼吸器-硅胶变色" + " ";
                    flags.add("1");
                    break;
                case "hxq_gjtps":
                    defectValue = defectValue + "呼吸器-硅胶筒破损" + " ";
                    flags.add("1");
                    break;
                case "ywzt_yfyc":
                    defectValue = defectValue + "油位状态-油封异常" + " ";
                    flags.add("1");
                    break;
                case "bj_bpmh":
                    defectValue = defectValue + "表计-表盘模糊" + " ";
                    flags.add("1");
                    break;
                case "bj_bpps":
                    defectValue = defectValue + "表计-表盘破损" + " ";
                    flags.add("1");
                    break;
                case "bj_wkps":
                    defectValue = defectValue + "表计-外壳破损" + " ";
                    flags.add("1");
                    break;
                case "mcqdmsh":
                    defectValue = defectValue + "门窗墙地面损坏" + " ";
                    flags.add("1");
                    break;
                case "gbps":
                    defectValue = defectValue + "盖板破损" + " ";
                    flags.add("1");
                    break;
                case "gjptwss":
                    defectValue = defectValue + "构架爬梯未上锁" + " ";
                    flags.add("1");
                    break;
                case "xmbhyc":
                    defectValue = defectValue + "箱门闭合异常" + " ";
                    flags.add("1");
                    break;
                case "jsxs":
                    defectValue = defectValue + "金属锈蚀" + " ";
                    flags.add("1");
                    break;
                case"sly_bjbmyw":
                    defectValue=defectValue+"部件表面油污"+ " ";
                    flags.add("1");
                    break;
                case "gbqs":
                    defectValue=defectValue+"盖板缺失"+ " ";
                    flags.add("1");
                    break;
                case "yxcr":
                    defectValue=defectValue+"越线闯入"+ " ";
                    flags.add("1");
                    break;
                case "hzyw":
                    defectValue=defectValue+"火灾烟雾"+ " ";
                    flags.add("1");
                    break;
                case "xdwcr":
                    defectValue=defectValue+"小动物闯入"+ " ";
                    flags.add("1");
                    break;
                case "sndmjs":
                    defectValue=defectValue+"室内地面积水"+ " ";
                    flags.add("1");
                    break;
                case "kgg_ybf":
                    defectValue=defectValue+"开关柜-压板分"+ " ";
                    flags.add("1");
                    break;
                case "kgg_ybh":
                    defectValue=defectValue+"开关柜-压板合"+ " ";
                    flags.add("1");
                    break;
                case "bjdsyc":
                    defectValue=defectValue+"表计读数异常"+" ";
                    flags.add("1");
                    break;
                case "normal":
                    defectValue="图像无差异";
                    flags.add("1");
                    break;
                case "abnormal":
                    defectValue="图像有差异";
                    flags.add("1");
                    break;


                case "dthtps":
                    defectValue="导体护套破损";
                    flags.add("1");
                    break;
                case "yxdghsg":
                    defectValue="引线断股或松股";
                    flags.add("1");
                    break;
                case "wpdaqs":
                    defectValue="未佩戴安全绳";
                    flags.add("1");
                    break;
                case "qmls":
                    defectValue="墙面漏水";
                    flags.add("1");
                    break;
                case "wdls":
                    defectValue="屋顶漏水";
                    flags.add("1");
                    break;
                case "ywzt_ywzsjyc":
                    defectValue="油位指示计异常";
                    flags.add("1");
                    break;
                case "hxq_yfps":
                    defectValue="呼吸器油封破损";
                    flags.add("1");
                    break;
                case "dxdg":
                    defectValue="导线断股";
                    flags.add("1");
                    break;
                case "sly_jhbyw":
                    defectValue = "套管胶合部油污";
                    flags.add("1");
                    break;
                case "hkgnl":
                    defectValue = "汇控柜凝露";
                    flags.add("1");
                    break;
                case "pzqcd":
                    defectValue = "膨胀器冲顶";
                    flags.add("1");
                    break;
                case "drqgd":
                    defectValue = "电容器鼓肚";
                    flags.add("1");
                    break;
                case "":
                    defectValue = defectValue+"";
                    break;
                default:
                    defectRealValue=resultValue;
                    flags.add("1");
                    break;
            }
        }

        if(flags.contains("1")){
            if(!(defectRealValue.equals("1"))){
                return defectRealValue+"device";
            }
            return defectValue;
        }else {
            return "null";
        }

    }


    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectWarnInfo(Long deviceMeteId){
        return analyseDataOperateDao.selectWarnInfo(deviceMeteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long selectCurrentWarn(){
        return analyseDataOperateDao.selectCurrentWarn();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long selectCurrentDefect(){
        return analyseDataOperateDao.selectCurrentDefect();
    }

    public void lowTaskGoOn(String taskId){
        String lowTaskKey = "lowTask:" + taskId;
        List<String> lowTaskList = redisTemplate.opsForList().range(lowTaskKey, 0, -1);

        if(lowTaskList != null && lowTaskList.size()>0){
            Map<String, Object> params = new HashMap<>();
            params.put("lowTaskIdList", lowTaskList);
            Constant.otherServerList(lowTaskList,Constant.GET_LOW_TASK_GO_ON);
        }
    }

    public HashMap<String,String> selectDeviceNameInfo(Long instanceId){
        return analyseDataOperateDao.selectDeviceNameInfo(instanceId);
    }

    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath 源文件地址
     * @param targetPathName 目标文件地址名称
     */
    public void uploadFileToUpFtps(String sourcePath, String targetPathName) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getKeypw(), upFtpsConfig.getUsername(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误:{}", e);
        }
    }

    public String selectPMSByCameraId(Long cameraId){
        return analyseDataOperateDao.selectPMSByCameraId(cameraId);
    }
}

