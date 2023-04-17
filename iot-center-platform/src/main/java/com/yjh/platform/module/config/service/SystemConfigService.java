package com.yjh.platform.module.config.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.module.config.dao.SystemConfigDao;
import com.yjh.platform.module.config.entity.ConfigTreeNode;
import com.yjh.platform.module.config.entity.SystemConfig;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 系统配置表 服务实现类
 * </p>
 *
 * @author
 * @since 2023-04-12
 */
@Service
public class SystemConfigService {

    private final SystemConfigDao systemConfigDao;

    public SystemConfigService(SystemConfigDao systemConfigDao) {
        this.systemConfigDao = systemConfigDao;
    }

    public List<ConfigTreeNode> selectConfigInfo() {
        return systemConfigDao.selectConfigInfo();
    }

    public int update(SystemConfig systemConfig) {
        flushCatch();
        return systemConfigDao.update(systemConfig);
    }
    public int batchUpdate(List<SystemConfig> systemConfig) {
        flushCatch();
        return systemConfigDao.batchUpdate(systemConfig);
    }

    public void flushCatch() {

    }

    public List<SystemConfig> selectAll() {
        return systemConfigDao.selectAll();
    }
}
