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

    private RedisTemplate redisTemplate;
    private Integer port;
    private Integer channelNum;
    private String voiceDeviceId;
    private String ftpUrl;
    private String owner;
    private String ownerCode;
    private String voicePath = redisTemplate.opsForHash().entries("t_sys_param:absVoicePath").get("content").toString();
    private Integer voiceFileRecordTime = Integer.parseInt(redisTemplate.opsForHash().entries("t_sys_param:voiceFileRecordTime").get("content").toString());
    private long dateTime = System.currentTimeMillis();
    private long dateTimeAfter = dateTime+voiceFileRecordTime*60*1000;
    private volatile boolean isThreadStart;
    private static TradioLibrary sdk_= TradioLibrary.INSTANCE;

    public RecordVoiceFileThread(RedisTemplate redisTemplate, Integer port, String voiceDeviceId, Integer channelNum,
                                 String ftpUrl, String owner, String ownerCode, boolean isThreadStart) {
        this.redisTemplate = redisTemplate;
        this.port = port;
        this.voiceDeviceId = voiceDeviceId;
        this.channelNum = channelNum;
        this.ftpUrl = ftpUrl;
        this.owner = owner;
        this.ownerCode = ownerCode;
        this.isThreadStart = isThreadStart;
    }

    @Override
    public void run() {
        try {
            while (isThreadStart){
                if (System.currentTimeMillis() >dateTimeAfter) {
                    dateTime = System.currentTimeMillis();
                    dateTimeAfter = dateTime+dateTime*60*1000;
                }
                String voiceName = "voice"+ voiceDeviceId +new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(dateTime))+ "-" +new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(dateTimeAfter));
                if (sdk_.NET_TRADIO_Init() == 0) {
                    log.info("SDK初始化成功");
                } else { log.info("SDK初始化失败"); }

                LongBuffer hd = LongBuffer.allocate(1);
                if(sdk_.NET_TRADIO_CreateDevice(hd) == 0){
                    log.info("创建设备成功");
                }else{ log.info("创建设备失败"); }

                final int[] byteArratTemLength = {0};
                long hdForData = hd.get();
                final int[] i= {1};
                sdk_.NET_TRADIO_SetRtpCallback(hdForData, new TradioLibrary.PRtpCallback() {
                    @Override
                    public void apply(Pointer data, int len, int channel, int db, int sample_rate, long dev) {
                        log.info("收到数据：charPtr1=" + data + "，int1=" + len + "，int2=" + channel + "，int3=" + db
                                + "，long1=" + dev + "，sample_rate=" + sample_rate);
                        i[0]=i[0]+1;

                        byte[] bytesArrayTem = new byte[len];
                        StringBuilder StrArrayTem = new StringBuilder();
                        for (int i = 0; i < len; i++) {
                            byte[] byteOne = new byte[1];
                            byteOne[0] = data.getByte(i);
                            System.arraycopy(byteOne, 0, bytesArrayTem, i, 1);
                            StrArrayTem.append(String.format("%02x ", data.getByte(i)));
                        }
                        log.info("receiveOriginalDataArray:" + StrArrayTem);

                        if (channel==channelNum-1) {
                            try {
                                File file = new File(voicePath);
                                if (!file.exists()) { file.mkdirs(); }
                                File tempWav = new File(file, voiceName +".aac");
                                if (!tempWav.exists()) try { tempWav.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
                                FileOutputStream fos = new FileOutputStream(tempWav, true);
                                fos.write(bytesArrayTem, 0, bytesArrayTem.length);
                                fos.flush();
                                fos.close();
                                byteArratTemLength[0] = byteArratTemLength[0]+len;
                            } catch (IOException e) { e.getMessage(); }
                        }
                    }
                }, 0);

                NET_TRADIO_DEVICEINFO dev = new NET_TRADIO_DEVICEINFO();
                log.info("i[0]/40*1000): "+i[0]/40*1000);
                if (sdk_.NET_TRADIO_Login(hdForData, ftpUrl, port, owner, ownerCode, dev) == 0) { log.info("注册成功"); }else { log.info("注册失败"); }
                try { Thread.sleep(i[0]/40*1000); } catch (InterruptedException e) { e.getMessage(); }
                log.info("byteArrayLength: "+byteArratTemLength[0]);

                int id = 0;
                if (sdk_.NET_TRADIO_Logout(id) != 0) { log.info("设备注销成功"); } else { log.info("设备注销失败"); }
                sdk_.NET_TRADIO_Clear();
                String urlAACToWAV = "ffmpeg -y -i "+voicePath+voiceName+".aac -acodec pcm_s16le -ac 2 -ar 32000 " + voicePath +voiceName + ".wav";
                log.info("urlAACToWAV: "+urlAACToWAV);
                try { Runtime.getRuntime().exec(urlAACToWAV); } catch (IOException e) { e.getMessage(); }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
