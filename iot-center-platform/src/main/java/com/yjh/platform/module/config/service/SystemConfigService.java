package com.yjh.platform.module.config.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.IPUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.config.dao.SystemConfigDao;
import com.yjh.platform.module.config.entity.ConfigTreeNode;
import com.yjh.platform.module.config.entity.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
@Service
public class SystemConfigService {

    private final SystemConfigDao systemConfigDao;

    private final ApplicationProperties applicationProperties;

    private final RedisTemplate redisTemplate;


    @Value("${spring.application.name}")
    private String appName;
    @Value("${server.port}")
    private String appPort;

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
        checkParams(systemConfig);
        systemConfigInfoToRedis(systemConfig);
        flushCatch();
        return systemConfigDao.batchUpdate(systemConfig);
    }

    private void checkParams(List<SystemConfig> systemConfigList) {
        Map<String, SystemConfig> configMap = systemConfigDao.selectByConfigList(systemConfigList);

        systemConfigList.forEach(systemConfig -> {
            String configKey = systemConfig.getConfigType() + "_" + systemConfig.getConfigKey();
            SystemConfig ruleConfig = configMap.get(configKey);
            String jsonRule = ruleConfig.getRule();
            if (StringUtils.isNotEmpty(jsonRule) && StringUtils.isNotEmpty(systemConfig.getConfigValue())) {
                boolean mismatch = false;
                String pmsg = "";
                try {
                    JSONObject ruleObj = JSON.parseObject(jsonRule);
                    String prule = ruleObj.getString("rule");
                    pmsg = ruleObj.getString("msg");

                    if (!StringUtils.isAnyEmpty(prule, pmsg) && !systemConfig.getConfigValue().matches(prule)) {
                        mismatch = true;
                    }
                } catch (Exception e) {
                    log.error("规则解析失败： {}", jsonRule, e);
                }
                if (mismatch) {
                    throw new BusinessException(ResultCodeEnum.CODE20017.getCode(),
                        "参数：【" + systemConfig.getConfigTypeName() + " > " + systemConfig.getConfigName() + "】不符合规则，" + pmsg);
                }
            }
        });

    }

    private void systemConfigInfoToRedis(List<SystemConfig> configList){
        configList.forEach(systemConfig ->{
            Map<String,String> map = new HashMap<>();
            map.put(systemConfig.getConfigKey(),systemConfig.getConfigValue());
            redisTemplate.opsForHash().putAll(ApplicationProperties.SYSTEM_CONFIG_KEY +systemConfig.getConfigType(),map);
            updateToOtherServer(systemConfig);
        });
    }

    public void flushCatch() {
        applicationProperties.flush();
    }

    public List<SystemConfig> selectAll() {
        return systemConfigDao.selectAll();
    }

    private void updateToOtherServer(SystemConfig systemConfig){
        try {
            //TCP服务配置修改
            String upSystem = "upSystem";
            if (upSystem.equals(systemConfig.getConfigType())){
                boolean isChange;
                switch (systemConfig.getConfigKey()){
                    case "upSystemFlag":
                    case "upSystemIp":
                    case "upSystemPort":
                        isChange = true;
                        break;
                    default:
                        isChange = false;
                        break;
                }
                if (isChange && applicationProperties.getUpSystemFtps().isEnable()){
                    Constant.getToOtherServer(Constant.UPDATE_TCP_CONTENT);
                }
            }
            String robotServerConfig = "robotServerConfig";
            if (robotServerConfig.equals(systemConfig.getConfigType())){
                String port = "nettyServerPort";
                //Robot服务端口修改
                if (port.equals(systemConfig.getConfigKey())){
                    Constant.getToOtherServer(Constant.UPDATE_ROBOT_SERVER);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
