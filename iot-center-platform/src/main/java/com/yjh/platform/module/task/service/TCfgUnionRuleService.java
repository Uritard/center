package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.dao.TCfgMeteDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TCfgMete;
import com.yjh.platform.module.task.dao.TCfgUnionRuleDao;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.entity.TCfgUnionRule;
import com.yjh.platform.module.task.entity.TCfgUnionRuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author lqh
* @since 2020-09-18
*/
@Service
public class TCfgUnionRuleService{

    @Autowired
    private TCfgUnionRuleDao tCfgUnionRuleDao;
    @Autowired
    private TCfgMeteDao tCfgMeteDao;

    @Autowired
    private TCruisePlanDao tCruisePlanDao;

    @Logs(title = "插入", code = "TCfgUnionRule",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int add(TCfgUnionRule tCfgUnionRule) {
            String regex = "id:([\\w]*?)\\W";
            Matcher matcher = Pattern.compile(regex).matcher(tCfgUnionRule.getRuleContent());
            List<String> list = new LinkedList<>();
            while (matcher.find()){
                list.add(matcher.group(1).trim());
            }
        tCfgUnionRule.setInputParam(list.toString().replaceAll("\\[","")
                .replaceAll("]",""));
            return this.tCfgUnionRuleDao.add(tCfgUnionRule);
    }


    @Logs(title = "删除", code = "TCfgUnionRule",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long ruleId) {
        return this.tCfgUnionRuleDao.deleteByPrimaryId(ruleId);
    }

    @Logs(title = "更新", code = "TCfgUnionRule",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgUnionRule tCfgUnionRule) {
            String regex = "id:([\\w]*?)\\W";
            Matcher matcher = Pattern.compile(regex).matcher(tCfgUnionRule.getRuleContent());
            List<String> list = new LinkedList<>();
            while (matcher.find()){
                list.add(matcher.group(1).trim());
            }
            tCfgUnionRule.setInputParam(list.toString().replaceAll("\\[","")
                                                       .replaceAll("]",""));
            return this.tCfgUnionRuleDao.update(tCfgUnionRule);
    }

    @Logs(title = "主键查询", code = "TCfgUnionRule",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public TCfgUnionRule selectByPrimaryId(Long ruleId) {
        return this.tCfgUnionRuleDao.selectByPrimaryId(ruleId);
    }

    @Logs(title = "查询", code = "TCfgUnionRule",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgUnionRule> select(Long ruleId, Long planId, String ruleName, String ruleType, String ruleContent, Integer ruleDelay, String description, String inputParam, Date createTime, Date updateTime) {
        List<TCfgUnionRule> tCfgUnionRuleList = tCfgUnionRuleDao.select(ruleId, planId, ruleName, ruleType, ruleContent, ruleDelay, description, inputParam, createTime, updateTime);
        return tCfgUnionRuleList;
    }

    @Logs(title = "分页查询", code = "TCfgUnionRule",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgUnionRuleDetail> selectByPage(String ruleName) {
        TCfgUnionRule tCfgUnionRule = new TCfgUnionRule();
        tCfgUnionRule.setRuleName(ruleName);
        List<TCfgUnionRuleDetail> tCfgUnionRuleList = tCfgUnionRuleDao.selectByPage(tCfgUnionRule);
        if (tCfgUnionRuleList != null && tCfgUnionRuleList.size()>0){
            for (TCfgUnionRuleDetail item: tCfgUnionRuleList) {
                item.setRuleForShow(item.getRuleContent());
                for (String item1:item.getInputParam().split(", ")) {
                    String str = item.getRuleForShow();
                    TCfgMete tCfgMete = tCfgMeteDao.selectByPrimaryId(item1);
                    StringBuilder stringBuilder = new StringBuilder("id:");
                    item.setRuleForShow(str.replace(stringBuilder.append(item1),tCfgMete.getMeteName()));
                }

            }
        }
        return tCfgUnionRuleList;
    }

    @Logs(title = "批量插入", code = "TCfgUnionRule",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCfgUnionRule> list) {
        int i = 0;
        for (TCfgUnionRule item: list) {
            i = i+this.tCfgUnionRuleDao.add(item);
        }
        return i;
    }

    @Logs(title = "批量删除", code = "TCfgUnionRule",content = "根据web传入的参数批量删除")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String ruleIds) {
    List<String> list1= Arrays.asList(ruleIds.split(","));
    return this.tCfgUnionRuleDao.batchDelete(list1);
    }

    @Logs(title = "查询四遥树信息", code = "TCfgUnionRule",content = "查询四遥信息")
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectForTCfgMete(){
        List<AreaInfo> list = new LinkedList<>();
        //遥信
        List<AreaInfo> listItem1 = this.tCfgUnionRuleDao.selectForTCfgMete(1);
        AreaInfo areaInfoItem1 = new AreaInfo();
        areaInfoItem1.setId(1L);
        areaInfoItem1.setUpId(-1L);
        areaInfoItem1.setLabel("遥信");
        areaInfoItem1.setChildren(listItem1);
        areaInfoItem1.setInfoType("meteKind");
        list.add(areaInfoItem1);
        //遥测
        List<AreaInfo> listItem2 = this.tCfgUnionRuleDao.selectForTCfgMete(2);
        AreaInfo areaInfoItem2 = new AreaInfo();
        areaInfoItem2.setId(2L);
        areaInfoItem2.setUpId(-1L);
        areaInfoItem2.setLabel("遥测");
        areaInfoItem2.setChildren(listItem2);
        areaInfoItem2.setInfoType("meteKind");
        list.add(areaInfoItem2);

        return list;
    }


}

