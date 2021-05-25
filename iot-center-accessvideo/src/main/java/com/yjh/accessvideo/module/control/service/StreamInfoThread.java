package com.yjh.accessvideo.module.control.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.utils.http.HttpClientUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StreamInfoThread implements Runnable {


    private String srsStopUrl = "";
    private int livePath;
    private Long cameraId;
    private Map<String, Object> returnMap = new HashMap<>();
    private RedisTemplate redisTemplate;

    public StreamInfoThread(String srsStopUrl, int livePath, Long cameraId, Map<String, Object> returnMap, RedisTemplate redisTemplate) {
        this.srsStopUrl = srsStopUrl;
        this.livePath = livePath;
        this.cameraId = cameraId;
        this.returnMap = returnMap;
        this.redisTemplate = redisTemplate;
    }

    private static final Logger log = LoggerFactory.getLogger(StreamInfoThread.class);

    @Override
    public void run() {
        String getInfoUrl="http://"+srsStopUrl+":8082/api/v1/streams/";
        JSONObject jsonList = new JSONObject();
        try {
            Thread.sleep(4000);
            jsonList = HttpClientUtils.sendGet(getInfoUrl, null);
        } catch (Exception e) {e.getMessage();}
        assert jsonList != null;
        List<String> streamsJsonObjectList = JSONArray.parseArray(jsonList.getString("streams"),String.class);
        int streamListSize = streamsJsonObjectList.size();
        for (int i = 0; i < streamListSize; i++) {
            String streambeanStr = streamsJsonObjectList.get(i);
            JSONObject streambeanJson = JSONObject.parseObject(streambeanStr);
            //livePath-cameraId
            String name = streambeanJson.getString("name");
            String videoFlowId = streambeanJson.getString("id");
            String publish = streambeanJson.getString("publish");
            JSONObject publishJson = JSONObject.parseObject(publish);
            if (Objects.equals(name, String.valueOf(livePath)) && StringUtils.isNotEmpty(publishJson.getString("cid"))) {
                returnMap.put("videoFlowId", videoFlowId);
                Constant.mapsForCamera.put(String.valueOf(cameraId), videoFlowId);
                log.info("realReturnMap: " + returnMap);
                redisTemplate.opsForHash().putAll("cameraRealFlow:" + cameraId, returnMap);
            }
        }
        log.info("realMapsForCamera: " + Constant.mapsForCamera);
    }
}
