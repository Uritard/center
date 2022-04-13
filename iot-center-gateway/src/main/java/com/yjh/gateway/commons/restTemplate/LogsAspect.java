package com.yjh.gateway.commons.restTemplate;



import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;


/**
 * @Description
 * @Author tt
 * @Date 2019/7/30
 **/
@Aspect
@Component
public class LogsAspect {


    private static final String LOG_URL = "http://iot-center-platform/sysUser/v1/logoutGateway";

    private static final String ADD_LOG = "http://iot-center-share/sysLog/v1/add";


    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }

    public void post(MultiValueMap<String, Object> params) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                System.out.println("gateWayLogAdd...");
                serviceRestTemplate.postForObject(ADD_LOG, params, String.class);
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    public void loginLogsSend(HttpServletRequest request,String ip, String type, String title, String content, String userName, String userIds, Integer state){
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.set("logType", type);
        param.set("ip", ip);
        param.set("title", title);
        param.set("state", state);
        param.set("userId", userIds);
        param.set("userName", userName);
        param.set("requestOrigin", request.getRequestURL());
        param.set("requestPath", request.getRequestURI());
        param.set("requestMethod", request.getMethod());
        param.set("content", content);
        LogsAspect logsAspects = new LogsAspect();
        logsAspects.post(param);
    }

}
