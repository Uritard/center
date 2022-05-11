package com.yjh.accessrobot.module.command.controller;

import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.thread.AnalysisResultThread;
import com.yjh.accessrobot.netty.thread.InspectionResultThread;
import com.yjh.accessrobot.netty.thread.NonhomologousWarnThread;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author 丫C
 * @date 2022/5/7
 */
@RestController
@RequestMapping("/test/v1")
@Api(value = "/test", tags = "YC测试")
public class YcTestController {

    @Value("${other.webSocketUrl}")
    private String websocketUrl;

    @Autowired
    RedisTemplate redisTemplate;

    private String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

    private final Logger log = LoggerFactory.getLogger(YcTestController.class);

    @ApiOperation(value = "测试机器人模拟装置上报结果")
    @GetMapping(value = "/testMoNiZhuangZhi")
    public void testMoNiZhuangZhi(@RequestParam(value = "filePath") String filePath,
                                  @RequestParam(value = "robotCode") String robotCode,
                                  @RequestParam(value = "taskName") String taskName,
                                  @RequestParam(value = "deviceId") String deviceId,
                                  @RequestParam(value = "taskCode") String taskCode,
                                  @RequestParam(value = "sendCode") String sendCode,
                                  @RequestParam(value = "fileType") String fileType) {

        List<Map<String,Object>> Items = new ArrayList<>();
        Map<String,Object> map = new HashMap<>(16);
        map.put("robot_code", robotCode);// "E100-001"
        map.put("task_name",taskName );// "ydf211220220506211313"
        map.put("file_path", filePath);// 1/2022/05/07/e63496ffe63c444cb7725bffee5966a2/FIR/E100-00120220507143446_result.jpg
        map.put("patroldevice_code", robotCode);// "E100-001"
        map.put("device_id", deviceId);// "2B07D364183C4163AE664C361DF02B57"
        map.put("patroldevice_name", robotCode);
        map.put("task_code", taskCode);// "e63496ffe63c444cb7725bffee5966a2"
        map.put("valid", "1");
        map.put("device_name", "设备2-红外普测2");
        map.put("unit", "℃");
        map.put("recognition_type", "4");
        map.put("file_type", fileType);// 2  3
        map.put("value_type", "0");
        map.put("value", "32.8");
        map.put("value_unit", "32.86℃");
        map.put("time", "2022-05-06 21:14:18");
        map.put("rectangle", "");
        map.put("task_patrolled_id", "e63496ffe63c444cb7725bffee5966a2_20220507143303");
        Items.add(map);
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode) // "192.168.1.15"
                .setItems(Items);

        Map<String, String> cruiseResultMap = new HashMap<>(16);
        Map<String, Object> item = xmlBaseModel.getItems().get(0);
        cruiseResultMap.put("patrolDeviceName", String.valueOf(item.get("patroldevice_name")));
        cruiseResultMap.put("patrolDeviceCode", String.valueOf(item.get("patroldevice_code")));
        cruiseResultMap.put("robotCode", xmlBaseModel.getSendCode());
        cruiseResultMap.put("taskName", item.get("task_name").toString());
        cruiseResultMap.put("taskCode", item.get("task_code").toString());
        cruiseResultMap.put("deviceName", item.get("device_name").toString());
        cruiseResultMap.put("deviceId", item.get("device_id").toString());
        // 2022过检 新增字段value_type 0:默认值类型 11:局放放电频次 12:局放信号峰值 13:局放信号均值
        cruiseResultMap.put("valueType", item.get("value_type").toString());
        cruiseResultMap.put("value", item.get("value").toString());
        cruiseResultMap.put("valueUnit", item.get("value_unit").toString());
        cruiseResultMap.put("unit", item.get("unit").toString());
        cruiseResultMap.put("time", item.get("time").toString());
        cruiseResultMap.put("recognitionType", item.get("recognition_type").toString());
        cruiseResultMap.put("fileType", item.get("file_type").toString());
        cruiseResultMap.put("rectangle", item.get("rectangle").toString());
        cruiseResultMap.put("taskPatrolledId", item.get("task_patrolled_id").toString());
        if (Objects.nonNull(item.get("valid"))) {
            cruiseResultMap.put("valid", item.get("valid").toString());
        }
        String ftpFilePath = item.get("file_path").toString();
        Map<String, Object> mapsss =  resultFileHandler(xmlBaseModel, ftpFilePath, cruiseResultMap);

        log.info("机器人巡视结果数据是：{}" , cruiseResultMap);

        if (Boolean.TRUE.equals(mapsss.get("flag"))){
            // Start AnalysisResultThread
            String resultPath = String.valueOf(mapsss.get("temporaryOriginPath"));
            String ftpFileName = String.valueOf(mapsss.get("ftpFileName"));
            AnalysisResultThread analysisResultThread = new AnalysisResultThread(cruiseResultMap, resultPath, ftpFileName, websocketUrl, redisTemplate);
            TaskExecutePool.getInstance().execute(analysisResultThread);
        }else {
            // Start CruiseResultDealThread
            InspectionResultThread cruiseResultDealThread = new InspectionResultThread(cruiseResultMap, redisTemplate, websocketUrl, true);
            TaskExecutePool.getInstance().execute(cruiseResultDealThread);

            //非同源告警处理
            NonhomologousWarnThread nonhomologousWarnThread = new NonhomologousWarnThread(cruiseResultMap, redisTemplate, websocketUrl,1);
            TaskExecutePool.getInstance().execute(nonhomologousWarnThread);
        }
    }

    private Map<String, Object> resultFileHandler(XMLBaseModel xmlBaseModel, String ftpFilePath, Map<String, String> cruiseResultMap ){
        Map<String, Object> temporaryMap= new HashMap<>(16);
        Map<String, String> isAlarmMap = new HashMap<>(16);

        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, Object> item = xmlBaseModel.getItems().get(0);

        try {
            String developAbsoluteUrl = absoluteImgMap.get("content") + "/" + todayTime + "/" + item.get("task_code").toString() + "/";
            String developRelativeUrl = relativeImgMap.get("content") + "/" + todayTime + "/" + item.get("task_code").toString() + "/";
            // 可见光结果、红外fir、音频wav
            String[] sArray = ftpFilePath.split("/");
            String ftpFileName = sArray[sArray.length - 1];
            String temporaryFilePath = filePathMap.get("content") + "/" + ftpFilePath;
            log.info("temporaryFilePath==={}",temporaryFilePath);

            String fileType = item.get("file_type").toString();
            // 红外原图
            if (item.containsKey("origin_file_path") && Objects.equals("1",fileType)) {
                // 红外原图
                String ftpInfraredOriginPath = item.get("origin_file_path").toString();
                String[] sArray2 = ftpInfraredOriginPath.split("/");
                // 红外原图名称
                String ftpInfraredOriginName = sArray2[sArray2.length - 1];
                String temporaryInfraredOriginPath = filePathMap.get("content") + "/" + ftpInfraredOriginPath;
                // 拷贝原图
                cruiseResultMap.put("absolutePath", developAbsoluteUrl + "InfraredOrigin" + "/" + ftpInfraredOriginName);
            }
            String ftpOriginPath = null;
            // 可见光原图、音频和红外结果
            if (item.containsKey("origin_file_result_path")) {
                // 可见光原图、红外结果
                ftpOriginPath = item.get("origin_file_result_path").toString();
            } else {
                // 可见光原图、音频
                ftpOriginPath = item.get("file_path").toString();
            }

            String[] sArray2 = ftpOriginPath.split("/");
            // 原图文件名称
            String ftpOriginName = sArray2[sArray2.length - 1];
            String temporaryOriginPath = filePathMap.get("content") + "/" + ftpOriginPath;
            log.info("temporaryOriginPath==={}",temporaryOriginPath);

            /*
             * 针对2022过检
             * 27大类、表计、缺陷、判别需要机器人,且需算法分析
             * 这些都是可见光的图片
             * 巡视结果只有file_path字段,没有origin_file_result_path和origin_file_path
             * */
            boolean flag = item.containsKey("file_path") && !item.containsKey("origin_file_result_path") && !item.containsKey("origin_file_path");
            temporaryMap.put("temporaryOriginPath", temporaryOriginPath);
            temporaryMap.put("ftpFileName", ftpFileName);
            temporaryMap.put("flag", flag);

            // 1.红外 2.可见光 3.音频 4.视频
            if (Objects.equals("1",fileType)) {
                // 拷贝巡视结果图
                // 拷贝fir
                cruiseResultMap.put("relativePath", developRelativeUrl + "Infrared" + "/" + ftpOriginName);
                cruiseResultMap.put("resultPic", developRelativeUrl + "FIR" + "/" + ftpFileName);

                isAlarmMap.put("relativePath", developRelativeUrl + "Infrared" + "/" + ftpOriginName);
                isAlarmMap.put("OriginPath",item.get("origin_file_path").toString());
            } else if (Objects.equals("2",fileType) && Boolean.FALSE.equals(flag)) {
                // 拷贝巡视结果图
                // 拷贝原图
                cruiseResultMap.put("relativePath", developRelativeUrl + "CCD" + "/" + ftpFileName);
                cruiseResultMap.put("absolutePath", developAbsoluteUrl + "BigImg" + "/" + ftpOriginName);

                isAlarmMap.put("relativePath", developRelativeUrl + "CCD" + "/" + ftpFileName);
                isAlarmMap.put("OriginPath",item.get("origin_file_path").toString());
            } else if (Objects.equals("3",fileType)) {
                // 拷贝巡视结果图
                cruiseResultMap.put("relativePath", developRelativeUrl + "Audio" + "/" + ftpFileName);
                cruiseResultMap.put("absolutePath", developAbsoluteUrl + "Audio" + "/" + ftpOriginName);
            }else if (Objects.equals("4", fileType)) {
                // 拷贝巡视结果视频文件
                cruiseResultMap.put("relativePath", developRelativeUrl + "Video" + "/" + ftpFileName);
                cruiseResultMap.put("absolutePath", developAbsoluteUrl + "Video" + "/" + ftpOriginName);
            }

        }catch (Exception e){
            log.error("对机器人结果文件处理及判断结果是否告警出现错误:{}", e.getMessage());
        }
        return temporaryMap;
    }
}
