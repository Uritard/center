package com.yjh.platform.module.device.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.dao.TDeviceMaintenanceDao;
import com.yjh.platform.module.device.entity.IdAndNameDetail;
import com.yjh.platform.module.device.entity.TDeviceMaintenance;
import com.yjh.platform.module.device.entity.TDeviceMaintenanceDetail;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
* @author lqh
* @since 2021-01-11
*/
@Service
public class TDeviceMaintenanceService{

    @Autowired
    private TDeviceMaintenanceDao tDeviceMaintenanceDao;
    private Logger log = LoggerFactory.getLogger(TDeviceMaintenanceService.class);


    @Logs(title = "插入", code = "module",content = "插入")
    @Transactional(rollbackFor = Exception.class)
    public int add(TDeviceMaintenance tDeviceMaintenance) {
//        {
//            "deviceIdList": [
//            100000028,100000034
//  ],
//        "maintenanceId": 1,
//            "maintenanceName": "清华1号",
//                "maintenanceStart": "2021-01-11 06:36:54.759Z",
//                "maintenanceStop": "2021-02-11 06:36:54.759Z"
//        }
        tDeviceMaintenance.setDeviceId(tDeviceMaintenance.getDeviceIdList().get(0));
        this.tDeviceMaintenanceDao.add(tDeviceMaintenance);
        Long id = tDeviceMaintenance.getMaintenanceId();
        if(tDeviceMaintenance.getMaintenanceStart() == null){
            tDeviceMaintenance.setMaintenanceStart(new Date());
        }
        this.tDeviceMaintenanceDao.deleteByPrimaryId(tDeviceMaintenance.getMaintenanceId());

        List<Long> list = tDeviceMaintenance.getDeviceIdList();

        //给机器人下发检修区域指令
        {
            List<String> inspectionCodeList = tDeviceMaintenanceDao.selectCruiseIdAndDeviceId(list);
            HashMap<String,Object> params = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            params.put("enable",1);
            params.put("deviceList",inspectionCodeList);
            params.put("startTime",sdf.format(tDeviceMaintenance.getMaintenanceStart()));
            params.put("endTime",sdf.format(tDeviceMaintenance.getMaintenanceStop()));
            params.put("deviceLevel",3);
            sendPostRequest(Constant.Maintenance_Issued,params);
        }
        List<TDeviceMaintenance> addList = new ArrayList<>();
        for(Long item:list){
            TDeviceMaintenance deviceMaintenanceItem = new TDeviceMaintenance();
            deviceMaintenanceItem.setDeviceId(item)
                    .setMaintenanceName(tDeviceMaintenance.getMaintenanceName())
                    .setIsValid(tDeviceMaintenance.getIsValid())
                    .setMaintenanceStart(tDeviceMaintenance.getMaintenanceStart())
                    .setMaintenanceId(id)
                    .setMaintenanceStop(tDeviceMaintenance.getMaintenanceStop());
            addList.add(deviceMaintenanceItem);
        }
        return this.tDeviceMaintenanceDao.batchAdd(addList);
    }

    public Result sendPostRequest(String url, HashMap<String,Object> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.postForObject(url, params,Result.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }

    @Logs(title = "删除", code = "module",content = "删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long maintenanceId) {
        //给机器人下发检修区域指令
        {
            List<Long> list = tDeviceMaintenanceDao.selectDeviceIds(maintenanceId);
            List<String> inspectionCodeList = tDeviceMaintenanceDao.selectCruiseIdAndDeviceId(list);
            HashMap<String,Object> params = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            params.put("enable",0);
            params.put("deviceList",inspectionCodeList);
            params.put("startTime",sdf.format(new Date()));
            params.put("endTime",sdf.format(new Date()));
            params.put("deviceLevel",3);
            sendPostRequest(Constant.Maintenance_Issued,params);
        }
        return this.tDeviceMaintenanceDao.deleteByPrimaryId(maintenanceId);
    }

    @Logs(title = "更新", code = "module",content = "更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TDeviceMaintenance tDeviceMaintenance) {
        List<Long> list = tDeviceMaintenance.getDeviceIdList();
        List<TDeviceMaintenance> addList = new ArrayList<>();
        this.deleteByPrimaryId(tDeviceMaintenance.getMaintenanceId());
        if(tDeviceMaintenance.getMaintenanceStart() == null){
            tDeviceMaintenance.setMaintenanceStart(new Date());
        }
        for(Long item:list){
            TDeviceMaintenance deviceMaintenanceItem = new TDeviceMaintenance();
            deviceMaintenanceItem.setDeviceId(item)
                    .setMaintenanceName(tDeviceMaintenance.getMaintenanceName())
                    .setIsValid(tDeviceMaintenance.getIsValid())
                    .setMaintenanceStart(tDeviceMaintenance.getMaintenanceStart())
                    .setMaintenanceId(tDeviceMaintenance.getMaintenanceId())
                    .setMaintenanceStop(tDeviceMaintenance.getMaintenanceStop());
            addList.add(deviceMaintenanceItem);
        }
        //给机器人下发检修区域指令
        {
            List<String> inspectionCodeList = tDeviceMaintenanceDao.selectCruiseIdAndDeviceId(list);
            HashMap<String,Object> params = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            params.put("enable",1);
            params.put("deviceList",inspectionCodeList);
            params.put("startTime",sdf.format(tDeviceMaintenance.getMaintenanceStart()));
            params.put("endTime",sdf.format(tDeviceMaintenance.getMaintenanceStop()));
            params.put("deviceLevel",3);
            sendPostRequest(Constant.Maintenance_Issued,params);
        }
        return this.tDeviceMaintenanceDao.batchAdd(addList);
    }

    @Logs(title = "主键查询", code = "module",content = "主键查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TDeviceMaintenance> selectByPrimaryId(Long maintenanceId) {
        List<TDeviceMaintenance> tDeviceMaintenanceList = tDeviceMaintenanceDao.selectByPrimaryId(maintenanceId);
        Date now = new Date();
        List<TDeviceMaintenance> re =new ArrayList<>();
        for(TDeviceMaintenance item: tDeviceMaintenanceList){
            if (item.getMaintenanceStop().compareTo(now) <= 0  ){
                item.setEffectiveState(407);
                item.setEffectiveStateName("已失效");
            }
            if(item.getMaintenanceStart().compareTo(now) >= 0  ){
                item.setEffectiveState(408);
                item.setEffectiveStateName("已生效");
            }
            if(item.getMaintenanceStart().compareTo(now) <= 0  && item.getMaintenanceStop().compareTo(now) >= 0){
                item.setEffectiveState(406);
                item.setEffectiveStateName("已生效");
            }
            re.add(item);
            //&& endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0)
        }
        return re;
    }

    @Logs(title = "查询", code = "module",content = "查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TDeviceMaintenance> select(Long maintenanceId, String maintenanceName, Long deviceId, Integer isValid, Date maintenanceStart, Date maintenanceStop,Integer effectiveState) {
        List<TDeviceMaintenance> tDeviceMaintenanceList = tDeviceMaintenanceDao.select(maintenanceId,maintenanceName,deviceId,isValid,maintenanceStart,maintenanceStop);
        Date now = new Date();
        List<TDeviceMaintenance> re =new ArrayList<>();
        for(TDeviceMaintenance item: tDeviceMaintenanceList){
            if (item.getMaintenanceStop().compareTo(now) <= 0  ){
                item.setEffectiveState(407);
                item.setEffectiveStateName("已失效");
            }
            if(item.getMaintenanceStart().compareTo(now) >= 0  ){
                item.setEffectiveState(408);
                item.setEffectiveStateName("已生效");
            }
            if(item.getMaintenanceStart().compareTo(now) <= 0  && item.getMaintenanceStop().compareTo(now) >= 0){
                item.setEffectiveState(406);
                item.setEffectiveStateName("已生效");
            }
            if(effectiveState != null && item.getEffectiveState() != effectiveState){
                continue;
            }
            re.add(item);
            //&& endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0)
        }
        return re;
    }

    @Logs(title = "分页查询", code = "module",content = "分页查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TDeviceMaintenanceDetail> selectByPage(String maintenanceName,Integer effectiveState) {
        List<TDeviceMaintenanceDetail> tDeviceMaintenanceList = tDeviceMaintenanceDao.selectByPage(maintenanceName);
        Date now = new Date();
        List<TDeviceMaintenanceDetail> re =new ArrayList<>();
        for(TDeviceMaintenanceDetail item: tDeviceMaintenanceList){
            if (item.getMaintenanceStop().compareTo(now) <= 0  ){
                item.setEffectiveState(407);
                item.setEffectiveStateName("已失效");
            }
            if(item.getMaintenanceStart().compareTo(now) >= 0  ){
                item.setEffectiveState(408);
                item.setEffectiveStateName("未生效");
            }
            if(item.getMaintenanceStart().compareTo(now) <= 0  && item.getMaintenanceStop().compareTo(now) >= 0){
                item.setEffectiveState(406);
                item.setEffectiveStateName("已生效");
            }
            if(effectiveState != null && !(item.getEffectiveState().equals(effectiveState))){
                System.out.println(item.getEffectiveState());
                System.out.println(!(item.getEffectiveState().equals(effectiveState)));
                continue;
            }
            List<IdAndNameDetail> list = this.tDeviceMaintenanceDao.selectIdAndName(item.getMaintenanceId());
            //item.setUpRegionList(this.tDeviceMaintenanceDao.selectDeviceIds(item.getMaintenanceId()));
            item.setDeviceInfo(list);
            re.add(item);
            //&& endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0)
        }
        return re;
    }

    @Logs(title = "查询区域下的设备", code = "module",content = "查询区域下的设备")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectDeviceDetail(Long maintenanceId){
        Map<String,Object> re = new HashMap<>();
        List<IdAndNameDetail> list1 = this.tDeviceMaintenanceDao.selectIdAndName(maintenanceId);
        re.put("deviceInfo",list1);
        List<Long> list2 = this.tDeviceMaintenanceDao.selectDeviceIds(maintenanceId);
        re.put("deviceIds",list2);
        return re;
    }

    @Logs(title = "批量插入", code = "module",content = "批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TDeviceMaintenance> list) {
        return this.tDeviceMaintenanceDao.batchAdd(list);
    }

    @Logs(title = "批量删除", code = "module",content = "批量删除")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String maintenanceId) {
    List<String> list1= Arrays.asList(maintenanceId.split(","));
        //给机器人下发检修区域指令
        {
            List<Long> list = tDeviceMaintenanceDao.selectDeviceIds2(list1);
            List<String> inspectionCodeList = tDeviceMaintenanceDao.selectCruiseIdAndDeviceId(list);
            HashMap<String,Object> params = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            params.put("enable",0);
            params.put("deviceList",inspectionCodeList);
            params.put("startTime",sdf.format(new Date()));
            params.put("endTime",sdf.format(new Date()));
            params.put("deviceLevel",3);
            sendPostRequest(Constant.Maintenance_Issued,params);
        }
    return this.tDeviceMaintenanceDao.batchDelete(list1);
    }


    @Logs(title = "查询区域下的设备", code = "module",content = "查询区域下的设备")
    @Transactional(rollbackFor = Exception.class)
    public List<IdAndNameDetail> selectDevice(String deviceIds) {
        String[] list = deviceIds.split(",");
        List<Long> idList = new ArrayList<>();
        for (String item: list) {
            idList.add(Long.valueOf(item));
        }
        if(idList != null && idList.size() != 0){
            return this.tDeviceMaintenanceDao.selectDevice(idList);
        }
        return null;
    }

    @Logs(title = "查询设备下的巡视点", code = "module",content = "查询设备下的巡视点")
    @Transactional(rollbackFor = Exception.class)
    public List<IdAndNameDetail> selectInstance(Long deviceId) {
        return this.tDeviceMaintenanceDao.selectInstance(deviceId);
    }


    @Logs(title = "站端检修区域下发", code = "module",content = "站端检修区域下发")
    @Transactional(rollbackFor = Exception.class)
    public int systemSend(XMLBaseModel xmlBaseModel) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String,Object>> list = xmlBaseModel.getItems();
        for (Map<String,Object> item:list) {
             String enable = item.get("enable").toString();
             String start_time = item.get("start_time").toString();
             String end_time = item.get("end_time").toString();
             String device_level = item.get("device_level").toString();
             String device_list = item.get("device_list").toString();
             String[] dd = device_list.split(",");
             List<Long> instanceIdList = new ArrayList<>();
             for (String str:dd) {
                 instanceIdList.add(Long.valueOf(str));
             }
             List<Long> deviceIdLst = tDeviceMaintenanceDao.selectDeviceIdList(instanceIdList);
             if("1".equals(enable)){
                 //设置检修区域
                TDeviceMaintenance tDeviceMaintenance = new TDeviceMaintenance();
                tDeviceMaintenance.setMaintenanceName("检修区域"+start_time);
                tDeviceMaintenance.setMaintenanceStart(simpleDateFormat.parse(start_time));
                tDeviceMaintenance.setMaintenanceStop(simpleDateFormat.parse(end_time));
                tDeviceMaintenance.setDeviceIdList(deviceIdLst);
                this.add(tDeviceMaintenance);
             }
             if("0".equals(enable)){
                //删除检修区域
                 tDeviceMaintenanceDao.deleteByDeviceIdList(deviceIdLst,"检修区域"+start_time);
             }

        }
        return 1;
    }

}

