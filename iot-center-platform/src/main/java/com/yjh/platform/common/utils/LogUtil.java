package com.yjh.platform.common.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    private Logger logger;

    public LogUtil(Class clazz){
        logger = LoggerFactory.getLogger(clazz);
    }

    public void info(String msg){
        logger.info(handlerMessage(msg));
    }
    public void error(String msg){
        logger.error(handlerMessage(msg));
    }
    public void error(String msg,Throwable e){
        logger.error(handlerMessage(msg),e);
    }
    public void debug(String msg){
        logger.debug(handlerMessage(msg));
    }
    public void warn(String msg){
        logger.warn(handlerMessage(msg));
    }

    public boolean isDebugEnabled(){
        return logger.isDebugEnabled();
    }

    /**
     * 处理消息特殊字符
     * @param msg
     * @return
     */
    private String handlerMessage(String msg){
        if(null != msg){
            String pattern = "\\n\\r";
            return msg.replace(pattern,"");
        }
        return "";
    }


}
