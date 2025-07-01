/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.compress.extractor.Extractor;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.common.utils.ExcelReadListener;
import com.yjh.platform.common.utils.PlatformXmlUtil;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.device.service.TStdDeviceAttrService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.feign.RobotProxy;
import com.yjh.platform.module.simple.dao.SimpleDeviceMapper;
import com.yjh.platform.module.simple.entity.ModelCommand;
import com.yjh.platform.module.simple.entity.SimpleDeviceExcel;
import com.yjh.platform.module.simple.entity.SimpleDeviceModel;
import com.yjh.platform.module.simple.service.ISimpleDeviceService;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.service.TRobotInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-06-03
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SimpleDeviceServiceImpl extends ServiceImpl<SimpleDeviceMapper, TStdDevice> implements ISimpleDeviceService {
    private final TStdRegionService tStdRegionService;
    private final TStdDeviceAttrService tStdDeviceAttrService;
    private final TRobotInfoService robotService;
    private final RobotProxy robotProxy;

    public static final String MODEL_FILE_LAS = "las";
    public static final String MODEL_FILE_XLSX = "xlsx";
    public static final String MODEL_FILE_TXT = "txt";
    public static final String MODEL_FILE_SVG = "svg";
    public static final String MODEL_FILE_XML = "xml";

    private static final Map<String, String> COMMAND_MAP = new HashMap<>(8);

    static {
        COMMAND_MAP.put("1", MODEL_FILE_LAS);
        COMMAND_MAP.put("2", MODEL_FILE_XLSX);
        COMMAND_MAP.put("3", MODEL_FILE_TXT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importModel(MultipartFile file, Long robotId) {
        if (!com.yjh.platform.common.utils.FileUtil.checkFileName(file.getOriginalFilename(), new String[] {"zip", "7z"})) {
            throw new BusinessException(ResultCodeEnum.CODE10015, "请上传正确的zip或7z文件");
        }
        try {
            // 压缩包解压缩
            String outputPath = extract(file);

            // 遍历文件夹，根据文件后缀名对应解析
            Map<String, String> pathMap = modelFileList(outputPath);

            if (pathMap.containsKey(MODEL_FILE_XLSX)) {
                List<TStdRegion> regions = importDeviceModel(pathMap.get(MODEL_FILE_XLSX));
                addRobotInfo(regions, pathMap);
            } else if (robotId != null) {
                TRobotInfo robotInfo = robotService.selectByPrimaryId(robotId);
                migrationFile(robotInfo.getUpRegionId(), pathMap);
            }

            // 删除临时文件
            FileUtil.del(outputPath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.CODE10015, "上传文件格式不正确，请上传正确的zip或7z文件");
        }

        return false;
    }

    /**
     * 模型下发
     * @param modelSend 模型指令
     */
    @Override
    public void modelSend(ModelCommand modelSend) {
        try {
            TRobotInfo robotInfo = robotService.selectByPrimaryId(modelSend.getRobotId());
            long regionId = robotInfo.getUpRegionId();
            // 获取模型路径
            Map<String, String> modelMap = loadRegionFiles(regionId);

            // 模型指令固定，则下发固定模型
            if (StringUtils.isNotEmpty(modelSend.getCommand())) {
                sendToRobot(modelSend.getCommand(), regionId, robotInfo.getRobotCode(), modelMap);
            } else {
                // 模型指令为空，则下发所有模型
                for (String command : COMMAND_MAP.keySet()) {
                    sendToRobot(command, regionId, robotInfo.getRobotCode(), modelMap);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.CODE10010, "获取模型文件失败");
        }
    }

    /**
     * 下发模型指令给机器人
     */
    private void sendToRobot(String command, long regionId, String robotCode, Map<String, String> modelMap) {
        String path;
        String pathType = COMMAND_MAP.get(command);
        if (MODEL_FILE_XLSX.equals(pathType)) {
            // 如果是设备模型，则不下发excel，下发xml格式模型文件
            // 生成xml模型文件
            path = pointModel(regionId);
        } else {
            path = modelMap.get(pathType);
        }
        Result ret = robotProxy.modelSend(robotCode, command, path);
        log.info("模型下发返回结果: {} - {}", command, ret);
    }

    /**
     * 模型导出
     * @param modelSend 模型指令
     * @return 模型路径
     */
    @Override
    public String modelExport(ModelCommand modelSend) {
        try {
            TRobotInfo robotInfo = robotService.selectByPrimaryId(modelSend.getRobotId());
            long regionId = robotInfo.getUpRegionId();
            // 获取模型路径
            Map<String, String> modelMap = loadRegionFiles(regionId);
            String path;
            // 根据模型指令找出对应模型，返回给前端
            if (StringUtils.isNotEmpty(modelSend.getCommand())) {
                String pathType = COMMAND_MAP.get(modelSend.getCommand());
                path = modelMap.get(pathType);
                if (MODEL_FILE_XLSX.equals(pathType)) {
                    // 如果是设备模型，则需重新生成 excel 文件
                    path = generateDeviceExcel(regionId, path);
                }

                path = StringUtils.replace(path, SysParamConfig.getSysContent("fileAbsPath"), Constant.FILE_REAL_PATH);
            } else {
                // 模型指令为空，则导出所有模型，使用zip压缩文件夹
                String zipName = regionId + ".zip";
                String zipPath = CommonUtils.concatPath(SysParamConfig.getSysContent("zipPath"), zipName);
                path = CommonUtils.concatPath(Constant.ZIP_REAL_PATH, zipName);
                File zipFile = new File(zipPath);
                FileUtil.del(zipFile);
                // modelMap 过滤掉 xml 格式文件，然后转成 File
                String modelDir = simpleModelDir(regionId);
                // 如果excel文件不存在，则默认文件名为 model.xlsx
                String excelPath = modelMap.getOrDefault(MODEL_FILE_XLSX, CommonUtils.concatPath(modelDir, "model.xlsx"));
                generateDeviceExcel(regionId, excelPath);

                File[] files = modelMap.entrySet()
                    .stream()
                    .filter(entry -> !MODEL_FILE_XML.equals(entry.getKey()))
                    .map(entry -> new File(entry.getValue()))
                    .toArray(File[]::new);

                ZipUtil.zip(zipFile, false, files);
            }
            return path;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.CODE10010, "获取模型文件失败");
        }
    }

    /**
     * 压缩包解压缩
     * 文件名有中文会导致上传失败，捕捉到后再次使用 GBK 编码解压缩一次
     */
    private String extract(MultipartFile file) throws IOException {
        String outputPath = simpleModelDir(System.currentTimeMillis());

        // 临时文件暂存，直接用流无法二次操作，如果文件名有中文会导致解压失败
        File outTmpZip = new File(CommonUtils.concatPath(outputPath, file.getOriginalFilename()));
        FileUtil.mkdir(outputPath);
        file.transferTo(outTmpZip.getAbsoluteFile());
        // 解压缩，自适应压缩包
        try (Extractor extractor = CompressUtil.createExtractor(StandardCharsets.UTF_8, outTmpZip)) {
            extractor.extract(FileUtil.mkdir(outputPath));
        } catch (IORuntimeException e) {
            log.error("解压失败，可能是编码问题，切换 GB18030 再试一次: {}", e.getMessage());
            try (Extractor extractor = CompressUtil.createExtractor(Charset.forName("GB18030"), outTmpZip)) {
                extractor.extract(FileUtil.mkdir(outputPath));
            }
        }
        log.info("临时上传文件包: {}", outputPath);
        return outputPath;
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
        EasyExcelFactory.read(filePath, SimpleDeviceExcel.class, modelExcelListener).headRowNumber(1).sheet(0).doRead();

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
            regionSet.add(excelEntity.getAreaName());
            baySet.add(excelEntity.getBayName());
            bayToRegionMap.put(excelEntity.getBayName(), excelEntity.getAreaName());
            deviceSet.add(excelEntity.getDeviceName());
            deviceToBayMap.put(excelEntity.getDeviceName(), excelEntity.getBayName());
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
            if (Objects.nonNull(device) && device.getUpRegionId().equals(bayByNameMap.get(excelEntity.getBayName()).getRegionId())) {
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
                robot.setIsUse(isUse);
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

    }

    /**
     * 迁移文件
     * 将临时文件迁移至区域ID对应的文件夹下，并返回SVG的路径
     */
    public String migrationFile(Long regionId, Map<String, String> pathMap) {
        String outputPath = simpleModelDir(regionId);
        // 遍历文件夹，根据文件后缀名删除对应文件
        try (Stream<Path> pathStream = Files.walk(Paths.get(outputPath))) {
            pathStream.filter(path -> pathMap.containsKey(PathUtils.getExtension(path).toLowerCase())).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    log.error("删除失败: {}", p);
                }
            });
        } catch (IOException e) {
            log.error("删除文件夹内文件失败: {}", outputPath, e);
        }

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

    /**
     * 获取简易模型文件夹
     */
    private static @NotNull String simpleModelDir(Long regionId) {
        String outputPathParent = SysParamConfig.getSysContent("simpleModelPath");
        return outputPathParent + File.separator + regionId;
    }

    /**
     * 遍历文件夹，按文件后缀对文件分类
     */
    private Map<String, String> modelFileList(String modelDir) throws IOException {
        Map<String, String> pathMap = new HashMap<>(8);

        try {
            // 遍历文件夹，根据文件后缀名对应解析
            Files.walkFileTree(Paths.get(modelDir), new SimpleFileVisitor<Path>() {
                //遍历文件
                @Override
                @NotNull
                public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                    // 获取文件后缀名，根据后缀名做不同处理。后缀名包括las，xlsx，txt，svg
                    String suffix = PathUtils.getExtension(file).toLowerCase();
                    String filePath = file.normalize().toAbsolutePath().toString();
                    // 匹配 excel 文件，后缀为 xls xlsx
                    if (StringUtils.equalsAny(suffix, "xls", "xlsx")) {
                        pathMap.put(MODEL_FILE_XLSX, filePath);
                    } else if (StringUtils.equalsAny(suffix, MODEL_FILE_LAS, MODEL_FILE_TXT, MODEL_FILE_SVG, MODEL_FILE_XML)) {
                        pathMap.put(suffix, filePath);
                    }

                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            log.error("遍历文件失败 {}", e.getMessage());
        }

        return pathMap;
    }

    /**
     * 获取区域对应模型文件
     * @param regionId 区域ID
     * @return 文件列表
     * @throws IOException 遍历文件异常
     */
    public Map<String, String> loadRegionFiles(Long regionId) throws IOException {

        String outputPath = simpleModelDir(regionId);

        return modelFileList(outputPath);

    }

    /**
     * 获取设备模型
     * @param regionId 区域ID
     * @return 设备模型列表
     */
    public String pointModel(Long regionId) {
        String fileName = "simple_device_model.xml";
        try {
            List<SimpleDeviceModel> list = getBaseMapper().selectDeviceModel(regionId);

            // 查询区域的上级作为站所
            TStdRegion station = tStdRegionService.selectUpRegion(regionId);
            String stationCode = String.valueOf(station.getRegionId());
            String stationName = station.getRegionName();
            list.forEach(item -> {
                item.setStationCode(stationCode);
                item.setStationName(stationName);
                // 坐标数据库内以;号分割不同点，模型要求全部以,分割
                item.setMasterCoordinate(item.getMasterCoordinate().replace(";", ","));
                // 相应类型全部转成文档标准的值
                item.setMeterType(getUpDict("meterType", item.getMeterType()));
                item.setAppearanceType(getUpDict("appearanceType", item.getAppearanceType()));
                item.setRecognitionTypeList(getUpDict("meteType", item.getRecognitionTypeList()));//
                item.setDeviceType(getUpDict("deviceType", item.getDeviceType()));
                item.setVoltageLevel(DictConvertUtil.DICT.covertToDict("voltageLevel", item.getVoltageLevel()));
            });

            return PlatformXmlUtil.createXmlFileT(list, simpleModelDir(regionId), fileName, "Device_Model");
        } catch (IOException e) {
            log.error("导出数据模型文件失败: {}", fileName, e);
        }
        return null;
    }

    /**
     * 从数据库导出数据模型文件
     * @param regionId  区域ID
     * @param excelPath excel 路径
     * @return 文件路径
     */
    public String generateDeviceExcel(Long regionId, String excelPath) {
        List<SimpleDeviceModel> list = getBaseMapper().selectDeviceModel(regionId);
        List<SimpleDeviceExcel> excelDataList = new ArrayList<>((int)(list.size() * 1.4));

        // 查询区域的上级作为站所
        TStdRegion station = tStdRegionService.selectUpRegion(regionId);
        String stationCode = String.valueOf(station.getRegionId());
        String stationName = station.getRegionName();
        // 已存在元素集合
        Set<String> existId = new HashSet<>(list.size());
        list.forEach(item -> {
            item.setStationCode(stationCode);
            item.setStationName(stationName);

            // 相应类型全部转成文档标准的值
            item.setVoltageLevel(DictConvertUtil.DICT.covertToDict("voltageLevel", item.getVoltageLevel()));

            // 若元素已存在，则跳过这条数据
            if (!existId.add(item.getMainDeviceId())) {
                return;
            }
            // 组装 excel 数据
            SimpleDeviceExcel excel = new SimpleDeviceExcel();
            BeanUtil.copyProperties(item, excel);
            excel.setDeviceName(item.getMainDeviceName());
            // 坐标数据库内以,;号分割不同点，excel 需要拆分成单个点
            String[] coordinate = item.getMasterCoordinate().split("[;,]");
            // 一共4个点，每个点三个坐标，以供12个坐标值
            if (coordinate.length >= 12) {
                excel.setX1(NumberUtils.toDouble(coordinate[0]));
                excel.setY1(NumberUtils.toDouble(coordinate[1]));
                excel.setZ1(NumberUtils.toDouble(coordinate[2]));
                excel.setX2(NumberUtils.toDouble(coordinate[3]));
                excel.setY2(NumberUtils.toDouble(coordinate[4]));
                excel.setZ2(NumberUtils.toDouble(coordinate[5]));
                excel.setX3(NumberUtils.toDouble(coordinate[6]));
                excel.setY3(NumberUtils.toDouble(coordinate[7]));
                excel.setZ3(NumberUtils.toDouble(coordinate[8]));
                excel.setX4(NumberUtils.toDouble(coordinate[9]));
                excel.setY4(NumberUtils.toDouble(coordinate[10]));
                excel.setZ4(NumberUtils.toDouble(coordinate[11]));
            }
            excelDataList.add(excel);
        });
        FileUtil.mkParentDirs(excelPath);
        EasyExcelFactory.write(excelPath, SimpleDeviceExcel.class).sheet(stationName).doWrite(excelDataList);

        return excelPath;
    }

    /**
     * 获取对应字典内标准值，不存在则使用原始值
     */
    private String getUpDict(String colName, String dictCode) {
        String upDict = DictConvertUtil.DICT.getUpDictCode(colName, dictCode);
        return StringUtils.defaultIfEmpty(upDict, dictCode);
    }

}
