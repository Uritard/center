package com.yjh.platform.module.device.entity;


import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.yjh.platform.common.annotation.ExcelExtend;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
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
    @ExcelProperty(value = "设备名称")
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
    @ExcelProperty(value = "设备类型")
    @ExcelExtend(dictType = "voiceType")
    private String voiceType;
    private String voiceTypeName;

    @Length(max = 32, message = "voiceModel长度必须小于等于32")
    @ApiModelProperty(value = "设备型号")
    @ExcelProperty(value = "设备型号")
    @ExcelExtend(dictType = "voiceModel")
    private String voiceModel;
    private String voiceModelName;

    @Length(max = 32, message = "voiceFactory长度必须小于等于32")
    @ApiModelProperty(value = "生产厂家")
    @ExcelProperty(value = "生产厂家")
    @ExcelExtend(dictType = "voiceFactory")
    private String voiceFactory;
    private String voiceFactoryName;

    @Length(max = 255, message = "ftp地址长度必须小于等于255")
    @ApiModelProperty(value = "ftp地址")
    @ExcelProperty(value = "IP")
    private String ftpUrl;

    @Length(max = 32, message = "所属电站长度必须小于等于32")
    @ApiModelProperty(value = "所属电站")
    private String stationId;

    @Length(max = 255, message = "owner长度必须小于等于255")
    @ApiModelProperty(value = "用户名")
    private String owner;

    @Length(max = 255, message = "ownerCode长度必须小于等于255")
    @ApiModelProperty(value = "登陆密码")
    private String ownerCode;

    @Max(value = 999999)
    @ApiModelProperty(value = "端口号")
    @ExcelProperty(value = "端口号")
    private Integer port;

    @Length(max = 255, message = "Ftp声纹绝对路径长度必须小于等于255")
    @ApiModelProperty(value = "Ftp声纹绝对路径")
    @TableField("absoluPath")
    private String absoluPath;

    @Length(max = 255, message = "Ftp声纹相对路径长度必须小于等于255")
    @ApiModelProperty(value = "Ftp声纹相对路径")
    @TableField("relativePath")
    private String relativePath;

    @Length(max = 11, message = "分贝告警值长度必须小于等于11")
    @ApiModelProperty(value = "分贝告警值")
    @ExcelProperty(value = "分贝告警值")
    @TableField("dbValue")
    private String dbValue;

    @Length(max = 11, message = "频率限值长度必须小于等于11")
    @ApiModelProperty(value = "频率限值")
    @ExcelProperty(value = "频率告警值")
    private String fValue;

    @Length(max = 11, message = "幅值限值长度必须小于等于11")
    private String mpValue;

    @Length(max = 255, message = "算法配置文件路径长度必须小于等于255")
    @ApiModelProperty(value = "算法配置文件路径")
    @TableField("filePath")
    private String filePath;

    @Length(max = 32, message = "channelNum长度必须小于等于32")
    @ExcelProperty(value = "通道号")
    private String channelNum;

    @ExcelProperty(value = "所属区域")
    private String regionName;

    private String deviceName;

    private String deviceTypeName;

    @Length(max = 30, message = "PMS ID长度必须小于等于30")
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

    @ExcelExtend(defaultValue = "离线")
    private String state;
    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 原始id(下级同步的id)
     */
    private String originId;

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

    public String getEdgeCode() {
        return edgeCode;
    }

    public void setEdgeCode(String edgeCode) {
        this.edgeCode = edgeCode;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }
}
