package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.thread.InspectionResultThread;
import com.yjh.accessrobot.netty.thread.IsWarnAfterCruiseThread;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Slf4j
@Service
public class InspectionResultHandler implements MessageHandlerStrategy, InitializingBean {

    @Value("${other.webSocketUrl}")
    private String websocketUrl;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    private String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到巡视结果了+++++++++++++++++");

        // Deal with robot task result data
        String robotCode = xmlBaseModel.getSendCode();
        Map<String, String> cruiseResultMap = new HashMap<>(16);
        Map<String, Object> item = xmlBaseModel.getItems().get(0);

        // 2022过检 robot_name -> patroldevice_name
        cruiseResultMap.put("patrolDeviceName", String.valueOf(item.get("patroldevice_name")));
        cruiseResultMap.put("patrolDeviceCode", String.valueOf(item.get("patroldevice_code")));
        cruiseResultMap.put("robotCode",robotCode);
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
        robotService.uploadFile(ftpFilePath, ftpFilePath);

        // 结果文件处理及判断结果是否告警
        resultFileHandler(xmlBaseModel,ftpFilePath,cruiseResultMap);

        log.info("机器人巡视结果数据是：{}" , cruiseResultMap);
        // Start CruiseResultDealThread
        InspectionResultThread cruiseResultDealThread = new InspectionResultThread(cruiseResultMap, redisTemplate, websocketUrl, true);
        TaskExecutePool.getInstance().execute(cruiseResultDealThread);

        String cruiseResultXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true,robotCode));
        byte[] cruiseResultProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, cruiseResultXmlString);
        RobotServerHandler.send(cruiseResultProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);

        resultUpToStation(xmlBaseModel,robotCode);
    }

    /**
     * 对机器人结果文件处理及判断结果是否告警
     * @param xmlBaseModel xml格式的内容
     * @param ftpFilePath  机器人上报结果文件路径
     * @param cruiseResultMap 机器人巡视结果临时Map
     * @return void
     */
    private void resultFileHandler(XMLBaseModel xmlBaseModel,String ftpFilePath,Map<String, String> cruiseResultMap ){
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> isAlarmMap = new HashMap<>(16);
        Map<String, Object> item = xmlBaseModel.getItems().get(0);


        String developAbsoluteUrl = absoluteImgMap.get("content") + "/" + todayTime + "/" + item.get("task_code").toString() + "/";
        String developRelativeUrl = relativeImgMap.get("content") + "/" + todayTime + "/" + item.get("task_code").toString() + "/";
        // 可见光结果、红外fir、音频wav
        String[] sArray = ftpFilePath.split("/");
        String ftpFileName = sArray[sArray.length - 1];
        String temporaryFilePath = filePathMap.get("content") + "/" + ftpFilePath;
        log.info("temporaryFilePath==={}",temporaryFilePath);

        // 红外原图
        if (item.containsKey("origin_file_path")) {
            // 红外原图
            String ftpInfraredOriginPath = item.get("origin_file_path").toString();
            String[] sArray2 = ftpInfraredOriginPath.split("/");
            // 红外原图名称
            String ftpInfraredOriginName = sArray2[sArray2.length - 1];
            String temporaryInfraredOriginPath = filePathMap.get("content") + "/" + ftpInfraredOriginPath;
            // 拷贝原图
            copyFileToDevelop(temporaryInfraredOriginPath, developAbsoluteUrl + "InfraredOrigin");
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

        // 针对2022过检
        boolean flag = item.containsKey("file_path") && !item.containsKey("origin_file_result_path") && !item.containsKey("origin_file_path");
        temporaryMethod(cruiseResultMap, filePathMap, developAbsoluteUrl, developRelativeUrl, ftpFileName, temporaryFilePath, ftpOriginPath, flag);

        String[] sArray2 = ftpOriginPath.split("/");
        // 原图文件名称
        String ftpOriginName = sArray2[sArray2.length - 1];
        String temporaryOriginPath = filePathMap.get("content") + "/" + ftpOriginPath;
        log.info("temporaryOriginPath==={}",temporaryOriginPath);

        String fileType = item.get("file_type").toString();
        // 1.红外 2.可见光 3.音频 4.视频
        if (Objects.equals("1",fileType)) {
            // 拷贝巡视结果图
            copyFileToDevelop(temporaryOriginPath, developAbsoluteUrl + "Infrared");
            // 拷贝fir
            copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "FIR");
            cruiseResultMap.put("relativePath", developRelativeUrl + "Infrared" + "/" + ftpOriginName);
            cruiseResultMap.put("resultPic", developRelativeUrl + "FIR" + "/" + ftpFileName);

            isAlarmMap.put("relativePath", developRelativeUrl + "Infrared" + "/" + ftpOriginName);
        } else if (Objects.equals("2",fileType)) {
            // 拷贝巡视结果图
            copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "CCD");
            // 拷贝原图
            copyFileToDevelop(temporaryOriginPath, developAbsoluteUrl + "BigImg");
            cruiseResultMap.put("relativePath", developRelativeUrl + "CCD" + "/" + ftpFileName);
            cruiseResultMap.put("absolutePath", developAbsoluteUrl + "BigImg" + "/" + ftpOriginName);

            isAlarmMap.put("relativePath", developRelativeUrl + "CCD" + "/" + ftpFileName);
        } else if (Objects.equals("3",fileType)) {
            // 拷贝巡视结果图
            copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "Audio");
            cruiseResultMap.put("relativePath", developRelativeUrl + "Audio" + "/" + ftpFileName);
            cruiseResultMap.put("absolutePath", developAbsoluteUrl + "Audio" + "/" + ftpOriginName);
        }else if (Objects.equals("4", fileType)) {
            // 拷贝巡视结果视频文件
            copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "Video");
            cruiseResultMap.put("relativePath", developRelativeUrl + "Video" + "/" + ftpFileName);
            cruiseResultMap.put("absolutePath", developAbsoluteUrl + "Video" + "/" + ftpOriginName);
        }

        /// 新版的图片处理
        /*String fileType = item.get("file_type").toString();
        String ftpFilePath = item.get("file_path").toString();
        String sArray[] = ftpFilePath.split("/");
        String ftpFileName = sArray[sArray.length - 1];//巡视结果文件名称
        String temporaryFilePath = filePathMap.get(redisValue) + "/" +ftpFilePath;

        String ftpOriginPath = null;
        if (item.containsKey("origin_file_path")){
            ftpOriginPath = item.get("origin_file_path").toString();
        }else {
            ftpOriginPath = item.get("file_path").toString();
        }
        String sArray2[] = ftpOriginPath.split("/");
        String ftpOriginName = sArray2[sArray2.length - 1];//原图文件名称
        String temporaryOriginPath = filePathMap.get(redisValue) + "/" +ftpOriginPath;
        log.info("temporaryOriginPath==="+temporaryOriginPath);

        if ("1".equals(fileType)){//红外
            copyFileToDevelop(temporaryOriginPath,developAbsoluteUrl + "Infrared");//拷贝原图
            copyFileToDevelop(temporaryFilePath,developAbsoluteUrl + "FIR");//拷贝巡视结果图
            cruiseResultMap.put("relativePath",developRelativeUrl + "Infrared" + "/" + ftpOriginName);
            cruiseResultMap.put("absolutePath",developAbsoluteUrl + "Infrared" + "/" + ftpOriginName);
            cruiseResultMap.put("resultPic", developRelativeUrl + "FIR" + "/" +ftpFileName);
        }else if ("2".equals(fileType)){//可见光
            copyFileToDevelop(temporaryFilePath,developAbsoluteUrl + "CCD");//拷贝巡视结果图
            copyFileToDevelop(temporaryOriginPath,developAbsoluteUrl + "BigImg");//拷贝原图
            cruiseResultMap.put("relativePath",developRelativeUrl + "CCD" + "/" + ftpFileName);
            cruiseResultMap.put("absolutePath",developAbsoluteUrl+ "BigImg" + "/"+ ftpOriginName);
        }else if ("3".equals(fileType)){//音频
            copyFileToDevelop(temporaryFilePath,developAbsoluteUrl + "Audio");//拷贝巡视结果图
            cruiseResultMap.put("relativePath",developRelativeUrl + "Audio" + "/" +ftpFileName);
            cruiseResultMap.put("absolutePath",developAbsoluteUrl + "Audio" + "/" + ftpOriginName);
        }*/

        // 只对红外和可见光进行二次分析判断是否产生告警
        isAlarmMap.put("robotCode", xmlBaseModel.getSendCode());
        isAlarmMap.put("taskCode", item.get("task_code").toString());
        isAlarmMap.put("deviceId", item.get("device_id").toString());
        isAlarmMap.put("value", item.get("value").toString());
        // Start IsWarnAfterCruiseThread
        IsWarnAfterCruiseThread isWarnAfterCruiseThread = new IsWarnAfterCruiseThread(isAlarmMap, redisTemplate, websocketUrl);
        TaskExecutePool.getInstance().execute(isWarnAfterCruiseThread);
    }

    /**
     * 27大类、表计、缺陷、判别需要机器人,且需算法分析
     * 这些都是可见光的图片
     * 巡视结果只有file_path字段,没有origin_file_result_path和origin_file_path
     * @param
     * @param cruiseResultMap 机器人巡视结果临时Map
     * @param filePathMap
     * @param developAbsoluteUrl
     * @param developRelativeUrl
     * @param ftpFileName
     * @param temporaryFilePath
     * @param ftpOriginPath
     * @param flag 是否满足条件 即是否为模拟机器人做任务
     * @return void
     */
    private void temporaryMethod(Map<String, String> cruiseResultMap, Map<String, String> filePathMap, String developAbsoluteUrl, String developRelativeUrl, String ftpFileName, String temporaryFilePath, String ftpOriginPath, boolean flag) {
        if (Boolean.FALSE.equals(flag)) {
            return;
        }
        // 机器人上传至ftp的文件路径
        String temporaryOriginPath = filePathMap.get("content") + "/" + ftpOriginPath;
        // 将机器人上传在ftp文件夹的图片复制到算法需要的目录

        // 组装算法服务需要的信息  调用算法服务









        // 剩下的结果存储就交给video服务来处理

        /*String temporaryOriginPath = filePathMap.get("content") + "/" + ftpOriginPath;
        String[] sArray2 = ftpOriginPath.split("/");
        // 原图文件名称
        String ftpOriginName = sArray2[sArray2.length - 1];
        // 拷贝巡视结果图
        copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "CCD");
        // 拷贝原图
        copyFileToDevelop(temporaryOriginPath, developAbsoluteUrl + "BigImg");
        cruiseResultMap.put("relativePath", developRelativeUrl + "CCD" + "/" + ftpFileName);
        cruiseResultMap.put("absolutePath", developAbsoluteUrl + "BigImg" + "/" + ftpOriginName);*/
    }

    /**
     * 巡视点结果上报站端
     * @param xmlBaseModel xml格式的内容
     * @param robotCode 机器人唯一标识
     * @return void
     */
    public void resultUpToStation(XMLBaseModel xmlBaseModel,String robotCode){
        Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("Robot_SPAndIN_Info:" + robotCode + ":" + xmlBaseModel.getCode());
        String instanceId = redisInfoMap.get("instanceId");
        Map<String, String> mapForGet = redisTemplate.opsForHash().entries("t_cruise_task_result:" + xmlBaseModel.getCode() + ":" + instanceId);
        xmlBaseModel.getItems().get(0).put("material_id", mapForGet.get("realCode"));
        xmlBaseModel.getItems().get(0).put("data_type", mapForGet.get("0x02"));
        xmlBaseModel.getItems().get(0).put("patroldevice_code", instanceId);
//          SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
//          xmlBaseModel.getItems().get(0).put("taskPatrolledId",mapForGet.get("taskId")+"_"+simpleDateFormat2.format(mapForGet.get("cruiseTime")));
        xmlBaseModel.getItems().get(0).remove("robot_code");
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        Map<String, List<XMLBaseModel>> cruiseResult = new HashMap<>(3);
        cruiseResult.put("list", list);
        log.info("信息上报：-" + cruiseResult);
        // 江苏要求
//        Constant.otherServer(cruiseResult,Constant.TCP_URL);
        // 国网要求
        robotService.upToCruise(xmlBaseModel);
    }

    /**
     * 将ftp服务器上的文件复制到开发环境
     */
    public static void copyFileToDevelop(String source,String aim){
        File ff=new File(aim);
        if (!ff.exists()){
            ff.setWritable(true, false);
            ff.mkdirs();
        }
        try {
            String url = "cp " + source + " "+aim;
            Runtime.getRuntime().exec(url);
        }catch (Exception e){
            e.getMessage();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.INSPECTION_RESULT.getCode(), this);
    }
}
