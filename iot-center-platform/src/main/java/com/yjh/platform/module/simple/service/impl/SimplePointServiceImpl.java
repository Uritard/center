package com.yjh.platform.module.simple.service.impl;

import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Maps;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import cn.hutool.core.io.FileUtil;
import com.yjh.platform.common.utils.ImageSplitUtil;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.service.TRobotInspectionService;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.simple.entity.BasePhotoBuild;
import com.yjh.platform.module.simple.entity.BasePhotoInfo;
import com.yjh.platform.module.simple.service.SimplePointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static final String ORIGINAL_PIC = "bigi_0.jpg";

    private final TRobotInspectionService tRobotInspectionService;
    private final TStdDeviceService stdDeviceService;

    /**
     * 获取底图
     *
     * @param deviceId 设备id
     * @return BasePhotoInfo
     */
    @Override
    public BasePhotoInfo basePhoto(Long deviceId) {
        TStdDevice stdDevice = stdDeviceService.selectByPrimaryId(deviceId);
        String photoPath =
            ImageSplitUtil.convertPath(stdDevice.getRegionPath(), Constant.SIMPLE_PIC, SysParamConfig.getSysContent("simplePicPath"));
        if (StringUtils.isBlank(photoPath)) {
            throw new BusinessException("该设备没有底图！");
        }
        File photoFile = new File(photoPath);
        if (!photoFile.exists()) {
            throw new BusinessException("底图文件不存在！");
        }
        try {
            //将底图拷贝到临时目录下
            String tempPath = photoFile.getParent() + File.separator + TEMP_PATH + File.separator + photoFile.getName();
            File tempFile = new File(tempPath);
            FileUtil.mkParentDirs(tempFile);
            FileUtil.copyFile(photoFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
            //拆图
            List<String> mainPaths = ImageSplitUtil.splitImage(tempFile, 2, ImageSplitUtil.ImageSplitType.VERTICAL, BASE_PREFIX);
            return new BasePhotoInfo().setMainPath(mainPaths.get(0)).setSparePath(mainPaths.get(1));
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
        String mainPath = ImageSplitUtil.convertPath(basePhotoInfo.getMainPath(), Constant.SIMPLE_PIC,
                SysParamConfig.getSysContent("simplePicPath"));
        File file = new File(mainPath);

        if (!file.exists()) {
            throw new BusinessException("底图文件不存在！");
        }

        Map<String, Object> result = Maps.newHashMap();

        try {
            List<String> splitImageList =
                ImageSplitUtil.splitImage(file, splitNum, ImageSplitUtil.ImageSplitType.HORIZONTAL, MAIN_POINT_PREFIX);
            result.put("mainPaths", splitImageList);
            if (StringUtils.isNotBlank(basePhotoInfo.getSparePath())) {
                String sparePath = ImageSplitUtil.convertPath(basePhotoInfo.getSparePath(), Constant.SIMPLE_PIC,
                    SysParamConfig.getSysContent("simplePicPath"));
                File spareFile = new File(sparePath);
                if (spareFile.exists()) {
                    List<String> splitSpareImageList =
                        ImageSplitUtil.splitImage(spareFile, splitNum, ImageSplitUtil.ImageSplitType.HORIZONTAL, SPARE_POINT_PREFIX);
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
     * 底图保存
     *
     * @param buildList 底图信息
     * @return Boolean
     */
    @Override
    public Boolean basePhotoBuild(List<BasePhotoBuild> buildList) {
        if (buildList == null || buildList.size() == 0) {
            throw new BusinessException("底图信息不能为空！");
        }
        String simplePicPath = SysParamConfig.getSysContent("simplePicPath");
        List<TRobotInspection> robotInspections = new ArrayList<>();
        Long deviceId = buildList.get(0).getDeviceId();
        String devicePath = simplePicPath + File.separator + deviceId + File.separator;
        List<TRobotInspection> inspections =
            tRobotInspectionService.selectByPage(new TRobotInspection().setMainDeviceId(String.valueOf(deviceId)));
        try {
            String componentId = IdUtil.getSnowflakeNextIdStr();
            for (BasePhotoBuild build : buildList) {
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
                robotInspection.setInspectionType(1).setInspectionCode(inspectionCode).setRobotId(build.getRobotId()).setSaveTypeList("jpg")
                    .setInspectionName(build.getDeviceName() + "/" + build.getPhotoName()).setPhotoNum(build.getPhotoNum())
                    .setMainDeviceId(String.valueOf(build.getDeviceId())).setComponentId(componentId)
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
}
