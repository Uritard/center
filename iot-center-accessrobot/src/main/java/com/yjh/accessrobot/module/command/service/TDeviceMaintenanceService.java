package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.dao.TDeviceMaintenanceMapper;
import com.yjh.accessrobot.module.command.entity.TDeviceMaintenance;
import com.yjh.accessrobot.module.command.entity.TDeviceMaintenanceModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
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
            tDeviceMaintenance.setOriginId(deviceMaintenanceMode.getConfigCode());
            tDeviceMaintenance.setMaintenanceName(deviceMaintenanceMode.getStationName());
            tDeviceMaintenance.setIsValid(Integer.valueOf(deviceMaintenanceMode.getEnable()));
            tDeviceMaintenance.setCoordinatePixel(deviceMaintenanceMode.getCoordinatePixel());
            tDeviceMaintenance.setDeviceLevel(deviceMaintenanceMode.getDeviceLevel());
            tDeviceMaintenance.setMaintenanceStart(DateTimeUtil.getDate(deviceMaintenanceMode.getStartTime()));
            tDeviceMaintenance.setMaintenanceStop(DateTimeUtil.getDate(deviceMaintenanceMode.getEndTime()));
            switch (deviceMaintenanceMode.getDeviceLevel()){
                //间隔,主设备
                case "1":
                case "2":
                    List<String> deviceList = tDeviceMaintenanceMapper.selectDeviceIdsList(deviceMaintenanceMode.getDeviceList());
                    tDeviceMaintenance.setDeviceIds(deviceList.toString().replace("[","").replace("]",""));
                    List<String> instanceLists = tDeviceMaintenanceMapper.selectInstanceIdsByDeviceIdList(deviceList);
                    tDeviceMaintenance.setInstanceIds(instanceLists.toString().replace("[","").replace("]",""));
                    break;
                //点位
                case "3":
                case "4":
                    List<String> instanceList = tDeviceMaintenanceMapper.selectInstanceIdsList(deviceMaintenanceMode.getDeviceList());
                    List<String> deviceLists = tDeviceMaintenanceMapper.selectDeviceIdsByInstanceList(instanceList);
                    tDeviceMaintenance.setDeviceIds(deviceLists.toString().replace("[","").replace("]",""));
                    tDeviceMaintenance.setInstanceIds(instanceList.toString().replace("[","").replace("]",""));
                    break;
                //部件
                default:
                    break;
            }
            return tDeviceMaintenance;
        }).collect(Collectors.toList());

        List<TDeviceMaintenance> oldDeviceMaintenanceList = tDeviceMaintenanceMapper.selectByEdge(edgeCode);

        Map<String, TDeviceMaintenance> oldDeviceMaintenanceMap = oldDeviceMaintenanceList.stream().collect(Collectors.toMap(TDeviceMaintenance::getOriginId, Function.identity()));
        Map<String, TDeviceMaintenance> newDeviceMaintenanceMap = tDeviceMaintenanceList.stream().collect(Collectors.toMap(TDeviceMaintenance::getOriginId, Function.identity()));

        // 更新的数据
        SetUtils.SetView<String> updateSet = SetUtils.intersection(oldDeviceMaintenanceMap.keySet(), newDeviceMaintenanceMap.keySet());
        if (CollectionUtils.isNotEmpty(updateSet)){
            tDeviceMaintenanceList.stream().filter(tDeviceMaintenance -> updateSet.contains(tDeviceMaintenance.getOriginId())).forEach(tDeviceMaintenance -> {
                TDeviceMaintenance oldDeviceMaintenance = oldDeviceMaintenanceMap.get(tDeviceMaintenance.getOriginId());
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
}
