package com.yjh.accessvideo.module.control.service;

import com.yjh.accessvideo.commons.utils.DateTimeUtil;
import com.yjh.accessvideo.hik.HCNetSDK;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 优化ARM架构发送数据到设备的逻辑
 *
 * @author 丫C
 * @date 2023/5/16
 */
@Slf4j
public class VoiceDataSendToDevice {
    private byte[] storeByte = new byte[1024 * 512];
    private final byte[] storeLock = new byte[0];
    private volatile int bufDataLen = 0;
    private static final HCNetSDK HC_NET_SDK = HCNetSDK.INSTANCE;
    private final int dataSize;
    private final long timeInterval;

    public VoiceDataSendToDevice(int dataSize, long timeInterval){
        this.dataSize = dataSize;
        this.timeInterval = timeInterval;
    }

    void voiceSendByArm(int lVoiceTranHandle, byte[] receiveByte, int dataLength) {
        try {
            synchronized (storeLock) {
                System.arraycopy(receiveByte, 0, storeByte, bufDataLen, dataLength);
                bufDataLen = bufDataLen + dataLength;
            }
            // 只要数据大于指定字节数就发送
            while (bufDataLen >= dataSize) {
                HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(dataSize);
                System.arraycopy(storeByte, 0, ptrPcmData.byValue, 0, dataSize);
                ptrPcmData.write();

                // 转发语音数据:G711-160字节 PCM-1920字节
                if (!HC_NET_SDK.NET_DVR_VoiceComSendData(lVoiceTranHandle, ptrPcmData.byValue, dataSize)) {
                    log.error("NET_DVR_VoiceComSendData failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                    return;
                }
                log.info("send data to device success...now time:{}", DateTimeUtil.getDateTimeString(new Date(), true));

                TimeUnit.MILLISECONDS.sleep(timeInterval);

                synchronized (storeLock) {
                    System.arraycopy(storeByte, dataSize, storeByte, 0, bufDataLen - dataSize);
                    bufDataLen -= dataSize;
                }
            }
        } catch (Exception e){
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
