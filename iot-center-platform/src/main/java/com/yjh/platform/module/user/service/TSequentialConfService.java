package com.yjh.platform.module.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.device.dao.TCfgDeviceDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TCfgDevice;
import com.yjh.platform.module.patrol.service.IntelAnalysisService;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import com.yjh.platform.module.user.dao.TSequentialConfDao;
import com.yjh.platform.module.user.entity.TSequentialConf;
import com.yjh.platform.module.video.FileUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.yjh.platform.common.Constant.redisTemplate;

/**
* @author lqh
* @since 2021-01-21
*/
@Service
public class TSequentialConfService{

    @Autowired
    private ApplicationProperties applicationProperties;
    @Value("${sequential.cameraCapture}")
    private String cameraCapture;

    @Value("${sequential.startRecordVideo}")
    private String startRecordVideo;
    @Value("${sequential.endRecordVideo}")
    private String endRecordVideo;


    @Autowired
    private IntelAnalysisService intelAnalysisService;
    @Autowired
    private TSequentialConfDao tSequentialConfDao;
    @Autowired
    private TCfgDeviceDao tCfgDeviceDao;


    private final Logger log = LoggerFactory.getLogger(TSequentialConfService.class);

    @Transactional(rollbackFor = Exception.class)
    public int add(TSequentialConf tSequentialConf) {
        List<Long> cameraIdList = tSequentialConfDao.selectCameraId();
        if(CollectionUtils.isNotEmpty(cameraIdList) && cameraIdList.contains(tSequentialConf.getCameraId())){
            return -1;
        }
        tSequentialConf.setCfgMeteId(tSequentialConf.getCfgDeviceId());
        if(tSequentialConfDao.selectByPrimaryId(tSequentialConf.getCfgDeviceId()) != null){
           return tSequentialConfDao.update(tSequentialConf);
        }else {
            return this.tSequentialConfDao.add(tSequentialConf);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String cfgDeviceId) {
        return this.tSequentialConfDao.deleteByPrimaryId(cfgDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TSequentialConf tSequentialConf) {
        TSequentialConf old = tSequentialConfDao.selectByPrimaryId(tSequentialConf.getCfgDeviceId());
        if(!old.getCameraId().equals(tSequentialConf.getCameraId())){
            List<Long> cameraIdList = tSequentialConfDao.selectCameraId();
            if(cameraIdList != null && cameraIdList.size() >0 && cameraIdList.contains(tSequentialConf.getCameraId())){
                return -1;
            }
        }
        tSequentialConf.setCfgMeteId(tSequentialConf.getCfgDeviceId());
        return this.tSequentialConfDao.update(tSequentialConf);
    }

    @Transactional(rollbackFor = Exception.class)
    public TSequentialConf selectByPrimaryId(String cfgDeviceId) {
        return this.tSequentialConfDao.selectByPrimaryId(cfgDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TSequentialConf> select(String cfgDeviceId, String cfgMeteId, Long presetId, String identifyResult) {
        List<TSequentialConf> tSequentialConfList = tSequentialConfDao.select(cfgDeviceId, cfgMeteId, presetId, identifyResult);
        return tSequentialConfList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TSequentialConf> selectByPage(String cfgDeviceName) {
        List<TSequentialConf> tSequentialConfList = tSequentialConfDao.selectByPage(cfgDeviceName);
        return tSequentialConfList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TSequentialConf> list) {
        return this.tSequentialConfDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String cfgDeviceId) {
    List<String> list1= Arrays.asList(cfgDeviceId.split(","));
    return this.tSequentialConfDao.batchDelete(list1);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectForCfgDeviceTree(String cfgDeviceName){
        List<AreaInfo> list = new LinkedList<>();
        //遥信
        List<AreaInfo> listItem1 = this.tSequentialConfDao.selectForTCfgMete(1,cfgDeviceName);
        AreaInfo areaInfoItem1 = new AreaInfo();
        areaInfoItem1.setId(-1L);
        areaInfoItem1.setUpId(null);
        areaInfoItem1.setLabel("遥信");
        areaInfoItem1.setChildren(listItem1);
        areaInfoItem1.setInfoType("meteKind");
        list.add(areaInfoItem1);
        //遥测
        List<AreaInfo> listItem2 = this.tSequentialConfDao.selectForTCfgMete(2,cfgDeviceName);
        AreaInfo areaInfoItem2 = new AreaInfo();
        areaInfoItem2.setId(2L);
        areaInfoItem2.setUpId(-2L);
        areaInfoItem2.setLabel("遥测");
        areaInfoItem2.setChildren(listItem2);
        areaInfoItem2.setInfoType("meteKind");
        list.add(areaInfoItem2);
        //遥测
        List<AreaInfo> listItem3 = this.tSequentialConfDao.selectForTCfgMete(3,cfgDeviceName);
        AreaInfo areaInfoItem3 = new AreaInfo();
        areaInfoItem3.setId(3L);
        areaInfoItem3.setUpId(-1L);
        areaInfoItem3.setLabel("遥控");
        areaInfoItem3.setChildren(listItem3);
        areaInfoItem3.setInfoType("meteKind");
        list.add(areaInfoItem3);
        //遥测
        List<AreaInfo> listItem4 = this.tSequentialConfDao.selectForTCfgMete(4,cfgDeviceName);
        AreaInfo areaInfoItem4 = new AreaInfo();
        areaInfoItem4.setId(4L);
        areaInfoItem4.setUpId(-4L);
        areaInfoItem4.setLabel("遥调");
        areaInfoItem4.setChildren(listItem4);
        areaInfoItem4.setInfoType("meteKind");
        list.add(areaInfoItem4);

        return list;
    }

    /**
     * 替换redis中的一键顺控结果sequentialResult
     *
     * @param resultNum resultNum
     * @return result
     */
    public String setSequentialResult(Integer resultNum) {
        if (resultNum == null || resultNum < 1 || resultNum > 4) {
            return "resultNum must 1-4";
        }

        redisTemplate.opsForHash().put("t_sys_param:sequentialResult", "content", resultNum.toString());

        // 将人工干预的开关打开
        redisTemplate.opsForHash().put("t_sys_param:sequentialFlag", "content", "true");
        return "ok";
    }

    /**
     * 手动干预静默监视识别结果
     *
     * @param type type
     * @return result
     */
    public String setSilentMonitorResult(Integer type) {
        String typeStr = getSilentMonitorType(type);
        if (StringUtils.isEmpty(typeStr)) {
            typeStr = "sly_bjbmyw";
        }

        redisTemplate.opsForValue().set("t_sys_param.silentMonitorAnalyseResult.type", typeStr);

        // 将人工干预的开关打开
        redisTemplate.opsForValue().set("t_sys_param.silentMonitorAnalyseResult.needManMade", true);
        return "ok";
    }

    /**
     * 从redis中获取静默监视AnalyseType映射关系
     *
     * @param type type
     * @return result
     */
    private String getSilentMonitorType(Integer type) {
        String silentMonitorAnalyseMapStr = (String)redisTemplate.opsForHash().get("t_sys_param:silentMonitorAnalyseMap", "content");
        String[] arr = silentMonitorAnalyseMapStr.split(",");

        Map<Integer, String> map = new HashMap<>();
        for (int i=0; i<arr.length; i++) {
            String[] subArr = arr[i].split("\\|");
            map.put(Integer.valueOf(subArr[0]), subArr[1]);
        }

        return map.get(type);
    }

    @Transactional(rollbackFor = Exception.class)
    public String sequential(String meteId) {
        // 获取一键顺控配置
        List<Map<String, Object>> list = sequentialInfo(meteId);
        if (list.isEmpty()){
            log.info("【meteId {} sequential config is empty!】", meteId);
            return "ok";
        }

        Map<String,Object> map = sequentialInfo(meteId).get(0);
        Map<String, String> jasonMaps2 = new HashMap<>(8);
        jasonMaps2.put("type", "newSequential");
        jasonMaps2.put("cfgDeviceId", meteId);
        jasonMaps2.put("sort", map.get("sort").toString());
        jasonMaps2.put("state", "进行中");
        log.info("发送给前端的消息:{}", JSON.toJSONString(jasonMaps2));
        Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMaps2);

        Constant.sequentialState.put("state", ((Long)map.get("sort")).intValue());
        Constant.sequentialState.put("cfgDeviceId", meteId);
        // 为了后续的假数据处理
        Constant.sequentialState.put("meteResult", map.get("state"));
        log.info("sequentialState:{}", Constant.sequentialState);

        if(StringUtils.isNotEmpty(meteId)){
            TSequentialConf sequentialConf = selectByPrimaryId(meteId);
            if(sequentialConf != null && sequentialConf.getPresetId() != null){
                update(sequentialConf);
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelAlgorithmAnalysis","content"));
        if (StringUtils.equals("true", flag)){
            // 收到聚焦信号开始录视频
            try {
                String services = HttpClientUtils.getInstance().getUrl(startRecordVideo+ "?cameraId=" +  map.get("cameraId").toString(), null);
                JSONObject jsonObject =JSONObject.parseObject(services);
                Constant.filePath = String.valueOf(jsonObject.get("data"));
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
       return "ok";
    }

    @Transactional(rollbackFor = Exception.class)
    public String sequentialRec(String meteId) {
        // 获取一键顺控配置
        List<Map<String,Object>> list = tSequentialConfDao.selectForSequenceInfoByMeteId(meteId);
        if (list.isEmpty()){
            log.info("meteId {} sequentialRec config is empty!", meteId);
            return "fail";
        }

        Map<String , Object> param = new HashMap<>(4);
        String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelAlgorithmAnalysis","content"));
        if (StringUtils.equals("true", flag)) {
            // 收到变位信号停止录视频
            try {
                String filePath = Constant.filePath;
                //等3s再停止录像
                Thread.sleep(3000);
                if (StringUtils.isNotEmpty(filePath)) {
                    String fileName = filePath.trim().substring(filePath.trim().lastIndexOf("/") + 1);
                    String services = HttpClientUtils.getInstance().getUrl(endRecordVideo + "?fileName=" + fileName, null);
                    log.info("视频结果: {}", services);
                    filePath = filePath.replace(".h264", ".mp4");
                    if (StringUtils.isNotEmpty(filePath)) {
                        param.put("picPath", filePath);
                        param.put("fileName", fileName);
                        param.put("fileType", "4");
                    } else {
                        throw new BusinessException("一键顺控-变位信号-录像地址为空");
                    }
                }
            } catch (Exception e) {
                log.error("一键顺控-变位信号-录像失败", e);
            }
        } else {
            //收到变位信号抓图
            try {
                Thread.sleep(1000);
                String services = HttpClientUtils.getInstance().getUrl(cameraCapture + "?cameraId=" + list.get(0).get("cameraId").toString(), null);
                JSONObject jsonObject = JSONObject.parseObject(services);
                Map<String, Object> re = (Map<String, Object>) jsonObject.get("data");
                if (!Objects.isNull(re.get("absPath"))) {
                    param.put("picPath", re.get("absPath").toString());
                    param.put("fileType", "2");
                } else {
                    throw new BusinessException("一键顺控-变位信号-抓图地址为空");
                }
            } catch (Exception e) {
                log.error("一键顺控-变位信号-抓图失败", e);
            }
        }

        // 一键顺控结果具体处理
        sequentialRecHandler(param, meteId, list);
        return "ok";
    }

    public void sequentialRecHandler(Map<String , Object> param, String meteId, List<Map<String,Object>> list) {
        String edgeLevel = Constant.getLevelEdge();
        //边缘节点:发送结果到区域巡视主机   巡视主机:发给算法进行分析
        if ("1".equals(edgeLevel)){
            String stationId = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeId", "content"));
            Map<String ,Object> filePathMap = new HashMap<>();
            String filePath = String.valueOf(param.get("picPath"));
            String fileName = StringUtils.substringAfterLast(filePath, "/");
            filePathMap.put("filePath", filePath);
            filePathMap.put("targetPath", stationId + "/" + "videoFile" + "/" + fileName);
            Constant.mapToOtherServer(filePathMap, Constant.TCP_UPLOAD_FILE);
           /* String videoName = String.valueOf(param.get("fileName"));
            String videoFilePath = String.valueOf(param.get("voicePath"));
            log.info("视频文件: {} {}", videoName, videoFilePath);
            if (StringUtils.isNotEmpty(videoName)) {
                filePathMap.put("filePath", videoFilePath);
                filePathMap.put("targetPath", "VideoFile" + "/" + videoName);
                Constant.mapToOtherServer(filePathMap, Constant.TCP_UPLOAD_FILE);
            }*/
            //上送视频文件结果
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> xmlItems = new ArrayList<>();
            Map<String, Object> xmlItem = new HashMap<>(16);
            xmlBaseModel.setType("61");
            xmlItem.put("patroldevice_name", "");
            xmlItem.put("patroldevice_code", "");
            xmlItem.put("task_name", "一键顺控任务");
            xmlItem.put("task_code", stationId + meteId);
            xmlItem.put("device_name", list.get(0).get("deviceName"));
            xmlItem.put("device_id", list.get(0).get("instanceId"));
            xmlItem.put("value_type", "0");
            xmlItem.put("value", "");
            xmlItem.put("value_unit", "");
            xmlItem.put("unit", "");
            xmlItem.put("time", "");
            //一键顺控检测 自定义为1001
            xmlItem.put("recognition_type", "1001");
            xmlItem.put("file_type",  param.get("fileType"));
            xmlItem.put("file_path", stationId + "/videoFile" + "/" + fileName);
            xmlItem.put("rectangle", "");
            xmlItem.put("task_patrolled_id", meteId);
            xmlItem.put("data_type", "1");
            xmlItem.put("valid", "1");
            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> xmlBaseModelArrayList = new ArrayList<>();
            xmlBaseModelArrayList.add(xmlBaseModel);
            Map<String,List<XMLBaseModel>> taskStatus = new HashMap<>(2);
            taskStatus.put("list", xmlBaseModelArrayList);
            Constant.otherServer(taskStatus, Constant.TCP_URL);
            return;
        }
        try{
            Analysis analysis = new Analysis();
            analysis.setAnalyseType("6");
            analysis.setInstanceId(Long.valueOf(list.get(0).get("cfgDeviceId").toString()));
            analysis.setIsAi(1);
            Map<String, Object> mapForPicModelPath = redisTemplate.opsForHash().entries("t_sys_param:picModelPath");
            String picModelPath = (String) mapForPicModelPath.get("content");
            analysis.setPicModelPath(picModelPath + "/" + list.get(0).get("presetId"));
            analysis.setTaskId("yjsk#meteId=" + list.get(0).get("cfgDeviceId"));
            analysis.setPicPath(param.get("picPath").toString());
            List<Analysis> analysisList = new ArrayList<>();
            analysisList.add(analysis);
            // 调用智能分析主机接口进行分析
            try {
                intelAnalysisService.picAnalyseNoDetection(analysisList);
            } catch (Exception e) {
                log.error("调用智能分析主机进行缺陷分析异常：", e);
            }
        }catch (Exception e){
            log.error("一键顺控-变位信号-调用算法识别主机失败:",e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String sequentialRecBack(Map<String,String> recBack) {
        // 获取一键顺控配置
        Map<String, Object> map = sequentialInfo(recBack.get("meteId")).get(0);
        Map<String, String> param = new HashMap<>();
        try {
            String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelAlgorithmAnalysis", "content"));
            if (StringUtils.equals("false", flag)) {
                String value = "";
                if (!Objects.isNull(recBack.get("code")) && StringUtils.equalsAny(recBack.get("code"), "200", "2000")) {
                    String orcMete = (String) Constant.sequentialState.get("meteResult");
                    String orc;
                    if (StringUtils.contains(orcMete, "分")) {
                        orc = "分";
                    } else {
                        orc = "合";
                    }
                    if ("200".equals(recBack.get("code")) && !Objects.isNull(recBack.get("desc"))) {
                        String ret = recBack.get("desc");
                        log.info("传入信号量: {}, 分合: {}, 识别信号量: {}", orcMete, orc, ret);
                        if ("unknown".equals(ret) || !ret.equals(orc)) {
                            param.put("resultValue", orc + "闸异常");
                            param.put("resultState", orc + "不到位");
                            if (StringUtils.equals("分", orc)) {
                                value = "3";
                            } else {
                                value = "4";
                            }
                        } else {
                            param.put("resultValue", orc + "闸正常");
                            param.put("resultState", orc + "位");
                            if (StringUtils.equals("分", orc)) {
                                value = "1";
                            } else {
                                value = "2";
                            }
                        }
                    } else if ("2000".equals(recBack.get("code")) && !Objects.isNull(recBack.get("value"))) {
                        String ret = recBack.get("value");
                        log.info("传入信号量: {}, 分合: {}, 识别信号量: {}", orcMete, orc, ret);
                        if ("1".equals(ret) && "分".equals(orc)) {
                            param.put("resultValue", "分闸正常");
                            value = "1";
                            param.put("resultState", orc + "位");
                        } else if ("2".equals(ret) && "合".equals(orc)) {
                            param.put("resultValue", "合闸正常");
                            value = "2";
                            param.put("resultState", orc + "位");
                        } else {
                            // 识别返回 3 和 4
                            param.put("resultValue", orc + "闸异常");
                            param.put("resultState", orc + "不到位");
                            if (StringUtils.equals("分", orc)) {
                                value = "3";
                            } else {
                                value = "4";
                            }
                        }
                    } else {
                        param.put("resultValue", "分析失败");
                        param.put("resultState", "无效状态");
                    }
                } else {
                    param.put("resultValue", "分析失败");
                    param.put("resultState", "无效状态");
                }
                upToMonitorSystem(recBack, map, value);
            } else {
                // 一键顺控是否使用自定义结果 true-是
                String sequentialFlag = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sequentialFlag").get("content"));
                log.info("sequentialFlag ： {}", sequentialFlag);
                if (StringUtils.equals("true", sequentialFlag)) {
                    String sequentialResult = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sequentialResult").get("content"));
                    recBack.put("value", sequentialResult);
                    log.info("干预一键顺控结果结束，recBack： {}", JSONUtil.toJSONString(recBack));
                    // 关闭人工干预开关，下次继续走算法分析
                    redisTemplate.opsForHash().put("t_sys_param:sequentialFlag", "content", "false");
                }

                if ("2000".equals(recBack.get("code"))) {
                    String ret = recBack.getOrDefault("value", "0");
                    switch (ret) {
                        case "1":
                            param.put("resultValue", "分闸正常");
                            break;
                        case "2":
                            param.put("resultValue", "合闸正常");
                            break;
                        case "3":
                            param.put("resultValue", "分闸异常");
                            break;
                        case "4":
                            param.put("resultValue", "合闸异常");
                            break;
                        default:
                            param.put("resultValue", "分析失败");
                            break;
                    }
                } else {
                    param.put("resultValue", "分析失败");
                }
                upToMonitorSystem(recBack, map, recBack.get("value"));
            }

            Map<String, String> jasonMapsResult = new HashMap<>(8);
            jasonMapsResult.put("type", "newSequentialResult");
            jasonMapsResult.put("cfgDeviceId", recBack.get("meteId"));
            jasonMapsResult.put("sort", String.valueOf(Double.valueOf(map.get("sort").toString()).intValue()));
            jasonMapsResult.put("state", param.get("resultValue"));
            jasonMapsResult.put("identifyResult", param.get("resultValue"));
            log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMapsResult));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapsResult);

            List<String> listSort = tSequentialConfDao.selectLastStep();
            log.info("顺控执行完毕， {}-{}", JSONUtil.toJSONString(listSort), JSONUtil.toJSONString(map));
            if (listSort.get(listSort.size() - 1).equals(map.get("cfgDeviceId"))) {
                // 这是最后一个步骤
                String time = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:cleanTime", "content"));
                Thread.sleep(Integer.parseInt(time) * 1000);
                Constant.sequentialState.put("state", -1);
                Constant.sequentialState.put("cfgDeviceId", "");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "ok";
    }

    private void upToMonitorSystem(Map<String, String> recBack, Map<String, Object> map, String param) {
        log.info("recBack:{},map:{},param:{}", recBack, map, param);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("_yyyyMMdd_HHmmss");
        String stationId = String.valueOf(redisTemplate.opsForHash().entries("region:" + map.get("edgeCode")).get("stationId"));
        String ftpsFilePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content"));
        String path = ftpsFilePath + "/" + stationId + "/linkage/";

        try {
            String value = "";
            switch (param){
                case "1": value = "分位"; break;
                case "2": value = "合位"; break;
                case "3": value = "分不到位"; break;
                case "4": value = "合不到位"; break;
                default: value = "无效状态"; break;
            }
            FileUtil.createDirectory(path);
            log.info("applicationProperties:{}", applicationProperties);
            String devicePath = path + applicationProperties.getSequentialConfig().getSequentialVideocfmResult().replace("{{date}}", simpleDateFormat2.format(new Date()));

            File txt=new File(devicePath);
            if(txt.delete()){
                txt.delete();
            }
            if (!txt.exists()) {
                txt.createNewFile();
            }
            String meteId = StringUtils.substringAfter(recBack.get("meteId"), stationId);
//                FileWriter fw = new FileWriter(txt, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(txt,true), applicationProperties.getSequentialConfig().getSequentialFileCharset()));
            bw.write("<!Entity=设备状态请求结果\tver='V1.0'\ttime='"+DateTimeUtil.format(new Date())+"'(文件最新时间)!>\r\n");
            bw.write("<DeviceInfo::设备状态>\r\n");
            bw.write("@序号\t站序号\t监控索引号\t设备名称\t设备状态\t事件时标\r\n");
            bw.write("#1\t"+1+"\t"+ meteId +"\t"+ map.get("deviceName")+"\t"+ value +"\t"+DateTimeUtil.format(new Date())+"\r\n");
            bw.write("</DeviceInfo::设备状态>\r\n");
            bw.flush();
            bw.close();
//                fw.close();
            if (Objects.nonNull(map.get("edgeCode"))) {
                Map<String, Object> mapForSend = new HashMap<>(3);
                mapForSend.put("edgeCode", map.get("edgeCode"));
                mapForSend.put("command", "2");
                mapForSend.put("filePath", devicePath.replace(ftpsFilePath, ""));
                log.info("Sending file to edge...");
                Constant.mapToOtherServer(mapForSend, Constant.LINKAGE_FILE_TRANSFER);
            } else {
                //将生成的顺控确认文件发送给主辅监控系统
                Map<String, List<String>> mapForSend = new HashMap<>();
                List<String> list = new ArrayList<>();
                list.add(devicePath);
                mapForSend.put("list", list);
                log.info("Sending file to udp...");
                Constant.otherServer(mapForSend, Constant.UDP_SEND);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> sequentialInfo(String cfgDeviceId){
        List<Map<String, Object>> list = tSequentialConfDao.selectForSequenceInfo(cfgDeviceId);

        Integer step = (Integer) Constant.sequentialState.get("state");
        if(-1 == step) {
            list.get(0).put("state", -1);
            return list;
        } else {
            if (StringUtils.isEmpty(cfgDeviceId)) {
                list.get(0).put("state", 1);
                return list;
            }
        }
        return list;
    }
    @Transactional(rollbackFor = Exception.class)
    public String unionTask(String cfgDeviceId,String order){
        String ftpsFilePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content"));
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("_yyyyMMdd_HHmmss");
        log.info("applicationProperties:{}", applicationProperties);

        try{
            TCfgDevice tCfgDevice = tCfgDeviceDao.selectByPrimaryId(cfgDeviceId);

            Map<String, String> map = redisTemplate.opsForHash().entries("region:" + tCfgDevice.getEdgeCode());
            String stationId = map.get("stationId");
            String path = ftpsFilePath + "/" + stationId + "/linkage/";
            FileUtil.createDirectory(path);
            String devicePath = path + applicationProperties.getSequentialConfig().getSequentialReturnLinkage().replace("{{date}}",simpleDateFormat2.format(new Date()));
            log.info("unionTask devicePath {}", devicePath);
            File txt=new File(devicePath);

            if(txt.exists()){
                txt.delete();
            }
            if (!txt.exists()) {
                txt.createNewFile();
            }

            cfgDeviceId = StringUtils.substringAfter(cfgDeviceId, stationId);
            FileWriter fw = new FileWriter(txt, true);
            //BufferedWriter bw = new BufferedWriter(fw,"UTF-8");
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(txt), applicationProperties.getSequentialConfig().getSequentialFileCharset()));

            bw.write("<!Entity=反向联动请求\tver='V1.0'\ttime='"+ DateTimeUtil.format(new Date())+"'(文件最新时间)!>\r\n");
            bw.write("<DeviceInfo::控制状态信息>\r\n");
            bw.write("@序号\t站序号\t监控索引号\t设备名称\t类型\t联动指令\r\n");
            bw.write("#1\t"+1+"\t"+cfgDeviceId+"\t"+tCfgDevice.getDeviceName()+"\t"+"遥控"+"\t"+order+"\r\n");
            bw.write("</DeviceInfo::控制状态信息>\r\n");
            bw.flush();
            bw.close();
            fw.close();
            //将生成的反向联动文件发送给主辅监控系统
            if (Objects.nonNull(tCfgDevice.getEdgeCode())) {
                Map<String, Object> mapForSend = new HashMap<>(3);
                mapForSend.put("edgeCode", tCfgDevice.getEdgeCode());
                mapForSend.put("command", "3");
                mapForSend.put("filePath", devicePath.replace(ftpsFilePath,""));
                log.info("Sending file to edge...");
                Constant.mapToOtherServer(mapForSend, Constant.LINKAGE_FILE_TRANSFER);
            } else {
                Map<String, List<String>> mapForSend = new HashMap<>();
                List<String> list = new ArrayList<>();
                list.add(devicePath);
                mapForSend.put("list", list);
                log.info("Sending file to udp...");
                Constant.otherServer(mapForSend, Constant.UDP_SEND);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return "ok";
    }

    public String receiveFile(String devicePath) {
        log.info("文件路径:{}", devicePath);

        // 读取内容
        String content = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(devicePath), "GB2312"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("#")) {
                    content = line.split("\\s+")[4];
                    break;
                }
            }
            br.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        String cfgDeviceId = String.valueOf(Constant.sequentialState.get("cfgDeviceId"));
        // 给前端通知已完成
        Map<String, String> jasonMapsResult = new HashMap<>(8);
        jasonMapsResult.put("type", "newSequentialResult");
        jasonMapsResult.put("cfgDeviceId", cfgDeviceId);
        jasonMapsResult.put("sort", "0");
        jasonMapsResult.put("state", content);
        jasonMapsResult.put("identifyResult", content);
        log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMapsResult));
        Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapsResult);

        List<String> listSort = tSequentialConfDao.selectLastStep();
        log.info("顺控执行完毕:{}", JSONUtil.toJSONString(listSort));
        if (listSort.get(listSort.size() - 1).equals(cfgDeviceId)) {
            // 这是最后一个步骤
            String time = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:cleanTime", "content"));
            try {
                Thread.sleep(Integer.parseInt(time) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Constant.sequentialState.put("state", -1);
            Constant.sequentialState.put("cfgDeviceId", "");
        }
        return "";
    }
}

