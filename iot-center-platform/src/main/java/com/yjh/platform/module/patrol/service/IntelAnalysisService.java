package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.AlarmService;
import com.yjh.platform.common.mqtt.FtpsService;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.Defect;
import com.yjh.platform.common.mqtt.alarmMsgBody.Different;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.configuration.IntelAnalysisFtpsConfig;
import com.yjh.platform.configuration.IntelligentAlgorithmConfig;
import com.yjh.platform.configuration.UpFtpsConfig;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.entity.AlgorithmExceptionEnum;
import com.yjh.platform.module.patrol.entity.AnalysePatrolTaskResult;
import com.yjh.platform.module.patrol.entity.TAlgorithmInfo;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.patrol.entity.interlanalysis.*;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.entity.TSysParam;
import com.yjh.platform.module.user.service.TSequentialConfService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

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
    @Autowired
    private FtpsService ftpsservice;
    @Autowired
    private PatrolResultHandler patrolResultHandler;
    @Autowired
    private AlarmService alarmService;
    @Autowired
    private TCameraPresetDao tCameraPresetDao;
    @Lazy
    @Autowired
    private TSequentialConfService tSequentialConfService;


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
            log.error(e.getMessage(), e);
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
            // 如果是静默监视识别 只要求识别越线闯入、未穿工装和未带安全帽
            if (Objects.equals("12", analysis.getAnalyseType())) {
                //静默的  判断他是本系统的还是下级
                TCameraPreset tCameraPreset = tCameraPresetDao.selectIsDownSystemPreset(analysis.getInstanceId());
                String recognizeType = "";
                if (analysis.getTaskId().contains("this")){
                    log.info("是本级系统来的--{}",analysis.getInstanceId());
                    recognizeType = tCameraPresetDao.selectRecognizeTypeByPresetId(analysis.getInstanceId());
                } else {
                    //下级来的
                    recognizeType = tCameraPresetDao.selectRecognizeTypeByPresetId(tCameraPreset.getPresetId());

                }
                AnalyseObject analyseObject = new AnalyseObject();
                analyseObject.setTypeList(Arrays.asList(recognizeType.split(",")));
                list.add(packagePicAnalyseRequest(analysis, analyseObject));
            } else if (Objects.equals("14", analysis.getAnalyseType())) {
                // AnalyseType 14表示相机预置位偏移识别
                list.add(pkPicAnalyseReqForPreset(analysis));
            } else {
                AnalyseObject analyseObject = setAnalyseObject(analysis);
                list.add(packagePicAnalyseRequest(analysis, analyseObject));
            }
        }
        return list;
    }

    /**
     * 将图片地址转成Analysis格式，并复用现有框架代码进行算法接口的调用
     *
     * @param picModelPath picModelPath
     * @param picTargetPath picTargetPath
     * @param idStr idStr
     * @return result
     */
    public List<Analysis> getAnalysisList(String picModelPath, String picTargetPath, String idStr) {
        Analysis analysis = new Analysis();
        analysis.setAnalyseType("14");
        analysis.setTaskId(idStr + "_presetCheck");
        analysis.setInstanceId(-1L);
        analysis.setPicPath(picTargetPath);
        analysis.setPicModelPath(picModelPath);
        analysis.setIsAi(1);

        return Arrays.asList(analysis);
    }

    /**
     * 组装PicAnalyseRequest参数，用于请求算法接口
     *
     * @param analysis analysis
     * @return result
     */
    private PicAnalyseRequest pkPicAnalyseReqForPreset(Analysis analysis) {
        PicAnalyseRequest picAnalyseRequest = new PicAnalyseRequest();
        picAnalyseRequest.setRequestHostIp(algorithmConfig.getResultIp());
        picAnalyseRequest.setRequestHostPort(algorithmConfig.getResultPort());
        picAnalyseRequest.setRequestId(UUID.randomUUID() + "#" + analysis.getTaskId());
        AnalyseObject analyseObject = pkAnalyseObjectForPreset(analysis);
        picAnalyseRequest.setObjectList(Arrays.asList(analyseObject));
        return picAnalyseRequest;
    }

    /**
     * analysis生成AnalyseObject参数
     *
     * @param analysis analysis
     * @return result
     */
    private AnalyseObject pkAnalyseObjectForPreset(Analysis analysis) {
        AnalyseObject analyseObject = new AnalyseObject();

        String instanceId = String.valueOf(analysis.getInstanceId());
        analyseObject.setObjectId(instanceId);
        String targetNamePath = upLoadFileByHttpPath(analysis.getPicPath());
        String modelNamePath = upLoadFileByHttpPath(analysis.getPicModelPath());
        List<String> imageUrlList = new ArrayList<>();
        imageUrlList.add(targetNamePath);
        analyseObject.setImageUrlList(imageUrlList);
        analyseObject.setImageNormalUrlPath(modelNamePath);
        analyseObject.setTypeList(Arrays.asList("tx_yzwpy"));
        return analyseObject;
    }

    /**
     * 将http地址所在的图片文件上传ftps，并返回ftps路径
     *
     * @param httpPath httpPath
     * @return result
     */
    private String upLoadFileByHttpPath(String httpPath) {
        try {
            String[] split = httpPath.split("/");
            String targetNamePath = split[split.length - 2] + "/" + split[split.length - 1];
            uploadFileToFtps(httpPath, "/" + targetNamePath, intelAnalysisFtpsConfig);
            return targetNamePath;
        } catch (Exception e) {
            log.error("upLoadFileByHttpPath err, httpPath: {}, msg: {}", httpPath, e.getMessage());
            return "";
        }
    }

    /**
     * 算法识别后的预置位结果解析，只有当返回结果确定偏移时，对redis中缓存状态数据进行更新
     *
     * @param response response
     * @param flagId flagId
     */
    private void presetCheckHandle(PicAnalyseResponse response, String flagId) {
        try {
            // 只有识别结果明确为偏移时才去修改redis
            if ("1".equals(response.getResultsList().get(0).getResults().get(0).getValue())) {
                String[] arr = flagId.split("_");
                Long cameraId = Long.parseLong(arr[0]);
                Long presetId = Long.parseLong(arr[1]);
                String key = String.format("CAMERA_PRESET_CHECK_RESULT_%d", cameraId);
                redisTemplate.opsForHash().put(key, presetId, -1);
            }
        } catch (Exception e) {
            log.error("presetCheckHandle fail, flagId: {}, err: {}", flagId, e.getMessage());
        }
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
                    String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isEPRI","content"));
                    if (StringUtils.equals("false", flag)){
                        // 拿提前拍好的预置位作为判别基准图
                        String imageNormalUrlPath = analyseDataOperateDao.selectPresetImgByCruise(analysis.getInstanceId());
                        imageNormalUrlPath = imageNormalUrlPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:presetRealImgPath","content")),
                                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:presetImgPath","content")));
                        String[] split = imageNormalUrlPath.split("/");
                        String targetNamePath = split[split.length - 2] + "/" + split[split.length - 1];
                        uploadFileToFtps(imageNormalUrlPath, "/" + targetNamePath, intelAnalysisFtpsConfig);
                        analyseObject.setImageNormalUrlPath(targetNamePath);
                    }else {
                        // 拿电科院给的图
                        String devicePointId = analyseDataOperateDao.selectDevicePointIdByInstanceId(analysis.getInstanceId());
                        String filePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:distinguishReferencePath","content"));
                        String imageNormalUrlPath = "";
                        File folder = new File(filePath);
                        File[] listFiles = folder.listFiles();
                        for (File direFile : listFiles){
                            if (direFile.getName().contains(devicePointId)){
                                imageNormalUrlPath = filePath + "/" + direFile.getName() + "/" + direFile.listFiles()[0].getName();
                                log.info("文件名称:{}", imageNormalUrlPath);
                            }
                        }
                        String[] split = imageNormalUrlPath.split("/");
                        String targetNamePath = split[split.length - 3] + "/" + split[split.length - 2] + "/" + split[split.length - 1];
                        uploadFileToFtps(imageNormalUrlPath, targetNamePath, intelAnalysisFtpsConfig);
                        analyseObject.setImageNormalUrlPath(targetNamePath);
                    }

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
                case "10":
                    typeList.add("switch");
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
            log.error(e.getMessage(), e);
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
            log.error(e.getMessage(), e);
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
            String result = postUrlParams(algorithmConfig.getUpdateUrl(), testJson.toJSONString());
            log.info("result==={}", result);

            if (StringUtils.isEmpty(result)){
                return Response.basRequest();
            }
            JSONObject jsonObject = JSON.parseObject(result);
            code = Integer.parseInt(String.valueOf(jsonObject.get("code")));
        }catch (Exception e){
            log.error(e.getMessage(), e);
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
        // 一键顺控
        if (Objects.equals("yjsk", flagId)){
            yjskHandle(response);
            return;
        }
        // 算法接口测试
        if (Objects.equals("666666", flagId)){
            algorithmTestHandle(response);
            return;
        }
        // 预置位偏移识别，flagId包含cameraId和presetId
        if (flagId.endsWith("presetCheck")) {
            presetCheckHandle(response, flagId);
            return;
        }
        // 普通图像分析
        List<AnalysePatrolTaskResult> resultList = sendAnalysePatrolTaskResult(response, flagId);
        patrolResultHandler.analysePatrolTaskResult(resultList);
    }

    /**
     * 算法分析结果解析
     * @param response 算法返回结果
     * @param taskId 任务id
     */
    private List<AnalysePatrolTaskResult> sendAnalysePatrolTaskResult(PicAnalyseResponse response, String taskId){
        // 遍历多个点的分析结果,不同的巡视点
        List<AnalysePatrolTaskResult> resultList = new ArrayList<>();
        try {
            for (AnalyseResult analyseResult : response.getResultsList()) {
                AnalysePatrolTaskResult taskResult = new AnalysePatrolTaskResult();
                StringJoiner resultValue = new StringJoiner(",");
                StringJoiner resultDesc = new StringJoiner(" ");
                StringJoiner resultImg = new StringJoiner(" ");

                String instanceId = analyseResult.getObjectId();
                String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
                String originPicPath = String.valueOf(redisTemplate.opsForHash().get(redisKeyName, "origpic"));
//                log.info("originPicPath===={}", originPicPath);

                String algorithmType = getAlgorithmTypeMap(instanceId);
                // 判断该巡视点是否为27大类的点 若是  直接拿假数据  不用判断返回的巡视结果
                String devicePointId = analyseDataOperateDao.selectDevicePointIdByInstanceId(Long.valueOf(instanceId));
                boolean flag = StringUtils.equals("398", algorithmType)
                        && !generateMapFormat().isEmpty()
                        && generateMapFormat().containsKey(devicePointId);
                if (Boolean.TRUE.equals(flag)){
                    resultValue.add(String.valueOf(generateMapFormat().get(devicePointId)));
                    taskResult.setResultValue(String.valueOf(resultValue));
                    taskResult.setTaskId(taskId);
                    taskResult.setInstanceId(instanceId);
                    taskResult.setResultDesc("");
                    taskResult.setAnalyseResultImg(originPicPath);
                    taskResult.setAnalyseType(algorithmType);
                    taskResult.setConf("0.0");
                    resultList.add(taskResult);
                    continue;
                }

                List<AnalyseResultItem> results = analyseResult.getResults();
                // 遍历多个分析结果,同一个巡视点
                taskResult.setTaskId(taskId);
                taskResult.setInstanceId(instanceId);
                taskResult.setAnalyseType(algorithmType);
                for (AnalyseResultItem result : results) {
                    taskResult.setConf(String.valueOf(result.getConf()));
                    try {
                        // 2000:正确 2001:图像数据错误 2002:算法分析失败
                        if (StringUtils.equals("2001", result.getCode()) || StringUtils.equals("2002", result.getCode())){
                            boolean includeDesc = AlgorithmExceptionEnum.isIncludeDesc(result.getDesc());
                            taskResult.setResultValue(includeDesc ?
                                    AlgorithmExceptionEnum.getInstance(result.getDesc()).getContent() : result.getDesc());
                            taskResult.setResultDesc(result.getDesc());
                            taskResult.setAnalyseResultImg(originPicPath);
                        }else {
                            Map<String, String> map = getResultMap(resultValue, resultDesc, resultImg, originPicPath, result);

                            if (StringUtils.isBlank(map.get("resultValue"))) {
//                                log.info("没有识别出来任何缺陷");
                                taskResult.setAnalyseResultImg(originPicPath);
                            }else {
//                                log.info("识别出来了缺陷");
                                List<String> resultImgList = new ArrayList<>();
                                Collections.addAll(resultImgList, StringUtils.split(map.get("resultImg"), " "));
                                resultImgList = resultImgList.stream().distinct().collect(Collectors.toList());
                                taskResult.setAnalyseResultImg(resultImgList.get(0));
                            }
                            taskResult.setResultDesc(map.getOrDefault("resultDesc", ""));
                            taskResult.setResultValue(map.getOrDefault("resultValue", ""));
                        }
                    } catch (Exception e) {
                        log.error("算法分析结果解析点位失败：", e);
                    }
                    resultList.add(taskResult);
                }
            }
        }catch (Exception e){
            log.error("算法分析结果解析异常：", e);
        }
        log.info("resultList=={}", resultList);
        return resultList;
    }

    /**
     * 查询巡视点关联的算法类型
     * @param instanceId 巡视点id
     * @return String
     */
    public String getAlgorithmTypeMap(String instanceId) {
        String analyseType = "";
        try {
            // is_ai为on缺陷,is_judge为on判别,algorithm_id非空为表计
            Map<String, Object> algorithmTypeMap = analyseDataOperateDao.selectAlgorithmByInstanceId(Long.valueOf(instanceId));
            log.info("algorithmTypeMap==={}", JSON.toJSONString(algorithmTypeMap));

            // 判断巡视点配置的算法类型 398-缺陷 11判别 1-12表计
            if (Objects.equals("on",  algorithmTypeMap.get("is_ai"))){
                analyseType = "398";
            }else if (Objects.nonNull(algorithmTypeMap.get("algorithm_id"))){
                analyseType = String.valueOf(algorithmTypeMap.get("algorithm_type"));
            }else {
                analyseType = "11";
            }
        }catch (Exception e){
            log.error("判断巡视点配置的算法类型异常：", e);
        }
        return analyseType;
    }

    private Map<String, String> getResultMap(StringJoiner resultValue, StringJoiner resultDesc, StringJoiner resultImg, String originPicPath, AnalyseResultItem result) {
        String type = result.getType();
        String value = Optional.ofNullable(result.getValue()).orElse("");
        String desc = Optional.ofNullable(result.getDesc()).orElse("");

        Map<String, String> map = new HashMap<>(5);
        if (Objects.equals("tx_pb",type)){
            // 判别
            map = distinguishResultHandler(result, resultDesc, resultValue, resultImg, originPicPath, type, value, map);
        }else {
            // 缺陷和设备状态识别
            map = defectOrRecognizeHandler(result, resultDesc, resultValue, resultImg, originPicPath, type, value, desc, map);
        }
        return map;
    }

    /**
     * 判别结果处理
     */
    private Map<String, String> distinguishResultHandler(AnalyseResultItem result, StringJoiner resultDesc, StringJoiner resultValue, StringJoiner resultImg,
                                                         String originPicPath, String type, String value,  Map<String, String> map) {
        String targetPath;
        try {
            String resImageUrl = result.getResImageUrl();
            if (StringUtils.isNotEmpty(resImageUrl)) {
                resImageUrl = resImageUrl.startsWith("/") ? resImageUrl.substring(1) : resImageUrl;
            }
            if (StringUtils.equals("1", value)){
                resultValue.add("abnormal");
                targetPath = copyFileFromFtps(type, resImageUrl);
                resultImg.add(targetPath);
            }else {
                resultValue.add("normal");
                resultImg.add(originPicPath);
            }
            for (Area area : result.getPos()){
                for (Point point : area.getAreas()){
                    resultValue.add(point.getX() > 0 ? String.valueOf(point.getX()) : "0.0");
                    resultValue.add(point.getY() > 0 ? String.valueOf(point.getY()) : "0.0");
                }
            }
            map.put("resultDesc", String.valueOf(resultDesc.add("tx_pb")));
            map.put("resultValue", String.valueOf(resultValue));
            map.put("resultImg", String.valueOf(resultImg));
        }catch (Exception e){
            log.error("组装判别结果异常：", e);
            map.put("resultDesc", "");
            map.put("resultValue", "结果解析失败");
            map.put("resultImg", resultImg.length() > 0 ? String.valueOf(resultImg) : originPicPath);
        }
        return map;
    }

    /**
     * 缺陷和识别结果处理
     */
    private Map<String, String> defectOrRecognizeHandler(AnalyseResultItem result, StringJoiner resultDesc, StringJoiner resultValue, StringJoiner resultImg,
                                                         String originPicPath, String type, String value, String desc, Map<String, String> map) {
        String targetPath;
        try {
            String resImageUrl = result.getResImageUrl().startsWith("/") ? result.getResImageUrl().substring(1) : result.getResImageUrl();
            List<TAlgorithmInfo> list = analyseDataOperateDao.selectAlgorithmInfo(type);

            if (list.isEmpty()){
                if (StringUtils.isEmpty(type)){
                    // 没有识别出来任何缺陷 results为空  只有当缺陷识别才会这样，判别的results是有值的
                    map.put("resultDesc", "");
                    map.put("resultValue", "");
                    map.put("resultImg", originPicPath);
                    return map;
                }
                if (StringUtils.equals("0", value)){
                    resultDesc.add(type);
                }
                if (StringUtils.equals("1", value)){
                    resultValue.add(type);
                    for (Area area : result.getPos()){
                        for (Point point : area.getAreas()){
                            resultValue.add(point.getX() > 0 ? String.valueOf(point.getX()) : "0.0");
                            resultValue.add(point.getY() > 0 ? String.valueOf(point.getY()) : "0.0");
                        }
                    }
                    resultValue.add(Optional.ofNullable(String.valueOf(result.getConf())).orElse("0.0"));
                    resultDesc.add(desc);
                    targetPath = copyFileFromFtps(type, resImageUrl);
                    resultImg.add(targetPath);
                }
            }else {
                targetPath = copyFileFromFtps(type, resImageUrl);
                resultImg.add(targetPath);
                if (StringUtils.equals("数据错误", value)){
                    map.put("resultDesc", "数据错误");
                    map.put("resultValue", "数据错误");
                    map.put("resultImg", originPicPath);
                    return map;
                }
                if (StringUtils.equals("未获得读数", value)){
                    map.put("resultDesc", "未获得读数");
                    map.put("resultValue", "未获得读数");
                    map.put("resultImg", originPicPath);
                    return map;
                }
                if (Objects.equals("meter", type) || Objects.equals("infrared", type) || Objects.equals("qrcode", type)){
                    // 对于表计、红外、实物ID直接获取值
                    resultValue.add(value);
                }else {
                    // 根据value获取对应的值
                    if (ArrayUtils.contains(new String[]{"0","1","2","3","4","5","6"}, value)){
                        int typeValue = Integer.parseInt(list.get(0).getAnalyseType() + value);
                        String resultDescTemp = Optional.ofNullable(RecogniseStatusEnum.getValueByCode(typeValue)).orElse(RecogniseStatusEnum.UNKNOWN).getValue();
                        resultValue.add(resultDescTemp);
                    }else {
                        resultValue.add("算法返回格式不正确");
                    }
                }
            }
            map.put("resultDesc", String.valueOf(resultDesc));
            map.put("resultValue", String.valueOf(resultValue));
            map.put("resultImg", String.valueOf(resultImg));
        }catch (Exception e){
            log.error("组装缺陷或识别结果异常：", e);
            map.put("resultDesc", "");
            map.put("resultValue", "结果解析失败");
            map.put("resultImg", resultImg.length() > 0 ? String.valueOf(resultImg) : originPicPath);
        }
        return map;
    }

    private void algorithmTestHandle(PicAnalyseResponse response){
        //判断是缺陷还是判别
        String analyseType = response.getResultsList().get(0).getResults().get(0).getType();
        String yearMonth = new SimpleDateFormat("yyyyMM").format(new Date());
        String nowTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        if (StringUtils.isEmpty(analyseType)){
            log.info("没有识别出来任何缺陷");
            return;
        }
        // 判别
        if("tx_pb".equals(analyseType) || Objects.isNull(analyseType)){

            String origpcimagename=nowTime +"_测试间隔_测试设备_测试测点_原图.jpg";
            //拼接算法管理平台原始图片推送地址
            String remoteorigfilepath=ftpsservice.getFtpsRemotePath() + "/" +"判别"+"/"+yearMonth+"/"+origpcimagename;

            String resImageUrl = copyFileFromFtps(response.getResultsList().get(0).getResults().get(0).getType(),
                    response.getResultsList().get(0).getResults().get(0).getResImageUrl());
            log.info("resImageUrl:"+resImageUrl);

            resImageUrl = resImageUrl.replace("//","/");
            log.info("home路径："+resImageUrl);

            String imgF = nowTime+"_测试间隔_测试设备_测试测点_判别告警.jpg";
            //拼接算法管理平台分析告警结果图片地址
            String remotefilepath=ftpsservice.getFtpsRemotePath() + "/" +"判别"+"/"+yearMonth+"/"+imgF;

            String imgBase =nowTime+ "_测试间隔_测试设备_测试测点_判别基准.jpg";
            //拼接算法管理平台分析告警结果图片地址
            String remoteBaseFilePath=ftpsservice.getFtpsRemotePath() + "/" +"判别"+"/"+yearMonth+"/"+imgBase;

            //基准图
            String imageNormalUrlPath = analyseDataOperateDao.selectPresetImgByCruise(Long.valueOf(response.getResultsList().get(0).getObjectId()));
            String basePath = imageNormalUrlPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:presetRealImgPath","content")),
                            String.valueOf(redisTemplate.opsForHash().get("t_sys_param:presetImgPath","content")))
                    .replace("//","/");
            log.info("判别基准："+basePath);
            Alarm alarmDetail=new Alarm();

            try {
                String defectResultRealImg = resImageUrl.replaceAll(
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultImg","content")),
                        redisTemplate.opsForHash().get("t_sys_param:judgeResultRealImg","content")+"/");
                Map<String, Object> jasonMaps = new HashMap<>(16);
                jasonMaps.put("type", "algorithmTest");
                jasonMaps.put("path", defectResultRealImg);
                String json = JSON.toJSONString(jasonMaps);
                log.info("发送给前端的消息：{}", json);
                postUrl(syncWebsocketUrl, json);
            }catch (Exception e){
                log.error("算法测试发送websocket异常：", e);
            }

            List<Different> differentList=new ArrayList<>();
            for (AnalyseResultItem item :response.getResultsList().get(0).getResults()){
                Different different = new Different();
                different.setX1((int)item.getPos().get(0).getAreas().get(0).getX());
                different.setY1((int)item.getPos().get(0).getAreas().get(0).getY());
                different.setX2((int)item.getPos().get(0).getAreas().get(1).getX());
                different.setY2((int)item.getPos().get(0).getAreas().get(1).getY());

                differentList.add(different);
            }
            alarmDetail.setDifferent(differentList);
            alarmDetail.setBay_name("测试间隔");
            alarmDetail.setDevice_name("测试设备");
            alarmDetail.setPoint_name("测试测点");
            alarmDetail.setTime(DateTimeUtil.format(new Date()));
            //图片原图
            alarmDetail.setPic_raw(remoteorigfilepath);
            alarmDetail.setPic_diff_base(remoteBaseFilePath);
            alarmDetail.setPic_different(remotefilepath);
            alarmDetail.setPic_height(1080);
            alarmDetail.setPic_width(1920);
            log.info("判别消息：{}",alarmDetail);

            ftpsservice.uploadFile("原图",Constant.algorithmTestPicPath,remoteorigfilepath);
            ftpsservice.uploadFile("基准",basePath,remoteBaseFilePath);
            ftpsservice.uploadFile("结果",resImageUrl,remotefilepath);

            alarmService.PushMsg(alarmDetail);
            log.info("发送算法管理平台结束");
        }
        List<TAlgorithmInfo> list = analyseDataOperateDao.selectAlgorithmInfo(analyseType);

        // 缺陷
        if (list.isEmpty()){

            String origpcimagename=nowTime +"_测试间隔_测试设备_测试测点_原图.jpg";
            //拼接算法管理平台原始图片推送地址
            String remoteorigfilepath=ftpsservice.getFtpsRemotePath() + "/" +"缺陷"+"/"+yearMonth+"/"+origpcimagename;

            String resImageUrl = copyFileFromFtps(response.getResultsList().get(0).getResults().get(0).getType(),
                    response.getResultsList().get(0).getResults().get(0).getResImageUrl());
            String imgF = nowTime +"_测试间隔_测试设备_测试测点_缺陷告警.jpg";
            //拼接算法管理平台分析告警结果图片地址
            String remotefilepath=ftpsservice.getFtpsRemotePath() + "/" +"缺陷"+"/"+yearMonth+"/"+imgF;

            Alarm alarmDetail=new Alarm();

            try {
                String defectResultRealImg = resImageUrl.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content"))+"/",
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg","content")));
                Map<String, Object> jasonMaps = new HashMap<>(16);
                jasonMaps.put("type", "algorithmTest");
                jasonMaps.put("path", defectResultRealImg);
                String json = JSON.toJSONString(jasonMaps);
                log.info("发送给前端的消息：{}", json);
                postUrl(syncWebsocketUrl, json);
            }catch (Exception e){
                log.info("算法测试发送websocket异常：", e);
            }

            List<Defect> defectList1=new ArrayList<>();
            for (AnalyseResultItem item :response.getResultsList().get(0).getResults()){
                Defect defect= new Defect();
                defect.setX1((int)item.getPos().get(0).getAreas().get(0).getX());
                defect.setY1((int)item.getPos().get(0).getAreas().get(0).getY());
                defect.setX2((int)item.getPos().get(0).getAreas().get(1).getX());
                defect.setY2((int)item.getPos().get(0).getAreas().get(1).getY());
                int confidence = (int)(item.getConf() * 100);
                defect.setConfidence(confidence);
                defect.setDesc(item.getDesc()+"(坐标位置 "+defect.getX1()+","+
                        defect.getY1()+","+
                        defect.getX2()+","+
                        defect.getY2()+";"+
                        "置信度 "+ confidence
                        +"%)");
                defect.setType(item.getType());
                defectList1.add(defect);
            }
            alarmDetail.setDefect(defectList1);
            alarmDetail.setBay_name("测试间隔");
            alarmDetail.setDevice_name("测试设备");
            alarmDetail.setPoint_name("测试测点");
            alarmDetail.setTime(DateTimeUtil.format(new Date()));
            //图片原图
            alarmDetail.setPic_raw(remoteorigfilepath);
            alarmDetail.setPic_defect(remotefilepath);
            alarmDetail.setPic_height(1080);
            alarmDetail.setPic_width(1920);
            log.info("缺陷测试消息：{}",alarmDetail);

            ftpsservice.uploadFile("原图", Constant.algorithmTestPicPath,remoteorigfilepath);
            ftpsservice.uploadFile("结果图",resImageUrl,remotefilepath);

            alarmService.PushMsg(alarmDetail);
            log.info("发送算法管理平台结束");
        }else {
            String value = response.getResultsList().get(0).getResults().get(0).getValue();
            Map<String, Object> jasonMaps = new HashMap<>(16);
            jasonMaps.put("type", "algorithmTest");
            jasonMaps.put("value", value);
            String json = JSON.toJSONString(jasonMaps);
            log.info("发送给前端的消息：{}", json);

            try {
                postUrl(syncWebsocketUrl, json);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 一键顺控结果处理
     * @param response 算法返回结果
     */
    private void yjskHandle(PicAnalyseResponse response){
        Map<String,String> recBack = new HashMap<>(9);
        try{
            recBack.put("meteId",StringUtils.substringAfter(response.getRequestId(),"="));
            recBack.put("code",response.getResultsList().get(0).getResults().get(0).getCode());
            recBack.put("conf",String.valueOf(response.getResultsList().get(0).getResults().get(0).getConf()));
            recBack.put("desc",response.getResultsList().get(0).getResults().get(0).getDesc());
            recBack.put("resImageUrl",response.getResultsList().get(0).getResults().get(0).getResImageUrl());
            recBack.put("type",response.getResultsList().get(0).getResults().get(0).getType());
            recBack.put("value",response.getResultsList().get(0).getResults().get(0).getValue());
            String services = tSequentialConfService.sequentialRecBack(recBack);
            log.info("一键顺控services：{}" , services);
        }catch (Exception e){
            log.error("一键顺控-变相信号-分析主机返回处理失败: ", e);
        }
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
            if (StringUtils.isEmpty(results.get(0).getType())){
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

                // 因为算法端乱改乱改 所以就在这里截取了 不想改动后面的逻辑(拼接路径)
                String resImageUrl = result.getResImageUrl().startsWith("/") ? result.getResImageUrl().substring(1) : result.getResImageUrl();
                /*.replaceAll(
                redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath","content") + "/", "")*/;

                String targetPath = copyFileFromFtps(type, resImageUrl);
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
                String[] resultArr = String.valueOf(content).split("\\s+");

                List<TWarnInfo> list = silentAlarmStore(resultArr, map, resultImgList);
                alarmToUpSystem(map, list);
            }

//            alarmToSFZJ(isHave,String.valueOf(map.get("custom_id")),results);
        }
    }

    /**
     * 静默监视告警结果存储
     *
     * @param resultArr 告警内容
     * @param map 巡视点信息
     * @param resultImgList 结果图片
     * @return TWarnInfo
     */
    private List<TWarnInfo> silentAlarmStore(String[] resultArr, Map<String, Object> map, List<String> resultImgList) {
        List<TWarnInfo> list = new ArrayList<>();
        try {
            for (int i = 0; i < resultArr.length; i++) {
                String alarmLevel = analyseDataOperateService.selectAlarmLevel(resultArr[i]);
                TWarnInfo tWarnInfo = new TWarnInfo()
                        .setWarnLevel(ValueUtil.toInteger(alarmLevel,131))
                        .setWarnTime(new Date())
                        .setWarnName("静默监视告警数据")
                        .setWarnContent(resultArr[i])
                        .setDeviceId(Long.valueOf(String.valueOf(map.get("device_id"))))
                        .setCunstomId(String.valueOf(map.get("custom_id")))
                        .setInstanceId(Long.valueOf(String.valueOf(map.get("instance_id"))))
                        .setStdMeteId(Long.valueOf(String.valueOf(map.get("device_mete_id"))))
                        .setConfMode(276)
                        .setDefectModel(450)
                        .setAlarmSource(689)
                        .setImagePath(resultImgList.get(0));
                analyseDataOperateDao.insertWarnInfo(tWarnInfo);

                list.add(tWarnInfo);

                //webSocket通知前端调用查询告警弹框的接口
                Long warnId = analyseDataOperateDao.selectCurrentWarn();
                Map<String, Object> jasonMaps = new HashMap<>(16);
                jasonMaps.put("type", "alarmPopUp");
                jasonMaps.put("warnId", warnId);
                jasonMaps.put("defectModel", 450);
                String json = JSON.toJSONString(jasonMaps);
                log.info("发送给前端的消息：{}", json);
                String edgeLevel = String.valueOf(ValueUtil.getOrDefault(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"),""));
                if (!Constant.LEVEL_UP_SYSTEM.equals(edgeLevel)){
                    postUrl(syncWebsocketUrl, json);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("组装并存储告警信息异常：", e);
        }
        return null;
    }

    private void alarmToSFZJ(Boolean isHave, String instanceId, List<AnalyseResultItem> results){
        try{
            String flag= ftpsservice.getFlag();
            if("1".equals(flag) && isHave) {
                log.info("defect类型:开始向算法管理平台发送图片和mqtt消息");
                Alarm alarmDetail = new Alarm();
                String year = Integer.toString(LocalDate.now().getYear());
                String month = Integer.toString(LocalDate.now().getMonthValue());

                // 获取原始图路径 resultImgRealPath
                String origpicpath = redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath","content") + instanceId + ".jpg";
                String[] str2 = origpicpath.split("/");
                String origpcimagename = str2[str2.length - 1];
                //拼接算法管理平台原始图片推送地址
                String remoteorigfilepath = ftpsservice.getFtpsRemotePath() + "/" + "缺陷" + "/" + year + "/" + month + "/" + origpcimagename;
                log.info("开始向算法管理平台发送图片和mqtt消息");
                HashMap<String, String> nameMap = analyseDataOperateService.selectDeviceNameInfo(Long.valueOf(instanceId));
                List<Defect> defectList1 = new ArrayList<>();
                String targetPath= "";
                for (AnalyseResultItem result : results) {
                    String code = Optional.ofNullable(result.getCode()).orElse("");
                    String value = Optional.ofNullable(result.getValue()).orElse("");
                    String type = Optional.ofNullable(result.getType()).orElse("");
                    String desc = Optional.ofNullable(result.getDesc()).orElse("");
                    int conf = (int)(result.getConf() * 100);
                    targetPath = copyFileFromFtps(type, result.getResImageUrl());
                    if("1".equals(value)){
                        Defect defect = new Defect();
                        defect.setX1((int)result.getPos().get(0).getAreas().get(0).getX());
                        defect.setY1((int)result.getPos().get(0).getAreas().get(0).getY());
                        defect.setX2((int)result.getPos().get(0).getAreas().get(1).getX());
                        defect.setY2((int)result.getPos().get(0).getAreas().get(1).getY());
                        defect.setType(type);
                        defect.setDesc(desc);
                        defect.setConfidence(conf);
                        defectList1.add(defect);
                    }
                }
                //先取出算法平台返回的resultinfo中的结果图片路径
                String resultImagebak = targetPath;
                String[] str = resultImagebak.split("/");
                String imagename = str[str.length - 1];
                //拼接算法管理平台分析告警结果图片地址
                String remotefilepath = ftpsservice.getFtpsRemotePath() + "/" + "缺陷" + "/" + year + "/" + month + "/" + imagename;

                alarmDetail.setDefect(defectList1);
                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                //需要修改位devicename
                alarmDetail.setDevice_name(nameMap.get("deviceName"));
                alarmDetail.setPoint_name(nameMap.get("meteName"));
                alarmDetail.setTime(DateTimeUtil.format(new Date()));
                //图片原图
                alarmDetail.setPic_raw(remoteorigfilepath);
                //判别基准图路径
                alarmDetail.setPic_diff_base("");
                //判别告警图路径,即分析结果图
                alarmDetail.setPic_different("");
                //缺陷告警图路径
                alarmDetail.setPic_defect(remotefilepath);
                ftpsservice.uploadFile("遥信告警", origpicpath, remoteorigfilepath);
                ftpsservice.uploadFile("遥信告警", resultImagebak, remotefilepath);
                if (!ftpsservice.fileExits(remoteorigfilepath)) {
                    //原始图片上传
                    ftpsservice.uploadFile("遥信告警", origpicpath, remoteorigfilepath);
                }
                if (!ftpsservice.fileExits(remotefilepath)) {
                    ftpsservice.uploadFile("遥信告警", resultImagebak, remotefilepath);
                }

                log.info("巡视主机与智能分析主机：origpicpath:{}", origpicpath);
                log.info("巡视主机与智能分析主机：remoteorigfilepath:{}", remoteorigfilepath);
                log.info("巡视主机与智能分析主机：resultImagebak:{}", resultImagebak);
                log.info("巡视主机与智能分析主机：remotefilepath:{}", remotefilepath);
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
     * @param tWarnInfoList 告警数据
     */
    private void alarmToUpSystem(Map<String, Object> map, List<TWarnInfo> tWarnInfoList) {
        for (TWarnInfo tWarnInfo : tWarnInfoList){
            String warnTime = DateTimeUtil.format(tWarnInfo.getWarnTime());
            // 针对渗漏油、设备变形、设备断裂、设备倾斜四类隐患，每小时一次采集与识别
            if (tWarnInfo.getWarnContent().contains("渗漏油") || tWarnInfo.getWarnContent().contains("设备变形")
                    || tWarnInfo.getWarnContent().contains("设备断裂") || tWarnInfo.getWarnContent().contains("设备倾斜")) {
                if (Objects.nonNull(map.get("preset_id")) && Objects.nonNull(map.get("camera_id"))) {
                    log.info("渗漏油、设备变形... 采集与识别");
                    String presetId = String.valueOf(map.get("preset_id"));
                    String cameraId = String.valueOf(map.get("camera_id"));
                    String silentMonitoringKey = "silent_monitoring:" + cameraId + ":" + presetId;
                    Map<String, String> redisInfoMap = new HashMap<>();
                    redisInfoMap.put("cameraId", cameraId);
                    redisInfoMap.put("presetId", presetId);

                    if (!redisTemplate.hasKey(silentMonitoringKey)) { // 判断是否有数据 没有初始化，有后续直接取
                        redisInfoMap.put("lastTime", warnTime);
                        redisTemplate.opsForHash().putAll(silentMonitoringKey, redisInfoMap);
                    } else {
                        String lastTime = String.valueOf(redisTemplate.opsForHash().get(silentMonitoringKey, "lastTime"));
                        int i = DateTimeUtil.hoursBetween(DateTimeUtil.parse(lastTime), DateTimeUtil.parse(warnTime));
                        if (i < 1) {
                            return;
                        } else {
                            log.info("上次告警上传时间：" + lastTime + " -- 这次告警时间：" + warnTime);
                            redisInfoMap.put("lastTime", warnTime);
                            redisTemplate.opsForHash().putAll(silentMonitoringKey, redisInfoMap);
                        }
                    }
                }
            }

            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> xmlItems = new ArrayList<>();
            Map<String, Object> xmlItem = new HashMap<>(16);

            xmlBaseModel.setType("63");
            xmlItem.put("patroldevice_code", map.get("device_id"));
            xmlItem.put("patroldevice_name", map.get("device_name"));
            switch (tWarnInfo.getWarnLevel()){
                case 130:
                    xmlItem.put("alarm_level", "1");
                    break;
                case 131:
                    xmlItem.put("alarm_level", "2");
                    break;
                case 132:
                    xmlItem.put("alarm_level", "3");
                    break;
                case 133:
                    xmlItem.put("alarm_level", "4");
                    break;
                default:
                    break;
            }

            try {
                String[] alarmType = algorithmConfig.getSilentMonitorNameAndType().split(",");
                for (String str : alarmType) {
                    String[] split = new String(str.getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8).split(":");
                    if (tWarnInfo.getWarnContent().contains(split[0])) {
                        xmlItem.put("monitor_type", split[1]);
                        break;
                    }
                }
                // 目前都是识别图片 所以是5
                xmlItem.put("file_type", "5");
                String imgPath = tWarnInfo.getImagePath().replaceAll(
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg", "content")),
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content")));
                String targetNamePath = imgPath.replace(
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content")), "").substring(1);
                log.info("imgPath:{},targetNamePath:{}",imgPath,targetNamePath);
                uploadFileToUpFtps(imgPath, "jm/" + targetNamePath, upFtpsConfig);

                xmlItem.put("file_path", targetNamePath);
                xmlItem.put("time", warnTime);
                xmlItem.put("content", tWarnInfo.getWarnContent());
                xmlItems.add(xmlItem);
                xmlBaseModel.setItems(xmlItems);
                List<XMLBaseModel> list = new ArrayList<>();
                list.add(xmlBaseModel);
                Map<String, List<XMLBaseModel>> alarmMap = new HashMap<>(3);
                alarmMap.put("list", list);
                log.info("告警上报：{}", alarmMap);
                Constant.otherServer(alarmMap, Constant.TCP_URL);
            }catch (Exception e){
                log.error("向上级系统上报静默监视告警异常: ", e);
            }
        }
    }


    /**
     * 读取配置文件的假数据转化为map格式
     *
     */
    private Map<String, Object> generateMapFormat() {
        Map<String, Object> map = new HashMap<>(16);
        String path = String.valueOf(this.getClass().getClassLoader().getResource(fileName));
        if (CommonUtils.isEmptyOrNullstr(path)){
            return map;
        }

        path = path.replace("\\", "/");

        if (path.contains(":")) {
            path = path.replace("file:", "");
        }
        JSONObject jsonObject = null;
        try {
            String input = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
            jsonObject = JSONObject.parseObject(input);
        } catch (IOException e) {
            log.error("json解析失败: ", e);
        }

        if (Objects.nonNull(jsonObject)){
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()){
                map.put(entry.getKey(), entry.getValue());
            }
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
            log.error("将ftps的文件复制到指定目录异常：", e);
        }
        return null;
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
            log.error(e.getMessage(), e);
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
            log.error("将文件上传至巡视主机ftp服务器错误: ", e);
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
            log.error("将文件上传至上级系统ftp服务器错误: ", e);
        }
    }

    public void algorithmUpdateResult(UpdateResponse response) throws Exception{
        Map<String, Object> jasonMaps = new HashMap<>(16);
        jasonMaps.put("type", "algorithmUpdateTest");
        jasonMaps.put("result", StringUtils.equals("1", response.getResult()) ? "成功" : "失败");
        String json = JSON.toJSONString(jasonMaps);
        log.info("发送给前端的消息：{}", json);

        postUrl(syncWebsocketUrl, json);
    }

}
