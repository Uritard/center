package com.yjh.platform.configuration;

import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.module.config.dao.SystemConfigDao;
import com.yjh.platform.module.config.entity.SystemConfig;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lqh
 * @Date: 2023/04/13
 */
@Data
@Slf4j
@Configuration
public class ApplicationProperties {

    /**
     * 27大类数据配置文件
     */
    @Value("${spring.inspection.conf}")
    private String inspectionConf;
    /**
     * 顺控是否自定义结果 0-否 1-是
     */
    @Value("${spring.sequential.result.flag}")
    private String sequentialResultFlag;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SystemConfigDao systemConfigDao;

    public final static String SYSTEM_CONFIG_KEY ="systemConfigKey:";

    //上级系统ftps
    private FtpsConfig upSystemFtps;
    //分析主机ftps
    private FtpsConfig intelAnalysisFtps;
    //算法管理平台
    private FtpsConfig managerSystemFtps;
    //算法管理平台mqtt配置
    private ManagerMqttConfig managerMqttConfig;
    //算法调用配置
    private AlgorithmServerConfig algorithmServerConfig;
    // 分析主机配置
    private IntelligentAlgorithmConfig intelAlgorithmConfig;
    //顺控相关配置
    private SequentialConfig sequentialConfig;
    //声纹相关配置
    private AudioConfig audioConfig;
    //其他配置
    private OtherConfig otherConfig;

    @PostConstruct
    public void flushCatch() {
        List<SystemConfig> configList = systemConfigDao.selectAll();
        configList.forEach(systemConfig ->{
            Map<String,String> map = new HashMap<>();
            map.put(systemConfig.getConfigKey(),systemConfig.getConfigValue());
            redisTemplate.opsForHash().putAll(SYSTEM_CONFIG_KEY + systemConfig.getConfigType(),map);
        });
        this.flush();
    }

    @Data
    @Accessors(chain = true)
    public static class FtpsConfig{

        private String ip;
        private Integer port;
        private String userName;
        private String password;
    }

    @Data
    @Accessors(chain = true)
    public static class ManagerMqttConfig {

        private String mqttHost;
        private String mqttUser;
        private String mqttPwd;
        private String mqttTopic;
        private String mqttHeartTopic;
        private String mqttProvinceName;
        private String mqttCityName;
        private String mqttStationName;
        private String mqttSectionName;
        private String mqttSectionIp;
        private String mqttNodeId;
        private Integer voltLevel;
        private String managerServerFtpsRemotePath;
    }

    @Data
    @Accessors(chain = true)
    public static class AlgorithmServerConfig{

        //识别算法端口
        private Integer nettyRecognizePort;
        //AI算法端口
        private Integer nettyAiPort;
        //27大类数据配置文件
        private String confFileName;
    }

    /**
     * 若使用 BeanUtils.populate 方法则不可以使用链式编程，需设置 @Accessors(chain = false)
     */
    @Data
    @Accessors(chain = false)
    public static class IntelligentAlgorithmConfig {
        private String analysisUrl;
        private String updateUrl;
        private String resultIp;
        private String resultPort;
        private String presetCheck;
        private String defectType;
        private String distinguishType;
        private String silentMonitorNameAndType;

    }

    @Data
    @Accessors(chain = true)
    public static class SequentialConfig{

        //一键顺控CIME文件名称
        private String sequentialVideocfmResult;
        //反向联动文件名称
        private String sequentialReturnLinkage;
        //顺控文件编码
        private String sequentialFileCharset;
        //顺控是否自定义结果
        private String sequentialResultFlag;
    }

    @Data
    @Accessors(chain = true)
    public static class AudioConfig{

        //声纹tcp端口
        private Integer audioTcpServerPort;
        //字节顺序解析方式
        private Boolean audioTcpByteOrderLittleEndianEnabled;
        //声纹设备的mqtt地址配置
        private String audioMqttHost;
        //声纹mqtt登录用户名
        private String audioMqttUser;
        //声纹mqtt登录密码
        private String audioMqttPwd;
    }

    @Data
    @Accessors(chain = true)
    public static class OtherConfig{

        //接口权限开关
        private Boolean springInterfaceApi;
        //是否调用调用算法组相机偏移校验功能
        private Boolean cameraPresetSecondCheck;
        //非同源趋势对比支持汉字
        private String nonhomologousWarn;
        //stationCode
        private String stationCode;
    }

    public void flush(){

        Map<String,String> redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"upSystem");
        ApplicationProperties.FtpsConfig upSystemFtps = new ApplicationProperties.FtpsConfig();
        upSystemFtps.setIp(redisMap.get("upSystemFtpsIp"))
                .setPort(ValueUtil.toInteger(redisMap.get("upSystemFtpsPort"),10012))
                .setUserName(redisMap.get("upSystemFtpsUsername"))
                .setPassword(redisMap.get("upSystemFtpsPassword"));
        this.upSystemFtps = upSystemFtps;

        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"algorithmSystem");
        ApplicationProperties.FtpsConfig intelAnalysisFtps = new ApplicationProperties.FtpsConfig();
        intelAnalysisFtps.setIp(redisMap.get("algorithmSystemFtpsIp"))
                .setPort(ValueUtil.toInteger(redisMap.get("algorithmSystemFtpsPort"),10012))
                .setUserName(redisMap.get("algorithmSystemFtpsUsername"))
                .setPassword(redisMap.get("algorithmSystemFtpsPassword"));
        this.intelAnalysisFtps = intelAnalysisFtps;

        try {
            if (this.intelAlgorithmConfig == null) {
                this.intelAlgorithmConfig = new IntelligentAlgorithmConfig();
            }
            BeanUtils.populate(this.intelAlgorithmConfig, redisMap);

        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("智能分析主机配置解析失败", e);
        }

        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"algorithmServerConfig");
        ApplicationProperties.AlgorithmServerConfig algorithmServerConfig = new ApplicationProperties.AlgorithmServerConfig();
        algorithmServerConfig.setNettyRecognizePort(ValueUtil.toInteger(redisMap.get("nettyRecognizePort"),13668))
            .setNettyAiPort(ValueUtil.toInteger(redisMap.get("nettyAiPort"),13669))
            .setConfFileName(inspectionConf);
        this.algorithmServerConfig = algorithmServerConfig;

        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"managerSystem");
        ApplicationProperties.FtpsConfig managerSystemFtps = new ApplicationProperties.FtpsConfig();
        managerSystemFtps.setIp(redisMap.get("managerSystemFtpsIp"))
                .setPort(ValueUtil.toInteger(redisMap.get("managerSystemFtpsPort"),10012))
                .setUserName(redisMap.get("managerSystemFtpsUsername"))
                .setPassword(redisMap.get("managerSystemFtpsPassword"));
        this.managerSystemFtps = managerSystemFtps;

        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"managerSystem");
        ApplicationProperties.ManagerMqttConfig managerMqttConfig = new ApplicationProperties.ManagerMqttConfig();
        managerMqttConfig.setMqttHost(redisMap.get("mqttHost"))
                .setMqttUser(redisMap.get("mqttUser"))
                .setMqttPwd(redisMap.get("mqttPwd"))
                .setMqttTopic(redisMap.get("mqttTopic"))
                .setMqttHeartTopic(redisMap.get("mqttHeartTopic"))
                .setMqttProvinceName(redisMap.get("mqttProvinceName"))
                .setMqttCityName(redisMap.get("mqttCityName"))
                .setMqttStationName(redisMap.get("mqttSectionName"))
                .setMqttSectionName(redisMap.get("mqttSectionName"))
                .setMqttSectionIp(redisMap.get("mqttSectionIp"))
                .setMqttNodeId(redisMap.get("mqttNodeId"))
                .setVoltLevel(ValueUtil.toInteger(redisMap.get("mqttNodeId"),220))
                .setManagerServerFtpsRemotePath(redisMap.get("mqttHost"));
        this.managerMqttConfig = managerMqttConfig;

        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"sequentialConfig");
        ApplicationProperties.SequentialConfig sequentialConfig = new ApplicationProperties.SequentialConfig();
        sequentialConfig.setSequentialVideocfmResult(redisMap.get("sequentialVideocfmResult"))
                .setSequentialReturnLinkage(redisMap.get("sequentialReturnLinkage"))
                .setSequentialFileCharset(redisMap.get("sequentialFileCharset"))
                .setSequentialResultFlag(sequentialResultFlag);
        this.sequentialConfig = sequentialConfig;

        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"audioConfig");
        ApplicationProperties.AudioConfig audioConfig = new ApplicationProperties.AudioConfig();
        audioConfig.setAudioTcpServerPort(ValueUtil.toInteger(redisMap.get("audioTcpServerPort"),10021))
                .setAudioTcpByteOrderLittleEndianEnabled(ValueUtil.toBoolean(redisMap.get("audioTcpByteOrderLittleEndianEnabled"),false))
                .setAudioMqttHost(redisMap.get("audioMqttHost"))
                .setAudioMqttUser(redisMap.get("audioMqttUser"))
                .setAudioMqttPwd(redisMap.get("audioMqttPwd"));
        this.audioConfig = audioConfig;
        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"otherConfig");
        ApplicationProperties.OtherConfig otherConfig = new ApplicationProperties.OtherConfig();
        otherConfig.setSpringInterfaceApi(ValueUtil.toBoolean(redisMap.get("springInterfaceApi"),false))
                .setCameraPresetSecondCheck(ValueUtil.toBoolean(redisMap.get("cameraPresetSecondCheck"),false))
                .setStationCode((String)redisTemplate.opsForHash().get("t_sys_param:edgeId","content"))
                .setNonhomologousWarn(redisMap.get("nonhomologousWarn"));
        this.otherConfig = otherConfig;

        Constant.apiPermissions= this.getOtherConfig().getSpringInterfaceApi();
        Constant.nonhomologousWarn = this.getOtherConfig().getNonhomologousWarn();

        log.info("系统配置: {}", this);
    }
}
