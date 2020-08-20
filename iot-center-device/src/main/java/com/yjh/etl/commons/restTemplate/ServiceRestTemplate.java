package com.yjh.etl.commons.restTemplate;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @Description
 * @Author tt
 * @Date 2019/9/24
 **/
@Component
public class ServiceRestTemplate extends RestTemplate {
    public ServiceRestTemplate() {
        super();
    }
}
