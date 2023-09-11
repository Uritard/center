package com.yjh.platform.module.video.service;

import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.module.video.dao.CameraConDao;
import com.yjh.platform.module.video.entity.RobotConInfo;
import com.yjh.video.api.CameraVendor;
import com.yjh.video.api.entity.PlayEntity;
import com.yjh.video.api.service.IPlayService;
import com.yjh.video.api.service.VideoServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author jinyujiang
 * @Description 普宙无人机视频接入
 * @Date create in 2023/5/15 10:12
 */
@Service
public class DroneCameraConService {

    private final Logger log = LoggerFactory.getLogger(DroneCameraConService.class);

    @Autowired
    private CameraConDao cameraConDao;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;


    /**
     * 获取无人机的三路视频流（无人机、机巢内、机巢外）
     *
     * @param robotId robotId
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> droneStartRealPlayNew(Long robotId) {
        // 登录，获取token
        RobotConInfo robotConInfo = cameraConDao.selectDroneConInfo(robotId);
        log.info("cameraConDao.selectDroneConInfo(robotId), 查询结果：{}", JSONUtil.toJSONString(robotConInfo));

        List<Map<String, Object>> returnMapList = new ArrayList<>();
        String nestInner = "0";
        String nestOuter = "1";
        String droneCamera = "3";

        Map<String, Object> nestInnerMap = getWebRtcMap(robotConInfo, nestInner);
        returnMapList.add(nestInnerMap);

        Map<String, Object> nestOuterMap = getWebRtcMap(robotConInfo, nestOuter);
        returnMapList.add(nestOuterMap);

        Map<String, Object> droneCameraMap = getWebRtcMap(robotConInfo, droneCamera);
        returnMapList.add(droneCameraMap);

        return returnMapList;
    }

    private Map<String, Object> getWebRtcMap(RobotConInfo robotConInfo, String nestInner) {
        Map<String, Object> returnInferadMap = new HashMap<>();
        try {
            String streamId = robotConInfo.getNestCode() + nestInner;
            String transUrlinferad = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "droneVideo"));
            transUrlinferad = String.format(transUrlinferad, robotConInfo.getRobotIp(), robotConInfo.getRobotPort(), streamId,streamId);
            transUrlinferad = sign(transUrlinferad, streamId);
            log.info("transUrlinferad: {}" , transUrlinferad);
            IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
            PlayEntity playEntity = PlayEntity.builder()
                    .command(transUrlinferad)
                    .deviceId(robotConInfo.getNestCode() + nestInner)
                    .build();
            iPlayService.videoPlayByFFM(playEntity);

            webRtcUrl(streamId, returnInferadMap);

            returnInferadMap.put("cameraType", nestInner);
        } catch (Exception e) {
            log.error("获取无人机视频流失败 getWebRtcMap err: ", e);
        }

        return returnInferadMap;
    }

    private void webRtcUrl(String id, Map<String, Object> returnMap) {
        String mediaServer = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "mediaServer"));
        String videoHttps = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "videoHttpsEnable"));
        String hostIp = (String)redisTemplate.opsForHash().get("t_sys_param:hostIp", "content");
        String webRtcUrl;
        if (CameraConService.MEDIA_ZLK.equalsIgnoreCase(mediaServer)) {
            // http://127.0.0.1/index/api/webrtc?app=live&stream=test&type=play
            String scheam = "1".equals(videoHttps) ? "https://" : "http://";
            webRtcUrl = scheam + hostIp + "/ZLM/index/api/webrtc?app=live&stream=" + id + "&type=play";
        } else {
            // webrtc://172.24.39.10/live/40001
            webRtcUrl = "webrtc://" + hostIp + "/live/" + id;
        }
        returnMap.put("webRtcUrl", webRtcUrl);
    }

    public String sign(String transUrl, String cameraId) {
        String mediaServer = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "mediaServer"));
        String mediaSignKey = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "mediaSignKey"));
        if (!CameraConService.MEDIA_ZLK.equalsIgnoreCase(mediaServer)) {
            return transUrl;
        }
        String signKey = StringUtils.isEmpty(cameraId) ? mediaSignKey : cameraId + "_" + mediaSignKey;
        String checkSign = DigestUtils.md5DigestAsHex(signKey.getBytes());
        String checkUrl = StringUtils.substringAfterLast(transUrl, "/");

        return StringUtils.contains(checkUrl, "?") ? (transUrl + "\\&sign=" + checkSign) : (transUrl + "?sign=" + checkSign);
    }
}
