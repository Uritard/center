package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author czh
 * @since 2020-08-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraRecorder对象", description = "摄像头录像机信息表")
public class TCameraRecorder implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long recordId;

    @ApiModelProperty(value = "服务器名称")
    private String recordName;

    @ApiModelProperty(value = "备用名称")
    private String aliasName;

    @ApiModelProperty(value = "服务器地址")
    private String recordIp;

    @ApiModelProperty(value = "传输协议")
    private String protocal;

    @ApiModelProperty(value = "http端口")
    private Integer httpPort;

    @ApiModelProperty(value = "传输端口")
    private Integer transPort;

    @ApiModelProperty(value = "控制端口")
    private Integer  rtspPort;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "密码")
    private String pwd;

    @ApiModelProperty(value = "根目录")
    private String root;

    @ApiModelProperty(value = "最大通道数")
    private Integer  maxChannel;

    @ApiModelProperty(value = "缓存磁盘空间")
    private Integer  hddSize;

    @ApiModelProperty(value = "缓存天数")
    private Integer  bufferDay;

    @ApiModelProperty(value = "录制文件时长")
    private Integer  timeLong;
}
