package com.yjh.accessvideo.hik;

import com.sun.jna.Pointer;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.hik.handler.CbVoiceDataCallBack;
import com.yjh.accessvideo.hik.handler.VoiceDataCallBack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * 海康语音SDK使用工具
 * @author 丫C
 * @date 2023/4/17
 */
@Slf4j
public class HikUtilsApp {

    /**
     * sdk日志等级
     */
    @Value("${nvr.log.level}")
    private int logLevel;

    /**
     * sdk日志路径
     */
    @Value("${nvr.log.path}")
    private String sdkLogPath;
    /**
     * 语音转发句柄
     */
    private int lVoiceTranHandle = -1;

    private static final HCNetSDK HC_NET_SDK = HCNetSDK.INSTANCE;
    private VoiceDataCallBack voiceDataCallBack = null;

    /**
     * 初始化
     */
    public void initSdk() {
        boolean initSuc = HC_NET_SDK.NET_DVR_Init();
        if (!initSuc) {
            log.error("Init sdk fail, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
            return;
        }
        HC_NET_SDK.NET_DVR_SetLogToFile(logLevel, sdkLogPath, false);
        log.info("==================================NET_DVR_Init success==================================");
    }

    /**
     * 设备登录
     */
    public boolean deviceLogin(HikDeviceInfo hikDeviceInfo) {
        // 设备登录信息
        HCNetSDK.NET_DVR_USER_LOGIN_INFO strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        // 设备信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

        String deviceIp = hikDeviceInfo.getDeviceIp();
        strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(deviceIp.getBytes(), 0, strLoginInfo.sDeviceAddress, 0, deviceIp.length());

        String userName = hikDeviceInfo.getUserName();
        strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(userName.getBytes(), 0, strLoginInfo.sUserName, 0, userName.length());

        String password = hikDeviceInfo.getPassword();
        strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(password.getBytes(), 0, strLoginInfo.sPassword, 0, password.length());

        Integer devicePort = hikDeviceInfo.getDevicePort();
        strLoginInfo.wPort = devicePort.shortValue();
        strLoginInfo.bUseAsynLogin = false;
        strLoginInfo.write();
        log.info("ip地址:{},端口号:{},登录账号:{},登录密码:{}", deviceIp, devicePort, userName, password);
        int lUserId = HC_NET_SDK.NET_DVR_Login_V40(strLoginInfo, strDeviceInfo);
        if (lUserId <= -1) {
            log.error("Device login fail, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
            return false;
        } else {
            log.info("==================================NET_DVR_Login_V40 success==================================");
            Constant.hikDeviceUserIdMaps.put(String.valueOf(hikDeviceInfo.getHikDeviceId()), lUserId);
            return true;
        }
    }


    /**
     * 获取当前生效的对讲音频压缩参数
     */
    public CompressionAudio getAudioCompress(int lUserId){
        CompressionAudio compressionAudioTemp = new CompressionAudio();

        HCNetSDK.NET_DVR_COMPRESSION_AUDIO compressionAudio = new HCNetSDK.NET_DVR_COMPRESSION_AUDIO();
        compressionAudio.write();

        if (HC_NET_SDK.NET_DVR_GetCurrentAudioCompress(lUserId, compressionAudio)){
            log.info("==================================NET_DVR_GetCurrentAudioCompress success==================================");

            compressionAudioTemp.setByAudioEncType(compressionAudio.byAudioEncType);
            compressionAudioTemp.setByAudioSamplingRate(compressionAudio.byAudioSamplingRate);
            compressionAudioTemp.setByAudioBitRate(compressionAudio.byAudioBitRate);
            compressionAudioTemp.setByres(compressionAudio.byres);
            compressionAudioTemp.setBySupport(compressionAudio.bySupport);
            log.info("compressionAudioTemp:{}", compressionAudioTemp);
            return compressionAudioTemp;
        }
        log.error("Get audio compress fail, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
        return compressionAudioTemp;
    }

    /**
     * 开启语音对讲
     */
    public int startVoiceCom(int lUserId, int dwVoiceChan){
        // 语音通道号。对于设备本身的语音对讲通道，从1开始；对于设备的IP通道，
        // 为登录返回的起始对讲通道号(byStartDTalkChan) + IP通道索引 - 1，
        // 例如客户端通过NVR跟其IP Channel02所接前端IPC进行对讲，则dwVoiceChan=byStartDTalkChan + 1
        // 需要回调的语音数据类型：0- 编码后的语音数据，1- 编码前的PCM原始数据
        boolean bret = true;
        if (voiceDataCallBack == null) {
            voiceDataCallBack = new VoiceDataCallBack();
        }
        int lVoiceComHandle = HC_NET_SDK.NET_DVR_StartVoiceCom_V30(lUserId, dwVoiceChan, bret, voiceDataCallBack, null);
        if (lVoiceComHandle == -1) {
            log.error("Start voice com fail, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
        }else {
            log.info("==================================NET_DVR_StartVoiceCom_V30 success==================================");
            Constant.hikDeviceVoiceComHandleMaps.put(lUserId, lVoiceComHandle);
        }
        return lVoiceComHandle;
    }

    /**
     * 停止语音对讲
     */
    public boolean stopVoiceCom(int lVoiceComHandle) {
        if (HC_NET_SDK.NET_DVR_StopVoiceCom(lVoiceComHandle)){
            log.info("==================================NET_DVR_StopVoiceCom success==================================");
            return true;
        }
        log.error("Stop voice com fail, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
        return false;
    }

    /**
     * 开启语音转发
     */
    public int startVoiceTrans(int lUserId, int dwVoiceChan, CbVoiceDataCallBack cbVoiceDataCallBack, Pointer pointer){
        lVoiceTranHandle = HC_NET_SDK.NET_DVR_StartVoiceCom_MR_V30(lUserId, dwVoiceChan, cbVoiceDataCallBack, pointer);
        if (lVoiceTranHandle == -1){
            log.error("Start voice trans fail, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
        }else {
            log.info("==================================NET_DVR_StartVoiceCom_MR_V30 success==================================");
            Constant.hikDeviceVoiceTransHandleMaps.put(lUserId, lVoiceTranHandle);
        }
        return lVoiceTranHandle;
    }

    /**
     * 关闭语音转发
     */
    public boolean stopVoiceTrans(int lVoiceTranHandle){
        if (HC_NET_SDK.NET_DVR_StopVoiceCom(lVoiceTranHandle)){
            log.info("==================================NET_DVR_StopVoiceCom success==================================");
            return true;
        }
        log.error("Client audio start fail, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
        return false;
    }

    /**
     * 设备注销
     */
    public boolean deviceLogout(int lUserId){
        if (HC_NET_SDK.NET_DVR_Logout(lUserId)) {
            log.info("==================================NET_DVR_Logout success==================================");
            return true;
        }
        log.error("Logout fail, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
        return false;
    }
}
