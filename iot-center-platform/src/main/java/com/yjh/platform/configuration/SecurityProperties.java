package com.yjh.platform.configuration;

import com.yjh.platform.common.Constant;
import com.yjh.platform.module.user.entity.TSysParam;
import com.yjh.platform.module.user.service.TSysParamService;
import lombok.Data;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;

@Data
@Configuration
@Order
public class SecurityProperties implements ApplicationRunner {
    @Resource
    private TSysParamService tSysParamService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.getIsCode();
    }
//    @Value("${spring.security.isDecode}")
//    private String isDecode;

    /**
     * 获取是否加解密
     */
    private void getIsCode() {
        TSysParam tSysParam=tSysParamService.selectByPrimaryCode();
        Constant.isDecode=tSysParam.getContent();
    }
}
