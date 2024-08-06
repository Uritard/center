package com.yjh.imitator.common;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private HttpUtil() {
    }

    private static OkHttpClient getClient() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
            logger.info("http请求参数：{}", message);
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        // 设置连接超时时间
        httpClientBuilder.connectTimeout(5, TimeUnit.SECONDS);
        // 设置读取超时时间
        httpClientBuilder.readTimeout(10, TimeUnit.SECONDS);
        // 设置连接池
        httpClientBuilder.connectionPool(new ConnectionPool(16, 5, TimeUnit.MINUTES));
        // OkHttp進行添加拦截器 loggingInterceptor
        httpClientBuilder.addInterceptor(logging);

        return httpClientBuilder.build();
    }

    /**
     * <p>发送GET请求
     *
     * @param url   GET请求地址(带参数)
     * @param param GET 请求参数容器
     * @return 与当前请求对应的响应内容字
     */
    public static String getUrl(String url, Map<String, Object> param) {
        OkHttpClient client = getClient();

        String responseStr = null;

        HttpUrl.Builder httpUrl = HttpUrl.parse(url).newBuilder();
        if (!CollectionUtils.isEmpty(param)) {
            param.forEach((k, v) -> {
                if (Objects.nonNull(v)) {
                    httpUrl.addQueryParameter(k, Objects.toString(v));
                }
            });

        }

        Request request = new Request.Builder().get().url(httpUrl.build()).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    responseStr = responseBody.string();
                }
            } else {
                responseStr = response.code() + " — " + response.message();
            }
        } catch (ConnectException e) {
            logger.error("连接服务失败: {}, {}", url, e.getMessage());
        } catch (IOException e) {
            logger.error("[{}]请求失败: {}", url, e.getMessage());
        }

        return responseStr;
    }

    /**
     * <p>发送POST请求
     *
     * @param url          POST请求地址
     * @param parameterMap POST请求参数容器
     * @return 与当前请求对应的响应内容字
     */
    public static String postUrl(String url, Map<String, Object> parameterMap) {
        OkHttpClient client = getClient();

        String responseStr = null;

        FormBody.Builder httpBody = new FormBody.Builder();
        if (!CollectionUtils.isEmpty(parameterMap)) {
            parameterMap.forEach((k, v) -> {
                if (Objects.nonNull(v)) {
                    httpBody.add(k, Objects.toString(v));
                }
            });
        }

        Request request = new Request.Builder().post(httpBody.build()).url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    responseStr = responseBody.string();
                }
            } else {
                responseStr = response.code() + " — " + response.message();
            }
        } catch (ConnectException e) {
            logger.error("连接服务失败: {}, {}", url, e.getMessage());
        } catch (IOException e) {
            logger.error("[{}]请求失败: {}", url, e.getMessage());
        }

        return responseStr;
    }

    public static String postUrl(String url, String jsonStr) {
        OkHttpClient client = getClient();

        String responseStr = null;

        RequestBody httpBody = RequestBody.create(StringUtils.defaultString(jsonStr), MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_VALUE));

        Request request = new Request.Builder().post(httpBody).url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    responseStr = responseBody.string();
                }
            } else {
                responseStr = response.code() + " — " + response.message();
            }
        } catch (ConnectException e) {
            logger.error("连接服务失败: {}, {}", url, e.getMessage());
            responseStr = "连接服务失败...";
        } catch (IOException e) {
            logger.error("[{}]请求失败: {}", url, e.getMessage());
            responseStr = e.getMessage();
        }

        return responseStr;
    }
}

