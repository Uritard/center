package com.yjh.accessvideo.module.control.service;

import com.yjh.accessvideo.commons.utils.http.HttpClientUtils;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.commons.utils.JSONUtil;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    public List<Map<String, Object>> droneStartRealPlay(Long robotId) {
        // 登录，获取token
        RobotConInfo robotConInfo = cameraConDao.selectDroneConInfo(robotId);
        log.info("cameraConDao.selectDroneConInfo(robotId), 查询结果：{}", JSONUtil.toJSONString(robotConInfo));
        String token = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:droneStation", "token"));
        if (token == null || StringUtils.isBlank(token)) {
            log.info("token 为空，需要获取token");
            token = initToken(robotConInfo);
        } else {
            log.info("token 不为空， {}", token);
        }

        if (StringUtils.isBlank(token)) {
            // 获取token失败，
            return getRealPlayUrlList(robotConInfo);
        }

        // 调用无人机视频流接口，并拼接视频流地址
        String droneStr = "http://%s:%s/prod-api/nest/nest/playStream/open/%s/3";
        String nestInnerStr = "http://%s:%s/prod-api/nest/nest/playStream/open/%s/0";
        String nestOutsideStr = "http://%s:%s/prod-api/nest/nest/playStream/open/%s/1";
        Map<String, Object> droneMap = getPlayStreamMap(droneStr, robotConInfo, token, "3");
        log.info("获取无人机的三路视频流,getPlayStreamMap 结果,droneMap：{}", JSONUtil.toJSONString(droneMap));

        if (droneMap == null || droneMap.size() == 0) {
            // token失效后需要再次获取token
            token = initToken(robotConInfo);
            droneMap = getPlayStreamMap(droneStr, robotConInfo, token, "3");
        }

        Map<String, Object> nestInnerMap = getPlayStreamMap(nestInnerStr, robotConInfo, token, "0");
        log.info("获取无人机的三路视频流,getPlayStreamMap 结果,nestInnerMap：{}", JSONUtil.toJSONString(nestInnerMap));

        Map<String, Object> nestOutsideMap = getPlayStreamMap(nestOutsideStr, robotConInfo, token, "1");
        log.info("获取无人机的三路视频流,getPlayStreamMap 结果,nestOutsideMap：{}", JSONUtil.toJSONString(nestOutsideMap));

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
        log.info("进入getPlayStreamMap方法，入参： urlStr：{},  robotConInfo: {}， token：{}， cameraType：{}", urlStr, JSONUtil.toJSONString(robotConInfo), token, cameraType);

        try {
            String url = String.format(urlStr, robotConInfo.getRobotIp(), robotConInfo.getRobotPort(), robotConInfo.getNestCode());
            log.info("getPlayStreamMap方法中，url： {}", url);

            DronePlayStreamResultData data = getPlayStream(url, token);
            log.info("获取无人机的三路视频流,getPlayStream 结果， data: {}", JSONUtil.toJSONString(data));
            Map<String, Object> map = getPlayStreamUrl(data, cameraType);
            log.info("获取无人机的三路视频流 getPlayStreamUrl 结果, map：{}", JSONUtil.toJSONString(map));
            return map;
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
        try {
            String loginStr = "http://%s:%s/prod-api/auth/login";
            String loginUrl = String.format(loginStr, robotConInfo.getRobotIp(), robotConInfo.getRobotPort());

            log.info("initToken方法中，获取token: loginUrl： {}", loginUrl);
            String token = getDroneToken(loginUrl, robotConInfo);

            redisTemplate.opsForHash().put("t_sys_param:droneStation", "token", token);

            log.info("获取token: {}", token);
            return token;
        } catch (Exception e) {
            log.error("initToken err !", e);
            return "";
        }
    }

    /**
     * 登录无人机站端并获取token
     *
     * @param url url
     * @return result
     */
    private String getDroneToken(String url, RobotConInfo robotConInfo) {
        log.info("进入getDroneToken方法，入参： url：{},  robotConInfo: {}", url, JSONUtil.toJSONString(robotConInfo));

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("Content-Type","application/json");
        headerMap.put("TENANTID","10000");
        headerMap.put("CLIENTID","nest");

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("userName", robotConInfo.getInferadUsername());
        parameterMap.put("passWord", robotConInfo.getInferadPassword());
        parameterMap.put("uuid", getUUID());
        parameterMap.put("code", "666666");

        log.info("登录无人机站端并获取token,post请求入参：url：{}， headerMap：{}， parameterMap：{}", url, JSONUtil.toJSONString(headerMap), JSONUtil.toJSONString(parameterMap));
        String resultJson = HttpClientUtils.getInstance().doPost(url, headerMap, parameterMap);
        log.info("登录无人机站端并获取token,post请求结果: resultJson: {}", resultJson);

        DroneLoginResult responseEntity = JSONUtil.toBean(resultJson, DroneLoginResult.class);

        if (responseEntity != null && responseEntity.getData() != null) {
            log.info("responseEntity 不为空，responseEntity: {}", JSONUtil.toJSONString(responseEntity));
            return responseEntity.getData().getAccess_token();
        } else {
            log.info("responseEntity 为空，responseEntity: {}", JSONUtil.toJSONString(responseEntity));
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
        log.info("进入getPlayStream方法，入参： url：{},  token: {}", url, token);

        try {
           Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("TENANTID", "10000");
            headerMap.put("Authorization", "Bearer " + token);
            log.info("getPlayStream 发起请求，入参：url: {}， headerMap: {}", url, JSONUtil.toJSONString(headerMap));
            String resultJson = HttpClientUtils.getInstance().doGet(url, headerMap);
            log.info("获取视频流参数,请求结果: resultJson: {}", resultJson);
            DronePlayStreamResult resEntity = JSONUtil.toBean(resultJson, DronePlayStreamResult.class);
            log.info("getPlayStream resEntity: ", JSONUtil.toJSONString(resEntity));
            if (resEntity != null) {
                return resEntity.getData();
            }
        } catch (Exception e) {
            log.error("getPlayStream err!", e);
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
        log.info("进入getPlayStreamUrl方法，入参： data：{},  cameraType: {}", JSONUtil.toJSONString(data), cameraType);

        String rtmpStr = "rtmp://%s:%s/live/%s";
        String httpStr = "http://%s:%s/live/%s.flv";
        String wsStr = "ws://%s:%s/live/%s.flv";

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

    /**
     * 根据robotInfo生成固定的视频流地址
     *
     * @param robotConInfo robotConInfo
     * @return result
     */
    private List<Map<String, Object>> getRealPlayUrlList(RobotConInfo robotConInfo) {
        List<Map<String, Object>> listResult = new ArrayList<>();
        DronePlayStreamResultData dataDrone = getDronePlayStreamResultData(robotConInfo, "3");
        Map<String, Object> mapDrone = getPlayStreamUrl(dataDrone, "3");
        listResult.add(mapDrone);

        DronePlayStreamResultData dataNestInner = getDronePlayStreamResultData(robotConInfo, "0");
        Map<String, Object> mapNestInner = getPlayStreamUrl(dataNestInner, "0");
        listResult.add(mapNestInner);

        DronePlayStreamResultData dataNestOutside = getDronePlayStreamResultData(robotConInfo, "1");
        Map<String, Object> mapNestOutside = getPlayStreamUrl(dataNestOutside, "1");
        listResult.add(mapNestOutside);

        return listResult;
    }

    /**
     * 根据cameraType，生成对应的DronePlayStreamResultData
     *
     * @param robotConInfo robotConInfo
     * @param cameraType cameraType
     * @return result
     */
    private DronePlayStreamResultData getDronePlayStreamResultData(RobotConInfo robotConInfo, String cameraType) {
        DronePlayStreamResultData data = new DronePlayStreamResultData();
        data.setDrone_ip(robotConInfo.getRobotIp());
        data.setHttp_port("8888");
        data.setRtmp_port("1935");
        data.setSteam(robotConInfo.getNestCode() + cameraType);
        return data;
    }
}
