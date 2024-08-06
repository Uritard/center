package com.yjh.platform.module.config.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONString;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yjh.platform.module.config.dao.HomeModelConfigMapper;
import com.yjh.platform.module.config.entity.HomeModelConfig;
import com.yjh.platform.module.config.entity.HomeModelConfigVO;
import com.yjh.platform.module.config.entity.ModelInfo;
import com.yjh.platform.module.config.service.HomeModelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HomeModelConfigServiceImpl implements HomeModelConfigService {
    @Resource
    private HomeModelConfigMapper homeModelConfigMapper;
    @Override
    public List selectByUserId(Long userId) {
        List<HomeModelConfig> homeModelConfigList = homeModelConfigMapper.selectList(
                new QueryWrapper<HomeModelConfig>().lambda().in(HomeModelConfig::getUserId, userId, 10000L));
        Map<Long, HomeModelConfig> config = homeModelConfigList.stream()
                .collect(Collectors.toMap(HomeModelConfig::getUserId, Function.identity(), (a, b) -> a));
        if (config.get(userId) != null) {
            return JSONUtil.parseArray(config.get(userId).getModelConfig()).toList(ModelInfo.class);
        } else {
            return JSONUtil.parseArray(config.get(10000L).getModelConfig()).toList(ModelInfo.class);
        }
    }

    @Override
    public Boolean updateByUserId(HomeModelConfigVO homeModelConfig) {
        homeModelConfigMapper.delete(new QueryWrapper<HomeModelConfig>().lambda().eq(HomeModelConfig::getUserId, homeModelConfig.getUserId()));
        String jsonStr = JSONUtil.toJsonStr(homeModelConfig.getModelConfig());
        HomeModelConfig dto = new HomeModelConfig();
        dto.setModelConfig(jsonStr);
        dto.setUserId(homeModelConfig.getUserId());
        homeModelConfigMapper.insert(dto);
        return true;
    }
}
