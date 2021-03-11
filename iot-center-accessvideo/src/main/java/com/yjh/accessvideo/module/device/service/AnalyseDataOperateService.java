package com.yjh.accessvideo.module.device.service;


import com.yjh.accessvideo.module.device.dao.AnalyseDataOperateDao;
import com.yjh.accessvideo.module.device.entity.*;
import io.swagger.models.auth.In;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.StyledEditorKit;
import java.util.*;

@Service
public class AnalyseDataOperateService {
    @Autowired
    private AnalyseDataOperateDao analyseDataOperateDao;

    @Autowired
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(AnalyseDataOperateService.class);

    //@Logs(title = "告警信息插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertWarnInfo(TWarnInfo tWarnInfo) {
        return this.analyseDataOperateDao.insertWarnInfo(tWarnInfo);
    }

    //@Logs(title = "告警信息批量插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertWarnInfo(List<TWarnInfo> list) {
        return this.analyseDataOperateDao.batchInsertWarnInfo(list);
    }

    //@Logs(title = "算法结果插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertVideoAnalyseResult(TVideoAnalyseResult tVideoAnalyseResult) {
        return this.analyseDataOperateDao.insertVideoAnalyseResult(tVideoAnalyseResult);
    }

    //@Logs(title = "算法结果批量插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertVideoAnalyseResult(List<TVideoAnalyseResult> list) {
        return this.analyseDataOperateDao.batchInsertVideoAnalyseResult(list);
    }

    //@Logs(title = "巡视任务结果单查", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectByPrimaryIdCruiseResult(String taskResultId) {
        return this.analyseDataOperateDao.selectByPrimaryIdCruiseResult(taskResultId);
    }

    //@Logs(title = "巡视任务结果修改", code = "")
    @Transactional(rollbackFor = Exception.class)
    public int updateCruiseResult(TCruiseResult tCruiseResult) {
        return this.analyseDataOperateDao.updateCruiseResult(tCruiseResult);
    }


    //@Logs(title = "任务-巡视点状态结果单查", code = "")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskResult selectByPrimaryIdCruiseTaskResult(String taskResultId) {
        return this.analyseDataOperateDao.selectByPrimaryIdCruiseTaskResult(taskResultId);
    }

    //@Logs(title = "任务-巡视点状态结构更新", code = "")
    @Transactional(rollbackFor = Exception.class)
    public int updateCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult) {
        return this.analyseDataOperateDao.updateCruiseTaskResult(tCruiseTaskResult);
    }

    //@Logs(title = "任务-巡视点状态结构新增插入", code = "")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult) {
        return this.analyseDataOperateDao.insertCruiseTaskResult(tCruiseTaskResult);
    }

    //@Logs(title = "巡视点详细结果信息插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseTaskResultDetail(TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        return this.analyseDataOperateDao.insertCruiseTaskResultDetail(tCruiseTaskResultDetail);
    }

    //@Logs(title = "巡视点详细结果信息批量插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> list) {
        return this.analyseDataOperateDao.batchInsertCruiseTaskResultDetail(list);
    }

    //@Logs(title = "巡视数据结果信息插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int insertCruiseDataResult(TCruiseDataResult tCruiseDataResult) {
        return this.analyseDataOperateDao.insertCruiseDataResult(tCruiseDataResult);
    }

    //@Logs(title = "巡视数据结果信息批量插入", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseDataResult(List<TCruiseDataResult> list) {
        return this.analyseDataOperateDao.batchInsertCruiseDataResult(list);
    }

    //@Logs(title = "标准测点信息查询", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TStdDevicemete selectByPrimaryIdDeviceMete(Long deviceMeteId) {
        return this.analyseDataOperateDao.selectByPrimaryIdDeviceMete(deviceMeteId);
    }

    //@Logs(title = "根据巡视点ID查询标准测点信息", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TStdDevicemete selectDeviceMeteByInstanceId(Long instanceId) {
        return this.analyseDataOperateDao.selectDeviceMeteByInstanceId(instanceId);
    }

    //@Logs(title = "根据巡视点ID查询巡视点信息", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePointInstance selectPointInstance(Long instanceId) {
        return this.analyseDataOperateDao.selectPointInstance(instanceId);
    }

    //@Logs(title = "查询字典码", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String selectDictCode(String colName, String dictNote) {
        return this.analyseDataOperateDao.selectDictCode(colName, dictNote);
    }

    //@Logs(title = "查询字典内容", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String selectDictNote(String dictCode, String colName) {
        return analyseDataOperateDao.selectDictNote(dictCode, colName);
    }

    //@Logs(title = "查询任务下所有巡视点", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> selectCruiseByTask(String taskId) {
        return this.analyseDataOperateDao.selectCruiseByTaskId(taskId);
    }

    //@Logs(title = "插入缺陷信息")
    @Transactional(rollbackFor = Exception.class)
    public int insertDefectInfo(TDefectInfo tDefectInfo) {
        return this.analyseDataOperateDao.insertDefectInfo(tDefectInfo);
    }

    //@Logs(title = "插入缺陷信息")
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
                  redisTemplate.opsForHash().put(cruiseRedisKey,"temResult","1");//临时值为正常点
                }else {
                  redisTemplate.opsForHash().put(cruiseRedisKey,"temResult","0");//临时值为异常点
                }
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
//            case "1":
//            case "2":
//                resultNum=resultValue;
//                cruiseResultFinal=cruiseResultTem;
//                abnormalTypeFinal=abnormalTypeTem;
//                break;
        }

        handleResultMap.put("resultNum",resultNum);
        handleResultMap.put("cruiseResult",cruiseResultFinal);
        handleResultMap.put("cruiseAbnormal",abnormalTypeFinal);
        log.info("----------巡视结果集产生---------");
        log.info("doubleResultMap----------:"+handleResultMap);
        return handleResultMap;
    }



//    public String nonUnpacking(String body) {
//        log.info("body:"+body);
//        body=body.replaceAll("\\s++","");
//        String usefulBody=body;
//        log.info("content-length:"+body.length());
//        if (body.matches("\\{\"msgData.*?\"2\"}")) { //表计整包
//            log.info("整包数据1");
//        } else if (body.matches("\\{\"msgType.*?}}}}")) { //缺陷整包
//            log.info("整包数据2");
//        } else { //拆包
//            if (body.matches("\\{\"msgData.*?") || body.matches("\\{\"msgType.*?") ) { //拆包A
//                if(body.contains("\"msgType\": \"4\"") || body.contains("\"msgType\":\"4\"")){
//                    usefulBody="";
//                }else if(body.contains("\"msgType\":\"6\"")){
//                    usefulBody=body;
//                }
//                else {
//                    redisTemplate.opsForHash().put("algoResponse", "A", body);
//                    log.info("获取上半包数据");
//                    usefulBody="";
//                }
//            } else if (body.matches(".*?\"2\"}") || body.matches(".*?}}}}")) { //拆包B
//                redisTemplate.opsForHash().put("algoResponse", "B", body);
//                usefulBody = (redisTemplate.opsForHash().entries("algoResponse")).get("A").toString().concat(body);
////                redisTemplate.delete("algoResponse");
//                log.info("success:" + usefulBody);
//            }
//        }
//        log.info("usefulBody:"+usefulBody);
//        return usefulBody;
//
//    }

    /**
     * -------算法服务报文反拆包-------
     * @param body 算法服务返回的报文消息体
     * @return  完整的合并拆包信息的结果报文
     */
    //反拆包
    public String nonUnpacking(String body) {
        //body清除空格
        body = body.replaceAll("\\s++", "");
        String usefulBody = "";//处理结果初始化
        if (body.matches("\\{\"msgData.*?\"2\"}") || body.matches("\\{\"msgType.*?}}}}")) { //数据结果整包
            log.info("整包数据...");
            usefulBody = body;
        } else if (body.matches("\\{\"msgData.*?") || body.matches("\\{\"msgType.*?") || body.matches("}")) {  //结果半包
            if (body.contains("\"msgType\":\"4\"")) { //注册消息

            } else if (body.contains("\"msgType\":\"6\"")) {  //结束消息
                usefulBody = body;
            } else {                                         //数据结果（上半包）
                redisTemplate.opsForHash().put("algoResponse", "A", body);
                log.info("获取上半包数据");
            }

        } else if (body.matches(".*?\"2\"}") || body.matches(".*?}}}}")) {   //数据结果（下半包）
            redisTemplate.opsForHash().put("algoResponse", "B", body);
            usefulBody = (redisTemplate.opsForHash().entries("algoResponse")).get("A").toString().concat(body); //开始合体
//                redisTemplate.delete("algoResponse");
            log.info("success:" + usefulBody);
        }
        log.info("usefulBody:" + usefulBody);
        return usefulBody;

    }


    /**
     * ------巡视点告警配置判断（综合告警预判断）-------
     * @param meteKind 测点类型
     * @param stateZero 遥信状态0
     * @param alarmState 遥信预告警状态
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
    //@Logs(title = "告警配置判断")
    @Transactional(rollbackFor = Exception.class)
    public int warnSettings(String meteKind,
                            String stateZero,
                            Integer alarmState,
                            Float highLimit1,
                            Float lowLimit1,
                            Float highLimit2,
                            Float lowLimit2,
                            Float highLimit3,
                            Float lowLimit3,
                            Float highLimit4,
                            Float lowLimit4) {
        int warnFlag=0;
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
        log.info("alarmMeter:"+alarmMeter);
        if(Objects.nonNull(meteKind)){
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
            }
        }

        return warnFlag;

    }


    /**
     * -------表计识别 数据告警判断 --------
     * @param value 算法识别结果
     * @param highLimit1
     * @param lowLimit1
     * @param highLimit2
     * @param lowLimit2
     * @param highLimit3
     * @param lowLimit3
     * @param highLimit4
     * @param lowLimit4
     * @return 返回遥测告警等级或未告警
     */
    //@Logs(title = "表计识别-告警判断-数值结果判断", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public int warnJudgement(Float value,
                             Float highLimit1,
                             Float lowLimit1,
                             Float highLimit2,
                             Float lowLimit2,
                             Float highLimit3,
                             Float lowLimit3,
                             Float highLimit4,
                             Float lowLimit4) {

        log.info("input----------------------------------:"+value+"--"+
                highLimit1+"*"+
                lowLimit1+"*"+
                highLimit2+"*"+
                 lowLimit2+"*"+
                 highLimit3+"*"+
                 lowLimit3+"*"+
                highLimit4+"*"+
               lowLimit4);

        Boolean emergency1 = false;
        Boolean emergency2 = false;
        Boolean worse1 = false;
        Boolean worse2 = false;
        Boolean general1 = false;
        Boolean general2 = false;
        Boolean warns1 = false;
        Boolean warns2 = false;

        if (Objects.nonNull(lowLimit4))
            emergency1 = value <= lowLimit4;
        if (Objects.nonNull(highLimit4))
            emergency2 = value >= highLimit4;
        if (Objects.nonNull(lowLimit3))
            worse1 = value <= lowLimit3;
        if (Objects.nonNull(highLimit3))
            worse2 = value >= highLimit3;
        if (Objects.nonNull(lowLimit2))
            general1 = value <= lowLimit2;
        if (Objects.nonNull(highLimit2))
            general2 = value >= highLimit2;
        if (Objects.nonNull(lowLimit1))
            warns1 = value <= lowLimit1;
        if (Objects.nonNull(highLimit1))
            warns2 = value >= highLimit1;

        if (emergency1 || emergency2) {
            return 4;//危急
        } else if (worse1 || worse2) {
            return 3;//严重
        } else if (general1 || general2) {
            return 2;//一般
        } else if (warns1 || warns2) {
            return 1;//预警
        } else {
            return 0;//正常
        }

    }

    //@Logs(title = "表计识别-告警判断-文字结果判断", code = "")
    @Transactional(rollbackFor = Exception.class)
    public int warnJudgementTelesignaling(String value, String stateOne, String stateTwo, Integer alarmState) {
        log.info("value" + value);
        log.info("stateOne" + stateOne);
        log.info("stateTwo" + stateTwo);
        log.info("alarmState" + alarmState);
        int finalResult = 0;//0-非告警 1-告警
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
        }
        return finalResult;
    }

    // TODO: 2020/11/21 表计识别结果告警判断--文字结果判断


    /**
     * -----缺陷识别结果解析（标签数据转化文字描述）------
     * @param resultValueOrigin 缺陷算法识别结果(逗号分隔)
     * @return
     */
    //@Logs(title = "缺陷识别结果解析", code = "")
    @Transactional(rollbackFor = Exception.class)
    public String resolveDefectResult(String resultValueOrigin) {
        String resultValue=resultValueOrigin.replaceAll(","," ");
        log.info("----缺陷识别结果解析---resultValue:" + resultValue);
        String defectValue = "";
        String value = resultValue;
        String finalValue = value.replaceAll("[0-9]", "");
        String[] str2 = finalValue.split("\\s+");
        for (int i = 0; i < str2.length; i++) {
            switch (str2[i]) {
                case "wcaqm":
                    defectValue = defectValue + "未穿安全帽" + " ";
                    break;
                case "wcgz":
                    defectValue = defectValue + "未穿工装" + " ";
                    break;
                case "rydd":
                    defectValue = defectValue + "人员倒地" + " ";
                    break;
                case "xy":
                    defectValue = defectValue + "吸烟" + " ";
                    break;
                case "sly_dmyw":
                    defectValue = defectValue + "地面油污" + " ";
                    break;
                case "yw_nc":
                    defectValue = defectValue + "鸟窝" + " ";
                    break;
                case "yw_gkxfw":
                    defectValue = defectValue + "挂空悬浮物" + " ";
                    break;
                case "bmwh":
                    defectValue = defectValue + "表面污秽" + " ";
                    break;
                case "jyz_pl":
                    defectValue = defectValue + "绝缘子-破裂" + " ";
                    break;
                case "jyz_lw":
                    defectValue = defectValue + "绝缘子-裂纹" + " ";
                    break;
                case "hxq_gjbs":
                    defectValue = defectValue + "呼吸器-硅胶变色" + " ";
                    break;
                case "hxq_gjtps":
                    defectValue = defectValue + "呼吸器-硅胶筒破损" + " ";
                    break;
                case "ywzt_yfyc":
                    defectValue = defectValue + "油位状态-油封异常" + " ";
                    break;
                case "bj_bpmh":
                    defectValue = defectValue + "表计-表盘模糊" + " ";
                    break;
                case "bj_bpps":
                    defectValue = defectValue + "表计-表盘破损" + " ";
                    break;
                case "bj_wkps":
                    defectValue = defectValue + "表计-外壳破损" + " ";
                    break;
                case "mcqdmsh":
                    defectValue = defectValue + "门窗墙地面损坏" + " ";
                    break;
                case "gbps":
                    defectValue = defectValue + "盖板破损" + " ";
                    break;
                case "gjptwss":
                    defectValue = defectValue + "构架爬梯未上锁" + " ";
                    break;
                case "xmbhyc":
                    defectValue = defectValue + "箱门闭合异常" + " ";
                    break;
                case "jsxs":
                    defectValue = defectValue + "金属锈蚀" + " ";
                    break;
                case"sly_bjbmyw":
                    defectValue=defectValue+"部件表面油污"+ " ";
                    break;
                case "gbqs":
                    defectValue=defectValue+"盖板缺失"+ " ";
                    break;
                case "yxcr":
                    defectValue=defectValue+"越线闯入"+ " ";
                    break;
                case "hzyw":
                    defectValue=defectValue+"烟火识别"+ " ";
                    break;
                case "xdwsb":
                    defectValue=defectValue+"小动物识别"+ " ";
                    break;
                case "smdmjs":
                    defectValue=defectValue+"积水检测"+ " ";
                    break;
                case "kgg_ybf":
                    defectValue=defectValue+"压板分"+ " ";
                    break;
                case "kgg_ybh":
                    defectValue=defectValue+"压板合"+ " ";
                    break;
                case "bjdsyc":
                    defectValue=defectValue+"表计读数异常"+" ";
                    break;
                default:
                    return "null";
            }
        }
        return defectValue;
    }


    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectWarnInfo(Long deviceMeteId){
        return analyseDataOperateDao.selectWarnInfo(deviceMeteId);
    }
}

