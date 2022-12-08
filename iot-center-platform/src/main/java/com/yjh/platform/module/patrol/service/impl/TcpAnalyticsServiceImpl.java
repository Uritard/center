/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.AnalyticsService;
import com.yjh.platform.netty.client.AnalysisClientHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/20
 * @since [产品/模块版本] （可选）
 */
@Component
public class TcpAnalyticsServiceImpl implements AnalyticsService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 表计算法端口
     */
    @Value("${netty.recognize.port}")
    int recognizePort;

    /**
     * 缺陷算法端口
     */
    @Value("${netty.ai.port}")
    int aiPort;

    AtomicLong algorithmMsgId = new AtomicLong(100000000L);
    AtomicLong defectMsgId = new AtomicLong(200000000L);

    private HashOperations<String, String, String> hashOperations;

    public TcpAnalyticsServiceImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    /**
     * 表计算法分析
     *
     * @param analysisList 分析参数
     * @return 返回结果
     */
    @Override
    public Result analytics(List<Analysis> analysisList) {
        Result result = new Result();
        log.info("analysisList____-----____: {}", JSON.toJSONString(analysisList));
        log.info("recognizePort-----: {}", recognizePort);
        JSONObject analysisObject = new JSONObject();
        JSONObject msgDataObject = new JSONObject();
        log.info("进入识别算法方法----------");
        long msgId = algorithmMsgId.incrementAndGet();
        analysisObject.put("msgID", String.valueOf(msgId));
        analysisObject.put("port", recognizePort);
        log.info("algorithmMsgId: {}", msgId);
        analysisObject.put("msgType", "1");
        msgDataObject.put("desNode", "serverSocket");
        msgDataObject.put("srcNode", "clientSocket001");
        try {
            int i = 1;
            for (Analysis analysis : analysisList) {
                log.info("表计IsAI：{}" , analysis.getIsAi());
                if (analysis.getIsAi() == 1) {
                    JSONObject pictureInfoObject = new JSONObject();
                    JSONObject pictureDataObject = new JSONObject();
                    pictureDataObject.put("analyseType", analysis.getAnalyseType());
                    pictureDataObject.put("imagePath", analysis.getPicPath());
                    pictureDataObject.put("modelPath", analysis.getPicModelPath());
                    // 表计算法图片存储路径
                    pictureDataObject.put("meterResultImg", redisTemplate.opsForHash().get("t_sys_param:meterResultImg", "content"));
                    pictureDataObject.put("taskId", analysis.getTaskId());
                    pictureDataObject.put("instanceId", analysis.getInstanceId().toString());
                    if ("9".equals(analysis.getAnalyseType())) {
                        pictureDataObject.put("csvPath",
                            StringUtils.replace(analysis.getCsvPath(), hashOperations.get("t_sys_param:infraredRealPath", "content"),
                                hashOperations.get("t_sys_param:infraredStorePath", "content")));
                        pictureDataObject.put("dataPath",
                            StringUtils.replace(analysis.getDataPath(), hashOperations.get("t_sys_param:infraredRealPath", "content"),
                                hashOperations.get("t_sys_param:infraredStorePath", "content")));
                        pictureDataObject.put("imagePath",
                            StringUtils.replace(analysis.getPicPath(), hashOperations.get("t_sys_param:resultImgRealPath", "content"),
                                hashOperations.get("t_sys_param:resultImgPath", "content")));
                    }
                    pictureInfoObject.put("pictureInfo" + i, pictureDataObject);
                    msgDataObject.put("data", pictureInfoObject);
                }
                i++;
            }
            analysisObject.put("msgData", msgDataObject);
            log.info("analysisObject----: {}", analysisObject);
            AnalysisClientHandler handler = AnalysisClientHandler.getAnalysisClientHandlerHashMap().get(recognizePort);
            if(handler != null) {
                handler.sendDataReguest(analysisObject);
                result.setCode(200, "SUCCESS");
            } else {
                result.setCode(400, "ERROR");
                log.error("表计识别算法未正确初始化 ！！！");
            }

            log.info("算法数据初始化-----完成");
        } catch (Exception e) {
            result.setCode(400, "ERROR");
            log.error("表计识别算法异常：", e);
        }
        return result;
    }

    /**
     * 缺陷和判别算法
     *
     * @param analysisList 分析参数
     * @return 返回结果
     */
    @Override
    public Result defect(List<Analysis> analysisList) {
        Result result = new Result();
        JSONObject analysisObject = new JSONObject();
        JSONObject msgDataObject = new JSONObject();
        long msgId = defectMsgId.incrementAndGet();
        analysisObject.put("msgID", String.valueOf(msgId));
        log.info("defectMsgId: {}", msgId);
        analysisObject.put("msgType", "1");
        msgDataObject.put("desNode", "serverSocket");
        msgDataObject.put("srcNode", "clientSocket001");
        log.info("进入缺陷算法方法----------");
        try {
            int i = 1;
            JSONObject pictureInfoObject = new JSONObject();
            for (Analysis analysis : analysisList) {

                if ("11".equals(analysis.getAnalyseType())) {
                    JSONObject normalPictureDataObject = new JSONObject();
                    normalPictureDataObject.put("analyseType", analysis.getAnalyseType());
                    normalPictureDataObject.put("modelPath", analysis.getPicModelPath());
                    normalPictureDataObject.put("taskId", analysis.getTaskId());
                    normalPictureDataObject.put("instanceId", String.valueOf(analysis.getInstanceId()));

                    String flag = hashOperations.get("t_sys_param:isEPRI", "content");
                    if (StringUtils.equals("false", flag)) {
                        // 拿提前拍好的预置位作为判别基准图
                        String imagePath = analysis.getReferenceImage();;
                        normalPictureDataObject.put("imagePath", imagePath);
                    } else {
                        // 拿电科院给的图
                        String devicePointId = analysis.getDevicePointId();
                        String filePath = hashOperations.get("t_sys_param:distinguishReferencePath", "content");
                        String imageNormalUrlPath = "";
                        File folder = new File(filePath + "/" + devicePointId);
                        if (folder.exists()) {
                            File[] listFiles = folder.listFiles();
                            if (listFiles != null) {
                                for (File direFile : listFiles) {
                                    imageNormalUrlPath = direFile.getAbsolutePath();
                                    log.info("文件名称:{}", imageNormalUrlPath);
                                }
                            }
                        }
                        normalPictureDataObject.put("imagePath", imageNormalUrlPath);
                    }
                    pictureInfoObject.put("pictureInfo" + i, normalPictureDataObject);
                    i++;
                }

                log.info("缺陷IsAI：{}", analysis.getIsAi());
                if (analysis.getIsAi() == 0 || "11".equals(analysis.getAnalyseType())) {
                    JSONObject pictureDataObject = new JSONObject();
                    pictureDataObject.put("analyseType", analysis.getAnalyseType());
                    pictureDataObject.put("imagePath", analysis.getPicPath());
                    pictureDataObject.put("modelPath", analysis.getPicModelPath());
                    pictureDataObject.put("taskId", analysis.getTaskId());
                    pictureDataObject.put("instanceId", analysis.getInstanceId().toString());
                    pictureInfoObject.put("pictureInfo" + i, pictureDataObject);
                    log.info("DATA内容: {}", pictureInfoObject);
                }
                i++;
            }
            msgDataObject.put("data", pictureInfoObject);
            analysisObject.put("msgData", msgDataObject);
            log.info("aiPort---------: {}", aiPort);

            AnalysisClientHandler handler = AnalysisClientHandler.getAnalysisClientHandlerHashMap().get(aiPort);
            if(handler != null) {
                handler.sendDataReguest(analysisObject);
                result.setCode(200, "SUCCESS");
            } else {
                result.setCode(400, "ERROR");
                log.error("缺陷识别算法未正确初始化 ！！！");
            }
            log.info("算法数据初始化-----完成");
        } catch (Exception e) {
            result.setCode(400, "ERROR");
            log.error("缺陷算法识别异常：", e);
        }

        log.info("缺陷算法结束------------{}", analysisObject);

        return result;
    }

    @Override
    public void afterPropertiesSet() {
        AbstractVideoCruise.AnalyticsFactory.registerAnalytics(CruiseConstant.AnalyticsEnum.TCP, this);
    }
}
