
package com.yjh.platform.common.handler;

import com.google.common.collect.Lists;
import com.yjh.platform.common.logs.interceptor.HttpLogInterceptor;
import com.yjh.platform.common.logs.interceptor.HttpTraceInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
  * @ClassName: MyMvcConfiguration
  * @Description: 拦截器配置
  * @author
  * @date 2018/5/16 16:29
  *
  */
@Configuration
public class MyMvcConfiguration implements WebMvcConfigurer {

    private final HttpLogInterceptor httpLogInterceptor;

    private final HttpTraceInterceptor httpTraceInterceptor;

    public MyMvcConfiguration(HttpLogInterceptor httpLogInterceptor, HttpTraceInterceptor httpTraceInterceptor) {
        this.httpLogInterceptor = httpLogInterceptor;
        this.httpTraceInterceptor = httpTraceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> swaggerPathList = Lists.newArrayList("/v2/api-docs", "/webjars/**", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html/**");
        registry.addInterceptor(httpTraceInterceptor).addPathPatterns("/**").excludePathPatterns(swaggerPathList);
        registry.addInterceptor(httpLogInterceptor).addPathPatterns("/**").excludePathPatterns(swaggerPathList);
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
    }
}