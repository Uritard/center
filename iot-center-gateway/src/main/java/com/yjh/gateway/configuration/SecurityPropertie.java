package com.yjh.gateway.configuration;


import com.yjh.gateway.common.Constant;
import com.yjh.gateway.module.gateway.entity.TSysParam;
import com.yjh.gateway.module.gateway.service.TSysParamService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Configuration
@Order
public class SecurityPropertie implements ApplicationRunner {
    @Resource
    private TSysParamService tSysParamService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.getIsCode();
    }

    /**
     * 获取是否验证参数篡改
     */
    private void getIsCode() {
        TSysParam tSysParam=tSysParamService.selectByPrimaryCode();
        Constant.isDecode=tSysParam.getContent();
    }

    /**
     * 获取是否开启Ukey
     */
    private void getIsUkey() {
        TSysParam tSysParam=tSysParamService.selectByPrimaryUkey();
        Constant.isUkey=tSysParam.getContent();
    }
}
