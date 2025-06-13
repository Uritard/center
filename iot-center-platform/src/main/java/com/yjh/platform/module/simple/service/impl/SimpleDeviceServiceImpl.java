/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.common.utils.ExcelReadListener;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.device.service.TStdDeviceAttrService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.simple.dao.TStdDeviceMapper;
import com.yjh.platform.module.simple.entity.SimpleDeviceExcel;
import com.yjh.platform.module.simple.service.ISimpleDeviceService;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.service.TRobotInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-06-03
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SimpleDeviceServiceImpl extends ServiceImpl<TStdDeviceMapper, TStdDevice> implements ISimpleDeviceService {
    private final RedisTemplate<?, ?> redisTemplate;
    private final TStdRegionService tStdRegionService;
    private final TStdDeviceAttrService tStdDeviceAttrService;
    private final TRobotInfoService robotService;

    public static final String MODEL_FILE_LAS = "las";
    public static final String MODEL_FILE_XLSX = "xls";
    public static final String MODEL_FILE_TXT = "txt";
    public static final String MODEL_FILE_SVG = "svg";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importModel(MultipartFile file) {

        String outputPathParent = SysParamConfig.getSysContent("simpleModelPath");
        String outputPath = outputPathParent + File.separator + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMATTER);
        Map<String, String> pathMap = new HashMap<>(8);
        try {
            ZipUtil.unzip(file.getInputStream(), FileUtil.mkdir(outputPath), StandardCharsets.UTF_8);

            // 遍历文件夹，根据文件后缀名对应解析
            Files.walkFileTree(Paths.get(outputPath), new SimpleFileVisitor<Path>() {
                //遍历文件
                @Override
                @NotNull
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // 获取文件后缀名，根据后缀名做不同处理。后缀名包括las，xlsx，txt，svg
                    String suffix = FileNameUtil.getSuffix(file.toFile()).toLowerCase();
                    String filePath = file.normalize().toAbsolutePath().toString();
                    // 匹配 excel 文件，后缀为 xls xlsx
                    if (StringUtils.equalsAny(suffix, "xls", "xlsx")) {
                        pathMap.put(MODEL_FILE_XLSX, filePath);
                    } else {
                        pathMap.put(suffix, filePath);
                    }

                    return super.visitFile(file, attrs);
                }
            });

            if (pathMap.containsKey(MODEL_FILE_XLSX)) {
                List<TStdRegion> regions = importDeviceModel(pathMap.get(MODEL_FILE_XLSX));
                addRobotInfo(regions, pathMap);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.CODE10015, "上传文件格式不正确，请上传正确的zip文件");
        }

        return false;
    }

    /**
     * excel  导入模型
     * @param filePath excel文件路径
     * @return 区域信息
     */
    private List<TStdRegion> importDeviceModel(String filePath) {
        log.info("导入 excel 数据: {}", filePath);
        String[] heads =
            new String[] {"变电站名称", "电压等级", "区域名称", "间隔名称", "设备名称", "X1", "Y1", "Z1", "X2", "Y2", "Z2", "X3", "Y3",
                "Z3", "X4", "Y4", "Z4"};
        ExcelReadListener<SimpleDeviceExcel> modelExcelListener = new ExcelReadListener<>(heads);
        ReadSheet readSheet = new ReadSheet(0);
        EasyExcelFactory.read(filePath, SimpleDeviceExcel.class, modelExcelListener).headRowNumber(1).build().read(readSheet);

        List<SimpleDeviceExcel> excelEntities = modelExcelListener.getExcelEntities();
        List<String> errorExcelList = modelExcelListener.getErrorExcelEntities();
        log.warn("excel导入错误信息：{}", ExcelReadListener.prettyErrors(errorExcelList));
        return importSimpleDevice(excelEntities);
    }

    /**
     * 导入模型中excel内的设备信息
     */
    public List<TStdRegion> importSimpleDevice(List<SimpleDeviceExcel> excelEntities) {
        log.info("开始导入模型数据");
        // 遍历excel数据，统计所有站所、区域、间隔
        Set<String> stationSet = new HashSet<>(16);
        Set<String> regionSet = new HashSet<>(16);
        Set<String> baySet = new HashSet<>(16);
        Map<String, String> bayToRegionMap = new HashMap<>(16);
        Set<String> deviceSet = new HashSet<>(16);
        Map<String, String> deviceToBayMap = new HashMap<>(64);
        for (SimpleDeviceExcel excelEntity : excelEntities) {
            stationSet.add(excelEntity.getStationName());
            regionSet.add(excelEntity.getRegionName());
            baySet.add(excelEntity.getIntervalName());
            bayToRegionMap.put(excelEntity.getIntervalName(), excelEntity.getRegionName());
            deviceSet.add(excelEntity.getDeviceName());
            deviceToBayMap.put(excelEntity.getDeviceName(), excelEntity.getIntervalName());
        }

        if (stationSet.size() > 1) {
            throw new BusinessException(ResultCodeEnum.CODE20019, "导入数据异常，只可以有一个站所");
        }

        // 站所下子区域
        Map<String, TStdRegion> regionMap = new HashMap<>(16);
        // 导入站所
        TStdRegion station = importStation(stationSet, regionMap);

        // 根据名称存储间隔信息
        Map<String, TStdRegion> regionByNameMap = new HashMap<>(16);
        // 导入区域
        List<TStdRegion> unExistRegionList = importRegions(regionSet, regionMap, regionByNameMap, station);

        // 导入间隔
        Map<String, TStdRegion> bayByNameMap = importBay(baySet, regionMap, regionByNameMap, unExistRegionList, bayToRegionMap);
        log.info("区域信息：{}", JSON.toJSONString(regionByNameMap));

        // 导入设备
        importDevices(excelEntities, bayByNameMap, deviceSet, deviceToBayMap);
        return new ArrayList<>(regionByNameMap.values());
    }

    /**
     * 处理站所信息
     * @param stationSet 导入站所
     * @param regionMap  站所下区域
     * @return 站所
     */
    private TStdRegion importStation(Set<String> stationSet, Map<String, TStdRegion> regionMap) {
        List<TStdRegion> regionList = tStdRegionService.queryAll();
        Map<String, TStdRegion> stdRegionMap =
            regionList.stream().collect(Collectors.toMap(TStdRegion::getRegionName, Function.identity(), (k1, k2) -> k1));
        // 判断站所是否存在
        String stationName = IterableUtils.first(stationSet);
        TStdRegion station = stdRegionMap.get(stationName);

        if (station != null) {
            // 站所存在，则查询站所下所有区域，以备后续查询
            List<TStdRegion> downRegion = tStdRegionService.selectDownRegion(station.getRegionId());
            regionMap.putAll(downRegion.stream().collect(Collectors.toMap(TStdRegion::getRegionName, Function.identity(), (k1, k2) -> k1)));
        } else {
            // 站所不存在，则创建
            TStdRegion root = tStdRegionService.selectRootRegion();
            station = new TStdRegion();
            station.setUpRegionId(root.getRegionId());
            station.setRegionCode(StringUtils.EMPTY);
            station.setRegionName(stationName);
            station.setState(1);
            tStdRegionService.insert(station);
        }
        // 返回站所信息
        return station;
    }

    /**
     * 处理区域信息
     * @param regionSet       导入区域信息
     * @param regionMap       已存在区域信息
     * @param regionByNameMap 区域名称对应区域
     * @param station         站所
     * @return 新导入区域列表
     */
    private List<TStdRegion> importRegions(Set<String> regionSet, Map<String, TStdRegion> regionMap,
        Map<String, TStdRegion> regionByNameMap, TStdRegion station) {
        // 遍历区域，获取不在站所内的区域
        Set<String> unExistRegions = regionSet.stream().filter(r -> {
            // regionMap 存在则false，并存入 regionByNameMap，不存在则true
            TStdRegion region = regionMap.get(r);
            if (Objects.nonNull(region)) {
                regionByNameMap.put(r, region);
                return false;
            }
            return true;
        }).collect(Collectors.toSet());

        // 批量创建不存在区域
        Long stationRegionId = station.getRegionId();
        List<TStdRegion> unExistRegionList = unExistRegions.stream().map(r -> {
            TStdRegion region = new TStdRegion();
            region.setUpRegionId(stationRegionId);
            region.setRegionCode(StringUtils.EMPTY);
            region.setRegionName(r);
            region.setState(1);
            return region;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(unExistRegionList)) {
            tStdRegionService.batchInsert(unExistRegionList);
        }

        return unExistRegionList;
    }

    /**
     * 导入间隔信息
     * @param baySet            待导入间隔
     * @param regionMap         站所下所有间隔
     * @param regionByNameMap   区间名称对应区间
     * @param unExistRegionList 新导入区域列表
     * @param bayToRegionMap    间隔区域名称对应
     */
    private Map<String, TStdRegion> importBay(Set<String> baySet, Map<String, TStdRegion> regionMap,
        Map<String, TStdRegion> regionByNameMap, List<TStdRegion> unExistRegionList, Map<String, String> bayToRegionMap) {
        // 间隔对应关系
        Map<String, TStdRegion> bayByNameMap = new HashMap<>(16);

        // 数据库中已存在区域
        Set<Long> existRegions = regionByNameMap.values().stream().map(TStdRegion::getRegionId).collect(Collectors.toSet());

        Set<String> unExistBays = baySet.stream().filter(r -> {
            // 找出存在的间隔，regionMap 存在则false，并存入 regionByNameMap，不存在则true
            TStdRegion region = regionMap.get(r);
            // 间隔需存在，且父级属于已存在区域
            if (Objects.nonNull(region) && existRegions.contains(region.getUpRegionId())) {
                bayByNameMap.put(r, region);
                return false;
            }
            return true;
        }).collect(Collectors.toSet());

        // 补全区域 region
        regionByNameMap.putAll(
            unExistRegionList.stream().collect(Collectors.toMap(TStdRegion::getRegionName, Function.identity(), (k1, k2) -> k1)));

        // 批量创建不存在间隔 bay
        List<TStdRegion> unExistBayList = unExistBays.stream().map(r -> {
            TStdRegion region = new TStdRegion();
            region.setUpRegionId(regionByNameMap.get(bayToRegionMap.get(r)).getRegionId());
            region.setRegionCode(StringUtils.EMPTY);
            region.setRegionName(r);
            region.setState(1);
            return region;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(unExistBayList)) {
            tStdRegionService.batchInsert(unExistBayList);
        }

        // 补全间隔 bay
        bayByNameMap.putAll(
            unExistBayList.stream().collect(Collectors.toMap(TStdRegion::getRegionName, Function.identity(), (k1, k2) -> k1)));

        return bayByNameMap;
    }

    /**
     * 导入设备信息
     * @param excelEntities  excel数据
     * @param bayByNameMap   间隔信息
     * @param deviceSet      设备名称
     * @param deviceToBayMap 设备对应间隔信息
     */
    private void importDevices(List<SimpleDeviceExcel> excelEntities, Map<String, TStdRegion> bayByNameMap, Set<String> deviceSet,
        Map<String, String> deviceToBayMap) {
        log.info("开始导入设备信息...");
        // 根据 bayByNameMap 和 deviceSet 查询数据库中设备，设备的 upRegionId 在 bayByNameMap 中，且设备名称在 deviceSet 中
        List<TStdDevice> tStdDevices = getBaseMapper().selectList(Wrappers.lambdaQuery(TStdDevice.class)
            .in(TStdDevice::getUpRegionId, bayByNameMap.values().stream().map(TStdRegion::getRegionId).collect(Collectors.toList()))
            .in(TStdDevice::getDeviceName, deviceSet));
        // 获取库里不存在的设备
        Map<String, TStdDevice> deviceMap =
            tStdDevices.stream().collect(Collectors.toMap(TStdDevice::getDeviceName, Function.identity(), (k1, k2) -> k1));
        List<TStdDevice> insertDevices = new ArrayList<>();
        List<TStdDeviceAttr> updateDevices = new ArrayList<>();

        int deviceType = NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("deviceType", "二次屏柜"));
        for (SimpleDeviceExcel excelEntity : excelEntities) {
            String deviceName = excelEntity.getDeviceName();
            TStdDevice device = deviceMap.get(deviceName);

            // 获取电压等级
            String voltageLevel = DictConvertUtil.DICT.getDictCode("voltageLevel", excelEntity.getVoltageLevel());
            TStdDeviceAttr deviceAttr = new TStdDeviceAttr();
            deviceAttr.setVoltageLevel(voltageLevel);
            // 设置坐标值，四个坐标分成两组，格式为 x1,y1,z1;x2,y2,z2  x3,y3,z3;x4,y4,z4
            deviceAttr.setLatitude(
                excelEntity.getX3() + "," + excelEntity.getY3() + "," + excelEntity.getZ3() + ";" + excelEntity.getX4() + ","
                    + excelEntity.getY4() + "," + excelEntity.getZ4());
            deviceAttr.setLongitude(
                excelEntity.getX1() + "," + excelEntity.getY1() + "," + excelEntity.getZ1() + ";" + excelEntity.getX2() + ","
                    + excelEntity.getY2() + "," + excelEntity.getZ2());
            // 设备存在，且区域与导入区域匹配
            if (Objects.nonNull(device) && device.getUpRegionId().equals(bayByNameMap.get(excelEntity.getIntervalName()).getRegionId())) {
                deviceAttr.setDeviceId(device.getDeviceId());
                updateDevices.add(deviceAttr);
            } else {
                device = new TStdDevice();
                TStdRegion upRegion = bayByNameMap.get(deviceToBayMap.get(deviceName));
                device.setUpRegionId(upRegion.getRegionId());
                device.setDeviceName(deviceName);
                device.setDeviceType(deviceType);
                device.setPositionType(Constant.SIMPLE_DEVICE_POSITION_TYPE);
                device.setUpRegionName(upRegion.getRegionName());
                deviceAttr.setUsedTime(new Date());
                insertDevices.add(device);
            }
            device.setTStdDeviceAttr(deviceAttr);
        }
        // 不存在的设备批量插入
        if (CollectionUtils.isNotEmpty(insertDevices)) {
            super.saveBatch(insertDevices, 1000);
            List<TStdDeviceAttr> attrList =
                insertDevices.stream().map(t -> t.getTStdDeviceAttr().setDeviceId(t.getDeviceId())).collect(Collectors.toList());
            tStdDeviceAttrService.batchInsert(attrList);
        }
        // 已存在设备批量更新，因为tStdDevice表里面信息基本不会变，所以只更新属性，因为设备名称变更后就是新增设备
        if (CollectionUtils.isNotEmpty(updateDevices)) {
            tStdDeviceAttrService.batchUpdate(updateDevices);
        }
    }

    /**
     * 增加机器人信息
     */
    public void addRobotInfo(List<TStdRegion> regions, Map<String, String> pathMap) {

        // 获取使用中状态
        String isUse = DictConvertUtil.DICT.getDictCode("robotUse", "使用中");
        int robotType = NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("robotType", "简易机器人"));
        String robotPosition = DictConvertUtil.DICT.getDictCode("robotPosition", "室内轮式");
        // 理论上区域不会很多，应该只有一个，所以就用for循环来处理了
        for (TStdRegion region : regions) {
            String svgFilePath = migrationFile(region.getRegionId(), pathMap);
            // 判断简易机器人设备是否存在，根据 upRegionId 判断
            List<TRobotInfo> robots =
                robotService.select(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, isUse, null, region.getRegionId(), null, null, null, null, null, null, null,
                    null, null);

            if (robots.isEmpty()) {
                TRobotInfo robot = new TRobotInfo();
                robot.setRobotName(region.getRegionName() + "简易机器人");
                robot.setUpRegionId(region.getRegionId());
                String code = "SI300_" + RandomUtil.randomString(4);
                robot.setRobotCode(code);
                robot.setRobotNum(code);
                robot.setRobotType(robotType);
                robot.setRobotPosition(robotPosition);
                Date today = DateUtil.beginOfDay(new Date());
                robot.setMadeDate(today);
                robot.setCommissionDate(today);
                robot.setPhotePath(svgFilePath);

                robotService.insert(robot, Constant.MANAGER_USER_ID);
            } else {
                // 循环修改简易机器人设备
                for (TRobotInfo robot : robots) {
                    robot.setPhotePath(svgFilePath);
                    robotService.update(robot, Constant.MANAGER_USER_ID);
                }
            }
        }

        // 删除临时文件
        // pathMap.values().stream().findFirst().map(m -> FileUtil.del(FileUtil.getParent(m, 1)));
    }

    /**
     * 迁移文件
     * 将临时文件迁移至区域ID对应的文件夹下，并返回SVG的路径
     */
    public String migrationFile(Long regionId, Map<String, String> pathMap) {

        String outputPathParent = SysParamConfig.getSysContent("simpleModelPath");
        String outputPath = outputPathParent + File.separator + regionId;
        File out = FileUtil.mkdir(outputPath);
        // 清空文件夹
        log.info("清空文件夹: {}", outputPath);
        FileUtil.clean(out);

        String svgPath = null;
        for (Map.Entry<String, String> entry : pathMap.entrySet()) {
            log.info("迁移文件: {}", entry.getValue());
            File outFile = FileUtil.copy(entry.getValue(), outputPath, true);
            if (MODEL_FILE_SVG.equals(entry.getKey())) {
                svgPath = outFile.getAbsolutePath();
                // svg 绝对路径转为网络相对路径
                String filePath = SysParamConfig.getSysContent("fileAbsPath");
                svgPath = svgPath.replace(filePath, Constant.FILE_REAL_PATH);
                log.info("svg 网络相对地址: {}", svgPath);
            }
        }

        return svgPath;
    }

}
