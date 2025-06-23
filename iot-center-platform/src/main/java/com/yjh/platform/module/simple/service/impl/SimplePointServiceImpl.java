package com.yjh.platform.module.simple.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.yjh.commons.DateFormat;
import com.yjh.commons.DateUtils;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import cn.hutool.core.io.FileUtil;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.common.utils.ImageSplitUtil;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.service.TCruisePointInstanceService;
import com.yjh.platform.module.device.service.TRobotInspectionService;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdDevicemeteService;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.entity.CalibrationData;
import com.yjh.platform.module.patrol.service.IntelAnalysisService;
import com.yjh.platform.module.simple.entity.*;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.service.UPatrolResultService;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.simple.entity.BasePhotoBuild;
import com.yjh.platform.module.simple.entity.BasePhotoInfo;
import com.yjh.platform.module.simple.entity.InitialTaskStatus;
import com.yjh.platform.module.simple.service.SimplePointService;
import com.yjh.platform.module.task.entity.TCruiseTaskAdd;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.service.TRobotInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.*;

import static com.yjh.platform.module.patrol.CruiseConstant.*;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/3
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimplePointServiceImpl implements SimplePointService {

    /**
     * 临时路径
     */
    private static final String TEMP_PATH = "temp";
    /**
     * 主图分割命名前缀
     */
    private static final String BASE_PREFIX = "base_";
    /**
     * 主图点位分割命名前缀
     */
    private static final String MAIN_POINT_PREFIX = "mainPoint_";
    /**
     * 备图点位分割命名前缀
     */
    private static final String SPARE_POINT_PREFIX = "sparePoint_";
    /**
     * 标定原图
     */
    private static final String ORIGINAL_PIC = "bigi_0.jpg";
    /**
     * 标定文件
     */
    private static final String T_MODEL_JSON = "tmodel.json";

    private final TRobotInspectionService tRobotInspectionService;
    private final TStdDeviceService stdDeviceService;
    private final UPatrolTaskService uPatrolTaskService;
    private final TRobotInfoService trobotInfoService;
    private final UPatrolResultDao uPatrolResultDao;
    private final TRobotInfoDao tRobotInfoDao;
    private final TStdDevicemeteService stdDeviceMeteService;
    private final TCruisePointInstanceService cruisePointInstanceService;
    private final IntelAnalysisService analysisService;
    private final RedisTemplate redisTemplate;

    /**
     * 获取底图
     *
     * @param deviceId 设备id
     * @return BasePhotoInfo
     */
    @Override
    public BasePhotoInfo basePhoto(Long deviceId) {
        TStdDevice stdDevice = stdDeviceService.selectByPrimaryId(deviceId);
        if (StringUtils.isBlank(stdDevice.getRegionPath())) {
            throw new BusinessException("该设备目前没有底图！");
        }
        String photoPath =
            ImageSplitUtil.convertPath(stdDevice.getRegionPath(), Constant.SIMPLE_PIC, SysParamConfig.getSysContent("simplePicPath"));
        File photoFile = new File(photoPath);
        if (!photoFile.exists()) {
            throw new BusinessException("底图文件不存在！");
        }
        try {
            //拆图
            int splitNum = 2;
            String[] mainPaths = ImageSplitUtil.splitImage(photoFile, splitNum, ImageSplitUtil.ImageSplitType.VERTICAL, BASE_PREFIX);
            if (mainPaths.length != splitNum) {
                throw new BusinessException("底图分割异常！");
            }
            return new BasePhotoInfo().setMainPath(mainPaths[0]).setSparePath(mainPaths[1]);
        } catch (Exception e) {
            log.error("底图处理异常！", e);
            throw new BusinessException("底图处理异常！");
        }
    }

    /**
     * 底图拆分
     *
     * @param basePhotoInfo 底图信息
     * @return Map
     */
    @Override
    public Map<String, Object> splitPhoto(BasePhotoInfo basePhotoInfo) {
        if (StringUtils.isBlank(basePhotoInfo.getMainPath())) {
            throw new BusinessException("底图路径不能为空");
        }

        Integer splitNum = basePhotoInfo.getSplitNum();
        if (splitNum == null || splitNum <= 0) {
            throw new BusinessException("拆分数量必须大于0");
        }

        // 转换获取绝对路径
        String mainPath =
            ImageSplitUtil.convertPath(basePhotoInfo.getMainPath(), Constant.SIMPLE_PIC, SysParamConfig.getSysContent("simplePicPath"));
        File file = new File(mainPath);

        if (!file.exists()) {
            throw new BusinessException("底图文件不存在！");
        }

        Map<String, Object> result = Maps.newHashMap();

        try {
            //将底图拷贝到临时目录下
            File mainTempFile = copyFile(file);
            String[] splitImageList =
                ImageSplitUtil.splitImage(mainTempFile, splitNum, ImageSplitUtil.ImageSplitType.HORIZONTAL, MAIN_POINT_PREFIX);
            result.put("mainPaths", splitImageList);
            if (StringUtils.isNotBlank(basePhotoInfo.getSparePath())) {
                String sparePath = ImageSplitUtil.convertPath(basePhotoInfo.getSparePath(), Constant.SIMPLE_PIC,
                    SysParamConfig.getSysContent("simplePicPath"));
                File spareFile = new File(sparePath);
                if (spareFile.exists()) {
                    //将底图拷贝到临时目录下
                    File splitTempFile = copyFile(spareFile);
                    String[] splitSpareImageList =
                        ImageSplitUtil.splitImage(splitTempFile, splitNum, ImageSplitUtil.ImageSplitType.HORIZONTAL, SPARE_POINT_PREFIX);
                    result.put("sparePaths", splitSpareImageList);
                }
            }
            return result;
        } catch (IOException e) {
            log.error("底图拆分发生IO异常！mainPath={}, splitNum={}", mainPath, splitNum, e);
            throw new BusinessException("底图拆分发生IO异常！");
        } catch (Exception e) {
            log.error("底图拆分发生未知异常！mainPath={}, splitNum={}", mainPath, splitNum, e);
            throw new BusinessException("底图拆分异常！");
        }
    }

    /**
     * 拷贝文件
     *
     * @param file 文件
     * @return File
     */
    private File copyFile(File file) {
        //将底图拷贝到临时目录下
        String tempPath = file.getParent() + File.separator + TEMP_PATH + File.separator + file.getName();
        File tempFile = new File(tempPath);
        FileUtil.mkParentDirs(tempFile);
        FileUtil.copyFile(file, tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    /**
     * 底图保存
     *
     * @param photoBuild 底图信息
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean basePhotoBuild(BasePhotoBuild photoBuild) {
        if (CollectionUtils.isEmpty(photoBuild.getPhotoInfo())) {
            throw new BusinessException("底图信息不能为空！");
        }
        String simplePicPath = SysParamConfig.getSysContent("simplePicPath");
        List<TRobotInspection> robotInspections = new ArrayList<>();
        Long deviceId = photoBuild.getDeviceId();
        String devicePath = simplePicPath + File.separator + deviceId + File.separator;
        List<TRobotInspection> inspections =
            tRobotInspectionService.selectByPage(new TRobotInspection().setMainDeviceId(String.valueOf(deviceId)));
        try {
            String componentId = IdUtil.getSnowflakeNextIdStr();
            for (PointPhotoInfo build : photoBuild.getPhotoInfo()) {
                String inspectionCode = IdUtil.getSnowflakeNextIdStr();
                String sourcePath = ImageSplitUtil.convertPath(build.getPhotoPath(), Constant.SIMPLE_PIC, simplePicPath);
                File sourceFile = new File(sourcePath);
                if (!sourceFile.exists()) {
                    throw new BusinessException("底图文件不存在！");
                }
                //目标路径确认
                String targetPath = devicePath + inspectionCode + File.separator + ORIGINAL_PIC;
                FileUtil.mkParentDirs(targetPath);
                //将临时文件拷贝到简易测点图片目录下
                FileUtil.copyFile(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                TRobotInspection robotInspection = new TRobotInspection();
                robotInspection.setInspectionType(1).setInspectionCode(inspectionCode).setRobotId(photoBuild.getRobotId())
                    .setSaveTypeList("jpg").setInspectionName(photoBuild.getDeviceName() + "/" + build.getPhotoName())
                    .setPhotoNum(build.getPhotoNum()).setMainDeviceId(String.valueOf(photoBuild.getDeviceId())).setComponentId(componentId)
                    .setPropertyPicPath(ImageSplitUtil.convertPath(targetPath, simplePicPath, Constant.SIMPLE_PIC));
                robotInspections.add(robotInspection);
            }
            int res = tRobotInspectionService.batchInsert(robotInspections);
            if (res > 0) {
                //如果该点位之前有数据，则删除
                if (CollectionUtils.isNotEmpty(inspections)) {
                    for (TRobotInspection inspection : inspections) {
                        FileUtil.del(ImageSplitUtil.convertPath(inspection.getPropertyPicPath(), Constant.SIMPLE_PIC, simplePicPath));
                        tRobotInspectionService.deleteByPrimaryId(inspection.getInspectionId());
                    }
                }
                //更新设备名称 + 偏移量
                TStdDevice stdDevice = new TStdDevice();
                stdDevice.setDeviceId(photoBuild.getDeviceId()).setDeviceName(photoBuild.getDeviceName());
                stdDeviceService.update(stdDevice);
                //清理临时文件
                FileUtil.clean(devicePath + TEMP_PATH);
            }
            return res > 0;
        } catch (Exception e) {
            log.error("底图保存异常！", e);
            robotInspections.forEach(
                f -> FileUtil.del(ImageSplitUtil.convertPath(f.getPropertyPicPath(), Constant.SIMPLE_PIC, simplePicPath)));
            throw new BusinessException("底图保存异常！");
        } finally {
            //清理临时文件
            FileUtil.cleanEmpty(new File(devicePath));
        }
    }

        /**
     * si300初始任务下发
     *
     * @param devicesId 屏柜Id
     * @param robotId 机器人id
     * @return Map
     */
    @Override
    public Map<String, Object> initTaskAdd(List<Long> devicesId, Long robotId) {
        InitialTaskStatus initialTaskStatus = initTaskStatus(robotId, null);
        if ( initialTaskStatus.getInitStatus() == TASK_STATE_EXECUTING || initialTaskStatus.getInitStatus() == TASK_STATE_NOT_START) {
            throw new BusinessException("当前机器人有未完成的初始任务！");
        }
        String deviceList = StringUtils.join(devicesId, ",");
        if (StringUtils.isBlank(deviceList)) {
            //当机器人没有绑定屏柜时，默认使用机器人的上级区域下的所有屏柜
            Long upRegionId = trobotInfoService.selectByPrimaryId(robotId).getUpRegionId();
            List<TStdDevice> stdDevices = stdDeviceService.select(null, null, null, null, null, null, null, null, upRegionId, null, null, null, null, null);
            deviceList = stdDevices.stream().map(TStdDevice::getDeviceId).map(String::valueOf).collect(Collectors.joining(","));
        }
        TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
        //初始任务均为立即任务
        tCruiseTaskAdd.setTaskName("si300初始任务_" + DateUtils.dateToString(new Date(), DateFormat.YYYYMMDDHHMMSS)).setRobotId(robotId).setDeviceList(deviceList).setIfRun(CruiseConstant.TaskTypeEnum.NOW.getType()).setType(INITIAL_PATROL);

        return uPatrolTaskService.addTask(tCruiseTaskAdd, true);
    }

    @Override
    public InitialTaskStatus initTaskStatus(Long robotId, Long regionId) {
        if  (regionId != null) {
            robotId = tRobotInfoDao.selectByRegionId(regionId).getRobotId();
        }

        InitialTaskStatus initialTaskStatus = uPatrolResultDao.initialTaskStatusQueryByRobotId(robotId);
        initialTaskStatus.setInitStatusName(DictConvertUtil.DICT.covertToDict("taskState", initialTaskStatus.getInitStatus()));
        return initialTaskStatus;
    }


    /**
     * 子点位标定数据保存
     *
     * @param dataBuild 标定数据信息
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean calibrationDataBuild(CalibrationDataBuild dataBuild) {
        if (CollectionUtils.isEmpty(dataBuild.getDeviceConfig())) {
            throw new BusinessException("子点位标定数据不能为空！");
        }
        TRobotInspection robotInspection = tRobotInspectionService.selectByPrimaryId(dataBuild.getInspectionId());
        if (robotInspection == null) {
            throw new BusinessException("外观点位信息不存在！");
        }
        //清理子点位标定数据
        cruisePointInstanceService.deleteByCruiseId(robotInspection.getInspectionId());
        stdDeviceMeteService.deleteByComponentId(robotInspection.getComponentId());

        List<DeviceConfig> deviceConfigList = dataBuild.getDeviceConfig();
        List<TStdDeviceMete> deviceMeteList = new ArrayList<>(deviceConfigList.size());
        for (DeviceConfig config : deviceConfigList) {
            AnalyseMeteTypeEnum typeEnum = AnalyseMeteTypeEnum.findAny(config.getDevType());
            TStdDeviceMete stdDeviceMete = new TStdDeviceMete();
            stdDeviceMete.setDeviceId(Long.valueOf(robotInspection.getMainDeviceId())).setMeteName(config.getDevName()).setCustomId("700")
                .setCustomName("本体").setMeteType(String.valueOf(typeEnum.getCode())).setAnalyseType(typeEnum.getType()).setIsAi("off")
                .setIsJudge("off").setIsTemdif(0).setInspectionType("1").setPositionType("simple").setRedundantType("1")
                .setComponentId(robotInspection.getComponentId()).setDevicePointId("");
            deviceMeteList.add(stdDeviceMete);
        }
        int resMete = stdDeviceMeteService.batchAdd(deviceMeteList);
        if (resMete > 0) {
            List<TCruisePointInstance> cruisePointInstanceList = new ArrayList<>(resMete);
            for (int i = 0; i < deviceMeteList.size(); i++) {
                TCruisePointInstance cruisePointInstance = new TCruisePointInstance();
                cruisePointInstance.setDeviceMeteId(deviceMeteList.get(i).getDeviceMeteId())
                    .setDeviceId(deviceMeteList.get(i).getDeviceId()).setCruiseId(robotInspection.getInspectionId())
                    .setCruiseName(deviceMeteList.get(i).getMeteName()).setCruiseType(CruiseConstant.TypeEnum.ROBOT.getCode());
                cruisePointInstanceList.add(cruisePointInstance);
                if (deviceConfigList.get(i).getDevType().equals(AnalyseMeteTypeEnum.NEW_SX_BJ.getName())) {
                    if (CollectionUtils.isEmpty(deviceConfigList.get(i).getParams())) {
                        throw new BusinessException(deviceConfigList.get(i).getDevName() + "的标定参数不能为空！");
                    }
                    deviceConfigList.get(i).getParams().get(0).setDevUuid(deviceConfigList.get(i).getDevUuid());
                }
                deviceConfigList.get(i).setDevUuid(String.valueOf(deviceMeteList.get(i).getDeviceMeteId()));
            }
            int resInstance = cruisePointInstanceService.batchInsert(cruisePointInstanceList);
            if (resInstance > 0) {
                //更新点位的标定信息
                TRobotInspection deviceInfo = new TRobotInspection();
                deviceInfo.setInspectionId(robotInspection.getInspectionId()).setDeviceInfo(JSON.toJSONString(deviceConfigList));
                int a = tRobotInspectionService.update(deviceInfo);
                if (a > 0) {
                    // 调用算法模块 更新算法标定信息
                    robotInspection.setDeviceInfo(JSON.toJSONString(deviceConfigList));
                    ThreadPoolUtil.PATROL_POOL.addThread(() -> calibrationDataUpload(robotInspection));
                }
                return a > 0;
            }
        }
        return false;
    }

    /**
     * 调用算法模块 添加标定信息
     *
     * @param robotInspection 标定信息
     */
    private void calibrationDataUpload(TRobotInspection robotInspection) {
        CalibrationData data = buildCalibrationData(robotInspection);
        boolean res = analysisService.calibrationDataUpload(Collections.singletonList(data));
        if (res) {
            log.info("上传标定文件成功！");
        }
    }

    /**
     * 构建标定数据
     *
     * @param robotInspection 巡检信息
     * @return CalibrationData
     */
    private CalibrationData buildCalibrationData(TRobotInspection robotInspection) {
        String picPath = robotInspection.getPropertyPicPath();
        if (StringUtils.isBlank(picPath)) {
            log.error("上传标定文件，基础图片为空！");
            return null;
        }
        picPath = picPath.replace(Constant.SIMPLE_PIC, SysParamConfig.getSysContent("simplePicPath"));
        File file = new File(picPath);
        if (!file.exists()) {
            log.error("上传标定文件，基础图片不存在！");
            return null;
        }
        String filePath = file.getParent() + File.separator + T_MODEL_JSON;
        FileUtil.writeUtf8String(JSONUtil.beautifyJson(StrUtil.toUnderlineCase(robotInspection.getDeviceInfo())), filePath);
        CalibrationData data = new CalibrationData();
        data.setTemplateId(String.valueOf(robotInspection.getInspectionId())).setPicPath(picPath).setFilePath(filePath);
        return data;
    }

    /**
     * 获取子点位标定数据
     *
     * @param inspectionId 巡检id
     * @return CalibrationDataBuild
     */
    @Override
    public List<DeviceConfig> selectCalibrationDataBuild(Long inspectionId) {
        TRobotInspection robotInspection = tRobotInspectionService.selectByPrimaryId(inspectionId);
        List<DeviceConfig> deviceConfigList = new ArrayList<>();
        if (robotInspection != null && StringUtils.isNotBlank(robotInspection.getDeviceInfo())) {
            deviceConfigList = JSON.parseArray(robotInspection.getDeviceInfo(), DeviceConfig.class);
        }
        return deviceConfigList;
    }

    /**
     * 标定数据批量上传
     *
     * @param inspectionIds 巡检id
     * @return Boolean
     */
    @Override
    public Boolean calibrationDataBatchUpload(List<Long> inspectionIds) {
        List<TRobotInspection> robotInspectionList = tRobotInspectionService.selectByInspectionIds(inspectionIds);
        List<CalibrationData> calibrationDataList = new ArrayList<>();
        robotInspectionList.forEach(robotInspection -> {
            CalibrationData data = buildCalibrationData(robotInspection);
            if (Objects.nonNull(data)) {
                calibrationDataList.add(data);
            }
        });
        if (calibrationDataList.isEmpty()) {
            return true;
        }
        return analysisService.calibrationDataUpload(calibrationDataList);
    }

    /**
     * 测试分析
     *
     * @param inspectionId 巡检id
     * @return Boolean
     */
    @Override
    public Boolean analyseTest(Long inspectionId, Long userId) {
        TRobotInspection robotInspection = tRobotInspectionService.selectByPrimaryId(inspectionId);
        String picPath = robotInspection.getPropertyPicPath().replace(Constant.SIMPLE_PIC, SysParamConfig.getSysContent("simplePicPath"));
        File file = new File(picPath);
        if (!file.exists()) {
            log.error("基础图片不存在！");
            throw new BusinessException("基础图片不存在！");
        }
        // 调用算法接口分析结果
        List<Analysis> analysisList = new ArrayList<>();
        Analysis analysis = new Analysis().setAnalyseType(AnalyseMeteTypeEnum.NEW_METER.getName()).setInstanceId(inspectionId)
            .setTaskId(AnalyseTypeEnum.ANALYSE_NEW_METER_TEST.getName() + "_" + robotInspection.getInspectionId() + "_" + userId)
            .setPicPath(picPath).setPicModelPath("/" + inspectionId);
        analysisList.add(analysis);
        return analysisService.analyseTest(analysisList);
    }

    /**
     * 查询巡视点位的算法分析结果数据
     *
     * @param inspectionId 巡检id
     * @return 算法分析结果数据
     */
    @Override
    public List<MeterAnalyseResult> selectMeterAnalyseResult(Long inspectionId) {
        String key = AnalyseTypeEnum.ANALYSE_NEW_METER_TEST.getName() + ":" + inspectionId;
        Map<String, String> meterAnalyseMapList = redisTemplate.opsForHash().entries(key);
        List<MeterAnalyseResult> meterAnalyseList = new ArrayList<>();
        meterAnalyseMapList.forEach((k, v) -> {
            MeterAnalyseResult meterAnalyseResult = new MeterAnalyseResult();
            meterAnalyseResult.setId(k);
            meterAnalyseResult.setValue(v);
            meterAnalyseList.add(meterAnalyseResult);
        });
        return meterAnalyseList;
    }
}
