package com.yjh.accessudp.commons.logs;

import com.yjh.accessudp.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessudp.commons.utils.http.IPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description
 * @Author tt
 * @Date 2019/9/19
 **/
@Component
public class CommonLogs {
    @Autowired
    private LogsConfig logsConfig;
    private static final String LOG_URL = "http://energy-platform-logs/log/add";

    /**
     * 常规日志
     *
     * @param title
     * @param code
     * @param content
     */
    public void info(String title, String code, String content) {
        this.add(1, title, code, content, null, null);
    }


    public void info(String title, String code, String content, String userName) {
        this.add(1, title, code, content, userName, null);
    }

    public void info(String title, String code, String content, String userName, String userId) {
        this.add(1, title, code, content, userName, userId);
    }

    /**
     * 错误日志
     *
     * @param title
     * @param code
     * @param content
     */
    public void error(String title, String code, String content) {
        this.add(2, title, code, content, null, null);
    }

    public void error(String title, String code, String content, String userName) {
        this.add(2, title, code, content, userName, null);
    }

    /**
     * 操作人userId
     *
     * @param title
     * @param code
     * @param content
     * @param userName
     * @param userId
     */
    public void error(String title, String code, String content, String userName, String userId) {
        this.add(2, title, code, content, userName, userId);
    }

    /**
     * 异常日志
     *
     * @param title
     * @param code
     * @param content
     */
    public void exception(String title, String code, String content) {
        this.add(3, title, code, content, null, null);
    }

    public void exception(String title, String code, String content, String userName) {
        this.add(3, title, code, content, userName, null);
    }

    public void exception(String title, String code, String content, String userName, String userId) {
        this.add(3, title, code, content, userName, userId);
    }

    private void add(int state, String title, String code, String content, String userName, String userId) {
        try {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("state", state);
            params.set("title", title);
            params.set("code", code);
            params.set("content", content);
            params.set("serviceId", logsConfig.getName());
            try {
                HttpServletRequest request = null;
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
                if (null != servletRequestAttributes) {
                    request = servletRequestAttributes.getRequest();
                    if (request != null) {
                        if (null == userId) {
                            params.set("userId", request.getHeader("userId"));
                        } else {
                            params.set("userId", userId);
                        }
                        if (null == userName) {
                            params.set("userName", request.getHeader("userName"));
                        } else {
                            params.set("userName", userName);
                        }
                        params.set("ip", IPUtil.getRemoteIP(request));
                    }
                }

            } catch (Exception e1) {

            }
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(LOG_URL, params, String.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
