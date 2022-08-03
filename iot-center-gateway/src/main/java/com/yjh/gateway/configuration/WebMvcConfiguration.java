package com.yjh.gateway.configuration;

import com.yjh.gateway.common.handler.StaticResourceHandler;
import com.yjh.gateway.common.handler.WebLogHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author
 * @ClassName: MyMvcConfiguration
 * @Description: 拦截器配置
 * @date 2018/5/16 16:29
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private StaticResourceHandler staticResourceHandler;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new WebLogHandler())
            //.excludePathPatterns("/configuration/ui/**")
            .excludePathPatterns("/swagger-resources/**").excludePathPatterns("/webjars/**").excludePathPatterns("/swagger-ui.html/**")
            .excludePathPatterns("/imgs/**").excludePathPatterns("/files/**").excludePathPatterns("/assets/**");

        registry.addInterceptor(staticResourceHandler).addPathPatterns(StaticResourceHandler.RESOURCE_PATH);
    }

    /**
     * 配置静态访问资源
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // swagger
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

        // 静态资源配置
        registry.addResourceHandler("/imgs/**").addResourceLocations("file:/home/yjh_iot_center/iot-picture/");
        registry.addResourceHandler("/files/**").addResourceLocations("file:/home/yjh_iot_center/iot-files/");
        registry.addResourceHandler("/assets/img/**").addResourceLocations("file:/home/yjh_iot_center/iotCenter-web/dist/assets/img/");
        registry.addResourceHandler("/assets/images/**")
            .addResourceLocations("file:/home/yjh_iot_center/iotCenter-web/dist/assets/images/");
    }

}