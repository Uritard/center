package com.yjh.accessvideo.module.control.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.common.utils.FtpsUtil;
import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.utils.ByteUtil;
import com.yjh.accessvideo.commons.utils.DateTimeUtil;
import com.yjh.accessvideo.commons.utils.VideoUtil;
import com.yjh.accessvideo.commons.utils.http.HttpClientUtils;
import com.yjh.accessvideo.configuration.PlatFromFtpsConfig;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.hik.PlayCtrl;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.*;
import com.yjh.accessvideo.thread.TaskExecutePool;
import com.yjh.accessvideo.threads.RecordFileThread;
import com.yjh.accessvideo.threads.TranscodeThread;
import com.yjh.accessvideo.videostreamer.ProcessManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.yjh.accessvideo.commons.utils.FileUtil.createDirectory;

/**
 * @author tt
 * @since 2020-08-20
 */
@Service
public class CameraConService {

    private final Logger log = LoggerFactory.getLogger(CameraConService.class);

    @Autowired
    private CameraConDao cameraConDao;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Value("${nvr.rtmp.video}")
    private String UrlTem;

    @Value("${nvr.rtmp.back}")
    private String UrlBackTem;
    @Value("${nvr.download.trans}")
    private String TransUrl;

    @Value("${nvr.record.plan:false}")
    private boolean recordPlan;

    @Value("${robot.light.video}")
    private String robotLightTem;

    @Value("${robot.inferad.video}")
    private String robotInferadTem;

    @Value("${robot.A200.infrared.video}")
    private String robotA200InfraredTem;

    @Value("${nvr.ffmpeg.toMp4}")
    private String ffmpegToMp4;

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

    @Value("${video.https.enable}")
    private Integer videoHttps;//是否支持https

    @Value("${service.video.Path}")
    private String SERVICE_URL;//获取可视台账用户

    @Value("${srs.stop.url}")
    private String srsStopUrl;//srs停止播流

    @Value("${realtime.video.definition}")
    private String videoDefinition;

    @Value("${system.webSocket.url}")
    private String syncWebsocketUrl;//WS调用接口地址

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private PlatFromFtpsConfig platFromFtpsConfig;

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    private static PlayCtrl playCtrl = PlayCtrl.INSTANCE;
    private NativeLong m_lRealPlayHandle = new NativeLong(-1);// playhandle
    //    private NativeLong m_minsOne = new NativeLong(-1);
//    private HCNetSDK.NET_DVR_PREVIEWINFO dvr_previewinfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
//     private NativeLong lUserIDLong = new NativeLong(-1);
    private HCNetSDK.NET_DVR_CLIENTINFO m_sClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();    // play structure
    private int m_lPort = -1;
    private FRealDataCallBack fRealDataCallBack = new FRealDataCallBack();

    // private int lUserID = -1;//用户句柄
    private NativeLong mVoiceTalkHandle = new NativeLong(-1);//对讲句柄
    private int mPlayBackHandle = -1;//历史录像回放句柄
    //设备登录信息
    private HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
    //设备信息
    private HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

    @Resource
    private ProcessManager manager;

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> startRealPlay(Long cameraId) {

        Map<String, Object> returnMap = new HashMap<>();
//        if (Constant.mapsForCamera.size()>0) {
//            if (Objects.nonNull(Constant.mapsForCamera.get(String.valueOf(cameraId)))) {
//                Map<String, Object> cameraFlowMap = redisTemplate.opsForHash().entries("cameraRealFlow:" + cameraId);
//                if (cameraFlowMap.size()>0) {
//                    cameraFlowMap.put("cameraId", String.valueOf(cameraId));
//                    return cameraFlowMap;
//                }
//            }
//        }
//        try {
//            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
//            String userName = cameraConInfo.getCameraManager();
//            String password = cameraConInfo.getCameraCode();
//            String cameraIp = cameraConInfo.getCameraIp();
//            int cameraPort = cameraConInfo.getPort();
//            int iChanNum = 1;
//            int cameraType = cameraConInfo.getCameraType();
//            if (cameraType==206) { iChanNum = 2; }
//
//            String transUrl = "";
//            if (cameraType == 205) {
//                transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,
//                videoDefinition, cameraId);
//            } else if (cameraType == 206) {
//                transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum, 1, cameraId);
//            }
//            Runtime.getRuntime().exec(transUrl);
//            String[] rtmpUrls = transUrl.split("rtmp");
//            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];
//            returnMap.put("rtmpUrl", rtmpUrl);
//            isHttps(videoHttps, String.valueOf(cameraId), returnMap);
//            String webRtc = "webrtc://" + hostIp + "/live/" + cameraId;
//            returnMap.put("webRtcUrl", webRtc);
//            StreamInfoThread streamInfoThread = new StreamInfoThread(srsStopUrl, "cameraRealFlow:", Integer
//            .parseInt(String.valueOf(cameraId)), cameraId, returnMap, redisTemplate);
//            Thread thread = new Thread(streamInfoThread);
//            thread.setDaemon(true);
//            thread.start();
//            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, cameraType:{},
//            cameraId:{}, transUrl:{}"
//                    , userName, password, cameraIp, cameraPort, iChanNum, cameraType, cameraId, transUrl);
//        } catch (Exception e) { e.getMessage(); }

        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            String userName = cameraConInfo.getIdentityManager(); //nvr 用户名
            String password = cameraConInfo.getIdentityCode();    //nvr 密码
            String cameraIp = cameraConInfo.getRecordIp();        //nvr ip
            int cameraPort = cameraConInfo.getRtspPort();         //nvr rtsp port
            int iChanNum = cameraConInfo.getChannelNum();         //nvr 通道号
            int cameraType = cameraConInfo.getCameraType();
            String transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,
                    videoDefinition, cameraId);
            VideoInfo videoInfo = new VideoInfo().setCommand(transUrl).setId(cameraId);
            manager.run(videoInfo);

            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];
            returnMap.put("rtmpUrl", rtmpUrl);
            isHttps(videoHttps, String.valueOf(cameraId), returnMap);
            String webRtc = "webrtc://" + hostIp + "/live/" + cameraId;
            returnMap.put("webRtcUrl", webRtc);
            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, cameraType:{}, cameraId:{}, " +
                            "transUrl:{}"
                    , userName, password, cameraIp, cameraPort, iChanNum, cameraType, cameraId, transUrl);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return returnMap;
    }

    //@Logs(title = "相机批量播放", code = "cameraPlay", content = "相机批量播放")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> batchStartRealPlay(String cameraIds) {

        List<Map<String, Object>> returnMapList = new ArrayList<>();
        if (Objects.isNull(cameraIds) || cameraIds.length() == 0) {
            return returnMapList;
        }
        List<Long> list = new ArrayList<>();
        List<String> robotList = new ArrayList<>();
        String[] cameraIdArry = cameraIds.split(",");
        for (String aCameraIdArry : cameraIdArry) {
            if (StringUtils.endsWith(aCameraIdArry, "9901")) {
                robotList.add(aCameraIdArry);
            } else if (!Objects.equals(aCameraIdArry, "")) {
                list.add(Long.valueOf(aCameraIdArry));
            }
        }
        log.info("list: {}， robotList: {}", JSON.toJSONString(list), JSON.toJSONString(robotList));

        list.forEach(cameraId -> {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            String userName = cameraConInfo.getIdentityManager(); //nvr 用户名
            String password = cameraConInfo.getIdentityCode();    //nvr 密码
            String cameraIp = cameraConInfo.getRecordIp();        //nvr ip
            int cameraPort = cameraConInfo.getRtspPort();         //nvr rtsp port
            int iChanNum = cameraConInfo.getChannelNum();         //nvr 通道号
            int cameraType = cameraConInfo.getCameraType();
            String transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,
                    videoDefinition, cameraId);
            VideoInfo videoInfo = new VideoInfo().setCommand(transUrl).setId(cameraId);
            try {
                manager.run(videoInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, cameraType:{}, cameraId:{}, " +
                            "transUrl:{}"
                    , userName, password, cameraIp, cameraPort, iChanNum, cameraType, cameraId, transUrl);
            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];

            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
            returnMap.put("rtmpUrl", rtmpUrl);
            isHttps(videoHttps, String.valueOf(cameraId), returnMap);
            String webRtc = "webrtc://" + hostIp + "/live/" + cameraId;
            returnMap.put("webRtcUrl", webRtc);
            StreamInfoThread streamInfoThread = new StreamInfoThread(srsStopUrl, "cameraRealFlow:",
                    Integer.parseInt(String.valueOf(cameraId)), cameraId, returnMap, redisTemplate);
            Thread thread = new Thread(streamInfoThread);
            thread.setDaemon(true);
            thread.start();

            returnMapList.add(returnMap);
        });

        robotList.forEach(robotCameraId -> {
            long robotId = NumberUtils.toLong(StringUtils.remove(robotCameraId, "9901"));
            RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
            if (robotConInfo == null) {
                log.error("robot is null, robotCameraId: {}", robotCameraId);
                return;
            }
            Map<String, Object> returnLightMap = getRobotStream(robotConInfo, robotCameraId);
            returnMapList.add(returnLightMap);
        });

        log.info("realReturnMapList: {}", JSON.toJSONString(returnMapList));
        return returnMapList;
    }

    //@Logs(title = "机器人相机播放", code = "robotPlay", content = "机器人相机播放")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> robotStartRealPlay(Long robotId) {
        List<Map<String, Object>> returnMapList = new ArrayList<>();

        try {
            String lightCameraId = robotId + "9901";
            String infraredCameraId = robotId + "9902";
            RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);

            Map<String, Object> returnLightMap = getRobotStream(robotConInfo, lightCameraId);

            String inferadIp = robotConInfo.getLnferadIp();
            Integer inferadPort = robotConInfo.getInferadPort();
            String transUrlinferad;
            if (robotConInfo.getRobotType() == 161) {
                String inferadUsername = robotConInfo.getInferadUsername();
                String inferadPassword = robotConInfo.getInferadPassword();
                transUrlinferad = String.format(robotLightTem, inferadUsername, inferadPassword, inferadIp,
                        inferadPort, 1, infraredCameraId);
            } else if (robotConInfo.getRobotType() == 157) {
                transUrlinferad = String.format(robotA200InfraredTem, inferadIp, inferadPort, infraredCameraId);
            } else {
                transUrlinferad = String.format(robotInferadTem, inferadIp, inferadPort, infraredCameraId);
            }
            VideoInfo videoInfo2 = new VideoInfo().setCommand(transUrlinferad).setId(Long.parseLong(infraredCameraId));
            manager.run(videoInfo2);

            log.info("robotInferadInfo: {}, {}, {}, robotTransUrlinferad:{}", inferadIp, inferadPort,
                    infraredCameraId, transUrlinferad);
            String[] rtmpUrlsInferad = transUrlinferad.split("rtmp");
            String rtmpUrlInferad = "rtmp" + rtmpUrlsInferad[rtmpUrlsInferad.length - 1];

            Map<String, Object> returnInferadMap = new HashMap<>();
            returnInferadMap.put("inferad", infraredCameraId);
            returnInferadMap.put("rtmpUrlInferad", rtmpUrlInferad);
            isHttps(videoHttps, infraredCameraId, returnInferadMap);
            String InferadWebRtc = "webrtc://" + hostIp + "/live/" + infraredCameraId;
            returnInferadMap.put("webRtcUrl", InferadWebRtc);
            returnMapList.add(returnLightMap);
            returnMapList.add(returnInferadMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMapList;
    }

    public Map<String, Object> getRobotStream(RobotConInfo robotConInfo, String robotCameraId){
        Map<String, Object> returnLightMap = new HashMap<>();

        try {
            String lightIp = robotConInfo.getLightIp();
            String lightPort = robotConInfo.getLightPort();
            String lightUsername = robotConInfo.getIdentityManager();
            String lightPassword = robotConInfo.getIdentityCode();

            String transUrlLight = String.format(robotLightTem, lightUsername, lightPassword, lightIp, lightPort, 1,
                robotCameraId);
            VideoInfo videoInfo = new VideoInfo().setCommand(transUrlLight).setId(Long.parseLong(robotCameraId));
            manager.run(videoInfo);
            log.info("robotLightInfo: {}, {}, {}, {}, {}, robotTransUrlLight:{}", lightUsername, lightPassword,
                lightIp, lightPort, robotCameraId, transUrlLight);
            String[] rtmpUrls = transUrlLight.split("rtmp");
            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];

            returnLightMap.put("light", robotCameraId);
            returnLightMap.put("rtmpUrl", rtmpUrl);
            String lightWebRtc = "webrtc://" + hostIp + "/live/" + robotCameraId;
            returnLightMap.put("webRtcUrl", lightWebRtc);
            isHttps(videoHttps, robotCameraId, returnLightMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return returnLightMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> startPlayBack(Long cameraId, String startTime, String stopTime) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = new CameraConInfo();
            if (String.valueOf(cameraId).contains("9901") || String.valueOf(cameraId).contains("9902")) {
                long robotId;
                if (String.valueOf(cameraId).contains("9901")) {
                    robotId = Long.parseLong(String.valueOf(cameraId).replace("9901", ""));
                    RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
                    RecorderConInfo recorderConInfo = cameraConDao.selectByRecordId(robotConInfo.getRecordId());
                    cameraConInfo.setCameraId(cameraId);
                    cameraConInfo.setIdentityManager(recorderConInfo.getIdentityManager());
                    cameraConInfo.setIdentityCode(recorderConInfo.getIdentityCode());
                    cameraConInfo.setRecordIp(recorderConInfo.getRecordIp());
                    cameraConInfo.setRtspPort(recorderConInfo.getRtspPort());
                    cameraConInfo.setChannelNum(Integer.parseInt(robotConInfo.getNumLight()));
                } else {
                    robotId = Long.parseLong(String.valueOf(cameraId).replace("9902", ""));
                    RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
                    RecorderConInfo recorderConInfo = cameraConDao.selectByRecordId(robotConInfo.getRecordId());
                    cameraConInfo.setCameraId(cameraId);
                    cameraConInfo.setIdentityManager(recorderConInfo.getIdentityManager());
                    cameraConInfo.setIdentityCode(recorderConInfo.getIdentityCode());
                    cameraConInfo.setRecordIp(recorderConInfo.getRecordIp());
                    cameraConInfo.setRtspPort(recorderConInfo.getRtspPort());
                    cameraConInfo.setChannelNum(Integer.parseInt(robotConInfo.getNumInferad()));
                }

            } else {
                cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            }
            String userName = cameraConInfo.getIdentityManager(); //nvr 用户名
            String password = cameraConInfo.getIdentityCode();    //nvr 密码
            String cameraIp = cameraConInfo.getRecordIp();        //nvr ip
            int cameraPort = cameraConInfo.getRtspPort();         //nvr rtsp port
            int iChanNum = cameraConInfo.getChannelNum();         //nvr 通道号

            String startTimeTem = startTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
            String stopTimeTem = stopTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
            String starttime = startTimeTem.replace(" ", "Z");
            String endtime = stopTimeTem.replace(" ", "Z");

            Long id = System.currentTimeMillis();
            String transUrl = String.format(UrlBackTem, userName, password, cameraIp, cameraPort, iChanNum, 1,
                    starttime, endtime, id);
            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, starttime:{}, endtime:{}, " +
                            "historyPath:{}, historyTransUrl: {}"
                    , userName, password, cameraIp, cameraPort, iChanNum, starttime, endtime, cameraId, transUrl);
            VideoInfo videoInfo = new VideoInfo().setCommand(transUrl).setId(id);
            manager.run(videoInfo);

            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];

            returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
            returnMap.put("rtmpUrl", rtmpUrl);
            String webRtc = "webrtc://" + hostIp + "/history/" + id;
            returnMap.put("webRtcUrl", webRtc);
            isHttpsHistory(videoHttps, String.valueOf(id), returnMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> startVideoBack(String token,
        Long cameraId, String startTime, String stopTime) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            String userName = cameraConInfo.getIdentityManager(); //nvr 用户名
            String password = cameraConInfo.getIdentityCode();    //nvr 密码
            String cameraIp = cameraConInfo.getRecordIp();        //nvr ip
            int cameraPort = cameraConInfo.getRtspPort();         //nvr rtsp port
            int iChanNum = cameraConInfo.getChannelNum();         //nvr 通道号

            String startTimeTem = startTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
            String stopTimeTem = stopTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
            String starttime = startTimeTem.replace(" ", "Z");
            String endtime = stopTimeTem.replace(" ", "Z");

            Long id = Long.parseLong(token);
            String transUrl = String.format(UrlBackTem, userName, password, cameraIp, cameraPort, iChanNum, 1,
                    starttime, endtime, id);
            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, starttime:{}, endtime:{}, " +
                            "historyPath:{}, historyTransUrl: {}"
                    , userName, password, cameraIp, cameraPort, iChanNum, starttime, endtime, cameraId, transUrl);
            VideoInfo videoInfo = new VideoInfo().setBack(true).setCommand(transUrl).setId(id);
            manager.run(videoInfo);

            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];

            returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
            returnMap.put("rtmpUrl", rtmpUrl);
            String webRtc = "webrtc://" + hostIp + "/history/" + id;
            returnMap.put("webRtcUrl", webRtc);
            isHttpsHistory(videoHttps, String.valueOf(id), returnMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    //机器人相机-视频回放
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> startRobotPlayBack(Long robotId, String startTime, String stopTime) {

//        //定义返回结果集与可见光、红外历史流地址容器
//        List<Map<String,Object>> robotHistoryList=new ArrayList<>();
//        Map<String,Object> lightReturnMap=new HashMap<>();
//        Map<String,Object> infraredReturnMap=new HashMap<>();
//        String lightCameraId = String.valueOf(robotId)+"9901";
//        String infraredCameraId = String.valueOf(robotId)+"9902";
//
//        //判断是否已存在历史视频流
//        if (Constant.mapsForHistory.size()>0) {
//            if (Objects.nonNull(Constant.mapsForHistory.get(lightCameraId)) || Objects.nonNull(Constant
//            .mapsForHistory.get(infraredCameraId))) {
//                Map<String, Object> robotLightFlowMap = redisTemplate.opsForHash().entries("cameraHistoryFlow:" +
//                lightCameraId);
//                Map<String, Object> robotInferadFlowMap = redisTemplate.opsForHash().entries("cameraHistoryFlow:" +
//                infraredCameraId);
//                if (robotLightFlowMap.size()>0) robotHistoryList.add(robotLightFlowMap);
//                if (robotInferadFlowMap.size()>0) robotHistoryList.add(robotInferadFlowMap);
//            }
//            if (robotHistoryList.size()>1) { return robotHistoryList; }
//        }
//
//        //开始时间 与 结束时间 格式化
//        String startTimeTem = startTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
//        String stopTimeTem = stopTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
//        String starttime = startTimeTem.replace(" ", "Z");
//        String endtime = stopTimeTem.replace(" ", "Z");
//
//        //获取机器人基本信息
//        RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
//
//        try {
//            //开启可见光历史流
//            String userName=robotConInfo.getIdentityManager();
//            String password=robotConInfo.getIdentityCode();
//            String ipLight=robotConInfo.getLightIp();
//            String portLigh=robotConInfo.getLightPort();
//            String lightNum=robotConInfo.getNumLight();
//
//            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, starttime:{}, endtime:{},
//            historyPath:{}"
//                    , userName, password, ipLight, portLigh, lightNum, starttime, endtime, String.valueOf(robotId)
//                    +"9901");
//            String transUrl = String.format(UrlBackTem, userName, password, ipLight, portLigh, lightNum, 1,
//            starttime, endtime, String.valueOf(robotId)+"9901");
//
//            log.info("historyTransUrl: {}", transUrl);
//            Runtime.getRuntime().exec(new String[]{"sh", "-c", transUrl});
//            Thread.sleep(3000);
//            String[] rtmpUrls = transUrl.split("rtmp");
//            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];
//
//            lightReturnMap.put("robotId", String.valueOf(robotId));
//            lightReturnMap.put("rtmpUrl", rtmpUrl);
//            String lightWebRtc = "webrtc://" + hostIp + "/history/" + lightCameraId;
//            lightReturnMap.put("webRtcUrl", lightWebRtc);
//            isHttpsHistory(videoHttps, lightCameraId, lightReturnMap);
//
//            StreamInfoThread streamInfoThread = new StreamInfoThread(srsStopUrl, "cameraHistoryFlow:", Integer
//            .parseInt(lightCameraId), Long.parseLong(lightCameraId), lightReturnMap, redisTemplate);
//            Thread thread = new Thread(streamInfoThread);
//            thread.setDaemon(true);
//            thread.start();
////
////            String getInfoUrl="http://"+srsStopUrl+":1985/api/v1/streams/";
////            JSONObject jsonList = new JSONObject();
////            jsonList = HttpClientUtils.sendGet(getInfoUrl, null);
////            assert jsonList != null;
////            List<String> streamsJsonObjectList = JSONArray.parseArray(jsonList.getString("streams"),String.class);
////            //livePath
////            streamsJsonObjectList.stream().map(JSONObject::parseObject).forEach(streamJson -> {
////                String name = streamJson.getString("name");
////                String videoFlowId = streamJson.getString("id");
////                String publish = streamJson.getString("publish");
////                JSONObject publishjson = JSONObject.parseObject(publish);
////                if (Objects.equals(name, String.valueOf(robotId) + "9901") && StringUtils.isNotEmpty(publishjson
// .getString("cid"))) {
////                    lightReturnMap.put("videoFlowId", videoFlowId);
////                    Constant.mapsForHistory.put(String.valueOf(robotId) + "9901", videoFlowId);
////                    redisTemplate.opsForHash().putAll("cameraHistoryFlow:" + robotId + ":light", lightReturnMap);
////                    robotHistoryList.add(lightReturnMap);
////                }
////            });
//
//            //开启红外历史流
//            userName=robotConInfo.getInferadUsername();
//            password=robotConInfo.getInferadPassword();
//            String infraredIp=robotConInfo.getLnferadIp();
//            String infraredPort=Objects.nonNull(robotConInfo.getInferadPort())?robotConInfo.getInferadPort()
//            .toString():null;
//            String infraredNum=robotConInfo.getNumInferad();
//
//            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, starttime:{}, endtime:{},
//            historyPath:{}"
//                    , userName, password, infraredIp, infraredPort, infraredNum, starttime, endtime, robotId+"9902");
//            String transUrlIn = String.format(UrlBackTem, userName, password, ipLight, portLigh, lightNum, 1,
//            starttime, endtime, robotId+"9902");
//
//            log.info("historyTransUrl: {}", transUrlIn);
//            Runtime.getRuntime().exec(new String[]{"sh", "-c", transUrlIn});
//            Thread.sleep(3000);
//            String[] rtmpUrlsIn = transUrlIn.split("rtmp");
//            String rtmpUrlIn = "rtmp" + rtmpUrlsIn[rtmpUrlsIn.length - 1];
//
//            infraredReturnMap.put("robotId", String.valueOf(robotId));
//            infraredReturnMap.put("rtmpUrl", rtmpUrlIn);
//            String infraredWebRtc = "webrtc://" + hostIp + "/history/" + infraredCameraId;
//            infraredReturnMap.put("webRtcUrl", infraredWebRtc);
//            if (videoHttps == 1) {
//                String flvsUrl = "https://" + hostIp + ":8088/history/" + infraredCameraId + ".flv";
//                infraredReturnMap.put("flvUrl", flvsUrl);
//            } else {
//                String flvUrl = "http://" + hostIp + ":10080/history/" + infraredCameraId + ".flv";
//                infraredReturnMap.put("flvUrl", flvUrl);
//            }
//            StreamInfoThread streamInfoThread2 = new StreamInfoThread(srsStopUrl, "cameraHistoryFlow:", Integer
//            .parseInt(infraredCameraId), Long.parseLong(infraredCameraId), lightReturnMap, redisTemplate);
//            Thread thread2 = new Thread(streamInfoThread2);
//            thread2.setDaemon(true);
//            thread2.start();
//
////            String getInfoUrlIn="http://"+srsStopUrl+":1985/api/v1/streams/";
////            JSONObject jsonListIn = new JSONObject();
////            jsonListIn = HttpClientUtils.sendGet(getInfoUrlIn, null);
////            assert jsonListIn != null;
////            List<String> streamsJsonObjectListIn = JSONArray.parseArray(jsonListIn.getString("streams"),String
// .class);
////            //livePath
////            streamsJsonObjectListIn.stream().map(JSONObject::parseObject).forEach(streamJson -> {
////                String name = streamJson.getString("name");
////                String videoFlowId = streamJson.getString("id");
////                String publish = streamJson.getString("publish");
////                JSONObject publishjson = JSONObject.parseObject(publish);
////                if (Objects.equals(name, String.valueOf(robotId) + "9902") && StringUtils.isNotEmpty(publishjson
// .getString("cid"))) {
////                    infraredReturnMap.put("videoFlowId", videoFlowId);
////                    Constant.mapsForHistory.put(String.valueOf(robotId) + "9902", videoFlowId);
////                    redisTemplate.opsForHash().putAll("cameraHistoryFlow:" + robotId + ":inferad",
// infraredReturnMap);
////                    robotHistoryList.add(infraredReturnMap);
////                }
////            });
//        } catch (Exception e) { e.getMessage(); }
//
//        log.info("historyMapsForRobot:{}, robotHistoryList:{}", Constant.mapsForHistory, robotHistoryList);
//        return robotHistoryList;

        //定义返回结果集与可见光、红外历史流地址容器
        List<Map<String, Object>> robotHistoryList = new ArrayList<>();
        Map<String, Object> lightReturnMap = new HashMap<>();
        Map<String, Object> infraredReturnMap = new HashMap<>();

        //开始时间 与 结束时间 格式化
        String startTimeTem = startTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
        String stopTimeTem = stopTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
        String starttime = startTimeTem.replace(" ", "Z");
        String endtime = stopTimeTem.replace(" ", "Z");
        try {
            //获取机器人基本信息
            RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
            String lightCameraId = robotId + "9901";
            String infraredCameraId = robotId + "9902";
            //开启可见光历史流
            String userName = robotConInfo.getIdentityManager();
            String password = robotConInfo.getIdentityCode();
            String ipLight = robotConInfo.getLightIp();
            String portLigh = robotConInfo.getLightPort();
            String lightNum = robotConInfo.getNumLight();

            Long lightId = Long.parseLong(lightCameraId) + System.currentTimeMillis();
            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, starttime:{}, endtime:{}, " +
                            "historyPath:{}"
                    , userName, password, ipLight, portLigh, lightNum, starttime, endtime, lightId);
            String transUrl = String.format(UrlBackTem, userName, password, ipLight, portLigh, lightNum, 1, starttime
                    , endtime, lightId);

            log.info("historyTransUrl: {}", transUrl);
            VideoInfo videoInfo = new VideoInfo().setCommand(transUrl).setId(lightId);
            manager.run(videoInfo);
            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];

            lightReturnMap.put("robotId", String.valueOf(robotId));
            lightReturnMap.put("rtmpUrl", rtmpUrl);
            String lightWebRtc = "webrtc://" + hostIp + "/history/" + lightId;
            lightReturnMap.put("webRtcUrl", lightWebRtc);
            isHttpsHistory(videoHttps, String.valueOf(lightId), lightReturnMap);

            //开启红外历史流
            userName = robotConInfo.getInferadUsername();
            password = robotConInfo.getInferadPassword();
            String infraredIp = robotConInfo.getLnferadIp();
            String infraredPort = Objects.nonNull(robotConInfo.getInferadPort()) ?
                    robotConInfo.getInferadPort().toString() : null;
            String infraredNum = robotConInfo.getNumInferad();

            Long infraredId = Long.parseLong(infraredCameraId) + System.currentTimeMillis();
            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, starttime:{}, endtime:{}, " +
                            "historyPath:{}"
                    , userName, password, infraredIp, infraredPort, infraredNum, starttime, endtime, infraredId);
            String transUrlIn = String.format(UrlBackTem, userName, password, ipLight, portLigh, lightNum, 1,
                    starttime, endtime, infraredId);

            log.info("historyTransUrl: {}", transUrlIn);
            VideoInfo videoInfo2 = new VideoInfo().setCommand(transUrlIn).setId(infraredId);
            manager.run(videoInfo2);
            String[] rtmpUrlsIn = transUrlIn.split("rtmp");
            String rtmpUrlIn = "rtmp" + rtmpUrlsIn[rtmpUrlsIn.length - 1];
            infraredReturnMap.put("robotId", String.valueOf(robotId));
            infraredReturnMap.put("rtmpUrl", rtmpUrlIn);
            String infraredWebRtc = "webrtc://" + hostIp + "/history/" + infraredId;
            infraredReturnMap.put("webRtcUrl", infraredWebRtc);
            isHttpsHistory(videoHttps, String.valueOf(infraredId), infraredReturnMap);
            Thread.sleep(3000);
            robotHistoryList.add(lightReturnMap);
            robotHistoryList.add(infraredReturnMap);
        } catch (Exception e) {
            e.getMessage();
        }
        return robotHistoryList;
    }

    /**
     * 实时视频是否为https
     */
    private void isHttps(Integer videoHttps, String id, Map<String, Object> returnMap) {
        if (videoHttps == 1) {
            String flvsUrl = "https://" + hostIp + ":8088/live/" + id + ".flv";
            returnMap.put("flvUrl", flvsUrl);
        } else {
            String flvUrl = "http://" + hostIp + ":10080/live/" + id + ".flv";
            returnMap.put("flvUrl", flvUrl);
        }
    }

    /**
     * 历史视频是否为https
     */
    private void isHttpsHistory(Integer videoHttps, String id, Map<String, Object> returnMap) {
        String flvsUrl = "";
        String flvUrl = "";
        if (videoHttps == 1) {
            flvsUrl = "https://" + hostIp + ":8088/history/" + id + ".flv";
            returnMap.put("flvUrl", flvsUrl);
        } else {
            flvUrl = "http://" + hostIp + ":10080/history/" + id + ".flv";
            returnMap.put("flvUrl", flvUrl);
        }
    }

    //@Logs(title = "云台控制", code = "cameraControl")
    @Transactional(rollbackFor = Exception.class)
    public Object pTZControl(int dwPTZCommand, Long cameraId, int dStop, int speed) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
//        int iChanNum = cameraConInfo.getChannelNum() + 32;
//        m_lRealPlayHandle = realPlay(iChanNum, cameraConInfo.getRecordId());
//        if (m_lRealPlayHandle.intValue() == -1) {
//            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
//            return "fail";
//        }
//        if (dwPTZCommand == 29) {
//            return hCNetSDK.NET_DVR_PTZControlWithSpeed(m_lRealPlayHandle, dwPTZCommand, dStop, speed);
//        } else {
//            if (hCNetSDK.NET_DVR_PTZControlWithSpeed(m_lRealPlayHandle, dwPTZCommand, 0, speed)) {
//
//                try { Thread.sleep(200); } catch (Exception e) { e.getMessage(); }
//                hCNetSDK.NET_DVR_PTZControlWithSpeed(m_lRealPlayHandle, dwPTZCommand, 1, speed);
//                return hCNetSDK.NET_DVR_StopRealPlay(m_lRealPlayHandle);
//            } else {
//                return "PTZ control fail, errorInfo: " + hCNetSDK.NET_DVR_GetLastError();
//            }
//        }
        //修改为直接调用控制sdk NET_DVR_PTZControlWithSpeed_Other
        int iChanNum = cameraConInfo.getChannelNum() + 32;
        if (Objects.isNull(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())))) {
            registerNVR(cameraConInfo.getRecordId());
        }
        int userId = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
        if (hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, iChanNum, dwPTZCommand
                , dStop, speed)) {
            return "susses";
        } else {
            return "false";
        }

    }

    //@Logs(title = "相机抓图", code = "capturePicture")
    @Transactional(rollbackFor = Exception.class)
    public String capturePicture(String filePath, Long cameraId, String meteName) {
        CameraConInfo cameraConInfo = new CameraConInfo();
        if (String.valueOf(cameraId).contains("9901") || String.valueOf(cameraId).contains("9902")) {
            long robotId;
            if (String.valueOf(cameraId).contains("9901")) {
                robotId = Long.parseLong(String.valueOf(cameraId).replace("9901", ""));
                RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
                cameraConInfo.setCameraId(cameraId);
                cameraConInfo.setRecordId(robotConInfo.getRecordId());
                cameraConInfo.setChannelNum(Integer.parseInt(robotConInfo.getNumLight()));
            } else {
                robotId = Long.parseLong(String.valueOf(cameraId).replace("9902", ""));
                RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
                cameraConInfo.setCameraId(cameraId);
                cameraConInfo.setRecordId(robotConInfo.getRecordId());
                cameraConInfo.setChannelNum(Integer.parseInt(robotConInfo.getNumInferad()));
            }
            cameraConInfo.setCameraType(205); //机器人

        } else {
            cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        }
        int iChanNum = cameraConInfo.getChannelNum() + 32;
        // 超脑类型 NVR 不加 32
        if("813".equals(cameraConInfo.getRecorderType())){
            iChanNum = cameraConInfo.getChannelNum();
        }

        HCNetSDK.NET_DVR_JPEGPARA lpJpegPara = new HCNetSDK.NET_DVR_JPEGPARA();
        lpJpegPara.wPicSize = 0xff;
        lpJpegPara.wPicQuality = 0;/* 图片质量系数 0-最好 1-较好 2-一般 */
        if (Objects.nonNull(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())))) {
            int lUserIDLong = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
            log.info("lUserIDLong: {}", lUserIDLong);
            if (cameraConInfo.getCameraType() == 207) {
                log.info("红外相机，特殊拍照");
                m_sClientInfo.lChannel = iChanNum;
                if (Objects.nonNull(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())))) {
                    lUserIDLong = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
                    m_lPort = hCNetSDK.NET_DVR_RealPlay_V30(lUserIDLong, m_sClientInfo, fRealDataCallBack, null, true);
                    log.info("m_lPort: " + m_lPort);
                }
                //打开测温信息
                if (!playCtrl.PlayM4_RenderPrivateData(m_lPort, 0x20, 1)) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("PlayM4_RenderPrivateData fail, error code: " + iErr);
                    return "PlayM4_RenderPrivateData, error code: " + iErr;
                }
                if (!playCtrl.PlayM4_RenderPrivateDataEx(m_lPort, 0x20, 7, 1)) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("PlayM4_RenderPrivateDataEx fail, error code: " + iErr);
                    return "PlayM4_RenderPrivateDataEx, error code: " + iErr;
                }
                if (!playCtrl.PlayM4_SetOverlayPriInfoFlag(m_lPort, 0x20, true)) {
                    int iErr = playCtrl.PlayM4_GetLastError();
                    log.error("PlayM4_SetOverlayPriInfoFlag fail, error code: " + iErr);
                    return "PlayM4_SetOverlayPriInfoFlag, error code: " + iErr;
                }
                //获取播放库未使用的通道号
                if (!playCtrl.PlayM4_GetPort(new NativeLongByReference(new NativeLong(m_lPort)))) {
                    int iErr = playCtrl.PlayM4_GetLastError();
                    log.error("capture picture fail(PlayM4_GetPort), error code: " + iErr);
                    return "capture picture fail(PlayM4_GetPort), error code: " + iErr;
                }
                if (!playCtrl.PlayM4_Play(new NativeLong(m_lPort), null)) {
                    int iErr = playCtrl.PlayM4_GetLastError();
                    log.error("capture picture fail(PlayM4_Play), error code: " + iErr);
                    return "capture picture fail(PlayM4_Play), error code: " + iErr;
                }
                byte[] sJpgPicBuffer = new byte[1920 * 1080 * 2];
                IntByReference ipSizeReturned = new IntByReference();
                if (!playCtrl.PlayM4_GetJPEG(m_lPort, sJpgPicBuffer, 1920 * 1080 * 2, ipSizeReturned)) {
                    int iErr = playCtrl.PlayM4_GetLastError();
                    log.error("capture picture fail(PlayM4_GetJPEG), error code: " + iErr);
                    return "capture picture fail(PlayM4_GetJPEG), error code: " + iErr;
                }
                byteToImage(sJpgPicBuffer, filePath, meteName);
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

                int captureMethod = NumberUtils.toInt((String)redisTemplate.opsForHash().get("t_sys_param:captureMethod","content"), 3);

                boolean flag = false;
                InputStream inputStream = null;
                // 这里提供三种抓图方式，1和3是SDK示例中提供的接口，但是1会导致文件名不正确，多了未知后缀，2和3可正常使用，2是直接生成图片，
                // 3是将图片信息写入缓存，然后直接用缓存中信息添加水印，可以减少对磁盘IO
                if (captureMethod == 1) {
                    flag = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserIDLong, iChanNum, lpJpegPara, filePath.getBytes());
                } else if (captureMethod == 2) {
                    flag = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserIDLong, iChanNum, lpJpegPara, filePath);
                } else if (captureMethod == 3) {
                    // 返回图片大小
                    IntByReference a = new IntByReference();
                    // 图片缓冲区大小
                    ByteBuffer jpegBuffer = ByteBuffer.allocate(1920 * 1080);
                    flag = hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(lUserIDLong, iChanNum, lpJpegPara, jpegBuffer, 1920 * 1080, a);
                    // 图片输出流
                    inputStream = new ByteArrayInputStream(jpegBuffer.array(), 0, a.getValue());
                }
                log.info("capture picture method:{}, result: {}, inputStream: {}", captureMethod, flag, inputStream);

                if (!flag) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("capture picture fail(NET_DVR_CaptureJPEGPicture), error code: {}", iErr);
                    return "capture picture fail(NET_DVR_CaptureJPEGPicture), error code: " + iErr;
                } else {
                    if (StringUtils.isNotEmpty(meteName)) {
                        if (captureMethod == 3){
                            pictureWaterMark(inputStream, filePath, DateTimeUtil.format(new Date()) + "--" + meteName);
                        } else {
                            pictureWaterMark(filePath, DateTimeUtil.format(new Date()) + "--" + meteName);
                        }
                    } else if (captureMethod == 3) {
                        // 写入图片，没有调用 pictureWaterMark 方法需要手动的将图片写入文件
                        try (FileOutputStream outputStream = new FileOutputStream(filePath)){
                            IOUtils.copyLarge(inputStream, outputStream);
                            outputStream.flush();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        } finally {
                            IOUtils.closeQuietly(inputStream);
                        }
                    }
                }
            }
            return "success";
        } else {
            return "userID is null";
        }
    }

    /**
     * 给图片设置水印
     *
     * @param filePath         图片地址
     * @param waterMarkContent 水印内容
     */
    private void pictureWaterMark(String filePath, String waterMarkContent) {
        try {
            File file = new File(filePath);
            BufferedImage image = ImageIO.read(file);
            //获取图片的宽
            waterMarkWrite(image, waterMarkContent, file);
        } catch (Exception e) {
            log.error("图片设置水印错误: ", e);
        }
    }

    /**
     * 给图片设置水印
     *
     * @param filePath         图片地址
     * @param waterMarkContent 水印内容
     */
    private void pictureWaterMark(InputStream inputStream, String filePath, String waterMarkContent) {
        try {
            File file = new File(filePath);
            if (!(!file.exists() && file.createNewFile())) {
                log.error("创建文件失败，{}", filePath);
            }
            BufferedImage image = ImageIO.read(inputStream);
            waterMarkWrite(image, waterMarkContent, file);
        } catch (Exception e) {
            log.error("图片设置水印错误: ", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void waterMarkWrite(BufferedImage image, String waterMarkContent, File file) throws IOException {
        String suffix = StringUtils.substringAfterLast(file.getName(), ".");
        //获取图片的宽
        int srcImgWidth = image.getWidth();
        //获取图片的高
        int srcImgHeight = image.getHeight();

        // 创建画笔
        Graphics2D pen = image.createGraphics();
        // 设置画笔颜色
        pen.setColor(new Color(179, 250, 233, 200));
        // 设置画笔字体样式
        pen.setFont(new Font("微软雅黑", Font.BOLD, 30));

        //设置水印的坐标(为原图片右下角)
        int x = srcImgWidth - getWatermarkLength(waterMarkContent, pen) - 20;
        int y = srcImgHeight - 20;

        // 写上水印文字和坐标
        pen.drawString(waterMarkContent, x, y);
        try (FileImageOutputStream fos = new FileImageOutputStream(file)){
            ImageIO.write(image, suffix, fos);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 获取水印文字的长度
     *
     * @param waterMarkContent
     * @param g
     * @return
     */
    private static int getWatermarkLength(String waterMarkContent, Graphics2D g) {
        return g.getFontMetrics(g.getFont()).charsWidth(waterMarkContent.toCharArray(), 0, waterMarkContent.length());
    }

    public boolean presetAction(Long presetId, Long cameraId, int presetCmd) {
        return presetAction(presetId, cameraId, presetCmd, null);
    }

    public boolean presetAction(Long presetId, Long cameraId, int presetCmd, String presetName) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
        if (cameraConInfo == null) {
            //摄像机id和预置位Id不正确
            throw new BusinessException("无此摄像机或摄像机预置位不正确");
        }
        int iChanNum = cameraConInfo.getChannelNum() + 32;
        int iPreset = cameraConInfo.getPresetNum();
        int lUserIDLong = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
        boolean ret = hCNetSDK.NET_DVR_PTZPreset_Other(lUserIDLong, iChanNum, presetCmd, iPreset);

        // 设置预置位名称 调用接口报参数错误，比对参数后未发现问题，取消设置预置位名称
        /*if (ret && HCNetSDK.SET_PRESET == presetCmd && StringUtils.isNotBlank(presetName)) {
            HCNetSDK.NET_DVR_PRESET_NAME dvrPresetName = new HCNetSDK.NET_DVR_PRESET_NAME();
            dvrPresetName.wPresetNum = (short)iPreset;
            dvrPresetName.byPTZPosExEnable = 0;
            System.arraycopy(presetName.getBytes(), 0, dvrPresetName.byName, 0, presetName.length());
            dvrPresetName.write();

            if (!hCNetSDK.NET_DVR_SetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_SET_PRESET_NAME, iChanNum, dvrPresetName.getPointer(), dvrPresetName.size())) {
                log.error("设置预置位名称失败， name: {}, err: {}", presetName, hCNetSDK.NET_DVR_GetLastError());
            }
        }*/

        if (ret && presetCmd == HCNetSDK.CLE_PRESET) {
            String capturePresetPath = getPresetBasePath();
            String delPresetPic = "rm -rf " + capturePresetPath + "/" + presetId;
            try {
                Runtime.getRuntime().exec(delPresetPic);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return ret;
    }

    //@Logs(title = "获取相机状态", code = "getCameraStatus", content = "获取相机状态信息")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> getCameraStatus(Long recordId) {
        List<CameraStatusInfo> cameraConInfoMap = cameraConDao.cameraInfoByNVR(recordId);
        log.info("cameraConInfoMap: " + cameraConInfoMap);
        int iChanNumTem = 0xFFFFFFFF;
        Map<String, String> channleStatusMap = new HashMap<>();
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) {
            int lUserIDLong = Constant.maps.get(String.valueOf(recordId));
            IntByReference intByReference = new IntByReference(0);
            HCNetSDK.NET_DVR_IPPARACFG m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG(); //NET_DVR_IPPARACFG_V40
            // --支持128通道
            m_strIpparaCfg.write();
            Pointer m_strIpparaCfgPointer = m_strIpparaCfg.getPointer();
            if (!hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_GET_IPPARACFG, iChanNumTem,
                    m_strIpparaCfgPointer, m_strIpparaCfg.size(), intByReference)) {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("get camera status fail, error code: " + iErr);
                channleStatusMap.put("get camera status fail, error code: ", String.valueOf(iErr));
                channleStatusMap.put("errorCode: ", "401");
                return channleStatusMap;
            }
            m_strIpparaCfg.read();
            //设备支持IP通道
            for (int iChannum = 1; iChannum < HCNetSDK.MAX_IP_CHANNEL; iChannum++) {
                if (m_strIpparaCfg.struIPChanInfo[iChannum - 1].byEnable == 1) {
                    for (CameraStatusInfo cameraStatusInfo : cameraConInfoMap) {
                        if (Objects.equals(cameraStatusInfo.getChannelNum(), iChannum)) {
                            channleStatusMap.put(String.valueOf(cameraStatusInfo.getCameraId()), "1");
                        }
                    }
                }
                if (m_strIpparaCfg.struIPChanInfo[iChannum - 1].byEnable == 0) {
                    for (CameraStatusInfo cameraStatusInfo : cameraConInfoMap) {
                        if (Objects.equals(cameraStatusInfo.getChannelNum(), iChannum)) {
                            channleStatusMap.put(String.valueOf(cameraStatusInfo.getCameraId()), "0");
                        }
                    }
                }

            }
            log.info("channelStatusMap: " + channleStatusMap);
            channleStatusMap.put("errorCode: ", "200");
            return channleStatusMap;
        }
        channleStatusMap.put("errorMessage: ", "recordId is null");
        channleStatusMap.put("errorCode: ", "403");
        return channleStatusMap;
    }

    //@Logs(title = "获取相机树状态", code = "getCameraStatusTree", content = "获取NVR下挂相机树状态")
    @Transactional(rollbackFor = Exception.class)
    public List<CameraAreaInfo> getCameraStatusTree(String cameraName, Integer flag) {
        //if (Objects.nonNull(cameraName)) {}
        List<CameraAreaInfo> tree = new ArrayList<>();
        Map<String, String> state = new HashMap<>();
        List<CameraAreaInfo> listTree = this.cameraConDao.selectCameraTree(cameraName);
        for (CameraAreaInfo areaInfoMap : listTree) {
            if (areaInfoMap.getUpId() == -1) {
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
        diGui(tree, listTree, state, flag);
        return tree;

    }

    private void diGui(List<CameraAreaInfo> areaInfoList, List<CameraAreaInfo> listTree, Map<String, String> state,
                       Integer flag) {
        for (CameraAreaInfo areaInfo : areaInfoList) {
            List<CameraAreaInfo> childrenList = new ArrayList<>();
            for (CameraAreaInfo areaInfoMap : listTree) {
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    CameraAreaInfo areaInfoTem = new CameraAreaInfo();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    areaInfoTem.setLabel(areaInfoMap.getLabel());
                    areaInfoTem.setInfoType(areaInfoMap.getInfoType());
                    areaInfoTem.setUpName(areaInfoMap.getUpName());
                    areaInfoTem.setChannelNum(areaInfoMap.getChannelNum());
                    areaInfoTem.setStatusInfo(state.get(areaInfoMap.getId().toString()));
                    if (flag != null && flag == 1) {
                        if ("1".equals(state.get(areaInfoMap.getId().toString()))) {
                            //在线
                            childrenList.add(areaInfoTem);
                        }
                    } else {
                        childrenList.add(areaInfoTem);
                    }

                }
            }
            if (childrenList.size() > 0) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree, state, flag);
            }
        }
    }

    //    @Logs(title = "获取NVR存储状态", code = "getNVRStoreInfo", content = "获取NVR存储状态信息")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> getNVRStoreInfo(Long recordId) {
        List<CameraStatusInfo> cameraConInfoMap = cameraConDao.cameraInfoByNVR(recordId);
        log.info("cameraConInfoMap: {}", cameraConInfoMap);
        NativeLong iChanNumTem = new NativeLong(0);
        Map<String, String> channleStatusMap = new HashMap<>();
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) {
            NativeLong lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(recordId)));
            IntByReference ibrBytesReturned = new IntByReference(0);
            HCNetSDK.NET_DVR_HDCFG m_struHDCfg = new HCNetSDK.NET_DVR_HDCFG();
            m_struHDCfg.write();
            Pointer lpPicConfig = m_struHDCfg.getPointer();
            if (!hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong.intValue(), HCNetSDK.NET_DVR_GET_HDCFG, iChanNumTem.intValue(), lpPicConfig,
                    m_struHDCfg.size(), ibrBytesReturned)) {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("get NVR status fail, error code: {}", iErr);
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
                hardCapacityTotal = hardCapacityTotal + hardCapacity;
                int hardFree = netDvrSingleHd.dwFreeSpace;
                hardFreeTotal = hardFreeTotal + hardFree;
                log.info(s + "， hardNo: " + hardNo + ", hardCapacity: " + hardCapacity + ", hardFree" + hardFree);
            }
            channleStatusMap.put("recorderId", String.valueOf(recordId));
            channleStatusMap.put("capacityTotal", String.valueOf(hardCapacityTotal));
            channleStatusMap.put("freeTotal", String.valueOf(hardFreeTotal));
            return channleStatusMap;
        }
        channleStatusMap.put("errorMessage", "录像机不在线");
        return channleStatusMap;
    }

    /**
     * 每6小时刷新一次录像机信息
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void refreshRecordsOnSchedule() {
        List<RecorderConInfo> recordersInfo = cameraConDao.SelectRecords();
        recordersInfo.stream().map(RecorderConInfo::getRecordId).forEach(recordId -> {
            reRegister(recordId);
            redisTemplate.opsForValue().set("recorderInfo:" + recordId, JSON.toJSONString(getNVRStoreAndChanle(recordId)));
            redisTemplate.expire("recorderInfo:" + recordId, 7, TimeUnit.DAYS);
        });
    }

    /**
     * 获取NVR存储状态和通道信息
     *
     * @param recordId
     * @return
     */
    public Map<String, Object> getNVRStoreAndChanle(Long recordId) {

        NativeLong iChanNumTem = new NativeLong(0);
        Map<String, Object> channleStatusMap = new HashMap<>();
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) {
            NativeLong lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(recordId)));

            // 查询磁盘信息
            IntByReference ibrBytesReturned = new IntByReference(0);
            HCNetSDK.NET_DVR_HDCFG m_struHDCfg = new HCNetSDK.NET_DVR_HDCFG();
            m_struHDCfg.write();
            Pointer lpPicConfig = m_struHDCfg.getPointer();
            if (!hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_GET_HDCFG, iChanNumTem, lpPicConfig,
                    m_struHDCfg.size(), ibrBytesReturned)) {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("get NVR status fail, error code: {}", iErr);
                channleStatusMap.put("get NVR status fail, error code: ", String.valueOf(iErr));
                channleStatusMap.put("errorMessage", "录像机不在线");
                channleStatusMap.put("status", "离线");
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
                hardCapacityTotal = hardCapacityTotal + hardCapacity;
                int hardFree = netDvrSingleHd.dwFreeSpace;
                hardFreeTotal = hardFreeTotal + hardFree;
                log.info(s + "， hardNo: " + hardNo + ", hardCapacity: " + hardCapacity + ", hardFree" + hardFree);
            }
            channleStatusMap.put("recorderId", String.valueOf(recordId));
            channleStatusMap.put("capacityTotal", String.valueOf(hardCapacityTotal));
            channleStatusMap.put("freeTotal", String.valueOf(hardFreeTotal));
            HCNetSDK.NET_DVR_DEVICEINFO_V40 device = Constant.deviceMaps.get(recordId);
            // 查询通道信息
            List<Map<String, Object>> chanInfo = getNVRIpparaCfg(recordId, lUserIDLong, iChanNumTem,
                    new HCNetSDK.NET_DVR_TIME(), new HCNetSDK.NET_DVR_TIME());
            channleStatusMap.put("channel", chanInfo);
            channleStatusMap.put("status", "在线");
            return channleStatusMap;
        }
        channleStatusMap.put("errorMessage", "录像机不在线");
        channleStatusMap.put("status", "离线");
        return channleStatusMap;
    }

    public List<Map<String, Object>> getNVRIpparaCfg(Long recordId, String startTime, String stopTime) {
        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Integer lUserIDLong = Constant.maps.get(String.valueOf(recordId));
        if (Objects.nonNull(lUserIDLong)) {
            HCNetSDK.NET_DVR_TIME stTime = new HCNetSDK.NET_DVR_TIME();
            HCNetSDK.NET_DVR_TIME edTime = new HCNetSDK.NET_DVR_TIME();

            if (StringUtils.isNotEmpty(startTime)) {
                Date d = DateTimeUtil.parse(startTime);
                date2DvrTime(d, stTime);
            }
            if (StringUtils.isNotEmpty(stopTime)) {
                Date d = DateTimeUtil.parse(stopTime);
                date2DvrTime(d, edTime);
            }

            retList = getNVRIpparaCfg(recordId, new NativeLong(lUserIDLong), new NativeLong(0), stTime, edTime);
        }
        if (retList.isEmpty()) {
            map.put("errorMessage", "录像机不在线");
            retList.add(map);
        }
        return retList;
    }

    /**
     * startTime 和 stopTime 不能为 null，至少要 new HCNetSDK.NET_DVR_TIME()
     */
    public List<Map<String, Object>> getNVRIpparaCfg(Long recordId, NativeLong lUserIDLong, NativeLong iChanNumTem,
                                                     HCNetSDK.NET_DVR_TIME startTimeI,
                                                     HCNetSDK.NET_DVR_TIME stopTimeI) {

        IntByReference ibrBytesReturned = new IntByReference(0);
        HCNetSDK.NET_DVR_IPPARACFG m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG();
        m_strIpparaCfg.write();
        Pointer lpIpConfig = m_strIpparaCfg.getPointer();
        boolean cfg = hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_GET_IPPARACFG, iChanNumTem,
                lpIpConfig, m_strIpparaCfg.size(), ibrBytesReturned);
        List<Map<String, Object>> channelInfoList = new ArrayList<>();
        if (cfg) {
            m_strIpparaCfg.read();
            // 通道信息
            HCNetSDK.NET_DVR_IPCHANINFO[] ipChans = m_strIpparaCfg.struIPChanInfo;
            // 设备信息
            HCNetSDK.NET_DVR_IPDEVINFO[] ipDevs = m_strIpparaCfg.struIPDevInfo;
            // ipChanNum = m_strIpparaCfg.dwDChanNum;
            // ipChanStart = m_strIpparaCfg.dwStartDChan;
            HCNetSDK.NET_DVR_DEVICEINFO_V40 device = Constant.deviceMaps.get(recordId);
            int ipChanNum = device.struDeviceV30.byIPChanNum;
            int ipChanStart = device.struDeviceV30.byStartDChan;

            log.info("NVR[{} - {}] ip channel start: {}, num: {}", recordId, lUserIDLong.longValue(), ipChanStart,
                    ipChanNum);

            HCNetSDK.NET_DVR_FILECOND_V40 fileCond = new HCNetSDK.NET_DVR_FILECOND_V40();
            fileCond.dwFileType = 0xff;
            fileCond.dwIsLocked = 0xff;
            boolean nvrFileLog = "true".equals(redisTemplate.opsForHash().get("t_sys_param:nvrFileLog", "content"));
            for (int i = 0; i < ipChans.length; i++) {
                int ipId = ipChans[i].byIPID;
                if (ipId == 0) {
                    // 通道未启用，跳出
                    continue;
                }
                HCNetSDK.NET_DVR_TIME startTime = newDvrTime(startTimeI);
                HCNetSDK.NET_DVR_TIME stopTime = newDvrTime(stopTimeI);
                int chanStart = (ipChanStart == 0 || ipChanStart == 32) ? ipChanStart + 1 : ipChanStart;
                NativeLong ipChan = new NativeLong(i + chanStart);
                Map<String, Object> chanInfoMap = new LinkedHashMap<>();
                chanInfoMap.put("ipChanNum", i + 1);
                chanInfoMap.put("enable", ipChans[i].byEnable);
                chanInfoMap.put("channel", ipChans[i].byChannel);
                chanInfoMap.put("ipAddr", ipDevs[ipId - 1].struIP.toString());

                // 录像计划和通道名称
                MutablePair<String, List<String[]>> nameAndPlan = recordCfg(lUserIDLong, ipChan);
                chanInfoMap.put("chanName", nameAndPlan.getLeft());
                List<String[]> recordschedList = nameAndPlan.getRight();
                if (recordPlan) {
                    chanInfoMap.put("recordPlan", recordschedList);
                }

                long timeRecord = 0L;
                long timeDefect = 0L;
                int[] intact = new int[3];

                // 处理开始结束时间
                String[] recordSpan = recordTimeSpan(startTime, stopTime, ipChan.intValue(), lUserIDLong);
                chanInfoMap.put("recordSpan", recordSpan);
                // 设置通道号，开始/结束时间
                fileCond.lChannel = ipChan.intValue();
                fileCond.struStartTime = startTime;
                fileCond.struStopTime = stopTime;
                log.info("NVR file find start, channel: {}, beginTime: {}, endTime: {}", ipChan,
                        fileCond.struStartTime.toStringTime(),
                        fileCond.struStopTime.toStringTime());
                // 查询文件
                int lFindFile = hCNetSDK.NET_DVR_FindFile_V40(lUserIDLong.intValue(), fileCond);
                HCNetSDK.NET_DVR_FINDDATA_V40 strFile = new HCNetSDK.NET_DVR_FINDDATA_V40();
                // 迭代查询所有文件
                int lNext;
                HCNetSDK.NET_DVR_TIME preEndTime = null;
                long preStopTime = 0L;
                boolean found = true;
                if (lFindFile == -1) {
                    found = false;
                    log.error("文件查找失败 NET_DVR_FindFile_V40, channel: {}, [{} —— {}]", ipChan.intValue(),
                            hCNetSDK.NET_DVR_GetLastError(), lFindFile);
                }
                while (found) {
                    lNext = hCNetSDK.NET_DVR_FindNextFile_V40(lFindFile, strFile);
                    if (lNext == HCNetSDK.NET_DVR_FILE_SUCCESS) {

                        long stTime = dvrTime2Timestamp(strFile.struStartTime);
                        long spTime = dvrTime2Timestamp(strFile.struStopTime);
                        long speed = spTime - stTime;
                        // 计算录像时长
                        timeRecord += speed;
                        if (nvrFileLog) {
                            log.info("找到文件, channel: {}, fileName: {}, time: [{}  {}]", ipChan.intValue(),
                                    new String(strFile.sFileName).trim(),
                                    strFile.struStartTime.toStringTime(), strFile.struStopTime.toStringTime());
                        }
                        // 录像完整性视频段校验
                        recordCheck2(recordschedList, intact, strFile.struStartTime, preEndTime);

                        // 录像完整性时间校验
                        long def = (preStopTime > 0 && stTime > preStopTime) ? (stTime - preStopTime) : 0L;
                        timeDefect += def;
                        preEndTime = newDvrTime(strFile.struStopTime);
                        preStopTime = spTime;
                    } else if (lNext == HCNetSDK.NET_DVR_FILE_NOFIND) {
                        log.info("没有找到文件, channel: {}！", ipChan.intValue());
                        found = false;
                    } else if (lNext != HCNetSDK.NET_DVR_ISFINDING) {
                        log.info("文件查找关闭 NET_DVR_FindClose_V30, channel: {}, [{} —— {}]", ipChan.intValue(),
                                hCNetSDK.NET_DVR_GetLastError(), lNext);
                        hCNetSDK.NET_DVR_FindClose_V30(lFindFile);
                        found = false;
                    }
                }
                String sTemp = timeStr(timeRecord);
                chanInfoMap.put("recordTime", sTemp);
                int intactTime = 10000;
                if (timeRecord > 0L && timeDefect > 0L) {
                    intactTime = (int) ((timeRecord * 10000) / (timeDefect + timeRecord));
                }
                chanInfoMap.put("intactTime", intactTime);
                chanInfoMap.put("defectTime", timeStr(timeDefect));

                if (intact[0] > 0) {
                    intact[2] = (intact[0] * 10000) / (intact[0] + intact[1]);
                }
                chanInfoMap.put("intact", intact);

                channelInfoList.add(chanInfoMap);
            }
        } else {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.error("get getNVRIpparaCfg status fail, error code: {}", iErr);
        }

        return channelInfoList;
    }

    public String[] recordTimeSpan(HCNetSDK.NET_DVR_TIME startTime, HCNetSDK.NET_DVR_TIME stopTime, int ipChan,
                                   NativeLong lUserIDLong) {
        // 获取录像开始结束时间
        HCNetSDK.NET_DVR_RECORD_TIME_SPAN_INQUIRY timeSpanInquiry = new HCNetSDK.NET_DVR_RECORD_TIME_SPAN_INQUIRY();
        timeSpanInquiry.byType = 0;
        timeSpanInquiry.dwSize = timeSpanInquiry.size();
        HCNetSDK.NET_DVR_RECORD_TIME_SPAN lpResult = new HCNetSDK.NET_DVR_RECORD_TIME_SPAN();
        timeSpanInquiry.write();
        if (hCNetSDK.NET_DVR_InquiryRecordTimeSpan(lUserIDLong.intValue(), ipChan, timeSpanInquiry, lpResult)) {
            lpResult.read();
            log.info("通道录像起止时间, beginTime: {}, endTime: {}", lpResult.strBeginTime.toStringTime(),
                    lpResult.strEndTime.toStringTime());
            if (startTime.dwYear == 0 || stopTime.dwYear == 0) {
                startTime.dwYear = lpResult.strBeginTime.dwYear;
                startTime.dwMonth = lpResult.strBeginTime.dwMonth;
                startTime.dwDay = lpResult.strBeginTime.dwDay;
                startTime.dwHour = lpResult.strBeginTime.dwHour;
                startTime.dwMinute = lpResult.strBeginTime.dwMinute;
                startTime.dwSecond = lpResult.strBeginTime.dwSecond;

                stopTime.dwYear = lpResult.strEndTime.dwYear;
                stopTime.dwMonth = lpResult.strEndTime.dwMonth;
                stopTime.dwDay = lpResult.strEndTime.dwDay;
                stopTime.dwHour = lpResult.strEndTime.dwHour;
                stopTime.dwMinute = lpResult.strEndTime.dwMinute;
                stopTime.dwSecond = lpResult.strEndTime.dwSecond;
            }
        } else {
            log.info("通道录像起止时间查询失败, error: {}", hCNetSDK.NET_DVR_GetLastError());
            if (startTime.dwYear == 0 || stopTime.dwYear == 0) {
                Calendar cal = Calendar.getInstance();
                stopTime.dwYear = cal.get(Calendar.YEAR);
                stopTime.dwMonth = cal.get(Calendar.MONTH) + 1;
                stopTime.dwDay = cal.get(Calendar.DAY_OF_MONTH);
                stopTime.dwHour = 23;
                stopTime.dwMinute = 59;
                stopTime.dwSecond = 59;
                // 7天前
                cal.add(Calendar.DAY_OF_MONTH, -7);
                startTime.dwYear = cal.get(Calendar.YEAR);
                startTime.dwMonth = cal.get(Calendar.MONTH) + 1;
                startTime.dwDay = cal.get(Calendar.DAY_OF_MONTH);
            }
        }
        String[] recordSpan = new String[]{lpResult.strBeginTime.toStringTime(), lpResult.strEndTime.toStringTime()};
        return recordSpan;
    }

    /**
     * 录像计划
     */
    public MutablePair<String, List<String[]>> recordCfg(NativeLong lUserIDLong, NativeLong lChannel) {

        List<String[]> recordschedList = new ArrayList<>();
        if (recordPlan) {
            IntByReference ibrBytesReturned = new IntByReference(0);
            HCNetSDK.NET_DVR_RECORD_V30 m_dvrRecord = new HCNetSDK.NET_DVR_RECORD_V30();
            m_dvrRecord.write();
            Pointer lpPicConfig = m_dvrRecord.getPointer();
            boolean cfg = hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_GET_RECORDCFG_V30, lChannel,
                    lpPicConfig, m_dvrRecord.size(),
                    ibrBytesReturned);

            if (cfg) {
                m_dvrRecord.read();

                HCNetSDK.NET_DVR_RECORDDAY[] allDay = m_dvrRecord.struRecAllDay;
                HCNetSDK.NET_DVR_RECORDSCHEDWEEK[] recordscheds = m_dvrRecord.struRecordSched;
                for (int i = 0; i < recordscheds.length; i++) {
                    HCNetSDK.NET_DVR_RECORDSCHED[] recordsched = recordscheds[i].struRecordSched;
                    List<String> record = new ArrayList<>();
                    log.info("录像计划, wAllDayRecord:{}, byRecordType:{}, byStartHour[0]:{}, byStopHour[0]:{}",
                            allDay[i].wAllDayRecord, allDay[i].byRecordType, recordsched[0].struRecordTime.byStartHour,
                            recordsched[0].struRecordTime.byStopHour);
                    if (recordsched[0].struRecordTime.byStartHour == recordsched[0].struRecordTime.byStopHour) {
                        if (allDay[i].wAllDayRecord == 1 && allDay[i].byRecordType == 0) {
                            record.add("00:00 - 24:00");
                        }
                    } else {
                        for (HCNetSDK.NET_DVR_RECORDSCHED rs : recordsched) {
                            if (!rs.struRecordTime.isEmpty() && rs.byRecordType == 0) {
                                record.add(rs.struRecordTime.toStringTime());
                            }
                        }
                    }
                    recordschedList.add(record.toArray(record.toArray(new String[0])));
                }

            } else {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("get NET_DVR_GetDVRConfig[录像计划] status fail, error code: {}", iErr);
            }
        }
        IntByReference ibrBytesReturned2 = new IntByReference(0);
        HCNetSDK.NET_DVR_PICCFG_V30 m_dvrRecord2 = new HCNetSDK.NET_DVR_PICCFG_V30();
        m_dvrRecord2.write();
        Pointer lpPicConfig2 = m_dvrRecord2.getPointer();
        boolean cfg2 = hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_GET_PICCFG_V30, lChannel,
                lpPicConfig2, m_dvrRecord2.size(), ibrBytesReturned2);
        String sChanName;
        if (cfg2) {
            m_dvrRecord2.read();
            try {
                sChanName = new String(m_dvrRecord2.sChanName, "GB2312").trim();
            } catch (UnsupportedEncodingException e) {
                sChanName = "IPCamera" + lChannel.intValue();
            }

        } else {
            sChanName = "IPCamera" + lChannel.intValue();
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.error("get NET_DVR_GetDVRConfig[录像计划] status fail, error code: {}", iErr);
        }

        return MutablePair.of(sChanName, recordschedList);
    }

    public void recordCheck2(List<String[]> recordschedList, int[] intact, HCNetSDK.NET_DVR_TIME struStartTime,
                             HCNetSDK.NET_DVR_TIME preEndTime) {
        // 开始或结束时间为空记为视频完整
        if (preEndTime == null || preEndTime.dwYear == 0 || struStartTime.dwYear == 0) {
            intact[0] = intact[0] + 1;
            return;
        }

        boolean undamaged =
                struStartTime.dwYear == preEndTime.dwYear && struStartTime.dwMonth == preEndTime.dwMonth && struStartTime.dwDay == preEndTime.dwDay
                        && struStartTime.dwHour == preEndTime.dwHour && struStartTime.dwMinute == preEndTime.dwMinute && struStartTime.dwSecond == preEndTime.dwSecond;
        if (undamaged) {
            // 完整
            intact[0] = intact[0] + 1;
        } else {

            // TODO 不完整可以再次区分是不是因为录像计划导致的不完整
            /*Calendar date = dvrTime2Date(struStartTime);
            int weekDay = date.get(Calendar.DAY_OF_WEEK);
            int weekIdx = weekDay - 2;
            if(weekIdx < 0){
                weekIdx = 6;
            }
            String[] records = recordschedList.get(weekIdx);
            long speed = 60 * 1000;
            for(String rd : records){
                String[] rds = rd.split("-");
                String[] start = rds[0].trim().split(":");
                String[] end = rds[1].trim().split(":");
                // 开始时间
                Calendar date2 = (Calendar)date.clone();
                date2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start[0]));
                date2.set(Calendar.MINUTE, Integer.parseInt(start[1]));
                // 结束时间
                Calendar date3 = (Calendar)date.clone();
                date2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end[0]));
                date2.set(Calendar.MINUTE, Integer.parseInt(end[1]));

                long sts = date.getTimeInMillis() - date2.getTimeInMillis();
                long eds = date3.getTimeInMillis() - date.getTimeInMillis();
                if((sts >= 0 && sts < speed) || (eds >= 0 && eds < speed)){
                    intact[0] = intact[0] + 1;
                    return;
                }
            }*/
            // 不完整
            intact[1] = intact[1] + 1;
        }

    }

    /**
     * 录像完整性校验
     * NVR 不支持完整性校验，需自己实现
     */
    @Deprecated
    public void recordCheck(int lUserIDLong, int[] intact, HCNetSDK.NET_DVR_TIME struStartTime,
                            HCNetSDK.NET_DVR_TIME struStopTime) {
        HCNetSDK.NET_DVR_RECORD_CHECK_COND checkCond = new HCNetSDK.NET_DVR_RECORD_CHECK_COND();
        checkCond.byCheckType = 0;
        checkCond.struBeginTime = new HCNetSDK.NET_DVR_TIME_EX(struStartTime);
        checkCond.struEndTime = new HCNetSDK.NET_DVR_TIME_EX(struStopTime);
        checkCond.write();
        Pointer checkPointer = checkCond.getPointer();

        int lchek = hCNetSDK.NET_DVR_StartRemoteConfig(lUserIDLong, HCNetSDK.NET_DVR_RECORD_CHECK,
                checkPointer, checkCond.size(), null, null);
        if (lchek == -1) {
            log.info("录像完整性校验失败 [{}]", hCNetSDK.NET_DVR_GetLastError());
            return;
        }
        // checkCond.read();
        while (true) {
            HCNetSDK.NET_DVR_RECORD_CHECK_RET checkRet = new HCNetSDK.NET_DVR_RECORD_CHECK_RET();
            checkRet.write();
            Pointer checkRetPointer = checkRet.getPointer();

            lchek = hCNetSDK.NET_DVR_GetNextRemoteConfig(lchek, checkRetPointer, checkRet.size());
            if (lchek == HCNetSDK.NET_SDK_GET_NEXT_STATUS_SUCCESS) {
                checkRet.read();
                if (checkRet.byRecordNotComplete == 0) {
                    // 完整
                    intact[0] = intact[0] + 1;
                } else {
                    // 不完整
                    intact[1] = intact[1] + 1;
                }
                log.info("录像完整性校验, 完整 : {}", checkRet.byRecordNotComplete);
            } else if (lchek != HCNetSDK.NET_SDK_GET_NEXT_STATUS_NEED_WAIT) {
                log.info("录像校验关闭 NET_DVR_StopRemoteConfig [{}]", lchek);
                hCNetSDK.NET_DVR_StopRemoteConfig(lchek);
                break;
            }
        }
    }

    public long dvrTime2Timestamp(HCNetSDK.NET_DVR_TIME struTime) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.MILLISECOND, 0);
        date.set(struTime.dwYear, struTime.dwMonth, struTime.dwDay, struTime.dwHour, struTime.dwMinute,
                struTime.dwSecond);
        return date.getTimeInMillis();
    }

    public Calendar dvrTime2Date(HCNetSDK.NET_DVR_TIME struTime) {
        Calendar date = Calendar.getInstance();
        date.set(struTime.dwYear, struTime.dwMonth, struTime.dwDay, struTime.dwHour, struTime.dwMinute,
                struTime.dwSecond);
        return date;
    }

    public void date2DvrTime(Date date, HCNetSDK.NET_DVR_TIME struTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        struTime.dwYear = cal.get(Calendar.YEAR);
        struTime.dwMonth = cal.get(Calendar.MONTH) + 1;
        struTime.dwDay = cal.get(Calendar.DAY_OF_MONTH);
        struTime.dwHour = cal.get(Calendar.HOUR_OF_DAY);
        struTime.dwMinute = cal.get(Calendar.MINUTE);
        struTime.dwSecond = cal.get(Calendar.SECOND);
    }

    public String timeStr(long time) {
        if (time < 1000L) {
            return "0s";
        }
        int iTemp;
        String sTemp;
        long ms = 60 * 1000L;
        long hm = 60 * 60 * 1000L;
        long dh = 24 * 60 * 60 * 1000L;
        if (time < hm) {
            iTemp = (int) (time / ms);
            sTemp = iTemp + "m ";
            iTemp = (int) ((time % ms) / 1000);
            sTemp += iTemp + "s";
        } else if (time < dh) {
            iTemp = (int) (time / hm);
            sTemp = iTemp + "h ";
            iTemp = (int) ((time % hm) / ms);
            sTemp += iTemp + "m";
        } else {
            iTemp = (int) (time / dh);
            sTemp = iTemp + "d ";
            iTemp = (int) ((time % dh) / hm);
            sTemp += iTemp + "h ";
            iTemp = (int) ((time % hm) / ms);
            sTemp += iTemp + "m";
        }
        return sTemp;
    }

    public HCNetSDK.NET_DVR_TIME newDvrTime(HCNetSDK.NET_DVR_TIME struTime) {
        HCNetSDK.NET_DVR_TIME newstru = new HCNetSDK.NET_DVR_TIME();
        newstru.dwYear = struTime.dwYear;
        newstru.dwMonth = struTime.dwMonth;
        newstru.dwDay = struTime.dwDay;
        newstru.dwHour = struTime.dwHour;
        newstru.dwMinute = struTime.dwMinute;
        newstru.dwSecond = struTime.dwSecond;
        return newstru;
    }

    /**
     * 判断 NVR 是否掉线，如果掉线则重新注册
     *
     * @param recordId
     */
    public void reRegister(Long recordId) {
        NativeLong iChanNumTem = new NativeLong(0);
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) {
            NativeLong lUserIDLong = new NativeLong(Constant.maps.get(String.valueOf(recordId)));

            // 查询磁盘信息，判断NVR是否掉线
            IntByReference ibrBytesReturned = new IntByReference(0);
            HCNetSDK.NET_DVR_HDCFG m_struHDCfg = new HCNetSDK.NET_DVR_HDCFG();
            m_struHDCfg.write();
            Pointer lpPicConfig = m_struHDCfg.getPointer();
            if (!hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_GET_HDCFG, iChanNumTem, lpPicConfig,
                    m_struHDCfg.size(), ibrBytesReturned)) {
                log.info("NVR has offline, now beginning reregister: {}", recordId);
                registerNVR(recordId);
            }
        } else {
            log.info("NVR not register, now beginning register: {}", recordId);
            registerNVR(recordId);
        }
    }

    //@Logs(title = "NVR注册", code = "NVRRegister")
    @Transactional(rollbackFor = Exception.class)
    public String registerNVR(Long recordId) {
        int lUserID = -1;
        RecorderConInfo recorderConInfo = cameraConDao.selectByRecordId(recordId);
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) {
            lUserID = Constant.maps.get(String.valueOf(recordId));
        }

        if (lUserID > -1) {
            //NVR log out first...
            log.info("record has been registered already, lUserIDOri: {}", lUserID);
            hCNetSDK.NET_DVR_Logout(lUserID);
            lUserID = -1;
        }
        String m_sDeviceIP = recorderConInfo.getRecordIp();
        String m_sUsername = recorderConInfo.getIdentityManager();
        String m_sPassword = recorderConInfo.getIdentityCode();
        Short m_port = recorderConInfo.getHttpPort().shortValue();
        //注册
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = m_port;
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是

        m_strLoginInfo.write();

        HCNetSDK.NET_DVR_DEVICEINFO_V40 strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();

        lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, strDeviceInfo);
        log.info("register nvr {}, ip is {}, port is {}, user{}, pass{}, lUserID:{}", recorderConInfo.getRecordName(), m_sDeviceIP,
                m_port, recorderConInfo.getIdentityManager(), recorderConInfo.getIdentityCode(), lUserID);
        if (lUserID == -1) {
            log.error("recordName:{}, register fail, error code:{}", recorderConInfo.getRecordName(),
                    hCNetSDK.NET_DVR_GetLastError());
            return recorderConInfo.getRecordName() + " register fail, error code:" + hCNetSDK.NET_DVR_GetLastError();
        } else {
            Constant.maps.put(String.valueOf(recordId), lUserID);
            Constant.deviceMaps.put(recordId, strDeviceInfo);
            lUserID = -1;
            log.info("Constant.maps: {}, NVR {} register success.", Constant.maps, recorderConInfo.getRecordName());
            return "NVR " + recorderConInfo.getRecordName() + " register success.";
        }

    }

    private int realPlay(int lChannel, long recordId) {
        m_sClientInfo.lChannel = lChannel;
        int lUserIDLong = -1;
        if (Objects.nonNull(Constant.maps.get(String.valueOf(recordId)))) {
            lUserIDLong = Constant.maps.get(String.valueOf(recordId));
            return hCNetSDK.NET_DVR_RealPlay_V30(lUserIDLong,
                    m_sClientInfo, null, null, true);
        } else {
            return lUserIDLong;
        }
    }

    //byte数组到图片
    private void byteToImage(byte[] data, String path, String meteName) {
        if (data.length < 3 || "".equals(path)) {
            return;
        }
        try {
            FileImageOutputStream imageOutput = new FileImageOutputStream(new File(path));
            imageOutput.write(data, 0, data.length);
            imageOutput.flush();
            imageOutput.close();
            log.info("Make Picture success,Please find image in {}", path);
            // 红外图片添加水印的开关
            String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isWatermarkToInfrared", "content"
            ));
            if (StringUtils.isNotEmpty(meteName) && StringUtils.equals("true", flag)) {
                pictureWaterMark(path, DateTimeUtil.format(new Date()) + "--" + meteName);
            }
        } catch (Exception ex) {
            log.info("Exception: " + ex);
            ex.getMessage();
        }
    }

    //    judge is camera controlled. by tt. 1-不可控
//    @Logs(title = "获取相机当前控制状态", code = "isCameraControlled", content = "获取相机当前控制状态")
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void isCameraControlled(Long cameraId) {
        Map<String, Object> camreaStatusMap = redisTemplate.opsForHash().entries("camera_info:" + cameraId);
        log.info("camreaStatusMap: {}, camreaStatusMapState: {}", camreaStatusMap, camreaStatusMap.get("state"));
        if (Objects.nonNull(camreaStatusMap.get("state"))) {
            Integer state = Integer.parseInt(String.valueOf(camreaStatusMap.get("state")));
            if (Objects.equals(state, 1)) {
                try {
                    //判断时间问题
                    String lastTime = String.valueOf(camreaStatusMap.get("lastTime"));
                    Date endDate = format.parse(lastTime);
                    if (System.currentTimeMillis() - endDate.getTime() > 10 * 60 * 1000) {
                        //最后一次操控时间距离现在大于10分钟
                        camreaStatusMap.put("state", 0);
                        redisTemplate.opsForHash().putAll("camera_info", camreaStatusMap);
                        return;
                    }
                } catch (Exception e) {
                    log.info("时间转换出错：", e);
                }
                log.info("unable to control...");
                throw new BusinessException("相机不可控！");
            }
        }
    }

    public String startRecordVideo(long cameraId) {
        //返回结果
        String judge = "";
        int lRealPlayHandle;
        int iErr = 0;
        try {
            CameraConInfo cameraConInfo = new CameraConInfo();
            cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            cameraConInfo.setChannelNum(cameraConInfo.getChannelNum() + 32);
            //生成文件名
            Date date = new Date();
            String fileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(date) + ".h264";
            log.info("生成文件名" + fileName);
            //保存文件地址
            createDirectory(videoPath);
            String path = videoPath + fileName;
            log.info("保存文件地址：" + path);
            int lUserIDLong = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
            HCNetSDK.NET_DVR_PREVIEWINFO struPlayInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
            struPlayInfo.lChannel = cameraConInfo.getChannelNum();
            struPlayInfo.dwStreamType = 0;
            lRealPlayHandle = hCNetSDK.NET_DVR_RealPlay_V40(lUserIDLong, struPlayInfo, null, null);
            if (lRealPlayHandle < 0) {
                iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("get camera video NET_DVR_RealPlay_V40, error code: " + iErr);
            }
            hCNetSDK.NET_DVR_SaveRealData(lRealPlayHandle, path);
            Constant.recordLongMap.put(fileName, lRealPlayHandle);

            judge = path;
        } catch (Exception e) {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("获取视频方发异常：" + iErr);
            log.info(e.getMessage());
        }
        return judge;
    }

    public void endRecordVideo(String fileName) {
        try {
            log.info("停止录制视频");
            String path = videoPath + fileName;
            fileName = fileName.replace(videoPath, "");
            int lRealPlayHandle = Constant.recordLongMap.get(fileName);
//            hCNetSDK.NET_DVR_StopSaveRealData(lRealPlayHandle);
            hCNetSDK.NET_DVR_StopRealPlay(lRealPlayHandle);
            String ffmUrl = String.format(ffmpegToMp4, path, path.replace(".h264", ".mp4"));
            log.info("h264 to mp4 url is {}", ffmUrl);
            Runtime.getRuntime().exec(ffmUrl);
            Thread.sleep(1000);
            String url = "chmod 777 " + path.replace(".h264", ".mp4");
            Runtime.getRuntime().exec(url);
        } catch (Exception ignored) {
            log.info("录制失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String startDvrToPlace(long cameraId) {
        //返回结果
        String judge = "";
        int lRealPlayHandle;
        int iErr = 0;
        try {
            CameraConInfo cameraConInfo = new CameraConInfo();
            if (String.valueOf(cameraId).contains("9901") || String.valueOf(cameraId).contains("9902")) {
                long robotId;
                if (String.valueOf(cameraId).contains("9901")) {
                    robotId = Long.parseLong(String.valueOf(cameraId).replace("9901", ""));
                    RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
                    cameraConInfo.setCameraId(cameraId);
                    cameraConInfo.setRecordId(robotConInfo.getRecordId());
                    cameraConInfo.setChannelNum(Integer.parseInt(robotConInfo.getNumLight()) + 32);
                    cameraConInfo.setUpRegionId(robotConInfo.getUpRegionId());
                } else {
                    robotId = Long.parseLong(String.valueOf(cameraId).replace("9902", ""));
                    RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
                    cameraConInfo.setCameraId(cameraId);
                    cameraConInfo.setRecordId(robotConInfo.getRecordId());
                    cameraConInfo.setChannelNum(Integer.parseInt(robotConInfo.getNumInferad()) + 32);
                    cameraConInfo.setUpRegionId(robotConInfo.getUpRegionId());
                }

            } else {
                cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
                cameraConInfo.setChannelNum(cameraConInfo.getChannelNum() + 32);
            }
            //生成文件名
            Date date = new Date();
            String fileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(date) + ".h264";
            log.info("生成文件名" + fileName);
            //保存文件地址
            createDirectory(videoPath);
            String path = videoPath + fileName;
            log.info("保存文件地址：" + path);
            int lUserIDLong = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
            HCNetSDK.NET_DVR_PREVIEWINFO struPlayInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
            struPlayInfo.lChannel = cameraConInfo.getChannelNum();
            struPlayInfo.dwStreamType = 0;
            lRealPlayHandle = hCNetSDK.NET_DVR_RealPlay_V40(lUserIDLong, struPlayInfo, null, null);
            if (lRealPlayHandle < 0) {
                iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("get camera video NET_DVR_RealPlay_V40, error code: " + iErr);
            }
            hCNetSDK.NET_DVR_SaveRealData(lRealPlayHandle, path);
            Constant.recordLongMap.put(fileName, lRealPlayHandle);
            //防止前端未调用停止接口
            RecordFileThread recordFileThread = new RecordFileThread(cameraConDao, redisTemplate, hCNetSDK,
                    date.getTime(), fileName, videoPath, savePath);
            TaskExecutePool.getInstance().execute(recordFileThread);
            judge = fileName;
            RecordFileInfo recordFileInfo = new RecordFileInfo();
            recordFileInfo.setFileName(fileName.replace(".h264", ""));
            recordFileInfo.setStartTime(date);
            recordFileInfo.setCameraId(cameraId);
            recordFileInfo.setUpRegionId(cameraConInfo.getUpRegionId());
            cameraConDao.insertRecordFile(recordFileInfo);
        } catch (Exception e) {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("获取视频方发异常：" + iErr);
            log.info(e.getMessage());
        }
        return judge;
    }

    @Transactional(rollbackFor = Exception.class)
    public String stopDvrToPlace(String fileName, String userId) {
        //返回结果
        String judge = "录制文件正在生成中...";
        try {
            int lRealPlayHandle = Constant.recordLongMap.get(fileName);
            hCNetSDK.NET_DVR_StopRealPlay(lRealPlayHandle);
            String url = "chmod 777 " + videoPath + fileName;
            Runtime.getRuntime().exec(url);
            TranscodeThread transcodeThread = new TranscodeThread(videoPath, savePath, fileName, syncWebsocketUrl,
                    userId, cameraConDao, platFromFtpsConfig);
            TaskExecutePool.getInstance().execute(transcodeThread);
        } catch (Exception ignored) {
            log.info("录制失败");
        }
        return judge;
    }

    /**
     * 下载nvr文件
     *
     * @param cameraId  摄像头id
     * @param startTime 开始时间
     * @param stopTime  结束时间
     */

    public String getDVRToPlace(long cameraId, String startTime, String stopTime) throws ParseException {
        //返回结果
        String judge = null;
        //错误码
        int iErr = 0;
        try {
            //查询视频参数
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            cameraConInfo.setChannelNum(cameraConInfo.getChannelNum() + 32);
           /* if (hCNetSDK.NET_DVR_Init()) {
                log.info("初始化成功开始注册登录：");*/
            // String login = registerNVR(cameraConInfo.getRecordId());
            /*if (!login.isEmpty()) {*/
            // log.info("登录成功：" + login);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //生成文件名
            String fileName = String.valueOf(cameraId) +"-"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".mp4";
            String fileNameTemp = String.valueOf(cameraId) +"-"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_temp"+".mp4";
            log.info("生成文件名" + fileName);
            //保存文件地址
            String path = videoPath + fileNameTemp;
            log.info("保存文件地址：" + path);
            int lUserIDLong = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
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
            int lChannel = cameraConInfo.getChannelNum();
            log.info("lChannel赋值成功:" + lChannel);
            //查找文件存在不存在
            int findFile = hCNetSDK.NET_DVR_FindFile(lUserIDLong, lChannel, 0xff, struStartTime, struStopTim);
            log.info("findFile查找文件接口:" + "findFile：" + findFile + "lUserIDLong:" + lUserIDLong + "lChannel:" + lChannel);
            if (findFile > -1) {
                //获取时间断的视频接口
                int fileByTime = hCNetSDK.NET_DVR_GetFileByTime(lUserIDLong, lChannel, struStartTime, struStopTim, path );
                log.info("\n调用NET_DVR_GetFileByTime接口获取到：\n" + fileByTime + "lUserIDLong:" + lUserIDLong + "\nlChannel" + lChannel + "\npath:" + path);
                if (fileByTime < 0) {
                    iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("get camera video NET_DVR_GetFileByTime, error code: " + iErr);
                    judge = null;
                } else {
                    if (fileByTime < 100) {
                        //  hCNetSDK.NET_DVR_SetLogToFile(3,"/home/yjh_iot_center/sdklog",false);
                        //使用NET_DVR_GetFileByTime_V40接口必须使用一下播放接口才能下载视频到本地
                        boolean PlayBackControl = hCNetSDK.NET_DVR_PlayBackControl(fileByTime,
                                HCNetSDK.NET_DVR_PLAYSTART, 0, null);
                        log.info("PlayBackControl：" + PlayBackControl + "dwControlCode:" + HCNetSDK.NET_DVR_PLAYSTART + "dwInValue:" + 0 + "lpOutValue" + HCNetSDK.NET_DVR_GETTOTALTIME);
                        if (PlayBackControl) {
                            log.info("NET_DVR_PlayBackControl成功调用");
                            iErr = hCNetSDK.NET_DVR_GetLastError();
                            log.info("NET_DVR_PlayBackControl成功调用后NET_DVR_GetLastError：" + iErr);
                            IntByReference nPos = new IntByReference(0);
                            while (1 == 1)//获取下载进度，确认下载结束，则停止下载
                            {
                                int m_lLoadHandle = fileByTime;
                                hCNetSDK.NET_DVR_PlayBackControl(m_lLoadHandle, HCNetSDK.NET_DVR_PLAYGETPOS, 0, nPos);
                                //log.info("NET_DVR_PlayBackControl循环内接口参数"+"m_lLoadHandle"+m_lLoadHandle+"nPos"+nPos);
                                if (nPos.getValue() > 100) {
                                    hCNetSDK.NET_DVR_StopGetFile(m_lLoadHandle);
                                    m_lLoadHandle = -1;
                                    log.info("由于网络原因或DVR忙,下载异常终止!");
                                    judge = null;
                                    break;
                                } else if (nPos.getValue() == 100) {
                                    hCNetSDK.NET_DVR_StopGetFile(m_lLoadHandle);

                                    m_lLoadHandle = -1;
                                    log.info("按时间下载结束!");
                                    String transUrl = String.format(TransUrl,path,videoPath + fileName);
                                    String url = "chmod 777 " + path;
                                    String deleteUrl = "rm -f" + path;
                                    log.info(url);
                                    log.info(transUrl);
                                    log.info(deleteUrl);
                                    //修改权限
                                    Runtime.getRuntime().exec(url);
                                    //转码
                                    Runtime.getRuntime().exec(transUrl).waitFor();
                                    //删除临时文件
                                    Runtime.getRuntime().exec(deleteUrl);
                                    log.info("nPos.getValue!:>>>" + nPos.getValue());
                                    judge = savePath + fileName;
                                    copyFile(fileName, videoPath + fileName);
                                    // hCNetSDK.NET_DVR_Logout(m_lLoadHandle);
                                    // hCNetSDK.NET_DVR_Cleanup();
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
            } else {
                judge = null;
                int rr = hCNetSDK.NET_DVR_GetLastError();
                log.info("get file by time  NET_DVR_FindFile返回, error rr: " + rr);
            }

               /* } else {
                    iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("登录 接口获取到：iErr" + iErr);
                    log.info("视频登录失败！！！");
                    judge = null;

                }*/
         /* } else {
                iErr = hCNetSDK.NET_DVR_GetLastError();
                log.info("调用NET_DVR_Init 接口获取到：" + iErr);
                log.info("视频初始化失败！！！");
                judge = null;
            }*/
        } catch (Exception e) {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("获取视频方发异常：");
            log.info(e.getMessage());
            judge = null;
        }
        return judge;
    }

    /**
     * 获取摄像头文件列表
     *
     * @param cameraId  摄像头id
     * @param startTime 开始时间
     * @param stopTime  结束时间
     */
    public List<Map<String, String>> getFile(long cameraId, String startTime, String stopTime) {
        //初始化结果集
        List<Map<String, String>> fileMapList = new ArrayList<>();
        //查询摄像机信息
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        cameraConInfo.setChannelNum(cameraConInfo.getChannelNum() + 32);
        //处理时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!Constant.maps.containsKey(String.valueOf(cameraConInfo.getRecordId()))) {
            return Collections.EMPTY_LIST;
        }
        int lUserIDLong = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
        log.info("lUserIDLong获取到：" + lUserIDLong);
        Calendar startCalendar = Calendar.getInstance();
        //开始时间
        HCNetSDK.NET_DVR_TIME struStartTime = new HCNetSDK.NET_DVR_TIME();
        HCNetSDK.NET_DVR_TIME struStopTime = new HCNetSDK.NET_DVR_TIME();
        try {
            Date startTimes = format.parse(startTime);
            startCalendar.setTime(startTimes);
            log.info("startTime获取到：" + startTime);
            log.info("stopTime时间转：" + stopTime);
            struStartTime.dwYear = startCalendar.get(Calendar.YEAR);
            struStartTime.dwMonth = startCalendar.get(Calendar.MONTH) + 1;
            struStartTime.dwDay = startCalendar.get(Calendar.DATE);
            struStartTime.dwHour = startCalendar.get(Calendar.HOUR_OF_DAY);
            struStartTime.dwMinute = startCalendar.get(Calendar.MINUTE);
            struStartTime.dwSecond = startCalendar.get(Calendar.SECOND);
            // log.info("struStartTime 开始时间结束："+struStartTime.toString());
            //结束时间
            Date endTimes = format.parse(stopTime);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endTimes);
            struStopTime.dwYear = endCalendar.get(Calendar.YEAR);
            struStopTime.dwMonth = endCalendar.get(Calendar.MONTH) + 1;
            struStopTime.dwDay = endCalendar.get(Calendar.DATE);
            struStopTime.dwHour = endCalendar.get(Calendar.HOUR_OF_DAY);
            struStopTime.dwMinute = endCalendar.get(Calendar.MINUTE);
            struStopTime.dwSecond = endCalendar.get(Calendar.SECOND);
            log.info("时间赋值成功");
            int lChannel = cameraConInfo.getChannelNum();
            log.info("lChannel赋值成功:" + lChannel);
            //查找文件是否存在
            int findFile = hCNetSDK.NET_DVR_FindFile(lUserIDLong, lChannel, 0xff, struStartTime, struStopTime);
            log.info("findFile查找文件接口:" + "findFile-->>>：" + findFile);
            log.info("findFile查找文件接口:" + "findFile-->>>：" + findFile + "<<<<lUserIDLong:" + lUserIDLong + "lChannel:" + lChannel);

            if (findFile > -1) {
                log.info("NET_DVR_FindFile接口请求成功findFile:" + findFile);
                HCNetSDK.NET_DVR_FIND_DATA findData= new HCNetSDK.NET_DVR_FIND_DATA();
                // 文件列表接口
                int findNextFile = hCNetSDK.NET_DVR_FindNextFile(findFile, findData);
                log.info("findNextFile>>:" + findNextFile);
                if (findNextFile > -1) {
                    log.info("NET_DVR_FindNextFile接口请求成功findFile:" + findNextFile);
                    //当找到录像文件时接口将返回1000，当没有查找到文件或查找结束将返回1003或者1004，返回1002表示当前正在查找
                    while (findNextFile != HCNetSDK.NET_DVR_NOMOREFILE && findNextFile != HCNetSDK.NET_DVR_FILE_NOFIND) {
                        switch ((int)findNextFile) {
                            case HCNetSDK.NET_DVR_FILE_SUCCESS:
                                log.info("NET_DVR_FindNextFile接口请求成功:" + findNextFile);
                                //处理文件信息
                                dealFoundFile(findData,fileMapList);
                                findNextFile = hCNetSDK.NET_DVR_FindNextFile(findFile, findData);
                                break;
                            case HCNetSDK.NET_DVR_ISFINDING:
                                findNextFile = hCNetSDK.NET_DVR_FindNextFile(findFile, findData);
                                break;
                            default:
                                log.info("查找文件时异常");
                                int iErr = hCNetSDK.NET_DVR_GetLastError();
                                log.info("查找文件时候异常iErr:" + iErr);
                                break;
                        }
                    }
                } else {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("查找下一个文件是失败iErr:" + iErr);
                }
            } else {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.info("文件不存在:" + iErr);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return fileMapList;
    }

    private void dealFoundFile(HCNetSDK.NET_DVR_FIND_DATA dvrFindData, List<Map<String, String>> list) {
        Map<String, String> map = Maps.newHashMap();
        map.put("fileName", new String(dvrFindData.sFileName).trim());
        map.put("startTime", dvrFindData.struStartTime.toFormatTime());
        map.put("endTime", dvrFindData.struStopTime.toFormatTime());
        int iTemp = 0;
        String MyString;
        if (dvrFindData.dwFileSize < 1024 * 1024) {
            iTemp += (dvrFindData.dwFileSize) / (1024);
            MyString = iTemp + "K";
        } else {
            iTemp = (dvrFindData.dwFileSize) / (1024 * 1024);
            MyString = iTemp + "M";
            iTemp = ((dvrFindData.dwFileSize) % (1024 * 1024)) / (1204);
            MyString = MyString + iTemp + "K";
        }
        map.put("fileSize", MyString);
        list.add(map);
    }

    /**
     * 获取测温数据和图片
     *
     * @param cameraId 摄像头id
     * @param presetId 预置位id
     */
    public Map<String, String> givePicFir(long cameraId, Long presetId, String meteName) {
        Map<String, String> map = new HashMap<String, String>();
        int lUserID = -1;
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
            cameraConInfo.setChannelNum(cameraConInfo.getChannelNum() + 32);
            log.info("通道号：" + cameraConInfo.getChannelNum());
            log.info("getRecordId:" + cameraConInfo.getRecordId());
            String userName = cameraConInfo.getCameraManager();
            log.info("userName" + userName);
            String password = cameraConInfo.getCameraCode();
            log.info("password" + password);
            String cameraIp = cameraConInfo.getCameraIp();
            log.info("cameraIp" + cameraIp);
            String port = cameraConInfo.getInfreadPort().toString();
            log.info("port:>>>" + port);
           /* if (!hCNetSDK.NET_DVR_Init()) {
                log.info("初始化失败");
            }*/
            log.info("开始登录。。。。。");
            m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
            System.arraycopy(cameraIp.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, cameraIp.length());
            m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
            System.arraycopy(userName.getBytes(), 0, m_strLoginInfo.sUserName, 0, userName.length());
            m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
            System.arraycopy(password.getBytes(), 0, m_strLoginInfo.sPassword, 0, password.length());
            m_strLoginInfo.wPort = Short.parseShort(port);
            m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
            m_strLoginInfo.write();
            log.info("登录账号:" + userName + "ip地址" + cameraIp + "登录密码" + password + "端口号" + port);
            lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
            log.info("NET_DVR_Login_V40:返回值" + lUserID);
            int lUserIDLong = lUserID;
            //转到预置点
            boolean Preset = hCNetSDK.NET_DVR_PTZPreset_Other(lUserIDLong, 2, HCNetSDK.GOTO_PRESET,
                    cameraConInfo.getPresetNum());
            if (Preset) {
                log.info("转到预置点成功：Preset->" + Preset);
            } else {
                log.info("转到预置点信息Preset->" + Preset);
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.info("转到预置点失败错误码" + iErr);
            }

//            try {
//                long waitTime =
//                        Long.parseLong(redisTemplate.opsForHash().get("t_sys_param:waitTime", "content").toString());
//                log.info("waitTime:----------" + waitTime);
//                Thread.sleep(waitTime);
//            } catch (Exception e) {
//                log.error("error----" + e);
//            }
            //       getSetconfig(lUserIDLong);
            HCNetSDK.NET_DVR_JPEGPICTURE_WITH_APPENDDATA m_strJpegWithAppenData =
                    new HCNetSDK.NET_DVR_JPEGPICTURE_WITH_APPENDDATA();
            m_strJpegWithAppenData.dwSize = m_strJpegWithAppenData.size();
            m_strJpegWithAppenData.dwChannel = 1;
            HCNetSDK.BYTE_ARRAY ptrJpegByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 * 1024);
            HCNetSDK.BYTE_ARRAY ptrP2PDataByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 * 1024);
            m_strJpegWithAppenData.pJpegPicBuff = ptrJpegByte.getPointer();
            m_strJpegWithAppenData.pP2PDataBuff = ptrP2PDataByte.getPointer();
            // log.info("m_strJpegWithAppenData的值:"+m_strJpegWithAppenData.toString());
            boolean bRet = hCNetSDK.NET_DVR_CaptureJPEGPicture_WithAppendData(lUserIDLong, 2, m_strJpegWithAppenData);
            log.info("bRet返回值：" + bRet);
            if (bRet) {
                FileOutputStream fout;
                String newName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                //测温图片
                if (m_strJpegWithAppenData.dwJpegPicLen > 0) {
                    String path = hotPic + newName + ".jpg";
                    log.info("hotPic地址：" + path);
                    fout = new FileOutputStream(path);
                    //将字节写入文件
                    long offset = 0;
                    ByteBuffer buffers = m_strJpegWithAppenData.pJpegPicBuff.getByteBuffer(offset,
                            m_strJpegWithAppenData.dwJpegPicLen);
                    byte[] bytes = new byte[m_strJpegWithAppenData.dwJpegPicLen];
                    buffers.rewind();
                    buffers.get(bytes);
                    fout.write(bytes);
                    fout.close();
                    // 红外图片添加水印的开关
                    String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isWatermarkToInfrared",
                            "content"));
                    if (StringUtils.isNotEmpty(meteName) && StringUtils.equals("true", flag)) {
                        log.info("红外图片，添加水印，路径：" + path);
                        pictureWaterMark(path, DateTimeUtil.format(new Date()) + "--" + meteName);
                    }
                    map.put("picPath", hotPicshow + newName + ".jpg");
                    map.put("urlPath", hotPicshow + newName + ".jpg");
                    map.put("absPath", hotPic + newName + ".jpg");
                    copyFile(newName + ".jpg", path);
                    log.info("hotPicshow地址：" + hotPicshow + newName + ".jpg");
                } else {
                    map = null;
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("图片获取失败错误码" + iErr);
                }
                //测温数据data
                if (m_strJpegWithAppenData.dwP2PDataLen > 0) {
                    String path = hotFir + newName + ".data";
                    log.info("hotFirdata地址：" + path);
                    fout = new FileOutputStream(path);
                    //将字节写入文件
                    long offset = 0;
                    ByteBuffer buffers = m_strJpegWithAppenData.pP2PDataBuff.getByteBuffer(offset,
                            m_strJpegWithAppenData.dwP2PDataLen);
                    byte[] bytes = new byte[m_strJpegWithAppenData.dwP2PDataLen];
                    buffers.rewind();
                    buffers.get(bytes);
                    fout.write(bytes);
                    fout.close();
                    map.put("dataPath", hotFirShow + newName + ".data");
                    copyFile(newName + ".data", path);
                    log.info("hotFirShowdata地址：" + hotFirShow + newName + ".data");
                } else {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("data获取失败错误码" + iErr);
                }
                //测温数据csv
                if (m_strJpegWithAppenData.dwP2PDataLen > 0) {
                    String path = hotFir + newName + ".csv";
                    log.info("hotFircsv地址：" + path);
                    FileWriter fos = new FileWriter(path);
                    log.info("dwJpegPicHeight: " + m_strJpegWithAppenData.dwJpegPicHeight + ", dwJpegPicWidth: " + m_strJpegWithAppenData.dwJpegPicWidth);
                    //height 512 width 640
                    for (int i = 1; i <= m_strJpegWithAppenData.dwJpegPicHeight; i++) {
                        for (int j = 1; j <= m_strJpegWithAppenData.dwJpegPicWidth; j++) {
                            byte[] sourceData =
                                    m_strJpegWithAppenData.pP2PDataBuff.getByteArray((i - 1) * m_strJpegWithAppenData.dwJpegPicWidth * 4 + (j - 1) * 4, 4);
                            int ss =
                                    sourceData[0] & 0xFF | (sourceData[1] & 0xFF) << 8 | (sourceData[2] & 0xFF) << 16 | (sourceData[3] & 0xFF) << 24;
                            fos.write(String.valueOf(Float.intBitsToFloat(ss)));
                            fos.write(",");
                        }
                        fos.append('\n');
                    }
                    fos.flush();
                    fos.close();
                    map.put("csvPath", hotFirShow + newName + ".csv");
                    copyFile(newName + ".csv", path);
                    log.info("hotFirShowcsv地址：" + hotFirShow + newName + ".csv");

                } else {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("csv获取失败错误码" + iErr);
                }
            } else {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.info("红外温感文件获取接口调用失败" + iErr);
            }
        } catch (Exception e) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("系统异常" + iErr);
            log.error("红外图片抓取异常", e);
        } finally {
            hCNetSDK.NET_DVR_Logout(lUserID);
            //hCNetSDK.NET_DVR_Cleanup();
        }
        return map;
    }

    public static final int ISAPI_DATA_LEN = 2 * 1024 * 1024;
    public static final int ISAPI_STATUS_LEN = 2 * 1024 * 1024;
    public static final int BYTE_ARRAY_LEN = 1024;

    /**
     * dlt664红外抓图
     *
     * @param cameraId
     * @param presetId
     * @return
     */
    public Map<String, String> givePicFir2(long cameraId, Long presetId, String meteName) {
        Map<String, String> map = new HashMap<String, String>();
        int lUserID = -1;
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
            cameraConInfo.setChannelNum(cameraConInfo.getChannelNum() + 32);
            log.info("通道号：" + cameraConInfo.getChannelNum());
            log.info("getRecordId:" + cameraConInfo.getRecordId());
            String userName = cameraConInfo.getCameraManager();
            log.info("userName" + userName);
            String password = cameraConInfo.getCameraCode();
            log.info("password" + password);
            String cameraIp = cameraConInfo.getCameraIp();
            log.info("cameraIp" + cameraIp);
            String port = cameraConInfo.getInfreadPort().toString();
            log.info("port:>>>" + port);
           /* if (!hCNetSDK.NET_DVR_Init()) {
                log.info("初始化失败");
            }*/
            log.info("开始登录。。。。。");
            m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
            System.arraycopy(cameraIp.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, cameraIp.length());
            m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
            System.arraycopy(userName.getBytes(), 0, m_strLoginInfo.sUserName, 0, userName.length());
            m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
            System.arraycopy(password.getBytes(), 0, m_strLoginInfo.sPassword, 0, password.length());
            m_strLoginInfo.wPort = Short.parseShort(port);
            m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
            m_strLoginInfo.write();
            log.info("登录账号:" + userName + "ip地址" + cameraIp + "登录密码" + password + "端口号" + port);
            lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
            log.info("NET_DVR_Login_V40:返回值" + lUserID);
//            int lUserIDLong = lUserID;
            //转到预置点
            boolean Preset = hCNetSDK.NET_DVR_PTZPreset_Other(lUserID, 2, HCNetSDK.GOTO_PRESET,
                    cameraConInfo.getPresetNum());
            if (Preset) {
                log.info("转到预置点成功：Preset->" + Preset);
            } else {
                log.info("转到预置点信息Preset->" + Preset);
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.info("转到预置点失败错误码" + iErr);
            }

            try {
                Long waitTime =
                        Long.valueOf(redisTemplate.opsForHash().get("t_sys_param:waitTime", "content").toString());
                log.info("waitTime:----------" + waitTime);
                Thread.sleep(waitTime);
            } catch (Exception e) {
                log.error("error----" + e);
            }

//            m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
//            System.arraycopy(cameraIp.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, cameraIp.length());
//            m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
//            System.arraycopy(userName.getBytes(), 0, m_strLoginInfo.sUserName, 0, userName.length());
//            m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
//            System.arraycopy(password.getBytes(), 0, m_strLoginInfo.sPassword, 0, password.length());
//            m_strLoginInfo.wPort = Short.parseShort(port);
//            m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
//            m_strLoginInfo.write();
//            log.info("登录账号:" + userName + "ip地址" + cameraIp + "登录密码" + password + "端口号" + port);
//            lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
//            log.info("NET_DVR_Login_V40:返回值" + lUserID);
//            lUserIDLong = new NativeLong(lUserID);
            //透传url
            String strURL = "/ISAPI/Thermal/channels/2/thermometry/jpegPicWithAppendData?format=json";
//            HCNetSDK.BYTE_ARRAY ptrUrl = new HCNetSDK.BYTE_ARRAY(BYTE_ARRAY_LEN);
//            ptrUrl.byValue = strURL.getBytes();
//            ptrUrl.write();
            //透传json standard 标准模式
            String buf = "{\n" +
                    "\t\"JpegPicWithAppendDataParam\":\n" +
                    "\t{\n" +
                    "\t\t\"captureMode\":\"standard\"\n" +
                    "\t}\n" +
                    "}";
//            HCNetSDK.BYTE_ARRAY pbuf = new HCNetSDK.BYTE_ARRAY(BYTE_ARRAY_LEN);
//            pbuf.byValue = buf.getBytes();
//            pbuf.write();
//
//            HCNetSDK.NET_DVR_XML_CONFIG_INPUT struXMLInput = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
//            struXMLInput.read();
//            struXMLInput.dwSize = struXMLInput.size();
//            struXMLInput.lpRequestUrl = ptrUrl.getPointer();
//            struXMLInput.dwRequestUrlLen = ptrUrl.byValue.length;
//            struXMLInput.lpInBuffer = pbuf.getPointer();
//            struXMLInput.dwInBufferSize = buf.length();
//            struXMLInput.write();
//
//            HCNetSDK.BYTE_ARRAY ptrStatusByte = new HCNetSDK.BYTE_ARRAY(ISAPI_STATUS_LEN);
//            ptrStatusByte.read();
//
            HCNetSDK.BYTE_ARRAY ptrOutByte = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
            ptrOutByte.read();

//            HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT struXMLOutput = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();
//            struXMLOutput.read();
//            struXMLOutput.dwSize = struXMLOutput.size();
//            struXMLOutput.lpOutBuffer = ptrOutByte.getPointer();
//            struXMLOutput.dwOutBufferSize = ptrOutByte.size();
//            struXMLOutput.lpStatusBuffer = ptrStatusByte.getPointer();
//            struXMLOutput.dwStatusSize = ptrStatusByte.size();
//            struXMLOutput.write();

//            if (!hCNetSDK.NET_DVR_STDXMLConfig(lUserID, struXMLInput, struXMLOutput)) {
//                int iErr = hCNetSDK.NET_DVR_GetLastError();
//                log.error("NET_DVR_STDXMLConfig失败，错误号：" + iErr);
//            } else {
            byte [] result = postISAPI(cameraIp,userName,password,strURL, buf);
//                struXMLOutput.read();
                ptrOutByte.byValue= result;
                ptrOutByte.write();
                ptrOutByte.read();
//                ptrStatusByte.read();
                FileOutputStream fout;
                String newName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                try {

                    //测温图片
                    String picPath = hotPic + newName + ".jpg";
                    log.info("hotPic地址：" + picPath);
                    fout = new FileOutputStream(picPath);
//                    long offsetPic = 419;
                    long offsetPic = 327;
                    ByteBuffer picBuffers = ptrOutByte.getPointer().getByteBuffer(offsetPic,
                            (result.length - offsetPic));
                    byte[] picBytes =
                            new byte[result.length - Integer.parseInt(String.valueOf(offsetPic + 16))];
                    picBuffers.rewind();
                    picBuffers.get(picBytes);
                    fout.write(picBytes);
                    fout.close();
                    // 红外图片添加水印的开关
                    String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isWatermarkToInfrared",
                            "content"));
                    if (StringUtils.isNotEmpty(meteName) && StringUtils.equals("true", flag)) {
                        log.info("红外图片，添加水印，路径：" + picPath);
                        pictureWaterMark(picPath, DateTimeUtil.format(new Date()) + "--" + meteName);
                    }
                    map.put("picPath", hotPicshow + newName + ".jpg");
                    map.put("urlPath", hotPicshow + newName + ".jpg");
                    map.put("absPath", picPath);
                    copyFile(newName + ".jpg", picPath);
                    log.info("hotPicshow地址：" + hotPicshow + newName + ".jpg");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String date = sdf.format(new Date());
                    date = ByteUtil.stringToHex(date);
                    String picString = ByteUtil.toHexString(picBytes);
                    //时间位置
                    assert date != null;
                    int b = picString.indexOf(date);
                    if (b < 8) {
                        throw new RuntimeException("红外图片时间异常, b=" + b);
                    }
                    String hs = picString.substring(b - 8, b - 4);
                    byte[] heightByte = ByteUtil.hex2byte(hs.getBytes());
                    String ws = picString.substring(b - 4, b);
                    byte[] widthByte = ByteUtil.hex2byte(ws.getBytes());
                    //矩阵高度
                    int height = ByteUtil.unintFrom2Bytes(heightByte, 0, true);
                    log.info("矩阵高度 {} ", height);
                    //矩阵宽度
                    int width = ByteUtil.unintFrom2Bytes(widthByte, 0, true);
                    log.info("矩阵宽度 {} ", width);

                    int a = b + 28;
                    //拍摄时间之前的字节
                    String realPic = picString.substring(0, a);
                    //拍摄时间之前的字节长度
                    long size1 = ByteUtil.hex2byte(realPic.getBytes()).length;
                    //红外温度值点阵数据
                    String dataStr = picString.substring(a, a + (width * height * 8));
                    //红外温度值点阵数据长度
                    long size2 = ByteUtil.hex2byte(dataStr.getBytes()).length;
                    //起始位置
                    long size3 = picBytes.length - size2 - size1;
                    //测温数据data
                    String datPath = hotFir + newName + ".data";
                    log.info("hotFirdata地址：" + datPath);
                    fout = new FileOutputStream(datPath);
                    //将字节写入文件
                    long offset = offsetPic + size1;
                    ByteBuffer buffers = ptrOutByte.getPointer().getByteBuffer(offset,
                            result.length - offset);
                    byte[] bytes =
                            new byte[result.length - Integer.parseInt(String.valueOf(size3 + offset + 16))];
                    buffers.rewind();
                    buffers.get(bytes);
                    fout.write(bytes);
                    fout.close();
                    map.put("dataPath", hotFirShow + newName + ".data");
                    copyFile(newName + ".data", datPath);
                    log.info("hotFirShowdata地址：" + hotFirShow + newName + ".data");

                    String csvPath = hotFir + newName + ".csv";
                    log.info("hotFircsv地址：" + csvPath);
                    FileWriter fos = new FileWriter(csvPath);

                    HCNetSDK.BYTE_ARRAY byte_array = new HCNetSDK.BYTE_ARRAY(Integer.parseInt(String.valueOf(size2)));
                    byte_array.byValue = bytes;
                    byte_array.write();
                    byte_array.read();

                    float max = 0;
                    float min = 0;
                    for (int i = 1; i <= width; i++) {
                        for (int j = 1; j <= height; j++) {
                            byte[] sourceData =
                                    byte_array.getPointer().getByteArray((i - 1) * width * 4 + (j - 1) * 4, 4);
                            int ss =
                                    sourceData[0] & 0xFF | (sourceData[1] & 0xFF) << 8 | (sourceData[2] & 0xFF) << 16 | (sourceData[3] & 0xFF) << 24;
                            float value = Float.intBitsToFloat(ss);
                            max = max == 0 ? value : max;
                            min = min == 0 ? value : min;
                            max = Math.max(max, value);
                            min = Math.min(min, value);
                            fos.write(String.valueOf(value));
                            fos.write(",");
                        }
                        fos.append('\n');
                    }
                    log.info("max {}", max);
                    log.info("min {}", min);
                    fos.flush();
                    fos.close();
                    // 红外是否需要算法分析 不需要的话直接给温度值
                    boolean isInfraredAnalysis = Boolean.valueOf(redisTemplate.opsForHash().get("t_sys_param:isInfraredAnalysis", "content").toString());
                    if (Boolean.FALSE.equals(isInfraredAnalysis)){
                        DecimalFormat df = new DecimalFormat("##0.00");
                        map.put("resultNum", df.format(max) + "," + df.format(min));
                    }
                    map.put("csvPath", hotFirShow + newName + ".csv");
                    copyFile(newName + ".csv", csvPath);
                    log.info("hotFirShowcsv地址：" + hotFirShow + newName + ".csv");
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
//            }
        } catch (Exception e) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.error("红外图片抓取异常 [{}]", iErr, e);
        } finally {
            hCNetSDK.NET_DVR_Logout(lUserID);
        }
        return map;
    }

    /**
     * 透传ISAPI
     * @param ip
     * @param username
     * @param password
     * @param isapiUrl
     * @param json
     * @return
     * @throws Exception
     */
    public static byte[] postISAPI(String ip, String username, String password, String isapiUrl, String json)
            throws Exception {
        String url = "http://" + ip + isapiUrl;
        HttpPost httpPost = new HttpPost(url);
        Credentials creds = new UsernamePasswordCredentials(username, password);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, creds);
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        httpPost.setEntity(new StringEntity(json, "UTF-8"));
        HttpResponse response = httpclient.execute(httpPost);
        return  EntityUtils.toByteArray(response.getEntity());
    }
    /**
     * 语音对讲开始
     *
     * @param videoIntercomId 门口机id
     */
    public int startVoiceTalk(Long videoIntercomId) {

        int re = 1;
        int lUserID = -1;
        log.info("开启可视对讲");
        try {
            String logpath = "/home/yjh/iot-center-accessvideo-1.0.0/logs";

            log.info("开启可视对讲videoIntercomId：" + videoIntercomId);
            String url = SERVICE_URL + "?videoIntercomId=" + videoIntercomId;
            String services = HttpClientUtils.getInstance().getUrl(url, null);
            log.info("services：" + services);
            JSONObject jsonObject = JSONObject.parseObject(services);
            Map<String, Object> videoIntercom = (Map<String, Object>) jsonObject.get("data");
            String userName = videoIntercom.get("owner").toString();
            log.info("userName" + userName);
            String password = videoIntercom.get("ownerCode").toString();
            log.info("password" + password);
            String cameraIp = videoIntercom.get("cameraIp").toString();
            log.info("cameraIp" + cameraIp);
            String port = videoIntercom.get("port").toString();
            log.info("port:>>>" + port);
            if (!hCNetSDK.NET_DVR_Init()) {
                log.info("语音对讲初始化失败");
            }
            log.info("语音对讲开始登录。。。。。");
            hCNetSDK.NET_DVR_SetLogToFile(3, logpath, false);
            m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
            System.arraycopy(cameraIp.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, cameraIp.length());
            m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
            System.arraycopy(userName.getBytes(), 0, m_strLoginInfo.sUserName, 0, userName.length());
            m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
            System.arraycopy(password.getBytes(), 0, m_strLoginInfo.sPassword, 0, password.length());
            m_strLoginInfo.wPort = Short.parseShort(port);
            m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
            m_strLoginInfo.write();
            log.info("登录账号:" + userName + "ip地址" + cameraIp + "登录密码" + password + "端口号" + port);
            lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
            log.info("NET_DVR_Login_V40:返回值" + lUserID);

            HCNetSDK.FVoiceDataCallBack_V30 fVoiceDataCallBack = null;
            HCNetSDK.NET_DVR_COMPRESSION_AUDIO lpCompressAudio = new HCNetSDK.NET_DVR_COMPRESSION_AUDIO();
            boolean net_DVR_GetCurrentAudioCompress = hCNetSDK.NET_DVR_GetCurrentAudioCompress(lUserID, lpCompressAudio);
            log.info("net_DVR_GetCurrentAudioCompress:" + net_DVR_GetCurrentAudioCompress);
            byte byAudioEncType = lpCompressAudio.byAudioEncType;
            byte[] byres = lpCompressAudio.byres;
            // byte byAudioSamplingRate = lpCompressAudio.byAudioSamplingRate;
            // byte byAudioBitRate = lpCompressAudio.byAudioBitRate;
            // byte bySupport = lpCompressAudio.bySupport;
            log.info("音频编码类型={}   音频采样率={}    音频码率={}  bySupport={}", byAudioEncType, "", "", Arrays.toString(byres));

            if (mVoiceTalkHandle.longValue() < 0) {
                int mVoiceTalkHandle = hCNetSDK.NET_DVR_StartVoiceCom_V30(lUserID, 1, false, fVoiceDataCallBack, null);
                log.info("mVoiceTalkHandle:返回值" + mVoiceTalkHandle);
                if (mVoiceTalkHandle == -1) {
                    re = 0;
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("NET_DVR_StartVoiceCom_V30 mVoiceTalkHandle   --->" + iErr);
                } else {
                    re = 1;
                    log.info("NET_DVR_StartVoiceCom_V30 SUCC 内部");
                    // hCNetSDK.NET_DVR_SetVoiceComClientVolume(new NativeLong(mVoiceTalkHandle), (short) 0xffff);
                }
            } else {
                re = 1;
                //  hCNetSDK.NET_DVR_SetVoiceComClientVolume(mVoiceTalkHandle, (short) 0xffff);
                log.info("NET_DVR_StartVoiceCom_V30 SUCC:开启对讲");
            }
        } catch (Exception e) {
            re = 0;
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            e.printStackTrace();
            log.info("NET_DVR_StartVoiceCom_V30 Exception" + iErr);
            log.info("NET_DVR_StartVoiceCom_V30 ....." + e.getMessage());
        } finally {
            hCNetSDK.NET_DVR_Logout(lUserID);
            //hCNetSDK.NET_DVR_Cleanup();
        }
        return re;
    }

    /**
     * 语音对讲结束
     */
    public void stopVoiceTalk() {
        try {
            if (mVoiceTalkHandle.longValue() >= 0) {
                // boolean res=  hCNetSDK.NET_DVR_StopRemoteConfig(mVoiceTalkHandle.intValue());
                boolean res = hCNetSDK.NET_DVR_StopVoiceCom(mVoiceTalkHandle.intValue());
                log.info("NET_DVR_StopVoiceCom 返回：" + res);
                if (res) {
                    mVoiceTalkHandle = new NativeLong(-1);
                    log.info("NET_DVR_StopRemoteConfig SUCC ");
                } else {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.info("NET_DVR_StopRemoteConfig false: " + iErr);
                }

                mVoiceTalkHandle = new NativeLong(-1);
                log.info("关闭后的mVoiceTalkHandle值：" + mVoiceTalkHandle);
            }

        } catch (Exception e) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("NET_DVR_StopRemoteConfig Exception " + iErr);
            log.info("NET_DVR_StopRemoteConfig Exception " + e.getMessage());
        }
    }

    /**
     * 语音对讲视频
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startVideoRealPlay(Long videoIntercomId) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            String url = SERVICE_URL + "?videoIntercomId=" + videoIntercomId;
            String services = HttpClientUtils.getInstance().getUrl(url, null);
            log.info("services" + services);
            JSONObject jsonObject = JSONObject.parseObject(services);
            log.info("jsonObject" + jsonObject.toJSONString());
            Map<String, Object> videoIntercom = (Map<String, Object>) jsonObject.get("data");
            String userName = videoIntercom.get("owner").toString();
            log.info("userName" + userName);
            String password = videoIntercom.get("ownerCode").toString();
            log.info("password" + password);
            String cameraIp = videoIntercom.get("cameraIp").toString();
            log.info("cameraIp" + cameraIp);
            String cameraPort = videoIntercom.get("rtspPort").toString();
            log.info("rtspPort" + cameraPort);
            int iChanNum = 1;
            String livePath = String.valueOf(videoIntercomId) + "9909";
            log.info(userName + " " + password + " " + cameraIp + " " + cameraPort + " " + iChanNum + " " + " " + videoIntercomId);
            String transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,
                    videoDefinition, livePath);
            Runtime.getRuntime().exec(transUrl);
            log.info("transUrl: " + transUrl);
            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];
            String flvUrl = "http://" + hostIp + ":10080/live/" + livePath + ".flv";
            String webRtc = "webrtc://" + hostIp + "/live/" + livePath;
            returnMap.put("webRtcUrl", webRtc);
            returnMap.put("flvUrl", flvUrl);
            returnMap.put("rtmpUrl", rtmpUrl);
            StreamInfoThread streamInfoThread = new StreamInfoThread(srsStopUrl, "cameraRealFlow:",
                    Integer.parseInt(livePath), Long.parseLong(livePath), returnMap, redisTemplate);
            Thread thread = new Thread(streamInfoThread);
            thread.setDaemon(true);
            thread.start();
            log.info("returnMap: " + returnMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return returnMap;
    }

    //    @Logs(title = "获取可视状态", content = "获取可视状态",logType = 1)
    @Transactional(rollbackFor = Exception.class)
    public int getVidemoIntercomStatus(Long videoIntercomId) {
        int re = 1;

        int lUserID = -1;
        try {
            log.info("开启可视对讲videoIntercomId：" + videoIntercomId);
            String url = SERVICE_URL + "?videoIntercomId=" + videoIntercomId;
            String services = HttpClientUtils.getInstance().getUrl(url, null);
            log.info("services：" + services);
            JSONObject jsonObject = JSONObject.parseObject(services);
            Map<String, Object> videoIntercom = (Map<String, Object>) jsonObject.get("data");
            String userName = videoIntercom.get("owner").toString();
            log.info("userName" + userName);
            String password = videoIntercom.get("ownerCode").toString();
            log.info("password" + password);
            String cameraIp = videoIntercom.get("cameraIp").toString();
            log.info("cameraIp" + cameraIp);
            String port = videoIntercom.get("port").toString();
            log.info("port:>>>" + port);
           /* if (!hCNetSDK.NET_DVR_Init()) {
                log.info("初始化失败");
            }*/
            log.info("开始登录。。。。。");
            m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
            System.arraycopy(cameraIp.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, cameraIp.length());
            m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
            System.arraycopy(userName.getBytes(), 0, m_strLoginInfo.sUserName, 0, userName.length());
            m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
            System.arraycopy(password.getBytes(), 0, m_strLoginInfo.sPassword, 0, password.length());
            m_strLoginInfo.wPort = Short.parseShort(port);
            m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
            m_strLoginInfo.write();
            log.info("登录账号:" + userName + "ip地址" + cameraIp + "登录密码" + password + "端口号" + port);
            lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
            if (lUserID > -1) {
                re = 0;
            } else {
                re = 1;
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.info("获取可视状态 错误 " + iErr);
            }
        } catch (Exception e) {
            re = 1;
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("获取可视状态  Exception " + iErr);
        } finally {
            hCNetSDK.NET_DVR_Logout(lUserID);
            //  hCNetSDK.NET_DVR_Cleanup();
        }
        return re;
    }

    /**
     * 获取cvs上某行某列的值
     */
    public String lineTemperature(String path, String points) {

        int row, column, xPlus, yPlus;
        String[] arrs = points.split(",");
        File file = new File(path);
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        path = hotFirShow + fileName + ".csv";
        log.info("cvs path:" + path);
        String temperature = "0.00";

        if (arrs.length == 4) {
            column = Integer.parseInt(arrs[0]);
            row = Integer.parseInt(arrs[1]) + 1;
            xPlus = Integer.parseInt(arrs[2]);
            yPlus = Integer.parseInt(arrs[3]);
            try {
                URL url = new URL(path);
                URLConnection connection = url.openConnection();
                InputStream stream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream, "GBK");
                BufferedReader reade = new BufferedReader(reader);
                String line = null;
                int rowIndex = 1;
                List<Double> arr = new ArrayList<>();
                while ((line = reade.readLine()) != null) {
                    //CSV格式文件为逗号分隔符文件，这里根据逗号切分
                    String[] item = line.split(",");
                    if (row <= rowIndex && rowIndex <= row + yPlus) {
                        List<Double> arrColumn = new ArrayList<>();
                        for (int i = 0; i <= item.length; i++) {
                            if (column - 1 <= i && i <= column + xPlus) {
                                arrColumn.add(Double.parseDouble(item[i - 1]));
                            }
                        }
                        arr.add(Collections.max(arrColumn));
                    } else if (rowIndex > row + yPlus) {
                        break;
                    }
                    rowIndex++;
                }
                log.info("arr: " + arr);
                //转换保留后两位小数
                temperature = new DecimalFormat("0.00").format(Collections.max(arr));
                log.info("框测temperature转换保留后两位小数" + temperature);
            } catch (Exception e) {
                e.getMessage();
            }
            return temperature;
        } else {
            column = Integer.parseInt(arrs[0]);
            row = Integer.parseInt(arrs[1]) + 1;
            try {
                URL url = new URL(path);
                URLConnection connection = url.openConnection();
                InputStream stream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream, "GBK");
                BufferedReader reade = new BufferedReader(reader);
                String line = null;
                int rowIndex = 0;
                while ((line = reade.readLine()) != null) {
                    //CSV格式文件为逗号分隔符文件，这里根据逗号切分
                    String[] item = line.split(",");
                    if (row == rowIndex) {
                        temperature = item[column - 1];
                        break;
                    }
                    rowIndex++;
                }
                //转换保留后两位小数
                temperature = new DecimalFormat("0.00").format(Double.parseDouble(temperature));
                log.info("点测temperature转换保留后两位小数" + temperature);
            } catch (Exception e) {
                e.getMessage();
            }
            return temperature;
        }
    }

    /**
     * 获取行列区域画面最高温度
     *
     * @param cameraId 摄像头id
     */
    public String getlineTemperature(long cameraId, String points) {
        int row = 0, column = 0, xPlus = 0, yPlus = 0;
        String[] arrs = points.split(",");
        String temperature = "0.00";

        log.info("arrs.length：" + arrs.length);
        if (arrs.length == 4) {
            column = Integer.parseInt(arrs[0]);
            row = Integer.parseInt(arrs[1]) + 1;
            xPlus = Integer.parseInt(arrs[2]);
            yPlus = Integer.parseInt(arrs[3]);
        }
        if (arrs.length == 2) {
            column = Integer.parseInt(arrs[0]);
            row = Integer.parseInt(arrs[1]) + 1;
        }
        int lUserIDLong = -1;
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            cameraConInfo.setChannelNum(cameraConInfo.getChannelNum() + 32);
            String userName = cameraConInfo.getCameraManager();
            String password = cameraConInfo.getCameraCode();
            String cameraIp = cameraConInfo.getCameraIp();
            String port = cameraConInfo.getInfreadPort().toString();
            log.info("通道号：" + cameraConInfo.getChannelNum() + ", recordId:" + cameraConInfo.getRecordId() + ", " +
                    "userName:" + userName + ", password:" + password
                    + ", cameraIp:" + cameraIp + ", port:" + port);
            m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
            System.arraycopy(cameraIp.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, cameraIp.length());
            m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
            System.arraycopy(userName.getBytes(), 0, m_strLoginInfo.sUserName, 0, userName.length());
            m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
            System.arraycopy(password.getBytes(), 0, m_strLoginInfo.sPassword, 0, password.length());
            m_strLoginInfo.wPort = Short.parseShort(port);
            m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
            m_strLoginInfo.write();
            lUserIDLong = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);// new
            // NativeLong(Constant.maps.get(String.valueOf(cameraConInfo.getRecordId())));
            log.info("lUserIDLong" + lUserIDLong);
            HCNetSDK.NET_DVR_JPEGPICTURE_WITH_APPENDDATA m_strJpegWithAppenData =
                    new HCNetSDK.NET_DVR_JPEGPICTURE_WITH_APPENDDATA();
            m_strJpegWithAppenData.dwSize = m_strJpegWithAppenData.size();
            m_strJpegWithAppenData.dwChannel = 1;
            HCNetSDK.BYTE_ARRAY ptrJpegByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 * 1024);
            HCNetSDK.BYTE_ARRAY ptrP2PDataByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 * 1024);
            m_strJpegWithAppenData.pJpegPicBuff = ptrJpegByte.getPointer();
            m_strJpegWithAppenData.pP2PDataBuff = ptrP2PDataByte.getPointer();
            boolean bRet = hCNetSDK.NET_DVR_CaptureJPEGPicture_WithAppendData(lUserIDLong, 2, m_strJpegWithAppenData);
            log.info("bRet返回值：" + bRet);
            //height 512 width 640
            if (bRet && m_strJpegWithAppenData.dwP2PDataLen > 0) {
                if (arrs.length == 4) {
                    float[] arr = new float[yPlus - row + 1];
                    int rowIndex = 0;
                    for (int i = 1; i <= m_strJpegWithAppenData.dwJpegPicHeight; i++) {
                        if (i >= yPlus) {
                            break;
                        }
                        if (row <= i && i < yPlus) {
                            float[] arrTem = new float[xPlus - column + 1];
                            int columnIndex = 0;
                            for (int j = 1; j <= m_strJpegWithAppenData.dwJpegPicWidth; j++) {
                                if (column <= j && j < xPlus) {
                                    byte[] sourceData =
                                            m_strJpegWithAppenData.pP2PDataBuff.getByteArray((i - 1) * m_strJpegWithAppenData.dwJpegPicWidth * 4 + (j - 1) * 4, 4);
                                    int ss =
                                            sourceData[0] & 0xFF | (sourceData[1] & 0xFF) << 8 | (sourceData[2] & 0xFF) << 16 | (sourceData[3] & 0xFF) << 24;
                                    arrTem[columnIndex] = Float.intBitsToFloat(ss);
                                    columnIndex++;
                                }
                            }
                            Arrays.sort(arrTem);
                            arr[rowIndex] = arrTem[arrTem.length - 1];
                            rowIndex++;
                        }
                    }
                    //获取最大温度值
                    Arrays.sort(arr);
                    float max = arr[arr.length - 1];
                    temperature = new DecimalFormat("##0.00").format(max);
                    log.info("框测数据: " + temperature);
                }
                if (arrs.length == 2) {
                    for (int i = 1; i <= m_strJpegWithAppenData.dwJpegPicHeight; i++) {
                        if (i == row) {
                            for (int j = 1; j <= m_strJpegWithAppenData.dwJpegPicWidth; j++) {
                                if (j == column) {
                                    byte[] sourceData =
                                            m_strJpegWithAppenData.pP2PDataBuff.getByteArray((i - 1) * m_strJpegWithAppenData.dwJpegPicWidth * 4 + (j - 1) * 4, 4);
                                    int ss =
                                            sourceData[0] & 0xFF | (sourceData[1] & 0xFF) << 8 | (sourceData[2] & 0xFF) << 16 | (sourceData[3] & 0xFF) << 24;
                                    temperature = new DecimalFormat("##0.00").format(Float.intBitsToFloat(ss));
                                    log.info("点测数据: " + temperature);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            } else {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.info("温度获取失败错误码: " + iErr);
            }

        } catch (Exception e) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("系统异常: " + iErr);
        } finally {
            hCNetSDK.NET_DVR_Logout(lUserIDLong);
        }
        return temperature;
    }

    /**
     * 设置发射率和距离
     */
    public void getSetconfig(int lUserID) {

        HCNetSDK.NET_DVR_THERMOMETRY_PRESETINFO m_struThermometryInfo = new HCNetSDK.NET_DVR_THERMOMETRY_PRESETINFO();
        m_struThermometryInfo.dwSize = m_struThermometryInfo.size();
        HCNetSDK.NET_DVR_THERMOMETRY_COND m_struThermometryCond = new HCNetSDK.NET_DVR_THERMOMETRY_COND();
        m_struThermometryCond.dwSize = m_struThermometryCond.size();
        //通道号
        m_struThermometryCond.dwChannel = 2;
        m_struThermometryCond.write();
        HCNetSDK.NET_DVR_STD_CONFIG struCfg = new HCNetSDK.NET_DVR_STD_CONFIG();
        struCfg.lpCondBuffer = m_struThermometryCond.getPointer();
        struCfg.dwCondSize = m_struThermometryCond.size();
        HCNetSDK.BYTE_ARRAY m_szStatusBuf = new HCNetSDK.BYTE_ARRAY(4096 * 4);
        struCfg.lpStatusBuffer = m_szStatusBuf.getPointer();
        struCfg.dwStatusSize = 4096 * 4;
        struCfg.byDataType = 0;
        boolean bRet = hCNetSDK.NET_DVR_GetSTDConfig(lUserID, 6701, struCfg);
        if (bRet) {
            int nErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("NET_DVR_GetSTDConfig 信息：" + nErr);
        } else {
            log.info("NET_DVR_GetSTDConfig  success");
        }
        m_struThermometryInfo.dwSize = m_struThermometryInfo.size();
        // m_struThermometryInfo.wPresetNo = 1;
        m_struThermometryInfo.struPresetInfo[0] = new HCNetSDK.NET_DVR_THERMOMETRY_PRESETINFO_PARAM();
        m_struThermometryInfo.struPresetInfo[0].byEnabled = 1;
        m_struThermometryInfo.struPresetInfo[0].byRuleID = 1;
        // 距离，单位：米(m)，取值范围：[0,10000]
        m_struThermometryInfo.struPresetInfo[0].wDistance = 2;
        //发射率(即物体向外辐射能量的本领，精确到小数点后两位)，取值范围：[0.01, 1.00]
        //该值对于不支持规则框以及预置点的设备使用
        m_struThermometryInfo.struPresetInfo[0].fEmissivity = (float) 0.98;
        m_struThermometryInfo.struPresetInfo[0].byReflectiveEnabled = 0;
        //反射温度 精确到小数后一位
        //m_struThermometryInfo.struPresetInfo[0].fReflectiveTemperature = 20;
        //距离单位: 0- 米(m)，1- 英尺(feet)，2-厘米（cm）
        m_struThermometryInfo.struPresetInfo[0].byDistanceUnit = 0;
        m_struThermometryInfo.write();
        struCfg.lpInBuffer = m_struThermometryInfo.getPointer();
        struCfg.dwInSize = m_struThermometryInfo.size();


        boolean m = hCNetSDK.NET_DVR_SetSTDConfig(lUserID, 6701, struCfg);
        if (m == false) {
            int nErr = hCNetSDK.NET_DVR_GetLastError();
            log.info("NET_DVR_SET_THERMOMETRY_PRESETINFO 信息：" + nErr);
        } else {
            log.info("NET_DVR_SET_THERMOMETRY_PRESETINFO success");
        }
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
//            int lUserIDLong = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
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
//               // boolean PlayBackControl = hCNetSDK.NET_DVR_PlayBackControl(fileV40, hCNetSDK.NET_DVR_PLAYSTART,
//               0, null);
//                boolean PlayBackControl=hCNetSDK.NET_DVR_PlayBackControl_V40(fileV40,hCNetSDK.NET_DVR_PLAYSTART,
//                null,0,null,null);
//                log.info("PlayBackControl：" + PlayBackControl + "dwControlCode:" + hCNetSDK.NET_DVR_PLAYSTART +
//                "dwInValue:" + 0 + "lpOutValue" + hCNetSDK.NET_DVR_GETTOTALTIME);
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

    //@Logs(title = "相机停止播放", code = "cameraStopPlay")
    @Transactional(rollbackFor = Exception.class)
    public String stopRealPlay(Long cameraId, String rtmpUrl) {
//        String urlStop = null;
//        String livePath;
//        if (Objects.equals(null, rtmpUrl) || rtmpUrl.equals("")) {
//            String rtmpUrlCamera = Constant.mapsForCamera.get(String.valueOf(cameraId));
//            String[] rtmpUrlCameras = rtmpUrlCamera.split("/");
//            livePath = rtmpUrlCameras[rtmpUrlCameras.length - 1];
//        } else {
//            String[] rtmpUrls = rtmpUrl.split("/");
//            livePath = rtmpUrls[rtmpUrls.length - 1];
//        }
//        log.info("livePath: " + livePath);
//        String url = "ps -ef | grep ffmpeg | grep '" + livePath + "' | grep -v 'grep'";
//        log.info("stopUrl: " + url);
//        try {
//            Process processForId = Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
//            processForId.waitFor();
//            BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(),
//            "UTF-8"));
//            String lineForId = null;
//            StringBuilder dataBackForId = new StringBuilder();
//            while ((lineForId = readerForId.readLine()) != null) {
//                dataBackForId.append(lineForId).append('\n');
//            }
//            Integer processNum = Integer.parseInt(dataBackForId.substring(9, 15).replace(" ", ""));
//            urlStop = "kill -9 " + processNum;
//            Runtime.getRuntime().exec(urlStop);
//            Constant.mapsForCamera.remove(String.valueOf(cameraId));
//            redisTemplate.delete("cameraRealFlow:" + cameraId);
//        } catch (Exception e) { e.getMessage(); }
        //todo 目前先不关闭ffmpeg进程,在多用户同时播放统一个相机视频，一个用户关闭进程后，另外一个用户则无法观看
        //回放视频流可停止
        if (StringUtils.isNotBlank(rtmpUrl) && rtmpUrl.contains("history")) {
            cameraId = Long.valueOf(StringUtils.substringAfterLast(rtmpUrl, "/"));
            manager.terminate(cameraId);
            log.info("kill all history stream");
            StreamStopThread streamStopThread = new StreamStopThread();
            TaskExecutePool.getInstance().execute(streamStopThread);
        }
//        manager.terminate(cameraId);
        return "stop " + cameraId + " preview success!";
    }

    //@Logs(title = "机器人停止播放", code = "robotStopPlay", content = "机器人相机停止播放")
    @Transactional(rollbackFor = Exception.class)
    public String robotStopRealPlay(Long robotId) {
//        String urlStop = "";
//        String livePath;
//        String lightCameraId = String.valueOf(robotId)+"9901";
//        String infraredCameraId = String.valueOf(robotId)+"9902";
////        String rtmpUrlCamera = Constant.mapsForCamera.get(lightCameraId);
////        if (Objects.isNull(rtmpUrlCamera)) {
////            return urlStop;
////        }
////        String[] rtmpUrlCameras = rtmpUrlCamera.split("/");
////        livePath = rtmpUrlCameras[rtmpUrlCameras.length - 1];
//        livePath = lightCameraId;
//        log.info("livePath: " + livePath);
//        String url = "ps -ef | grep ffmpeg | grep '" + livePath + "' | grep -v 'grep'";
//        log.info("stopRobotLightUrl: " + url);
//        try {
//            Process processForId = Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
//            processForId.waitFor();
//            BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(),
//            "UTF-8"));
//            String lineForId = null;
//            StringBuilder dataBackForId = new StringBuilder();
//            while ((lineForId = readerForId.readLine()) != null) {
//                dataBackForId.append(lineForId).append('\n');
//            }
//            Integer processNum = Integer.parseInt(dataBackForId.substring(9, 15).replace(" ", ""));
//            urlStop = "kill -9 " + processNum;
//            Runtime.getRuntime().exec(urlStop);
//            Constant.mapsForCamera.remove(lightCameraId);
//            redisTemplate.delete("cameraRealFlow:" + lightCameraId);
//
////            String rtmpUrlRobot = Constant.mapsForCamera.get(infraredCameraId);
////            String[] rtmpUrlRobots = rtmpUrlRobot.split("/");
////            livePath = rtmpUrlCameras[rtmpUrlRobots.length - 1];
//            livePath = infraredCameraId;
//            log.info("livePath: " + livePath);
//            String urlRobot = "ps -ef | grep ffmpeg | grep '" + livePath + "' | grep -v 'grep'";
//            log.info("stopRobotInfraedUrl: " + urlRobot);
//            Process processForIdRobot = Runtime.getRuntime().exec(new String[]{"sh", "-c", urlRobot});
//            processForIdRobot.waitFor();
//            BufferedReader readerForIdRobot = new BufferedReader(new InputStreamReader(processForIdRobot
//            .getInputStream(), "UTF-8"));
//            String lineForIdRobot = null;
//            StringBuilder dataBackForIdRobot = new StringBuilder();
//            while ((lineForIdRobot = readerForIdRobot.readLine()) != null) {
//                dataBackForIdRobot.append(lineForIdRobot).append('\n');
//            }
//            Integer processNumRobot = Integer.parseInt(dataBackForIdRobot.substring(9, 15).replace(" ", ""));
//            String urlStopRobot = "kill -9 " + processNumRobot;
//            Runtime.getRuntime().exec(urlStopRobot);
//            Constant.mapsForCamera.remove(infraredCameraId);
//            redisTemplate.delete("cameraRealFlow:" + infraredCameraId);
//        } catch (Exception e) { e.getMessage(); }
        //todo 目前先不关闭ffmpeg进程,在多用户同时播放统一个相机视频，一个用户关闭进程后，另外一个用户则无法观看
//        String lightCameraId = String.valueOf(robotId)+"9901";
//        manager.terminate(Long.parseLong(lightCameraId));
//        String infraredCameraId = String.valueOf(robotId)+"9902";
//        manager.terminate(Long.parseLong(infraredCameraId));
        return "stop " + robotId + " preview success!";
    }

    @Transactional(rollbackFor = Exception.class)
    public String stopStream(Long cameraId) {
        manager.terminate(cameraId);
        return "stop " + cameraId + " preview success!";
    }

    @Transactional(rollbackFor = Exception.class)
    public String stopAllStream() {
        manager.terminateAll();
        return "stop All preview success!";
    }

    /*
     * 获取全画面最高温度
     *
     * @param cameraId 摄像头id
     * @return
     */
//    public List<String> getTemperature(long cameraId) {
//
//        List<String> list = new ArrayList<String>();
//        try {
//            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
//            cameraConInfo.setChannelNum(cameraConInfo.getChannelNum() + 32);
//            log.info("通道号：" + cameraConInfo.getChannelNum());
//            log.info("getRecordId:" + cameraConInfo.getRecordId());
//            String userName = cameraConInfo.getCameraManager();
//            log.info("userName" + userName);
//            String password = cameraConInfo.getCameraCode();
//            log.info("password" + password);
//            String cameraIp = cameraConInfo.getCameraIp();
//            log.info("cameraIp" + cameraIp);
//            String port = cameraConInfo.getInfreadPort().toString();
//            log.info("port:>>>" + port);
//         /*  if (!hCNetSDK.NET_DVR_Init()) {
//                log.info("初始化失败");
//            }*/
//            log.info("开始登录。。。。。");
//            m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
//            System.arraycopy(cameraIp.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, cameraIp.length());
//            m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
//            System.arraycopy(userName.getBytes(), 0, m_strLoginInfo.sUserName, 0, userName.length());
//            m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
//            System.arraycopy(password.getBytes(), 0, m_strLoginInfo.sPassword, 0, password.length());
//            m_strLoginInfo.wPort = Short.parseShort(port);
//            m_strLoginInfo.bUseAsynLogin = 0; //是否异步登录：0- 否，1- 是
//            m_strLoginInfo.write();
//            log.info("登录账号:" + userName + "ip地址" + cameraIp + "登录密码" + password + "端口号" + port);
//            lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
//            log.info("NET_DVR_Login_V40:返回值" + lUserID);
//            //log.info("lUserIDLong:返回值"+lUserIDLong);
//            NativeLong lUserIDLong = new NativeLong(lUserID);// new NativeLong(Constant.maps.get(String.valueOf(cameraConInfo
//            .getRecordId())));
//            log.info("lUserIDLong" + lUserIDLong);
//            //getSetconfig(lUserIDLong);
//            HCNetSDK.NET_DVR_JPEGPICTURE_WITH_APPENDDATA m_strJpegWithAppenData = new HCNetSDK
//            .NET_DVR_JPEGPICTURE_WITH_APPENDDATA();
//            m_strJpegWithAppenData.dwSize = m_strJpegWithAppenData.size();
//            m_strJpegWithAppenData.dwChannel = 2;
//            HCNetSDK.BYTE_ARRAY ptrJpegByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 * 1024);
//            HCNetSDK.BYTE_ARRAY ptrP2PDataByte = new HCNetSDK.BYTE_ARRAY(2 * 1024 * 1024);
//            m_strJpegWithAppenData.pJpegPicBuff = ptrJpegByte.getPointer();
//            m_strJpegWithAppenData.pP2PDataBuff = ptrP2PDataByte.getPointer();
//           // log.info("m_strJpegWithAppenData的值:" + m_strJpegWithAppenData.toString());
//            boolean bRet = hCNetSDK.NET_DVR_CaptureJPEGPicture_WithAppendData(lUserIDLong, 2, m_strJpegWithAppenData);
//            log.info("bRet返回值：" + bRet);
//            if (bRet) {
//                if (m_strJpegWithAppenData.dwP2PDataLen > 0) {
//                    list.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//                    byte[] byTempData = new byte[4];
//                    float[] arr = new float[m_strJpegWithAppenData.dwP2PDataLen];
//                    for (int i = 1; i <= m_strJpegWithAppenData.dwJpegPicWidth; i++) {
//                        for (int j = 1; j <= m_strJpegWithAppenData.dwJpegPicHeight; j++) {
//                            ByteBuffer TempDatabuffers = m_strJpegWithAppenData.pP2PDataBuff.getByteBuffer((i - 1)
//                            * (j - 1) * 4, 4);
//                            TempDatabuffers.get(byTempData);
//                            int l;
//                            l = byTempData[0];
//                            l &= 0xff;
//                            l |= ((long) byTempData[1] << 8);
//                            l &= 0xffff;
//                            l |= ((long) byTempData[2] << 16);
//                            l &= 0xffffff;
//                            l |= ((long) byTempData[3] << 24);
//                            arr[i] = Float.intBitsToFloat(l);
////                                  log.info("行数：" + i +"列数：" + j + "温度数据：" +   Float.intBitsToFloat(l) );
//                        }
//                    }
//                    Arrays.parallelSort(arr);
//                    float max  = arr[arr.length - 1];
//                    /*//获取最大温度值
//                    float max = arr[0];
//                    for (int i = 0; i < arr.length; i++) {
//                        //4.把获取到的数据一次和temp进行比较，并将最大的值赋值给temp
//                        if (arr[i] > max) {
//                            max = arr[i];
//                        }
//                    }*/
//                    //转换保留后两位小数
//                    list.add( new DecimalFormat("##0.00").format(max)+ "℃");
//
//                } else {
//                    list = null;
//                    int iErr = hCNetSDK.NET_DVR_GetLastError();
//                    log.info("温度不存在" + iErr);
//                }
//
//            } else {
//                int iErr = hCNetSDK.NET_DVR_GetLastError();
//                log.info("温度获取失败错误码" + iErr);
//            }
//
//        } catch (Exception e) {
//            int iErr = hCNetSDK.NET_DVR_GetLastError();
//            log.info("系统异常" + iErr);
//        } finally {
//            hCNetSDK.NET_DVR_Logout(lUserIDLong);
//            //hCNetSDK.NET_DVR_Cleanup();
//        }
//        return list;
//    }

    public void pushCtrlTime(Long cameraId) {
        try {
            String str = "camera_info:" + cameraId;
            Map<String, String> map = redisTemplate.opsForHash().entries(str);
            if (map.size() > 0) {
                map.put("lastTime", format.format(new Date()));
            } else {
                map = new HashMap<>();
                map.put("cameraId", String.valueOf(cameraId));
            }
            redisTemplate.opsForHash().putAll(str, map);
        } catch (Exception e) {
            log.info("更新相机最后操作时间出错：" + e);
        }
    }

    public String getPresetBasePath() {
        return (String) redisTemplate.opsForHash().get("t_sys_param:presetImgPath", "content");
    }

    public String getPresetUrlPath() {
        return (String) redisTemplate.opsForHash().get("t_sys_param:presetRealImgPath", "content");
    }

    public Map<String,String> playBackByTime(Long cameraId,String startTime,String endTime) {
        Map<String,String> returnMap = Maps.newHashMap();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            String userName = cameraConInfo.getIdentityManager(); //nvr 用户名
            String password = cameraConInfo.getIdentityCode();    //nvr 密码
            String cameraIp = cameraConInfo.getRecordIp();        //nvr ip
            int cameraPort = cameraConInfo.getRtspPort();         //nvr rtsp port
            int iChanNum = cameraConInfo.getChannelNum();         //nvr 通道号

            String startTimeTem = startTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
            String stopTimeTem = endTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
            String starttime = startTimeTem.replace(" ", "Z");
            String endtime = stopTimeTem.replace(" ", "Z");

            Long id = System.currentTimeMillis();
            String transUrl = String.format(UrlBackTem, userName, password, cameraIp, cameraPort, iChanNum, 1,
                starttime, endtime, id);
            log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, starttime:{}, endtime:{}, " +
                    "historyPath:{}, historyTransUrl: {}"
                , userName, password, cameraIp, cameraPort, iChanNum, starttime, endtime, cameraId, transUrl);
            VideoInfo videoInfo = new VideoInfo().setCommand(transUrl).setId(id);
            manager.run(videoInfo);

            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp" + rtmpUrls[rtmpUrls.length - 1];
            log.info("rtmpUrl:{}",rtmpUrl);
            returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
            returnMap.put("rtmpUrl", rtmpUrl);
            String webRtc = "webrtc://" + hostIp + "/history/" + id;
            log.info("webrtc:{}",webRtc);
            returnMap.put("webRtcUrl", webRtc);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return returnMap;
    }

    private void copyFile(String ftpsPath, String localPath) {
        // 如果 ftpsTurbo 为 true，则表示设置了文件盘共享，或者没有单独部署，不使用 ftps 对文件进行传输拷贝
        if (Constant.ftpsTurbo()) {
            return;
        }
        try {
            if (StringUtils.isEmpty(ftpsPath) || StringUtils.isEmpty(localPath)) {
                return;
            }
            FtpsUtil.putFile(localPath, "video" + ftpsPath, platFromFtpsConfig.getIp(), platFromFtpsConfig.getPort(),
                    platFromFtpsConfig.getKeypw(), platFromFtpsConfig.getUsername(), platFromFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至 platform ftp 服务器错误:", e);
        }
        HashMap<String, String> param = new HashMap<>();
        param.put("ftpsPath", "video" + ftpsPath);
        param.put("localPath", localPath);
        Constant.otherServerMap(param, Constant.COPY_FILE_URL);
    }

    /**
     * 获取相机对应与之位的PTZ数据
     *
     * @param presetId presetId
     * @param cameraId cameraId
     * @return result
     */
    public String getCameraPTZ(Long presetId, Long cameraId) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
        int userId = Constant.maps.get(String.valueOf(cameraConInfo.getRecordId()));
        Integer channelNum = cameraConInfo.getChannelNum();
        return getCameraPTZ(userId, channelNum, cameraConInfo.getPresetNum());
    }

    /**
     * 调用相机SDK，获取相机对应与之位的PTZ数据
     *
     * @param userId userId
     * @param channelNum channelNum
     * @param presetNum presetNum
     * @return result
     */
    private String getCameraPTZ(Integer userId, Integer channelNum, int presetNum) {
        try {
            IntByReference ibrBytesReturned = new IntByReference();
            HCNetSDK.NET_DVR_PTZPOS ipcfg = new HCNetSDK.NET_DVR_PTZPOS();
            ipcfg.write();
            Pointer lpIpParaConfig = ipcfg.getPointer();
            // 获取相关参数配置
            if (hCNetSDK.NET_DVR_GetDVRConfig(userId, HCNetSDK.NET_DVR_GET_PTZPOS, channelNum + 32,
                lpIpParaConfig, ipcfg.size(), ibrBytesReturned)) {
                ipcfg.read();
                return String.format("%d,%d,%d", ipcfg.wPanPos, ipcfg.wTiltPos, ipcfg.wZoomPos);
            } else {
                log.error("获取预置位信息失败, error: {}", hCNetSDK.NET_DVR_GetLastError());
            }
        } catch (Exception e) {
            log.error("获取相机预置位PTZ参数错误: {}", e.getMessage());
        }

        return "";
    }
}
