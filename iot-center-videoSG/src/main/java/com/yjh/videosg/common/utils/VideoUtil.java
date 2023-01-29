/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.videosg.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import ws.schild.jave.*;

import java.io.File;
import java.util.ArrayList;

/**
 * @author hyh
 * @since 2022/4/10
 **/
@Slf4j
public class VideoUtil {

    /**
     * h264文件转 mp4文件
     * @param filePath h264文件绝对路径
     */
    public static void h264ToMp4(String filePath) {
        if (StringUtils.isEmpty(filePath) || !filePath.endsWith("h264")) {
            return;
        }
        try {
            File file = new File(filePath);
            String mp4FilePath = filePath.replace("h264", "mp4");
            File mpFile = new File(mp4FilePath);
            if (!mpFile.exists()) {
                mpFile.createNewFile();
            }
            //获取文件多媒体类
            MultimediaObject sourceFile = new MultimediaObject(file);

            VideoAttributes video = new VideoAttributes();
            AudioAttributes audio = new AudioAttributes();
            //音频编码器
            audio.setCodec("libmp3lame");
            //位速率又叫比特率，是指在单位时间内可以传输多少数据
            audio.setBitRate(64000);
            //音频的通道数，一般来说 都是单通道和双通道（立体音）
            audio.setChannels(1);
            //是指在数码音频和视频技术应用中，当进行模拟/数码转换时，每秒钟对模拟信号进行取样时的快慢次数
            audio.setSamplingRate(22050);
            //视频编码器
            video.setCodec("libx264");
            //位速率又叫比特率，是指在单位时间内可以传输多少数据
            video.setBitRate(800000);
            //画面桢速率
            video.setFrameRate(20);
            video.setSize(new VideoSize(1920, 1080));
            EncodingAttributes attr = new EncodingAttributes();
            attr.setFormat("mp4");
            attr.setAudioAttributes(audio);
            attr.setVideoAttributes(video);
            Encoder encoder = new Encoder();
            encoder.encode(sourceFile, mpFile, attr);
        } catch (Exception e) {
            log.error("h264视频转mp4异常:", e);
        }
    }


    /**
     * 特殊字符处理
     * @param command
     * @return
     */
    public static String[] partitionCommandLine(final String command) {
        final ArrayList<String> commands = new ArrayList<>();

        int index = 0;

        StringBuffer buffer = new StringBuffer(command.length());

        boolean isApos = false;
        boolean isQuote = false;
        while (index < command.length()) {
            final char c = command.charAt(index);

            switch (c) {
                case ' ':
                    if (!isQuote && !isApos) {
                        final String arg = buffer.toString();
                        buffer = new StringBuffer(command.length() - index);
                        if (arg.length() > 0) {
                            commands.add(arg);
                        }
                    } else {
                        buffer.append(c);
                    }
                    break;
                case '\'':
                    if (!isQuote) {
                        isApos = !isApos;
                    } else {
                        buffer.append(c);
                    }
                    break;
                case '"':
                    if (!isApos) {
                        isQuote = !isQuote;
                    } else {
                        buffer.append(c);
                    }
                    break;
                default:
                    buffer.append(c);
            }

            index++;
        }

        if (buffer.length() > 0) {
            final String arg = buffer.toString();
            commands.add(arg);
        }
        return commands.toArray(new String[commands.size()]);
    }
}
