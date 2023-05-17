package com.yjh.accessvideo.module.control.service;

import com.yjh.accessvideo.hik.HCNetSDK;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author 丫C
 * @date 2023/5/16
 */
@Slf4j
public class VoiceDataSendToDevice {
    private byte[] storeByte = new byte[1024 * 256];
    private final byte[] storeLock = new byte[0];
    private volatile int bufDataLen = 0;
    private static final int PCM_DATA_SIZE = 1920;
    private static final HCNetSDK HC_NET_SDK = HCNetSDK.INSTANCE;

    void voiceSendByArm(int lVoiceTranHandle, byte[] receiveByte, int dataLength){

        synchronized (storeLock) {
            System.arraycopy(receiveByte, 0, storeByte, bufDataLen, dataLength);
            bufDataLen = bufDataLen + dataLength;
        }

        // 只要数据大于指定字节数就发送
        while (bufDataLen >= PCM_DATA_SIZE) {
            HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(PCM_DATA_SIZE);
            System.arraycopy(storeByte, 0, ptrPcmData.byValue, 0, PCM_DATA_SIZE);
            ptrPcmData.write();

            // 每次发送固定大小1920字节数据
            if (!HC_NET_SDK.NET_DVR_VoiceComSendData(lVoiceTranHandle, ptrPcmData.byValue, PCM_DATA_SIZE)) {
                log.error("NET_DVR_VoiceComSendData failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                return;
            }

            // 1920 / (16000 * 1 * 16 / 8 * 1 / 1000) = 60
            try {
                TimeUnit.MILLISECONDS.sleep(60);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            synchronized (storeLock) {
                System.arraycopy(storeByte, PCM_DATA_SIZE, storeByte, 0, bufDataLen - PCM_DATA_SIZE);
                bufDataLen -= PCM_DATA_SIZE;
            }
        }
    }
}
