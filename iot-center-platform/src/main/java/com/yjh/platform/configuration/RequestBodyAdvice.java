package com.yjh.platform.configuration;


import com.yjh.platform.common.utils.LogUtil;
import com.yjh.platform.common.utils.NumConstant;
import com.yjh.platform.common.annotation.SecretAnnotation;
import com.yjh.platform.common.utils.smUtil.Demo;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * RequestBodyAdvice配置类.
 */
@RestControllerAdvice
public class RequestBodyAdvice implements org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice {

    private static LogUtil logger = new LogUtil(RequestBodyAdvice.class);
    //国密规范测试私钥
    private static final String prik = "055C74CFB227BD9CDFF242D233096BC6FDBAFB59D001D5EE7F857ADFC6BF1501";
    private static final String pubk = "04673FC4F3D41C9470E32AABCB5A958E2CE528959F373D0F7AB2B82E65BF4DE8FB67716A269993585451888C8450E92A75A6C34EDFF748097BEAD8E41C2976E8AA";


    /**
     * supports.
     *
     * @param methodParameter MethodParameter
     * @param type            Type
     * @param converterType   Class<? extends HttpMessageConverter<?>>
     * @return boolean
     */
    @Override
    public boolean supports(final MethodParameter methodParameter,
                            final Type type,
                            final Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }


    /**
     * beforeBodyRead.
     *
     * @param inputMessage    HttpInputMessage
     * @param methodParameter MethodParameter
     * @param type            Type
     * @param converterType   Class<? extends HttpMessageConverter<?>>
     * @return HttpInputMessage
     * @throws IOException 异常
     */
    @Override
    public HttpInputMessage beforeBodyRead(
            final HttpInputMessage inputMessage,
            final MethodParameter methodParameter,
            final Type type,
            final Class<? extends HttpMessageConverter<?>> converterType)
            throws IOException {
        boolean decode = false;
        if (methodParameter.getMethod().
                isAnnotationPresent(SecretAnnotation.class)) {
            SecretAnnotation secretAnnotation =
                    methodParameter.
                            getMethodAnnotation(SecretAnnotation.class);
            decode = secretAnnotation.decode();
        }
        if (decode) {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = null;
            InputStream inputStream = inputMessage.getBody();
            if (null != inputStream) {
                bufferedReader = new BufferedReader(
                        new InputStreamReader(inputStream));
                char[] charBuffer = new char[NumConstant.NUM_1024];
                int bytesRead = -1;
                while ((bytesRead =
                        bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(
                            charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
            try {
                logger.info("进入参数校验--------------");
                logger.info(inputMessage.getHeaders() + "");
                String token = inputMessage.getHeaders().get("webcode") != null ? inputMessage.getHeaders().get("webcode").get(0) : "";
                logger.info("进入参数校验--------------原始token : " + token);
                String buliderString = stringBuilder.toString();
                StringBuilder sb = new StringBuilder();
                for (char c : buliderString.toCharArray()) {
                    sb.append(Integer.toUnsignedString(c, 10));
                }
                String tokens = Demo.summary(sb.toString());
                logger.info("进入参数校验--------------后端解析token : " + tokens);
                if (!tokens.equals(token)) {
                    logger.error("参数篡改" + buliderString + " ,之后的token: " + token);
                    throw new RuntimeException("参数篡改");
                }
                return new MyHttpInputMessage(
                        inputMessage.getHeaders()
                        , new java.io.ByteArrayInputStream(
                        buliderString.getBytes("UTF-8")));
            } catch (Exception e) {
                logger.error("解密参数报错：" + e.getMessage());
                return null;
            }
        }
        return inputMessage;
    }

    /**
     * afterBodyRead.
     *
     * @param body          Object
     * @param inputMessage  HttpInputMessage
     * @param parameter     MethodParameter
     * @param targetType    Type
     * @param converterType Class<? extends HttpMessageConverter<?>>
     * @return Object
     */
    @Override
    public Object afterBodyRead(
            final Object body,
            final HttpInputMessage inputMessage,
            final MethodParameter parameter,
            final Type targetType,
            final Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * handleEmptyBody.
     *
     * @param body          Object
     * @param inputMessage  HttpInputMessage
     * @param parameter     MethodParameter
     * @param targetType    Type
     * @param converterType Class<? extends HttpMessageConverter<?>>
     * @return Object
     */
    @Override
    public Object handleEmptyBody(
            final Object body,
            final HttpInputMessage inputMessage,
            final MethodParameter parameter,
            final Type targetType,
            final Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * MyHttpInputMessage类.
     */
    static class MyHttpInputMessage implements HttpInputMessage {
        /**
         * 头.
         */
        private HttpHeaders headers;
        /**
         * 内容.
         */
        private InputStream body;

        /**
         * 构造函数.
         *
         * @param newhttpHeaders HttpHeaders
         * @param newbody        InputStream
         */
        public MyHttpInputMessage(final HttpHeaders newhttpHeaders,
                                  final InputStream newbody) {
            this.headers = newhttpHeaders;
            this.body = newbody;
        }

        /**
         * 获取内容.
         *
         * @return InputStream
         * @throws IOException 异常
         */
        @Override
        public InputStream getBody() throws IOException {
            return body;
        }

        /**
         * 获取头.
         *
         * @return HttpHeaders
         */
        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        /**
         * 设置头.
         *
         * @param newheaders HttpHeaders
         */
        public void setHeaders(final HttpHeaders newheaders) {
            this.headers = newheaders;
        }

        /**
         * 设置内容.
         *
         * @param newbody InputStream
         */
        public void setBody(final InputStream newbody) {
            this.body = newbody;
        }
    }

}
