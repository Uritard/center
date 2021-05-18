package com.yjh.platform.common.tradio;

import com.sun.jna.Pointer;
import com.yjh.platform.common.Constant;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.LongBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @author tt
 * @date 2021/1/6 13:24
 */
@lombok.extern.slf4j.Slf4j
public class RecordVoiceFileTestThread implements Runnable {

    private long hdForData = 0l;;
    private volatile boolean isThreadStart = Constant.isThreadStart;
    private static TradioLibrary sdk_= TradioLibrary.INSTANCE;

    private String voicePath;
    private String voiceName;
    private Long voiceDeviceId;
    private long dateTime;
    private long dateTimeAfter;
    private Integer voiceFileRecordTime;

    public RecordVoiceFileTestThread(RedisTemplate redisTemplate, Long voiceDeviceId, long hdForData) {
        this.hdForData = hdForData;
        this.voiceDeviceId = voiceDeviceId;
        this.voiceFileRecordTime = Integer.parseInt(redisTemplate.opsForHash().entries("t_sys_param:voiceFileRecordTime").get("content").toString());
        this.dateTime = System.currentTimeMillis();
        this.dateTimeAfter = dateTime+voiceFileRecordTime*60*1000;
        this.voicePath = redisTemplate.opsForHash().entries("t_sys_param:absVoicePath").get("content").toString();
        this.voiceName = voiceDeviceId +"_"+new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTime))+"-"
                +new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTimeAfter))+"_";
    }

    @Override
    public void run() {
        try {
            while (isThreadStart){
                log.info("Thread is " + Thread.currentThread().getName() + Thread.currentThread().getId());
                if (!Constant.isThreadStart) {
                    isThreadStart = false;
                    log.info("Thread stop success!");
                }
                sdk_.NET_TRADIO_SetRtpCallback(hdForData, new TradioLibrary.PRtpCallback() {
                    @Override
                    @Async
                    public void apply(Pointer data, int len, int channel, int db, int sample_rate, long dev) {

                        byte[] sourceData = data.getByteArray(0,len);
//                        StringBuilder StrArrayTem = new StringBuilder();
//                        for (int i = 0; i < len; i++) { StrArrayTem.append(String.format("%02x ", sourceData[i])); }
//                        log.info("receiveOriginalDataArray:" + StrArrayTem);
                        String timeTem = new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime));
                        try {
                            File file = new File(voicePath+"/"+voiceDeviceId+"/"+timeTem);
                            if (!file.exists()) { file.mkdirs(); }
                            File tempWav = new File(file, voiceName +".aac");
                            if (!tempWav.exists()) try { tempWav.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
                            FileOutputStream fos = new FileOutputStream(tempWav, true);
                            fos.write(sourceData, 0, sourceData.length);
                            fos.flush();
                            fos.close();
                        } catch (Exception e) { e.getMessage(); }
                    }
                }, 0);
                Thread.sleep(10000);

                if (System.currentTimeMillis() >dateTimeAfter) {
                    String timeAfterTem = new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime));
                    String urlAACToWAV = "ffmpeg -y -i "+voicePath+"/"+voiceDeviceId+"/"+timeAfterTem+"/"+voiceName+".aac -acodec pcm_s16le -ac 2 -ar 32000 " +
                            voicePath+"/"+voiceDeviceId+"/"+timeAfterTem+"/"+voiceName + ".wav";
                    log.info("urlAACToWAV: "+urlAACToWAV);
                    try {
                        Runtime.getRuntime().exec(urlAACToWAV);
                        Thread.sleep(2000);
                        Runtime.getRuntime().exec("rm -rf "+voicePath+"/"+voiceDeviceId+"/"+timeAfterTem+"/"+voiceName+".aac");
                    } catch (IOException e) { e.getMessage(); }
                    dateTime = System.currentTimeMillis();
                    dateTimeAfter = dateTime+voiceFileRecordTime*60*1000;
                    voiceName = voiceDeviceId +"_"+new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTime))+"-"
                            +new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTimeAfter))+"_";
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            isThreadStart = false;
        }
    }
}
