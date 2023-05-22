package com.yjh.platform.common.quartz;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.common.utils.HttpAysncClientUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.task.dao.TWarnInfoDao;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import com.yjh.platform.module.task.service.TWarnInfoService;
import com.yjh.platform.module.user.service.TCameraPresetService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author quzhihui
 * @date 2022/8/30 - 14:11
 */
@lombok.extern.slf4j.Slf4j
public class SilentAlarmThread implements Runnable {
    private static final int XML = 1;
    private static final int JSON = 2;
    private static final int IMAGE = 3;
    private static final int AUDIOFILE = 4;
    private static final int HeadSize = 256;
    private static final String end = "\r\n";
    private static final String boundary = "boundary";
    private static final String ContentT = "Content-Type: ";
    private static final String ContentL = "Content-Length: ";
    private static final String ContentI = "Content-ID: ";
    private static final String MSG = "success";

    private static String strXML;
    private String ip;
    private String port;
    private String presetId;
    private String cameraId;
      private String eventType;
    private String filePath;
    private String eventState;
    private String syncWebsocketUrl;

    /**
     * 调用海康 接口
     */
    private static final String ALERT_URL = "/ISAPI/Event/notification/alertStream";

    private RedisTemplate redisTemplate;
    private TCameraPresetService tCameraPresetService;
    private ApplicationProperties applicationProperties;

    private TWarnInfoService tWarnInfoDao;

    public String getPresetId() {
        return presetId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public SilentAlarmThread() {
    }

    public SilentAlarmThread(String ip, String port, String presetId, String cameraId, RedisTemplate redisTemplate,
                             TCameraPresetService tCameraPresetService,ApplicationProperties applicationProperties, String syncWebsocketUrl,TWarnInfoService tWarnInfoDao) {
        this.ip = ip;
        this.port = port;
        this.presetId = presetId;
        this.cameraId = cameraId;
        this.redisTemplate = redisTemplate;
        this.tCameraPresetService = tCameraPresetService;
        this.applicationProperties = applicationProperties;
        this.syncWebsocketUrl = syncWebsocketUrl;
        this.tWarnInfoDao=tWarnInfoDao;
    }

    @Override
    public void run() {
        try {
            log.info("进入秒级静默线程");
            HttpAysncClientUtil.LonLink("http://" + ip + ":" + port + ALERT_URL, this);
        } catch (Exception e) {
            log.error("秒级静默线程错误: ", e);
        }
    }


    public void makeXMLData(String content) {
        if (content == null) {
            return;
        }
        log.info("秒级静默监视数据处理 -- xml");
        try {
            // 静默任务开关
            String silentFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isSilentTask", "content"));
            if (Boolean.FALSE.toString().equals(silentFlag)) {
                log.info("isSilentTask is false");
                HttpAysncClientUtil.StopLink();
                return;
            }
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            // 获取根节点
            Element rootEle = document.getRootElement();
            // 获取根节点下所有子节点
            List<Element> list = rootEle.elements();
            for (Element element : list) {
                if (element.getName().equals("eventType")) {
                    eventType = element.getText();
                    log.info("eventType --- " + element.getText());
                    if (!element.getText().equals("mixedTargetDetection") && !element.getText().equals("fielddetection")) {
                        return;
                    }
                    if (element.getText().equals("VMD") || element.getText().equals("videoloss")) {
                        return;
                    }
                }
                if (element.getName().equals("eventState")) {
                    // 事件状态  [active#有效事件,inactive#无效事件]
                    if (element.getText().equals("inactive")) {
                        eventState = element.getText();
                    }
                }
            }
            log.info(content);
        } catch (Exception e) {
            log.error("秒级静默xml处理失败: ", e);
        }
        log.info(content);
    }

    public void makeJSONData(String content) {
        if (content == null) {
            return;
        }
        log.info("秒级静默监视数据处理 -- json");
        log.info(content);
        try {
            // 静默任务开关
            String silentFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isSilentTask", "content"));
            if (Boolean.FALSE.toString().equals(silentFlag)) {
                log.info("isSilentTask is false");
                HttpAysncClientUtil.StopLink();
                return;
            }
            JSONObject json = JSONObject.parseObject(content);
            if ("inactive".equals(json.getString("eventState"))) {
                eventState = json.getString("eventState");
            }
            if (StringUtils.isNotEmpty(json.getString("eventType"))) {
                eventType = json.getString("eventType");
                log.info("eventType --- " + json.getString("eventType"));
            }
            if ("AIOP_Video".equals(eventType)) {
                try {
                    // 由于安全帽和人员拒绝徘徊用的是一个算法，给的数据格式差不多
                    // 简单区分，在AI 开发平台配置是，将安全帽配置为第一个规则
                    // 即可利用规则id ruleID 区分他们（ruleID：从1开始，顺序）
                    JSONObject aiopData = json.getJSONObject("AIOPData");
                    JSONObject events = aiopData.getJSONObject("events");
                    JSONArray alertInfo = events.getJSONArray("alertInfo");
                    JSONObject ruleInfo = alertInfo.getJSONObject(0).getJSONObject("ruleInfo");
                    if ("1".equals(ruleInfo.getString("ruleID"))) {
                        eventType = "anquanmao";
                    } else {
                        eventType = "renyuan";
                    }
                } catch (Exception e) {
                    log.info("AI开发平台，解析json错误: ", e);
                    eventType = "error";
                }
            }
        } catch (Exception e) {
            log.error("秒级静默json处理失败: ", e);
        }
    }

    private void makImageData(char[] imageBuf) {
        String str = new String(imageBuf);
        if (str.contains("[bg_upload:1]")) {
            return;
        }
        log.info("秒级静默监视数据处理 -- img");
        try {
            // 静默任务开关
            String silentFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isSilentTask", "content"));
            if (Boolean.FALSE.toString().equals(silentFlag)) {
                log.info("isSilentTask is false");
                HttpAysncClientUtil.StopLink();
                return;
            }
            if (Objects.equals("fielddetection", eventType) || Objects.equals("mixedTargetDetection", eventType) ||
                    Objects.equals("anquanmao", eventType) || Objects.equals("renyuan", eventType)) {
                Charset charset = StandardCharsets.ISO_8859_1;
                CharBuffer charBuffer = CharBuffer.allocate(imageBuf.length);
                charBuffer.put(imageBuf);
                charBuffer.flip();
                ByteBuffer byteBuffer = charset.encode(charBuffer);
                byte[] image = byteBuffer.array();

                int max = 9999, min = 1;
                int ran = (int) (Math.random() * (max - min) + min);
                SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
                String filePathTem = "/" + formatter.format(new Date()) + ran + ".jpg";
                filePath = "/home/yjh_iot_center/iot-picture/resultImg" + filePathTem;
                log.info("filePath:{}", filePath);
                File file = new File(filePath);
                FileUtils.writeByteArrayToFile(file, image);

//                String url = "chmod 777 " + filePath;
//                Runtime.getRuntime().exec(url);
//                copyFile(filePathTem, filePath);

                log.info("filePathTem:{}", filePathTem);
                silentHandler(filePath, presetId, eventType);
            }
        } catch (Exception e) {
            log.error("图片处理失败: ", e);
        }
    }

    /**
     * 将响应的字符截取成 xml 或 json
     *
     * @param chBuffer
     */
    public void makeData(List<Character> chBuffer) {
        //Data offset
        int offset = 0;
        int infoType = 0;
        if (chBuffer.isEmpty() || chBuffer.size() < HeadSize) {
            return;
        }
        List<Character> targetList = chBuffer.subList(0, HeadSize);
        StringBuilder targetBuf = new StringBuilder();
        for (char tempNode : targetList) {
            targetBuf.append(tempNode);
        }
//        StringBuilder sb =  new StringBuilder();
//        for (char c : chBuffer){
//            sb.append(c);
//        }
//        log.info("recive data, targetBuf:{}, data: {}", targetBuf, sb);
        String strHeadBuf = targetBuf.toString();
        if (strHeadBuf.contains(boundary)) {
            if (strHeadBuf.contains(ContentT)) {
                offset += strHeadBuf.indexOf(ContentT);
                if (strHeadBuf.contains("xml")) {
                    infoType = XML;
                } else if (strHeadBuf.contains("json")) {
                    infoType = JSON;
                } else if (strHeadBuf.contains("image")) {
                    infoType = IMAGE;
                } else if (strHeadBuf.contains("audio/wav")) {
                    infoType = AUDIOFILE;
                }
            }

            StringBuilder strlen = new StringBuilder();
            int len = 0;
            if (strHeadBuf.contains(ContentL)) {
                offset = strHeadBuf.indexOf(ContentL);
                offset += ContentL.length();

                for (; strHeadBuf.charAt(offset) != '\r'; offset++) {
                    strlen.append(strHeadBuf.charAt(offset));
                }
                len = Integer.parseInt(strlen.toString());
            }
            StringBuilder strien = new StringBuilder();
            if (strHeadBuf.contains(ContentI)) {
                offset = strHeadBuf.indexOf(ContentI);
                offset += ContentI.length();
                for (; strHeadBuf.charAt(offset) != '\r'; offset++) {
                    strien.append(strHeadBuf.charAt(offset));
                }
            }
            offset += (2 * end.length());
            if (chBuffer.size() >= offset + len) {
                char[] imageBuf = null;
                switch (infoType) {
                    case XML: {
                        StringBuilder XmlBuf = new StringBuilder();
                        targetList = chBuffer.subList(offset, offset + len);
                        for (char c : targetList) {
                            XmlBuf.append(c);
                        }
                        strXML = XmlBuf.toString();
                        for (int i = 0; i < (offset + len) && chBuffer.size() > 0; i++) {
                            chBuffer.remove(0);
                        }
                        break;
                    }
                    case JSON: {
                        StringBuilder JsonBuf = new StringBuilder();
                        targetList = chBuffer.subList(offset, offset + len);
                        for (char c : targetList) {
                            JsonBuf.append(c);
                        }
                        strXML = JsonBuf.toString();
                        for (int i = 0; i < (offset + len) && chBuffer.size() > 0; i++) {
                            chBuffer.remove(0);
                        }
                        break;
                    }
                    case IMAGE: {
                        if (chBuffer.size() > offset + len) {
                            imageBuf = new char[len];
                            targetList = chBuffer.subList(offset, offset + len);
                            for (int i = 0; i < len; i++) {
                                imageBuf[i] = targetList.get(i);
                            }
                            for (int i = 0; i < (offset + len + end.length()) && chBuffer.size() > 0; i++) {
                                chBuffer.remove(0);
                            }
                        }
                        break;
                    }
                    case AUDIOFILE: {
                        if (chBuffer.size() > offset + len) {
                            imageBuf = new char[len];
                            targetList = chBuffer.subList(offset, offset + len);
                            for (int i = 0; i < len; i++) {
                                imageBuf[i] = targetList.get(i);
                            }
                            chBuffer = chBuffer.subList(offset + len, chBuffer.size());
                        }
                        break;
                    }
                }
                if (infoType == XML) {
                    makeXMLData(strXML);
                } else if (infoType == JSON) {
                    makeJSONData(strXML);
                }  else if (infoType == IMAGE) {
                    makImageData(imageBuf);
                }  else if (infoType == AUDIOFILE) {
                    log.info("AUDIOFILE, {}", AUDIOFILE);
                }
                makeData(chBuffer);
            }
        }
    }

    public void stopAlarmGuard(boolean flag) {
        try {
            String cameraId = this.cameraId;
            String presetId = this.presetId;
            String key = Constant.SILENT_SECOND + cameraId + ":" + presetId;
            Map<String, Object> entries = redisTemplate.opsForHash().entries(key);
            if (cameraId.equals(String.valueOf(entries.get("cameraId"))) && presetId.equals(String.valueOf(entries.get("presetId")))) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("camera_info:" + cameraId);
                String state = redisInfoMap.get("state");
                if (!StringUtils.equals("0", state)) {
                    redisTemplate.opsForHash().delete(key);
                    return;
                }
                if (tCameraPresetService.selectSilentByCameraIdAndPresetId(cameraId, presetId) == 0) {
                    log.info("tCameraPresetService.selectSilentByCameraIdAndPresetId(cameraId, presetId) == 0");
                    redisTemplate.opsForHash().delete(key);
                    return;
                }
                if (flag) {
                    redisTemplate.opsForHash().delete(key);
                }
            }
        }catch (Exception e){
            log.error("秒级静默监视删除redis配置出错：",e);
        }
    }

    private void copyFile(String ftpsPath, String localPath){
        try {
            if(StringUtils.isEmpty(ftpsPath) || StringUtils.isEmpty(localPath)) {return;}
            FtpsUtil.putFile(localPath, ftpsPath, applicationProperties.getUpSystemFtps().getIp(), applicationProperties.getUpSystemFtps().getPort(),
                    applicationProperties.getUpSystemFtps().getUserName(), applicationProperties.getUpSystemFtps().getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误: ", e);
        }
    }

    /**
     * 分析数据，将告警入库，上送
     * @param presetId 预置位id  充当巡视点ID
     */
    private void silentHandler(String filePath, String presetId, String eventType) {
        if (Objects.nonNull(filePath)) {
            try {
                String monitorType;
                String desc;
                if ("fielddetection".equals(eventType)) {
                    // 小动物入侵
                    monitorType = "103";
                    desc = "小动物入侵";
                } else if ("mixedTargetDetection".equals(eventType)) {
                    // 未穿长袖工作服
                    monitorType = "3";
                    desc = "未穿工装";
                } else if ("anquanmao".equals(eventType)){
                    monitorType = "1";
                    desc = "未穿安全帽";
                } else if ("renyuan".equals(eventType)){
                    monitorType = "4";
                    desc = "人员聚集/徘徊";
                } else {
                    monitorType = "";
                    desc = "";
                }
                Map<String, Object> map = tCameraPresetService.selectInstanceInfo(Long.valueOf(presetId));

                TWarnInfo tWarnInfo = silentMonitorHandle(filePath, desc, map);
                alarmToUpSystem(map, tWarnInfo, monitorType);
            } catch (Exception e) {
                log.error("静默监视异常: ", e);
            }
        }
    }

    /**
     * 静默监视产生告警处理
     */
    private TWarnInfo silentMonitorHandle(String imageUrl, String desc, Map<String, Object> map) {
        try {
            // 图片在ftps上的全路径
//            String resultAbsolutePath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath","content")  + imageUrl;
//            String targetPath = redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content") + imageUrl;
//            FileUtil.copyFileUsingStream(resultAbsolutePath, targetPath);
//
            String defectResultRealImg = imageUrl.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath","content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath","content")));
            String alarmLevel = tCameraPresetService.selectAlarmLevel("defect_model", desc);
            if (alarmLevel == null) {
                alarmLevel = "132";
            }
            TWarnInfo tWarnInfo = new TWarnInfo()
                    .setWarnLevel(Integer.valueOf(alarmLevel))
                    .setWarnTime(new Date())
                    .setWarnName("静默监视告警数据")
                    .setWarnContent(desc)
                    .setDeviceId(Long.valueOf(String.valueOf(map.get("device_id"))))
                    .setCunstomId(String.valueOf(map.get("custom_id")))
                    .setInstanceId(Long.valueOf(String.valueOf(map.get("instance_id"))))
                    .setStdMeteId(Long.valueOf(String.valueOf(map.get("device_mete_id"))))
                    .setConfMode(276)
                    .setDefectModel(450)
                    .setAlarmSource(800)
                    .setImagePath(defectResultRealImg);
            tWarnInfoDao.insert(tWarnInfo);
            //webSocket通知前端调用查询告警弹框的接口
            Map<String, String> jasonMaps = new HashMap<>(16);
            jasonMaps.put("type", "alarmPopUp");
            jasonMaps.put("warnType", "1");
            jasonMaps.put("warnLevel", alarmLevel);
            jasonMaps.put("warnId", ValueUtil.toString(tWarnInfo.getWarnId(),"1"));
            jasonMaps.put("defectModel", "450");
            String json = com.alibaba.fastjson.JSON.toJSONString(jasonMaps);
            log.info("发送给前端的消息: {}", json);
            if (!Constant.isUpSystem()){
//                restTemplatePost(syncWebsocketUrl, json);
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps);
            }
            return tWarnInfo;
        } catch (Exception e) {
            log.error("组装并存储告警信息出错: ", e);
        }
        return null;
    }


    /**
     * 静默监视告警向上级系统上报
     *
     */
    private void alarmToUpSystem(Map<String, Object> map, TWarnInfo tWarnInfo, String monitorType) {
        if (tWarnInfo != null) {
            String warnTime = DateTimeUtil.format(tWarnInfo.getWarnTime());

            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> xmlItems = new ArrayList<>();
            Map<String, Object> xmlItem = new HashMap<>(16);

            xmlBaseModel.setType("63");
            xmlItem.put("patroldevice_code", map.get("preset_id"));
            xmlItem.put("patroldevice_name", map.get("device_name"));
            switch (tWarnInfo.getWarnLevel()) {
                case 130:
                    xmlItem.put("alarm_level", "1");
                    break;
                case 131:
                    xmlItem.put("alarm_level", "2");
                    break;
                case 132:
                    xmlItem.put("alarm_level", "3");
                    break;
                case 133:
                    xmlItem.put("alarm_level", "4");
                    break;
                default:
                    break;
            }

            try {
                xmlItem.put("monitor_type", monitorType);
                // 目前都是识别图片 所以是5
                xmlItem.put("file_type", "5");
                String imgPath = tWarnInfo.getImagePath().replaceAll(
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content")),
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")));
                String targetNamePath = imgPath.replace(
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")), "").substring(1);
                log.info("imgPath:{},targetNamePath:{}", imgPath, targetNamePath);
                String edgeCode = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeId").get("content"));
                String timeFormat = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String ftpsTarPath = edgeCode+"/jm/" + timeFormat.substring(0,4) + "/" + timeFormat.substring(4,6) + "/" + timeFormat.substring(6,8)+"/"+cameraId+"/"+presetId+"_"+System.currentTimeMillis()+".jpg";

                uploadFileToUpFtps(imgPath, ftpsTarPath, applicationProperties.getUpSystemFtps());

                xmlItem.put("file_path", ftpsTarPath);
                xmlItem.put("time", warnTime);
                xmlItem.put("content", tWarnInfo.getWarnContent());
                xmlItems.add(xmlItem);
                xmlBaseModel.setItems(xmlItems);
                List<XMLBaseModel> list = new ArrayList<>();
                list.add(xmlBaseModel);
                Map<String, List<XMLBaseModel>> alarmMap = new HashMap<>(3);
                alarmMap.put("list", list);
                log.info("告警上报: {}", com.alibaba.fastjson.JSON.toJSONString(alarmMap));
                Constant.otherServer(alarmMap, Constant.TCP_URL);
            } catch (Exception e) {
                log.error("向上级系统上报静默监视告警出错: ", e);
            }
        }
    }

    /**
     * 请求其他服务
     *
     * @param url  请求地址
     * @param json 发送内容
     * @return String
     */
    public void restTemplatePost(String url, String json) {
        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForEntity(url, json, String.class);
        }catch (Exception e){
            log.error("请求其他服务错误: ", e);
        }
    }


    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath 源文件地址
     * @param targetPathName 目标文件地址名称
     */
    private void uploadFileToUpFtps(String sourcePath, String targetPathName, ApplicationProperties.FtpsConfig upFtpsConfig) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getUserName(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误: ", e);
        }
    }
}

