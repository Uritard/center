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

import java.util.*;
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


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long ruleId) {
        return this.tCfgUnionRuleDao.deleteByPrimaryId(ruleId);
    }

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

    @Transactional(rollbackFor = Exception.class)
    public TCfgUnionRule selectByPrimaryId(Long ruleId) {
        return this.tCfgUnionRuleDao.selectByPrimaryId(ruleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCfgUnionRule> select(Long ruleId, Long planId, String ruleName, String ruleType, String ruleContent, Integer ruleDelay, String description, String inputParam, Date createTime, Date updateTime,Long cameraId,Long presetId) {
        List<TCfgUnionRule> tCfgUnionRuleList = tCfgUnionRuleDao.select(ruleId, planId, ruleName, ruleType, ruleContent, ruleDelay, description, inputParam, createTime, updateTime,cameraId,presetId);
        return tCfgUnionRuleList;
    }

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
                    if (Objects.nonNull(tCfgMete) && Objects.nonNull(str)) {
                        item.setRuleForShow(str.replace(stringBuilder.append(item1), tCfgMete.getMeteName()));
                    }
                }

            }
        }
        return tCfgUnionRuleList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCfgUnionRule> list) {
        int i = 0;
        for (TCfgUnionRule item: list) {
            i = i+this.tCfgUnionRuleDao.add(item);
        }
        return i;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String ruleIds) {
    List<String> list1= Arrays.asList(ruleIds.split(","));
    return this.tCfgUnionRuleDao.batchDelete(list1);
    }

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
        areaInfoItem2.setUpId(-2L);
        areaInfoItem2.setLabel("遥测");
        areaInfoItem2.setChildren(listItem2);
        areaInfoItem2.setInfoType("meteKind");
        list.add(areaInfoItem2);
        //遥控
        List<AreaInfo> listItem3 = this.tCfgUnionRuleDao.selectForTCfgMete(3);
        AreaInfo areaInfoItem3 = new AreaInfo();
        areaInfoItem3.setId(3L);
        areaInfoItem3.setUpId(-3L);
        areaInfoItem3.setLabel("遥控");
        areaInfoItem3.setChildren(listItem3);
        areaInfoItem3.setInfoType("meteKind");
        list.add(areaInfoItem3);

        //遥调
        List<AreaInfo> listItem4 = this.tCfgUnionRuleDao.selectForTCfgMete(4);
        AreaInfo areaInfoItem4 = new AreaInfo();
        areaInfoItem4.setId(4L);
        areaInfoItem4.setUpId(-4L);
        areaInfoItem4.setLabel("遥调");
        areaInfoItem4.setChildren(listItem4);
        areaInfoItem4.setInfoType("meteKind");
        list.add(areaInfoItem4);

        return list;
    }


}

