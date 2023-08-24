/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.module.user.dao.SysKeyDao;
import com.yjh.platform.module.user.entity.SysKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/4/17
 * @since [产品/模块版本] （可选）
 */
@Service
public class SysKeyService {
    private static final Logger log = LoggerFactory.getLogger(SysKeyService.class);

    @Autowired
    private SysKeyDao sysKeyDao;
    @Autowired
    private Demo demo;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private LogsRecord logsRecord;

    private static final String MOTLEY_CODE = "eWlqaWFoZQ";


    @Transactional(rollbackFor = Exception.class)
    public int insert(SysKey sysKey, HttpServletRequest request) throws IOException {
        String userIds = request.getHeader("userId");
        String userName = String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userIds, "userName"));
        String identifier = request.getParameter("identifier");

        String name = (String)redisTemplate.opsForHash().get("userInfo:" + sysKey.getUserId(), "userName");

        String bindName = sysKey.getBindName();
        if (StringUtils.isEmpty(bindName)) {
            log.error("绑定类型不正确, {}, {}", sysKey.getBindType(), sysKey.getBindName());
            throw new BusinessException(ResultCodeEnum.PARAMERROR.getCode(), "绑定类型不正确！");
        }
        if(sysKey.getBindType() == SysKey.BindEnum.UKEY.getCode()) {
            SysKey sk = sysKeyDao.selectBySerialNum(sysKey.getSerialNum());
            if (sk != null) {
                log.error("Ukey 重复绑定, {}, {}", sk.getUserId(), sk.getSerialNum());
                throw new BusinessException(ResultCodeEnum.PARAMERROR.getCode(), "每个 UKey 只可绑定一个用户！");
            }
            String isDecode = (String) redisTemplate.opsForHash().get("t_sys_param:isEncryption", "content");
            // 判断是否需要加解密
            if ("true".equals(isDecode)) {
                String dpkey = demo.decryptIdentifier(sysKey.getPubKey(), identifier);
                if (StringUtils.isNotEmpty(dpkey)) {
                    sysKey.setPubKey(dpkey);
                }
            }
            if(StringUtils.length(sysKey.getPubKey()) > 512) {
                throw new BusinessException(ResultCodeEnum.PARAMERROR.getCode(), "公钥长度必须小于512位");
            }
            redisTemplate.opsForHash().put("sysKey:" + sysKey.getUserId() + ":" + sysKey.getBindType(), sysKey.getSerialNum(), sysKey.getPubKey());
        } else {
            redisTemplate.opsForSet().add("sysKey:" + sysKey.getUserId() + ":" + sysKey.getBindType(), sysKey.getSerialNum());
        }

        int total = sysKeyDao.insert(sysKey);
        logsRecord.LoginLogsSend(request, "28", "用户绑定", userName + "对用户" + name + "绑定了" + bindName, userName, String.valueOf(userIds), 1);
        return total;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long keyId, HttpServletRequest request) {
        String userIds = request.getHeader("userId");
        String userName = String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userIds, "userName"));

        SysKey sysKey = sysKeyDao.selectByPrimaryId(keyId);
        if (sysKey == null) {
            log.error("用户绑定不存在, {}", keyId);
            throw new BusinessException(ResultCodeEnum.PARAMERROR.getCode(), "用户绑定不存在！");
        }
        Long userId = sysKey.getUserId();
        String names = String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userId, "userName"));
        String bindName = sysKey.getBindName();

        if(sysKey.getBindType() == SysKey.BindEnum.UKEY.getCode()) {
            redisTemplate.opsForHash().delete("sysKey:" + sysKey.getUserId() + ":" + sysKey.getBindType(), sysKey.getSerialNum());
        } else {
            redisTemplate.opsForSet().remove("sysKey:" + sysKey.getUserId() + ":" + sysKey.getBindType(), sysKey.getSerialNum());
        }
        int ret = sysKeyDao.deleteByPrimaryId(keyId);
        logsRecord.LoginLogsSend(request, "4", "删除绑定", userName + "删除了" + names + "用户绑定" + bindName, userName, String.valueOf(userId), 1);
        return ret;
    }

    public void loadKeysToRedis() {
        Set<String> keys = redisTemplate.keys("sysKey:*");
        redisTemplate.delete(keys);

        List<SysKey> all = sysKeyDao.selectAll();
        all.forEach(sysKey -> {
            if(sysKey.getBindType() == SysKey.BindEnum.UKEY.getCode()) {
                redisTemplate.opsForHash().put("sysKey:" + sysKey.getUserId() + ":" + sysKey.getBindType(), sysKey.getSerialNum(), sysKey.getPubKey());
            } else {
                redisTemplate.opsForSet().add("sysKey:" + sysKey.getUserId() + ":" + sysKey.getBindType(), sysKey.getSerialNum());
            }
        });
    }

    public List<SysKey> getUserKeys(Long userId) {
        List<SysKey> userKeyList = sysKeyDao.selectByUserId(userId);
        userKeyList.forEach(sysKey -> {
            String pubKey = sysKey.getPubKey();
            int len = pubKey.length();
            if(len > 8) {
                String pubM = pubKey.substring(0, 8) + "*********************";
                sysKey.setPubKey(pubM);
            }
        });
        return userKeyList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertUser(String userName) {
        String uniqueUser = Demo.summary(MOTLEY_CODE + userName);
        return sysKeyDao.insertUser(uniqueUser);
    }

    public int countUser(String userName) throws IOException {
        String uniqueUser = Demo.summary(MOTLEY_CODE + userName);
        return sysKeyDao.countUser(uniqueUser);
    }
}
