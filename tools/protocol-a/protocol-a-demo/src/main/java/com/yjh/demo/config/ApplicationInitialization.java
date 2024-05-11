package com.yjh.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/13
 * @since [产品/模块版本] （可选）
 */
@Configuration
@Slf4j
public class ApplicationInitialization implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {


        log.info("========= 数据初始化完成 ==========");
    }

}
