package com.yjh.platform.module.patrol.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.PathUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.algorithm.AlgorithmService;
import com.yjh.platform.common.Constant;
import com.yjh.platform.algorithm.FtpsService;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.Defect;
import com.yjh.platform.common.mqtt.alarmMsgBody.Different;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdDevicemeteService;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.entity.AlgorithmExceptionEnum;
import com.yjh.platform.module.patrol.entity.AnalysePatrolTaskResult;
import com.yjh.platform.module.patrol.entity.TAlgorithmInfo;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.patrol.entity.interlanalysis.Point;
import com.yjh.platform.module.patrol.entity.interlanalysis.*;
import com.yjh.platform.module.patrol.service.impl.HttpAnalyticsServiceImpl;
import com.yjh.platform.module.patrol.thread.AlgorithmAnalyseThread;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.service.AlarmShieldService;
import com.yjh.platform.module.user.service.TCameraPresetService;
import com.yjh.platform.module.user.service.TSequentialConfService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * @author 丫C
 * @date 2022/4/11
 */
@Service
public class IntelAnalysisService {
    private final Logger log = LoggerFactory.getLogger(IntelAnalysisService.class);

    /**
     * webSocket请求地址
     */

    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AnalyseDataOperateDao analyseDataOperateDao;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private FtpsService ftpsService;
    @Autowired
    private AlgorithmService algorithmService;
    @Lazy
    @Autowired
    private TSequentialConfService tSequentialConfService;
    @Autowired
    private TStdDeviceService tStdDeviceService;
    @Autowired
    private TStdDevicemeteService tStdDevicemeteService;
    @Autowired
    private TCameraPresetService tCameraPresetService;
    @Lazy
    @Autowired
    private HttpAnalyticsServiceImpl analyticsService;
    @Autowired
    private AlarmShieldService alarmShieldService;
    @Autowired
    private LogsRecord logsRecord;

    private List<String> TASK_RESULT = new ArrayList<>();
    private final String TASK_INTEL_ANALYSIS_RESULT="TASK_INTEL_ANALYSIS_RESULT:";

    private final String ALGORITHM_PARAMS = "ALGORITHM_PARAMS:";

    /**
     * 巡视主机请求图像分析--功能
     *
     * @param analysisList 需要分析的对象
     * @return Response
     */
    public List<Response> picAnalyseNoDetection(List<Analysis> analysisList){
        log.info("开始，巡视主机请求图像分析--功能，入参：analysisList： {}", JSONUtil.toJSONString(analysisList));
        List<Response> responseList;
        String taskId = analysisList.get(0).getTaskId();
        if (checkAnalyseNeedPlanB(taskId)) {
            log.info("PlanB开关打开，入参：request： {}", JSONUtil.toJSONString(analysisList));
            responseList = processAnalysePlanB(analysisList);
        } else {
            Result result = analyticsService.defect(analysisList);
            responseList = (List<Response>)result.getData();
        }

        return responseList;
    }

    private boolean checkAnalyseNeedPlanB(String taskId) {
        try {
            if (StringUtils.endsWith(taskId, "jm")) {
                return checkAnalyseNeedPlanBJm();
            } else if (StringUtils.endsWith(taskId, "yjsk")) {
                return checkAnalyseNeedPlanBYjsk();
            }
        } catch (Exception e) {
            log.error("checkAnalyseNeedPlanB err, ", e);
        }

        return false;
    }

    private boolean checkAnalyseNeedPlanBJm() {
        Object needManMadeObj = redisTemplate.opsForValue().get("t_sys_param.silentMonitorAnalyseResult.needManMade");
        boolean silentMonitorNeedManMade = false;
        if (needManMadeObj != null) {
            silentMonitorNeedManMade = (Boolean) needManMadeObj;
        }

        return silentMonitorNeedManMade;
    }

    private boolean checkAnalyseNeedPlanBYjsk() {
        return Boolean.parseBoolean(SysParamConfig.getSysContent("sequentialFlag"));
    }

    private List<Response> processAnalysePlanB(List<Analysis> analysisList) {
        List<Response> responseList = new ArrayList<>();
        try {
            List<PicAnalyseRequest> list = analyticsService.formatTransition(analysisList);
            for (PicAnalyseRequest request : list) {
                PicAnalyseResponse picAnalyseResponse = getAnalyseResp(request);
                log.info("手动拼接PicAnalyseResponse ： {}", JSONUtil.toJSONString(picAnalyseResponse));
                picAnalyseRetNotify(picAnalyseResponse);
                responseList.add(new Response(200));
            }
        } catch (Exception e) {
            log.info("processAnalysePlanB出现异常：", e);
            responseList.add(Response.serverError());
        }
        return responseList;
    }

    private PicAnalyseResponse getAnalyseResp(PicAnalyseRequest request) {
        PicAnalyseResponse response = new PicAnalyseResponse();
        response.setRequestId(request.getRequestId());
        AnalyseResult analyseResult = getAnalyseResp(request.getObjectList().get(0));
        response.setResultList(Arrays.asList(analyseResult));

        return response;
    }

    private AnalyseResult getAnalyseResp(AnalyseObject analyseObject) {
        AnalyseResult analyseResult = new AnalyseResult();
        AnalyseResultItem item = getAnalyseResult(analyseObject);
        analyseResult.setResults(Arrays.asList(item));
        analyseResult.setObjectId(analyseObject.getObjectId());
        redisTemplate.opsForHash().put("silentMonitorImageUrl", analyseResult.getObjectId(), analyseObject.getImagePathList().get(0));
        return analyseResult;
    }

    private AnalyseResultItem getAnalyseResult(AnalyseObject analyseObject) {
        AnalyseResultItem item = new AnalyseResultItem();
        item.setResImagePath(analyseObject.getImagePathList().get(0));
        item.setCode("2000");
        item.setConf(0.0f);
        item.setDesc("发现异常");
        item.setValue("1");
        String type = (String) redisTemplate.opsForValue().get("t_sys_param.silentMonitorAnalyseResult.type");
        item.setType(type);
        return item;
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
        String imgPath = tCameraPresetService.getImageLocalPath(picTargetPath, true);
        analysis.setReferenceImage(imgPath);
        analysis.setPicPath(picModelPath);
        // analysis.setPicModelPath(picModelPath);
        analysis.setTargetParent("presetCheck");
        analysis.setIsAi(1);

        return Arrays.asList(analysis);
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
            if ("2000".equals(response.getResultList().get(0).getResults().get(0).getCode())
                && "1".equals(response.getResultList().get(0).getResults().get(0).getValue())) {
                log.info("算法识别预置位偏移, flagId: {}", flagId);
                String[] arr = flagId.split("_");
                Long cameraId = Long.parseLong(arr[0]);
                String presetIdStr = arr[1];
                String key = String.format("CAMERA_PRESET_CHECK_RESULT:%d", cameraId);
                redisTemplate.opsForHash().put(key, presetIdStr, "-1");

                tCameraPresetService.cameraPresetCheckWarn(Long.parseLong(presetIdStr));
            }
        } catch (Exception e) {
            log.error("presetCheckHandle fail, flagId: {}, err: {}", flagId, e.getMessage());
        }
    }

    /**
     * 巡视主机请求算法更新--接口
     *
     * @param request 参数
     * @return Response
     */
    public Response algorithmUpdate(UpdateRequest request){
        Map<String, Object> param = new HashMap<>(16);
        param.put("requestHostIp", applicationProperties.getIntelAlgorithmConfig().getResultIp());
        param.put("requestHostPort", applicationProperties.getIntelAlgorithmConfig().getResultPort());
        param.put("requestId", request.getRequestId());
        param.put("type", request.getType());

        String[] split = request.getAlgorithmPath().split("/");
        String targetNamePath =  split[split.length - 2] + "/" + split[split.length - 1];
        uploadFileToFtps(request.getAlgorithmPath(), "/" + targetNamePath, applicationProperties.getIntelAnalysisFtps());

        param.put("algorithmPath", targetNamePath);

        JSONObject testJson = (JSONObject) JSONObject.toJSON(param);
        printJsonMsg(testJson);

        int code;
        try {
            String result = postUrlParams(applicationProperties.getIntelAlgorithmConfig().getUpdateUrl(), testJson.toJSONString());
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
     * 请求算法资源信息接口
     *
     * @return 算法资源信息
     */
    public JSONObject algorithmResource() {
        JSONObject jsonObject = new JSONObject();
        try {
            String url = applicationProperties.getIntelAlgorithmConfig().getAlgorithmResourceUrl();
            String result = HttpClientUtils.getInstance().getUrl(url, null);
//            log.info("result==={}", result);
            if (StringUtils.isNotBlank(result)) {
                jsonObject = JSONObject.parseObject(result);
                if ("200".equals(jsonObject.getObject("code", String.class))) {
                    jsonObject.remove("code");
                    jsonObject.put("network_rtt_avg", SystemInfoUtil.getNetDelay(url) + "ms");
                    List<Map<String, String>> versionInfo = new ArrayList<>();
                    String version1 = String.valueOf(redisTemplate.opsForHash().entries(ALGORITHM_PARAMS + "1").get("version"));
                    if (StringUtils.isNotBlank(version1)) {
                        Map<String, String> versionMap = new HashMap<>(2);
                        versionMap.put("type", "1");
                        versionMap.put("version", version1);
                        versionInfo.add(versionMap);
                    }
                    String version2 = String.valueOf(redisTemplate.opsForHash().entries(ALGORITHM_PARAMS + "2").get("version"));
                    if (StringUtils.isNotBlank(version2)) {
                        Map<String, String> versionMap = new HashMap<>(2);
                        versionMap.put("type", "2");
                        versionMap.put("version", version2);
                        versionInfo.add(versionMap);
                    }
                    jsonObject.put("version_info", versionInfo);
                    jsonObject.put("province_name", applicationProperties.getManagerAlgorithmConfig().getProvinceName());
                    jsonObject.put("city_name", applicationProperties.getManagerAlgorithmConfig().getCityName());
                    jsonObject.put("section_id", applicationProperties.getManagerAlgorithmConfig().getSectionId());
                    jsonObject.put("section_name", applicationProperties.getManagerAlgorithmConfig().getSectionName());
                    jsonObject.put("volt_level", applicationProperties.getManagerAlgorithmConfig().getVoltLevel());
                    jsonObject.put("station_name", applicationProperties.getManagerAlgorithmConfig().getStationName());
                    jsonObject.put("service_port", applicationProperties.getManagerAlgorithmConfig().isEnable() ? "1" : "0");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return jsonObject;
    }

    /**
     * 请求系统自检信息
     *
     * @return 自检信息
     */
    public Map<String, Object> systemCheck(HttpServletRequest request, Long userId) {
        Map<String, Object> res = Maps.newHashMap();
        String userName = String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userId, "userName"));
        try {
            String url = applicationProperties.getIntelAlgorithmConfig().getSystemCheckUrl();
            String result = HttpClientUtils.getInstance().getUrl(url, null);
            if (StringUtils.isNotBlank(result)) {
                JSONObject json = JSONObject.parseObject(result);
                if (200 == Integer.parseInt(String.valueOf(json.get("code")))) {
                    JSONObject data = (JSONObject) json.get("data");
                    if (data.containsKey("cpu")) {
                        Map<String, Object> cpuMap = new HashMap<>(2);
                        cpuMap.put("rate", data.get("cpu"));
                        int cpuUse = Integer.parseInt(String.valueOf(data.get("cpu")));
                        Map<String, String> redisMap = redisTemplate.opsForHash().entries("t_sys_param:cpuFreeMin");
                        double cpuFreeMin = Double.parseDouble(redisMap.get("content"));
                        if ((100 - cpuUse) < cpuFreeMin) {
                            logsRecord.LoginLogsSend(request, "5", "cpu最小空闲报警", "分析主机cpu最小空闲报警", userName, String.valueOf(userId), 1);
                            cpuMap.put("code", "01");
                        } else {
                            cpuMap.put("code", "02");
                        }
                        res.put("cpu", cpuMap);
                    }
                    if (data.containsKey("memory")) {
                        Map<String, Object> memoryMap = (Map<String, Object>) data.get("memory");
                        Map<String, String> redisMap = redisTemplate.opsForHash().entries("t_sys_param:MemoryFreeMin");
                        double memoryFreeMin = Double.parseDouble(redisMap.get("content"));
                        int memoryUse = Integer.parseInt(String.valueOf(memoryMap.get("rate")));
                        if (100 - memoryUse < memoryFreeMin) {
                            logsRecord.LoginLogsSend(request, "5", "内存最小空闲报警", "分析主机内存最小空闲报警", userName, String.valueOf(userId), 1);
                            memoryMap.put("code", "01");
                        } else {
                            memoryMap.put("code", "02");
                        }
                        res.put("memory", memoryMap);
                    }
                    if (data.containsKey("disk")) {
                        Map<String, Object> diskMap = (Map<String, Object>) data.get("disk");
                        Map<String, String> redisMap = redisTemplate.opsForHash().entries("t_sys_param:DiskFreeMin");
                        double memoryFreeMin = Double.parseDouble(redisMap.get("content"));
                        int diskUse = Integer.parseInt(String.valueOf(diskMap.get("rate")));
                        if (100 - diskUse < memoryFreeMin) {
                            logsRecord.LoginLogsSend(request, "5", "磁盘最小空闲报警", "分析主机磁盘最小空闲报警", userName, String.valueOf(userId), 1);
                            diskMap.put("code", "01");
                        } else {
                            diskMap.put("code", "02");
                        }
                        res.put("disk", diskMap);
                    }
                    res.put("gpu", data.get("gpu"));
                    res.put("gpu_memory", data.get("gpu_memory"));
                }
            }
        } catch (Exception e) {
            res.put("status", "离线");
            log.error("分析主机离线，无法通信", e);
        }
        return res;
    }

    public void picAnalyseRetNotify(PicAnalyseResponse response){
        log.info("巡视主机收到分析结果开始解析: {}", JSONUtil.toJSONString(response));
        response = checkIsIn(response);
        if (response == null){
            log.info("本测点结果算法已经返回过，本次返回的结果不正确，本次结果不处理！");
            return;
        }
        String flagId = response.getRequestId().split("#")[1];
        // 正常的巡视多了接口会阻塞，所以放在线程池
        if (ArrayUtils.contains(new String[]{"jm", "yjsk", "666666"}, flagId) || StringUtils.endsWith(flagId, "presetCheck")) {
            picResAnalyse(response);
        }else {
            AlgorithmAnalyseThread analyseThread = new AlgorithmAnalyseThread(response, flagId);
            ThreadPoolUtil.PATROL_POOL.addThread(analyseThread);
        }
    }

    private PicAnalyseResponse checkIsIn(PicAnalyseResponse response){
        String taskId = response.getRequestId().split("#")[1];
        String key = TASK_INTEL_ANALYSIS_RESULT+taskId;
        Boolean isIn = redisTemplate.boundSetOps(key).isMember(response.getRequestId());
        if (isIn){
            //这个条结果算法已经返回过结果
            List<AnalyseResult> analyseResults = response.getResultList();
            //找到返回结果中出现非2000的结果说明本次返回的结果是不可用的
            boolean isOkResult = true;
            for (AnalyseResult analyseResult: analyseResults){
                for (AnalyseResultItem analyseResultItem : analyseResult.getResults()){
                    if (!"2000".equals(analyseResultItem.getCode())){
                        isOkResult = false;
                        break;
                    }
                }
            }
            if (!isOkResult){
                return  null;
            }
        } else {
            redisTemplate.boundSetOps(key).add(response.getRequestId());
            redisTemplate.boundSetOps(key).expire(2, TimeUnit.DAYS);
        }
        return response;
    }

    /**
     *  巡视主机收到分析结果开始解析
     *
     * @param response 参数
     */
    @Async
    public void picResAnalyse(PicAnalyseResponse response){
        String flagId = response.getRequestId().split("#")[1];
        // 静默监视结果
        if (Objects.equals("jm", flagId)){
            log.info("flagId判断为，静默监视结果");
            silentMonitorHandle(response);
            return;
        }
        // 一键顺控
        if (Objects.equals("yjsk", flagId)){
            log.info("flagId判断为，一键顺控");
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
    }

    /**
     * 算法分析结果解析
     * @param response 算法返回结果
     * @param taskId 任务id
     */
    public List<AnalysePatrolTaskResult> sendAnalysePatrolTaskResult(PicAnalyseResponse response, String taskId){
        // 遍历多个点的分析结果,不同的巡视点
        List<AnalysePatrolTaskResult> resultList = new ArrayList<>();
        List<String> falseDataCruiseList = new ArrayList<>();
        try {
            for (AnalyseResult analyseResult : response.getResultList()) {

                String instanceId = analyseResult.getObjectId();
                String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
                String originPicPath = String.valueOf(redisTemplate.opsForHash().get(redisKeyName, "origpic"));

                String algorithmType = getAlgorithmTypeMap(instanceId);
                // 判断该巡视点是否为27大类的点 若是  直接拿假数据  不用判断返回的巡视结果
                String devicePointId = analyseDataOperateDao.selectDevicePointIdByInstanceId(Long.valueOf(instanceId));
                boolean flag = StringUtils.equals("398", algorithmType)
                        && !generateMapFormat().isEmpty()
                        && generateMapFormat().containsKey(devicePointId);
                if (Boolean.TRUE.equals(flag)){
                    AnalysePatrolTaskResult taskResult = new AnalysePatrolTaskResult();
                    taskResult.setTaskId(taskId);
                    taskResult.setInstanceId(instanceId);
                    taskResult.setAnalyseType(algorithmType);
                    falseDataCruiseList.add(instanceId);
                    // json格式："123":"1--正常--坐标--置信度"
                    String jsonValue= String.valueOf(generateMapFormat().get(devicePointId));
                    String[] vals = jsonValue.split("--");
                    taskResult.setResultValue(vals[0]);
                    taskResult.setTaskId(taskId);
                    taskResult.setInstanceId(instanceId);
                    taskResult.setResultDesc(vals[1]);
                    taskResult.setAnalyseType(algorithmType);
                    if (vals.length >= 4) {
                        taskResult.setRectangle(vals[2]);
                        taskResult.setConf(vals[3]);
                    }
                    taskResult.setAnalyseResultImg(originPicPath);
                    // 拿图并复制
                    String falseDataPicPath = SysParamConfig.getSysContent("falseDataPicPath");
                    String resultImagePath = SysParamConfig.getSysContent("resultImgPath");
                    File folder = new File(falseDataPicPath);
                    File[] listFiles = folder.listFiles();
                    for (File direFile : listFiles) {
                        if (direFile.getName().contains(devicePointId)) {
                            direFile.getAbsolutePath();
                            FileUtil.copyFileUsingStream(direFile.getAbsolutePath(), resultImagePath + direFile.getName());
                            taskResult.setAnalyseResultImg(resultImagePath + direFile.getName());
                        }
                    }
                    resultList.add(taskResult);
                    break;
                }

                List<AnalyseResultItem> results = analyseResult.getResults();
                for (AnalyseResultItem result : results) {
                    // 遍历多个分析结果,同一个巡视点
                    AnalysePatrolTaskResult taskResult = new AnalysePatrolTaskResult();
                    taskResult.setTaskId(taskId);
                    taskResult.setInstanceId(instanceId);
                    taskResult.setAnalyseType(algorithmType);
                    taskResult.setRectangle(getRectangleByAnalyseResult(result));
                    taskResult.setConf(String.valueOf(result.getConf()));
                    try {
                        // 2000:正确 2001:图像数据错误 2002:算法分析失败
                        if (!StringUtils.equals("2000", result.getCode())){
                            boolean includeDesc = AlgorithmExceptionEnum.isIncludeDesc(result.getDesc());
                            String desc = includeDesc ? AlgorithmExceptionEnum.getInstance(result.getDesc()).getContent() : result.getDesc();
                            taskResult.setResultValue("-1");
                            taskResult.setResultDesc(desc);
                            taskResult.setAnalyseResultImg(originPicPath);
                        } else {
                            getResultMap(taskResult, originPicPath, result);
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
     * 将算法返回的坐标信息填写到rectangle中
     *
     * @param result AnalyseResultItem
     * @return result
     */
    private String getRectangleByAnalyseResult(AnalyseResultItem result) {
        if (CollectionUtils.isEmpty(result.getPos())) {
            return null;
        }

        List<Point> areas = result.getPos().get(0).getAreas();
        if (CollectionUtils.isEmpty(areas) || areas.size() != 2) {
            return null;
        }
        Point p1 = areas.get(0);
        Point p2 = areas.get(1);

        return String.format("%.1f,%.1f;%.1f,%.1f;%.1f,%.1f;%.1f,%.1f", p1.getX(), p1.getY(), p2.getX(), p1.getY(), p1.getX(), p2.getY(), p2.getX(), p2.getY());
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

    private void getResultMap(AnalysePatrolTaskResult taskResult, String originPicPath, AnalyseResultItem result) {
        String type = result.getType();
        String value = Optional.ofNullable(result.getValue()).orElse("");
        String desc = Optional.ofNullable(result.getDesc()).orElse("");

        if (Objects.equals("tx_pb",type)){
            // 判别
            distinguishResultHandler(result, taskResult, originPicPath, type, value);
        }else {
            // 缺陷和设备状态识别
            defectOrRecognizeHandler(result, taskResult, originPicPath, type, value, desc);
        }
    }

    /**
     * 判别结果处理
     */
    private void distinguishResultHandler(AnalyseResultItem result, AnalysePatrolTaskResult taskResult, String originPicPath, String type,
        String value) {
        String targetPath = null;
        try {
            String resImageUrl = result.getResImagePath();
            if (StringUtils.isNotEmpty(resImageUrl)) {
                resImageUrl = resImageUrl.startsWith("/") ? resImageUrl.substring(1) : resImageUrl;
            }
            taskResult.setResultValue(value);
            taskResult.setResultDesc("图像有差异");
            targetPath = copyFileFromFtps(type, resImageUrl);
            if (StringUtils.equals("1", value)) {
                taskResult.setAnalyseResultImg(targetPath);
            } else {
                taskResult.setAnalyseResultImg(originPicPath);
                taskResult.setResultDesc("未见异常");
            }
        } catch (Exception e) {
            log.error("组装判别结果异常：", e);
            taskResult.setResultValue("-1");
            taskResult.setResultDesc("分析失败");
            taskResult.setAnalyseResultImg(StringUtils.isNotEmpty(targetPath) ? targetPath : originPicPath);
        }
    }

    /**
     * 缺陷和识别结果处理
     */
    private void defectOrRecognizeHandler(AnalyseResultItem result, AnalysePatrolTaskResult taskResult, String originPicPath, String type,
        String value, String desc) {
        String targetPath = null;
        try {
            String resImageUrl = result.getResImagePath().startsWith("/") ? result.getResImagePath().substring(1) : result.getResImagePath();
            List<TAlgorithmInfo> list = analyseDataOperateDao.selectAlgorithmInfo(type);

            if (StringUtils.isNotEmpty(resImageUrl)) {
                targetPath = copyFileFromFtps(type, resImageUrl);
                taskResult.setAnalyseResultImg(targetPath);
            }

            taskResult.setResultValue(value);
            taskResult.setResultDesc(desc);

            if (list.isEmpty()) {
                taskResult.setResultValue(type);
                if (StringUtils.isEmpty(type) || StringUtils.equals("0", value)) {
                    // 没有识别出来任何缺陷 results为空  只有当缺陷识别才会这样，判别的results是有值的
                    taskResult.setResultValue("0");
                    taskResult.setResultDesc("未见异常");
                    taskResult.setAnalyseResultImg(originPicPath);
                }
            } else {
                // 非表计，数显，红外，二维码识别
                if (!StringUtils.equalsAny(type, "meter", "infrared", "qrcode")) {
                    // 根据value获取对应的值
                    if (ArrayUtils.contains(new String[] {"0", "1", "2", "3", "4", "5", "6"}, value)) {
                        int typeValue = Integer.parseInt(list.get(0).getAnalyseType() + value);
                        String resultDescTemp =
                            Optional.ofNullable(RecogniseStatusEnum.getValueByCode(typeValue)).orElse(RecogniseStatusEnum.UNKNOWN)
                                .getValue();
                        String descVal = result.getDesc();
                        if (StringUtils.containsAny(descVal, "储能", "非储能", "红", "蓝", "远方", "就地", "开")) {
                            resultDescTemp = descVal;
                        }
                        taskResult.setResultDesc(resultDescTemp);
                    } else {
                        taskResult.setResultDesc("算法返回格式不正确");
                    }
                } else {
                    taskResult.setResultDesc("");
                }
            }
        } catch (Exception e) {
            log.error("组装缺陷或识别结果异常：", e);
            taskResult.setResultValue("-1");
            taskResult.setResultDesc("分析失败");
            taskResult.setAnalyseResultImg(StringUtils.isNotEmpty(targetPath) ? targetPath : originPicPath);
        }
    }

    private void algorithmTestHandle(PicAnalyseResponse response) {
        //判断是缺陷还是判别
        List<AnalyseResult> resultsList = response.getResultList();

        String yearMonth = new SimpleDateFormat("yyyyMM").format(new Date());
        String nowTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        if (CollectionUtils.isEmpty(resultsList)) {
            log.info("没有识别出来任何缺陷");
            return;
        }
        List<String> meterTypeList = analyseDataOperateDao.selectAlgorithmType();

        List<Alarm> alarms = new ArrayList<>();
        for (AnalyseResult analyseResult : resultsList) {
            String objectId = analyseResult.getObjectId();
            Long instanceId = NumberUtils.toLong(objectId);
            Map<String, String> nameMap = analyseDataOperateService.selectDeviceNameInfo(instanceId);

            String bayName = nameMap.getOrDefault("upRegionName", "测试1#主变间隔");
            String deviceName = nameMap.getOrDefault("deviceName", "测试35kV侧避雷器");
            String pointName = nameMap.getOrDefault("meteName", "测试泄漏电流表");

            String imageBaseName = nowTime + "_" + bayName + "_" + deviceName + "_" + pointName + "_";
            String origpcimagename = imageBaseName + "原图.jpg";

            List<AnalyseResultItem> results = analyseResult.getResults();
            List<Different> differentList = new ArrayList<>();
            List<Defect> defectList = new ArrayList<>();
            Alarm alarmDetail = new Alarm();

            alarmDetail.setBay_name(bayName);
            alarmDetail.setDevice_name(deviceName);
            alarmDetail.setPoint_name(pointName);
            alarmDetail.setTime(DateTimeUtil.format(new Date()));
            alarmDetail.setPic_height(1080);
            alarmDetail.setPic_width(1920);

            for (AnalyseResultItem item : results) {
                String analyseType = item.getType();
                if ("tx_pb".equals(analyseType) || Objects.isNull(analyseType)) {
                    if ("0".equals(item.getValue())) {
                        log.info("图像没有差异：{}", item);
                        break;
                    }
                    String resImageUrl = copyFileFromFtps(item.getType(), item.getResImagePath());
                    log.info("resImageUrl:{}", resImageUrl);

                    resImageUrl = resImageUrl.replace("//", "/");
                    log.info("home路径：{}", resImageUrl);

                    String imgF = imageBaseName + "判别告警.jpg";
                    //拼接算法管理平台分析告警结果图片地址
                    String remotefilepath =
                        applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath() + "/" + "判别" + "/" + yearMonth + "/"
                            + imgF;

                    String imgBase = imageBaseName + "判别基准.jpg";
                    //拼接算法管理平台分析告警结果图片地址
                    String remoteBaseFilePath =
                        applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath() + "/" + "判别" + "/" + yearMonth + "/"
                            + imgBase;

                    //基准图
                    String imageNormalUrlPath = analyseDataOperateDao.selectPresetImgByCruise(instanceId);
                    String basePath = imageNormalUrlPath.replaceAll(SysParamConfig.getSysContent("presetRealImgPath"),
                        SysParamConfig.getSysContent("presetImgPath")).replace("//", "/");
                    log.info("判别基准：{}", basePath);

                    String remoteorigfilepath =
                        applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath() + "/" + "判别" + "/" + yearMonth + "/"
                            + origpcimagename;

                    try {
                        String defectResultRealImg = resImageUrl.replaceAll(SysParamConfig.getSysContent("judgeResultImg"),
                            SysParamConfig.getSysContent("judgeResultRealImg") + "/");
                        Map<String, Object> jasonMaps = new HashMap<>(16);
                        jasonMaps.put("type", "algorithmTest");
                        jasonMaps.put("path", defectResultRealImg);
                        String json = JSON.toJSONString(jasonMaps);
                        log.info("发送给前端的消息：{}", json);
                        postUrl(Constant.WEBSOCKET_URL, json);
                    } catch (Exception e) {
                        log.error("算法测试发送websocket异常：", e);
                    }

                    Different different = new Different();
                    different.setX1((int)item.getPos().get(0).getAreas().get(0).getX());
                    different.setY1((int)item.getPos().get(0).getAreas().get(0).getY());
                    different.setX2((int)item.getPos().get(0).getAreas().get(1).getX());
                    different.setY2((int)item.getPos().get(0).getAreas().get(1).getY());
                    differentList.add(different);

                    //图片原图
                    alarmDetail.setPic_raw(remoteorigfilepath);
                    alarmDetail.setPic_diff_base(remoteBaseFilePath);
                    alarmDetail.setPic_different(remotefilepath);

                    ftpsService.uploadFile("原图", Constant.algorithmTestPicPath, remoteorigfilepath);
                    ftpsService.uploadFile("基准", basePath, remoteBaseFilePath);
                    ftpsService.uploadFile("结果", resImageUrl, remotefilepath);
                } else if (!meterTypeList.contains(analyseType)) {
                    if ("0".equals(item.getValue())) {
                        log.info("没有缺陷：{}", item);
                        break;
                    }
                    String remoteorigfilepath =
                        applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath() + "/" + "缺陷" + "/" + yearMonth + "/"
                            + origpcimagename;

                    String resImageUrl = copyFileFromFtps(item.getType(), item.getResImagePath());
                    String imgF = imageBaseName + "缺陷告警.jpg";
                    //拼接算法管理平台分析告警结果图片地址
                    String remotefilepath =
                        applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath() + "/" + "缺陷" + "/" + yearMonth + "/"
                            + imgF;

                    try {
                        String defectResultRealImg = resImageUrl.replaceAll(SysParamConfig.getSysContent("defectResultImg") + "/",
                            SysParamConfig.getSysContent("defectResultRealImg"));
                        Map<String, Object> jasonMaps = new HashMap<>(16);
                        jasonMaps.put("type", "algorithmTest");
                        jasonMaps.put("path", defectResultRealImg);
                        String json = JSON.toJSONString(jasonMaps);
                        log.info("发送给前端的消息：{}", json);
                        postUrl(Constant.WEBSOCKET_URL, json);
                    } catch (Exception e) {
                        log.info("算法测试发送websocket异常：", e);
                    }

                    Defect defect = new Defect();
                    defect.setX1((int)item.getPos().get(0).getAreas().get(0).getX());
                    defect.setY1((int)item.getPos().get(0).getAreas().get(0).getY());
                    defect.setX2((int)item.getPos().get(0).getAreas().get(1).getX());
                    defect.setY2((int)item.getPos().get(0).getAreas().get(1).getY());
                    int confidence = (int)(item.getConf() * 100);
                    defect.setConfidence(confidence);
                    defect.setDesc(
                        item.getDesc() + "(坐标位置 " + defect.getX1() + "," + defect.getY1() + "," + defect.getX2() + "," + defect.getY2()
                            + ";" + "置信度 " + confidence + "%)");
                    defect.setType(item.getType());
                    defectList.add(defect);

                    //图片原图
                    alarmDetail.setPic_raw(remoteorigfilepath);
                    alarmDetail.setPic_defect(remotefilepath);

                    ftpsService.uploadFile("原图", Constant.algorithmTestPicPath, remoteorigfilepath);
                    ftpsService.uploadFile("结果图", resImageUrl, remotefilepath);
                } else {
                    String value = item.getValue();
                    Map<String, Object> jasonMaps = new HashMap<>(16);
                    jasonMaps.put("type", "algorithmTest");
                    jasonMaps.put("value", value);
                    String json = JSON.toJSONString(jasonMaps);
                    log.info("发送给前端的消息：{}", json);

                    try {
                        postUrl(Constant.WEBSOCKET_URL, json);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            if (!differentList.isEmpty()) {
                alarmDetail.setDifferent(differentList);
            }
            if (!defectList.isEmpty()) {
                alarmDetail.setDefect(defectList);
            }

            log.info("告警管理平台消息：{}", JSON.toJSONString(alarmDetail));

            if (!differentList.isEmpty() || !defectList.isEmpty()) {
                alarms.add(alarmDetail);
            }
        }

        if (!alarms.isEmpty()) {
            algorithmService.pushAlarmMsg(alarms);
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
            recBack.put("code",response.getResultList().get(0).getResults().get(0).getCode());
            recBack.put("conf",String.valueOf(response.getResultList().get(0).getResults().get(0).getConf()));
            recBack.put("desc",response.getResultList().get(0).getResults().get(0).getDesc());
            recBack.put("resImageUrl",response.getResultList().get(0).getResults().get(0).getResImagePath());
            recBack.put("type",response.getResultList().get(0).getResults().get(0).getType());
            recBack.put("value",response.getResultList().get(0).getResults().get(0).getValue());
            log.info("一键顺控recBack:{}", JSONUtil.toJSONString(recBack));
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
        for (AnalyseResult  analyseResult : response.getResultList()){
            log.info("遍历当前analyseResult：", JSONUtil.toJSONString(analyseResult));
            List<String> content = new ArrayList<>();
            List<String> resultImg = new ArrayList<>();

            List<AnalyseResultItem> results = analyseResult.getResults();
            if (StringUtils.isEmpty(results.get(0).getType())){
                // 没有识别出来任何缺陷 results为空
                log.info("没有识别出来任何缺陷 results为空");
                continue;
            }
            Map<String, Object> map = analyseDataOperateDao.selectInstanceInfo(Long.valueOf(analyseResult.getObjectId()));
            log.info("analyseDataOperateDao.selectInstanceInfo入参及结果，ObjectId：{}， map: {}", analyseResult.getObjectId(), JSONUtil.toJSONString(map));

            log.info("人工干预静默监视识别结果开始，干预前的结果：{}", JSONUtil.toJSONString(analyseResult));
            // 人工干预静默监视识别结果
            processSilentMonitorResult(analyseResult);
            log.info("人工干预静默监视识别结果结束，干预后的结果：{}", JSONUtil.toJSONString(analyseResult));

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
                content.add(type);

                // 因为算法端乱改乱改 所以就在这里截取了 不想改动后面的逻辑(拼接路径)
                String resImageUrl = result.getResImagePath().startsWith("/") ? result.getResImagePath().substring(1) : result.getResImagePath();

                String targetPath = copyFileFromFtps(type, resImageUrl);
                String defectResultRealImg = targetPath.replaceAll(SysParamConfig.getSysContent("defectResultImg"),
                    SysParamConfig.getSysContent("defectResultRealImg"));
                resultImg.add(defectResultRealImg);
            }

            List<String> resultImgList = resultImg.stream().distinct().collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(resultImgList)){
                String[] resultArr = content.toArray(new String[0]);

                List<TWarnInfo> list = silentAlarmStore(resultArr, map, resultImgList);
                alarmToUpSystem(map, list);
            }

        }
    }

    /**
     * 人工干预静默算法识别结果
     *
     * @param analyseResult analyseResult
     */
    private void processSilentMonitorResult(AnalyseResult  analyseResult) {
        log.info("人工干预静默监视识别结果: {}", JSONUtil.toJSONString(analyseResult));
        try {
            List<AnalyseResultItem> analyseResults = analyseResult.getResults();
            String objectId = analyseResult.getObjectId();

            if (CollectionUtils.isEmpty(analyseResults)) {
                log.info("人工干预静默监视, analyseResults为空，返回");
                return;
            }

            Object needManMadeObj = redisTemplate.opsForValue().get("t_sys_param.silentMonitorAnalyseResult.needManMade");
            boolean silentMonitorNeedManMade = false;
            if (needManMadeObj != null) {
                silentMonitorNeedManMade = (Boolean) needManMadeObj;
            }

            if (!silentMonitorNeedManMade) {
                log.info("人工干预静默监视识，needManMade开关关闭， 返回");
                return;
            }

            String type = (String) redisTemplate.opsForValue().get("t_sys_param.silentMonitorAnalyseResult.type");
            String imageUrl = (String) redisTemplate.opsForHash().get("silentMonitorImageUrl", objectId);
            log.info("从redis中获取silentMonitorImageUrl， objectId: {}, imageUrl: {}, type: {}", objectId, imageUrl, type);

            String analyseImageUrl = createAnalyseImage(imageUrl, objectId, type);
            for (AnalyseResultItem result : analyseResults) {
                result.setCode("2000");
                result.setValue("1");
                result.setType(type);
                result.setResImagePath(analyseImageUrl);
                setAnalyseArea(result);
            }

            log.info("人工干预静默监视识,拼接analyseResults，拼接后的结果： {}", JSONUtil.toJSONString(analyseResults));
            redisTemplate.opsForValue().set("t_sys_param.silentMonitorAnalyseResult.needManMade", false);
        } catch (Exception e) {
            log.error("人工干预静默算法识别结果失败：", e);
        }
    }

    /**
     * 设置Area
     *
     * @param result result
     */
    private void setAnalyseArea(AnalyseResultItem result) {
        Area area = new Area();
        List<Point> points = new ArrayList<>();
        points.add(new Point(100, 100));
        points.add(new Point(1820, 980));
        area.setAreas(points);

        result.setPos(Arrays.asList(area));
    }

    /**
     * 将图片复制到本地进行绘制告警框，再从本地上传到ftps
     *
     * @param imageUrl imageUrl
     * @param objectId objectId
     * @param type type
     * @return result
     */
    private String createAnalyseImage(String imageUrl, String objectId, String type) {
        int max=9999,min=1;
        int ran = (int) (Math.random()*(max-min)+min);
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        String filePathTem = formatter.format(new Date())+ ran;

        String analyseImageUrl = String.format("/data/sb_output/%s_%s.jpg", objectId, filePathTem);
        String localPath = String.format("/home/yjh_iot_center/iot-picture/resultImg/%s_%s.jpg", objectId, filePathTem);
        ftpsService.downloadAnalysisFile(imageUrl, localPath);

        // 绘制告警图像
        pictureWaterMark(localPath, type);

        ftpsService.uploadAnalysisFile(analyseImageUrl, localPath);
        return analyseImageUrl;
    }

    /**
     * 给图片设置水印和告警框
     *
     * @param filePath         图片地址
     * @param waterMarkContent 水印内容
     */
    private void pictureWaterMark(String filePath, String waterMarkContent) {
        log.info("给图片设置水印和告警框和水印文字：filePath: {}, waterMarkContent: {}", filePath, waterMarkContent);
        try {
            File file = new File(filePath);
            BufferedImage image = ImageIO.read(file);
            //获取图片的宽
            waterMarkWrite(image, waterMarkContent, file);
        } catch (Exception e) {
            log.error("图片设置水印错误: ", e);
        }
    }

    /**
     * 给图片设置水印和告警框
     *
     * @param image image
     * @param waterMarkContent waterMarkContent
     * @param file file
     * @throws IOException IOException
     */
    private void waterMarkWrite(BufferedImage image, String waterMarkContent, File file) throws IOException {
        String suffix = StringUtils.substringAfterLast(file.getName(), ".");
        //获取图片的宽
        int srcImgWidth = image.getWidth();
        //获取图片的高
        int srcImgHeight = image.getHeight();

        // 创建画笔
        Graphics2D pen = image.createGraphics();
        // 设置画笔颜色
        pen.setColor(Color.blue);
        // 设置画笔字体样式
        pen.setFont(new Font("微软雅黑", Font.BOLD, 30));

        //设置水印的坐标(为原图片右下角)
        int x = srcImgWidth - getWatermarkLength(waterMarkContent, pen) - 110;
        int y = 90;

        // 写上水印文字和坐标
        pen.drawString(waterMarkContent, x, y);

        pen.setColor(Color.red);
        pen.drawRect(100, 100, srcImgWidth - 200, srcImgHeight - 200);
        try (FileImageOutputStream fos = new FileImageOutputStream(file)){
            ImageIO.write(image, suffix, fos);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 获取水印文字的长度
     *
     * @param waterMarkContent
     * @param g
     * @return
     */
    private static int getWatermarkLength(String waterMarkContent, Graphics2D g) {
        return g.getFontMetrics(g.getFont()).charsWidth(waterMarkContent.toCharArray(), 0, waterMarkContent.length());
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
        log.info("静默监视告警结果存储silentAlarmStore,入参： resultArr： {}， map：{}, resultImgList: {}",
            JSONUtil.toJSONString(resultArr), JSONUtil.toJSONString(map), JSONUtil.toJSONString(resultImgList));

        List<TWarnInfo> list = new ArrayList<>();
        try {
            for (int i = 0; i < resultArr.length; i++) {
                String alarmLevel = analyseDataOperateService.selectAlarmLevel(resultArr[i]);
                TWarnInfo tWarnInfo = new TWarnInfo()
                        .setWarnLevel(ValueUtil.toInteger(alarmLevel,131))
                        .setWarnTime(DateUtil.dateSecond())
                        .setWarnName("静默监视告警数据")
                        .setWarnContent(analyseDataOperateService.resolveDefectResult(resultArr[i]))
                        .setDeviceId(Long.valueOf(String.valueOf(map.get("device_id"))))
                        .setCunstomId(String.valueOf(map.get("custom_id")))
                        .setInstanceId(Long.valueOf(String.valueOf(map.get("instance_id"))))
                        .setStdMeteId(Long.valueOf(String.valueOf(map.get("device_mete_id"))))
                        .setConfMode(276)
                        .setDefectModel(450)
                        .setAlarmSource(689)
                        .setImagePath(resultImgList.get(0));

                if (Objects.nonNull(tWarnInfo.getDeviceId())) {
                    TStdDevice tStdDevice = tStdDeviceService.selectByUnionKeys(tWarnInfo.getDeviceId());
                    if (Objects.nonNull(tStdDevice)) {
                        tWarnInfo.setDeviceName(tStdDevice.getDeviceName());
                    }
                }
                if (Objects.nonNull(tWarnInfo.getStdMeteId())) {
                    TStdDeviceMete tStdDevicemete = tStdDevicemeteService.selectByPrimaryId(tWarnInfo.getStdMeteId());
                    if (Objects.nonNull(tStdDevicemete)) {
                        tWarnInfo.setDeviceMeteName(tStdDevicemete.getMeteName());
                        tWarnInfo.setLabelAttri(tStdDevicemete.getLabelAttri());
                    }
                }
                analyseDataOperateDao.insertWarnInfo(tWarnInfo);

                list.add(tWarnInfo);

                if (!alarmShieldService.isShield(tWarnInfo.getStdMeteId())){
                    //webSocket通知前端调用查询告警弹框的接口
                    Long warnId = analyseDataOperateDao.selectCurrentWarn();
                    Map<String, Object> jasonMaps = new HashMap<>(16);
                    jasonMaps.put("type", "alarmPopUp");
                    jasonMaps.put("warnId", warnId);
                    jasonMaps.put("defectModel", 450);
                    jasonMaps.put("warnLevel", tWarnInfo.getWarnLevel());
                    jasonMaps.put("warnType", "1");
                    String json = JSON.toJSONString(jasonMaps);
                    log.info("发送给前端的消息：{}", json);
                    if (!Constant.isUpSystem()){
                        postUrl(Constant.WEBSOCKET_URL, json);
                    }
                } else {
                log.info("此告警被屏蔽了，不弹窗！");
                }

            }
            return list;
        } catch (Exception e) {
            log.error("组装并存储告警信息异常：", e);
        }
        return null;
    }


    /**
     * 静默监视告警向上级系统上报
     *
     * @param map 巡视点信息
     * @param tWarnInfoList 告警数据
     */
    private void alarmToUpSystem(Map<String, Object> map, List<TWarnInfo> tWarnInfoList) {
        if (!applicationProperties.getUpSystemFtps().isEnable()) {
            return;
        }
        String edgeCode = (String)redisTemplate.opsForHash().entries("t_sys_param:edgeCode").get("content");
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
            String patroldeviceCode = MapUtils.getString(map, "b_camera_channel_id");
            if (StringUtils.isEmpty(patroldeviceCode)) {
                patroldeviceCode = StringUtils.isEmpty(MapUtils.getString(map, "camera_channel_id")) ? MapUtils.getString(map, "camera_id") : MapUtils.getString(map, "camera_channel_id");
            }
            xmlItem.put("patroldevice_code", patroldeviceCode);
            xmlItem.put("patroldevice_name", map.get("camera_name"));
            switch (tWarnInfo.getWarnLevel()){
                case 130:
                case 131:
                    xmlItem.put("alarm_level", "1");
                    break;
                case 132:
                    xmlItem.put("alarm_level", "2");
                    break;
                case 133:
                    xmlItem.put("alarm_level", "3");
                    break;
                default:
                    break;
            }

            try {
                xmlItem.put("monitor_type", "100");
                String[] alarmType = applicationProperties.getIntelAlgorithmConfig().getSilentMonitorNameAndType().split(",");
                for (String str : alarmType) {
                    String[] split =/* new String(*/str/*.getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8)*/.split(":");
                    if (tWarnInfo.getWarnContent().contains(split[0])) {
                        xmlItem.put("monitor_type", split[1]);
                        break;
                    }
                }
                // 目前都是识别图片 所以是5
                xmlItem.put("file_type", "5");
                String imgPath = tWarnInfo.getImagePath().replaceAll(SysParamConfig.getSysContent("defectResultRealImg"),
                    SysParamConfig.getSysContent("defectResultImg"));
                String targetNamePath = imgPath.replace(SysParamConfig.getSysContent("defectResultImg"), "").substring(1);
                String edgeId = SysParamConfig.getSysContent("edgeId");
                targetNamePath = cn.hutool.core.io.FileUtil.normalize(edgeId + "/jm/" + targetNamePath);
                log.info("imgPath:{},targetNamePath:{}",imgPath,targetNamePath);
                uploadFileToUpFtps(imgPath, targetNamePath, applicationProperties.getUpSystemFtps());

                xmlItem.put("file_path", targetNamePath);
                xmlItem.put("time", warnTime);
                xmlItem.put("content", tWarnInfo.getWarnContent());
                xmlItem.put("origin_id", tWarnInfo.getWarnId());
                xmlItem.put("edge_code", edgeCode);
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
        String path = String.valueOf(this.getClass().getClassLoader().getResource(applicationProperties.getAlgorithmServerConfig().getConfFileName()));
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
        if (StringUtils.isEmpty(sourcePath)) {
            return "";
        }
        try {
            String targetPath;
            if (Objects.isNull(type) || Objects.equals("tx_pb",type)){
                // 判别
                targetPath = SysParamConfig.getSysContent("judgeResultImg") + "/" + sourcePath;
            }else {
                List<String> analyseType = analyseDataOperateDao.selectAlgorithmType();
                if (analyseType.contains(type)){
                    // 表计识别(设备状态识别)
                    targetPath = SysParamConfig.getSysContent("meterResultImg") + "/" +sourcePath;
                }else {
                    // 缺陷
                    targetPath = SysParamConfig.getSysContent("defectResultImg") + "/" +sourcePath;
                }
            }
            ftpsService.downloadAnalysisFile(sourcePath, targetPath);
//            // 图片在ftps上的全路径
//            String resultAbsolutePath = SysParamConfig.getSysContent("ftpsFilePath") + "/" +sourcePath;
//            FileUtil.copyFileUsingStream(resultAbsolutePath, targetPath);
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
     * 请求其他服务
     * @param url
     * @return String
     */
    private String getUtl(String url) {
        HttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet();
        httpGet.addHeader("Content-type", "application/json;charset=utf-8");
        httpGet.setHeader("Accept", "application/json");
        try {
            HttpResponse response = client.execute(httpGet);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error("get请求失败", e);
        }
        return "";
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
    private void uploadFileToFtps(String sourcePath, String targetPathName, ApplicationProperties.FtpsConfig ftpsConfig) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, ftpsConfig.getIp(), ftpsConfig.getPort()
                    , ftpsConfig.getUserName(), ftpsConfig.getPassword());
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
    private void uploadFileToUpFtps(String sourcePath, String targetPathName, ApplicationProperties.FtpsConfig upFtpsConfig) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            if ("0".equals(applicationProperties.getUpSystemFtps().getFlag())){
                log.info("上级系统开关未开！ {}",applicationProperties.getUpSystemFtps().getFlag());
                return;
            }
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(), upFtpsConfig.getUserName(), upFtpsConfig.getPassword());
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

        postUrl(Constant.WEBSOCKET_URL, json);
    }

    /**
     * 算法参数获取
     * @param type <1>:=状态类型 <2>:=缺陷类型
     * @return 算法参数
     */
    public Map<String, String> getAlgorithmParams(String type) {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(3);
        map.put("section_id", applicationProperties.getManagerAlgorithmConfig().getSectionId());
        map.put("station_id", SysParamConfig.getSysContent("edgeId"));
        map.put("type", type);
        items.add(map);
        XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("314").setItems(items);
        Result re = Constant.otherObjServer(xmlBaseModel, Constant.TCP_SYNC_CLOUD_URL);
        Map<String, String> prams = new HashMap<>();
        if (Objects.nonNull(re)) {
            List<Map<String, String>> res = Object2List.castListMap(re.getData(), String.class, String.class);
            if (CollectionUtils.isNotEmpty(res)) {
                prams = res.get(0);
                redisTemplate.opsForHash().putAll(ALGORITHM_PARAMS + type, prams);
            }
        }
        return prams;
    }

    /**
     * 算法版本获取接口
     * @param type <1>:=状态类型 <2>:=缺陷类型
     * @param algorithmManufacturer 算法厂商 当值为空时， 代表获取所有厂商的算法历史版本
     * @return 算法版本信息
     */
    public List<Map<String, String>> getAlgorithmVersion(String type, String algorithmManufacturer) {
        List<Map<String,Object>> items = new ArrayList<>();
        Map<String,Object> map = new HashMap<>(4);
        map.put("algorithm_manufacturer", algorithmManufacturer);
        map.put("section_id", applicationProperties.getManagerAlgorithmConfig().getSectionId());
        map.put("station_id", SysParamConfig.getSysContent("edgeId"));
        map.put("type", type);
        items.add(map);
        XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("315").setItems(items);
        Result re = Constant.otherObjServer(xmlBaseModel, Constant.TCP_SYNC_CLOUD_URL);
        List<Map<String, String>> res = new ArrayList<>();
        if (Objects.nonNull(re.getData())) {
            res = Object2List.castListMap(re.getData(), String.class, String.class);
            res.forEach(m -> m.put("type", type));
        }
        return res;
    }

    /**
     * 算法版本切换接口
     * @param type <1>:=状态类型 <2>:=缺陷类型
     * @param version 算法版本号
     */
    public void algorithmVersionChange(String type, String version) {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(4);
        map.put("section_id", applicationProperties.getManagerAlgorithmConfig().getSectionId());
        map.put("station_id", SysParamConfig.getSysContent("edgeId"));
        map.put("version", version);
        map.put("type", type);
        items.add(map);
        XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("316").setItems(items);
        Result re = Constant.otherObjServer(xmlBaseModel, Constant.TCP_SYNC_CLOUD_URL);
        if (Objects.nonNull(re.getData())) {
            List<Map<String, Object>> list = Object2List.castListMap(re.getData(), String.class, Object.class);
            if (CollectionUtils.isNotEmpty(list)) {
                Map<String, Object> res = list.get(0);
                ThreadPoolUtil.PATROL_POOL.addThread(()->{
                    String algorithmManufacturer = String.valueOf(res.get("algorithm_manufacturer"));
                    String recordTime = String.valueOf(res.get("record_time"));
                    String algorithmPath = String.valueOf(res.get("algorithm_path"));
                    log.info("算法切换请求完成, 算法厂商：{}, 算法版本ID：{}, 算法版本记录时间：{}, 算法FTPS路径：{}",
                            algorithmManufacturer, version, recordTime, algorithmPath);
                    String algorithmPathName = StringUtils.substringAfterLast(algorithmPath, "/");
                    //存放到临时文件
                    String localAlgorithmPath = SysParamConfig.getSysContent("tempReflect") + "/" + algorithmPathName;
                    ftpsService.downLoadFile(localAlgorithmPath, algorithmPath);
                    //开始算法更新
                    UpdateRequest request = new UpdateRequest()
                            .setType(type)
                            .setRequestId(String.valueOf(UUID.randomUUID()))
                            .setAlgorithmPath(localAlgorithmPath);
                    algorithmUpdate(request);
                });
            }
        }
    }
}
