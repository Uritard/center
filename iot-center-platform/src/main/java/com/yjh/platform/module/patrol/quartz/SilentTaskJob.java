package com.yjh.platform.module.patrol.quartz;

import com.yjh.commons.DateUtils;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.entity.interlanalysis.Response;
import com.yjh.platform.module.patrol.service.IntelAnalysisService;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.video.service.CameraConService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: lqh
 * 静默任务
 * @Date: 2023/01/31
 */
@Slf4j
public class SilentTaskJob implements Runnable {

    private TCameraPresetDao tCameraPresetDao;

    private RedisTemplate redisTemplate;

    private IntelAnalysisService intelAnalysisService;

    private Integer presetType;

    private ApplicationProperties applicationProperties;

    private String stationCode;

    private Integer waitTime;

    private CameraConService cameraConService;

    public SilentTaskJob(TCameraPresetDao tCameraPresetDao,
                         RedisTemplate redisTemplate,
                         IntelAnalysisService intelAnalysisService,
                         Integer presetType,
                         ApplicationProperties applicationProperties,
                         String stationCode,
                         Integer waitTime, CameraConService cameraConService){
        this.tCameraPresetDao = tCameraPresetDao;
        this.redisTemplate = redisTemplate;
        this.intelAnalysisService = intelAnalysisService;
        this.presetType = presetType;
        this.applicationProperties = applicationProperties;
        this.stationCode = stationCode;
        this.waitTime = waitTime;
        this.cameraConService = cameraConService;
    }

    private static final String MSG = "success";
    private static final String FLAG = "false";

    @Override
    public void run() {
        // 静默任务开关
        String silentFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isSilentTask", "content"));
        if (StringUtils.equals("false", silentFlag)) {
            log.info("静默任务开关：isSilentTask 没开");
            return;
        }

        log.info("静默任务定时任务==静默监视类型：{}", presetType);
        // 分析主机开关
        String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelDefectAnalysis", "content"));
        if (StringUtils.equals(FLAG, flag) && Constant.isHost()) {
            log.info("巡视主机分析主机开关：isIntelDefectAnalysis 没开");
            return;
        }
        List<TCameraPreset> presetList = tCameraPresetDao.selectCameraByPresetType(presetType);
        presetList.forEach(this::process);

    }

    private void process(TCameraPreset preset) {
        long cameraId = preset.getCameraId();
        long presetId = preset.getPresetId();
        String presetName = preset.getPresetName();

        Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("camera_info:" + cameraId);
        String state = redisInfoMap.get("state");
        String lastTime = redisInfoMap.get("lastTime");

//            Integer keepSilent = Integer.parseInt(silentTaskTime.substring(4,5)) * 60 * 1000;
        // 相机状态为闲置(state为0闲置,为1占用)时,做静默任务
        if ("1".equals(state)) {
            log.error("该相机被使用 cameraId:{}",cameraId);
            return;
        }
        int count=tCameraPresetDao.selectCameraPresetInTask(String.valueOf(presetId));
        if(count>0){
            log.error("该摄像机正在任务中 cameraId:{} presetId:{}",cameraId,presetId);
            return;
        }

        Date now = new Date();
        Date lastDate = DateUtils.dateFromString(lastTime);
        if ((now.getTime() - lastDate.getTime()) >= (waitTime*1000) ){
            log.info("cameraId为{},presetId为{}的相机准备做静默任务", cameraId, presetId);
            try {
                cameraConService.moveToPreset(presetId, cameraId);
                // 静默不等待摄像头转到预置位
                // 拍照
                Map<String, String> result = cameraConService.capturePicture("silent", null, cameraId, presetName);
                if (MapUtils.isEmpty(result)) {
                    log.info("抓图失败 result:{}", result);
                    return;
                }
                String absPath = result.get("absPath");
                String edgeLevel = Constant.getLevelEdge();
                log.info("edgeLevel:{}",edgeLevel);
                //如果是边缘节点 上传巡视主机
                String resultImgPath;
                if (Constant.LEVEL_EDGE.equals(edgeLevel)) {
                    resultImgPath = uploadPicture(absPath, String.valueOf(cameraId), String.valueOf(presetId), presetName);
                    //否则调用算法分析
                } else {
                    // 分析
                    resultImgPath = analysePicture(absPath, presetId);
                }
                if (StringUtils.isNotEmpty(resultImgPath)) {
                    Files.delete(Paths.get(resultImgPath));
                }
            } catch (Exception e) {
                log.error("设置摄像机状态出错" + e.getMessage());
                redisInfoMap.put("state", "0");
                redisTemplate.opsForHash().putAll("camera_info:" + cameraId, redisInfoMap);
            }
        }
    }

    /**
     * 拿到相机拍照结果，边缘节点将图片上传至巡检主机
     *
     * @param absPath   相机抓图返回结果
     * @param cameraId 相机id  充当巡检设备编码
     * @param presetId 预置位id  充当巡视点ID
     */
    private String uploadPicture(String absPath, String cameraId, String presetId, String presetName) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //静默数据上送
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> xmlItems = new ArrayList<>();
            Map<String, Object> xmlItem = new HashMap<>();
            xmlBaseModel.setType("64");
            String edgeId = (String) redisTemplate.opsForHash().entries(Constant.T_SYS_PARAM + "edgeId").get("content");
            xmlBaseModel.setCode(edgeId);
            xmlItem.put("patroldevice_code", cameraId);
            xmlItem.put("device_name", presetName);
            xmlItem.put("device_id", presetId);
            xmlItem.put("time", simpleDateFormat.format(new Date()));
            xmlItem.put("rectangle", "");
            xmlItem.put("file_type", "2");

            //上传图片
            String timeFormat = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String edgeCode = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeId").get("content"));
            String ftpsTarPath = edgeCode+"/jm/" + timeFormat.substring(0,4) + "/" + timeFormat.substring(4,6) + "/" + timeFormat.substring(6,8)+"/"+cameraId+"/"+presetId+".jpg";
            FtpsUtil.putFile(absPath, ftpsTarPath, applicationProperties.getUpSystemFtps().getIp(), applicationProperties.getUpSystemFtps().getPort(),
                    applicationProperties.getUpSystemFtps().getUserName(), applicationProperties.getUpSystemFtps().getPassword());
            xmlItem.put("file_path", ftpsTarPath);
            xmlItem.put("monitor_type", "");

            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> cruiseResult = new HashMap<>();
            cruiseResult.put("list", list);

            log.info("信息上报：- " + cruiseResult);
            Constant.otherServer(cruiseResult, Constant.TCP_URL);
        } catch (Exception e) {
            log.error("静默监视异常: ", e);
        }
        return absPath;
    }

    /**
     * 根据相机拍照结果发送算法进行分析
     *
     * @param absPath   相机抓图返回结果
     * @param presetId 预置位id  充当巡视点ID
     */
    private String analysePicture(String absPath, Long presetId) {
        // 调用算法接口分析结果
        List<Analysis> analysisList = new ArrayList<>();
        Analysis analysis = new Analysis()
                // 暂定静默监视识别类型为12,没有实际意义
                .setAnalyseType("12")
                .setInstanceId(presetId)
                .setTaskId("jm")
                .setPicPath(absPath);
        analysisList.add(analysis);
        List<Response> responseList= intelAnalysisService.picAnalyseNoDetection(analysisList);
        log.info("param:{} result:{}",StringUtils.join(analysisList),StringUtils.join( responseList));
        return absPath;
    }

}
