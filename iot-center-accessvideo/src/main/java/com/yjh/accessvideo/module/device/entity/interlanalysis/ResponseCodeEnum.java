package com.yjh.accessvideo.module.device.entity.interlanalysis;

/**
 * 响应返回值
 *
 * @author 丫C
 * @date 2022/4/11
 */
public enum ResponseCodeEnum {
    /*
    * 成功
    * */
    REQUEST_SUCCESS(200, "成功"),
    /*
     * 表示客户端请求有语法错误，不能被服务器所理解
     * */
    ERROR_GRAMMAR(400, "语法错误"),
    /*
     * 服务端异常
     * */
    REQUEST_ABNORMAL(500, "服务端异常");

    /**
     * 状态码
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String value;

    private ResponseCodeEnum(Integer code, String value){
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }
    public String getValue() {
        return value;
    }

    public static ResponseCodeEnum getInstance(Integer code){
        for (ResponseCodeEnum codeEnum : values()) {
            if (codeEnum.getCode().equals(code)) {
                return codeEnum;
            }
        }
        return null;
    }

}
