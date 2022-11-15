package com.yjh.accesstcp.module.device.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/14
 * @since [产品/模块版本] （可选）
 */
@Data
public class VoiceDeviceModel extends TVoiceDevice{
    private String patroldeviceName;
    private String patroldeviceCode;
    private String stationName;
    private String stationCode;
    private String deviceModel;
    private String manufacturer;
    private String useUnit;
    private String deviceSource;
    private String productionDate;
    private String productionCode;
    private String istransport;
    private String useMode;
    private String videoMode;
    private String place;
    private String type;
    private String patroldeviceInfo;
    private String robotsCode;
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
}
