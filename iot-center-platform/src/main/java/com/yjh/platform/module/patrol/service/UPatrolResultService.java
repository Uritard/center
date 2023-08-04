package com.yjh.platform.module.patrol.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.common.utils.ResultConvertUtil;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.entity.enums.IdentifyStateEnum;
import com.yjh.platform.module.task.dao.TWarnInfoDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.ReportManageService;
import com.yjh.platform.module.task.service.TWarnInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Author: lqh
 * @Date: 2022/10/25
 */
@Slf4j
@Service
public class UPatrolResultService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    @Qualifier( "redisTemplateForThree" )
    private RedisTemplate redisTemplateForThree;
    @Autowired
    private UPatrolResultDao uPatrolResultDao;
    @Autowired
    private ReportManageService reportManageService;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private TStdDeviceDao tStdDeviceDao;
    @Autowired
    private TWarnInfoDao tWarnInfoDao;
    @Autowired
    private TWarnInfoService tWarnInfoService;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Autowired
    private ProcessResultToUpSystem processResultToUpSystem;
    @Autowired
    private LogsRecord logsRecord;

    public List<TCruiseResultExpand> selectTaskByPage(String taskName, Integer cState, Integer cType, Integer deviceType, String startTime,
        String endTime, List<Long> deviceIdList, Integer meteType, String customId, Integer isCheck) {
        List<TCruiseResultExpand> resultExpandList =
            uPatrolResultDao.selectTaskByPage(taskName, cState, cType, deviceType, startTime, endTime, deviceIdList, meteType, customId,
                isCheck);
        DictConvertUtil.DictOptional optional = DictConvertUtil.optional("planType", "cType", "planType").add("taskState", "cState", "taskState");
        DictConvertUtil.DICT.covertToDict(resultExpandList, optional);

        return resultExpandList;
    }
    public List<String> selectTaskByPageByPage(String taskName, Integer cState, Integer cType, Integer deviceType, String startTime,
        String endTime, List<Long> deviceIdList, Integer meteType, String customId, Integer isCheck) {
        return uPatrolResultDao.selectTaskByPageByPage(taskName, cState, cType, deviceType, startTime, endTime, deviceIdList, meteType, customId,
                isCheck);
    }

    public List<CruiseResultDetail> selectCruiseByPage(String taskId, Integer cruiseType, Integer cruiseResult, Integer deviceType,
        String startTime, String endTime, List<Long> deviceIdList, String customId,Integer isWarn) {
        List<CruiseResultDetail> cruiseResultDetailList =
                uPatrolResultDao.selectCruiseByPage(taskId, cruiseType, cruiseResult, deviceType, startTime, endTime, deviceIdList,
                    customId, isWarn);

        DictConvertUtil.DictOptional optional = DictConvertUtil.optional("cruiseType").add("cruiseResult").add("evaluationState")
            .add("abnormalType", "cruiseAbnormal", "abnormalType").add("identifyResult").add("alarmLevel").add("meteKind");
        DictConvertUtil.DICT.covertToDict(cruiseResultDetailList, optional);

            String currentEdge = (String) redisTemplate.opsForHash().get("t_sys_param:edgeName", "content");
            cruiseResultDetailList.forEach(c -> {
                if (StringUtils.isBlank(c.getEdgeName())) {
                    c.setEdgeName(currentEdge);
                }
            });
        return cruiseResultDetailList;
    }

    public List<StatisticalResult> taskStatistical() {

        // DateTimeUtil.getCurMonthDate()
        Date now = new Date();
        DateTime start = DateUtil.beginOfWeek(now);
        DateTime lastWeek = DateUtil.lastWeek();

        String weekStart = start.toString("yyyy-MM-dd") + " 00:00:00";//本周一的日期
        String weekEnd = DateUtil.endOfWeek(now).toString("yyyy-MM-dd") + " 23:59:59";//本周日的日期
        String lastWeekStart = DateUtil.beginOfWeek(lastWeek).toString("yyyy-MM-dd") + " 00:00:00";//上周一的日期
        String lastWeekend = DateUtil.endOfWeek(lastWeek).toString("yyyy-MM-dd") + " 23:59:59";//上周日的日期
        log.info("本周一：" + weekStart + ",本周日：" + weekEnd + ",上周一：" + lastWeekStart + ",上周日：" + lastWeekend);

        String colName1 = "plan_type";

        List<StatisticalResult> taskStatisticalList = new ArrayList<>();

        StatisticalResult statisticalResult = new StatisticalResult();
        statisticalResult.setTimeNode("本周");
        statisticalResult.setStatisticalList(uPatrolResultDao.taskStatistical(colName1, weekStart, weekEnd));
        taskStatisticalList.add(statisticalResult);

        StatisticalResult statisticalResult1 = new StatisticalResult();
        statisticalResult1.setTimeNode("上周");
        statisticalResult1.setStatisticalList(uPatrolResultDao.taskStatistical(colName1, lastWeekStart, lastWeekend));
        taskStatisticalList.add(statisticalResult1);

        return taskStatisticalList;
    }

    public List<CruiseStatistical> cruiseStatisticalByAbnormal() {
        return uPatrolResultDao.cruiseStatisticalByAbnormal();
    }

    public List<CruiseResultDetail> selectAbnormalResult(String taskResultId, Integer cruiseType, Integer cruiseResult, Integer deviceType,
        String instanceName, String startTime, String endTime, List<Long> deviceIdList, String customId) {
        List<CruiseResultDetail> cruiseResultDetailList = new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()) {
            cruiseResultDetailList =
                uPatrolResultDao.selectAbnormalResult(taskResultId, cruiseType, cruiseResult, deviceType, instanceName, startTime, endTime,
                    deviceIdList, customId);
        }
        DictConvertUtil.DictOptional dictOptional = DictConvertUtil.optional("cruiseType")
            .add("evaluationState")
            .add("cruiseResult")
            .add("abnormalType","cruiseAbnormal","abnormalType1")
            .add("abnormalType","remark","abnormalType2")
            .add("identifyResult")
            .add("meteKind")
            .add("alarmLevel");

        DictConvertUtil.DICT.covertToDict(cruiseResultDetailList,dictOptional);

        cruiseResultDetailList.forEach(cruiseResultDetail -> {
            if (StringUtils.isNotBlank(cruiseResultDetail.getRemark())) {
                cruiseResultDetail.setAbnormalType(cruiseResultDetail.getAbnormalType1() + "," + cruiseResultDetail.getAbnormalType2());
            } else {
                cruiseResultDetail.setAbnormalType(cruiseResultDetail.getAbnormalType1());
            }
        });
        return cruiseResultDetailList;
    }

    public List<TaskSimpleInfo> selectTaskIsRunning() {
        List<TaskSimpleInfo> novelTaskList = uPatrolResultDao.selectTaskIsRunning();
        for (TaskSimpleInfo temTask : novelTaskList) {
            List<Long> counts = uPatrolResultDao.cruiseInspectCount(temTask.getTaskId());
            temTask.setDeviceMeteCount(counts.get(0));
            temTask.setCameraCount(counts.get(1));
            temTask.setRobotPointsCount(counts.get(2));

        }

        return novelTaskList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int manualReview(CruiseManualReview cruiseManualReview, String userId) {
        String thisPointType = "表计";
        Map<String,Object> result = uPatrolResultDao.selectMeteInfoByInstanceId(cruiseManualReview.getInstanceId());
        if (Objects.nonNull(result)){
            String ai = String.valueOf(result.get("ai"));
            String judge = String.valueOf(result.get("judge"));
            if (IdentifyStateEnum.FAULT.getCode().equals(cruiseManualReview.getIdentifyState())) {
                String KEY_PREFIX = "";
                if ("on".equals(ai)) {
                    //缺陷
                    KEY_PREFIX ="DEFECT:" + cruiseManualReview.getInstanceId();
                    //将redisData存入redis
                } else if ("on".equals(judge)){
                    KEY_PREFIX ="JUDGE:" + cruiseManualReview.getInstanceId();
                }
                //塞
                if (StringUtils.isNotEmpty(KEY_PREFIX)) {
                    redisTemplateForThree.opsForValue().set(KEY_PREFIX,cruiseManualReview.getPersonCheck());
                }
            }

            if ("on".equals(ai)){
                thisPointType = "缺陷";
            } else if ("on".equals(judge)){
                thisPointType = "判别";
            }
        }


        String userName = (String)redisTemplate.opsForHash().entries("userInfo:" + userId).get("userName");
        Date date = new Date();
        cruiseManualReview.setCheckUser(userName);
        cruiseManualReview.setCheckDate(date);
        cruiseManualReview.setModifyNum(ResultConvertUtil.convertResult(cruiseManualReview.getPersonCheck(),thisPointType));
        //manualReview
        int result1 = uPatrolResultDao.manualReview(cruiseManualReview);

        //查询该巡检点审核后的相关信息
        AfterManualReviewInfo afterManualReviewInfo =
            uPatrolResultDao.selectJudgeCondition(cruiseManualReview.getInstanceId(), cruiseManualReview.getTaskId());
        //更新测点信息
        TStdDeviceMeteUpdate stdDeviceMeteUpdate = new TStdDeviceMeteUpdate().setDeviceMeteId(afterManualReviewInfo.getDeviceMeteId())
            .setIdentifyResult(cruiseManualReview.getIdentifyResult()).setUpdateTime(afterManualReviewInfo.getCruiseTime());
        uPatrolResultDao.updateDeviceMeteUpdate(stdDeviceMeteUpdate);

        // 对审核后的任务进行处理，判断告警
        afterManualReviewInfo(afterManualReviewInfo, userId, date);

        // 审核结果向上级系统同步
        processResultToUpSystem.reviewToUpSystem(Collections.singletonList(cruiseManualReview), false);

        int result2 = patrolTaskReview(cruiseManualReview.getTaskId());
        //        insert QrDecode as device's real code. by tt.
        TCruisePointInstance tCruisePointInstance = tCruisePointInstanceDao.selectByPrimaryId(cruiseManualReview.getInstanceId());
        if (Objects.nonNull(tCruisePointInstance)) {
            String analyseType = uPatrolResultDao.selectAlgorithmType(tCruisePointInstance.getDeviceMeteId());
            if (Objects.nonNull(analyseType) && Objects.equals(analyseType, "8")) {
                if (!NumberUtils.isNumber(cruiseManualReview.getPersonCheck())){
                    throw new BusinessException("实物编码应为纯数字！");
                }
                TStdDevice tStdDevice = new TStdDevice();
                tStdDevice.setDeviceId(tCruisePointInstance.getDeviceId());
                tStdDevice.setRealCode(cruiseManualReview.getPersonCheck());
                tStdDeviceDao.update(tStdDevice);
            }
        }
        return result1 + result2;
    }

    /**
     * 对整个任务状态进行判断，修改审核状态，并生成巡视报告
     */
    private int patrolTaskReview(String taskId) {
        //获取审核后该任务下的巡检点审核信息
        List<CruiseManualReview> cruiseManualReviewList = uPatrolResultDao.selectManualDetail(taskId);

        //判断是否全部审核，若都已审核，统计所有的审核人，统计最晚审核的时间，将信息插入
        HashSet<String> haS1 = new HashSet<>();
        int checkedSize = 0;
        Date lastDate = null;
        for (CruiseManualReview cmr : cruiseManualReviewList) {
            if (cmr.getEvaluationState() == 256) {
                haS1.add(cmr.getCheckUser());
                checkedSize++;
                if (lastDate == null || (cmr.getCheckDate() != null && cmr.getCheckDate().after(lastDate))) {
                    lastDate = cmr.getCheckDate();
                }
            }
        }
        String checkUserName = StringUtils.join(haS1, ",");

        int result2 = 0;
        if (checkedSize == cruiseManualReviewList.size()) {
            result2 = uPatrolResultDao.updateCheck(taskId, checkUserName, lastDate, "1");
            //自动生成巡视报告
            String reportFilePath = reportManageService.cruiseReportGenerate(taskId);
            log.info("自动生成巡视报告的路径是=={}", reportFilePath);
        }
        return result2;
    }

    /**
     * 对审核后的任务进行处理，判断告警
     */
    private void afterManualReviewInfo(AfterManualReviewInfo afterManualReviewInfo, String userId, Date date) {
        String taskId = afterManualReviewInfo.getTaskId();
        Long instanceId = afterManualReviewInfo.getInstanceId();
        //查询该巡检点对应测点配置的告警阈值相关信息
        TStdDevicemete tStdDevicemete = uPatrolResultDao.selectDeviceMeteInfo(instanceId);
        log.info("tStdDeviceMete===" + tStdDevicemete);

        // 该巡视点无了,找不到对应
        if (Objects.isNull(tStdDevicemete)){
            return;
        }
        String personCheck = afterManualReviewInfo.getModifyNum().split(",")[0];
        String checkDesc = afterManualReviewInfo.getPersonCheck();

        Map<String, String> initInfo = new HashMap<>(16);
        initInfo.put("valueTemp", personCheck);

        String sysLevel = Constant.getLevelEdge();
        boolean isTemDif = StringUtils.equals("2", sysLevel)
                && 1 == tStdDevicemete.getIsTemdif()
                && Objects.equals("222", tStdDevicemete.getMeteType());
        if (isTemDif){
            // 配置了红外温差任务用差值去判断告警
            String temperature = String.valueOf(redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("temperature", ""));
            if (!CommonUtils.isEmptyOrNullstr(temperature)){
                double abs = Math.abs(Double.parseDouble(temperature) - Double.parseDouble(personCheck));
                initInfo.put("valueTemp", String.valueOf(abs));
                initInfo.put("temperature", temperature);
                initInfo.put("warnName", tStdDevicemete.getMeteName() + "温差任务");
                initInfo.put("warnContent", "传感器环境温度与测温产生温差:环境" + temperature + "--测温" + checkDesc + "--温差" + abs);
                initInfo.put("outRange", String.valueOf(abs));
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("value", personCheck);
        params.put("valueDesc", checkDesc);
        params.put("stdDeviceMeteName", tStdDevicemete.getMeteName());
        params.put("meteKind", tStdDevicemete.getMeteKind());
        params.put("alarmState", tStdDevicemete.getAlarmState());
        params.put("stateZero", tStdDevicemete.getStateZero());
        params.put("stateOne", tStdDevicemete.getStateOne());
        params.put("alarmLevel", tStdDevicemete.getAlarmLevel());
        params.put("highLimit1", tStdDevicemete.getHighLimit1());
        params.put("lowLimit1", tStdDevicemete.getLowLimit1());
        params.put("highLimit2", tStdDevicemete.getHighLimit2());
        params.put("lowLimit2", tStdDevicemete.getLowLimit2());
        params.put("highLimit3", tStdDevicemete.getHighLimit3());
        params.put("lowLimit3", tStdDevicemete.getLowLimit3());
        params.put("highLimit4", tStdDevicemete.getHighLimit4());
        params.put("lowLimit4", tStdDevicemete.getLowLimit4());
        log.info("params的值是===" + params);

        Result result = sendPostRequest(Constant.WARN_JUDGE, params);
        Map<String, Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
        log.info("object转map的东西===" + map);
        Boolean isWarN = false;
        String outRange = null;
        if (Objects.nonNull(map)) {
            isWarN = (Boolean)map.get("isWarn");
            if (Objects.nonNull(map.get("outRange"))) {
                outRange = map.get("outRange").toString();
            }
        }
        // 审核值为-1 不会产生告警
        if ("-1".equals(afterManualReviewInfo.getModifyNum())) {
            isWarN = false;
        }

        //组装告警基本信息
        TWarnInfo warnInfo = new TWarnInfo();
        warnInfo.setWarnTime(date);
        warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
        warnInfo.setCunstomId(tStdDevicemete.getCustomId());
        warnInfo.setInstanceId(afterManualReviewInfo.getInstanceId());
        warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
        warnInfo.setConfMode(275);
        warnInfo.setDealType(286);
        warnInfo.setDealInfo("程序正常，告警属实");
        Integer warnFlag = NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("defectModel", "其他"), 450);
        warnInfo.setDefectModel(warnFlag);
        // String type = tRobotInspectionDao.selectTypeByInstanceId(afterManualReviewInfo.getInstanceId());
        int alarmSource= 998;
        String type = DictConvertUtil.DICT.covertToDict("cruiseType", afterManualReviewInfo.getCruiseType());
        if (StringUtils.isNotEmpty(type)){
            alarmSource = NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("alarmSource", type), alarmSource);
        }
        warnInfo.setAlarmSource(alarmSource);
        warnInfo.setImagePath(afterManualReviewInfo.getPicPath());
        warnInfo.setValue(afterManualReviewInfo.getPersonCheck());
        warnInfo.setTaskId(afterManualReviewInfo.getTaskId());
        log.info("warnInfo==" + warnInfo);

        //判断该点是否已在告警表  声纹测点分开处理
        if (998 == alarmSource){
            //声纹测点 声纹告警规则
            map = voiceIsWarn(afterManualReviewInfo.getModifyNum(),tStdDevicemete.getDBValue(),
                    tStdDevicemete.getFValue(),tStdDevicemete.getMeteName());
            reviewVoiceWarn(afterManualReviewInfo,map,userId,date,warnInfo,taskId,instanceId);
        }else {
            if (afterManualReviewInfo.getIsWarn() >= 1) {
                List<TWarnInfo> warnIdList =
                        uPatrolResultDao.selectWarnId(afterManualReviewInfo.getTaskId(), afterManualReviewInfo.getInstanceId());
                for (TWarnInfo tWarnInfo : warnIdList) {
                    if ((tWarnInfo.getAlarmOwner() != null && 1 == tWarnInfo.getAlarmOwner())) {
                        log.info("下级的不做处理" + tWarnInfo);
                        continue;
                    }
                    //存在
                    //判断这个告警是用谁的告警规则产生的 如果是上级的 就是属实 反之不属实 并且 下级的不做处理
                    if (Boolean.TRUE.equals(isWarN)) {
                        //触发告警
                        // 修改告警信息表
                        uPatrolResultDao.updateWarnInfo(tWarnInfo.getWarnId(), isTemDif ? initInfo.get("warnName") : MapUtils.getString(map, "warnName"),
                                MapUtils.getInteger(map, "warnLevel"), isTemDif ? initInfo.get("warnContent") : MapUtils.getString(map, "warnContent"),
                                "程序正常，告警属实", 286, isTemDif ? initInfo.get("outRange") : outRange, userId,
                                date);
                    } else {
                        uPatrolResultDao.updateWarnInfo(tWarnInfo.getWarnId(), null, null, null,
                                "程序异常，告警误报", 287, null, userId, date);
                    }
                    sendWebSocket(tWarnInfo.getWarnId());
                }
            } else {
                //不存在
                //判断该点是否产生告警以及告警信息
                if (Boolean.TRUE.equals(isWarN)) {//触发告警
                    warnInfo.setWarnName(isTemDif ? initInfo.get("warnName") : String.valueOf(map.get("warnName")));
                    warnInfo.setWarnLevel(Integer.valueOf(String.valueOf(map.get("warnLevel"))));
                    warnInfo.setWarnContent(isTemDif ? initInfo.get("warnContent") : String.valueOf(map.get("warnContent")));
                    warnInfo.setOutRange(isTemDif ? initInfo.get("outRange") : outRange);
                    warnInfo.setDealTime(date);
                    warnInfo.setDealPersonId(userId);
                    log.info("要插库的告警数据是===" + warnInfo);
                    tWarnInfoService.insert(warnInfo);
                    sendWebSocket(warnInfo.getWarnId());
                    uPatrolResultDao.updateIsWarn(taskId, instanceId);
                }
            }
        }

    }

    public void reviewVoiceWarn(AfterManualReviewInfo afterManualReviewInfo,Map<String, Object> map,String userId, Date date,
                                TWarnInfo warnInfo,String taskId,Long instanceId){
        List<TWarnInfo> warnIdList =
                uPatrolResultDao.selectWarnId(afterManualReviewInfo.getTaskId(), afterManualReviewInfo.getInstanceId());
        warnIdList.forEach(tWarnInfo ->{
            //声纹的特殊处理
            if (tWarnInfo.getWarnName().contains("分贝") && ValueUtil.toBoolean(map.get("isDbWarn"),false) ){
                uPatrolResultDao.updateWarnInfo(tWarnInfo.getWarnId(), null,
                        null, MapUtils.getString(map, "dbWarnContent"),
                        "程序正常，告警属实", 286, MapUtils.getString(map, "outDbRange"), userId,
                        date);
                map.remove("isDbWarn");
            } else if (tWarnInfo.getWarnName().contains("频率") && ValueUtil.toBoolean(map.get("isFWarn"),false)){
                uPatrolResultDao.updateWarnInfo(tWarnInfo.getWarnId(), null,
                        null, MapUtils.getString(map, "fWarnContent"),
                        "程序正常，告警属实", 286, MapUtils.getString(map, "outFRange"), userId,
                        date);
                map.remove("isFWarn");
            } else {
                uPatrolResultDao.updateWarnInfo(tWarnInfo.getWarnId(), null, null, null,
                        "程序异常，告警误报", 287, null, userId, date);
            }
        });
        //处理声纹的
        if (ValueUtil.toBoolean(map.get("isDbWarn"),false)){
            warnInfo.setWarnId(null);
            warnInfo.setWarnName(MapUtils.getString(map, "warnDbName"));
            warnInfo.setWarnLevel(131);
            warnInfo.setWarnContent(MapUtils.getString(map, "dbWarnContent"));
            warnInfo.setOutRange(MapUtils.getString(map, "outDbRange"));
            warnInfo.setDealTime(date);
            warnInfo.setDealPersonId(userId);
            log.info("要插库的告警数据是===" + warnInfo);
            tWarnInfoService.insert(warnInfo);
            sendWebSocket(warnInfo.getWarnId());
            uPatrolResultDao.updateIsWarn(taskId, instanceId);
        }
        if (ValueUtil.toBoolean(map.get("isFWarn"),false)){
            warnInfo.setWarnId(null);
            warnInfo.setWarnName(MapUtils.getString(map, "warnFName"));
            warnInfo.setWarnLevel(131);
            warnInfo.setWarnContent(MapUtils.getString(map, "fWarnContent"));
            warnInfo.setOutRange(MapUtils.getString(map, "outFRange"));
            warnInfo.setDealTime(date);
            warnInfo.setDealPersonId(userId);
            log.info("要插库的告警数据是===" + warnInfo);
            tWarnInfoService.insert(warnInfo);
            sendWebSocket(warnInfo.getWarnId());
            uPatrolResultDao.updateIsWarn(taskId, instanceId);
        }
    }

    public Map<String, Object> voiceIsWarn(String personCheck,int warnDbVal, int warnfVal,String meteName){
        Map<String, Object> map =new HashMap<>(8);
        String[] str = personCheck.split(",");
        log.info("personCheck:{},warnDbVal:{},warnfVal:{}",personCheck,warnDbVal,warnfVal);
        if (str.length == 2){
            //按照要求 前面是分贝  后面是频率
            int maxDbVal = ValueUtil.toInteger(str[0],0);
            int maxfVal = ValueUtil.toInteger(str[1],0);
            if (maxDbVal > warnDbVal){
                map.put("isDbWarn",true);
                map.put("warnDbName",meteName+"分贝数据异常");
                map.put("dbWarnContent",meteName+"分贝:"+maxDbVal+"--一般告警");
                map.put("outDbRange",maxDbVal-warnDbVal);
            }
            if (maxfVal > warnfVal){
                map.put("isFWarn",true);
                map.put("warnFName",meteName+"频率数据异常");
                map.put("fWarnContent",meteName+"频率:"+maxfVal+"--一般告警");
                map.put("outFRange",maxfVal-warnfVal);
            }
        } else {
            map.put("isWarn",false);
        }
        return map;
    }

    public int manualReviewTask(String taskId, String userId, HttpServletRequest request) {
        Date date = new Date();
        String userName = (String)redisTemplate.opsForHash().entries("userInfo:" + userId).get("userName");
        //审核任务
        UPatrolResult uPatrolResult = uPatrolResultDao.selectByPrimaryId(taskId);
        logsRecord.LogsSend(request, "5", "审核任务", "一键审核任务-" + uPatrolResult.getTaskName());
        //审核未被审核的巡视点
        CruiseManualReview cruiseManualReview = new CruiseManualReview().setCheckUser(userName).setCheckDate(date).setTaskId(taskId);
        uPatrolResultDao.manualReviewByTask(cruiseManualReview);

        List<CruiseManualReview> reviewList =  uPatrolResultDao.selectManualDetail(taskId);

        //判断是否全部审核，若都已审核，统计所有的审核人，统计最晚审核的时间，将信息插入
        HashSet<String> haS1 = new HashSet<>();
        for (CruiseManualReview cmr : reviewList) {
            haS1.add(cmr.getCheckUser());
        }
        String checkUserName = StringUtils.join(haS1, ",");
        int result = uPatrolResultDao.updateCheck(taskId, checkUserName, date, "1");

        uPatrolResultDao.updateWarnInfoByTask(userId, date, taskId);
        tStdDeviceDao.updateByTask(taskId);

        // 审核结果向上级系统同步
        processResultToUpSystem.reviewToUpSystem(reviewList, true);

        //自动生成巡视报告
        String reportFilePath = reportManageService.cruiseReportGenerate(taskId);
        log.info("自动生成巡视报告的路径是==" + reportFilePath);
        return result + reviewList.size();
    }

    /**
     * 下级系统审核信息同步
     */
    public int manualReviewTask(List<CruiseManualReview> resultList) {
        // 审核任务
        for (CruiseManualReview review : resultList) {
            Long originId = review.getInstanceId();
            TCruisePointInstance insInfo = tRobotInspectionDao.selectRealInstance(String.valueOf(originId), review.getSendCode());
            log.info("originId: {}, edgeCode: {}, instanceInfo: {}", originId, review.getSendCode(), JSON.toJSONString(insInfo));
            review.setInstanceId(insInfo.getInstanceId());

            log.info("准备更改的的东西是==={}", JSON.toJSONString(review));
            uPatrolResultDao.manualReviewByTaskInstance(review);

            //更新测点信息
            Long deviceMeteId = uPatrolResultDao.selectDeviceMeteId(review.getInstanceId());
            TStdDeviceMeteUpdate stdDeviceMeteUpdate =
                new TStdDeviceMeteUpdate().setDeviceMeteId(deviceMeteId).setIdentifyResult(review.getIdentifyResult()).setUpdateTime(review.getCruiseTime());
            uPatrolResultDao.updateDeviceMeteUpdate(stdDeviceMeteUpdate);
            // 下级系统同步审核信息不对告警进行重新判断
            // afterManualReviewInfo(review.getTaskId(), review.getInstanceId(), review.getCheckUser(), review.getCheckDate());
        }
        // 校验父级是否需要审核并生成巡视报告
        int result = patrolTaskReview(resultList.get(0).getTaskId());
        return result + resultList.size();
    }

    public Result sendPostRequest(String url, Map<String, Object> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class, params);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }

    public void sendWebSocket(Long warnId) {
        //给前端推webSocket
        Map<String, String> jasonMap = new HashMap<>();
        jasonMap.put("type", "finishedOneAlarm");
        jasonMap.put("alarmId", warnId.toString());
        String json = JSON.toJSONString(jasonMap);
        System.out.println(("发送给前端的消息===" + json));
        try {
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);
        } catch (Exception e) {
            System.out.println("发送websocket出错");
        }

    }
}
