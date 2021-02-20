package com.yjh.platform.common.aop;


import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.yjh.platform.common.utils.LogUtil;
import com.yjh.platform.common.utils.smUtil.SM2Utils;
import com.yjh.platform.common.utils.smUtil.Util;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.context.annotation.Configuration;


import java.util.Iterator;
import java.util.Map;


/**
 * AOP设定解密操作
 */
@Aspect
@Configuration
public class DecryptAspect {
    private static LogUtil logger = new LogUtil(DecryptAspect.class);
    //国密规范测试公钥
    private static final String prik = "055C74CFB227BD9CDFF242D233096BC6FDBAFB59D001D5EE7F857ADFC6BF1501";

    /**
     * 切点方法定义 get参数
     */
    @Pointcut("@annotation(com.yjh.platform.common.annotation.Decryptsingle)")
    public void apiLogPointCut() {
    }

    /**
     * 切点方法定义 class
     */
    @Pointcut("@annotation(com.yjh.platform.common.annotation.Decryptentity)")
    public void apiLogPointCutEntity() {
    }

    /**
     * 切点方法定义 map参数
     */
    @Pointcut("@annotation(com.yjh.platform.common.annotation.Decrypt)")
    public void apiLogPointCutMap() {
    }


    /**
     * 方法执行前执行内容
     *
     * @param joinPoint 切点
     * @return Object
     * @throws Throwable 异常
     */
    @Around("apiLogPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("进入aop--------------");
        Object[] paramValues = joinPoint.getArgs();
        Object[] map=new Object[paramValues.length];
        int i=0;
        for(Object obj:paramValues){
            String value = String.valueOf(obj);
            if (StringUtils.isNotBlank(value)) {
                String decrypt = new String(SM2Utils.decrypt(Base64.decode(new String(Base64.encode(Util.hexToByte(prik))).getBytes()), Base64.decode(value.getBytes())));
                map[i] = decrypt;
            }else{
                map[i]=obj;
            }
            i++;
        }
        Object[] param = map;
        Object result = joinPoint.proceed(param);
        return result;
    }

    /**
     * 方法执行前执行内容
     *
     * @param joinPoint 切点
     * @return Object
     * @throws Throwable 异常
     */
    @Around("apiLogPointCutEntity()")
    public Object aroundEntuty(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("进入aop--------------");
        Object[] paramValues = joinPoint.getArgs();
        Class classes = paramValues[0].getClass();
        String params = new Gson().toJson(paramValues[0]);
        Map<String, Object> map = new Gson().fromJson(params, Map.class);
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String value = String.valueOf(entry.getValue());
            if (StringUtils.isNotBlank(value)) {
                map.put(entry.getKey(), new String(SM2Utils.decrypt(Base64.decode(new String(Base64.encode(Util.hexToByte(prik))).getBytes()), Base64.decode(value.getBytes()))));
            }
        }
        Object[] param = {JSON.parseObject(JSON.toJSONString(map), classes)};
        //执行方法
        Object result = joinPoint.proceed(param);
        return result;
    }


    /**
     * 方法执行前执行内容
     *
     * @param joinPoint 切点
     * @return Object
     * @throws Throwable 异常
     */
    @Around("apiLogPointCutMap()")
    public Object aroundMap(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("进入aop--------------");
        Object[] paramValues = joinPoint.getArgs();
        String params = new Gson().toJson(paramValues[0]);
        Map<String, Object> map = new Gson().fromJson(params, Map.class);
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String value = String.valueOf(entry.getValue());
            if (StringUtils.isNotBlank(value)) {
                map.put(entry.getKey(), new String(SM2Utils.decrypt(Base64.decode(new String(Base64.encode(Util.hexToByte(prik))).getBytes()), Base64.decode(value.getBytes()))));
            }
        }
        Object[] param = {map};
        //执行方法
        Object result = joinPoint.proceed(param);
        return result;
    }
}
