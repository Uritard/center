package com.yjh.platform.configuration;

import com.yjh.platform.configuration.UserInterface;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author tt
 * @Date 2019/6/19
 **/
@Component
public class UserManager implements UserInterface {
    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    public long getCreatorId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String creatorId = request.getHeader(CREATOR_ID);
        return StringUtils.isEmpty(creatorId) ? 0l : Long.parseLong(creatorId);
    }

    public String getUserName() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getHeader("userName");
    }

    public String getAppKey() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getHeader("appKey");
    }

    /**
     * @param code
     * @return
     */
    public String getAuthByCode(String code) {
        long creatorId = getCreatorId();
        if (0 == creatorId) {
            return null;
        }
        Object obj = redisTemplate.opsForHash().get(USER_AUTH + creatorId, code);
        if (null != obj) {
            return String.valueOf(obj);
        }
        return null;
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public Map getUserMap() {
        long creatorId = getCreatorId();
        if (0 == creatorId) {
            return new HashMap();
        }
        return redisTemplate.opsForHash().entries(USER_BASE + creatorId);
    }

    /**
     * 获取用户角色（1001,111,10003）
     *
     * @return String
     */
    public String getUserRole() {
        String appKey = getAppKey();
        if (Objects.isNull(appKey)) return null;
        Map<String, Object> map = (Map<String, Object>) redisTemplate.opsForValue().get(appKey);
        if (Objects.isNull(map)) return null;
        return String.valueOf(map.get("roleId"));
    }

    /**
     * 是否是超级管理员
     *
     * @return
     */
    public boolean isSysAdmin() {
        String roles = getUserRole();
        if (null == roles) {
            return false;
        }
        String[] strs = StringUtils.split(roles, ",");
        for (String str : strs) {
            if ("1".equals(str)) {//角色ID=1 是系统管理员，系统默认角色
                return true;
            }
        }
        return false;
    }

}
