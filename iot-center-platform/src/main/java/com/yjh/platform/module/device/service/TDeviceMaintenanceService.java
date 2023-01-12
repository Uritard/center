package com.yjh.platform.module.device.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TDeviceMaintenanceDao;
import com.yjh.platform.module.device.entity.IdAndNameDetail;
import com.yjh.platform.module.device.entity.TDeviceMaintenance;
import com.yjh.platform.module.device.entity.TDeviceMaintenanceDetail;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import com.yjh.platform.module.device.entity.DeviceAndInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author lqh
 * @since 2021-01-11
 */
@Service
public class TDeviceMaintenanceService{

    @Autowired
    private TDeviceMaintenanceDao tDeviceMaintenanceDao;
    private Logger log = LoggerFactory.getLogger(TDeviceMaintenanceService.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RedisTemplate redisTemplate;
    private static final Pattern PATTERN = Pattern.compile("^((([1-9]\\d{0,4},){0,2}([1-9]\\d{0,4});){0,3}([1-9]\\d{0,4},){0,2}([1-9]\\d{0,4}))$");

    @Transactional(rollbackFor = Exception.class)
    public int add(TDeviceMaintenance tDeviceMaintenance) {
        checkParam(tDeviceMaintenance);
        if(tDeviceMaintenance.getMaintenanceStart() == null){
            tDeviceMaintenance.setMaintenanceStart(new Date());
        }
        tDeviceMaintenance.setDeviceIdList(tDeviceMaintenance.getDeviceIdList().stream().distinct().collect(Collectors.toList()));
        List<Long> deviceList = tDeviceMaintenance.getDeviceIdList();
        List<DeviceAndInstance> deviceAndInstanceList = tDeviceMaintenance.getDeviceAndInstanceList();
        List<Long> instanceList = new ArrayList<>();
        for(DeviceAndInstance item:deviceAndInstanceList){
            instanceList.add(item.getInstanceId());
        }
        tDeviceMaintenance.setDeviceIds(tDeviceMaintenance.getDeviceIdList()
                .toString().replace("[","").replace("]","").replace(" ", ""));
        tDeviceMaintenance.setInstanceIds(instanceList
                .toString().replace("[","").replace("]","").replace(" ", ""));
        this.tDeviceMaintenanceDao.add(tDeviceMaintenance);
        createMaintenance(tDeviceMaintenance, instanceList, deviceList, 1);
        return 1;
    }



    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long maintenanceId) {
        //给机器人下发检修区域指令
        TDeviceMaintenance tDeviceMaintenance = this.selectByPrimaryId(maintenanceId);
        List<Long> instanceList = Arrays.stream(tDeviceMaintenance.getInstanceIds().split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        List<Long> deviceList = Arrays.stream(tDeviceMaintenance.getDeviceIds().split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        createMaintenance(tDeviceMaintenance, instanceList, deviceList, 0);
        return this.tDeviceMaintenanceDao.deleteByPrimaryId(maintenanceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TDeviceMaintenance tDeviceMaintenance) {
        this.deleteByPrimaryId(tDeviceMaintenance.getMaintenanceId());
        return this.add(tDeviceMaintenance);
    }

    /**
     * 给所有在线设备以及下级节点下发检修区域指令
     * @param tDeviceMaintenance
     * @param instanceList
     * @param deviceList
     */
    private void createMaintenance(TDeviceMaintenance tDeviceMaintenance, List<Long> instanceList, List<Long> deviceList, int enable) {
        //直连型机器人(所选测点所对应的机器人)
        List<String> robotCodeList = tDeviceMaintenanceDao.selectOnlineRobot(instanceList);
        robotCodeList.forEach(robot -> {
            String deviceListString = "";
            switch (tDeviceMaintenance.getDeviceLevel()) {
                case "1":
                    List<String> robotRegionList = tDeviceMaintenanceDao.selectRobotRegionIdList(instanceList);
                    if (CollectionUtils.isNotEmpty(robotRegionList)) {
                        deviceListString = StringUtils.join(robotRegionList.toArray(), ",");
                    }
                    break;
                case "2":
                    List<String> robotMainDeviceList = tDeviceMaintenanceDao.selectRobotMainDeviceIdList(instanceList);
                    if (CollectionUtils.isNotEmpty(robotMainDeviceList)) {
                        deviceListString = StringUtils.join(robotMainDeviceList.toArray(), ",");
                    }
                    break;
                case "3":
                    List<String> robotInstanceIdList = tDeviceMaintenanceDao.selectRobotInstanceIdList(instanceList);
                    deviceListString = StringUtils.join(robotInstanceIdList.toArray(), ",");
                    break;
                case "4":
                    List<String> robotComponentIdList = tDeviceMaintenanceDao.selectRobotComponentIdList(instanceList);
                    deviceListString = StringUtils.join(robotComponentIdList.toArray(), ",");
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotEmpty(deviceListString)) {
                sendPostRequest(enable, deviceListString, robot, tDeviceMaintenance);
            }
        });
        //下级节点(所选设备点所对应的下级节点)
        List<String> edgeList = tDeviceMaintenanceDao.selectOnlineEdge(deviceList);
        edgeList.forEach(edge -> {
            String deviceListString = "";
            switch (tDeviceMaintenance.getDeviceLevel()) {
                case "1":
                    List<String> regionList = tDeviceMaintenanceDao.selectRegionIdList(deviceList);
                    if (CollectionUtils.isNotEmpty(regionList)) {
                        deviceListString = StringUtils.join(regionList.toArray(), ",");
                    }
                    break;
                case "2":
                    List<String> mainDeviceList = tDeviceMaintenanceDao.selectMainDeviceIdList(deviceList);
                    if (CollectionUtils.isNotEmpty(mainDeviceList)) {
                        deviceListString = StringUtils.join(mainDeviceList.toArray(), ",");
                    }
                    break;
                case "3":
                case "4":
                    List<String> instanceIdList = tDeviceMaintenanceDao.selectInstanceIdList(instanceList);
                    if (CollectionUtils.isNotEmpty(instanceIdList)) {
                        deviceListString = StringUtils.join(instanceIdList.toArray(), ",");
                    }
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotEmpty(deviceListString)) {
                sendPostRequest(enable, deviceListString, edge, tDeviceMaintenance);
            }
        });
    }

    public Result sendPostRequest(int enable, String deviceListString, String onlineCode, TDeviceMaintenance tDeviceMaintenance) {
        HashMap<String,Object> params = new HashMap<>(8);
        params.put("enable", enable);
        params.put("device_list", deviceListString);
        params.put("start_time",sdf.format(tDeviceMaintenance.getMaintenanceStart()));
        params.put("end_time",sdf.format(tDeviceMaintenance.getMaintenanceStop()));
        params.put("device_level",tDeviceMaintenance.getDeviceLevel());
        params.put("config_code", tDeviceMaintenance.getMaintenanceId());
        params.put("coordinate_pixel", tDeviceMaintenance.getCoordinatePixel());
        params.put("online_code", onlineCode);

        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.postForObject(Constant.Maintenance_Issued, params,Result.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }

    /**
     * 参数校验
     */
    private void checkParam(TDeviceMaintenance maintenance){
        if(StringUtils.isAnyEmpty(maintenance.getCoordinatePixel(), maintenance.getDeviceLevel())){
            throw new BusinessException(ResultCodeEnum.CODE20017.getCode(), "设备层级或坐标不可为空");
        }
        int level = NumberUtils.toInt(maintenance.getDeviceLevel());
        if (level < 1 || level > 4) {
            throw new BusinessException(ResultCodeEnum.CODE20017.getCode(), "设备层级错误(1-4)");
        }
        if (!PATTERN.matcher(maintenance.getCoordinatePixel()).matches()) {
            throw new BusinessException(ResultCodeEnum.CODE20017.getCode(), "坐标格式错误(x1,y1,z1;x2,y2,z2;x3,y3,z3;x4,y4,z4)，单个坐标最长5位");
        }
        if (CollectionUtils.isEmpty(maintenance.getDeviceAndInstanceList())){
            throw new BusinessException(ResultCodeEnum.CODE20017.getCode(), "巡视点不能为空");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public TDeviceMaintenance selectByPrimaryId(Long maintenanceId) {
        TDeviceMaintenance tDeviceMaintenance = tDeviceMaintenanceDao.selectByPrimaryId(maintenanceId);
        Date now = new Date();
        if (tDeviceMaintenance.getMaintenanceStop().compareTo(now) <= 0  ){
            tDeviceMaintenance.setEffectiveState(407);
            tDeviceMaintenance.setEffectiveStateName("已失效");
        }
        if(tDeviceMaintenance.getMaintenanceStart().compareTo(now) >= 0  ){
            tDeviceMaintenance.setEffectiveState(408);
            tDeviceMaintenance.setEffectiveStateName("已生效");
        }
        if(tDeviceMaintenance.getMaintenanceStart().compareTo(now) <= 0  && tDeviceMaintenance.getMaintenanceStop().compareTo(now) >= 0){
            tDeviceMaintenance.setEffectiveState(406);
            tDeviceMaintenance.setEffectiveStateName("已生效");
        }
        //&& endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0)
        return tDeviceMaintenance;
    }

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
            List<IdAndNameDetail> list = this.tDeviceMaintenanceDao.selectIdAndName(item.getDeviceIds().replace(" ","").split(","));
            //item.setUpRegionList(this.tDeviceMaintenanceDao.selectDeviceIds(item.getMaintenanceId()));
            item.setDeviceInfo(list);
            re.add(item);
            //&& endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0)
        }
        return re;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectDeviceDetail(Long maintenanceId){
        Map<String,Object> re = new HashMap<>();
        TDeviceMaintenance t = this.selectByPrimaryId(maintenanceId);
        List<IdAndNameDetail> list1 = this.tDeviceMaintenanceDao.selectIdAndName(t.getDeviceIds().replace(" ","").split(","));
        re.put("deviceInfo",list1);
        List<DeviceAndInstance> list2 = this.tDeviceMaintenanceDao.selectDeviceIds(t.getInstanceIds().replace(" ","").split(","));
        re.put("deviceIds",list2);
        return re;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TDeviceMaintenance> list) {
        return this.tDeviceMaintenanceDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String maintenanceId) {
        List<String> list1= Arrays.asList(maintenanceId.split(","));
        list1.forEach(item->{
            this.deleteByPrimaryId(Long.valueOf(item));
        });
        return 1;

//        //给机器人下发检修区域指令
//        {
//            List<Long> list = tDeviceMaintenanceDao.selectDeviceIds2(list1);
//            List<String> deviceList = tDeviceMaintenanceDao.selectRobotDeviceId(list);
//
//            HashMap<String,Object> params = new HashMap<>();
//            params.put("enable",0);
//            params.put("deviceList",deviceList);
//            params.put("startTime",sdf.format(new Date()));
//            params.put("endTime",sdf.format(new Date()));
//            params.put("deviceLevel",2);
//            //配置编码
//            params.put("configCode", "");
//            //检修区域坐标框
//            params.put("coordinatePixel", "");
//            sendPostRequest(Constant.Maintenance_Issued,params);
//        }
//    return this.tDeviceMaintenanceDao.batchDelete(list1);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<DeviceAndInstance> selectDevice(String deviceIds) {
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

    @Transactional(rollbackFor = Exception.class)
    public List<IdAndNameDetail> selectInstance(Long deviceId) {
        return this.tDeviceMaintenanceDao.selectInstance(deviceId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int systemSend(XMLBaseModel xmlBaseModel) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> list = xmlBaseModel.getItems();
        for (Map<String, Object> item : list) {
            String enable = item.get("enable").toString();
            String startTime = item.get("start_time").toString();
            String endTime = item.get("end_time").toString();
            String deviceLevel = item.get("device_level").toString();
            String deviceList = item.get("device_list").toString();
            String coordinatePixel = item.get("coordinate_pixel").toString();
            List<Long> idList = Arrays.stream(deviceList.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
            if (!"".equals(deviceLevel)) {
                List<Long> deviceIdLst = new ArrayList<>();
                switch (deviceLevel) {
                    case "1":
                        //区域
                        deviceIdLst = tDeviceMaintenanceDao.selectDeviceByRegion(idList);
                        break;
                    case "2":
                        deviceIdLst = idList;
                        break;
                    case "3":
                        deviceIdLst = tDeviceMaintenanceDao.selectDeviceIdListByIns(idList);
                        break;
                    default:
                        break;
                }
                if ("1".equals(enable)) {
                    List<DeviceAndInstance> lists = new ArrayList<>();
                    if (!deviceIdLst.isEmpty()) {
                        List<Long> ins = tDeviceMaintenanceDao.selectInsByDeviceId(deviceIdLst);
                        ins.forEach(insItem -> {
                            DeviceAndInstance deviceAndInstance = new DeviceAndInstance();
                            deviceAndInstance.setInstanceId(insItem);
                            lists.add(deviceAndInstance);
                        });
                    }
                    //设置检修区域
                    TDeviceMaintenance tDeviceMaintenance = new TDeviceMaintenance();
                    tDeviceMaintenance.setMaintenanceName("检修区域" + startTime);
                    tDeviceMaintenance.setMaintenanceStart(simpleDateFormat.parse(startTime));
                    tDeviceMaintenance.setMaintenanceStop(simpleDateFormat.parse(endTime));
                    tDeviceMaintenance.setDeviceIdList(deviceIdLst);
                    tDeviceMaintenance.setDeviceLevel(deviceLevel);
                    tDeviceMaintenance.setDeviceAndInstanceList(lists);
                    tDeviceMaintenance.setCoordinatePixel(coordinatePixel);
                    this.add(tDeviceMaintenance);
                }
                if ("0".equals(enable)) {
                    //删除检修区域
                    tDeviceMaintenanceDao.deleteByDeviceIdList(StringUtils.join(deviceIdLst.toArray(), ","), "检修区域" + startTime);
                }
            }
        }
        return 1;
    }

}

