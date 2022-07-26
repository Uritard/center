package com.yjh.gateway.configuration;

import com.yjh.gateway.common.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Map;

@Configuration
@Order
public class SecurityPropertie implements ApplicationRunner {
    //    @Resource
    //    private TSysParamService tSysParamService;

    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private CommonConfig commonConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //        this.getIsCode();
        //        this.getIsUkey();
        String fields = commonConfig.getFieldsNotValidat();
        if (StringUtils.isNotEmpty(fields)) {
            Constant.FIELDS_NOT_VALIDAT = fields.split(",");
        } else {
            Constant.FIELDS_NOT_VALIDAT = new String[0];
        }
    }

    /**
     * 获取是否验证参数篡改
     */
    private void getIsCode() {
        Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:isDecode");
        Constant.isDecode = map.get("content");
        // TSysParam tSysParam=tSysParamService.selectByPrimaryCode();
        // Constant.isDecode=tSysParam.getContent();
    }

    /**
     * 获取是否开启Ukey
     */
    private void getIsUkey() {
        Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:isUkey");
        Constant.isUkey = map.get("content");
        //        TSysParam tSysParam=tSysParamService.selectByPrimaryUkey();
        //        Constant.isUkey=tSysParam.getContent();
    }
}
