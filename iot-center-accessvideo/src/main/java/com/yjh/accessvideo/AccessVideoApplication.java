package com.yjh.accessvideo;

import com.alibaba.fastjson.JSON;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.RecorderConInfo;
import com.yjh.accessvideo.module.control.service.CameraConService;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import com.yjh.accessvideo.netty.client.NettyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;


/**
 * Created by tt on 2019/7/31
 */
@SpringBootApplication(scanBasePackages = "com.yjh.accessvideo")
@EnableDiscoveryClient
@Slf4j
@EnableAsync
@EnableScheduling
public class AccessVideoApplication implements CommandLineRunner {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    @Autowired
    private CameraConDao cameraConDao;

    @Autowired
    private CameraConService cameraConService;

    /**
     * sdk路径
     */
    @Value("${nvr.sdk.path}")
    private String sdkPath;

    /**
     * sdk日志路径
     */
    @Value("${nvr.log.path}")
    private String sdkLogPath;

    /**
     * sdk日志等级
     */
    @Value("${nvr.log.level}")
    private int logLevel;

    /**
     * 识别算法端口
     */
    @Value("${netty.recognize.port}")
    private int recognizePort;

    /**
     * 缺陷算法端口
     */
    @Value("${netty.ai.port}")
    private int aiPort;

    /**
     * 算法服务端IP
     */
    @Value("${netty.server.url}")
    private String serverUrl;

    /**
     * WS调用接口地址
     */
    @Value("${system.webSocket.url}")
    private String syncWebsocketUrl;

    @Value("${spring.interface.api}")
    private String interfaceApi;

    /**
     * 变电站编码
     */
    @Value("${station.code}")
    private String stationCode;

    /**
     * video服务是否单独部署
     */
    @Value("${video.separate.deploy:false}")
    private String videoSeparateDeploy;

    /**
     * 用户句柄
     */
    private int lUserID;

    /**
     * 设备登录信息
     */
    private HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    private NettyClient nettyClient = new NettyClient();

    public static void main(String[] args) {
        SpringApplication.run(AccessVideoApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        Constant.videoSeparateDeploy = Boolean.valueOf(videoSeparateDeploy);
        Constant.WEBSOCKET_URL = syncWebsocketUrl;
        Constant.redisTemplate = redisTemplate;
        Constant.apiPermissions = Boolean.valueOf(interfaceApi);
        log.info("videoAccess is running...");
        setSDKCom();
        if (!hCNetSDK.NET_DVR_Init()) {
            log.error("init fail..");
            return;
        }
        log.info("init success..");
        hCNetSDK.NET_DVR_SetLogToFile(logLevel, sdkLogPath, false);
        register();
        // InetSocketAddress remoteAddress1 = new InetSocketAddress(serverUrl, recognizePort);
        // InetSocketAddress remoteAddress2 = new InetSocketAddress(serverUrl, aiPort);
        // nettyClient.start(remoteAddress1, remoteAddress2, redisTemplate,analyseDataOperateService,
        // syncWebsocketUrl, stationCode);
    }

    private void setSDKCom() {
        //设置HCNetSDKCom组件库所在路径
        String strPathCom = sdkPath;
        HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
        System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
        struComPath.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());

        //设置libcrypto.so所在路径
        HCNetSDK.BYTE_ARRAY ptrByteArrayCrypto = new HCNetSDK.BYTE_ARRAY(256);
        String strPathCrypto = sdkPath + "/libcrypto.so";
        System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
        ptrByteArrayCrypto.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArrayCrypto.getPointer());

        //设置libssl.so所在路径
        HCNetSDK.BYTE_ARRAY ptrByteArraySsl = new HCNetSDK.BYTE_ARRAY(256);
        String strPathSsl = sdkPath + "/libssl.so";
        System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
        ptrByteArraySsl.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArraySsl.getPointer());

        //设置libPlayCtrl.so所在路径
        HCNetSDK.BYTE_ARRAY ptrPlayCtrl = new HCNetSDK.BYTE_ARRAY(256);
        String ptrPlayCtrlPath = sdkPath + "/libPlayCtrl.so";
        System.arraycopy(ptrPlayCtrlPath.getBytes(), 0, ptrPlayCtrl.byValue, 0, ptrPlayCtrlPath.length());
        ptrPlayCtrl.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(5, ptrPlayCtrl.getPointer());

    }

    private void register() {

        if (lUserID > -1) {
            //NVR log out first...
            hCNetSDK.NET_DVR_Logout(lUserID);
            lUserID = -1;
        }
        List<RecorderConInfo> recorderConInfoList = cameraConDao.SelectRecords();
        cameraConService.refreshRecordsOnSchedule();
        log.info("NVR list: " + recorderConInfoList);
        new Thread(() -> {
            for (RecorderConInfo recorderConInfo : recorderConInfoList) {
                //设备信息
                HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
                long recordId = recorderConInfo.getRecordId();
                String m_sDeviceIP = recorderConInfo.getRecordIp();
                String m_sUsername = recorderConInfo.getIdentityManager();
                String m_sPassword = recorderConInfo.getIdentityCode();
                Short m_port = recorderConInfo.getHttpPort().shortValue();
                log.info("register nvr" + recorderConInfo.getRecordName() + ", ip is " + m_sDeviceIP + ", port is " + m_port);
                //注册
                m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
                System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());

                m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
                System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());

                m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
                System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

                m_strLoginInfo.wPort = m_port;

                m_strLoginInfo.bUseAsynLogin = 0; //是否异步登录：0- 否，1- 是

                m_strLoginInfo.write();
                lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
                log.info("m_sDeviceIP: " + m_sDeviceIP + ", lUserID: " + lUserID);
                if (lUserID == -1) {
                    log.error(recorderConInfo.getRecordName() + " register fail, error code:" + hCNetSDK.NET_DVR_GetLastError());
                } else {
                    Constant.maps.put(String.valueOf(recordId), lUserID);
                    Constant.deviceMaps.put(recordId, m_strDeviceInfo);
                    log.info("NVR " + recorderConInfo.getRecordName() + " register success.");
                    //IP通道个数
                    log.info("The max number of IP channels: {}", m_strDeviceInfo.struDeviceV30.byIPChanNum);
                }
            }
            log.info("Constant.maps: {}", JSON.toJSONString(Constant.maps));
        }).start();

    }

}
