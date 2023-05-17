package com.yjh.accessvideo.module.control.service;

import com.sun.jna.Pointer;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.utils.DateTimeUtil;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.hik.transmit.*;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.CameraConInfo;
import com.yjh.accessvideo.module.control.entity.RobotConInfo;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

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

    private static final int G711_DATA_SIZE = 640;
    private static final int PCM_DATA_SIZE = 1920;
    private static final int G711_ENCODE_DATA_SIZE = 160;

    private static final HCNetSDK HC_NET_SDK = HCNetSDK.INSTANCE;
    private CbVoiceDataCallBack cbVoiceDataCallBack;

    private final CameraConDao cameraConDao;
    private final RedisTemplate redisTemplate;
    private VoiceDataSendToDevice voiceDataSendToDevice = null;

    public VoiceComService(CameraConDao cameraConDao, RedisTemplate redisTemplate) {
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
            result.setMessage(209, "用户注册设备失败,请重试!");
            return result;
        }

        // 未登录
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(String.valueOf(deviceId))) {
            result.setMessage(209, "用户注册设备失败,请重试!");
            return result;
        }

        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(deviceId));
        // 获取对讲音频当前参数
        CompressionAudio audioCompress = hikUtilsApp.getAudioCompress(lUserId);
        Constant.encodeFormat = (int) audioCompress.getByAudioEncType();

        if (cbVoiceDataCallBack == null) {
            cbVoiceDataCallBack = new CbVoiceDataCallBack(webSocketUrl);
        }
        int startVoiceTrans = hikUtilsApp.startVoiceTrans(lUserId, cbVoiceDataCallBack, null);
        if (startVoiceTrans == -1) {
            result.setMessage(209, "开启语音对讲失败,请重试!");
            return result;
        }
        result.setMessage(200, "开启语音对讲功能成功!");
        log.info("--------------Start voice trans success!--------------");
        return result;
    }

    public Result stopVoiceTrans(Long deviceId, Result result) {
        // 释放音频解码资源
        if (Constant.pDecHandle != null) {
            HC_NET_SDK.NET_DVR_ReleaseG711Decoder(Constant.pDecHandle);
        }
        // 将编码当前方式置为空
        if (Constant.encodeFormat != null) {
            Constant.encodeFormat = null;
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

        // 未登录
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(String.valueOf(deviceId))) {
            result.setMessage(209, "用户注册设备失败,请重试!");
            return result;
        }

        // 未开启
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(deviceId));
        if (Constant.hikDeviceVoiceTransHandleMaps.isEmpty() || !Constant.hikDeviceVoiceTransHandleMaps.containsKey(lUserId)) {
            result.setMessage(209, "语音对讲设备未开启,请重试!");
            return result;
        }

        // 停止语音转发
        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        Integer lVoiceTranHandle = Constant.hikDeviceVoiceTransHandleMaps.get(lUserId);
        boolean stopVoiceTrans = hikUtilsApp.stopVoiceTrans(lVoiceTranHandle);
        if (!stopVoiceTrans) {
            result.setMessage(209, "关闭语音对讲失败,请重试!");
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
        if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(String.valueOf(cameraId))) {
            return -1;
        }
        Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(cameraId));
        HikUtilsApp hikUtilsApp = new HikUtilsApp();
        CompressionAudio audioCompress = hikUtilsApp.getAudioCompress(lUserId);
        Constant.encodeFormat = (int) audioCompress.getByAudioEncType();

        String format = DateTimeUtil.formatThreadLocal(new Date());

        // 保存回调函数中的音频数据
        String filePathName = USE_DIR + "/AudioFile/ReceiveData/originAudio-" + format + ".g7";
        if (VoiceTransConstant.AudioEncType.AAC.getCode() == Constant.encodeFormat) {
            filePathName = filePathName.replace("g7", "aac");
        } else if (VoiceTransConstant.AudioEncType.PCM.getCode() == Constant.encodeFormat) {
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
            Constant.outputStream = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        if (VoiceTransConstant.AudioEncType.G711_A.getCode() == Constant.encodeFormat) {
            // 保存回调函数的G711解码后的音频数据
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
            cbVoiceDataCallBack = new CbVoiceDataCallBack(webSocketUrl);
        }
        int startVoiceTrans = hikUtilsApp.startVoiceTrans(lUserId, cbVoiceDataCallBack, null);

        // 测试接收设备发送一段时间的音频数据
        if (0 != timeItem) {
            try {
                Thread.sleep(timeItem);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            log.info("接收设备发送音频数据时间结束。。。");
            return startVoiceTrans;
        }

        voiceSendData(startVoiceTrans, fileName, format, armFramework);

        return startVoiceTrans;
    }

    /**
     * 发送数据
     *
     * @param lVoiceTranHandle 语音对讲句柄
     * @param fileName         文件名称
     * @param format           格式化时间
     * @param armFramework     是否为arm架构  1:是 0:否
     */
    public void voiceSendData(int lVoiceTranHandle, String fileName, String format, Integer armFramework) {
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

        if (1 == armFramework) {
            voiceSendByArm(lVoiceTranHandle, dataLength, ptrVoiceByte);
            return;
        }

        voiceSendNotArm(lVoiceTranHandle, filePathNameTemp, format, dataLength, ptrVoiceByte);
    }

    /**
     * 非arm架构发送的是编码后的G711数据
     *
     * @param lVoiceTranHandle 语音转发句柄
     * @param filePathNameTemp 文件路径
     * @param format           格式化时间
     * @param dataLength       数据长度
     * @param ptrVoiceByte     发送的数据
     */
    public void voiceSendNotArm(int lVoiceTranHandle, String filePathNameTemp, String format, int dataLength, HCNetSDK.BYTE_ARRAY ptrVoiceByte) {
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

        while ((dataLength - iEncodeSize) > G711_DATA_SIZE || ((dataLength - iEncodeSize) > 0 && (dataLength - iEncodeSize) <= G711_DATA_SIZE)) {
            HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(G711_DATA_SIZE);
            int length = Math.min((dataLength - iEncodeSize), G711_DATA_SIZE);
            System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrPcmData.byValue, 0, length);
            ptrPcmData.write();

            // 规定输入数据的大小为320字节, 编码成功是160字节
            HCNetSDK.BYTE_ARRAY ptrG711Data = new HCNetSDK.BYTE_ARRAY(320);
            ptrG711Data.write();

            // 音频编码
            HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM strutAudioParam = new HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM();
            strutAudioParam.in_buf = ptrPcmData.getPointer();
            strutAudioParam.out_buf = ptrG711Data.getPointer();
            strutAudioParam.out_frame_size = G711_ENCODE_DATA_SIZE;
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

            iEncodeSize += G711_DATA_SIZE;
            for (int i = 0, size = strutAudioParam.out_frame_size / G711_ENCODE_DATA_SIZE; i < size; i++) {
                HCNetSDK.BYTE_ARRAY ptrG711Send = new HCNetSDK.BYTE_ARRAY(G711_ENCODE_DATA_SIZE);
                System.arraycopy(ptrG711Data.byValue, i * G711_ENCODE_DATA_SIZE, ptrG711Send.byValue, 0, G711_ENCODE_DATA_SIZE);
                ptrG711Send.write();

                // 转发语音G711数据,每次发送160字节
                if (!HC_NET_SDK.NET_DVR_VoiceComSendData(lVoiceTranHandle, ptrG711Send.byValue, G711_ENCODE_DATA_SIZE)) {
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

    /**
     * arm架构发送的是原始pcm数据
     * 优化发送数据逻辑
     *
     * @param lVoiceTranHandle 语音对讲句柄
     * @param dataLength       数据长度
     * @param ptrVoiceByte     发送的数据
     */
    public void voiceSendByArmOptimize(int lVoiceTranHandle, int dataLength, HCNetSDK.BYTE_ARRAY ptrVoiceByte) {
        log.info("------------------------voiceSendByArm's dataLength:{}------------------------", dataLength);

        byte[] receiveByte = ptrVoiceByte.byValue;
        if (voiceDataSendToDevice == null) {
            voiceDataSendToDevice = new VoiceDataSendToDevice();
        }
        voiceDataSendToDevice.voiceSendByArm(lVoiceTranHandle, receiveByte, dataLength);

    }

    /**
     * arm架构发送的是原始pcm数据
     *
     * @param lVoiceTranHandle 语音对讲句柄
     * @param dataLength       数据长度
     * @param ptrVoiceByte     发送的数据
     */
    public void voiceSendByArm(int lVoiceTranHandle, int dataLength, HCNetSDK.BYTE_ARRAY ptrVoiceByte) {
        int iEncodeSize = 0;
        log.info("------------------------voiceSendByArm's dataLength:{}------------------------", dataLength);

        while ((dataLength - iEncodeSize) > PCM_DATA_SIZE || ((dataLength - iEncodeSize) > 0 && (dataLength - iEncodeSize) <= PCM_DATA_SIZE)) {
            HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(PCM_DATA_SIZE);
            int length = Math.min((dataLength - iEncodeSize), PCM_DATA_SIZE);
            System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrPcmData.byValue, 0, length);
            ptrPcmData.write();

            iEncodeSize += PCM_DATA_SIZE;
            // 每次发送固定大小1920字节数据
            if (!HC_NET_SDK.NET_DVR_VoiceComSendData(lVoiceTranHandle, ptrPcmData.byValue, PCM_DATA_SIZE)) {
                log.error("NET_DVR_VoiceComSendData failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                return;
            }

            // 1920 / (16000 * 1 * 16 / 8 * 1 / 1000) = 60
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
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
            String deviceId = new String(deviceIdByte, StandardCharsets.UTF_8);
            // 音频数据
            byte[] dataByte = new byte[allDataBytes.length - 1 - i];
            System.arraycopy(allDataBytes, i + 1, dataByte, 0, allDataBytes.length - 1 - i);

            // 未登录
            if (Constant.hikDeviceUserIdMaps.isEmpty() || !Constant.hikDeviceUserIdMaps.containsKey(deviceId)) {
                log.error("hikDeviceUserIdMaps is ERROR");
                return;
            }

            // 未开启
            Integer lUserId = Constant.hikDeviceUserIdMaps.get(deviceId);
            if (Constant.hikDeviceVoiceTransHandleMaps.isEmpty() || !Constant.hikDeviceVoiceTransHandleMaps.containsKey(lUserId)) {
                log.error("hikDeviceVoiceTransHandleMaps is ERROR");
                return;
            }

            Integer lVoiceTranHandle = Constant.hikDeviceVoiceTransHandleMaps.get(lUserId);
            int dataLength = dataByte.length;
            HCNetSDK.BYTE_ARRAY ptrVoiceByte = new HCNetSDK.BYTE_ARRAY(dataLength);
            ptrVoiceByte.byValue = dataByte;
            ptrVoiceByte.write();

            voiceSendByArmOptimize(lVoiceTranHandle, dataLength, ptrVoiceByte);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
