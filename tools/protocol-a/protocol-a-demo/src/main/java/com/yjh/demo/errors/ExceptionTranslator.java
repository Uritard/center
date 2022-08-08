package com.yjh.demo.errors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import java.util.concurrent.TimeoutException;

/**
 * Copyright (c) 2021 Yijiahe Technology Co., Ltd. All rights reserved.
 *
 * @author wuyu
 * @since 3/18/21
 */
//全局异常处理
@EnableAutoConfiguration(exclude = ErrorMvcAutoConfiguration.class)
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling {

    //非法参数异常
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Problem> handlerIllegalArgumentException(IllegalArgumentException exception, NativeWebRequest request) {
        Problem problem = Problem.builder()
                .withStatus(Status.BAD_REQUEST)
                .with("message", exception.getMessage())
                .build();
        return create(exception, problem, request);
    }

    //方法参数类型不匹配异常
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Problem> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception, NativeWebRequest request) {
        Problem problem = Problem.builder()
                .withStatus(Status.METHOD_NOT_ALLOWED)
                .with("message", "方法参数类型不匹配")
                .build();
        return create(exception, problem, request);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<Problem> handleTimeoutException(TimeoutException exception, NativeWebRequest request) {
        Problem problem = Problem.builder()
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .with("message", exception.getMessage())
                .build();
        return create(exception, problem, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleException(Exception exception, NativeWebRequest request) {
        Problem problem = Problem.builder()
                .withStatus(Status.BAD_REQUEST)
                .with("message", "请求处理异常: " + exception.getMessage())
                .build();
        return create(exception, problem, request);
    }
}
