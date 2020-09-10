package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.CustomInfo;
import com.yjh.platform.module.device.entity.MeteModelDetail;
import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-07
*/
@Service
public class TStdMetemodelDetailService{

    @Autowired
    private TStdMetemodelDetailDao tStdMetemodelDetailDao;

    @Autowired
    private TStdDeviceDao tStdDeviceDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdMeteModelDetail tStdMeteModelDetail) {
        return this.tStdMetemodelDetailDao.add(tStdMeteModelDetail);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long modelId,Long meteId) {
        return this.tStdMetemodelDetailDao.deleteByPrimaryId(modelId,meteId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdMeteModelDetail tStdMeteModelDetail) {
//        List<CustomInfo> customInfoList=tStdDeviceDao.selectCustomInfoByDict();
//       for(CustomInfo customInfo:customInfoList){
//           if(tStdMeteModelDetail.getcustomTypeName().equals(customInfo.getcustomTypeName())){
//               tStdMeteModelDetail.setCustomType(customInfo.getCustomType());
//           }
//       }

        return this.tStdMetemodelDetailDao.update(tStdMeteModelDetail);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> selectByPrimaryId(Long modelId) {
        return this.tStdMetemodelDetailDao.selectByPrimaryId(modelId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> select(Long modelId, Long meteId, Integer customType, String customTypeName, String meteCode, String meteName, String meteType, Integer meteKind,String unit, String alarmNote, String alarmExplain, String alarmType, Float upEffect, Float downEffect, Integer alarmLevel, Float highLimit1, Float lowLimit1, Float highLimit2, Float lowLimit2,Float highLimit3, Float lowLimit3,Float highLimit4, Float lowLimit4, Integer alarmDelay, Integer alarmCnt, BigDecimal thresholdAbs, BigDecimal thresholdPer, Integer modulus) {
        List<TStdMeteModelDetail> tStdMeteModelDetailList = tStdMetemodelDetailDao.select(modelId, meteId, customType, customTypeName, meteCode, meteName, meteType, meteKind,unit, alarmNote, alarmExplain, alarmType, upEffect, downEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2,highLimit3, lowLimit3,highLimit4, lowLimit4, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus);
        return tStdMeteModelDetailList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> selectByPage(TStdMeteModelDetail tStdMeteModelDetail) {
        List<TStdMeteModelDetail> tStdMeteModelDetailList = tStdMetemodelDetailDao.selectByPage(tStdMeteModelDetail);
        return tStdMeteModelDetailList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdMeteModelDetail> list) {
        return this.tStdMetemodelDetailDao.batchAdd(list);
    }


}

