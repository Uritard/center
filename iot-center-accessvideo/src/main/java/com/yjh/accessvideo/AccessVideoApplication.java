package com.yjh.accessvideo;

import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.netty.client.NettyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.net.InetSocketAddress;


/**
 * Created by tt on 2019/7/31
 */
@SpringBootApplication(scanBasePackages = "com.yjh.accessvideo")
@EnableDiscoveryClient
@Slf4j
public class AccessVideoApplication implements CommandLineRunner {

    @Value("${nvr.server.ip}")
    private String m_sDeviceIP;//已登录设备的IP地址
    @Value("${nvr.server.username}")
    private String m_sUsername;//设备用户名
    @Value("${nvr.server.password}")
    private String m_sPassword;//设备密码
    @Value("${nvr.server.port}")
    private Short m_port;//设备端口
    @Value("${nvr.sdk.path}")
    private String sdkPath;//sdk路径

    @Value("${netty.recognize.port}")
    private int recognizePort;//识别算法端口
    @Value("${netty.ai.port}")
    private int aiPort;//缺陷算法端口
    @Value("${netty.server.url}")
    private String serverUrl;//算法服务端IP

    private int lUserID;//用户句柄
    //设备登录信息
    private HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
    //设备信息
    private HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    private NettyClient nettyClient = new NettyClient();

    public static void main(String[] args) {
        SpringApplication.run(AccessVideoApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("videoAccess is running...");
        if (!hCNetSDK.NET_DVR_Init()) { log.error("init fail.."); return;} else { log.info("init success..");}
        hCNetSDK.NET_DVR_SetLogToFile(3,"/home/yjh_iot_center/iot-center-accessvideo-1.0.0",false);
        if (register()) {log.info("register success..");}
        InetSocketAddress remoteAddress1 = new InetSocketAddress(serverUrl, recognizePort);
        InetSocketAddress remoteAddress2 = new InetSocketAddress(serverUrl, aiPort);
        nettyClient.start(remoteAddress1, remoteAddress2);
    }

    private boolean register() {
        if (lUserID > -1) {
            //先注销
            hCNetSDK.NET_DVR_Logout(lUserID);
            lUserID = -1;
        }
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

        if (lUserID == -1) {
            log.error("register fail, error code:" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        } else {
            Constant.maps.put("lUserID", lUserID);
            //设置HCNetSDKCom组件库所在路径
            String strPathCom = sdkPath;
            HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
            System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
            struComPath.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());

            //设置libcrypto.so所在路径
            HCNetSDK.BYTE_ARRAY ptrByteArrayCrypto = new HCNetSDK.BYTE_ARRAY(256);
            String strPathCrypto = sdkPath+"/libcrypto.so";
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            HCNetSDK.BYTE_ARRAY ptrByteArraySsl = new HCNetSDK.BYTE_ARRAY(256);
            String strPathSsl = sdkPath+"/libssl.so";
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArraySsl.getPointer());
            return true;
        }
    }

}
