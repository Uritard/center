package com.yjh.platform.module.device.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.enums.AlarmLevelEnum;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.module.device.dao.TAlgorithmConfBakDao;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTypeDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author tt
* @since 2020-08-08
*/
@Service
public class TStdDevicemeteService{

    private Logger log = LoggerFactory.getLogger(TStdDevicemeteService.class);
    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private TStdDeviceDao tStdDeviceDao;
    @Autowired
    private TDictBusinessDao tDictBusinessDao;
    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;
    @Autowired
    private TCruisePlanAttrDao tCruisePlanAttrDao;
    @Autowired
    private TAlgorithmConfBakDao tAlgorithmConfBakDao;
    @Autowired
    private TCruiseTypeDao tCruiseTypeDao;


    @Transactional(rollbackFor = Exception.class)
    public int add(TStdDeviceMeteDetail tStdDeviceMeteDetail) {


        //若插入的测点中的deviceId、customId存在于device表中，则不更新device表；否则进行更新
//        TStdDevice tStdDevice=tStdDeviceDao.selectByUnionKeys(tStdDeviceMeteDetail.getDeviceId(),tStdDeviceMeteDetail.getCustomType());//查看当前设备ID和部位ID下的设备信息
//        if(Objects.isNull(tStdDevice)){
//            List<TStdDevice> stdDevice=tStdDeviceDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceId());
//            stdDevice.get(0).setCustomId(tStdDeviceMeteDetail.getCustomType());
//            List<TDictBusiness> list = tDictBusinessDao.select(null,tStdDeviceMeteDetail.getCustomType(),null,null,null,null,null);
//            stdDevice.get(0).setCustomName(list.get(0).getDictNote());
//            tStdDeviceDao.add(stdDevice.get(0));
            //测点中不存在本体部位 且 新增的测点部位不为本体 则删除之前设备的本体部位
//            if(tStdDevicemeteDao.selectByDevCus(tStdDeviceMeteDetail.getDeviceId(),"101").size()==0 && !(tStdDeviceMeteDetail.getCustomType().equals("101"))){
//                tStdDeviceDao.deleteByUnionKeys(tStdDeviceMeteDetail.getDeviceId(),"101");  //删除之前本体部位的设备
//            }
//        }
        if(StringUtils.isNotBlank(tStdDeviceMeteDetail.getAlarmLevelString())) {
            String[] alarmLevelList = tStdDeviceMeteDetail.getAlarmLevelString().split(",");
            tStdDeviceMeteDetail.setAlarmLevel(Integer.parseInt(alarmLevelList[0]));
        }

        this.tStdDevicemeteDao.add(tStdDeviceMeteDetail);
        //配置算法
        if(tStdDeviceMeteDetail.getAnalyseType() != null){
            TAlgorithmInfo tAlgorithmInfo = tAlgorithmConfBakDao.selectByAnalyseType(String.valueOf(tStdDeviceMeteDetail.getAnalyseType()));
            TAlgorithmConfBak tAlgorithmConfBak = new TAlgorithmConfBak();
            if(tAlgorithmInfo != null){
                tAlgorithmConfBak.setAlgorithmId(tAlgorithmInfo.getAlgorithmId());
                tAlgorithmConfBak.setDeviceMeteId(tStdDeviceMeteDetail.getDeviceMeteId());
                tAlgorithmConfBakDao.add(tAlgorithmConfBak);
            }
        }
//        if("on".equals(tStdDeviceMeteDetail.getIsAi())){
//            TAlgorithmInfo tAlgorithmInfo = tAlgorithmConfBakDao.selectByAnalyseType("398");
//            TAlgorithmConfBak tAlgorithmConfBak = new TAlgorithmConfBak();
//            if(tAlgorithmInfo != null){
//                tAlgorithmConfBak.setAlgorithmId(tAlgorithmInfo.getAlgorithmId());
//                tAlgorithmConfBak.setDeviceMeteId(tStdDeviceMeteDetail.getDeviceMeteId());
//                tAlgorithmConfBakDao.add(tAlgorithmConfBak);
//            }
//        }
        return  1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceMeteId) {
        //删除测点配置的算法
        tAlgorithmConfBakDao.deleteByPrimaryId(deviceMeteId);
        TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(deviceMeteId);
        Long deviceId=tStdDeviceMete.getDeviceId();
        String customId=tStdDeviceMete.getCustomId();

        this.tStdDevicemeteDao.deleteByPrimaryId(deviceMeteId);//删除当前标准测点

//        if((tStdDevicemeteDao.selectDeviceMeteByDeviceCustom(deviceId,customId)).size()==0){//判断是否存在与被删除测点相同设备Id和部位Id的测点
////
//            if(tStdDevicemeteDao.selectByDevId(deviceId).size()==0){ //判断当前设备下是否有测点，若不存在则清空device并新增一个本体部位设备
////                List<TStdDevice> tStdDevices=tStdDeviceDao.selectByPrimaryId(deviceId);
//                tStdDeviceDao.deleteByUnionKeys(deviceId);
////                if(tStdDevices.size()==1){
////                tStdDevices.get(0).setCustomId("700");
////                tStdDevices.get(0).setCustomName("本体");
////                tStdDeviceDao.add(tStdDevices.get(0));
////                }
//            }else {                                               //若存在则删除与当前测点关联的设备信息
//                tStdDeviceDao.deleteByUnionKeys(deviceId);
//            }
////
//        }
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        //tCruisePointInstance.setDeviceMeteId(deviceMeteId);
        List<Long> haveList = tStdDevicemeteDao.selectHave(deviceMeteId);
        //删除关联的表
        if(haveList != null && haveList.size()>0){
            tCruisePointInstanceDao.deleteByInstanceId(haveList);//删除巡检点
            tCruisePlanAttrDao.deleteByInstanceId(haveList);
            //tCruiseTaskAttrDao.deleteByInstanceId(haveList);
            tCruiseTypeDao.deleteForInstanceId(haveList);
        }
        return this.tStdDevicemeteDao.deleteByPrimaryId(deviceMeteId);//删除测点
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDeviceMeteDetail tStdDeviceMeteDetail) {

        if(StringUtils.isNotBlank(tStdDeviceMeteDetail.getAlarmLevelString())) {
            String[] alarmLevelList = tStdDeviceMeteDetail.getAlarmLevelString().split(",");
            tStdDeviceMeteDetail.setAlarmLevel(Integer.parseInt(alarmLevelList[0]));
        }
        //修改测点配置的算法
        if ((Objects.nonNull(tStdDeviceMeteDetail.getAnalyseType())
                || Objects.equals("on",tStdDeviceMeteDetail.getIsAi())) && Objects.equals("on", tStdDeviceMeteDetail.getIsJudge())) {
            return 0;
        }
        //配置算法
        if(tStdDeviceMeteDetail.getAnalyseType() != null){
            TAlgorithmConfBak tAlgorithmConfBak  = new TAlgorithmConfBak();
            //若配置了算法，则判断数据库中是否存在当前数据，存在则传递edgeCode
            TAlgorithmConfBak    tAlgorithmMeteBack = tAlgorithmConfBakDao.selectByPrimaryId(tStdDeviceMeteDetail.getDeviceMeteId());
            tAlgorithmConfBakDao.deleteByPrimaryId(tStdDeviceMeteDetail.getDeviceMeteId());
            TAlgorithmInfo tAlgorithmInfo = tAlgorithmConfBakDao.selectByAnalyseType(String.valueOf(tStdDeviceMeteDetail.getAnalyseType()));
            if(tAlgorithmInfo != null){
                if (Objects.nonNull(tAlgorithmMeteBack)) {
                    tAlgorithmConfBak.setEdgeCode(tAlgorithmMeteBack.getEdgeCode());
                }
                tAlgorithmConfBak.setAlgorithmId(tAlgorithmInfo.getAlgorithmId());
                tAlgorithmConfBak.setDeviceMeteId(tStdDeviceMeteDetail.getDeviceMeteId());
                tAlgorithmConfBakDao.add(tAlgorithmConfBak);
            }
        }else {
            tAlgorithmConfBakDao.deleteByPrimaryId(tStdDeviceMeteDetail.getDeviceMeteId());
        }

        // 非表计类型时，meter_type字段需要清空
        if (!tStdDeviceMeteDetail.getMeteType().equals("221")) {
            tStdDeviceMeteDetail.setMeterType(null);
        }

        return this.tStdDevicemeteDao.update(tStdDeviceMeteDetail);
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceMete selectByPrimaryId(Long deviceMeteId) {
        return this.tStdDevicemeteDao.selectByPrimaryId(deviceMeteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> select(Long deviceMeteId, Long deviceId,String devicePointId,String cusomId,Long meteId, String meteKind,String meteType, String meteName, Integer deviceType, Integer inspectionType,  String positionType,Integer analyseType, String unit, String alarmNote, String alarmType, Float upEffect, Float downEffect, Integer alarmLevel, Float highLimit1, Float lowLimit1, Float highLimit2, Float lowLimit2, Integer alarmDelay, Integer alarmCnt, BigDecimal thresholdAbs, BigDecimal thresholdPer, Integer modulus, String remark,String stateZero,String stateOne,Integer  alarmState,Integer meterType,Integer appearanceType) {
        List<TStdDeviceMete> tStdDeviceMeteList = tStdDevicemeteDao.select(deviceMeteId, deviceId, devicePointId, cusomId, meteId, meteKind, meteType,meteName, deviceType, inspectionType, positionType,analyseType, unit, alarmNote, alarmType, upEffect, downEffect, alarmLevel, highLimit1, lowLimit1, highLimit2, lowLimit2, alarmDelay, alarmCnt, thresholdAbs, thresholdPer, modulus, remark,stateZero,stateOne,alarmState,meterType,appearanceType);
        return tStdDeviceMeteList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectForPage(TStdDeviceMeteDetail tStdDeviceMeteDetail){
        return tStdDevicemeteDao.selectForPage(tStdDeviceMeteDetail);
    }
    @Transactional(rollbackFor = Exception.class)
    public Result selectByPage(TStdDeviceMeteDetail tStdDeviceMeteDetail,List<Long> listForPage) {
        Result result = new Result();

        Integer pageNum = tStdDeviceMeteDetail.getPageNum();
        Integer pageSize = tStdDeviceMeteDetail.getPageSize();

        Integer isRedundant = tStdDeviceMeteDetail.getIsRedundant();

        Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
        List<TStdDeviceMeteDetail> list = tStdDevicemeteDao
            .selectByPage(tStdDeviceMeteDetail.getMeteName(), tStdDeviceMeteDetail.getDeviceId(), tStdDeviceMeteDetail.getRedundantType(),
                isRedundant, listForPage);

        fillDeviceMeteInfo(list);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("count", page.getTotal());
        resultMap.put("list",list);
        result.setData(resultMap);
        return result;
    }

    private void fillDeviceMeteInfo(List<TStdDeviceMeteDetail> list) {
        //填充字典值

        DictConvertUtil.DictOptional optional = DictConvertUtil
            .optional("alarmLevel")
            .add("analyseType")
            .add("alarmType")
            .add("customType")
            .add("meteType")
            .add("meteKind")
            .add("alarmLevel");
        DictConvertUtil.DICT.covertToDict(list,optional);


        for (TStdDeviceMeteDetail detail : list) {
            if (StringUtils.isEmpty(detail.getAnalyseTypeName())) {
                String analyseName = "on".equals(detail.getIsAi()) ? "缺陷" : "on".equals(detail.getIsJudge()) ? "判别" : "无";
                detail.setAnalyseTypeName(analyseName);
            }
        }

        //填充告警规则信息
        list.forEach(tStdDeviceMeteDetail -> {
            StringBuilder rules = new StringBuilder("--");
            if (Objects.nonNull(tStdDeviceMeteDetail.getMeteKind()) && tStdDeviceMeteDetail.getMeteKind() == 1) {
                rules = new StringBuilder(
                    (StringUtils.isNotBlank(tStdDeviceMeteDetail.getAlarmLevelName()) ? tStdDeviceMeteDetail.getAlarmLevelName() : ""));
                if (StringUtils.isNotBlank(rules.toString())) {
                    rules.append('：').append(tStdDeviceMeteDetail.getAlarmState() == 0 ?
                        (StringUtils.isNotBlank(tStdDeviceMeteDetail.getStateZero()) ? tStdDeviceMeteDetail.getStateZero() : "") :
                        (StringUtils.isNotBlank(tStdDeviceMeteDetail.getStateZero()) ? tStdDeviceMeteDetail.getStateZero() : ""));
                }
                else {
                    rules.append("--");
                }
            } else if (Objects.nonNull(tStdDeviceMeteDetail.getMeteKind()) && tStdDeviceMeteDetail.getMeteKind() == 2) {
                rules = new StringBuilder();
                String ruleStr = getRules(tStdDeviceMeteDetail);
                if (StringUtils.isNotEmpty(ruleStr)) {
                    rules.append(ruleStr);
                } else {
                    rules.append("--");
                }

            }
            tStdDeviceMeteDetail.setRules(rules.toString());
        });
    }

    /**
     * 拼接告警规则
     *
     * @param tStdDeviceMeteDetail tStdDeviceMeteDetail
     * @return result
     */
    private String getRules(TStdDeviceMeteDetail tStdDeviceMeteDetail) {
        String level130 = getOneLevelRules(AlarmLevelEnum.ALARM_LEVEL_130.getDictNote(), tStdDeviceMeteDetail.getLowLimit1(), tStdDeviceMeteDetail.getHighLimit1());
        String level131 = getOneLevelRules(AlarmLevelEnum.ALARM_LEVEL_131.getDictNote(), tStdDeviceMeteDetail.getLowLimit2(), tStdDeviceMeteDetail.getHighLimit2());
        String level132 = getOneLevelRules(AlarmLevelEnum.ALARM_LEVEL_132.getDictNote(), tStdDeviceMeteDetail.getLowLimit3(), tStdDeviceMeteDetail.getHighLimit3());
        String level133 = getOneLevelRules(AlarmLevelEnum.ALARM_LEVEL_133.getDictNote(), tStdDeviceMeteDetail.getLowLimit4(), tStdDeviceMeteDetail.getHighLimit4());

        StringBuilder rules = new StringBuilder();
        return rules.append(level130).append(level131).append(level132).append(level133).toString();
    }

    private String getOneLevelRules(String levelName, Float lowLimit, Float highLimit) {
        if (lowLimit != null && highLimit != null) {
            return String.format("%s：%s--%s；", levelName, lowLimit.toString(), highLimit.toString());
        } else {
            return "";
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdDeviceMete> list) {
        return this.tStdDevicemeteDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectDevMeteByModelId(Long modelId) {
        return tStdDevicemeteDao.selectDevMeteByModelId(modelId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDevMete(List<TStdDeviceMete> list) {
        Long deviceId = list.get(0).getDeviceId();
        tStdDevicemeteDao.deleteByDevId(deviceId);
        return this.tStdDevicemeteDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByDevId(Long deviceId) {
        return this.tStdDevicemeteDao.deleteByDevId(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectByDevCus(Long deviceId,String customType) {
        return this.tStdDevicemeteDao.selectByDevCus(deviceId, customType);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TStdDeviceMete> selectPreDeviceMete(Long modelId,Long deviceId,Long customType){
        return this.tStdDevicemeteDao.selectPreDeviceMete(modelId,  deviceId, customType);
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String list) {
        int deleteCount=0;
        List<String> list1= Arrays.asList(list.split(","));
        for (String item:list1) {
            tAlgorithmConfBakDao.deleteByPrimaryId(Long.valueOf(item));
            List<Long> haveList = tStdDevicemeteDao.selectHave(Long.valueOf(item));
            tCruisePointInstanceDao.deleteByDeviceMeteId(Long.valueOf(item));//遍历删除巡检点
            //删除关联的表
            if(haveList != null && haveList.size()>0){
                tCruisePlanAttrDao.deleteByInstanceId(haveList);
                //tCruiseTaskAttrDao.deleteByInstanceId(haveList);
                tCruiseTypeDao.deleteForInstanceId(haveList);
            }

            TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(Long.valueOf(item));
            log.info("deviceMete---------------------"+tStdDeviceMete);
            Long deviceId=tStdDeviceMete.getDeviceId();
            String customId=tStdDeviceMete.getCustomId();
//            TStdDeviceMete tStdDeviceMete=tStdDevicemeteDao.selectByPrimaryId(Long.valueOf(item));//根据devicemeteId查测点
//            Long deviceId=tStdDeviceMete.getDeviceId();
//            String customId=tStdDeviceMete.getCustomId();
            deleteCount=tStdDevicemeteDao.deleteByPrimaryId(Long.valueOf(item))+deleteCount;

            if((tStdDevicemeteDao.selectDeviceMeteByDeviceCustom(deviceId,customId)).size()==0){//判断是否存在与被删除测点相同设备Id和部位Id的测点
//
                if(tStdDevicemeteDao.selectByDevId(deviceId).size()==0){ //判断当前设备下是否有测点，若不存在则清空device并新增一个本体部位设备
                    List<TStdDevice> tStdDevices=tStdDeviceDao.selectByPrimaryId(deviceId);
                    tStdDeviceDao.deleteByUnionKeys(deviceId);
//                    if(tStdDevices.size()==1){
//                        tStdDevices.get(0).setCustomId("700");
//                        tStdDevices.get(0).setCustomName("本体");
//                        tStdDeviceDao.add(tStdDevices.get(0));
//                    }
                }else {                                               //若存在则删除与当前测点关联的设备信息
                    tStdDeviceDao.deleteByUnionKeys(deviceId);
                }
//
            }

//            if((tStdDevicemeteDao.selectDeviceMeteByDeviceCustom(deviceId,customId)).size()==0){
//                tStdDeviceDao.deleteByUnionKeys(deviceId,customId);
//            }

        }
        return deleteCount;//批量删除标准测点
    }


}

