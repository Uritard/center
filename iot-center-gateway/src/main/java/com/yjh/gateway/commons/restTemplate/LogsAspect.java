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



/**
 * @Description
 * @Author tt
 * @Date 2019/7/30
 **/
@Aspect
@Component
public class LogsAspect {


    private static final String LOG_URL = "http://iot-center-share/sysLog/v1/add";


    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }

    public void post(MultiValueMap<String, Object> params) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                System.out.println("platformLogAdd...");
                serviceRestTemplate.postForObject(LOG_URL, params, String.class);
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }


}
