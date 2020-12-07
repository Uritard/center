package com.yjh.accessvideo.module.control.service;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.logs.Logs;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.hik.PlayCtrl;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.CameraConInfo;
import com.yjh.accessvideo.module.control.entity.CameraStatusInfo;
import com.yjh.accessvideo.module.control.entity.RecorderConInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
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
    private RedisTemplate redisTemplate;
    @Autowired
    private CameraConDao cameraConDao;
    @Value("${nvr.rtmp.video}")
    private String UrlTem;

    @Value("${nvr.rtmp.back}")
    private String UrlBackTem;

    @Value("${nvr.capture.Preset}")
    private String capturePresetPath;//预置位路径

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    private static PlayCtrl playCtrl = PlayCtrl.INSTANCE;
    private NativeLong m_lRealPlayHandle = new NativeLong(-1);// playhandle
    private NativeLong m_minsOne = new NativeLong(-1);
    private NativeLong lUserIDLong = new NativeLong(-1);
    private HCNetSDK.NET_DVR_CLIENTINFO m_sClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();	// play structure
    private HCNetSDK.NET_DVR_PREVIEWINFO dvr_previewinfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
    private NativeLong m_lPort =  new NativeLong(-1);
    FRealDataCallBack fRealDataCallBack = new FRealDataCallBack();


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
                Constant.maps.put("livePath", Constant.maps.get("livePath")+1);
            } else {
                livePath = 123;
                Constant.maps.put("livePath", 123);
            }
            log.info("Constant.maps: "+Constant.maps);
            log.info(userName+" "+password+" "+cameraIp+" "+cameraPort+" "+iChanNum+" "+cameraType+" "+livePath);
            String transUrl = "";
            if (cameraType==205) {
                transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,2, livePath);
            } else if (cameraType==206) {transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,1, livePath);}
            Runtime.getRuntime().exec(transUrl);
            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp"+rtmpUrls[rtmpUrls.length-1];
            returnMap.put("rtmpUrl", rtmpUrl);
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
        } catch (Exception e) {e.getMessage();}
        return "stop " + cameraId + " preview success!";
    }

    @Logs(title = "相机批量播放", code = "cameraPlay")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> batchStartRealPlay(String cameraIds) {
        List<Long> list = new ArrayList<>();
        String[] cameraIdArry = cameraIds.split(",");
        for (String aCameraIdArry : cameraIdArry) list.add(Long.valueOf(aCameraIdArry));
        List<Map<String, Object>> returnMapList = new ArrayList<>();
        try {
            List<CameraConInfo> cameraConInfoList = cameraConDao.batchSelectConInfo(list);
            for (CameraConInfo cameraConInfo: cameraConInfoList) {
                String userName = cameraConInfo.getUserName();
                String password = cameraConInfo.getPwd();
                String cameraIp = cameraConInfo.getRecordIp();
                int cameraPort = cameraConInfo.getRtspPort();
                int iChanNum = cameraConInfo.getChannelNum();
                int cameraType = cameraConInfo.getCameraType();
                int livePath;
                if (Objects.nonNull(Constant.maps.get("livePath"))) {
                    livePath = Constant.maps.get("livePath")+1;
                    Constant.maps.put("livePath", Constant.maps.get("livePath")+1);
                } else {
                    livePath = 123;
                    Constant.maps.put("livePath", 123);
                }
                log.info("Constant.maps: "+Constant.maps);
                log.info(userName+" "+password+" "+cameraIp+" "+cameraPort+" "+iChanNum+" "+cameraType+" "+livePath);
                String transUrl = "";
                if (cameraType==205) {
                    transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,2, livePath);
                } else if (cameraType==206) {transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,1, livePath);}
                Runtime.getRuntime().exec(transUrl);
                String[] rtmpUrls = transUrl.split("rtmp");
                String rtmpUrl = "rtmp"+rtmpUrls[rtmpUrls.length-1];
                Map<String, Object> returnMap = new HashMap<>();
                returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
                returnMap.put("rtmpUrl", rtmpUrl);
                returnMapList.add(returnMap);
                Constant.mapsForCamera.put(String.valueOf(cameraConInfo.getCameraId()), rtmpUrl);
                log.info("mapsForCamera: "+Constant.mapsForCamera);
            }
        } catch (Exception e) {e.getMessage();}
        log.info("returnMapList: "+returnMapList);
        return returnMapList;
    }

    @Logs(title = "视频回放", code = "cameraPlayBack")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startPlayBack(Long cameraId, Date startTime, Date stopTime) {
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
                Constant.maps.put("historyPath", Constant.maps.get("historyPath")+1);
            } else {
                historyPath = 123;
                Constant.maps.put("historyPath", 123);
            }
            log.info("Constant.maps: "+Constant.maps);
            int yearStart = startTime.getYear()+1900;
            int monthStart = startTime.getMonth()+1;
            int dateStart = startTime.getDate();
            int hourStart = startTime.getHours();
            int minuteStart = startTime.getMinutes();
            int secondStart = startTime.getSeconds();
            String starttime = yearStart+monthStart+dateStart+"T"+hourStart+minuteStart+secondStart+"Z";

            int yearStop = stopTime.getYear()+1900;
            int monthStop = stopTime.getMonth()+1;
            int dateStop = stopTime.getDate();
            int hourStop = stopTime.getHours();
            int minuteStop = stopTime.getMinutes();
            int secondStop = stopTime.getSeconds();
            String endtime = yearStop+monthStop+dateStop+"T"+hourStop+minuteStop+secondStop+"Z";
            ///usr/bin/ffmpeg -loglevel error -rtsp_transport tcp -i rtsp://%s:%s@%s:%s/Streaming/tracks/%s0%s?starttime=%s&endtime=%s -vcodec copy -an -f flv rtmp://192.168.9.40:1935/live/%s
            //rtsp://admin:hik12345@192.168.33.2:554/Streaming/tracks/101?starttime=20201111T095500Z&endtime=20201111T100005Z
            log.info("userName: "+userName+",password: "+password+",cameraIp: "+cameraIp+",cameraPort: " +cameraPort
                    +",iChanNum: "+iChanNum +",starttime: "+starttime+",endtime: "+endtime+",historyPath: "+historyPath);
            String transUrl = String.format(UrlBackTem, userName, password, cameraIp, cameraPort, iChanNum,1, starttime, endtime, historyPath);
            log.info("transUrl: "+transUrl);
            Runtime.getRuntime().exec(transUrl);
            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp"+rtmpUrls[rtmpUrls.length-1];
            returnMap.put("rtmpUrl", rtmpUrl);
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

    @Logs(title = "获取相机状态", code = "getCameraStatus")
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
            return channleStatusMap;
        }
        channleStatusMap.put("errorMessage: " ,"userID is null");
        return channleStatusMap;
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


//    @Logs(title = "获取nvr信息", code = "getNVRSystemInfo")
//    @Transactional(rollbackFor = Exception.class)
//    public Map<String,Object> getNVRSystemInfo(){
//        Map<String,Object> re = new HashMap<>();
//        List<RecorderConInfo> recorderConInfoList = cameraConDao.SelectRecords();
//        log.info("recorderConInfoList: "+recorderConInfoList);
//        long recordId = 1234;
//        NativeLong iChanNumTem = new NativeLong(0xFFFFFFFF);
//        for (RecorderConInfo recorderConInfo:recorderConInfoList) {
//            recordId = recorderConInfo.getRecordId();
//            lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(recordId)));
//            IntByReference intByReference = new IntByReference(0);
//            HCNetSDK.NET_DVR_HDCFG system = new HCNetSDK.NET_DVR_HDCFG();
//            system.write();
//            Pointer m_strIpparaCfgPointer = system.getPointer();
//            if(!hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong,HCNetSDK.NET_DVR_GET_HDCFG,iChanNumTem,m_strIpparaCfgPointer,system.size(),intByReference)){
//                int iErr = hCNetSDK.NET_DVR_GetLastError();
//                log.error("get camera status fail, error code: "+iErr);
//            }
//            system.read();
//            log.info("system:   ",system);
//        }
//        return re;
//    }


}

