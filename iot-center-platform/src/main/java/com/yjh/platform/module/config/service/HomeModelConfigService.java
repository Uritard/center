package com.yjh.platform.module.config.service;

import com.yjh.platform.module.config.entity.HomeModelConfig;
import com.yjh.platform.module.config.entity.HomeModelConfigVO;

import java.util.List;

public interface HomeModelConfigService {
    /**
     * 根据用户查询配置
     * @return
     */
    List selectByUserId(Long userId);

    Boolean updateByUserId(HomeModelConfigVO homeModelConfig);

}
