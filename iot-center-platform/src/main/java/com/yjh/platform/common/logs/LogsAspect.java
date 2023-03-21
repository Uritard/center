package com.yjh.platform.common.logs;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.constant.ModelTypeEnum;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.IPUtil;
import jdk.nashorn.internal.scripts.JO;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description
 * @Author tt
 * @Date 2019/7/30
 **/
@Aspect
@Component
public class LogsAspect {
    private static final Logger log = LoggerFactory.getLogger(IPUtil.class);

    private static final String LOG_URL = "http://iot-center-share/sysLog/v1/add";
    @Autowired
    private LogsConfig logsConfig;
    @Autowired
    private RedisTemplate redisTemplate;

    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }

    @Pointcut("execution(* com.yjh..controller..*.*(..))")
    public void selectAspect() {
    }

    @Around("selectAspect()")
    public Object checkSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();
        Object[] args = joinPoint.getArgs();
        Logs annotation = signature.getMethod().getAnnotation(Logs.class);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        StringBuilder content = new StringBuilder("");
        String userId = "99999", userName = null, serviceId = null, ip = null,userRole=null;
        HttpServletRequest request = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        if (null != servletRequestAttributes) {
            request = servletRequestAttributes.getRequest();
            if (Objects.nonNull(request.getHeader("userId")) && !Objects.equals(request.getHeader("userId"), "undefined")) {
                userId = request.getHeader("userId");
                userName = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("userName"));
                userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            } else { userName = "admin"; }
        }
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        log.info("ttIp: "+ip);
        Object result = null;
        if (annotation != null) {
            String contentStr = annotation.content();
            String title = annotation.title();
            String codeName = annotation.codeName();
            String replyStr = null;
            if(StringUtils.isNotEmpty(codeName)){
                JSONObject parameters = getRequestParams(request, args, paramAnnotations);
                String kind = parameters.getString(codeName);
                boolean isDrone = ("droneType".equals(codeName) && StringUtils.isNotEmpty(kind)) || ("type".equals(codeName) && "2".equals(kind));
                if (isDrone) {
                    replyStr = "无人机";
                }
                if (replyStr != null) {
                    contentStr = contentStr.replace("机器人", replyStr);
                    title = title.replace("机器人", replyStr);
                }
            }

            content.append(contentStr);
            //处理导出模型文件
            if ("导出模型文件".equals(title)) {
                JSONObject parameters = getRequestParams(request,args,paramAnnotations);
                String modelName = ModelTypeEnum.exportMap().get(parameters.getString("type"));
                if (StringUtils.isNotBlank(modelName)) {
                    content.append("-").append(modelName);
                }
            }

            params.set("userId", userId);
            params.set("userName", userName);
            params.set("requestOrigin", request.getRequestURL());
            params.set("requestPath", request.getRequestURI());
            params.set("requestMethod", request.getMethod());
            if(Constant.apiPermissions){
                try {
                    if (StringUtils.isNotEmpty(annotation.authority())) {
                        String ans = annotation.authority();
                        assert userRole != null;
                        if (!ans.contains(userRole)) {
                            //todo 越权访问入日志
                            params.set("logType", "24");
                            params.set("ip", ip);
                            params.set("title", annotation.title());
                            params.set("state", 3);
                            Map<String, String> jsonMap = new HashMap<>(8);
                            jsonMap.put("type", "alarmPopUp");
                            jsonMap.put("ip", ip);
                            jsonMap.put("warningInfo", "越权访问告警！！！");
                            jsonMap.put("userId", userId);
                            jsonMap.put("logType", "24");
                            jsonMap.put("userName", userName);
                            jsonMap.put("title", annotation.title());
                            jsonMap.put("content", String.valueOf(content));
                            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jsonMap);
                            content.append(";用户").append(userName).append("存在越权访问!");
                            params.set("content", content.toString());
                            if (!"修改系统用户数据".equals(annotation.title()) && !"删除系统用户数据".equals(annotation.title())) {
                                post(params);
                            }
                            Result re = new Result();
                            re.setCode(209, "此用户无权限");
                            return re;
                        }
                    }

                }catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }


            try {
//                serviceId = logsConfig.getName();
                params.set("logType", annotation.logType());
                params.set("ip", ip);
                params.set("title", title);
                params.set("state", 1);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            try {
                // 记录操作日志...谁..在什么时间..做了什么事情..
                result = joinPoint.proceed();
                params.set("content", content.toString());
                boolean flag = true;
                switch (title){
                    case "修改系统用户数据":
                    case "删除系统用户数据":
                    case "新增任务":
                    case "删除任务":
                    case "任务启动":
                    case "任务暂停":
                    case "任务恢复":
                    case "任务终止":
                    case "新增预案":
                    case "删除预案":
                    case "修改预案":
                        flag =false;
                        break;
                    default:
                        break;
                }
                if (flag) {
                    post(params);
                }
                return result;
            } catch (BusinessException e) {
                params.set("state", 2);
                params.set("content", content.toString() + "；错误信息：" + e.getMessage());
                post(params);
                throw e;
            } catch (Throwable e) {
                params.set("content", content.toString() + "；异常信息：" + e.getMessage());
                params.set("state", 3);
                post(params);
                throw e;
            }
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            serviceId = logsConfig.getName();
            params.set("logType", annotation.logType());
            params.set("ip", ip);
            params.set("title", "内部接口错误");
            params.set("state", 3);
            params.set("content", "异常信息: " + e.getMessage() + "\n" + getStackMsg(e));
            params.set("userId", userId);
            params.set("userName", userName);
            post(params);
            throw e;
        }
    }

    public void post(MultiValueMap<String, Object> params) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                System.out.println("platformLogAdd...");
                serviceRestTemplate.postForObject(LOG_URL, params, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String getStackMsg(Throwable e) {
        try {
            System.out.println("platformStack");
            StringBuffer sb = new StringBuffer();
            sb.append(e.getMessage() + "\n");
            StackTraceElement[] stackArray = e.getStackTrace();
            for (StackTraceElement element : stackArray) {
                sb.append(element.toString() + "\n");
            }
            return sb.toString();
        } catch (Exception e1) {
            return "";
        }
    }

    private JSONObject getRequestParams(HttpServletRequest request, Object[] args, Annotation[][] paramAnnotations) {
        String method = request.getMethod().toUpperCase();
        //判断是POST请求还是GET请求（不同请求获取参数方式不同）
        JSONObject paramJson = new JSONObject();
        try {
            // 获取 get 方式传入参数，传入数组只获取第一个
            Map<String, String[]> map = request.getParameterMap();
            if (!(Objects.isNull(map) || map.isEmpty())) {
                for (Map.Entry<String, String[]> entry : map.entrySet()) {
                    if(StringUtils.isNotEmpty(entry.getValue()[0])) {
                        paramJson.put(entry.getKey(), entry.getValue()[0]);
                    }
                }
            }
            // 判断是否存在注解
            if(ArrayUtils.isEmpty(paramAnnotations)){
                return paramJson;
            }
            int idx = -1;
            outside:
            for (int i = 0; i < paramAnnotations.length; i++) {
                Annotation[] anns = paramAnnotations[i];
                if(ArrayUtils.isEmpty(anns)){
                    continue;
                }
                for (int j = 0; j < anns.length; j++) {
                    Annotation annotation = anns[j];
                    // 判断 @RequestBody 注解是第几个参数
                    if(annotation instanceof RequestBody){
                        idx = i;
                        break outside;
                    }
                }
            }
            // 获取参数，然后存入 json
            if(idx >= 0) {
                JSONObject bodyParams = (JSONObject)JSONObject.toJSON(args[idx]);
                paramJson.putAll(bodyParams);
            }

        } catch (Exception e) {
            log.error("***************getRequestParams throw Exception：***************", e);
        }
        return paramJson;
    }

}
