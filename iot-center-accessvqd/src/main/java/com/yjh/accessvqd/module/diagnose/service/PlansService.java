package com.yjh.accessvqd.module.diagnose.service;

import com.yjh.accessvqd.commons.logs.Logs;
import com.yjh.accessvqd.commons.result.ResultCodeEnum;
import com.yjh.accessvqd.commons.utils.http.HttpClientUtils;
import com.yjh.accessvqd.commons.utils.xmlAnalyse.PlansXML;
import com.yjh.accessvqd.commons.utils.xmlAnalyse.ResponseXML;
import com.yjh.accessvqd.module.diagnose.dao.TDiagnosePlanAttrDao;
import com.yjh.accessvqd.module.diagnose.dao.TDiagnosePlanDao;
import com.yjh.accessvqd.module.diagnose.dao.TDiagnosePlanDetailDao;
import com.yjh.accessvqd.module.diagnose.entity.*;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

/**
 * @author czh
 * @since 2021-01-14
 */
@Service
public class PlansService {

    private Logger log = LoggerFactory.getLogger(PlansService.class);

    @Value("http://192.168.33.241:800/PSIA/Custom/SelfExt/AS/VQDDiagnose/plans")
    private String diagnosePlan_URl;

    @Autowired
    private TDiagnosePlanDao tDiagnosePlanDao;

    @Autowired
    private TDiagnosePlanDetailDao tDiagnosePlanDetailDao;

    @Autowired
    private TDiagnosePlanAttrDao tDiagnosePlanAttrDao;

    @Autowired
    private RedisTemplate redisTemplate;


    @Logs(title = "诊断计划列表查询")
    @Transactional(rollbackFor = Exception.class)
    public List<Plans> getPlanList(String planName) {
        List<Plans> plans = new ArrayList<>();//调用api查询的周期任务list
        List<Plans> plansList = new ArrayList<>();//任务基本信息库中的任务
        List<String> cyclePlanId = new ArrayList<>();//存放周期任务ID
        List<Plans> finalResults = new ArrayList<>();//存放最终的任务结果
        try {
            plansList = tDiagnosePlanDao.selectByPage(planName);
            for (Plans origin : plansList) {
                if (origin.getPlanType().contains("立即")) {
                    origin.setPeriod("-1");
                    TDiagnosePlanDetail tDiagnosePlanDetail = tDiagnosePlanDetailDao.selectByPrimaryId(origin.getDiagnosePlanId());
                    List<TDiagnosePlanAttr> tDiagnosePlanAttrs = tDiagnosePlanAttrDao.selectByPrimaryId(origin.getDiagnosePlanId());
                    // 完善立即任务信息
                    origin.setMono(tDiagnosePlanDetail.getMonoOpt());
                    origin.setPtz(tDiagnosePlanDetail.getPtzOpt());
                    origin.setCover(tDiagnosePlanDetail.getCoverOpt());
                    origin.setScene(tDiagnosePlanDetail.getSceneOpt());
                    origin.setFlash(tDiagnosePlanDetail.getFlashOpt());
                    origin.setShake(tDiagnosePlanDetail.getShakeOpt());
                    origin.setFreeze(tDiagnosePlanDetail.getFreezeOpt());
                    origin.setStreak(tDiagnosePlanDetail.getStreakOpt());
                    origin.setNoise(tDiagnosePlanDetail.getNoiseOpt());
                    origin.setChroma(tDiagnosePlanDetail.getChromaOpt());
                    origin.setDark(tDiagnosePlanDetail.getDarkOpt());
                    origin.setBright(tDiagnosePlanDetail.getBrightOpt());
                    origin.setContrast(tDiagnosePlanDetail.getContrastOpt());
                    origin.setBlur(tDiagnosePlanDetail.getBlurOpt());
                    origin.setSignal(tDiagnosePlanDetail.getSignalOpt());
                    List<String> taskList = new ArrayList<>();
                    for (TDiagnosePlanAttr taskId : tDiagnosePlanAttrs) {
                        taskList.add(taskId.getChannelId());
                    }
                    origin.setTaskList(taskList);
                    finalResults.add(origin);//立即任务完整信息放入结果集
                } else if (origin.getPlanType().contains("周期")) {
                    cyclePlanId.add(origin.getDiagnosePlanId());//获取数据库中周期任务ID
                }
            }
            String plan = HttpClientUtils.getInstance().getUrl(diagnosePlan_URl, null);
            plans = PlansXML.unPackingXML(plan);//调用API获取周期任务详细信息
            for (Plans cyclePlan : plans) {//匹配并筛组合（基本信息与详细信息匹配）选根据条件查询出的周期任务
                for (String id : cyclePlanId) {
                    if (cyclePlan.getDiagnosePlanId().equals(id)) {
                        Plans temPlan = tDiagnosePlanDao.selectByPrimaryId(id);
                        cyclePlan.setPlanType(temPlan.getPlanType());
                        cyclePlan.setPlanName(temPlan.getPlanName());
                        cyclePlan.setUserId(temPlan.getUserId());
                        cyclePlan.setUserName(temPlan.getUserName());
                        cyclePlan.setStartTime(temPlan.getStartTime());
                        cyclePlan.setEndTime(temPlan.getEndTime());
                        List<String> weeks = new ArrayList<>();
                        if (Objects.nonNull(cyclePlan.getStartTime1()))
                            weeks.add("mon");
                        if (Objects.nonNull(cyclePlan.getStartTime2()))
                            weeks.add("tues");
                        if (Objects.nonNull(cyclePlan.getStartTime3()))
                            weeks.add("wed");
                        if (Objects.nonNull(cyclePlan.getStartTime4()))
                            weeks.add("thur");
                        if (Objects.nonNull(cyclePlan.getStartTime5()))
                            weeks.add("fri");
                        if (Objects.nonNull(cyclePlan.getStartTime6()))
                            weeks.add("sat");
                        if (Objects.nonNull(cyclePlan.getStartTime7()))
                            weeks.add("sun");
                        cyclePlan.setWeeks(weeks);
                        finalResults.add(cyclePlan);//周期任务完整信息放入结果集
                    }
                }
            }
        } catch (Exception e) {
            log.error("计划查询失败" + e);
        }
        return finalResults;
    }


    @Logs(title = "主键ID查询任务")
    @Transactional(rollbackFor = Exception.class)
    public Plans selectByPlanId(String diagnosePlanId) {

        Plans plans = tDiagnosePlanDao.selectByPlanId(diagnosePlanId);
        if (plans.getPlanType().contains("立即")) {
            plans.setPeriod("-1");

            TDiagnosePlanDetail tDiagnosePlanDetail = tDiagnosePlanDetailDao.selectByPrimaryId(plans.getDiagnosePlanId());
            List<TDiagnosePlanAttr> tDiagnosePlanAttrs = tDiagnosePlanAttrDao.selectByPrimaryId(plans.getDiagnosePlanId());
            // 完善立即任务信息
            plans.setMono(tDiagnosePlanDetail.getMonoOpt());
            plans.setPtz(tDiagnosePlanDetail.getPtzOpt());
            plans.setCover(tDiagnosePlanDetail.getCoverOpt());
            plans.setScene(tDiagnosePlanDetail.getSceneOpt());
            plans.setFlash(tDiagnosePlanDetail.getFlashOpt());
            plans.setShake(tDiagnosePlanDetail.getShakeOpt());
            plans.setFreeze(tDiagnosePlanDetail.getFreezeOpt());
            plans.setStreak(tDiagnosePlanDetail.getStreakOpt());
            plans.setNoise(tDiagnosePlanDetail.getNoiseOpt());
            plans.setChroma(tDiagnosePlanDetail.getChromaOpt());
            plans.setDark(tDiagnosePlanDetail.getDarkOpt());
            plans.setBright(tDiagnosePlanDetail.getBrightOpt());
            plans.setContrast(tDiagnosePlanDetail.getContrastOpt());
            plans.setBlur(tDiagnosePlanDetail.getBlurOpt());
            plans.setSignal(tDiagnosePlanDetail.getSignalOpt());
            List<String> taskList = new ArrayList<>();
            for (TDiagnosePlanAttr taskId : tDiagnosePlanAttrs) {
                taskList.add(taskId.getChannelId());
            }
            plans.setTaskList(taskList);
        } else {
            try {
                String plan = HttpClientUtils.getInstance().getUrl(diagnosePlan_URl + "/" + diagnosePlanId, null);
                plans = PlansXML.unpackingXMLByPlanId(plan);//调用API获取周期任务详细信息

                Plans temPlan = tDiagnosePlanDao.selectByPrimaryId(diagnosePlanId);//周期任务中转容器
                plans.setPlanType(temPlan.getPlanType());
                plans.setPlanName(temPlan.getPlanName());
                plans.setUserId(temPlan.getUserId());
                plans.setUserName(temPlan.getUserName());
                plans.setStartTime(temPlan.getStartTime());
                plans.setEndTime(temPlan.getEndTime());
                List<String> weeks = new ArrayList<>();
                if (Objects.nonNull(plans.getStartTime1()))
                    weeks.add("mon");
                if (Objects.nonNull(plans.getStartTime2()))
                    weeks.add("tues");
                if (Objects.nonNull(plans.getStartTime3()))
                    weeks.add("wed");
                if (Objects.nonNull(plans.getStartTime4()))
                    weeks.add("thur");
                if (Objects.nonNull(plans.getStartTime5()))
                    weeks.add("fri");
                if (Objects.nonNull(plans.getStartTime6()))
                    weeks.add("sat");
                if (Objects.nonNull(plans.getStartTime7()))
                    weeks.add("sun");
                plans.setWeeks(weeks);

            } catch (Exception e) {
                log.error("周期任务查询失败：" + e);
            }

        }

        return plans;
    }

    @Logs(title = "诊断任务新增与修改")
    @Transactional(rollbackFor = Exception.class)
    public String diagnosePlanUpAdd(Plans plans) {
        String status = "finish";
        try {
            //新增信息标志
            boolean addFlag = Objects.isNull(tDiagnosePlanDao.selectByPrimaryId(plans.getDiagnosePlanId()));


            if (addFlag == true) { //新增
                plans.setDiagnosePlanId(String.valueOf(UUID.randomUUID()).replace("-", ""));
                List<TDiagnosePlanAttr> taskList = new ArrayList<>();//立即任务-taskList对象
                TDiagnosePlanDetail tDiagnosePlanDetail = new TDiagnosePlanDetail();//立即任务-详细检测项对象
                List<String> channelIds = new ArrayList<>();//本次任务下的监测点ID
                switch (plans.getPeriod()) {
                    case "-1":
                        plans.setPlanType("立即任务");


                        for (String taskId : plans.getTaskList()) { //立即任务监测点ID封装
                            TDiagnosePlanAttr tDiagnosePlanAttr = new TDiagnosePlanAttr();
                            tDiagnosePlanAttr.setDiagnosePlanId(plans.getDiagnosePlanId());
                            tDiagnosePlanAttr.setChannelId(taskId);
                            taskList.add(tDiagnosePlanAttr);


                            tDiagnosePlanDetail.setDiagnosePlanId(plans.getDiagnosePlanId());
                            tDiagnosePlanDetail.setBlurOpt(plans.getBlur());
                            tDiagnosePlanDetail.setBrightOpt(plans.getBright());
                            tDiagnosePlanDetail.setChromaOpt(plans.getChroma());
                            tDiagnosePlanDetail.setContrastOpt(plans.getContrast());
                            tDiagnosePlanDetail.setCoverOpt(plans.getCover());
                            tDiagnosePlanDetail.setDarkOpt(plans.getDark());
                            tDiagnosePlanDetail.setFlashOpt(plans.getFlash());
                            tDiagnosePlanDetail.setFreezeOpt(plans.getFreeze());
                            tDiagnosePlanDetail.setMonoOpt(plans.getMono());
                            tDiagnosePlanDetail.setNoiseOpt(plans.getNoise());
                            tDiagnosePlanDetail.setPtzOpt(plans.getPtz());
                            tDiagnosePlanDetail.setSceneOpt(plans.getScene());
                            tDiagnosePlanDetail.setShakeOpt(plans.getShake());
                            tDiagnosePlanDetail.setSignalOpt(plans.getSignal());
                            tDiagnosePlanDetail.setStreakOpt(plans.getStreak());
                        }
                        break;
                    case "0":
                        plans.setPlanType("周期任务");
                        break;
                }
                plans.setStartTime(new Date());
                String result = HttpClientUtils.getInstance().putUrl(diagnosePlan_URl + "/" + plans.getDiagnosePlanId(), PlansXML.generatePlansXMl(plans));
                status = ResponseXML.unPackingXMl(result);
                if (status.equals("成功")) {
                    channelIds = plans.getTaskList();
                    redisTemplate.opsForList().rightPushAll("diagnosePlan:" + plans.getDiagnosePlanId(), channelIds);
                    tDiagnosePlanDao.insert(plans);//插入所有任务的基本信息
                    if (plans.getPeriod().equals("-1")) {
                        tDiagnosePlanDetailDao.insert(tDiagnosePlanDetail);//插入立即任务的详细信息
                        log.info("taskList:-------------" + taskList);
                        tDiagnosePlanAttrDao.batchInsert(taskList);//插入立即任务的监测点ID
                    }
                }
            } else {   //修改
                switch (plans.getPeriod()) {
                    case "-1":
                        break;
                    case "0":
                        String result = HttpClientUtils.getInstance().putUrl(diagnosePlan_URl + "/" + plans.getDiagnosePlanId(), PlansXML.generatePlansXMl(plans));
                        status = ResponseXML.unPackingXMl(result);
                        break;
                }
                if (status.equals("成功")) {
                    plans.setStartTime(new Date());
                    tDiagnosePlanDao.update(plans);
                }

            }
//            String result=HttpClientUtils.getInstance().putUrl(diagnosePlan_URl+"/"+plans.getDiagnosePlanId(),PlansXML.generatePlansXMl(plans));
//            status=ResponseXML.unPackingXMl(result);
//            log.info("planInfo------------------------:"+plans);
//            if(status.equals("成功")){
//                if(addFlag==true){
//                    tDiagnosePlanDao.insert(plans);//插入所有任务的基本信息
//                    tDiagnosePlanDetailDao.insert(tDiagnosePlanDetail);//插入立即任务的详细信息
//                    log.info("taskList:-------------"+taskList);
//                    tDiagnosePlanAttrDao.batchInsert(taskList);//插入立即任务的监测点ID
//                }else {
//                    tDiagnosePlanDao.update(plans);
//                }
//            }
        } catch (Exception e) {
            status="failed";
            log.error("计划任务新增或修改失败" + e);
        }
        return status;
    }

    @Logs(title = "诊断任务删除")
    @Transactional(rollbackFor = Exception.class)
    public String diagnosePlanDelete(List<String> planIds) {
        String status = null;
        try {
            for (String planId : planIds) {
                if (tDiagnosePlanAttrDao.selectByPrimaryId(planId).size() != 0) {//立即任务
                    tDiagnosePlanDao.deleteByPrimaryId(planId);
                    tDiagnosePlanDetailDao.deleteByPrimaryId(planId);
                    tDiagnosePlanAttrDao.deleteByPrimaryId(planId);
                    status = "success-atOnce";
                } else {
                    String result = HttpClientUtils.getInstance().deleteUrl(diagnosePlan_URl + "/" + planId, null);
                    status = ResponseXML.unPackingXMl(result);
                    if (status.equals("正常")) {
                        tDiagnosePlanDao.deleteByPrimaryId(planId);
                        tDiagnosePlanDetailDao.deleteByPrimaryId(planId);
                        tDiagnosePlanAttrDao.deleteByPrimaryId(planId);
                        status = "success-cycle";
                    }
                }
            }
        } catch (Exception e) {
            log.error("删除失败" + e);
        }
        return status;
    }

    @Logs(title = "查询近一次任务")
    @Transactional(rollbackFor = Exception.class)
    public PlanInfo selectTheLastPlan() {
        return tDiagnosePlanDao.selectLastPlan();
    }


    @Logs(title = "查询NVR-监测点树",code ="tCameraInfo")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectNVRChannelTree(String diagnosePlanId) {
        Map<String,Object>finalResult=new HashMap<>();
        List<String>checked=new ArrayList<>();
        List<NVRChannelTree> roots=tDiagnosePlanDao.selectNVRNode();
        for(NVRChannelTree root:roots){
            root.setUpId(Long.valueOf("-1"));
            root.setLevel("1");
            root.setChildren(tDiagnosePlanDao.selectChannelNode(root.getId()));
        }
        if(tDiagnosePlanAttrDao.selectByPrimaryId(diagnosePlanId).size() !=0){
            for(TDiagnosePlanAttr attr:tDiagnosePlanAttrDao.selectByPrimaryId(diagnosePlanId)){
               checked.add(attr.getChannelId());
            }
        }else {
            try {
                String xmlIds=HttpClientUtils.getInstance().getUrl(diagnosePlan_URl+"/"+diagnosePlanId+"/"+"TaskList",null);
//                String xmlIds=HttpClientUtils.getInstance().getUrl(diagnosePlan_URl+"/"+diagnosePlanId,null);
                log.info("result---------:"+PlansXML.unpackingXMLByPlanId(xmlIds));
                checked=PlansXML.unpackingXmlTaskList(xmlIds);
            }catch (IOException | DocumentException i){
                log.error("查询错误："+i);
            }

        }
        finalResult.put("checked",checked);
        finalResult.put("tree",roots);
        return finalResult;

    }


}
