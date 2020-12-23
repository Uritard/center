package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.service.TCfgDeviceService;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.task.controller.TCruiseTaskController;
import com.yjh.platform.module.task.dao.TCfgDataCurrentDao;
import com.yjh.platform.module.task.dao.TCfgUnionRuleDao;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.dao.TUnionTaskDao;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author czh
 * @since 2020-08-25
 */
@Service
public class TCfgDataCurrentService {

    private Logger log = LoggerFactory.getLogger(TCfgDataCurrentService.class);

    @Autowired
    private TCfgDataCurrentDao tCfgDataCurrentDao;

    @Autowired
    private TCfgUnionRuleDao tCfgUnionRuleDao;

    @Autowired
    private TUnionTaskService tUnionTaskService;

    @Autowired
    private TUnionTaskDao tUnionTaskDao;

    @Autowired
    private TCruisePlanDao tCruisePlanDao;

    @Autowired
    private TCruiseTaskService tCruiseTaskService;

    @Autowired
    private TCruiseTaskController tCruiseTaskController;

    @Autowired
    private TStdDeviceService tStdDeviceService;

    @Autowired
    private TCfgDeviceService tCfgDeviceService;

    public static String meteValues(String commintValue){
        if("返回".equals(commintValue)){
            return "1";
        }
        if("启动".equals(commintValue)){
            return "0";
        }
        if("合".equals(commintValue)){
            return "1";
        }
        if("分".equals(commintValue)){
            return "0";
        }
        if("降".equals(commintValue)){
            return "1";
        }
        if("升".equals(commintValue)){
            return "0";
        }
        if("停".equals(commintValue)){
            return "2";
        }
        if("投入".equals(commintValue)){
            return "1";
        }
        if("退出".equals(commintValue)){
            return "0";
        }
        if("控合".equals(commintValue)){
            return "1";
        }
        if("控分".equals(commintValue)){
            return "0";
        }
        if("未储能".equals(commintValue)){
            return "1";
        }
        if("已储能".equals(commintValue)){
            return "0";
        }
        if("联锁".equals(commintValue)){
            return "1";
        }
        if("解锁".equals(commintValue)){
            return "0";
        }
        if("成功".equals(commintValue)){
            return "1";
        }
        if("失败".equals(commintValue)){
            return "0";
        }
        if("远方".equals(commintValue)){
            return "1";
        }
        if("本地".equals(commintValue)){
            return "0";
        }
        if("合上".equals(commintValue)){
            return "1";
        }
        if("断开".equals(commintValue)){
            return "0";
        }
        if("中断".equals(commintValue)){
            return "1";
        }
        if("恢复".equals(commintValue)){
            return "0";
        }
        if("复归".equals(commintValue)){
            return "2";
        }
        if("上限".equals(commintValue)){
            return "1";
        }
        if("下限".equals(commintValue)){
            return "0";
        }
        if("触发".equals(commintValue)){
            return "2";
        }
        return null;
    }

    @Logs(title = "插入", code = "TCfgDataCurrent",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgDataCurrent tCfgDataCurrent) {
        return this.tCfgDataCurrentDao.insert(tCfgDataCurrent);
    }

    @Logs(title = "删除", code = "TCfgDataCurrent",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long meteId) {
        return this.tCfgDataCurrentDao.deleteByPrimaryId(meteId);
    }

    @Logs(title = "更新", code = "TCfgDataCurrent",content = "根据web传入的参数更新数据")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgDataCurrent tCfgDataCurrent) {
        return this.tCfgDataCurrentDao.update(tCfgDataCurrent);
    }

    @Logs(title = "主键查询", code = "TCfgDataCurrent",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public TCfgDataCurrent selectByPrimaryId(Long meteId) {
        return this.tCfgDataCurrentDao.selectByPrimaryId(meteId);
    }

    @Logs(title = "查询", code = "TCfgDataCurrent",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDataCurrent> select(Long meteId, Long deviceId, String cunstomId, Date recordTime, Integer meteKind, String regionId, String meteValue, String lastMeteValue) throws ScriptException {
        List<TCfgDataCurrent> tCfgDataCurrentList = tCfgDataCurrentDao.select(meteId, deviceId, cunstomId, recordTime, meteKind, regionId, meteValue, lastMeteValue);

//        Log cLogger = LogFactory.getLog(this.getClass());
//        cLogger.info();

        return tCfgDataCurrentList;
    }

    @Logs(title = "分页查询", code = "TCfgDataCurrent",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDataCurrent> selectByPage(TCfgDataCurrent tCfgDataCurrent) {
        List<TCfgDataCurrent> tCfgDataCurrentList = tCfgDataCurrentDao.selectByPage(tCfgDataCurrent);
        return tCfgDataCurrentList;
    }

    @Logs(title = "批量插入", code = "TCfgDataCurrent",content = "根据web传入的参数批量删除")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgDataCurrent> list) {
        return this.tCfgDataCurrentDao.batchInsert(list);
    }




    //TODO: 测试方法 用完删除
    @Logs(title = "联动规则一次、二次匹配与规则计算、条件判断", code = "TCfgDataCurrent",content = "联动规则匹配")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTask> unionRulesMatchAndCalculate(String meteMap) throws ScriptException {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Set<Long> plans = new HashSet<>();//满足触发条件的预案
        Log cLogger = LogFactory.getLog(this.getClass());
        cLogger.info("清华方数据："+meteMap);
        cLogger.info("meteId--"+meteMap);
        //联动规则一次匹配
        Set<TCfgUnionRule> rules = new HashSet<>();
        
           cLogger.info("开始");
            List<TCfgUnionRule> unionrules = tCfgUnionRuleDao.selectUnionRuleByMeteId(meteMap);
            for (TCfgUnionRule rule : unionrules) {
                cLogger.info(rule.getInputParam());
                rules.add(rule);
            }
        cLogger.info("结束");
        cLogger.info("规则："+rules);

        List<Long> meteIdR = new ArrayList<>();//一次匹配到的规则所涵盖的所有meteId
        for (TCfgUnionRule rule : rules) {
            String[] currentMeteId = rule.getInputParam().split(", ");
            for (int i = 0; i < currentMeteId.length; i++) {
                meteIdR.add(Long.valueOf(currentMeteId[i]));
            }
        }

        //根据一次匹配拿到的meteId获取实时数据
        List<TCfgDataCurrent> currents = new ArrayList<>();
        for (Long meteId : meteIdR) {
            TCfgDataCurrent currentDatas = tCfgDataCurrentDao.selectCurrentDataByMeteId(meteId);//根据传来的发生变化的量的MeteId条件查询需要比较计算的实时数据
            if (currentDatas != null) {
                //汉字四遥值映射转换
                String finalValues=meteValues(currentDatas.getMeteValue());
                if(Objects.nonNull(finalValues)){
                    currentDatas.setMeteValue(finalValues);
                }
                currents.add(currentDatas);
            }

        }

        //规则二次匹配与规则计算，判断表达式是否触发
        //

        List<TCfgUnionRule> unionRule=new ArrayList<>();//触发联动的规则
        List<String> contents=new ArrayList<>();//触发联动的断面数据
        for (TCfgUnionRule rule : rules) {
            String content = rule.getRuleContent().replaceAll("\\{", "").replaceAll("}", "").replaceAll(",", "");
            for (TCfgDataCurrent current : currents) {
                StringBuilder stringBuilder = new StringBuilder("id:");
                String contentTemp = content.replaceAll((stringBuilder.append((current.getMeteId()).toString())).toString(), current.getMeteValue());
                content = contentTemp;

                if (content.contains("id:")) {
                    cLogger.info("未完成");
                } else if (content.contains("<") || content.contains(">") || content.contains("=")) {
                    ScriptEngineManager sm = new ScriptEngineManager();//字符串表达式计算
                    ScriptEngine engine = sm.getEngineByName("js");
                    String sum = engine.eval(content).toString();
                    if (sum == "true") {
                        unionRule.add(rule);
                        contents.add(content);
                        try {
                            int delay = rule.getRuleDelay();
                            Thread.sleep(delay * 1000);
                            cLogger.info(rule.getRuleName());
                            plans.add(rule.getPlanId());
                        } catch (Exception e) {
                            e.getMessage();
                        }
                        cLogger.info("执行预案");
                        cLogger.info(rule.getRuleName());
                        break;
                    } else if (sum == "false") {
                        cLogger.info("不满足触发条件");
                        break;
                    }

                } else {
                    cLogger.info("表达式错误");
                }

            }

        }
        //将满足条件的联动规则预案生成任务并执行
        List<TCruiseTask>tCruiseTasks=new ArrayList<>();
        for(Long plan:plans){
//            TCruiseTask tCruiseTask=new TCruiseTask();
            TCruisePlanCount tCruisePlan=tCruisePlanDao.selectByPrimaryId(plan);
//            tCruiseTasks.add(tCruiseTask);
            TCruiseTaskAdd tCruiseTaskAdd=new TCruiseTaskAdd();
            tCruiseTaskAdd.setPlanId(tCruisePlan.getPlanId());
            tCruiseTaskAdd.setTaskName(tCruisePlan.getPlanName()+simpleDateFormat.format(new Date()));
            tCruiseTaskAdd.setIfRun(173);
            tCruiseTaskAdd.setStartTime(new Date());
            tCruiseTaskAdd.setTaskType(218);
            Result result=tCruiseTaskController.insert(tCruiseTaskAdd);
            String taskId=result.getData().toString();//联动任务ID
            cLogger.info("联动开始执行");
            //联动记录插库
            tUnionTaskService.insertRecord(meteIdR.get(0),taskId,unionRule.get(0).getRuleId(), null, new Date(), contents.get(0));
            // TODO: 2020/12/12 待优化WB
            // webSocket通知前端产生联动信息
            Map<String, Object> jasonMap = new HashMap<>();
            jasonMap.put("type", "newLinkage");
            log.info("cfgDevice:"+currents.get(0).getDeviceId().toString());
            jasonMap.put("alarmName", tCfgDeviceService.selectByPrimaryId(currents.get(0).getDeviceId().toString()).getDeviceName());
            jasonMap.put("alarmTime",simpleDateFormat.format(new Date()));
            jasonMap.put("alarmContent", "触发联动");
            String jsonT = JSON.toJSONString(jasonMap);
            log.info("发送给前端的消息：" + jsonT);
            WebSocketServer.sendMsg(jsonT);

            TCfgUnionRule tCfgUnionRule = tCfgUnionRuleDao.selectByPrimaryId(unionRule.get(0).getRuleId());
            //根据该规则id是否设置了联动监控推送，若是，则将满足该条规则id产生的联动相关信息推送给前端；不是，不推
            if ("1".equals(tCfgUnionRule.getRuleType())){
                //webSocket通知前端调联动弹框的接口
                Map<String, Object> jasonMaps2 = new HashMap<>();
                jasonMaps2.put("type", "linkagePopUp");
                jasonMaps2.put("unionId", taskId);
                String json = JSON.toJSONString(jasonMaps2);
                log.info("发送给前端的消息：" + json);
                WebSocketServer.sendMsg(json);
            }
        }

        cLogger.info("联动任务：----"+tCruiseTasks);
//        return plans;
        return tCruiseTasks;
    }




//    @Logs(title = "联动规则一次、二次匹配与规则计算、条件判断", code = "TCfgDataCurrent",content = "联动规则匹配")
//    @Transactional(rollbackFor = Exception.class)
//    public List<TCruiseTask> unionRulesMatchAndCalculate(List<Long> meteIds) throws ScriptException {
//        Set<Long> plans = new HashSet<>();//满足触发条件的预案
//        Log cLogger = LogFactory.getLog(this.getClass());
//        cLogger.info("清华方数据："+meteIds);
//        //联动规则一次匹配
//        Set<TCfgUnionRule> rules = new HashSet<>();
//
//        for (Long meteId : meteIds) {
//            List<TCfgUnionRule> unionrules = tCfgUnionRuleDao.selectUnionRuleByMeteId(meteId.toString());
//            for (TCfgUnionRule rule : unionrules) {
//                cLogger.info(rule.getInputParam());
//                rules.add(rule);
//            }
//        }
//        Set<Long> meteIdR = new HashSet<>();//一次匹配到的规则所涵盖的所有meteId
//        for (TCfgUnionRule rule : rules) {
//            String[] currentMeteId = rule.getInputParam().split(", ");
//            for (int i = 0; i < currentMeteId.length; i++) {
//                meteIdR.add(Long.valueOf(currentMeteId[i]));
//            }
//        }
//
//        //根据一次匹配拿到的meteId获取实时数据
//        List<TCfgDataCurrent> currents = new ArrayList<>();
//        for (Long meteId : meteIdR) {
//            TCfgDataCurrent currentDatas = tCfgDataCurrentDao.selectCurrentDataByMeteId(meteId);//根据传来的发生变化的量的MeteId条件查询需要比较计算的实时数据
//            if (currentDatas != null) {
//                currents.add(currentDatas);
//            }
//
//        }
//
//        //规则二次匹配与规则计算，判断表达式是否触发
//        // TODO: 2020/9/25 待完善--触发条件向巡检模块微服务发送预案信息Post方法;
//
//        for (TCfgUnionRule rule : rules) {
//            String content = rule.getRuleContent().replaceAll("\\{", "").replaceAll("}", "").replaceAll(",", "");
//            for (TCfgDataCurrent current : currents) {
//                StringBuilder stringBuilder = new StringBuilder("id:");
//                String contentTemp = content.replaceAll((stringBuilder.append((current.getMeteId()).toString())).toString(), current.getMeteValue());
//                content = contentTemp;
//
//                if (content.contains("id:")) {
//                    cLogger.info("未完成");
//                } else if (content.contains("<") || content.contains(">") || content.contains("=")) {
//                    ScriptEngineManager sm = new ScriptEngineManager();//字符串表达式计算
//                    ScriptEngine engine = sm.getEngineByName("js");
//                    String sum = engine.eval(content).toString();
//                    if (sum == "true") {
//                        tUnionTaskService.insertRecord(rule.getRuleId(), null, new Date(), content);
//                        try {
//                            int delay = rule.getRuleDelay();
//                            Thread.sleep(delay * 1000);
//                            cLogger.info(rule.getRuleName());
//                            plans.add(rule.getPlanId());
//                        } catch (Exception e) {
//                            e.getMessage();
//                        }
//                        cLogger.info("执行预案");
//                        cLogger.info(rule.getRuleName());
//                        break;
//                    } else if (sum == "false") {
//                        cLogger.info("不满足触发条件");
//                        break;
//                    }
//
//                } else {
//                    cLogger.info("表达式错误");
//                }
//
//            }
//
//        }
//        //将满足条件的联动规则预案生成任务并执行
//        List<TCruiseTask>tCruiseTasks=new ArrayList<>();
//        for(Long plan:plans){
//        TCruiseTask tCruiseTask=new TCruiseTask();
//        TCruisePlanCount tCruisePlan=tCruisePlanDao.selectByPrimaryId(plan);
//        tCruiseTask.setPlanId(tCruisePlan.getPlanId());
//        tCruiseTask.setTaskName(tCruisePlan.getPlanName());
//        tCruiseTask.setIfRun(173);
//        tCruiseTaskService.insert(tCruiseTask);//执行联动任务
//        cLogger.info("联动开始执行");
//        tCruiseTasks.add(tCruiseTask);
//        }
////        return plans;
//        return tCruiseTasks;
//    }


    @Logs(title = "联动记录&&告警信息-滚动刷新", code = "TCfgDataCurrent",content = "联动信息滚动刷新")
    @Transactional(rollbackFor = Exception.class)
    public Map unionRecordRoll(String unionId) {
        // TODO: 2020/9/25 功能待完善--联动记录Id-单个/多个（demo仅使用单个）;记录结果集可用新类型来装（demo临时使用Map）
        // TODO: 2020/9/28  告警信息滚动刷新（信息获取->活动告警表?）（告警信息刷新方式：定时刷新or信号量变化时刷新?） show->不同告警类型下的数量与总数量or具体告警信息?
        TUnionTask tUnionTask = tUnionTaskDao.selectByPrimaryId(unionId);
        Map<String, Object> unionRecord = new HashMap<>();
        unionRecord.put("unionId", tUnionTask.getUnionId());
        unionRecord.put("unionName", tUnionTask.getUnionName());
        unionRecord.put("startTime", tUnionTask.getStartTime());
        return unionRecord;
    }


}

