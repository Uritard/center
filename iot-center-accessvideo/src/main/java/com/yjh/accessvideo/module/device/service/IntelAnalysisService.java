package com.yjh.accessvideo.module.device.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yjh.accessvideo.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.module.device.entity.interlanalysis.*;
import com.yjh.accessvideo.netty.client.DataDealThread;
import com.yjh.accessvideo.thread.TaskExecutePool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author 丫C
 * @date 2022/4/11
 */
@Service
public class IntelAnalysisService {

    /**
     * 分析结果反馈ip地址
     */
    @Value("${analysis.result.ip}")
    private String analysisResultIp;
    /**
     * 分析结果返回端口
     */
    @Value("${analysis.result.port}")
    private String analysisResultPort;
    /**
     * 请求图像分析路径
     */
    @Value("${intelligent.analysis.url}")
    private String intelAnalysisUrl;
    /**
     * 请求算法更新路径
     */
    @Value("${update.model.url}")
    private String updateModelUrl;
    /**
     * webSocket请求地址
     */
    @Value("${system.webSocket.url}")
    private String syncWebsocketUrl;

    private Logger log = LoggerFactory.getLogger(IntelAnalysisService.class);

    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    @Autowired
    private RedisTemplate redisTemplate;

    public IntelAnalysisService(AnalyseDataOperateService analyseDataOperateService) {
        this.analyseDataOperateService = analyseDataOperateService;
    }

    /**
     * 巡视主机请求图像分析--接口
     *
     * @param picAnalyseRequest 参数
     * @return ResponseEntity<Response>
     */
    public ResponseEntity<Response> picAnalyse(PicAnalyseRequest picAnalyseRequest){
        Map<String, Object> param = new HashMap<>(16);
        param.put("requestHostIp", picAnalyseRequest.getRequestHostIp());
        param.put("requestHostPort", picAnalyseRequest.getRequestHostPort());
        param.put("requestId", picAnalyseRequest.getRequestId());

        List<Map<String, Object>> objectList = new ArrayList<>();
        picAnalyseRequest.getObjectList().forEach(analyseObject -> {
            Map<String, Object> map = new HashMap<>(16);
            map.put("objectId", analyseObject.getObjectId());
            map.put("imageNormalUrlPath", analyseObject.getImageNormalUrlPath());
            map.put("typeList", analyseObject.getTypeList());
            map.put("imageUrlList", analyseObject.getImageUrlList());
            objectList.add(map);
        });
        param.put("objectList", objectList);

        printJsonMsg(param);

        ResponseEntity<Response> response = null;
        try {
            response = restTemplatePost(intelAnalysisUrl, param);
            otherReturn(response);
        }catch (Exception e){
            log.error(e.getMessage());
        }

        return response;
    }

    /**
     * 巡视主机请求图像分析--功能
     *
     * @param analysisObject json格式的参数
     * @return ResponseEntity<Response>
     */
    public ResponseEntity<Response> picAnalyseNoDetection(JSONObject analysisObject){
        PicAnalyseRequest request = formatTransition(analysisObject);
        ResponseEntity<Response> response = picAnalyse(request);
        return response;
    }

    /**
     * 巡视主机请求算法更新--接口
     *
     * @param request 参数
     * @return ResponseEntity<Response>
     */
    public ResponseEntity<Response> algorithmUpdate(UpdateRequest request){
        Map<String, Object> param = new HashMap<>(16);
        param.put("requestHostIp", request.getRequestHostIp());
        param.put("requestHostPort", request.getRequestHostPort());
        param.put("requestId", request.getRequestId());
        param.put("algorithmPath", request.getAlgorithmPath());

        printJsonMsg(param);

        ResponseEntity<Response> response = null;
        try {
            response = restTemplatePost(updateModelUrl, param);
            otherReturn(response);
        }catch (Exception e){
            log.error(e.getMessage());
        }

        return response;
    }

    /**
     * 巡视主机请求算法更新--功能
     *
     * @param request 参数
     * @return ResponseEntity<Response>
     */
    public ResponseEntity<Response> algorithmUpdateNoDetection(UpdateRequest request){
        Map<String, Object> param = new HashMap<>(16);
        param.put("requestHostIp", analysisResultIp);
        param.put("requestHostPort", analysisResultPort);
        param.put("requestId", String.valueOf(UUID.randomUUID()));
        param.put("algorithmPath", request.getAlgorithmPath());

        ResponseEntity<Response> response = null;
        try {
            response = restTemplatePost(updateModelUrl, param);
            otherReturn(response);
        }catch (Exception e){
            log.error(e.getMessage());
        }

        return response;
    }

    /**
     *  巡视主机收到分析结果开始解析
     *
     * @param response 参数
     */
    public ResponseEntity<Response> picAnalyseRetNotify(PicAnalyseResponse response){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgID", String.valueOf(100000001));
        jsonObject.put("msgType", "2");
        JSONObject msgDataObject = new JSONObject();
        msgDataObject.put("desNode", "clientSocket001");
        msgDataObject.put("srcNode", "serverSocket");

        String taskId = response.getRequestId().split("#")[1];
        List<AnalyseResult> resultsList = response.getResultsList();

        JSONObject resultInfoObject = new JSONObject();

        int i = 1;
        // 遍历多个点的分析结果,不同的巡视点
        for (AnalyseResult analyseResult : resultsList) {
            JSONObject resultDataObject = new JSONObject();
            resultDataObject.put("taskId", taskId);
            resultDataObject.put("instanceId", analyseResult.getObjectId());

            List<AnalyseResultItem> results = analyseResult.getResults();
            // 遍历单个点的分析结果,不同的缺陷
            for (AnalyseResultItem result : results) {
                resultDataObject.put("analyseType", "11");
                resultDataObject.put("resultValue", result.getDesc());
                resultDataObject.put("analyseResultImg", result.getResImageUrl());
                resultDataObject.put("pictureCoordinate", result.getPos());

                // 这是我们解析暂时用不到的字段值
                resultDataObject.put("code", result.getCode());
                resultDataObject.put("conf", result.getConf());
                resultDataObject.put("value", result.getValue());
                i++;
            }
            resultInfoObject.put("resultInfo" + i, resultDataObject);
        }
        msgDataObject.put("data", resultInfoObject);
        jsonObject.put("msgData", msgDataObject);

        String pretty = JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        System.out.println(pretty);

        int remotePort = 13669;
        DataDealThread dataDealThread = new DataDealThread(jsonObject.toJSONString(), remotePort, redisTemplate, analyseDataOperateService, syncWebsocketUrl);
        TaskExecutePool.getInstance().execute(dataDealThread);

        return ResponseEntity.ok(Response.ok());
    }

    /**
     * Json格式转换为http需要的参数
     *
     * @param analysisObject Json格式的参数
     * @return PicAnalyseRequest
     */
    private PicAnalyseRequest formatTransition(JSONObject analysisObject) {
        log.info("analysisObject=={}", analysisObject);

        PicAnalyseRequest picAnalyseRequest = new PicAnalyseRequest();
        picAnalyseRequest.setRequestHostIp(analysisResultIp);
        picAnalyseRequest.setRequestHostPort(analysisResultPort);
        List<AnalyseObject> objectList = new ArrayList<>();

        JSONObject jsonObjectData = JSON.parseObject(JSON.parseObject(analysisObject.get("msgData").toString()).get("data").toString());
        Iterator iterator = jsonObjectData.entrySet().iterator();
        // 迭代器取出data中的每一个数据
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            // 遍历每一个结果子集
            JSONObject jsonObjectResult = JSON.parseObject(entry.getValue().toString());
            log.info("数据======{}", jsonObjectResult);
            String taskId = jsonObjectResult.get("taskId").toString();
            picAnalyseRequest.setRequestId(UUID.randomUUID() + "#" + taskId);

            AnalyseObject analyseObject = new AnalyseObject();
            analyseObject.setObjectId(jsonObjectResult.get("instanceId").toString());
            // 图像分析类型,可多选
            List<String> typeList = new ArrayList<>();
            typeList.add(jsonObjectResult.get("analyseType").toString());
            analyseObject.setTypeList(typeList);
            // 待分析图像的URL,可多选
            List<String> imageUrlList = new ArrayList<>();
            imageUrlList.add(jsonObjectResult.get("imagePath").toString());

            analyseObject.setImageUrlList(imageUrlList);
            objectList.add(analyseObject);
        }
        picAnalyseRequest.setObjectList(objectList);
        return picAnalyseRequest;
    }

    /**
     * 其他格式的输出
     *
     * @param response 请求参数
     */
    private void otherReturn(ResponseEntity<Response> response) {
        if (Objects.isNull(response.getBody())) {
            return;
        }else {
            ResponseCodeEnum instance = ResponseCodeEnum.getInstance(response.getBody().getCode());
            Map<String, Object> map = new HashMap<>(5);
            assert instance != null;
            map.put("code", instance.getCode());
            map.put("description", instance.getValue());
            log.info("返回结果:{}", map);
        }
    }

    /**
     * 请求其他服务
     *
     * @param url 请求地址
     * @param map 请求参数
     * @return String
     */
    public ResponseEntity<Response> restTemplatePost(String url, Map<String, Object> map) {
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, ResponseEntity.class);
    }

    /**
     * 测试:json格式打印输入参数
     *
     * @param param 请求参数
     */
    private void printJsonMsg(Map<String, Object> param) {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(param);

        String pretty = JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        System.out.println(pretty);

        System.out.println("====================================================================================");
    }

}
