package com.yjh.accessvideo.module.control.service;

import com.alibaba.fastjson.JSONObject;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.common.logs.Logs;
import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.commons.utils.http.HttpClientUtils;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.hik.PlayCtrl;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.*;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.entity.TCameraInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.stream.FileImageOutputStream;
import java.io.*;
import java.nio.ByteBuffer;
import java.sql.ClientInfoStatus;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


/**
 * @author tt
 * @since 2020-08-20
 */
@Service
public class CameraConService {

    private Logger log = LoggerFactory.getLogger(CameraConService.class);

    @Autowired
    private CameraConDao cameraConDao;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;
    @Value("${nvr.rtmp.video}")
    private String UrlTem;

    @Value("${nvr.rtmp.back}")
    private String UrlBackTem;

    @Value("${robot.light.video}")
    private String robotLightTem;

    @Value("${robot.inferad.video}")
    private String robotInferadTem;

    @Value("${nvr.capture.Preset}")
    private String capturePresetPath;//预置位路径

    @Value("${spring.redis.host}")
    private String hostIp;

    @Value("${nvr.capture.video}")
    private String videoPath;//视频保存位置

    @Value("${nvr.capture.hotPic}")
    private String hotPic;//红外图片路径

    @Value("${nvr.capture.hotPicshow}")
    private String hotPicshow;//红外图片路径对外


    @Value("${nvr.capture.hotFir}")
    private String hotFir;//红外csv和fir文件路径


    @Value("${nvr.capture.hotFirShow}")
    private String hotFirShow;//红外csv和fir文件路径对外

    @Value("${nvr.capture.savePath}")
    private String savePath;//下载视频地址
    @Value("${service.video.Path}")
    private String SERVICE_URL;//获取可视台账用户


    @Value("${realtime.video.definition}")
    private String videoDefinition;

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    private static PlayCtrl playCtrl = PlayCtrl.INSTANCE;
    private NativeLong m_lRealPlayHandle = new NativeLong(-1);// playhandle
    private NativeLong m_minsOne = new NativeLong(-1);
    private NativeLong lUserIDLong = new NativeLong(-1);
    private HCNetSDK.NET_DVR_CLIENTINFO m_sClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();	// play structure
    private HCNetSDK.NET_DVR_PREVIEWINFO dvr_previewinfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
    private NativeLong m_lPort =  new NativeLong(-1);
    FRealDataCallBack fRealDataCallBack = new FRealDataCallBack();

    private int lUserID = -1;//用户句柄
    private NativeLong mVoiceTalkHandle =new  NativeLong(-1);//对讲句柄
    //设备登录信息
    private HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
    //设备信息
    private HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startRealPlay(Long cameraId) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            Map<String, Object> cameraFlowMap = redisTemplate.opsForHash().entries("cameraRealFlow:"+cameraId);
            if (Objects.nonNull(cameraFlowMap.get("rtmpUrl"))) {
                cameraFlowMap.put("cameraId", String.valueOf(cameraId));
                return cameraFlowMap;
            }
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
            String userName = cameraConInfo.getIdentityManager();
            String password = cameraConInfo.getIdentityCode();
            String cameraIp = cameraConInfo.getRecordIp();
            int cameraPort = cameraConInfo.getRtspPort();
            int iChanNum = cameraConInfo.getChannelNum();
            int cameraType = cameraConInfo.getCameraType();
            int livePath;
            if (Constant.maps.get("livePath") != null) {
                livePath = Constant.maps.get("livePath")+1;
                Constant.maps.put("livePath", livePath);
            } else {
                livePath = 123;
                Constant.maps.put("livePath", 123);
            }
            log.info("Constant.maps: "+Constant.maps);
            log.info(userName+" "+password+" "+cameraIp+" "+cameraPort+" "+iChanNum+" "+cameraType+" "+livePath);
            String transUrl = "";
            if (cameraType==205) {
                transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,videoDefinition, livePath);
            } else if (cameraType==206) {transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,1, livePath);}
            Runtime.getRuntime().exec(transUrl);
            log.info("transUrl: "+transUrl);
            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp"+rtmpUrls[rtmpUrls.length-1];
            String flvUrl = "http://"+hostIp+":8000/live/"+livePath+".flv";
            returnMap.put("rtmpUrl", rtmpUrl);
            returnMap.put("flvUrl", flvUrl);
            Constant.mapsForCamera.put(String.valueOf(cameraId), rtmpUrl);
            redisTemplate.opsForHash().putAll("cameraRealFlow:"+cameraId, returnMap);
            log.info("mapsForCamera: "+Constant.mapsForCamera);
            log.info("returnMap: "+returnMap);
        } catch (Exception e) {e.getMessage();}
        return returnMap;
    }

    //@Logs(title = "相机停止播放", code = "cameraStopPlay")
    @Transactional(rollbackFor = Exception.class)
    public String stopRealPlay(Long cameraId, String rtmpUrl) {
        String urlStop = null;
        String livePath;
        redisTemplate.delete("cameraRealFlow:"+cameraId);
        try {
            if (Objects.equals(null, rtmpUrl) || rtmpUrl.equals("")) {
                String rtmpUrlCamera = Constant.mapsForCamera.get(String.valueOf(cameraId));
                String[] rtmpUrlCameras = rtmpUrlCamera.split("/");
                livePath = rtmpUrlCameras[rtmpUrlCameras.length-1];
            } else {
                String[] rtmpUrls = rtmpUrl.split("/");
                livePath = rtmpUrls[rtmpUrls.length-1];
            }
            log.info("livePath: "+livePath);
            String url = "ps -ef | grep ffmpeg | grep '"+ livePath +"' | grep -v 'grep'";
            log.info("stopUrl: "+url);
            Process processForId=Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
            processForId.waitFor();
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(), "UTF-8"));
            String lineForId = null;
            StringBuilder dataBackForId = new StringBuilder();
            while ((lineForId = readerForId.readLine()) != null) {
                dataBackForId.append(lineForId).append('\n');
            }
            Integer processNum = Integer.parseInt(dataBackForId.substring(9,15).replace(" ",""));
            urlStop = "kill -9 "+processNum;
            Runtime.getRuntime().exec(urlStop);
            Constant.mapsForCamera.remove(String.valueOf(cameraId));
        } catch (Exception e) {e.getMessage();}
        return "stop " + cameraId + " preview success!";
    }

    //@Logs(title = "相机批量播放", code = "cameraPlay", content = "相机批量播放")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> batchStartRealPlay(String cameraIds) {
        log.info("cameraIds: "+cameraIds);
        List<Map<String, Object>> returnMapList = new ArrayList<>();
        if (Objects.isNull(cameraIds) || cameraIds.length()==0) return returnMapList;
        List<Long> list = new ArrayList<>();
        String[] cameraIdArry = cameraIds.split(",");
        for (String aCameraIdArry : cameraIdArry) {
            if (!Objects.equals(aCameraIdArry, "")) {list.add(Long.valueOf(aCameraIdArry));}
        }
        log.info("list: "+list);
        try {
            for (Long cameraId: list) {
                Map<String, Object> cameraFlowMap = redisTemplate.opsForHash().entries("cameraRealFlow:"+cameraId);
                if (Objects.nonNull(cameraFlowMap.get("rtmpUrl"))) {
                    cameraFlowMap.put("cameraId", String.valueOf(cameraId));
                    returnMapList.add(cameraFlowMap);
                } else {
                    CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
                    String userName = cameraConInfo.getIdentityManager();
                    String password = cameraConInfo.getIdentityCode();
                    String cameraIp = cameraConInfo.getRecordIp();
                    int cameraPort = cameraConInfo.getRtspPort();
                    int iChanNum = cameraConInfo.getChannelNum();
                    int cameraType = cameraConInfo.getCameraType();
                    int livePath;
                    if (Objects.nonNull(Constant.maps.get("livePath"))) {
                        livePath = Constant.maps.get("livePath")+1;
                        Constant.maps.put("livePath", livePath);
                    } else {
                        livePath = 123;
                        Constant.maps.put("livePath", 123);
                    }
                    log.info("Constant.maps: "+Constant.maps);
                    log.info(userName+" "+password+" "+cameraIp+" "+cameraPort+" "+iChanNum+" "+cameraType+" "+livePath);
                    String transUrl = "";
                    if (cameraType==205) {
                        transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,videoDefinition, livePath);
                    } else if (cameraType==206) {transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,1, livePath);}
                    Runtime.getRuntime().exec(transUrl);
                    log.info("transUrl: "+transUrl);
                    String[] rtmpUrls = transUrl.split("rtmp");
                    String rtmpUrl = "rtmp"+rtmpUrls[rtmpUrls.length-1];
                    String flvUrl = "http://"+hostIp+":8000/live/"+livePath+".flv";
                    Map<String, Object> returnMap = new HashMap<>();
                    returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
                    returnMap.put("rtmpUrl", rtmpUrl);
                    returnMap.put("flvUrl", flvUrl);
                    returnMapList.add(returnMap);
                    Constant.mapsForCamera.put(String.valueOf(cameraConInfo.getCameraId()), rtmpUrl);
                    redisTemplate.opsForHash().putAll("cameraRealFlow:"+cameraId, returnMap);
                    log.info("mapsForCamera: "+Constant.mapsForCamera);
                }
            }
        } catch (Exception e) {e.getMessage();}
        log.info("returnMapList: "+returnMapList);
        return returnMapList;
    }

    //@Logs(title = "机器人相机播放", code = "robotPlay", content = "机器人相机播放")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> robotStartRealPlay(Long robotId) {
        List<Map<String, Object>> returnMapList = new ArrayList<>();
        RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);

        String lightIp = robotConInfo.getLightIp();
        String lightPort = robotConInfo.getLightPort();
        String lightUsername = robotConInfo.getIdentityManager();
        String lightPassword = robotConInfo.getIdentityCode();
        int livePath =110;
        if (Objects.isNull(Constant.maps.get("livePath"))) {
            livePath = 123;
            Constant.maps.put("livePath", 123);
        }
        if (Objects.nonNull(Constant.maps.get("livePath"))) {
            livePath = Constant.maps.get("livePath")+1;
            Constant.maps.put("livePath", livePath);
        }
        log.info("Constant.maps: "+Constant.maps);
        // /usr/bin/ffmpeg -loglevel error -rtsp_transport tcp -i rtsp://%s:%s@%s:%s/Streaming/Channels/10%s?transportmode=unicast -vcodec copy -an -f flv rtmp://192.168.9.40:1935/live/%s
        log.info("lightInfo: "+lightUsername+" "+lightPassword+" "+lightIp+" "+lightPort+" "+livePath);
        String transUrlLight = String.format(robotLightTem, lightUsername, lightPassword, lightIp, lightPort, 1, livePath);
        try { Runtime.getRuntime().exec(transUrlLight); } catch (Exception e) {e.getMessage();}
        log.info("transUrlLight: "+transUrlLight);
        String[] rtmpUrls = transUrlLight.split("rtmp");
        String rtmpUrl = "rtmp"+rtmpUrls[rtmpUrls.length-1];
        String flvUrl = "http://"+hostIp+":8000/live/"+livePath+".flv";
        Map<String, Object> returnLightMap = new HashMap<>();
        returnLightMap.put("light", String.valueOf(robotId));
        returnLightMap.put("rtmpUrl", rtmpUrl);
        returnLightMap.put("flvUrl", flvUrl);
        Constant.mapsForRobot.put(String.valueOf(robotId)+":light", rtmpUrl);
        log.info("mapsForRobot: "+Constant.mapsForRobot);

        String inferadIp = robotConInfo.getLnferadIp();
        Integer inferadPort = robotConInfo.getInferadPort();
        livePath = Constant.maps.get("livePath")+1;
        Constant.maps.put("livePath", livePath);
        log.info("Constant.maps: "+Constant.maps);
        log.info("inferadInfo: "+" "+inferadIp+" "+inferadPort+" "+livePath);
        String transUrlinferad = String.format(robotInferadTem, inferadIp, inferadPort, livePath);
        try { Runtime.getRuntime().exec(transUrlinferad); } catch (Exception e) {e.getMessage();}
        log.info("transUrlinferad: "+transUrlinferad);
        String[] rtmpUrlsInferad = transUrlinferad.split("rtmp");
        String rtmpUrlInferad = "rtmp"+rtmpUrlsInferad[rtmpUrlsInferad.length-1];
        String flvUrlInferad = "http://"+hostIp+":8000/live/"+livePath+".flv";
        Map<String, Object> returnInferadMap = new HashMap<>();
        returnInferadMap.put("inferad", String.valueOf(robotId));
        returnInferadMap.put("rtmpUrlInferad", rtmpUrlInferad);
        returnInferadMap.put("flvUrlInferad", flvUrlInferad);
        Constant.mapsForRobot.put(String.valueOf(robotId)+":inferad", rtmpUrlInferad);
        log.info("mapsForRobot: "+Constant.mapsForRobot);

        returnMapList.add(returnLightMap);
        returnMapList.add(returnInferadMap);
        log.info("returnMapList: "+returnMapList);
        return returnMapList;
    }

    //@Logs(title = "机器人停止播放", code = "robotStopPlay", content = "机器人相机停止播放")
    @Transactional(rollbackFor = Exception.class)
    public String robotStopRealPlay(Long robotId) {
        String urlStop = null;
        String livePath;
        try {
            String rtmpUrlCamera = Constant.mapsForRobot.get(String.valueOf(robotId+":light"));
            String[] rtmpUrlCameras = rtmpUrlCamera.split("/");
            livePath = rtmpUrlCameras[rtmpUrlCameras.length-1];

            log.info("livePath: "+livePath);
            String url = "ps -ef | grep ffmpeg | grep '"+ livePath +"' | grep -v 'grep'";
            log.info("stopRobotLightUrl: "+url);
            Process processForId=Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
            processForId.waitFor();
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(), "UTF-8"));
            String lineForId = null;
            StringBuilder dataBackForId = new StringBuilder();
            while ((lineForId = readerForId.readLine()) != null) {
                dataBackForId.append(lineForId).append('\n');
            }
            Integer processNum = Integer.parseInt(dataBackForId.substring(9,15).replace(" ",""));
            urlStop = "kill -9 "+processNum;
            Runtime.getRuntime().exec(urlStop);
            Constant.mapsForRobot.remove(String.valueOf(robotId+":light"));

            String rtmpUrlRobot = Constant.mapsForRobot.get(String.valueOf(robotId+":inferad"));
            String[] rtmpUrlRobots = rtmpUrlRobot.split("/");
            livePath = rtmpUrlCameras[rtmpUrlRobots.length-1];
            log.info("livePath: "+livePath);
            String urlRobot = "ps -ef | grep ffmpeg | grep '"+ livePath +"' | grep -v 'grep'";
            log.info("stopRobotInfraedUrl: "+urlRobot);
            Process processForIdRobot=Runtime.getRuntime().exec(new String[]{"sh", "-c", urlRobot});
            processForIdRobot.waitFor();
            BufferedReader readerForIdRobot = new BufferedReader(new InputStreamReader(processForIdRobot.getInputStream(), "UTF-8"));
            String lineForIdRobot = null;
            StringBuilder dataBackForIdRobot = new StringBuilder();
            while ((lineForIdRobot = readerForIdRobot.readLine()) != null) {
                dataBackForIdRobot.append(lineForIdRobot).append('\n');
            }
            Integer processNumRobot = Integer.parseInt(dataBackForIdRobot.substring(9,15).replace(" ",""));
            String urlStopRobot = "kill -9 "+processNumRobot;
            Runtime.getRuntime().exec(urlStopRobot);
            Constant.mapsForRobot.remove(String.valueOf(robotId+":inferad"));
        } catch (Exception e) {e.getMessage();}
        return "stop " + robotId + " preview success!";
    }

    //@Logs(title = "视频回放", code = "cameraPlayBack")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startPlayBack(Long cameraId, String startTime, String stopTime) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
            String userName = cameraConInfo.getIdentityManager();
            String cameraIp = cameraConInfo.getRecordIp();
            String password = cameraConInfo.getIdentityCode();
            int cameraPort = cameraConInfo.getRtspPort();
            int iChanNum = cameraConInfo.getChannelNum();
            int historyPath;
            if (Objects.nonNull(Constant.maps.get("historyPath"))) {
                historyPath = Constant.maps.get("historyPath")+1;
                Constant.maps.put("historyPath", historyPath);
            } else {
                historyPath = 123;
                Constant.maps.put("historyPath", 123);
            }
            log.info("Constant.maps: "+Constant.maps);

            String startTimeTem = startTime.replace("-", "").replace(":", "").replace(" ", "T")+" ";
            String stopTimeTem = stopTime.replace("-", "").replace(":", "").replace(" ", "T")+" ";
            String starttime = startTimeTem.replace(" ", "Z");
            String endtime = stopTimeTem.replace(" ", "Z");
            ///usr/bin/ffmpeg -loglevel error -rtsp_transport tcp -i rtsp://%s:%s@%s:%s/Streaming/tracks/%s0%s?starttime=%s&endtime=%s -vcodec copy -an -f flv rtmp://192.168.9.40:1935/live/%s
            //rtsp://admin:hik12345@192.168.33.2:554/Streaming/tracks/101?starttime=20201111T095500Z&endtime=20201111T100005Z
            log.info("userName: "+userName+",password: "+password+",cameraIp: "+cameraIp+",cameraPort: " +cameraPort
                    +",iChanNum: "+iChanNum +",starttime: "+starttime+",endtime: "+endtime+",historyPath: "+historyPath);
            String transUrl = String.format(UrlBackTem, userName, password, cameraIp, cameraPort, iChanNum,1, starttime, endtime, historyPath);
            log.info("transUrl: "+transUrl);
            Runtime.getRuntime().exec(new String[]{"sh", "-c", transUrl});
            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp"+rtmpUrls[rtmpUrls.length-1];
            String flvUrl = "http://"+hostIp+":8000/history/"+historyPath+".flv";
            returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
            returnMap.put("rtmpUrl", rtmpUrl);
            returnMap.put("flvUrl", flvUrl);
            Constant.mapsForCamera.put(String.valueOf(cameraId), rtmpUrl);
            log.info("mapsForCamera: "+Constant.mapsForCamera);
            log.info("returnMap: "+returnMap);
        } catch (Exception e) {e.getMessage();}
        return returnMap;
    }

    //@Logs(title = "云台控制", code = "cameraControl")
    @Transactional(rollbackFor = Exception.class)
    public Object pTZControl(int dwPTZCommand, Long cameraId, int dStop, int speed) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        m_lRealPlayHandle = realPlay(iChanNum, cameraConInfo.getRecordId());
        if (m_lRealPlayHandle.intValue() == -1) {
            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
            return "fail";
        }
        if (dwPTZCommand==29) {
            return hCNetSDK.NET_DVR_PTZControlWithSpeed(m_lRealPlayHandle, dwPTZCommand, dStop, speed);
        } else {
            if (hCNetSDK.NET_DVR_PTZControlWithSpeed(m_lRealPlayHandle, dwPTZCommand, 0, speed)) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {e.getMessage();}
                hCNetSDK.NET_DVR_PTZControlWithSpeed(m_lRealPlayHandle, dwPTZCommand, 1, speed);
                return hCNetSDK.NET_DVR_StopRealPlay(m_lRealPlayHandle);
            } else { return "PTZ control fail, errorInfo: "+hCNetSDK.NET_DVR_GetLastError(); }
        }

    }

    //@Logs(title = "相机抓图", code = "capturePicture")
    @Transactional(rollbackFor = Exception.class)
    public String capturePicture(String filePath, Long cameraId) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        NativeLong iChanNumLong = new NativeLong(iChanNum);
        HCNetSDK.NET_DVR_JPEGPARA lpJpegPara = new HCNetSDK.NET_DVR_JPEGPARA();
        lpJpegPara.wPicSize = 0xff;
        lpJpegPara.wPicQuality = 1;/* 图片质量系数 0-最好 1-较好 2-一般 */
        if (Objects.nonNull(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())))) {
            lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())));
            log.info("lUserIDLong: "+lUserIDLong);
            if (cameraConInfo.getCameraType()==207) {
                log.info("红外相机，特殊拍照");
                m_sClientInfo.lChannel = new NativeLong(iChanNum);
                if (Objects.nonNull(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())))) {
                    lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())));
                    m_lPort = hCNetSDK.NET_DVR_RealPlay_V30(lUserIDLong, m_sClientInfo, fRealDataCallBack, null, true);
                    log.info("m_lPort: "+m_lPort);
                }
                //打开测温信息
                if (!playCtrl.PlayM4_RenderPrivateData(m_lPort.longValue(), 0x20, 1)) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("PlayM4_RenderPrivateData fail, error code: "+iErr);
                    return "PlayM4_RenderPrivateData, error code: "+iErr;
                }
                if (!playCtrl.PlayM4_RenderPrivateDataEx(m_lPort.longValue(), 0x20, 7, 1)) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("PlayM4_RenderPrivateDataEx fail, error code: "+iErr);
                    return "PlayM4_RenderPrivateDataEx, error code: "+iErr;
                }
                if (!playCtrl.PlayM4_SetOverlayPriInfoFlag(m_lPort.longValue(), 0x20, true)) {
                    int iErr = playCtrl.PlayM4_GetLastError();
                    log.error("PlayM4_SetOverlayPriInfoFlag fail, error code: "+iErr);
                    return "PlayM4_SetOverlayPriInfoFlag, error code: "+iErr;
                }
                //获取播放库未使用的通道号
                if (!playCtrl.PlayM4_GetPort(new NativeLongByReference(m_lPort))) {
                    int iErr = playCtrl.PlayM4_GetLastError();
                    log.error("capture picture fail(PlayM4_GetPort), error code: "+iErr);
                    return "capture picture fail(PlayM4_GetPort), error code: "+iErr;
                }
                if(!playCtrl.PlayM4_Play(m_lPort, null)) {
                    int iErr = playCtrl.PlayM4_GetLastError();
                    log.error("capture picture fail(PlayM4_Play), error code: "+iErr);
                    return "capture picture fail(PlayM4_Play), error code: "+iErr;
                }
                byte[] sJpgPicBuffer = new byte[1920*1080*2];
                IntByReference ipSizeReturned = new IntByReference();
                if(!playCtrl.PlayM4_GetJPEG(m_lPort.longValue(), sJpgPicBuffer, 1920*1080*2, ipSizeReturned)) {
                    int iErr = playCtrl.PlayM4_GetLastError();
                    log.error("capture picture fail(PlayM4_GetJPEG), error code: "+iErr);
                    return "capture picture fail(PlayM4_GetJPEG), error code: "+iErr;
                }
                byteToImage(sJpgPicBuffer, filePath);
                //关闭测温信息
//                if (!playCtrl.PlayM4_RenderPrivateData(iChanNum, 0x20, 0)) {
//                    int iErr = playCtrl.PlayM4_GetLastError();
//                    log.error("PlayM4_RenderPrivateData fail, error code: "+iErr);
//                    return "PlayM4_RenderPrivateData, error code: "+iErr;
//                }
//                if (!playCtrl.PlayM4_RenderPrivateDataEx(iChanNum, 0x20, 7, 0)) {
//                    int iErr = playCtrl.PlayM4_GetLastError();
//                    log.error("PlayM4_RenderPrivateDataEx fail, error code: "+iErr);
//                    return "PlayM4_RenderPrivateDataEx, error code: "+iErr;
//                }
            } else {
                if (!hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserIDLong, iChanNumLong, lpJpegPara, filePath)) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("capture picture fail(NET_DVR_CaptureJPEGPicture), error code: "+iErr);
                    return "capture picture fail(NET_DVR_CaptureJPEGPicture), error code: "+iErr;
                }}
            return "success";
        } else { return "userID is null"; }
    }

    //@Logs(title = "预置点调用", code = "presetAction")
    @Transactional(rollbackFor = Exception.class)
    public boolean PresetAction(Long presetId, Long cameraId, int presetCmd) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,presetId);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        int iPreset = cameraConInfo.getPresetNum();

        hCNetSDK.NET_DVR_PTZPreset_Other(lUserIDLong, iChanNum, presetCmd, iPreset);

//        m_lRealPlayHandle = realPlay(iChanNum, cameraConInfo.getRecordId());
//        if (m_lRealPlayHandle.intValue() == -1) {
//            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
//            return false;
//        }
//        if (!hCNetSDK.NET_DVR_PTZPreset(m_lRealPlayHandle, presetCmd, iPreset)) {
//            log.error("set presetPoint fail, presetId：" + iPreset+", error code: "+hCNetSDK.NET_DVR_GetLastError());
//            return false;
//        }
        if (presetCmd==9) {
            String delPresetPic = "rm -rf "+capturePresetPath + "/"+presetId;
            try { Runtime.getRuntime().exec(delPresetPic); } catch (Exception e) { e.getMessage(); }
        }
        return true;
    }

    //@Logs(title = "获取相机状态", code = "getCameraStatus", content = "获取相机状态信息")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> getCameraStatus(Long recordId) {
        List<CameraStatusInfo> cameraConInfoMap = cameraConDao.cameraInfoByNVR(recordId);
        log.info("cameraConInfoMap: "+cameraConInfoMap);
        NativeLong iChanNumTem = new NativeLong(0);
        Map<String, String> channleStatusMap = new HashMap<>();
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) {
            lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(recordId)));
            IntByReference intByReference = new IntByReference(0);
            HCNetSDK.NET_DVR_IPPARACFG m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG();
            m_strIpparaCfg.write();
            Pointer m_strIpparaCfgPointer = m_strIpparaCfg.getPointer();
            if (!hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_GET_IPPARACFG, iChanNumTem, m_strIpparaCfgPointer, m_strIpparaCfg.size(), intByReference)) {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("get camera status fail, error code: "+iErr);
                channleStatusMap.put("get camera status fail, error code: ", String.valueOf(iErr));
                channleStatusMap.put("errorCode: " ,"401");
                return channleStatusMap;
            }
            m_strIpparaCfg.read();
            //设备支持IP通道
            for(int iChannum =1; iChannum < HCNetSDK.MAX_IP_CHANNEL+1; iChannum++) {
                if (m_strIpparaCfg.struIPChanInfo[iChannum-1].byEnable == 1){
                    for (CameraStatusInfo cameraStatusInfo:cameraConInfoMap) {
                        if (Objects.equals(cameraStatusInfo.getChannelNum(), iChannum)) channleStatusMap.put(String.valueOf(cameraStatusInfo.getCameraId()), "1");
                    }
                }
                if (m_strIpparaCfg.struIPChanInfo[iChannum-1].byEnable == 0) {
                    for (CameraStatusInfo cameraStatusInfo:cameraConInfoMap) {
                        if (Objects.equals(cameraStatusInfo.getChannelNum(), iChannum)) channleStatusMap.put(String.valueOf(cameraStatusInfo.getCameraId()), "0");
                    }
                }

            }
            log.info("channelStatusMap: "+channleStatusMap);
            channleStatusMap.put("errorCode: " ,"200");
            return channleStatusMap;
        }
        channleStatusMap.put("errorMessage: " ,"recordId is null");
        channleStatusMap.put("errorCode: " ,"403");
        return channleStatusMap;
    }

    //@Logs(title = "获取相机树状态", code = "getCameraStatusTree", content = "获取NVR下挂相机树状态")
    @Transactional(rollbackFor = Exception.class)
    public List<CameraAreaInfo> getCameraStatusTree(String cameraName,Integer flag) {
        //if (Objects.nonNull(cameraName)) {}
        List<CameraAreaInfo> tree = new ArrayList<>();
        Map<String,String> state = new HashMap<>();
        List<CameraAreaInfo> listTree = this.cameraConDao.selectCameraTree(cameraName);
        for(Iterator<CameraAreaInfo> it = listTree.iterator();it.hasNext();){
            CameraAreaInfo areaInfoMap = it.next();
            if ( areaInfoMap.getUpId()==-1) {
                CameraAreaInfo areaInfoCountry = new CameraAreaInfo();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setUpId(areaInfoMap.getUpId());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountry.setStatusInfo("1");
                tree.add(areaInfoCountry);
                state.putAll(getCameraStatus(areaInfoMap.getId()));
                //log.info("nvr map：  ",state);
            }
        }
        //log.info("状态map：  ",state);
        diGui(tree, listTree,state,flag);
        return tree;

    }

    private void diGui(List<CameraAreaInfo> areaInfoList, List<CameraAreaInfo> listTree,Map<String,String> state,Integer flag) {
        for(CameraAreaInfo areaInfo : areaInfoList){
            List<CameraAreaInfo> childrenList = new ArrayList<>();
            for(Iterator<CameraAreaInfo> it = listTree.iterator();it.hasNext();){
                CameraAreaInfo areaInfoMap = it.next();
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    CameraAreaInfo areaInfoTem = new CameraAreaInfo();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    areaInfoTem.setLabel(areaInfoMap.getLabel());
                    areaInfoTem.setInfoType(areaInfoMap.getInfoType());
                    areaInfoTem.setUpName(areaInfoMap.getUpName());
                    areaInfoTem.setChannelNum(areaInfoMap.getChannelNum());
                    areaInfoTem.setStatusInfo(state.get(areaInfoMap.getId().toString()));
                    if(flag != null && flag == 1){
                        if("1".equals(state.get(areaInfoMap.getId().toString()))){
                            //在线
                            childrenList.add(areaInfoTem);
                        }else {
                            continue;
                        }
                    }else {
                        childrenList.add(areaInfoTem);
                    }

                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree,state,flag);
            }
        }
    }

//    @Logs(title = "获取NVR存储状态", code = "getNVRStoreInfo", content = "获取NVR存储状态信息")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> getNVRStoreInfo(Long recordId) {
        List<CameraStatusInfo> cameraConInfoMap = cameraConDao.cameraInfoByNVR(recordId);
        log.info("cameraConInfoMap: "+cameraConInfoMap);
        NativeLong iChanNumTem = new NativeLong(0);
        Map<String, String> channleStatusMap = new HashMap<>();
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) {
            lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(recordId)));
            IntByReference ibrBytesReturned = new IntByReference(0);
            HCNetSDK.NET_DVR_HDCFG m_struHDCfg = new HCNetSDK.NET_DVR_HDCFG();
            m_struHDCfg.write();
            Pointer lpPicConfig = m_struHDCfg.getPointer();
            if (!hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_GET_HDCFG, iChanNumTem, lpPicConfig, m_struHDCfg.size(), ibrBytesReturned)) {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("get NVR status fail, error code: "+iErr);
                channleStatusMap.put("get NVR status fail, error code: ", String.valueOf(iErr));
                return channleStatusMap;
            }
            m_struHDCfg.read();
            int hardCapacityTotal = 0;
            int hardFreeTotal = 0;
            for (int i = 0; i < m_struHDCfg.dwHDCount; i++) {
                String s = "硬盘号" + m_struHDCfg.struHDInfo[i].dwHDNo;
                HCNetSDK.NET_DVR_SINGLE_HD netDvrSingleHd = m_struHDCfg.struHDInfo[i];
                int hardNo = netDvrSingleHd.dwHDNo;
                int hardCapacity = netDvrSingleHd.dwCapacity;
                hardCapacityTotal = hardCapacityTotal+hardCapacity;
                int hardFree = netDvrSingleHd.dwFreeSpace;
                hardFreeTotal = hardFreeTotal+hardFree;
                log.info(s+"， hardNo: "+hardNo+", hardCapacity: "+hardCapacity+", hardFree"+hardFree);
            }
            channleStatusMap.put("recorderId", String.valueOf(recordId));
            channleStatusMap.put("capacityTotal", String.valueOf(hardCapacityTotal));
            channleStatusMap.put("freeTotal", String.valueOf(hardFreeTotal));
            return channleStatusMap;
        }
        channleStatusMap.put("errorMessage" ,"录像机不在线");
        return channleStatusMap;
    }

    //@Logs(title = "NVR注册", code = "NVRRegister")
    @Transactional(rollbackFor = Exception.class)
    public String registerNVR(Long recordId) {

        RecorderConInfo recorderConInfo = cameraConDao.selectByRecordId(recordId);
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) lUserID = Constant.maps.get(String.valueOf(recordId));
        log.info("lUserID: "+lUserID);

        if (lUserID > -1) {
            //NVR log out first...
            hCNetSDK.NET_DVR_Logout(lUserID);
            lUserID = -1;
        }
        String m_sDeviceIP = recorderConInfo.getRecordIp();
        String m_sUsername = recorderConInfo.getIdentityManager();
        String m_sPassword = recorderConInfo.getIdentityCode();
        Short m_port = recorderConInfo.getHttpPort().shortValue();
        log.info("register nvr"+recorderConInfo.getRecordName()+", ip is "+m_sDeviceIP+", port is "+m_port);
        //注册
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = m_port;
        m_strLoginInfo.bUseAsynLogin = 0; //是否异步登录：0- 否，1- 是

        m_strLoginInfo.write();
        lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        log.info("m_sDeviceIP: "+m_sDeviceIP+", lUserID: "+lUserID);
        if (lUserID == -1) {
            log.error(recorderConInfo.getRecordName()+" register fail, error code:" + hCNetSDK.NET_DVR_GetLastError());
            return recorderConInfo.getRecordName()+" register fail, error code:" + hCNetSDK.NET_DVR_GetLastError();
        } else {
            Constant.maps.put(String.valueOf(recordId), lUserID);
            log.info("Constant.maps: "+Constant.maps);
            log.info("NVR "+recorderConInfo.getRecordName()+" register success.");
            return "NVR "+recorderConInfo.getRecordName()+" register success.";
        }

    }

    private NativeLong realPlay(int lChannel, long recordId) {
        m_sClientInfo.lChannel = new NativeLong(lChannel);
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) {
            lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(recordId)));
            return hCNetSDK.NET_DVR_RealPlay_V30(lUserIDLong,
                    m_sClientInfo, null, null, true);
        } else { return lUserIDLong;}
    }

    //byte数组到图片
    private void byteToImage(byte[] data,String path){
        if(data.length<3||path.equals("")) return;
        try{
            FileImageOutputStream imageOutput = new FileImageOutputStream(new File(path));
            imageOutput.write(data, 0, data.length);
            imageOutput.flush();
            imageOutput.close();
            log.info("Make Picture success,Please find image in " + path);
        } catch(Exception ex) {
            log.info("Exception: " + ex);
            ex.getMessage();
        }
    }

//    judge is camera controlled. by tt. 1-不可控
//    @Logs(title = "获取相机当前控制状态", code = "isCameraControlled", content = "获取相机当前控制状态")
    @Transactional(rollbackFor = Exception.class)
    public Result isCameraControlled (Long cameraId) {
        Result result = new Result();
        Map<String, Object> camreaStatusMap = redisTemplate.opsForHash().entries("camera_info:"+cameraId);
        log.info("camreaStatusMap: "+camreaStatusMap);
        log.info("camreaStatusMapState: "+camreaStatusMap.get("state"));
        if (Objects.nonNull(camreaStatusMap.get("state"))) {
            Integer state = Integer.parseInt(String.valueOf(camreaStatusMap.get("state")));
            if (Objects.equals(state,1)){
                log.info("unable");
                throw new BusinessException("相机不可控！");
            }
        }
        return result;
    }

//    @Logs(title = "获取到视频存入指定文件中 保存为Mp4格式文件", code = "getDVRConfig", content = "获取到实时视频存入指定文件中 保存为Mp4格式文")
//    public int getDVRToPlace(long cameraId, String startTime, String stopTime) throws ParseException {
//        int judge = 0;
//        try {
//            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
//            //注册
//            registerNVR(cameraConInfo.getRecordId());
//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            //生成文件名
//            String fileName = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()) + ".mp4";
//            log.info("生成文件名" + fileName);
//            //保存文件地址
//            String path = videoPath + fileName;
//            HCNetSDK.NET_DVR_PLAYCOND pDownloadCond = new HCNetSDK.NET_DVR_PLAYCOND();
//            //通道号
//            pDownloadCond.dwChannel = cameraConInfo.getChannelNum();
//            log.info("dwChannel：" + cameraConInfo.getChannelNum());
//            lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())));
//            Calendar c = Calendar.getInstance();
//
//            //开始时间
//            HCNetSDK.NET_DVR_TIME struStartTime = new HCNetSDK.NET_DVR_TIME();
//            HCNetSDK.NET_DVR_TIME struStopTim = new HCNetSDK.NET_DVR_TIME();
//            Date startTimes = format.parse(startTime);//
//            c.setTime(startTimes);
//            log.info("startTime获取到：" + startTime);
//            log.info("stopTime时间转：" + stopTime);
//            struStartTime.dwYear = c.get(java.util.Calendar.YEAR);
//            struStartTime.dwMonth = c.get(java.util.Calendar.MONTH) + 1;
//            struStartTime.dwDay = c.get(java.util.Calendar.DATE);
//            struStartTime.dwHour = c.get(Calendar.HOUR_OF_DAY);
//            struStartTime.dwMinute = c.get(java.util.Calendar.MINUTE);
//            struStartTime.dwSecond = c.get(Calendar.SECOND);
//            pDownloadCond.struStartTime = struStartTime;
//            log.info("pDownloadCond.struStartTime 开始时间结束："+pDownloadCond.struStartTime);
//            //结束时间
//            Date endTimes = format.parse(stopTime);//
//            c.setTime(endTimes);
//            struStopTim.dwYear = c.get(java.util.Calendar.YEAR);
//            struStopTim.dwMonth = c.get(java.util.Calendar.MONTH) + 1;
//            struStopTim.dwDay = c.get(java.util.Calendar.DATE);
//            struStopTim.dwHour = c.get(Calendar.HOUR_OF_DAY);
//            struStopTim.dwMinute = c.get(java.util.Calendar.MINUTE);
//            struStopTim.dwSecond = c.get(Calendar.SECOND);
//            pDownloadCond.struStopTime = struStopTim;
//            log.info(" pDownloadCond.struStopTime结束时间"+ pDownloadCond.struStopTime);
//            log.info("时间赋值成功");
//            NativeLong lChannel = new NativeLong(cameraConInfo.getChannelNum());
//            log.info("lChannel赋值成功:" + lChannel);
//
//            //流ID，使用流ID方式时dwChannel设为0xffffffff
//            //pDownloadCond.byStreamID="0xffffffff".getBytes();
//            //是否抽帧：0- 不抽帧，1- 抽帧
//            //pDownloadCond.byDrawFrame=1;
//            //码流类型：0- 主码流，1- 子码流，2- 码流三
//            pDownloadCond.byStreamType = 0;
//            log.info("lUserIDLong：" + lUserIDLong);
//            log.info("path：" + path);
//
//            //按时间段截取视频
//            NativeLong fileV40 = hCNetSDK.NET_DVR_GetFileByTime_V40(lUserIDLong, path, pDownloadCond);
//            log.info("fileV40：-------" + fileV40);
//            if (fileV40.longValue() < 0)
//            {
//                int iErr = hCNetSDK.NET_DVR_GetLastError();
//                log.error("get camera video path, error code: " + iErr);
//            } else
//            {
//                int iErr ;
//                hCNetSDK.NET_DVR_SetLogToFile(3, "/home/yjh_iot_center/sdklog", false);
//                //使用NET_DVR_GetFileByTime_V40接口必须使用一下播放接口才能下载视频到本地
//                 iErr = hCNetSDK.NET_DVR_GetLastError();
//                log.error("get camera video fileV40大于0: " + iErr);
//               // boolean PlayBackControl = hCNetSDK.NET_DVR_PlayBackControl(fileV40, hCNetSDK.NET_DVR_PLAYSTART, 0, null);
//                boolean PlayBackControl=hCNetSDK.NET_DVR_PlayBackControl_V40(fileV40,hCNetSDK.NET_DVR_PLAYSTART,null,0,null,null);
//                log.info("PlayBackControl：" + PlayBackControl + "dwControlCode:" + hCNetSDK.NET_DVR_PLAYSTART + "dwInValue:" + 0 + "lpOutValue" + hCNetSDK.NET_DVR_GETTOTALTIME);
//                if (PlayBackControl)
//                {
//                    log.info("NET_DVR_PlayBackControl成功调用");
//                    iErr = hCNetSDK.NET_DVR_GetLastError();
//                    log.info("NET_DVR_PlayBackControl成功调用后NET_DVR_GetLastError：" + iErr);
//                    //成功把地址保持在redis中
//                    //IntByReference LPOutValue = new IntByReference();
//                   // log.info("LPOutValue 下载进度:" + LPOutValue);
//                    //hCNetSDK.NET_DVR_PlayBackControl(fileV40, hCNetSDK.NET_DVR_PLAYSTART, 0, null);
//                    hCNetSDK.NET_DVR_PlayBackControl_V40(fileV40,hCNetSDK.NET_DVR_PLAYSTART,null,0,null,null);
//                      Downloadtimer = new Timer();//新建定时器
//                      Downloadtimer.schedule(new DownloadTask(), 0, 5000);//0秒后开始响应函数
//                    log.info("redis put ");
//                        redisTemplate.opsForHash().put("fileV40", "fileV40", fileV40);
//                    log.info("redis put ok "+fileV40);
//                       // log.info("LPOutValue 下载进度:" + LPOutValue.getValue());
//                        redisTemplate.opsForHash().put("vidioPth", "vidioPth", savePath + fileName);
//                        redisTemplate.opsForHash().put("fileName", "fileName", fileName);
//                        String url = "chmod 777 " + savePath + fileName;
//                        Runtime.getRuntime().exec(url);
//                        log.info("Make video success,Please video  in " + savePath + fileName);
//                        judge = 1;
//                } else
//                 {
//                    judge = 0;
//                    iErr = hCNetSDK.NET_DVR_GetLastError();
//                    log.error("get camera video NET_DVR_PlayBackControl, error code: " + iErr);
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return judge;
//    }
@Autowired
private TCameraInfoDao TCameraInfoDao;
    /**
     *下载nvr文件
     * @param cameraId 摄像头id
     * @param startTime 开始时间
     * @param stopTime 结束时间
     */

    public String getDVRToPlace(long cameraId, String startTime, String stopTime) throws ParseException {
        //返回结果
        String  judge = null;
        //错误码
        int iErr = 0;
        try {
            //查询视频参数
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            cameraConInfo.setChannelNum(cameraConInfo.getChannelNum()+32);
            if (hCNetSDK.NET_DVR_Init()) {
                log.info("初始化成功开始注册登录：");
                String login = registerNVR(cameraConInfo.getRecordId());
                if (!login.isEmpty()) {
                    log.info("登录成功：" + login);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //生成文件名
                    String fileName = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date()) + ".mp4";
                    log.info("生成文件名" + fileName);
                    //保存文件地址
                    String path = videoPath + fileName;
                    log.info("保存文件地址：" + path);
                    lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())));
                    log.info("lUserIDLong获取到：" + lUserIDLong);
                    Calendar c = Calendar.getInstance();
                    //开始时间
                    HCNetSDK.NET_DVR_TIME struStartTime = new HCNetSDK.NET_DVR_TIME();
                    HCNetSDK.NET_DVR_TIME struStopTim = new HCNetSDK.NET_DVR_TIME();
                    Date startTimes = format.parse(startTime);
                    c.setTime(startTimes);
                    log.info("startTime获取到：" + startTime);
                    log.info("stopTime时间转：" + stopTime);
                    struStartTime.dwYear = c.get(Calendar.YEAR);
                    struStartTime.dwMonth = c.get(Calendar.MONTH) + 1;
                    struStartTime.dwDay = c.get(Calendar.DATE);
                    struStartTime.dwHour = c.get(Calendar.HOUR_OF_DAY);
                    struStartTime.dwMinute = c.get(Calendar.MINUTE);
                    struStartTime.dwSecond = c.get(Calendar.SECOND);
                    // log.info("struStartTime 开始时间结束："+struStartTime.toString());
                    //结束时间
                    Date endTimes = format.parse(stopTime);
                    c.setTime(endTimes);
                    struStopTim.dwYear = c.get(Calendar.YEAR);
                    struStopTim.dwMonth = c.get(Calendar.MONTH) + 1;
                    struStopTim.dwDay = c.get(Calendar.DATE);
                    struStopTim.dwHour = c.get(Calendar.HOUR_OF_DAY);
                    struStopTim.dwMinute = c.get(Calendar.MINUTE);
                    struStopTim.dwSecond = c.get(Calendar.SECOND);
                    log.info("时间赋值成功");
                    NativeLong lChannel = new NativeLong(cameraConInfo.getChannelNum());
                    log.info("lChannel赋值成功:" + lChannel);
                    //查找文件存在不存在
                    NativeLong findFile= hCNetSDK.NET_DVR_FindFile(lUserIDLong,lChannel,0xff,struStartTime,struStopTim);
                    log.info("findFile查找文件接口:" +"findFile："+findFile+"lUserIDLong:"+lUserIDLong+"lChannel:"+lChannel);
                    if (findFile.longValue()>-1) {
                        //获取时间断的视频接口
                        NativeLong fileByTime = hCNetSDK.NET_DVR_GetFileByTime(lUserIDLong, lChannel, struStartTime, struStopTim, path);
                        log.info("调用NET_DVR_GetFileByTime接口获取到：" + fileByTime+"lUserIDLong:"+lUserIDLong+"lChannel"+lChannel+"path"+path);
                        if (fileByTime.longValue() < 0) {
                            iErr = hCNetSDK.NET_DVR_GetLastError();
                            log.error("get camera video NET_DVR_GetFileByTime, error code: " + iErr);
                            judge = null;
                        } else {
                            if (fileByTime.longValue() < 100) {
                                //  hCNetSDK.NET_DVR_SetLogToFile(3,"/home/yjh_iot_center/sdklog",false);
                                //使用NET_DVR_GetFileByTime_V40接口必须使用一下播放接口才能下载视频到本地
                                boolean PlayBackControl = hCNetSDK.NET_DVR_PlayBackControl(fileByTime, HCNetSDK.NET_DVR_PLAYSTART, 0, null);
                                log.info("PlayBackControl：" + PlayBackControl + "dwControlCode:" + HCNetSDK.NET_DVR_PLAYSTART + "dwInValue:" + 0 + "lpOutValue" + HCNetSDK.NET_DVR_GETTOTALTIME);
                                if (PlayBackControl) {
                                    log.info("NET_DVR_PlayBackControl成功调用");
                                    iErr = hCNetSDK.NET_DVR_GetLastError();
                                    log.info("NET_DVR_PlayBackControl成功调用后NET_DVR_GetLastError：" + iErr);
                                    IntByReference nPos = new IntByReference(0);
                                    while (1 == 1)//获取下载进度，确认下载结束，则停止下载
                                    {
                                        NativeLong m_lLoadHandle = fileByTime;
                                        hCNetSDK.NET_DVR_PlayBackControl(m_lLoadHandle, HCNetSDK.NET_DVR_PLAYGETPOS, 0, nPos);
                                        //log.info("NET_DVR_PlayBackControl循环内接口参数"+"m_lLoadHandle"+m_lLoadHandle+"nPos"+nPos);
                                        if (nPos.getValue() > 100) {
                                            hCNetSDK.NET_DVR_StopGetFile(m_lLoadHandle);
                                            m_lLoadHandle.setValue(-1);
                                            log.info("由于网络原因或DVR忙,下载异常终止!");
                                            judge = null;
                                            break;
                                        } else if (nPos.getValue() == 100) {
                                            hCNetSDK.NET_DVR_StopGetFile(m_lLoadHandle);
                                            m_lLoadHandle.setValue(-1);
                                            log.info("按时间下载结束!");
                                            String url = "chmod 777 " + videoPath + fileName;
                                            Runtime.getRuntime().exec(url);
                                            log.info("nPos.getValue!:>>>" + nPos.getValue());
                                            judge = savePath + fileName;
                                            hCNetSDK.NET_DVR_Logout(m_lLoadHandle);
                                            hCNetSDK.NET_DVR_Cleanup();
                                            break;
                                        }
                                    }

                                } else {
                                    judge = null;
                                    iErr = hCNetSDK.NET_DVR_GetLastError();
                                    log.error("get camera video NET_DVR_PlayBackControl, error code: " + iErr);
                                }
                            } else {
                                judge = null;
                                int rr = hCNetSDK.NET_DVR_GetLastError();
                                log.info("get camera video NET_DVR_GetFileByTime大于100返回, error rr: " + rr);
                            }

                        }
                    }else
                    {
                        judge = null;
                        int rr = hCNetSDK.NET_DVR_GetLastError();
                        log.info("get file by time  NET_DVR_FindFile返回, error rr: " + rr);
                    }

                } else {
                    iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("登录 接口获取到：iErr"+iErr);
                    log.info("视频登录失败！！！");
                    judge = null;

                }
            }
            else
            {
                iErr = hCNetSDK.NET_DVR_GetLastError();
                log.info("调用NET_DVR_Init 接口获取到："+iErr);
                log.info("视频初始化失败！！！");
                judge = null;
            }
        } catch (Exception e) {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("获取视频方发异常：");
            judge = null;
        }
        return judge;
    }

    /**
     * 获取摄像头文件列表
     * @param cameraId 摄像头id
     * @param startTime 开始时间
     * @param stopTime 结束时间
     */
    public    List<HCNetSDK.NET_DVR_FIND_DATA> geifile(long cameraId, String startTime, String stopTime)
    {
        List< HCNetSDK.NET_DVR_FIND_DATA> list =new ArrayList< HCNetSDK.NET_DVR_FIND_DATA>();
       // List<Vector<String>> list =new ArrayList<Vector<String>>();
    try {

        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        cameraConInfo.setChannelNum(cameraConInfo.getChannelNum() + 32);
        if (hCNetSDK.NET_DVR_Init()) {
            log.info("初始化成功开始注册登录：");
            String login = registerNVR(cameraConInfo.getRecordId());
            if (!login.isEmpty()) {
                log.info("登录成功：" + login);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())));
                log.info("lUserIDLong获取到：" + lUserIDLong);
                Calendar c = Calendar.getInstance();
                //开始时间
                HCNetSDK.NET_DVR_TIME struStartTime = new HCNetSDK.NET_DVR_TIME();
                HCNetSDK.NET_DVR_TIME struStopTim = new HCNetSDK.NET_DVR_TIME();
                Date startTimes = format.parse(startTime);
                c.setTime(startTimes);
                log.info("startTime获取到：" + startTime);
                log.info("stopTime时间转：" + stopTime);
                struStartTime.dwYear = c.get(Calendar.YEAR);
                struStartTime.dwMonth = c.get(Calendar.MONTH) + 1;
                struStartTime.dwDay = c.get(Calendar.DATE);
                struStartTime.dwHour = c.get(Calendar.HOUR_OF_DAY);
                struStartTime.dwMinute = c.get(Calendar.MINUTE);
                struStartTime.dwSecond = c.get(Calendar.SECOND);
                // log.info("struStartTime 开始时间结束："+struStartTime.toString());
                //结束时间
                Date endTimes = format.parse(stopTime);
                c.setTime(endTimes);
                struStopTim.dwYear = c.get(Calendar.YEAR);
                struStopTim.dwMonth = c.get(Calendar.MONTH) + 1;
                struStopTim.dwDay = c.get(Calendar.DATE);
                struStopTim.dwHour = c.get(Calendar.HOUR_OF_DAY);
                struStopTim.dwMinute = c.get(Calendar.MINUTE);
                struStopTim.dwSecond = c.get(Calendar.SECOND);
                log.info("时间赋值成功");
                NativeLong lChannel = new NativeLong(cameraConInfo.getChannelNum());
                log.info("lChannel赋值成功:" + lChannel);
                //查找文件存在不存在
                NativeLong findFile = hCNetSDK.NET_DVR_FindFile(lUserIDLong, lChannel, 0xff, struStartTime, struStopTim);
                log.info("findFile查找文件接口:" +"findFile-->>>："+findFile.longValue());
                log.info("findFile查找文件接口:" +"findFile-->>>："+findFile.longValue()+"<<<<lUserIDLong:"+lUserIDLong+"lChannel:"+lChannel);
                HCNetSDK.NET_DVR_FIND_DATA FindData=new  HCNetSDK.NET_DVR_FIND_DATA();
                if (findFile.longValue()>-1)
                {
                    log.info("NET_DVR_FindFile接口请求成功findFile:" + findFile.longValue());
                    FindData = new HCNetSDK.NET_DVR_FIND_DATA();
                            // 文件列表接口
                    NativeLong findNextFile = hCNetSDK.NET_DVR_FindNextFile(findFile, FindData);
                    log.info("findNextFile>>:"+findNextFile.longValue());
                    if(findNextFile.longValue()>-1)
                    {
                        log.info("NET_DVR_FindNextFile接口请求成功findFile:" + findNextFile.longValue());
                        //当找到录像文件时接口将返回1000，当没有查找到文件或查找结束将返回1003或者1004，返回1002表示当前正在查找
                        while (findNextFile.longValue() != HCNetSDK.NET_DVR_NOMOREFILE &&findNextFile.longValue() != HCNetSDK.NET_DVR_FILE_NOFIND)
                        {
                            switch ((int) findNextFile.longValue()) {
                                case HCNetSDK.NET_DVR_FILE_SUCCESS:
                                    log.info("NET_DVR_FindNextFile接口请求成功:"+findNextFile.longValue());
                                    HCNetSDK.NET_DVR_FIND_DATA savedate = new HCNetSDK.NET_DVR_FIND_DATA();
                                    savedate.sFileName = FindData.sFileName;
                                    savedate.dwFileSize = FindData.dwFileSize;
                                    savedate.struStartTime = FindData.struStartTime;
                                    savedate.struStopTime = FindData.struStopTime;
                                    list.add(savedate);
                                    findNextFile = hCNetSDK.NET_DVR_FindNextFile(findFile,FindData);
                                    break;
                                case  HCNetSDK.NET_DVR_FILE_NOFIND:
                                    list=null;
                                    log.info("没有文件！");
                                    break;

                                case  HCNetSDK.NET_DVR_ISFINDING:
                                    findNextFile = hCNetSDK.NET_DVR_FindNextFile(findFile,FindData);
                                    break;
                                case HCNetSDK.NET_DVR_NOMOREFILE:
                                    log.info("查找完毕！");
                                    break;
                                default:
                                    list=null;
                                     log.info("查找文件时异常");
                                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                                    log.info("查找文件时候异常iErr:"+iErr);
                                    break;
                            }
                        }
                        //释放资源
                        hCNetSDK.NET_DVR_Logout(lUserIDLong);
                        hCNetSDK.NET_DVR_Cleanup();
                    }else
                    {
                        list=null;
                        int iErr = hCNetSDK.NET_DVR_GetLastError();
                        log.info("查找下一个文件是失败iErr:"+iErr);
                    }

                }else
                    {
                        list=null;
                        int iErr = hCNetSDK.NET_DVR_GetLastError();
                        log.info("文件不存在:"+iErr);
                    }
            }
        }
        }catch (Exception e)
        {
            list=null;
            log.error( e.getMessage());
        }
    return  list;
    }

    /**
     * 获取全画面最高温度
     * @param cameraId 摄像头id
     * @return
     */
    public List<String> getTemperature(long cameraId)
    {

        List<String> list=new ArrayList<String>();
       try {

           /*if (lUserIDLong.longValue()<0)
           {
               hCNetSDK.NET_DVR_Init();
               TCameraInfo tCameraInfo =TCameraInfoDao.selectCamera(cameraId);
               tCameraInfo.setChannelNum(tCameraInfo.getChannelNum()+32);
               m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
               System.arraycopy(tCameraInfo.getCameraIp().getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, tCameraInfo.getCameraIp().length());
               m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
               System.arraycopy(tCameraInfo.getUrl().getBytes(), 0, m_strLoginInfo.sUserName, 0, tCameraInfo.getUrl().length());
               m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
               System.arraycopy(tCameraInfo.getUnit().getBytes(), 0, m_strLoginInfo.sPassword, 0, tCameraInfo.getUnit().length());
               m_strLoginInfo.wPort =tCameraInfo.getPort().shortValue() ;
               m_strLoginInfo.bUseAsynLogin = 0; //是否异步登录：0- 否，1- 是
               m_strLoginInfo.write();
               log.info("登录账号:"+tCameraInfo.getUrl().getBytes()+"ip地址"+tCameraInfo.getCameraIp()+"登录密码"+tCameraInfo.getUnit()+"端口号"+tCameraInfo.getChannelNum());
               int i=hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
               log.info("NET_DVR_Login_V40:返回值"+i);
               lUserIDLong = new NativeLong(i);
               log.info("lUserIDLong:返回值"+lUserIDLong);
           }*/
         CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
           cameraConInfo.setChannelNum(cameraConInfo.getChannelNum()+32);
           log.info("通道号："+cameraConInfo.getChannelNum());
           log.info("getRecordId:"+cameraConInfo.getRecordId());
               lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())));
               log.info("lUserIDLong"+lUserIDLong);
                   HCNetSDK.NET_DVR_JPEGPICTURE_WITH_APPENDDATA   m_strJpegWithAppenData = new HCNetSDK.NET_DVR_JPEGPICTURE_WITH_APPENDDATA();
                   m_strJpegWithAppenData.dwSize = m_strJpegWithAppenData.size();
                   m_strJpegWithAppenData.dwChannel =1;
                   HCNetSDK.BYTE_ARRAY ptrJpegByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 *1024);
                   HCNetSDK.BYTE_ARRAY ptrP2PDataByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 *1024);
                   m_strJpegWithAppenData.pJpegPicBuff =ptrJpegByte.getPointer();
                   m_strJpegWithAppenData.pP2PDataBuff = ptrP2PDataByte.getPointer();
                   log.info("m_strJpegWithAppenData的值:"+m_strJpegWithAppenData.toString());
                   boolean bRet =hCNetSDK.NET_DVR_CaptureJPEGPicture_WithAppendData(lUserIDLong,2, m_strJpegWithAppenData);
                   log.info("bRet返回值："+bRet);
                   if (bRet)
                   {
                       if (m_strJpegWithAppenData.dwP2PDataLen>0)
                       {
                           list.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                           byte [] byTempData = new byte[4];
                           float[] arr =new float[m_strJpegWithAppenData.dwP2PDataLen];
                           for (int i = 1 ;i<= m_strJpegWithAppenData.dwJpegPicWidth;i++)
                           {
                               for (int j = 1 ;j<= m_strJpegWithAppenData.dwJpegPicHeight;j++)
                               {
                                   ByteBuffer TempDatabuffers = m_strJpegWithAppenData.pP2PDataBuff.getByteBuffer((i-1)*(j-1)*4, 4);
                                   TempDatabuffers.get(byTempData);
                                   int l;
                                   l = byTempData[0];
                                   l &= 0xff;
                                   l |= ((long) byTempData[1] << 8);
                                   l &= 0xffff;
                                   l |= ((long) byTempData[ 2] << 16);
                                   l &= 0xffffff;
                                   l |= ((long) byTempData[3] << 24);
                                   arr[i]=Float.intBitsToFloat(l);
                                  //log.info("行数：" + i +"列数：" + j + "温度数据：" +   Float.intBitsToFloat(l) );
                               }
                           }
                           //获取最大温度值
                           float max=arr[0];
                           for (int i=0;i<arr.length;i++){
                               //4.把获取到的数据一次和temp进行比较，并将最大的值赋值给temp
                               if(arr[i]>max){
                                   max=arr[i];
                               }
                           }
                           //转换保留后两位小数
                           list.add( new  DecimalFormat("##0.00").format(max)+"℃");

                       }else
                           {
                               list=null;
                               int iErr = hCNetSDK.NET_DVR_GetLastError();
                               log.info("温度不存在"+iErr);
                           }

                   }else{
                       int iErr = hCNetSDK.NET_DVR_GetLastError();
                       log.info("温度获取失败错误码"+iErr);
                   }

       }catch (Exception e){
           int iErr = hCNetSDK.NET_DVR_GetLastError();
           log.info("系统异常"+iErr);
       }finally {
           hCNetSDK.NET_DVR_Logout(lUserIDLong);
           hCNetSDK.NET_DVR_Cleanup();
       }
        return list;
    }



    /**
     * 获取测温数据和图片
     * @param cameraId 摄像头id
     *  @param presetId 预置位id
     * @return
     */
    public Map<String,String> givePicFir(long cameraId,Long presetId)
    {
        Map<String,String> map=new HashMap<String,String>();

        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
          /*  if (lUserIDLong.longValue()<0)
            {
                hCNetSDK.NET_DVR_Init();
                TCameraInfo tCameraInfo =TCameraInfoDao.selectCamera(cameraId);
                tCameraInfo.setChannelNum(tCameraInfo.getChannelNum()+32);
                m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
                System.arraycopy(tCameraInfo.getCameraIp().getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, tCameraInfo.getCameraIp().length());
                m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
                System.arraycopy(tCameraInfo.getUrl().getBytes(), 0, m_strLoginInfo.sUserName, 0, tCameraInfo.getUrl().length());
                m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
                System.arraycopy(tCameraInfo.getUnit().getBytes(), 0, m_strLoginInfo.sPassword, 0, tCameraInfo.getUnit().length());
                m_strLoginInfo.wPort =tCameraInfo.getPort().shortValue() ;
                m_strLoginInfo.bUseAsynLogin = 0; //是否异步登录：0- 否，1- 是
                m_strLoginInfo.write();
                log.info("登录账号:"+tCameraInfo.getUrl().getBytes()+"ip地址"+tCameraInfo.getCameraIp()+"登录密码"+tCameraInfo.getUnit()+"端口号"+tCameraInfo.getChannelNum());
                int i=hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
                log.info("NET_DVR_Login_V40:返回值"+i);
                lUserIDLong = new NativeLong(i);
                log.info("lUserIDLong:返回值"+lUserIDLong);
            }*/
           cameraConInfo.setChannelNum(cameraConInfo.getChannelNum()+32);
            log.info("通道号："+cameraConInfo.getChannelNum());
            log.info("getRecordId:"+cameraConInfo.getRecordId());
            lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())));
            log.info("lUserIDLong"+lUserIDLong);
            //转到预置点
            boolean Preset=  hCNetSDK.NET_DVR_PTZPreset_Other(lUserIDLong, 2, HCNetSDK.GOTO_PRESET, cameraConInfo.getPresetNum());
            if (Preset){
                log.info("转到预置点成功：Preset->"+Preset);
            }else
                {
                    log.info("转到预置点信息Preset->"+Preset);
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("转到预置点失败错误码"+iErr);
                }

            HCNetSDK.NET_DVR_JPEGPICTURE_WITH_APPENDDATA   m_strJpegWithAppenData = new HCNetSDK.NET_DVR_JPEGPICTURE_WITH_APPENDDATA();
            m_strJpegWithAppenData.dwSize = m_strJpegWithAppenData.size();
            m_strJpegWithAppenData.dwChannel =1;
            HCNetSDK.BYTE_ARRAY ptrJpegByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 *1024);
            HCNetSDK.BYTE_ARRAY ptrP2PDataByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 *1024);
            m_strJpegWithAppenData.pJpegPicBuff =ptrJpegByte.getPointer();
            m_strJpegWithAppenData.pP2PDataBuff = ptrP2PDataByte.getPointer();
           // log.info("m_strJpegWithAppenData的值:"+m_strJpegWithAppenData.toString());
            boolean bRet =hCNetSDK.NET_DVR_CaptureJPEGPicture_WithAppendData(lUserIDLong,2, m_strJpegWithAppenData);
            log.info("bRet返回值："+bRet);
            if (bRet)
            {
                FileOutputStream fout;
                String newName   = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date());
                //测温图片
                if(m_strJpegWithAppenData.dwJpegPicLen>0)
                {
                    String path= hotPic+newName+".jpg";
                    log.info("hotPic地址："+path);
                    fout= new FileOutputStream(path);
                        //将字节写入文件
                        long offset = 0;
                        ByteBuffer buffers = m_strJpegWithAppenData.pJpegPicBuff.getByteBuffer(offset, m_strJpegWithAppenData.dwJpegPicLen);
                        byte [] bytes = new byte[m_strJpegWithAppenData.dwJpegPicLen];
                        buffers.rewind();
                        buffers.get(bytes);
                        fout.write(bytes);
                        fout.close();
                        map.put("picPath",hotPicshow+newName+".jpg");
                    log.info("hotPicshow地址："+hotPicshow+newName+".jpg");
                }else
                    {
                        map=null;
                        int iErr = hCNetSDK.NET_DVR_GetLastError();
                        log.info("图片获取失败错误码"+iErr);
                    }
                //测温数据data
                if(m_strJpegWithAppenData.dwP2PDataLen>0)
                {
                    String path= hotFir+newName+".data";
                    log.info("hotFirdata地址："+path);
                    fout= new FileOutputStream(path);
                    //将字节写入文件
                    long offset = 0;
                    ByteBuffer buffers = m_strJpegWithAppenData.pP2PDataBuff.getByteBuffer(offset, m_strJpegWithAppenData.dwP2PDataLen);
                    byte [] bytes = new byte[m_strJpegWithAppenData.dwP2PDataLen];
                    buffers.rewind();
                    buffers.get(bytes);
                    fout.write(bytes);
                    fout.close();
                    map.put("dataPath",hotFirShow+newName+".data");
                    log.info("hotFirShowdata地址："+hotFirShow+newName+".data");
                }else
                    {
                        int iErr = hCNetSDK.NET_DVR_GetLastError();
                        log.info("data获取失败错误码"+iErr);
                    }
                //测温数据cvs
                if(m_strJpegWithAppenData.dwP2PDataLen>0)
                {
                    String path= hotFir+newName+".csv";
                    log.info("hotFircsv地址："+path);
                    byte [] byTempData = new byte[4];
                    FileWriter fos=new FileWriter (path);
                    for (int i = 1 ;i<= m_strJpegWithAppenData.dwJpegPicWidth;i++)
                    {
                        for (int j = 1 ;j<= m_strJpegWithAppenData.dwJpegPicHeight;j++)
                        {
                            ByteBuffer buffers = m_strJpegWithAppenData.pP2PDataBuff.getByteBuffer((i-1)*(j-1)*4, 4);
                            buffers.get(byTempData);
                            int l;
                            l = byTempData[0];
                            l &= 0xff;
                            l |= ((long) byTempData[1] << 8);
                            l &= 0xffff;
                            l |= ((long) byTempData[ 2] << 16);
                            l &= 0xffffff;
                            l |= ((long) byTempData[3] << 24);
                            fos.write(String.valueOf(Float.intBitsToFloat(l)));
                            fos.write(",");
                        }
                        fos.append('\n');
                    }
                    fos.flush();
                    fos.close();
                    map.put("csvPath",hotFirShow+newName+".csv");
                    log.info("hotFirShowcsv地址："+hotFirShow+newName+".csv");

                }else
                {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("csv获取失败错误码"+iErr);
                }

            }else
                {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("红外温感文件获取接口调用失败"+iErr);
                }

        }catch (Exception e)
        {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("系统异常"+iErr);
        }
        return map;
    }


    /**
     * 语音对讲开始
     * @param videoIntercomId 门口机id
     */
    public int startVoiceTalk(Long videoIntercomId)
    {

        int re=1;
        try {
          String url= SERVICE_URL+"?videoIntercomId="+videoIntercomId;
            String services =HttpClientUtils.getInstance().getUrl(SERVICE_URL, null);
            JSONObject jsonObject =JSONObject.parseObject(services);
            Map<String,String> videoIntercom= (Map<String,String>) jsonObject.get("data");

          //VideoIntercom  videoIntercom=  VideoIntercomDao.selectByPrimaryId(videoIntercomId);
            if (! hCNetSDK.NET_DVR_Init())
            {
                log.info("语音对讲初始化失败");
            }
            m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
            System.arraycopy(videoIntercom.get("cameraIp").getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, videoIntercom.get("cameraIp").length());
            m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
            System.arraycopy(videoIntercom.get("owner").getBytes(), 0, m_strLoginInfo.sUserName, 0, videoIntercom.get("owner").length());
            m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
            System.arraycopy(videoIntercom.get("ownerCode").getBytes(), 0, m_strLoginInfo.sPassword, 0, videoIntercom.get("ownerCode").length());
            m_strLoginInfo.wPort =Short.decode(videoIntercom.get("port")) ;
            m_strLoginInfo.bUseAsynLogin = 0; //是否异步登录：0- 否，1- 是
            m_strLoginInfo.write();
            log.info("登录账号:"+videoIntercom.get("owner")+"ip地址"+videoIntercom.get("cameraIp")+"登录密码"+videoIntercom.get("ownerCode")+"端口号"+videoIntercom.get("port"));
               int i=hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
               log.info("NET_DVR_Login_V40:返回值"+i);
               lUserIDLong = new NativeLong(i);
            log.info("lUserIDLong:返回值"+lUserIDLong);
            if (mVoiceTalkHandle.longValue() < 0 )
            {
                mVoiceTalkHandle = hCNetSDK.NET_DVR_StartVoiceCom_V30(lUserIDLong, 1, false, null,  null);
                log.info("mVoiceTalkHandle:返回值"+mVoiceTalkHandle);
                if (mVoiceTalkHandle.longValue() == -1)
                {
                    re=0;
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("NET_DVR_StartVoiceCom_V30 failed"+iErr);
                }
                else
                {
                    re=1;
                    log.info( "NET_DVR_StartVoiceCom_V30 SUCC");
                }
            }
            else
            {
                re=1;
                log.info( "NET_DVR_StartVoiceCom_V30 SUCC:开启对讲");
            }
        }catch (Exception e) {
            re=0;
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("NET_DVR_StartVoiceCom_V30 failed"+iErr);
            log.info("NET_DVR_StartVoiceCom_V30 ....."+e.getMessage());
        }
        return re;
    }

    /**
     * 语音对讲结束
     */

    public void stopVideoCallSession()
    {
        try {
            if(mVoiceTalkHandle.longValue() >= 0) {
                boolean res=  hCNetSDK.NET_DVR_StopRemoteConfig(mVoiceTalkHandle.intValue());
                if (res)
                {
                    log.info("NET_DVR_StopRemoteConfig SUCC ");
                }else
                    {
                        int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("NET_DVR_StopRemoteConfig failed "+iErr);
                }

                mVoiceTalkHandle  =new  NativeLong(-1);
            }

        }catch (Exception e)
        {
            log.info("NET_DVR_StopRemoteConfig failed "+e.getMessage());
        }
    }






}

