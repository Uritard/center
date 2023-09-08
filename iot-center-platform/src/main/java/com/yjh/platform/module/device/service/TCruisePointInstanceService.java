package com.yjh.platform.module.device.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.controller.TCruisePointInstanceController;
import com.yjh.platform.module.device.dao.*;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.patrol.dao.UPatrolPlanAttrDao;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTypeDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.video.service.CameraConService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author tt
 * @since 2020-08-08
 */
@Service
public class TCruisePointInstanceService{

    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;

    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;

    @Autowired
    private TCruisePlanAttrDao tCruisePlanAttrDao;

    @Autowired
    private TCameraPresetDao tCameraPresetDao;

    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;

    @Autowired
    private TStdDeviceDao tStdDeviceDao;

    @Autowired
    private TCruisePointAttrDao tCruisePointAttrDao;
    @Autowired
    private TCruiseTypeDao tCruiseTypeDao;

    @Autowired
    private TVoiceDeviceDao tVoiceDeviceDao;
    @Autowired
    private UPatrolResultDao uPatrolResultDao;
    @Autowired
    private UPatrolPlanAttrDao uPatrolPlanAttrDao;
    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;

    @Autowired
    private TCameraScreenDao tCameraScreenDao;
    @Resource
    private CameraConService cameraConService;

    private Logger log = LoggerFactory.getLogger(TCruisePointInstanceController.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruisePointInstance tCruisePointInstance) {
        return this.tCruisePointInstanceDao.insert(tCruisePointInstance);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long instanceId) {
        return this.tCruisePointInstanceDao.deleteByPrimaryId(instanceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(TCruisePointInstance tCruisePointInstance) {
        return this.tCruisePointInstanceDao.delete(tCruisePointInstance);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePointInstance tCruisePointInstance) {
        return this.tCruisePointInstanceDao.update(tCruisePointInstance);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruisePointInstance selectByPrimaryId(Long instanceId) {
        return this.tCruisePointInstanceDao.selectByPrimaryId(instanceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> select(Long instanceId, Long deviceMeteId, String stationId, String stationName, Long deviceId, String customId, String dataFormat, Integer identifyType, Integer identifySonType, Integer cruiseType, Long cruiseId, String cruiseName, String cruiseContent, String positionType, String unit, Integer ifSy, Integer syType, Integer ifVideotape, String videotapeTime, String textDesc, String sort) {
        List<TCruisePointInstance> tCruisePointInstanceList = tCruisePointInstanceDao.select(instanceId, deviceMeteId, stationId, stationName, deviceId, customId, dataFormat, identifyType, identifySonType, cruiseType, cruiseId, cruiseName, cruiseContent, positionType, unit, ifSy, syType, ifVideotape, videotapeTime, textDesc, sort);
        return tCruisePointInstanceList;
    }

    private void getConForCruisePoint(Integer length,TCruisePointByPageDetail tStdDeviceMeteForPointDetailItem,List<TCruisePointByPageDetail> listAll){
        //机器人
        String robotTypeNum = "228";
        //视频 红外
        String cameraTypeNum = "229";
        //声纹
        String voiceTypeNum = "232";
        //无人机
        String droneTypeNum = "524";
        if(robotTypeNum.equals(tStdDeviceMeteForPointDetailItem.getCruiseType())) {
            listAll.get(length).getRobotType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map = new HashMap<>();
            map.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getRobotType().getCruiseIdList().add(tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getRobotType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
        //视频 红外
        if(cameraTypeNum.equals(tStdDeviceMeteForPointDetailItem.getCruiseType()) || "230".equals(tStdDeviceMeteForPointDetailItem.getCruiseType())) {
                listAll.get(length).getCameraType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
                Map<Object,Object> map1 = new HashMap<>();
                map1.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
                listAll.get(length).getCameraType().getCruiseIdList().add(tStdDeviceMeteForPointDetailItem.getCruiseId());
                listAll.get(length).getCameraType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
//        if("230".equals(tStdDeviceMeteForPointDetailItem.getCruiseTypeName())) {
//            listAll.get(length).getInfraredType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
//            Map<Object,Object> map2 = new HashMap<>();
//            map2.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
//            listAll.get(length).getInfraredType().getList().add(map2);
//            listAll.get(length).getInfraredType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
//        }
        //在线监控  内容未作
//        if("231".equals((tStdDeviceMeteForPointDetailItem.getCruiseType()))) {
//            listAll.get(length).getVoiceType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
//            Map<Object,Object> map3 = new HashMap<>();
//            map3.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
//            listAll.get(length).getVoiceType().getList().add(map3);
//            listAll.get(length).getVoiceType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
//        }
//        //scada 内容未作
        if(voiceTypeNum.equals((tStdDeviceMeteForPointDetailItem.getCruiseType()))) {
            listAll.get(length).getVoiceType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().getCruiseIdList().add(tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
        if(droneTypeNum.equals((tStdDeviceMeteForPointDetailItem.getCruiseType()))) {
            listAll.get(length).getDroneType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getDroneType().getCruiseIdList().add(tStdDeviceMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getDroneType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
        }
    }
    private int isIn(List<TStdDeviceMeteForPointDetail> listAll,Long deviceMeteId){
        int i = -1;
        for (int j = 0; j < listAll.size(); j++) {
            if(listAll.get(j).getDeviceMeteId().equals(deviceMeteId)){
                return j;
            }
        }
        return i;
    }
//    @Transactional(rollbackFor = Exception.class)
//    public Result selectCruisePointByPage2(TStdDeviceMete tStdDeviceMete,int pageNum,@RequestParam(value = "pageSize",required = false,defaultValue = "0") int pageSize) {
//        Result result = new Result();
//        Map<String, Object> resultMap = new HashMap<>();
//        try {
//            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
//            List<Long> listForPage = tCruisePointInstanceDao.selectForCruiseByPage(tStdDeviceMete);
//            resultMap.put("count", page.getTotal());
//            List<TStdDeviceMeteForPointDetail> listAll = new LinkedList<>();
//            if(listForPage != null && listForPage.size()!=0) {
//                List<TStdDeviceMeteForPointDetail> list = tCruisePointInstanceDao.selectCruisePointByPage(tStdDeviceMete);
//                listAll.add(list.get(0));
//                if (list.get(0).getCruiseType() != null) {
//                    getConForCruisePoint(0, list.get(0), listAll);
//                }
//                list.remove(0);
//                int size = 0;
//                for (TStdDeviceMeteForPointDetail tStdDeviceMeteForPointDetailItem : list) {
//                    int length = isIn(listAll,tStdDeviceMeteForPointDetailItem.getDeviceMeteId());
//                    if (length != -1) {
//                        getConForCruisePoint(length, tStdDeviceMeteForPointDetailItem, listAll);
//                    } else {
//                        listAll.add(tStdDeviceMeteForPointDetailItem);
//                        getConForCruisePoint(listAll.size()-1, tStdDeviceMeteForPointDetailItem, listAll);
//                    }
//                    size++;
//                }
//            }
//            resultMap.put("list", listAll);
//            result.setData(resultMap);
//        }catch (Exception e) {
//            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
//            log.error("巡检点实例分页查询失败描述：", e);
//        }
//        return result;
//    }
    /*
    * 过滤掉A数组中不在数组B中的元素
    */
    private List<Long> listFilter(List<Long> listA,List<Long> listB){
        List<Long> listC = new ArrayList<>();
        for (Long element : listA) {
            if (listB.contains(element)) {
                listC.add(element);
            }
        }
        return listC;
    }

    public Result selectCruisePointByPage(TStdDeviceMete tStdDeviceMete,int pageNum,@RequestParam(value = "pageSize",required = false,defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<Long> listForPage = tCruisePointInstanceDao.selectByBindNew(tStdDeviceMete);
            resultMap.put("count", page.getTotal());
            List<TCruisePointByPageDetail> listAll = new LinkedList<>();
            if(listForPage != null && listForPage.size()!=0) {
                List<TCruisePointByPageDetail> list = tCruisePointInstanceDao.selectCruisePointByPage(tStdDeviceMete.getMeteName(),tStdDeviceMete.getDeviceId(),
                        tStdDeviceMete.getCustomId(),tStdDeviceMete.getPageSize(),listForPage);
                if(list != null && list.size()>1){
                    if(list.get(0).getDeviceMeteId() != list.get(1).getDeviceMeteId()){
                        listAll.add(list.get(0));
                        getConForCruisePoint(0, list.get(0), listAll);
                    }else {
                        listAll.add(list.get(0));
                        getConForCruisePoint(0, list.get(0), listAll);
                    }
                    for(int i=1;i<list.size();i++){
                        if(list.get(i).getDeviceMeteId().equals(list.get(i-1).getDeviceMeteId())){
                            getConForCruisePoint(listAll.size()-1, list.get(i), listAll);
                        }else {
                            listAll.add(list.get(i));
                            getConForCruisePoint(listAll.size()-1, list.get(i), listAll);
                        }
                    }
                }else if(list.size()==1){
                        listAll.add(list.get(0));
                    getConForCruisePoint(0, list.get(0), listAll);
                }
            }
            resultMap.put("list", listAll);
            //log.info("listAll"+listAll.get(100));
            result.setData(resultMap);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例分页查询失败描述：", e);
        }
        return result;
    }

    private void getConSYForCruisePoint(Integer length,TCfgMeteForPointDetail tCfgMeteForPointDetailItem,List<TCfgMeteForPointDetail> listAll){
        //机器人
        if("228".equals(tCfgMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getRobotType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map = new HashMap<>();
            map.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getRobotType().getList().add(map);
            listAll.get(length).getRobotType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
        //视频 红外
        if("229".equals(tCfgMeteForPointDetailItem.getCruiseTypeName())) {
            listAll.get(length).getCameraType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map1 = new HashMap<>();
            map1.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getCameraType().getList().add(map1);
            listAll.get(length).getCameraType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
//        if("红外巡检".equals(tStdDeviceMeteForPointDetailItem.getCruiseTypeName())) {
//            listAll.get(length).getInfraredType().setCruiseType(tStdDeviceMeteForPointDetailItem.getCruiseType());
//            Map<Object,Object> map2 = new HashMap<>();
//            map2.put("cruiseId",tStdDeviceMeteForPointDetailItem.getCruiseId());
//            listAll.get(length).getInfraredType().getList().add(map2);
//            listAll.get(length).getInfraredType().setCruiseTypeName(tStdDeviceMeteForPointDetailItem.getCruiseTypeName());
//        }
        //在线监控  内容未作
        if("231".equals((tCfgMeteForPointDetailItem.getCruiseTypeName()))) {
            listAll.get(length).getVoiceType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().getList().add(map3);
            listAll.get(length).getVoiceType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
        //scada 内容未作
        if("232".equals((tCfgMeteForPointDetailItem.getCruiseTypeName()))) {
            listAll.get(length).getVoiceType().setCruiseType(tCfgMeteForPointDetailItem.getCruiseType());
            Map<Object,Object> map3 = new HashMap<>();
            map3.put("cruiseId",tCfgMeteForPointDetailItem.getCruiseId());
            listAll.get(length).getVoiceType().getList().add(map3);
            listAll.get(length).getVoiceType().setCruiseTypeName(tCfgMeteForPointDetailItem.getCruiseTypeName());
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public Result selectSYCruisePointByPage(TCfgMeteForPointDetail tCfgMeteForPointDetail,int pageNum,int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<String> listForPage = tCruisePointInstanceDao.selectSYForCruiseByPage(tCfgMeteForPointDetail);
            resultMap.put("count", page.getTotal());
            List<TCfgMeteForPointDetail> listAll = new LinkedList<>();
            if(listForPage.size() != 0) {
                List<TCfgMeteForPointDetail> list = tCruisePointInstanceDao.selectSYCruisePointByPage(listForPage);
                listAll.add(list.get(0));
                if (list.get(0).getCruiseType() != null) {
                    getConSYForCruisePoint(0, list.get(0), listAll);
                }
                list.remove(0);
                for (TCfgMeteForPointDetail tCfgMeteForPointDetailItem : list) {
                    int length = listAll.size() - 1;
                    if (tCfgMeteForPointDetailItem.getMeteId().equals(listAll.get(length).getMeteId())) {
                        getConSYForCruisePoint(length, tCfgMeteForPointDetailItem, listAll);
                    } else {
                        listAll.add(tCfgMeteForPointDetailItem);
                        getConSYForCruisePoint(length + 1, tCfgMeteForPointDetailItem, listAll);
                    }
                }
            }
            resultMap.put("list", listAll);
            result.setData(resultMap);
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("巡检点实例分页查询失败描述：", e);
        }
        return result;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> selectByPage(TCruisePointInstance tCruisePointInstance) {
        List<TCruisePointInstance> list = tCruisePointInstanceDao.selectByPage(tCruisePointInstance);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruisePointInstance> list) {
        return this.tCruisePointInstanceDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePointInstance> StdMeteUnionInspectionId(Long deviceId) {
        List<TCruisePointInstance> tCruisePointInstanceList = tCruisePointInstanceDao.StdMeteUnionInspectionId(deviceId);
        return tCruisePointInstanceList;
    }


    @Transactional(rollbackFor = Exception.class)
    public int instanceUpdate(TCruisePointInstanceDetail tCruisePointInstanceDetail) {
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(tCruisePointInstanceDetail.getDeviceMeteId());
        tCruisePointInstance.setDeviceId(tCruisePointInstanceDetail.getDeviceId());
        tCruisePointInstance.setCustomId(tCruisePointInstanceDetail.getCustomId());
        tCruisePointInstance.setCruiseType(tCruisePointInstanceDetail.getCruiseType());
        tCruisePointInstance.setIfSy(1);
        tCruisePointInstance.setUnit(tCruisePointInstanceDetail.getUnit());
        tCruisePointInstance.setPositionType(tCruisePointInstanceDetail.getPositionType());
        TStdRegion tStdRegion = tCruisePointInstanceDao.selectTSRegionForStation();
        tCruisePointInstance.setStationId(tStdRegion.getStationId());
        tCruisePointInstance.setStationName(tStdRegion.getStationName());
        List<Long> cruiseIdList = tCruisePointInstanceDao.selectCruiseId(tCruisePointInstanceDetail);//已经有了的巡检点
        Integer cruiseType = tCruisePointInstanceDetail.getCruiseType();
        // 如果是可见光或者是红外需要将可见光和红外的巡检点全部查出来
        if (cruiseType == 229 || cruiseType == 230) {
            int ctype = cruiseType == 229 ? 230 : 229;
            tCruisePointInstanceDetail.setCruiseType(ctype);
            List<Long> otherCruiseIdList = tCruisePointInstanceDao.selectCruiseId(tCruisePointInstanceDetail);
            if (CollectionUtils.isNotEmpty(otherCruiseIdList)) {
                cruiseIdList.addAll(otherCruiseIdList);
            }
        }
        int result = -1;

        //巡检点配置
        if (tCruisePointInstanceDetail.getIds().size() != 0) {
            List<TCruisePointInstance> list = new ArrayList<>();
            List<Long> paramIds = new ArrayList<>();
            for (Long id : tCruisePointInstanceDetail.getIds()) {
                if (cruiseIdList != null && cruiseIdList.size() > 0) {
                    if (cruiseIdList.contains(id)) {
                        cruiseIdList.remove(id);
                        continue;
                    }
                }
                paramIds.add(id);
                //插入信息关联表
//                TCruisePointInfo tCruisePointInfo = new TCruisePointInfo();
//                tCruisePointInfo.setInstanceId(tCruisePointInstance.getInstanceId());
//                tCruisePointInfo.setDeviceMeteId(tCruisePointInstance.getDeviceMeteId());
//                tCruisePointInstanceDao.insertInstanceInfo(tCruisePointInfo);

            }

            if (CollectionUtils.isNotEmpty(paramIds)) {
                Map<Long, String> nameMap = Maps.newHashMap();
                if (cruiseType == 229 || cruiseType == 230) {//视频
                    List<TCameraPreset> tCameraPreset = tCameraPresetDao.selectByPrimaryIds(paramIds);
                    nameMap = tCameraPreset.stream().collect(Collectors.toMap(TCameraPreset::getPresetId, TCameraPreset::getPresetName));
                }
                if (cruiseType == 228 || cruiseType == 524) {//机器人或无人机
//                            TRobotInspection tRobotInspection = tRobotInspectionDao.selectByPrimaryId(id);
                    List<TRobotInspectionTmp> tRobotInspection = tRobotInspectionDao.selectTRobotInspectionByIds(paramIds);
                    nameMap = tRobotInspection.stream().collect(Collectors.toMap(TRobotInspectionTmp::getInspectionId, TRobotInspectionTmp::getInspectionName));
                }
                if (cruiseType == 232) {//声纹
                    //声纹是一个测点对应一个巡视设备
                    List<VoiceDeviceAllInfoDetail> voiceDeviceAllInfoDetail = tVoiceDeviceDao.selectByPrimaryIds(paramIds);
                    nameMap = voiceDeviceAllInfoDetail.stream().collect(Collectors.toMap(VoiceDeviceAllInfoDetail::getVoiceDeviceId, VoiceDeviceAllInfoDetail::getVoiceDeviceName));
                }

                for (Long id : paramIds) {
                    TCruisePointAttr tCruisePointAttr = new TCruisePointAttr();
                    tCruisePointInstance.setCruiseId(id);
                    tCruisePointInstance.setCruiseName(nameMap.get(id));
                    TCruisePointInstance tCruisePointInstance1 = new TCruisePointInstance();
                    BeanUtils.copyProperties(tCruisePointInstance,tCruisePointInstance1);
                    list.add(tCruisePointInstance1);
                    tCruisePointAttr.setInstanceName(tCruisePointInstanceDetail.getMeteName() + "/" + nameMap.get(id));
                    tCruisePointAttr.setInstanceId(tCruisePointInstance.getInstanceId());

                }

            }

            if (CollectionUtils.isNotEmpty(list)) {
                if (list.size() > 1000) {
                    List<List<TCruisePointInstance>> lists = Lists.partition(list, 1000);
                    ExecutorService executorService = Executors.newFixedThreadPool(lists.size());
                    lists.forEach(subList ->
                            executorService.submit(() -> {
                                tCruisePointInstanceDao.batchInsert(subList);
                            }));
                    executorService.shutdown();
                } else {
                    tCruisePointInstanceDao.batchInsert(list);
                }
            }
        }
        //删除关联表的巡检实例
        if (cruiseIdList != null && cruiseIdList.size() > 0) {
            List<Long> instanceIdList = tCruisePointInstanceDao.selectInstanceId(cruiseIdList, tCruisePointInstanceDetail.getDeviceMeteId());
//            List<Long> instancePlanList = tCruisePlanAttrDao.batchSelectAttr(instanceIdList);
            List<Long> instancePlanList = uPatrolResultDao.batchSelectAttr(instanceIdList);
            if (instancePlanList.size() > 0) {
                result = -3;
                return result;
            }
            tCruisePointInstanceDao.deleteByInstanceId(instanceIdList);
            tCruisePlanAttrDao.deleteByInstanceId(instanceIdList);
            uPatrolPlanAttrDao.deleteByInstanceId(instanceIdList);
            tCruiseTypeDao.deleteForInstanceId(instanceIdList);
        }
        return result;
    }
    @Transactional(rollbackFor = Exception.class)
    public int warnInspectUpdate(TCruisePointInstanceDetail tCruisePointInstanceDetail){
        TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
        tCruisePointInstance.setDeviceMeteId(tCruisePointInstanceDetail.getDeviceMeteId());
        tCruisePointInstance.setDeviceId(tCruisePointInstanceDetail.getDeviceId());

        tCruisePointInstance.setCruiseType(tCruisePointInstanceDetail.getCruiseType());
        tCruisePointInstance.setIfSy(0);
        tCruisePointInstance.setSyType(tCruisePointInstanceDetail.getMeteKind());
        tCruisePointInstance.setUnit(tCruisePointInstanceDetail.getUnit());
        tCruisePointInstance.setStationId(tCruisePointInstanceDetail.getStationId());
        tCruisePointInstance.setStationName(tCruisePointInstanceDetail.getStationName());
        this.delete(tCruisePointInstance);
        Integer cruiseType = tCruisePointInstanceDetail.getCruiseType();
        int result = -1;
        if (tCruisePointInstanceDetail.getIds().size()!=0){
            for (Long id:tCruisePointInstanceDetail.getIds()) {
                if(cruiseType == 229){//视频
                    TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(id);
                    tCruisePointInstance.setCruiseId(id);
                    tCruisePointInstance.setCruiseName(tCameraPreset.getPresetName());
                }
                if(cruiseType == 228){//机器人
//                    TRobotInspection tRobotInspection = tRobotInspectionDao.selectByPrimaryId(id);
                    TRobotInspectionTmp tRobotInspection = tRobotInspectionDao.selectTRobotInspectionTmp(id);
                    tCruisePointInstance.setCruiseId(id);
                    tCruisePointInstance.setCruiseName(tRobotInspection.getInspectionName());
                }
//                if(cruiseType == 230){//红外
//                    TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(id);
//                    tCruisePointInstance.setCruiseId(id);
//                    tCruisePointInstance.setCruiseName(tCameraPreset.getPresetName());
//                }
                if(cruiseType == 232){//声纹
                    List<TStdDevice> tStdDevice = tStdDeviceDao.selectByPrimaryId(id);
                    tCruisePointInstance.setCruiseName(tStdDevice.get(0).getDeviceName());
                }
                //231 在线监控 232 声纹
                result =  tCruisePointInstanceDao.insert(tCruisePointInstance);
            }
        }
        return result;
    }




    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> selectCruiseCount(String taskId){
        Map<String,Integer>map=new HashMap<>();
        map.put("Count",this.tCruisePointInstanceDao.selectCruiseCount(taskId));
        map.put("Count",uPatrolResultDao.selectCruiseCount(taskId));
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CruiseCountOfType> selectCruiseCountByType(String taskId){
//        return this.tCruisePointInstanceDao.selectCruiseCountByType(taskId);
        return this.uPatrolResultDao.selectCruiseCountByType(taskId);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfoDeviceId> selectCameraByDeviceMeteId(Long deviceMeteId) {
        TStdDeviceMete devicemete = tStdDevicemeteDao.selectByPrimaryId(deviceMeteId);
        if (devicemete == null) {
            throw new BusinessException("测点已不存在！");
        }
        List<AreaInfoDeviceId> areaInfos = tCruisePointInstanceDao.selectDeviceByDeviceMeteId(deviceMeteId);
        String camera = "camera";
        String robot = "robot";
        String drone = "drone";
        if (areaInfos.stream().anyMatch(areaInfo -> camera.equals(areaInfo.getInfoType()))) {
            Map<String, String> map = new HashMap<>();
            List<Long> recordIdList = tCameraScreenDao.selectRecordId();
            for (Long recordId : recordIdList) {
                HashMap<String, Object> recordIdMap = new HashMap<>();
                recordIdMap.put("recordId", recordId);
                Map<String, String> reMap = cameraConService.getCameraStatus(recordId);
                if (MapUtils.isEmpty(reMap)) {
                    continue;
                }
                map.putAll(reMap);
            }
            areaInfos.forEach(areaInfo -> {
                if ("camera".equals(areaInfo.getInfoType())) {
                    if (map.get(areaInfo.getId().toString()) != null) {
                        areaInfo.setState(Integer.valueOf(map.get(areaInfo.getId().toString())));
                    } else {
                        areaInfo.setState(0);
                    }
                }
            });
            AreaInfoDeviceId areaInfo = new AreaInfoDeviceId().setId(1L).setLabel("摄像机").setInfoType("camera");
            areaInfos.add(areaInfo);
        }
        if (areaInfos.stream().anyMatch(areaInfo -> robot.equals(areaInfo.getInfoType()))) {
            AreaInfoDeviceId areaInfo = new AreaInfoDeviceId().setId(2L).setLabel("机器人").setInfoType("robot");
            areaInfos.add(areaInfo);
        }
        if (areaInfos.stream().anyMatch(areaInfo -> drone.equals(areaInfo.getInfoType()))) {
            AreaInfoDeviceId areaInfo = new AreaInfoDeviceId().setId(3L).setLabel("无人机").setInfoType("drone");
            areaInfos.add(areaInfo);
        }
        return assembleTrees(areaInfos);
    }

    public List<AreaInfoDeviceId> assembleTrees(Collection<AreaInfoDeviceId> trees) {
        if (CollectionUtils.isEmpty(trees)) {
            return Collections.emptyList();
        }

        // 构建树主键/实例映射表，并初始化树的子节点集合
        Map<?, AreaInfoDeviceId> mapping = trees.stream().peek(tree -> tree.setChildren(new LinkedList<>()))
                .collect(Collectors.toMap(AreaInfoDeviceId::getId, t -> t, (o, n) -> n));

        // 查找并关联树节点，返回所有没有父节点的树
        return trees.stream().filter(tree -> {
            AreaInfoDeviceId parent = ifNull(tree.getUpId(), mapping::get);
            if (parent != null) {
                parent.getChildren().add(tree);
            }
            return Objects.isNull(parent);
        }).collect(Collectors.toList());
    }

    /**
     * 返回不为空的对象（如果第一个对象为空，则返回第二个对象）
     *
     * @param object   目标对象
     * @param function 目标对象方法
     * @param <T>      目标对象类型泛型
     * @param <R>      返回对象类型泛型
     * @return 返回对象
     */
    public static <T, R> R ifNull(T object, Function<T, R> function) {
        return object == null || function == null ? null : function.apply(object);
    }

}
