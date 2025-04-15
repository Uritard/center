package com.yjh.platform.module.modelconf.service;

import com.yjh.platform.module.modelconf.entity.StdModelConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author huyuhang
* @description 针对表【std_model_config(模型配置表)】的数据库操作Service
* @createDate 2025-04-15 11:20:30
*/
public interface StdModelConfigService extends IService<StdModelConfig> {

    /**
     * 获取默认的模型配置信息
     * @param droneId 无人机id
     * @return 配置信息
     */
    StdModelConfig getDefaultConfig(Long droneId);
}
