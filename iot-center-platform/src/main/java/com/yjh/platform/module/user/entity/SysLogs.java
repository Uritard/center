package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2020-07-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysLogs对象", description = "审计日志表")
public class SysLogs implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "主键UUID")
    @TableId(value = "log_id", type = IdType.AUTO)
    private String logId;

    @ApiModelProperty(value = "分类标志（主要是各个业务自定义编码，用来区分检索）")
    private String logType;

    @ApiModelProperty(value = "请求者IP")
    private String ip;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "日志状态，1正确日志，2错误日志")
    private Integer state;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "操作人ID")
    private Long userId;

    @ApiModelProperty(value = "操作者名称")
    private String userName;

    @ApiModelProperty(value = "更新时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


}
