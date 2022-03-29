package com.yjh.accessvideo.commons.utils.http;


import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.commons.result.BusinessException;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpClientUtils {

    private static HttpClientUtils instance;

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private HttpClientUtils() {
    }

    public static synchronized HttpClientUtils getInstance() {
        if (instance == null) {
            instance = new HttpClientUtils();
        }
        return instance;
    }

    /**
     * <p>发送GET请求
     *
     * @param url       GET请求地址(带参数)
     * @param headerMap GET请求头参数容器
     * @return 与当前请求对应的响应内容字
     */
    public String doGet(String url, Map<String, Object> headerMap) {
        String content = null;
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpGet getMethod = new HttpGet(url);
            //头部请求信息
            if (headerMap != null) {
                Iterator iterator = headerMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();
                    getMethod.addHeader(entry.getKey().toString(), entry.getValue().toString());
                }
            }
            //发送get请求
            CloseableHttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
                    //读取内容
                    content = EntityUtils.toString(httpResponse.getEntity());
                } finally {
                    httpResponse.close();
                }
            } else {
                throw new BusinessException(httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (IOException ex) {
            throw new BusinessException(ex.getMessage());
        } finally {
            try {
                closeHttpClient(httpClient);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return content;
    }


    /**
     * <p>发送POST请求
     *
     * @param url          POST请求地址
     * @param headerMap    POST请求头参数容器
     * @param parameterMap POST请求参数容器
     * @return 与当前请求对应的响应内容字
     */
    public String doPost(String url, Map<String, Object> headerMap, Map<String, Object> parameterMap) {
        String content = null;
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpPost postMethod = new HttpPost(url);
//            postMethod.setHeader("Content-Type", "application/json;charset=utf-8");
//            postMethod.setHeader("Accept", "application/json;charset=utf-8");

            //头部请求信息
            if (headerMap != null) {
                Iterator iterator = headerMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();
                    postMethod.addHeader(entry.getKey().toString(), entry.getValue().toString());
                }
            }

            if (parameterMap != null) {
                Iterator iterator = parameterMap.keySet().iterator();
                List<NameValuePair> nvps = new ArrayList<>();
                while (iterator.hasNext()) {
                    String key = iterator.next().toString();
                    nvps.add(new BasicNameValuePair(key, parameterMap.get(key).toString()));
                }
                postMethod.setEntity(new UrlEncodedFormEntity(nvps));
            }

            CloseableHttpResponse httpResponse = httpClient.execute(postMethod);
            try {
                //读取内容
                content = EntityUtils.toString(httpResponse.getEntity());
            } finally {
                httpResponse.close();
            }

        } catch (IOException ex) {
            throw new BusinessException(ex.getMessage());
        } finally {
            try {
                closeHttpClient(httpClient);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return content;
    }

    public String getUrl(String url, String json) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Content-type", "application/json;charset=utf-8");
        httpGet.setHeader("Accept", "application/json");
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");
        return result;
    }

    public String postUrl(String url, String json) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    public CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    private void closeHttpClient(CloseableHttpClient client) throws IOException {
        if (client != null) {
            client.close();
        }
    }

    public static void get(String url) {
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = HttpClients.createDefault().execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


    private static final CloseableHttpClient httpclient;
    static{
        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(3000).build();
        httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }
    public static JSONObject sendGet(String url, Map<String, Object> params) throws ParseException, UnsupportedEncodingException, IOException{

        if(params !=null && !params.isEmpty()){

            List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());

            for (String key :params.keySet()){
                pairs.add(new BasicNameValuePair(key, params.get(key).toString()));
            }
            url +="?"+EntityUtils.toString(new UrlEncodedFormEntity(pairs), "UTF-8");
        }

        //请求超时时间设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                .setSocketTimeout(5000).build();

        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);

        CloseableHttpResponse response = httpclient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode !=200){
            httpGet.abort();
            throw new RuntimeException("HttpClient,error status code :" + statusCode);
        }
        HttpEntity entity = response.getEntity();
        String result = null;
        if (entity != null) {
            result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            response.close();
            JSONObject resultJson = JSONObject.parseObject(result);
            return resultJson;
        }else{
            return null;
        }
    }

    /**
     * 发送http delete请求
     */
    public static void httpDelete(String url, Map<String,String> headers){
//        if(encode == null){ encode = "utf-8"; }
//        String content = null;
        //since 4.3 不再使用 DefaultHttpClient
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
        HttpDelete httpdelete = new HttpDelete(url);
        //设置header
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) { httpdelete.setHeader(entry.getKey(),entry.getValue()); }
        }
//        CloseableHttpResponse httpResponse = null;
        try {
            closeableHttpClient.execute(httpdelete);
//            httpResponse = closeableHttpClient.execute(httpdelete);
//            HttpEntity entity = httpResponse.getEntity();
//            content = EntityUtils.toString(entity, encode);
//            response.setBody(content);
//            response.setHeaders(httpResponse.getAllHeaders());
//            response.setReasonPhrase(httpResponse.getStatusLine().getReasonPhrase());
//            response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) { e.printStackTrace();
//        }finally{
//            try { httpResponse.close(); } catch (IOException e) { e.printStackTrace(); }
        }
        //关闭连接、释放资源
        try { closeableHttpClient.close(); } catch (IOException e) { e.printStackTrace(); }
    }

}

