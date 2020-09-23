package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.dao.TCfgUnionRuleDao;
import com.yjh.platform.module.task.entity.TCfgDataCurrent;
import com.yjh.platform.module.task.dao.TCfgDataCurrentDao;

import java.util.*;

import com.yjh.platform.module.task.entity.TCfgUnionRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
* @author czh
* @since 2020-08-25
*/
@Service
public class TCfgDataCurrentService{

    @Autowired
    private TCfgDataCurrentDao tCfgDataCurrentDao;

    @Autowired
    private TCfgUnionRuleDao tCfgUnionRuleDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgDataCurrent tCfgDataCurrent) {
        return this.tCfgDataCurrentDao.insert(tCfgDataCurrent);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long meteId) {
        return this.tCfgDataCurrentDao.deleteByPrimaryId(meteId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgDataCurrent tCfgDataCurrent) {
        return this.tCfgDataCurrentDao.update(tCfgDataCurrent);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgDataCurrent selectByPrimaryId(Long meteId) {
        return this.tCfgDataCurrentDao.selectByPrimaryId(meteId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDataCurrent> select(Long meteId, Long deviceId, String cunstomId, Date recordTime, Integer meteKind, String regionId, String meteValue, String lastMeteValue) throws ScriptException {
        List<TCfgDataCurrent> tCfgDataCurrentList = tCfgDataCurrentDao.select(meteId, deviceId, cunstomId, recordTime, meteKind, regionId, meteValue, lastMeteValue);

//        Log cLogger = LogFactory.getLog(this.getClass());
//        cLogger.info();

        return tCfgDataCurrentList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgDataCurrent> selectByPage(TCfgDataCurrent tCfgDataCurrent) {
        List<TCfgDataCurrent> tCfgDataCurrentList = tCfgDataCurrentDao.selectByPage(tCfgDataCurrent);
        return tCfgDataCurrentList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgDataCurrent> list){
        return this.tCfgDataCurrentDao.batchInsert(list);
    }

//    @Logs(title = "联动规则一次匹配",code = "module")
//    @Transactional(rollbackFor = Exception.class)
//    public Set<TCfgUnionRule> unionRuleslMatch(){
//        Set<TCfgUnionRule> result=new HashSet<>();
//        List<Long> meteIds= tCfgDataCurrentDao.selectAllMeteId();
//        for(Long meteId:meteIds){
//            List<TCfgUnionRule> rules=tCfgUnionRuleDao.selectUnionRuleByMeteId(meteId.toString());
//            for(TCfgUnionRule rule:rules){
//                result.add(rule);
//            }
//        }
//       return result;
//    }

    @Logs(title = "联动规则一次、二次匹配与规则计算、条件判断",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Set<Long> unionRulescaculator(List<Long> meteIds) throws ScriptException {
        // TODO: 2020/9/22  规则内部实时数据比较、表达式定位赋值、判断表达式是否计算完成、判断表达式结果并执行不同工作路线->返回满足触发表达式条件的规则（预案信息）
        //联动规则一次匹配
        Set<TCfgUnionRule> rules=new HashSet<>();

        for(Long meteId:meteIds){
            List<TCfgUnionRule> unionrules=tCfgUnionRuleDao.selectUnionRuleByMeteId(meteId.toString());
            for(TCfgUnionRule rule:unionrules){
                rules.add(rule);
            }
        }
        Log cLogger = LogFactory.getLog(this.getClass());
        Set<Long> plans=new HashSet<>( );//满足触发条件的预案
        List<TCfgDataCurrent> currents=new ArrayList<>();
        for(Long meteId:meteIds){
            TCfgDataCurrent currentDates=tCfgDataCurrentDao.selectCurrentDataByMeteId(meteId);//根据传来的发生变化的量的MeteId条件查询需要比较计算的实时数据
            currents.add(currentDates);
        }


        for(TCfgUnionRule rule:rules){
            String content=rule.getRuleContent();
            for(TCfgDataCurrent current:currents){
                 StringBuilder stringBuilder=new StringBuilder("id=");
                 String contentTemp=content.replaceAll((stringBuilder.append((current.getMeteId()).toString())).toString(),current.getMeteValue());
                 content=contentTemp;

                 if(content.contains("id=")){
                     cLogger.info("未完成");
                 }else {
                     ScriptEngineManager sm=new ScriptEngineManager();
                     ScriptEngine engine=sm.getEngineByName("js");
                     String sum=engine.eval(content).toString();
                     if(sum=="true"){
                         plans.add(rule.getPlanId());
                         cLogger.info("执行预案");
                         cLogger.info(rule.getRuleName());
                         break;
                     }else if(sum=="false"){
                         cLogger.info("不满足触发条件");
                         break;
                     }

                 }


            }
        }
        return plans;
    }


}

