package com.yjh.platform.module.video.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.io.Files;
import com.yjh.commons.ValueUtil;
import com.alibaba.fastjson2.JSONObject;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.module.video.dao.CameraConDao;
import com.yjh.platform.module.video.entity.*;
import com.yjh.video.api.CameraVendor;
import com.yjh.video.api.entity.*;
import com.yjh.video.api.entity.response.RecordInfo;
import com.yjh.video.api.entity.response.RecordItem;
import com.yjh.video.api.entity.response.RecordSpace;
import com.yjh.video.api.entity.response.RecordResultInfo;
import com.yjh.video.api.result.Result;
import com.yjh.video.api.service.*;
import com.yjh.video.api.util.PathVariableUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    public static Map<String, String> maps = new ConcurrentHashMap<>();

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
            PlayEntity playEntity =
                PlayEntity.builder().deviceId(cameraConInfo.getDeviceChannel()).channelId(cameraConInfo.getCameraChannelId()).build();
            IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
            Result result = iPlayService.videoPlayByWvp(playEntity);
            Map data = (Map)result.getData();
            webRtcUrl((String)data.get("rtc"), returnMap, 1);
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
            PlayEntity playEntity =
                PlayEntity.builder().deviceId(cameraConInfo.getDeviceChannel()).channelId(cameraConInfo.getCameraChannelId()).build();
            Result result = new Result();
            IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
            try {
                result = iPlayService.videoPlayByWvp(playEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map data = (Map)result.getData();
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
            webRtcUrl((String)data.get("rtc"), returnMap, 1);
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
                        Map data = (Map)result.getData();
                        Map<String, Object> lightMap = new HashMap<>();
                        lightMap.put("light", lightCameraId);
                        lightMap.put("rtmpUrl", data.get("rtmp"));
                        webRtcUrl((String)data.get("rtc"), lightMap, 1);
                        returnMapList.add(lightMap);
                    }
                    if (StringUtils.isNotEmpty(robotConInfo.getInfraredChannelId())) {
                        playEntity = PlayEntity.builder().deviceId(recorderConInfo.getDeviceChannel())
                            .channelId(String.valueOf(robotConInfo.getInfraredChannelId())).build();
                        Result result = iPlayService.videoPlayByWvp(playEntity);
                        Map data = (Map)result.getData();
                        Map<String, Object> infraredMap = new HashMap<>();
                        infraredMap.put("inferad", infraredCameraId);
                        infraredMap.put("rtmpUrlInferad", data.get("rtmp"));
                        webRtcUrl((String)data.get("rtc"), infraredMap, 1);
                        returnMapList.add(infraredMap);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } else {
            // 拉流
            try {
                // 可见光相机拉流
                Map<String, Object> returnLightMap = getRobotStream(robotConInfo, lightCameraId);
                // 红外相机拉流
                String inferadIp = robotConInfo.getLnferadIp();
                Integer inferadPort = robotConInfo.getInferadPort();
                String inferadUsername = robotConInfo.getInferadUsername();
                String inferadPassword = robotConInfo.getInferadPassword();
                String robotInfraredVideo = String.valueOf(
                    redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "ffmpeg-" + robotConInfo.getInfraredVendor()));
                Map<String, String> hostIpMap = redisTemplate.opsForHash().entries("t_sys_param:zmlHostIp");
                String hostIp = hostIpMap.get("content");
                Map params = new HashMap();
                params.put("username", inferadUsername);
                params.put("password", inferadPassword);
                params.put("ip", inferadIp);
                params.put("port", inferadPort);
                params.put("channelId", 1);
                params.put("hostIp", hostIp);
                params.put("name", infraredCameraId);
                String transUrlInfrared = PathVariableUtil.variableParse(robotInfraredVideo, params);
                transUrlInfrared = sign(transUrlInfrared, infraredCameraId);
                PlayEntity playEntity = PlayEntity.builder().deviceId(infraredCameraId).command(transUrlInfrared).build();
                iPlayService.videoPlayByFFM(playEntity);
                log.info("robotInferadInfo: {}, {}, {}, robotTransUrlinferad:{}", inferadIp, inferadPort, infraredCameraId,
                    transUrlInfrared);
                String[] rtmpUrlsInferad = transUrlInfrared.split("rtmp");
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
        IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
        try {
            if (StringUtils.isNotEmpty(robotConInfo.getLightChannelId()) && Objects.nonNull(robotConInfo.getRecordId())
                && robotConInfo.getRecordId() > 0L) {
                RecorderConInfo recorderConInfo = cameraConDao.selectByRecordId(robotConInfo.getRecordId());
                PlayEntity playEntity =
                    new PlayEntity.Builder().deviceId(recorderConInfo.getDeviceChannel()).channelId(robotConInfo.getLightChannelId())
                        .build();
                Result result = iPlayService.videoPlayByWvp(playEntity);
                Map data = (Map)result.getData();
                webRtcUrl((String)data.get("rtc"), returnLightMap, 1);
            } else {
                String lightIp = robotConInfo.getLightIp();
                String lightPort = robotConInfo.getLightPort();
                String lightUsername = robotConInfo.getIdentityManager();
                String lightPassword = robotConInfo.getIdentityCode();
                String robotLightVideo = String.valueOf(
                    redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "ffmpeg-" + robotConInfo.getLightVendor()));
                Map<String, String> hostIpMap = redisTemplate.opsForHash().entries("t_sys_param:zmlHostIp");
                String hostIp = hostIpMap.get("content");
                Map params = new HashMap();
                params.put("username", lightUsername);
                params.put("password", lightPassword);
                params.put("ip", lightIp);
                params.put("port", lightPort);
                params.put("channelId", 1);
                params.put("hostIp", hostIp);
                params.put("name", robotCameraId);
                String transUrlLight = PathVariableUtil.variableParse(robotLightVideo, params);
                transUrlLight = sign(transUrlLight, robotCameraId);
                PlayEntity playEntity = new PlayEntity.Builder().command(transUrlLight).deviceId(robotCameraId).build();
                iPlayService.videoPlayByFFM(playEntity);
                log.info("robotLightInfo: {}, {}, {}, {}, {}, robotTransUrlLight:{}", lightUsername, lightPassword, lightIp, lightPort,
                    robotCameraId, transUrlLight);
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
                Map<String, String> hostIpMap = redisTemplate.opsForHash().entries("t_sys_param:zmlHostIp");
                String hostIp = hostIpMap.get("content");
                Map params = new HashMap();
                params.put("username", userName);
                params.put("password", password);
                params.put("ip", cameraIp);
                params.put("port", cameraPort);
                params.put("channelId", iChanNum);
                params.put("starttime", starttime);
                params.put("endtime", endtime);
                params.put("hostIp", hostIp);
                params.put("name", id);
                String transUrl = PathVariableUtil.variableParse(nvrRtmpBack, params);
                transUrl = sign(transUrl, id);
                log.info("userName:{}, password:{}, cameraIp:{}, cameraPort:{}, iChanNum:{}, starttime:{}, endtime:{}, "
                        + "historyPath:{}, historyTransUrl: {}", userName, password, cameraIp, cameraPort, iChanNum, starttime, endtime,
                    cameraId, transUrl);
                PlayEntity playEntity = PlayEntity.builder().deviceId(id.toString()).command(transUrl).build();
                IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
                iPlayService.videoPlayByFFM(playEntity);
                webRtcUrl(id, returnMap, true);
            } else {
                String starttime = startTime.replace(" ", "%20");
                String endtime = stopTime.replace(" ", "%20");
                PlayBackEntity playBackEntity =
                    PlayBackEntity.builder().deviceId(cameraConInfo.getDeviceChannel()).channelId(cameraConInfo.getCameraChannelId())
                        .startTime(starttime).endTime(endtime).build();
                IPlaybackService iPlaybackService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlaybackService.class);
                Result result = iPlaybackService.playBackStart(playBackEntity);
                Map resultMap = (Map)result.getData();
                webRtcUrl((String)resultMap.get("rtc"), returnMap, 1);
            }
            returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return returnMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> startVideoBack(String token, Long cameraId, String startTime, String stopTime) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            String starttime = startTime.replace(" ", "%20");
            String endtime = stopTime.replace(" ", "%20");
            PlayBackEntity playBackEntity =
                PlayBackEntity.builder().deviceId(cameraConInfo.getDeviceChannel()).channelId(cameraConInfo.getCameraChannelId())
                    .startTime(starttime).endTime(endtime).build();
            IPlaybackService iPlaybackService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlaybackService.class);
            Result result = iPlaybackService.playBackStart(playBackEntity);
            Map resultMap = (Map)result.getData();
            webRtcUrl((String)resultMap.get("rtc"), returnMap, 1);
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
        PtzControlEntity ptzControlEntity =
            PtzControlEntity.builder().deviceId(cameraConInfo.getDeviceChannel()).channelId(cameraConInfo.getCameraChannelId())
                .command(command).horizonSpeed(horizonSpeed).verticalSpeed(verticalSpeed).zoomSpeed(zoomSpeed).build();
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
     * 获取相机预置点的PTZ值，并抓图
     *
     * @param presetId   预置位id
     * @param cameraId   相机id
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
     * @param presetId  预置位id
     * @param cameraId  相机id
     * @param presetCmd 预置位名称
     * @return 结果
     */
    public boolean presetAction(Long presetId, Long cameraId, PresetCmd presetCmd) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, presetId);
        if (cameraConInfo == null) {
            throw new BusinessException("无此摄像机或摄像机预置位不正确");
        }
        IPtzService ptzService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPtzService.class);
        PresetEntity presetEntity =
            PresetEntity.builder().presetId(String.valueOf(cameraConInfo.getPresetNum())).deviceId(cameraConInfo.getDeviceChannel())
                .channelId(cameraConInfo.getCameraChannelId()).presetCmd(presetCmd).build();
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
     *
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
        PresetEntity presetEntity =
            PresetEntity.builder().ip(cameraConInfo.getCameraIp()).port(cameraConInfo.getPort()).userName(cameraConInfo.getCameraManager())
                .password(cameraConInfo.getCameraCode()).build();
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

    public Map<String, String> capturePictureWithControlled(String parentPath, String absolutePath, Long cameraId, String meteName) {
        isCameraControlled(cameraId);

        return capturePicture(parentPath, absolutePath, cameraId, meteName);
    }

    /**
     * 抓图接口
     *
     * @param parentPath   父路径，如果抓图不想全部存在一个目录下，可以传入父路径，默认在 /home/yjh_iot_center/iot-picture/resultImg/ 下创建传入的路径
     * @param absolutePath 绝对路径，与 parentPath 互斥，若未传，则抓图默认存在 resultImg 路径，若传入，则以 absolutePath 为准，路径必须在 /home/yjh_iot_center/iot-picture 下
     * @param cameraId     相机ID
     * @param meteName     测点名称，若传入，则会在图片添加测点名称水印
     * @return
     */
    public Map<String, String> capturePicture(String parentPath, String absolutePath, Long cameraId, String meteName) {
        String filePath = absolutePath;
        String urlPath;
        if (StringUtils.isEmpty(absolutePath)) {
            int ran = RandomUtil.randomInt(1000, 10000);
            String parent = StringUtils.isEmpty(parentPath) ? "" : parentPath + "/";
            String filePathTem = parent + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMAT) + ran + ".jpg";
            String captureResultPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content"));
            String capturePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content"));
            filePathTem = StringUtils.stripStart(filePathTem, "/\\");
            filePath = FilenameUtils.concat(captureResultPath, filePathTem);
            urlPath = FilenameUtils.concat(capturePath, filePathTem);
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
        String userName = hasNvr ? cameraConInfo.getIdentityManager() : cameraConInfo.getCameraManager();
        String pwd = hasNvr ? cameraConInfo.getIdentityCode() : cameraConInfo.getCameraCode();

        SnapEntity entity =
                SnapEntity.builder().ip(ip).port(port).channelNum(cameraConInfo.getChannelNum()).deviceId(cameraConInfo.getDeviceChannel())
                        .channelId(cameraConInfo.getCameraChannelId()).userName(userName).password(pwd).imgPath(filePath).build();

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
     *
     * @param deviceId 机器人id(获取机器人的可见光视频通道编码) 或者 相机id
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
        return (String)redisTemplate.opsForHash().get("t_sys_param:presetImgPath", "content");
    }

    public String getPresetUrlPath() {
        return (String)redisTemplate.opsForHash().get("t_sys_param:presetRealImgPath", "content");
    }

    @Scheduled(cron = "0 0 */6 * * ?")
    public void refreshRecordsOnSchedule() {
        List<RecorderConInfo> recordersInfo = cameraConDao.SelectRecords();
        recordersInfo.stream().map(RecorderConInfo::getRecordId).forEach(recordId -> {
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
        RecorderConInfo conInfo = cameraConDao.selectByRecordId(recordId);
        Map<String, Object> channleStatusMap = new HashMap<>();

        RecordFileEntity entity =
            RecordFileEntity.builder().ip(conInfo.getRecordIp()).port(conInfo.getHttpPort()).deviceId(conInfo.getDeviceChannel())
                .userName(conInfo.getIdentityManager()).password(conInfo.getIdentityCode()).build();

        IRecordService recordService = VideoServiceFactory.loadSnapService(cameraVendor(conInfo.getVendorId()), IRecordService.class);
        try {
            Result<List<RecordInfo>> listResult = recordService.recordAllFiles(entity);

            if (listResult.isSuccess()) {
                Result<RecordSpace> space = recordService.recordSpace();
                RecordSpace rs = space.getData();
                channleStatusMap.put("recorderId", conInfo.getRecordId());
                channleStatusMap.put("capacityTotal", rs.getCapacity());
                channleStatusMap.put("freeTotal", rs.getFreeSpace());
                // 查询通道信息
                List<Map<String, Object>> chanInfo = recordChannelInfo(listResult.getData());
                channleStatusMap.put("channel", chanInfo);
                channleStatusMap.put("status", "在线");
                return channleStatusMap;
            }
        } catch (Exception e) {
            log.error("获取NVR信息失败：{}", entity, e);
        }
        channleStatusMap.put("errorMessage", "录像机不在线");
        channleStatusMap.put("status", "离线");
        return channleStatusMap;
    }

    /**
     * 处理通道下文件信息
     */
    public List<Map<String, Object>> recordChannelInfo(List<RecordInfo> listRecord) {
        List<Map<String, Object>> channelInfoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(listRecord)) {
            return channelInfoList;
        }
        int i = 1;
        for (RecordInfo info : listRecord) {
            Map<String, Object> chanInfoMap = new LinkedHashMap<>();
            chanInfoMap.put("ipChanNum", i++);
            chanInfoMap.put("enable", info.getStatus());
            chanInfoMap.put("channel", info.getChannelId());
            chanInfoMap.put("chanName", info.getName());
            channelInfoList.add(chanInfoMap);

            List<RecordItem> itemList = info.getRecordList();
            if (CollectionUtils.isEmpty(itemList)) {
                continue;
            }

            int[] intact = new int[3];
            Date[] recordSpan = new Date[2];
            // 单个通道完整性校验
            integralityCheck(chanInfoMap, itemList, intact, recordSpan);

            String[] recordSpanStr = new String[]{DateTimeUtil.format(recordSpan[0]), DateTimeUtil.format(recordSpan[1])};
            chanInfoMap.put("recordSpan", recordSpanStr);

            if (intact[0] > 0) {
                intact[2] = (intact[0] * 10000) / (intact[0] + intact[1]);
            }
            chanInfoMap.put("intact", intact);
        }

        return channelInfoList;
    }

    /**
     * 完整性计算
     */
    private void integralityCheck(Map<String, Object> chanInfoMap, List<RecordItem> itemList, int[] intact, Date[] recordSpan) {
        long timeRecord = 0L;
        long timeDefect = 0L;
        Date preEndTime = null;
        for (RecordItem item : itemList) {
            Date startTime = DateTimeUtil.parse(item.getStartTime());
            Date endTime = DateTimeUtil.parse(item.getEndTime());

            if(recordSpan[0] == null || recordSpan[0].after(startTime)) {
                recordSpan[0] = startTime;
            }
            if(recordSpan[1] == null || recordSpan[1].before(endTime)) {
                recordSpan[1] = endTime;
            }
            long speed = endTime.getTime() - startTime.getTime();
            // 计算录像时长
            timeRecord += speed;

            // 录像完整性时间校验
            long def = (preEndTime != null && startTime.getTime() > preEndTime.getTime()) ? (startTime.getTime() - preEndTime.getTime()) : 0L;

            // 录像完整性视频段校验，若两个文件之间缺失时间小于2s，则表示没有间断
            if (def < 2000) {
                // 完整
                intact[0] = intact[0] + 1;
            } else {
                // 不完整
                intact[1] = intact[1] + 1;
            }

            timeDefect += def;
            preEndTime = endTime;
        }

        String sTemp = timeStr(timeRecord);
        chanInfoMap.put("recordTime", sTemp);
        int intactTime = 10000;
        if (timeRecord > 0L && timeDefect > 0L) {
            intactTime = (int)((timeRecord * 10000) / (timeDefect + timeRecord));
        }
        chanInfoMap.put("intactTime", intactTime);
        chanInfoMap.put("defectTime", timeStr(timeDefect));
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
        if (vendorId == null) {
            return CameraVendor.DEF;
        }
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

    /**
     * dlt664红外抓图
     *
     * @param cameraId
     * @param presetId
     * @return
     */
    public HashMap<String, String> givePicFir(long cameraId, Long presetId, String meteName) {
        HashMap<String, String> map = new HashMap<>();
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

            //转到预置点
            IPtzService iPtzService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPtzService.class);
            PresetEntity presetEntity = PresetEntity.builder()
                    .deviceId(cameraConInfo.getDeviceChannel())
                    .channelId(cameraConInfo.getCameraChannelId())
                    .presetId(presetId.toString()).build();
            Result presetCommand = iPtzService.presetCommand(presetEntity);
            if (presetCommand.getCode() == 200) {
                log.info("转到预置点成功：Preset->" + presetCommand.getData());
            } else {
                log.info("转到预置点失败->" + presetCommand.getData());
            }

            try {
                Long waitTime =
                        Long.valueOf(redisTemplate.opsForHash().get("t_sys_param:waitTime", "content").toString());
                log.info("waitTime:----------" + waitTime);
                Thread.sleep(waitTime);
            } catch (Exception e) {
                log.error("error----" + e);
            }

            IRecordService iRecordService = VideoServiceFactory.loadSnapService(cameraVendor(cameraConInfo.getVendorId()), IRecordService.class);
            String hotPic = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content"));
            String hotPicShow = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content"));
            String hotFir = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:infraredStorePath", "content"));
            String hotFirShow = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:infraredRealPath", "content"));
            Boolean flag = Boolean.valueOf(redisTemplate.opsForHash().get("t_sys_param:isWatermarkToInfrared", "content").toString());
            Boolean infraredAnalysis = Boolean.valueOf(redisTemplate.opsForHash().get("t_sys_param:isInfraredAnalysis", "content").toString());
            PicPlayEntity build = PicPlayEntity.builder()
                    .ip(cameraIp)
                    .port(cameraConInfo.getPort())
                    .password(password)
                    .userName(userName)
                    .hotPic(hotPic)
                    .hotPicShow(hotPicShow)
                    .hotFirShow(hotFirShow)
                    .hotFir(hotFir)
                    .meteName(meteName)
                    .flag(flag)
                    .infraredAnalysis(infraredAnalysis).build();
            Result<HashMap<String, String>> hashMapResult = iRecordService.givePicFir(build);
            if (hashMapResult.getCode() == 200) {
                return hashMapResult.getData();
            } else {
                map.put("error", "获取红外文件_dlt664失败");
                return map;
            }
        } catch (Exception e) {
            log.error("红外图片抓取异常" + e);
        }
        return map;
    }

    /**
     * @param nStartX  参数范围 0~255
     * @param nStartY  参数范围 0~255
     * @param nEndX    参数范围 0~255
     * @param nEndY    参数范围 0~255
     * @param cameraId 相机编码
     * @return
     */
    public Map<String, String> regionFocus(int nStartX, int nStartY, int nEndX, int nEndY, long cameraId) {
        Map<String, String> map = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
            if (205 == cameraConInfo.getCameraType()) {
                String userName = cameraConInfo.getCameraManager();
                String password = cameraConInfo.getCameraCode();
                String cameraIp = cameraConInfo.getCameraIp();

                IRecordService iRecordService = VideoServiceFactory.loadSnapService(cameraVendor(cameraConInfo.getVendorId()), IRecordService.class);
                PositionPlayEntity playEntity = PositionPlayEntity.builder()
                        .ip(cameraIp)
                        .port(cameraConInfo.getPort())
                        .userName(userName)
                        .password(password)
                        .nEndX(nEndX)
                        .nEndY(nEndY)
                        .nStartX(nStartX)
                        .nStartY(nStartY)
                        .build();
                Result result = iRecordService.regionFocus(playEntity);
                if (result.getCode() != 200) {
                    log.info("请求结果" + result);
                }
                if (result.getData() != null) {
                    Map<String, String> resultMap = (Map<String, String>) result.getData();
                    if ("OK".equals(resultMap.get("statusString"))) {
                        map.put("result", "ok");
                    } else {
                        map.put("result", resultMap.get("subStatusCode"));
                    }
                } else {
                    map.put("result", "error");
                }
            } else {
                map.put("result", "camera type is not allowed!");
            }
        } catch (Exception e) {
            log.error("区域对焦失败", e);
        }
        return map;
    }

    /**
     * 导出相机设备参数
     *
     * @param cameraId cameraId
     * @return result
     */
    public boolean exportCameraConfig(Long cameraId) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        log.info("exportCameraConfig 参数，cameraConInfo：{}", JSONUtil.toJSONString(cameraConInfo));
        String userName = cameraConInfo.getCameraManager();
        String password = cameraConInfo.getCameraCode();
        String cameraIp = cameraConInfo.getCameraIp();

        try {
            IRecordService iRecordService = VideoServiceFactory.loadSnapService(cameraVendor(cameraConInfo.getVendorId()), IRecordService.class);
            PlayEntity playEntity = PlayEntity.builder()
                    .ip(cameraIp)
                    .port(cameraConInfo.getPort())
                    .userName(userName)
                    .password(password).build();
            Result<byte[]> result = iRecordService.exportCameraConfig(playEntity);
            String filePath = getConfigFileDir();
            String fileName = getConfigFileName(cameraId.toString());
            log.info("相机 {} 配置文件路径：{}{}", cameraId, filePath, fileName);
            bytesToFile(result.getData(), filePath, fileName);
        } catch (Exception e) {
            log.info("exportCameraConfig err: {}", e);
            return false;
        }

        return true;
    }

    /**
     * 获取相机设备参数的文件名存储路径
     *
     * @return result
     */
    private String getConfigFileDir() {
        return String.valueOf(redisTemplate.opsForHash().get("t_sys_param:cameraConfigPath", "content"));
    }

    /**
     * 生成相机设备参数的文件名
     *
     * @param cameraId cameraId
     * @return result
     */
    private String getConfigFileName(String cameraId) {
        return String.format("configurationData_%s.data", cameraId);
    }


    /**
     * 将Byte数组转换成文件
     *
     * @param bytes    byte数组
     * @param filePath 文件路径  如 D://test/ 最后“/”结尾
     * @param fileName 文件名
     */
    public static void bytesToFile(byte[] bytes, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            file = new File(filePath + fileName);
            if (!file.getParentFile().exists()) {
                //文件夹不存在 生成
                file.getParentFile().mkdirs();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
        }
    }


    /**
     * 批量配分相机设备参数
     *
     * @param cameraIds cameraIds
     * @return result
     */
    public boolean exportCameraConfigBatch(List<Long> cameraIds) {
        if (CollectionUtils.isEmpty(cameraIds)) {
            log.info("exportCameraConfigBatch 入参cameraIds为空！");
            return false;
        }

        log.info("exportCameraConfigBatch 入参cameraIds: {}", JSONUtil.toJSONString(cameraIds));
        cameraIds.forEach(cameraId -> {
            try {
                exportCameraConfig(cameraId);
            } catch (Exception e) {
                log.info("相机{}备份失败，errMsg:{}", cameraId, e.getMessage());
            }
        });

        return true;
    }


    /**
     * 恢复相机配置信息
     *
     * @param cameraId cameraId
     * @return result
     */
    public boolean importCameraConfig(Long cameraId) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        log.info("exportCameraConfig 参数，cameraConInfo：{}", JSONUtil.toJSONString(cameraConInfo));
        // 导入配置文件
        boolean processResult = uploadConfigFile(cameraConInfo);
        if (processResult) {
            log.info("相机：{} 导入配置文件成功，即将重启", cameraId);
            // 重启相机
            processResult = reboot(cameraConInfo);
            log.info("相机：{} 重启相机结果：{}", cameraId, processResult);
        } else {
            throw new BusinessException("相机配置信息导入失败！");
        }

        log.info("相机：{} 恢复相机配置信息结果：{}", cameraId, processResult);
        return processResult;
    }


    /**
     * put请求导入相机设备参数配置文件
     *
     * @param cameraConInfo cameraConInfo
     * @return result
     */
    private boolean uploadConfigFile(CameraConInfo cameraConInfo) {
        log.info("uploadConfigFile入参，cameraConInfo： {}", JSONUtil.toJSONString(cameraConInfo));

        if (cameraConInfo == null) {
            throw new BusinessException("相机不存在！");
        }
        String username = cameraConInfo.getCameraManager();
        String password = cameraConInfo.getCameraCode();
        String cameraIp = cameraConInfo.getCameraIp();
        String cameraId = cameraConInfo.getCameraId().toString();

        File backFile = new File(getConfigFileDir() + getConfigFileName(cameraId));
        if (!backFile.exists() || !backFile.isFile()) {
            throw new BusinessException("相机配置信息不存在，请先备份！");
        }
        try {
            IRecordService iRecordService = VideoServiceFactory.loadSnapService(cameraVendor(cameraConInfo.getVendorId()), IRecordService.class);
            FilePlayEntity playEntity = FilePlayEntity.builder()
                    .ip(cameraIp)
                    .port(cameraConInfo.getPort())
                    .userName(username)
                    .password(password)
                    .body(getBytesByFile(backFile)).build();
            Result<Map<String, String>> result = iRecordService.uploadConfigFile(playEntity);
            if (result.getCode() != 200) {
                log.info(result.getMsg());
                return false;
            }
        } catch (Exception e) {
            log.info("err", e);
            return false;
        }
        return true;
    }

    public static byte[] getBytesByFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
            byte[] b = new byte[8192];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            byte[] data = bos.toByteArray();
            bos.close();
            return data;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }


    /**
     * 重启相机
     *
     * @param cameraConInfo cameraConInfo
     * @return result
     */
    private boolean reboot(CameraConInfo cameraConInfo) {
        String userName = cameraConInfo.getCameraManager();
        String password = cameraConInfo.getCameraCode();
        String cameraIp = cameraConInfo.getCameraIp();

        try {
            IRecordService iRecordService = VideoServiceFactory.loadSnapService(cameraVendor(cameraConInfo.getVendorId()), IRecordService.class);
            PlayEntity playEntity = PlayEntity.builder()
                    .ip(cameraIp)
                    .port(cameraConInfo.getPort())
                    .userName(userName)
                    .password(password).build();
            Result reboot = iRecordService.reboot(playEntity);
            log.info("reboot result is, putResult: {}", reboot);
        } catch (Exception e) {
            log.info("camera reboot err: {}", e);
            return false;
        }

        return true;
    }

    //@Logs(title = "获取相机状态", code = "getCameraStatus", content = "获取相机状态信息")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> getCameraStatus(Long recordId) {
        List<CameraStatusInfo> cameraConInfoMap = cameraConDao.cameraInfoByNVR(recordId);
//        IRecordService iRecordService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IRecordService.class);
        Map<String, String> channleStatusMap = new HashMap<>();
        for (CameraStatusInfo cameraStatusInfo : cameraConInfoMap) {
            if (!maps.containsKey(cameraStatusInfo.getDeviceChannel())) {
                IRecordService iRecordService = VideoServiceFactory.loadSnapService(cameraVendor(cameraStatusInfo.getVendorId()), IRecordService.class);
                PlayEntity playEntity = PlayEntity.builder()
                        .deviceId(cameraStatusInfo.getDeviceChannel()).build();
                Result result = iRecordService.getCameraStatus(playEntity);
                JSONObject data = (JSONObject) result.getData();
                String online = (String) data.get("Online");
                if (StringUtils.isNotEmpty(online)) {
                    if (online.equals("ONLINE")) {
                        maps.put(cameraStatusInfo.getDeviceChannel(), "1");
                    } else {
                        maps.put(cameraStatusInfo.getDeviceChannel(), "0");
                    }
                } else {
                    maps.put(cameraStatusInfo.getDeviceChannel(), "-1"); // 未知
                }
            }
            channleStatusMap.put(String.valueOf(cameraStatusInfo.getCameraId()), maps.get(cameraStatusInfo.getDeviceChannel()));
        }
        log.info("channelStatusMap: " + channleStatusMap);
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


    /**
     * 获取行列区域画面最高温度
     *
     * @param cameraId 摄像头id
     */
    public String getLineTemperature(long cameraId, String points) throws Exception{
        Integer x1 = null;
        Integer y1 = null;
        Integer x2 = null;
        Integer y2 = null;
        String[] pointList = points.split(",");
        x1 = ValueUtil.toInteger(pointList[0],0);
        y1 = ValueUtil.toInteger(pointList[1],0);
        if (pointList.length == 4){
            x2 = ValueUtil.toInteger(pointList[2],1);
            y2 = ValueUtil.toInteger(pointList[3],1);
        }
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        ThermometerEntity entity = ThermometerEntity.builder()
                .ip(cameraConInfo.getCameraIp())
                .port(cameraConInfo.getPort())
                .userName(cameraConInfo.getCameraManager())
                .password(cameraConInfo.getCameraCode())
                .x1(x1)
                .y1(y1)
                .x2(x2)
                .y2(y2)
                .build();
        IInfraredService iInfraredService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IInfraredService.class);
        Result result = iInfraredService.getTemperature(entity);
        if (result.getCode() != 200) {
            throw new RuntimeException(result.getMsg());
        }
        return result.getData().toString();
    }

    /**
     * 开始录像
     *
     * @param cameraId 摄像头id
     */
    public void startRecord(long cameraId) throws Exception{
        //保存文件地址
        String videoPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:videoPath", "content"));
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        RecordCtrlEntity entity = RecordCtrlEntity.builder()
                .deviceId(cameraConInfo.getDeviceChannel())
                .channelId(cameraConInfo.getCameraChannelId())
                .customizedPath(videoPath)
                .build();
        IRecordService iRecordService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IRecordService.class);
        Result result = iRecordService.startRecord(entity);
        if (result.getCode() != 200) {
            throw new RuntimeException(result.getMsg());
        }
    }

    /**
     * 结束录像
     *
     * @param cameraId 摄像头id
     */
    public String stopRecord(long cameraId) throws Exception{
        String videoPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:videoPath", "content"));
        String videoRealPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:videoRealPath", "content"));
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId, null);
        RecordCtrlEntity entity = RecordCtrlEntity.builder()
                .deviceId(cameraConInfo.getDeviceChannel())
                .channelId(cameraConInfo.getCameraChannelId())
                .build();
        IRecordService iRecordService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IRecordService.class);
        Result<RecordResultInfo> result = iRecordService.stopRecord(entity);
        if (result.getCode() != 200) {
            throw new RuntimeException(result.getMsg());
        }
        if (!result.getData().getFileUrl().contains(videoPath)){
            RecordResultInfo recordResultInfo = result.getData();
            int index = recordResultInfo.getRecordUrl().lastIndexOf("/");
            String dirPath = videoPath+recordResultInfo.getRecordUrl().substring(0,index);
            //需要移动文件
            File dir = new File(dirPath);
            if (!dir.exists()){
                dir.mkdirs();
            }
            String newFileUrl = videoPath+result.getData().getRecordUrl();
            File sourceFile = new File(result.getData().getFileUrl());
            File destinationFile  = new File(newFileUrl);
            Files.move(sourceFile,destinationFile);

        }

        return videoRealPath+result.getData().getRecordUrl();
    }

}
