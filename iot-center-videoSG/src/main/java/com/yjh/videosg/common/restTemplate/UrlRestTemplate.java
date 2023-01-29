/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.videosg.common.restTemplate;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @Description
 * @Author tt
 * @Date 2020/6/18
 **/
@Component
public class UrlRestTemplate extends RestTemplate {
    public UrlRestTemplate() {
        super();
    }

    public UrlRestTemplate(ClientHttpRequestFactory requestFactory) {
        super(requestFactory);
    }

}
