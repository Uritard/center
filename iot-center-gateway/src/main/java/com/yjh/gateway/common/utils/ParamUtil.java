package com.yjh.gateway.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.google.common.collect.Maps;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.http.HttpServletRequestWrapper;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hyh
 * @since 2022/4/13
 **/
public class ParamUtil {

    private static Logger logger = LoggerFactory.getLogger(ParamUtil.class);

    private static Pattern paramPattern = Pattern.compile("[~$%^&*+<>?\"{}();']+");

    public static Map<String, Object> getRequestParams(RequestContext ctx) {
        String method = ctx.getRequest().getMethod().toUpperCase();
        String uri = ctx.getRequest().getRequestURI();
        //判断是POST请求还是GET请求（不同请求获取参数方式不同）
        Map<String, Object> param = new LinkedHashMap<>();
        try {
            if (uri.startsWith("/zuul")) {
                byte[] requestByte = saveaIns(ctx.getRequest().getInputStream());
                rewriteRequest(ctx, requestByte);
                param = Maps.newLinkedHashMap();
                DiskFileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setHeaderEncoding("UTF-8");
                List<FileItem> list = upload.parseRequest(new ServletRequestContext(ctx.getRequest()));
                for (FileItem item : list) {
                    String name = item.getFieldName();
                    if (item.isFormField()) {
                        String value = item.getString("UTF-8");
                        param.put(name, value);
                    } else {
                        String filename = item.getName();
                        param.put(name, filename);
                    }
                }
                rewriteRequest(ctx, requestByte);
            } else {
                if ("GET".equals(method)) {
                    Map<String, List<String>> map = ctx.getRequestQueryParams();
                    if (!(Objects.isNull(map) || map.isEmpty())) {
                        param = Maps.newLinkedHashMap();
                        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                            param.put(entry.getKey(), entry.getValue().get(0));
                        }
                    }
                } else if ("POST".equals(method) || "PUT".equals(method)) {
                    String contentType = ctx.getRequest().getContentType();
                    if(StringUtils.contains(contentType, "multipart/form-data")){
                        Map<String, List<String>> parMap = ctx.getRequestQueryParams();
                        if (MapUtils.isNotEmpty(parMap)) {
                            for (Map.Entry<String, List<String>> entry : parMap.entrySet()) {
                                param.put(entry.getKey(), entry.getValue().get(0));
                            }
                        }

                        MultipartResolver resolver = new CommonsMultipartResolver(ctx.getRequest().getServletContext());
                        MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(ctx.getRequest());
                        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
                        for(Map.Entry<String, MultipartFile> entry : fileMap.entrySet()){
                            MultipartFile mFile = entry.getValue();
                            param.put(mFile.getName(), mFile.getOriginalFilename());
                        }
                    } else {
                        try (InputStream inputStream = ctx.getRequest().getInputStream()) {
                            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                            logger.info("***************原始参数：{}***************", body);
                            if (!"[]".equals(body) && StringUtils.isNotEmpty(body)) {
                                param = JSONObject.parseObject(body, LinkedHashMap.class, Feature.OrderedField);
                            }
                            Map<String, List<String>> map = ctx.getRequestQueryParams();
                            if (MapUtils.isNotEmpty(map)) {
                                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                                    param.put(entry.getKey(), entry.getValue().get(0));
                                }
                            }
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("***************ParamUtil->getRequestParams throw Exception：{}***************", e);
        }
        return param;
    }

    private static void rewriteRequest(RequestContext ctx, byte[] paramBytes) {
        ctx.setRequest(new HttpServletRequestWrapper(ctx.getRequest()) {
            @Override
            public ServletInputStream getInputStream() throws IOException {
                return new ServletInputStreamWrapper(paramBytes);
            }

            @Override
            public int getContentLength() {
                return paramBytes.length;
            }

            @Override
            public long getContentLengthLong() {
                return paramBytes.length;
            }
        });
    }

    /**
     * 保存流对象（输入流在第二次使用的时候会失效）
     * 在需要用到InputStream的地方再封装成InputStream
     * ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf);
     * Workbook wb = new HSSFWorkbook(byteArrayInputStream);//byteArrayInputStream 继承了InputStream，故这样用并没有问题
     * 如果只需要用到一次inputstream流，就不用这样啦，直接用就OK
     *
     * @param ins
     */
    public static byte[] saveaIns(InputStream ins) {
        byte[] buf = null;
        try {
            if (ins != null) {
                buf = org.apache.commons.io.IOUtils.toByteArray(ins);//ins为InputStream流
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return buf;
    }


    public static String validated(Map<String, Object> paramMap) {

        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            // 对密码不进行校验
            if (StringUtils.endsWithIgnoreCase(key, "PCode") || ArrayUtils.contains(new String[]{"password", "ruleContent"}, key)) {
                continue;
            }
            if (val instanceof String && StringUtils.isNotEmpty((String) val)) {
                Matcher matcher = paramPattern.matcher((String) val);
                if (matcher.find()) {
//                    return "参数校验失败，请勿传入非法字符【~!$%^&*+<>?\"{}();'】";
                    return "参数校验失败，请勿传入非法字符";
                }
            }
        }

        return "";
    }
}
