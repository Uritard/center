package com.yjh.accessvideo.hik.transmit;

import com.sun.jna.Pointer;
import com.yjh.accessvideo.hik.HCNetSDK;
import lombok.extern.slf4j.Slf4j;

/**
 * 语音对讲回调函数
 * @author 丫C
 * @date 2023/4/18
 */
@Slf4j
public class VoiceDataCallBack implements HCNetSDK.FVoiceDataCallBack_V30 {

    @Override
    public void invoke(int lVoiceComHandle, Pointer pRecvDataBuffer, int dwBufSize, byte byAudioFlag, Pointer pUser) {
        log.info("lVoiceComHandle:{},pRecvDataBuffer:{},dwBufSize:{},byAudioFlag:{},pUser:{}",
                lVoiceComHandle, pRecvDataBuffer, dwBufSize, byAudioFlag, pUser);
    }
}
