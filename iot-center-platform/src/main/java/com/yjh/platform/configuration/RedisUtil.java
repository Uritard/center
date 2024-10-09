package com.yjh.platform.configuration;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    private static RedisTemplate<String, Object> redisTemplate;

    private static Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    public static void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtil.redisTemplate = redisTemplate;
    }

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public static void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 批量删除key
     *
     * @param pattern
     */

    public static void removePattern(final String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public static void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public static boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public static Object get(final String key) {
        Object result = null;
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<String, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean set(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<String, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    /**
     * 读取缓存，根据固定的
     * 输入对应的key  redis库编号以及hashname
     * 查询对应的value
     * @param key,dataBaseIndex,hashName
     * @return
     */
    public static Object get(final String key,int dataBaseIndex,String hashName) {
        JedisConnectionFactory connectionFactory =(JedisConnectionFactory) redisTemplate.getRequiredConnectionFactory();
        connectionFactory.getStandaloneConfiguration().setDatabase(dataBaseIndex);
        String value=(String)redisTemplate.opsForHash().get(hashName,key);
        return value;
    }

    public static void set(Map map, int databaseindex, String hashname) {
        JedisConnectionFactory connectionFactory =(JedisConnectionFactory) redisTemplate.getRequiredConnectionFactory();
        connectionFactory.getStandaloneConfiguration().setDatabase(databaseindex);
        redisTemplate.opsForHash().putAll(hashname,map);
    }

    public static Set<String> redisScan(String key) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newLinkedHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match(key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                } else {
                    break;
                }
            }

            return keys;
        });
    }

    public static boolean setHashGroupAndExpire(String prefex, String suffixKey, Map<String, String> valueMap, int expire) {
        String infoKey = suffixKey;
        if (StringUtils.isNotEmpty(prefex)) {
            infoKey = prefex + ":" + suffixKey;
            redisTemplate.opsForList().rightPush(prefex, infoKey);
        }
        redisTemplate.opsForHash().putAll(infoKey, valueMap);
        redisTemplate.expire(infoKey, expire, TimeUnit.DAYS);
        redisTemplate.expire(prefex, expire, TimeUnit.DAYS);
        return true;
    }
}
