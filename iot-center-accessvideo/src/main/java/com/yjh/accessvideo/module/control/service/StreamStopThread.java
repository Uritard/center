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
import java.util.Objects;

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
        try {
            //减少IO
            Thread.sleep(20000);
            //解析每一个stream，循环比对，找到页面传递的设备ID对应的流，并判断是否需要关闭
            for (int i = 0; i < streamListSize; i++) {
                String streambeanStr = streamsJsonObjectList.get(i);
                JSONObject streambeanJson = JSONObject.parseObject(streambeanStr);
                String videoFlowId = streambeanJson.getString("id");
                String publish = streambeanJson.getString("publish");
                JSONObject publishjson = JSONObject.parseObject(publish);
                Integer clients = streambeanJson.getInteger("clients"); //观看人数
                String cid = publishjson.getString("cid");
                String livePath = streambeanJson.getString("name");

                if (StringUtils.isNotEmpty(cid) && clients<2) {
                    //踢掉
                    String deleteUrl="http://"+srsStopUrl+":8082/api/v1/clients/"+cid;
                    log.info("关闭流-->id: {}, cid：{}, clients: {}", videoFlowId, cid, clients);
                    HttpClientUtils.httpDelete(deleteUrl,null);
                    Constant.mapsForCamera.remove(livePath);
                    Constant.mapsForHistory.remove(livePath);
                    log.info("停流成功");
                } else if (StringUtils.isEmpty(cid)) {
                    log.info("{}流为空", livePath);
                    for (String key:Constant.mapsForCamera.keySet()) {
                        if (Objects.equals(key, livePath)) {
                            log.info("{}实时匹配{}", key, livePath);
                            Constant.mapsForCamera.remove(key);
                        }
                    }
                    for (String key:Constant.mapsForHistory.keySet()) {
                        log.info(Constant.mapsForHistory.get(key)+" "+livePath);
                        if (Objects.equals(key, livePath)) {
                            log.info("{}历史匹配{}", key, livePath);
                            Constant.mapsForHistory.remove(key); }
                    }
                } else if (clients>1){ log.info("{}流有人在看", videoFlowId); }
                String url = "ps -ef | grep ffmpeg | grep '/" + livePath + "' | grep -v 'grep'";

                Process processForId = Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
                processForId.waitFor();
                BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(), "UTF-8"));
                String lineForId = null;
                StringBuilder dataBackForId = new StringBuilder();
                while ((lineForId = readerForId.readLine()) != null) {
                    dataBackForId.append(lineForId).append('\n');
                }
                log.info("流进程："+dataBackForId);
                if (dataBackForId.length()>0) {
                    Integer processNum = Integer.parseInt(dataBackForId.substring(9, 15).replace(" ", ""));
                    String urlStop = "kill -9 " + processNum;
                    Runtime.getRuntime().exec(urlStop);
                }
                if (dataBackForId.length() ==0) {
                    Constant.mapsForCamera.remove(livePath);
                    Constant.mapsForHistory.remove(livePath);
                }
            }
        } catch (Exception e) {
            Constant.mapsForCamera.clear();
            Constant.mapsForHistory.clear();
            log.info("停流异常，清空所有流。。。");
            e.getMessage();
        } finally {
            try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }
            killProcess();
        }
        log.info("Constant.mapsForCamera: {}, Constant.mapsForHistory: {}", Constant.mapsForCamera, Constant.mapsForHistory);
    }

    private void killProcess() {
        try {
            if (Constant.mapsForCamera.size()==0 && Constant.mapsForHistory.size()==0) {
                String url = "ps -ef | grep ffmpeg | grep -v 'grep'";

                Process processForId = Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
                processForId.waitFor();
                Thread.sleep(1000);
                BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(), "UTF-8"));
                String lineForId = null;
                StringBuilder dataBackForId = new StringBuilder();
                while ((lineForId = readerForId.readLine()) != null) {
                    dataBackForId.append(lineForId).append('\n');
                }
                log.info("流进程："+dataBackForId);
                if (dataBackForId.length()>0) {
                    Integer processNum = Integer.parseInt(dataBackForId.substring(9, 15).replace(" ", ""));
                    String urlStop = "kill -9 " + processNum;
                    Runtime.getRuntime().exec(urlStop);
                    killProcess();
                }
            }
        } catch (Exception e) { e.getMessage(); }
    }
}
