package com.yjh.accessvideo.module.control.service;

import com.sun.jna.Pointer;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.hik.transmit.*;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.CameraConInfo;
import com.yjh.accessvideo.module.control.entity.RobotConInfo;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 丫C
 * @date 2023/4/17
 */
@Service
public class VoiceComService {

    @Value("${spring.websocket.send.url}")
    private String webSocketUrl;

    private final Logger log = LoggerFactory.getLogger(VoiceComService.class);
    private static final String USE_DIR = System.getProperty("user.dir");
    private static final HCNetSDK HC_NET_SDK = HCNetSDK.INSTANCE;
    private CbVoiceDataCallBack cbVoiceDataCallBack;

    private final CameraConDao cameraConDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private Map<Long, VoiceDataSendToDevice> voiceDataSendToDevice = new ConcurrentHashMap<>(4);

    public VoiceComService(CameraConDao cameraConDao, RedisTemplate<String, Object> redisTemplate) {
        this.cameraConDao = cameraConDao;
        this.redisTemplate = redisTemplate;
    }

    public boolean deviceLogin(Long deviceId) {
        HikDeviceInfo hikDeviceInfo = new HikDeviceInfo();

        String port = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:cameraServicePort", "content"));
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(deviceId, null);
        if (Objects.nonNull(cameraConInfo)) {
            hikDeviceInfo.setDeviceIp(cameraConInfo.getCameraIp());
            hikDeviceInfo.setDevicePort(Integer.valueOf(port));
            hikDeviceInfo.setUserName(cameraConInfo.getCameraManager());
            hikDeviceInfo.setPassword(cameraConInfo.getCameraCode());
            hikDeviceInfo.setHikDeviceId(cameraConInfo.getCameraId());
        } else {
            RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(deviceId);
            hikDeviceInfo.setDeviceIp(robotConInfo.getLightIp());
            hikDeviceInfo.setDevicePort(Integer.valueOf(port));
            hikDeviceInfo.setUserName(robotConInfo.getIdentityManager());
            hikDeviceInfo.setPassword(robotConInfo.getIdentityCode());
            hikDeviceInfo.setHikDeviceId(robotConInfo.getRobotId());
        }

        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        return hikUtilsApp.deviceLogin(hikDeviceInfo);
    }

    public Result startVoiceTrans(Long deviceId, Result result) {
        boolean deviceLogin = deviceLogin(deviceId);
        if (!deviceLogin) {
            result.setMessage(209, VoiceTransConstant.DEVICE_LOGIN_ERROR);
            return result;
        }

        // 未登录
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(deviceId)) {
            result.setMessage(209, VoiceTransConstant.DEVICE_LOGIN_ERROR);
            return result;
        }

        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(deviceId);
        // 获取对讲音频当前参数
        CompressionAudio audioCompress = hikUtilsApp.getAudioCompress(lUserId);
        Constant.hikDeviceEncodeFormatMaps.put(deviceId, (int) audioCompress.getByAudioEncType());

        TestAudioFile.testRecAudioFile(deviceId, audioCompress.getByAudioEncType());
        TestAudioFile.testSendAudioFile(deviceId);

        if (cbVoiceDataCallBack == null) {
            cbVoiceDataCallBack = new CbVoiceDataCallBack(webSocketUrl, deviceId);
        }
        int startVoiceTrans = hikUtilsApp.startVoiceTrans(lUserId, cbVoiceDataCallBack, null);
        if (startVoiceTrans == -1) {
            result.setMessage(209, VoiceTransConstant.START_VOICE_ERROR);
            return result;
        }
        result.setMessage(200, "开启语音对讲功能成功!");
        log.info("--------------Start voice trans success!--------------");
        return result;
    }

    public Result stopVoiceTrans(Long deviceId, Result result) {
        // (如果是刷新页面【deviceId:5201314】,关闭所有正在连接的语音设备)
        // 稍等一下再停止操作,防止还有正在发送和接收的数据处理
        waitTimeInterval(2000);

        // 释放音频解码资源
        if (Constant.pDecHandle != null) {
            HC_NET_SDK.NET_DVR_ReleaseG711Decoder(Constant.pDecHandle);
        }

        // 关闭文件流
        if (Constant.outputStream != null) {
            try {
                Constant.outputStream.close();
                Constant.outputStream = null;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (Constant.outputStreamPcm != null) {
            try {
                Constant.outputStreamPcm.close();
                Constant.outputStreamPcm = null;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        if (VoiceTransConstant.CONSTANT == deviceId) {
            Constant.hikDeviceEncodeFormatMaps = new ConcurrentHashMap<>(4);
            if (MapUtils.isNotEmpty(Constant.hikDeviceUserIdMaps) && MapUtils.isNotEmpty(Constant.hikDeviceVoiceTransHandleMaps)) {
                Constant.hikDeviceVoiceTransHandleMaps.forEach((key, value) -> {
                    HikUtilsApp hikUtilsApp = new HikUtilsApp();
                    hikUtilsApp.stopVoiceTrans(value);
                });
            }
            result.setMessage(200, "关闭所有设备语音对讲功能成功!");
            log.info("--------------Stop all voice trans success!--------------");
            return result;
        }
        // 删除编码类型
        Constant.hikDeviceEncodeFormatMaps.remove(deviceId);

        // 未登录
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(deviceId)) {
            result.setMessage(209, VoiceTransConstant.DEVICE_LOGIN_ERROR);
            return result;
        }

        // 未开启
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(deviceId);
        if (Constant.hikDeviceVoiceTransHandleMaps.isEmpty() || !Constant.hikDeviceVoiceTransHandleMaps.containsKey(lUserId)) {
            result.setMessage(209, VoiceTransConstant.NOT_OPEN_VOICE);
            return result;
        }

        // 停止语音转发
        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        Integer lVoiceTranHandle = Constant.hikDeviceVoiceTransHandleMaps.get(lUserId);
        boolean stopVoiceTrans = hikUtilsApp.stopVoiceTrans(lVoiceTranHandle);
        if (!stopVoiceTrans) {
            result.setMessage(209, VoiceTransConstant.STOP_VOICE_ERROR);
            return result;
        }
        result.setMessage(200, "关闭语音对讲功能成功!");
        log.info("--------------Stop voice trans success!--------------");
        return result;
    }

    /**
     * 语音对讲测试:开启 + 发送数据 + 处理回调
     *
     * @param cameraId     相机id
     * @param timeItem     等待时间间隔，主要为了测试设备端只发数据
     * @param fileName     文件名称
     * @param armFramework 是否为arm架构
     * @return int
     */
    public int startVoiceTransTest(Long cameraId, long timeItem, String fileName, Integer armFramework) {
        // 未登录
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(cameraId)) {
            return -1;
        }
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(cameraId);
        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        CompressionAudio audioCompress = hikUtilsApp.getAudioCompress(lUserId);
        Constant.hikDeviceEncodeFormatMaps.put(cameraId, (int) audioCompress.getByAudioEncType());

        // 测试数据
        TestAudioFile.testRecAudioFile(cameraId, Constant.hikDeviceEncodeFormatMaps.get(cameraId));
        TestAudioFile.testSendAudioFile(cameraId);

        if (cbVoiceDataCallBack == null) {
            cbVoiceDataCallBack = new CbVoiceDataCallBack(webSocketUrl, cameraId);
        }
        int startVoiceTrans = hikUtilsApp.startVoiceTrans(lUserId, cbVoiceDataCallBack, null);

        // 测试接收设备发送一段时间的音频数据
        if (0 != timeItem) {
            waitTimeInterval(timeItem);
            log.info("接收设备发送音频数据时间结束。。。");
            return startVoiceTrans;
        }

        voiceSendDataTest(startVoiceTrans, fileName, armFramework, cameraId);

        return startVoiceTrans;
    }

    /**
     * 发送数据
     *
     * @param lVoiceTranHandle 语音对讲句柄
     * @param fileName         文件名称
     * @param armFramework     是否为arm架构  1:是 0:否
     * @param deviceId         设备id
     */
    public void voiceSendDataTest(int lVoiceTranHandle, String fileName, Integer armFramework, Long deviceId) {
        String filePathNameTemp = USE_DIR + "/AudioFile/SendData/";
        FileInputStream voiceFile = null;
        try {
            // 原始音频文件
            voiceFile = new FileInputStream(filePathNameTemp + fileName);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        int dataLength = 0;
        try {
            dataLength = voiceFile.available();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (dataLength < 0) {
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

        if (VoiceTransConstant.AudioEncType.PCM.getCode() == Constant.hikDeviceEncodeFormatMaps.get(deviceId)) {
            voiceSendPcm(lVoiceTranHandle, ptrVoiceByte);
        } else if (VoiceTransConstant.AudioEncType.G711_A.getCode() == Constant.hikDeviceEncodeFormatMaps.get(deviceId)) {
            if (1 == armFramework) {
                voiceSendG711ByArm(lVoiceTranHandle, ptrVoiceByte);
            } else {
                voiceSendG711NotArm(lVoiceTranHandle, dataLength, ptrVoiceByte);
            }
        }
    }

    /**
     * 接收音频数据并发送给设备
     *
     * @param request  请求
     */
    public void receiveAndSendVoiceData(HttpServletRequest request) {
        try {
            InputStream inputStream = request.getInputStream();
            byte[] allDataBytes = IOUtils.toByteArray(inputStream);
            // 设备id长度
            byte[] lengthByte = new byte[1];
            System.arraycopy(allDataBytes, 0, lengthByte, 0, 1);
            String length = new String(lengthByte, StandardCharsets.UTF_8);
            int i = Integer.parseInt(length);
            // 设备id
            byte[] deviceIdByte = new byte[i];
            System.arraycopy(allDataBytes, 1, deviceIdByte, 0, i);
            Long deviceId = NumberUtils.toLong(new String(deviceIdByte, StandardCharsets.UTF_8));
            // 音频数据
            byte[] dataByte = new byte[allDataBytes.length - 1 - i];
            System.arraycopy(allDataBytes, i + 1, dataByte, 0, allDataBytes.length - 1 - i);

            // 未登录
            if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(deviceId)) {
                log.error("Without this device in hikDeviceUserIdMaps");
                return;
            }

            // 未开启
            Integer lUserId = Constant.hikDeviceUserIdMaps.get(deviceId);
            if (Constant.hikDeviceVoiceTransHandleMaps.isEmpty() || !Constant.hikDeviceVoiceTransHandleMaps.containsKey(lUserId)) {
                log.error("Without this device in hikDeviceVoiceTransHandleMaps");
                return;
            }

            if (Objects.nonNull(Constant.sendStreamPcm)){
                try {
                    Constant.sendStreamPcm.write(dataByte);
                } catch (Exception e) {
                    log.error("保存原始发送文件失败", e);
                }
            }

            Integer lVoiceTranHandle = Constant.hikDeviceVoiceTransHandleMaps.get(lUserId);
            int dataLength = dataByte.length;
            HCNetSDK.BYTE_ARRAY ptrVoiceByte = new HCNetSDK.BYTE_ARRAY(dataLength);
            ptrVoiceByte.byValue = dataByte;
            ptrVoiceByte.write();

            if (StringUtils.equals(VoiceTransConstant.X86, Constant.SYSTEM_ARCH)) {
                voiceSendByNotArm(lVoiceTranHandle, deviceId, ptrVoiceByte, dataLength);
            } else {
                voiceSendByArmOptimize(lVoiceTranHandle, deviceId, ptrVoiceByte, dataLength);
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * X86架构--发送的是编码后的G711数据
     *
     * @param lVoiceTranHandle 语音对讲句柄
     * @param dataLength       数据长度
     * @param ptrVoiceByte     发送的数据
     */
    public void voiceSendG711NotArm(int lVoiceTranHandle, int dataLength, HCNetSDK.BYTE_ARRAY ptrVoiceByte) {
        int iEncodeSize = 0;
        // 音频编码信息结构体
        HCNetSDK.NET_DVR_AUDIOENC_INFO encodeInfo = new HCNetSDK.NET_DVR_AUDIOENC_INFO();
        encodeInfo.write();
        // 初始化G711音频编码
        Pointer encoder = HC_NET_SDK.NET_DVR_InitG711Encoder(encodeInfo);

        int g711DataSize = VoiceTransConstant.G711_DATA_SIZE;
        int g711EncodeDataSize = VoiceTransConstant.G711_ENCODE_DATA_SIZE;
        while ((dataLength - iEncodeSize) > g711DataSize || ((dataLength - iEncodeSize) > 0 && (dataLength - iEncodeSize) <= g711DataSize)) {
            HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(g711DataSize);
            int length = Math.min((dataLength - iEncodeSize), g711DataSize);
            System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrPcmData.byValue, 0, length);
            ptrPcmData.write();

            // 规定输入数据的大小为320字节, 编码成功是160字节
            HCNetSDK.BYTE_ARRAY ptrG711Data = new HCNetSDK.BYTE_ARRAY(320);
            ptrG711Data.write();

            // 音频编码
            HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM strutAudioParam = new HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM();
            strutAudioParam.in_buf = ptrPcmData.getPointer();
            strutAudioParam.out_buf = ptrG711Data.getPointer();
            strutAudioParam.out_frame_size = g711EncodeDataSize;
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
            if (Objects.nonNull(Constant.sendStream)){
                try {
                    ByteBuffer bufferG711 = strutAudioParam.out_buf.getByteBuffer(0, strutAudioParam.out_frame_size);
                    byte[] bytesG711 = new byte[strutAudioParam.out_frame_size];
                    bufferG711.rewind();
                    bufferG711.get(bytesG711);
                    Constant.sendStream.write(bytesG711);
                } catch (Exception e) {
                    log.error("保存转码后发送文件失败", e);
                }
            }

            iEncodeSize += g711DataSize;
            for (int i = 0, size = strutAudioParam.out_frame_size / g711EncodeDataSize; i < size; i++) {
                HCNetSDK.BYTE_ARRAY ptrG711Send = new HCNetSDK.BYTE_ARRAY(g711EncodeDataSize);
                System.arraycopy(ptrG711Data.byValue, i * g711EncodeDataSize, ptrG711Send.byValue, 0, g711EncodeDataSize);
                ptrG711Send.write();

                // 转发语音G711数据,每次发送160字节
                if (!HC_NET_SDK.NET_DVR_VoiceComSendData(lVoiceTranHandle, ptrG711Send.byValue, g711EncodeDataSize)) {
                    log.error("NET_DVR_VoiceComSendData failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                    // 数据发送结束,关闭编码库资源
                    HC_NET_SDK.NET_DVR_ReleaseG711Encoder(encoder);
                    return;
                }

                waitTimeInterval(VoiceTransConstant.G711_DATA_INTERVAL);
            }
        }
        // 数据发送结束,关闭编码库资源
        HC_NET_SDK.NET_DVR_ReleaseG711Encoder(encoder);
    }

    /**
     * arm架构--发送的是编码后的G711数据
     *
     * @param lVoiceTranHandle 语音对讲句柄
     * @param ptrVoiceByte     发送的数据
     */
    public void voiceSendG711ByArm(int lVoiceTranHandle, HCNetSDK.BYTE_ARRAY ptrVoiceByte) {
        int iEncodeSize = 0;

        byte[] g711AllData = AudioFormatUtil.encode(ptrVoiceByte.byValue);
        int dataLength = g711AllData.length;

        int g711EncodeDataSize = VoiceTransConstant.G711_ENCODE_DATA_SIZE;
        while ((dataLength - iEncodeSize) > g711EncodeDataSize || ((dataLength - iEncodeSize) > 0 && (dataLength - iEncodeSize) <= g711EncodeDataSize)) {
            HCNetSDK.BYTE_ARRAY ptrG711Data = new HCNetSDK.BYTE_ARRAY(g711EncodeDataSize);
            int length = Math.min((dataLength - iEncodeSize), g711EncodeDataSize);
            System.arraycopy(g711AllData, iEncodeSize, ptrG711Data.byValue, 0, length);
            ptrG711Data.write();

            iEncodeSize += g711EncodeDataSize;

            // 转发语音G711数据,每次发送160字节
            if (!HC_NET_SDK.NET_DVR_VoiceComSendData(lVoiceTranHandle, ptrG711Data.byValue, g711EncodeDataSize)) {
                log.error("NET_DVR_VoiceComSendData failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                return;
            }

            waitTimeInterval(VoiceTransConstant.G711_DATA_INTERVAL);
        }
    }

    /**
     * arm架构和X86架构--发送的是原始pcm数据
     *
     * @param lVoiceTranHandle 语音对讲句柄
     * @param ptrVoiceByte     发送的数据
     */
    public void voiceSendPcm(int lVoiceTranHandle, HCNetSDK.BYTE_ARRAY ptrVoiceByte) {
        int iEncodeSize = 0;
        int dataLength = ptrVoiceByte.byValue.length;

        int pcmDataSize = VoiceTransConstant.PCM_DATA_SIZE;
        while ((dataLength - iEncodeSize) > pcmDataSize || ((dataLength - iEncodeSize) > 0 && (dataLength - iEncodeSize) <= pcmDataSize)) {
            HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(pcmDataSize);
            int length = Math.min((dataLength - iEncodeSize), pcmDataSize);
            System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrPcmData.byValue, 0, length);
            ptrPcmData.write();

            // 将发送的语音数据写入到文件中
            if (Objects.nonNull(Constant.sendStream)){
                try {
                    Constant.sendStream.write(ptrPcmData.byValue);
                } catch (Exception e) {
                    log.error("保存转码后发送文件失败", e);
                }
            }

            iEncodeSize += pcmDataSize;
            // 转发语音PCM数据,每次发送1920字节
            if (!HC_NET_SDK.NET_DVR_VoiceComSendData(lVoiceTranHandle, ptrPcmData.byValue, pcmDataSize)) {
                log.error("NET_DVR_VoiceComSendData failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                return;
            }

            waitTimeInterval(VoiceTransConstant.PCM_DATA_INTERVAL);
        }
    }

    /**
     * arm架构--原始pcm数据 / 编码后的G711数据
     * 优化发送数据逻辑
     *
     * @param lVoiceTranHandle 语音对讲句柄
     * @param deviceId         设备id
     * @param ptrVoiceByte     发送的数据
     * @param dataLength       数据长度
     */
    public void voiceSendByArmOptimize(int lVoiceTranHandle, Long deviceId, HCNetSDK.BYTE_ARRAY ptrVoiceByte, int dataLength) {
        log.info("------------------------voiceSendByArm's dataLength:{}------------------------", dataLength);
        byte[] receiveByte = ptrVoiceByte.byValue;

        // 默认为PCM编码
        int dataSize = VoiceTransConstant.PCM_DATA_SIZE;
        long timeInterval = VoiceTransConstant.PCM_DATA_INTERVAL;

        if (Constant.hikDeviceEncodeFormatMaps.isEmpty() || !Constant.hikDeviceEncodeFormatMaps.containsKey(deviceId)) {
            log.error("Without this device in hikDeviceEncodeFormatMaps");
            return;
        }

        if (VoiceTransConstant.AudioEncType.G711_A.getCode() == Constant.hikDeviceEncodeFormatMaps.get(deviceId)) {
            dataSize = VoiceTransConstant.G711_ENCODE_DATA_SIZE;
            timeInterval = VoiceTransConstant.G711_DATA_INTERVAL;
            receiveByte = AudioFormatUtil.encode(receiveByte);
            dataLength = receiveByte.length;
        }

        int finalDataSize = dataSize;
        long finalTimeInterval = timeInterval;
        voiceDataSendToDevice.computeIfAbsent(deviceId, key -> new VoiceDataSendToDevice(finalDataSize, finalTimeInterval))
                .voiceSendByArm(lVoiceTranHandle, receiveByte, dataLength);
    }

    /**
     * X86架构--原始pcm数据 / 编码后的G711数据
     *
     * @param lVoiceTranHandle 语音对讲句柄
     * @param deviceId         设备id
     * @param ptrVoiceByte     发送的数据
     * @param dataLength       数据长度
     */
    public void voiceSendByNotArm(int lVoiceTranHandle, Long deviceId, HCNetSDK.BYTE_ARRAY ptrVoiceByte, int dataLength) {
        if (Constant.hikDeviceEncodeFormatMaps.isEmpty() || !Constant.hikDeviceEncodeFormatMaps.containsKey(deviceId)) {
            log.error("Without this device in hikDeviceEncodeFormatMaps");
            return;
        }

        if (VoiceTransConstant.AudioEncType.G711_A.getCode() == Constant.hikDeviceEncodeFormatMaps.get(deviceId)) {
            voiceSendG711NotArm(lVoiceTranHandle, dataLength, ptrVoiceByte);
        } else {
            voiceSendPcm(lVoiceTranHandle, ptrVoiceByte);
        }
    }

    /**
     * 等待时间间隔
     *
     * @param timeInterval 时间间隔
     */
    private void waitTimeInterval(long timeInterval) {
        try {
            Thread.sleep(timeInterval);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
