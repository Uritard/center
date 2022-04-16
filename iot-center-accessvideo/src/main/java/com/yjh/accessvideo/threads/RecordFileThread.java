package com.yjh.accessvideo.threads;

import com.sun.jna.NativeLong;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.utils.VideoUtil;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.RecordFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.yjh.accessvideo.commons.utils.FileUtil.deleteFile;

/**
 * @author hyh
 * @since 2022/4/11
 **/
@Slf4j
public class RecordFileThread implements Runnable{

    private RedisTemplate redisTemplate;

    private final long startTime;

    private final String fileName;
    private final String videoPath;
    private final String savePath;

    private final HCNetSDK hCNetSDK;

    private final CameraConDao cameraConDao;

    public RecordFileThread(CameraConDao cameraConDao, RedisTemplate redisTemplate, HCNetSDK hCNetSDK,
                            long startTime, String fileName,String videoPath, String savePath){
        this.cameraConDao = cameraConDao;
        this.redisTemplate = redisTemplate;
        this.hCNetSDK = hCNetSDK;
        this.startTime = startTime;
        this.fileName = fileName;
        this.videoPath = videoPath;
        this.savePath = savePath;
    }

    /**
     * 未调用停止录像接口
     */

    @Override
    public void run() {
        while (Optional.ofNullable(Constant.recordLongMap.get(fileName)).isPresent()){
            try {
                int runnedTime = Math.toIntExact((System.currentTimeMillis() - startTime) / (1000 * 60));
                int overTime = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:logoutTime", "content")));
                if (runnedTime > overTime) {
                    NativeLong lRealPlayHandle = Constant.recordLongMap.get(fileName);
                    hCNetSDK.NET_DVR_StopRealPlay(lRealPlayHandle);
                    String judge = savePath + fileName;
                    String path =  videoPath + fileName;
                    VideoUtil.h264ToMp4(path);
                    deleteFile(path);
                    RecordFileInfo recordFileInfo = new RecordFileInfo();
                    recordFileInfo.setFileName(fileName.replace(".h264", ""));
                    recordFileInfo.setEndTime(new Date());
                    recordFileInfo.setFilePath(judge.replace("h264", "mp4"));
                    recordFileInfo.setAbsoluteFilePath(path.replace("h264", "mp4"));
                    cameraConDao.updateRecordFile(recordFileInfo);
                    Constant.recordLongMap.remove(fileName);
                    break;
                }
                TimeUnit.SECONDS.sleep(20);
            }catch (InterruptedException e){
                log.info("录像监控进程报错", e);
                break;
            }
        }
    }
}
