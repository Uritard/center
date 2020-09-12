package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author YC
 * @date 2020/8/24 - 11:28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraRecorderByDict", description = "录像服务器表新增")
public class TCameraRecorderByDict {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "录像机ID")
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @ApiModelProperty(value = "服务器名称")
    private String recordName;

    @ApiModelProperty(value = "NVR类型")
    private String recorderType1;

    @ApiModelProperty(value = "录像机类型")
    private String recorderType;

    @ApiModelProperty(value = "备用名称")
    private String aliasName;

    @ApiModelProperty(value = "服务器地址")
    private String recordIp;

    @ApiModelProperty(value = "传输协议")
    private String protocol;

    @ApiModelProperty(value = "协议类型")
    private String protocolType;

    @ApiModelProperty(value = "http端口")
    private Integer httpPort;

    @ApiModelProperty(value = "传输端口")
    private Integer transPort;

    @ApiModelProperty(value = "控制端口")
    private Integer rtspPort;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "密码")
    private String pwd;

    @ApiModelProperty(value = "根目录")
    private String protocolUrl;

    @ApiModelProperty(value = "最大通道数")
    private Integer maxChannel;

    @ApiModelProperty(value = "缓存磁盘空间")
    private Integer hddSize;

    @ApiModelProperty(value = "缓存天数")
    private Integer bufferDay;

    @ApiModelProperty(value = "录制文件时长 单位秒")
    private Integer timeLong;


}
