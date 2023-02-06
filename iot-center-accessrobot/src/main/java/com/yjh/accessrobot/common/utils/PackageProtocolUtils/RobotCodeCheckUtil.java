package com.yjh.accessrobot.common.utils.PackageProtocolUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @Author jinyujiang
 * @Description
 * @Date create in 2023/2/3 10:45
 */
@Component
@Slf4j
public class RobotCodeCheckUtil {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private static RedisTemplate staticRedisTemplate;

    @PostConstruct
    public void init() {
        staticRedisTemplate = redisTemplate;
    }

    /**
     * 检测rootName是否包含在机器人和无人机台账中
     *
     * @param rootName rootName
     * @return result
     */
    public static boolean checkRobotCode(String rootName) {
        if (StringUtils.isEmpty(rootName) || staticRedisTemplate == null) {
            return false;
        }

        try {
            Map<String, String> allRobotCodeMap = staticRedisTemplate.opsForHash().entries("AllRobotCode");
            if (allRobotCodeMap != null || allRobotCodeMap.size() > 0) {
                return allRobotCodeMap.containsValue(rootName);
            }
        } catch (Exception e) {
            log.info("checkRobotCode fail, err: {}", e.getMessage());
        }

        return false;
    }
}
