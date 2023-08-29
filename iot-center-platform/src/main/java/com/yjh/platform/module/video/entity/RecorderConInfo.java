package com.yjh.platform.module.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author yc
 * @since 2020-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RecorderConInfo对象", description = "录像机对象")
public class RecorderConInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "录像机ID")
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @ApiModelProperty(value = "服务器名称")
    private String recordName;

    @ApiModelProperty(value = "服务器地址")
    private String recordIp;

    @ApiModelProperty(value = "传输协议")
    private String protocol;

    @ApiModelProperty(value = "http端口")
    private Integer httpPort;

    @ApiModelProperty(value = "传输端口")
    private Integer transPort;

    @ApiModelProperty(value = "控制端口")
    private Integer rtspPort;

    @ApiModelProperty(value = "用户名")
    private String identityManager;

    @ApiModelProperty(value = "密码")
    private String identityCode;

    @ApiModelProperty(value = "最大通道数")
    private Integer maxChannel;

    @ApiModelProperty(value = "录像机类型")
    private String recorderType;

    @ApiModelProperty(value = "设备ID")
    private String deviceChannel;

}
