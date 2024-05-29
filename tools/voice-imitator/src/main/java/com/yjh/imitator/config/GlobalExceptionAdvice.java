/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.imitator.config;

import com.yjh.imitator.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {
    public GlobalExceptionAdvice() {

    }

    @ExceptionHandler({Exception.class})
    public Object handle(HttpServletRequest request, Exception e) {
        this.logger(request, e);
        String uri = request.getRequestURI();
        if (StringUtils.startsWithAny(uri, "/static/", "/public/", "/resources/", "/favicon.ico")
            || StringUtils.endsWithAny(uri, "wav", "js", "css", "html")) {
            // 请求指向静态资源
            return null;
        }
        Result ret = Result.ofError(400);
        log.error("系统错误返回响应: {}", ret);
        return ret;
    }

    private void logger(HttpServletRequest request, Exception e) {
        String contentType = request.getHeader("Content-Type");
        log.error("统一异常处理 uri: {} content-type: {} exception: {}", request.getRequestURI(), contentType, e);
    }
}
