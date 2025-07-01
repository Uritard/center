package com.yjh.platform.configuration;

import cn.hutool.core.util.StrUtil;
import com.yjh.platform.common.utils.JSONUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import org.springframework.cloud.openfeign.encoding.HttpEncoding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/**
 * @Description
 * @Author tt
 * @Date 2019/9/24
 **/
@Configuration
public class FeignConfiguration implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(attributes)) {
            HttpServletRequest request = attributes.getRequest();
            Enumeration headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = (String)headerNames.nextElement();
                    String values = request.getHeader(name);
                    template.header(name, values);
                }
            }
        }
        template.header(HttpEncoding.ACCEPT_ENCODING_HEADER, HttpEncoding.GZIP_ENCODING, HttpEncoding.DEFLATE_ENCODING);
    }

    /**
     * 自定义openfeign解码器，解决返回数据gzip压缩问题，下面配置无效
     * feign.compression.response.enabled=true
     * feign.compression.response.useGzipDecoder=true
     */
    @Bean
    public Decoder feignDecoder() {
        return (response, type) -> {
            try (InputStream is = response.body().asInputStream()) {
                //判断响应内容是否经过GZIP压缩
                boolean isGzip = Optional.ofNullable(response.headers().get("content-encoding"))
                        .orElse(Collections.emptyList())
                        .stream()
                        .anyMatch(header -> StrUtil.containsIgnoreCase(header, "gzip"));
                //如果响应是GZIP压缩的,创建 GZIPInputStream解压,否则直接使用原始输入流
                try (InputStream inputStream = isGzip ? new GZIPInputStream(is) : is) {
                    return JSONUtil.getObjectMapper().readValue(
                            inputStream,
                            JSONUtil.getObjectMapper().getTypeFactory().constructType(type)
                    );
                }
            }
        };
    }
}