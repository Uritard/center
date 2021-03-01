package com.yjh.platform.common.tradio;

import com.sun.jna.Pointer;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.LongBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author tt
 * @date 2021/1/6 13:24
 */
@lombok.extern.slf4j.Slf4j
public class RecordVoiceFileThread implements Runnable {

    private Integer port;
    private String voiceDeviceId;
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

    public RecordVoiceFileThread(RedisTemplate redisTemplate, Integer port, String voiceDeviceId, String channelNum,
                                 String ftpUrl, String owner, String ownerCode, boolean isThreadStart) {
        this.port = port;
        this.voiceDeviceId = voiceDeviceId;
        this.ftpUrl = ftpUrl;
        this.owner = owner;
        this.ownerCode = ownerCode;
        this.isThreadStart = isThreadStart;
        voicePath = redisTemplate.opsForHash().entries("t_sys_param:absVoicePath").get("content").toString();
        voiceFileRecordTime = Integer.parseInt(redisTemplate.opsForHash().entries("t_sys_param:voiceFileRecordTime").get("content").toString());
        dateTime = System.currentTimeMillis();
        dateTimeAfter = dateTime+voiceFileRecordTime*60*1000;
        channelNumList = channelNum.split(",");
        voiceName = voiceDeviceId +"T"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(dateTime))+"T"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(dateTimeAfter))+"T";
    }

    @Override
    public void run() {
        try {
            while (isThreadStart){
                if (System.currentTimeMillis() >dateTimeAfter) {
                    String urlAACToWAV1 = "ffmpeg -y -i "+voicePath+voiceDeviceId+"/1/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime))+"/"+voiceName+"01.aac -acodec pcm_s16le -ac 2 -ar 32000 " +
                            voicePath+voiceDeviceId+"/1/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime))+"/"+voiceName + "01.wav";
                    String urlAACToWAV2 = "ffmpeg -y -i "+voicePath+voiceDeviceId+"/2/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime))+"/"+voiceName+"02.aac -acodec pcm_s16le -ac 2 -ar 32000 " +
                            voicePath+voiceDeviceId+"/2/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime))+"/"+voiceName + "02.wav";
                    log.info("urlAACToWAV: "+urlAACToWAV1);
                    try {
                        Runtime.getRuntime().exec(urlAACToWAV1);
                        Runtime.getRuntime().exec(urlAACToWAV2);
                        Thread.sleep(2000);
                        Runtime.getRuntime().exec("rm -rf "+voicePath+voiceDeviceId+"/1/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime))+"/"+voiceName+"01.aac");
                        Runtime.getRuntime().exec("rm -rf "+voicePath+voiceDeviceId+"/2/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime))+"/"+voiceName+"02.aac");
                    } catch (IOException e) { e.getMessage(); }
                    dateTime = System.currentTimeMillis();
                    dateTimeAfter = dateTime+voiceFileRecordTime*60*1000;
                    voiceName = voiceDeviceId +"T"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(dateTime))+"T"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(dateTimeAfter))+"T";
                }
                if (sdk_.NET_TRADIO_Init() == 0) {
                    log.info("SDK初始化成功");
                } else { log.info("SDK初始化失败"); }

                LongBuffer hd = LongBuffer.allocate(1);
                if(sdk_.NET_TRADIO_CreateDevice(hd) == 0){
                    log.info("创建设备成功");
                }else{ log.info("创建设备失败"); }

                long hdForData = hd.get();
                sdk_.NET_TRADIO_SetRtpCallback(hdForData, new TradioLibrary.PRtpCallback() {
                    @Override
                    public void apply(Pointer data, int len, int channel, int db, int sample_rate, long dev) {
                        log.info("收到数据：charPtr1=" + data + "，int1=" + len + "，int2=" + channel + "，int3=" + db
                                + "，long1=" + dev + "，sample_rate=" + sample_rate);

                        byte[] bytesArrayTem = new byte[len];
                        StringBuilder StrArrayTem = new StringBuilder();
                        for (int i = 0; i < len; i++) {
                            byte[] byteOne = new byte[1];
                            byteOne[0] = data.getByte(i);
                            System.arraycopy(byteOne, 0, bytesArrayTem, i, 1);
                            StrArrayTem.append(String.format("%02x ", data.getByte(i)));
                        }
                        log.debug("receiveOriginalDataArray:" + StrArrayTem);
                        for (String channelNumTem:channelNumList) {
                            Integer channelNumTerm = Integer.parseInt(channelNumTem)-1;
                            if (channel == channelNumTerm) {
                                try {
                                    File file = new File(voicePath+voiceDeviceId+"/"+channelNumTem+"/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime)));
                                    if (!file.exists()) { file.mkdirs(); }
                                    File tempWav = new File(file, voiceName + "0" + Integer.parseInt(channelNumTem) +".aac");
                                    if (!tempWav.exists()) try { tempWav.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
                                    FileOutputStream fos = new FileOutputStream(tempWav, true);
                                    fos.write(bytesArrayTem, 0, bytesArrayTem.length);
                                    fos.flush();
                                    fos.close();
                                } catch (IOException e) { e.getMessage(); }
                            }
                        }
                    }
                }, 0);

                NET_TRADIO_DEVICEINFO dev = new NET_TRADIO_DEVICEINFO();
                if (sdk_.NET_TRADIO_Login(hdForData, ftpUrl, port, owner, ownerCode, dev) == 0) { log.info("注册成功"); }else { log.info("注册失败"); }
                try { Thread.sleep(6000); } catch (InterruptedException e) { e.getMessage(); }
                if (sdk_.NET_TRADIO_Logout(0) != 0) { log.info("设备注销成功"); } else { log.info("设备注销失败"); }
                sdk_.NET_TRADIO_Clear();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
