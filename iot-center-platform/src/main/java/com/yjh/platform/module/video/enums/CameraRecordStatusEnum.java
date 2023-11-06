package com.yjh.platform.module.video.enums;

/**
 * @author zhangyuyi
 * @create 2023-11-06
 */
public enum CameraRecordStatusEnum {
    START(1, "开始播放"),
    STOP(2, "结束播放");
    private Integer code;
    private String desc;

    CameraRecordStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
