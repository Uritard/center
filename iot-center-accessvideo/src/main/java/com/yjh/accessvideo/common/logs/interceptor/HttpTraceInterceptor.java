package com.yjh.accessvideo.common.logs.interceptor;

import com.yjh.accessvideo.common.logs.track.HttpTracing;
import com.yjh.accessvideo.commons.utils.http.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lichensi
 * @date 2020/12/9 16:27
 */
@Configuration
@Slf4j
public class HttpTraceInterceptor implements HandlerInterceptor, HttpTracing {

    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        try {
            final String routeTraceId = request.getHeader(Tags.TRACE_ID.getName());
            if (StringUtils.isBlank(routeTraceId)) {
                this.newRoute();
            } else {
                this.setRoute(routeTraceId);
            }

            final String httpRequestId = request.getHeader(Tags.REQUEST_ID.getName());
            if (StringUtils.isNotBlank(httpRequestId)) {
                this.setHttpRouteRequestId(httpRequestId);
            }

            final String httpRequestIp = request.getHeader(Tags.REQUEST_IP.getName());
            if (StringUtils.isNotBlank(httpRequestIp)) {
                this.setHttpRouteRequestIp(httpRequestIp);
            } else {
                this.setHttpRouteRequestIp(IPUtil.getRemoteIP(request));
            }

            final String httpRequestMethod = request.getHeader(Tags.REQUEST_METHOD.getName());
            if (StringUtils.isNotBlank(httpRequestMethod)) {
                this.setHttpRouteRequestMethod(httpRequestMethod);
            } else {
                this.setHttpRouteRequestMethod(request.getMethod());
            }

            final String httpRequestOrigin = request.getHeader(Tags.REQUEST_ORIGIN.getName());
            if (StringUtils.isNotBlank(httpRequestOrigin)) {
                this.setHttpRouteRequestOrigin(httpRequestOrigin);
            } else {
                this.setHttpRouteRequestOrigin(request.getScheme() + "://" + request.getHeader("host"));
            }

            final String httpRequestPath = request.getHeader(Tags.REQUEST_PATH.getName());
            if (StringUtils.isNotBlank(httpRequestPath)) {
                this.setHttpRouteRequestPath(httpRequestPath);
            } else {
                this.setHttpRouteRequestPath(request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("[yjh-track] [链路追踪] ", ex);
        }
        return true;
    }

    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex) {
        this.removeRoute();
    }
}
