package com.yjh.platform.module.simple.service;

import com.yjh.platform.module.simple.entity.PatrolDeviceVersion;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <功能描述>
 *
 * @author shaobinfen
 * @date 2025/6/25
 * @since [产品/模块版本](可选)
 */
public interface SimpleUpgradeService {

    /**
     * 上传版本升级包到文件服务器
     * @param file 版本升级包
     * @param robotType 巡视设备型号
     * @param text 版本详细说明
     * @param userId 用户id
     */
    void uploadUpgradeFile(MultipartFile file, Integer robotType, String text, String userId) throws IOException;

    /**
     * 远程升级
     * @param id 版本包id
     * @param robotList 视设备编码列表(多个巡视设备以逗号分割)
     * @param userId 用户id
     */
    void upgradeNotify(Long id, String robotList, String userId);

    /**
     * 分页查询
     */
    List<PatrolDeviceVersion> selectPage(PatrolDeviceVersion patrolDeviceVersion);

    /**
     * 批量删除
     */
    void batchDelete(String ids);
}
