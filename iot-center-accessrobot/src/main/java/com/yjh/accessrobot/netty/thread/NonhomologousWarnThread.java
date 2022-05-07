package com.yjh.accessrobot.netty.thread;

import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.service.NonhomologousWarnService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.Map;
import java.util.Set;

/**
 * @author sunjinyan
 * @since 2022-04-08
 * 机器人侧非同源告警处理线程
 */
@lombok.extern.slf4j.Slf4j
public class NonhomologousWarnThread implements Runnable{

    private RedisTemplate redisTemplate;
    private Map<String,String> cruiseResultMap;
    private String webSocketUrl;
    private Boolean changeTaskStatus;
    private int isResult;
    private static final String IS_AI_AlGORITHM = "on";

    public NonhomologousWarnThread(Map<String,String> cruiseResultMap, RedisTemplate redisTemplate, String webSocketUrl,int isResult){
        this.cruiseResultMap = cruiseResultMap;
        this.redisTemplate = redisTemplate;
        this.webSocketUrl = webSocketUrl;
        this.isResult = isResult;
    }

    @Override
    public void run(){
        try {
            log.info("开始处理巡检结果并生成相应的非同源告警 >>>>>>> cruiseResultMap==={}, isResult ==={}", cruiseResultMap, isResult);
            StaticContextAccessor.getBean(NonhomologousWarnService.class).insertNonhomologousWarn(cruiseResultMap,isResult);
        } catch (Exception e) {
            log.error("巡检结果处理失败", e);
            throw new RuntimeException("巡检结果处理失败");
        }
    }

    /**
     * Redis数据库批量查询Key值游标
     */
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }
}
