package com.yjh.gateway.common.authFilter;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.yjh.gateway.commons.utils.NumConstant;
import com.yjh.gateway.commons.utils.smUtil.Demo;
import com.yjh.gateway.commons.utils.smUtil.SM2Utils;
import com.yjh.gateway.commons.utils.smUtil.Util;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
public class zuulFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(zuulFilter.class);

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
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        MyRequestWrapper requestWrapper = null;
        if (request instanceof HttpServletRequest) {
            requestWrapper = new MyRequestWrapper(request);
            if ("POST".equals(request.getMethod().toUpperCase())) {
                String webcode = null;
                String signStr = null;
                String plainText = null;
                String body = requestWrapper.getBody();
                Map<String, Object> paramBodyMap = new LinkedHashMap<>();
                JSONObject jsonModel = new JSONObject(true);
                if (!StringUtils.isEmpty(body)) {
                    paramBodyMap = JSONObject.parseObject(body, LinkedHashMap.class);
                }
                for (Map.Entry<String, Object> param : paramBodyMap.entrySet()) {
                    if (!"webcode".equals(param.getKey()) && !"signStr".equals(param.getKey()) && !"plainText".equals(param.getKey())) {
                        jsonModel.put(param.getKey(), param.getValue());
                    }
                    if ("webcode".equals(param.getKey())) {
                        webcode = String.valueOf(param.getValue());
                    }
                    if ("signStr".equals(param.getKey())) {
                        signStr = String.valueOf(param.getValue());
                    }
                    if ("plainText".equals(param.getKey())) {
                        plainText = String.valueOf(param.getValue());
                    }
                }
                String token = Demo.summary(jsonModel.toJSONString());
                if (!token.equals(webcode)) {
                    log.error("参数篡改" + jsonModel.toJSONString() + " ,之后的token: " + token);
                    //throw new RuntimeException("参数篡改");
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                    return false;
                }
                if (!Demo.verify(plainText, signStr)) {
                    log.error("签名验证结果 - " + Demo.verify(plainText, signStr));
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                    return false;
                }
                ctx.setRequest(requestWrapper);
            } else {
                String tokenStr = request.getParameter("webcode");
                String signStr = request.getParameter("signStr");
                String plainText = request.getParameter("plainText");
                JSONObject jsonModel = new JSONObject(true);
                Map<String, String[]> params = new TreeMap<>(request.getParameterMap());
                for (Map.Entry<String, String[]> param : params.entrySet()) {
                    if (!"webcode".equals(param.getKey()) && !"signStr".equals(param.getKey()) && !"plainText".equals(param.getKey())) {
                        jsonModel.put(param.getKey(), ((String[]) param.getValue())[0]);
                    }
                }
                String token = Demo.summary(jsonModel.toJSONString());
                if (!token.equals(tokenStr)) {
                    log.error("参数篡改" + jsonModel.toJSONString() + " ,之后的token: " + token);
                    //throw new RuntimeException("参数篡改");
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                    return false;
                }
                if (!Demo.verify(plainText, signStr)) {
                    log.error("签名验证结果 - " + Demo.verify(plainText, signStr));
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                    return false;
                }
            }
        }
//        String contentType = request.getContentType();
//        if (null == contentType || !contentType.contains("application/json")) {
//            String tokenStr = request.getParameter("webcode");
//            String signStr = request.getParameter("signStr");
//            String plainText = request.getParameter("plainText");
//            JSONObject jsonModel = new JSONObject(true);
//            Map<String, String[]> params = new TreeMap<>(request.getParameterMap());
//            for (Map.Entry<String, String[]> param : params.entrySet()) {
//                if (!"webcode".equals(param.getKey())||!"signStr".equals(param.getKey())||!"plainText".equals(param.getKey())) {
//                    jsonModel.put(param.getKey(), ((String[]) param.getValue())[0]);
//                }
//            }
//            String token = Demo.summary(jsonModel.toJSONString());
//            if (!token.equals(tokenStr)) {
//                log.error("参数篡改" + jsonModel.toJSONString() + " ,之后的token: " + token);
//                //throw new RuntimeException("参数篡改");
//                ctx.setSendZuulResponse(false);
//                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
//                return false;
//            }
//            if (!Demo.verify(plainText, signStr)) {
//                log.error("签名验证结果 - " + Demo.verify(plainText, signStr));
//                ctx.setSendZuulResponse(false);
//                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
//                return false;
//            }
//        } else {
//            StringBuilder stringBuilder = new StringBuilder();
//            BufferedReader bufferedReader = null;
//            try {
//                if (null != request.getInputStream()) {
//                    bufferedReader = new BufferedReader(
//                            new InputStreamReader(request.getInputStream()));
//                    char[] charBuffer = new char[NumConstant.NUM_1024];
//                    int bytesRead = -1;
//                    while ((bytesRead =
//                            bufferedReader.read(charBuffer)) > 0) {
//                        stringBuilder.append(
//                                charBuffer, 0, bytesRead);
//                    }
//                } else {
//                    stringBuilder.append("");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            String token = request.getHeader("webcode") != null ? request.getHeader("webcode") : "";
//            String signStr = request.getHeader("signStr") != null ? request.getHeader("signStr") : "";
//            String plainText = request.getHeader("plainText") != null ? request.getHeader("plainText") : "";
//            log.info("进入参数校验--------------原始token : " + token);
//            String buliderString = stringBuilder.toString();
//            String tokens = Demo.summary(buliderString);
//            log.info("进入参数校验--------------后端解析token : " + tokens);
//            if (!tokens.equals(token)) {
//                ctx.setSendZuulResponse(false);
//                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
//                return false;
//            }
//            if (!Demo.verify(plainText, signStr)) {
//                log.error("签名验证结果 - " + Demo.verify(plainText, signStr));
//                ctx.setSendZuulResponse(false);
//                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
//                return false;
//            }
//            ctx.setResponseBody(buliderString);
//        }
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
