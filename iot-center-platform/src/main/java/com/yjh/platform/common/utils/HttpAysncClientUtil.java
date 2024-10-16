package com.yjh.platform.common.utils;

import com.yjh.platform.common.quartz.SilentAlarmThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
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
                return httpClientInit(username, password);
            });
        } else {
            return HTTP_ASYNC_CLIENT_MAP.computeIfAbsent(key, k -> httpClientInit(username, password));
        }
    }

    private static CloseableHttpAsyncClient httpClientInit(String username, String password) {
        //摘要认证
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return HttpAsyncClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();
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
            // Open the connection
            httpAsyncclient.start();

            HttpGet get = new HttpGet(uri);

            // Re3connect the query thread with a timeout on
            ThreadPoolUtil.COMMON_POOL.addThread(new ReConnect(httpAsyncclient, alarmData));

            // 创建连接，设置接收报警事件的回调函数
            // Url="http://"+ip+":"+port+"/ISAPI/Event/notification/alertStream";
            Future<Boolean> future = httpAsyncclient.execute(HttpAsyncMethods.create(get), new ResponseConsumer(alarmData), callback);

            Boolean result = future.get();

            if (result != null && result) {
                log.info("Request successfully executed");
            } else {
                log.info("Request failed");
            }
            assert result != null;
            log.info(result.toString());
        } catch (Exception e) {
            alarmData.stopAlarmGuard();
            log.error(e.getMessage(), e);
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

    @RequiredArgsConstructor
    static class ReConnect implements Runnable {
        private int reconnect = 3;
        private int timeout = 10000;

        private final CloseableHttpAsyncClient httpAsyncclient;
        private final SilentAlarmThread alarmData;

        @Override
        public void run() {
            try {
                while (alarmData.reciveTime() == 0L && reconnect > 0) {
                    if (timeout <= 0) {
                        log.info("reconnect == {}", --reconnect);
                        if (reconnect == 0) {
                            httpAsyncclient.close();
                            alarmData.stopAlarmGuard();
                        } else {
                            // Timeout reconnect, clear buffer, flag bit initialization, close connection, open connection
                            stoplink = false;
                            timeout = 10000;
                            httpAsyncclient.close();
                            httpAsyncclient.start();
                        }
                    } else {
                        Thread.sleep(1000);
                        timeout -= 1000;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            log.info("reconnect thread closed == {}", alarmData.reciveTime());
        }
    }
}