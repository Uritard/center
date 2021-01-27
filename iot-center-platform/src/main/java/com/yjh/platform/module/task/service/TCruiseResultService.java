package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;

import com.alibaba.druid.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Sets;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceAttrDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
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

import java.util.*;

/**
 * @author czh
 * @since 2020-08-25
 */
@Service
public class TCruiseResultService{

    private Logger log = LoggerFactory.getLogger(TCruiseResultService.class);
    @Autowired
    private TCruiseResultDao tCruiseResultDao;
    @Autowired
    private ReportManageService reportManageService;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private TStdDeviceAttrDao tStdDeviceAttrDao;
    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Autowired
    private TStdDeviceDao tStdDeviceDao;
    @Autowired
    private TWarnInfoDao tWarnInfoDao;

    private RedisTemplate redisTemplate;


    @Logs(title = "插入", code = "cruiseResult",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseResult tCruiseResult) {
        return this.tCruiseResultDao.insert(tCruiseResult);
    }

    @Logs(title = "删除", code = "cruiseResult",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskResultId) {
        return this.tCruiseResultDao.deleteByPrimaryId(taskResultId);
    }

    @Logs(title = "更新", code = "cruiseResult",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseResult tCruiseResult) {
        return this.tCruiseResultDao.update(tCruiseResult);
    }

    @Logs(title = "主键查询", code = "cruiseResult",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectByPrimaryId(String taskResultId) {
        return this.tCruiseResultDao.selectByPrimaryId(taskResultId);
    }

    @Logs(title = "查询", code = "cruiseResult",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseResult> select(String taskResultId, String taskId, String taskName, String areaId, Integer cType, Integer cState, Integer modifyState, Integer taskCount, Integer taskWait, String checkUser, Date checkDate, String weather, Date createTime, Date executeTime, String taskCode, String remark) {
        List<TCruiseResult> tCruiseResultList = tCruiseResultDao.select(taskResultId, taskId,taskName, areaId, cType, cState, modifyState, taskCount, taskWait, checkUser, checkDate, weather, createTime, executeTime, taskCode, remark);
        return tCruiseResultList;
    }

    @Logs(title = "分页查询--巡视结果确认", code = "cruiseResult",content = "根据web传递的参数查询任务结果")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseResultExpand> selectTaskByPage(String taskName,Integer cType, Integer cState) {
        List<TCruiseResultExpand> tCruiseResultExpandList = tCruiseResultDao.selectTaskByPage(taskName,cState,cType);
        return tCruiseResultExpandList;
    }
    @Logs(title = "分页查询--任务结果详细", code = "cruiseResult",content = "根据web传递的参数查询巡检点结果")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectCruiseByPage( String taskResultId,Integer cruiseType,Integer cruiseResult,Integer deviceType,String startTime,String endTime,Long regionId,int pageNum,int pageSize) {

        List<Long> regionIdList = tStdRegionDao.selectDownId(regionId);//查询该regionId子节点
        log.info("regionIdList是==="+regionIdList);
        List<Long> deviceIdList = tStdDeviceDao.selectDeviceIdListByRegion(regionIdList);
        log.info("deviceIdList是==="+deviceIdList);

        Map<String, Object> resultMap = new HashMap<>();

        Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
        List<CruiseResultDetail> cruiseResultDetailList = new ArrayList<>();
        if (deviceIdList != null &&deviceIdList.size() > 0){
            cruiseResultDetailList = tCruiseResultDao.selectCruiseByPage(taskResultId, cruiseType, cruiseResult, deviceType, startTime, endTime, deviceIdList);
        }

        resultMap.put("count",page.getTotal());
        resultMap.put("list", cruiseResultDetailList);
        return resultMap;
    }
    @Logs(title = "人工修正", code = "cruiseResult",content = "根据web传递的参数进行人工修正")
    @Transactional(rollbackFor = Exception.class)
    public int manualReview(CruiseManualReview cruiseManualReview,String userId) throws Exception{
        //获取审核人
        Integer userID = Integer.valueOf(userId);
        String userName = tCruiseResultDao.selectUserName(userID);
        cruiseManualReview.setCheckUser(userName);
        //获取审核时间
        Date cruiseCheckDate = new Date();
        cruiseManualReview.setCheckDate(cruiseCheckDate);
        //审核
        int result1 = tCruiseResultDao.manualReview(cruiseManualReview);

        log.info("cruiseDataId是==="+cruiseManualReview.getCruiseDataId());
        Map<String,Object> judgeCondition = tCruiseResultDao.selectJudgeCondition(cruiseManualReview.getCruiseDataId());
        log.info("judgeCondition是==="+judgeCondition);
        String personCheck = judgeCondition.get("person_check").toString();
//        log.info("人工审核的测点值信息是==="+personCheck);
        int isWarn =  Integer.parseInt(judgeCondition.get("is_warn").toString());
//        log.info("查询的告警条件isWarn是==="+isWarn);
        String taskId = judgeCondition.get("task_id").toString();
//        log.info("查询的告警条件taskId是==="+taskId);
        String picPath = judgeCondition.get("picpath").toString();
//        log.info("查询的图片路径是==="+picPath);
        Long instanceId = Long.valueOf(judgeCondition.get("instance_id").toString());
//        log.info("查询的告警条件instanceId是==="+instanceId);
        Integer identifyResult = Integer.valueOf(judgeCondition.get("identify_result").toString());
//        log.info("人工审核的实际结果是==="+identifyResult);
        //查询该巡检点对应测点配置的告警阈值相关信息
        TStdDevicemete tStdDevicemete = tCruiseResultDao.selectDeviceMeteInfo(instanceId);
        log.info("tStdDevicemete==="+tStdDevicemete);

        //组装告警基本信息
        TWarnInfo warnInfo = new TWarnInfo();
        warnInfo.setWarnTime(new Date());
        if (Objects.nonNull(tStdDevicemete.getAlarmType())){
            warnInfo.setWarnType(Integer.parseInt(tStdDevicemete.getAlarmType()));
        }
        warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
        warnInfo.setCunstomId(tStdDevicemete.getCustomId());
        warnInfo.setInstanceId(instanceId);
        warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
        warnInfo.setConfMode(275);//已核查
        warnInfo.setDealType(286);//属实
        warnInfo.setDefectModel(405);//其他
        warnInfo.setAlarmSource(282);//主辅设备
        warnInfo.setImagePath(picPath);
        warnInfo.setValue(personCheck);
        warnInfo.setTaskId(taskId);
        log.info("warnInfo=="+warnInfo);

        HashMap<String,Object> params = new HashMap<>();
        params.put("value",personCheck);
        params.put("stdDeviceMeteName",tStdDevicemete.getMeteName());
        params.put("meteKind",tStdDevicemete.getMeteKind());
        params.put("alarmState",tStdDevicemete.getAlarmState());
        params.put("stateZero",tStdDevicemete.getStateZero());
        params.put("stateOne",tStdDevicemete.getStateOne());
        params.put("alarmLevel",tStdDevicemete.getAlarmLevel());
        params.put("highLimit1",tStdDevicemete.getHighLimit1());
        params.put("lowLimit1",tStdDevicemete.getLowLimit1());
        params.put("highLimit2",tStdDevicemete.getHighLimit2());
        params.put("lowLimit2",tStdDevicemete.getLowLimit2());
        params.put("highLimit3",tStdDevicemete.getHighLimit3());
        params.put("lowLimit3",tStdDevicemete.getLowLimit3());
        params.put("highLimit4",tStdDevicemete.getHighLimit4());
        params.put("lowLimit4",tStdDevicemete.getLowLimit4());
        log.info("params的值是==="+params);

        Result result = sendPostRequest(Constant.WARN_JUDGE,params);
        log.info("result==="+result);

        Map<String,Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
        log.info("object转map的东西==="+map);
        Boolean isWarN = (Boolean)map.get("isWarn");
        String outRange = null;
        if (Objects.nonNull(map.get("outRange"))){
            outRange = map.get("outRange").toString();
        }

        //判断该点是否已在告警表
        if ("".equals(judgeCondition.get("is_warn").toString()) || judgeCondition.get("is_warn").toString() != null) {
            if (isWarn == 1) {
                //存在
                //判断该点是否产生告警以及告警信息
                if (isWarN){//触发告警
                    // 修改告警信息表
                    tCruiseResultDao.updateWarnInfo(taskId, instanceId,
                            map.get("warnName").toString(),
                            Integer.valueOf(map.get("warnLevel").toString()),
                            map.get("warnContent").toString(),
                            outRange,userName,cruiseCheckDate);
                }else {
                    tCruiseResultDao.updateWarnInfo2(taskId, instanceId,userName,cruiseCheckDate);
                }
            }else {
                //不存在
                //判断该点是否产生告警以及告警信息
                if (isWarN){//触发告警
                    warnInfo.setWarnName(map.get("warnName").toString());
                    warnInfo.setWarnLevel(Integer.valueOf(map.get("warnLevel").toString()));
                    warnInfo.setWarnContent(map.get("warnContent").toString());
                    warnInfo.setOutRange(outRange);
                    warnInfo.setDealTime(cruiseCheckDate);
                    warnInfo.setDealPersonId(userName);
                    log.info("要插库的告警数据是==="+warnInfo);
                    tWarnInfoDao.insert(warnInfo);
                }
            }
        }
        /*if ( isWarn == 1){
            tCruiseResultDao.updateWarnInfo(taskId,instanceId);//更新告警表信息
            if (identifyResult == 261){//结果正确
                tCruiseResultDao.updateWarnInfo2(taskId,instanceId);//更新告警表信息
                Long warnId = tCruiseResultDao.selectWarnId(taskId,instanceId);//根据任务和巡检点id查询告警id
                //给前端推webSocket
                Map<String,Object> jasonMap=new HashMap<>();
                jasonMap.put("type","finishedOneAlarm");
                jasonMap.put("alarmId",warnId);
                String json= JSON.toJSONString(jasonMap);
                System.out.println(("发送给前端的消息==="+json));
                WebSocketServer.sendMsg(json);
            }
        }*/

        //获取审核后的信息
        String taskResultId1  = cruiseManualReview.getTaskResultId();
        List<CruiseManualReview> cruiseManualReviewList = tCruiseResultDao.selectManualDetail(taskResultId1);
        //判断是否全部审核，若都已审核，统计所有的审核人，统计最晚审核的时间，将信息插入
        HashSet<String> haS1 = new HashSet<>();
        for (CruiseManualReview cMR:cruiseManualReviewList){
            if (cMR.getEvaluationState()==257){
                break;
            }else {
                haS1.add(cMR.getCheckUser());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String checkUser:haS1){
            sb.append(checkUser + ",");
        }
        String checkUserName = sb.toString().substring(0,sb.toString().length()-1);
        String taskResultId = cruiseManualReview.getTaskResultId();
        Date taskCheckDate =findLastDate(cruiseManualReviewList);
        //更新任务审核人以及审核时间
        List<String> list = new ArrayList<>();
        for (CruiseManualReview cMR:cruiseManualReviewList) {
            if (cMR.getEvaluationState() == 256) {
                list.add(cMR.getCheckUser());
            }
        }
        int result2 = 0;
        if (list.size() == cruiseManualReviewList.size()){
            result2 = tCruiseResultDao.updateCheck(taskResultId,checkUserName,taskCheckDate);

            //自动生成巡视报告
            String taskID = cruiseManualReview.getTaskId();
            reportManageService.cruiseReportGenerate(taskID);
        }
//        insert QrDecode as device's real code. by tt.
        TCruisePointInstance tCruisePointInstance = tCruisePointInstanceDao.selectByPrimaryId(cruiseManualReview.getInstanceId());
        String analyseType = tCruiseResultDao.selectAlgorithmType(tCruisePointInstance.getDeviceMeteId());
        if (Objects.nonNull(analyseType) && Objects.equals(analyseType, "8")) {
            TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
            tStdDeviceAttr.setDeviceId(tCruisePointInstance.getDeviceId());
            tStdDeviceAttr.setRealCode(cruiseManualReview.getPersonCheck());
            tStdDeviceAttrDao.update(tStdDeviceAttr);
        }

        return result1+result2;
    }
    public Result sendPostRequest(String url, HashMap<String,Object> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class,params);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }
    public Date findLastDate(List<CruiseManualReview> list) {
        CruiseManualReview cruiseManualReview = new CruiseManualReview();
        Long dates[] = new Long[list.size()];

        for (int i = 0; i < list.size(); i++) {
            // 把date类型的时间对象转换为long类型，时间越往后，long的值就越大，
            // 所以就依靠这个原理来判断距离现在最近的时间
            Date timeTempOne = list.get(i).getCheckDate();
            if(timeTempOne!=null){
                dates[i] = timeTempOne.getTime();
            } else {
                dates[i] = Long.valueOf(0);
            }
        }
        Long maxIndex = dates[0];// 定义最大值为该数组的第一个数
        for (int j = 0; j < dates.length; j++) {
            if (maxIndex < dates[j]) {
                maxIndex = dates[j];
                cruiseManualReview = list.get(j);
            } else {
                // 找到了这个j
                cruiseManualReview = list.get(0);
            }
        }
        return cruiseManualReview.getCheckDate();
    }
    @Logs(title = "巡视任务结果统计", code = "cruiseResult",content = "根据巡视类型统计本周和上周的任务结果")
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticalResult> taskStatistical() throws Exception{

        String weekStart = tCruiseResultDao.selectOnMonday();//本周一的日期
        String weekEnd = tCruiseResultDao.selectOnSunday();//本周日的日期
        String lastWeekStart = tCruiseResultDao.selectLastMonday();//上周一的日期
        String lastWeekend = tCruiseResultDao.selectLastSunday();//上周日的日期
        log.info("weekStart=="+weekStart);
        log.info("weekEnd=="+weekEnd);
        log.info("lastWeekStart=="+lastWeekStart);
        log.info("lastWeekend=="+lastWeekend);

        String colName1 = "plan_type";

        List<StatisticalResult> taskStatisticalList = new ArrayList<>();

        StatisticalResult statisticalResult = new StatisticalResult();
        statisticalResult.setTimeNode("本周");
        statisticalResult.setStatisticalList(tCruiseResultDao.taskStatistical(colName1,weekStart,weekEnd));
        taskStatisticalList.add(statisticalResult);

        StatisticalResult statisticalResult1 = new StatisticalResult();
        statisticalResult1.setTimeNode("上周");
        statisticalResult1.setStatisticalList(tCruiseResultDao.taskStatistical(colName1,lastWeekStart,lastWeekend));
        taskStatisticalList.add(statisticalResult1);

        return taskStatisticalList;
    }
    @Logs(title = "巡视点结果统计", code = "cruiseResult",content = "根据巡视数据状态统计巡检点结果")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseStatistical> cruiseStatistical() {
        List<CruiseStatistical> list = tCruiseResultDao.cruiseStatistical();//异常
        CruiseStatistical cruiseStatistical = tCruiseResultDao.cruiseStatistical2();//正常
        list.add(cruiseStatistical);
        return list;
    }
    @Logs(title = "巡视点结果统计", code = "cruiseResult",content = "根据正常异常状态统计")
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticalTools> cruiseStatisticalByStatus() {
        List<StatisticalTools> statisticalToolsList = tCruiseResultDao.cruiseStatisticalByStatus();
        return statisticalToolsList;
    }
    @Logs(title = "巡视点结果统计", code = "cruiseResult",content = "根据异常分类统计")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseStatistical> cruiseStatisticalByAbnormal() {
        List<CruiseStatistical> tsList = tCruiseResultDao.cruiseStatisticalByAbnormal();
        return tsList;
    }
    @Logs(title = "批量插入", code = "cruiseResult")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseResult> list) {
        return this.tCruiseResultDao.batchInsert(list);
    }

    @Logs(title = "查询正在执行中的任务",code = "cruiseResult",content = "查询正在执行中的任务")
    @Transactional(rollbackFor = Exception.class)
    public List<TaskSimpleInfo> selectTaskIsRunning(){
        List<TaskSimpleInfo> novelTaskList=tCruiseResultDao.selectTaskIsRunning();
        for(TaskSimpleInfo temTask:novelTaskList){
            List<Long> counts=tCruiseResultDao.cruiseInspectCount(temTask.getTaskId());
            temTask.setDeviceMeteCount(counts.get(0));
            temTask.setCameraCount(counts.get(1));
            temTask.setRobotPointsCount(counts.get(2));

        }

        return novelTaskList ;
    }




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

