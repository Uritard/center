package com.yjh.platform.module.simple.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.algorithm.FtpsService;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.enums.UpgradeStatusEnum;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.feign.RobotProxy;
import com.yjh.platform.module.simple.dao.SimpleUpgradeMapper;
import com.yjh.platform.module.simple.entity.PatrolDeviceVersion;
import com.yjh.platform.module.simple.service.SimpleUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author shaobinfen
 * @date 2025/6/25
 * @since [产品/模块版本](可选)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleUpgradeServiceImpl extends ServiceImpl<SimpleUpgradeMapper, PatrolDeviceVersion> implements SimpleUpgradeService {

    private final RedisTemplate redisTemplate;

    private final RobotProxy robotProxy;

    private final FtpsService ftpsService;

    @SuppressWarnings("unchecked")
    @Override
    public void uploadUpgradeFile(MultipartFile file, Integer robotType, String text, String userId) throws IOException {
        if (!FileUtil.isZipFile(file) && !FileUtil.isTarFile(file))
            throw new BusinessException("文件类型只支持zip或者tar格式!");
        String userName = Convert.toStr(redisTemplate.opsForHash().get("userInfo:" + userId, "userName"));
        String packagePathParent = SysParamConfig.getSysContent("upgradePackagePath");
        String upgradePackagePath = packagePathParent + File.separator + DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);
        Path path = Paths.get(upgradePackagePath);
        if (Files.notExists(path)) Files.createDirectories(path);
        String fileName = file.getOriginalFilename();
        Path packageFullPath = path.resolve(Objects.requireNonNull(fileName));
        file.transferTo(packageFullPath.toFile());
        log.info("系统版本升级包上传成功, 本地路径: {}", packageFullPath);
        PatrolDeviceVersion patrolDeviceVersion = new PatrolDeviceVersion()
                .setName(FilenameUtils.removeExtension(fileName))
                .setRobotType(robotType)
                .setText(text)
                .setFilePath(packageFullPath.toString())
                .setCreateUser(userName)
                .setCreateTime(LocalDateTime.now());
        getBaseMapper().insertSelective(patrolDeviceVersion);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void upgradeNotify(Long id, String robotList, String userId) {
        PatrolDeviceVersion patrolDeviceVersion = getBaseMapper().selectByPrimaryId(id);
        String packageFullPath = patrolDeviceVersion.getFilePath();
        Path path = Paths.get(packageFullPath);
        if (Files.notExists(path))
            throw new BusinessException("本地版本升级包: " + path.getFileName() + "不存在!");
        List<String> list = Arrays.stream(robotList.split(",")).collect(Collectors.toList());
        list.removeIf(robotCode -> {
            String upgradeStatus = Convert.toStr(redisTemplate.opsForValue().get("UpgradeStatus:" + robotCode), "");
            if (StrUtil.isEmpty(upgradeStatus) || StrUtil.equals(upgradeStatus, UpgradeStatusEnum.UPGRADE_COMPLETE.getDesc())) {
                redisTemplate.opsForValue().set("UpgradeStatus:" + robotCode, UpgradeStatusEnum.START_UPGRADE.getDesc(), 1, TimeUnit.HOURS);
                return false;
            } else {
                log.info("机器人: {}正在升级中, 当前状态: {}", robotCode, upgradeStatus);
                return true;
            }
        });
        if (CollectionUtil.isNotEmpty(list)) {
            ThreadPoolUtil.COMMON_POOL.addThread(() -> {
                String remoteFilePath = Constant.UPGRADE_PACKAGE_PATH + File.separator + DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN)
                        + File.separator + FilenameUtils.getName(packageFullPath);
                if (ftpsService.notExists(remoteFilePath)) {
                    log.info("文件: {}不存在,先上传...", remoteFilePath);
                    ftpsService.uploadAnalysisFile(remoteFilePath, packageFullPath);
                }
                list.forEach(robotCode -> {
                    redisTemplate.opsForValue().set("UpgradeStatus:" + robotCode, UpgradeStatusEnum.PACKAGE_UPLOADED.getDesc(), 1, TimeUnit.HOURS);
                    Result result = robotProxy.upgradeSend(robotCode, userId, remoteFilePath);
                    log.info("巡视设备远程升级调用robot服务返回结果: {}", result.toString());
                });
            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<PatrolDeviceVersion> selectPage(PatrolDeviceVersion patrolDeviceVersion) {
        return getBaseMapper().selectByPage(patrolDeviceVersion);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDelete(String ids) {
        Arrays.stream(ids.split(",")).map(Long::parseLong).forEach(id -> {
            PatrolDeviceVersion patrolDeviceVersion = getBaseMapper().selectByPrimaryId(id);
            String filePath = patrolDeviceVersion.getFilePath();
            Path path = Paths.get(filePath);
            try {
                Files.deleteIfExists(path);
                getBaseMapper().deleteByPrimaryId(id);
            } catch (IOException ioe) {
                log.error("文件: {}删除失败, 保留id为: {}的数据", filePath, id, ioe);
            }
        });
    }
}
