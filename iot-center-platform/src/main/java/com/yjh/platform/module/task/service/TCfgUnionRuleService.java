package com.yjh.platform.module.task.service;

import com.yjh.platform.module.device.dao.TCfgMeteDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TCfgMete;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.entity.TCfgUnionRule;
import com.yjh.platform.module.task.dao.TCfgUnionRuleDao;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yjh.platform.module.task.entity.TCfgUnionRuleDetail;
import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TCfgUnionRule tCfgUnionRule) {
        if(isValid(tCfgUnionRule.getRuleContent())){
            String regex = "id=([\\w]*?)\\W";
            Matcher matcher = Pattern.compile(regex).matcher(tCfgUnionRule.getRuleContent());
            List<String> list = new LinkedList<>();
            while (matcher.find()){
                list.add(matcher.group(1).trim());
            }
            tCfgUnionRule.setInputParam(list.toString().replaceAll("\\[","").replaceAll("]",""));
            return this.tCfgUnionRuleDao.add(tCfgUnionRule);
        }
        return -1;
    }
    public static boolean isValid(String s) {
        Stack<Character> stack = new Stack();
        // 取出字符串中的每一个括号
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            // 如果遇到左括号，进行压栈，
            // 如果遇到右括号，进行出栈
            switch (ch) {
                case '(':
                case '[':
                case '{':
                    stack.push(ch);
                    break;
                case ')':
                case '}':
                case ']':
                    // 如果此时遇到了右括号
                    if (stack.isEmpty()) {
                        return false;
                    }
                    // 如果此时栈中保存着左括号,看与右括号是否匹配
                    // 移除栈顶元素
                    char left = stack.pop();
                    // 如果不匹配
                    if (!((left == '(' && ch == ')') || (left == '{' && ch == '}') || (left == '[' && ch == ']'))) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        // 此时字符串中字符已经都走过了
        // 栈中应该是空的
        if (stack.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }


    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long ruleId) {
        return this.tCfgUnionRuleDao.deleteByPrimaryId(ruleId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgUnionRule tCfgUnionRule) {
        if(isValid(tCfgUnionRule.getRuleContent())){
            String regex = "id=([\\w]*?)\\W";
            Matcher matcher = Pattern.compile(regex).matcher(tCfgUnionRule.getRuleContent());
            List<String> list = new LinkedList<>();
            while (matcher.find()){
                list.add(matcher.group(1).trim());
            }
            tCfgUnionRule.setInputParam(list.toString().replaceAll("\\[","").replaceAll("]",""));
            return this.tCfgUnionRuleDao.update(tCfgUnionRule);
        }
        return -1;
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgUnionRule selectByPrimaryId(Long ruleId) {
        return this.tCfgUnionRuleDao.selectByPrimaryId(ruleId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgUnionRule> select(Long ruleId, Long planId, String ruleName, String ruleType, String ruleContent, Integer ruleDelay, String description, String inputParam, Date createTime, Date updateTime) {
        List<TCfgUnionRule> tCfgUnionRuleList = tCfgUnionRuleDao.select(ruleId, planId, ruleName, ruleType, ruleContent, ruleDelay, description, inputParam, createTime, updateTime);
        return tCfgUnionRuleList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgUnionRuleDetail> selectByPage(String ruleName) {
        TCfgUnionRule tCfgUnionRule = new TCfgUnionRule();
        tCfgUnionRule.setRuleName(ruleName);
        List<TCfgUnionRuleDetail> tCfgUnionRuleList = tCfgUnionRuleDao.selectByPage(tCfgUnionRule);
        for (TCfgUnionRuleDetail item: tCfgUnionRuleList) {
            item.setRuleForShow(item.getRuleContent());
            for (String item1:item.getInputParam().split(", ")) {
                String str = item.getRuleForShow();
                TCfgMete tCfgMete = tCfgMeteDao.selectByPrimaryId(item1);
                StringBuilder stringBuilder = new StringBuilder("id=");
                item.setRuleForShow(str.replace(stringBuilder.append(item1),tCfgMete.getMeteName()));
            }
        }
        return tCfgUnionRuleList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCfgUnionRule> list) {
        int i = 0;
        for (TCfgUnionRule item: list) {
            i = i+this.tCfgUnionRuleDao.add(item);
        }
        return i;
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String ruleIds) {
    List<String> list1= Arrays.asList(ruleIds.split(","));
    return this.tCfgUnionRuleDao.batchDelete(list1);
    }

    @Logs(title = "查询四遥树信息", code = "module")
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
        areaInfoItem1.setInfoType("infoType");
        list.add(areaInfoItem1);
        //遥测
        List<AreaInfo> listItem2 = this.tCfgUnionRuleDao.selectForTCfgMete(2);
        AreaInfo areaInfoItem2 = new AreaInfo();
        areaInfoItem2.setId(2L);
        areaInfoItem1.setUpId(-1L);
        areaInfoItem2.setLabel("遥测");
        areaInfoItem2.setChildren(listItem2);
        areaInfoItem2.setInfoType("infoType");
        list.add(areaInfoItem2);

        return list;
    }

    @Logs(title = "查询预案信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> selectForTCPlan(String planName){
        return this.tCruisePlanDao.select(null,planName,null,null,null,null);
    }



}

