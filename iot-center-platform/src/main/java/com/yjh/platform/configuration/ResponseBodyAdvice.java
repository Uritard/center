package com.yjh.platform.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yjh.platform.common.utils.LogUtil;
import com.yjh.platform.common.annotation.SecretAnnotations;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import javax.servlet.http.HttpServletRequest;


/**
 * ResponseBodyAdvice配置类.
 */
@RestControllerAdvice
public class ResponseBodyAdvice implements  org.springframework.web.servlet.mvc. method.annotation.ResponseBodyAdvice {

    //国密规范测试公钥
    private static final String pubk = "04673FC4F3D41C9470E32AABCB5A958E2CE528959F373D0F7AB2B82E65BF4DE8FB67716A269993585451888C8450E92A75A6C34EDFF748097BEAD8E41C2976E8AA";

    private static LogUtil logger = new LogUtil(ResponseBodyAdvice.class);

    /**
     * supports.
     *
     * @param methodParameter MethodParameter
     * @param aClass          Class
     * @return boolean
     */
    @Override
    public boolean supports(final MethodParameter methodParameter,
                            final Class aClass) {
        return true;
    }

    /**
     * beforeBodyWrite.
     *
     * @param body            Object
     * @param methodParameter MethodParameter
     * @param mediaType       MediaType
     * @param aClass          Class
     * @param request         ServerHttpRequest
     * @param response        ServerHttpResponse
     * @return Object
     */
    @Override
    public Object beforeBodyWrite(
            final Object body,
            final MethodParameter methodParameter,
            final MediaType mediaType,
            final Class aClass,
            final ServerHttpRequest request,
            final ServerHttpResponse response) {
        HttpServletRequest req =
                ((ServletRequestAttributes) RequestContextHolder.
                        getRequestAttributes()).getRequest();
        if (null != req.getSession().getAttribute("content-agent")) {
            response.getHeaders().add("content-agent",
                    req.getSession().
                            getAttribute("content-agent").toString());

        }
        boolean encode = false;
        if (methodParameter.getMethod().
                isAnnotationPresent(SecretAnnotations.class)) {
            SecretAnnotations secretAnnotation =
                    methodParameter.
                            getMethodAnnotation(SecretAnnotations.class);
            encode = secretAnnotation.encode();
        }
        if (encode) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String result = objectMapper.
                        writerWithDefaultPrettyPrinter().
                        writeValueAsString(body);
                response.getHeaders().
                        add("isdecode", "1");
                byte[] cipherText = null;
              //  cipherText = SM2Utils.encrypt(Base64.decode(new String(Base64.encode(Util.hexToByte(pubk))).getBytes()), result.getBytes());
                return new String(Base64.encode(cipherText));
            } catch (Exception e) {
                return body;
            }
        } else {
            response.getHeaders().
                    add("isdecode", "0");
        }

        return body;
    }

}
