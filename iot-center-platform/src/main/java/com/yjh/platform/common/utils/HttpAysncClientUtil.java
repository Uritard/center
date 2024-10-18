package com.yjh.platform.common.utils;

import com.yjh.platform.common.quartz.SilentAlarmThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.client.util.HttpAsyncClientUtils;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author quzhihui
 * @date 2022/8/30 - 15:35
 */
@Slf4j
public class HttpAysncClientUtil {

    private static boolean stoplink = false;

    private static final Map<HttpHost, CloseableHttpAsyncClient> HTTP_ASYNC_CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * Initializes a long connection communication object
     */
    private static CloseableHttpAsyncClient httpCredentialsInit(String host, int port, String username, String password, boolean newLink) {
        HttpHost key = new HttpHost(host, port);

        if (newLink) {
            return HTTP_ASYNC_CLIENT_MAP.compute(key, (k, v) -> {
                HttpAsyncClientUtils.closeQuietly(v);
                CloseableHttpAsyncClient httpAsyncclient = httpClientInit(username, password);
                // Open the connection
                httpAsyncclient.start();
                return httpAsyncclient;
            });
        } else {
            return HTTP_ASYNC_CLIENT_MAP.computeIfAbsent(key, k -> {
                CloseableHttpAsyncClient httpAsyncclient = httpClientInit(username, password);
                // Open the connection
                httpAsyncclient.start();
                return httpAsyncclient;
            });
        }
    }

    private static CloseableHttpAsyncClient httpClientInit(String username, String password) {
        //摘要认证
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(10000).build();
        return HttpAsyncClients.custom().setDefaultRequestConfig(config).setDefaultCredentialsProvider(credentialsProvider).build();
    }

    //Long connection function
    public static void lonLink(String url, String user, String password, SilentAlarmThread alarmData) {
        log.info(url + "请求开始");
        stoplink = false;
        try {
            //设置回调函数
            FutureCallback<Boolean> callback = new FutureCallback<Boolean>() {
                @Override
                public void cancelled() {
                    log.info("cancelled: {}", url);
                }

                @Override
                public void completed(Boolean arg0) {
                    log.info("completed: {}", url);
                }

                @Override
                public void failed(Exception arg0) {
                    alarmData.stopAlarmGuard();
                    log.error("failed: {}", url, arg0);
                }
            };
            Map<String, Object> params = new LinkedHashMap<>(8);
            params.put("returnData", "success");
            URI uri = uriBuild(url, params);
            CloseableHttpAsyncClient httpAsyncclient = httpCredentialsInit(uri.getHost(), uri.getPort(), user, password, false);

            HttpGet get = new HttpGet(uri);

            // 创建连接，设置接收报警事件的回调函数
            // Url="http://"+ip+":"+port+"/ISAPI/Event/notification/alertStream";
            Future<Boolean> future = httpAsyncclient.execute(HttpAsyncMethods.create(get), new ResponseConsumer(alarmData), callback);
            alarmData.setRequestFuture(future);

            Boolean result = future.get();

            if (result != null && result) {
                log.info("Request successfully executed");
            } else {
                log.info("Request failed");
            }
            assert result != null;
            log.info(result.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (alarmData.reciveTime() == 0L && alarmData.retryNums() > 0) {
                log.info("连接失败，重试...");
                lonLink(url, user, password, alarmData);
            } else {
                alarmData.stopAlarmGuard();
            }
        }
    }

    private static URI uriBuild(String url, Map<String, Object> params) {
        URI uri;
        try {
            URIBuilder uriBuilder = new URIBuilder(URI.create(url));
            if (params != null && !params.isEmpty()) {
                params.forEach((k, v) -> uriBuilder.addParameter(k, v == null ? "" : String.valueOf(v)));
            }
            uri = uriBuilder.build();
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x.getMessage(), x);
        }
        return uri;
    }

    public static void stopLink() {
        stoplink = true;
    }

    @RequiredArgsConstructor
    static class ResponseConsumer extends AsyncCharConsumer<Boolean> {

        private final SilentAlarmThread alarmData;

        // Message type
        private String type;
        private StringBuilder chBuffer = new StringBuilder();

        @Override
        protected void onResponseReceived(final HttpResponse response) {
            log.info("onResponseReceived: {}", response.toString());

            // Determine the message type
            String tbuf = response.toString();
            if (tbuf.contains("multipart")) {
                type = "multipart";
            } else if (tbuf.contains("xml")) {
                type = "xml";
            } else if (tbuf.contains("json")) {
                type = "json";
            }
        }

        /**
         * Callback function to receive a message
         */
        @Override
        protected void onCharReceived(final CharBuffer buf, final IOControl ioctrl) throws IOException {

            while (buf.hasRemaining()) {
                char c = buf.get();
                chBuffer.append(c);
            }
            alarmData.reciveUpdate();
            // Parsing by message type
            alarmData.makeData(chBuffer);
            if (stoplink) {
                alarmData.stopAlarmGuard();
                buf.clear();
                this.close();
                chBuffer = new StringBuilder();
                log.info("stoplink == true");
                stoplink = false;
            }
        }

        @Override
        protected Boolean buildResult(final HttpContext context) {
            return Boolean.TRUE;
        }
    }
}