package com.yjh.accessvideo.module.control.service;

import com.yjh.accessvideo.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.commons.utils.JSONUtil;
import com.yjh.accessvideo.module.control.entity.DroneLoginBodyEntity;
import com.yjh.accessvideo.module.control.entity.DroneLoginResult;
import com.yjh.accessvideo.module.control.entity.DronePlayStreamResult;
import com.yjh.accessvideo.module.control.entity.DronePlayStreamResultData;
import com.yjh.accessvideo.module.control.entity.RobotConInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author jinyujiang
 * @Description 普宙无人机视频接入
 * @Date create in 2023/5/15 10:12
 */
public class DroneCameraConService {

    private final Logger log = LoggerFactory.getLogger(DroneCameraConService.class);

    @Autowired
    private CameraConDao cameraConDao;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Value("${spring.redis.host}")
    private String hostIp;

    @Value("${drone.station.userName}")
    private String droneUserName;

    @Value("${drone.station.password}")
    private String dronePassword;

    /**
     * 获取无人机的三路视频流（无人机、机巢内、机巢外）
     *
     * @param robotId robotId
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> droneStartRealPlay(Long robotId) {
        // 登录，获取token
        RobotConInfo robotConInfo = cameraConDao.selectDroneConInfo(robotId);
        String token = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:droneStation", "token"));
        if (StringUtils.isEmpty(token)) {
            token = initToken(robotConInfo);
        }

        // 调用无人机视频流接口，并拼接视频流地址
        String droneStr = "http://$s:$s/prod-api/nest/nest/playStream/open/%s/3";
        String nestInnerStr = "http://$s:$s/prod-api/nest/nest/playStream/open/%s/0";
        String nestOutsideStr = "http://$s:$s/prod-api/nest/nest/playStream/open/%s/1";
        Map<String, Object> droneMap = getPlayStreamMap(droneStr, robotConInfo, token, "3");
        if (droneMap == null || droneMap.size() == 0) {
            // token失效后需要再次获取token
            token = initToken(robotConInfo);
            droneMap = getPlayStreamMap(droneStr, robotConInfo, token, "3");
        }

        Map<String, Object> nestInnerMap = getPlayStreamMap(nestInnerStr, robotConInfo, token, "0");
        Map<String, Object> nestOutsideMap = getPlayStreamMap(nestOutsideStr, robotConInfo, token, "1");
        return Arrays.asList(droneMap, nestInnerMap, nestOutsideMap);
    }

    /**
     * 获取无人机视频流参数
     *
     * @param urlStr urlStr
     * @param robotConInfo robotConInfo
     * @param token token
     * @param cameraType cameraType
     * @return result
     */
    private Map<String, Object> getPlayStreamMap(String urlStr, RobotConInfo robotConInfo, String token, String cameraType) {
        try {
            String url = String.format(urlStr, robotConInfo.getRobotIp(), robotConInfo.getRobotPort(), robotConInfo.getNestCode());
            DronePlayStreamResultData data = getPlayStream(url, token);
            return getPlayStreamUrl(data, cameraType);
        } catch (Exception e) {
            log.error("getPlayStreamMap err", e);
            return null;
        }
    }

    /**
     * 获取token
     *
     * @param robotConInfo robotConInfo
     * @return result
     */
    private String initToken(RobotConInfo robotConInfo) {
        String loginStr = "http://$s:%s/prod-api/auth/login";
        String loginUrl = String.format(loginStr, robotConInfo.getRobotIp(), robotConInfo.getRobotPort());
        String token = getDroneToken(loginUrl);

        redisTemplate.opsForHash().put("t_sys_param:droneStation", "token", token);

        return token;
    }

    /**
     * 登录无人机站端并获取token
     *
     * @param url url
     * @return result
     */
    private String getDroneToken(String url) {
        //设置请求头参数
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type","application/json");
        httpHeaders.add("TENANTID","10000");
        httpHeaders.add("CLIENTID","nest");

        //设置body参数，并转成json字符串
        DroneLoginBodyEntity bodyEntity = new DroneLoginBodyEntity();
        bodyEntity.setUserName(droneUserName);
        bodyEntity.setPassWord(dronePassword);
        bodyEntity.setUuid(getUUID());
        bodyEntity.setCode("666666");

        String content = JSONUtil.toJSONString(bodyEntity);
        HttpEntity<String> httpEntity = new HttpEntity<>(content, httpHeaders);
        ResponseEntity<DroneLoginResult> responseEntity = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForEntity(url, httpEntity, DroneLoginResult.class);
        if (responseEntity != null && responseEntity.getBody() != null && responseEntity.getBody().getData() != null) {
            return responseEntity.getBody().getData().getAccess_token();
        }

        return "";
    }

    /**
     * 获取视频流参数，用于拼接视频流地址
     *
     * @param url url
     * @param token token
     * @return result
     */
    private DronePlayStreamResultData getPlayStream(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("TENANTID", "10000");
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<DronePlayStreamResult> resEntity = StaticContextAccessor.getBean(ServiceRestTemplate.class).exchange(url, HttpMethod.GET, requestEntity, DronePlayStreamResult.class);

        if (resEntity != null && resEntity.getBody() != null && resEntity.getBody().getData() != null) {
            return resEntity.getBody().getData();
        }

        return null;
    }

    /**
     * 拼接视频流地址
     *
     * @param data data
     * @param cameraType cameraType
     * @return result
     */
    private Map<String, Object> getPlayStreamUrl(DronePlayStreamResultData data, String cameraType) {
        String rtmpStr = "rtmp://%s:%s/live/%s";
        String httpStr = "rtmp://%s:%s/live/%s.flv";
        String wsStr = "rtmp://%s:%s/live/%s.flv";

        Map<String, Object> map = new HashMap<>();
        map.put("rtmpUrlInferad", String.format(rtmpStr, data.getDrone_ip(), data.getRtmp_port(), data.getSteam()));
        map.put("flvUrl", String.format(httpStr, data.getDrone_ip(), data.getHttp_port(), data.getSteam()));
        map.put("webRtcUrl", String.format(wsStr, data.getDrone_ip(), data.getHttp_port(), data.getSteam()));
        map.put("cameraType", cameraType);

        return map;
    }

    /**
     * 32位UUID生成方法
     *
     * @return 32位UUID生成方法
     */
    public String getUUID() {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();
        if (StringUtils.isNotEmpty(uuidStr)) {
            uuidStr = uuidStr.toUpperCase();
            uuidStr = uuidStr.replaceAll("-", "");
        } else {
            uuidStr = "";
        }
        return uuidStr;
    }
}
