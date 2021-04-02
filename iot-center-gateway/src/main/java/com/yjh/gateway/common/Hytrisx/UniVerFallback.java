package com.yjh.gateway.common.Hytrisx;

import com.alibaba.fastjson.JSON;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


@Component
public class UniVerFallback implements FallbackProvider {

    /**
     * 指定 可以熔断拦截 哪些服务
     * @return
     */
    @Override
    public String getRoute() {
//        return "springcloud-ms-member";//指定 可熔断某个服务 服务名是配置文件中配置的各服务的serviceId
        return "*"; //指定    可熔断所有服务
    }

    /**
     * 指定  熔断后返回的定制化内容
     * @param route
     * @param cause
     * @return
     */
    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.OK;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return 200;
            }

            @Override
            public String getStatusText() throws IOException {
                return "OK";
            }

            @Override
            public void close() {

            }

            /**
             * 设置响应体
             * @return
             * @throws IOException
             */
            @Override
            public InputStream getBody() throws IOException {
                //TODO 此处可以做日志记录
                return new ByteArrayInputStream(JSON.toJSONString("服务正在启动中,请稍后").getBytes("UTF-8"));
            }

            /**
             * 设置响应头信息
             * @return
             */
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON_UTF8);//指定响应体 格式+编码方式
                return headers;
            }
        };
    }
}
