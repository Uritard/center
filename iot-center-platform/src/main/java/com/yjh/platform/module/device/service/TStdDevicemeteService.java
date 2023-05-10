package com.yjh.platform.module.device.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.yjh.platform.common.enums.AlarmLevelEnum;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.module.device.dao.*;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTypeDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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

    @Autowired
    private LinkAutoMapper linkAutoMapper;


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
    /**
     * 新建测点
     *
     * @param buildList 模型巡视点列表
     */
    public void importExcel(List<ExcelEntity> buildList)  {
        log.info("开始执行导入:{}", new Date());
        //取出当前根节点
        Long rootId = linkAutoMapper.selectRoot();

        //取出所有区域
        Set<String> regionNameSet = buildList.stream().map(ExcelEntity::getAreaName).collect(Collectors.toSet());
        //查询已存在区域
        List<TStdRegion> tStdRegionList = linkAutoMapper.listTStdRegionByParentIdAndNameList(rootId, regionNameSet);
        //处理不存在区域
        if (CollectionUtils.isNotEmpty(tStdRegionList)) {
            tStdRegionList.forEach(o -> regionNameSet.remove(o.getRegionName()));
        }

        if (CollectionUtils.isNotEmpty(regionNameSet)) {
            List<TStdRegion> insertList = new ArrayList<>(regionNameSet.size());
            regionNameSet.forEach(o -> {
                TStdRegion tStdRegion = new TStdRegion();
                tStdRegion.setUpRegionId(rootId);
                tStdRegion.setRegionName(o);
                tStdRegion.setState(1);
                insertList.add(tStdRegion);
            });
            linkAutoMapper.batchInsertRegion(insertList);
            //将新增区域放入list
            tStdRegionList.addAll(insertList);
        }

        //替换区域Id
        Map<String, TStdRegion> areaNameToId =
            tStdRegionList.stream().collect(Collectors.toMap(TStdRegion::getRegionName, Function.identity()));
        buildList.forEach(o -> o.setAreaId(String.valueOf(areaNameToId.get(o.getAreaName()).getRegionId())));
        Multimap<Long, ExcelEntity> areaToBayMap = HashMultimap.create();
        buildList.forEach(o -> areaToBayMap.put(Long.valueOf(o.getAreaId()), o));

        Set<Long> areaIdSet = areaToBayMap.keySet();
        List<ExcelEntity> buildListAfterRegion = new LinkedList<>();
        List<TStdRegion> totalRegion = new ArrayList<>();
        for (Long areaId : areaIdSet) {
            List<ExcelEntity> sameAreaList = new ArrayList<>(areaToBayMap.get(areaId));
            Set<String> bayNameSet = sameAreaList.stream().map(ExcelEntity::getRegionName).collect(Collectors.toSet());
            List<TStdRegion> existsBayList = linkAutoMapper.selectByRegionNameEqual(areaId, bayNameSet);

            existsBayList.forEach(tStdRegion -> bayNameSet.remove(tStdRegion.getRegionName()));
            List<TStdRegion> insertList = new ArrayList<>(bayNameSet.size());
            if (CollectionUtils.isNotEmpty(bayNameSet)) {
                bayNameSet.forEach(o -> {
                    TStdRegion tStdRegion = new TStdRegion();
                    tStdRegion.setUpRegionId(areaId);
                    tStdRegion.setRegionName(o);
                    tStdRegion.setState(1);
                    insertList.add(tStdRegion);
                });
                linkAutoMapper.batchInsertRegion(insertList);
                existsBayList.addAll(insertList);
            }

            Map<String, TStdRegion> bayNameToBayToId =
                existsBayList.stream().collect(Collectors.toMap(TStdRegion::getRegionName, Function.identity()));

            sameAreaList.forEach(ExcelEntity -> {
                ExcelEntity.setRegionId(String.valueOf(bayNameToBayToId.get(ExcelEntity.getRegionName()).getRegionId()));
            });

            buildListAfterRegion.addAll(sameAreaList);
            totalRegion.addAll(existsBayList);
        }
        Map<Long, TStdRegion> idToName = totalRegion.stream().collect(Collectors.toMap(TStdRegion::getRegionId, Function.identity()));

        Multimap<Long, ExcelEntity> multimap = HashMultimap.create();
        buildListAfterRegion.forEach(o -> multimap.put(Long.valueOf(o.getRegionId()), o));

        Set<Long> set = multimap.keySet();

        List<TStdDevice> totalStdDevices = new ArrayList<>();
        List<TStdDevice> insertDevices = new ArrayList<>();

        List<ExcelEntity> buildListAfterDevice = new LinkedList<>();
        for (Long regionId : set) {
            List<ExcelEntity> excelEntities = new ArrayList<>(multimap.get(regionId));
            Set<String> deviceNames = new HashSet<>();
            Map<String, String> map = excelEntities.stream().filter(o -> deviceNames.add(o.getMainDeviceName()))
                .collect(Collectors.toMap(ExcelEntity::getMainDeviceName, ExcelEntity::getDeviceTypeId));
            deviceNames.clear();
            Map<String, String> nameToMainId = excelEntities.stream().filter(o -> deviceNames.add(o.getMainDeviceName()))
                .collect(Collectors.toMap(ExcelEntity::getMainDeviceName, ExcelEntity::getMainDeviceId));
            List<TStdDevice> tStdDevices = linkAutoMapper.selectByDeviceNameEqual(regionId, deviceNames);
            if (CollectionUtils.isNotEmpty(tStdDevices)) {
                tStdDevices.forEach(o -> o.setMainDeviceId(nameToMainId.get(o.getDeviceName())));
                tStdDevices.forEach(tStdDevice -> deviceNames.remove(tStdDevice.getDeviceName()));
                totalStdDevices.addAll(tStdDevices);
            }
            if (CollectionUtils.isNotEmpty(deviceNames)) {
                deviceNames.forEach(deviceName -> {
                    Integer deviceType = Integer.parseInt(map.get(deviceName));
                    TStdDevice tStdDevice = new TStdDevice();
                    tStdDevice.setUpRegionId(regionId);
                    tStdDevice.setMainDeviceId(nameToMainId.get(deviceName));
                    tStdDevice.setDeviceName(deviceName);
                    tStdDevice.setDeviceType(deviceType);
                    tStdDevice.setUpRegionName(idToName.get(regionId).getRegionName());
                    insertDevices.add(tStdDevice);
                    TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();
                    tStdDeviceAttr.setUsedTime(new Date());
                    tStdDevice.setTStdDeviceAttr(tStdDeviceAttr);

                });
            }
            excelEntities.forEach(o -> o.setMainDeviceId(String.valueOf(nameToMainId.get(o.getMainDeviceName()))));
            buildListAfterDevice.addAll(excelEntities);
        }
        if (CollectionUtils.isNotEmpty(insertDevices)) {
            linkAutoMapper.batchInsertDevice(insertDevices);
            List<TStdDeviceAttr> tStdDeviceAttrs = insertDevices.stream().map(o -> {
                o.getTStdDeviceAttr().setDeviceId(o.getDeviceId());
                return o.getTStdDeviceAttr();
            }).collect(Collectors.toList());
            linkAutoMapper.batchInsertDeviceAttr(tStdDeviceAttrs);
            totalStdDevices.addAll(insertDevices);

        }

        Set<String> strings = new HashSet<>();

        Map<String, Long> stringLongMap = totalStdDevices.stream().filter(o -> strings.add(o.getMainDeviceId()))
            .collect(Collectors.toMap(TStdDevice::getMainDeviceId, TStdDevice::getDeviceId));

        buildListAfterDevice.forEach(o -> o.setDeviceId(stringLongMap.get(o.getMainDeviceId())));

        Multimap<Long, ExcelEntity> multiMapForDevice = HashMultimap.create();
        buildListAfterDevice.forEach(o -> multiMapForDevice.put(o.getDeviceId(), o));
        List<TStdDeviceMete> totalMete = new ArrayList<>();
        multiMapForDevice.keySet().forEach(deviceId -> {
            List<ExcelEntity> entityList = new ArrayList<>(multiMapForDevice.get(deviceId));
            Set<String> meteNames = entityList.stream().map(ExcelEntity::getMeteName).collect(Collectors.toSet());
            List<TStdDeviceMete> tStdDeviceMetes = linkAutoMapper.selectByMeteNameEqual(deviceId, meteNames);
            Map<String, TStdDeviceMete> tStdDeviceMeteMap =
                tStdDeviceMetes.stream().collect(Collectors.toMap(TStdDeviceMete::getMeteName, Function.identity()));

            entityList.forEach(excelEntity -> {
                if (!tStdDeviceMeteMap.containsKey(excelEntity.getMeteName())) {
                    TStdDeviceMete tStdDeviceMete = new TStdDeviceMete();
                    tStdDeviceMete.setDeviceId(excelEntity.getDeviceId());
                    tStdDeviceMete.setDevicePointId(String.valueOf(excelEntity.getMeteId()));
                    tStdDeviceMete.setDeviceType(Integer.valueOf(excelEntity.getDeviceTypeId()));
                    tStdDeviceMete.setCustomId(excelEntity.getCustomId());
                    tStdDeviceMete.setCustomName(excelEntity.getCustomName());
                    tStdDeviceMete.setMeterType(excelEntity.getMeterTypeId());
                    tStdDeviceMete.setMeteType(excelEntity.getMeteTypeId());
                    tStdDeviceMete.setMeteName(excelEntity.getMeteName());
                    tStdDeviceMete.setMeteKind(excelEntity.getMeteKindId());
                    //默认字段
                    tStdDeviceMete.setPositionType(excelEntity.getPositionType());
                    tStdDeviceMete.setIsAi(excelEntity.getIsAi());
                    tStdDeviceMete.setIsJudge(excelEntity.getIsJudge());
                    if (Objects.nonNull(excelEntity.getAlarmLevel())) {
                        Integer alarmLevel = excelEntity.getAlarmLevel();
                        tStdDeviceMete.setAlarmLevel(alarmLevel);
                        StringJoiner stringJoiner   = new StringJoiner(",");
                        for(int i = alarmLevel; i <= 133;i++) { 
                            stringJoiner.add(String.valueOf(i));
                        }
                        tStdDeviceMete.setAlarmLevelString(stringJoiner.toString());
                    }
                    tStdDeviceMete.setHighLimit1(excelEntity.getHighLimit1() == null ? null : NumberUtils.toFloat(excelEntity.getHighLimit1()));
                    tStdDeviceMete.setHighLimit2(excelEntity.getHighLimit2() == null ? null : NumberUtils.toFloat(excelEntity.getHighLimit2()));
                    tStdDeviceMete.setHighLimit3(excelEntity.getHighLimit3() == null ? null : NumberUtils.toFloat(excelEntity.getHighLimit3()));
                    tStdDeviceMete.setHighLimit4(excelEntity.getHighLimit4() == null ? null : NumberUtils.toFloat(excelEntity.getHighLimit4()));
                    tStdDeviceMete.setLowLimit1(excelEntity.getLowLimit1() == null ? null : NumberUtils.toFloat(excelEntity.getLowLimit1()));
                    tStdDeviceMete.setLowLimit2(excelEntity.getLowLimit2() == null ? null : NumberUtils.toFloat(excelEntity.getLowLimit2()));
                    tStdDeviceMete.setLowLimit3(excelEntity.getLowLimit3() == null ? null : NumberUtils.toFloat(excelEntity.getLowLimit3()));
                    tStdDeviceMete.setLowLimit4(excelEntity.getLowLimit4() == null ? null : NumberUtils.toFloat(excelEntity.getLowLimit4()));
                    tStdDeviceMete.setUnit(excelEntity.getUnit());
                    tStdDeviceMete.setAlarmNote(excelEntity.getAlarmNote());
                    tStdDeviceMete.setAlarmState(0);
                    tStdDeviceMete.setRedundantType(excelEntity.getRedundantType());
                    tStdDeviceMete.setIsTemdif(0);
                    totalMete.add(tStdDeviceMete);
                }
            });
        });
        if (CollectionUtils.isNotEmpty(totalMete)) {
            linkAutoMapper.insertDeviceMete(totalMete);
        }
    }

}

