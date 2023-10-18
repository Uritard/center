
package com.yjh.accessmeter.common.aop;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
  * @ClassName: MyMvcConfiguration
  * @Description: 拦截器配置
  * @author
  * @date 2018/5/16 16:29
  *
  */
@Configuration
public class MyMvcConfiguration extends WebMvcConfigurationSupport {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(new WebLogHandler())
                //.excludePathPatterns("/configuration/ui/**")
                .excludePathPatterns("/swagger-resources/**")
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/swagger-ui.html/**")
                .excludePathPatterns("/image/**");
    }
    /**
     * 配置静态访问资源
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //示例：js目录下是静态资源
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/js/");
        // swagger
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:/home/yjh_iot_center/iot-center-accessvideo-1.0.0/picture/");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false)    //设置是否是后缀模式匹配,即:/test.*
//                .setUseTrailingSlashMatch(false)     //设置是否自动后缀路径模式匹配,即：/test/
                .setUseRegisteredSuffixPatternMatch(true);  //开启路径后缀匹配
    }
    
}