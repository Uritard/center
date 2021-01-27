package com.yjh.platform.common.logs.interceptor;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.OperateLogDto;
import com.yjh.platform.common.logs.OperateLogEvent;
import com.yjh.platform.common.logs.track.HttpTracing;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author lichensi
 * @date 2020/12/9 15:42
 */
@Slf4j
@Configuration
public class HttpLogInterceptor implements HandlerInterceptor {

    private static final String IGNORE_CONTENT_TYPE = "multipart/form-data";

    private static final Set<String> IGNORE_PATH_SET = new HashSet<>();

    private static final Gson GSON = new GsonBuilder().create();

    private static final ThreadLocal<HttpLog> LOG_CONTEXT = new TransmittableThreadLocal<>();

    private final ApplicationContext applicationContext;

    public HttpLogInterceptor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化HTTP日志
     *
     * @return {@link HttpLog}
     */
    private static HttpLog initHttpLog(String path) {
        HttpLog httpLog = LOG_CONTEXT.get();
        if (Objects.isNull(httpLog)) {
            httpLog = new HttpLog();
            httpLog.setBeginTime(System.currentTimeMillis());
            LOG_CONTEXT.set(httpLog);
        }
        httpLog.setPath(path);
        return httpLog;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (IGNORE_PATH_SET.contains(path)) {
            return true;
        }
        initHttpLog(path);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String path = request.getRequestURI();
        if(handler instanceof HandlerMethod) {
            HandlerMethod h = (HandlerMethod)handler;
            Method targetMethod = h.getMethod();

            if(targetMethod.isAnnotationPresent(Logs.class)){
                Logs logAnnotation = targetMethod.getAnnotation(Logs.class);
                OperateLogDto operateLogDto = null;
                if(Objects.isNull(ex)){
                    //此处可以判断返回结果result中的是否成功标识，记录正确或者错误日志
                    operateLogDto = OperateLogDto.buildSuccessLog(logAnnotation);
                } else {
                    operateLogDto = OperateLogDto.buildExceptionLog(logAnnotation);
                }
                applicationContext.publishEvent(new OperateLogEvent(operateLogDto, HttpTracing.getHttpRouteTrackInfo() ,this));
            }
        }

        if (IGNORE_PATH_SET.contains(path) || StringUtils.equals(IGNORE_CONTENT_TYPE, request.getContentType())) {
            LOG_CONTEXT.remove();
            return;
        }

        HttpLog httpLog = LOG_CONTEXT.get();
        if (Objects.nonNull(httpLog)) {
            log.info("Http {} Request {} >>> Response {}, takes {}ms.", request.getMethod(), path,
                    GSON.toJson(httpLog), httpLog.spendTime());
            LOG_CONTEXT.remove();
        }
    }

    @Getter
    @Setter
    private static class HttpLog {

        private String path;

        private String origin;

        private String host;

        private long beginTime;

        private long endTime;

        private Object requestBody;

        private Object responseBody;

        public long spendTime() {
            return endTime - beginTime;
        }
    }

    @ControllerAdvice
    static class HttpLogResponseAdvice implements ResponseBodyAdvice<Object> {
        @Override
        public boolean supports(MethodParameter parameter, Class aClass) {
            return true;
        }

        @Override
        public Object beforeBodyWrite(Object body, MethodParameter parameter, MediaType mediaType, Class aClass,
                                      ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
            HttpLog httpLog = LOG_CONTEXT.get();
            if (Objects.nonNull(httpLog)) {
                httpLog.setEndTime(System.currentTimeMillis());
                httpLog.setResponseBody(body);
            }
            return body;
        }
    }

    @ControllerAdvice
    static class SysOpLogResponseAdvice implements ResponseBodyAdvice<Object> {
        @Override
        public boolean supports(MethodParameter parameter, Class aClass) {
            return true;
        }

        @Override
        public Object beforeBodyWrite(Object body, MethodParameter parameter, MediaType mediaType, Class aClass,
                                      ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
            HttpLog httpLog = LOG_CONTEXT.get();
            if (Objects.nonNull(httpLog)) {
                httpLog.setEndTime(System.currentTimeMillis());
                httpLog.setResponseBody(body);
            }
            return body;
        }
    }

    @ControllerAdvice
    static class HttpLogRequestAdvice implements RequestBodyAdvice {

        @Override
        public boolean supports(MethodParameter parameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
            return true;
        }

        @Override
        public HttpInputMessage beforeBodyRead(HttpInputMessage message, MethodParameter parameter, Type type,
                                               Class<? extends HttpMessageConverter<?>> aClass) {
            return message;
        }

        @Override
        public Object afterBodyRead(Object body, HttpInputMessage message, MethodParameter parameter, Type type,
                                    Class<? extends HttpMessageConverter<?>> aClass) {
            HttpLog httpLog = LOG_CONTEXT.get();
            if (Objects.nonNull(httpLog)) {
                HttpHeaders httpHeaders = message.getHeaders();
                httpLog.setHost(httpHeaders.getHost().getHostString());
                httpLog.setOrigin(httpHeaders.getOrigin());
                httpLog.setRequestBody(body);
            }
            return body;
        }

        @Override
        public Object handleEmptyBody(Object body, HttpInputMessage message, MethodParameter parameter, Type type,
                                      Class<? extends HttpMessageConverter<?>> aClass) {
            return body;
        }
    }
}
