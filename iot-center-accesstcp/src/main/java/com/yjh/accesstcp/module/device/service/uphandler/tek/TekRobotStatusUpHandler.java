package com.yjh.accesstcp.module.device.service.uphandler.tek;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.HttpClientUtils;
import com.yjh.accesstcp.module.device.dao.TRobotInfoMapper;
import com.yjh.accesstcp.module.device.entity.RobotModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/12/30
 * @since [产品/模块版本] （可选）
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class TekRobotStatusUpHandler {
    private final TRobotInfoMapper tRobotInfoMapper;
    private final UrlPathHandler urlPathHandler;
    private final TaskScheduler taskScheduler;

    @Value("${up.tek.duration:5}")
    private Long duration;
    private static final String ROBOT_STATUS_UPLOAD_URL = "/distribute/data/service/robotStatus/receive";

    public void uploadRobotStatus(RedisTemplate redisTemplate) {
        //鉴权
        urlPathHandler.authToken();
        //心跳发送
        taskScheduler.scheduleAtFixedRate(new InternalRunner(tRobotInfoMapper, urlPathHandler, redisTemplate), Instant.now(),
            Duration.ofSeconds(duration));
    }

    static class InternalRunner implements Runnable {
        private final TRobotInfoMapper tRobotInfoMapper;
        private final UrlPathHandler urlPathHandler;
        private final RedisTemplate redisTemplate;

        public InternalRunner(TRobotInfoMapper tRobotInfoMapper, UrlPathHandler urlPathHandler, RedisTemplate redisTemplate) {
            this.tRobotInfoMapper = tRobotInfoMapper;
            this.urlPathHandler = urlPathHandler;
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void run() {
            try {
                String url = urlPathHandler.getTekUrl(ROBOT_STATUS_UPLOAD_URL);
                List<Map<String, Object>> robotStatusList = new ArrayList<>();
                List<RobotModel> robotList = tRobotInfoMapper.selectAllRobot();
                for (RobotModel robot : robotList) {
                    Map<String, Object> params = new LinkedHashMap<>();
                    String[] edges = StringUtils.split(Constant.edgeCode(), "-", 2);
                    params.put("lineCode", ArrayUtil.get(edges, 0));
                    params.put("stationCode", ArrayUtil.get(edges, 1));
                    params.put("patrolDeviceUniqueCode", Constant.edgeCode() + "-" + robot.getRobotIp() + "-0");
                    params.put("onlineStatus", StringUtils.equals("在线", robot.getRobotStatus()) ? 1 : 0);
                    //自动模式
                    params.put("runModel", 1);
                    Map<String, Object> mapForRobotState =
                        redisTemplate.opsForHash().entries("RobotStatus:" + robot.getRobotCode() + ":41");
                    //空闲
                    int patrolStatus = 0;
                    if (mapForRobotState.size() != 0) {
                        String state = mapForRobotState.get("value").toString();
                        switch (state) {
                            case "2":
                                //巡检
                                patrolStatus = 1;
                                break;
                            case "3":
                            case "4":
                                //暂停
                                patrolStatus = 2;
                            default:
                                break;
                        }
                    }
                    params.put("patrolStatus", patrolStatus);
                    robotStatusList.add(params);
                }
                String result = HttpClientUtils.getInstance().doPost(url, JSON.toJSONString(robotStatusList));
                log.info("上报机器人状态返回： {}", result);
            } catch (IOException e) {
                log.error("上报机器人状态失败", e);
            }
        }
    }
}
