package com.yjh.accessvideo.common.logs.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author lichensi
 * @date 2020/12/10 16:32
 */
public enum RequestMethodEnum {

    UNKNOWN(0,"UNKNOWN"),

    GET(1,"GET"),

    HEAD(2,"HEAD"),

    POST(3,"POST"),

    PUT(4,"PUT"),

    DELETE(5,"DELETE"),

    CONNECT(6,"CONNECT"),

    OPTIONS(7,"OPTIONS"),

    TRACE(8,"TRACE");

    private int code;

    private String desc;

    RequestMethodEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static int ofCode(String desc){
        if(StringUtils.isNotBlank(desc)){
            for(RequestMethodEnum methodEnum : values()){
                if(StringUtils.equalsIgnoreCase(methodEnum.desc, desc)){
                    return methodEnum.code;
                }
            }
        }
        return UNKNOWN.code;
    }
}
