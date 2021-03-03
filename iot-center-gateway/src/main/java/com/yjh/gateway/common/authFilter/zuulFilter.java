package com.yjh.gateway.common.authFilter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.yjh.gateway.common.Constant;
import com.yjh.gateway.common.utils.Decode;
import com.yjh.gateway.common.utils.MultisMap;
import com.yjh.gateway.commons.utils.smUtil.Demo;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
public class zuulFilter extends ZuulFilter {


    private static Logger log = LoggerFactory.getLogger(zuulFilter.class);

    @Resource
    private RedisTemplate redisTemplate;

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
        if ("true".equals(isDecode)) {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            MyRequestWrapper requestWrapper = null;
            MultipartHttpServletRequest multipartHttpServletRequest = null;
            String webcode = request.getHeader("summary") != null ? request.getHeader("summary") : "";
            if (request instanceof HttpServletRequest) {
                if ("POST".equals(request.getMethod().toUpperCase()) || "PUT".equals(request.getMethod().toUpperCase())) {
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
                                filePath+='"'+mFile.getOriginalFilename()+'"' + ";";
                            }
                        }
                        String filePaths = filePath.substring(0, filePath.length() - 1);
                        StringBuilder sb = new StringBuilder();
                        if (null != filePaths) {
                            for (char c : filePaths.toCharArray()) {
                                sb.append(Integer.toUnsignedString(c, 10));
                            }
                        }
                        String token = Demo.summary(sb.toString());
                        if (!token.equals(webcode)) {
                            log.error("参数篡改" + filePaths + " ,之后的summary: " + token + ",前端summary: " + webcode);
                            ctx.setSendZuulResponse(false);
                            ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                            return false;
                        }
                        ctx.setRequest(multipartHttpServletRequest);
                    } else {
                        requestWrapper = new MyRequestWrapper(request);
                        String body = requestWrapper.getBody();
                        StringBuilder sb = new StringBuilder();
                        if (null != body) {
                            for (char c : body.toCharArray()) {
                                sb.append(Integer.toUnsignedString(c, 10));
                            }
                            String token = Demo.summary(sb.toString());
                            if (!token.equals(webcode)) {
                                log.error("参数篡改" + body + " ,之后的summary: " + token + ",前端summary: " + webcode);
                                ctx.setSendZuulResponse(false);
                                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                                return false;
                            }
                        }
                        ctx.setRequest(requestWrapper);
                    }
                } else {
                    String str = request.getQueryString();
                    if (str != null) {
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
                            String token = Demo.summary(sb.toString());
                            if (!token.equals(webcode)) {
                                log.error("参数篡改" + jsonModel.toJSONString() + " ,之后的summary: " + token + ",前端summary: " + webcode);
                                ctx.setSendZuulResponse(false);
                                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        if ("true".equals(isUkey)) {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            String signStr = request.getHeader("signStr") != null ? request.getHeader("signStr") : "";
            String webcode = request.getHeader("summary") != null ? request.getHeader("summary") : "";
            if (StringUtils.isNoneBlank(signStr)) {
                boolean status = Demo.verify(webcode, signStr);
                if (!status) {
                    log.error("签名验证结果 - " + status);
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(HttpStatus.SC_PAYMENT_REQUIRED);
                    return false;
                }
            }
        }
        return true;

    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String s = String.format("%s >>> %s", request.getMethod(), request.getRequestURL().toString());
        log.info(s);
        return null;
    }
}
