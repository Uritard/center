package com.yjh.platform.module.modelconf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.module.modelconf.entity.StdModelConfig;
import com.yjh.platform.module.modelconf.service.StdModelConfigService;
import com.yjh.platform.module.modelconf.dao.StdModelConfigMapper;
import org.springframework.stereotype.Service;

/**
* @author huyuhang
* @description 针对表【std_model_config(模型配置表)】的数据库操作Service实现
* @createDate 2025-04-15 11:20:30
*/
@Service
public class StdModelConfigServiceImpl extends ServiceImpl<StdModelConfigMapper, StdModelConfig>
    implements StdModelConfigService{

    @Override
    public StdModelConfig getDefaultConfig(Long droneId) {
        QueryWrapper<StdModelConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StdModelConfig::getStatus, 1).eq(StdModelConfig::getDroneId, droneId).last("limit 1");
        return getBaseMapper().selectOne(queryWrapper);
    }
}




