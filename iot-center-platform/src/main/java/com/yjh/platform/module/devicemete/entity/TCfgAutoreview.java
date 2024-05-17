package com.yjh.platform.module.devicemete.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 自动审核配置表
 * </p>
 *
 * @author Chenfei
 * @since 2024-05-20
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TCfgAutoreview implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自动审核确认ID
     */
    @ApiModelProperty(value = "自动审核确认ID")
    @TableId(value = "autoreview_id", type = IdType.AUTO)
    private Long autoreviewId;

    @ApiModelProperty(value = "自动审核名称")
    private String autoreviewName;

    @ApiModelProperty(value = "自动审核类型，1-巡视结果 2-巡视告警，可多选，使用,分割")
    private String autoreviewType;

    @TableField(exist=false)
    @ApiModelProperty(value = "自动审核类型")
    private String[] autoreviewTypes;
    /**
     * 操作名称
     */
    @TableField(exist=false)
    @ApiModelProperty(value = "操作名称")
    private String autoreviewTypeName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @TableField(exist=false)
    @ApiModelProperty(value = "详细点位信息")
    private List<TCfgAutoreviewDetail> detail;
}
