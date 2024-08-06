package com.yjh.platform.module.device.service;

import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.CustomInfo;
import com.yjh.platform.module.device.entity.MeteModelDetail;
import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;

import java.beans.IntrospectionException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.StringUtils;
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

    @Transactional(rollbackFor = Exception.class)
    public int add(TStdMeteModelDetail tStdMeteModelDetail) {
        return this.tStdMetemodelDetailDao.add(tStdMeteModelDetail);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long modelId,Long meteId) {
        return this.tStdMetemodelDetailDao.deleteByPrimaryId(modelId,meteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TStdMeteModelDetail tStdMeteModelDetail) {
        return this.tStdMetemodelDetailDao.update(tStdMeteModelDetail);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> selectByPrimaryId(Long modelId) {
        return this.tStdMetemodelDetailDao.selectByPrimaryId(modelId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> select(Long modelId, Long meteId, String customType, String customTypeName, String meteName, String meteType, Integer meteKind, Integer analyseType,String unit, String alarmNote, String alarmExplain, String alarmType, Float upEffect, Float downEffect, Integer alarmLevel, Float highLimit1, Float lowLimit1, Float highLimit2, Float lowLimit2, Float highLimit3, Float lowLimit3, Float highLimit4, Float lowLimit4, Integer alarmDelay, Integer alarmCnt, BigDecimal thresholdAbs, BigDecimal thresholdPer, Integer modulus, String labelAttri) {
        List<TStdMeteModelDetail> tStdMeteModelDetailList = tStdMetemodelDetailDao.select(modelId, meteId, customType, customTypeName,  meteName, meteType, meteKind,analyseType,unit, alarmNote, alarmExplain, alarmType, upEffect, downEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2,highLimit3, lowLimit3,highLimit4, lowLimit4, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, labelAttri);
        return tStdMeteModelDetailList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> selectByPage(TStdMeteModelDetail tStdMeteModelDetail) {
        List<TStdMeteModelDetail> tStdMeteModelDetailList = tStdMetemodelDetailDao.selectByPage(tStdMeteModelDetail);
        tStdMeteModelDetailList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getLabelAttri())){
                item.setLabelAttris(item.getLabelAttri().split(","));
                item.setLabelAttriName(labelAttriName(item.getLabelAttri()));
            }
        });
        return tStdMeteModelDetailList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdMeteModelDetail> list) {
        return this.tStdMetemodelDetailDao.batchAdd(list);
    }

    public List<KeyValue<String, String>> labelAttriName(String labelAttri) {
        String lableNames = DictConvertUtil.DICT.covertToDict("labelAttri", labelAttri);
        if (StringUtils.isNotEmpty(lableNames)) {
            List<KeyValue<String, String>> pairNames = new ArrayList<>();
            String[] is = labelAttri.split(",");
            String[] names = lableNames.split(",");
            for (int i = 0; i < is.length; i++) {
                pairNames.add(new DefaultKeyValue<>(names[i], CommonUtils.color(is[i])));
            }
            return pairNames;
        }
        return Collections.emptyList();
    }


}

