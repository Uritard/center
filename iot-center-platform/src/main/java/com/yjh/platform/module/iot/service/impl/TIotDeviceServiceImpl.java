package com.yjh.platform.module.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.TreesUtil;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.iot.dao.TIotDeviceMapper;
import com.yjh.platform.module.iot.entity.TIotDevice;
import com.yjh.platform.module.iot.entity.TIotDeviceExtend;
import com.yjh.platform.module.iot.entity.TIotDevicePoint;
import com.yjh.platform.module.iot.service.TIotDevicePointService;
import com.yjh.platform.module.iot.service.TIotDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author YIJIAHE
 * @description 针对表【t_iot_device(物联设备表)】的数据库操作Service实现
 * @createDate 2023-11-28 14:48:41
 */
@Service
@Slf4j
public class TIotDeviceServiceImpl extends ServiceImpl<TIotDeviceMapper, TIotDevice> implements TIotDeviceService {

    @Resource
    private TIotDeviceMapper tIotDeviceMapper;

    @Resource
    private TStdRegionService tStdRegionService;

    @Resource
    private TStdDeviceDao tStdDeviceDao;

    @Resource
    private TIotDevicePointService tIotDevicePointService;

    @Autowired
    @Qualifier("serviceRestTemplate")
    private RestTemplate serviceRestTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(TIotDevice iotDevice) {
        boolean ret = super.save(iotDevice);

        if (iotDevice instanceof TIotDeviceExtend) {
            // 自动创建第一个通道
            TIotDeviceExtend iotExtend = (TIotDeviceExtend)iotDevice;
            TIotDevicePoint devicePoint =
                new TIotDevicePoint().setIotDeviceId(iotExtend.getId()).setIotDeviceName(iotExtend.getDeviceName()).setChannelNum("1")
                    .setPointName(Optional.ofNullable(iotExtend.getPointName()).orElse(iotExtend.getDeviceName()))
                    .setExtend(iotExtend.getExtend()).setUnit(iotExtend.getUnit());
            tIotDevicePointService.save(devicePoint);
        }

        return ret;
    }

    @Override
    public boolean updateById(TIotDevice iotDevice) {
        boolean ret = super.updateById(iotDevice);

        Result result = serviceRestTemplate.getForObject(Constant.SEND_METER_URL + "/update?id={0}", Result.class, iotDevice.getId());
        log.info("update device collect, {}", result);

        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        QueryWrapper<TIotDevicePoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iot_device_id", id);
        tIotDevicePointService.remove(queryWrapper);

        Result result = serviceRestTemplate.getForObject(Constant.SEND_METER_URL + "/delete?id={0}", Result.class, id);
        if (!(Optional.ofNullable(result).orElseThrow(() -> new BusinessException(ResultCodeEnum.CODE10001, "调用接口删除数据采集失败"))
            .isSuccess())) {
            throw new BusinessException(ResultCodeEnum.DELETEERROR, result.getMessage());
        }

        return super.removeById(id);
    }

    @Override
    public List<AreaInfo> selectDevTree(String level, Long upRegionId, String name) {
        List<AreaInfo> areaTree = new ArrayList<>();
        if (StringUtils.isNotBlank(name)) {
            //查设备
            List<Long> iotDeviceList = tIotDeviceMapper.selectIotDeviceByName(name);
            if (CollectionUtils.isNotEmpty(iotDeviceList)) {
                List<Long> regionList = tIotDeviceMapper.selectRegionByIotDeviceList(iotDeviceList);
                regionList = tStdRegionService.getRegionIdByLeafNode(new HashSet<>(regionList));
                if (CollectionUtils.isNotEmpty(regionList)) {
                    areaTree = tIotDeviceMapper.selectIotDeviceByNameTree(iotDeviceList, regionList);
                }
            }
        } else {
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
     *
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




