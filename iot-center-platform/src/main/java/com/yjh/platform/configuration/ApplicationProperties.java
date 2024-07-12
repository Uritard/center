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
    //算法管理平台配置
    private ManagerAlgorithmConfig managerAlgorithmConfig;
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
    //video服务配置
    private VideoServerConfig videoServerConfig;

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
    public static class VideoServerConfig{

        //紧急调阅视频并发路数
        private Integer emergencyAccessNum;

    }

    @Data
    @Accessors(chain = true)
    public static class FtpsConfig{

        private String flag;
        private String ip;
        private Integer port;
        private String userName;
        private String password;

        public boolean isEnable() {
            return "1".equals(flag);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class ManagerAlgorithmConfig {
        private String provinceName;
        private String cityName;
        private String sectionId;
        private String sectionName;
        private String stationName;
        private Integer voltLevel;
        private String managerServerFtpsRemotePath;
        private boolean managerSystemFlag;

        public boolean isEnable() {
            return managerSystemFlag;
        }
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
        private String defectAnalysisUrl;
        private String updateUrl;
        private String algorithmResourceUrl;
        private String systemCheckUrl;
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
        //数据召唤文件生成路径
        private String dataCallPath;
        //数据召唤文件名称
        private String dataCallName;
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
        //请求采集声纹数据接口
        private String voiceprintDataCollectUrl;
        //请求声纹分析接口
        private String voiceprintAnalyseUrl;
        //请求声纹结果反馈ip地址
        private String requestHostIp;
        //请求声纹结果返回端口
        private String requestHostPort;

    }

    @Data
    @Accessors(chain = true)
    public static class OtherConfig{

        //是否调用调用算法组相机偏移校验功能
        private Boolean cameraPresetSecondCheck;
        //非同源趋势对比支持汉字
        private String nonhomologousWarn;
        //stationCode
        private String stationCode;
        //结果对应数字
        private String coverResult;
    }

    public void flush(){

        Map<String,String> redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"upSystem");
        ApplicationProperties.FtpsConfig upSystemFtps = new ApplicationProperties.FtpsConfig();
        upSystemFtps.setIp(redisMap.get("upSystemFtpsIp"))
                .setFlag(redisMap.get("upSystemFlag"))
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
            //发布参数修改
            redisTemplate.convertAndSend(SYSTEM_CONFIG_KEY + "algorithmSystem", redisMap);
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
        ApplicationProperties.ManagerAlgorithmConfig managerAlgorithmConfig = new ApplicationProperties.ManagerAlgorithmConfig();
        managerAlgorithmConfig
                .setProvinceName(redisMap.get("provinceName"))
                .setCityName(redisMap.get("cityName"))
                .setSectionId(redisMap.get("sectionId"))
                .setSectionName(redisMap.get("sectionName"))
                .setStationName(redisMap.get("stationName"))
                .setVoltLevel(ValueUtil.toInteger(redisMap.get("voltLevel"),220))
                .setManagerServerFtpsRemotePath(redisMap.get("managerServerFtpsRemotePath"))
                .setManagerSystemFlag("1".equals(redisMap.get("managerSystemFlag")));
        this.managerAlgorithmConfig = managerAlgorithmConfig;

        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"sequentialConfig");
        ApplicationProperties.SequentialConfig sequentialConfig = new ApplicationProperties.SequentialConfig();
        sequentialConfig.setSequentialVideocfmResult(redisMap.get("sequentialVideocfmResult"))
                .setSequentialReturnLinkage(redisMap.get("sequentialReturnLinkage"))
                .setSequentialFileCharset(redisMap.get("sequentialFileCharset"))
                .setSequentialResultFlag(sequentialResultFlag)
                .setDataCallPath(redisMap.get("dataCallPath"))
                .setDataCallName(redisMap.get("dataCallName"));
        this.sequentialConfig = sequentialConfig;

        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"audioConfig");
        ApplicationProperties.AudioConfig audioConfig = new ApplicationProperties.AudioConfig();
        audioConfig.setAudioTcpServerPort(ValueUtil.toInteger(redisMap.get("audioTcpServerPort"),10021))
                .setAudioTcpByteOrderLittleEndianEnabled(ValueUtil.toBoolean(redisMap.get("audioTcpByteOrderLittleEndianEnabled"),false))
                .setAudioMqttHost(redisMap.get("audioMqttHost"))
                .setAudioMqttUser(redisMap.get("audioMqttUser"))
                .setAudioMqttPwd(redisMap.get("audioMqttPwd"))
                .setVoiceprintDataCollectUrl(redisMap.get("voiceprintDataCollectUrl"))
                .setVoiceprintAnalyseUrl(redisMap.get("voiceprintAnalyseUrl"))
                .setRequestHostIp(redisMap.get("requestHostIp"))
                .setRequestHostPort(redisMap.get("requestHostPort"))
        ;
        this.audioConfig = audioConfig;
        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"otherConfig");
        ApplicationProperties.OtherConfig otherConfig = new ApplicationProperties.OtherConfig();
        otherConfig.setCameraPresetSecondCheck(ValueUtil.toBoolean(redisMap.get("cameraPresetSecondCheck"),false))
                .setStationCode((String)redisTemplate.opsForHash().get("t_sys_param:edgeId","content"))
                .setCoverResult(redisMap.get("coverResult"))
                .setNonhomologousWarn(redisMap.get("nonhomologousWarn"));
        this.otherConfig = otherConfig;

        redisMap = redisTemplate.opsForHash().entries(SYSTEM_CONFIG_KEY +"videoServerConfig");
        ApplicationProperties.VideoServerConfig videoServerConfig = new ApplicationProperties.VideoServerConfig();
        videoServerConfig.setEmergencyAccessNum(ValueUtil.toInteger(redisMap.get("emergencyAccessNum"),10));
        this.videoServerConfig = videoServerConfig;

        Constant.nonhomologousWarn = this.getOtherConfig().getNonhomologousWarn();
        Constant.setUpSystem(this.getUpSystemFtps().isEnable());

        log.info("系统配置: {}", this);
    }
}
