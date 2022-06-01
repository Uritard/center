package com.yjh.accessvideo.service;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.utils.DateTimeUtil;
import com.yjh.accessvideo.module.device.entity.TWarnInfo;
import com.yjh.accessvideo.module.device.entity.XMLBaseModel;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 将各类结果上报上级系统
 *
 * @author 丫C
 * @date 2022/5/31
 */
@Service
public class ProcessResultToUpSystem{

    /**
     * 变电站编码
     */
    @Value("${station.code}")
    private String stationCode;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private final Logger log = LoggerFactory.getLogger(ProcessResultToUpSystem.class);

    /**
     * 告警或结果上报站端
     *
     * @param analyseType 分析类型
     * @param cruiseResultMap 巡视结果map
     * @param cruiseResult 巡视结果map
     * @param alarmLevel 告警等级
     * @param tWarnInfo 告警信息
     */
    @Async
    public XMLBaseModel alarmAndResultToUpSystem(String analyseType, Map<String, String> cruiseResultMap, Map<String, String> cruiseResult, String alarmLevel, TWarnInfo tWarnInfo){
        log.info("cruiseResultMap==={}", cruiseResultMap);
        log.info("cruiseResult==={}", cruiseResult);
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> xmlItem = new HashMap<>(16);
        try {
            xmlItem.put("patroldevice_code", Optional.ofNullable(cruiseResult.get("instanceId")).orElse(""));
            xmlItem.put("patroldevice_name", Optional.ofNullable(cruiseResult.get("instanceName")).orElse(""));
            xmlItem.put("task_name", Optional.ofNullable(cruiseResult.get("taskName")).orElse(""));
            xmlItem.put("task_code", Optional.ofNullable(cruiseResult.get("taskCode")).orElse(""));
            xmlItem.put("device_name", Optional.ofNullable(cruiseResult.get("cruiseName")).orElse(""));
            xmlItem.put("device_id", Optional.ofNullable(cruiseResult.get("device_mete_id")).orElse(""));
            xmlItem.put("time", DateTimeUtil.format(new Date()));
            // 识别类型、文件类型、文件名命名
            String recognitionType = "";
            String fileNamePath = "";
            String fileType = "";
            switch (analyseType){
                case "1":
                case "2":
                case "3": recognitionType = "1"; fileNamePath = "/CCD/"; fileType = "2"; break;
                case "4":
                case "6":
                case "11": recognitionType = "2"; fileNamePath = "/CCD/"; fileType = "2"; break;
                case "5": recognitionType = "6"; fileNamePath = "/CCD/"; fileType = "5"; break;
                case "7": recognitionType = "3"; fileNamePath = "/CCD/"; fileType = "2"; break;
                case "398":
                case "8": recognitionType = "3"; fileNamePath = "/CCD/"; fileType = "5"; break;
                case "9": recognitionType = "4"; fileNamePath = "/FIR/"; fileType = "1"; break;
                case "10": recognitionType = "2"; fileNamePath = "/CCD/"; fileType = "5"; break;
                case "13": recognitionType = "5"; fileNamePath = "/Audio/"; fileType = "3"; break;
                default: break;
            }
            xmlItem.put("file_type", fileType);
            xmlItem.put("recognition_type", recognitionType);
            // 根据相机id获取相机pms编码 （仿照机器人编码）
            String cameraPmS = analyseDataOperateService.selectPMSByCameraId(Long.valueOf(cruiseResult.get("cameraId")));
            String deviceMeteId = cruiseResult.get("device_mete_id");
            String taskId = cruiseResult.get("taskId");
            // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR/设备点位ID_编码_时间.jpg
            String timeFormat = simpleDateFormat.format(new Date());
            String tagPath = stationCode + "/" + timeFormat.substring(0,4) + "/" + timeFormat.substring(4,6) + "/" + timeFormat.substring(6,8)
                    + "/" + taskId + fileNamePath + deviceMeteId + "_" + cameraPmS + "_" + timeFormat + ".jpg";

            xmlItem.put("task_patrolled_id", taskId + "_" + simpleDateFormat.format(simpleDateFormat.parse(cruiseResult.get("cruiseTime"))));
            Map<String, String> resMap = new HashMap<>(5);

            if (Objects.isNull(tWarnInfo)){
                // 巡视结果
                resMap = packageCruiseResultInfo(cruiseResultMap, cruiseResult, xmlBaseModel, xmlItem, tagPath);
            }else {
                // 告警
                resMap = packageAlarmInfo(alarmLevel, tWarnInfo, xmlBaseModel, xmlItem, deviceMeteId, tagPath);
            }
            log.info("imgPath==={},tagPath==={}", resMap.get("imgPath"), resMap.get("tagPath"));
            analyseDataOperateService.uploadFileToUpFtps(resMap.get("imgPath"), "/" + resMap.get("tagPath"));

            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            map.put("list", list);
            log.info("信息上报==={}", map);
            Constant.otherServer(map, Constant.TCP_URL);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return xmlBaseModel;
    }

    /**
     * 组装告警信息
     *
     * @param alarmLevel 告警等级
     * @param tWarnInfo 告警信息
     * @param xmlBaseModel
     * @param xmlItem
     * @param deviceMeteId
     * @param tagPath
     * @return Map<String, String>
     */
    private Map<String, String> packageAlarmInfo(String alarmLevel, TWarnInfo tWarnInfo, XMLBaseModel xmlBaseModel, Map<String, Object> xmlItem, String deviceMeteId, String tagPath) {
        Map<String, String> resultPathMap = new HashMap<>(5);
        String imgPath = tWarnInfo.getImagePath().replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg","content")),
                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultImg","content")));
        try {
            tagPath = "alarm/" + tagPath;
            xmlItem.put("file_path",tagPath);
            xmlBaseModel.setType("62");
            xmlItem.put("alarm_level", Optional.ofNullable(alarmLevel).orElse(""));
            Map<String, Object> info = analyseDataOperateService.selectWarnInfo(NumberUtils.toLong(deviceMeteId));
            log.info("info==={}", info);
            xmlItem.put("alarm_type", Optional.ofNullable(info.get("alarm_type")).orElse(""));
            xmlItem.put("value", Optional.ofNullable(tWarnInfo.getValue()).orElse(""));
            xmlItem.put("unit", Optional.ofNullable(info.get("unit")).orElse(""));
            xmlItem.put("value_unit", tWarnInfo.getValue() + xmlItem.get("unit"));
            xmlItem.put("content", tWarnInfo.getWarnContent());
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        resultPathMap.put("imgPath", imgPath);
        resultPathMap.put("tagPath", tagPath);
        return resultPathMap;
    }

    /**
     * 组装巡视结果信息
     *
     * @param cruiseResultMap
     * @param cruiseResult
     * @param xmlBaseModel
     * @param xmlItem
     * @param tagPath
     * @return Map<String, String>
     */
    private Map<String, String> packageCruiseResultInfo(Map<String, String> cruiseResultMap, Map<String, String> cruiseResult, XMLBaseModel xmlBaseModel, Map<String, Object> xmlItem, String tagPath) {
        Map<String, String> resultPathMap = new HashMap<>(5);
        String imgPath = "";
        String instanceId = cruiseResult.get("instanceId");
        String taskId = cruiseResult.get("taskId");
        String picPath = String.valueOf(redisTemplate.opsForHash().get("t_cruise_task_result:" + taskId + ":" + instanceId, "picpath"));
        log.info("picPath====={}", picPath);
        if (picPath.contains("meter")){
            // 表计
            imgPath = picPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultImg","content")));
        }else if (picPath.contains("defect")){
            // 缺陷
            imgPath = picPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content")));
        }else if (picPath.contains("panbie")){
            // 判别
            imgPath = picPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultImg","content")));
        }else{
            // 原图
            imgPath = picPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath","content")));
        }
        try {
            tagPath = "task/" + tagPath;
            xmlItem.put("file_path",tagPath);
            xmlBaseModel.setType("61");
            xmlItem.put("material_id", Optional.ofNullable(cruiseResult.get("realCode")).orElse(""));
            xmlItem.put("value", "");
            xmlItem.put("unit", "");
            xmlItem.put("value_unit", Optional.ofNullable(cruiseResultMap.get("resultNum")).orElse(""));
            xmlItem.put("value_type", "0");
            xmlItem.put("rectangle", "");
            xmlItem.put("data_type", "0x01");
            String valid = "";
            if ("--".equals(cruiseResultMap.get("resultNum")) || "null".equals(cruiseResultMap.get("resultNum"))) {
                valid = "0";
            } else {
                valid = "1";
            }
            xmlItem.put("valid", valid);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        resultPathMap.put("imgPath", imgPath);
        resultPathMap.put("tagPath", tagPath);
        return resultPathMap;
    }

    /**
     * 缺陷及判别结果上报站端
     *
     * @param jsonObjectResult 算法返回结果
     * @param resultList 缺陷或判别结果
     * @return void
     */
    @Async
    public void defectAndDistinguishToUpSystem(JSONObject jsonObjectResult, Set<String> resultList){
        try {
            String resultImg = jsonObjectResult.getString("analyseResultImg");
            String taskId = jsonObjectResult.getString("taskId");
            String instanceId = jsonObjectResult.getString("instanceId");

            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);

            for (String key : resultList){
                XMLBaseModel xmlBaseModel = new XMLBaseModel();
                List<Map<String, Object>> xmlItems = new ArrayList<>();
                Map<String, Object> xmlItem = new HashMap<>(16);
                xmlBaseModel.setType("62");
                xmlItem.put("patroldevice_code", Optional.ofNullable(cruiseResultMap.get("instanceId")).orElse(""));
                xmlItem.put("patroldevice_name", Optional.ofNullable(cruiseResultMap.get("instanceName")).orElse(""));
                xmlItem.put("task_name", Optional.ofNullable(cruiseResultMap.get("taskName")).orElse(""));
                xmlItem.put("task_code", Optional.ofNullable(cruiseResultMap.get("taskCode")).orElse(""));
                xmlItem.put("device_name", Optional.ofNullable(cruiseResultMap.get("cruiseName")).orElse(""));
                xmlItem.put("device_id", Optional.ofNullable(cruiseResultMap.get("device_mete_id")).orElse(""));
                xmlItem.put("time", DateTimeUtil.format(new Date()));
                // 缺陷属于外观异常告警类型,识别类型为设备外观查看,告警文件类型为识别图片  所以都是固定值
                xmlItem.put("alarm_type", "6");
                xmlItem.put("file_type", "5");
                xmlItem.put("recognition_type", "3");
                xmlItem.put("value", "");
                xmlItem.put("unit", "");
                // 根据相机id获取相机pms编码 （仿照机器人编码）
                String cameraPmS = analyseDataOperateService.selectPMSByCameraId(Long.valueOf(cruiseResultMap.get("cameraId")));
                String deviceMeteId = cruiseResultMap.get("device_mete_id");
                // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD/设备点位ID_编码_时间.jpg
                String timeFormat = simpleDateFormat.format(new Date());
                String tagPath = "alarm/" + stationCode + "/" + timeFormat.substring(0,4) + "/" + timeFormat.substring(4,6) + "/" + timeFormat.substring(6,8)
                        + "/" + taskId + "/CCD/" + deviceMeteId + "_" + cameraPmS + "_" + timeFormat + ".jpg";
                xmlItem.put("file_path", tagPath);
                log.info("imgPath==={},tagPath==={}", resultImg, tagPath);
                analyseDataOperateService.uploadFileToUpFtps(resultImg, "/" + tagPath);
                xmlItem.put("task_patrolled_id", taskId + "_" + simpleDateFormat.format(simpleDateFormat.parse(cruiseResultMap.get("cruiseTime"))));
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                String value = redisInfoMap.get("value");
                if (value.contains("abnormal")){
                    // 判别
                    xmlItem.put("value_unit", "图像有差异");
                    xmlItem.put("content", "图像有差异");
                    // 判别告警等级暂定为一般
                    xmlItem.put("alarm_level", "2");
                }else {
                    // 缺陷
                    xmlItem.put("value_unit", Optional.ofNullable(redisInfoMap.get("defectContent")).orElse(""));
                    xmlItem.put("content", Optional.ofNullable(redisInfoMap.get("defectContent")).orElse(""));
                    String defectLevel = redisInfoMap.get("defectContent");
                    switch (defectLevel){
                        case "130":
                            xmlItem.put("alarm_level", "1");
                            break;
                        case "131":
                            xmlItem.put("alarm_level", "2");
                            break;
                        case "132":
                            xmlItem.put("alarm_level", "3");
                            break;
                        case "133":
                            xmlItem.put("alarm_level", "4");
                            break;
                        default:
                            break;
                    }
                }

                xmlItems.add(xmlItem);
                xmlBaseModel.setItems(xmlItems);
                List<XMLBaseModel> list = new ArrayList<>();
                list.add(xmlBaseModel);
                Map<String, List<XMLBaseModel>> map = new HashMap<>();
                map.put("list", list);
                log.info("信息上报==={}", map);
                try {
                    Constant.otherServer(map, Constant.TCP_URL);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }
}
