package com.yjh.platform.module.config.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.config.dao.SystemConfigDao;
import com.yjh.platform.module.config.entity.ConfigTreeNode;
import com.yjh.platform.module.config.entity.SystemConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final ApplicationProperties applicationProperties;

    private final RedisTemplate redisTemplate;

    private final String systemConfigKey="systemConfigKey:";

    public SystemConfigService(SystemConfigDao systemConfigDao,
                               ApplicationProperties applicationProperties,
                               RedisTemplate redisTemplate) {
        this.systemConfigDao = systemConfigDao;
        this.applicationProperties = applicationProperties;
        this.redisTemplate = redisTemplate;
    }

    public List<ConfigTreeNode> selectConfigInfo() {
        return systemConfigDao.selectConfigInfo();
    }

    public int update(SystemConfig systemConfig) {
        List<SystemConfig> systemConfigList = new ArrayList<>();
        systemConfigList.add(systemConfig);
        systemConfigInfoToRedis(systemConfigList);
        flushCatch();
        return systemConfigDao.update(systemConfig);
    }
    public int batchUpdate(List<SystemConfig> systemConfig) {
        systemConfigInfoToRedis(systemConfig);
        return systemConfigDao.batchUpdate(systemConfig);
    }

    private void systemConfigInfoToRedis(List<SystemConfig> configList){
        configList.forEach(systemConfig ->{
            Map<String,String> map = new HashMap<>();
            map.put(systemConfig.getConfigKey(),systemConfig.getConfigValue());
            redisTemplate.opsForHash().putAll(systemConfigKey+systemConfig.getConfigType(),map);
        });
    }

    public void flushCatch() {
        applicationProperties.flush();
    }

    public List<SystemConfig> selectAll() {
        return systemConfigDao.selectAll();
    }
}
