package com.yjh.platform.module.iot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.TreesUtil;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.iot.entity.TIotDevice;
import com.yjh.platform.module.iot.service.TIotDeviceService;
import com.yjh.platform.module.iot.dao.TIotDeviceMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author YIJIAHE
 * @description 针对表【t_iot_device(物联设备表)】的数据库操作Service实现
 * @createDate 2023-11-28 14:48:41
 */
@Service
public class TIotDeviceServiceImpl extends ServiceImpl<TIotDeviceMapper, TIotDevice> implements TIotDeviceService {

    @Resource
    private TIotDeviceMapper tIotDeviceMapper;

    @Resource
    private TStdRegionService tStdRegionService;

    @Resource
    private TStdDeviceDao tStdDeviceDao;

    @Override
    public List<AreaInfo> selectDevTree(String level, Long upRegionId, String name) {
        List<AreaInfo> areaTree = new ArrayList<>();
        if (StringUtils.isNotBlank(name)){
            //查设备
            List<Long> iotDeviceList = tIotDeviceMapper.selectIotDeviceByName(name);
            if (CollectionUtils.isNotEmpty(iotDeviceList)){
                List<Long> regionList = tIotDeviceMapper.selectRegionByIotDeviceList(iotDeviceList);
                regionList = tStdRegionService.getRegionIdByLeafNode(new HashSet<>(regionList));
                if (CollectionUtils.isNotEmpty(regionList)){
                    areaTree = tIotDeviceMapper.selectIotDeviceByNameTree(iotDeviceList, regionList);
                }
            }
        }else {
            switch (level) {
                case "5":
                    areaTree = tStdDeviceDao.selectDevTreeRegion();
                    areaTree = TreesUtil.assembleTrees(areaTree);
                    areaTree = areaAddDeviceTree(areaTree);
                    break;
                case "6":
                    areaTree = tIotDeviceMapper.selectDeviceByRegionId(upRegionId);
                    TreesUtil.assembleTrees(areaTree);
                    break;
                default:
                    throw new BusinessException("设备树展示层级输入有误！");
            }
        }
        return areaTree;
    }

    /**
     * 将区域第一层级下挂的设备添加上
     * @param areaTree 设备树
     * @return 设备树
     */
    private List<AreaInfo> areaAddDeviceTree(List<AreaInfo> areaTree) {
        if (areaTree == null) {
            return null;
        }
        areaTree.forEach(area -> {
            String region = "region";
            if (region.equals(area.getInfoType())) {
                if (area.getChildren() != null && area.getChildren().size() > 0) {
                    area.getChildren().addAll(tIotDeviceMapper.selectDeviceByRegionId(area.getId()));
                    areaAddDeviceTree(area.getChildren());
                }
            }
        });
        return areaTree;
    }
}




