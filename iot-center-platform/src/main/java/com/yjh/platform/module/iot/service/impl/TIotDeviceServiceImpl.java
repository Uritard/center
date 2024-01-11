package com.yjh.platform.module.iot.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.common.utils.TreesUtil;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.iot.dao.TIotDeviceMapper;
import com.yjh.platform.module.iot.entity.IotDeviceDataEx;
import com.yjh.platform.module.iot.entity.TIotDevice;
import com.yjh.platform.module.iot.entity.TIotDeviceExtend;
import com.yjh.platform.module.iot.entity.TIotDevicePoint;
import com.yjh.platform.module.iot.service.TIotDevicePointService;
import com.yjh.platform.module.iot.service.TIotDeviceService;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.scheduled.ScheduledMapConfig;
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
import java.util.*;
import java.util.stream.Collectors;

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
            List<TIotDevicePoint> devicePoints = iotExtend.getPointList();
            // 校验通道号
            channelNumCheck(devicePoints);

            devicePoints.forEach(d -> {
                d.setIotDeviceId(iotExtend.getId());
                d.setIotDeviceName(iotExtend.getDeviceName());
            });
            if(Constant.logUpLv2()) {
                log.info("IotDevice pointsAdd: {}", JSON.toJSONString(devicePoints));
            }
            tIotDevicePointService.saveBatch(devicePoints);
        }

        // 15 s后将设备加入到采集列表中，并触发一次采集
        String scheduleTaskId = "IotDevice_" + iotDevice.getId();
        ScheduledMapConfig.schedule(scheduleTaskId, 15, iotDevice, device -> {
            Result result = serviceRestTemplate.getForObject(Constant.SEND_IOTDEVICE_URL + "/add?id={0}", Result.class, device.getId());
            log.info("add device collect, {}", result);
        });
        ThreadPoolUtil.COMMON_POOL.addThread(this::needDeviceUpload);
        return ret;
    }

    @Override
    public boolean updateById(TIotDevice iotDevice) {
        boolean ret = super.updateById(iotDevice);

        if (iotDevice instanceof TIotDeviceExtend) {
            // 自动创建第一个通道
            TIotDeviceExtend iotExtend = (TIotDeviceExtend)iotDevice;
            List<TIotDevicePoint> devicePoints = iotExtend.getPointList();
            // 校验通道号
            channelNumCheck(devicePoints);

            List<TIotDevicePoint> devicePointsOld = tIotDevicePointService.listByDeviceId(iotDevice.getId());
            Map<String, TIotDevicePoint> pointMap = devicePointsOld.stream().collect(Collectors.toMap(TIotDevicePoint::getChannelNum, s->s, (e1,e2)->e1));

            List<TIotDevicePoint> pointsInsert = new ArrayList<>();
            List<TIotDevicePoint> pointsUpdate = new ArrayList<>();

            devicePoints.forEach(d -> {
                d.setIotDeviceId(iotExtend.getId());
                d.setIotDeviceName(iotExtend.getDeviceName());
                if (d.getId() == null) {
                    TIotDevicePoint pold = pointMap.get(d.getChannelNum());
                    if (pold != null) {
                        d.setId(pold.getId());
                    }
                }
                if (d.getId() == null) {
                    pointsInsert.add(d);
                } else {
                    pointsUpdate.add(d);
                }
            });
            List<Long> pointsDelete = devicePointsOld.stream().map(TIotDevicePoint::getId)
                .filter(id -> devicePoints.stream().noneMatch(p2 -> p2.getId() != null && id.equals(p2.getId())))
                .collect(Collectors.toList());
            if (!pointsInsert.isEmpty()) {
                if(Constant.logUpLv2()) {
                    log.info("IotDevice pointsInsert: {}", JSON.toJSONString(pointsInsert));
                }
                tIotDevicePointService.saveBatch(pointsInsert);
            }
            if (!pointsUpdate.isEmpty()) {
                if(Constant.logUpLv2()) {
                    log.info("IotDevice pointsUpdate: {}", JSON.toJSONString(pointsUpdate));
                }
                tIotDevicePointService.updateBatchById(pointsUpdate);
            }
            if (!pointsDelete.isEmpty()) {
                if(Constant.logUpLv2()) {
                    log.info("IotDevice pointsDelete: {}", JSON.toJSONString(pointsDelete));
                }
                tIotDevicePointService.removeByIds(pointsDelete);
            }
        }

        Result result = serviceRestTemplate.getForObject(Constant.SEND_IOTDEVICE_URL + "/update?id={0}", Result.class, iotDevice.getId());
        log.info("update device collect, {}", result);
        ThreadPoolUtil.COMMON_POOL.addThread(this::needDeviceUpload);
        return ret;
    }

    private void channelNumCheck(List<TIotDevicePoint> devicePoints) {
        long disCount = devicePoints.stream().map(TIotDevicePoint::getChannelNum).filter(StringUtils::isNotBlank).distinct().count();
        if (disCount != devicePoints.size()) {
            throw new BusinessException(ResultCodeEnum.CODE10005, "通道号不可重复不可为空");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        QueryWrapper<TIotDevicePoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iot_device_id", id);
        tIotDevicePointService.remove(queryWrapper);

        // 取消定时器
        String scheduleTaskId = "IotDevice_" + id;
        ScheduledMapConfig.remove(scheduleTaskId);

        Result result = serviceRestTemplate.getForObject(Constant.SEND_IOTDEVICE_URL + "/delete?id={0}", Result.class, id);
        if (!(Optional.ofNullable(result).orElseThrow(() -> new BusinessException(ResultCodeEnum.CODE10001, "调用接口删除数据采集失败"))
            .isSuccess())) {
            throw new BusinessException(ResultCodeEnum.DELETEERROR, result.getMessage());
        }
        ThreadPoolUtil.COMMON_POOL.addThread(this::needDeviceUpload);
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

    private void needDeviceUpload(){
        if (Constant.updateSyncModel()) {
            deviceUpload();
        }
    }

    @Override
    public void deviceUpload(){
        List<IotDeviceDataEx> deviceDataList = getBaseMapper().selectIotDeviceInfo();
        try {
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            List<XMLBaseModel> xmlBaseModelList = new ArrayList<>();
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> itemList = new ArrayList<>();

            xmlBaseModel.setItems(itemList);
            xmlBaseModel.setType("iotDevice");
            xmlBaseModelList.add(xmlBaseModel);

            map.put("list", xmlBaseModelList);
            deviceDataList.forEach(tIotDeviceData -> {
                Map<String, Object> item = new HashMap<>();
                item.put("pointId", tIotDeviceData.getPointId());
                item.put("pointName", tIotDeviceData.getPointName());
                item.put("iotDeviceId", tIotDeviceData.getIotDeviceId());
                item.put("iotDeviceName", tIotDeviceData.getIotDeviceName());
                item.put("ip", tIotDeviceData.getIp());
                item.put("port", tIotDeviceData.getPort());
                item.put("address", tIotDeviceData.getAddress());
                item.put("unit", tIotDeviceData.getUnit());
                item.put("upRegionId", tIotDeviceData.getUpRegionId());
                item.put("createTime", DateTimeUtil.format(new Date()));
                item.put("magnificationCoefficient", tIotDeviceData.getMagnificationCoefficient());
                item.put("channelNum", tIotDeviceData.getChannelNum());
                item.put("iotDeviceType", tIotDeviceData.getIotDeviceType());
                item.put("deviceId", tIotDeviceData.getDeviceId());
                item.put("upRegionName", tIotDeviceData.getUpRegionName());
                item.put("controllable", tIotDeviceData.getControllable());
                item.put("extend", tIotDeviceData.getExtend());
                itemList.add(item);
            });
            Constant.otherServer(map, Constant.TCP_URL);
        } catch (Exception e) {
            log.info("上报物联设备出错！", e);
        }
    }

}




