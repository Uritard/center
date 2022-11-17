package com.yjh.accessrobot.module.command.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 声纹ftp配置表
 * @TableName t_voice_config
 */
@Data
public class TVoiceConfig implements Serializable {
    /**
     * 
     */
    private Long configId;

    /**
     * ftp地址
     */
    private String ftpUrl;

    /**
     * 所属电站
     */
    private String stationId;

    /**
     * 用户名
     */
    private String owner;

    /**
     * 登陆密码
     */
    private String ownerCode;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 通道
     */
    private String channelNum;

    /**
     * Ftp声纹绝对路径
     */
    private String absoluPath;

    /**
     * Ftp声纹相对路径
     */
    private String relativePath;

    /**
     * 分贝告警值
     */
    private String dbValue;

    /**
     * 频率限值
     */
    private String fValue;

    /**
     * 幅值限值
     */
    private String mpValue;

    /**
     * 算法配置文件路径
     */
    private String filePath;

    /**
     * pmsId
     */
    private String pmsId;

    private static final long serialVersionUID = 1L;
}