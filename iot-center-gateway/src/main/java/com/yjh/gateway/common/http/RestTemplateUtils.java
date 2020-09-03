package com.yjh.gate.common.http;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class RestTemplateUtils {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<String> callInterface(String url, HttpMethod method, JSONObject bodyParam, Map<String, Object> urlParam) {
        // exchange方法的uriVariables参数不能为null，所以此处初始化
        if (urlParam == null) {
            urlParam = new HashMap<>();
        }
        // 请求消息头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> entity = new HttpEntity<JSONObject>(bodyParam, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class, urlParam);
        return response;
    }

    public ResponseEntity<String> callInterface(String url, HttpMethod method, JSONObject bodyParam,
                                                       Map<String, Object> urlParam, Map<String, Object> headerParam) {
        // exchange方法的uriVariables参数不能为null，所以此处初始化
        if (urlParam == null) {
            urlParam = new HashMap<>();
        }

        // 请求消息头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        for (Map.Entry<String, Object> entry : headerParam.entrySet()) {
            headers.set(entry.getKey(), entry.getValue().toString());
        }
        HttpEntity<JSONObject> entity = new HttpEntity<JSONObject>(bodyParam, headers);
        // 拼接url参数
        if (urlParam.size() > 0) {
            url = url + "?";
            for (Map.Entry<String, Object> entry : urlParam.entrySet()) {
                url = url + entry.getKey() + "={" + entry.getKey() + "}&";
            }
            url = url.substring(0, url.length() - 1);
        }
        ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class, urlParam);
        return response;
    }
}
