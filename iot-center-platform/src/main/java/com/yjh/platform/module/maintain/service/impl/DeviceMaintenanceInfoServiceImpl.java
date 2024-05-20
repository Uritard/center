package com.yjh.platform.module.maintain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.common.utils.TreesUtil;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.maintain.entity.DeviceMaintenanceInfo;
import com.yjh.platform.module.maintain.service.DeviceMaintenanceInfoService;
import com.yjh.platform.module.maintain.dao.DeviceMaintenanceInfoMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author YIJIAHE
 * @description 针对表【device_maintenance_info(设备维护信息表)】的数据库操作Service实现
 * @createDate 2024-04-25 11:22:27
 */
@Service
public class DeviceMaintenanceInfoServiceImpl extends ServiceImpl<DeviceMaintenanceInfoMapper, DeviceMaintenanceInfo>
        implements DeviceMaintenanceInfoService {

    @Override
    public Map<String, List<DeviceMaintenanceInfo>> selectLastTime(Long deviceId) {
        QueryWrapper<DeviceMaintenanceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId);
        queryWrapper.orderByDesc("create_time");
        List<DeviceMaintenanceInfo> deviceMaintenanceInfoList = super.list(queryWrapper);
        DictConvertUtil.optional("cruiseDeviceType", "deviceType", "deviceTypeName")
                .add("maintenanceType").covertToDict(deviceMaintenanceInfoList);
        return deviceMaintenanceInfoList.stream().collect(Collectors.groupingBy(DeviceMaintenanceInfo::getMaintenanceTypeName));
    }

    @Override
    public List<AreaInfo> selectCruiseDeviceTree() {
        return TreesUtil.assembleTrees(getBaseMapper().selectCruiseDeviceTree());
    }

    @Override
    public void filter(List<AreaInfo> devTreeList, String name) {
        Iterator<AreaInfo> it = devTreeList.iterator();
        while (it.hasNext()) {
            AreaInfo areaInfo = it.next();
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(areaInfo.getChildren())) {
                this.filter(areaInfo.getChildren(), name);
            }
            //根据输入的名称
            if (ArrayUtils.contains(new String[]{"robot", "drone", "camera", "record", "voice",}, areaInfo.getInfoType())) {
                if (StringUtils.isNotEmpty(name) && !areaInfo.getLabel().contains(name)) {
                    it.remove();
                }
            }
        }
    }
}




