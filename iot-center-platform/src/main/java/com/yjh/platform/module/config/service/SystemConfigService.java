package com.yjh.platform.module.config.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.MqttUtilsServer;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.config.dao.SystemConfigDao;
import com.yjh.platform.module.config.entity.ConfigTreeNode;
import com.yjh.platform.module.config.entity.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

    private final String systemConfigKey="systemConfigKey:";

    private MqttUtilsServer algorithmMqtt;

    private MqttUtilsServer voiceMqtt;

    @Value("${spring.application.name}")
    private String appName;
    @Value("${server.port}")
    private String appPort;

    public SystemConfigService(SystemConfigDao systemConfigDao,
                               ApplicationProperties applicationProperties,
                               RedisTemplate redisTemplate,
                               @Qualifier("voiceMqtt") MqttUtilsServer voiceMqtt,
                               @Qualifier("algorithmMqtt") MqttUtilsServer algorithmMqtt) {
        this.systemConfigDao = systemConfigDao;
        this.applicationProperties = applicationProperties;
        this.redisTemplate = redisTemplate;
        this.voiceMqtt = voiceMqtt;
        this.algorithmMqtt = algorithmMqtt;
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

    private void checkParams(List<SystemConfig> systemConfigList){
        systemConfigList.forEach(systemConfig -> {
                String jsonRule = systemConfig.getRule();
                if (StringUtils.isNotEmpty(jsonRule)) {
                    if (!systemConfig.getConfigValue().matches(jsonRule)) {
                        throw new BusinessException("参数：" +systemConfig.getConfigTypeName()+" "+ systemConfig.getConfigName() + " 不符合规则，请输入正确的参数");
                    }
                }
        });

    }

    private void systemConfigInfoToRedis(List<SystemConfig> configList){
        configList.forEach(systemConfig ->{
            Map<String,String> map = new HashMap<>();
            map.put(systemConfig.getConfigKey(),systemConfig.getConfigValue());
            redisTemplate.opsForHash().putAll(systemConfigKey+systemConfig.getConfigType(),map);
            updateToOtherServer(systemConfig);
            updateVoiceMqttConfig(systemConfig);
            updateAlgorithmMqttConfig(systemConfig);
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
                if (isChange){
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

    private void updateVoiceMqttConfig(SystemConfig systemConfig){
        try {
            Boolean updateFlag = false;
            if ("audioConfig".equals(systemConfig.getConfigType())) {
                switch (systemConfig.getConfigKey()) {
                    case "audioTcpServerPort":
                        if (!applicationProperties.getAudioConfig().getAudioTcpServerPort().equals(Integer.valueOf(systemConfig.getConfigValue()))) {
                            applicationProperties.getAudioConfig().setAudioTcpServerPort(Integer.valueOf(systemConfig.getConfigValue()));
                            updateFlag = true;
                        }
                        break;
                    case "audioMqttHost":
                        if (!applicationProperties.getAudioConfig().getAudioMqttHost().equals(systemConfig.getConfigValue())) {
                            applicationProperties.getAudioConfig().setAudioMqttHost(systemConfig.getConfigValue());
                            updateFlag = true;
                        }
                        break;
                    case "audioMqttUser":
                        if (!applicationProperties.getAudioConfig().getAudioMqttUser().equals(systemConfig.getConfigValue())) {
                            applicationProperties.getAudioConfig().setAudioMqttUser(systemConfig.getConfigValue());
                            updateFlag = true;
                        }
                        break;
                    case "audioMqttPwd":
                        if (!applicationProperties.getAudioConfig().getAudioMqttPwd().equals(systemConfig.getConfigValue())) {
                            applicationProperties.getAudioConfig().setAudioMqttPwd(systemConfig.getConfigValue());
                            updateFlag = true;
                        }
                        break;
                    default:
                        break;
                }
                if (updateFlag) {
                    log.info("voiceMqtt 配置已修改：{}",applicationProperties.getAudioConfig());
                    voiceMqtt.shutdown();
                    voiceMqtt = new MqttUtilsServer(applicationProperties.getAudioConfig().getAudioMqttHost(), getIp() + "#" + appName + "#" + appPort, applicationProperties.getAudioConfig().getAudioMqttUser(), applicationProperties.getAudioConfig().getAudioMqttPwd());
                }
            }
        }catch (Exception e){
            log.error("重新创建 voiceMqtt 异常 ：",e);
        }
    }

    private void updateAlgorithmMqttConfig(SystemConfig systemConfig){
        try {
            Boolean updateFlag = false;
            if ("algorithmMqttConfig".equals(systemConfig.getConfigType())) {
                switch (systemConfig.getConfigKey()) {
                    case "mqttHost":
                        if (!applicationProperties.getManagerMqttConfig().getMqttHost().equals(systemConfig.getConfigValue())) {
                            applicationProperties.getManagerMqttConfig().setMqttHost(systemConfig.getConfigValue());
                            updateFlag = true;
                        }
                        break;
                    case "mqttUser":
                        if (!applicationProperties.getManagerMqttConfig().getMqttUser().equals(systemConfig.getConfigValue())) {
                            applicationProperties.getManagerMqttConfig().setMqttUser(systemConfig.getConfigValue());
                            updateFlag = true;
                        }
                        break;
                    case "mqttPwd":
                        if (!applicationProperties.getManagerMqttConfig().getMqttPwd().equals(systemConfig.getConfigValue())) {
                            applicationProperties.getManagerMqttConfig().setMqttPwd(systemConfig.getConfigValue());
                            updateFlag = true;
                        }
                        break;
                    default:
                        break;
                }
                if (updateFlag) {
                    log.info("algorithmMqtt 配置已修改：{}",applicationProperties.getManagerMqttConfig());
                    algorithmMqtt.shutdown();
                    algorithmMqtt = new MqttUtilsServer(applicationProperties.getManagerMqttConfig().getMqttHost(), getIp() + "#" + appName + "#" + appPort, applicationProperties.getManagerMqttConfig().getMqttUser(), applicationProperties.getManagerMqttConfig().getMqttPwd());
                }
            }
        }catch (Exception e){
            log.error("重新创建 algorithmMqtt 异常 ：",e);
        }
    }

    /**
     * 获取ip地址
     * @return
     */
    private String getIp() {
        String ip = "0.0.0.0";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }
}
