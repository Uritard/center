package com.yjh.gateway.common.authFilter;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.yjh.gateway.common.Constant;
import com.yjh.gateway.commons.utils.smUtil.Demo;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
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

        Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:isDecode");
        String isDecode =map.get("content");
        Map<String,String> uKeymap= redisTemplate.opsForHash().entries("t_sys_param:isUkey");
        String isUkey =uKeymap.get("content");
        if("true".equals(isDecode)) {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            MyRequestWrapper requestWrapper = null;
            if (request instanceof HttpServletRequest) {
                requestWrapper = new MyRequestWrapper(request);
                if ("POST".equals(request.getMethod().toUpperCase())||"PUT".equals(request.getMethod().toUpperCase())) {
                    String webcode = null;
                    String body = requestWrapper.getBody();
                    Map<String, Object> paramBodyMap = new LinkedHashMap<>();
                    JSONObject jsonModel = new JSONObject(true);
                    if (!StringUtils.isEmpty(body)) {
                        paramBodyMap = JSONObject.parseObject(body, LinkedHashMap.class);
                    }
                    for (Map.Entry<String, Object> param : paramBodyMap.entrySet()) {
                        if (!"summary".equals(param.getKey()) && !"signStr".equals(param.getKey()) && !"plainText".equals(param.getKey())) {
                            jsonModel.put(param.getKey(), param.getValue());
                        }
                        if ("summary".equals(param.getKey())) {
                            webcode = String.valueOf(param.getValue());
                        }
                    }
                    String token = Demo.summary(URLEncoder.encode(jsonModel.toJSONString()));
                    if (!token.equals(webcode)) {
                        log.error("参数篡改" + jsonModel.toJSONString() + " ,之后的summary: " + token+",前端summary"+webcode);
                        ctx.setSendZuulResponse(false);
                        ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                        return false;
                    }
                    ctx.setRequest(requestWrapper);
                } else {
                    String tokenStr = request.getParameter("summary");
                    JSONObject jsonModel = new JSONObject(true);
                    Map<String, String[]> params = new TreeMap<>(request.getParameterMap());
                    for (Map.Entry<String, String[]> param : params.entrySet()) {
                        if (!"summary".equals(param.getKey()) && !"signStr".equals(param.getKey()) && !"plainText".equals(param.getKey())) {
                            jsonModel.put(param.getKey(), ((String[]) param.getValue())[0]);
                        }
                    }
                    if (jsonModel.size() != 0) {
                        String token = Demo.summary(URLEncoder.encode(jsonModel.toJSONString()));
                        if (!token.equals(tokenStr)) {
                            log.error("参数篡改" + jsonModel.toJSONString() + " ,之后的summary: " + token+",前端summary"+tokenStr);
                            ctx.setSendZuulResponse(false);
                            ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                            return false;
                        }
                    }
                }
            }
        }
        if("true".equals(isUkey)){
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            MyRequestWrapper requestWrapper = null;
            if (request instanceof HttpServletRequest) {
                requestWrapper = new MyRequestWrapper(request);
                if ("POST".equals(request.getMethod().toUpperCase())||"PUT".equals(request.getMethod().toUpperCase())) {
                    String signStr = null;
                    String plainText = null;
                    String body = requestWrapper.getBody();
                    Map<String, Object> paramBodyMap = new LinkedHashMap<>();
                    if (!StringUtils.isEmpty(body)) {
                        paramBodyMap = JSONObject.parseObject(body, LinkedHashMap.class);
                    }
                    for (Map.Entry<String, Object> param : paramBodyMap.entrySet()) {
                        if ("signStr".equals(param.getKey())) {
                            signStr = String.valueOf(param.getValue());
                        }
                        if ("plainText".equals(param.getKey())) {
                            plainText = String.valueOf(param.getValue());
                        }
                    }
                    boolean status=Demo.verify(plainText, signStr);
                    if (!status) {
                        log.error("签名验证结果 - " + status);
                        ctx.setSendZuulResponse(false);
                        ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                        return false;
                    }
                    ctx.setRequest(requestWrapper);
                } else {
                    String signStr = request.getParameter("signStr");
                    String plainText = request.getParameter("plainText");
                    boolean status = Demo.verify(plainText, signStr);
                    if (!status) {
                        log.error("签名验证结果 - " + status);
                        ctx.setSendZuulResponse(false);
                        ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                        return false;
                    }
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
