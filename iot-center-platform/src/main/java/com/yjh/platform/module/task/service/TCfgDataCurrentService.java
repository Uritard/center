package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.module.device.service.TCfgDeviceService;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.dao.TCfgDataCurrentDao;
import com.yjh.platform.module.task.dao.TCfgUnionRuleDao;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author czh
 * @since 2020-08-25
 */
@Service
public class TCfgDataCurrentService {

    private final Logger log = LoggerFactory.getLogger(TCfgDataCurrentService.class);

    @Autowired
    private TCfgDataCurrentDao tCfgDataCurrentDao;

    @Autowired
    private TCfgUnionRuleDao tCfgUnionRuleDao;

    @Autowired
    private TUnionTaskService tUnionTaskService;

    @Autowired
    private TCruisePlanDao tCruisePlanDao;

    @Autowired
    private UPatrolTaskService uPatrolTaskService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TCfgDeviceService tCfgDeviceService;

    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPresetForTask?presetId={presetId}&cameraId={cameraId}";

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

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgDataCurrent tCfgDataCurrent) {
        return this.tCfgDataCurrentDao.insert(tCfgDataCurrent);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long meteId) {
        return this.tCfgDataCurrentDao.deleteByPrimaryId(meteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgDataCurrent tCfgDataCurrent) {
        return this.tCfgDataCurrentDao.update(tCfgDataCurrent);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCfgDataCurrent selectByPrimaryId(Long meteId) {
        return this.tCfgDataCurrentDao.selectByPrimaryId(meteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDataCurrent> select(Long meteId, Long deviceId, String cunstomId, Date recordTime, Integer meteKind, String regionId, String meteValue, String lastMeteValue) throws ScriptException {
        return tCfgDataCurrentDao.select(meteId, deviceId, cunstomId, recordTime, meteKind, regionId, meteValue, lastMeteValue);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDataCurrent> selectByPage(String meteName,Date startDate,Date endDate) {
        List<TCfgDataCurrent> tCfgDataCurrentList = tCfgDataCurrentDao.selectByPage(meteName,startDate,endDate);
        tCfgDataCurrentList.forEach(item -> {
            String[] nameList = item.getDeviceName().split("/");
            String defaultName = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeCode", "content"));
            if (nameList.length<3){
                item.setRegion(defaultName);
                item.setStationName(defaultName);
                item.setDeviceName(nameList[nameList.length-1]);
            } else {
                item.setRegion(nameList[0]);
                item.setStationName(nameList[0]);
                item.setDeviceName(nameList[1]+"/"+nameList[2]);
            }

        });
        return tCfgDataCurrentList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgDataCurrent> list) {
        return this.tCfgDataCurrentDao.batchInsert(list);
    }


    public List<TCruiseTask> unionRulesMatchAndCalculate(String meteMap) {
        log.info("【meteMap】:{}", meteMap);
        // 联动规则一次匹配
        Set<TCfgUnionRule> rules = new HashSet<>();
        List<TCfgUnionRule> unionRules = tCfgUnionRuleDao.selectUnionRuleByMeteId(meteMap);
        for (TCfgUnionRule rule : unionRules) {
            log.info(rule.getInputParam());
            rules.add(rule);
        }
        log.info("【rules】:{}", rules);

        // 一次匹配到的规则所涵盖的所有meteId
        List<Long> meteIdR = new ArrayList<>();
        for (TCfgUnionRule rule : rules) {
            String[] currentMeteId = rule.getInputParam().split(", ");
            for (int i = 0; i < currentMeteId.length; i++) {
                meteIdR.add(Long.valueOf(currentMeteId[i]));
            }
        }

        // 根据一次匹配拿到的meteId获取实时数据
        List<TCfgDataCurrent> currents = new ArrayList<>();
        for (Long meteId : meteIdR) {
            // 根据传来的发生变化的量的MeteId条件查询需要比较计算的实时数据
            TCfgDataCurrent currentDatas = tCfgDataCurrentDao.selectCurrentDataByMeteId(meteId);
            if (currentDatas != null) {
                // 汉字四遥值映射转换
                String finalValues = meteValues(currentDatas.getMeteValue());
                if(Objects.nonNull(finalValues)){
                    currentDatas.setMeteValue(finalValues);
                }
                currents.add(currentDatas);
            }
        }

        // 规则二次匹配与规则计算，判断表达式是否触发
        List<TCfgUnionRule> unionRule = new ArrayList<>();
        // 触发联动的断面数据
        List<String> contents = new ArrayList<>();
        // 满足触发条件的预案
        Set<Long> plans = new HashSet<>();
        for (TCfgUnionRule rule : rules) {
            String content = rule.getRuleContent()
                    .replaceAll("\\{", "")
                    .replaceAll("}", "")
                    .replaceAll(",", "");
            for (TCfgDataCurrent current : currents) {
                StringBuilder stringBuilder = new StringBuilder("id:");
                String contentTemp = content.replaceAll((stringBuilder.append((current.getMeteId()).toString())).toString(), current.getMeteValue());
                content = contentTemp;

                if (content.contains("id:")) {
                    log.info("未完成");
                } else if (content.contains("<") || content.contains(">") || content.contains("=")) {
                    // 字符串表达式计算
                    ScriptEngineManager sm = new ScriptEngineManager();
                    ScriptEngine engine = sm.getEngineByName("js");
                    String sum = null;
                    try {
                        sum = engine.eval(content).toString();
                    } catch (ScriptException e) {
                        log.error(e.getMessage(), e);
                    }

                    if (StringUtils.equals("false", sum)) {
                        log.info("不满足触发条件");
                        break;
                    }

                    // 预案为空
                    if (rule.getPlanId() == null && rule.getPresetId() != null){
                        // 配了联动预置位
                        Map<String,String> map = new HashMap<>();
                        map.put("type","linkagePresetPopUp");
                        map.put("cameraId", String.valueOf(rule.getCameraId()));
                        map.put("presetId",String.valueOf(rule.getPresetId()));
                        map.put("meteName",current.getDeviceName());
                        map.put("meteKindName",current.getMeteKindName());
                        map.put("meteValue",current.getMeteValue());
                        map.put("time", DateTimeUtil.format(current.getRecordTime()));
                        String json = JSON.toJSONString(map);
                        log.info("发送给前端的联动预置位消息{}", json);
                        // 将摄像机转到预置位
                        ThreadPoolUtil.COMMON_POOL.addThread(new Runnable() {
                            @Override
                            public void run() {
                                String str = "camera_info:" + rule.getCameraId();
                                Map<String, String> map = redisTemplate.opsForHash().entries(str);
                                if ("0".equals(map.get("state"))) {
                                    HashMap<String, Object> moveMap = new HashMap<>();
                                    moveMap.put("presetId", rule.getPresetId());
                                    moveMap.put("cameraId", rule.getCameraId());
                                    move(moveMap);
                                    map.put("lastTime",DateTimeUtil.format(new Date()));
                                    redisTemplate.opsForHash().putAll(str,map);
                                }
                            }
                        });
                        Constant.websocketSendMsg(Constant.WEBSOCKET_URL, map);
                    }
                    unionRule.add(rule);
                    contents.add(content);
                    try {
                        int delay = rule.getRuleDelay();
                        Thread.sleep(delay * 1000);
                        log.info(rule.getRuleName());
                        plans.add(rule.getPlanId());
                    } catch (Exception e) {
                       log.error(e.getMessage(), e);
                    }
                    log.info("【执行预案】:{}", rule.getRuleName());

                } else {
                    log.info("表达式错误");
                    try {
                        int delay = rule.getRuleDelay();
                        Thread.sleep(delay * 1000);
                        log.info(rule.getRuleName());
                        plans.add(rule.getPlanId());
                    } catch (Exception e) {
                       log.error(e.getMessage(), e);
                    }
                }
            }
        }

        //将满足条件的联动规则预案生成任务并执行
        List<TCruiseTask>tCruiseTasks=new ArrayList<>();
        for(Long plan : plans){
            log.info("【planId】:{}", plan);
            if (plan == null){
                continue;
            }
            TCruisePlanCount tCruisePlan=tCruisePlanDao.selectByPrimaryId(plan);
            TCruiseTaskAdd tCruiseTaskAdd=new TCruiseTaskAdd();
            tCruiseTaskAdd.setPlanId(tCruisePlan.getPlanId());
            tCruiseTaskAdd.setTaskName(tCruisePlan.getPlanName() + DateTimeUtil.format(new Date()));
            tCruiseTaskAdd.setIfRun(173);
            tCruiseTaskAdd.setStartTime(new Date());
            tCruiseTaskAdd.setCycleExecuteTime("");
            tCruiseTaskAdd.setUnionTaskStatus("1");
            Map<String, Object> taskMap = uPatrolTaskService.addTask(tCruiseTaskAdd, true);
            // 联动任务ID
            String taskId= String.valueOf(taskMap.get("taskId"));
            log.info("联动开始执行");
            // 联动记录插库
            TCfgDataCurrent unionForGetTime = tCfgDataCurrentDao.selectCurrentDataByMeteId(Long.valueOf(meteMap));
            tUnionTaskService.insertRecord(meteIdR.get(0), taskId, unionRule.get(0).getRuleId(), null, new Date(),
                    contents.get(0),unionForGetTime.getRecordTime());

            Map<String,String> currentUnionInfo=new HashMap<>();
            currentUnionInfo.put("unionId",taskId);
            currentUnionInfo.put("isPop","false");

            // webSocket通知前端产生联动信息
            Map<String, String> jasonMap = new HashMap<>();
            jasonMap.put("type", "newLinkage");
            log.info("cfgDevice:{}", currents.get(0).getDeviceId().toString());
            jasonMap.put("deviceName", tCfgDeviceService.selectByPrimaryId(currents.get(0).getDeviceId().toString()).getDeviceName());
            jasonMap.put("time", DateTimeUtil.format(new Date()));
            jasonMap.put("warnContent", tCfgDeviceService.selectByPrimaryId(currents.get(0).getDeviceId().toString()).getDeviceName()+"触发联动");
            log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMap));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);

            TCfgUnionRule tCfgUnionRule = tCfgUnionRuleDao.selectByPrimaryId(unionRule.get(0).getRuleId());
            // 根据该规则id是否设置了联动监控推送，若是，则将满足该条规则id产生的联动相关信息推送给前端；不是，不推
            if ("1".equals(tCfgUnionRule.getRuleType())){
                currentUnionInfo.put("isPop","true");
                Map<String, String> jasonMaps2 = new HashMap<>();
                jasonMaps2.put("type", "linkagePopUp");
                jasonMaps2.put("unionId", taskId);
                log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMaps2));
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps2);
            }
            redisTemplate.opsForValue().set("currentUnion",currentUnionInfo,3, TimeUnit.MINUTES);
        }
        log.info("【联动任务】:{}", tCruiseTasks);
        return tCruiseTasks;
    }


    /**
     * 相机转到预置位
     * @param map 参数
     * @return void
     */
    private void move(HashMap<String, Object> map) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.getForObject(MOVE_URL, String.class, map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

