package com.yjh.platform.module.user.service;

import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.module.device.dao.TCfgDeviceDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TCfgDevice;
import com.yjh.platform.module.task.dao.TCfgDataCurrentDao;
import com.yjh.platform.module.task.entity.TCfgDataCurrent;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import com.yjh.platform.module.user.controller.TSequentialConfController;
import com.yjh.platform.module.user.dao.TSysParamDao;
import com.yjh.platform.module.user.entity.TSequentialConf;
import com.yjh.platform.module.user.dao.TSequentialConfDao;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yjh.platform.module.user.entity.TSysParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

import javax.management.ObjectName;

import static com.yjh.platform.common.Constant.redisTemplate;

/**
* @author lqh
* @since 2021-01-21
*/
@Service
public class TSequentialConfService{

    @Value("${sequential.videocfmresult}")
    private String videocfmresultFile;

    @Value("${sequential.returnlinkage}")
    private String returnlinkageFile;
    @Value("${sequential.fileCharset:GB2312}")
    private String fileCharset;
    @Value("${sequential.cameraCapture}")
    private String cameraCapture;
    @Value("${sequential.picRec}")
    private String picRec;

    @Value("${sequential.startRecordVideo}")
    private String startRecordVideo;
    @Value("${sequential.endRecordVideo}")
    private String endRecordVideo;


    @Autowired
    private TSequentialConfDao tSequentialConfDao;
    @Autowired
    private TSysParamDao tSysParamDao;
    @Autowired
    private TCfgDeviceDao tCfgDeviceDao;


    private Logger log = LoggerFactory.getLogger(TSequentialConfService.class);

    @Transactional(rollbackFor = Exception.class)
    public int add(TSequentialConf tSequentialConf) {
        List<Long> cameraIdList = tSequentialConfDao.selectCameraId();
        if(cameraIdList != null && cameraIdList.size() >0 && cameraIdList.contains(tSequentialConf.getCameraId())){
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

    @Transactional(rollbackFor = Exception.class)
    public String sequential(String meteId) throws Exception{
        List<Map<String, Object>> list = sequentialInfo(meteId);
        if (list.isEmpty()){
            return "ok";
        }
        {
            Map<String,Object> map = this.sequentialInfo(meteId).get(0);
            Map<String, String> jasonMaps2 = new HashMap<>();
            jasonMaps2.put("type", "newSequential");
            jasonMaps2.put("cfgDeviceId", meteId);
            jasonMaps2.put("sort", /*String.valueOf(Double.valueOf(*/map.get("sort").toString()/*).intValue())*/);
            Constant.sequentialState.put("state",((Long)map.get("sort")).intValue());
            Constant.sequentialState.put("cfgDeviceId",meteId);
            Constant.sequentialState.put("meteResult",map.get("state"));
            jasonMaps2.put("state", "进行中");
            String json = JSON.toJSONString(jasonMaps2);
            log.info("发送给前端的消息：" + json);
            try{
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMaps2);
            }catch (Exception e){
                System.out.println("发送websocket出错");
            }

            if(meteId != null && !"".equals(meteId)){
                TSequentialConf sequentialConf = this.selectByPrimaryId(meteId);
                if(sequentialConf != null && sequentialConf.getPresetId() != null){
                    this.update(sequentialConf);
                }
            }

            Thread.sleep(1000);
            // 开关
            String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelAlgorithmAnalysis","content"));
            if (StringUtils.equals("true", flag)){
                // 收到聚焦信号停止录视频
                try {
                    String services = HttpClientUtils.getInstance().getUrl(startRecordVideo+ "?cameraId=" +  map.get("cameraId").toString(), null);
                    JSONObject jsonObject =JSONObject.parseObject(services);
                    String result = String.valueOf(jsonObject.get("data"));
                    Constant.filePath = result;
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }

            //结果
//            {"type": "newSequentialResult",
//                    "cfgDeviceId": "1001",
//                    "sort": "1",
//                    "state": "控合",
//                    "identifyResult": "合"
//            }
//            顺控
//            {"type": "newSequential",
//                    "cfgDeviceId": "1001",
//                    "sort": "1",
//                    "state": "控合"
//            }

//            Thread.sleep(20000);
//            //todo 生成一个巡视任务
//
//            //todo 发给算法进行分析
//
//            try{
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("_yyyyMMdd_HHmmss");
//                //Map<String,String> mapResult = this.sequentialInfo(meteId).get(0);
//                TSysParam tSysParam = tSysParamDao.selectByParamType("unionDeviceInfoPath");
//                String devicePath = tSysParam.getContent()+"/"+videocfmresultFile.replace(".",simpleDateFormat2.format(new Date())+".");
//
//                File txt=new File(devicePath);
//                if(txt.exists()){
//                    txt.delete();
//                }
//                if (!txt.exists()) {
//                    txt.createNewFile();
//                }
////                FileWriter fw = new FileWriter(txt, true);
//                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
//                        new FileOutputStream(txt,true), "UTF-8"));
//                bw.write("<!Entity=设备状态请求结果\tver='V1.0'\ttime='"+simpleDateFormat.format(new Date())+"'(文件最新时间）!>\r\n");
//                bw.write("<DeviceInfo::设备状态>\r\n");
//                bw.write("@序号\t站序号\t监控索引号\t设备名称\t设备状态\t事件时标\r\n");
//                bw.write("#1\t"+1+"\t"+meteId+"\t"+map.get("deviceName")+"\t"+map.get("state")+"\t"+simpleDateFormat.format(new Date())+"\r\n");
//                bw.write("</DeviceInfo::设备状态>\r\n");
//                bw.flush();
//                bw.close();
////                fw.close();
//                //将生成的顺控确认文件发送给主辅监控系统
//                Map<String,List<String>> mapForSend = new HashMap<>();
//                List<String> list = new ArrayList<>();
//                list.add(devicePath);
//                mapForSend.put("list",list);
//                Constant.otherServer(mapForSend,Constant.UDP_SEND);
//            }catch (Exception e){log.info("生成顺控确认文件失败"+e.getMessage());}
//
//            Map<String, String> jasonMapsResult = new HashMap<>();
//            jasonMapsResult.put("type", "newSequentialResult");
//            jasonMapsResult.put("cfgDeviceId", meteId);
//            jasonMapsResult.put("sort", String.valueOf(Double.valueOf(map.get("sort").toString()).intValue()));
//            jasonMapsResult.put("state", map.get("state").toString());
//            jasonMapsResult.put("identifyResult", map.get("state").toString());
//            String jsonResult = JSON.toJSONString(jasonMapsResult);
//            log.info("发送给前端的消息：" + jsonResult);
//            try{
//                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapsResult);
//            }catch (Exception e){
//                System.out.println("发送websocket出错");
//            }
//
//            List<String> listSort = tSequentialConfDao.selectLastStep();
//            if(listSort.get(listSort.size()-1).equals(meteId) ){
//                //这是最后一个步骤
//                TSysParam time = tSysParamDao.selectByParamType("cleanTime");
//                Thread.sleep(Integer.valueOf(time.getContent())*1000);
//                Constant.sequentialState.put("state",-1);
//                Constant.sequentialState.put("cfgDeviceId","");
//            }
            //todo 生成顺控文件
        }
       return "ok";
    }

    @Transactional(rollbackFor = Exception.class)
    public String sequentialRec(String meteId) throws Exception{
        {

            String stationCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeCode", "content"));
            String edgeLevel = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeLevel", "content"));
            log.info("stationCode==={}", stationCode);
            Map<String,Object> map = tSequentialConfDao.selectForSequenceInfoByMeteId(meteId).get(0);
            Map<String , Object> param = new HashMap<>();
            // 开关
            String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelAlgorithmAnalysis","content"));
            if (StringUtils.equals("true", flag)){
                // 收到变位信号停止录视频
                try {
                    String filePath = Constant.filePath;
                    //等3s再停止录像
                    Thread.sleep(3000);
                    //收到变位信号抓图
                    try{
                        String services = HttpClientUtils.getInstance().getUrl(cameraCapture+ "?cameraId=" +  map.get("cameraId"), null);
                        log.info("抓图结果: {}", services);
                        JSONObject jsonObject =JSONObject.parseObject(services);
                        Map<String,Object> re = jsonObject.getJSONObject("data");
                        if(!Objects.isNull(re) && !Objects.isNull(re.get("absPath"))){
                            param.put("picPath",re.get("absPath").toString());
                            param.put("fileType", "2");
                        }else{
                            throw new BusinessException("一键顺控-变位信号-抓图地址为空");
                        }
                    }catch (Exception e){
                        log.error("一键顺控-变位信号-抓图失败", e);
                    }
                    if(StringUtils.isNotEmpty(filePath)) {
                        String fileName = filePath.trim().substring(filePath.trim().lastIndexOf("/") + 1);
                        HttpClientUtils.getInstance().getUrl(endRecordVideo + "?fileName=" + fileName, null);

                        filePath = filePath.replace(".h264", ".mp4");
                        if (StringUtils.isNotEmpty(filePath)) {
                            param.put("picPath", filePath);
                            param.put("fileName", fileName);
                            param.put("fileType", "4");
                        } else {
                            throw new BusinessException("一键顺控-变位信号-录像地址为空");
                        }
                    }
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }else{
                //收到变位信号抓图
                try{
                    Thread.sleep(1000);
                    String services = HttpClientUtils.getInstance().getUrl(cameraCapture+ "?cameraId=" +  map.get("cameraId").toString(), null);
                    JSONObject jsonObject =JSONObject.parseObject(services);
                    Map<String,Object> re= (Map<String,Object>) jsonObject.get("data");
                    if(!Objects.isNull(re.get("absPath"))){
                        param.put("picPath",re.get("absPath").toString());
                        param.put("fileType", "2");
                    }else{
                        throw new BusinessException("一键顺控-变位信号-抓图地址为空");
                    }
                }catch (Exception e){
                    log.error("一键顺控-变位信号-抓图失败",e);
                }
            }

            //节点为 边缘节点
            if ("1".equals(edgeLevel)){
                //发送结果到区域巡视主机
                //发文件 picPath /home/yjh_iot_center/iot-picture/resultImg/120920221041304111061.jpg
                String resultImgPath = (String)redisTemplate.opsForHash().get("t_sys_param:resultImgPath","content");
                Map<String ,Object> filePathMap = new HashMap<>();
                String filePath = String.valueOf(param.get("picPath"));
                String fileName = filePath.replace(resultImgPath, "");
                filePathMap.put("filePath", filePath);
                filePathMap.put("targetPath", stationCode + "/" + "videoFile" + "/" + fileName);
                Constant.mapToOtherServer(filePathMap, Constant.TCP_UPLOAD_FILE);
                String videoName = String.valueOf(param.get("fileName"));
                String videoFilePath = String.valueOf(param.get("voicePath"));
                log.info("视频文件: {} {}", videoName, videoFilePath);
                if (StringUtils.isNotEmpty(videoName)) {
                    filePathMap.put("filePath", videoFilePath);
                    filePathMap.put("targetPath", "VideoFile" + "/" + videoName);
                    Constant.mapToOtherServer(filePathMap, Constant.TCP_UPLOAD_FILE);
                }
                //上送结果（视频文件）
                XMLBaseModel xmlBaseModel = new XMLBaseModel();
                List<Map<String, Object>> xmlItems = new ArrayList<>();
                Map<String, Object> xmlItem = new HashMap<>(5);
                xmlBaseModel.setType("61");
                xmlItem.put("patroldevice_name", "");
                xmlItem.put("patroldevice_code", "");
                xmlItem.put("task_name", "一键顺控任务");
                xmlItem.put("task_code", stationCode + meteId);
                xmlItem.put("device_name", map.get("deviceName"));
                xmlItem.put("device_id", map.get("instanceId"));
                xmlItem.put("value_type", "0");
                xmlItem.put("value", "");
                xmlItem.put("value_unit", "");
                xmlItem.put("unit", "");
                xmlItem.put("time", "");
                //一键顺控检测
                xmlItem.put("recognition_type", "1001");
                xmlItem.put("file_type",  param.get("fileType"));
                xmlItem.put("file_path", stationCode + "/videoFile" + "/" + fileName);
                xmlItem.put("rectangle", "");
                xmlItem.put("task_patrolled_id", meteId);
                xmlItem.put("data_type", "01");
                xmlItem.put("valid", "1");
                xmlItems.add(xmlItem);
                xmlBaseModel.setItems(xmlItems);
                List<XMLBaseModel> xmlBaseModelArrayList = new ArrayList<>();
                xmlBaseModelArrayList.add(xmlBaseModel);
                Map<String,List<XMLBaseModel>> taskStatus = new HashMap<>(2);
                taskStatus.put("list", xmlBaseModelArrayList);
                Constant.otherServer(taskStatus, Constant.TCP_URL);
            }else {
                //节点为 巡视主机
                //todo 发给算法进行分析
                try{
                    param.put("analyseType",6);
                    param.put("instanceId",map.get("cfgDeviceId"));
                    //模板图片路径
                    param.put("isAi",1);
                    Map<String, Object> mapForPicModelPath = redisTemplate.opsForHash().entries("t_sys_param:picModelPath");
                    String picModelPath = (String) mapForPicModelPath.get("content");

                    param.put("picModelPath",picModelPath+"/"+map.get("presetId"));
//                param.put("taskId",UUID.randomUUID()+"#yjsk#meteId="+map.get("cfgDeviceId"));
                    param.put("taskId","yjsk#meteId="+map.get("cfgDeviceId"));
                    List<Map<String,Object>> analysis = new ArrayList<>();
                    analysis.add(param);
                    log.info("调用video算法识别接口param={},url={}",JSON.toJSONString(analysis),picRec);
                    Map<String,List<Map<String,Object>>> analysisList = new HashMap<>();
                    analysisList.put("list",analysis);
                    ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
                    if (null != serviceRestTemplate) {
                        serviceRestTemplate.postForObject(picRec, analysisList, String.class);
                    }
//
//                String services = HttpClientUtils.getInstance().postUrl(picRec, JSON.toJSONString(analysis));
//                JSONObject jsonObject =JSONObject.parseObject(services);
//                if(!Objects.isNull(jsonObject.get("code"))&&jsonObject.get("code").toString().equals("200")){
//                    log.info("一键顺控-变位信号-发送至算法识别主机成功");
//                }else{
//                    throw new BusinessException("一键顺控-变位信号-发送至算法识别主机失败"+jsonObject.toJSONString());
//                }
                }catch (Exception e){
                    log.error("一键顺控-变位信号-调用算法识别主机失败:",e);
                }
            }
        }
        return "ok";
    }

    @Transactional(rollbackFor = Exception.class)
    public String sequentialRecBack(Map<String,String> recBack) throws Exception{
        {
            Map<String,Object> map = this.sequentialInfo(recBack.get("meteId")).get(0);
            Map<String , String> param = new HashMap<>();
            try {
                // 开关
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
                                if (StringUtils.equals("分", orc)){
                                    value = "3";
                                }else{
                                    value = "4";
                                }
                            } else {
                                param.put("resultValue", orc + "闸正常");
                                param.put("resultState", orc + "位");
                                if (StringUtils.equals("分", orc)){
                                    value = "1";
                                }else{
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
                                if (StringUtils.equals("分", orc)){
                                    value = "3";
                                }else{
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
                }else{
                    // 一键顺控是否使用自定义结果 true-是
                    String sequentialFlag = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sequentialFlag").get("content"));
                    if (StringUtils.equals("true", sequentialFlag)){
                        String sequentialResult = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sequentialResult").get("content"));
                        recBack.put("value", sequentialResult);
                    }

                    if(!Objects.isNull(recBack.get("code")) && "2000".equals(recBack.get("code"))){
                        if(!Objects.isNull(recBack.get("value"))){
                            String ret = recBack.get("value");
                            switch (ret){
                                case "1": param.put("resultValue", "分闸正常"); break;
                                case "2": param.put("resultValue", "合闸正常"); break;
                                case "3": param.put("resultValue", "分闸异常"); break;
                                case "4": param.put("resultValue", "合闸异常"); break;
                                default: break;
                            }
                        }else{
                            param.put("resultValue", "分析失败");
                        }
                    }else{
                        param.put("resultValue", "分析失败");
                    }

                    upToMonitorSystem(recBack, map, recBack.get("value"));
                }
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

            Map<String, String> jasonMapsResult = new HashMap<>();
            jasonMapsResult.put("type", "newSequentialResult");
            jasonMapsResult.put("cfgDeviceId", recBack.get("meteId"));
            jasonMapsResult.put("sort", String.valueOf(Double.valueOf(map.get("sort").toString()).intValue()));
            jasonMapsResult.put("state", param.get("resultValue"));
            jasonMapsResult.put("identifyResult", param.get("resultValue"));
            String jsonResult = JSON.toJSONString(jasonMapsResult);
            log.info("发送给前端的消息：" + jsonResult);
            try{
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapsResult);
            }catch (Exception e){
                log.error("发送websocket出错",e);
            }

            List<String> listSort = tSequentialConfDao.selectLastStep();
            log.info("顺控执行完毕， {}-{}", JSONUtil.toJSONString(listSort), JSONUtil.toJSONString(map));
            if(listSort.get(listSort.size()-1).equals(map.get("cfgDeviceId")) ){
                //这是最后一个步骤
                TSysParam time = tSysParamDao.selectByParamType("cleanTime");
                Thread.sleep(Integer.valueOf(time.getContent())*1000);
                Constant.sequentialState.put("state",-1);
                Constant.sequentialState.put("cfgDeviceId","");
            }
            //todo 生成顺控文件
        }
        return "ok";
    }

    private void upToMonitorSystem(Map<String, String> recBack, Map<String, Object> map, String param) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("_yyyyMMdd_HHmmss");

        try {
            String value = "";
            switch (param){
                case "1": value = "分位"; break;
                case "2": value = "合位"; break;
                case "3": value = "分不到位"; break;
                case "4": value = "合不到位"; break;
                default: value = "无效状态"; break;
            }
            String stationCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeCode", "content"));
            String ftpsFilePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content"));
            String devicePath = ftpsFilePath + "/" + stationCode + "/linkage/" + videocfmresultFile.replace("{{date}}", simpleDateFormat2.format(new Date()));

            File txt=new File(devicePath);
            if(txt.exists()){
                txt.delete();
            }
            if (!txt.exists()) {
                txt.createNewFile();
            }
            String stationId = String.valueOf(redisTemplate.opsForHash().entries("region:" + map.get("edgeCode")).get("stationId"));
            String meteId = StringUtils.substringAfter(recBack.get("meteId"), stationId);
//                FileWriter fw = new FileWriter(txt, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(txt,true), fileCharset));
            bw.write("<!Entity=设备状态请求结果\tver='V1.0'\ttime='"+simpleDateFormat.format(new Date())+"'(文件最新时间)!>\r\n");
            bw.write("<DeviceInfo::设备状态>\r\n");
            bw.write("@序号\t站序号\t监控索引号\t设备名称\t设备状态\t事件时标\r\n");
            bw.write("#1\t"+1+"\t"+ meteId +"\t"+ map.get("deviceName")+"\t"+ value +"\t"+simpleDateFormat.format(new Date())+"\r\n");
            bw.write("</DeviceInfo::设备状态>\r\n");
            bw.flush();
            bw.close();
//                fw.close();
            if (Objects.nonNull(map.get("edgeCode"))) {
                Map<String, Object> mapForSend = new HashMap<>(3);
                mapForSend.put("edgeCode", map.get("edgeCode"));
                mapForSend.put("command", "2");
                mapForSend.put("filePath", devicePath.replace(ftpsFilePath, ""));
                Constant.mapToOtherServer(mapForSend, Constant.LINKAGE_FILE_TRANSFER);
            } else {
                //将生成的顺控确认文件发送给主辅监控系统
                Map<String, List<String>> mapForSend = new HashMap<>();
                List<String> list = new ArrayList<>();
                list.add(devicePath);
                mapForSend.put("list", list);
                Constant.otherServer(mapForSend, Constant.UDP_SEND);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> sequentialInfo(String cfgDeviceId){
        //todo 写入识别结果
        return tSequentialConfDao.selectForSequenceInfo(cfgDeviceId);
    }
    @Transactional(rollbackFor = Exception.class)
    public String unionTask(String cfgDeviceId,String order){

        try{
            TCfgDevice tCfgDevice = tCfgDeviceDao.selectByPrimaryId(cfgDeviceId);
            String stationCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeCode", "content"));
            String ftpsFilePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content"));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("_yyyyMMdd_HHmmss");

            String devicePath = ftpsFilePath + "/" + stationCode + "/linkage/" + returnlinkageFile.replace("{{date}}",simpleDateFormat2.format(new Date()));
            File txt=new File(devicePath);

            if(txt.exists()){
                txt.delete();
            }
            if (!txt.exists()) {
                txt.createNewFile();
            }
            String stationId = String.valueOf(redisTemplate.opsForHash().entries("region:" + tCfgDevice.getEdgeCode()).get("stationId"));
            cfgDeviceId = StringUtils.substringAfter(cfgDeviceId, stationId);
            FileWriter fw = new FileWriter(txt, true);
            //BufferedWriter bw = new BufferedWriter(fw,"UTF-8");
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(txt), fileCharset));

            bw.write("<!Entity=反向联动请求\tver='V1.0'\ttime='"+simpleDateFormat.format(new Date())+"'(文件最新时间)!>\r\n");
            bw.write("<DeviceInfo::控制状态信息>\r\n");
            bw.write("@序号\t站序号\t监控索引号\t设备名称\t类型\t联动指令\r\n");
            bw.write("#1\t"+1+"\t"+cfgDeviceId+"\t"+tCfgDevice.getDeviceName()+"\t"+"遥控"+"\t"+order+"\r\n");
            bw.write("</DeviceInfo::设备资源信息>\r\n");
            bw.flush();
            bw.close();
            fw.close();
            //将生成的反向联动文件发送给主辅监控系统
            if (Objects.nonNull(tCfgDevice.getEdgeCode())) {
                Map<String, Object> mapForSend = new HashMap<>(3);
                mapForSend.put("edgeCode", tCfgDevice.getEdgeCode());
                mapForSend.put("command", "3");
                mapForSend.put("filePath", devicePath.replace(ftpsFilePath,""));
                Constant.mapToOtherServer(mapForSend, Constant.LINKAGE_FILE_TRANSFER);
            } else {
                Map<String, List<String>> mapForSend = new HashMap<>();
                List<String> list = new ArrayList<>();
                list.add(devicePath);
                mapForSend.put("list", list);
                Constant.otherServer(mapForSend, Constant.UDP_SEND);
            }
        } catch (IOException e) {
            log.error("生成顺控确认文件失败" + e);
        } catch (Exception e) {
            log.error("发送顺控确认文件失败" + e);
        }

        return "ok";
    }
}

