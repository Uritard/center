package com.yjh.accessvideo.threads;

import com.alibaba.fastjson.JSON;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.utils.VideoUtil;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.RecordFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.yjh.accessvideo.commons.utils.FileUtil.deleteFile;

/**
 * @author hyh
 * @since 2022/4/14
 **/
@Slf4j
public class TranscodeThread implements Runnable {

    private final String videoPath;

    private final String savePath;

    private final String fileName;

    private final String syncWebsocketUrl;

    private final String userId;

    private final CameraConDao cameraConDao;

    public TranscodeThread(String videoPath,String savePath,String fileName,String syncWebsocketUrl, String userId, CameraConDao cameraConDao) {
        this.videoPath = videoPath;
        this.savePath = savePath;
        this.fileName = fileName;
        this.syncWebsocketUrl = syncWebsocketUrl;
        this.userId = userId;
        this.cameraConDao = cameraConDao;
    }

    @Override
    public void run() {
        try {
            Date date = new Date();
            String path = videoPath + fileName;
            VideoUtil.h264ToMp4(path);
            deleteFile(path);
            String judge = savePath + fileName.replace("h264", "mp4");
            RecordFileInfo recordFileInfo = new RecordFileInfo();
            recordFileInfo.setFileName(fileName.replace(".h264", ""));
            recordFileInfo.setEndTime(date);
            recordFileInfo.setFilePath(judge);
            recordFileInfo.setAbsoluteFilePath(path.replace("h264", "mp4"));
            cameraConDao.updateRecordFile(recordFileInfo);
            Constant.recordLongMap.remove(fileName);
            Map<String, String> jasonMap = new HashMap<>(3);
            jasonMap.put("type", "recordFilePath");
            jasonMap.put("userId", userId);
            jasonMap.put("filePath", judge);
            String json = JSON.toJSONString(jasonMap);
            postUrl(syncWebsocketUrl, json);
        }catch (Exception e){
            log.error("录像文件转码失败", e);
        }
    }

    /**
     *
     * @param url
     * @param json
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public String postUrl(String url, String json) throws IOException, URISyntaxException {
        log.info("webSocketUrl"+url);
        CloseableHttpClient client = HttpClients.createDefault();
        URI uri = new URIBuilder(url).setParameter("json", json).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }
}
