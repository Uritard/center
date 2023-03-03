/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.configuration.IntelAnalysisFtpsConfig;
import com.yjh.platform.configuration.IntelligentAlgorithmConfig;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.entity.interlanalysis.AnalyseObject;
import com.yjh.platform.module.patrol.entity.interlanalysis.PicAnalyseRequest;
import com.yjh.platform.module.patrol.entity.interlanalysis.Response;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.AnalyticsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/27
 * @since [产品/模块版本] （可选）
 */
@Component
public class HttpAnalyticsServiceImpl implements AnalyticsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final IntelAnalysisFtpsConfig intelAnalysisFtpsConfig;
    private final IntelligentAlgorithmConfig algorithmConfig;

    private HashOperations<String, String, String> hashOperations;
    private Map<String, List<String>> analyseTypeMap = new HashMap<>(16);

    public HttpAnalyticsServiceImpl(RedisTemplate<String, Object> redisTemplate, IntelAnalysisFtpsConfig intelAnalysisFtpsConfig,
        IntelligentAlgorithmConfig algorithmConfig) {
        this.redisTemplate = redisTemplate;
        this.intelAnalysisFtpsConfig = intelAnalysisFtpsConfig;
        this.algorithmConfig = algorithmConfig;
        this.hashOperations = redisTemplate.opsForHash();
        reloadAnalyseType();
    }

    /**
     * 初始化分析类型对应分析主机分析关系
     */
    public void reloadAnalyseType() {

        analyseTypeMap.put("11", Arrays.asList(algorithmConfig.getDistinguishType().split(",")));
        analyseTypeMap.put("398", Arrays.asList(algorithmConfig.getDefectType().split(",")));
        analyseTypeMap.put("1", Collections.singletonList("meter"));
        analyseTypeMap.put("2", Collections.singletonList("meter"));
        analyseTypeMap.put("3", Collections.singletonList("meter"));
        analyseTypeMap.put("5", Collections.singletonList("switch"));
        analyseTypeMap.put("6", Collections.singletonList("isolator"));
        analyseTypeMap.put("8", Collections.singletonList("qrcode"));
        analyseTypeMap.put("9", Collections.singletonList("infrared"));
        analyseTypeMap.put("10", Collections.singletonList("switch"));
        analyseTypeMap.put("13", Collections.singletonList("sound"));
        analyseTypeMap.put("12", Arrays.asList(algorithmConfig.getSilentMonitorType().split(",")));
    }

    /**
     * 表计算法分析
     *
     * @param analysisList 分析参数
     * @return 返回结果
     */
    @Override
    public Result analytics(List<Analysis> analysisList) {
        List<Response> responseList = new ArrayList<>();
        List<PicAnalyseRequest> list = formatTransition(analysisList);
        int code = ResultCodeEnum.NORMAL.getCode();
        for (PicAnalyseRequest request : list) {
            Response response = picAnalyse(request);
            responseList.add(response);
            if (200 != response.getCode()) {
                code = response.getCode();
            }
        }
        Result result = new Result(responseList);
        result.setCode(code, 200 == code ? "SUCCESS" : "ERROR");
        return result;
    }

    private List<PicAnalyseRequest> formatTransition(List<Analysis> analysisList) {
        List<PicAnalyseRequest> list = new ArrayList<>();
        for (Analysis analysis : analysisList) {
            // 如果是静默监视识别 只要求识别越线闯入、未穿工装和未带安全帽
            if (Objects.equals("12", analysis.getAnalyseType())) {
                AnalyseObject analyseObject = new AnalyseObject();
                analyseObject.setTypeList(Collections.singletonList("yxcr"));
                list.add(packagePicAnalyseRequest(analysis, analyseObject));

                AnalyseObject analyseObject2 = new AnalyseObject();
                analyseObject2.setTypeList(Arrays.asList("wcaqm", "wcgz"));
                list.add(packagePicAnalyseRequest(analysis, analyseObject2));
            } else {
                AnalyseObject analyseObject = setAnalyseObject(analysis);
                list.add(packagePicAnalyseRequest(analysis, analyseObject));
            }
        }
        return list;
    }

    /**
     * 巡视主机请求图像分析--接口
     *
     * @param picAnalyseRequest 参数
     * @return Response
     */
    public Response picAnalyse(PicAnalyseRequest picAnalyseRequest) {

        JSONObject testJson = (JSONObject)JSONObject.toJSON(picAnalyseRequest);
        log.info(JSONUtil.prettyJSONString(testJson));

        try {
            String result = HttpClientUtils.getInstance().postUrl(algorithmConfig.getAnalysisUrl(), testJson.toJSONString());
            log.info("result==={}", result);

            if (StringUtils.isEmpty(result)) {
                return Response.basRequest();
            }
            JSONObject jsonObject = JSON.parseObject(result);
            int code = jsonObject.getIntValue("code");
            return new Response(code);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Response.serverError();
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

        String analyseType = analysis.getAnalyseType();
        try {
            if ("11".equals(analyseType)) {

                String imageNormalUrlPath = "";
                String targetNamePath;
                // 判别基准图
                String flag = hashOperations.get("t_sys_param:isEPRI", "content");
                if (StringUtils.equals("false", flag)) {
                    // 拿提前拍好的预置位作为判别基准图
                    imageNormalUrlPath = analysis.getReferenceImage();
                    log.info("拿提前拍好的预置位作为判别基准图:{}", imageNormalUrlPath);
                    String[] split = imageNormalUrlPath.split("/");
                    targetNamePath = split[split.length - 3] + "/" + split[split.length - 2] + "/" + split[split.length - 1];
                    analyseObject.setImageNormalUrlPath(targetNamePath);

                } else {
                    // 拿电科院给的图
                    log.info("flag:{}", flag);
                    String devicePointId = analysis.getDevicePointId();
                    String filePath = hashOperations.get("t_sys_param:distinguishReferencePath", "content");
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
                    log.info("拿电科院给的图:{}", imageNormalUrlPath);
                    String[] split = imageNormalUrlPath.split("/");
                    targetNamePath = split[split.length - 3] + "/" + split[split.length - 2] + "/" + split[split.length - 1];
                    analyseObject.setImageNormalUrlPath(targetNamePath);
                }
                // 上传基准图
                uploadFileToFtps(imageNormalUrlPath, "/" + targetNamePath, intelAnalysisFtpsConfig);
            } else {
                String presetId = StringUtils.substringAfterLast(analysis.getPicModelPath(), "/");
                analyseObject.setImageNormalUrlPath(presetId);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        List<String> typeList = analyseTypeMap.getOrDefault(analyseType, new ArrayList<>());
        typeList = processDefectAnalyseType(analyseType, typeList);
        analyseObject.setTypeList(typeList);

        return analyseObject;
    }

    /**
     * 特殊处理AI判别，优先获取redis上的值
     *
     * @param analyseType analyseType
     * @param typeList typeList
     */
    private List<String> processDefectAnalyseType(String analyseType, List<String> typeList) {
        if (StringUtils.isNotEmpty(analyseType) && "11".equals(analyseType)) {
            String defectAnalyseType = (String)redisTemplate.opsForHash().get("t_sys_param:defectAnalyseType", "content");
            if (StringUtils.isNotEmpty(defectAnalyseType)) {
                log.info("特殊处理AI判别，优先获取redis上的值: {}", defectAnalyseType);
                return Arrays.asList(defectAnalyseType);
            }
        }

        return typeList;
    }

    /**
     * 组装要发给智能分析主机图像分析接口的请求参数
     *
     * @param analysis      算法信息
     * @param analyseObject 点位具体信息
     * @return PicAnalyseRequest
     */
    private PicAnalyseRequest packagePicAnalyseRequest(Analysis analysis, AnalyseObject analyseObject) {
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
            String targetNamePath = split[split.length - 2] + "/" + split[split.length - 1];
            uploadFileToFtps(analysis.getPicPath(), "/" + targetNamePath, intelAnalysisFtpsConfig);

            imageUrlList.add(targetNamePath);
            analyseObject.setImageUrlList(imageUrlList);

            objectList.add(analyseObject);
            picAnalyseRequest.setObjectList(objectList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return picAnalyseRequest;
    }

    /**
     * 缺陷和判别算法
     *
     * @param analysisList 分析参数
     * @return 返回结果
     */
    @Override
    public Result defect(List<Analysis> analysisList) {
        List<Response> responseList = new ArrayList<>();
        List<PicAnalyseRequest> list = formatTransition(analysisList);
        int code = ResultCodeEnum.NORMAL.getCode();
        for (PicAnalyseRequest request : list) {
            Response response = picAnalyse(request);
            responseList.add(response);
            if (200 != response.getCode()) {
                code = response.getCode();
            }
        }

        Result result = new Result(responseList);
        result.setCode(code, 200 == code ? "SUCCESS" : "ERROR");
        return result;
    }

    /**
     * 将文件上传至巡视主机ftp服务器
     *
     * @param sourcePath     源文件地址
     * @param targetPathName 目标文件地址名称
     */
    private void uploadFileToFtps(String sourcePath, String targetPathName, IntelAnalysisFtpsConfig ftpsConfig) {
        try {
            if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {
                return;
            }
            FtpsUtil.putFile(sourcePath, targetPathName, ftpsConfig.getIp(), ftpsConfig.getPort(), ftpsConfig.getKeypw(),
                ftpsConfig.getUsername(), ftpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至巡视主机ftp服务器错误:", e);
        }
    }

    @Override
    public void afterPropertiesSet() {
        AbstractVideoCruise.AnalyticsFactory.registerAnalytics(CruiseConstant.AnalyticsEnum.HTTP, this);
    }
}
