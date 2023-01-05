package com.yjh.gateway.common.authFilter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.yjh.gateway.common.utils.Decode;
import com.yjh.gateway.common.utils.IpUtil;
import com.yjh.gateway.common.utils.MultisMap;
import com.yjh.gateway.common.utils.ParamUtil;
import com.yjh.gateway.common.websocket.WebSocketServer;
import com.yjh.gateway.commons.restTemplate.LogsAspect;
import com.yjh.gateway.commons.utils.gmhelper.SM2Verify_SKF;
import com.yjh.gateway.commons.utils.http.IPUtil;
import com.yjh.gateway.commons.utils.smUtil.Demo;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

@Component
public class zuulFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(zuulFilter.class);

    // 国密规范测试用户ID
    private static final String UKEY_USERID ="1234567812345678";

    private static volatile Map<String, String[]> SECURE_SIGNS;

    @Value("${spring.logout.path}")
    private String LOGOUT_GATEWAY_URL;

    private static RedisTemplate redisTemplate;

    @Resource
    private LogsAspect logsAspect;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @SneakyThrows
    @Override
    public boolean shouldFilter() {
        Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:isDecode");
        String isDecode = map.get("content");
        Map<String, String> uKeymap = redisTemplate.opsForHash().entries("t_sys_param:isUkey");
        String isUkey = uKeymap.get("content");
        Map<String, String> ipmap = redisTemplate.opsForHash().entries("t_sys_param:isIp");
        String isIp = ipmap.get("content");
        Map<String, String> ipLoginmap = redisTemplate.opsForHash().entries("t_sys_param:isIpLogin");
        String isIpLogin = ipLoginmap.get("content");
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String url = request.getRequestURI();
        String remoteIp = IPUtil.getRemoteIP(request);
        log.info("tt-url: {}", remoteIp);
        ctx.getZuulRequestHeaders().put("HTTP_X_FORWARDED_FOR", remoteIp);

        String userId = request.getHeader("userId") != null ? request.getHeader("userId") : "";
        String token = request.getHeader("token") != null ? request.getHeader("token") : "";
        if (!url.contains("/sysUser/v1/login")
            && !url.contains("/sysUser/v1/randomNumbers")
            && !url.contains("/sysUser/v1/loginChangePassword")
            && !url.contains("/sysUser/v1/getPubk")
            && !url .contains("/tSysParam/v1/sysConfig")
            && !url .contains("/tSysParam/v1/homePageInfo")) {
            if (StringUtils.isNoneBlank(token)) {
                Map<String, String> appKeymap = redisTemplate.opsForHash().entries("appKey:" + userId + ":" + token);
                String isLogin = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isLogin", "content"));
                if (appKeymap.size() == 0) {
                    log.error("用户未登陆===================================================================================");
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(HttpStatus.SC_USE_PROXY);
                    return false;
                } else {
                    Long expireTime = Long.valueOf(appKeymap.get("expireTime"));
                    int logoutTime = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:logoutTime", "content")));
                    if (System.currentTimeMillis() - expireTime > 60000 * logoutTime) {
                        log.error("token已失效===========================================================================");
                        String userName = appKeymap.get("userName");
                        this.postUrl(token, remoteIp, userId, userName);
                        ctx.setSendZuulResponse(false);
                        ctx.setResponseStatusCode(HttpStatus.SC_USE_PROXY);
                        return false;
                    } else {
                        if (!url.contains("/sysLog/v1/errLog") && !url.contains("/tWarnInfo/v1/warnCountsNonIdentify") &&
                                !url.contains("/homePage/v1/getWeatherInfo") && !url.contains("/tUnionTask/v1/linkageMonitorData") &&
                                !url.contains("/homePage/v1/taskOnExecute") && !url.contains("/homePage/v1/countByAlarmLevel") &&
                                !url.contains("/tRobotInspection/v1/selectRobotTaskMessage") && !url.contains("/tRobotInspection/v1/selectRobotStatus") &&
                                !url.contains("/systemInfo/v1/getDiskOnUse") && !url.contains("/systemInfo/v1/getCpuOnUse") &&
                                !url.contains("/systemInfo/v1/getDisk") && !url.contains("/systemInfo/v1/getCPU") &&
                                !url.contains("/systemInfo/v1/getMemory") && !url.contains("/tCameraScreen/v1/cameraStateTree") &&
                                !url.contains("/tCruiseTaskResult/v1/selectCruiseAdvance") && !url.contains("/tCruiseTaskResult/v1/selectCurrentCruiseTaskResult") &&
                                !url.contains("/tCruiseTaskResult/v1/selectRealTimeWarnInfo") && !url.contains("/tCruisePointInstance/v1/selectCruiseCountByType") &&
                                !url.contains("/tCruiseTaskResult/v1/selectCruiseStatusCount") && !url.contains("/tCameraScreen/v1/selectCameraTreeWithRobot") &&
                                !url.contains("/tCameraInfo/v1/selectByPage") && !url.contains("/tCameraRecorder/v1/selectByPage") &&
                                !url.contains("/tRobotInfo/v1/selectByPage") && !url.contains("/videoIntercom/v1/selectByPage") &&
                                !url.contains("/tVoiceDevice/v1/selectByPage") && !url.contains("/homePage/v1/warnInfo") &&
                                !url.contains("/sysUser/v1/randomNumbers") && !url.contains("/sysUser/v1/getPubk")
                        ) {
                            redisTemplate.opsForHash().put("appKey:" + userId + ":" + token, "expireTime", String.valueOf(System.currentTimeMillis()));
                            if ("true".equals(isLogin)) {
                                redisTemplate.opsForHash().put("user:" + userId, "expireTime", String.valueOf(System.currentTimeMillis()));
                            }
                        }
                    }
                }
            } else {
                log.error("无token========================================================================================");
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(HttpStatus.SC_USE_PROXY);
                return false;
            }
        }

        Map<String, Object>  paramMap = ParamUtil.getRequestParams(ctx);
        log.info("paramMap {}", paramMap);
        String needValid = (String) redisTemplate.opsForHash().get("t_sys_param:paramsValidate", "content");
        // 统一验证参数，不允许【~!$%^&*+<>?"{}();'】
        if ("true".equals(needValid)) {
            String validatStr = ParamUtil.validated(paramMap);
            if (StringUtils.isNotEmpty(validatStr)) {
                return errorRespnse(ctx, HttpStatus.SC_BAD_REQUEST, "{\"code\":400,\"message\":\"" + validatStr + "\"}");
            }
        }
        String userName = "unknown";
        if (paramMap != null && paramMap.containsKey("userName")){
            userName = String.valueOf(paramMap.get("userName"));
        }else {
            userName = (String) redisTemplate.opsForHash().get("appKey:" + userId + ":" + token, "userName");
        }

        if (!secureSignsVerify(url, userId, userName)) {
            return errorRespnse(ctx, HttpStatus.SC_FORBIDDEN, "{\"code\":403,\"message\":\"缺少当前资源访问权限，请联系管理员!\"}");
        }

        if ("true".equals(isDecode)) {
            if (!url.contains("/sysUser/v1/randomNumbers")
                &&!url.contains("/sysUser/v1/getPubk")
                && !url.contains("/sysUser/v1/login")
                && !url.contains("/sysUser/v1/loginChangePassword")
                &&!url.contains("/tSysParam/v1/sysConfig")
                && !url .contains("/tSysParam/v1/homePageInfo")) {
                String absCode = request.getHeader("absCode") != null ? request.getHeader("absCode") : "";
                if (StringUtils.isNoneBlank(userId)) {
                    StringBuilder sb = new StringBuilder();
                    for (char c : userId.toCharArray()) {
                        sb.append(Integer.toUnsignedString(c, 10));
                    }
                    String absToken = Demo.summary(sb.toString());
                    if (!absToken.equals(absCode)) {
                        log.error("参数篡改userId: " + userId + " ,之后的absCode: " + absToken + ",前端absCode: " + absCode);
//                        ctx.setSendZuulResponse(false);
//                        ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                        return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"参数篡改，请联系管理员!\"}");
                    }
                } else {
                    log.error("无userId====================================================================================");
//                    ctx.setSendZuulResponse(false);
//                    ctx.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
                    return errorRespnse(ctx, HttpStatus.SC_BAD_REQUEST, "{\"code\":400,\"message\":\"参数错误，请刷新页面或联系管理员处理!\"}");
                }
            }
            MyRequestWrapper requestWrapper = null;
            MultipartHttpServletRequest multipartHttpServletRequest = null;
            String webcode = request.getHeader("summary") != null ? request.getHeader("summary") : "";
            if (request instanceof HttpServletRequest) {
                if ("POST".equals(request.getMethod().toUpperCase()) || "PUT".equals(request.getMethod().toUpperCase())) {
                    if ("true".equals(isIp)) {
                        String origin = request.getHeader("Origin") != null ? request.getHeader("Origin") : "";
                        String referer = request.getHeader("Referer") != null ? request.getHeader("Referer") : "";
                        String refererHost = referer.substring(referer.indexOf(":") + 3);
                        if (refererHost.contains(":")) {
                            refererHost = refererHost.substring(0, refererHost.indexOf(":"));
                        } else if(refererHost.contains("/")) {
                            refererHost = refererHost.substring(0, refererHost.indexOf("/"));
                        }
                        String orig=origin.substring(origin.lastIndexOf('/') + 1);
                        String ym="yjh.biandian.com";
                        String localIp = IpUtil.getLocalIp();
                        if(orig.contains(":")) {
                            String originIp = origin.substring(origin.lastIndexOf('/') + 1, origin.lastIndexOf(':'));
                            if (!localIp.equals(originIp) || !localIp.equals(refererHost)) {
                                if (!ym.equals(originIp) || !ym.equals(refererHost)) {
                                    log.error("IP篡改: " + referer + " ,之后的ip: " + origin + ",本机IP: " + localIp);
//                                    ctx.setSendZuulResponse(false);
//                                    ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                                    String logIp = !localIp.equals(originIp) && !ym.equals(originIp) ? originIp : refererHost;
                                    logsAspect.loginLogsSend(request, originIp, "27", "IP地址异常", "IP篡改: " + logIp + ",本机IP: " + localIp , userName , userId, 2);
                                    return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"IP地址异常，请联系管理员!\"}");
                                }
                            }
                        }else{
                            String originIp = origin.substring(origin.lastIndexOf('/') + 1);
                            if (!localIp.equals(originIp) || !localIp.equals(refererHost)) {
                                if (!ym.equals(originIp) || !ym.equals(refererHost)) {
                                    log.error("IP篡改: " + refererHost + " ,之后的ip: " + originIp + ",本机IP: " + IpUtil.getLocalIp());
//                                    ctx.setSendZuulResponse(false);
//                                    ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                                    String logIp = !localIp.equals(originIp) && !ym.equals(originIp) ? originIp : refererHost;
                                    logsAspect.loginLogsSend(request, originIp, "27", "IP地址异常", "IP篡改: " + logIp + ",本机IP: " + localIp , userName, userId, 2);
                                    return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"IP地址异常，请联系管理员!\"}");
                                }
                            }
                        }
                    }
                    String contentType = request.getContentType();
                    if (contentType != null && contentType.contains("multipart/form-data")) {
                        String filePath = "";
                        MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
                        multipartHttpServletRequest = resolver.resolveMultipart(request);
                        Map<String, MultipartFile> fileMap = multipartHttpServletRequest.getFileMap();
                        Iterator<Map.Entry<String, MultipartFile>> it = fileMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<String, MultipartFile> entry = it.next();
                            MultipartFile mFile = entry.getValue();
                            if (mFile.getSize() != 0 && !"".equals(mFile.getName())) {
                                filePath += mFile.getOriginalFilename() + ";";
                            }
                        }
                        String filePaths = '"' + filePath.substring(0, filePath.length() - 1) + '"';
                        StringBuilder sb = new StringBuilder();
                        if (null != filePaths) {
                            for (char c : filePaths.toCharArray()) {
                                sb.append(Integer.toUnsignedString(c, 10));
                            }
                        }
                        String webToken = Demo.summary(sb.toString());
                        if (!webToken.equals(webcode)) {
                            log.error("参数篡改" + filePaths + " ,之后的summary: " + webToken + ",前端summary: " + webcode);
//                            ctx.setSendZuulResponse(false);
//                            ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                            return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"参数篡改，请联系管理员!\"}");
                        }
                        ctx.setRequest(multipartHttpServletRequest);
                    } else {
                        requestWrapper = new MyRequestWrapper(request);
                        String body = requestWrapper.getBody();
                        StringBuilder sb = new StringBuilder();
                        if (StringUtils.isBlank(body)) {
                            body = requestWrapper.getQueryMapsString();
                        }
                        if (StringUtils.isNoneBlank(body)) {
                            for (char c : body.toCharArray()) {
                                sb.append(Integer.toUnsignedString(c, 10));
                            }
                            String webToken = Demo.summary(sb.toString());
                            if (!webToken.equals(webcode)) {
                                log.error("参数篡改" + body + " ,之后的summary: " + webToken + ",前端summary: " + webcode);
//                                ctx.setSendZuulResponse(false);
//                                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                                return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"参数篡改，请联系管理员!\"}");
                            }
                        }
                        ctx.setRequest(requestWrapper);
                    }
                } else {
                    String str = request.getQueryString();
                    if (StringUtils.isNoneBlank(str)) {
                        MultisMap multiMap = new MultisMap();
                        Decode.decodeTo(str, multiMap, "UTF-8");
                        JSONObject jsonObject = new JSONObject(multiMap);
                        Map<String, Object> params = JSON.parseObject(jsonObject.toString(), LinkedHashMap.class);
                        JSONObject jsonModel = new JSONObject(true);
                        for (Map.Entry<String, Object> param : params.entrySet()) {
                            jsonModel.put(param.getKey(), param.getValue());
                        }
                        if (jsonModel.size() != 0) {
                            StringBuilder sb = new StringBuilder();
                            if (null != jsonModel.toJSONString()) {
                                for (char c : jsonModel.toJSONString().toCharArray()) {
                                    sb.append(Integer.toUnsignedString(c, 10));
                                }
                            }
                            String webToken = Demo.summary(sb.toString());
                            if (!webToken.equals(webcode)) {
                                log.error("参数篡改" + jsonModel.toJSONString() + " ,之后的summary: " + webToken + ",前端summary: " + webcode);
//                                ctx.setSendZuulResponse(false);
//                                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                                return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"参数篡改，请联系管理员!\"}");
                            }
                        }
                    }
                }
            }
        }

        if(StringUtils.isEmpty(userId)) {
            if("true".equals(redisTemplate.opsForHash().get("t_sys_param:isEncryption", "content"))){
                String identifier = String.valueOf(paramMap.get("identifier"));
                String priks = (String) redisTemplate.opsForHash().get("pubk:" + identifier, "prik");
                userName = Demo.decryptIdentifier(userName, priks);
                log.info("用户参数: {}, 之后的: {}, identifier: {}", userName, priks, identifier);
            }
            if(StringUtils.isNotEmpty(userName)) {
                userId = (String) redisTemplate.opsForHash().get("userInfo:nameId", userName);
            }
        }
        if(StringUtils.isEmpty(userId)) {
            userId = Objects.toString(paramMap.get("userId"));
        }
        // 判断登录用户 ip 地址
        if ("true".equals(isIpLogin)) {
            if (!url.contains("/sysUser/v1/randomNumbers")
                &&!url.contains("/homePage/v1/getWeatherInfo")
                &&!url.contains("/sysUser/v1/getPubk")
                &&!url.contains("/tSysParam/v1/sysConfig")
                && !url .contains("/tSysParam/v1/homePageInfo")) {
                if(StringUtils.isEmpty(userId)) {
                    return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"用户错误，请尝试强制刷新页面(CTRL + F5)!\"}");
                }
                String ipAddr = "\"" + IpUtil.getRemoteIP(request) + "\"";
                Set<String> ips = redisTemplate.opsForSet().members("sysKey:" + userId + ":2");

                boolean ipValid = ips == null || ips.isEmpty() || ips.contains(ipAddr);

                if (!ipValid || StringUtils.isEmpty(userId)) {
                    log.error("IP 地址验证失败 - {}-{} {}", userId, userName, ipValid);
                    ipAddr = ipAddr.replace("\"", "");
                    JSONObject jsonMap = new JSONObject();
                    jsonMap.put("type", "alarmPopUp");
                    jsonMap.put("ip", ipAddr);
                    jsonMap.put("warningInfo", "未绑定的IP地址");
                    jsonMap.put("userId", userId);
                    jsonMap.put("logType", "6");
                    jsonMap.put("userName", userName);
                    jsonMap.put("content", "用户[" + userName + "]在未绑定的IP地址访问系统!");
                    WebSocketServer.sendMsg(jsonMap.toJSONString());
                    logsAspect.loginLogsSend(request, ipAddr, "6", "登录", "IP:" + ipAddr + "与用户" + userName + "未绑定", userName, userId, 2);
                    return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"未绑定的IP地址!\"}");
                }
            }
        }
        // 判断登录用户 UKey
        if ("true".equals(isUkey)) {
            String signStr = request.getHeader("signStr") != null ? request.getHeader("signStr") : "";
            String webcode = request.getHeader("summary") != null ? request.getHeader("summary") : "";
            if (!url.contains("/sysUser/v1/randomNumbers")
                &&!url.contains("/homePage/v1/getWeatherInfo")
                &&!url.contains("/sysUser/v1/getPubk")
                &&!url.contains("/sysUser/v1/logout")
                &&!url.contains("/tSysParam/v1/sysConfig")
                && !url .contains("/tSysParam/v1/homePageInfo")) {
                if(StringUtils.isEmpty(userId)) {
                    return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"用户错误，请尝试强制刷新页面(CTRL + F5)!\"}");
                }
                String ukeyId = request.getHeader("ukeyId") != null ? request.getHeader("ukeyId") : "";
                // String xlh = ukeyId.substring(0,16);

                String pubkey = (String)redisTemplate.opsForHash().get("sysKey:" + userId + ":1", ukeyId);
                if(StringUtils.isEmpty(pubkey)){
                    log.error("序列号不正确------------------------{}-{} {}", userId, userName, ukeyId);

                    return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"请插入正确的ukey!\"}");
                }
                boolean status = SM2Verify_SKF.SM2Verify(pubkey, signStr, webcode, UKEY_USERID);
                if (!status) {
                    log.error("签名验证结果 - {}", status);

                    return errorRespnse(ctx, HttpStatus.SC_UNAUTHORIZED, "{\"code\":401,\"message\":\"签名验证失败，请重新请求或 ukey 和用户不匹配!\"}");
                }
            }
        }
        return true;

    }

    private boolean secureSignsVerify(String url, String userId, String username) {
        loadSecureSigns();
        if (MapUtils.isEmpty(SECURE_SIGNS)) {
            return true;
        }
        String uri = StringUtils.substringBefore(url, "?");
        // 判断当前 uri 是否在安全标识范围内
        boolean flag = false;
        for (String[] pathValues : SECURE_SIGNS.values()) {
            if (StringUtils.endsWithAny(uri, pathValues)) {
                flag = true;
                break;
            }
        }
        // 不在安全标识范围，直接返回 true
        if (!flag) {
            return true;
        }
        // 在安全标识范围内，判断当前用户安全标识
        String[] pathSigns = SECURE_SIGNS.containsKey(userId) ? SECURE_SIGNS.get(userId) : SECURE_SIGNS.get(username);
        // 当前用户无安全标识，返回 false
        if (ArrayUtils.isEmpty(pathSigns)) {
            return false;
        }
        // 当前用户有安全标识，判断当前连接是否属于当前用户的安全标识
        return StringUtils.endsWithAny(uri, pathSigns);
    }

    private static void loadSecureSigns() {
        if (SECURE_SIGNS == null) {
            synchronized (UKEY_USERID) {
                if (SECURE_SIGNS == null) {
                    String secureSigns = (String)redisTemplate.opsForHash().get("t_sys_param:secureSigns", "content");
                    log.info("安全标识配置: {}", secureSigns);
                    if (StringUtils.isEmpty(secureSigns)) {
                        return;
                    }
                    SECURE_SIGNS = new HashMap<>(16);
                    // user1:path1,path2;user2:path3,path4
                    String[] userSigns = secureSigns.split(";");
                    for (String sign : userSigns) {
                        if (!StringUtils.contains(sign, ":")) {
                            continue;
                        }
                        String[] userSign = sign.split(":", 2);
                        SECURE_SIGNS.put(userSign[0], StringUtils.split(userSign[1], ","));
                    }
                }
            }
        }
    }

    public static void reloadSecureSigns() {
        SECURE_SIGNS = null;
        loadSecureSigns();
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String s = String.format("%s >>> %s", request.getMethod(), request.getRequestURL().toString());
        log.info(s);
        return null;
    }

    //请求webSocket发送方法
    public String postUrl(String token, String ip, String userId, String userName) throws IOException, URISyntaxException {
        CloseableHttpClient client = HttpClients.createDefault();
        URI uri = new URIBuilder(LOGOUT_GATEWAY_URL).setParameter("token", token).setParameter("ip", ip).setParameter("userId", userId).setParameter("userName", userName).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(token, Charset.forName("UTF-8")));
        httpPost.setEntity(new StringEntity(ip, Charset.forName("UTF-8")));
        httpPost.setEntity(new StringEntity(userId, Charset.forName("UTF-8")));
        httpPost.setEntity(new StringEntity(userName, Charset.forName("UTF-8")));
        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    boolean errorRespnse(RequestContext ctx, int nStatusCode, String errorMsg){
        ctx.setSendZuulResponse(false);
        ctx.addZuulResponseHeader("Content-Type", "application/json;charset=UTF-8");
        ctx.setResponseStatusCode(nStatusCode);
        ctx.setResponseBody(errorMsg);
        return false;
    }

    @Resource
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        zuulFilter.redisTemplate = redisTemplate;
    }
}
