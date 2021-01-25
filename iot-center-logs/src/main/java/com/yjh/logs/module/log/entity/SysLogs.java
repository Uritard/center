package com.yjh.logs.module.log.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

/**
 * @author tt
 * @since 2020-08-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysLogs对象", description = "审计日志表")
public class SysLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 50,message = "logId长度必须小于等于50")
    @ApiModelProperty(value = "主键UUID")
    @TableField(value = "log_id",updateStrategy = FieldStrategy.IGNORED)
    private String logId;

    @Length(max = 50,message = "logType长度必须小于等于50")
    @ApiModelProperty(value = "分类标志（主要是各个业务自定义编码，用来区分检索）")
    @TableField(value = "log_type",updateStrategy = FieldStrategy.IGNORED)
    private String logType;

    @Length(max = 50,message = "ip长度必须小于等于50")
    @ApiModelProperty(value = "请求者IP")
    @TableField(value = "ip",updateStrategy = FieldStrategy.IGNORED)
    private String ip;

    @Length(max = 256,message = "title长度必须小于等于256")
    @ApiModelProperty(value = "标题")
    @TableField(value = "title",updateStrategy = FieldStrategy.IGNORED)
    private String title;

    @Max(value=9)
    @ApiModelProperty(value = "日志状态，1正确日志，2错误日志")
    @TableField(value = "state",updateStrategy = FieldStrategy.IGNORED)
    private Integer state;

    @ApiModelProperty(value = "内容")
    @TableField(value = "content",updateStrategy = FieldStrategy.IGNORED)
    private String content;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "操作人ID")
    @TableField(value = "user_id",updateStrategy = FieldStrategy.IGNORED)
    private Long userId;

    @Length(max = 50,message = "userName长度必须小于等于50")
    @TableField(value = "user_name",updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "操作者名称")
    private String userName;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间",example = "2018-10-01 12:18:48")
    private Date createTime;


}
