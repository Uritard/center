package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.task.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Author: lqh
 * @Date: 2022/10/25
 */
@Service
public class UPatrolResultService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UPatrolResultDao uPatrolResultDao;

    public List<TCruiseResultExpand> selectTaskByPage(String taskName,Integer cState, Integer cType,Integer deviceType,String startTime,String endTime,List<Long> deviceIdList,Integer meteType,String customId,Integer isCheck) {
        List<TCruiseResultExpand> list =new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()){
            list = uPatrolResultDao.selectTaskByPage(taskName,cState,cType,deviceType,startTime,endTime,deviceIdList,meteType,customId,isCheck);
        }
        return list;
    }

    public List<CruiseResultDetail>  selectCruiseByPage( String taskResultId,Integer cruiseType,Integer cruiseResult,Integer deviceType,String startTime,String endTime,List<Long> deviceIdList,String customId) {
        List<CruiseResultDetail> cruiseResultDetailList = new ArrayList<>();
        if (deviceIdList != null && !deviceIdList.isEmpty()){
            cruiseResultDetailList = uPatrolResultDao.selectCruiseByPage(taskResultId, cruiseType, cruiseResult, deviceType, startTime, endTime, deviceIdList,customId);
        }
        return cruiseResultDetailList;
    }
/*
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
        //        Long deviceMeteId = tCruiseResultDao.selectDeviceMeteId(cruiseManualReview.getCruiseDataId());
        Long deviceMeteId = uPatrolResultDao.selectDeviceMeteId(cruiseManualReview.getInstanceId());
        TStdDeviceMeteUpdate stdDeviceMeteUpdate = new TStdDeviceMeteUpdate()
            .setDeviceMeteId(deviceMeteId)
            .setIdentifyResult(cruiseManualReview.getIdentifyResult());
        tCruiseResultDao.updateDeviceMeteUpdate(stdDeviceMeteUpdate);

        //查询该巡检点审核后的相关信息
        //        AfterManualReviewInfo afterManualReviewInfo = tCruiseResultDao.selectJudgeCondition(cruiseManualReview.getCruiseDataId());
        AfterManualReviewInfo
            afterManualReviewInfo = uPatrolResultDao.selectJudgeCondition(cruiseManualReview.getInstanceId(),cruiseManualReview.getTaskId());
        //查询该巡检点对应测点配置的告警阈值相关信息
        TStdDevicemete tStdDevicemete = tCruiseResultDao.selectDeviceMeteInfo(afterManualReviewInfo.getInstanceId());
        log.info("tStdDeviceMete==="+tStdDevicemete);
        //该巡视点还在,能找到对应测点信息
        if (Objects.nonNull(tStdDevicemete)){
            Map<String,Object> params = new HashMap<>();
            String personCheck = afterManualReviewInfo.getPersonCheck().split(",")[0];
            params.put("value", personCheck);
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
            Boolean isWarN = false;
            String outRange = null;
            if(Objects.nonNull(map)){
                isWarN = (Boolean)map.get("isWarn");
                if (Objects.nonNull(map.get("outRange"))){
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
            Integer warnFlag = Integer.valueOf(tWarnInfoDao.selectDictCodeByNote("其他","defect_model"));
            warnInfo.setDefectModel(warnFlag);//其他
            warnInfo.setAlarmSource(282);//主辅设备
            warnInfo.setImagePath(afterManualReviewInfo.getPicPath());
            warnInfo.setValue(afterManualReviewInfo.getPersonCheck());
            warnInfo.setTaskId(afterManualReviewInfo.getTaskId());
            log.info("warnInfo=="+warnInfo);

            //判断该点是否已在告警表
            if (afterManualReviewInfo.getIsWarn() == 1) {
                List<Long> warnIdList = tCruiseResultDao.selectWarnId(afterManualReviewInfo.getTaskId(),afterManualReviewInfo.getInstanceId());
                for(Long warnId : warnIdList){
                    //存在
                    //判断该点是否产生告警以及告警信息
                    if (Boolean.TRUE.equals(isWarN)){//触发告警
                        // 修改告警信息表
                        tCruiseResultDao.updateWarnInfo(warnId,
                            map.get("warnName").toString(),
                            Integer.valueOf(map.get("warnLevel").toString()),
                            map.get("warnContent").toString(),
                            outRange,userId,date);
                        sendWebSocket(warnId);
                    }else {
                        tCruiseResultDao.updateWarnInfo2(warnId,userId,date);
                        sendWebSocket(warnId);
                    }
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
        if (Objects.nonNull(tCruisePointInstance)){
            String analyseType = tCruiseResultDao.selectAlgorithmType(tCruisePointInstance.getDeviceMeteId());
            if (Objects.nonNull(analyseType) && Objects.equals(analyseType, "8")) {
                TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
                tStdDeviceAttr.setDeviceId(tCruisePointInstance.getDeviceId());
                tStdDeviceAttr.setRealCode(cruiseManualReview.getPersonCheck());
                tStdDeviceAttrDao.update(tStdDeviceAttr);
            }
        }
        return result1+result2;
    }*/
}
