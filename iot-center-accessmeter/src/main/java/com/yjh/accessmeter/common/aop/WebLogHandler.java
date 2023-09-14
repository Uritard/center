
package com.yjh.accessmeter.common.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * @author
 * @ClassName: WebLogHandler
 * @Description: 拦截器、记录请求日志等
 * @date 2018/5/16 16:29
 */
@Component
public class WebLogHandler implements HandlerInterceptor {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(WebLogHandler.class);

    /**
     * 请求开始时间
     */
    ThreadLocal<Long> stThreadLocal = new ThreadLocal<Long>();

    /**
     * 进入controller之前
     *
     * @param request  request
     * @param response response
     * @param handler  handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        stThreadLocal.set(startTime);
        return true;
    }


    /**
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // TODO Auto-generated method stub

    }

    /**
     *  结果渲染之前
     * @param request
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // TODO Auto-generated method stub
        long startTime = stThreadLocal.get();
        stThreadLocal.remove();
        long costTime = System.currentTimeMillis() - startTime;
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        logger.info("本次请求信息如下: RequestURL:{}  RemoteAddr:{} Method:{}  RequestParams:{}  costTime: {} ms ", request.getRequestURL().toString(), request.getRemoteAddr(),
                handlerMethod.getBean().getClass().getName() + "." + handlerMethod.getMethod().getName(), buildParam(request), costTime);
    }


    private  String buildParam(HttpServletRequest request) {
        Enumeration<String> enumeration = request.getParameterNames();
        StringBuilder builder = new StringBuilder();
        while (enumeration.hasMoreElements()) {
            String paraName = enumeration.nextElement();
            builder.append(paraName).append(":").append(request.getParameter(paraName));
            if (enumeration.hasMoreElements()) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

}
