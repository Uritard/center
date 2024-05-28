package com.yjh.imitator.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.yjh.imitator.constant.Constant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/20
 * @since [产品/模块版本] （可选）
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private VoiceProperties voiceProperties;

    /**
     * 定义全局默认时间序列化
     */
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 请求拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);

    }

    /**
     * 配置静态访问资源
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = voiceProperties.getVoicePath();
        try {
            File root = ResourceUtils.getFile(ResourceUtils.FILE_URL_PREFIX + path);
            if (!root.exists()) {
                root = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + path);
            }
            path = root.toURI().toString();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        registry.addResourceHandler(Constant.FILES_PREFIX + "**").addResourceLocations(path).resourceChain(false);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
            // .indentOutput(true)
            .dateFormat(new SimpleDateFormat(DATE_FORMAT))
            .simpleDateFormat(DATE_TIME_FORMAT)
            .serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
            .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
            .deserializers(new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
            .deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
            .modulesToInstall(new ParameterNamesModule());

        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jsonConverter) {
                jsonConverter.setObjectMapper(builder.build());
            }
        }
    }

}
