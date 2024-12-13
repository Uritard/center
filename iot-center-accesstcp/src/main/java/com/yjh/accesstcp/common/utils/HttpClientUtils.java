package com.yjh.accesstcp.common.utils;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accesstcp.commons.result.BusinessException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * http 连接
 * @author Chenfei
 */
public class HttpClientUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtils.class);

    private static volatile HttpClientUtils instance;
    private static CloseableHttpClient client;
    private static final Map<AuthScope, HttpContext> HTTP_CONTEXT_MAP = new ConcurrentHashMap<>();
    /**
     * 存储所有的 AuthScope，用来对单个服务器进行请求加锁
     */
    private static final Map<HttpHost, AuthScope> AUTH_SCOPE_LOCK_MAP = new ConcurrentHashMap<>();
    private static final Map<HttpHost, Supplier<?>> TOKEN_AUTH_FUNCTION_MAP = new ConcurrentHashMap<>();

    private HttpClientUtils() {
        RequestConfig requestConfig =
            RequestConfig.custom().setConnectTimeout(3000).setConnectionRequestTimeout(5000).setSocketTimeout(15000).build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(320);
        connManager.setDefaultMaxPerRoute(32);

        client = HttpClients.custom().setConnectionManager(connManager).setDefaultRequestConfig(requestConfig).build();
    }

    public static HttpClientUtils getInstance() {
        if (instance == null) {
            synchronized (HttpClientUtils.class) {
                if (instance == null) {
                    LOGGER.info("初始化instance");
                    instance = new HttpClientUtils();
                }
            }
        }
        return instance;
    }

    /**
     * get 请求
     */
    public String doGet(String url) throws IOException {
        return doGet(url, null, null, null, AuthType.TOKEN);
    }

    /**
     * get 请求
     */
    public String doGet(String url, Map<String, Object> params) throws IOException {
        return doGet(url, params, null, null, AuthType.TOKEN);
    }

    /**
     * get 请求
     */
    public String doGet(String url, Map<String, Object> params, String username, String password) throws IOException {
        return doGet(url, params, username, password, AuthType.DIGEST);
    }

    /**
     * get 请求
     * @param url      请求地址
     * @param params   请求参数
     * @param username 用户名
     * @param password 密码
     * @param authType 鉴权方式
     * @return 接口请求结果
     */
    public String doGet(String url, Map<String, Object> params, String username, String password, AuthType authType) throws IOException {
        URI uri = uriBuild(url, params);
        // 获取默认 context
        HttpContext context = credentialsContext(uri.getHost(), uri.getPort(), username, password, authType);

        HttpGet httpGet = new HttpGet(uri);
        return execute(httpGet, context);
    }

    /**
     * 构建 url，get 方式请求
     */
    private URI uriBuild(String url, Map<String, Object> params) {
        URI uri;
        try {
            URIBuilder uriBuilder = new URIBuilder(URI.create(url));
            if (MapUtils.isNotEmpty(params)) {
                params.forEach((k, v) -> uriBuilder.addParameter(k, v == null ? "" : String.valueOf(v)));
            }
            uri = uriBuilder.build();
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x.getMessage(), x);
        }
        return uri;
    }

    /**
     * post 请求，参数使用 json 字符串
     */
    public String doPost(String url, String json) throws IOException {
        return doPost(url, json, null, null, AuthType.TOKEN);
    }

    /**
     * post 请求，参数使用 json 字符串
     */
    public String doPost(String url, String json, String username, String password) throws IOException {
        return doPost(url, json, username, password, AuthType.DIGEST);
    }

    /**
     * post 请求，参数使用 json 字符串
     * @param url 请求地址
     * @param json 传参，json 字符串
     * @param username 用户名
     * @param password 密码
     * @param authType 鉴权方式
     * @return 返回字符串，json 格式
     * @throws IOException 异常
     */
    public String doPost(String url, String json, String username, String password, AuthType authType) throws IOException {
        URI uri = URI.create(url);

        // 获取默认 context
        HttpContext context = credentialsContext(uri.getHost(), uri.getPort(), username, password, authType);
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

        return execute(httpPost, context);
    }

    /**
     * post 请求，传入 params 则表示使用 Multipart 方式传参，可传入文件
     */
    public String doPost(String url, Map<String, Object> params) throws IOException {
        return doPost(url, params, null, null, AuthType.TOKEN);
    }

    /**
     * post 请求，传入 params 则表示使用 Multipart 方式传参，可传入文件
     */
    public String doPost(String url, Map<String, Object> params, String username, String password) throws IOException {
        return doPost(url, params, username, password, AuthType.DIGEST);
    }

    /**
     * post 请求，传入 params 则表示使用 Multipart 方式传参，可传入文件
     * @param url      路径
     * @param params   参数
     * @param username 用户名
     * @param password 密码
     * @param authType 鉴权方式
     * @return 接口返回结果
     * @throws IOException 异常
     */
    public String doPost(String url, Map<String, Object> params, String username, String password, AuthType authType) throws IOException {
        URI uri = URI.create(url);

        HttpContext context = credentialsContext(uri.getHost(), uri.getPort(), username, password, authType);
        HttpPost httpPost = new HttpPost(uri);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof File) {
                File file = (File)value;
                builder.addBinaryBody(key, file, ContentType.MULTIPART_FORM_DATA, file.getName());
            } else if (value instanceof String) {
                builder.addTextBody(key, (String)value, ContentType.TEXT_PLAIN);
            } else {
                // 将Integer类型的参数转换为字符串并添加
                builder.addTextBody(key, Objects.toString(value, ""));
            }
        }
        httpPost.setEntity(builder.build());
        // httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());

        return execute(httpPost, context);
    }

    /**
     * 配置鉴权规则，将鉴权规则缓存在
     * authType 不同方式采用不同的认证
     */
    public HttpContext credentialsContext(String host, int port, String username, String password, AuthType authType) {
        // 没有用户名密码且不是 token认证则直接返回 null
        if (StringUtils.isAnyEmpty(username, password) && authType != AuthType.TOKEN) {
            return null;
        }
        // ip和port确定唯一的 scope，scope 不使用手动创建而使用 AUTH_SCOPE_LOCK_MAP 是为了保存 scop，为了加锁，给每一个ip+port指向唯一地址加锁
        HttpHost key = new HttpHost(host, port);
        AuthScope scope = AUTH_SCOPE_LOCK_MAP.computeIfAbsent(key, AuthScope::new);

        // 创建HttpClientContext实例，使用自定义 context，这样才能保存前后两次请求的认证信息缓存，不必每次都生成认证信息
        HttpClientContext context = HttpClientContext.create();
        HttpContext ctx = authType != AuthType.DIGEST_ONE ? HTTP_CONTEXT_MAP.putIfAbsent(scope, context) : null;

        switch (authType) {
            case TOKEN:
                // 使用 token 方式鉴权
                if (ctx == null) {
                    // 获取 token 回调
                    Supplier<?> supplier = TOKEN_AUTH_FUNCTION_MAP.get(key);
                    if (supplier != null) {
                        // 执行回调，更新 HTTP_CONTEXT_MAP 存储的 context
                        String token = (String)supplier.get();
                        context.setUserToken(token);
                        ctx = HTTP_CONTEXT_MAP.get(scope);
                    }
                }
                break;
            case DIGEST:
            case DIGEST_ONE:
                // 判断用户名和密码是否变化
                boolean credentialsChange = credentialsChange(ctx, scope, username, password);
                if (ctx == null || credentialsChange) {
                    // 配置认证，digest 认证
                    Credentials creds = new UsernamePasswordCredentials(username, password);
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(scope, creds);

                    context.setCredentialsProvider(credsProvider);
                    ctx = context;
                }
                break;
            default:
                if (ctx == null) {
                    ctx = context;
                }
                break;
        }
        return ctx;

    }

    /**
     * 判断用户名和密码是否有变化
     */
    private boolean credentialsChange(HttpContext context, AuthScope scope, String username, String password) {
        if (context == null) {
            return true;
        }

        HttpClientContext ctx = HttpClientContext.adapt(context);

        CredentialsProvider credsProvider = ctx.getCredentialsProvider();
        Credentials credentials = credsProvider.getCredentials(scope);
        if (credentials == null) {
            return true;
        }
        return !(new EqualsBuilder().append(username, credentials.getUserPrincipal().getName())
            .append(password, credentials.getPassword())
            .isEquals());
    }

    /**
     * 登录鉴权
     * @param url 登录请求url
     * @param params 登录请求参数
     * @param heardAttrs 登录请求头
     * @param supplier 回调，当未鉴权时，如何触发登录回调
     * @return 登录返回参数
     * @param <T> 泛型
     * @throws IOException 登录异常
     */
    public <T> String tokenAuth(String url, Map<String, Object> params, Map<String, String> heardAttrs, Supplier<T> supplier)
        throws IOException {
        URI uri = uriBuild(url, params);

        HttpGet httpGet = new HttpGet(uri);

        // 请求头部信息
        if (MapUtils.isNotEmpty(heardAttrs)) {
            heardAttrs.forEach(httpGet::addHeader);
        }
        // 存储回调信息
        TOKEN_AUTH_FUNCTION_MAP.putIfAbsent(new HttpHost(uri.getHost(), uri.getPort()), supplier);
        // 执行登录请求
        return execute(httpGet, HttpClientContext.create());
    }

    public void updateContext(String url, HttpClientContext context) {
        URI uri = URI.create(url);
        HttpHost key = new HttpHost(uri.getHost(), uri.getPort());
        AuthScope scope = AUTH_SCOPE_LOCK_MAP.computeIfAbsent(key, AuthScope::new);
        HTTP_CONTEXT_MAP.put(scope, context);
    }

    /**
     * 重置鉴权信息，大华相机不支持缓存鉴权，必须每次返回401重新鉴权
     */
    public synchronized HttpContext resetCredentialsContext(HttpUriRequest request, HttpContext context) {
        HttpClientContext ctx = HttpClientContext.adapt(context);

        String host = request.getURI().getHost();
        int port = request.getURI().getPort();
        HttpHost key = new HttpHost(host, port);
        AuthScope scope = AUTH_SCOPE_LOCK_MAP.computeIfAbsent(key, AuthScope::new);

        // context 有 token 信息，表示使用的 token 鉴权
        if (Objects.nonNull(ctx.getUserToken())) {
            Supplier<?> supplier = TOKEN_AUTH_FUNCTION_MAP.get(key);
            if (supplier != null) {
                String token = (String)supplier.get();
                LOGGER.info("刷新token: {}", token);
                return HTTP_CONTEXT_MAP.get(scope);
            }
            return ctx;
        } else {
            // 使用 DIGEST 鉴权方式更新认证信息
            // 创建HttpClientContext实例，使用自定义 context，这样才能保存前后两次请求的认证信息缓存，不必每次都生成认证信息
            CredentialsProvider credsProvider = ctx.getCredentialsProvider();
            HttpClientContext ctx2 = HttpClientContext.create();
            ctx2.setCredentialsProvider(credsProvider);

            HTTP_CONTEXT_MAP.put(scope, ctx2);
            return ctx2;
        }
    }

    public String execute(HttpUriRequest request, HttpContext context) throws IOException {
        return execute(request, context, 0);
    }

    /**
     * 执行请求
     * @param request 请求信息
     * @param context 公共上下文信息
     * @param retry 重试次数，最大重试3次
     * @return 接口返回信息
     * @throws IOException 异常
     */
    private String execute(HttpUriRequest request, HttpContext context, int retry) throws IOException {
        // 使用 context 做同步处理，避免两个相同的服务请求同时做鉴权处理，会引起其中一个鉴权报错
        CloseableHttpResponse response;

        boolean haveToken = setToken(request, context);

        // context 不为空，且 context 中认证信息不为 SUCCESS 则需要加锁，避免锁异常
        boolean needSync = !haveToken && context != null && (((HttpClientContext)context).getTargetAuthState() == null
            || ((HttpClientContext)context).getTargetAuthState().getState() != AuthProtocolState.SUCCESS);
        if (needSync) {
            synchronized (getLock(request, context)) {
                response = client.execute(request, context);
            }
        } else {
            response = client.execute(request, context);
        }

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED && retry < 2) {
                // 提示没有认证则更新认证信息重试，一共重试 3 次
                LOGGER.warn("鉴权失败，重试：{} - {}  {}\n{}", request.getURI(), statusCode, retry,
                    EntityUtils.toString(response.getEntity()));
                if (retry > 0) {
                    context = resetCredentialsContext(request, context);
                }
            } else {
                LOGGER.error("请求失败：{} - {}  {}\n{}", request.getURI(), statusCode, retry, EntityUtils.toString(response.getEntity()));
                throw new BusinessException(statusCode, request.getURI().toString());
            }
        } finally {
            IoUtil.close(response);
        }
        return execute(request, context, ++retry);
    }

    public boolean setToken(HttpUriRequest request, HttpContext context) {
        if (context == null) {
            return false;
        }
        String token = Optional.ofNullable(((HttpClientContext)context).getUserToken()).map(Object::toString).orElse("");
        if (StringUtils.isNotEmpty(token)) {
            request.addHeader("token", token);
            request.addHeader("access_token", token);
            return true;
        }
        return false;
    }

    /**
     * 获取锁，AuthScope可以保证加锁效果，context 在不缓存鉴权信息的请求（例如大华）中无法保证加锁效果
     */
    private Object getLock(HttpUriRequest request, HttpContext context) {
        URI uri = request.getURI();
        HttpHost key = new HttpHost(uri.getHost(), uri.getPort());
        AuthScope scope = AUTH_SCOPE_LOCK_MAP.get(key);
        if (scope != null) {
            return scope;
        }
        if (context != null) {
            return context;
        }
        return this;
    }

    public enum AuthType {
        /**
         * 普通摘要认证
         */
        DIGEST,
        /**
         * 单次摘要认证，每次请求需要重新认证
         */
        DIGEST_ONE,
        /**
         * token 认证，需要单独请求鉴权接口
         */
        TOKEN,
        /**
         * 不需要认证
         */
        NONE
    }
}

