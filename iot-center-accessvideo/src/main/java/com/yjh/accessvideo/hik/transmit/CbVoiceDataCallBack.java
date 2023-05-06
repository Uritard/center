package com.yjh.accessvideo.hik.transmit;

import com.sun.jna.Pointer;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.hik.HCNetSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.ByteBuffer;

/**
 * 语音转发回调函数,接收设备传来的音频数据
 * @author 丫C
 * @date 2023/4/18
 */
@Slf4j
public class CbVoiceDataCallBack implements HCNetSDK.FVoiceDataCallBack_MR_V30{

    private static final HCNetSDK HC_NET_SDK = HCNetSDK.INSTANCE;
    private final RedisTemplate redisTemplate;
    static int AUDIO_HEADER_LENGTH = 44;

    public CbVoiceDataCallBack(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void invoke(int lVoiceComHandle, Pointer pRecvDataBuffer, int dwBufSize, byte byAudioFlag, Pointer pUser) {
        // byAudioFlag 语音标志：0- 本地采集的数据，1- 设备发送过来的语音数据
        if (0 == byAudioFlag){
            log.info("This is client sends audio data...");
            return;
        }

        log.info("进来了！！！数据大小:{}", dwBufSize);
        try {
            // 将设备发送的原始音频数据写入文件
            ByteBuffer buffers = pRecvDataBuffer.getByteBuffer(0, dwBufSize);
            byte[] originBytes = new byte[dwBufSize];
            buffers.rewind();
            buffers.get(originBytes);
            Constant.outputStream.write(originBytes);

            // 若编码格式为G711时,需要解码
            if (2 == Constant.encodeFormat){
                // 初始化音频解码
                if (Constant.pDecHandle == null) {
                    Constant.pDecHandle = HC_NET_SDK.NET_DVR_InitG711Decoder();
                }
                // G711音频解码
                HCNetSDK.NET_DVR_AUDIODEC_PROCESS_PARAM strutAudioParam = new HCNetSDK.NET_DVR_AUDIODEC_PROCESS_PARAM();
                strutAudioParam.in_buf = pRecvDataBuffer;
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
                    return;
                }
                strutAudioParam.read();

                // 读取解码之后PCM音频数据
                ByteBuffer bufferPcm = strutAudioParam.out_buf.getByteBuffer(0, strutAudioParam.out_frame_size);
                byte[] bytesPcm = new byte[strutAudioParam.out_frame_size];
                bufferPcm.rewind();
                bufferPcm.get(bytesPcm);

                // 打印收到设备端的音源数据
                bufferPcm.flip();
                int len = bufferPcm.limit() - bufferPcm.position();
                byte[] bytes = new byte[len];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = bufferPcm.get();
                }
                StringBuilder str = new StringBuilder();
                for (byte byteItem : bytes) {
                    str.append(String.format("%02x ", byteItem));
                }
                log.info("收到设备端的音源数据: {}", str);

                // 将设备发送的pcm音频数据写入文件
                Constant.outputStreamPcm.write(bytesPcm);

                // 组装wav发送ws到其他服务
                // 方式一
                WaveHeader header = new WaveHeader();
                header.fileLength = bytesPcm.length + (44 - 8);
                header.fmtHdrLength = 16;
                header.formatTag = 0x0001;
                header.channels = 1;
                header.samplesPerSec = 8000;
                header.bitsPerSample = 16;
                header.blockAlign = (short) (header.channels * header.bitsPerSample / 8);
                header.avgBytesPerSec = header.blockAlign * header.samplesPerSec;
                header.dataHdrLength = bytesPcm.length;
                byte[] headerBytes = header.getHeader();

                assert headerBytes.length == 44;
                byte[] byteResult = new byte[headerBytes.length + bytesPcm.length];
                System.arraycopy(headerBytes, 0, byteResult, 0, headerBytes.length);
                System.arraycopy(bytesPcm, 0, byteResult, headerBytes.length, bytesPcm.length);
                StringBuilder str1 = new StringBuilder();
                for (byte byteItem : byteResult) {
                    str1.append(String.format("%02x ", byteItem));
                }
                log.info("加头的音源数据1: {}", str1);

                // 方式二
                byte[] byteResult2 = new byte[headerBytes.length + bytesPcm.length];
                byte[] waveFileHeader = getWaveFileHeader(bytesPcm.length, 8000, 1, 16);
                System.arraycopy(waveFileHeader, 0, byteResult2, 0, waveFileHeader.length);
                System.arraycopy(bytesPcm, 0, byteResult2, waveFileHeader.length, bytesPcm.length);
                StringBuilder str2 = new StringBuilder();
                for (byte byteItem : byteResult2) {
                    str2.append(String.format("%02x ", byteItem));
                }
                log.info("加头的音源数据: {}", str2);

                // 调用其他服务发送ws
                String webSocketUrl = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:sendMsgBufferUrl","content"));
                Constant.websocketSendMsgBuffer(webSocketUrl, byteResult2);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 生成音频文件头部消息
     * @param totalAudioLen 不包括header的音频数据总长度
     * @param sampleRate 采样率,也就是录制时使用的频率、音频采样级别 8000 = 8KHz
     * @param channels audioRecord的声道数1/2
     * @param audioFormat  采样精度; 譬如 16bit
     * @return
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
}
