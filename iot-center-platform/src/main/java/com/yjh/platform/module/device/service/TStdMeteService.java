package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.MeteInfo;
import com.yjh.platform.module.device.entity.TStdMete;
import com.yjh.platform.module.device.dao.TStdMeteDao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.yjh.platform.module.device.entity.TStdMeteDetail;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-07
*/
@Service
public class TStdMeteService{

    @Autowired
    private TStdMeteDao tStdMeteDao;

    @Logs(title = "插入", code = "module",content = "根据页面传入的参数新增数据")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdMete tStdMete) {
        return this.tStdMeteDao.add(tStdMete);
    }

    @Logs(title = "删除", code = "module",content = "根据页面传入的参数删除数据")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long stdMeteId) {
        return this.tStdMeteDao.deleteByPrimaryId(stdMeteId);
    }

    @Logs(title = "更新", code = "module",content = "根据页面传入的参数修改数据")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdMete tStdMete) {
        return this.tStdMeteDao.update(tStdMete);
    }

    @Logs(title = "主键查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public TStdMete selectByPrimaryId(Long stdMeteId) {
        return this.tStdMeteDao.selectByPrimaryId(stdMeteId);
    }

    @Logs(title = "查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMete> select(Long stdMeteId, Integer deviceType, String meteType, String meteName, String alarmNote, String alarmExplain, String alarmType,Integer analyseType, String unit, Float upEffect, Float lowEffect, Integer alarmLevel, Integer alarmLimit, Float highLimit1, Float lowLimit1, Float highLimit2, Float lowLimit2,Float highLimit3, Float lowLimit3,Float highLimit4, Float lowLimit4,Integer alarmDelay, Integer alarmCnt, BigDecimal thresholdAbs, BigDecimal thresholdPer, Integer modulus, String remark) {
        List<TStdMete> tStdMeteList = tStdMeteDao.select(stdMeteId, deviceType, meteType, meteName, alarmNote, alarmExplain, alarmType, analyseType, unit, upEffect, lowEffect, alarmLevel, alarmLimit,highLimit1, lowLimit1, highLimit2, lowLimit2,highLimit3, lowLimit3,highLimit4, lowLimit4, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark);
        return tStdMeteList;
    }

    @Logs(title = "分页查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteDetail> selectByPage(Integer deviceType, String meteName) {
        List<TStdMeteDetail> tStdMeteList = tStdMeteDao.selectByPage(deviceType,meteName);
        return tStdMeteList;
    }

    @Logs(title = "批量插入", code = "module",content = "根据页面传入的参数批量插入数据")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdMete> list) {
        return this.tStdMeteDao.batchAdd(list);
    }


    @Logs(title = "根据设备ID查找",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<MeteInfo> selectByDeviceType(Integer deviceType){
        return this.tStdMeteDao.selectByDeviceType(deviceType);
    }

    @Logs(title = "设备类型树",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> deviceTypeTree(){
        List<TDictBusiness> list = tStdMeteDao.selectForDeviceTypeTree("device_type");
        List<AreaInfo> re = new ArrayList<>();
        AreaInfo tree = new AreaInfo();
        tree.setId(1L);
        tree.setLabel("设备类型树");
        tree.setInfoType("tree");
        List<AreaInfo> child = new ArrayList<>();
        for (TDictBusiness item:list) {
            AreaInfo areaInfo = new AreaInfo();
            areaInfo.setUpId(tree.getId());
            areaInfo.setUpName(tree.getLabel());
            areaInfo.setInfoType("device_type");
            areaInfo.setId(Long.valueOf(item.getDictCode()));
            areaInfo.setLabel(item.getDictNote());
            child.add(areaInfo);
        }
        tree.setChildren(child);
        re.add(tree);
        return re;
    }
    @Logs(title = "批量删除",code = "module", content = "批量删除测点")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String list){
        List<String> list1= Arrays.asList(list.split(","));
        return this.tStdMeteDao.batchDelete(list1);
    }
}

