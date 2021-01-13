package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author yc
 * @since 2020-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraRecorder对象", description = "录像服务器表")
public class TCameraRecorder implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "recordId不为空")
    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "录像机ID")
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @Length(max = 128,message = "recordName长度必须小于等于128")
    @ApiModelProperty(value = "服务器名称")
    private String recordName;

    @Length(max = 32,message = "recorderType长度必须小于等于32")
    @ApiModelProperty(value = "录像机类型")
    private String recorderType;

    @Length(max = 128,message = "aliasName长度必须小于等于128")
    @ApiModelProperty(value = "备用名称")
    private String aliasName;

    @Length(max = 32,message = "recordIp长度必须小于等于32")
    @ApiModelProperty(value = "服务器地址")
    private String recordIp;

    @Length(max = 16,message = "protocol长度必须小于等于16")
    @ApiModelProperty(value = "传输协议")
    private String protocol;

    @Max(value=999999999)
    @ApiModelProperty(value = "http端口")
    private Integer httpPort;

    @Max(value=999999999)
    @ApiModelProperty(value = "传输端口")
    private Integer transPort;

    @Max(value=999999999)
    @ApiModelProperty(value = "控制端口")
    private Integer rtspPort;

    @Length(max = 128,message = "userName长度必须小于等于128")
    @ApiModelProperty(value = "用户名")
    private String userName;

    @Length(max = 50,message = "pwd长度必须小于等于50")
    @ApiModelProperty(value = "密码")
    private String pwd;

    @Length(max = 255,message = "protocolUrl长度必须小于等于255")
    @ApiModelProperty(value = "协议路径")
    private String protocolUrl;

    @Max(value=999999999)
    @ApiModelProperty(value = "最大通道数")
    private Integer maxChannel;

    @Max(value=999999999)
    @ApiModelProperty(value = "缓存磁盘空间")
    private Integer hddSize;

    @Max(value=999999999)
    @ApiModelProperty(value = "缓存天数")
    private Integer bufferDay;

    @Max(value=999999999)
    @ApiModelProperty(value = "录制文件时长 单位秒")
    private Integer timeLong;


}
