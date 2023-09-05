package com.yjh.platform.module.video.service;

import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.module.video.dao.CameraConDao;
import com.yjh.platform.module.video.entity.RobotConInfo;
import com.yjh.video.api.CameraVendor;
import com.yjh.video.api.entity.PlayEntity;
import com.yjh.video.api.service.IPlayService;
import com.yjh.video.api.service.IRecordService;
import com.yjh.video.api.service.VideoServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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

    @Value("${spring.redis.host}")
    private String hostIp;

    @Autowired
    private RestTemplate restTemplate;


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
            String transUrlinferad = String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:videoServerConfig", "droneVideo"));
            transUrlinferad = String.format(transUrlinferad, robotConInfo.getRobotIp(), robotConInfo.getNestCode() + nestInner, robotConInfo.getNestCode() + nestInner);
            log.info("transUrlinferad: {}" , transUrlinferad);
            IPlayService iPlayService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IPlayService.class);
            PlayEntity playEntity = PlayEntity.builder()
                    .command(transUrlinferad)
                    .deviceId(robotConInfo.getNestCode() + nestInner)
                    .build();
            iPlayService.videoPlayByFFM(playEntity);
            String webRtc = "webrtc://" + hostIp + "/live/" + robotConInfo.getNestCode() + nestInner;
            returnInferadMap.put("cameraType", nestInner);
            returnInferadMap.put("webRtcUrl", webRtc);
        } catch (Exception e) {
            log.error("获取无人机视频流失败 getWebRtcMap err: ", e);
        }

        return returnInferadMap;
    }
}
