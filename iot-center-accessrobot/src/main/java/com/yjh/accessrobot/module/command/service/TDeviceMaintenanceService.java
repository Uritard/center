package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.dao.TDeviceMaintenanceMapper;
import com.yjh.accessrobot.module.command.entity.TDeviceMaintenance;
import com.yjh.accessrobot.module.command.entity.TDeviceMaintenanceModel;
import com.yjh.accessrobot.module.command.entity.TStdDeviceMete;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/1/5
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TDeviceMaintenanceService {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private TDeviceMaintenanceMapper tDeviceMaintenanceMapper;

    @Transactional(rollbackFor = Exception.class)
    public void saveReportData(List<TDeviceMaintenanceModel> deviceMaintenanceModelList, String edgeCode) {
        if(CollectionUtils.isEmpty(deviceMaintenanceModelList)){
            log.info("robotModelList is null");
            tDeviceMaintenanceMapper.deleteByEdgeCode(edgeCode);
            return ;
        }
        List<TDeviceMaintenance> tDeviceMaintenanceList = deviceMaintenanceModelList.stream().map(deviceMaintenanceMode -> {
            TDeviceMaintenance tDeviceMaintenance = new TDeviceMaintenance();
            tDeviceMaintenance.setMaintenanceId(null);
            tDeviceMaintenance.setEdgeCode(edgeCode);
            tDeviceMaintenance.setConfigCode(deviceMaintenanceMode.getConfigCode());
            try {
                tDeviceMaintenance.setOriginId(deviceMaintenanceMode.getConfigCode());
                tDeviceMaintenance.setMaintenanceName(deviceMaintenanceMode.getStationName() + "_" + deviceMaintenanceMode.getStartTime());
                tDeviceMaintenance.setIsValid(Integer.valueOf(deviceMaintenanceMode.getEnable()));
                tDeviceMaintenance.setCoordinatePixel(deviceMaintenanceMode.getCoordinatePixel());
                tDeviceMaintenance.setDeviceLevel(deviceMaintenanceMode.getDeviceLevel());
                tDeviceMaintenance.setMaintenanceStart(DateTimeUtil.getDate(deviceMaintenanceMode.getStartTime()));
                tDeviceMaintenance.setMaintenanceStop(DateTimeUtil.getDate(deviceMaintenanceMode.getEndTime()));

                List<String> instanceIds = convertInstances(edgeCode, NumberUtils.toInt(deviceMaintenanceMode.getDeviceLevel(), 3), deviceMaintenanceMode.getDeviceList());
                log.info("edgeCode： {} 获取 instanceIds：{}", edgeCode, instanceIds);
                if (CollectionUtils.isNotEmpty(instanceIds)) {
                    List<String> deviceLists = tDeviceMaintenanceMapper.selectDeviceIdsByInstanceList(instanceIds);
                    tDeviceMaintenance.setDeviceIds(StringUtils.join(deviceLists, ","));
                } else {
                    return null;
                }
                tDeviceMaintenance.setInstanceIds(StringUtils.join(instanceIds, ","));
                return tDeviceMaintenance;
            } catch (Exception e) {
                log.error("拼装检修区域失败", e);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        List<TDeviceMaintenance> oldDeviceMaintenanceList = tDeviceMaintenanceMapper.selectByEdge(edgeCode);

        Map<String, TDeviceMaintenance> oldDeviceMaintenanceMap = oldDeviceMaintenanceList.stream().collect(Collectors.toMap(TDeviceMaintenance::getConfigCode, Function.identity(), (e1,e2) -> e1));
        Map<String, TDeviceMaintenance> newDeviceMaintenanceMap = tDeviceMaintenanceList.stream().collect(Collectors.toMap(TDeviceMaintenance::getConfigCode, Function.identity(), (e1,e2) -> e1));

        // 更新的数据
        SetUtils.SetView<String> updateSet = SetUtils.intersection(oldDeviceMaintenanceMap.keySet(), newDeviceMaintenanceMap.keySet());
        if (CollectionUtils.isNotEmpty(updateSet)){
            tDeviceMaintenanceList.stream().filter(tDeviceMaintenance -> updateSet.contains(tDeviceMaintenance.getConfigCode())).forEach(tDeviceMaintenance -> {
                TDeviceMaintenance oldDeviceMaintenance = oldDeviceMaintenanceMap.get(tDeviceMaintenance.getConfigCode());
                tDeviceMaintenance.setMaintenanceId(oldDeviceMaintenance.getMaintenanceId());
                tDeviceMaintenanceMapper.updateByPrimaryKey(tDeviceMaintenance);
            });
        }
        //删除的数据
        SetUtils.SetView<String> deleteIdSet= SetUtils.difference(oldDeviceMaintenanceMap.keySet(), newDeviceMaintenanceMap.keySet());
        if (CollectionUtils.isNotEmpty(deleteIdSet)) {
            tDeviceMaintenanceMapper.deleteByEdgeCodeAndOriginId(edgeCode, deleteIdSet);
        }
        //新增的数据
        SetUtils.SetView<String> insertIdSet = SetUtils.difference(newDeviceMaintenanceMap.keySet(), oldDeviceMaintenanceMap.keySet());
        if (CollectionUtils.isNotEmpty(insertIdSet)){
            List<TDeviceMaintenance> insertList = tDeviceMaintenanceList.stream().filter(tDeviceMaintenance -> insertIdSet.contains(tDeviceMaintenance.getOriginId())).collect(Collectors.toList());
            tDeviceMaintenanceMapper.batchInsert(insertList);
        }
    }

    public List<String> convertInstances(String edgeCode, int deviceLevel, String deviceList) {
        boolean standardPoints = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("t_sys_param:standardPoints", "content"));
        boolean middlegroundIds = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("t_sys_param:middlegroundIds", "content"));

        log.info("获取 instanceIds 数据，edgeCode: {}, deviceLevel: {}, deviceList: {}", edgeCode, deviceLevel, deviceList);
        List<String> instanceIds;
            /*
        middlegroundIds = true
        间隔：t_std_region.up_region_ids
        主设备：t_std_device_attr.pms_id
        部件：t_std_devicemete.component_id
         */
        switch (deviceLevel) {
            case 1:
                // 间隔
                instanceIds = tDeviceMaintenanceMapper.selectInstanceIdsByRegionOrDevice(edgeCode, middlegroundIds, deviceList, null);
                break;
            case 2:
                // 主设备
                instanceIds = tDeviceMaintenanceMapper.selectInstanceIdsByRegionOrDevice(edgeCode, middlegroundIds, null, deviceList);
                break;
            case 3:
                // 设备点位
                // 如果从上级系统下发  点位为机器人的id 对应t_std_devicemete表中的device_point_id 需要转为 巡视系统的instanceId
                instanceIds = tDeviceMaintenanceMapper.selectInstanceIdsList(edgeCode, standardPoints ? 2 : 1, deviceList);
                break;
            case 4:
                // 设备部件
                if (middlegroundIds){
                    instanceIds = tDeviceMaintenanceMapper.selectInstanceIdsList(edgeCode, 3, deviceList);
                }else {
                    List<String> list = Arrays.asList(deviceList.split(","));
                    Map<String, String> deviceListMap = list.stream().collect(Collectors.toMap(e -> e.split("_")[0],
                        e -> e.split("_")[1], (a, b) -> a + "," + b));
                    List<TStdDeviceMete> deviceModels = new ArrayList<>();
                    deviceListMap.forEach((k, v) -> {
                        TStdDeviceMete dm = new TStdDeviceMete();
                        dm.setDeviceId(NumberUtils.toLong(k));
                        dm.setCustomId(v);
                        deviceModels.add(dm);
                    });
                    instanceIds = tDeviceMaintenanceMapper.selectInstanceIdsByComponent(edgeCode, deviceModels);
                }
                break;
            default:
                return Arrays.asList(StringUtils.split(deviceList, ","));
        }

        return instanceIds;
    }
}
