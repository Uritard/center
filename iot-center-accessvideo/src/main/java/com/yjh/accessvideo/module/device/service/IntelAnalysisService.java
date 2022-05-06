package com.yjh.accessvideo.module.device.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yjh.accessvideo.common.mqtt.AlarmService;
import com.yjh.accessvideo.common.mqtt.GetSpringUtil;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.Defect;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.common.utils.FtpsUtil;
import com.yjh.accessvideo.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessvideo.commons.utils.FileUtil;
import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.configuration.IntelAnalysisFtpsConfig;
import com.yjh.accessvideo.configuration.IntelligentAlgorithmConfig;
import com.yjh.accessvideo.configuration.UpFtpsConfig;
import com.yjh.accessvideo.module.device.dao.AnalyseDataOperateDao;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.module.device.entity.TAlgorithmInfo;
import com.yjh.accessvideo.module.device.entity.TWarnInfo;
import com.yjh.accessvideo.module.device.entity.XMLBaseModel;
import com.yjh.accessvideo.module.device.entity.interlanalysis.*;
import com.yjh.accessvideo.netty.client.DataDealThread;
import com.yjh.accessvideo.service.ftpsservice;
import com.yjh.accessvideo.thread.TaskExecutePool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 丫C
 * @date 2022/4/11
 */
@Service
public class IntelAnalysisService {
    /**
     * webSocket请求地址
     */
    @Value("${system.webSocket.url}")
    private String syncWebsocketUrl;
    /**
     * 27大类假数据读取来源
     */
    @Value("${conf.file.name}")
    private String fileName;
    /**
     * 缺陷分析端口
     */
    @Value("${netty.ai.port}")
    private int aiPort;
    /**
     * 表计识别分析端口
     */
    @Value("${netty.recognize.port}")
    private int recognizePort;

    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AnalyseDataOperateDao analyseDataOperateDao;
    @Autowired
    private IntelAnalysisFtpsConfig intelAnalysisFtpsConfig;
    @Autowired
    private UpFtpsConfig upFtpsConfig;
    @Autowired
    private IntelligentAlgorithmConfig algorithmConfig;

    private final Logger log = LoggerFactory.getLogger(IntelAnalysisService.class);

    public IntelAnalysisService(AnalyseDataOperateService analyseDataOperateService) {
        this.analyseDataOperateService = analyseDataOperateService;
    }

    /**
     * 巡视主机请求图像分析--接口
     *
     * @param picAnalyseRequest 参数
     * @return Response
     */
    public Response picAnalyse(PicAnalyseRequest picAnalyseRequest){
        Map<String, Object> param = new HashMap<>(16);
        param.put("requestHostIp", picAnalyseRequest.getRequestHostIp());
        param.put("requestHostPort", picAnalyseRequest.getRequestHostPort());
        param.put("requestId", picAnalyseRequest.getRequestId());

        List<Map<String, Object>> objectList = new ArrayList<>();
        picAnalyseRequest.getObjectList().forEach(analyseObject -> {
            Map<String, Object> map = new HashMap<>(16);
            map.put("objectId", Optional.ofNullable(analyseObject.getObjectId()).orElse(""));
            map.put("imageNormalUrlPath", Optional.ofNullable(analyseObject.getImageNormalUrlPath()).orElse(""));
            map.put("typeList", analyseObject.getTypeList());
            map.put("imageUrlList", analyseObject.getImageUrlList());
            objectList.add(map);
        });
        param.put("objectList", objectList);

        JSONObject testJson = (JSONObject) JSONObject.toJSON(param);
        printJsonMsg(testJson);

        try {
            String result = postUrlParams(algorithmConfig.getAnalysisUrl(), testJson.toJSONString());
            log.info("result==={}", result);

            if (StringUtils.isEmpty(result)){
                return Response.basRequest();
            }
            JSONObject jsonObject = JSON.parseObject(result);
            int code = Integer.parseInt(String.valueOf(jsonObject.get("code")));
            return new Response(code);
        }catch (Exception e){
            log.error(e.getMessage());
            return Response.serverError();
        }
    }

    /**
     * 巡视主机请求图像分析--功能
     *
     * @param analysisList 需要分析的对象
     * @return Response
     */
    public List<Response> picAnalyseNoDetection(List<Analysis> analysisList){
        List<Response> responseList = new ArrayList<>();
        List<PicAnalyseRequest> list = formatTransition(analysisList);
        for (PicAnalyseRequest request : list) {
            Response response = picAnalyse(request);
            responseList.add(response);
        }
        return responseList;
    }

    /**
     * 格式转换为http需要的参数
     *
     * @param analysisList 需要分析的对象
     * @return PicAnalyseRequest
     */
    private List<PicAnalyseRequest> formatTransition(List<Analysis> analysisList) {
        List<PicAnalyseRequest> list = new ArrayList<>();
        for (Analysis analysis: analysisList) {
            // 如果是静默监视识别 针对越线闯入和火焰烟雾再加两条请求
            if (Objects.equals("12", analysis.getAnalyseType())) {
                AnalyseObject analyseObject = new AnalyseObject();
                List<String> crossLineTypeList = new ArrayList<>();
                crossLineTypeList.add("yxcr");
                analyseObject.setTypeList(crossLineTypeList);
                list.add(packagePicAnalyseRequest(analysis, analyseObject));

                AnalyseObject analyseObject2 = new AnalyseObject();
                List<String> fireSmokeTypeList = new ArrayList<>();
                fireSmokeTypeList.add("fire");
                fireSmokeTypeList.add("smoke");
                analyseObject2.setTypeList(fireSmokeTypeList);
                list.add(packagePicAnalyseRequest(analysis, analyseObject2));
            }else {
                AnalyseObject analyseObject = setAnalyseObject(analysis);
                list.add(packagePicAnalyseRequest(analysis, analyseObject));
            }
        }
        return list;
    }

    /**
     * 添加算法识别类型和判别图片信息
     *
     * @param analysis 算法信息
     * @return AnalyseObject
     */
    private AnalyseObject setAnalyseObject(Analysis analysis) {
        AnalyseObject analyseObject = new AnalyseObject();
        List<String> typeList = new ArrayList<>();
        try {
            String analyseType = analysis.getAnalyseType();
            switch (analyseType){
                // 判别算法 需要判别基准图
                case "11":
                    // 判别基准图
                    String imageNormalUrlPath = analyseDataOperateDao.selectPresetImgByCruise(analysis.getInstanceId());
                    imageNormalUrlPath = imageNormalUrlPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:presetRealImgPath","content")),
                            String.valueOf(redisTemplate.opsForHash().get("t_sys_param:presetImgPath","content")));
                    String[] split = analysis.getPicPath().split("/");
                    String targetNamePath = split[split.length - 2] + "/" + split[split.length - 1];
                    uploadFileToFtps(imageNormalUrlPath, "/" + targetNamePath, intelAnalysisFtpsConfig);

                    analyseObject.setImageNormalUrlPath(targetNamePath);
                    String[] distinguishType = algorithmConfig.getDistinguishType().split(",");
                    Collections.addAll(typeList, distinguishType);
                    break;
                // 缺陷识别
                case "398":
                    String[] defectType = algorithmConfig.getDefectType().split(",");
                    Collections.addAll(typeList, defectType);
                    break;
                // 设备状态识别
                case "1":
                case "2":
                case "3":
                    typeList.add("meter");
                    break;
                case "5":
                    typeList.add("light");
                    break;
                case "6":
                    typeList.add("isolator");
                    break;
                case "8":
                    typeList.add("qrcode");
                    break;
                case "9":
                    typeList.add("infrared");
                    break;
                case "10":
                    typeList.add("switch");
                    break;
                case "13":
                    typeList.add("sound");
                    break;
                // 静默监视告警识别
                case "12":
                    String[] alarmType = algorithmConfig.getSilentMonitorType().split(",");
                    Collections.addAll(typeList, alarmType);
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        analyseObject.setTypeList(typeList);
        return analyseObject;
    }

    /**
     * 组装要发给智能分析主机图像分析接口的请求参数
     *
     * @param analysis 算法信息
     * @param analyseObject 点位具体信息
     * @return PicAnalyseRequest
     */
    private PicAnalyseRequest packagePicAnalyseRequest(Analysis analysis, AnalyseObject analyseObject){
        PicAnalyseRequest picAnalyseRequest = new PicAnalyseRequest();
        List<AnalyseObject> objectList = new ArrayList<>();
        List<String> imageUrlList = new ArrayList<>();
        try {
            picAnalyseRequest.setRequestHostIp(algorithmConfig.getResultIp());
            picAnalyseRequest.setRequestHostPort(algorithmConfig.getResultPort());
            picAnalyseRequest.setRequestId(UUID.randomUUID() + "#" + analysis.getTaskId());
            String instanceId = String.valueOf(analysis.getInstanceId());
            analyseObject.setObjectId(instanceId);

            String[] split = analysis.getPicPath().split("/");
            String targetNamePath =  split[split.length - 2] + "/" + split[split.length - 1];
            uploadFileToFtps(analysis.getPicPath(), "/" + targetNamePath, intelAnalysisFtpsConfig);

            imageUrlList.add(targetNamePath);
            analyseObject.setImageUrlList(imageUrlList);

            objectList.add(analyseObject);
            picAnalyseRequest.setObjectList(objectList);
        }catch (Exception e){
            e.printStackTrace();
        }
        return picAnalyseRequest;
    }

    /**
     * 巡视主机请求算法更新--接口
     *
     * @param request 参数
     * @return Response
     */
    public Response algorithmUpdate(UpdateRequest request){
        Map<String, Object> param = new HashMap<>(16);
        param.put("requestHostIp", request.getRequestHostIp());
        param.put("requestHostPort", request.getRequestHostPort());
        param.put("requestId", request.getRequestId());

        String[] split = request.getAlgorithmPath().split("/");
        String targetNamePath =  split[split.length - 2] + "/" + split[split.length - 1];
        uploadFileToFtps(request.getAlgorithmPath(), "/" + targetNamePath, intelAnalysisFtpsConfig);

        param.put("algorithmPath", targetNamePath);

        JSONObject testJson = (JSONObject) JSONObject.toJSON(param);
        printJsonMsg(testJson);

        int code;
        try {
            String result = postUrlParams(algorithmConfig.getAnalysisUrl(), testJson.toJSONString());
            log.info("result==={}", result);

            if (StringUtils.isEmpty(result)){
                return Response.basRequest();
            }
            JSONObject jsonObject = JSON.parseObject(result);
            code = Integer.parseInt(String.valueOf(jsonObject.get("code")));
        }catch (Exception e){
            log.error(e.getMessage());
            return Response.serverError();
        }
        return new Response(code);
    }


    /**
     *  巡视主机收到分析结果开始解析
     *
     * @param response 参数
     */
    @Async
    public void picAnalyseRetNotify(PicAnalyseResponse response){
        String flagId = response.getRequestId().split("#")[1];
        // 静默监视结果
        if (Objects.equals("jm", flagId)){
            silentMonitorHandle(response);
            return;
        }
        // 普通图像分析
        sendToDataDealThread(response, flagId);
    }

    /**
     *  静默监视产生告警处理
     *
     * @param response 参数
     */
    private void silentMonitorHandle(PicAnalyseResponse response) {
        // 遍历多个点的分析结果
        for (AnalyseResult  analyseResult : response.getResultsList()){
            StringJoiner content = new StringJoiner(" ");
            StringJoiner resultImg = new StringJoiner(" ");

            List<AnalyseResultItem> results = analyseResult.getResults();
            if (results.isEmpty()){
                // 没有识别出来任何缺陷 results为空
                log.info("没有识别出来任何缺陷 results为空");
                continue;
            }
            Map<String, Object> map = analyseDataOperateDao.selectInstanceInfo(Long.valueOf(analyseResult.getObjectId()));
            Boolean isHave = false;
            for (AnalyseResultItem result : results) {
                String code = Optional.ofNullable(result.getCode()).orElse("");
                String value = Optional.ofNullable(result.getValue()).orElse("");
                String type = Optional.ofNullable(result.getType()).orElse("");
                String desc = Optional.ofNullable(result.getDesc()).orElse("");

                if (!StringUtils.equals("2000", code)) {
                    log.error("巡视点为{}的图像数据错误", map.get("instance_id"));
                    continue;
                }
                if (StringUtils.equals("0", value)) {
                    log.info("巡视点为{}的图像无问题", map.get("instance_id"));
                    continue;
                }
                content.add(desc);

                String targetPath = copyFileFromFtps(type, result.getResImageUrl());
                String defectResultRealImg = targetPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content")),
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg","content")));
                resultImg.add(defectResultRealImg);
                if ("1".equals(value)){
                    isHave = true;
                }
            }

            List<String> resultImgList = new ArrayList<>();
            Collections.addAll(resultImgList, String.valueOf(resultImg).split(" "));
            resultImgList = resultImgList.stream().distinct().collect(Collectors.toList());
            if (StringUtils.isNotBlank(resultImgList.get(0))){
                TWarnInfo tWarnInfo = new TWarnInfo()
                        // 将静默监视产生的告级别警统一设置为预警
                        .setWarnLevel(130)
                        .setWarnTime(new Date())
                        .setWarnName("静默监视告警数据")
                        .setWarnContent(String.valueOf(content))
                        .setDeviceId(Long.valueOf(String.valueOf(map.get("device_id"))))
                        .setCunstomId(String.valueOf(map.get("custom_id")))
                        .setInstanceId(Long.valueOf(String.valueOf(map.get("instance_id"))))
                        .setStdMeteId(Long.valueOf(String.valueOf(map.get("device_mete_id"))))
                        .setConfMode(276)
                        .setDefectModel(450)
                        .setAlarmSource(689)
                        .setImagePath(resultImgList.get(0));
                analyseDataOperateDao.insertWarnInfo(tWarnInfo);

                // 静默监视告警向上级系统上报
                alarmToUpSystem(map, tWarnInfo);

                //webSocket通知前端调用查询告警弹框的接口
                Long warnId = analyseDataOperateDao.selectCurrentWarn();
                Map<String, Object> jasonMaps = new HashMap<>(16);
                jasonMaps.put("type", "alarmPopUp");
                jasonMaps.put("warnId", warnId);
                jasonMaps.put("defectModel", 450);
                String json = JSON.toJSONString(jasonMaps);
                log.info("发送给前端的消息：{}", json);
                try {
                    postUrl(syncWebsocketUrl, json);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            alarmToSFZJ(isHave,String.valueOf(map.get("custom_id")),results);
        }
    }

    private void alarmToSFZJ(Boolean isHave,String instanceId,List<AnalyseResultItem> results
                             ){
        try{
            ftpsservice ftpsservice= GetSpringUtil.getBean("ftpsservice");
            String flag= ftpsservice.getFlag();
            if("1".equals(flag) && isHave) {
                log.info("defect类型:开始向算法管理平台发送图片和mqtt消息");
                Alarm alarmDetail = new Alarm();
                String year = Integer.toString(LocalDate.now().getYear());
                String month = Integer.toString(LocalDate.now().getMonthValue());

                // 获取原始图路径 resultImgRealPath
                String origpicpath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath","content"))+instanceId+".jpg";
                String[] str2 = origpicpath.split("/");
                String origpcimagename = str2[str2.length - 1];
                //拼接算法管理平台原始图片推送地址
                String remoteorigfilepath = ftpsservice.getFtpsRemotePath() + "/" + "缺陷" + "/" + year + "/" + month + "/" + origpcimagename;
                log.info("开始向算法管理平台发送图片和mqtt消息");
                Iterator it = results.iterator();
                HashMap<String, String> nameMap = analyseDataOperateService.selectDeviceNameInfo(Long.valueOf(instanceId));
                List<Defect> defectList1 = new ArrayList<>();
                String targetPath= "";
                for (AnalyseResultItem result : results) {
                    String code = Optional.ofNullable(result.getCode()).orElse("");
                    String value = Optional.ofNullable(result.getValue()).orElse("");
                    String type = Optional.ofNullable(result.getType()).orElse("");
                    String desc = Optional.ofNullable(result.getDesc()).orElse("");
                    String conf = String.valueOf(result.getConf());
                    targetPath = copyFileFromFtps(type, result.getResImageUrl());
                    if("1".equals(value)){
                        Defect defect = new Defect();
                        defect.setX1(String.valueOf(result.getPos().get(0).getAreas().get(0).getX()));
                        defect.setY1(String.valueOf(result.getPos().get(0).getAreas().get(0).getY()));
                        defect.setX2(String.valueOf(result.getPos().get(0).getAreas().get(1).getX()));
                        defect.setY2(String.valueOf(result.getPos().get(0).getAreas().get(1).getY()));
                        defect.setType(type);
                        defect.setDesc(desc);
                        defect.setConfidence(conf);
                        defectList1.add(defect);
                    }

                }

                //,先取出算法平台返回的resultinfo中的结果图片路径
                String resultImagebak = targetPath; //分析结果过
                String[] str = resultImagebak.split("/");
                String imagename = str[str.length - 1];
                //拼接算法管理平台分析告警结果图片地址
                String remotefilepath = ftpsservice.getFtpsRemotePath() + "/" + "缺陷" + "/" + year + "/" + month + "/" + imagename;


                alarmDetail.setDefect(defectList1);
                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                alarmDetail.setDevice_name(nameMap.get("deviceName"));  //需要修改位devicename
                alarmDetail.setPoint_name(nameMap.get("meteName"));
                alarmDetail.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                alarmDetail.setPic_raw(remoteorigfilepath);         //图片原图
                alarmDetail.setPic_diff_base("");               //判别基准图路径
                alarmDetail.setPic_different("");               //判别告警图路径,即分析结果图
                alarmDetail.setPic_defect(remotefilepath);      //缺陷告警图路径//
                ftpsservice.uploadFile("遥信告警", origpicpath, remoteorigfilepath);
                ftpsservice.uploadFile("遥信告警", resultImagebak, remotefilepath);
                if (!ftpsservice.fileExits(remoteorigfilepath)) {
                    ftpsservice.uploadFile("遥信告警", origpicpath, remoteorigfilepath);  //原始图片上传
                }
                if (!ftpsservice.fileExits(remotefilepath)) {
                    ftpsservice.uploadFile("遥信告警", resultImagebak, remotefilepath);
                }

                log.info("巡视主机与智能分析主机：origpicpath:{}", origpicpath);
                log.info("巡视主机与智能分析主机：remoteorigfilepath:{}", remoteorigfilepath);
                log.info("巡视主机与智能分析主机：resultImagebak:{}", resultImagebak);
                log.info("巡视主机与智能分析主机：remotefilepath:{}", remotefilepath);
//                       ftp文件上传测试数据
//                      String localpath="D://信息化工作.jpg";
//                      ftpsservice.uploadFile("遥信告警",localpath,remotefilepath);
                AlarmService alarmService = GetSpringUtil.getBean("alarmService");
                alarmService.PushMsg(alarmDetail);
                log.info("发送算法管理平台结束");
            }
        }catch (Exception e){
            log.warn("发送给算法主机错误");
        }
    }

    /**
     * 静默监视告警向上级系统上报
     *
     * @param map 巡视点信息
     * @param tWarnInfo 告警数据
     * @return void
     */
    private void alarmToUpSystem(Map<String, Object> map, TWarnInfo tWarnInfo) {
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> xmlItem = new HashMap<>();
        try {
            xmlBaseModel.setType("63");
            xmlItem.put("patroldevice_code", map.get("device_id"));
            xmlItem.put("patroldevice_name", map.get("device_name"));
            switch (tWarnInfo.getWarnLevel()){
                case 130:
                    xmlItem.put("alarm_level", 1);
                    break;
                case 131:
                    xmlItem.put("alarm_level", 2);
                    break;
                case 132:
                    xmlItem.put("alarm_level", 3);
                    break;
                case 133:
                    xmlItem.put("alarm_level", 4);
                    break;
                default:
                    break;
            }
            if (tWarnInfo.getWarnContent().contains("越线")){
                xmlItem.put("monitor_type", 2);
            }else if (tWarnInfo.getWarnContent().contains("火")){
                xmlItem.put("monitor_type", 101);
            }else if (tWarnInfo.getWarnContent().contains("烟")){
                xmlItem.put("monitor_type", 201);
            }
            // 目前都是识别图片 所以是5
            xmlItem.put("file_type", 5);
            String imgPath = tWarnInfo.getImagePath().replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content")));
            String targetNamePath = imgPath.replace(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content")),
                    "");
            uploadFileToUpFtps(imgPath, "/" + targetNamePath, upFtpsConfig);

            xmlItem.put("file_path", "/" + targetNamePath);
            xmlItem.put("time", tWarnInfo.getWarnTime());
            xmlItem.put("content", tWarnInfo.getWarnContent());
            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> alarmMap = new HashMap<>();
            alarmMap.put("list", list);
            log.info("告警上报：-" + alarmMap);
            Constant.otherServer(alarmMap, Constant.TCP_URL);
        }catch (Exception e){
            log.error("向上级系统上报静默监视告警出错:{}", e.getMessage());
        }
    }

    /**
     * 将结果推送给结果处理线程 DataDealThread
     *
     * @param response 返回结果
     * @param taskId 任务id
     */
    private void sendToDataDealThread(PicAnalyseResponse response, String taskId) {
        JSONObject jsonObject = packageObject(response, taskId);

        printJsonMsg(jsonObject);

        JSONObject jsonObjectData = JSON.parseObject(JSON.parseObject(jsonObject.getString("msgData")).getString("data"));
        JSONObject jsonObject1 = JSON.parseObject(String.valueOf(jsonObjectData.get("resultInfo1")));

        String analyseType = jsonObject1.getString("analyseType");

        int port = recognizePort;
        if (StringUtils.equals("11", analyseType) || StringUtils.equals("398", analyseType)){
            port = aiPort;
        }
        try {
            DataDealThread dataDealThread = new DataDealThread(jsonObject.toJSONString(), port, redisTemplate, analyseDataOperateService, syncWebsocketUrl);
            TaskExecutePool.getInstance().execute(dataDealThread);
        }catch (Exception e){
            log.error(e.getMessage());
            log.error("算法结果处理线程异常:{}", e.getStackTrace()[0]);
        }
    }

    /**
     * 组装要发给 DataDealThread 的 JSONObject对象
     *
     * @param response 返回结果
     * @param taskId 任务id
     */
    private JSONObject packageObject(PicAnalyseResponse response, String taskId) {
        JSONObject jsonObject = new JSONObject();
        JSONObject resultInfoObject = new JSONObject();
        JSONObject msgDataObject = new JSONObject();
        try {
            int i = 1;
            // 遍历多个点的分析结果,不同的巡视点
            for (AnalyseResult analyseResult : response.getResultsList()) {
                JSONObject resultDataObject = new JSONObject();
                StringJoiner resultValue = new StringJoiner(",");
                StringJoiner resultDesc = new StringJoiner(" ");
                StringJoiner resultImg = new StringJoiner(" ");

                String instanceId = analyseResult.getObjectId();
                resultDataObject.put("taskId", taskId);
                resultDataObject.put("instanceId", instanceId);
                String redisKeyName = "t_cruise_task_result:" + taskId + instanceId;
                String picPath = String.valueOf(redisTemplate.opsForHash().get(redisKeyName, "picpath"));
                String originPicPath = picPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultImg","content")),
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultRealImg","content")));

                List<AnalyseResultItem> results = analyseResult.getResults();
                // 判断该巡视点是否为27大类的点 若是  直接拿假数据  不要返回的结果
                Long devicePointId =  analyseDataOperateDao.selectDevicePointIdByInstanceId(Long.valueOf(instanceId));
                if (!generateMapFormat().isEmpty() && generateMapFormat().containsKey(String.valueOf(devicePointId))){
                    resultValue.add(String.valueOf(generateMapFormat().get(devicePointId)));
                    resultDataObject.put("analyseType", 398);
                    resultDataObject.put("resultDesc", String.valueOf(resultDesc.add("")));
                    resultDataObject.put("resultValue", String.valueOf(resultValue));
                    resultDataObject.put("analyseResultImg", originPicPath);
                    resultInfoObject.put("resultInfo" + i, resultDataObject);
                    i++;
                    continue;
                }
                // 没有识别出来任何缺陷 results为空  只有当缺陷识别才会这样，判别的results是有值的
                if (results.isEmpty()){
                    resultDataObject.put("analyseType", 398);
                    resultDataObject.put("resultDesc", String.valueOf(resultDesc.add("")));
                    resultDataObject.put("resultValue", String.valueOf(resultValue.add("")));
                    resultDataObject.put("analyseResultImg", originPicPath);
                    resultInfoObject.put("resultInfo" + i, resultDataObject);
                    i++;
                    continue;
                }
                // 非27大类的缺陷及判别类型的点  遍历单个点的分析结果,不同的缺陷
                Map<String, String> map = new HashMap<>(5);
                for (AnalyseResultItem result : results) {
                    /*resultDataObject.put("pictureCoordinate", Objects.nonNull(result.getPos()) ? result.getPos() : new ArrayList<Area>());*/
                    resultDataObject.put("conf", String.valueOf(Objects.nonNull(result.getConf()) ? result.getConf() : 0.0));
                    // 图像数据正确
                    if (!Objects.equals("2000", result.getCode())) {
                        log.error("巡视点为{}的图像数据错误", instanceId);
                        continue;
                    }
                    log.info("进入map放置");
                    map = setRecognizeResult(result, resultDesc, resultValue, resultImg, resultDataObject, originPicPath, Long.valueOf(instanceId));
                }

                if (Objects.equals("", map.get("resultValue").trim())){
                    log.info("没有识别出来任何缺陷");
                    resultDataObject.put("analyseResultImg", originPicPath);
                }else {
                    log.info("识别出来了缺陷");
                    List<String> resultImgList = new ArrayList<>();
                    Collections.addAll(resultImgList, map.get("resultImg").split(" "));
                    resultImgList = resultImgList.stream().distinct().collect(Collectors.toList());
                    resultDataObject.put("analyseResultImg",  resultImgList.get(0));
                }
                resultDataObject.put("resultDesc", Optional.ofNullable(map.get("resultDesc")).orElse(""));
                resultDataObject.put("resultValue", Optional.ofNullable(map.get("resultValue")).orElse(""));
                resultInfoObject.put("resultInfo" + i, resultDataObject);
                i++;
            }
            msgDataObject.put("desNode", "clientSocket001");
            msgDataObject.put("srcNode", "serverSocket");
            msgDataObject.put("data", resultInfoObject);
            jsonObject.put("msgID", UUID.randomUUID());
            jsonObject.put("msgType", "2");
            jsonObject.put("msgData", msgDataObject);
        }catch (Exception e){
            log.error("组装要发给 DataDealThread 的 JSONObject对象错误：{}", e.getMessage());
        }
        return jsonObject;
    }

    /**
     * 读取配置文件的假数据转化为map格式
     *
     */
    private Map<String, Object> generateMapFormat() {
        String path = this.getClass().getClassLoader().getResource(fileName).toString();
        path = path.replace("\\", "/");

        if (path.contains(":")) {
            // 1
            /*path = path.substring(6);*/
            // 2
            path = path.replace("file:", "");
        }
        JSONObject jsonObject = null;
        try {
            String input = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
            jsonObject = JSONObject.parseObject(input);
        } catch (IOException e) {
            log.error("json解析失败:{}", e.getMessage());
        }
        Map<String, Object> map = new HashMap<>(16);
        if (Objects.nonNull(jsonObject)){
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()){
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    /**
     * 根据算法分析类型 获取识别结果 组装结果详情
     *
     * @param result 识别结果
     * @param resultDesc 算法分析类型描述
     * @param resultValue 算法分析结果
     * @param resultImg 算法分析图片
     * @param resultDataObject 拼装给DataDealThread的对象
     * @param originPicPath 原始图片地址
     * @param instanceId 巡视点id
     * @return Map<String, String>
     */
    private Map<String, String> setRecognizeResult(AnalyseResultItem result, StringJoiner resultDesc, StringJoiner resultValue,
                                                   StringJoiner resultImg, JSONObject resultDataObject, String originPicPath, Long instanceId){
        String type = result.getType();
        String value = Optional.ofNullable(result.getValue()).orElse("");
        String desc = Optional.ofNullable(result.getDesc()).orElse("");

        Long devicePointId =  analyseDataOperateDao.selectDevicePointIdByInstanceId(instanceId);
        Map<String, String> map = new HashMap<>(5);

        if (Objects.isNull(type) || Objects.equals("tx_pb",type)){
            // 判别
            getDistinguishResult(result, resultDesc, resultValue, resultImg, resultDataObject, originPicPath, type, value, devicePointId, map);
        }else {
            // 缺陷和设备状态识别
            getDefectOrIdentification(result, resultDesc, resultValue, resultImg, resultDataObject, type, value, desc, devicePointId, map);
        }
        return map;
    }

    private Map<String, String> getDistinguishResult(AnalyseResultItem result, StringJoiner resultDesc, StringJoiner resultValue, StringJoiner resultImg,
                                                     JSONObject resultDataObject, String originPicPath, String type, String value, Long devicePointId, Map<String, String> map) {
        String targetPath;
        try {
            // 判断该巡视点是否为判别的点 若是  直接拿假数据  不要返回的结果
            if (!generateMapFormat().isEmpty() && generateMapFormat().containsKey(String.valueOf(devicePointId))){
                resultValue.add(String.valueOf(generateMapFormat().get(devicePointId)));
                resultDataObject.put("analyseType", 11);
                if (Objects.equals("1", value)){
                    // 图像有差异 才会返回图片地址
                    targetPath = copyFileFromFtps(type, result.getResImageUrl());
                }else {
                    // 图像无差异 取原图
                    targetPath = originPicPath;
                }
                resultDataObject.put("analyseResultImg", targetPath);
                map.put("resultDesc", String.valueOf(resultDesc));
                map.put("resultValue", String.valueOf(resultValue));
                return map;
            }
            if (Objects.equals("1", value)){
                // 图像有差异 才会返回图片地址
                resultValue.add("abnormal");
                targetPath = copyFileFromFtps(type, result.getResImageUrl());
                resultImg.add(targetPath);
            }else {
                // 图像无差异 取原图
                resultValue.add("normal");
            }
            for (Area area : result.getPos()){
                for (Point point : area.getAreas()){
                    resultValue.add(String.valueOf(point.getX()));
                    resultValue.add(String.valueOf(point.getY()));
                }
            }
            resultDataObject.put("analyseType", 11);
            map.put("resultDesc", String.valueOf(resultDesc.add("tx_pb")));
            map.put("resultValue", String.valueOf(resultValue));
            map.put("resultImg",String.valueOf(resultImg));
        }catch (Exception e){
            log.error("组装判别结果出错：{}", e.getMessage());
        }
        return map;
    }

    private Map<String, String> getDefectOrIdentification(AnalyseResultItem result, StringJoiner resultDesc, StringJoiner resultValue, StringJoiner resultImg,
                                           JSONObject resultDataObject, String type, String value, String desc, Long devicePointId, Map<String, String> map) {
        String targetPath;
        try {
            // 根据返回的算法类型查询算法相关信息
            List<TAlgorithmInfo> list = analyseDataOperateDao.selectAlgorithmInfo(type);

            if (list.isEmpty()){
                /*
                 * 缺陷
                 * */
                if (Objects.equals("1", value)){
                    resultValue.add(type);
                    // 图像有缺陷
                    for (Area area : result.getPos()){
                        for (Point point : area.getAreas()){
                            resultValue.add(String.valueOf(point.getX()));
                            resultValue.add(String.valueOf(point.getY()));
                        }
                    }
                    resultValue.add(Optional.ofNullable(String.valueOf(result.getConf())).orElse("0.0"));
                    resultDesc.add(desc);
                    targetPath = copyFileFromFtps(type, result.getResImageUrl());
                    resultImg.add(targetPath);
                }else {
                    // 图像无缺陷
                    resultDesc.add(type);
                }
                resultDataObject.put("analyseType", 398);
            }else {
                /*
                 * 设备状态识别
                 * */
                // 判断该巡视点是否为表计的点 若是  直接拿假数据  不要返回的结果
                if (!generateMapFormat().isEmpty() && generateMapFormat().containsKey(String.valueOf(devicePointId))){
                    resultValue.add(String.valueOf(generateMapFormat().get(devicePointId)));
                    resultDataObject.put("analyseType", 1);
                    map.put("resultDesc", String.valueOf(resultDesc));
                    map.put("resultValue", String.valueOf(resultValue));
                    return map;
                }
                if (Objects.equals("meter", type) || Objects.equals("infrared", type) || Objects.equals("qrcode", type)){
                    // 对于表计、红外、实物ID直接获取值
                    resultValue.add(value);
                    resultDataObject.put("analyseType", 1);
                }else {
                    // 根据value获取对应的值
                    int typeValue = Integer.parseInt(list.get(0).getAnalyseType() + value);
                    String resultDescTemp = RecogniseStatusEnum.getValueByCode(typeValue).getValue();
                    resultValue.add(resultDescTemp);
                    resultDataObject.put("analyseType", list.get(0).getAnalyseType());
                }
                targetPath = copyFileFromFtps(type, result.getResImageUrl());
                resultImg.add(targetPath);
            }
            map.put("resultDesc", String.valueOf(resultDesc));
            map.put("resultValue", String.valueOf(resultValue));
            map.put("resultImg", String.valueOf(resultImg));
        }catch (Exception e){
            log.error("组装缺陷或设备状态识别结果出错：{}", e.getMessage());
        }
        return map;
    }

    /**
     * 将ftps的文件复制到指定目录
     *
     * @param type 类型
     * @param sourcePath 源文件路径
     * @return String
     */
    public String copyFileFromFtps(String type, String sourcePath){
        try {
            String targetPath;
            if (Objects.isNull(type) || Objects.equals("tx_pb",type)){
                // 判别
                targetPath = redisTemplate.opsForHash().get("t_sys_param:judgeResultImg","content") + "/" + sourcePath;
            }else {
                List<String> analyseType = analyseDataOperateDao.selectAlgorithmType();
                if (analyseType.contains(type)){
                    // 表计识别(设备状态识别)
                    targetPath = redisTemplate.opsForHash().get("t_sys_param:meterResultImg","content") + "/" +sourcePath;
                }else {
                    // 缺陷
                    targetPath = redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content") + "/" +sourcePath;
                }
            }
            // 图片在ftps上的全路径
            String resultAbsolutePath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath","content") + "/" +sourcePath;
            FileUtil.copyFileUsingStream(resultAbsolutePath, targetPath);
            return targetPath;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 请求其他服务
     *
     * @param url 请求地址
     * @param map 请求参数
     * @return String
     */
    public Response restTemplatePost(String url, Map<String, Object> map) {
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Response.class);
    }

    /**
     * 请求其他服务
     * @param url 路径
     * @param json 参数
     * @return String
     */
    private String postUrlParams(String url, String json) {
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        try {
            HttpResponse response = client.execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 测试:json格式打印输入参数
     *
     * @param jsonObject json数据
     */
    private void printJsonMsg(JSONObject jsonObject) {
        String pretty = JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        log.info(pretty);
    }

    /**
     * 请求webSocket发送数据
     *
     * @param url 请求地址
     * @param json 发送内容
     * @return String
     */
    public String postUrl(String url, String json) throws IOException, URISyntaxException {
        log.info("webSocketUrl=={}", url);
        CloseableHttpClient client = HttpClients.createDefault();
        URI uri = new URIBuilder(url).setParameter("json", json).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    /**
     * 将文件上传至巡视主机ftp服务器
     *
     * @param sourcePath 源文件地址
     * @param targetPathName 目标文件地址名称
     */
    private void uploadFileToFtps(String sourcePath, String targetPathName, IntelAnalysisFtpsConfig ftpsConfig) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, ftpsConfig.getIp(), ftpsConfig.getPort(),
                    ftpsConfig.getKeypw(), ftpsConfig.getUsername(), ftpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至ftp服务器错误:{}", e.getMessage());
        }
    }

    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath 源文件地址
     * @param targetPathName 目标文件地址名称
     */
    private void uploadFileToUpFtps(String sourcePath, String targetPathName, UpFtpsConfig upFtpsConfig) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getKeypw(), upFtpsConfig.getUsername(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至ftp服务器错误:{}", e.getMessage());
        }
    }


}
