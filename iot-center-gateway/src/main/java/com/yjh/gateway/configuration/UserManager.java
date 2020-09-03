package com.yjh.gateway.configuration;

import com.yjh.gateway.commons.result.BusinessException;
import com.yjh.gateway.commons.security.UserInterface;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author tt
 * @Date 2019/6/19
 **/
@Component
public class UserManager implements UserInterface {
    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    public long getUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader(USER_ID);
        return StringUtils.isEmpty(userId) ? 0l : Long.parseLong(userId);
    }

    /**
     * @param code
     * @return
     */
    public String getAuthByCode(String code) {
        long userId = getUserId();
        if (0 == userId) {
            return null;
        }
        Object obj = redisTemplate.opsForHash().get(USER_AUTH + userId, code);
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
        long userId = getUserId();
        if (0 == userId) {
            return new HashMap();
        }
        return redisTemplate.opsForHash().entries(USER_BASE + userId);
    }

    /**
     * 获取用户角色（1001,111,10003）
     *
     * @return String
     */
    public String getUserRoles() {
        long userId = getUserId();
        if (0 == userId) {
            return null;
        }
        Object obj = redisTemplate.opsForValue().get(USER_ROLE + userId);
        if (null != obj) {
            return String.valueOf(obj);
        }
        return null;
    }

    /**
     * 获取用户角色
     *
     * @return
     */
    public List<Long> getUserRolesList() {
        String userRoles = getUserRoles();
        if (StringUtils.isBlank(userRoles)) {
            throw new BusinessException("用户没有角色");
        }
        return Arrays.asList(userRoles.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
    }

    /**
     * 是否是超级管理员
     *
     * @return
     */
    public boolean isSysAdmin() {
        String roles = getUserRoles();
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
