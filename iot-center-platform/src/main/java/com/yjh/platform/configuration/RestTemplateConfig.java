package com.yjh.platform.configuration;

import com.yjh.platform.common.aop.handler.ThrowErrorHandler;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author
 * @ClassName:
 * @Description: 公共配置注入
 * @date 2018-5-18 13:42
 */
@Configuration
public class RestTemplateConfig {

    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    public RestTemplate restTemplate() {
        ServiceRestTemplate restTemplate = new ServiceRestTemplate(clientHttpRequestFactory());
        //Response status code 4XX or 5XX to the client.
        restTemplate.setErrorHandler(new ThrowErrorHandler());
        return restTemplate;
    }

    @Bean
    public HttpClientConnectionManager poolingConnectionManager() {
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        // 连接池最大连接数
        poolingConnectionManager.setMaxTotal(300);
        // 每个主机的并发
        poolingConnectionManager.setDefaultMaxPerRoute(100);
        return poolingConnectionManager;
    }

    @Bean
    public HttpClientBuilder httpClientBuilder() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        //设置HTTP连接管理器
        httpClientBuilder.setConnectionManager(poolingConnectionManager());
        return httpClientBuilder;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClientBuilder().build());
        factory.setReadTimeout(15000);
        factory.setConnectTimeout(5000);
        factory.setConnectionRequestTimeout(5000);
        return factory;
    }

}
