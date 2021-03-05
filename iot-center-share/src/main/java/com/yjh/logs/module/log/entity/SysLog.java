package com.yjh.logs.module.log.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2021-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysLog对象", description = "审计日志表")
public class SysLog implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "主键ID")
    @TableId(value = "log_id", type = IdType.AUTO)
    @TableField(value = "log_id",updateStrategy = FieldStrategy.IGNORED)
    private Long logId;

    @ApiModelProperty(value = "操作类型(1-查询;2-新增;3-修改;4-删除;5-执行)")
    @TableField(value = "log_type",updateStrategy = FieldStrategy.IGNORED)
    private String logType;

    @ApiModelProperty(value = "请求者IP")
    @TableField(value = "ip",updateStrategy = FieldStrategy.IGNORED)
    private String ip;

    @ApiModelProperty(value = "标题")
    @TableField(value = "title",updateStrategy = FieldStrategy.IGNORED)
    private String title;

    @ApiModelProperty(value = "日志状态，1正确日志，2错误日志，3异常日志")
    @TableField(value = "state",updateStrategy = FieldStrategy.IGNORED)
    private Integer state;

    @ApiModelProperty(value = "内容")
    @TableField(value = "content",updateStrategy = FieldStrategy.IGNORED)
    private String content;

    @ApiModelProperty(value = "操作人ID")
    @TableField(value = "user_id",updateStrategy = FieldStrategy.IGNORED)
    private Long userId;

    @ApiModelProperty(value = "操作者名称")
    @TableField(value = "user_name",updateStrategy = FieldStrategy.IGNORED)
    private String userName;

    @ApiModelProperty(value = "请求源 http://localhost:18711")
    @TableField(value = "request_origin",updateStrategy = FieldStrategy.IGNORED)
    private String requestOrigin;

    @ApiModelProperty(value = "请求路径 /api/v1/sysOpLog/recordLog")
    @TableField(value = "request_path",updateStrategy = FieldStrategy.IGNORED)
    private String requestPath;

    @ApiModelProperty(value = "请求方式(1-GET;2-HEAD;3-POST;4-PUT;5-DELETE;6-CONNECT;7-OPTIONS;8-TRACE;9-PATCH)")
    @TableField(value = "request_method",updateStrategy = FieldStrategy.IGNORED)
    private String requestMethod;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "create_time",updateStrategy = FieldStrategy.IGNORED)
    private Date createTime;


}
