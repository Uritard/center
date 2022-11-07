package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.task.entity.TWarnInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * 将各类结果上报上一级系统
 *
 * @author 丫C
 * @date 2022/5/31
 */
@Service
public class ProcessResultToUpSystem {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AnalyseDataOperateDao analyseDataOperateDao;
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;

    private static final String CCD_PATH = "/CCD/";
    private static final String FIR_PATH = "/FIR/";
    private static final String AUDIO_PATH = "/Audio/";

    private final Logger log = LoggerFactory.getLogger(ProcessResultToUpSystem.class);

    /**
     * 告警或结果上报上一级系统
     *
     * @param cruiseResultMap 巡视结果map
     * @param alarmLevel 告警等级
     * @param tWarnInfo 告警信息
     */
    @Async
    public XMLBaseModel alarmAndResultToUpSystem(Map<String, String> cruiseResultMap, String alarmLevel, TWarnInfo tWarnInfo){
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> xmlItem = new HashMap<>(16);
        try {
            String taskId = Optional.ofNullable(cruiseResultMap.get("taskId")).orElse("");
            String instanceId = Optional.ofNullable(cruiseResultMap.get("instanceId")).orElse("");
            String simpleDateFormat = DateTimeUtil.format3(new Date());
            String analyseType = getAlgorithmTypeMap(instanceId);
            HashMap<String, String> typeAndPathName = getTypeAndPathName(analyseType);

            xmlItem.put("patroldevice_code", "巡视设备名称");
            xmlItem.put("patroldevice_name", "巡视设备编码");
            xmlItem.put("task_name", Optional.ofNullable(cruiseResultMap.get("taskName")).orElse(""));
            xmlItem.put("task_code", taskId);
            xmlItem.put("device_name", Optional.ofNullable(cruiseResultMap.get("instanceName")).orElse(""));
            xmlItem.put("device_id", instanceId);
            xmlItem.put("time", Optional.ofNullable(cruiseResultMap.get("cruiseTime")).orElse(""));
            xmlItem.put("file_type", typeAndPathName.getOrDefault("fileType", ""));
            xmlItem.put("recognition_type", typeAndPathName.getOrDefault("recognitionType", ""));
            xmlItem.put("task_patrolled_id", taskId + "_" + simpleDateFormat);

            // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR/设备点位ID_编码_时间.jpg
            String stationCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:stationCode", "content"));
            String tagPath = stationCode + "/" + simpleDateFormat.substring(0,4) + "/" + simpleDateFormat.substring(4,6) + "/" + simpleDateFormat.substring(6,8)
                    + "/" + taskId + typeAndPathName.get("fileNamePath") + instanceId + "_巡视主机编码_" + simpleDateFormat + ".jpg";

            Map<String, String> resMap;
            if (Objects.isNull(tWarnInfo)){
                xmlBaseModel.setType("61");
                resMap = packageCruiseResultInfo(taskId, instanceId, cruiseResultMap, xmlItem, tagPath);
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
            log.info("往上一级准备上报的信息是==={}", map);
            Constant.otherServer(map, Constant.TCP_URL);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return xmlBaseModel;
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
        String imagePath = tWarnInfo.getImagePath();
        if (imagePath.contains("meter")){
            imagePath = imagePath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultImg","content")));
        }else if (imagePath.contains("ftpImg")){
            imagePath = imagePath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageRelative","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute","content")));
        }

        try {
            String recognitionType = String.valueOf(xmlItem.get("recognition_type"));
            String alarmType = "";
            switch (recognitionType){
                case "1":
                    alarmType = "7";
                    break;
                case "2":
                    alarmType = "10";
                    break;
                case "3":
                    alarmType = "6";
                    break;
                case "4":
                    alarmType = "1";
                    break;
                default:
                    break;
            }
            tagPath = "alarm/" + tagPath;
            xmlItem.put("file_path", tagPath);
            // 1-预警 2-一般 3-严重 4-危急
            xmlItem.put("alarm_level", Optional.ofNullable(alarmLevel).orElse(""));
            xmlItem.put("alarm_type", alarmType);
            xmlItem.put("value", Optional.ofNullable(tWarnInfo.getValue()).orElse(""));
            xmlItem.put("unit", "");
            xmlItem.put("value_unit", Optional.ofNullable(tWarnInfo.getValue()).orElse(""));
            xmlItem.put("content", Optional.ofNullable(tWarnInfo.getWarnContent()).orElse(""));
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        resultPathMap.put("imgPath", imagePath);
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
    private Map<String, String> packageCruiseResultInfo(String taskId, String instanceId, Map<String, String> cruiseResultMap, Map<String, Object> xmlItem, String tagPath) {
        Map<String, String> resultPathMap = new HashMap<>(5);
        String imgPath = "";
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
            imgPath = picPath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath","content")));
        }
        try {
            String materialId = analyseDataOperateService.selectMaterialId(Long.valueOf(cruiseResultMap.get("deviceId")));
            String resultNum = Optional.ofNullable(cruiseResultMap.get("resultNum")).orElse("");

            String cruiseType = Optional.ofNullable(cruiseResultMap.get("cruiseType")).orElse("");
            CruiseConstant.TypeEnum cruiseTypeEnum = CruiseConstant.TypeEnum.getEnum(NumberUtils.toInt(cruiseType));
            switch (cruiseTypeEnum){
                case VIDEO:
                case INFRARED:
                    cruiseType = "0x01";
                    break;
                case ROBOT:
                    cruiseType = "0x02";
                    break;
                case UAV:
                    cruiseType = "0x03";
                    break;
                case VOICE:
                    cruiseType = "0x04";
                    break;
                case ONLINE:
                    cruiseType = "0x05";
                    break;
                default:
                    break;
            }

            xmlItem.put("file_path", tagPath);
            xmlItem.put("material_id", Optional.ofNullable(materialId).orElse(""));
            xmlItem.put("value", resultNum);
            xmlItem.put("unit", "");
            xmlItem.put("value_unit", resultNum);
            xmlItem.put("value_type", "0");
            xmlItem.put("rectangle", Optional.ofNullable(cruiseResultMap.get("rectangle")).orElse(""));
            xmlItem.put("data_type", cruiseType);
            xmlItem.put("valid", ArrayUtils.contains(new String[]{"--", "null"}, resultNum) ? "0" : "1");
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        resultPathMap.put("imgPath", imgPath);
        resultPathMap.put("tagPath", tagPath);
        return resultPathMap;
    }

    /**
     * 缺陷及判别结果上报上一级系统
     *
     * @param cruiseResultMap redis的结果
     * @param resultList 缺陷或判别结果
     * @return void
     */
    @Async
    public void defectAndDistinguishToUpSystem(Map<String, String> cruiseResultMap, Set<String> resultList){
        try {
            for (String key : resultList){
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
                alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, tWarnInfo);
            }
        }catch (Exception e){
            log.error("缺陷及判别结果上报站端异常：" , e);
        }
    }
}
