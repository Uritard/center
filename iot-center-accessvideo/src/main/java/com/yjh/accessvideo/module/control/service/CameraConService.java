package com.yjh.accessvideo.module.control.service;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.logs.Logs;
import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.hik.PlayCtrl;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.stream.FileImageOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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

    private int lUserID;//用户句柄
    //设备登录信息
    private HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
    //设备信息
    private HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

    @Logs(title = "相机播放", code = "cameraPlay")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startRealPlay(Long cameraId) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
            String userName = cameraConInfo.getUserName();
            String password = cameraConInfo.getPwd();
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
            log.info("mapsForCamera: "+Constant.mapsForCamera);
            log.info("returnMap: "+returnMap);
        } catch (Exception e) {e.getMessage();}
        return returnMap;
    }

    @Logs(title = "相机停止播放", code = "cameraStopPlay")
    @Transactional(rollbackFor = Exception.class)
    public String stopRealPlay(Long cameraId, String rtmpUrl) {
        String urlStop = null;
        String livePath;
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

    @Logs(title = "相机批量播放", code = "cameraPlay", content = "相机批量播放")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> batchStartRealPlay(String cameraIds) {
        log.info("cameraIds: "+cameraIds);
        List<Map<String, Object>> returnMapList = new ArrayList<>();
        if (Objects.isNull(cameraIds) || cameraIds.length()==0) return returnMapList;
        List<Long> list = new ArrayList<>();
        String[] cameraIdArry = cameraIds.split(",");
        for (String aCameraIdArry : cameraIdArry) list.add(Long.valueOf(aCameraIdArry));
        log.info("list: "+list);
        try {
            for (Long cameraId: list) {
                CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
                String userName = cameraConInfo.getUserName();
                String password = cameraConInfo.getPwd();
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
                log.info("mapsForCamera: "+Constant.mapsForCamera);
            }
        } catch (Exception e) {e.getMessage();}
        log.info("returnMapList: "+returnMapList);
        return returnMapList;
    }

    @Logs(title = "机器人相机播放", code = "robotPlay", content = "机器人相机播放")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> robotStartRealPlay(Long robotId) {
        List<Map<String, Object>> returnMapList = new ArrayList<>();
        RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);

        String lightIp = robotConInfo.getLightIp();
        String lightPort = robotConInfo.getLightPort();
        String lightUsername = robotConInfo.getLightUsername();
        String lightPassword = robotConInfo.getLightPassword();
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

    @Logs(title = "机器人停止播放", code = "robotStopPlay", content = "机器人相机停止播放")
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

    @Logs(title = "视频回放", code = "cameraPlayBack")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startPlayBack(Long cameraId, String startTime, String stopTime) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
            String userName = cameraConInfo.getUserName();
            String cameraIp = cameraConInfo.getRecordIp();
            String password = cameraConInfo.getPwd();
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

    @Logs(title = "云台控制", code = "cameraControl")
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

    @Logs(title = "相机抓图", code = "capturePicture")
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

    @Logs(title = "预置点调用", code = "presetAction")
    @Transactional(rollbackFor = Exception.class)
    public boolean PresetAction(Long presetId, Long cameraId, int presetCmd) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,presetId);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        int iPreset = cameraConInfo.getPresetNum();
        m_lRealPlayHandle = realPlay(iChanNum, cameraConInfo.getRecordId());
        if (m_lRealPlayHandle.intValue() == -1) {
            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        if (!hCNetSDK.NET_DVR_PTZPreset(m_lRealPlayHandle, presetCmd, iPreset)) {
            log.error("set presetPoint fail, presetId：" + iPreset+", error code: "+hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        if (presetCmd==9) {
            String delPresetPic = "rm -rf "+capturePresetPath + "/"+presetId;
            try { Runtime.getRuntime().exec(delPresetPic); } catch (Exception e) { e.getMessage(); }
        }
        return true;
    }

    @Logs(title = "获取相机状态", code = "getCameraStatus", content = "获取相机状态信息")
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
            return channleStatusMap;
        }
        channleStatusMap.put("errorMessage: " ,"userID is null");
        return channleStatusMap;
    }

    @Logs(title = "获取相机树状态", code = "getCameraStatusTree", content = "获取NVR下挂相机树状态")
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

    @Logs(title = "获取NVR存储状态", code = "getNVRStoreInfo", content = "获取NVR存储状态信息")
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
                log.error("get camera status fail, error code: "+iErr);
                channleStatusMap.put("get camera status fail, error code: ", String.valueOf(iErr));
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
            channleStatusMap.put("recordId", String.valueOf(recordId));
            channleStatusMap.put("capacityTotal", String.valueOf(hardCapacityTotal));
            channleStatusMap.put("freeTotal", String.valueOf(hardFreeTotal));
            return channleStatusMap;
        }
        channleStatusMap.put("errorMessage: " ,"userID is null");
        return channleStatusMap;
    }

    @Logs(title = "NVR注册", code = "NVRRegister")
    @Transactional(rollbackFor = Exception.class)
    public String registerNVR(Long recordId) {

        RecorderConInfo recorderConInfo = cameraConDao.selectByRecordId(recordId);

        if (lUserID > -1) {
            //NVR log out first...
            hCNetSDK.NET_DVR_Logout(lUserID);
            lUserID = -1;
        }
        String m_sDeviceIP = recorderConInfo.getRecordIp();
        String m_sUsername = recorderConInfo.getUserName();
        String m_sPassword = recorderConInfo.getPwd();
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

//    judge is camera controlled. by tt.
    public Result isCameraControlled (Long cameraId) {
        Result result = new Result();
        List<Long> unableCameraList = cameraConDao.selectUnableCameraIds();
        for (Long unableCameraId:unableCameraList) {
            if (Objects.equals(unableCameraId, cameraId)) {
                throw new BusinessException("this camera is unable to control.");
            }
        }
        return result;
    }

}

