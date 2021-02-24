package com.yjh.platform.configuration;

import com.yjh.platform.common.Constant;
import com.yjh.platform.module.user.entity.TSysParam;
import com.yjh.platform.module.user.service.TSysParamService;
import lombok.Data;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Map;

@Data
@Configuration
@Order
public class SecurityProperties implements ApplicationRunner {
    //    @Resource
//    private TSysParamService tSysParamService;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.getIsCode();
    }

    /**
     * 获取是否加解密
     */
    private void getIsCode() {
        Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
        Constant.isDecode =map.get("content");
      //  Constant.isDecode = tSysParam.getContent();
    }
}
