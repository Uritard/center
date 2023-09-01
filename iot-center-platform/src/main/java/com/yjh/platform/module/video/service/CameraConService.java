package com.yjh.platform.module.video.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.video.dao.CameraConDao;
import com.yjh.platform.module.video.entity.CameraConInfo;
import com.yjh.platform.module.video.entity.RecorderConInfo;
import com.yjh.platform.module.video.entity.RobotConInfo;
import com.yjh.video.api.CameraVendor;
import com.yjh.video.api.entity.PlayBackEntity;
import com.yjh.video.api.entity.PlayEntity;
import com.yjh.video.api.entity.PtzControlEntity;
import com.yjh.video.api.entity.preset.PresetCmd;
import com.yjh.video.api.entity.preset.PresetEntity;
import com.yjh.video.api.result.Result;
import com.yjh.video.api.service.IPlayService;
import com.yjh.video.api.service.IPlaybackService;
import com.yjh.video.api.service.IPtzService;
import com.yjh.video.api.service.VideoServiceFactory;
import com.yjh.video.api.service.impl.IPlayServiceImpl;
import com.yjh.video.api.service.impl.IPtzServiceImpl;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhangyuyi
 * @create 2023-08-28
 */
@Service
public class CameraConService {
    private final static Logger log = LoggerFactory.getLogger(CameraConService.class);
    @Autowired
    private CameraConDao cameraConDao;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    /**
     * 流媒体服务器 ZLMediaKit
     */
    private static final String MEDIA_ZLK = "ZLMediaKit";

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public void isCameraControlled(Long cameraId) {
        Map<String, Object> camreaStatusMap = redisTemplate.opsForHash().entries("camera_info:" + cameraId);
        log.info("camreaStatusMap: {}, camreaStatusMapState: {}", camreaStatusMap, camreaStatusMap.get("state"));
        if (Objects.nonNull(camreaStatusMap.get("state"))) {
            int state = MapUtils.getIntValue(camreaStatusMap, "state");
            if (1 == state) {
                try {
                    //判断时间问题
                    String lastTime = MapUtils.getString(camreaStatusMap, "lastTime");
                    Date endDate = DateTimeUtil.parse(lastTime);
                    if (System.currentTimeMillis() - endDate.getTime() > 10 * 60 * 1000) {
                        //最后一次操控时间距离现在大于10分钟
                        camreaStatusMap.put("state", "0");
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

    public void pushCtrlTime(Long cameraId) {
        try {
            String str = "camera_info:" + cameraId;
            Map<String, String> map = redisTemplate.opsForHash().entries(str);
            if (map.size() > 0) {
                map.put("lastTime", format.format(new Date()));
            } else {
                map = new HashMap<>();
                map.put("cameraId", String.valueOf(cameraId));
                map.put("lastTime", format.format(new Date()));
            }
            redisTemplate.opsForHash().putAll(str, map);
        } catch (Exception e) {
            log.info("更新相机最后操作时间出错：" + e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> startRealPlay(Long cameraId) {

        Map<String, Object> returnMap = new HashMap<>();

        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            if (Objects.isNull(cameraConInfo)) {
                // 相机不存在
                returnMap.put("webRtcUrl", "");
                return returnMap;
            }
            PlayEntity.Builder builder = new PlayEntity.Builder();
            builder.setDeviceId(cameraConInfo.getDeviceChannel());
            builder.setChannelId(cameraConInfo.getCameraChannelId());
            PlayEntity playEntity = new PlayEntity.Builder()
                    .setDeviceId(cameraConInfo.getDeviceChannel())
                    .setChannelId(cameraConInfo.getCameraChannelId()).build();
            IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
            Result result = iPlayService.videoPlayByWvp(playEntity);
            Map data = (Map) result.getData();
            webRtcUrl((String) data.get("rtc"), returnMap, 1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return returnMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public String stopRealPlay(Long cameraId, String rtmpUrl) {
        //todo 目前先不关闭ffmpeg进程,在多用户同时播放统一个相机视频，一个用户关闭进程后，另外一个用户则无法观看
        //回放视频流可停止
        if (StringUtils.isNotBlank(rtmpUrl) && rtmpUrl.contains("history")) {
            cameraId = Long.valueOf(StringUtils.substringAfterLast(rtmpUrl, "/"));
//            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            // 调用停止播放API
            PlayEntity playEntity = PlayEntity.builder().setDeviceId(String.valueOf(cameraId)).build();
            IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
            iPlayService.stopRealPlay(playEntity);
        }
        return "stop " + cameraId + " preview success!";
    }

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
            PlayEntity.Builder builder = new PlayEntity.Builder();
            builder.setDeviceId(cameraConInfo.getDeviceChannel());
            builder.setChannelId(cameraConInfo.getCameraChannelId());
            PlayEntity playEntity = PlayEntity.builder().
                    setDeviceId(cameraConInfo.getDeviceChannel())
                    .setChannelId(cameraConInfo.getCameraChannelId())
                    .build();
            Result result = new Result();
            IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
            try {
                result = iPlayService.videoPlayByWvp(playEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map data = (Map) result.getData();
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
            webRtcUrl((String) data.get("rtc"), returnMap, 1);
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

        String lightCameraId = robotId + "9901";
        String infraredCameraId = robotId + "9902";
        RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
        IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
        // 当配置了nvr的通道就走nvr的，并且recordID != null and recordID > 0L and recorderConInfo != null and recorderConInfo.getRecordIp() != null ,反之走原先的
        if (Objects.nonNull(robotConInfo.getRecordId()) && robotConInfo.getRecordId() > 0L) {
            RecorderConInfo recorderConInfo = cameraConDao.selectByRecordId(robotConInfo.getRecordId());
            if (Objects.nonNull(recorderConInfo) && StringUtils.isNotEmpty(recorderConInfo.getRecordIp())) {
                try {
                    PlayEntity playEntity;
                    if (StringUtils.isNotEmpty(robotConInfo.getLightChannelId())) {
                        playEntity = PlayEntity.builder().setDeviceId(recorderConInfo.getDeviceChannel())
                                .setChannelId(String.valueOf(robotConInfo.getLightChannelId())).build();
                        Result result = iPlayService.videoPlayByWvp(playEntity);
                        Map data = (Map) result.getData();
                        Map<String, Object> lightMap = new HashMap<>();
                        lightMap.put("light", lightCameraId);
                        lightMap.put("rtmpUrl", data.get("rtmp"));
                        webRtcUrl((String) data.get("rtc"), lightMap, 1);
                        returnMapList.add(lightMap);
                    }
                    if (StringUtils.isNotEmpty(robotConInfo.getInfraredChannelId())) {
                        playEntity = PlayEntity.builder().setDeviceId(recorderConInfo.getDeviceChannel())
                                .setChannelId(String.valueOf(robotConInfo.getInfraredChannelId())).build();
                        Result result = iPlayService.videoPlayByWvp(playEntity);
                        Map data = (Map) result.getData();
                        Map<String, Object> infraredMap = new HashMap<>();
                        infraredMap.put("light", infraredCameraId);
                        infraredMap.put("rtmpUrl", data.get("rtmp"));
                        webRtcUrl((String) data.get("rtc"), infraredMap, 1);
                        returnMapList.add(infraredMap);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } else {
            // 拉流
            try {
                Map<String, Object> returnLightMap = getRobotStream(robotConInfo, lightCameraId);

                String inferadIp = robotConInfo.getLnferadIp();
                Integer inferadPort = robotConInfo.getInferadPort();
                String transUrlinferad;
                if (robotConInfo.getRobotType() == 161) {
                    String inferadUsername = robotConInfo.getInferadUsername();
                    String inferadPassword = robotConInfo.getInferadPassword();
                    String robotLightVideo = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "robotLightVideo"));
                    transUrlinferad = String.format(robotLightVideo, inferadUsername, inferadPassword, inferadIp,
                            inferadPort, 1, infraredCameraId);
                } else if (robotConInfo.getRobotType() == 157) {
                    String robotA200InfraredVideo = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "robotA200InfraredVideo"));
                    transUrlinferad = String.format(robotA200InfraredVideo, inferadIp, inferadPort, infraredCameraId);
                } else {
                    String robotInfraredVideo = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "robotInfraredVideo"));
                    transUrlinferad = String.format(robotInfraredVideo, inferadIp, inferadPort, infraredCameraId);
                }
                transUrlinferad = sign(transUrlinferad, infraredCameraId);
//                VideoInfo videoInfo2 = new VideoInfo().setCommand(transUrlinferad).setId(infraredCameraId);
                PlayEntity playEntity = PlayEntity.builder().setDeviceId(infraredCameraId)
                        .setCommand(transUrlinferad)
                        .build();
                iPlayService.videoPlayByFFM(playEntity);
                log.info("robotInferadInfo: {}, {}, {}, robotTransUrlinferad:{}", inferadIp, inferadPort,
                        infraredCameraId, transUrlinferad);
                String[] rtmpUrlsInferad = transUrlinferad.split("rtmp");
                String rtmpUrlInferad = "rtmp" + rtmpUrlsInferad[rtmpUrlsInferad.length - 1];

                Map<String, Object> returnInferadMap = new HashMap<>();
                returnInferadMap.put("inferad", infraredCameraId);
                returnInferadMap.put("rtmpUrlInferad", rtmpUrlInferad);
                webRtcUrl(infraredCameraId, returnInferadMap);
                returnMapList.add(returnLightMap);
                returnMapList.add(returnInferadMap);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return returnMapList;
    }

    public Map<String, Object> getRobotStream(RobotConInfo robotConInfo, String robotCameraId) {
        Map<String, Object> returnLightMap = new HashMap<>();
        String wvpServerIp = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "wvpServerIp"));
        Integer wvpServerPort = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "wvpServerPort")));
        IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
        try {
            if (StringUtils.isNotEmpty(robotConInfo.getLightChannelId()) && Objects.nonNull(robotConInfo.getRecordId())
                    && robotConInfo.getRecordId() > 0L) {
                RecorderConInfo recorderConInfo = cameraConDao.selectByRecordId(robotConInfo.getRecordId());
                PlayEntity playEntity = new PlayEntity.Builder()
                        .setDeviceId(recorderConInfo.getDeviceChannel())
                        .setChannelId(robotConInfo.getLightChannelId()).build();
                Result result = iPlayService.videoPlayByWvp(playEntity);
                Map data = (Map) result.getData();
                webRtcUrl((String) data.get("rtc"), returnLightMap, 1);
            } else {
                String lightIp = robotConInfo.getLightIp();
                String lightPort = robotConInfo.getLightPort();
                String lightUsername = robotConInfo.getIdentityManager();
                String lightPassword = robotConInfo.getIdentityCode();
                String robotLightVideo = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "robotLightVideo"));
                String transUrlLight = String.format(robotLightVideo, lightUsername, lightPassword, lightIp, lightPort, 1, robotCameraId);
                transUrlLight = sign(transUrlLight, robotCameraId);
                PlayEntity playEntity = new PlayEntity.Builder()
                        .setCommand(transUrlLight).setDeviceId(robotCameraId).build();
                iPlayService.videoPlayByFFM(playEntity);
                log.info("robotLightInfo: {}, {}, {}, {}, {}, robotTransUrlLight:{}", lightUsername, lightPassword,
                        lightIp, lightPort, robotCameraId, transUrlLight);
                returnLightMap.put("light", robotCameraId);
                webRtcUrl(robotCameraId, returnLightMap);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return returnLightMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public String robotStopRealPlay(Long robotId) {
        return "stop " + robotId + " preview success!";
    }

    @Transactional(rollbackFor = Exception.class)
    public String stopStream(String cameraId) {
        PlayEntity playEntity = PlayEntity.builder().setDeviceId(String.valueOf(cameraId)).build();
        IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
        iPlayService.stopRealPlay(playEntity);
        return "stop " + cameraId + " preview success!";
    }

    public String sign(String transUrl, Long cameraId) {
        String cameraStr = cameraId == null ? null : String.valueOf(cameraId);
        return sign(transUrl, cameraStr);
    }

    public String sign(String transUrl, String cameraId) {
        String mediaServer = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "mediaServer"));
        String mediaSignKey = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "mediaSignKey"));
        if (!MEDIA_ZLK.equalsIgnoreCase(mediaServer)) {
            return transUrl;
        }
        String signKey = StringUtils.isEmpty(cameraId) ? mediaSignKey : cameraId + "_" + mediaSignKey;
        String checkSign = DigestUtils.md5DigestAsHex(signKey.getBytes());
        String checkUrl = StringUtils.substringAfterLast(transUrl, "/");

        return StringUtils.contains(checkUrl, "?") ? (transUrl + "\\&sign=" + checkSign) : (transUrl + "?sign=" + checkSign);
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
                    cameraConInfo.setDeviceChannel(recorderConInfo.getDeviceChannel());
                    cameraConInfo.setCameraChannelId(String.valueOf(robotConInfo.getChannelNumLight()));
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
                    cameraConInfo.setDeviceChannel(recorderConInfo.getDeviceChannel());
                    cameraConInfo.setCameraChannelId(String.valueOf(robotConInfo.getChannelNumInferad()));
                }
            } else {
                cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            }
            Long id = System.currentTimeMillis();
            if (cameraConInfo.getCameraChannelId() == null) {
                String userName = cameraConInfo.getIdentityManager(); //nvr 用户名
                String password = cameraConInfo.getIdentityCode();    //nvr 密码
                String cameraIp = cameraConInfo.getRecordIp();        //nvr ip
                int cameraPort = cameraConInfo.getRtspPort();         //nvr rtsp port
                int iChanNum = cameraConInfo.getChannelNum();         //nvr 通道号

                String startTimeTem = startTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
                String stopTimeTem = stopTime.replace("-", "").replace(":", "").replace(" ", "T") + " ";
                String starttime = startTimeTem.replace(" ", "Z");
                String endtime = stopTimeTem.replace(" ", "Z");


                String nvrRtmpBack = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "nvrRtmpBack"));
                String transUrl = String.format(nvrRtmpBack, userName, password, cameraIp, cameraPort, iChanNum, 1,
                        starttime, endtime, id);
                transUrl = sign(transUrl, id);
                log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, starttime:{}, endtime:{}, " +
                                "historyPath:{}, historyTransUrl: {}"
                        , userName, password, cameraIp, cameraPort, iChanNum, starttime, endtime, cameraId, transUrl);
//                VideoInfo videoInfo = new VideoInfo().setCommand(transUrl).setId(id.toString());
                PlayEntity playEntity = PlayEntity.builder().setDeviceId(id.toString())
                        .setCommand(transUrl).build();
                IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
                iPlayService.videoPlayByFFM(playEntity);
                webRtcUrl(id, returnMap, true);
            } else {
                String starttime = startTime.replace(" ", "%20");
                String endtime = stopTime.replace(" ", "%20");
                PlayBackEntity playBackEntity = PlayBackEntity.builder().setDeviceId(cameraConInfo.getDeviceChannel())
                        .setChannelId(cameraConInfo.getCameraChannelId())
                        .setStartTime(starttime)
                        .setEndTime(endtime).build();
                IPlaybackService iPlaybackService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlaybackService.class);
                Result result = iPlaybackService.playBackStart(playBackEntity);
                Map resultMap = (Map) result.getData();
                webRtcUrl((String) resultMap.get("rtc"), returnMap, 1);
            }
            returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
            String starttime = startTime.replace(" ", "%20");
            String endtime = stopTime.replace(" ", "%20");
            PlayBackEntity playBackEntity = PlayBackEntity.builder()
                    .setDeviceId(cameraConInfo.getDeviceChannel())
                    .setChannelId(cameraConInfo.getCameraChannelId())
                    .setStartTime(starttime)
                    .setEndTime(endtime).build();
            IPlaybackService iPlaybackService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlaybackService.class);
            Result result = iPlaybackService.playBackStart(playBackEntity);
            Map resultMap = (Map) result.getData();
            webRtcUrl((String) resultMap.get("rtc"), returnMap, 1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return returnMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public Object pTZControl(int dwPTZCommand, Long cameraId, int dStop, int speed) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        String command;
        Integer horizonSpeed = 0;
        Integer verticalSpeed = 0;
        Integer zoomSpeed = 0;
        if (dStop == 1) {
            command = "stop";
            horizonSpeed = 0;
            verticalSpeed = 0;
            zoomSpeed = 0;
        } else {
            switch (dwPTZCommand) {
                case 21:
                    command = "up";
                    verticalSpeed = speed;
                    break;
                case 22:
                    command = "down";
                    verticalSpeed = speed;
                    break;
                case 23:
                    command = "left";
                    horizonSpeed = speed;
                    break;
                case 24:
                    command = "right";
                    horizonSpeed = speed;
                    break;
                case 11:
                    command = "zoomout";
                    zoomSpeed = speed;
                    break;
                case 12:
                    command = "zoomin";
                    zoomSpeed = speed;
                    break;
                case 25:
                    command = "upleft";
                    verticalSpeed = speed;
                    horizonSpeed = speed;
                    break;
                case 26:
                    command = "upright";
                    verticalSpeed = speed;
                    horizonSpeed = speed;
                    break;
                case 27:
                    command = "downleft";
                    verticalSpeed = speed;
                    horizonSpeed = speed;
                    break;
                case 28:
                    command = "downright";
                    verticalSpeed = speed;
                    horizonSpeed = speed;
                    break;
                default:
                    command = "stop";
                    horizonSpeed = 0;
                    verticalSpeed = 0;
                    zoomSpeed = 0;
                    break;
            }
        }
        IPtzService iPtzService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPtzService.class);
        PtzControlEntity ptzControlEntity = PtzControlEntity.builder()
                .setDeviceId(cameraConInfo.getDeviceChannel())
                .setChannelId(cameraConInfo.getCameraChannelId())
                .setCommand(command)
                .setHorizonSpeed(horizonSpeed)
                .setVerticalSpeed(verticalSpeed)
                .setZoomSpeed(zoomSpeed)
                .build();
        try {
            iPtzService.ptzControl(ptzControlEntity);
            return "success";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "false";
        }
    }

    /**
     * 预置位操作
     * @param presetId
     * @param cameraId
     * @param presetCmd
     * @return
     * @throws IOException
     */
    public boolean presetAction(Long presetId, Long cameraId, PresetCmd presetCmd) throws IOException {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
        if (cameraConInfo == null) {
            throw new BusinessException("无此摄像机或摄像机预置位不正确");
        }
        String wvpServerIp = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "wvpServerIp"));
        Integer wvpServerPort = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "wvpServerPort")));
        IPtzService ptzService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPtzService.class);
        PresetEntity presetEntity = PresetEntity.builder()
                .setIp(wvpServerIp)
                .setPort(wvpServerPort)
                .setPresetId(String.valueOf(presetId))
                .setDeviceId(cameraConInfo.getDeviceChannel())
                .setChannelId(cameraConInfo.getCameraChannelId())
                .setPresetCmd(presetCmd)
                .build();
        Result result = ptzService.presetCommand(presetEntity);
        boolean ret = result.getCode() == 200;
        if (ret && presetCmd.equals(PresetCmd.PRESET_DELETE)) {
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

    /**
     * 获取相机PTZ信息
     * @param presetId
     * @param cameraId
     * @return
     * @throws IOException
     */
    public String getCameraPTZ(Long presetId, Long cameraId) throws IOException {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
        if (cameraConInfo == null) {
            throw new BusinessException("无此摄像机或摄像机预置位不正确");
        }
        //调用海康IsApi接口
        IPtzService ptzService = VideoServiceFactory.loadSnapService(CameraVendor.HIK, IPtzService.class);
        PresetEntity presetEntity = PresetEntity.builder()
                .setIp(cameraConInfo.getCameraIp())
                .setPort(80)
                .setUserName(cameraConInfo.getCameraManager())
                .setPassword(cameraConInfo.getCameraCode())
                .build();
        Result result = ptzService.getCameraPtz(presetEntity);
        return String.valueOf(result.getData());
    }

    public String getPresetBasePath() {
        return (String) redisTemplate.opsForHash().get("t_sys_param:presetImgPath", "content");
    }

    /**
     * 组装webRtc播放路径，SRS和ZLMediaKit播放路径不同
     */
    private void webRtcUrl(Long id, Map<String, Object> returnMap) {
        webRtcUrl(String.valueOf(id), returnMap, false);
    }

    private void webRtcUrl(String url, Map<String, Object> returnMap, Integer isWebRtcUrl) {
        Map<String, String> hostIpMap = redisTemplate.opsForHash().entries("t_sys_param:hostIp");
        String hostIp = hostIpMap.get("content");
        String[] split = hostIp.split(":");
        String newUrl = CommonUtils.replaceIpAndPort(url, split[0], split[1]);
        returnMap.put("webRtcUrl", newUrl);
    }

    private void webRtcUrl(String id, Map<String, Object> returnMap) {
        webRtcUrl(id, returnMap, false);
    }

    private void webRtcUrl(Long id, Map<String, Object> returnMap, boolean isHistory) {
        webRtcUrl(String.valueOf(id), returnMap, isHistory);
    }

    private void webRtcUrl(String id, Map<String, Object> returnMap, boolean isHistory) {
        String mediaServer = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "mediaServer"));
        String videoHttps = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "videoHttpsEnable"));
        Map<String, String> hostIpMap = redisTemplate.opsForHash().entries("t_sys_param:hostIp");
        String hostIp = hostIpMap.get("content");
        String webRtcUrl;
        if (MEDIA_ZLK.equalsIgnoreCase(mediaServer)) {
            // http://127.0.0.1/index/api/webrtc?app=live&stream=test&type=play
            String scheam = "1".equals(videoHttps) ? "https://" : "http://";
            String app = isHistory ? "history" : "live";
            webRtcUrl = scheam + hostIp + "/ZLM/index/api/webrtc?app=" + app + "&stream=" + id + "&type=play";
        } else {
            // webrtc://172.24.39.10/live/40001
            webRtcUrl = "webrtc://" + hostIp + (isHistory ? "/history/" : "/live/") + id;
        }
        returnMap.put("webRtcUrl", webRtcUrl);
    }

}
