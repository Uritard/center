package com.yjh.platform.common.utils;

import com.yjh.platform.common.quartz.SilentAlarmThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

/**
 * @author quzhihui
 * @date 2022/8/30 - 15:35
 */
@Slf4j
public class HttpAysncClientUtil {

    public static CloseableHttpAsyncClient httpAsyncclient;
    private static int reconnect = 3;
    private static int timeout = 10000;
    private static boolean stoplink = false;
    private static boolean DataRecv = false;
    private static SilentAlarmThread alarmData;
    private static List<Character> chBuffer = new CopyOnWriteArrayList<>();

    //Initializes a long connection communication object
    public static void HttpAysncInit(String user, String password) {
        //摘要认证
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
        httpAsyncclient = HttpAsyncClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
    }

    //Long connection function
    public static void LonLink(String url, SilentAlarmThread data) {
        log.info(url + "请求开始");
        alarmData = data;
        stoplink = false;
        chBuffer.clear();
        try {
            //设置回调函数
            FutureCallback<Boolean> callback = new FutureCallback<Boolean>() {
                @Override
                public void cancelled() {
                    // TODO Auto-generated method stub
                    log.info("cancelled");
                }

                @Override
                public void completed(Boolean arg0) {
                    // TODO Auto-generated method stub
                    log.info("completed");
                }

                @Override
                public void failed(Exception arg0) {
                    // TODO Auto-generated method stub
                    alarmData.stopAlarmGuard(true);
                    log.error(arg0.getMessage(), arg0);
                    log.info("failed");
                }
            };
            // Open the connection
            httpAsyncclient.start();

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("returnData", "success"));
            URI uri = new URI(url + "?" + URLEncodedUtils.format(params, "utf-8"));
            HttpGet get = new HttpGet(uri);

            // Re3connect the query thread with a timeout on
            ReConnect rec = new ReConnect();
            Thread Rethread = new Thread(rec);
            Rethread.start();

            // 创建连接，设置接收报警事件的回调函数
            // Url="http://"+ip+":"+port+"/ISAPI/Event/notification/alertStream";
            Future<Boolean> future = httpAsyncclient.execute(
                    HttpAsyncMethods.create(get),
                    new ResponseConsumer(), callback);

            Boolean result = future.get();

            if (result != null && result) {
                log.info("Request successfully executed");
            } else {
                log.info("Request failed");
            }
            assert result != null;
            log.info(result.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(e.getMessage(), e);
        }
    }

    public static void StopLink() {
        stoplink = true;
        DataRecv = false;
    }

    static class ResponseConsumer extends AsyncCharConsumer<Boolean> {

        // Message type
        public String type;

        @Override
        protected void onResponseReceived(final HttpResponse response) {
            log.info("onResponseReceived" + response.toString());
            if (response.getStatusLine().getStatusCode() == 401) {
                Header[] headers = response.getHeaders("WWW-Authenticate");
                log.info("headers" + Arrays.toString(headers));
                String user = null;
                String password = null;
                for (Header header : headers) {
                    if (header.getName().equals("qop")) {
                        user = header.getValue();
                    }
                    if (header.getName().equals("nonce")) {
                        password = header.getValue();
                    }
                }
                HttpAysncClientUtil.HttpAysncInit(user, password);
            }
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

        // Callback function to receive a message
        @Override
        protected void onCharReceived(final CharBuffer buf, final IOControl ioctrl) throws IOException {
            DataRecv = true;
            // Parsing by message type
            if (type.equals("multipart")) {
                for (int i = 0; i < buf.length(); i++) {
                    chBuffer.add(buf.charAt(i));
                }
                alarmData.makeData(chBuffer);
            } else if (type.equals("xml")) {
                for (int i = 0; i < buf.length(); i++) {
                    chBuffer.add(buf.charAt(i));
                }
                alarmData.makeData(chBuffer);
            } else if (type.equals("json")) {
                for (int i = 0; i < buf.length(); i++) {
                    chBuffer.add(buf.charAt(i));
                }
                alarmData.makeData(chBuffer);
            }
            if (stoplink) {
                buf.clear();
                this.close();
                chBuffer.clear();
                alarmData.stopAlarmGuard(false);
                log.info("stoplink == true");
                stoplink = false;
            }
        }

        @Override
        protected Boolean buildResult(final HttpContext context) {
            return Boolean.TRUE;
        }
    }

    static class ReConnect implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                if (!DataRecv) {
                    if (timeout == 0) {
                        if (reconnect == 0) {
                            log.info("reconnect == 0");
                            httpAsyncclient.close();
                            alarmData.stopAlarmGuard(false);
                        } else {
                            // Timeout reconnect, clear buffer, flag bit initialization, close connection, open connection
                            chBuffer.clear();
                            stoplink = false;
                            timeout = 100000;
                            httpAsyncclient.close();
                            log.info("reconnect != 0");
                            httpAsyncclient.start();
                            reconnect--;
                        }
                    } else {
                        Thread.sleep(10);
                        timeout -= 10;
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                log.error(e.getMessage(), e);
            }

        }
    }
}