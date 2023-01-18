package com.yjh.platform.common.utils.mp3;

import com.yjh.platform.common.result.BusinessException;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;
import java.io.File;

@Slf4j
public class WaveOutMp3 {
    private AudioFormat af;
    private DataLine.Info dli;
    private SourceDataLine tdl;
    AudioInputStream audioInputStream;
    private String file;

    public WaveOutMp3(String file) {
        this.file = file;
    }

    /**
     * 打开音频目标数据行。从中读取音频数据格式为：采样率32kHz，每个样本16位，单声道，有符号的，little-endian。
     *
     * @return 成功打开返回true，否则false。
     */
    public boolean open() {
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(file));
            af = audioInputStream.getFormat();
            if (af.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, af.getSampleRate(), 16, af.getChannels(), af.getChannels() * 2, af.getSampleRate(), false);
                audioInputStream = AudioSystem.getAudioInputStream(af, audioInputStream);
            }
            //dli = new DataLine.Info(SourceDataLine.class, af);
//			tdl = (SourceDataLine) AudioSystem.getLine(dli);
//			tdl.open(af, FFT.FFT_N << 1);
        } catch (Exception e) {
            log.error("错误" + e);
            
            throw new BusinessException(209, "音频文件读取异常");
        }

        return true;
    }

    public void close() {
        try {
            audioInputStream.close();
//			tdl.drain();
//			tdl.stop();
//			tdl.close();
        } catch (Exception e) {
            log.error("关闭出错了" + e);
        }

    }

    public void start() {
        log.info("是不是null " + tdl);
        //tdl.start();
    }

    public void stop() {
        tdl.stop();
    }

    public int read(byte[] b, int len) throws Exception {
        return audioInputStream.read(b, 0, len);
    }

    private double phase0 = 0;

    /**
     * 产生频率264Hz，采样率为44.1kHz，幅值为0x7fff，每个样本16位的PCM。
     *
     * @param b   接收PCM样本。
     * @param len PCM样本字节数。
     */
    public void getWave264(byte[] b, int len) {
        double dt = 2 * 3.14159265358979323846 * 264 / 44100;
        int i, pcmi;
        len >>= 1;
        for (i = 0; i < len; i++) {
            pcmi = (short) (0x7fff * Math.sin(i * dt + phase0));
            b[2 * i] = (byte) pcmi;
            b[2 * i + 1] = (byte) (pcmi >>> 8);
        }
        phase0 += i * dt;
    }

    public float getSampleRate(){
        return af.getSampleRate();
    }

    public int getChannels(){
        return af.getChannels();
    }
}