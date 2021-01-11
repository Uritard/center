package com.yjh.platform.common.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource(value = "classpath:province.properties", encoding = "utf-8")
public class RedisAndYxsjUtil {
    //登陆错误redis缓存最大失败次数
    @Value("${lot.login.num}")
    private int  loginNum;

    //锁定时间
    @Value("${lot.login.time}")
    private int loginTime;

    //密码有效时间
    @Value("${lot.yxsj.time}")
    private int yxsjTime;

}
