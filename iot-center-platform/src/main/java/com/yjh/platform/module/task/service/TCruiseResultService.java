package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;

import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceAttrDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
import com.yjh.platform.module.task.dao.TWarnInfoDao;
import com.yjh.platform.module.task.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
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
    private TWarnInfoDao tWarnInfoDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseResult tCruiseResult) {
        return this.tCruiseResultDao.insert(tCruiseResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskResultId) {
        return this.tCruiseResultDao.deleteByPrimaryId(taskResultId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseResult tCruiseResult) {
        return this.tCruiseResultDao.update(tCruiseResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectByPrimaryId(String taskResultId) {
        return this.tCruiseResultDao.selectByPrimaryId(taskResultId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseResult> select(String taskResultId, String taskId, String taskName, String areaId, Integer cType, Integer cState, Integer modifyState, Integer taskCount, Integer taskWait, String checkUser, Date checkDate, String weather, Date createTime, Date executeTime, String taskCode, String remark) {
        return tCruiseResultDao.select(taskResultId, taskId,taskName, areaId, cType, cState, modifyState, taskCount, taskWait, checkUser, checkDate, weather, createTime, executeTime, taskCode, remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseResultExpand> selectTaskByPage(String taskName,Integer cState, Integer cType,Integer deviceType,String startTime,String endTime,List<Long> deviceIdList,Integer meteType) {
        List<TCruiseResultExpand> list =new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()){
            list = tCruiseResultDao.selectTaskByPage(taskName,cState,cType,deviceType,startTime,endTime,deviceIdList,meteType);
        }
        return list;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultDetail>  selectCruiseByPage( String taskResultId,Integer cruiseType,Integer cruiseResult,Integer deviceType,String startTime,String endTime,List<Long> deviceIdList) {
        List<CruiseResultDetail> cruiseResultDetailList = new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()){
            cruiseResultDetailList = tCruiseResultDao.selectCruiseByPage(taskResultId, cruiseType, cruiseResult, deviceType, startTime, endTime, deviceIdList);
        }
        return cruiseResultDetailList;
    }
    @Transactional(rollbackFor = Exception.class)
    public int manualReview(CruiseManualReview cruiseManualReview,String userId){
        //checkUser && checkDate
        String userName = tCruiseResultDao.selectUserName(Integer.valueOf(userId));
        Date date = new Date();
        cruiseManualReview.setCheckUser(userName);
        cruiseManualReview.setCheckDate(date);
        //manualReview
        int result1 = tCruiseResultDao.manualReview(cruiseManualReview);

        //更新测点信息
        Long deviceMeteId = tCruiseResultDao.selectDeviceMeteId(cruiseManualReview.getCruiseDataId());
        TStdDeviceMeteUpdate stdDeviceMeteUpdate = new TStdDeviceMeteUpdate()
                .setDeviceMeteId(deviceMeteId)
                .setIdentifyResult(cruiseManualReview.getIdentifyResult());
        tCruiseResultDao.updateDeviceMeteUpdate(stdDeviceMeteUpdate);

        //查询该巡检点审核后的相关信息
        AfterManualReviewInfo afterManualReviewInfo = tCruiseResultDao.selectJudgeCondition(cruiseManualReview.getCruiseDataId());
        //查询该巡检点对应测点配置的告警阈值相关信息
        TStdDevicemete tStdDevicemete = tCruiseResultDao.selectDeviceMeteInfo(afterManualReviewInfo.getInstanceId());
        log.info("tStdDeviceMete==="+tStdDevicemete);
        //该巡视点还在,能找到对应测点信息
        if (Objects.nonNull(tStdDevicemete)){
            Map<String,Object> params = new HashMap<>();
            params.put("value",afterManualReviewInfo.getPersonCheck());
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
            Map<String,Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
            log.info("object转map的东西==="+map);
            Boolean isWarN = (Boolean)map.get("isWarn");
            String outRange = null;
            if (Objects.nonNull(map.get("outRange"))){
                outRange = map.get("outRange").toString();
            }

            //组装告警基本信息
            TWarnInfo warnInfo = new TWarnInfo();
            warnInfo.setWarnTime(date);
//        warnInfo.setWarnType(Integer.valueOf(tStdDevicemete.getAlarmNote()));
            warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
            warnInfo.setCunstomId(tStdDevicemete.getCustomId());
            warnInfo.setInstanceId(afterManualReviewInfo.getInstanceId());
            warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
            warnInfo.setConfMode(275);//已核查
            warnInfo.setDealType(286);//属实
            warnInfo.setDealInfo("程序正常，告警属实");
            Integer warnFlag = Integer.valueOf(tWarnInfoDao.selectDictCodeByNote("其他","defect_model"));
            warnInfo.setDefectModel(warnFlag);//其他
            warnInfo.setAlarmSource(282);//主辅设备
            warnInfo.setImagePath(afterManualReviewInfo.getPicPath());
            warnInfo.setValue(afterManualReviewInfo.getPersonCheck());
            warnInfo.setTaskId(afterManualReviewInfo.getTaskId());
            log.info("warnInfo=="+warnInfo);

            //判断该点是否已在告警表
            if (afterManualReviewInfo.getIsWarn() == 1) {
                Long warnId = tCruiseResultDao.selectWarnId(afterManualReviewInfo.getTaskId(),afterManualReviewInfo.getInstanceId());
                //存在
                //判断该点是否产生告警以及告警信息
                if (Boolean.TRUE.equals(isWarN)){//触发告警
                    // 修改告警信息表
                    tCruiseResultDao.updateWarnInfo(afterManualReviewInfo.getTaskId(), afterManualReviewInfo.getInstanceId(),
                            map.get("warnName").toString(),
                            Integer.valueOf(map.get("warnLevel").toString()),
                            map.get("warnContent").toString(),
                            outRange,userId,date);
                    sendWebSocket(warnId);
                }else {
                    tCruiseResultDao.updateWarnInfo2(afterManualReviewInfo.getTaskId(), afterManualReviewInfo.getInstanceId(),
                            userId,date);
                    sendWebSocket(warnId);
                }
            }else {
                //不存在
                //判断该点是否产生告警以及告警信息
                if (Boolean.TRUE.equals(isWarN)){//触发告警
                    warnInfo.setWarnName(map.get("warnName").toString());
                    warnInfo.setWarnLevel(Integer.valueOf(map.get("warnLevel").toString()));
                    warnInfo.setWarnContent(map.get("warnContent").toString());
                    warnInfo.setOutRange(outRange);
                    warnInfo.setDealTime(date);
                    warnInfo.setDealPersonId(userId);
                    log.info("要插库的告警数据是==="+warnInfo);
                    tWarnInfoDao.insert(warnInfo);
                    sendWebSocket(warnInfo.getWarnId());
                    tCruiseResultDao.updateIsWarn(cruiseManualReview.getCruiseDataId());
                }
            }
        }

        //获取审核后该任务下的巡检点审核信息
        List<CruiseManualReview> cruiseManualReviewList = tCruiseResultDao.selectManualDetail(cruiseManualReview.getTaskResultId());
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
        String taskId = cruiseManualReview.getTaskId();
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
            result2 = tCruiseResultDao.updateCheck(taskId,checkUserName,taskCheckDate,"1");
            //自动生成巡视报告
            String taskID = cruiseManualReview.getTaskId();
            String reportFilePath = reportManageService.cruiseReportGenerate(taskID);
            log.info("自动生成巡视报告的路径是=="+reportFilePath);
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
    public Result sendPostRequest(String url, Map<String,Object> params) {
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
        Long datesArray[] = new Long[list.size()];

        for (int i = 0; i < list.size(); i++) {
            // 把date类型的时间对象转换为long类型，时间越往后，long的值就越大，
            // 所以就依靠这个原理来判断距离现在最近的时间
            Date timeTempOne = list.get(i).getCheckDate();
            if(timeTempOne!=null){
                datesArray[i] = timeTempOne.getTime();
            } else {
                datesArray[i] = Long.valueOf(0);
            }
        }
        Long maxIndex = datesArray[0];// 定义最大值为该数组的第一个数
        for (int j = 0; j < datesArray.length; j++) {
            if (maxIndex < datesArray[j]) {
                maxIndex = datesArray[j];
                cruiseManualReview = list.get(j);
            } else {
                // 找到了这个j
                cruiseManualReview = list.get(0);
            }
        }
        return cruiseManualReview.getCheckDate();
    }
    public void sendWebSocket(Long warnId){
        //给前端推webSocket
        Map<String,String> jasonMap=new HashMap<>();
        jasonMap.put("type","finishedOneAlarm");
        jasonMap.put("alarmId", warnId.toString());
        String json= JSON.toJSONString(jasonMap);
        System.out.println(("发送给前端的消息==="+json));
        try{
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
        }catch (Exception e){
            System.out.println("发送websocket出错");
        }

    }
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticalResult> taskStatistical(){

        String weekStart = tCruiseResultDao.selectOnMonday() + " 00:00:00";//本周一的日期
        String weekEnd = tCruiseResultDao.selectOnSunday() + " 23:59:59";//本周日的日期
        String lastWeekStart = tCruiseResultDao.selectLastMonday() + " 00:00:00";//上周一的日期
        String lastWeekend = tCruiseResultDao.selectLastSunday()+ " 23:59:59";//上周日的日期

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
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseStatistical> cruiseStatistical() {
        List<CruiseStatistical> list = tCruiseResultDao.cruiseStatistical();//异常
        CruiseStatistical cruiseStatistical = tCruiseResultDao.cruiseStatistical2();//正常
        list.add(cruiseStatistical);
        return list;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticalTools> cruiseStatisticalByStatus() {
        return tCruiseResultDao.cruiseStatisticalByStatus();
    }
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseStatistical> cruiseStatisticalByAbnormal() {
        return tCruiseResultDao.cruiseStatisticalByAbnormal();
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseResult> list) {
        return this.tCruiseResultDao.batchInsert(list);
    }

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
    public int manualReviewTask(String taskId, String userId, HttpServletRequest request){
        Date date = new Date();
        String userName = tCruiseResultDao.selectUserName(Integer.valueOf(userId));
        //审核任务
        int result = tCruiseResultDao.updateCheck(taskId,userName,date,"1");
        //审核未被审核的巡视点
        List<TCruiseDataResult> list = tCruiseResultDao.selectCruiseDataResult(taskId);
        log.info("未被审核的点=="+list);
        for (TCruiseDataResult res : list){
            CruiseManualReview cruiseManualReview = new CruiseManualReview()
                    .setCruiseDataId(res.getCruiseDataId())
                    .setCheckUser(userName)
                    .setCheckDate(date)
                    .setPersonCheck(res.getResultNum());
            if (res.getCruiseResult() == 246){//正常,实际:正常,算法:正确
                cruiseManualReview.setIdentifyResult(261);
                cruiseManualReview.setIdentifyState(258);
            } else {//异常,实际:数据异常,算法:错误
                cruiseManualReview.setIdentifyResult(264);
                cruiseManualReview.setIdentifyState(259);
            }
            log.info("准备更改的的东西是==="+cruiseManualReview);
            tCruiseResultDao.manualReview(cruiseManualReview);

            //查询该巡检点审核后的相关信息
            AfterManualReviewInfo afterManualReviewInfo = tCruiseResultDao.selectJudgeCondition(res.getCruiseDataId());
            //若该点生成告警,则核查为属实
            if (afterManualReviewInfo.getIsWarn() == 1) {
                Long warnId = tCruiseResultDao.selectWarnId(afterManualReviewInfo.getTaskId(), afterManualReviewInfo.getInstanceId());
                tCruiseResultDao.updateWarnInfo3(warnId,userId,date);
                sendWebSocket(warnId);
            }
        }
        //自动生成巡视报告
        String reportFilePath = reportManageService.cruiseReportGenerate(taskId);
        log.info("自动生成巡视报告的路径是=="+reportFilePath);
        return result + list.size();
    }
}

