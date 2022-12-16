package com.yjh.platform.module.patrol.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.dao.TStdDeviceAttrDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
import com.yjh.platform.module.task.dao.TWarnInfoDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.ReportManageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private TRobotInspectionDao tRobotInspectionDao;

    public List<TCruiseResultExpand> selectTaskByPage(String taskName, Integer cState, Integer cType, Integer deviceType, String startTime,
        String endTime, List<Long> deviceIdList, Integer meteType, String customId, Integer isCheck) {
        List<TCruiseResultExpand> list = new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()) {
            list =
                uPatrolResultDao.selectTaskByPage(taskName, cState, cType, deviceType, startTime, endTime, deviceIdList, meteType, customId,
                    isCheck);
        }
        return list;
    }

    public List<CruiseResultDetail> selectCruiseByPage(String taskId, Integer cruiseType, Integer cruiseResult, Integer deviceType,
        String startTime, String endTime, List<Long> deviceIdList, String customId) {
        List<CruiseResultDetail> cruiseResultDetailList = new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()) {
            cruiseResultDetailList =
                uPatrolResultDao.selectCruiseByPage(taskId, cruiseType, cruiseResult, deviceType, startTime, endTime, deviceIdList,
                    customId);
        }
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
        String userName = (String)redisTemplate.opsForHash().entries("userInfo:" + userId).get("userName");
        Date date = new Date();
        cruiseManualReview.setCheckUser(userName);
        cruiseManualReview.setCheckDate(date);
        //manualReview
        int result1 = uPatrolResultDao.manualReview(cruiseManualReview);

        //更新测点信息
        Long deviceMeteId = uPatrolResultDao.selectDeviceMeteId(cruiseManualReview.getInstanceId());
        TStdDeviceMeteUpdate stdDeviceMeteUpdate =
            new TStdDeviceMeteUpdate().setDeviceMeteId(deviceMeteId).setIdentifyResult(cruiseManualReview.getIdentifyResult());
        uPatrolResultDao.updateDeviceMeteUpdate(stdDeviceMeteUpdate);

        afterManualReviewInfo(cruiseManualReview.getTaskId(), cruiseManualReview.getInstanceId(), userId, date);

        int result2 = patrolTaskReview(cruiseManualReview.getTaskId());
        //        insert QrDecode as device's real code. by tt.
        TCruisePointInstance tCruisePointInstance = tCruisePointInstanceDao.selectByPrimaryId(cruiseManualReview.getInstanceId());
        if (Objects.nonNull(tCruisePointInstance)) {
            String analyseType = uPatrolResultDao.selectAlgorithmType(tCruisePointInstance.getDeviceMeteId());
            if (Objects.nonNull(analyseType) && Objects.equals(analyseType, "8")) {
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
            log.info("自动生成巡视报告的路径是==" + reportFilePath);
        }
        return result2;
    }

    /**
     * 对审核后的任务进行处理，判断告警
     */
    private void afterManualReviewInfo(String taskId, Long instanceId, String userId, Date date) {
        //查询该巡检点审核后的相关信息
        AfterManualReviewInfo afterManualReviewInfo = uPatrolResultDao.selectJudgeCondition(instanceId, taskId);
        //查询该巡检点对应测点配置的告警阈值相关信息
        TStdDevicemete tStdDevicemete = uPatrolResultDao.selectDeviceMeteInfo(afterManualReviewInfo.getInstanceId());
        log.info("tStdDeviceMete===" + tStdDevicemete);
        //该巡视点还在,能找到对应测点信息
        if (Objects.nonNull(tStdDevicemete)) {
            Map<String, Object> params = new HashMap<>();
            String personCheck = afterManualReviewInfo.getPersonCheck().split(",")[0];
            params.put("value", personCheck);
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
            Integer warnFlag = Integer.valueOf(tWarnInfoDao.selectDictCodeByNote("其他", "defect_model"));
            warnInfo.setDefectModel(warnFlag);//其他
            warnInfo.setAlarmSource(282);//主辅设备
            warnInfo.setImagePath(afterManualReviewInfo.getPicPath());
            warnInfo.setValue(afterManualReviewInfo.getPersonCheck());
            warnInfo.setTaskId(afterManualReviewInfo.getTaskId());
            log.info("warnInfo==" + warnInfo);

            //判断该点是否已在告警表
            if (afterManualReviewInfo.getIsWarn() == 1) {
                List<Long> warnIdList =
                    uPatrolResultDao.selectWarnId(afterManualReviewInfo.getTaskId(), afterManualReviewInfo.getInstanceId());
                for (Long warnId : warnIdList) {
                    //存在
                    //判断该点是否产生告警以及告警信息
                    if (Boolean.TRUE.equals(isWarN)) {//触发告警
                        // 修改告警信息表
                        uPatrolResultDao.updateWarnInfo(warnId, map.get("warnName").toString(),
                            Integer.valueOf(map.get("warnLevel").toString()), map.get("warnContent").toString(), 286, outRange, userId,
                            date);
                        sendWebSocket(warnId);
                    } else {
                        uPatrolResultDao.updateWarnInfo(warnId, null, null, null, 287, null, userId, date);
                        sendWebSocket(warnId);
                    }
                }
            } else {
                //不存在
                //判断该点是否产生告警以及告警信息
                if (Boolean.TRUE.equals(isWarN)) {//触发告警
                    warnInfo.setWarnName(map.get("warnName").toString());
                    warnInfo.setWarnLevel(Integer.valueOf(map.get("warnLevel").toString()));
                    warnInfo.setWarnContent(map.get("warnContent").toString());
                    warnInfo.setOutRange(outRange);
                    warnInfo.setDealTime(date);
                    warnInfo.setDealPersonId(userId);
                    log.info("要插库的告警数据是===" + warnInfo);
                    tWarnInfoDao.insert(warnInfo);
                    sendWebSocket(warnInfo.getWarnId());
                    uPatrolResultDao.updateIsWarn(taskId, instanceId);
                }
            }
        }
    }

    public int manualReviewTask(String taskId, String userId, HttpServletRequest request) {
        Date date = new Date();
        String userName = (String)redisTemplate.opsForHash().entries("userInfo:" + userId).get("userName");
        //审核任务
        int result = uPatrolResultDao.updateCheck(taskId, userName, date, "1");
        //审核未被审核的巡视点
        List<UPatrolDataResult> list = uPatrolResultDao.selectCruiseDataResult(taskId);
        log.info("未被审核的点==" + list);
        for (UPatrolDataResult res : list) {
            CruiseManualReview cruiseManualReview =
                new CruiseManualReview().setCruiseDataId(res.getCruiseDataId()).setCheckUser(userName).setCheckDate(date)
                    .setPersonCheck(res.getResultNum()).setTaskId(res.getTaskId()).setInstanceId(res.getInstanceId());
            if (res.getCruiseResult() == 246) {//正常,实际:正常,算法:正确
                cruiseManualReview.setIdentifyResult(261);
                cruiseManualReview.setIdentifyState(258);
            } else {//异常,实际:数据异常,算法:错误
                cruiseManualReview.setIdentifyResult(264);
                cruiseManualReview.setIdentifyState(259);
            }
            log.info("准备更改的的东西是===" + cruiseManualReview);
            uPatrolResultDao.manualReview(cruiseManualReview);

            //查询该巡检点审核后的相关信息
            AfterManualReviewInfo afterManualReviewInfo = uPatrolResultDao.selectJudgeCondition(res.getInstanceId(), res.getTaskId());
            //若该点生成告警,则核查为属实
            if (afterManualReviewInfo.getIsWarn() == 1) {
                List<Long> warnIdList =
                    uPatrolResultDao.selectWarnId(afterManualReviewInfo.getTaskId(), afterManualReviewInfo.getInstanceId());
                for (Long warnId : warnIdList) {
                    uPatrolResultDao.updateWarnInfo(warnId, null, null, null, 286, null, userId, date);
                    sendWebSocket(warnId);
                }
            }
        }
        //自动生成巡视报告
        String reportFilePath = reportManageService.cruiseReportGenerate(taskId);
        log.info("自动生成巡视报告的路径是==" + reportFilePath);
        return result + list.size();
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
                new TStdDeviceMeteUpdate().setDeviceMeteId(deviceMeteId).setIdentifyResult(review.getIdentifyResult());
            uPatrolResultDao.updateDeviceMeteUpdate(stdDeviceMeteUpdate);

            afterManualReviewInfo(review.getTaskId(), review.getInstanceId(), review.getCheckUser(), review.getCheckDate());
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
