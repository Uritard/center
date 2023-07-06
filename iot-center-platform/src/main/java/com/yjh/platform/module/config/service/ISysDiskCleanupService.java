package com.yjh.platform.module.config.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yjh.platform.module.config.entity.CleanStep;
import com.yjh.platform.module.config.entity.SysDiskCleanup;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 磁盘清理记录 服务类
 * </p>
 *
 * @author Chenfei
 * @since 2023-06-30
 */
public interface ISysDiskCleanupService extends IService<SysDiskCleanup> {

    Map<String, Object> runningCleanup();

    boolean addTask(SysDiskCleanup sysDiskCleanup);

    boolean recoveryTask(int cleanId, int type);

    boolean deleteBack(int cleanId);
}
