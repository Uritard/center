/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.entity.interlanalysis.AnalyseObject;
import com.yjh.platform.module.patrol.entity.interlanalysis.PicAnalyseRequest;
import com.yjh.platform.module.patrol.entity.interlanalysis.Response;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.AnalyticsService;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private final ApplicationProperties applicationProperties;
    private final ApplicationProperties.IntelligentAlgorithmConfig algorithmConfig;
    private final TAlgorithmInfoDao tAlgorithmInfoDao;

    private HashOperations<String, String, String> hashOperations;
    private Map<String, List<String>> analyseTypeMap = new HashMap<>(32);

    public HttpAnalyticsServiceImpl(RedisTemplate<String, Object> redisTemplate, ApplicationProperties applicationProperties,
        TAlgorithmInfoDao tAlgorithmInfoDao) {
        this.redisTemplate = redisTemplate;
        this.applicationProperties = applicationProperties;
        this.tAlgorithmInfoDao = tAlgorithmInfoDao;
        this.algorithmConfig = applicationProperties.getIntelAlgorithmConfig();
        this.hashOperations = redisTemplate.opsForHash();
    }

    /**
     * 初始化分析类型对应分析主机分析关系
     */
    @PostConstruct
    public void reloadAnalyseType() {
        log.info("分析主机配置信息： {}", JSON.toJSONString(algorithmConfig));
        analyseTypeMap.put("11", Arrays.asList(algorithmConfig.getDistinguishType().split(",")));
        analyseTypeMap.put("398", Arrays.asList(algorithmConfig.getDefectType().split(",")));
        analyseTypeMap.put("14", Arrays.asList(algorithmConfig.getPresetCheck().split(",")));

        List<TAlgorithmInfo> tAlgorithmInfoList = this.tAlgorithmInfoDao.select(null, null, null, null, null, null, 1, null);
        if (CollectionUtils.isNotEmpty(tAlgorithmInfoList)) {
            tAlgorithmInfoList.forEach(info -> analyseTypeMap.put(info.getAnalyseType(), Arrays.asList(StringUtils.split(info.getAliasName(), ","))));
        }
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

    public List<PicAnalyseRequest> formatTransition(List<Analysis> analysisList) {
        List<PicAnalyseRequest> list = new ArrayList<>();
        for (Analysis analysis : analysisList) {
            // 如果是静默监视识别 根据数据库配置获取识别类型
            if (Objects.equals("12", analysis.getAnalyseType())) {
                //静默的  判断他是本系统的还是下级
                String recognizeType = tAlgorithmInfoDao.selectRecognizeTypeByPresetId(analysis.getInstanceId());
                AnalyseObject analyseObject = new AnalyseObject();
                analyseObject.setTypeList(Arrays.asList(recognizeType.split(",")));
                list.add(packagePicAnalyseRequest(analysis, analyseObject));

                saveSilentMonitorImageUrlToRedis(analyseObject);
            } else {
                AnalyseObject analyseObject = setAnalyseObject(analysis);
                list.add(packagePicAnalyseRequest(analysis, analyseObject));
            }
        }
        return list;
    }

    /**
     * 静默识别文件保存，用以人工干预识别结果
     */
    private void saveSilentMonitorImageUrlToRedis(AnalyseObject analysis) {
        boolean needManMade = Boolean.parseBoolean(
            (String)redisTemplate.opsForValue().get("t_sys_param.silentMonitorAnalyseResult.needManMade"));

        if (!needManMade) {
            log.info("t_sys_param.silentMonitorAnalyseResult.needManMade is {}", needManMade);
            return;
        }

        String imageUrl = analysis.getImageUrlList().get(0);
        String objectId = analysis.getObjectId();

        redisTemplate.opsForHash().put("silentMonitorImageUrl", objectId, imageUrl);
        redisTemplate.expire("silentMonitorImageUrl", 3, TimeUnit.DAYS);
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
            // 14 表示预置位偏移检测
            boolean presetCheck = "14".equals(analyseType);
            String targetParent = StringUtils.isEmpty(analysis.getTargetParent()) ? "" : analysis.getTargetParent() + "/";
            if ("11".equals(analyseType) || presetCheck) {

                String imageNormalUrlPath = "";
                // 为了能够让算法返回200 且返回分析结果 所以是null
                String targetNamePath = "null";
                // 判别基准图
                String flag = hashOperations.get("t_sys_param:isEPRI", "content");
                if (StringUtils.equals("false", flag) || presetCheck) {
                    // 拿提前拍好的预置位作为判别基准图
                    imageNormalUrlPath = analysis.getReferenceImage();
                    log.info("拿提前拍好的预置位作为判别基准图:{}", imageNormalUrlPath);
                    if (StringUtils.isNotEmpty(imageNormalUrlPath)) {
                        String[] split = imageNormalUrlPath.split("/");
                        targetNamePath = targetParent + split[split.length - 3] + "/" + split[split.length - 2] + "/" + split[split.length - 1];
                    }
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
                    if (StringUtils.isNotEmpty(imageNormalUrlPath)){
                        String[] split = imageNormalUrlPath.split("/");
                        targetNamePath = targetParent + split[split.length - 3] + "/" + split[split.length - 2] + "/" + split[split.length - 1];
                    }
                    analyseObject.setImageNormalUrlPath(targetNamePath);
                }
                // 上传基准图
                uploadFileToFtps(imageNormalUrlPath, "/" + targetNamePath, applicationProperties.getIntelAnalysisFtps());
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

            String targetParent = StringUtils.isEmpty(analysis.getTargetParent()) ? "" : analysis.getTargetParent() + "/";
            String prefixAbsolutePath = hashOperations.get("t_sys_param:prefixAbsolutePath", "content");
            String targetNamePath = targetParent + StringUtils.substringAfter(analysis.getPicPath(), prefixAbsolutePath);
            uploadFileToFtps(analysis.getPicPath(), "/" + targetNamePath, applicationProperties.getIntelAnalysisFtps());

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
    private void uploadFileToFtps(String sourcePath, String targetPathName, ApplicationProperties.FtpsConfig ftpsConfig) {
        try {
            if (CommonUtils.isEmptyOrNullstr(sourcePath) || CommonUtils.isEmptyOrNullstr(targetPathName)) {
                return;
            }
            FtpsUtil.putFile(sourcePath, targetPathName, ftpsConfig.getIp(), ftpsConfig.getPort(),
                ftpsConfig.getUserName(), ftpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至巡视主机ftp服务器错误:", e);
        }
    }

    @Override
    public void afterPropertiesSet() {
        AbstractVideoCruise.AnalyticsFactory.registerAnalytics(CruiseConstant.AnalyticsEnum.HTTP, this);
    }
}
