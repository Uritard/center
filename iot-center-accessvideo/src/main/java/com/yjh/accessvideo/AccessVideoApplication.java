package com.yjh.accessvideo;

import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.hik.HCNetSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


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

    private int lUserID;//用户句柄
    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    //设备登录信息
    private HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
    //设备信息
    private HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

    public static void main(String[] args) {
        SpringApplication.run(AccessVideoApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("videoAccess is running...");
        if (!hCNetSDK.NET_DVR_Init()) { log.error("初始化失败"); return;} else { log.info("初始化成功");}
        if (register()) {log.info("go on");}
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
            log.error("注册失败，错误号:" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        } else {
            Constant.maps.put("lUserID", lUserID);
            log.info("注册成功");
            return true;
        }
    }

}
