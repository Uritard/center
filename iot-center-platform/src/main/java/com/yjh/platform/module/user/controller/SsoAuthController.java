/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.user.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.IPUtil;
import com.yjh.platform.common.utils.smUtil.SM2Utils;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.entity.SysUserLogin;
import com.yjh.platform.module.user.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/15
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/ssoAuth/v1")
@Api(value = "/ssoAuth", tags = {"单点登录相关接口"})
@Slf4j
public class SsoAuthController {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private LogsRecord logsRecord;

    @ApiOperation(value = "生成sso授权信息")
    @GetMapping(value = "/generate")
    public Result insert(@RequestParam(value = "path") String path, HttpServletRequest request) {
        Result result = new Result();
        String username = null;
        String userId = null;
        try {
            userId = request.getHeader("userId");
            String token = request.getHeader("token");

            username = (String)redisTemplate.opsForHash().get("appKey:" + userId + ":" + token, "userName");

            Map<String, Object> loginMap = new HashMap<>(8);
            loginMap.put("toPath", path);
            loginMap.put("username", username);
            loginMap.put("timestamp", System.currentTimeMillis());
            loginMap.put("replayAvoid", RandomUtil.randomString(6));
            loginMap.put("address", IPUtil.getIp());

            String jsonStr = JSON.toJSONString(loginMap);

            String ssoPublicKey = SysParamConfig.getSysContent("ssoPublicKey");

            String secretData = SM2Utils.encrypt(Base64.decode(ssoPublicKey), jsonStr.getBytes(StandardCharsets.UTF_8));
            result.setData(secretData);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("生成sso登录信息失败:", e);
        } finally {
            int state = result.getCode() == 200 ? 1 : 2;
            logsRecord.LoginLogsSend(request, "6", "sso登录", "用户生成sso信息 - " + path, username, userId, state);
        }
        return result;
    }

    @ApiOperation(value = "sso鉴权")
    @GetMapping(value = "/authority")
    public Result authority(@RequestParam(value = "secret") String secret, HttpServletRequest request) {
        Result result = new Result();
        String ip = null;
        String username = null;
        String userId = "";
        try {
            String ssoPrivateKey = SysParamConfig.getSysContent("ssoPrivateKey");
            byte[] decodes = SM2Utils.decrypt(Base64.decode(ssoPrivateKey), Hex.decode(secret));

            String loginInfo = Strings.fromUTF8ByteArray(decodes);
            log.info("ssoLoginInfo: {}", loginInfo);

            JSONObject login = JSON.parseObject(loginInfo);
            ip = login.getString("address");
            username = login.getString("username");
            Long timestamp = login.getLong("timestamp");
            String replayAvoid = login.getString("replayAvoid");
            String toHttpPath = login.getString("toPath");

            int subIndex = toHttpPath.indexOf("/", Math.max(8, toHttpPath.lastIndexOf(":")));
            String toPath = toHttpPath.substring(subIndex);

            if (System.currentTimeMillis() - timestamp > 5 * 60 * 1000) {
                throw new BusinessException(ResultCodeEnum.CODE10108.getCode(), "用户认证已过期，请重新获取凭据");
            }
            String replayKey = "ssoAuthed:" + timestamp + "_" + replayAvoid;
            String userAuthed = (String)redisTemplate.opsForValue().get(replayKey);
            if (StringUtils.isNotEmpty(userAuthed)) {
                throw new BusinessException(ResultCodeEnum.CODE10108.getCode(), "重复请求，请重新获取凭据");
            }
            redisTemplate.opsForValue().set(replayKey, username, 10, TimeUnit.MINUTES);

            Map<String, Object> mapResult = new HashMap<>(8);
            mapResult.put("toPath", toPath);

            SysUserLogin sysUserLogin = sysUserDao.selectByUserNameAndL(username);
            if (sysUserLogin == null) {
                throw new BusinessException(ResultCodeEnum.CODE10101.getCode(), "系统无此用户，请确保用户信息已同步");
            }
            userId = String.valueOf(sysUserLogin.getUserId());
            sysUserLogin.setAppkey(RandomUtil.randomNumbers(10));

            sysUserService.userLoginInfo(mapResult, ip, sysUserLogin);

            result.setData(mapResult);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UNAUTHORIZED.getCode(), "sso鉴权失败，请求已过期或秘钥不正确");
            log.error("sso认证失败:", e);
        } finally {
            int state = result.getCode() == 200 ? 1 : 2;
            logsRecord.LoginLogsSend(request, "6", "sso登录", "用户生成sso信息 - " + ip, username, userId, state);
        }
        return result;
    }
}
