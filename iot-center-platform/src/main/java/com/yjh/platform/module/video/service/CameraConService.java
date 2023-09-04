package com.yjh.platform.module.video.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.video.dao.CameraConDao;
import com.yjh.platform.module.video.entity.CameraConInfo;
import com.yjh.platform.module.video.entity.RecorderConInfo;
import com.yjh.platform.module.video.entity.RobotConInfo;
import com.yjh.video.api.CameraVendor;
import com.yjh.video.api.entity.*;
import com.yjh.video.api.result.Result;
import com.yjh.video.api.service.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
            Map<String, String> map = new HashMap<>();
            map.put("cameraId", String.valueOf(cameraId));
            map.put("lastTime", DateTimeUtil.getDateTimeString());
            redisTemplate.opsForHash().putAll(str, map);
        } catch (Exception e) {
            log.info("更新相机最后操作时间出错：", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startRealPlay(Long cameraId) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            if (Objects.isNull(cameraConInfo)) {
                // 相机不存在
                returnMap.put("webRtcUrl", "");
                return returnMap;
            }
            PlayEntity playEntity = PlayEntity.builder()
                    .deviceId(cameraConInfo.getDeviceChannel())
                    .channelId(cameraConInfo.getCameraChannelId()).build();
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
            PlayEntity playEntity = PlayEntity.builder().deviceId(String.valueOf(cameraId)).build();
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
            PlayEntity playEntity = PlayEntity.builder()
                    .deviceId(cameraConInfo.getDeviceChannel())
                    .channelId(cameraConInfo.getCameraChannelId())
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
                        playEntity = PlayEntity.builder().deviceId(recorderConInfo.getDeviceChannel())
                                .channelId(String.valueOf(robotConInfo.getLightChannelId())).build();
                        Result result = iPlayService.videoPlayByWvp(playEntity);
                        Map data = (Map) result.getData();
                        Map<String, Object> lightMap = new HashMap<>();
                        lightMap.put("light", lightCameraId);
                        lightMap.put("rtmpUrl", data.get("rtmp"));
                        webRtcUrl((String) data.get("rtc"), lightMap, 1);
                        returnMapList.add(lightMap);
                    }
                    if (StringUtils.isNotEmpty(robotConInfo.getInfraredChannelId())) {
                        playEntity = PlayEntity.builder().deviceId(recorderConInfo.getDeviceChannel())
                                .channelId(String.valueOf(robotConInfo.getInfraredChannelId())).build();
                        Result result = iPlayService.videoPlayByWvp(playEntity);
                        Map data = (Map) result.getData();
                        Map<String, Object> infraredMap = new HashMap<>();
                        infraredMap.put("inferad", infraredCameraId);
                        infraredMap.put("rtmpUrlInferad", data.get("rtmp"));
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
                PlayEntity playEntity = PlayEntity.builder().deviceId(infraredCameraId)
                        .command(transUrlinferad)
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
                        .deviceId(recorderConInfo.getDeviceChannel())
                        .channelId(robotConInfo.getLightChannelId()).build();
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
                        .command(transUrlLight).deviceId(robotCameraId).build();
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
        PlayEntity playEntity = PlayEntity.builder().deviceId(String.valueOf(cameraId)).build();
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
                PlayEntity playEntity = PlayEntity.builder().deviceId(id.toString())
                        .command(transUrl).build();
                IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
                iPlayService.videoPlayByFFM(playEntity);
                webRtcUrl(id, returnMap, true);
            } else {
                String starttime = startTime.replace(" ", "%20");
                String endtime = stopTime.replace(" ", "%20");
                PlayBackEntity playBackEntity = PlayBackEntity.builder().deviceId(cameraConInfo.getDeviceChannel())
                        .channelId(cameraConInfo.getCameraChannelId())
                        .startTime(starttime)
                        .endTime(endtime).build();
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
                    .deviceId(cameraConInfo.getDeviceChannel())
                    .channelId(cameraConInfo.getCameraChannelId())
                    .startTime(starttime)
                    .endTime(endtime).build();
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
                .deviceId(cameraConInfo.getDeviceChannel())
                .channelId(cameraConInfo.getCameraChannelId())
                .command(command)
                .horizonSpeed(horizonSpeed)
                .verticalSpeed(verticalSpeed)
                .zoomSpeed(zoomSpeed)
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
     * 转动预置位
     *
     * @param presetId 预置位id
     * @param cameraId 相机id
     * @return 结果
     */
    public boolean moveToPreset(Long presetId, Long cameraId) {
        boolean flag;
        this.isCameraControlled(cameraId);
        flag = this.presetAction(presetId, cameraId, PresetCmd.PRESET_ACTION);
        this.pushCtrlTime(cameraId);
        return flag;
    }

    /**
     * 任务中转到预置点
     *
     * @param presetId 预置位id
     * @param cameraId 相机id
     * @return 结果
     */
    public boolean moveToPresetForTask(Long presetId, Long cameraId) {
        boolean flag;
        flag = this.presetAction(presetId, cameraId, PresetCmd.PRESET_ACTION);
        this.pushCtrlTime(cameraId);
        return flag;
    }

    /**
     * 设置预置点
     *
     * @param presetId 预置位id
     * @param cameraId 相机id
     * @return Ptz信息
     */
    public String setPreset(Long presetId, Long cameraId) {
        this.isCameraControlled(cameraId);
        this.presetAction(presetId, cameraId, PresetCmd.PRESET_ADD);
        return this.getCameraPTZ(presetId, cameraId);
    }

    /**
     *获取相机预置点的PTZ值，并抓图
     *
     * @param presetId 预置位id
     * @param cameraId 相机id
     * @param presetName 预置位名称
     * @return
     */
    public Map<String, Object> getPresetPtzAndPic(Long presetId, Long cameraId, String presetName) throws InterruptedException {
        Map<String, Object> resultMap = new HashMap<>(2);
        // 确认相机可控
        this.isCameraControlled(cameraId);
        //相机移动到预置位，并更新相机的操作时间
        this.presetAction(presetId, cameraId, PresetCmd.PRESET_ACTION);
        // 更新相机操作时间
        this.pushCtrlTime(cameraId);
        // 等10秒钟，确保相机镜头调整到位
        Thread.sleep(10000);
        // 再次确认相机可控
        this.isCameraControlled(cameraId);
        // 获取预置位的PTZ数据
        String ptzStr = this.getCameraPTZ(presetId, cameraId);
        resultMap.put("cameraPtz", ptzStr);
        // 抓图
        Map<String, String> picMap = this.capturePresetPicture(presetId, cameraId, presetName, "presetCheckImg");
        if (picMap != null) {
            resultMap.putAll(picMap);
        }
        return resultMap;
    }

    /**
     * 删除预置位
     *
     * @param presetId 预置位id
     * @param cameraId 相机id
     * @return
     * @throws IOException
     */
    public boolean cancelPreset(Long presetId, Long cameraId) throws IOException {
        String capturePresetPath = this.getPresetBasePath();
        this.isCameraControlled(cameraId);
        String cmd = "rm -rf " + capturePresetPath + "/" + presetId;
        log.info("删除语句" + cmd);
        Runtime.getRuntime().exec(cmd);
        return this.presetAction(presetId, cameraId, PresetCmd.PRESET_DELETE);
    }
    /**
     * 预置位操作
     *
     * @param presetId 预置位id
     * @param cameraId 相机id
     * @param presetCmd 预置位名称
     * @return 结果
     */
    public boolean presetAction(Long presetId, Long cameraId, PresetCmd presetCmd) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
        if (cameraConInfo == null) {
            throw new BusinessException("无此摄像机或摄像机预置位不正确");
        }
        IPtzService ptzService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPtzService.class);
        PresetEntity presetEntity = PresetEntity.builder()
                .presetId(String.valueOf(cameraConInfo.getPresetNum()))
                .deviceId(cameraConInfo.getDeviceChannel())
                .channelId(cameraConInfo.getCameraChannelId())
                .presetCmd(presetCmd)
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
     */
    public String getCameraPTZ(Long presetId, Long cameraId) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
        if (cameraConInfo == null) {
            throw new BusinessException("无此摄像机或摄像机预置位不正确");
        }
        //调用海康IsApi接口
        IPtzService ptzService = VideoServiceFactory.loadSnapService(CameraVendor.HIK, IPtzService.class);
        PresetEntity presetEntity = PresetEntity.builder()
                .ip(cameraConInfo.getCameraIp())
                .port(cameraConInfo.getPort())
                .userName(cameraConInfo.getCameraManager())
                .password(cameraConInfo.getCameraCode())
                .build();
        Result result = ptzService.getCameraPtz(presetEntity);
        return String.valueOf(result.getData());
    }

    /**
     * 预置位抓图
     */
    public Map<String, String> capturePresetPicture(Long presetId, Long cameraId, String meteName, String edgeCode) {
        isCameraControlled(cameraId);

        String capturePresetPath = getPresetBasePath();

        isCameraControlled(cameraId);
        String filePathTem = "/" + presetId + "/" + presetId + ".jpg";
        if (StringUtils.isNotEmpty(edgeCode)) {
            filePathTem = String.format("/%s%s", edgeCode, filePathTem);
        }

        String filePath = capturePresetPath + filePathTem;
        log.info("预置位抓图 filePathTem: {},  filePath: {}, edgeCode: {}", filePathTem, filePath, edgeCode);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return capturePicture(null, filePath, cameraId, meteName);
    }

    /**
     * 抓图接口
     * @param parentPath 父路径，如果抓图不想全部存在一个目录下，可以传入父路径，默认在 /home/yjh_iot_center/iot-picture/resultImg/ 下创建传入的路径
     * @param absolutePath 绝对路径，与 parentPath 互斥，若未传，则抓图默认存在 resultImg 路径，若传入，则以 absolutePath 为准，路径必须在 /home/yjh_iot_center/iot-picture 下
     * @param cameraId 相机ID
     * @param meteName 测点名称，若传入，则会在图片添加测点名称水印
     * @return
     */
    public Map<String, String> capturePicture(String parentPath, String absolutePath, Long cameraId, String meteName) {
        String filePath = absolutePath;
        String urlPath;
        if (StringUtils.isEmpty(absolutePath)) {
            int ran = RandomUtil.randomInt(1000, 10000);
            String parent = StringUtils.isEmpty(parentPath) ? "" : parentPath + "/";
            String filePathTem = "/" + parent + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMAT) + ran + ".jpg";
            String captureResultPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content"));
            String capturePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content"));
            filePath = captureResultPath + filePathTem;
            urlPath = capturePath + filePathTem;
        } else {
            // 图片物理路径前缀
            String absPrePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:prefixAbsolutePath", "content"));
            // 图片网络路径前缀
            String urlPrePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:prefixRelativePath", "content"));
            urlPath = StringUtils.replace(absolutePath, absPrePath, urlPrePath);
        }
        log.info("filePath: {}, urlPath: {}", filePath, urlPath);
        FileUtil.mkParentDirs(filePath);

        CameraConInfo cameraConInfo = new CameraConInfo();
        String cameraStr = String.valueOf(cameraId);
        if (cameraStr.contains("9901") || cameraStr.contains("9902")) {

            long robotId = NumberUtils.toLong(StringUtils.substring(cameraStr, 0, (cameraStr.length() - 4)));
            RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(robotId);
            cameraConInfo.setCameraId(cameraId);
            cameraConInfo.setRecordId(robotConInfo.getRecordId());
            cameraConInfo.setDeviceChannel(robotConInfo.getDeviceChannel());
            if (cameraStr.contains("9901")) {
                cameraConInfo.setChannelNum(NumberUtils.toInt(robotConInfo.getNumLight()));
                cameraConInfo.setDeviceChannel(robotConInfo.getLightChannelId());
            } else {
                cameraConInfo.setChannelNum(NumberUtils.toInt(robotConInfo.getNumInferad()));
                cameraConInfo.setDeviceChannel(robotConInfo.getInfraredChannelId());
            }
            //机器人
            cameraConInfo.setCameraType(205);
        } else {
            cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        }
        boolean hasNvr = cameraConInfo.getRecordId() != null && cameraConInfo.getRecordId() > 0;
        String vendor = hasNvr ? cameraConInfo.getRecordVendor() : cameraConInfo.getVendorId();
        if (!hasNvr) {
            cameraConInfo.setDeviceChannel(cameraConInfo.getCameraChannelId());
        }
        String ip = hasNvr ? cameraConInfo.getRecordIp() : cameraConInfo.getCameraIp();
        int port = hasNvr ? cameraConInfo.getRecordPort() : cameraConInfo.getPort();

        SnapEntity entity =
            SnapEntity.builder().ip(ip).port(port).channelNum(cameraConInfo.getChannelNum()).deviceId(cameraConInfo.getDeviceChannel())
                .channelId(cameraConInfo.getCameraChannelId()).imgPath(filePath).build();

        ISnapService iPlayService = VideoServiceFactory.loadSnapService(cameraVendor(vendor), ISnapService.class);
        Result<String> result = iPlayService.snap(entity);

        if (result.isSuccess() && StringUtils.isNotEmpty(meteName)) {
            pictureWaterMark(filePath, DateTimeUtil.format(new Date()) + "--" + meteName);
        }
        if (!result.isSuccess()) {
            throw new BusinessException(result.getCode(), result.getMsg());
        }

        // 更新相机最后操作时间
        pushCtrlTime(cameraId);

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("urlPath", urlPath);
        resultMap.put("absPath", filePath);
        resultMap.put("resultNum", "已拍照");

        return resultMap;
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
        try (FileImageOutputStream fos = new FileImageOutputStream(file)) {
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


    /**
     * 语音广播 目前只支持GB28181-2016版本
     * @param deviceId  机器人id(获取机器人的可见光视频通道编码) 或者 相机id
     */
    public String startVoiceTrans(Long deviceId) {
        String channelId;
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(deviceId, null);
        if (cameraConInfo == null) {
            RobotConInfo robotConInfo = cameraConDao.selectRobotConInfo(deviceId);
            if (robotConInfo == null) {
                throw new BusinessException("无此设备信息");
            } else {
                channelId = robotConInfo.getLightChannelId();
            }
        } else {
            channelId = cameraConInfo.getCameraChannelId();
        }
        if (StringUtils.isBlank(channelId)) {
            throw new BusinessException("此设备未配置通道ID");
        }
        IPlayService playService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
        PlayEntity playEntity = PlayEntity.builder().channelId(channelId).build();
        Result result = playService.broadcastPlay(playEntity);
        if (!result.isSuccess()) {
            throw new BusinessException(result.getMsg());
        }
        return channelId;
    }

    public String getPresetBasePath() {
        return (String) redisTemplate.opsForHash().get("t_sys_param:presetImgPath", "content");
    }

    public String getPresetUrlPath() {
        return (String) redisTemplate.opsForHash().get("t_sys_param:presetRealImgPath", "content");
    }

    /**
     * 组装webRtc播放路径，SRS和ZLMediaKit播放路径不同
     */
    private void webRtcUrl(Long id, Map<String, Object> returnMap) {
        webRtcUrl(String.valueOf(id), returnMap, false);
    }

    private void webRtcUrl(String url, Map<String, Object> returnMap, Integer isWebRtcUrl) {
        Map<String, String> hostIpMap = redisTemplate.opsForHash().entries("t_sys_param:hostIp");
        String videoHttps = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "videoHttpsEnable"));
        String hostIp = hostIpMap.get("content");
        String subUrl = url.substring(url.indexOf("/", url.lastIndexOf(":")));
        String schema = "1".equals(videoHttps) ? "https://" : "http://";
        String newUrl = schema + hostIp + "/ZLM" + subUrl;
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

    private CameraVendor cameraVendor(int vendorId) {
        return cameraVendor(String.valueOf(vendorId));
    }
    private CameraVendor cameraVendor(String vendorId) {
        CameraVendor cameraVendor;
        switch (vendorId) {
            case "207":
                // 海康
                cameraVendor = CameraVendor.HIK;
                break;
            case "208":
                // 大华
                cameraVendor = CameraVendor.DH;
                break;
            case "209":
                // 雄迈
                cameraVendor = CameraVendor.XM;
                break;
            case "210":
                // 高德红外
                cameraVendor = CameraVendor.GD;
                break;
            default:
                // 其他
                cameraVendor = CameraVendor.DEF;
                break;
        }
        return cameraVendor;
    }
}
