package com.yjh.platform.common.tradio;

import com.sun.jna.Pointer;
import com.yjh.platform.common.Constant;
import com.yjh.platform.module.device.entity.TVoiceDevice;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfo;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.device.entity.VoiceDeviceInfoDetail;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import org.springframework.data.redis.core.RedisTemplate;

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
public class RecordVoiceFileThread implements Runnable {

    private Integer port;
    private Long voiceDeviceId;
    private String ftpUrl;
    private String owner;
    private String ownerCode;
    private String voicePath;
    private Integer voiceFileRecordTime;
    private long dateTime;
    private long dateTimeAfter;
    private String[] channelNumList;
    private volatile boolean isThreadStart;
    private String voiceName;
    private static TradioLibrary sdk_= TradioLibrary.INSTANCE;
    private TVoiceDeviceService tVoiceDeviceService;

    public RecordVoiceFileThread(RedisTemplate redisTemplate, Integer port, Long voiceDeviceId, String channelNum,
                                 String ftpUrl, String owner, String ownerCode, boolean isThreadStart, TVoiceDeviceService tVoiceDeviceService) {
        this.port = port;
        this.voiceDeviceId = voiceDeviceId;
        this.ftpUrl = ftpUrl;
        this.owner = owner;
        this.ownerCode = ownerCode;
        this.isThreadStart = isThreadStart;
        this.voicePath = redisTemplate.opsForHash().entries("t_sys_param:absVoicePath").get("content").toString();
        this.voiceFileRecordTime = Integer.parseInt(redisTemplate.opsForHash().entries("t_sys_param:voiceFileRecordTime").get("content").toString());
        this.dateTime = System.currentTimeMillis();
        this.dateTimeAfter = dateTime+voiceFileRecordTime*60*1000;
        this.channelNumList = channelNum.split(",");
        this.voiceName = voiceDeviceId +"_"+new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTime))+"-"
                +new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTimeAfter))+"_";
        this.tVoiceDeviceService=tVoiceDeviceService;
    }

    @Override
    public void run() {
        try {
            long hdForData = 0l;
            if (Objects.isNull(Constant.voiceMap.get("voiceDeviceId"))) {
                if (sdk_.NET_TRADIO_Init() != 0) {
                    log.info("SDK初始化失败");
                    isThreadStart = false;
                }

                LongBuffer hd = LongBuffer.allocate(1);
                if(sdk_.NET_TRADIO_CreateDevice(hd) != 0) {
                    log.info("创建设备失败");
                    isThreadStart = false;
                }
                hdForData = hd.get();

                NET_TRADIO_DEVICEINFO dev = new NET_TRADIO_DEVICEINFO();
                if (sdk_.NET_TRADIO_Login(hdForData, ftpUrl, port, owner, ownerCode, dev) != 0) {
                    log.info("注册失败");
                    isThreadStart = false;
                    VoiceDeviceAllInfoDetail tVoiceDevice = tVoiceDeviceService.selectByPrimaryId(voiceDeviceId);
                    tVoiceDevice.setState("离线");
                    tVoiceDeviceService.update(tVoiceDevice);
                } else {
                    Constant.voiceMap.put("voiceDeviceId", 0);
                    VoiceDeviceAllInfoDetail tVoiceDevice = tVoiceDeviceService.selectByPrimaryId(voiceDeviceId);
                    tVoiceDevice.setState("在线");
                    tVoiceDeviceService.update(tVoiceDevice);
                }
            }

            while (isThreadStart){

                sdk_.NET_TRADIO_SetRtpCallback(hdForData, new TradioLibrary.PRtpCallback() {
                    @Override
                    public void apply(Pointer data, int len, int channel, int db, int sample_rate, long dev) {

                        byte[] sourceData = data.getByteArray(0,len);
//                        StringBuilder StrArrayTem = new StringBuilder();
//                        for (int i = 0; i < len; i++) { StrArrayTem.append(String.format("%02x ", sourceData[i])); }
//                        log.info("receiveOriginalDataArray:" + StrArrayTem);
                        String timeTem = new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime));
                        for (String channelNumTem:channelNumList) {
                            Integer channelNumTerm = Integer.parseInt(channelNumTem)-1;
                            if (channel == channelNumTerm) {
                                try {
                                    File file = new File(voicePath+"/"+voiceDeviceId+"/"+channelNumTem+"/"+timeTem);
                                    if (!file.exists()) { file.mkdirs(); }
                                    File tempWav = new File(file, voiceName + "0" + Integer.parseInt(channelNumTem) +".aac");
                                    if (!tempWav.exists()) try { tempWav.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
                                    FileOutputStream fos = new FileOutputStream(tempWav, true);
                                    fos.write(sourceData, 0, sourceData.length);
                                    fos.flush();
                                    fos.close();
                                } catch (Exception e) { e.getMessage(); }
                            }
                        }
                    }
                }, 0);

                if (System.currentTimeMillis() >dateTimeAfter+1000) {
                    String timeAfterTem = new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime));
                    String urlAACToWAV1 = "ffmpeg -y -i "+voicePath+"/"+voiceDeviceId+"/1/"+timeAfterTem+"/"+voiceName+"01.aac -acodec pcm_s16le -ac 2 -ar 32000 " +
                            voicePath+"/"+voiceDeviceId+"/1/"+timeAfterTem+"/"+voiceName + "01.wav";
//                    String urlAACToWAV2 = "ffmpeg -y -i "+voicePath+"/"+voiceDeviceId+"/2/"+timeAfterTem+"/"+voiceName+"02.aac -acodec pcm_s16le -ac 2 -ar 32000 " +
//                            voicePath+"/"+voiceDeviceId+"/2/"+timeAfterTem+"/"+voiceName + "02.wav";
                    log.info("urlAACToWAV: "+urlAACToWAV1);
                    try {
                        Runtime.getRuntime().exec(urlAACToWAV1);
//                        Runtime.getRuntime().exec(urlAACToWAV2);
                        Thread.sleep(2000);
                        Runtime.getRuntime().exec("rm -rf "+voicePath+"/"+voiceDeviceId+"/1/"+timeAfterTem+"/"+voiceName+"01.aac");
//                        Runtime.getRuntime().exec("rm -rf "+voicePath+"/"+voiceDeviceId+"/2/"+timeAfterTem+"/"+voiceName+"02.aac");
                    } catch (IOException e) { e.getMessage(); }
                    dateTime = System.currentTimeMillis();
                    dateTimeAfter = dateTime+voiceFileRecordTime*60*1000;
                    voiceName = voiceDeviceId +"_"+new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTime))+"-"
                            +new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date(dateTimeAfter))+"_";
                }

//                try {
//                    log.info("INThreadId; "+ Thread.currentThread().getId()+", isThreadStart: "+isThreadStart);
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) { e.getMessage(); }
//                sdk_.NET_TRADIO_Clear();
//                if (sdk_.NET_TRADIO_Logout(0) == 0) { log.info("设备注销失败"); }

//                try {
//                    isThreadStart = Constant.voiceDeviceState.get(ftpUrl);
//                } catch (Exception e) { e.getMessage(); }
//                sdk_.NET_TRADIO_Clear();
//                if (sdk_.NET_TRADIO_Logout(0) == 0) { log.info("设备注销失败"); }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            isThreadStart = false;
        }
    }
}
