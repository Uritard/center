package com.yjh.accessvideo.hik.handler;

import com.sun.jna.Pointer;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.hik.HCNetSDK;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * 语音转发回调函数,接收设备传来的音频数据
 * @author 丫C
 * @date 2023/4/18
 */
@Slf4j
public class CbVoiceDataCallBack implements HCNetSDK.FVoiceDataCallBack_MR_V30{

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

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
                    Constant.pDecHandle = hCNetSDK.NET_DVR_InitG711Decoder();
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

                // G711音频解码
                if (!hCNetSDK.NET_DVR_DecodeG711Frame(Constant.pDecHandle, strutAudioParam)) {
                    log.error("NET_DVR_DecodeG711Frame failed, error code:{}", hCNetSDK.NET_DVR_GetLastError());
                    return;
                }
                strutAudioParam.read();

                // 读取解码之后PCM音频数据
                ByteBuffer bufferPcm = strutAudioParam.out_buf.getByteBuffer(0, strutAudioParam.out_frame_size);
                byte[] bytesPcm = new byte[strutAudioParam.out_frame_size];
                bufferPcm.rewind();
                bufferPcm.get(bytesPcm);

                //重置 limit 和postion 值
                bufferPcm.flip();
                //获取buffer中有效大小
                int len = bufferPcm.limit() - bufferPcm.position();
                byte[] bytes = new byte[len];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = bufferPcm.get();
                }
                StringBuilder Str = new StringBuilder();
                for (byte byteItem : bytes) {
                    Str.append(String.format("%02x ", byteItem));
                }
                log.info("收到设备端的音源数据: {}", Str);

                //这里实现的是将设备发送的pcm音频数据写入文件
                Constant.outputStreamPcm.write(bytesPcm);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }
}
