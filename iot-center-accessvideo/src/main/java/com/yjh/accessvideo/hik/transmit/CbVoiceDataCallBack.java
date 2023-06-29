package com.yjh.accessvideo.hik.transmit;

import com.sun.jna.Pointer;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.utils.DateTimeUtil;
import com.yjh.accessvideo.hik.HCNetSDK;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;

/**
 * 语音转发回调函数,接收设备传来的音频数据
 * @author 丫C
 * @date 2023/4/18
 */
@Slf4j
public class CbVoiceDataCallBack implements HCNetSDK.FVoiceDataCallBack_MR_V30{

    private static final HCNetSDK HC_NET_SDK = HCNetSDK.INSTANCE;
    private final String webSocketUrl;
    private final Long deviceId;
    private static final int AUDIO_HEADER_LENGTH = 44;

    public CbVoiceDataCallBack(String webSocketUrl, Long deviceId){
        this.webSocketUrl = webSocketUrl;
        this.deviceId = deviceId;
    }

    /**
     * 保证多线程修改不会共同操作
     */
    public static synchronized void setDecHandle() {
        if (Constant.pDecHandle == null) {
            Constant.pDecHandle = HC_NET_SDK.NET_DVR_InitG711Decoder();
        }
    }

    @Override
    public void invoke(int lVoiceComHandle, Pointer receiveDataBuffer, int dwBufSize, byte byAudioFlag, Pointer pUser) {
        // byAudioFlag 语音标志：0- 本地采集的数据，1- 设备发送过来的语音数据
        if (0 == byAudioFlag){
            log.info("This is client sends audio data...");
            return;
        }

        log.info("Come in！！！Data size is :{},now time:{}", dwBufSize, DateTimeUtil.getDateTimeString(new Date(), true));

        try {
            // 将设备发送的原始音频数据写入文件
            ByteBuffer buffers = receiveDataBuffer.getByteBuffer(0, dwBufSize);
            byte[] originBytes = new byte[dwBufSize];
            buffers.rewind();
            buffers.get(originBytes);
            if (Objects.nonNull(Constant.outputStream)){
                Constant.outputStream.write(originBytes);
            }

            // 若编码格式为G711时,需要解码   G711 -> PCM
            if (VoiceTransConstant.AudioEncType.G711_A.getCode() == Constant.hikDeviceEncodeFormatMaps.get(deviceId)){
                if (StringUtils.equals(VoiceTransConstant.X86, Constant.SYSTEM_ARCH)) {
                    // 初始化音频解码
                    setDecHandle();

                    // G711音频解码
                    HCNetSDK.NET_DVR_AUDIODEC_PROCESS_PARAM strutAudioParam = new HCNetSDK.NET_DVR_AUDIODEC_PROCESS_PARAM();
                    strutAudioParam.in_buf = receiveDataBuffer;
                    strutAudioParam.in_data_size = dwBufSize;
                    HCNetSDK.BYTE_ARRAY ptrVoiceData = new HCNetSDK.BYTE_ARRAY(320);
                    ptrVoiceData.write();

                    strutAudioParam.out_buf = ptrVoiceData.getPointer();
                    strutAudioParam.out_frame_size = 320;
                    strutAudioParam.g711_type = 1;
                    strutAudioParam.write();

                    // G711音频解码调用sdk
                    if (!HC_NET_SDK.NET_DVR_DecodeG711Frame(Constant.pDecHandle, strutAudioParam)) {
                        log.error("NET_DVR_DecodeG711Frame failed, error code:{}", HC_NET_SDK.NET_DVR_GetLastError());
                        if (0 != HC_NET_SDK.NET_DVR_GetLastError()){
                            return;
                        }
                    }
                    strutAudioParam.read();

                    // 读取解码之后PCM音频数据
                    ByteBuffer bufferPcm = strutAudioParam.out_buf.getByteBuffer(0, strutAudioParam.out_frame_size);
                    byte[] bytesPcm = new byte[strutAudioParam.out_frame_size];
                    bufferPcm.rewind();
                    bufferPcm.get(bytesPcm);

//                    printRecData(bufferPcm);

                    if (Objects.nonNull(Constant.outputStreamPcm)) {
                        // 将设备发送的pcm音频数据写入文件
                        Constant.outputStreamPcm.write(bytesPcm);
                    }
                    originBytes = bytesPcm;
                    log.info("pcm data size is {}", bytesPcm.length);
                } else {
                    byte[] bytes = AudioFormatUtil.decode(originBytes);
                    originBytes = bytes;
                    if (Objects.nonNull(Constant.outputStreamPcm)) {
                        Constant.outputStreamPcm.write(bytes);
                    }
                    log.info("pcm data size is {}", bytes.length);
                }
            }

            if (VoiceTransConstant.AudioEncType.AAC.getCode() == Constant.hikDeviceEncodeFormatMaps.get(deviceId)) {
                log.info("Todo something by Aac");
            }

            // 组装wave并调用其他服务发送ws
            byte[] bytesResult = createWaveFile(originBytes, Constant.hikDeviceEncodeFormatMaps.get(deviceId));
            log.info("bytesResult data size is {}", bytesResult.length);
            Constant.websocketSendMsgBuffer(webSocketUrl, bytesResult);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 生成wav音频数据
     * @param originBytes 原始音频数据
     * @param encodeFormat 编码格式
     * @return byte[]
     */
    private byte[] createWaveFile(byte[] originBytes, Integer encodeFormat){
        // 总长 = wav头 + 原始音频长度
        byte[] byteResult = new byte[44 + originBytes.length];

        byte[] headerBytes = new byte[0];
        try {
            int totalAudioLen = originBytes.length;
            int sampleRate = 2 == encodeFormat ? 8000 : 16000;
            headerBytes = getWaveFileHeader(totalAudioLen, sampleRate, 1, 16);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }

        System.arraycopy(headerBytes, 0, byteResult, 0, headerBytes.length);
        System.arraycopy(originBytes, 0, byteResult, headerBytes.length, originBytes.length);
        return byteResult;
    }

    /**
     * 生成音频文件头部消息
     * @param totalAudioLen 不包括header的音频数据总长度
     * @param sampleRate 采样率,也就是录制时使用的频率、音频采样级别 8000 = 8KHz
     * @param channels audioRecord的声道数1/2
     * @param audioFormat  采样精度; 譬如 16bit
     * @return byte[]
     */
    public static byte[] getWaveFileHeader(long totalAudioLen, long sampleRate, int channels, long audioFormat) {
        long totalDataLen = totalAudioLen + 36;
        byte[] header = new byte[AUDIO_HEADER_LENGTH];
        long byteRate = (sampleRate * audioFormat * channels) / 8;

        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;

        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);

        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align
        header[32] = (byte) (audioFormat/8 * channels);
        header[33] = 0;
        header[34] = (byte) audioFormat;
        header[35] = 0;
        // data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }

    private void printRecData(ByteBuffer bufferPcm) {
        bufferPcm.flip();
        int len = bufferPcm.limit() - bufferPcm.position();
        byte[] bytes = new byte[len];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bufferPcm.get();
        }
        printByte(bytes);
    }

    private void printByte(byte[] bytes) {
        StringBuilder str = new StringBuilder();
        for (byte byteItem : bytes) {
            str.append(String.format("%02x ", byteItem));
        }
        log.info("音源数据: {}", str);
    }
}
