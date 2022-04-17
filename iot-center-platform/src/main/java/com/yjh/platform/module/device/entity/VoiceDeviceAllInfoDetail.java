package com.yjh.platform.module.device.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;

/**
 * @author lqh
 * @since 2021/3/2
 */
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "TVoiceDevice对象全量信息", description = "声纹设备表全部信息")
public class VoiceDeviceAllInfoDetail{

    private static final long serialVersionUID = 1L;
    @Max(value = 999999999999999999L)
    @ApiModelProperty(value = "声纹监控设备Id（默认为Ip地址）")
    private Long voiceDeviceId;

    @Length(max = 255, message = "voiceDeviceName长度必须小于等于255")
    @ApiModelProperty(value = "声纹监控设备名称（）")
    private String voiceDeviceName;

    @Max(value = 999999999999999999L)
    @ApiModelProperty(value = "变压器下面换流变的设备Id")
    private Long stdDeviceId;

    @Length(max = 64, message = "deviceType长度必须小于等于64")
    @ApiModelProperty(value = "被监测的设备类型")
    private String deviceType;

    @Max(value = 999999999999999999L)
    private Long configId;

    @Max(value = 999999999999999999L)
    @ApiModelProperty(value = "上级区域id")
    private Long upRegionId;

    @Length(max = 32, message = "voiceCode长度必须小于等于32")
    @ApiModelProperty(value = "声纹设备编码")
    private String voiceCode;

    @Length(max = 32, message = "voiceType长度必须小于等于32")
    @ApiModelProperty(value = "设备类型")
    private String voiceType;
    private String voiceTypeName;

    @Length(max = 32, message = "voiceModel长度必须小于等于32")
    @ApiModelProperty(value = "设备型号")
    private String voiceModel;
    private String voiceModelName;

    @Length(max = 32, message = "voiceFactory长度必须小于等于32")
    @ApiModelProperty(value = "生产厂家")
    private String voiceFactory;
    private String voiceFactoryName;

    @ApiModelProperty(value = "ftp地址")
    private String ftpUrl;

    @ApiModelProperty(value = "所属电站")
    private String stationId;

    @ApiModelProperty(value = "用户名")
    private String owner;

    @ApiModelProperty(value = "登陆密码")
    private String ownerCode;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "Ftp声纹绝对路径")
    @TableField("absoluPath")
    private String absoluPath;

    @ApiModelProperty(value = "Ftp声纹相对路径")
    @TableField("relativePath")
    private String relativePath;

    @ApiModelProperty(value = "分贝告警值")
    @TableField("dbValue")
    private String dbValue;

    @ApiModelProperty(value = "频率限值")
    private String fValue;

    private String mpValue;

    @ApiModelProperty(value = "算法配置文件路径")
    @TableField("filePath")
    private String filePath;

    private String channelNum;

    private String regionName;

    private String deviceName;

    private String deviceTypeName;

    private String pmsId;

    public String getOpenState() {
        return openState;
    }

    public void setOpenState(String openState) {
        this.openState = openState;
    }

    private String openState;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    private String state;


    public String getPmsId() {
        return pmsId;
    }

    public void setPmsId(String pmsId) {
        this.pmsId = pmsId;
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getVoiceDeviceId() {
        return voiceDeviceId;
    }

    public void setVoiceDeviceId(Long voiceDeviceId) {
        this.voiceDeviceId = voiceDeviceId;
    }

    public String getVoiceDeviceName() {
        return voiceDeviceName;
    }

    public void setVoiceDeviceName(String voiceDeviceName) {
        this.voiceDeviceName = voiceDeviceName;
    }

    public Long getStdDeviceId() {
        return stdDeviceId;
    }

    public void setStdDeviceId(Long stdDeviceId) {
        this.stdDeviceId = stdDeviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Long getUpRegionId() {
        return upRegionId;
    }

    public void setUpRegionId(Long upRegionId) {
        this.upRegionId = upRegionId;
    }

    public String getVoiceCode() {
        return voiceCode;
    }

    public void setVoiceCode(String voiceCode) {
        this.voiceCode = voiceCode;
    }

    public String getVoiceType() {
        return voiceType;
    }

    public void setVoiceType(String voiceType) {
        this.voiceType = voiceType;
    }

    public String getVoiceTypeName() {
        return voiceTypeName;
    }

    public void setVoiceTypeName(String voiceTypeName) {
        this.voiceTypeName = voiceTypeName;
    }

    public String getVoiceModel() {
        return voiceModel;
    }

    public void setVoiceModel(String voiceModel) {
        this.voiceModel = voiceModel;
    }

    public String getVoiceModelName() {
        return voiceModelName;
    }

    public void setVoiceModelName(String voiceModelName) {
        this.voiceModelName = voiceModelName;
    }

    public String getVoiceFactory() {
        return voiceFactory;
    }

    public void setVoiceFactory(String voiceFactory) {
        this.voiceFactory = voiceFactory;
    }

    public String getVoiceFactoryName() {
        return voiceFactoryName;
    }

    public void setVoiceFactoryName(String voiceFactoryName) {
        this.voiceFactoryName = voiceFactoryName;
    }

    public String getFtpUrl() {
        return ftpUrl;
    }

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAbsoluPath() {
        return absoluPath;
    }

    public void setAbsoluPath(String absoluPath) {
        this.absoluPath = absoluPath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getDbValue() {
        return dbValue;
    }

    public void setDbValue(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getfValue() {
        return fValue;
    }

    public void setfValue(String fValue) {
        this.fValue = fValue;
    }

    public String getMpValue() {
        return mpValue;
    }

    public void setMpValue(String mpValue) {
        this.mpValue = mpValue;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getChannelNum() {
        return channelNum;
    }

    public void setChannelNum(String channelNum) {
        this.channelNum = channelNum;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

}
