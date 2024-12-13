
package com.yjh.accesstcp.common.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;

/**
* @ClassName: WebLogHandler
* @Description: 拦截器、记录请求日志等
* @author
* @date 2018/5/16 16:29
*
*/
@Component
public class WebLogHandler implements HandlerInterceptor{

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
     * @param request  request
     * @param response response
     * @param handler handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //输出入参等信息
        printParams(request,response,handler);

        return true;
    }


    /**
     *
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
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // TODO Auto-generated method stub
        long st = stThreadLocal.get();
        long speedTime = System.currentTimeMillis() - st;
        logger.info("RequestId:{}, 本次请求耗时SpeedTime:{}ms ",st,speedTime);
    }

    /**
     *  输出入参等信息
     * @param request  request
     * @param response response
     * @param handler handler
     */
    private void printParams(HttpServletRequest request, HttpServletResponse response, Object handler){

        long st=System.currentTimeMillis();
        stThreadLocal.set(st);

        // 接收到请求，记录请求内容
        StringBuilder sb=new StringBuilder();

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        sb.append("本次请求信息如下:\r\n");
        sb.append("RequestId:").append(st).append("\r\n");
        sb.append("RequestURL:").append(request.getRequestURL().toString()).append("\r\n");
        sb.append("RemoteAddr : ").append(request.getRemoteAddr()).append("\r\n");
        sb.append("Method:")
            .append(handlerMethod.getBean().getClass().getName())
            .append(".")
            .append(handlerMethod.getMethod().getName())
            .append("\r\n");
        //获取所有请求参数：
        Enumeration<String> enu = request.getParameterNames();
        sb.append("RequestParams:[");
        while (enu.hasMoreElements()) {
            String paraName = enu.nextElement();
            sb.append(paraName).append(": ").append(request.getParameter(paraName));
            if(enu.hasMoreElements()){
                sb.append(",");
            }
        }
        sb.append("]\r\n");

        /*try {
            InputStream inputStream = request.getInputStream();
            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            // inputStream.reset();
            if (StringUtils.isNotEmpty(body)) {
                sb.append("RequestBody:").append(body).append("\r\n");
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }*/

        logger.info(sb.toString());
    }

}
