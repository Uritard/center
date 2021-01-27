package com.yjh.platform.common.logs.enums;

import java.util.Objects;

/**
 * @author lichensi
 * @date 2020/12/10 16:52
 */
public enum OperateTypeEnum {

    QUERY(1,"查询"),
    ADD(2,"增加"),
    EDIT(3,"修改"),
    DELETE(4,"删除"),
    EXECUTE(5,"执行");

    private Integer logType;

    private String content;

    OperateTypeEnum(int logType, String content) {
        this.logType = logType;
        this.content = content;
    }

    public static String assembleOperateContent(String title, Integer logType){
        if(Objects.nonNull(title)){
            for(OperateTypeEnum typeEnum : values()){
                if(logType.intValue() == typeEnum.logType){
                    return typeEnum.content + logType;
                }
            }
        }
        return logType.toString();
    }
}
