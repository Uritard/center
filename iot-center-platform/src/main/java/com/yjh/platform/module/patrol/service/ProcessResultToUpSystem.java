package com.yjh.platform.module.patrol.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.patrol.entity.TWarnInfo;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * 将各类结果上报上级系统
 *
 * @author 丫C
 * @date 2022/5/31
 */
@Service
public class ProcessResultToUpSystem {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;

    private static final String CCD_PATH = "/CCD/";
    private static final String FIR_PATH = "/FIR/";
    private static final String AUDIO_PATH = "/Audio/";

    private final Logger log = LoggerFactory.getLogger(ProcessResultToUpSystem.class);

    /**
     * 告警或结果上报站端
     *
     * @param analyseType 分析类型
     * @param cruiseResultMap 巡视结果map
     * @param alarmLevel 告警等级
     * @param tWarnInfo 告警信息
     */
    @Async
    public XMLBaseModel alarmAndResultToUpSystem(String analyseType, Map<String, String> cruiseResultMap, String alarmLevel, TWarnInfo tWarnInfo){
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> xmlItem = new HashMap<>(16);
        try {
            String taskId = Optional.ofNullable(cruiseResultMap.get("taskId")).orElse("");
            xmlItem.put("patroldevice_code", "巡视设备名称");
            xmlItem.put("patroldevice_name", "巡视设备编码");
            xmlItem.put("task_name", Optional.ofNullable(cruiseResultMap.get("taskName")).orElse(""));
            xmlItem.put("task_code", taskId);
            xmlItem.put("device_name", Optional.ofNullable(cruiseResultMap.get("instanceName")).orElse(""));
            String instanceId = Optional.ofNullable(cruiseResultMap.get("instanceId")).orElse("");
            xmlItem.put("device_id", instanceId);
            xmlItem.put("time", Optional.ofNullable(cruiseResultMap.get("cruiseTime")).orElse(""));
            HashMap<String, String> typeAndPathName = getTypeAndPathName(analyseType);
            xmlItem.put("file_type", typeAndPathName.getOrDefault("fileType", ""));
            xmlItem.put("recognition_type", typeAndPathName.getOrDefault("recognitionType", ""));
            String simpleDateFormat = DateTimeUtil.format3(new Date());
            xmlItem.put("task_patrolled_id", taskId + "_" + simpleDateFormat);

            // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR/设备点位ID_编码_时间.jpg
            String stationCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:stationCode", "content"));
            String tagPath = stationCode + "/" + simpleDateFormat.substring(0,4) + "/" + simpleDateFormat.substring(4,6) + "/" + simpleDateFormat.substring(6,8)
                    + "/" + taskId + typeAndPathName.get("fileNamePath") + instanceId + "_巡视主机编码_" + simpleDateFormat + ".jpg";

            Map<String, String> resMap;
            if (Objects.isNull(tWarnInfo)){
                xmlBaseModel.setType("61");
                resMap = packageCruiseResultInfo(cruiseResultMap, xmlItem, tagPath);
            }else {
                xmlBaseModel.setType("62");
                resMap = packageAlarmInfo(alarmLevel, tWarnInfo, xmlItem, tagPath);
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
     * 根据算法分析类型获取信息
     *
     * @param analyseType 算法分析类型
     * @return Map<String, String>
     */
    private HashMap<String, String> getTypeAndPathName(String analyseType){
        HashMap<String, String> map = new HashMap<>(4);
        // 识别类型、文件类型、文件名命名
        String recognitionType = "";
        String fileNamePath = "";
        String fileType = "";
        switch (analyseType){
            case "1":
            case "2":
            case "3":
                recognitionType = "1";
                fileNamePath = CCD_PATH;
                fileType = "2";
                break;
            case "4":
            case "6":
            case "11":
                recognitionType = "2";
                fileNamePath = CCD_PATH;
                fileType = "2";
                break;
            case "5":
                recognitionType = "6";
                fileNamePath = CCD_PATH;
                fileType = "5";
                break;
            case "7":
                recognitionType = "3";
                fileNamePath = CCD_PATH;
                fileType = "2";
                break;
            case "398":
            case "8":
                recognitionType = "3";
                fileNamePath = CCD_PATH;
                fileType = "5";
                break;
            case "9":
                recognitionType = "4";
                fileNamePath = FIR_PATH;
                fileType = "1";
                break;
            case "10":
                recognitionType = "2";
                fileNamePath = CCD_PATH;
                fileType = "5";
                break;
            case "13":
                recognitionType = "5";
                fileNamePath = AUDIO_PATH;
                fileType = "3";
                break;
            default:
                break;
        }
        map.put("recognitionType", recognitionType);
        map.put("fileNamePath", fileNamePath);
        map.put("fileType", fileType);
        return map;
    }

    /**
     * 组装告警信息
     *
     * @param alarmLevel 告警等级
     * @param tWarnInfo 告警信息
     * @param xmlItem
     * @param tagPath
     * @return Map<String, String>
     */
    private Map<String, String> packageAlarmInfo(String alarmLevel, TWarnInfo tWarnInfo, Map<String, Object> xmlItem, String tagPath) {
        Map<String, String> resultPathMap = new HashMap<>(4);
        String imgPath = tWarnInfo.getImagePath().replaceAll(
                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg","content")),
                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultImg","content")));
        try {
            tagPath = "alarm/" + tagPath;
            xmlItem.put("file_path", tagPath);
            xmlItem.put("alarm_level", Optional.ofNullable(alarmLevel).orElse(""));
            xmlItem.put("alarm_type", "6");
            xmlItem.put("value", Optional.ofNullable(tWarnInfo.getValue()).orElse(""));
            xmlItem.put("unit", "");
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
     * @param xmlItem
     * @param tagPath
     * @return Map<String, String>
     */
    private Map<String, String> packageCruiseResultInfo(Map<String, String> cruiseResultMap, Map<String, Object> xmlItem, String tagPath) {
        Map<String, String> resultPathMap = new HashMap<>(5);
        String imgPath = "";
        String instanceId = cruiseResultMap.get("instanceId");
        String taskId = cruiseResultMap.get("taskId");
        String picPath = String.valueOf(redisTemplate.opsForHash().get(PATROL_TASK_PREFIX + taskId + ":" + instanceId, "picpath"));
        log.info("picPath====={}", picPath);
        // meter-表计 defect-缺陷 panbie-判别
        if (picPath.contains("meter")){
            imgPath = picPath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultImg","content")));
        }else if (picPath.contains("defect")){
            imgPath = picPath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content")));
        }else if (picPath.contains("panbie")){
            imgPath = picPath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultImg","content")));
        }else{
            // 原图
            imgPath = picPath.replaceAll
                    (String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath","content")));
        }
        try {
            xmlItem.put("file_path", tagPath);
            String materialId = analyseDataOperateService.selectMaterialId(Long.valueOf(cruiseResultMap.get("deviceId")));
            xmlItem.put("material_id", Optional.ofNullable(materialId).orElse(""));
            String resultNum = Optional.ofNullable(cruiseResultMap.get("resultNum")).orElse("");
            xmlItem.put("value", resultNum);
            xmlItem.put("unit", "");
            xmlItem.put("value_unit", resultNum);
            xmlItem.put("value_type", "0");
            xmlItem.put("rectangle", Optional.ofNullable(cruiseResultMap.get("rectangle")).orElse(""));
            xmlItem.put("data_type", "0x01");
            xmlItem.put("valid", ArrayUtils.contains(new String[]{"--", "null"}, resultNum) ? "0" : "1");
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
     * @param cruiseResultMap redis的结果
     * @param resultList 缺陷或判别结果
     * @return void
     */
    @Async
    public void defectAndDistinguishToUpSystem(Map<String, String> cruiseResultMap, Set<String> resultList){
        try {
            for (String key : resultList){
                String instanceId = cruiseResultMap.get("instanceId");
                String analyseType = "";
                // 判别告警等级暂定为一般
                String alarmLevel = "2";
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                TWarnInfo tWarnInfo = new TWarnInfo();
                String value = redisInfoMap.get("value");
                if (value.contains("abnormal")){
                    // 判别
                    tWarnInfo.setValue("图像有差异");
                    tWarnInfo.setWarnContent("图像有差异");
                }else {
                    // 缺陷
                    tWarnInfo.setValue(Optional.ofNullable(redisInfoMap.get("defectContent")).orElse(""));
                    tWarnInfo.setWarnContent(Optional.ofNullable(redisInfoMap.get("defectContent")).orElse(""));
                    String defectLevel = redisInfoMap.get("defectContent");
                    switch (defectLevel){
                        case "130":
                            alarmLevel = "1";
                            break;
                        case "131":
                            alarmLevel = "2";
                            break;
                        case "132":
                            alarmLevel = "3";
                            break;
                        case "133":
                            alarmLevel = "4";
                            break;
                        default:
                            break;
                    }
                }
                alarmAndResultToUpSystem(analyseType, cruiseResultMap, alarmLevel, tWarnInfo);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }
}
