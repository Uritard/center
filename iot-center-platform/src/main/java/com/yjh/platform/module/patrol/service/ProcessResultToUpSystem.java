package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.AlarmService;
import com.yjh.platform.common.mqtt.FtpsService;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.Defect;
import com.yjh.platform.common.mqtt.alarmMsgBody.Different;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.AnalyseDataOperateDao;
import com.yjh.platform.module.patrol.entity.TCruisePointInstance;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.task.entity.TWarnInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.net.ftp.FTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * 将各类结果上报其他系统
 *
 * @author 丫C
 * @date 2022/5/31
 */
@Service
public class ProcessResultToUpSystem {

    private final RedisTemplate redisTemplate;
    private final AnalyseDataOperateDao analyseDataOperateDao;
    private final AnalyseDataOperateService analyseDataOperateService;
    private final FtpsService ftpsservice;
    private final AlarmService alarmService;

    private static final String CCD_PATH = "/CCD/";
    private static final String FIR_PATH = "/FIR/";
    private static final String AUDIO_PATH = "/Audio/";
    private static final String JUDGE = "panbie";
    private static final String DEFECT = "defect";
    private static final String METER = "meter";
    private static final String FTPIMG = "ftpImg";

    private final Logger log = LoggerFactory.getLogger(ProcessResultToUpSystem.class);

    public ProcessResultToUpSystem(RedisTemplate redisTemplate, AnalyseDataOperateDao analyseDataOperateDao, AnalyseDataOperateService analyseDataOperateService, FtpsService ftpsservice, AlarmService alarmService) {
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateDao = analyseDataOperateDao;
        this.analyseDataOperateService = analyseDataOperateService;
        this.ftpsservice = ftpsservice;
        this.alarmService = alarmService;
    }

    public XMLBaseModel alarmAndResultToUpSystem(Map<String, String> cruiseResultMap, String alarmLevel, TWarnInfo tWarnInfo){
        return alarmAndResultToUpSystem(Collections.singletonList(cruiseResultMap), alarmLevel, tWarnInfo);
    }

    /**
     * 告警或结果上报上一级系统
     *
     * @param cruiseResultList 巡视结果map
     * @param alarmLevel 告警等级
     * @param tWarnInfo 告警信息
     */
    @Async
    public XMLBaseModel alarmAndResultToUpSystem(List<Map<String, String>> cruiseResultList, String alarmLevel, TWarnInfo tWarnInfo){
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();

        try {

            String edgeCode = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeCode").get("content"));
            for(Map<String, String> cruiseResultMap : cruiseResultList) {
                log.info("cruiseResultMap=={}", cruiseResultMap);
                Map<String, Object> xmlItem = new HashMap<>(16);
                String taskId = Optional.ofNullable(cruiseResultMap.get("taskId")).orElse("");
                String instanceId = Optional.ofNullable(cruiseResultMap.get("instanceId")).orElse("");
                String simpleDateFormat = DateTimeUtil.format3(new Date());
                String analyseType = getAlgorithmTypeMap(instanceId);
                HashMap<String, String> typeAndPathName = getTypeAndPathName(analyseType);

                Map<String, String> patrolDevice = analyseDataOperateDao.selectPatrolDevice(instanceId);

                xmlItem.put("patroldevice_code", patrolDevice.get("deviceCode"));
                xmlItem.put("patroldevice_name", patrolDevice.get("deviceName"));
                xmlItem.put("task_name", Optional.ofNullable(cruiseResultMap.get("taskName")).orElse(""));
                xmlItem.put("task_code", taskId);
                xmlItem.put("device_name", Optional.ofNullable(cruiseResultMap.get("instanceName")).orElse(""));
                xmlItem.put("device_id", instanceId);
                xmlItem.put("time", Optional.ofNullable(cruiseResultMap.get("cruiseTime")).orElse(""));
                xmlItem.put("file_type", typeAndPathName.getOrDefault("fileType", ""));
                xmlItem.put("recognition_type", typeAndPathName.getOrDefault("recognitionType", ""));
                xmlItem.put("task_patrolled_id", taskId + "_" + simpleDateFormat);

                // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR/设备点位ID_编码_时间.jpg
                String stationCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeId", "content"));
                String tagPath = stationCode + "/" + simpleDateFormat.substring(0, 4) + "/" + simpleDateFormat.substring(4, 6) + "/" + simpleDateFormat.substring(6,
                    8) + "/" + taskId + typeAndPathName.get("fileNamePath") + instanceId + "_"+edgeCode +"_" + simpleDateFormat + ".jpg";

                Map<String, String> resMap;
                if (Objects.isNull(tWarnInfo)) {
                    xmlBaseModel.setType("61");
                    resMap = packageCruiseResultInfo(taskId, instanceId, cruiseResultMap, xmlItem, tagPath);
                } else {
                    xmlBaseModel.setType("62");
                    resMap = packageAlarmInfo(alarmLevel, tWarnInfo, xmlItem, tagPath);
                }
                log.info("imgPath==={},tagPath==={}", resMap.get("imgPath"), resMap.get("tagPath"));
                analyseDataOperateService.uploadFileToUpFtps(resMap.get("imgPath"), "/" + resMap.get("tagPath"));

                xmlItems.add(xmlItem);
            }
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            map.put("list", list);
            log.info("The information to be reported one level up is==={}", map);
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
            if (StringUtils.isEmpty(instanceId)){
                throw new Exception("instanceId is empty,please check with it...");
            }

            // is_ai为on缺陷,is_judge为on判别,algorithm_id非空为表计
            Map<String, Object> algorithmTypeMap = analyseDataOperateDao.selectAlgorithmByInstanceId(NumberUtils.toLong(instanceId));
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
                recognitionType = "3";
                fileNamePath = CCD_PATH;
                fileType = "2";
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
        if (StringUtils.contains(imagePath, METER)){
            imagePath = imagePath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultImg","content")));
        }else if (StringUtils.contains(imagePath, FTPIMG)){
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
        if (StringUtils.contains(picPath, METER)){
            imgPath = picPath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:meterResultImg","content")));
        }else if (StringUtils.contains(picPath, DEFECT)){
            imgPath = picPath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content")));
        }else if (StringUtils.contains(picPath, JUDGE)){
            imgPath = picPath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultRealImg","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultImg","content")));
        }else if (StringUtils.contains(picPath, FTPIMG)){
            imgPath = picPath.replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageRelative","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute","content")));
        }else{
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
        log.info("cruiseResultMap=={},resultList=={}", cruiseResultMap, resultList);
        try {
            for (String key : resultList){
                // 判别告警等级暂定为一般
                String alarmLevel = "2";
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                TWarnInfo tWarnInfo = new TWarnInfo();

                String value = redisInfoMap.get("value");
                if (value.contains("图像有差异")){
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

    /**
     * 缺陷和判别上报算法管理平台
     *
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @param msgId
     */
    public void defectToAlgorithmM(String taskId, String instanceId, String msgId) {
        // msg：判别告警 defect：缺陷告警
        Set<String> differentList= redisScan( "msg:" + msgId);
        Set<String> defectList = redisScan("defect:" + msgId);
        String flag= ftpsservice.getFlag();
        String ftpsRemotePath = ftpsservice.getFtpsRemotePath();

        String nowTime = DateTimeUtil.getDateofFormatString();
        String yearMonth = DateTimeUtil.getMonthDateString();
        if(CollectionUtils.isNotEmpty(differentList) && ("1".equals(flag))){
            log.info("判别告警类型:开始向算法管理平台发送图片和mqtt消息");

            HashMap<String, String> nameMap = analyseDataOperateService.selectDeviceNameInfo(Long.valueOf(instanceId));
            String picF = nowTime + "_" + nameMap.get("upRegionName") + "_" + nameMap.get("deviceName") + "_" + nameMap.get("meteName") + "_";

            // 先取出算法平台返回的resultinfo中的结果图片路径
            String resultImagebak = "analyseResultImg";
            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId+":"+ instanceId);
            String deviceName = Optional.ofNullable(cruiseResultMap.get("deviceName")).orElse("");
            String origPicPath = Optional.ofNullable(cruiseResultMap.get("origpic")).orElse("");

            String remoteorigfilepath = ftpsRemotePath + "/" +"判别"+"/"+yearMonth+"/"+picF+"原图.jpg";
            //,获取结果路径.并拼接算法管理平台对应远程文件路径
            String remotefilepath=ftpsRemotePath + "/" +"判别"+"/"+yearMonth+"/"+picF+"判别告警.jpg";
            //,获取基准路径.并拼接算法管理平台所需要的基准文件路径
            TCruisePointInstance tCruisePointInstance = analyseDataOperateService.selectPointInstance(Long.valueOf(instanceId));
            // 获取巡视点位id
            String cruiseId=String.valueOf(tCruisePointInstance.getCruiseid());
            String judgeBaseImagepath= redisTemplate.opsForHash().get("t_sys_param:presetImgPath","content").toString();
            //判定基准图路径位presetImgPath+巡视点+巡视点.jpg
            judgeBaseImagepath=judgeBaseImagepath+"/"+cruiseId+"/"+cruiseId+".jpg";
            //拼接算法管理平台分析告警结果图片地址
            String remotebaseimagicpath=ftpsRemotePath + "/" +"判别"+"/"+yearMonth+"/"+picF+"判别基准.jpg";

            Iterator it = differentList.iterator();
            List<Different> defectTempList = new ArrayList<>();
            Alarm alarmDetail = new Alarm();
            while (it.hasNext()){
                String key = it.next().toString();
                Map<String, String> differentListMap= redisTemplate.opsForHash().entries(key);
                String resultValue = differentListMap.get("value");
                log.info("判别结果：{}",resultValue);
                Different different = new Different();
                String[] re = resultValue.split(",");
                if(re != null && re.length > 4){
                    different.setX1((int) NumberUtils.toDouble(re[1]));
                    different.setY1((int) NumberUtils.toDouble(re[2]));
                    different.setX2((int) NumberUtils.toDouble(re[3]));
                    different.setY2((int) NumberUtils.toDouble(re[4]));
                }else {
                    different.setX1((int) NumberUtils.toDouble(resultValue));
                    different.setY1(0);
                    different.setX2(0);
                    different.setY2(0);
                }
                defectTempList.add(different);
                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                alarmDetail.setDevice_name(deviceName);
                alarmDetail.setPoint_name(nameMap.get("meteName"));
                alarmDetail.setTime(DateTimeUtil.format(new Date()));
                // 原图
                alarmDetail.setPic_raw(remoteorigfilepath);
                // 判别基准图
                alarmDetail.setPic_diff_base(remotebaseimagicpath);
                // 判别结果图
                alarmDetail.setPic_different(remotefilepath);
                // 缺陷告警图
                alarmDetail.setPic_defect("");
            }
            alarmDetail.setDifferent(defectTempList);
            // 原始图片上传
            ftpsservice.uploadFile("判别告警", origPicPath, remoteorigfilepath);
            // 判别基准图片上传
            ftpsservice.uploadFile("判别告警", judgeBaseImagepath, remotebaseimagicpath);
            // 判别结果图片上传
            ftpsservice.uploadFile("判别告警", resultImagebak, remotefilepath);
            log.info("判别预算法主机origpicpath:{}， remoteorigfilepath:{}", origPicPath, remoteorigfilepath);
            log.info("判别预算法主机judgeBaseImagepath:{}， remotebaseimagicpath:{}", judgeBaseImagepath, remotebaseimagicpath);
            log.info("判别预算法主机resultImagebak:{}， remotefilepath:{}", resultImagebak, remotefilepath);
            //可靠性 文件是否传输成功
            if( !ftpsservice.fileExits(remoteorigfilepath)){
                ftpsservice.uploadFile("判别告警", origPicPath, remoteorigfilepath);
            }
            if( !ftpsservice.fileExits(remotebaseimagicpath)){
                ftpsservice.uploadFile("判别告警", judgeBaseImagepath, remotebaseimagicpath);
            }
            if( !ftpsservice.fileExits(remotefilepath)){
                ftpsservice.uploadFile("判别告警", resultImagebak, remotefilepath);
            }
            alarmService.PushMsg(alarmDetail);
            log.info("判别告警发送算法管理平台结束");

            // 判别上报上一级系统
//            defectAndDistinguishToUpSystem(cruiseResultMap, differentList);
        }

        if(CollectionUtils.isNotEmpty(defectList) && ("1".equals(flag))) {
            log.info("缺陷告警类型:开始向算法管理平台发送图片和mqtt消息");

            HashMap<String, String> nameMap = analyseDataOperateService.selectDeviceNameInfo(Long.valueOf(instanceId));
            String picF = nowTime + "_" + nameMap.get("upRegionName") + "_" + nameMap.get("deviceName") + "_" + nameMap.get("meteName") + "_";

            // 先取出算法平台返回的resultinfo中的结果图片路径
            String resultImagebak = "analyseResultImg";
            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
            String deviceName = Optional.ofNullable(cruiseResultMap.get("deviceName")).orElse("");
            String origPicPath = Optional.ofNullable(cruiseResultMap.get("origpic")).orElse("");

            //拼接算法管理平台原始图片推送地址
            String remoteorigfilepath = ftpsRemotePath + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "原图.jpg";
            //拼接算法管理平台分析告警结果图片地址
            String remotefilepath = ftpsRemotePath + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "缺陷告警.jpg";

            Iterator it = defectList.iterator();
            Alarm alarmDetail = new Alarm();
            while (it.hasNext()) {
                String key = it.next().toString();
                Map<String, String> differentListMap = redisTemplate.opsForHash().entries(key);
                // 获取返回的resultvalue值，这个值就是缺陷和判别的x,y位置信息
                String resultinfo = differentListMap.get("value");
                log.info("缺陷结果：{}", resultinfo);
                // 目前格式："wcaqm,1049.0,216.0,1211.0,389.0,0.8829"
                String[] arr1 = resultinfo.split(",");
                List<Defect> defectTempList = new ArrayList<>();
                for (int i = 0; i < arr1.length; ) {
                    Defect defect = new Defect();
                    defect.setX1((int) NumberUtils.toDouble(arr1[i + 1]));
                    defect.setY1((int) NumberUtils.toDouble(arr1[i + 2]));
                    defect.setX2((int) NumberUtils.toDouble(arr1[i + 3]));
                    defect.setY2((int) NumberUtils.toDouble(arr1[i + 4]));
                    defect.setType(arr1[i]);
                    int confidence = (int) (NumberUtils.toDouble(arr1[i + 5]) * 100);
                    defect.setConfidence(confidence);
                    defect.setDesc(differentListMap.get("defectContent") +
                            "(坐标位置 " + defect.getX1() + "," + defect.getY1() + "," + defect.getX2() + "," + defect.getY2() + ";" +
                            "置信度 " + confidence + "%)"
                    );
                    defectTempList.add(defect);
                    i = i + 6;
                }
                alarmDetail.setDefect(defectTempList);
                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                alarmDetail.setDevice_name(deviceName);
                alarmDetail.setPoint_name(nameMap.get("meteName"));
                alarmDetail.setTime(DateTimeUtil.format(new Date()));
                // 原图
                alarmDetail.setPic_raw(remoteorigfilepath);
                // 判别基准图
                alarmDetail.setPic_diff_base("");
                // 判别结果图
                alarmDetail.setPic_different("");
                // 缺陷告警图
                alarmDetail.setPic_defect(remotefilepath);
            }
            // 原始图片上传
            ftpsservice.uploadFile("遥信告警", origPicPath, remoteorigfilepath);
            // 缺陷结果图片上传
            ftpsservice.uploadFile("遥信告警", resultImagebak, remotefilepath);
            if (!ftpsservice.fileExits(remoteorigfilepath)) {
                ftpsservice.uploadFile("遥信告警", origPicPath, remoteorigfilepath);
            }
            if (!ftpsservice.fileExits(remotefilepath)) {
                ftpsservice.uploadFile("遥信告警", resultImagebak, remotefilepath);
            }

            log.info("巡视主机与智能分析主机：origpicpath:{}", origPicPath);
            log.info("巡视主机与智能分析主机：remoteorigfilepath:{}", remoteorigfilepath);
            log.info("巡视主机与智能分析主机：resultImagebak:{}", resultImagebak);
            log.info("巡视主机与智能分析主机：remotefilepath:{}", remotefilepath);
            alarmService.PushMsg(alarmDetail);
            log.info("缺陷告警发送算法管理平台结束");

            // 缺陷上报上一级系统
            defectAndDistinguishToUpSystem(cruiseResultMap, defectList);
        }
    }

    /**
     * Redis数据库批量查询Key值游标
     * @param key redis的key
     * @return Set<String>
     */
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }
}
