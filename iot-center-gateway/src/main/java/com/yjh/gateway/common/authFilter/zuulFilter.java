package com.yjh.gateway.common.authFilter;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.yjh.gateway.commons.utils.NumConstant;
import com.yjh.gateway.commons.utils.smUtil.Demo;
import com.yjh.gateway.commons.utils.smUtil.SM2Utils;
import com.yjh.gateway.commons.utils.smUtil.Util;
import org.apache.http.HttpStatus;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

@Component
public class zuulFilter extends ZuulFilter {
    private static final String pubk = "04673FC4F3D41C9470E32AABCB5A958E2CE528959F373D0F7AB2B82E65BF4DE8FB67716A269993585451888C8450E92A75A6C34EDFF748097BEAD8E41C2976E8AA";
    private static Logger log = LoggerFactory.getLogger(zuulFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String contentType = request.getContentType();
        if (null == contentType || !contentType.contains("application/json")) {
            String tokenStr = request.getParameter("webcode");
            JSONObject jsonModel = new JSONObject(true);
            Map<String, String[]> params = new TreeMap<>(request.getParameterMap());
            for (Map.Entry<String, String[]> param : params.entrySet()) {
                if (!"webcode".equals(param.getKey())) {
                    jsonModel.put(param.getKey(), ((String[]) param.getValue())[0]);
                }
            }
            StringBuilder sb = new StringBuilder();
            if (null != jsonModel.toJSONString()) {
                for (char c : jsonModel.toJSONString().toCharArray()) {
                    sb.append(Integer.toUnsignedString(c, 10));
                }
            }
            String token = Demo.summary(sb.toString());
            if (!token.equals(tokenStr)) {
                log.error("参数篡改" + jsonModel.toJSONString() + " ,之后的token: " + token);
                //throw new RuntimeException("参数篡改");
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                return false;
            }
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = null;
            try {
                if (null != request.getInputStream()) {
                    bufferedReader = new BufferedReader(
                            new InputStreamReader(request.getInputStream()));
                    char[] charBuffer = new char[NumConstant.NUM_1024];
                    int bytesRead = -1;
                    while ((bytesRead =
                            bufferedReader.read(charBuffer)) > 0) {
                        stringBuilder.append(
                                charBuffer, 0, bytesRead);
                    }
                } else {
                    stringBuilder.append("");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String token = request.getHeader("webcode") != null ? request.getHeader("webcode") : "";
            log.info("进入参数校验--------------原始token : " + token);
            String buliderString = stringBuilder.toString();
            StringBuilder sb = new StringBuilder();
            for (char c : buliderString.toCharArray()) {
                sb.append(Integer.toUnsignedString(c, 10));
            }
            String tokens = Demo.summary(sb.toString());
            log.info("进入参数校验--------------后端解析token : " + tokens);
            if (!tokens.equals(token)) {
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
                return false;
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
