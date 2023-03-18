package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.dao.TCruiseNonhomologousPointInstanceDao;
import com.yjh.platform.module.device.entity.TCruiseNonhomologousPointInstance;
import com.yjh.platform.module.task.entity.TCruiseTriphaseRule;
import com.yjh.platform.module.task.dao.TCruiseTriphaseRuleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
* @author lqh
* @since 2023-03-18
*/
@Service
public class TCruiseTriphaseRuleService {

    @Autowired
    private TCruiseTriphaseRuleDao tCruiseTriphaseRuleDao;
    @Autowired
    private TCruiseNonhomologousPointInstanceDao tCruiseNonhomologousPointInstanceDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TCruiseTriphaseRule tCruiseTriphaseRule) {
        int checkExist = tCruiseTriphaseRuleDao.checkNonhomologousPointInstanceExist(tCruiseTriphaseRule);
        if(checkExist>0){
            throw new BusinessException("该非同源告警规则关联的巡视点已绑定其他非同源告警规则！");
        }else {
            tCruiseTriphaseRule.setOneCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseTriphaseRule.getInstanceOneId()));
            tCruiseTriphaseRule.setTwoCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseTriphaseRule.getInstanceTwoId()));
            tCruiseTriphaseRule.setTriCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseTriphaseRule.getInstanceTriId()));
        }
        return this.tCruiseTriphaseRuleDao.add(tCruiseTriphaseRule);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long triphaseId) {
        return this.tCruiseTriphaseRuleDao.deleteByPrimaryId(triphaseId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTriphaseRule tCruiseTriphaseRule) {
        tCruiseTriphaseRule.setOneCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseTriphaseRule.getInstanceOneId()));
        tCruiseTriphaseRule.setTwoCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseTriphaseRule.getInstanceTwoId()));
        tCruiseTriphaseRule.setTriCruiseDeviceName(tCruiseNonhomologousPointInstanceDao.selectCruiseDeviceByInstanceId(tCruiseTriphaseRule.getInstanceTriId()));
        return this.tCruiseTriphaseRuleDao.update(tCruiseTriphaseRule);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTriphaseRule selectByPrimaryId(Long triphaseId) {
        return this.tCruiseTriphaseRuleDao.selectByPrimaryId(triphaseId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTriphaseRule> select(Long triphaseId, String triphaseName, Long deviceMeteId, Long deviceId, String customId, Long instanceOneId, String instanceOneName, String oneCruiseDeviceName, Long instanceTwoId, String instanceTwoName, String twoCruiseDeviceName, Long instanceTriId, String instanceTriName, String triCruiseDeviceName, Integer identifyType, Integer identifySonType, Integer triphaseType, String warnThreshold, Integer warnLevel) {
        List<TCruiseTriphaseRule> tCruiseTriphaseRuleList = tCruiseTriphaseRuleDao.select(triphaseId, triphaseName, deviceMeteId, deviceId, customId, instanceOneId, instanceOneName, oneCruiseDeviceName, instanceTwoId, instanceTwoName, twoCruiseDeviceName, instanceTriId, instanceTriName, triCruiseDeviceName, identifyType, identifySonType, triphaseType, warnThreshold, warnLevel);
        return tCruiseTriphaseRuleList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseNonhomologousPointInstance> selectByPage(String name) {
        List<TCruiseNonhomologousPointInstance> tCruiseTriphaseRuleList = tCruiseTriphaseRuleDao.selectByPage(name);
        return tCruiseTriphaseRuleList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TCruiseTriphaseRule> list) {
        return this.tCruiseTriphaseRuleDao.batchAdd(list);
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String triphaseId) {
    List<String> list1= Arrays.asList(triphaseId.split(","));
    return this.tCruiseTriphaseRuleDao.batchDelete(list1);
    }



}

