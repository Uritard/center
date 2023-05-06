package com.yjh.accessvideo.module.control.service;

import com.sun.jna.Pointer;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.utils.DateTimeUtil;
import com.yjh.accessvideo.hik.transmit.CompressionAudio;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.hik.transmit.HikDeviceInfo;
import com.yjh.accessvideo.hik.transmit.HikUtilsApp;
import com.yjh.accessvideo.hik.transmit.CbVoiceDataCallBack;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.CameraConInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * @author 丫C
 * @date 2023/4/17
 */
@Service
public class VoiceComService {

    private final Logger log = LoggerFactory.getLogger(VoiceComService.class);
    private static final String USE_DIR = System.getProperty("user.dir");

    private static final int DATA_SIZE = 640;
    private static final int ENCODE_DATA_SIZE = 160;

    private static final HCNetSDK HC_NET_SDK = HCNetSDK.INSTANCE;
    private CbVoiceDataCallBack cbVoiceDataCallBack;

    private final CameraConDao cameraConDao;
    private final RedisTemplate redisTemplate;

    public VoiceComService(CameraConDao cameraConDao, RedisTemplate redisTemplate){
        this.cameraConDao = cameraConDao;
        this.redisTemplate = redisTemplate;
    }

    public boolean deviceLogin(Long cameraId) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);

        HikDeviceInfo hikDeviceInfo = new HikDeviceInfo()
                .setDeviceIp(cameraConInfo.getCameraIp())
                .setDevicePort(cameraConInfo.getPort())
                .setPassword(cameraConInfo.getCameraCode())
                .setUserName(cameraConInfo.getCameraManager())
                .setHikDeviceId(cameraConInfo.getCameraId());
        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        boolean deviceLogin = hikUtilsApp.deviceLogin(hikDeviceInfo);
        return deviceLogin;
    }

    public int startVoiceCom(Long cameraId, Integer dwVoiceChan){
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(String.valueOf(cameraId))){
            return -1;
        }
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(cameraId));
        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        return hikUtilsApp.startVoiceCom(lUserId, dwVoiceChan);
    }

    public boolean stopVoiceCom(Long cameraId){
        // 未登录
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(String.valueOf(cameraId))){
            return false;
        }
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(cameraId));
        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        // 未开启
        if (Constant.hikDeviceVoiceComHandleMaps.isEmpty() || !Constant.hikDeviceVoiceComHandleMaps.containsKey(lUserId)){
            return false;
        }
        Integer lVoiceComHandle = Constant.hikDeviceVoiceComHandleMaps.get(lUserId);
        boolean stopVoiceCom = hikUtilsApp.stopVoiceCom(lVoiceComHandle);

        return stopVoiceCom;
    }

    public int startVoiceTransTest(Long cameraId, long timeItem, String fileName){
        // 未登录
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(String.valueOf(cameraId))){
            return -1;
        }
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(cameraId));
        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        CompressionAudio audioCompress = hikUtilsApp.getAudioCompress(lUserId);
        Constant.encodeFormat = (int) audioCompress.getByAudioEncType();

        String format = DateTimeUtil.formatThreadLocal(new Date());

        // 保存回调函数的原始音频数据
        String filePathName = USE_DIR + "/AudioFile/ReceiveData/originAudio-" + format + ".g7";
        if (7 == Constant.encodeFormat){
            filePathName = filePathName.replace("g7", "aac");
        }else if (8 == Constant.encodeFormat){
            filePathName = filePathName.replace("g7", "pcm");
        }
        File file = new File(filePathName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        try {
            Constant.outputStream = new FileOutputStream(file,true);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        if (2 == Constant.encodeFormat){
            // 保存回调函数的解码后的音频数据  G711
            File filePcm = new File(USE_DIR + "/AudioFile/ReceiveData/decodeData-" + format + ".pcm");
            if (!filePcm.exists()) {
                try {
                    filePcm.createNewFile();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            try {
                Constant.outputStreamPcm = new FileOutputStream(filePcm, true);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }

        if (cbVoiceDataCallBack == null) {
            cbVoiceDataCallBack = new CbVoiceDataCallBack(redisTemplate);
        }
        int startVoiceTrans = hikUtilsApp.startVoiceTrans(lUserId, cbVoiceDataCallBack, null);

        // 测试接收设备发送一段时间的音频数据
        if (0 != timeItem){
            try {
                Thread.sleep(timeItem);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            log.info("接收设备发送音频数据时间结束。。。");
            return startVoiceTrans;
        }

        voiceComSendDataTest(startVoiceTrans, fileName, format);
        return startVoiceTrans;
    }

    public void voiceComSendDataTest(int lVoiceTranHandle, String fileName, String format){
        String filePathNameTemp = USE_DIR + "/AudioFile/SendData/";
        FileInputStream voiceFile = null;
        try {
            // 原始音频文件
            voiceFile = new FileInputStream(new File(filePathNameTemp + fileName));
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        int dataLength = 0;
        try {
            dataLength = voiceFile.available();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (dataLength < 0){
            log.error("input file dataSize < 0");
            return;
        }

        HCNetSDK.BYTE_ARRAY ptrVoiceByte = new HCNetSDK.BYTE_ARRAY(dataLength);
        try {
            voiceFile.read(ptrVoiceByte.byValue);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        ptrVoiceByte.write();

        int iEncodeSize = 0;
        // 音频编码信息结构体
        HCNetSDK.NET_DVR_AUDIOENC_INFO encodeInfo = new HCNetSDK.NET_DVR_AUDIOENC_INFO();
        encodeInfo.write();
        // 初始化G711音频编码
        Pointer encoder = HC_NET_SDK.NET_DVR_InitG711Encoder(encodeInfo);

        // G711编码音频文件
        File fileEncode = new File(filePathNameTemp + "encodeData-" + format + ".g7");
        if (!fileEncode.exists()) {
            try {
                fileEncode.createNewFile();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileEncode);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        while ((dataLength - iEncodeSize) > DATA_SIZE || ((dataLength - iEncodeSize) > 0 && (dataLength - iEncodeSize) <= DATA_SIZE)) {
            HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(DATA_SIZE);
            int length = Math.min((dataLength - iEncodeSize), DATA_SIZE);
            System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrPcmData.byValue, 0, length);
            ptrPcmData.write();

            // 规定输入数据的大小为320字节, 编码成功是160字节
            HCNetSDK.BYTE_ARRAY ptrG711Data = new HCNetSDK.BYTE_ARRAY(320);
            ptrG711Data.write();

            // 音频编码
            HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM strutAudioParam = new HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM();
            strutAudioParam.in_buf = ptrPcmData.getPointer();
            strutAudioParam.out_buf = ptrG711Data.getPointer();
            strutAudioParam.out_frame_size = ENCODE_DATA_SIZE;
            strutAudioParam.g711_type = 1;
            strutAudioParam.write();

            // G711音频编码
            if (!HC_NET_SDK.NET_DVR_EncodeG711Frame(encoder, strutAudioParam)) {
                log.error("NET_DVR_EncodeG711Frame failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                HC_NET_SDK.NET_DVR_ReleaseG711Encoder(encoder);
                return;
            }
            strutAudioParam.read();
            ptrG711Data.read();

            // 将PCM编码成G711数据写入到文件中
            ByteBuffer bufferG711 = strutAudioParam.out_buf.getByteBuffer(0, strutAudioParam.out_frame_size);
            byte[] bytesG711 = new byte[strutAudioParam.out_frame_size];
            bufferG711.rewind();
            bufferG711.get(bytesG711);
            try {
                fileOutputStream.write(bytesG711);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            iEncodeSize += DATA_SIZE;
            for (int i = 0, size = strutAudioParam.out_frame_size / ENCODE_DATA_SIZE; i < size; i++) {
                HCNetSDK.BYTE_ARRAY ptrG711Send = new HCNetSDK.BYTE_ARRAY(ENCODE_DATA_SIZE);
                System.arraycopy(ptrG711Data.byValue, i * ENCODE_DATA_SIZE, ptrG711Send.byValue, 0, ENCODE_DATA_SIZE);
                ptrG711Send.write();

                // 转发语音G711数据,每次发送160字节
                if (!HC_NET_SDK.NET_DVR_VoiceComSendData(lVoiceTranHandle, ptrG711Send.byValue, ENCODE_DATA_SIZE)) {
                    log.error("NET_DVR_VoiceComSendData failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                    // 数据发送结束,关闭编码库资源
                    HC_NET_SDK.NET_DVR_ReleaseG711Encoder(encoder);
                    return;
                }

                // 发送间隔时间20ms
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        // 数据发送结束,关闭编码库资源
        HC_NET_SDK.NET_DVR_ReleaseG711Encoder(encoder);
    }

    public boolean stopVoiceTransTest(Long cameraId){
        // 释放音频解码资源
        if (Constant.pDecHandle != null) {
            HC_NET_SDK.NET_DVR_ReleaseG711Decoder(Constant.pDecHandle);
        }
        if (Constant.encodeFormat != null){
            Constant.encodeFormat = null;
        }
        // 关闭文件流
        if (Constant.outputStream != null) {
            try {
                Constant.outputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (Constant.outputStreamPcm != null) {
            try {
                Constant.outputStreamPcm.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        // 未登录
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(String.valueOf(cameraId))){
            return false;
        }
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(cameraId));
        // 未开启
        if (Constant.hikDeviceVoiceTransHandleMaps.isEmpty() || !Constant.hikDeviceVoiceTransHandleMaps.containsKey(lUserId)){
            return false;
        }
        Integer lVoiceTranHandle = Constant.hikDeviceVoiceTransHandleMaps.get(lUserId);

        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        // 停止语音转发
        boolean stopVoiceTrans = hikUtilsApp.stopVoiceTrans(lVoiceTranHandle);
        return stopVoiceTrans;
    }

    public void receivedVoiceData(HttpServletRequest request, int lVoiceTranHandle) {
        ServletInputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        int dataLength = 0;
        try {
            dataLength = bufferedInputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (dataLength < 0){
            log.error("input file dataSize < 0");
            return;
        }

        HCNetSDK.BYTE_ARRAY ptrVoiceByte = new HCNetSDK.BYTE_ARRAY(dataLength);
        try {
            bufferedInputStream.read(ptrVoiceByte.byValue);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        ptrVoiceByte.write();

        int iEncodeSize = 0;
        // 音频编码信息结构体
        HCNetSDK.NET_DVR_AUDIOENC_INFO encodeInfo = new HCNetSDK.NET_DVR_AUDIOENC_INFO();
        encodeInfo.write();
        // 初始化G711音频编码
        Pointer encoder = HC_NET_SDK.NET_DVR_InitG711Encoder(encodeInfo);

        while ((dataLength - iEncodeSize) > DATA_SIZE || ((dataLength - iEncodeSize) > 0 && (dataLength - iEncodeSize) <= DATA_SIZE)) {
            HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(DATA_SIZE);
            int length = Math.min((dataLength - iEncodeSize), DATA_SIZE);
            System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrPcmData.byValue, 0, length);
            ptrPcmData.write();

            // 规定输入数据的大小为320字节, 编码成功是160字节
            HCNetSDK.BYTE_ARRAY ptrG711Data = new HCNetSDK.BYTE_ARRAY(320);
            ptrG711Data.write();

            // 音频编码
            HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM strutAudioParam = new HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM();
            strutAudioParam.in_buf = ptrPcmData.getPointer();
            strutAudioParam.out_buf = ptrG711Data.getPointer();
            strutAudioParam.out_frame_size = ENCODE_DATA_SIZE;
            strutAudioParam.g711_type = 1;
            strutAudioParam.write();

            // G711音频编码
            if (!HC_NET_SDK.NET_DVR_EncodeG711Frame(encoder, strutAudioParam)) {
                log.error("NET_DVR_EncodeG711Frame failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                HC_NET_SDK.NET_DVR_ReleaseG711Encoder(encoder);
                return;
            }
            strutAudioParam.read();
            ptrG711Data.read();

            iEncodeSize += DATA_SIZE;
            for (int i = 0, size = strutAudioParam.out_frame_size / ENCODE_DATA_SIZE; i < size; i++) {
                HCNetSDK.BYTE_ARRAY ptrG711Send = new HCNetSDK.BYTE_ARRAY(ENCODE_DATA_SIZE);
                System.arraycopy(ptrG711Data.byValue, i * ENCODE_DATA_SIZE, ptrG711Send.byValue, 0, ENCODE_DATA_SIZE);
                ptrG711Send.write();

                // 转发语音G711数据,每次发送160字节
                if (!HC_NET_SDK.NET_DVR_VoiceComSendData(lVoiceTranHandle, ptrG711Send.byValue, ENCODE_DATA_SIZE)) {
                    log.error("NET_DVR_VoiceComSendData failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                    // 数据发送结束,关闭编码库资源
                    HC_NET_SDK.NET_DVR_ReleaseG711Encoder(encoder);
                    return;
                }

                // 发送间隔时间20ms
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        // 数据发送结束,关闭编码库资源
        HC_NET_SDK.NET_DVR_ReleaseG711Encoder(encoder);
    }
}
