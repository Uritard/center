package com.yjh.accessvideo;

import com.alibaba.fastjson.JSON;
import com.sun.jna.Pointer;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.hik.handler.FLoginResultCallBackS;
import com.yjh.accessvideo.hik.handler.FMSGCallBack;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.RecorderConInfo;
import com.yjh.accessvideo.module.control.service.CameraConService;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 用户句柄
     */
    private int lUserID;

    /**
     * 报警布防句柄
     */
    private int lAlarmHandle = -1;

    /**
     *报警回调函数实现
     */

    FMSGCallBack fmsgCallBack;

    /**
     * 订阅通道
     */
    AtomicInteger iIndex = new AtomicInteger(0);

    /**
     * 设备登录信息
     */
    private HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    public static void main(String[] args) {
        SpringApplication.run(AccessVideoApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        Constant.redisTemplate = redisTemplate;
        log.info("videoAccess is running...");
        setSDKCom();
        if (!hCNetSDK.NET_DVR_Init()) {
            log.error("init fail..");
            return;
        }
        log.info("init success..");
        String sdkLogPath = System.getProperty("user.dir") + "/logs/sdk_log/";;
        log.info("sdkLogPath {} ", sdkLogPath);
        hCNetSDK.NET_DVR_SetLogToFile(3, sdkLogPath, false);
        register();
    }


    private void setSDKCom() {
        //设置HCNetSDKCom组件库所在路径
        String sdkPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:sdkPath", "content"));
        String sdkComPath = System.getProperty("user.dir") + sdkPath;
        log.info("sdkComPath {} ", sdkComPath);
        HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
        System.arraycopy(sdkComPath.getBytes(), 0, struComPath.sPath, 0, sdkComPath.length());
        struComPath.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());

        //设置libcrypto.so所在路径
        HCNetSDK.BYTE_ARRAY ptrByteArrayCrypto = new HCNetSDK.BYTE_ARRAY(256);
        String strPathCrypto = sdkComPath + "/libcrypto.so";
        System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
        ptrByteArrayCrypto.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArrayCrypto.getPointer());

        //设置libssl.so所在路径
        HCNetSDK.BYTE_ARRAY ptrByteArraySsl = new HCNetSDK.BYTE_ARRAY(256);
        String strPathSsl = sdkComPath + "/libssl.so";
        System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
        ptrByteArraySsl.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArraySsl.getPointer());

        //设置libPlayCtrl.so所在路径
        HCNetSDK.BYTE_ARRAY ptrPlayCtrl = new HCNetSDK.BYTE_ARRAY(256);
        String ptrPlayCtrlPath = sdkComPath + "/libPlayCtrl.so";
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

        HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG net_dvr_local_general_cfg = new HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG();
        net_dvr_local_general_cfg.byAlarmJsonPictureSeparate = 1;
        net_dvr_local_general_cfg.write();
        boolean setLocal = hCNetSDK.NET_DVR_SetSDKLocalCfg(HCNetSDK.NET_SDK_LOCAL_CFG_TYPE.NET_DVR_LOCAL_CFG_TYPE_GENERAL,
            net_dvr_local_general_cfg.getPointer());

        cameraConService.refreshRecordsOnSchedule();
        log.info("NVR list: " + recorderConInfoList);
        String captureResultPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content"));
        String capturePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content"));
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

                m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是

                m_strLoginInfo.write();
                m_strLoginInfo.cbLoginResult = new FLoginResultCallBackS();
                lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
                log.info("m_sDeviceIP: " + m_sDeviceIP + ", lUserID: " + lUserID);
                if (lUserID == -1) {
                    log.error(recorderConInfo.getRecordName() + " register fail, error code:" + hCNetSDK.NET_DVR_GetLastError());
                } else {
                    if ("813".equals(recorderConInfo.getRecorderType())) {
                        if (lAlarmHandle < 0)//尚未布防,需要布防
                        {
                            if (fmsgCallBack == null) {
                                fmsgCallBack = new FMSGCallBack(analyseDataOperateService,captureResultPath,capturePath);
                                Pointer pUser = null;
                                int index = iIndex.getAndIncrement();
                                if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V50(index,fmsgCallBack, pUser)) {
                                    log.error("设置回调函数失败!");
                                }
                            }
                            HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
                            m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
                            m_strAlarmInfo.byLevel = 1;
                            m_strAlarmInfo.byAlarmInfoType = 1;
                            m_strAlarmInfo.byDeployType = 1;
                            m_strAlarmInfo.write();
                            lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
                            if (lAlarmHandle == -1) {
                                log.error(recorderConInfo.getRecordName() + " subscribe fail, error code:" + hCNetSDK.NET_DVR_GetLastError());
                            } else {
                                Constant.DVRMaps.put(lUserID, recordId);
                                Constant.deviceMaps.put(recordId, m_strDeviceInfo);
                            }
                        }
                    }
                    else {
                        Constant.maps.put(String.valueOf(recordId), lUserID);
                        Constant.deviceMaps.put(recordId, m_strDeviceInfo);
                    }
                    log.info("NVR " + recorderConInfo.getRecordName() + " register success.");
                    //IP通道个数
                    log.info("The max number of IP channels: {}", m_strDeviceInfo.struDeviceV30.byIPChanNum);
                }
            }
            log.info("Constant.maps: {}", JSON.toJSONString(Constant.maps));
        }).start();

    }

}
