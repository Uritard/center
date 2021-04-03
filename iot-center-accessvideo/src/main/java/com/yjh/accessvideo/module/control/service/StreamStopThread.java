package com.yjh.accessvideo.module.control.service;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.utils.http.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StreamStopThread implements Runnable {

    private int streamListSize;
    private List<String> streamsJsonObjectList = new ArrayList<>();
    private String srsStopUrl = "";
    private RedisTemplate redisTemplate;

    public StreamStopThread(int streamListSize, List<String> streamsJsonObjectList, String srsStopUrl, RedisTemplate redisTemplate) {
        this.streamListSize = streamListSize;
        this.streamsJsonObjectList = streamsJsonObjectList;
        this.srsStopUrl = srsStopUrl;
        this.redisTemplate = redisTemplate;
    }

    private static final Logger log = LoggerFactory.getLogger(StreamStopThread.class);

    @Override
    public void run() {
        log.info("streamsJsonObjectList: {}", streamsJsonObjectList);
        //解析每一个stream，循环比对，找到页面传递的设备ID对应的流，并判断是否需要关闭
        for (int i = 0; i < streamListSize; i++) {
            String streambeanStr = streamsJsonObjectList.get(i);
            JSONObject streambeanJson = JSONObject.parseObject(streambeanStr);
            String publish = streambeanJson.getString("publish");
            JSONObject publishjson = JSONObject.parseObject(publish);
            Integer clients = streambeanJson.getInteger("clients"); //观看人数
            String cid = publishjson.getString("cid");
            String livePath = streambeanJson.getString("name");
            log.info("name: {}, cid：{}, clients: {}", livePath, cid, clients);

            if (StringUtils.isNotEmpty(cid) && clients<=2) {
                //踢掉
                log.info("关闭流开始");
                String delteUrl="http://"+srsStopUrl+":8082/api/v1/clients/"+cid;
                try { HttpClientUtils.httpDelete(delteUrl,null); } catch (Exception e) {e.getMessage();}
                String videoFlowId = streambeanJson.getString("id");
                redisTemplate.opsForHash().delete("cameraRealFlow:" + Constant.mapsForCamera.get(videoFlowId));
                Constant.mapsForCamera.remove(videoFlowId);
                redisTemplate.opsForHash().delete("cameraHistoryFlow:" + Constant.mapsForHistory.get(videoFlowId));
                Constant.mapsForHistory.remove(videoFlowId);
                log.info("关闭流："+delteUrl);
            } else { log.info("{}流为空或有人正在看。。。", livePath); }
            String url = "ps -ef | grep ffmpeg | grep '" + livePath + "' | grep -v 'grep'";
            try {
                Process processForId = Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
                processForId.waitFor();
                BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(), "UTF-8"));
                String lineForId = null;
                StringBuilder dataBackForId = new StringBuilder();
                while ((lineForId = readerForId.readLine()) != null) {
                    dataBackForId.append(lineForId).append('\n');
                }
                Integer processNum = Integer.parseInt(dataBackForId.substring(9, 15).replace(" ", ""));
                String urlStop = "kill -9 " + processNum;
                Runtime.getRuntime().exec(urlStop);
            } catch (Exception e) { e.getMessage(); }

        }
        log.info("Constant.mapsForCamera: {}, Constant.mapsForHistory: {}", Constant.mapsForCamera, Constant.mapsForHistory);
    }
}
