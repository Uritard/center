package com.yjh.platform.module.devicemete.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 自动审核配置详细表
 * </p>
 *
 * @author Chenfei
 * @since 2024-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TCfgAutoreviewDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "detail_id", type = IdType.AUTO)
    @ApiModelProperty(value = "详细ID，主键")
    private Long detailId;

    @ApiModelProperty(value = "无需审核确认ID")
    private Long autoreviewId;

    @ApiModelProperty(value = "配置类型，1-缺陷 2-告警 3-设备类型 4-设备 5-测点 6-标签")
    private Integer autoDetailType;

    @ApiModelProperty(value = "配置类型名称")
    @TableField(exist=false)
    private String autoDetailTypeName;

    @ApiModelProperty(value = "关联id")
    private String refId;

    @ApiModelProperty(value = "关联名称")
    private String refName;


}
